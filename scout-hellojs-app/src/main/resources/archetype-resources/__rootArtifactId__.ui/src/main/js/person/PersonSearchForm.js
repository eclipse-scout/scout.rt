import PersonSearchFormModel from './PersonSearchFormModel';
import {Form, models} from '@eclipse-scout/core';

export default class PersonSearchForm extends Form {

  constructor() {
    super();
  }

  _init(model) {
    super._init(model);
    this._initListeners();
  }

  _jsonModel() {
    return models.get(PersonSearchFormModel);
  }

  _initListeners() {
    let parentTable = this.parent.table;
    this.widget('SearchButton').on('action', parentTable.reload.bind(parentTable));
  }

  exportData() {
    return {
      firstName: this.widget('FirstNameField').value,
      lastName: this.widget('LastNameField').value
    };
  }
}
