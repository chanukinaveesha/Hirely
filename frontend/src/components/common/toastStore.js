import { create } from 'zustand'

let nextId = 0

export const useToastStore = create((set) => ({
  toasts: [],

  addToast: ({ variant = 'info', title, message, duration = 5000 }) => {
    const id = ++nextId
    set((state) => ({ toasts: [...state.toasts, { id, variant, title, message }] }))
    if (duration > 0) {
      setTimeout(() => {
        set((state) => ({ toasts: state.toasts.filter((toast) => toast.id !== id) }))
      }, duration)
    }
    return id
  },

  removeToast: (id) => {
    set((state) => ({ toasts: state.toasts.filter((toast) => toast.id !== id) }))
  },
}))

// Imperative helpers so any module (including the API client) can raise a
// toast without needing to be a React component.
function push(variant) {
  return (message, title) => useToastStore.getState().addToast({ variant, message, title })
}

export const toast = {
  success: push('success'),
  error: push('error'),
  warning: push('warning'),
  info: push('info'),
}
