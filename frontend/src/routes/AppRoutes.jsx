import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from '../pages/auth/LoginPage'
import RegisterPage from '../pages/auth/RegisterPage'
import CandidateDashboardPage from '../pages/CandidateDashboardPage'
import RecruiterDashboardPage from '../pages/RecruiterDashboardPage'
import AdminDashboardPage from '../pages/AdminDashboardPage'
import CandidateLayout from '../layouts/CandidateLayout'
import RecruiterLayout from '../layouts/RecruiterLayout'
import AdminLayout from '../layouts/AdminLayout'
import ProtectedRoute from './ProtectedRoute'
import { ADMIN_ROLES, CANDIDATE_ROLES, RECRUITER_HR_ROLES } from '../utils/roles'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute allowedRoles={CANDIDATE_ROLES} />}>
        <Route element={<CandidateLayout />}>
          <Route path="/candidate" element={<CandidateDashboardPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={RECRUITER_HR_ROLES} />}>
        <Route element={<RecruiterLayout />}>
          <Route path="/recruiter" element={<RecruiterDashboardPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={ADMIN_ROLES} />}>
        <Route element={<AdminLayout />}>
          <Route path="/admin" element={<AdminDashboardPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
