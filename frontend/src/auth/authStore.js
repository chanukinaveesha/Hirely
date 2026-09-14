import { create } from 'zustand'

const STORAGE_KEY = 'recruitsystem.auth'

function loadPersistedAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function persistAuth(auth) {
  if (auth) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(auth))
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
}

const persisted = loadPersistedAuth()

export const useAuthStore = create((set, get) => ({
  token: persisted?.token ?? null,
  user: persisted?.user ?? null,
  role: persisted?.user?.role ?? null,

  login: ({ token, ...user }) => {
    persistAuth({ token, user })
    set({ token, user, role: user.role })
  },

  logout: () => {
    persistAuth(null)
    set({ token: null, user: null, role: null })
  },

  isAuthenticated: () => Boolean(get().token),
}))
