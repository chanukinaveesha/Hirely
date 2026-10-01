import { useEffect, useState } from 'react'
import { acceptInterview, getMyInterviews, requestInterviewReschedule } from '../../api/interviewApi'
import { ACCEPTABLE_INTERVIEW_STATUSES } from '../../utils/interviewOptions'
import InterviewStatusBadge from '../../components/interview/InterviewStatusBadge'
import {
  Button,
  Card,
  CardHeader,
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

export default function MyInterviewsPage() {
  const [interviews, setInterviews] = useState([])
  const [loading, setLoading] = useState(true)
  const [actioningId, setActioningId] = useState(null)
  const [rescheduleTarget, setRescheduleTarget] = useState(null)
  const [preferredAt, setPreferredAt] = useState('')
  const [reason, setReason] = useState('')

  function loadInterviews() {
    setLoading(true)
    return getMyInterviews()
      .then(setInterviews)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadInterviews()
  }, [])

  async function handleAccept(interviewId) {
    setActioningId(interviewId)
    try {
      await acceptInterview(interviewId)
      toast.success('Interview confirmed.')
      await loadInterviews()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActioningId(null)
    }
  }

  async function submitRescheduleRequest() {
    if (!preferredAt) return
    setActioningId(rescheduleTarget)
    try {
      await requestInterviewReschedule(rescheduleTarget, { preferredAt, reason })
      toast.success('Reschedule request sent.')
      setRescheduleTarget(null)
      setPreferredAt('')
      setReason('')
      await loadInterviews()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActioningId(null)
    }
  }

  return (
    <Card>
      <CardHeader title="My interviews" description="Confirm or request a new time for your scheduled interviews." />

      {loading ? (
        <LoadingSpinner label="Loading interviews..." />
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableHeaderCell>Vacancy</TableHeaderCell>
              <TableHeaderCell>When</TableHeaderCell>
              <TableHeaderCell>Location</TableHeaderCell>
              <TableHeaderCell>Status</TableHeaderCell>
              <TableHeaderCell>Actions</TableHeaderCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {interviews.length === 0 && <TableEmpty colSpan={5} message="No interviews scheduled yet." />}
            {interviews.map((interview) => (
              <TableRow key={interview.id}>
                <TableCell>{interview.vacancyTitle}</TableCell>
                <TableCell>{new Date(interview.scheduledAt).toLocaleString()}</TableCell>
                <TableCell>{interview.location || '—'}</TableCell>
                <TableCell>
                  <InterviewStatusBadge status={interview.status} />
                </TableCell>
                <TableCell>
                  {ACCEPTABLE_INTERVIEW_STATUSES.includes(interview.status) && (
                    <div className="flex gap-2">
                      <Button size="sm" loading={actioningId === interview.id} onClick={() => handleAccept(interview.id)}>
                        Accept
                      </Button>
                      <Button
                        size="sm"
                        variant="secondary"
                        loading={actioningId === interview.id}
                        onClick={() => {
                          setRescheduleTarget(interview.id)
                          setPreferredAt('')
                          setReason('')
                        }}
                      >
                        Request reschedule
                      </Button>
                    </div>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      <Modal
        isOpen={rescheduleTarget !== null}
        onClose={() => setRescheduleTarget(null)}
        title="Request a new time"
        footer={
          <>
            <Button variant="ghost" onClick={() => setRescheduleTarget(null)}>
              Cancel
            </Button>
            <Button loading={actioningId === rescheduleTarget} onClick={submitRescheduleRequest}>
              Send request
            </Button>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          <FormField label="Preferred date & time" htmlFor="preferredAt">
            <Input
              id="preferredAt"
              type="datetime-local"
              value={preferredAt}
              onChange={(e) => setPreferredAt(e.target.value)}
            />
          </FormField>
          <FormField label="Reason" htmlFor="reason" helperText="Optional">
            <TextArea id="reason" rows={3} value={reason} onChange={(e) => setReason(e.target.value)} />
          </FormField>
        </div>
      </Modal>
    </Card>
  )
}
