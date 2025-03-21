import PersonSearchFormModel, {PersonSearchFormWidgetMap} from './PersonSearchFormModel';
import {Form, FormModel, FormTableControl, InitModelOf, scout} from '@eclipse-scout/core';
import {PersonRestrictionDo} from '../index';

export class PersonSearchForm extends Form {
  declare widgetMap: PersonSearchFormWidgetMap;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    let parent = this.parent as FormTableControl;
    let parentTable = parent.table;
    this.widget('SearchButton').on('action', () => parentTable.reload());
  }

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
