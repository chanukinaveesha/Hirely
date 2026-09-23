import { apiClient } from './apiClient'

export function listClientCompanies() {
  return apiClient.get('/client-companies').then((res) => res.data)
}
