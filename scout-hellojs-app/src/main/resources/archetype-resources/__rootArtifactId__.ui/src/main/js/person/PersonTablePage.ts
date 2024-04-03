import {EventHandler, InitModelOf, MessageBox, MessageBoxes, ObjectOrModel, PageWithTable, PageWithTableModel, scout, Table, TableRow} from '@eclipse-scout/core';
import {DataChangeEvent, Person, PersonForm, PersonRepository, PersonRestriction, PersonSearchFormData} from '../index';
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
    if (event.dataType === Person.ENTITY_TYPE) {
      this.reloadPage();
    }
  }

  protected override _loadTableData(searchFilter: PersonSearchFormData): JQuery.Promise<Person[]> {
    let restriction = scout.create(PersonRestriction, searchFilter, {
      ensureUniqueId: false
    });
    return PersonRepository.get().list(this._withMaxRowCountContribution(restriction));
  }

  protected override _transformTableDataToTableRows(tableData: Person[]): ObjectOrModel<TableRowWithPerson>[] {
    return tableData
      .map(person => {
        return {
          person: person,
          cells: [
            person.firstName,
            person.lastName,
            person.salary,
            person.external,
            person.personId
          ]
        };
      });
  }

  protected _getSelectedPerson(): Person {
    let selection = this.detailTable.selectedRow() as TableRowWithPerson;
    if (selection) {
      return selection.person;
    }
    return null;
  }

  protected _createPersonForm(): PersonForm {
    let outline = this.getOutline();
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
          PersonRepository.get().remove(this._getSelectedPerson().personId);
        }
      });
  }

  protected _onCreatePersonMenuAction() {
    let personForm = this._createPersonForm();
    let emptyPerson = scout.create(Person, {}, {
      ensureUniqueId: false
    });
    personForm.setData(emptyPerson);
    personForm.open();
  }
}

export interface TableRowWithPerson extends TableRow {
  person: Person;
}
