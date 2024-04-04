/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CancelMenu, Column, Form, FormModel, GroupBox, OkMenu, Table, TableField} from '../index';

export interface ShowInvisibleColumnsFormModel extends FormModel {
  table: Table;
  insertAfterColumn?: Column;
}

export default (): FormModel => ({
  objectType: Form,
  title: '${textKey:ShowColumns}',
  saveNeededVisible: false,
  rootGroupBox: {
    id: 'MainBox',
    objectType: GroupBox,
    mainBox: true,
    gridColumnCount: 1,
    menus: [
      {
        id: 'OkMenu',
        objectType: OkMenu
      },
      {
        id: 'CancelMenu',
        objectType: CancelMenu
      }
    ],
    fields: [
      {
        id: 'GroupBox',
        objectType: GroupBox,
        statusVisible: false,
        fields: [
          {
            id: 'ColumnsTableField',
            objectType: TableField,
            labelVisible: false,
            statusVisible: false,
            gridDataHints: {
              h: 6
            },
            table: {
              id: 'ColumnsTable',
              objectType: Table,
              checkable: true,
              checkableStyle: Table.CheckableStyle.CHECKBOX_TABLE_ROW,
              headerVisible: false,
              autoResizeColumns: true,
              columns: [
                {
                  id: 'KeyColumn',
                  objectType: Column,
                  primaryKey: true,
                  displayable: false
                },
                {
                  id: 'TitleColumn',
                  objectType: Column
                }
              ]
            }
          }
        ]
      }
    ]
  }
});

/* **************************************************************************
* GENERATED WIDGET MAPS
* **************************************************************************/

export type ShowInvisibleColumnsFormWidgetMap = {
  'MainBox': GroupBox;
  'OkMenu': OkMenu;
  'CancelMenu': CancelMenu;
  'GroupBox': GroupBox;
  'ColumnsTableField': TableField;
  'ColumnsTable': ColumnsTable;
};

export class ColumnsTable extends Table {
  declare columnMap: ColumnsTableColumnMap;
}

export type ColumnsTableColumnMap = {
  'KeyColumn': Column<Column>;
  'TitleColumn': Column;
};
