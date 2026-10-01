import { apiClient } from './apiClient'

export function createAssessment(payload) {
  return apiClient.post('/assessments', payload).then((res) => res.data)
}

export function getMyAssessments() {
  return apiClient.get('/assessments/mine').then((res) => res.data)
}

export function archiveAssessment(assessmentId) {
  return apiClient.put(`/assessments/${assessmentId}/archive`).then((res) => res.data)
}

export function assignAssessment(assessmentId, payload) {
  return apiClient.post(`/assessments/${assessmentId}/assign`, payload).then((res) => res.data)
}

export function getMyCandidateAssessments() {
  return apiClient.get('/candidate-assessments/mine').then((res) => res.data)
}

export function submitCandidateAssessment(candidateAssessmentId, payload) {
  return apiClient.put(`/candidate-assessments/${candidateAssessmentId}/submit`, payload).then((res) => res.data)
}

export function evaluateCandidateAssessment(candidateAssessmentId, payload) {
  return apiClient.put(`/candidate-assessments/${candidateAssessmentId}/evaluate`, payload).then((res) => res.data)
}

export function getCandidateAssessmentsForApplication(applicationId) {
  return apiClient.get(`/candidate-assessments/application/${applicationId}`).then((res) => res.data)
}

export function compareAssessmentsForVacancy(vacancyId) {
  return apiClient.get(`/candidate-assessments/vacancy/${vacancyId}/compare`).then((res) => res.data)
}
