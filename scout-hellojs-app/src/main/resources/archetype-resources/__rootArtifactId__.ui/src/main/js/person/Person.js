import * as $ from 'jquery';

export default class Person {

  constructor() {
    this.resourceType = 'Person';
    this.personId = null;
    this.firstName = null;
    this.lastName = null;
    this.salary = null;
    this.external = null;
  }

  static EVENT_TYPE = 'person';

  init(model) {
    ${symbol_dollar}
  .
    extend(this, model);
  }

  setFirstName(firstName) {
    this.firstName = firstName;
  }

  setLastName(lastName) {
    this.lastName = lastName;
  }

  setPersonId(id) {
    this.personId = id;
  }


  setSalary(salary) {
    this.salary = salary;
  }

  setExternal(external) {
    this.external = external;
  }
}
