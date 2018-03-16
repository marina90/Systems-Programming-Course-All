import {Component, Directive, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Directive({
  selector: 'input[type=datetime-local]',
  host: {
    '[value]': '_date',
    '(change)':'onDateChange($event.target.value)'
  }
})
export class DateDirective{
  private _date: string;
  @Input() set date(d: Date) {
    this._date = this.toDateString(d);
  }
  @Output() dateChange: EventEmitter<Date>;
  constructor() {
    this.date = new Date();
    this.dateChange = new EventEmitter();
  }

  private toDateString(date: Date): string {
    return (date.getFullYear().toString() + '-' + ("0" + (date.getMonth() + 1)).slice(-2) + '-' + ("0" + (date.getDate())).slice(-2))
      + 'T' + date.toTimeString().slice(0,5);
  }
  private parseDateString(date:string): Date {
    date = date.replace('T','-');
    let parts: string[] = date.split('-');
    let timeParts: string[] = parts[3].split(':');

    // new Date(year, month [, day [, hours[, minutes[, seconds[, ms]]]]])
    return new Date(+parts[0], +parts[1]-1, +parts[2], +timeParts[0], +timeParts[1]); // Note: months are 0-based

  }

  private onDateChange(value: string): void {
    if (value != this._date) {


      var parsedDate = this.parseDateString(value);

      // check if date is valid first
      if (parsedDate.getTime() != NaN) {
        this._date = value;
        this.dateChange.emit(parsedDate);
      }
    }
  }
}

