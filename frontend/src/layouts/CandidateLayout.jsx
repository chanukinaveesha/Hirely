import { Outlet } from 'react-router-dom'
import PortalHeader from './PortalHeader'

const NAV_ITEMS = [{ to: '/candidate', label: 'Dashboard' }]

export default function CandidateLayout() {
  return (
    <div className="min-h-screen bg-base">
      <PortalHeader title="Candidate Portal" navItems={NAV_ITEMS} />
      <main className="mx-auto max-w-6xl p-6">
        <Outlet />
      </main>
    </div>
  )
}
