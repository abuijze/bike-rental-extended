<template>
  <q-page padding center fixed-center class="rent-container">


    <div class="row">
      <div class="columns">
        <div class="text-h4">Register Bikes</div>
        <div class="row q-gutter-sm">
          <q-input label="Number of bikes" stack-label dense v-model="newBikeParams.count" />
          <q-input label="Bike type" stack-label dense v-model="newBikeParams.type" />
          <q-btn label="Generate Bikes" icon="add_circle_outline" @click="generateBikes(newBikeParams)" />
        </div>
      </div>

    </div>

    <div class="row q-pt-md full-width">
      <div class="columns">
        <div class="text-h4">Rent Bike</div>
        <div class="row q-gutter-sm">
          <q-input label="Bike type" stack-label dense v-model="rentalsParams.bikeType" />
          <q-input label="Loops" stack-label dense v-model="rentalsParams.loops" />
          <q-input label="Concurrency" stack-label dense v-model="rentalsParams.concurrency" />
          <q-input label="Delay (+/- 25%)" stack-label dense v-model="rentalsParams.delay" />
          <q-input label="Abandon payment rate (1/n)" stack-label dense v-model="rentalsParams.abandonPaymentFactor" />

          <q-btn icon="monetization_on" label="Rent" @click="generateRentals(rentalsParams)" />
    
        </div>
      </div>
    </div>


    <div class="row q-pt-md fit">
      <q-table
        dense
        class="fit"
        v-if="store.bikesInTheSystem"
        title="Bikes"
        :rows="store.getBikesAsList"
        :columns="columns"
        row-key="id"
        rows-per-page="20"
        :rows-per-page-options="paginationOptions"
      >
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td key="bikeId" :props="props">{{ props.row.bikeId }}</q-td>
            <q-td key="bikeType" :props="props">{{ props.row.bikeType }}</q-td>
            <q-td key="location" :props="props">{{ props.row.location }}</q-td>
            <q-td key="renter" :props="props">{{ props.row.renter }}</q-td>
            <q-td key="status" :props="props">
                {{ props.row.status }}
                <q-icon v-if="props.row.status ==='AVAILABLE'" name="done" color="positive" />
                <q-icon v-if="props.row.status ==='REQUESTED'" name="warning" color="warning" />
                <q-icon v-if="props.row.status ==='RENTED'" name="directions_bike" color="primary" />
              </q-td>
          </q-tr>
      </template>
      </q-table>
    </div>

  </q-page>
</template>

<script setup>
import {ref} from 'vue'
import {useBikeStore} from '../stores/bike-store'
import URL from '../settings'


const newBikeParams = ref({
  count: 10,
  type: "mountainbike"
})


const rentalsParams = ref({
            bikeType : "mountainbike",
            loops : "64",
            concurrency : "8",
            abandonPaymentFactor: 0,
            delay: 100
        })

const store = useBikeStore()

// Event source spec does not support headers, so we go for another url
// See https://html.spec.whatwg.org/multipage/server-sent-events.html#the-eventsource-interface
const evtSource = new EventSource(URL+"/bikeUpdatesJson");
evtSource.onmessage = (event) => {
  store.recordUpdate(JSON.parse(event.data))
};

const paginationOptions=[10,25,50,100]

const columns = [
  { name: 'bikeId', align: 'left', label: 'BikeId', field: 'bikeId', sortable: true },
  { name: 'bikeType', align: 'left',label: 'bikeType', field: 'bikeType', sortable: true },
  { name: 'location', align: 'left',label: 'location', field: 'location', sortable: true },
  { name: 'renter', align: 'left',label: 'renter', field: 'renter', sortable: true },
  { name: 'status', align: 'right',label: 'status', field: 'status', sortable: true },
  // { name: 'actions', label: 'actions', field: 'actions', sortable: true },
]

function generateBikes(params){
  store.generateBikes(params)
}
function generateRentals(params){
  store.generateRentals(params)
}

async function rentBikeManual(id){
  await store.rentBikeManual(id)
}
async function returnBikeManual(id){
  await store.returnBikeManual(id)
}

</script>
<style scoped>

.rent-container{
  max-width: 950px;
  margin: 0 auto;
}

</style>