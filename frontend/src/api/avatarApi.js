import { apiClient } from './apiClient'

export function getMyAvatar() {
  return apiClient.get('/avatars/me').then((res) => res.data)
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return apiClient
    .put('/avatars/me', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((res) => res.data)
}

export function removeAvatar() {
  return apiClient.delete('/avatars/me')
}
