import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyApplications, withdrawApplication } from '../../api/applicationApi'
import { WITHDRAWABLE_STATUSES } from '../../utils/applicationOptions'
import ApplicationStatusBadge from '../../components/application/ApplicationStatusBadge'
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

export default function MyApplicationsPage() {
  const [applications, setApplications] = useState([])
  const [loading, setLoading] = useState(true)
  const [withdrawingId, setWithdrawingId] = useState(null)

  function loadApplications() {
    setLoading(true)
    return getMyApplications()
      .then(setApplications)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadApplications()
  }, [])

  async function handleWithdraw(id) {
    setWithdrawingId(id)
    try {
      await withdrawApplication(id)
      toast.success('Application withdrawn.')
      await loadApplications()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setWithdrawingId(null)
    }
  }

  return (
    <Card>
      <CardHeader title="My applications" description="Track the status of the jobs you've applied to." />

      {loading ? (
        <LoadingSpinner label="Loading applications..." />
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableHeaderCell>Vacancy</TableHeaderCell>
              <TableHeaderCell>Company</TableHeaderCell>
              <TableHeaderCell>Applied</TableHeaderCell>
              <TableHeaderCell>Status</TableHeaderCell>
              <TableHeaderCell>Actions</TableHeaderCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {applications.length === 0 && <TableEmpty colSpan={5} message="You haven't applied to any vacancies yet." />}
            {applications.map((application) => (
              <TableRow key={application.id}>
                <TableCell>
                  <Link to={`/vacancies/${application.vacancyId}`} className="text-secondary hover:text-secondary-hover">
                    {application.vacancyTitle}
                  </Link>
                </TableCell>
                <TableCell>{application.clientCompanyName}</TableCell>
                <TableCell>{new Date(application.appliedAt).toLocaleDateString()}</TableCell>
                <TableCell>
                  <ApplicationStatusBadge status={application.status} />
                </TableCell>
                <TableCell>
                  {WITHDRAWABLE_STATUSES.includes(application.status) && (
                    <Button
                      size="sm"
                      variant="destructive"
                      loading={withdrawingId === application.id}
                      onClick={() => handleWithdraw(application.id)}
                    >
                      Withdraw
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Card>
  )
}
