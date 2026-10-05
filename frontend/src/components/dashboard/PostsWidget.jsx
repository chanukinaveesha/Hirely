import { Link } from 'react-router-dom'
import { toMediaUrl } from '../../api/mediaUrl'
import { Avatar, Card, CardHeader } from '../common'

function truncate(text, max = 140) {
  return text.length > max ? `${text.slice(0, max - 1)}…` : text
}

export default function PostsWidget({ posts = [] }) {
  return (
    <Card>
      <CardHeader title="Latest posts" description="What the community is sharing right now." />

      {posts.length === 0 ? (
        <p className="text-body text-ink-secondary">No posts yet.</p>
      ) : (
        <div className="flex flex-col gap-4">
          {posts.map((post) => (
            <Link
              key={post.id}
              to={post.linkUrl}
              className="flex items-start gap-3 rounded-sm border border-line px-3 py-2 transition-colors hover:border-line-strong"
            >
              <Avatar name={post.authorName} src={toMediaUrl(post.authorAvatarUrl)} size="sm" />
              <div>
                <p className="text-small font-medium text-ink-primary">{post.authorName}</p>
                <p className="text-small text-ink-secondary">{truncate(post.body)}</p>
              </div>
            </Link>
          ))}
        </div>
      )}

      <div className="mt-4">
        <Link to="/posts" className="text-small font-medium text-secondary hover:text-secondary-hover">
          View all posts →
        </Link>
      </div>
    </Card>
  )
}
