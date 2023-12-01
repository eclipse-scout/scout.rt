import $ from 'jquery';

export class Person {
  personId: string;
  firstName: string;
  lastName: string;
  salary: number;
  external: boolean;

  constructor() {
    this.personId = null;
    this.firstName = null;
    this.lastName = null;
    this.salary = null;
    this.external = null;
  }

  static ENTITY_TYPE = 'person';

  init(model: any) {
    ${symbol_dollar}.extend(this, model);
  }

  setFirstName(firstName: string) {
    this.firstName = firstName;
  }

  setLastName(lastName: string) {
    this.lastName = lastName;
  }

  setPersonId(id: string) {
    this.personId = id;
  }

  setSalary(salary: number) {
    this.salary = salary;
  }

  setExternal(external: boolean) {
    this.external = external;
  }
}
