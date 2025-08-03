export const useBikeUpdates = () => {
  const store = useBikeStore()
  const config = useRuntimeConfig()
  
  const startEventSource = () => {
    if (process.client) {
      const evtSource = new EventSource(`${config.public.apiBase}/bikeUpdatesJson`)
      
      evtSource.onmessage = (event) => {
        store.recordUpdate(JSON.parse(event.data))
      }
      
      evtSource.onerror = (error) => {
        console.error('EventSource error:', error)
      }
      
      // Cleanup on unmount
      onUnmounted(() => {
        evtSource.close()
      })
      
      return evtSource
    }
  }
  
  return {
    startEventSource
  }
}