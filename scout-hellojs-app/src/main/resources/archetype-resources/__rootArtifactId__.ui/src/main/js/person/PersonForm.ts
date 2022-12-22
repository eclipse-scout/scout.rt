import PersonFormModel, {PersonFormWidgetMap} from './PersonFormModel';
import {Form, FormModel, Status} from '@eclipse-scout/core';
import {Person, PersonRepository} from '../index';
import * as $ from 'jquery';

export class PersonForm extends Form {
  declare data: Person;
  declare widgetMap: PersonFormWidgetMap;

  protected override _jsonModel(): FormModel {
    return PersonFormModel();
  }

  override setData(data: Person) {
    super.setData(data);
  }

  override exportData(): Person {
    let person = this.data;
    person.setFirstName(this.widget('FirstNameField').value);
    person.setLastName(this.widget('LastNameField').value);
    person.setSalary(this.widget('SalaryField').value);
    person.setExternal(this.widget('ExternalField').value);
    return person;
  }

  override importData() {
    let person = this.data;
    this.widget('FirstNameField').setValue(person.firstName);
    this.widget('LastNameField').setValue(person.lastName);
    this.widget('SalaryField').setValue(person.salary);
    this.widget('ExternalField').setValue(person.external);
  }

  protected override _save(data: Person): JQuery.Promise<Status> {
    return (data.personId ? PersonRepository.get().store(data) : PersonRepository.get().create(data))
      .then(() => Status.ok());
  }

  protected override _load(): JQuery.Promise<Person> {
    if (this.data.personId) {
      // refresh data from server
      return PersonRepository.get().load(this.data.personId);
    }
    return ${symbol_dollar}.resolvedPromise(this.data);
  }
}

