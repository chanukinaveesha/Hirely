import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { changePassword, deactivateAccount, getProfile, updateProfile } from '../../api/profileApi'
import { useAuthStore } from '../../auth/authStore'
import {
  Badge,
  Button,
  Card,
  CardHeader,
  ErrorMessage,
  FormField,
  Input,
  LoadingSpinner,
  Modal,
  toast,
} from '../../components/common'

const STATUS_BADGE_VARIANT = {
  ACTIVE: 'success',
  INACTIVE: 'neutral',
  SUSPENDED: 'error',
}

export default function ProfilePage() {
  const navigate = useNavigate()
  const logout = useAuthStore((state) => state.logout)

  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)

  const [detailsForm, setDetailsForm] = useState({ name: '', phone: '' })
  const [detailsError, setDetailsError] = useState(null)
  const [savingDetails, setSavingDetails] = useState(false)

  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [passwordError, setPasswordError] = useState(null)
  const [changingPassword, setChangingPassword] = useState(false)

  const [deactivateOpen, setDeactivateOpen] = useState(false)
  const [deactivatePassword, setDeactivatePassword] = useState('')
  const [deactivateError, setDeactivateError] = useState(null)
  const [deactivating, setDeactivating] = useState(false)

  useEffect(() => {
    getProfile()
      .then((data) => {
        setProfile(data)
        setDetailsForm({ name: data.name, phone: data.phone ?? '' })
      })
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }, [])

  async function handleDetailsSubmit(event) {
    event.preventDefault()
    setDetailsError(null)
    setSavingDetails(true)
    try {
      const updated = await updateProfile(detailsForm)
      setProfile(updated)
      toast.success('Profile updated.')
    } catch (err) {
      setDetailsError(err.message)
    } finally {
      setSavingDetails(false)
    }
  }

  async function handlePasswordSubmit(event) {
    event.preventDefault()
    setPasswordError(null)

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordError('New password and confirmation do not match.')
      return
    }

    setChangingPassword(true)
    try {
      await changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      })
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
      toast.success('Password changed.')
    } catch (err) {
      setPasswordError(err.message)
    } finally {
      setChangingPassword(false)
    }
  }

  async function handleDeactivate(event) {
    event.preventDefault()
    setDeactivateError(null)
    setDeactivating(true)
    try {
      await deactivateAccount({ currentPassword: deactivatePassword })
      logout()
      navigate('/login')
    } catch (err) {
      setDeactivateError(err.message)
      setDeactivating(false)
    }
  }

  if (loading) return <LoadingSpinner label="Loading profile..." />
  if (!profile) return null

  return (
    <div className="flex flex-col gap-6">
      <Card>
        <CardHeader
          title="Profile details"
          description="Your account information."
          action={<Badge variant={STATUS_BADGE_VARIANT[profile.accountStatus]}>{profile.accountStatus}</Badge>}
        />

        <div className="mb-5 flex flex-wrap gap-2">
          <Badge variant="accent">{profile.role.replaceAll('_', ' ')}</Badge>
          {profile.clientCompanyName && <Badge variant="secondary">{profile.clientCompanyName}</Badge>}
        </div>

        <form onSubmit={handleDetailsSubmit} className="flex flex-col gap-4">
          <ErrorMessage message={detailsError} />

          <FormField label="Email" htmlFor="profile-email" helperText="Email can't be changed here.">
            <Input id="profile-email" value={profile.email} disabled />
          </FormField>

          <FormField label="Full name" htmlFor="profile-name">
            <Input
              id="profile-name"
              required
              value={detailsForm.name}
              onChange={(e) => setDetailsForm({ ...detailsForm, name: e.target.value })}
            />
          </FormField>

          <FormField label="Phone" htmlFor="profile-phone">
            <Input
              id="profile-phone"
              value={detailsForm.phone}
              onChange={(e) => setDetailsForm({ ...detailsForm, phone: e.target.value })}
            />
          </FormField>

          <div>
            <Button type="submit" loading={savingDetails}>
              Save changes
            </Button>
          </div>
        </form>
      </Card>

      <Card>
        <CardHeader title="Change password" description="Choose a new password for your account." />

        <form onSubmit={handlePasswordSubmit} className="flex flex-col gap-4">
          <ErrorMessage message={passwordError} />

          <FormField label="Current password" htmlFor="current-password">
            <Input
              id="current-password"
              type="password"
              required
              value={passwordForm.currentPassword}
              onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
            />
          </FormField>

          <FormField label="New password" htmlFor="new-password" helperText="At least 8 characters">
            <Input
              id="new-password"
              type="password"
              required
              minLength={8}
              value={passwordForm.newPassword}
              onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
            />
          </FormField>

          <FormField label="Confirm new password" htmlFor="confirm-password">
            <Input
              id="confirm-password"
              type="password"
              required
              value={passwordForm.confirmPassword}
              onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
            />
          </FormField>

          <div>
            <Button type="submit" loading={changingPassword}>
              Update password
            </Button>
          </div>
        </form>
      </Card>

      <Card>
        <CardHeader title="Deactivate account" description="This signs you out and disables sign-in until an administrator reactivates it." />
        <Button variant="destructive" onClick={() => setDeactivateOpen(true)}>
          Deactivate account
        </Button>
      </Card>

      <Modal
        isOpen={deactivateOpen}
        onClose={() => !deactivating && setDeactivateOpen(false)}
        title="Confirm account deactivation"
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeactivateOpen(false)} disabled={deactivating}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleDeactivate} loading={deactivating}>
              Deactivate
            </Button>
          </>
        }
      >
        <form onSubmit={handleDeactivate} className="flex flex-col gap-4">
          <p className="text-small">Enter your password to confirm. You'll be signed out immediately.</p>
          <ErrorMessage message={deactivateError} />
          <FormField label="Password" htmlFor="deactivate-password">
            <Input
              id="deactivate-password"
              type="password"
              required
              value={deactivatePassword}
              onChange={(e) => setDeactivatePassword(e.target.value)}
            />
          </FormField>
        </form>
      </Modal>
    </div>
  )
}
