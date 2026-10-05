import { useEffect, useState } from 'react'
import { getDashboard } from '../../api/dashboardApi'
import { useAuthStore } from '../../auth/authStore'
import PostsWidget from '../../components/dashboard/PostsWidget'
import { LoadingSpinner, toast } from '../../components/common'
import { ROLES } from '../../utils/roles'
import AdminView from './AdminView'
import CandidateView from './CandidateView'
import RecruiterHrPanelView from './RecruiterHrPanelView'

// Used by CandidateDashboardPage, RecruiterDashboardPage, and
// AdminDashboardPage as a thin wrapper — it self-determines which section
// to render from the signed-in role, since HR_EXECUTIVE and
// INTERVIEW_PANEL_MEMBER both land on RecruiterDashboardPage too.
export default function RoleDashboard() {
  const role = useAuthStore((state) => state.role)
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getDashboard()
      .then(setData)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <LoadingSpinner label="Loading dashboard..." />
  if (!data) return null

  return (
    <div className="flex flex-col gap-6">
      {role === ROLES.JOB_SEEKER && <CandidateView data={data.candidate} />}
      {(role === ROLES.RECRUITER || role === ROLES.HR_EXECUTIVE || role === ROLES.INTERVIEW_PANEL_MEMBER) && (
        <RecruiterHrPanelView role={role} recruiter={data.recruiter} hr={data.hr} panel={data.panel} />
      )}
      {role === ROLES.SYSTEM_ADMINISTRATOR && <AdminView data={data.admin} />}

      <PostsWidget posts={data.recentPosts} />
    </div>
  )
}
