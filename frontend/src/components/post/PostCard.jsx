import { Link } from 'react-router-dom'
import { toMediaUrl } from '../../api/mediaUrl'
import { useAuthStore } from '../../auth/authStore'
import { Avatar, Badge, Button, Card, toast } from '../common'
import { deletePost } from '../../api/postApi'

const TYPE_LABEL = {
  VACANCY_PROMO: 'Vacancy',
  PROFILE_SHOWCASE: 'Profile',
}

function formatDate(value) {
  return new Date(value).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

export default function PostCard({ post, onDeleted }) {
  const currentUserId = useAuthStore((state) => state.user?.id)
  const isOwner = currentUserId === post.authorId

  async function handleDelete() {
    try {
      await deletePost(post.id)
      toast.success('Post deleted.')
      onDeleted?.(post.id)
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <Card>
      <div className="flex items-start justify-between gap-4">
        <Link to={`/people/${post.authorId}`} className="flex items-center gap-3 hover:opacity-80">
          <Avatar name={post.authorName} src={toMediaUrl(post.authorAvatarUrl)} size="sm" />
          <div>
            <p className="text-small font-medium text-ink-primary">{post.authorName}</p>
            <p className="text-small text-ink-muted">{formatDate(post.createdAt)}</p>
          </div>
        </Link>
        <div className="flex items-center gap-2">
          <Badge variant={post.type === 'VACANCY_PROMO' ? 'accent' : 'secondary'}>
            {TYPE_LABEL[post.type] ?? post.type}
          </Badge>
          {isOwner && (
            <Button variant="ghost" size="sm" onClick={handleDelete}>
              Delete
            </Button>
          )}
        </div>
      </div>

      <p className="mt-4 whitespace-pre-wrap text-body text-ink-primary">{post.body}</p>

      {post.imageUrl && (
        <img
          src={toMediaUrl(post.imageUrl)}
          alt=""
          loading="lazy"
          className="mt-4 max-h-96 w-full rounded-md border border-line object-cover"
        />
      )}

      <div className="mt-4">
        <Link to={post.linkUrl} className="text-small font-medium text-secondary hover:text-secondary-hover">
          {post.type === 'VACANCY_PROMO' ? 'View vacancy' : 'View profile'} →
        </Link>
      </div>
    </Card>
  )
}
