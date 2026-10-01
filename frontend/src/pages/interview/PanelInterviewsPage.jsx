import { useEffect, useState } from 'react'
import { getMyPanelInterviews, submitInterviewFeedback } from '../../api/interviewApi'
import InterviewStatusBadge from '../../components/interview/InterviewStatusBadge'
import {
  Button,
  Card,
  CardHeader,
  ErrorMessage,
  FormField,
  Input,
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

export default function PanelInterviewsPage() {
  const [interviews, setInterviews] = useState([])
  const [loading, setLoading] = useState(true)
  const [feedbackTarget, setFeedbackTarget] = useState(null)
  const [score, setScore] = useState('')
  const [comments, setComments] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  function loadInterviews() {
    setLoading(true)
    return getMyPanelInterviews()
      .then(setInterviews)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadInterviews()
  }, [])

  async function submitFeedback() {
    setError(null)
    const numericScore = Number(score)
    if (!score || numericScore < 1 || numericScore > 10) {
      setError('Score must be between 1 and 10.')
      return
    }
    setSubmitting(true)
    try {
      await submitInterviewFeedback(feedbackTarget, { score: numericScore, comments })
      toast.success('Feedback submitted.')
      setFeedbackTarget(null)
      setScore('')
      setComments('')
      await loadInterviews()
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card>
      <CardHeader title="My panel interviews" description="Interviews you've been assigned to and their feedback." />

      {loading ? (
        <LoadingSpinner label="Loading interviews..." />
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableHeaderCell>Vacancy</TableHeaderCell>
              <TableHeaderCell>Candidate</TableHeaderCell>
              <TableHeaderCell>When</TableHeaderCell>
              <TableHeaderCell>Status</TableHeaderCell>
              <TableHeaderCell>Feedback</TableHeaderCell>
              <TableHeaderCell>Actions</TableHeaderCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {interviews.length === 0 && <TableEmpty colSpan={6} message="You have no assigned interviews yet." />}
            {interviews.map((interview) => (
              <TableRow key={interview.id}>
                <TableCell>{interview.vacancyTitle}</TableCell>
                <TableCell>{interview.candidateName}</TableCell>
                <TableCell>{new Date(interview.scheduledAt).toLocaleString()}</TableCell>
                <TableCell>
                  <InterviewStatusBadge status={interview.status} />
                </TableCell>
                <TableCell>
                  {interview.feedbackSubmittedCount}/{interview.panelMemberCount}
                </TableCell>
                <TableCell>
                  {interview.status === 'CONFIRMED' && (
                    <Button
                      size="sm"
                      onClick={() => {
                        setFeedbackTarget(interview.id)
                        setScore('')
                        setComments('')
                        setError(null)
                      }}
                    >
                      Submit feedback
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      <Modal
        isOpen={feedbackTarget !== null}
        onClose={() => setFeedbackTarget(null)}
        title="Interview feedback"
        footer={
          <>
            <Button variant="ghost" onClick={() => setFeedbackTarget(null)}>
              Cancel
            </Button>
            <Button loading={submitting} onClick={submitFeedback}>
              Submit
            </Button>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          <ErrorMessage message={error} />
          <FormField label="Score (1-10)" htmlFor="score" required>
            <Input id="score" type="number" min={1} max={10} value={score} onChange={(e) => setScore(e.target.value)} />
          </FormField>
          <FormField label="Comments" htmlFor="comments" helperText="Optional">
            <TextArea id="comments" rows={4} value={comments} onChange={(e) => setComments(e.target.value)} />
          </FormField>
        </div>
      </Modal>
    </Card>
  )
}
