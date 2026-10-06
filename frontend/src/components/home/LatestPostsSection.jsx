import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getFeed } from '../../api/postApi'
import PostCard from '../post/PostCard'
import { Card, ErrorMessage, LoadingSpinner } from '../common'

const LATEST_COUNT = 10

export default function LatestPostsSection() {
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    setLoading(true)
    setError(null)
    getFeed(0, LATEST_COUNT)
      .then((data) => {
        if (!cancelled) setPosts(data.items)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  function handleDeleted(postId) {
    setPosts((prev) => prev.filter((p) => p.id !== postId))
  }

  return (
    <section className="flex flex-col gap-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-h3 text-ink-primary">Latest posts</h2>
        <Link to="/posts" className="text-small font-medium text-secondary hover:text-secondary-hover">
          View all posts →
        </Link>
      </div>

      {loading ? (
        <LoadingSpinner label="Loading posts..." />
      ) : error ? (
        <ErrorMessage message={error} />
      ) : posts.length === 0 ? (
        <Card>
          <p className="text-body text-ink-secondary">No posts yet.</p>
        </Card>
      ) : (
        <div className="flex flex-col gap-4">
          {posts.map((post) => (
            <PostCard key={post.id} post={post} onDeleted={handleDeleted} />
          ))}
        </div>
      )}
    </section>
  )
}
