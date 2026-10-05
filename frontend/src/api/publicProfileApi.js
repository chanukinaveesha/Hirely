import { apiClient } from './apiClient'

export function getPublicProfile(userId) {
  return apiClient.get(`/public-profiles/${userId}`).then((res) => res.data)
}
