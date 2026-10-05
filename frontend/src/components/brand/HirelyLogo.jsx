import { Link } from 'react-router-dom'
import { useAuthStore } from '../../auth/authStore'
import { ROLES } from '../../utils/roles'
import { cn } from '../../utils/cn'

// Mirrors LoginPage.jsx's post-login redirect map — kept local rather than
// importing from there, since LoginPage.jsx isn't a platform-layer file.
const HOME_ROUTE_BY_ROLE = {
  [ROLES.JOB_SEEKER]: '/candidate',
  [ROLES.RECRUITER]: '/recruiter',
  [ROLES.HR_EXECUTIVE]: '/recruiter',
  [ROLES.INTERVIEW_PANEL_MEMBER]: '/recruiter',
  [ROLES.SYSTEM_ADMINISTRATOR]: '/admin',
}

export default function HirelyLogo({ className }) {
  const role = useAuthStore((state) => state.role)
  const to = HOME_ROUTE_BY_ROLE[role] ?? '/login'

  return (
    <Link
      to={to}
      className={cn(
        'font-heading text-h4 font-bold tracking-tight text-ink-primary transition-colors hover:text-accent',
        className,
      )}
    >
      Hirely
    </Link>
  )
}
