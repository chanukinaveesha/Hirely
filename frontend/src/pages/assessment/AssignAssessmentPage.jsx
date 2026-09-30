import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  assignAssessment,
  evaluateCandidateAssessment,
  getCandidateAssessmentsForApplication,
  getMyAssessments,
} from '../../api/assessmentApi'
import CandidateAssessmentStatusBadge from '../../components/assessment/CandidateAssessmentStatusBadge'
import { formatEnumLabel } from '../../utils/vacancyOptions'
import {
  Button,
  Card,
  CardHeader,
  ErrorMessage,
  FormField,
  Input,
  LoadingSpinner,
  Modal,
  Select,
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeaderCell,
  TableRow,
  TextArea,
  toast,
} from '../../components/common'

export default function AssignAssessmentPage() {
  const { applicationId } = useParams()
  const { state } = useLocation()
  const navigate = useNavigate()

  const [assignments, setAssignments] = useState([])
  const [assessments, setAssessments] = useState([])
  const [loading, setLoading] = useState(true)
  const [assessmentId, setAssessmentId] = useState('')
  const [deadline, setDeadline] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [evaluateTarget, setEvaluateTarget] = useState(null)
  const [score, setScore] = useState('')
  const [feedback, setFeedback] = useState('')
  const [evaluateError, setEvaluateError] = useState(null)
  const [evaluating, setEvaluating] = useState(false)

  function loadAssignments() {
    return getCandidateAssessmentsForApplication(applicationId)
      .then(setAssignments)
      .catch((err) => toast.error(err.message))
  }

  useEffect(() => {
    setLoading(true)
    Promise.all([
      loadAssignments(),
      getMyAssessments()
        .then((all) => setAssessments(all.filter((a) => !a.archived)))
        .catch((err) => toast.error(err.message)),
    ]).finally(() => setLoading(false))
  }, [applicationId])

  async function handleAssign(event) {
    event.preventDefault()
    setError(null)
    if (!assessmentId) {
      setError('Select an assessment.')
      return
    }
    setSubmitting(true)
    try {
      await assignAssessment(assessmentId, { applicationIds: [Number(applicationId)], deadline })
      toast.success('Assessment assigned and candidate notified.')
      setAssessmentId('')
      setDeadline('')
      await loadAssignments()
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  async function submitEvaluation() {
    setEvaluateError(null)
    const numericScore = Number(score)
    if (!score || numericScore < 0 || numericScore > 100) {
      setEvaluateError('Score must be between 0 and 100.')
      return
    }
    setEvaluating(true)
    try {
      await evaluateCandidateAssessment(evaluateTarget, { score: numericScore, feedback })
      toast.success('Assessment evaluated.')
      setEvaluateTarget(null)
      setScore('')
      setFeedback('')
      await loadAssignments()
    } catch (err) {
      setEvaluateError(err.message)
    } finally {
      setEvaluating(false)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <Card>
        <CardHeader
          title={state?.candidateName ? `Assign assessment — ${state.candidateName}` : 'Assign assessment'}
          description={state?.vacancyTitle ?? `Application #${applicationId}`}
          action={
            <Button variant="ghost" size="sm" onClick={() => navigate(-1)}>
              Back
            </Button>
          }
        />

        <form onSubmit={handleAssign} className="flex flex-col gap-4">
          <ErrorMessage message={error} />

          <div className="grid grid-cols-2 gap-4">
            <FormField label="Assessment" htmlFor="assessmentId">
              <Select id="assessmentId" value={assessmentId} onChange={(e) => setAssessmentId(e.target.value)}>
                <option value="">Select an assessment</option>
                {assessments.map((assessment) => (
                  <option key={assessment.id} value={assessment.id}>
                    {assessment.title} ({formatEnumLabel(assessment.type)})
                  </option>
                ))}
              </Select>
            </FormField>

            <FormField label="Submission deadline" htmlFor="deadline">
              <Input
                id="deadline"
                type="datetime-local"
                required
                value={deadline}
                onChange={(e) => setDeadline(e.target.value)}
              />
            </FormField>
          </div>

          <div>
            <Button type="submit" loading={submitting}>
              Assign
            </Button>
          </div>
        </form>
      </Card>

      <Card>
        <CardHeader title="Assigned assessments" description="History of tests assigned for this application." />

        {loading ? (
          <LoadingSpinner label="Loading assignments..." />
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Assessment</TableHeaderCell>
                <TableHeaderCell>Deadline</TableHeaderCell>
                <TableHeaderCell>Status</TableHeaderCell>
                <TableHeaderCell>Score</TableHeaderCell>
                <TableHeaderCell>Actions</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {assignments.length === 0 && <TableEmpty colSpan={5} message="No assessments assigned yet." />}
              {assignments.map((assignment) => (
                <TableRow key={assignment.id}>
                  <TableCell>
                    <p className="text-ink-primary">{assignment.assessmentTitle}</p>
                    <p className="text-small text-ink-muted">{formatEnumLabel(assignment.assessmentType)}</p>
                  </TableCell>
                  <TableCell>{new Date(assignment.deadline).toLocaleString()}</TableCell>
                  <TableCell>
                    <CandidateAssessmentStatusBadge status={assignment.status} />
                  </TableCell>
                  <TableCell>{assignment.score ?? '—'}</TableCell>
                  <TableCell>
                    {assignment.status === 'SUBMITTED' && (
                      <Button
                        size="sm"
                        onClick={() => {
                          setEvaluateTarget(assignment.id)
                          setScore('')
                          setFeedback('')
                          setEvaluateError(null)
                        }}
                      >
                        Evaluate
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>

      <Modal
        isOpen={evaluateTarget !== null}
        onClose={() => setEvaluateTarget(null)}
        title="Evaluate submission"
        footer={
          <>
            <Button variant="ghost" onClick={() => setEvaluateTarget(null)}>
              Cancel
            </Button>
            <Button loading={evaluating} onClick={submitEvaluation}>
              Submit
            </Button>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          <ErrorMessage message={evaluateError} />
          <p className="whitespace-pre-wrap rounded-sm border border-line bg-surface p-3 text-small text-ink-secondary">
            {assignments.find((a) => a.id === evaluateTarget)?.submissionText}
          </p>
          <FormField label="Score (0-100)" htmlFor="score" required>
            <Input id="score" type="number" min={0} max={100} value={score} onChange={(e) => setScore(e.target.value)} />
          </FormField>
          <FormField label="Feedback" htmlFor="feedback" helperText="Optional">
            <TextArea id="feedback" rows={3} value={feedback} onChange={(e) => setFeedback(e.target.value)} />
          </FormField>
        </div>
      </Modal>
    </div>
  )
}
