import { Outlet } from 'react-router-dom'
import PortalHeader from './PortalHeader'

const NAV_ITEMS = [
  { to: '/recruiter', label: 'Dashboard', end: true },
  { to: '/recruiter/vacancies', label: 'Vacancies' },
]

export default function RecruiterLayout() {
  return (
    <div className="min-h-screen bg-base">
      <PortalHeader title="Recruiter / HR Portal" navItems={NAV_ITEMS} />
      <main className="mx-auto max-w-6xl p-6">
        <Outlet />
      </main>
    </div>
  )
}
