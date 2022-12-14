/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AggregateTableControl, BooleanColumn, Column, FormTableControl, icons, Menu, NumberColumn, PageWithTable, PageWithTableModel, Table} from '@eclipse-scout/core';
import {PersonSearchForm} from '../index';

export default (): PageWithTableModel => ({
  id: '${simpleArtifactName}.PersonTablePage',
  objectType: PageWithTable,
  leaf: true,
  text: '${symbol_dollar}{textKey:Persons}',
  detailTable: {
    id: '${simpleArtifactName}.PersonTablePage.Table',
    objectType: Table,
    autoResizeColumns: true,
    columns: [
      {
        id: 'PersonIdColumn',
        objectType: Column,
        visible: false
      },
      {
        id: 'FirstNameColumn',
        objectType: Column,
        text: '${symbol_dollar}{textKey:FirstName}',
        width: 300
      },
      {
        id: 'LastNameColumn',
        objectType: Column,
        text: '${symbol_dollar}{textKey:LastName}',
        width: 300
      },
      {
        id: 'SalaryColumn',
        objectType: NumberColumn,
        text: '${symbol_dollar}{textKey:Salary}',
        width: 200
      },
      {
        id: 'ExternColumn',
        objectType: BooleanColumn,
        text: '${symbol_dollar}{textKey:External}',
        width: 150
      }
    ],
    menus: [
      {
        id: 'EditPersonMenu',
        objectType: Menu,
        text: '${symbol_dollar}{textKey:EditPerson}',
        iconId: icons.PENCIL,
        menuTypes: [Table.MenuTypes.SingleSelection]
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
          Table.MenuTypes.SingleSelection
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
  'ExternColumn': BooleanColumn;
};

export class PersonTablePageTable extends Table {
  declare columnMap: PersonTablePageTableColumnMap;
  declare widgetMap: PersonTablePageTableWidgetMap;
}
