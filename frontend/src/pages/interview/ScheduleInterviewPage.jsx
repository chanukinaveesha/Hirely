import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  cancelInterview,
  getInterviewsForApplication,
  listPanelMembers,
  proposeInterview,
  rescheduleInterview,
} from '../../api/interviewApi'
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
  toast,
} from '../../components/common'

const emptyForm = { scheduledAt: '', location: '', panelMemberIds: [] }

export default function ScheduleInterviewPage() {
  const { applicationId } = useParams()
  const { state } = useLocation()
  const navigate = useNavigate()

  const [interviews, setInterviews] = useState([])
  const [panelMembers, setPanelMembers] = useState([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [actioningId, setActioningId] = useState(null)
  const [rescheduleTarget, setRescheduleTarget] = useState(null)
  const [rescheduleAt, setRescheduleAt] = useState('')

  function loadInterviews() {
    return getInterviewsForApplication(applicationId)
      .then(setInterviews)
      .catch((err) => toast.error(err.message))
  }

  useEffect(() => {
    setLoading(true)
    Promise.all([loadInterviews(), listPanelMembers().then(setPanelMembers).catch((err) => toast.error(err.message))])
      .finally(() => setLoading(false))
  }, [applicationId])

  function togglePanelMember(id) {
    setForm((prev) => ({
      ...prev,
      panelMemberIds: prev.panelMemberIds.includes(id)
        ? prev.panelMemberIds.filter((existing) => existing !== id)
        : [...prev.panelMemberIds, id],
    }))
  }

  async function handlePropose(event) {
    event.preventDefault()
    setError(null)
    if (form.panelMemberIds.length === 0) {
      setError('Select at least one panel member.')
      return
    }
    setSubmitting(true)
    try {
      await proposeInterview({
        applicationId: Number(applicationId),
        scheduledAt: form.scheduledAt,
        location: form.location,
        panelMemberIds: form.panelMemberIds,
      })
      toast.success('Interview proposed and invitations sent.')
      setForm(emptyForm)
      await loadInterviews()
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  async function submitReschedule() {
    if (!rescheduleAt) return
    setActioningId(rescheduleTarget)
    try {
      await rescheduleInterview(rescheduleTarget, { scheduledAt: rescheduleAt })
      toast.success('Interview rescheduled.')
      setRescheduleTarget(null)
      setRescheduleAt('')
      await loadInterviews()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActioningId(null)
    }
  }

  async function handleCancel(interviewId) {
    setActioningId(interviewId)
    try {
      await cancelInterview(interviewId)
      toast.success('Interview cancelled.')
      await loadInterviews()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActioningId(null)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <Card>
        <CardHeader
          title={state?.candidateName ? `Schedule interview — ${state.candidateName}` : 'Schedule interview'}
          description={state?.vacancyTitle ?? `Application #${applicationId}`}
          action={
            <Button variant="ghost" size="sm" onClick={() => navigate(-1)}>
              Back
            </Button>
          }
        />

        <form onSubmit={handlePropose} className="flex flex-col gap-4">
          <ErrorMessage message={error} />

          <div className="grid grid-cols-2 gap-4">
            <FormField label="Date & time" htmlFor="scheduledAt" required>
              <Input
                id="scheduledAt"
                type="datetime-local"
                required
                value={form.scheduledAt}
                onChange={(e) => setForm({ ...form, scheduledAt: e.target.value })}
              />
            </FormField>
            <FormField label="Location" htmlFor="location" helperText="Room or meeting link">
              <Input id="location" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
            </FormField>
          </div>

          <FormField label="Panel members" required>
            {panelMembers.length === 0 ? (
              <p className="text-small text-ink-muted">No panel members registered yet.</p>
            ) : (
              <div className="flex flex-col gap-2 rounded-sm border border-line p-3">
                {panelMembers.map((member) => (
                  <label key={member.id} className="flex items-center gap-2 text-body text-ink-primary">
                    <input
                      type="checkbox"
                      checked={form.panelMemberIds.includes(member.id)}
                      onChange={() => togglePanelMember(member.id)}
                    />
                    {member.name} <span className="text-small text-ink-muted">({member.email})</span>
                  </label>
                ))}
              </div>
            )}
          </FormField>

          <div>
            <Button type="submit" loading={submitting}>
              Propose interview
            </Button>
          </div>
        </form>
      </Card>

      <Card>
        <CardHeader title="Scheduled interviews" description="History of interview slots for this application." />

        {loading ? (
          <LoadingSpinner label="Loading interviews..." />
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>When</TableHeaderCell>
                <TableHeaderCell>Location</TableHeaderCell>
                <TableHeaderCell>Panel</TableHeaderCell>
                <TableHeaderCell>Status</TableHeaderCell>
                <TableHeaderCell>Feedback</TableHeaderCell>
                <TableHeaderCell>Actions</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {interviews.length === 0 && <TableEmpty colSpan={6} message="No interviews scheduled yet." />}
              {interviews.map((interview) => (
                <TableRow key={interview.id}>
                  <TableCell>{new Date(interview.scheduledAt).toLocaleString()}</TableCell>
                  <TableCell>{interview.location || '—'}</TableCell>
                  <TableCell>{interview.panelMembers.map((member) => member.name).join(', ')}</TableCell>
                  <TableCell>
                    <InterviewStatusBadge status={interview.status} />
                  </TableCell>
                  <TableCell>
                    {interview.feedbackSubmittedCount}/{interview.panelMemberCount}
                  </TableCell>
                  <TableCell>
                    {interview.status !== 'CANCELLED' && (
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant="secondary"
                          loading={actioningId === interview.id}
                          onClick={() => {
                            setRescheduleTarget(interview.id)
                            setRescheduleAt('')
                          }}
                        >
                          Reschedule
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          loading={actioningId === interview.id}
                          onClick={() => handleCancel(interview.id)}
                        >
                          Cancel
                        </Button>
                      </div>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>

      <Modal
        isOpen={rescheduleTarget !== null}
        onClose={() => setRescheduleTarget(null)}
        title="Reschedule interview"
        footer={
          <>
            <Button variant="ghost" onClick={() => setRescheduleTarget(null)}>
              Cancel
            </Button>
            <Button loading={actioningId === rescheduleTarget} onClick={submitReschedule}>
              Confirm
            </Button>
          </>
        }
      >
        <FormField label="New date & time" htmlFor="rescheduleAt">
          <Input
            id="rescheduleAt"
            type="datetime-local"
            value={rescheduleAt}
            onChange={(e) => setRescheduleAt(e.target.value)}
          />
        </FormField>
      </Modal>
    </div>
  )
}
