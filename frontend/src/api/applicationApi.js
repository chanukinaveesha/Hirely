import { apiClient } from './apiClient'

export function applyToVacancy(vacancyId) {
  return apiClient.post('/applications', { vacancyId }).then((res) => res.data)
}

export function getMyApplications() {
  return apiClient.get('/applications/mine').then((res) => res.data)
}

export function withdrawApplication(applicationId) {
  return apiClient.put(`/applications/${applicationId}/withdraw`)
}

export function getApplicantsForVacancy(vacancyId) {
  return apiClient.get(`/applications/vacancy/${vacancyId}`).then((res) => res.data)
}

export function shortlistApplication(applicationId) {
  return apiClient.put(`/applications/${applicationId}/shortlist`).then((res) => res.data)
}

export function rejectApplication(applicationId) {
  return apiClient.put(`/applications/${applicationId}/reject`).then((res) => res.data)
}

export function selectApplication(applicationId) {
  return apiClient.put(`/applications/${applicationId}/select`).then((res) => res.data)
}
