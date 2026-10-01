import { useEffect, useState } from 'react'
import { archiveAssessment, createAssessment, getMyAssessments } from '../../api/assessmentApi'
import { ASSESSMENT_TYPES } from '../../utils/assessmentOptions'
import { formatEnumLabel } from '../../utils/vacancyOptions'
import {
  Badge,
  Button,
  Card,
  CardHeader,
  ErrorMessage,
  FormField,
  Input,
  LoadingSpinner,
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

const emptyForm = { title: '', description: '', type: ASSESSMENT_TYPES[0] }

export default function MyAssessmentsPage() {
  const [assessments, setAssessments] = useState([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [archivingId, setArchivingId] = useState(null)

  function loadAssessments() {
    setLoading(true)
    return getMyAssessments()
      .then(setAssessments)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadAssessments()
  }, [])

  async function handleCreate(event) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await createAssessment(form)
      toast.success('Assessment created.')
      setForm(emptyForm)
      await loadAssessments()
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleArchive(id) {
    setArchivingId(id)
    try {
      await archiveAssessment(id)
      toast.success('Assessment archived.')
      await loadAssessments()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setArchivingId(null)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <Card>
        <CardHeader title="Create assessment" description="Build a reusable aptitude or technical test." />

        <form onSubmit={handleCreate} className="flex flex-col gap-4">
          <ErrorMessage message={error} />

          <FormField label="Title" htmlFor="title">
            <Input id="title" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
          </FormField>

          <FormField label="Description / instructions" htmlFor="description">
            <TextArea
              id="description"
              required
              rows={4}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </FormField>

          <FormField label="Type" htmlFor="type">
            <Select id="type" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
              {ASSESSMENT_TYPES.map((type) => (
                <option key={type} value={type}>
                  {formatEnumLabel(type)}
                </option>
              ))}
            </Select>
          </FormField>

          <div>
            <Button type="submit" loading={submitting}>
              Create assessment
            </Button>
          </div>
        </form>
      </Card>

      <Card>
        <CardHeader title="My assessments" description="Reuse these when assigning to shortlisted candidates." />

        {loading ? (
          <LoadingSpinner label="Loading assessments..." />
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Title</TableHeaderCell>
                <TableHeaderCell>Type</TableHeaderCell>
                <TableHeaderCell>Created</TableHeaderCell>
                <TableHeaderCell>Status</TableHeaderCell>
                <TableHeaderCell>Actions</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {assessments.length === 0 && <TableEmpty colSpan={5} message="You haven't created any assessments yet." />}
              {assessments.map((assessment) => (
                <TableRow key={assessment.id}>
                  <TableCell>
                    <p className="text-ink-primary">{assessment.title}</p>
                    <p className="text-small text-ink-muted">{assessment.description}</p>
                  </TableCell>
                  <TableCell>{formatEnumLabel(assessment.type)}</TableCell>
                  <TableCell>{new Date(assessment.createdAt).toLocaleDateString()}</TableCell>
                  <TableCell>
                    <Badge variant={assessment.archived ? 'neutral' : 'success'}>
                      {assessment.archived ? 'Archived' : 'Active'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {!assessment.archived && (
                      <Button
                        size="sm"
                        variant="destructive"
                        loading={archivingId === assessment.id}
                        onClick={() => handleArchive(assessment.id)}
                      >
                        Archive
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>
    </div>
  )
}
