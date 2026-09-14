import { Link, Outlet } from 'react-router-dom'
import { useAuthStore } from '../auth/authStore'

export default function RecruiterLayout() {
  const { user, logout } = useAuthStore()

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-4">
        <nav className="flex items-center gap-6">
          <span className="font-semibold text-slate-800">Recruiter / HR Portal</span>
          <Link to="/recruiter" className="text-sm text-slate-600 hover:text-slate-900">
            Dashboard
          </Link>
        </nav>
        <div className="flex items-center gap-4 text-sm text-slate-600">
          <span>{user?.name}</span>
          <button onClick={logout} className="text-slate-500 hover:text-slate-900">
            Logout
          </button>
        </div>
      </header>
      <main className="p-6">
        <Outlet />
      </main>
    </div>
  )
}
