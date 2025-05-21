import PersonSearchFormModel, {PersonSearchFormWidgetMap} from './PersonSearchFormModel';
import {Form, FormModel, scout} from '@eclipse-scout/core';
import {PersonRestrictionDo} from '../index';

export class PersonSearchForm extends Form {
  declare widgetMap: PersonSearchFormWidgetMap;

  protected override _jsonModel(): FormModel {
    return PersonSearchFormModel();
  }

  override exportData(): PersonRestrictionDo {
    return scout.create(PersonRestrictionDo, {
      firstName: this.widget('FirstNameField').value,
      lastName: this.widget('LastNameField').value
    });
  }
}
