import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import {
  getApplicantsForVacancy,
  rejectApplication,
  shortlistApplication,
} from '../../api/applicationApi'
import { downloadApplicantResume } from '../../api/resumeApi'
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

const NOT_REJECTABLE = ['REJECTED', 'WITHDRAWN', 'SELECTED']
const SHORTLISTABLE_FROM = ['SUBMITTED', 'UNDER_REVIEW']

export default function ApplicationReviewPage() {
  const { vacancyId } = useParams()
  const [applicants, setApplicants] = useState([])
  const [loading, setLoading] = useState(true)
  const [actioningId, setActioningId] = useState(null)

  function loadApplicants() {
    setLoading(true)
    return getApplicantsForVacancy(vacancyId)
      .then(setApplicants)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadApplicants()
  }, [vacancyId])

  async function handleShortlist(id) {
    setActioningId(id)
    try {
      await shortlistApplication(id)
      toast.success('Candidate shortlisted.')
      await loadApplicants()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActioningId(null)
    }
  }

  async function handleReject(id) {
    setActioningId(id)
    try {
      await rejectApplication(id)
      toast.success('Application rejected.')
      await loadApplicants()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setActioningId(null)
    }
  }

  async function handleDownload(applicantId, applicantName) {
    try {
      await downloadApplicantResume(applicantId, `${applicantName}-resume`)
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <Card>
      <CardHeader title="Applicants" description="Review candidates who applied to this vacancy." />

      {loading ? (
        <LoadingSpinner label="Loading applicants..." />
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableHeaderCell>Candidate</TableHeaderCell>
              <TableHeaderCell>Applied</TableHeaderCell>
              <TableHeaderCell>Status</TableHeaderCell>
              <TableHeaderCell>Actions</TableHeaderCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {applicants.length === 0 && <TableEmpty colSpan={4} message="No one has applied yet." />}
            {applicants.map((applicant) => (
              <TableRow key={applicant.id}>
                <TableCell>
                  <p className="text-ink-primary">{applicant.jobSeekerName}</p>
                  <p className="text-small text-ink-muted">{applicant.jobSeekerEmail}</p>
                </TableCell>
                <TableCell>{new Date(applicant.appliedAt).toLocaleDateString()}</TableCell>
                <TableCell>
                  <ApplicationStatusBadge status={applicant.status} />
                </TableCell>
                <TableCell>
                  <div className="flex gap-2">
                    <Button size="sm" variant="secondary" onClick={() => handleDownload(applicant.id, applicant.jobSeekerName)}>
                      Download CV
                    </Button>
                    {SHORTLISTABLE_FROM.includes(applicant.status) && (
                      <Button
                        size="sm"
                        loading={actioningId === applicant.id}
                        onClick={() => handleShortlist(applicant.id)}
                      >
                        Shortlist
                      </Button>
                    )}
                    {!NOT_REJECTABLE.includes(applicant.status) && (
                      <Button
                        size="sm"
                        variant="destructive"
                        loading={actioningId === applicant.id}
                        onClick={() => handleReject(applicant.id)}
                      >
                        Reject
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
