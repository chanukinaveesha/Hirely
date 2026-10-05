import { create } from 'zustand'
import { getMyAvatar } from '../api/avatarApi'
import { toMediaUrl } from '../api/mediaUrl'

// Holds the signed-in user's own avatar URL so the header can reflect an
// upload/replace/remove immediately without every consumer re-fetching.
// Keyed by user id (not just a "loaded" flag) so switching accounts in the
// same tab doesn't show the previous user's cached avatar.
export const useAvatarStore = create((set, get) => ({
  avatarUrl: null,
  loadedForUserId: null,

  setAvatarUrl: (relativePath, userId) => set({ avatarUrl: toMediaUrl(relativePath), loadedForUserId: userId }),

  loadForUser: async (userId) => {
    if (!userId || get().loadedForUserId === userId) return
    try {
      const data = await getMyAvatar()
      set({ avatarUrl: data.hasAvatar ? toMediaUrl(data.imageUrl) : null, loadedForUserId: userId })
    } catch {
      set({ avatarUrl: null, loadedForUserId: userId })
    }
  },
}))
