import PersonFormModel, {PersonFormWidgetMap} from './PersonFormModel';
import {Form, FormModel, scout} from '@eclipse-scout/core';
import {PersonDo, PersonRestClient} from '../index';

export class PersonForm extends Form {
  declare data: PersonDo;
  declare widgetMap: PersonFormWidgetMap;

  protected override _jsonModel(): FormModel {
    return PersonFormModel();
  }

  override exportData(): PersonDo {
    return scout.create(PersonDo, {
      ...this.data,
      firstName: this.widget('FirstNameField').value,
      lastName: this.widget('LastNameField').value,
      salary: this.widget('SalaryField').value,
      external: this.widget('ExternalField').value
    });
  }

  override importData() {
    let person = this.data;
    this.widget('FirstNameField').setValue(person.firstName);
    this.widget('LastNameField').setValue(person.lastName);
    this.widget('SalaryField').setValue(person.salary);
    this.widget('ExternalField').setValue(person.external);
  }

  protected override _save(data: PersonDo): JQuery.Promise<void> {
    let rest = scout.create(PersonRestClient);
    return (data.id ? rest.store(data) : rest.create(data))
      .then(() => undefined);
  }

  protected override _load(): JQuery.Promise<PersonDo> {
    if (this.data.id) {
      this.setTitle(this.session.text('EditPerson'));
      // refresh data from server
      return scout.create(PersonRestClient).load(this.data.id)
        .then(p => {
          this.setSubTitle(`${p.firstName} ${p.lastName}`);
          return p;
        });
    }
    this.setTitle(this.session.text('CreatePerson'));
    return ${symbol_dollar}.resolvedPromise(this.data);
  }
}

