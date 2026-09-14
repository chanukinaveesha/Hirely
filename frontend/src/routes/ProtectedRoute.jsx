import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '../auth/authStore'

export default function ProtectedRoute({ allowedRoles }) {
  const { token, role } = useAuthStore()

  if (!token) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles && !allowedRoles.includes(role)) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}
