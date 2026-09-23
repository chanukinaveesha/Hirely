import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../auth/authStore'
import CandidateLayout from '../layouts/CandidateLayout'
import RecruiterLayout from '../layouts/RecruiterLayout'
import AdminLayout from '../layouts/AdminLayout'
import { ADMIN_ROLES, CANDIDATE_ROLES, RECRUITER_HR_ROLES } from '../utils/roles'

/**
 * For a route that must be reachable by every role but still needs portal
 * chrome (nav/header) — e.g. a shared profile or detail page. Picks the
 * layout based on the signed-in user's role instead of registering the
 * route once per role-gated block, which would make React Router match
 * whichever copy is declared first regardless of who's actually signed in.
 *
 * Usage:
 *   <Route element={<ProtectedRoute />}>
 *     <Route element={<PortalLayoutForRole />}>
 *       <Route path="/something/:id" element={<SomethingPage />} />
 *     </Route>
 *   </Route>
 */
export default function PortalLayoutForRole() {
  const role = useAuthStore((state) => state.role)

  if (CANDIDATE_ROLES.includes(role)) return <CandidateLayout />
  if (RECRUITER_HR_ROLES.includes(role)) return <RecruiterLayout />
  if (ADMIN_ROLES.includes(role)) return <AdminLayout />
  return <Navigate to="/login" replace />
}
