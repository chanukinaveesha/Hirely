import axios from 'axios'
import { useAuthStore } from '../auth/authStore'
import { toast } from '../components/common/toastStore'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  const { token } = useAuthStore.getState()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status

    if (status === 401) {
      useAuthStore.getState().logout()
    }

    const message = error.response?.data?.message ?? error.message ?? 'An unexpected error occurred'

    // Client-side validation/business errors (400/404/409/422 etc.) are shown
    // inline by the calling form. Only surface a toast for things the caller
    // couldn't have anticipated: network failures, server errors, or an
    // expired session.
    if (!status || status >= 500 || status === 401) {
      toast.error(message, status === 401 ? 'Session expired' : 'Something went wrong')
    }

    return Promise.reject({ ...error, message })
  },
)
