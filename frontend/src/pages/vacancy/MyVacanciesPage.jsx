import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { closeVacancy, getMyVacancies, publishVacancy } from '../../api/vacancyApi'
import VacancyStatusBadge from '../../components/vacancy/VacancyStatusBadge'
import {
  Button,
  Card,
  CardHeader,
  LoadingSpinner,
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeaderCell,
  TableRow,
  toast,
} from '../../components/common'

export default function MyVacanciesPage() {
  const [vacancies, setVacancies] = useState([])
  const [loading, setLoading] = useState(true)
  const [actioningId, setActioningId] = useState(null)

  function loadVacancies() {
    setLoading(true)
    return getMyVacancies()
      .then(setVacancies)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadVacancies()
  }, [])

  async function handlePublish(id) {
    setActioningId(id)
    try {
      await publishVacancy(id)
      toast.success('Vacancy published.')
      await loadVacancies()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActioningId(null)
    }
  }

  async function handleClose(id) {
    setActioningId(id)
    try {
      await closeVacancy(id)
      toast.success('Vacancy closed.')
      await loadVacancies()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActioningId(null)
    }
  }

  return (
    <Card>
      <CardHeader
        title="My vacancies"
        description="Postings you've created."
        action={
          <Link to="/recruiter/vacancies/new">
            <Button size="sm">New vacancy</Button>
          </Link>
        }
      />

      {loading ? (
        <LoadingSpinner label="Loading vacancies..." />
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableHeaderCell>Title</TableHeaderCell>
              <TableHeaderCell>Category</TableHeaderCell>
              <TableHeaderCell>Deadline</TableHeaderCell>
              <TableHeaderCell>Status</TableHeaderCell>
              <TableHeaderCell>Actions</TableHeaderCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {vacancies.length === 0 && <TableEmpty colSpan={5} message="You haven't posted any vacancies yet." />}
            {vacancies.map((vacancy) => (
              <TableRow key={vacancy.id}>
                <TableCell>
                  <Link to={`/vacancies/${vacancy.id}`} className="text-secondary hover:text-secondary-hover">
                    {vacancy.title}
                  </Link>
                </TableCell>
                <TableCell>{vacancy.category.replaceAll('_', ' ')}</TableCell>
                <TableCell>{vacancy.deadline}</TableCell>
                <TableCell>
                  <VacancyStatusBadge status={vacancy.status} />
                </TableCell>
                <TableCell>
                  <div className="flex gap-2">
                    {vacancy.status !== 'CLOSED' && (
                      <Link to={`/recruiter/vacancies/${vacancy.id}/edit`}>
                        <Button size="sm" variant="ghost">
                          Edit
                        </Button>
                      </Link>
                    )}
                    {vacancy.status === 'DRAFT' && (
                      <Button
                        size="sm"
                        variant="secondary"
                        loading={actioningId === vacancy.id}
                        onClick={() => handlePublish(vacancy.id)}
                      >
                        Publish
                      </Button>
                    )}
                    {vacancy.status !== 'CLOSED' && (
                      <Button
                        size="sm"
                        variant="destructive"
                        loading={actioningId === vacancy.id}
                        onClick={() => handleClose(vacancy.id)}
                      >
                        Close
                      </Button>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Card>
  )
}
