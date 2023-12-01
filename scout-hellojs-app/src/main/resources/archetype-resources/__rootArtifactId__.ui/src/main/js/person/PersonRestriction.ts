import $ from 'jquery';

export class PersonRestriction {
  firstName: string;
  lastName: string;

  constructor() {
    this.firstName = null;
    this.lastName = null;
  }

  init(model: any) {
    ${symbol_dollar}.extend(this, model);
  }

  setFirstName(firstName: string) {
    this.firstName = firstName;
  }

  setLastName(lastName: string) {
    this.lastName = lastName;
  }
}
