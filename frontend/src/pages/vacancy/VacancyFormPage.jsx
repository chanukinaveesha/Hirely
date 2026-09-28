import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createVacancy, getVacancy, updateVacancy } from '../../api/vacancyApi'
import { VACANCY_CATEGORIES, formatEnumLabel } from '../../utils/vacancyOptions'
import { Button, Card, ErrorMessage, FormField, Input, LoadingSpinner, Select, TextArea, toast } from '../../components/common'

const emptyForm = { title: '', description: '', requirements: '', category: VACANCY_CATEGORIES[0], location: '', deadline: '' }

export default function VacancyFormPage() {
  const { id } = useParams()
  const isEditing = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState(emptyForm)
  const [loading, setLoading] = useState(isEditing)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!isEditing) return

    getVacancy(id)
      .then((vacancy) => {
        setForm({
          title: vacancy.title,
          description: vacancy.description,
          requirements: vacancy.requirements,
          category: vacancy.category,
          location: vacancy.location ?? '',
          deadline: vacancy.deadline,
        })
      })
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }, [id, isEditing])

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      if (isEditing) {
        await updateVacancy(id, form)
        toast.success('Vacancy updated.')
      } else {
        await createVacancy(form)
        toast.success('Vacancy created as a draft.')
      }
      navigate('/recruiter/vacancies')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <LoadingSpinner label="Loading vacancy..." />

  return (
    <Card padding="lg" className="mx-auto max-w-2xl">
      <h1 className="text-h2 mb-1 text-ink-primary">{isEditing ? 'Edit vacancy' : 'Create vacancy'}</h1>
      <p className="mb-6 text-small text-ink-secondary">
        {isEditing ? 'Update the details below.' : 'This is saved as a draft — publish it when ready.'}
      </p>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <ErrorMessage message={error} />

        <FormField label="Title" htmlFor="title">
          <Input id="title" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </FormField>

        <FormField label="Description" htmlFor="description">
          <TextArea
            id="description"
            required
            rows={5}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </FormField>

        <FormField label="Requirements" htmlFor="requirements">
          <TextArea
            id="requirements"
            required
            rows={4}
            value={form.requirements}
            onChange={(e) => setForm({ ...form, requirements: e.target.value })}
          />
        </FormField>

        <div className="grid grid-cols-2 gap-4">
          <FormField label="Category" htmlFor="category">
            <Select id="category" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
              {VACANCY_CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {formatEnumLabel(category)}
                </option>
              ))}
            </Select>
          </FormField>

          <FormField label="Application deadline" htmlFor="deadline">
            <Input
              id="deadline"
              type="date"
              required
              value={form.deadline}
              onChange={(e) => setForm({ ...form, deadline: e.target.value })}
            />
          </FormField>
        </div>

        <FormField label="Location" htmlFor="location" helperText="Optional">
          <Input id="location" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
        </FormField>

        <div className="flex gap-3">
          <Button type="submit" loading={submitting}>
            {isEditing ? 'Save changes' : 'Create draft'}
          </Button>
          <Button type="button" variant="ghost" onClick={() => navigate('/recruiter/vacancies')}>
            Cancel
          </Button>
        </div>
      </form>
    </Card>
  )
}
