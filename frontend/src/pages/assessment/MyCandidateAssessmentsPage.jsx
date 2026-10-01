import { useEffect, useState } from 'react'
import { getMyCandidateAssessments, submitCandidateAssessment } from '../../api/assessmentApi'
import CandidateAssessmentStatusBadge from '../../components/assessment/CandidateAssessmentStatusBadge'
import { formatEnumLabel } from '../../utils/vacancyOptions'
import {
  Button,
  Card,
  CardHeader,
  ErrorMessage,
  FormField,
  LoadingSpinner,
  Modal,
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

export default function MyCandidateAssessmentsPage() {
  const [assessments, setAssessments] = useState([])
  const [loading, setLoading] = useState(true)
  const [takeTarget, setTakeTarget] = useState(null)
  const [submissionText, setSubmissionText] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  function loadAssessments() {
    setLoading(true)
    return getMyCandidateAssessments()
      .then(setAssessments)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadAssessments()
  }, [])

  async function handleSubmit() {
    setError(null)
    if (!submissionText.trim()) {
      setError('Enter your answer before submitting.')
      return
    }
    setSubmitting(true)
    try {
      await submitCandidateAssessment(takeTarget.id, { submissionText })
      toast.success('Assessment submitted.')
      setTakeTarget(null)
      setSubmissionText('')
      await loadAssessments()
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card>
      <CardHeader title="My assessments" description="Complete assigned tests before their deadline." />

      {loading ? (
        <LoadingSpinner label="Loading assessments..." />
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableHeaderCell>Vacancy</TableHeaderCell>
              <TableHeaderCell>Assessment</TableHeaderCell>
              <TableHeaderCell>Deadline</TableHeaderCell>
              <TableHeaderCell>Status</TableHeaderCell>
              <TableHeaderCell>Score</TableHeaderCell>
              <TableHeaderCell>Actions</TableHeaderCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {assessments.length === 0 && <TableEmpty colSpan={6} message="No assessments assigned yet." />}
            {assessments.map((assessment) => (
              <TableRow key={assessment.id}>
                <TableCell>{assessment.vacancyTitle}</TableCell>
                <TableCell>
                  <p className="text-ink-primary">{assessment.assessmentTitle}</p>
                  <p className="text-small text-ink-muted">{formatEnumLabel(assessment.assessmentType)}</p>
                </TableCell>
                <TableCell>{new Date(assessment.deadline).toLocaleString()}</TableCell>
                <TableCell>
                  <CandidateAssessmentStatusBadge status={assessment.status} />
                </TableCell>
                <TableCell>{assessment.score ?? '—'}</TableCell>
                <TableCell>
                  {assessment.status === 'ASSIGNED' && (
                    <Button
                      size="sm"
                      onClick={() => {
                        setTakeTarget(assessment)
                        setSubmissionText('')
                        setError(null)
                      }}
                    >
                      Take test
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      <Modal
        isOpen={takeTarget !== null}
        onClose={() => setTakeTarget(null)}
        title={takeTarget?.assessmentTitle}
        size="lg"
        footer={
          <>
            <Button variant="ghost" onClick={() => setTakeTarget(null)}>
              Cancel
            </Button>
            <Button loading={submitting} onClick={handleSubmit}>
              Submit
            </Button>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          <ErrorMessage message={error} />
          <p className="whitespace-pre-wrap rounded-sm border border-line bg-surface p-3 text-small text-ink-secondary">
            {takeTarget?.assessmentDescription}
          </p>
          <FormField label="Answer" htmlFor="submissionText">
            <TextArea
              id="submissionText"
              rows={8}
              value={submissionText}
              onChange={(e) => setSubmissionText(e.target.value)}
            />
          </FormField>
        </div>
      </Modal>
    </Card>
  )
}
