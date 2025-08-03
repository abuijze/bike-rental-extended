<template>
  <div class="min-h-screen">
    <!-- Top-right controls -->
    <div class="fixed top-4 right-4 z-50 flex items-center gap-2">
      <!-- Account component -->
      <div class="flex items-center gap-2">
        <UDropdown :items="accountItems" :popper="{ placement: 'bottom-end' }">
          <UButton
            :label="userStore.getUserName"
            icon="i-heroicons-user-circle"
            variant="ghost"
            size="sm"
            :ui="{ rounded: 'rounded-full' }"
          />
        </UDropdown>
      </div>
      
      <!-- Dark mode toggle -->
      <UButton
        @click="toggleColorMode"
        :icon="$colorMode.value === 'dark' ? 'i-heroicons-sun' : 'i-heroicons-moon'"
        variant="ghost"
        size="lg"
        square
        :ui="{ rounded: 'rounded-full' }"
        :title="$colorMode.value === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'"
      />
    </div>

    <UContainer class="py-8" :ui="{ constrained: 'max-w-none px-4 sm:px-6 lg:px-8' }">
      <div class="w-full">
        <h1 class="text-4xl font-bold mb-8 text-center">🚴 AxonIQ Bike Rental System</h1>
        
        <!-- Two-column layout -->
        <div class="grid grid-cols-1 xl:grid-cols-4 gap-6">
          <!-- Left column: Controls -->
          <div class="xl:col-span-1 space-y-6">
            <!-- Register Bikes Section -->
            <UCard>
              <template #header>
                <h2 class="text-xl font-semibold">Register Bikes</h2>
              </template>
              
              <div class="space-y-4">
                <UFormGroup label="Number of bikes">
                  <UInput v-model="newBikeParams.count" type="number" />
                </UFormGroup>
                
                <UFormGroup label="Bike type">
                  <UInput v-model="newBikeParams.type" />
                </UFormGroup>
                
                <UButton 
                  @click="generateBikes(newBikeParams)" 
                  icon="i-heroicons-plus-circle"
                  block
                >
                  Generate Bikes
                </UButton>
              </div>
            </UCard>

            <!-- Rent Bike Section -->
            <UCard>
              <template #header>
                <h2 class="text-xl font-semibold">Rent Bike</h2>
              </template>
              
              <div class="space-y-4">
                <UFormGroup label="Bike type">
                  <UInput v-model="rentalsParams.bikeType" />
                </UFormGroup>
                
                <UFormGroup label="Loops">
                  <UInput v-model="rentalsParams.loops" type="number" />
                </UFormGroup>
                
                <UFormGroup label="Concurrency">
                  <UInput v-model="rentalsParams.concurrency" type="number" />
                </UFormGroup>
                
                <UFormGroup label="Delay (+/- 25%)">
                  <UInput v-model="rentalsParams.delay" type="number" />
                </UFormGroup>
                
                <UFormGroup label="Abandon payment rate (1/n)">
                  <UInput v-model="rentalsParams.abandonPaymentFactor" type="number" />
                </UFormGroup>
                
                <UButton 
                  @click="generateRentals(rentalsParams)" 
                  icon="i-heroicons-currency-dollar"
                  block
                >
                  Rent
                </UButton>
              </div>
            </UCard>
          </div>

          <!-- Right column: Bikes Table -->
          <div class="xl:col-span-3">
            <UCard class="h-fit overflow-hidden">
              <template #header>
                <h2 class="text-xl font-semibold">Bikes Overview ({{ store.getBikesAsList.length }})</h2>
              </template>
              
              <div class="overflow-x-auto">
                <UTable 
                  :rows="store.getBikesAsList" 
                  :columns="columns"
                  :loading="false"
                  :ui="{ 
                    wrapper: 'overflow-visible',
                    base: 'min-w-full',
                    divide: 'divide-y divide-gray-300 dark:divide-gray-700'
                  }"
                >
                <template #status-data="{ row }">
                  <UBadge 
                    :color="getStatusColor(row.status)" 
                    variant="soft"
                    class="flex items-center gap-1"
                  >
                    <UIcon :name="getStatusIcon(row.status)" />
                    {{ row.status }}
                  </UBadge>
                </template>
                
                <template #actions-data="{ row }">
                  <div class="flex gap-1">
                    <UButton
                      v-if="row.status === 'AVAILABLE'"
                      @click="requestBikeAndOpenPayment(row.bikeId)"
                      icon="i-heroicons-map-pin"
                      size="xs"
                      variant="outline"
                      color="primary"
                      :ui="{ rounded: 'rounded-full' }"
                      title="Request this bike"
                    />
                    <UButton
                      v-if="row.status === 'REQUESTED' && row.paymentRef"
                      @click="openPaymentModal(row)"
                      icon="i-heroicons-credit-card"
                      size="xs"
                      variant="outline"
                      color="green"
                      :ui="{ rounded: 'rounded-full' }"
                      title="Process payment"
                    />
                    <UButton
                      v-if="row.status === 'RENTED'"
                      @click="returnBikeManual(row.bikeId)"
                      icon="i-heroicons-arrow-uturn-left"
                      size="xs"
                      variant="outline"
                      color="orange"
                      :ui="{ rounded: 'rounded-full' }"
                      title="Return this bike"
                    />
                  </div>
                </template>
                </UTable>
              </div>
            </UCard>
          </div>
        </div>
      </div>
    </UContainer>

    <!-- Payment Modal -->
    <UModal v-model="isPaymentModalOpen">
      <UCard :ui="{ ring: '', divide: 'divide-y divide-gray-300 dark:divide-gray-700' }">
        <template #header>
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
              Process Payment
            </h3>
            <UButton
              color="gray"
              variant="ghost"
              icon="i-heroicons-x-mark-20-solid"
              @click="isPaymentModalOpen = false"
            />
          </div>
        </template>

        <div class="space-y-4">
          <div v-if="selectedBike">
            <p class="text-sm text-gray-600 dark:text-gray-400">
              <strong>Bike ID:</strong> {{ selectedBike.bikeId }}
            </p>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              <strong>Bike Type:</strong> {{ selectedBike.bikeType }}
            </p>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              <strong>Payment Reference:</strong> {{ selectedBike.paymentRef }}
            </p>
          </div>
          
          <div class="bg-gray-50 dark:bg-gray-800 p-4 rounded-lg">
            <p class="text-sm text-gray-700 dark:text-gray-300">
              Would you like to accept or refuse this payment?
            </p>
          </div>
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton
              color="red"
              variant="outline"
              @click="refusePayment"
              :loading="isProcessingPayment"
            >
              Refuse Payment
            </UButton>
            <UButton
              color="green"
              @click="acceptPayment"
              :loading="isProcessingPayment"
            >
              Accept Payment
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>

    <!-- Login Modal -->
    <UModal v-model="isLoginModalOpen">
      <UCard :ui="{ ring: '', divide: 'divide-y divide-gray-300 dark:divide-gray-700' }">
        <template #header>
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
              Change User
            </h3>
            <UButton
              color="gray"
              variant="ghost"
              icon="i-heroicons-x-mark-20-solid"
              @click="isLoginModalOpen = false"
            />
          </div>
        </template>

        <div class="space-y-4">
          <div>
            <UFormGroup label="Enter your name">
              <UInput 
                ref="loginInput"
                v-model="loginName" 
                placeholder="Enter your name"
                @keyup.enter="loginUser"
              />
            </UFormGroup>
          </div>
          
          <div class="bg-gray-50 dark:bg-gray-800 p-4 rounded-lg">
            <p class="text-sm text-gray-700 dark:text-gray-300">
              This will be used as your renter name when requesting bikes.
            </p>
          </div>
        </div>

        <template #footer>
          <div class="flex justify-end gap-3">
            <UButton
              color="gray"
              variant="outline"
              @click="isLoginModalOpen = false"
            >
              Cancel
            </UButton>
            <UButton
              color="primary"
              @click="loginUser"
            >
              Update Name
            </UButton>
          </div>
        </template>
      </UCard>
    </UModal>
  </div>
</template>

<script setup>
const store = useBikeStore()
const userStore = useUserStore()
const { startEventSource } = useBikeUpdates()
const colorMode = useColorMode()

const toggleColorMode = () => {
  colorMode.preference = colorMode.preference === 'dark' ? 'light' : 'dark'
}

// Initialize color mode properly on mount
onMounted(() => {
  // Ensure color mode is properly initialized
  if (!colorMode.preference) {
    colorMode.preference = 'system'
  }
  startEventSource()
})

// Payment modal state
const isPaymentModalOpen = ref(false)
const selectedBike = ref(null)
const isProcessingPayment = ref(false)

// Login modal state
const isLoginModalOpen = ref(false)
const loginName = ref('')
const loginInput = ref(null)

// Account dropdown items
const accountItems = [[
  {
    label: 'Change User',
    icon: 'i-heroicons-user',
    click: () => openLoginModal()
  }
]]

const newBikeParams = ref({
  count: 10,
  type: "mountainbike"
})

const rentalsParams = ref({
  bikeType: "mountainbike",
  loops: "64",
  concurrency: "8",
  abandonPaymentFactor: 0,
  delay: 100
})

const columns = [
  { key: 'bikeId', label: 'Bike ID' },
  { key: 'bikeType', label: 'Bike Type' },
  { key: 'location', label: 'Location' },
  { key: 'renter', label: 'Renter' },
  { key: 'status', label: 'Status' },
  { key: 'actions', label: 'Actions' }
]

function getStatusColor(status) {
  switch (status) {
    case 'AVAILABLE': return 'green'
    case 'REQUESTED': return 'yellow'
    case 'RENTED': return 'blue'
    default: return 'gray'
  }
}

function getStatusIcon(status) {
  switch (status) {
    case 'AVAILABLE': return 'i-heroicons-check-circle'
    case 'REQUESTED': return 'i-heroicons-exclamation-triangle'
    case 'RENTED': return 'i-heroicons-user'
    default: return 'i-heroicons-question-mark-circle'
  }
}

function generateBikes(params) {
  store.generateBikes(params)
}

function generateRentals(params) {
  store.generateRentals(params)
}

async function requestBikeAndOpenPayment(bikeId) {
  try {
    const paymentRef = await store.rentBikeManual(bikeId, userStore.getUserName)
    
    if (paymentRef) {
      const bike = store.getBikesAsList.find(b => b.bikeId === bikeId)
      if (bike) {
        bike.paymentRef = paymentRef
        openPaymentModal(bike)
      } else {
        console.error('Could not find bike after request')
      }
    } else {
      console.error('No payment reference returned from bike request')
    }
  } catch (error) {
    console.error('Failed to request bike:', error)
    alert('Failed to request bike: ' + error.message)
  }
}


async function returnBikeManual(bikeId) {
  await store.returnBikeManual(bikeId)
}

function openPaymentModal(bike) {
  selectedBike.value = bike
  isPaymentModalOpen.value = true
}

async function acceptPayment() {
  if (!selectedBike.value?.paymentRef) return
  
  isProcessingPayment.value = true
  try {
    await store.acceptPayment(selectedBike.value.paymentRef)
    isPaymentModalOpen.value = false
    selectedBike.value = null
  } catch (error) {
    console.error('Failed to accept payment:', error)
  } finally {
    isProcessingPayment.value = false
  }
}

async function refusePayment() {
  if (!selectedBike.value?.paymentRef) return
  
  isProcessingPayment.value = true
  try {
    await store.refusePayment(selectedBike.value.paymentRef)
    isPaymentModalOpen.value = false
    selectedBike.value = null
  } catch (error) {
    console.error('Failed to refuse payment:', error)
    alert('Failed to refuse payment: ' + error.message)
  } finally {
    isProcessingPayment.value = false
  }
}

function openLoginModal() {
  loginName.value = userStore.getUserName
  isLoginModalOpen.value = true
  
  // Focus and select text in the input field after modal opens
  nextTick(() => {
    if (loginInput.value?.input) {
      loginInput.value.input.focus()
      loginInput.value.input.select()
    }
  })
}

function loginUser() {
  if (loginName.value.trim()) {
    userStore.setUser(loginName.value.trim())
    isLoginModalOpen.value = false
    loginName.value = ''
  }
}
</script>