import PersonFormModel from './PersonFormModel';
import {Form, models} from '@eclipse-scout/core';
import {Person, PersonRepository} from '../index';
import * as $ from 'jquery';

export default class PersonForm extends Form {

  constructor() {
    super();

    this.firstNameField = null;
    this.lastNameField = null;
    this.salaryField = null;
    this.externalField = null;
  }

  _jsonModel() {
    return models.get(PersonFormModel);
  }

  _init(model) {
    super._init(model);
    this._initFields();
  }

  /**
   * Override this method if you have different fields.
   * Then you need to customize importData and exportData too.
   */
  _initFields() {
    this.firstNameField = this.widget('FirstNameField');
    this.lastNameField = this.widget('LastNameField');
    this.salaryField = this.widget('SalaryField');
    this.externalField = this.widget('ExternalField');
  }

  exportData() {
    let person = this.data;
    person.setFirstName(this.firstNameField.value);
    person.setLastName(this.lastNameField.value);
    person.setSalary(this.salaryField.value);
    person.setExternal(this.externalField.value);
    return person;
  }

  importData() {
    let person = this.data;
    this.firstNameField.setValue(person.firstName);
    this.lastNameField.setValue(person.lastName);
    this.salaryField.setValue(person.salary);
    this.externalField.setValue(person.external);
  }

  _save(data) {
    return (data.personId ? PersonRepository.get().store(data) : PersonRepository.get().create(data))
      .then(this._onSaveDone.bind(this));
  }

  _onSaveDone(person) {
    this.session.desktop.dataChange({
      dataType: Person.EVENT_TYPE,
      data: person
    });

    return ${symbol_dollar}.resolvedPromise();
  }

  _load() {
    if (this.data.personId) {
      // refresh data from server
      return PersonRepository.get().load(this.data.personId);
    }
    return ${symbol_dollar}.resolvedPromise(this.data);
  }
}
