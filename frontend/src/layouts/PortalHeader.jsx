import { Link, NavLink } from 'react-router-dom'
import { useAuthStore } from '../auth/authStore'
import { Avatar, Button } from '../components/common'
import { cn } from '../utils/cn'

/**
 * Shared top nav for the three portal shells. `title` names the portal,
 * `navItems` is [{ to, label }] for that portal's own section links.
 */
export default function PortalHeader({ title, navItems = [] }) {
  const { user, logout } = useAuthStore()

  return (
    <header className="border-b border-line bg-surface px-6">
      <div className="flex h-16 items-center justify-between">
        <div className="flex items-center gap-8">
          <span className="text-h4 text-ink-primary">{title}</span>
          <nav className="flex items-center gap-6">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  cn(
                    'text-small font-medium transition-colors',
                    isActive ? 'text-accent' : 'text-ink-secondary hover:text-ink-primary',
                  )
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/profile" className="flex items-center gap-2 hover:opacity-80">
            <Avatar name={user?.name} size="sm" />
            <span className="text-small text-ink-secondary">{user?.name}</span>
          </Link>
          <Button variant="ghost" size="sm" onClick={logout}>
            Logout
          </Button>
        </div>
      </div>
    </header>
  )
}
