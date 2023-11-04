import {defineStore} from 'pinia';
import {v4 as uuidv4} from 'uuid';
import URL from '../settings';

const myId = uuidv4().substring(0,6)


export const useBikeStore = defineStore('bikes', {
  state: () => ({
    bikesInTheSystem: new Map(),
  }),
  getters: {
    getBikesAsList(){
        return Array.from(this.bikesInTheSystem.values())
    }
  },
  actions: {
    increment() {
    //   this.counter++;
    },
    async generateBikes(generateBikesParams){
        let res = await fetch(URL+'/?'+new URLSearchParams(generateBikesParams),{
            method: "POST"
        })
    },
    async rentBikeManual(bikeId){
        let res = await fetch(URL+`/requestBike?bikeId=${bikeId}&renter=${myId}`, {
            method: "POST"
        })
    },
    async returnBikeManual(bikeId){
        let res = await fetch(URL+`/returnBike?bikeId=${bikeId}`, {
            method: "POST"
        })
    },
    async generateRentals(rentalsParams){
        let res = await fetch(URL+'/generateRentals?'
        + new URLSearchParams(rentalsParams),{
            method: "POST"
        })
        // console.log(res)
    },
    recordUpdate(update){
        this.bikesInTheSystem.set(update.bikeId, update)
    }
  },
});
