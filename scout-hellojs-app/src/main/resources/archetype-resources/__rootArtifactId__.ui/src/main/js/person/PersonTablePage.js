import {PageWithTable, MessageBoxes, MessageBox, models, scout} from '@eclipse-scout/core';
import {Person, PersonRepository} from '../index';
import PersonTablePageModel from './PersonTablePageModel';
import * as $ from 'jquery';

export default class PersonTablePage extends PageWithTable {

  constructor() {
    super();

    this._dataChangeListener = null;
  }

  _jsonModel() {
    return models.get(PersonTablePageModel);
  }

  _init(model) {
    let m = ${symbol_dollar}.extend({}, this._jsonModel(), model);
    super._init(m);
    this._initListeners();
  }

  /**
   * Override this method if you want to customize the menu entries.
   */
  _initListeners() {
    this._dataChangeListener = this._onDataChange.bind(this);
    this.session.desktop.on('dataChange', this._dataChangeListener);

    let editPersonMenu = this.detailTable.widget('EditPersonMenu');
    editPersonMenu.on('action', this._onEditPersonMenuAction.bind(this));

    let deletePersonMenu = this.detailTable.widget('DeletePersonMenu');
    deletePersonMenu.on('action', this._onDeletePersonMenuAction.bind(this));

    let createPersonMenu = this.detailTable.widget('CreatePersonMenu');
    createPersonMenu.on('action', this._onCreatePersonMenuAction.bind(this));
  }

  _destroy() {
    this.session.desktop.off('dataChange', this._dataChangeListener);
    super._destroy();
  }

  _onDataChange(event) {
    if (event.dataType === Person.EVENT_TYPE) {
      this.reloadPage();
    }
  }

  _loadTableData(searchFilter) {
    let restriction = scout.create('${simpleArtifactName}.PersonRestriction', searchFilter, {
      ensureUniqueId: false
    });
    return PersonRepository.get().list(restriction);
  }

  _transformTableDataToTableRows(tableData) {
    return tableData
      .map(person => {
        return {
          person: person,
          cells: [
            person.personId,
            person.firstName,
            person.lastName,
            person.salary,
            person.external
          ]
        };
      });
  }

  _getSelectedPerson() {
    let selection = this.detailTable.selectedRow();
    if (selection) {
      return selection.person;
    }
    return null;
  }

  _createPersonForm() {
    let outline = this.getOutline();
    let personForm = scout.create('${simpleArtifactName}.PersonForm', {
      parent: outline
    });
    return personForm;
  }

  _onEditPersonMenuAction(event) {
    let personForm = this._createPersonForm();
    personForm.setData(this._getSelectedPerson());
    personForm.open();
  }

  _onDeletePersonMenuAction(event) {
    MessageBoxes.openYesNo(this.session.desktop, this.session.text('DeleteConfirmationTextNoItemList'))
      .then(button => {
        if (button === MessageBox.Buttons.YES) {
          PersonRepository.get()
            .remove(this._getSelectedPerson().personId)
            .then(this._onPersonDeleted.bind(this));
        }
      });
  }

  _onPersonDeleted() {
    this.session.desktop.dataChange({
      dataType: Person.EVENT_TYPE
    });
  }

  _onCreatePersonMenuAction(event) {
    let personForm = this._createPersonForm();
    let emptyPerson = scout.create('${simpleArtifactName}.Person', {}, {
      ensureUniqueId: false
    });
    personForm.setData(emptyPerson);
    personForm.open();
  }
}
