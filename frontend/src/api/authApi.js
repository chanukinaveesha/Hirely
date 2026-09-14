import { apiClient } from './apiClient'

export function register(payload) {
  return apiClient.post('/auth/register', payload).then((res) => res.data)
}

export function login(payload) {
  return apiClient.post('/auth/login', payload).then((res) => res.data)
}
