import { apiClient } from './apiClient'

export function getProfile() {
  return apiClient.get('/profile').then((res) => res.data)
}

export function updateProfile(payload) {
  return apiClient.put('/profile', payload).then((res) => res.data)
}

export function changePassword(payload) {
  return apiClient.put('/profile/password', payload)
}

export function deactivateAccount(payload) {
  return apiClient.put('/profile/deactivate', payload)
}
