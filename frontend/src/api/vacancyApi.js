import { apiClient } from './apiClient'

export function createVacancy(payload) {
  return apiClient.post('/vacancies', payload).then((res) => res.data)
}

export function updateVacancy(id, payload) {
  return apiClient.put(`/vacancies/${id}`, payload).then((res) => res.data)
}

export function publishVacancy(id) {
  return apiClient.put(`/vacancies/${id}/publish`).then((res) => res.data)
}

export function closeVacancy(id) {
  return apiClient.put(`/vacancies/${id}/close`).then((res) => res.data)
}

export function getVacancy(id) {
  return apiClient.get(`/vacancies/${id}`).then((res) => res.data)
}

export function getMyVacancies() {
  return apiClient.get('/vacancies/mine').then((res) => res.data)
}

export function searchVacancies(filters) {
  return apiClient.get('/vacancies/search', { params: filters }).then((res) => res.data)
}
