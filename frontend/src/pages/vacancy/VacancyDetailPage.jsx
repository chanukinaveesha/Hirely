import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getVacancy } from '../../api/vacancyApi'
import { useAuthStore } from '../../auth/authStore'
import VacancyStatusBadge from '../../components/vacancy/VacancyStatusBadge'
import { Button, Card, LoadingSpinner, toast } from '../../components/common'

export default function VacancyDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const currentUserId = useAuthStore((state) => state.user?.userId)

  const [vacancy, setVacancy] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getVacancy(id)
      .then(setVacancy)
      .catch((err) => {
        toast.error(err.message)
        navigate(-1)
      })
      .finally(() => setLoading(false))
  }, [id, navigate])

  if (loading) return <LoadingSpinner label="Loading vacancy..." />
  if (!vacancy) return null

  const isOwner = vacancy.postedByUserId === currentUserId

  return (
    <Card padding="lg" className="mx-auto max-w-3xl">
      <div className="mb-4 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-h2 text-ink-primary">{vacancy.title}</h1>
          <p className="mt-1 text-small text-ink-secondary">
            {vacancy.clientCompanyName} · {vacancy.location || 'Location not specified'}
          </p>
        </div>
        <VacancyStatusBadge status={vacancy.status} />
      </div>

      <dl className="mb-6 grid grid-cols-2 gap-4 text-small">
        <div>
          <dt className="text-label text-ink-muted">Category</dt>
          <dd className="text-ink-primary">{vacancy.category.replaceAll('_', ' ')}</dd>
        </div>
        <div>
          <dt className="text-label text-ink-muted">Application deadline</dt>
          <dd className="text-ink-primary">{vacancy.deadline}</dd>
        </div>
        <div>
          <dt className="text-label text-ink-muted">Posted by</dt>
          <dd className="text-ink-primary">{vacancy.postedByName}</dd>
        </div>
      </dl>

      <div className="mb-6">
        <h2 className="text-h4 mb-2 text-ink-primary">Description</h2>
        <p className="whitespace-pre-line text-body text-ink-secondary">{vacancy.description}</p>
      </div>

      <div className="mb-6">
        <h2 className="text-h4 mb-2 text-ink-primary">Requirements</h2>
        <p className="whitespace-pre-line text-body text-ink-secondary">{vacancy.requirements}</p>
      </div>

      {isOwner && (
        <Link to={`/recruiter/vacancies/${vacancy.id}/edit`}>
          <Button variant="secondary">Edit this vacancy</Button>
        </Link>
      )}
    </Card>
  )
}
