import PersonSearchFormModel, {PersonSearchFormWidgetMap} from './PersonSearchFormModel';
import {Form, FormModel, FormTableControl, InitModelOf} from '@eclipse-scout/core';

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

  override exportData(): PersonSearchFormData {
    return {
      firstName: this.widget('FirstNameField').value,
      lastName: this.widget('LastNameField').value
    };
  }
}

export type PersonSearchFormData = {
  firstName: string;
  lastName: string;
};
