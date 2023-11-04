<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <!-- <q-toolbar>


        <q-toolbar-title>
          AxonIQ Bike Rental
        </q-toolbar-title>

      </q-toolbar> -->
    </q-header>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
import {defineComponent} from 'vue'


export default defineComponent({
  name: 'MainLayout',

  components: {
  },

  setup () {

    return {
    }
  }
})
</script>
