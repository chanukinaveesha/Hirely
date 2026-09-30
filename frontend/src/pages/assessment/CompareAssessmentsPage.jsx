import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { compareAssessmentsForVacancy } from '../../api/assessmentApi'
import CandidateAssessmentStatusBadge from '../../components/assessment/CandidateAssessmentStatusBadge'
import { formatEnumLabel } from '../../utils/vacancyOptions'
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

export default function CompareAssessmentsPage() {
  const { vacancyId } = useParams()
  const navigate = useNavigate()
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    compareAssessmentsForVacancy(vacancyId)
      .then(setResults)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }, [vacancyId])

  return (
    <Card>
      <CardHeader
        title="Compare assessment results"
        description="All candidate assessment attempts for this vacancy, ranked by score."
        action={
          <Button variant="ghost" size="sm" onClick={() => navigate(-1)}>
            Back
          </Button>
        }
      />

      {loading ? (
        <LoadingSpinner label="Loading results..." />
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableHeaderCell>Candidate</TableHeaderCell>
              <TableHeaderCell>Assessment</TableHeaderCell>
              <TableHeaderCell>Status</TableHeaderCell>
              <TableHeaderCell>Score</TableHeaderCell>
              <TableHeaderCell>Feedback</TableHeaderCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {results.length === 0 && <TableEmpty colSpan={5} message="No assessments assigned for this vacancy yet." />}
            {results.map((result) => (
              <TableRow key={result.id}>
                <TableCell>{result.candidateName}</TableCell>
                <TableCell>
                  <p className="text-ink-primary">{result.assessmentTitle}</p>
                  <p className="text-small text-ink-muted">{formatEnumLabel(result.assessmentType)}</p>
                </TableCell>
                <TableCell>
                  <CandidateAssessmentStatusBadge status={result.status} />
                </TableCell>
                <TableCell>{result.score ?? '—'}</TableCell>
                <TableCell>{result.feedback ?? '—'}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Card>
  )
}
