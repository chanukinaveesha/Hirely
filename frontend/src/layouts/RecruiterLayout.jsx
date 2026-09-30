import { Outlet } from 'react-router-dom'
import PortalHeader from './PortalHeader'
import { useAuthStore } from '../auth/authStore'
import { ROLES } from '../utils/roles'

const RECRUITER_NAV_ITEMS = [
  { to: '/recruiter', label: 'Dashboard', end: true },
  { to: '/recruiter/vacancies', label: 'Vacancies' },
]

const PANEL_MEMBER_NAV_ITEMS = [
  { to: '/recruiter', label: 'Dashboard', end: true },
  { to: '/recruiter/panel-interviews', label: 'My Interviews' },
]

export default function RecruiterLayout() {
  const role = useAuthStore((state) => state.role)
  const navItems =
    role === ROLES.INTERVIEW_PANEL_MEMBER
      ? PANEL_MEMBER_NAV_ITEMS
      : RECRUITER_NAV_ITEMS

  return (
    <div className="min-h-screen bg-base">
      <PortalHeader title="Recruiter / HR Portal" navItems={navItems} />
      <main className="mx-auto max-w-6xl p-6">
        <Outlet />
      </main>
    </div>
  )
}