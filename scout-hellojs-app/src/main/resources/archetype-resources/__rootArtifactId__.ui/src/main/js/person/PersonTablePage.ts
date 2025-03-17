import {EventHandler, InitModelOf, MessageBox, MessageBoxes, ObjectOrModel, PageWithTable, PageWithTableModel, scout, Table, TableRow} from '@eclipse-scout/core';
import {DataChangeEvent, PersonDo, PersonForm, PersonRestClient, PersonRestrictionDo} from '../index';
import PersonTablePageModel, {PersonTablePageTable} from './PersonTablePageModel';

export class PersonTablePage extends PageWithTable {
  declare detailTable: PersonTablePageTable;
  protected _dataChangeListener: EventHandler<DataChangeEvent>;

  protected override _jsonModel(): PageWithTableModel {
    return PersonTablePageModel();
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this._dataChangeListener = this._onDataChange.bind(this);
    this.session.desktop.on('dataChange', this._dataChangeListener);
  }

  protected override _initDetailTable(table: Table) {
    super._initDetailTable(table);

    let editPersonMenu = this.detailTable.widget('EditPersonMenu');
    editPersonMenu.on('action', this._onEditPersonMenuAction.bind(this));

    let deletePersonMenu = this.detailTable.widget('DeletePersonMenu');
    deletePersonMenu.on('action', this._onDeletePersonMenuAction.bind(this));

    let createPersonMenu = this.detailTable.widget('CreatePersonMenu');
    createPersonMenu.on('action', this._onCreatePersonMenuAction.bind(this));
  }

  protected override _destroy() {
    this.session.desktop.off('dataChange', this._dataChangeListener);
    super._destroy();
  }

  protected _onDataChange(event: DataChangeEvent) {
    if (event.dataType === PersonRestClient.DATA_TYPE) {
      this.reloadPage();
    }
  }

  protected override _loadTableData(restriction: PersonRestrictionDo): JQuery.Promise<PersonDo[]> {
    return scout.create(PersonRestClient).list(this._withMaxRowCountContribution(restriction));
  }

  protected override _transformTableDataToTableRows(tableData: PersonDo[]): ObjectOrModel<TableRowWithPerson>[] {
    return tableData.map(person => {
      return {
        person: person,
        cells: [
          person.firstName,
          person.lastName,
          person.salary,
          person.external,
          person.id
        ]
      };
    });
  }

  protected _getSelectedPerson(): PersonDo {
    let selection = this.detailTable.selectedRow() as TableRowWithPerson;
    return selection?.person;
  }

  protected _createPersonForm(): PersonForm {
    let outline = this.outline;
    return scout.create(PersonForm, {
      parent: outline
    });
  }

  protected _onEditPersonMenuAction() {
    let personForm = this._createPersonForm();
    personForm.setData(this._getSelectedPerson());
    personForm.open();
  }

  protected _onDeletePersonMenuAction() {
    MessageBoxes.openYesNo(this.session.desktop, this.session.text('DeleteConfirmationTextNoItemList'))
      .then(button => {
        if (button === MessageBox.Buttons.YES) {
          scout.create(PersonRestClient).remove(this._getSelectedPerson().id);
        }
      });
  }

  protected _onCreatePersonMenuAction() {
    let personForm = this._createPersonForm();
    let emptyPerson = scout.create(PersonDo);
    personForm.setData(emptyPerson);
    personForm.open();
  }
}

export interface TableRowWithPerson extends TableRow {
  person: PersonDo;
}
