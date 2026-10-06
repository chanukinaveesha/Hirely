import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../auth/authStore'
import { HOME_ROUTE_BY_ROLE } from '../brand/HirelyLogo'
import { Button } from '../common'

export default function HeroSection() {
  const navigate = useNavigate()
  const role = useAuthStore((state) => state.role)
  const dashboardRoute = HOME_ROUTE_BY_ROLE[role] ?? '/login'

  return (
    <section className="relative overflow-hidden rounded-lg border border-line bg-surface px-6 py-16 text-center sm:py-20">
      <div className="bg-glow pointer-events-none absolute inset-0" aria-hidden="true" />
      <div className="relative mx-auto flex max-w-xl flex-col items-center gap-4">
        <h1 className="text-h1 text-ink-primary">Get hired :)</h1>
        <p className="text-body text-ink-secondary">
          Find your next role, showcase your work, and stay in the loop with what's happening
          across Hirely.
        </p>
        <div className="mt-4 flex w-full flex-col gap-3 sm:w-auto sm:flex-row">
          <Button
            variant="primary"
            size="lg"
            fullWidth
            className="sm:w-auto"
            onClick={() => navigate(dashboardRoute)}
          >
            Dashboard
          </Button>
          <Button
            variant="secondary"
            size="lg"
            fullWidth
            className="sm:w-auto"
            onClick={() => navigate('/posts')}
          >
            Posts
          </Button>
        </div>
      </div>
    </section>
  )
}
