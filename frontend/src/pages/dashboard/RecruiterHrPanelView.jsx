import { Link } from 'react-router-dom'
import { Badge, Card, CardHeader } from '../../components/common'
import { ROLES } from '../../utils/roles'
import { INTERVIEW_STATUS_BADGE_VARIANT } from '../../utils/interviewOptions'
import { VACANCY_STATUS_BADGE_VARIANT, formatEnumLabel } from '../../utils/vacancyOptions'

function formatDateTime(value) {
  return new Date(value).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

function StatTile({ label, value }) {
  return (
    <div className="rounded-sm border border-line bg-base px-4 py-3">
      <p className="text-h3 text-ink-primary">{value}</p>
      <p className="text-small text-ink-secondary">{label}</p>
    </div>
  )
}

function RecruiterSection({ recruiter }) {
  const statusEntries = Object.entries(recruiter.vacancyCountsByStatus ?? {})

  return (
    <>
      <Card>
        <CardHeader title="My vacancies" description="Counts by current status." />
        {statusEntries.length === 0 ? (
          <p className="text-body text-ink-secondary">You haven't posted any vacancies yet.</p>
        ) : (
          <div className="flex flex-wrap gap-2">
            {statusEntries.map(([status, count]) => (
              <Badge key={status} variant={VACANCY_STATUS_BADGE_VARIANT[status] ?? 'neutral'}>
                {formatEnumLabel(status)}: {count}
              </Badge>
            ))}
          </div>
        )}
        <Link to="/recruiter/vacancies" className="mt-3 inline-block text-small font-medium text-secondary hover:text-secondary-hover">
          Manage vacancies →
        </Link>
      </Card>

      <Card>
        <CardHeader title="Applicants" />
        <div className="grid grid-cols-2 gap-3">
          <StatTile label="Awaiting review" value={recruiter.applicantsAwaitingReview} />
          <StatTile label="Shortlisted" value={recruiter.shortlistedCount} />
        </div>
      </Card>
    </>
  )
}

function HrSection({ hr }) {
  return (
    <Card>
      <CardHeader title="HR tasks" />
      <div className="grid grid-cols-2 gap-3">
        <StatTile label="Assessments to evaluate" value={hr.assessmentsToEvaluate} />
        <StatTile label="Interviews to schedule" value={hr.interviewsToSchedule} />
      </div>
      <Link to="/recruiter/assessments" className="mt-3 inline-block text-small font-medium text-secondary hover:text-secondary-hover">
        Manage assessments →
      </Link>
    </Card>
  )
}

function PanelSection({ panel }) {
  return (
    <Card>
      <CardHeader title="Upcoming interviews" />
      {panel.upcomingInterviews?.length === 0 ? (
        <p className="text-body text-ink-secondary">No upcoming interviews.</p>
      ) : (
        <div className="flex flex-col gap-3">
          {panel.upcomingInterviews.map((interview) => (
            <div key={interview.id} className="flex items-center justify-between gap-3">
              <div>
                <p className="text-small font-medium text-ink-primary">{interview.vacancyTitle}</p>
                <p className="text-small text-ink-muted">{interview.candidateName} · {formatDateTime(interview.scheduledAt)}</p>
              </div>
              <Badge variant={INTERVIEW_STATUS_BADGE_VARIANT[interview.status] ?? 'neutral'}>
                {formatEnumLabel(interview.status)}
              </Badge>
            </div>
          ))}
        </div>
      )}
      <Link to="/recruiter/panel-interviews" className="mt-3 inline-block text-small font-medium text-secondary hover:text-secondary-hover">
        View all interviews →
      </Link>
    </Card>
  )
}

// Recruiter, HR, and panel members all land on the same /recruiter route
// (see LoginPage.jsx's redirect map), so this one view branches on which
// section the backend actually populated rather than on a route param.
export default function RecruiterHrPanelView({ role, recruiter, hr, panel }) {
  return (
    <div className="grid gap-6 lg:grid-cols-2">
      {role === ROLES.RECRUITER && recruiter && <RecruiterSection recruiter={recruiter} />}
      {role === ROLES.HR_EXECUTIVE && hr && <HrSection hr={hr} />}
      {role === ROLES.INTERVIEW_PANEL_MEMBER && panel && <PanelSection panel={panel} />}
    </div>
  )
}
