import { Link } from 'react-router-dom'
import { Badge, Card, CardHeader } from '../../components/common'
import { APPLICATION_STATUS_BADGE_VARIANT } from '../../utils/applicationOptions'
import { CANDIDATE_ASSESSMENT_STATUS_BADGE_VARIANT } from '../../utils/assessmentOptions'
import { INTERVIEW_STATUS_BADGE_VARIANT } from '../../utils/interviewOptions'
import { formatEnumLabel } from '../../utils/vacancyOptions'

function formatDateTime(value) {
  return new Date(value).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

export default function CandidateView({ data }) {
  const statusEntries = Object.entries(data.applicationCountsByStatus ?? {})

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <CardHeader title="My applications" description="Counts by current status." />
        {statusEntries.length === 0 ? (
          <p className="text-body text-ink-secondary">You haven't applied to any vacancies yet.</p>
        ) : (
          <div className="flex flex-wrap gap-2">
            {statusEntries.map(([status, count]) => (
              <Badge key={status} variant={APPLICATION_STATUS_BADGE_VARIANT[status] ?? 'neutral'}>
                {formatEnumLabel(status)}: {count}
              </Badge>
            ))}
          </div>
        )}
      </Card>

      <Card>
        <CardHeader title="My resume" />
        {data.resumeOnFile ? (
          <p className="text-body text-ink-primary">{data.resumeFileName}</p>
        ) : (
          <p className="text-body text-ink-secondary">You haven't uploaded a resume yet.</p>
        )}
        <Link to="/resume" className="mt-2 inline-block text-small font-medium text-secondary hover:text-secondary-hover">
          Manage resume →
        </Link>
      </Card>

      <Card>
        <CardHeader title="Upcoming interviews" />
        {data.upcomingInterviews?.length === 0 ? (
          <p className="text-body text-ink-secondary">No upcoming interviews.</p>
        ) : (
          <div className="flex flex-col gap-3">
            {data.upcomingInterviews.map((interview) => (
              <div key={interview.id} className="flex items-center justify-between gap-3">
                <div>
                  <p className="text-small font-medium text-ink-primary">{interview.vacancyTitle}</p>
                  <p className="text-small text-ink-muted">{formatDateTime(interview.scheduledAt)}</p>
                </div>
                <Badge variant={INTERVIEW_STATUS_BADGE_VARIANT[interview.status] ?? 'neutral'}>
                  {formatEnumLabel(interview.status)}
                </Badge>
              </div>
            ))}
          </div>
        )}
        <Link to="/interviews" className="mt-3 inline-block text-small font-medium text-secondary hover:text-secondary-hover">
          View all interviews →
        </Link>
      </Card>

      <Card>
        <CardHeader title="Assigned assessments" />
        {data.assignedAssessments?.length === 0 ? (
          <p className="text-body text-ink-secondary">No assessments assigned.</p>
        ) : (
          <div className="flex flex-col gap-3">
            {data.assignedAssessments.map((assessment) => (
              <div key={assessment.id} className="flex items-center justify-between gap-3">
                <div>
                  <p className="text-small font-medium text-ink-primary">{assessment.assessmentTitle}</p>
                  <p className="text-small text-ink-muted">Due {formatDateTime(assessment.deadline)}</p>
                </div>
                <Badge variant={CANDIDATE_ASSESSMENT_STATUS_BADGE_VARIANT[assessment.status] ?? 'neutral'}>
                  {formatEnumLabel(assessment.status)}
                </Badge>
              </div>
            ))}
          </div>
        )}
        <Link to="/assessments" className="mt-3 inline-block text-small font-medium text-secondary hover:text-secondary-hover">
          View all assessments →
        </Link>
      </Card>
    </div>
  )
}
