import * as $ from 'jquery';

export class PersonRestriction {
  resourceType: string;
  firstName: string;
  lastName: string;

  constructor() {
    this.resourceType = 'PersonRestriction';
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
