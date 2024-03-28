import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Bus } from '../model/Bus';
import { IonModal } from '@ionic/angular';
import { Coordinates } from '../model/Coordinates';

@Component({
  selector: 'app-bus-details',
  templateUrl: './bus-details.page.html',
  styleUrls: ['./bus-details.page.scss'],
})
export class BusDetailsPage implements OnInit {

  accordionOpen: boolean = false;
  favourite = false;

  constructor() { }

  ngOnInit() {
    if(this.bus.direction == "forward"){
      this.stops = Object.values(this.bus.route.stops.forwardStops);
    }
    else{
      this.stops = Object.values(this.bus.route.stops.backStops);
    }
  }

  @Input() modal!: IonModal;

  @Input() bus!: Bus;
  @Output() back: EventEmitter<void> = new EventEmitter<void>();

  stops: any;

  backToBuses(){
    this.back.emit();
  }

  resizeModal() {
    this.accordionOpen = !this.accordionOpen;
    const breakpoint = this.accordionOpen ? 1 : 0.30;
    this.modal.setCurrentBreakpoint(breakpoint);
  }

  addFavourite(add: boolean) {
    this.favourite = add;
  }

}
