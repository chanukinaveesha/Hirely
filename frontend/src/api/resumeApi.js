import { apiClient } from './apiClient'

export function getMyResume() {
  return apiClient.get('/resumes/me').then((res) => res.data)
}

export function uploadResume(file) {
  const formData = new FormData()
  formData.append('file', file)
  return apiClient
    .put('/resumes', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((res) => res.data)
}

export async function downloadMyResume() {
  const response = await apiClient.get('/resumes/me/download', { responseType: 'blob' })
  triggerBrowserDownload(response.data, extractFilename(response.headers['content-disposition'], 'resume'))
}

export async function downloadApplicantResume(applicationId, fallbackName) {
  const response = await apiClient.get(`/resumes/applications/${applicationId}/download`, { responseType: 'blob' })
  triggerBrowserDownload(response.data, extractFilename(response.headers['content-disposition'], fallbackName))
}

function extractFilename(contentDisposition, fallback) {
  const match = contentDisposition?.match(/filename="?([^"]+)"?/)
  return match ? match[1] : fallback
}

function triggerBrowserDownload(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
