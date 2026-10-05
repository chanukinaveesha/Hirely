import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getPublicProfile } from '../../api/publicProfileApi'
import { toMediaUrl } from '../../api/mediaUrl'
import { Avatar, Badge, Card, LoadingSpinner, toast } from '../../components/common'

export default function PublicProfilePage() {
  const { userId } = useParams()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    getPublicProfile(userId)
      .then(setProfile)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }, [userId])

  if (loading) return <LoadingSpinner label="Loading profile..." />
  if (!profile) return null

  return (
    <Card padding="lg" className="mx-auto max-w-md">
      <div className="flex flex-col items-center gap-4 text-center">
        <Avatar name={profile.name} src={toMediaUrl(profile.avatarUrl)} size="lg" />
        <div>
          <h1 className="text-h3 text-ink-primary">{profile.name}</h1>
          <div className="mt-2 flex flex-wrap justify-center gap-2">
            <Badge variant="accent">{profile.role.replaceAll('_', ' ')}</Badge>
            {profile.clientCompanyName && <Badge variant="secondary">{profile.clientCompanyName}</Badge>}
          </div>
        </div>
      </div>
    </Card>
  )
}
