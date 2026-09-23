import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../../api/authApi'
import { useAuthStore } from '../../auth/authStore'
import { ROLES } from '../../utils/roles'
import { Button, Card, ErrorMessage, FormField, Input } from '../../components/common'

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
    <div className="bg-glow flex min-h-screen items-center justify-center bg-base px-4">
      <Card padding="lg" className="w-full max-w-sm">
        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          <div>
            <h1 className="text-h2 text-ink-primary">Sign in</h1>
            <p className="mt-1 text-small text-ink-secondary">Welcome back — enter your details below.</p>
          </div>

          <ErrorMessage message={error} />

          <FormField label="Email" htmlFor="email">
            <Input
              id="email"
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </FormField>

          <FormField label="Password" htmlFor="password">
            <Input
              id="password"
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          </FormField>

          <Button type="submit" fullWidth loading={submitting}>
            {submitting ? 'Signing in...' : 'Sign in'}
          </Button>

          <p className="text-center text-small text-ink-secondary">
            No account?{' '}
            <Link to="/register" className="font-medium text-secondary hover:text-secondary-hover">
              Register
            </Link>
          </p>
        </form>
      </Card>
    </div>
  )
}
