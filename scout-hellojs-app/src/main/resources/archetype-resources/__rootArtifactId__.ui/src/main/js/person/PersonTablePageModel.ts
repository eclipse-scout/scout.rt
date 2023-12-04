import {AggregateTableControl, BooleanColumn, Column, FormTableControl, icons, Menu, NumberColumn, PageWithTable, PageWithTableModel, Table} from '@eclipse-scout/core';
import {PersonSearchForm} from '../index';

export default (): PageWithTableModel => ({
  objectType: PageWithTable,
  leaf: true,
  text: '${symbol_dollar}{textKey:Persons}',
  detailTable: {
    objectType: Table,
    columns: [
      {
        id: 'FirstNameColumn',
        objectType: Column,
        text: '${symbol_dollar}{textKey:FirstName}',
        width: 200
      },
      {
        id: 'LastNameColumn',
        objectType: Column,
        text: '${symbol_dollar}{textKey:LastName}',
        width: 200
      },
      {
        id: 'SalaryColumn',
        objectType: NumberColumn,
        text: '${symbol_dollar}{textKey:Salary}',
        width: 120
      },
      {
        id: 'ExternalColumn',
        objectType: BooleanColumn,
        text: '${symbol_dollar}{textKey:External}',
        width: 100
      },
      {
        id: 'PersonIdColumn',
        objectType: Column,
        displayable: false
      },
    ],
    menus: [
      {
        id: 'EditPersonMenu',
        objectType: Menu,
        text: '${symbol_dollar}{textKey:EditPerson}',
        iconId: icons.PENCIL,
        menuTypes: [Table.MenuType.SingleSelection]
      },
      {
        id: 'CreatePersonMenu',
        objectType: Menu,
        text: '${symbol_dollar}{textKey:CreatePerson}',
        iconId: icons.PLUS
      },
      {
        id: 'DeletePersonMenu',
        objectType: Menu,
        text: '${symbol_dollar}{textKey:DeletePerson}',
        iconId: icons.REMOVE,
        menuTypes: [
          Table.MenuType.SingleSelection
        ]
      }
    ],
    tableControls: [
      {
        id: 'SearchFormTableControl',
        objectType: FormTableControl,
        iconId: icons.SEARCH,
        form: {
          id: 'SearchForm',
          objectType: PersonSearchForm
        }
      },
      {
        id: 'AggregateTableControl',
        objectType: AggregateTableControl
      }
    ]
  }
});

export type PersonTablePageTableWidgetMap = {
  'EditPersonMenu': Menu;
  'CreatePersonMenu': Menu;
  'DeletePersonMenu': Menu;
  'SearchFormTableControl': FormTableControl;
  'SearchForm': PersonSearchForm;
  'AggregateTableControl': AggregateTableControl;
};

export type PersonTablePageTableColumnMap = {
  'PersonIdColumn': Column;
  'FirstNameColumn': Column;
  'LastNameColumn': Column;
  'SalaryColumn': NumberColumn;
  'ExternalColumn': BooleanColumn;
};

export class PersonTablePageTable extends Table {
  declare columnMap: PersonTablePageTableColumnMap;
  declare widgetMap: PersonTablePageTableWidgetMap;
}
