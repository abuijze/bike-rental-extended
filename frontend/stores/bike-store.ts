import {defineStore} from 'pinia'

interface Bike {
  bikeId: string
  bikeType: string
  location: string
  renter: string
  status: 'AVAILABLE' | 'REQUESTED' | 'RENTED'
  paymentRef?: string
}

interface GenerateBikesParams {
  count: number
  type: string
}

interface RentalsParams {
  bikeType: string
  loops: string
  concurrency: string
  abandonPaymentFactor: number
  delay: number
}

export const useBikeStore = defineStore('bikes', {
  state: () => ({
    bikesInTheSystem: new Map<string, Bike>(),
  }),
  getters: {
    getBikesAsList(): Bike[] {
      return Array.from(this.bikesInTheSystem.values())
    }
  },
  actions: {
    async generateBikes(generateBikesParams: GenerateBikesParams) {
      const config = useRuntimeConfig()
      const url = config.public.apiBase
      
      await fetch(`${url}/bikes?${new URLSearchParams({
        count: generateBikesParams.count.toString(),
        type: generateBikesParams.type
      })}`, {
        method: "POST"
      })
    },
    
    async rentBikeManual(bikeId: string, renterName?: string) {
      const config = useRuntimeConfig()
      const url = config.public.apiBase
      
      // If no renter name provided, try to get it from user store
      let renter = renterName
      if (!renter) {
        try {
          const userStore = useUserStore()
          renter = userStore.getUserName
        } catch (error) {
          console.error('Could not access user store:', error)
          renter = 'John Doe' // fallback
        }
      }
      
      const response = await fetch(`${url}/requestBike?bikeId=${bikeId}&renter=${encodeURIComponent(renter)}`, {
        method: "POST"
      })
      
      if (response.ok) {
        const paymentRef = await response.text()
        // Store payment reference for this bike
        const bike = this.bikesInTheSystem.get(bikeId)
        if (bike) {
          bike.paymentRef = paymentRef.trim()
          this.bikesInTheSystem.set(bikeId, bike)
        }
        return paymentRef.trim()
      }
      throw new Error('Failed to request bike')
    },
    
    async returnBikeManual(bikeId: string) {
      const config = useRuntimeConfig()
      const url = config.public.apiBase
      
      await fetch(`${url}/returnBike?bikeId=${bikeId}`, {
        method: "POST"
      })
    },
    
    async generateRentals(rentalsParams: RentalsParams) {
      const config = useRuntimeConfig()
      const url = config.public.apiBase
      
      await fetch(`${url}/generateRentals?${new URLSearchParams({
        bikeType: rentalsParams.bikeType,
        loops: rentalsParams.loops,
        concurrency: rentalsParams.concurrency,
        abandonPaymentFactor: rentalsParams.abandonPaymentFactor.toString(),
        delay: rentalsParams.delay.toString()
      })}`, {
        method: "POST"
      })
    },
    
    recordUpdate(update: Bike) {
      // Preserve payment reference when updating from server
      const existingBike = this.bikesInTheSystem.get(update.bikeId)
      if (existingBike?.paymentRef) {
        update.paymentRef = existingBike.paymentRef
      }
      this.bikesInTheSystem.set(update.bikeId, update)
    },
    
    async acceptPayment(paymentRef: string) {
      const config = useRuntimeConfig()
      const paymentServiceUrl = 'http://localhost:8081' // Payment service runs on port 8081
      
      // First find the payment ID using the reference
      const findResponse = await fetch(`${paymentServiceUrl}/findPayment?reference=${paymentRef}`)
      if (!findResponse.ok) {
        throw new Error('Failed to find payment ID')
      }
      const paymentId = await findResponse.text()
      
      // Then accept the payment using the payment ID
      const acceptResponse = await fetch(`${paymentServiceUrl}/acceptPayment?id=${paymentId.trim()}`, {
        method: "POST"
      })
      if (!acceptResponse.ok) {
        throw new Error('Failed to accept payment')
      }
    },
    
    async refusePayment(paymentRef: string) {
      const config = useRuntimeConfig()
      const paymentServiceUrl = 'http://localhost:8081' // Payment service runs on port 8081
      
      // First find the payment ID using the reference
      const findResponse = await fetch(`${paymentServiceUrl}/findPayment?reference=${paymentRef}`)
      if (!findResponse.ok) {
        console.error('Failed to find payment ID, status:', findResponse.status)
        throw new Error('Failed to find payment ID')
      }
      const paymentId = await findResponse.text()
      
      // Then reject the payment using the payment ID
      const rejectResponse = await fetch(`${paymentServiceUrl}/rejectPayment?id=${paymentId.trim()}`, {
        method: "POST"
      })
      if (!rejectResponse.ok) {
        console.error('Failed to reject payment, status:', rejectResponse.status)
        throw new Error('Failed to reject payment')
      }
    }
  },
})