import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from '../pages/auth/LoginPage'
import RegisterPage from '../pages/auth/RegisterPage'
import CandidateDashboardPage from '../pages/CandidateDashboardPage'
import RecruiterDashboardPage from '../pages/RecruiterDashboardPage'
import AdminDashboardPage from '../pages/AdminDashboardPage'
import VacancyFormPage from '../pages/vacancy/VacancyFormPage'
import MyVacanciesPage from '../pages/vacancy/MyVacanciesPage'
import VacancyDetailPage from '../pages/vacancy/VacancyDetailPage'
import CandidateLayout from '../layouts/CandidateLayout'
import RecruiterLayout from '../layouts/RecruiterLayout'
import AdminLayout from '../layouts/AdminLayout'
import ProtectedRoute from './ProtectedRoute'
import PortalLayoutForRole from './PortalLayoutForRole'
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
          <Route path="/recruiter/vacancies" element={<MyVacanciesPage />} />
          <Route path="/recruiter/vacancies/new" element={<VacancyFormPage />} />
          <Route path="/recruiter/vacancies/:id/edit" element={<VacancyFormPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={ADMIN_ROLES} />}>
        <Route element={<AdminLayout />}>
          <Route path="/admin" element={<AdminDashboardPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<PortalLayoutForRole />}>
          <Route path="/vacancies/:id" element={<VacancyDetailPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
