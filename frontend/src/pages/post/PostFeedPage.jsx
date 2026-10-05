import { useEffect, useState } from 'react'
import { getFeed } from '../../api/postApi'
import CreatePostForm from '../../components/post/CreatePostForm'
import PostCard from '../../components/post/PostCard'
import { Button, Card, LoadingSpinner, toast } from '../../components/common'

const PAGE_SIZE = 10

export default function PostFeedPage() {
  const [posts, setPosts] = useState([])
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(false)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)

  function loadPage(pageToLoad, replace) {
    const setLoader = replace ? setLoading : setLoadingMore
    setLoader(true)
    getFeed(pageToLoad, PAGE_SIZE)
      .then((data) => {
        setPosts((prev) => (replace ? data.items : [...prev, ...data.items]))
        setHasMore(data.hasMore)
        setPage(pageToLoad)
      })
      .catch((err) => toast.error(err.message))
      .finally(() => setLoader(false))
  }

  useEffect(() => {
    loadPage(0, true)
  }, [])

  function handleCreated(post) {
    setPosts((prev) => [post, ...prev])
  }

  function handleDeleted(postId) {
    setPosts((prev) => prev.filter((p) => p.id !== postId))
  }

  return (
    <div className="flex flex-col gap-6">
      <CreatePostForm onCreated={handleCreated} />

      {loading ? (
        <LoadingSpinner label="Loading posts..." />
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

      {hasMore && (
        <div className="flex justify-center">
          <Button variant="secondary" loading={loadingMore} onClick={() => loadPage(page + 1, false)}>
            Load more
          </Button>
        </div>
      )}
    </div>
  )
}
