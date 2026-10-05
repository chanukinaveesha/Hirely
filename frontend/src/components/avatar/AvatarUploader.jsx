import { useEffect, useRef, useState } from 'react'
import { getMyAvatar, removeAvatar, uploadAvatar } from '../../api/avatarApi'
import { toMediaUrl } from '../../api/mediaUrl'
import { useAuthStore } from '../../auth/authStore'
import { useAvatarStore } from '../../stores/avatarStore'
import { Avatar, Button, LoadingSpinner, toast } from '../common'

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_SIZE_BYTES = 2 * 1024 * 1024

export default function AvatarUploader({ name }) {
  const userId = useAuthStore((state) => state.user?.id)
  const setAvatarUrl = useAvatarStore((state) => state.setAvatarUrl)

  const [avatarUrl, setLocalAvatarUrl] = useState(null)
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [removing, setRemoving] = useState(false)
  const fileInputRef = useRef(null)

  useEffect(() => {
    getMyAvatar()
      .then((data) => setLocalAvatarUrl(data.hasAvatar ? toMediaUrl(data.imageUrl) : null))
      .catch(() => setLocalAvatarUrl(null))
      .finally(() => setLoading(false))
  }, [])

  async function handleFileChange(event) {
    const file = event.target.files[0]
    event.target.value = ''
    if (!file) return

    if (!ALLOWED_TYPES.includes(file.type)) {
      toast.error('Only JPG, PNG, or WEBP images are allowed.')
      return
    }
    if (file.size > MAX_SIZE_BYTES) {
      toast.error('Image must be 2MB or smaller.')
      return
    }

    setUploading(true)
    try {
      const updated = await uploadAvatar(file)
      const resolvedUrl = toMediaUrl(updated.imageUrl)
      setLocalAvatarUrl(resolvedUrl)
      setAvatarUrl(updated.imageUrl, userId)
      toast.success(avatarUrl ? 'Profile picture replaced.' : 'Profile picture uploaded.')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setUploading(false)
    }
  }

  async function handleRemove() {
    setRemoving(true)
    try {
      await removeAvatar()
      setLocalAvatarUrl(null)
      setAvatarUrl(null, userId)
      toast.success('Profile picture removed.')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setRemoving(false)
    }
  }

  if (loading) return <LoadingSpinner label="Loading profile picture..." />

  return (
    <div className="flex items-center gap-4">
      <Avatar name={name} src={avatarUrl} size="lg" />
      <div className="flex flex-col gap-2">
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          className="hidden"
          onChange={handleFileChange}
        />
        <div className="flex gap-2">
          <Button
            variant={avatarUrl ? 'ghost' : 'primary'}
            size="sm"
            loading={uploading}
            onClick={() => fileInputRef.current.click()}
          >
            {avatarUrl ? 'Replace photo' : 'Upload photo'}
          </Button>
          {avatarUrl && (
            <Button variant="ghost" size="sm" loading={removing} onClick={handleRemove}>
              Remove
            </Button>
          )}
        </div>
        <p className="text-small text-ink-muted">JPG, PNG, or WEBP. Up to 2MB.</p>
      </div>
    </div>
  )
}
