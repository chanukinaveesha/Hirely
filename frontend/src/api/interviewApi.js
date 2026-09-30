import { apiClient } from './apiClient'

export function proposeInterview(payload) {
  return apiClient.post('/interviews', payload).then((res) => res.data)
}

export function rescheduleInterview(interviewId, payload) {
  return apiClient.put(`/interviews/${interviewId}/reschedule`, payload).then((res) => res.data)
}

export function cancelInterview(interviewId) {
  return apiClient.put(`/interviews/${interviewId}/cancel`).then((res) => res.data)
}

export function acceptInterview(interviewId) {
  return apiClient.put(`/interviews/${interviewId}/accept`).then((res) => res.data)
}

export function requestInterviewReschedule(interviewId, payload) {
  return apiClient.put(`/interviews/${interviewId}/request-reschedule`, payload).then((res) => res.data)
}

export function getMyInterviews() {
  return apiClient.get('/interviews/mine').then((res) => res.data)
}

export function getInterviewsForApplication(applicationId) {
  return apiClient.get(`/interviews/application/${applicationId}`).then((res) => res.data)
}

export function getMyPanelInterviews() {
  return apiClient.get('/interviews/panel').then((res) => res.data)
}

export function listPanelMembers() {
  return apiClient.get('/interviews/panel-members').then((res) => res.data)
}

export function submitInterviewFeedback(interviewId, payload) {
  return apiClient.post(`/interviews/${interviewId}/feedback`, payload).then((res) => res.data)
}

export function getFeedbackForInterview(interviewId) {
  return apiClient.get(`/interviews/${interviewId}/feedback`).then((res) => res.data)
}
