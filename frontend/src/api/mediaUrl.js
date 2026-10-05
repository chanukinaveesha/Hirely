// Backend platform endpoints return media paths relative to the API root
// (e.g. "/avatars/5/image", no "/api" prefix) since an <img src> is a plain
// GET, not an apiClient request. This turns that into an absolute URL using
// the same base the rest of the app already points at.
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

export function toMediaUrl(path) {
  if (!path) return null
  return `${BASE_URL}${path}`
}
