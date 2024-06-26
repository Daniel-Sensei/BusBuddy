import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Bus } from '../model/Bus';
import { IonModal } from '@ionic/angular';
import { BusService } from '../service/bus.service';

import { PreferencesService } from '../service/preferences.service';

@Component({
  selector: 'app-bus-details',
  templateUrl: './bus-details.page.html',
  styleUrls: ['./bus-details.page.scss'],
})
export class BusDetailsPage implements OnInit {

  accordionOpen: boolean = false;
  favourite = false;
  destination: string = "";
  arrivals: any;

  @Input() modal!: IonModal;

  @Input() bus!: Bus;
  @Output() back: EventEmitter<void> = new EventEmitter<void>();

  stops: any;

  constructor(private busService: BusService, private preferencesService: PreferencesService) { }

  async ngOnInit() {
    this.checkFavourite();
    
    this.getStopsAndDestination();
    await this.getArrivals();

    this.busService.getBusFromRealtimeDatabase(this.bus.id).subscribe(async (bus) => {
      if(this.bus.direction !== bus.direction){
        this.bus.direction = bus.direction;
        this.getStopsAndDestination();
        await this.getArrivals();
      }
      if(this.bus.lastStop !== bus.lastStop){
        this.bus.lastStop = bus.lastStop;
      }
    });
  }

  getDestination(back = false): string {
    let destination = "";
    let code = this.bus.route.code.split("_")[1];

    if(!back){
      destination = code.split("-").join(" - ");
    }
    else{
      for(let i = code.split("-").length - 1; i >= 0; i--){
        destination += code.split("-")[i];
        if(i !== 0){
          destination += " - ";
        }
      }
    }
    return destination;
  }

  getNextArrivalByStop(stopId: string) {
    //find the stopId as a key in the arrivals map
    //the value of the key is an array of arrival times
    //return the first element of the array
    let arrival = this.arrivals[stopId];
    if(arrival){
      return arrival[0];
    }
  }

  getNextArrivalsByStop(stopId: string) {
    //find the stopId as a key in the arrivals map
    //the value of the key is an array of arrival times
    //return all the element joined by a " - "
    let arrival = this.arrivals[stopId];
    if(arrival){
      return arrival.join(" - ");
    }
  }

  getNextArrivalsByStopMinusFirst(stopId: string) {
    //find the stopId as a key in the arrivals map
    //the value of the key is an array of arrival times
    //return all the element joined by a " - ", excluding the first element
    //don't use shift() because it modifies the original array
    let arrival = this.arrivals[stopId];
    if(arrival){
      let newArrival = arrival.slice(1);
      return newArrival.join(" - ");
    }
  }

  getStopsAndDestination(){
    if(this.bus.direction === "back"){
      this.stops = Object.values(this.bus.route.stops.backStops);
      this.destination = this.getDestination(true);
    }
    else{
      this.stops = Object.values(this.bus.route.stops.forwardStops);
      this.destination = this.getDestination();
    }
  }
  
  async getArrivals(){
    this.arrivals = await this.busService.getArrivalsByBusAndDirection(this.bus.id, this.bus.direction);
  }


  backToBuses(){
    this.back.emit();
  }

  resizeModal() {
    this.accordionOpen = !this.accordionOpen;
    const breakpoint = this.accordionOpen ? 1 : 0.30;
    this.modal.setCurrentBreakpoint(breakpoint);
  }

  checkFavourite() {
    this.preferencesService.getFavorites('favouriteRoutes').then(routes => {
      this.favourite = routes.includes((this.bus.route.id || '') + '_' + (this.bus.route.code.split("_")[0] || '') + " " + (this.bus.route.company || ''));
    });
  }

  addFavourite(add: boolean) {
    this.favourite = add;
    if(add){
    this.preferencesService.addToFavorites('favouriteRoutes', (this.bus.route.id || '') + '_' + (this.bus.route.code.split("_")[0] || '') + " " + (this.bus.route.company || ''));
    }
    else{
      this.preferencesService.removeFromFavorites('favouriteRoutes', (this.bus.route.id || '') + '_' + (this.bus.route.code.split("_")[0] || '') + " " + (this.bus.route.company || ''));
    }
  }

}
