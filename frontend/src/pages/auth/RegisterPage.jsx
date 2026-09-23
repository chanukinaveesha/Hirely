import { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register } from '../../api/authApi'
import { listClientCompanies } from '../../api/clientCompanyApi'
import { useAuthStore } from '../../auth/authStore'
import { ROLES } from '../../utils/roles'
import { Button, Card, ErrorMessage, FormField, Input, Select } from '../../components/common'

const DASHBOARD_BY_ROLE = {
  [ROLES.JOB_SEEKER]: '/candidate',
  [ROLES.RECRUITER]: '/recruiter',
  [ROLES.HR_EXECUTIVE]: '/recruiter',
  [ROLES.INTERVIEW_PANEL_MEMBER]: '/recruiter',
  [ROLES.SYSTEM_ADMINISTRATOR]: '/admin',
}

const COMPANY_LINKED_ROLES = [ROLES.RECRUITER, ROLES.HR_EXECUTIVE]

const initialForm = { name: '', email: '', password: '', phone: '', role: ROLES.JOB_SEEKER }

export default function RegisterPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.login)
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const [companies, setCompanies] = useState([])
  const [companyMode, setCompanyMode] = useState('existing')
  const [clientCompanyId, setClientCompanyId] = useState('')
  const [newCompanyName, setNewCompanyName] = useState('')
  const [newCompanyIndustry, setNewCompanyIndustry] = useState('')

  const needsCompany = COMPANY_LINKED_ROLES.includes(form.role)

  useEffect(() => {
    if (!needsCompany || companies.length > 0) return
    listClientCompanies()
      .then(setCompanies)
      .catch(() => setCompanies([]))
  }, [needsCompany, companies.length])

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const payload = { ...form }
      if (needsCompany) {
        if (companyMode === 'existing') {
          payload.clientCompanyId = Number(clientCompanyId)
        } else {
          payload.newClientCompany = { companyName: newCompanyName, industry: newCompanyIndustry || undefined }
        }
      }

      const auth = await register(payload)
      setAuth(auth)
      navigate(DASHBOARD_BY_ROLE[auth.role] ?? '/login')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="bg-glow flex min-h-screen items-center justify-center bg-base px-4 py-10">
      <Card padding="lg" className="w-full max-w-sm">
        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          <div>
            <h1 className="text-h2 text-ink-primary">Create an account</h1>
            <p className="mt-1 text-small text-ink-secondary">Join the platform to get started.</p>
          </div>

          <ErrorMessage message={error} />

          <FormField label="Full name" htmlFor="name">
            <Input id="name" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </FormField>

          <FormField label="Email" htmlFor="email">
            <Input
              id="email"
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </FormField>

          <FormField label="Password" htmlFor="password" helperText="At least 8 characters">
            <Input
              id="password"
              type="password"
              required
              minLength={8}
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          </FormField>

          <FormField label="Phone" htmlFor="phone">
            <Input id="phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          </FormField>

          <FormField label="Role" htmlFor="role">
            <Select id="role" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
              {Object.values(ROLES).map((role) => (
                <option key={role} value={role}>
                  {role.replaceAll('_', ' ')}
                </option>
              ))}
            </Select>
          </FormField>

          {needsCompany && (
            <div className="flex flex-col gap-3 rounded-sm border border-line p-3">
              <div className="flex gap-2">
                <Button
                  type="button"
                  size="sm"
                  variant={companyMode === 'existing' ? 'primary' : 'ghost'}
                  onClick={() => setCompanyMode('existing')}
                >
                  Select existing company
                </Button>
                <Button
                  type="button"
                  size="sm"
                  variant={companyMode === 'new' ? 'primary' : 'ghost'}
                  onClick={() => setCompanyMode('new')}
                >
                  Create new company
                </Button>
              </div>

              {companyMode === 'existing' ? (
                <FormField label="Client company" htmlFor="clientCompanyId">
                  <Select
                    id="clientCompanyId"
                    required
                    value={clientCompanyId}
                    onChange={(e) => setClientCompanyId(e.target.value)}
                  >
                    <option value="" disabled>
                      {companies.length === 0 ? 'No companies yet — create one instead' : 'Choose a company'}
                    </option>
                    {companies.map((company) => (
                      <option key={company.id} value={company.id}>
                        {company.companyName}
                      </option>
                    ))}
                  </Select>
                </FormField>
              ) : (
                <>
                  <FormField label="Company name" htmlFor="newCompanyName">
                    <Input
                      id="newCompanyName"
                      required
                      value={newCompanyName}
                      onChange={(e) => setNewCompanyName(e.target.value)}
                    />
                  </FormField>
                  <FormField label="Industry" htmlFor="newCompanyIndustry" helperText="Optional">
                    <Input
                      id="newCompanyIndustry"
                      value={newCompanyIndustry}
                      onChange={(e) => setNewCompanyIndustry(e.target.value)}
                    />
                  </FormField>
                </>
              )}
            </div>
          )}

          <Button type="submit" fullWidth loading={submitting}>
            {submitting ? 'Creating account...' : 'Register'}
          </Button>

          <p className="text-center text-small text-ink-secondary">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-secondary hover:text-secondary-hover">
              Sign in
            </Link>
          </p>
        </form>
      </Card>
    </div>
  )
}
