import { apiClient } from './apiClient'

export function getDashboard() {
  return apiClient.get('/dashboard').then((res) => res.data)
}
