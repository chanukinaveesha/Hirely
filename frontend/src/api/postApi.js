import { apiClient } from './apiClient'

export function getFeed(page = 0, size = 10) {
  return apiClient.get('/posts', { params: { page, size } }).then((res) => res.data)
}

export function createPost({ type, body, vacancyId, image }) {
  const formData = new FormData()
  formData.append('type', type)
  formData.append('body', body)
  if (vacancyId) formData.append('vacancyId', vacancyId)
  if (image) formData.append('image', image)
  return apiClient
    .post('/posts', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((res) => res.data)
}

export function deletePost(id) {
  return apiClient.delete(`/posts/${id}`)
}
