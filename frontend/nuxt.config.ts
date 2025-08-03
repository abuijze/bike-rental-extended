export default defineNuxtConfig({
  devtools: { enabled: true },
  ssr: false,
  devServer: {
    port: 3000
  },
  app: {
    head: {
      title: 'Bike Rental System',
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' },
        { rel: 'icon', type: 'image/png', sizes: '16x16', href: '/icons/favicon-16x16.png' },
        { rel: 'icon', type: 'image/png', sizes: '32x32', href: '/icons/favicon-32x32.png' },
        { rel: 'icon', type: 'image/png', sizes: '96x96', href: '/icons/favicon-96x96.png' },
        { rel: 'icon', type: 'image/png', sizes: '128x128', href: '/icons/favicon-128x128.png' },
        { rel: 'apple-touch-icon', sizes: '128x128', href: '/icons/favicon-128x128.png' }
      ]
    }
  },
  modules: [
    '@nuxt/ui',
    '@pinia/nuxt'
  ],
  runtimeConfig: {
    public: {
      apiBase: process.env.API_BASE || (process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : '__DYNAMIC_API_BASE__'),
      paymentServiceBase: process.env.PAYMENT_SERVICE_BASE || (process.env.NODE_ENV === 'development' ? 'http://localhost:8081' : '__DYNAMIC_PAYMENT_BASE__'),
      isDev: process.env.NODE_ENV === 'development'
    }
  },
  nitro: {
    preset: 'static',
    output: {
      dir: 'target/frontend-build'
    }
  }
})