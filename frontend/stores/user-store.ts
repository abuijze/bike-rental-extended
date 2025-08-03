import {defineStore} from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: 'John Doe' as string,
  }),
  getters: {
    getUserName(): string {
      return this.currentUser
    }
  },
  actions: {
    setUser(name: string) {
      this.currentUser = name.trim() || 'John Doe'
    }
  },
})