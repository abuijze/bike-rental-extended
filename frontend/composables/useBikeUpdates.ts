const isConnected = ref(false)

export const useBikeUpdates = () => {
  const store = useBikeStore()
  const config = useRuntimeConfig()

  const startEventSource = () => {
    if (process.client) {
      const evtSource = new EventSource(`${config.public.apiBase}/bikeUpdatesJson`)

      evtSource.onopen = () => {
        isConnected.value = true
      }

      evtSource.onmessage = (event) => {
        store.recordUpdate(JSON.parse(event.data))
      }

      evtSource.onerror = () => {
        isConnected.value = false
      }

      // Cleanup on unmount
      onUnmounted(() => {
        evtSource.close()
        isConnected.value = false
      })

      return evtSource
    }
  }

  return {
    startEventSource,
    isConnected
  }
}
