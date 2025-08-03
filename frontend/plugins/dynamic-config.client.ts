export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()
  
  // Only override URLs in production (when not in dev mode)
  if (process.client && !config.public.isDev) {
    const currentHost = window.location.origin
    
    // Override API base if it contains placeholder or localhost
    if (config.public.apiBase.includes('localhost') || config.public.apiBase.includes('__DYNAMIC_API_BASE__')) {
      // In production, the backend runs on the same host/port as the frontend
      config.public.apiBase = currentHost
    }
    
    // Override payment service base if it contains placeholder or localhost
    if (config.public.paymentServiceBase.includes('localhost') || config.public.paymentServiceBase.includes('__DYNAMIC_PAYMENT_BASE__')) {
      // In production, assume payment service is also on the same host
      // (either same port or behind reverse proxy)
      config.public.paymentServiceBase = currentHost
    }
  }
})