import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../../api/authApi'
import { useAuthStore } from '../../auth/authStore'
import { ROLES } from '../../utils/roles'
import ErrorMessage from '../../components/common/ErrorMessage'

const DASHBOARD_BY_ROLE = {
  [ROLES.JOB_SEEKER]: '/candidate',
  [ROLES.RECRUITER]: '/recruiter',
  [ROLES.HR_EXECUTIVE]: '/recruiter',
  [ROLES.INTERVIEW_PANEL_MEMBER]: '/recruiter',
  [ROLES.SYSTEM_ADMINISTRATOR]: '/admin',
}

export default function LoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.login)
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const auth = await login(form)
      setAuth(auth)
      navigate(DASHBOARD_BY_ROLE[auth.role] ?? '/login')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50">
      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4 rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-xl font-semibold text-slate-800">Sign in</h1>
        <ErrorMessage message={error} />
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-slate-700">Email</label>
          <input
            id="email"
            type="email"
            required
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
        </div>
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-slate-700">Password</label>
          <input
            id="password"
            type="password"
            required
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
        </div>
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-md bg-slate-800 px-4 py-2 text-sm font-medium text-white hover:bg-slate-900 disabled:opacity-50"
        >
          {submitting ? 'Signing in...' : 'Sign in'}
        </button>
        <p className="text-center text-sm text-slate-500">
          No account? <Link to="/register" className="text-slate-800 underline">Register</Link>
        </p>
      </form>
    </div>
  )
}
