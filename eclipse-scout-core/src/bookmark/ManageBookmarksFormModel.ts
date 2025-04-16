/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkDo, BooleanColumn, CancelMenu, Column, Form, FormModel, GroupBox, icons, Menu, OkMenu, Table, TableField} from '../index';

export default (): FormModel => ({
  objectType: Form,
  title: '${textKey:BookmarksManageMenu}',
  displayHint: Form.DisplayHint.VIEW,
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
        gridDataHints: {
          w: 1
        },
        fields: [
          {
            id: 'BookmarksTableField',
            objectType: TableField,
            labelVisible: false,
            statusVisible: false,
            gridDataHints: {
              h: 6
            },
            table: {
              id: 'BookmarksTable',
              objectType: Table,
              autoResizeColumns: true,
              columns: [
                {
                  id: 'BookmarkColumn',
                  objectType: Column<BookmarkDo>,
                  displayable: false
                },
                {
                  id: 'NameColumn',
                  objectType: Column,
                  text: '${textKey:Name}',
                  width: 200
                }
              ],
              menus: [
                {
                  id: 'EditMenu',
                  objectType: Menu,
                  text: 'Edit',
                  iconId: icons.PENCIL,
                  keyStroke: 'shift-enter',
                  menuTypes: [Table.MenuType.SingleSelection]
                },
                {
                  id: 'DeleteMenu',
                  objectType: Menu,
                  text: 'Delete',
                  iconId: icons.REMOVE,
                  keyStroke: 'shift-enter',
                  menuTypes: [Table.MenuType.SingleSelection, Table.MenuType.MultiSelection]
                },
                {
                  id: 'MoveRowUpMenu',
                  objectType: Menu,
                  iconId: icons.ANGLE_UP,
                  tooltipText: '${textKey:ButtonMoveUp}',
                  stackable: false,
                  horizontalAlignment: 1
                },
                {
                  id: 'MoveRowDownMenu',
                  objectType: Menu,
                  iconId: icons.ANGLE_DOWN,
                  tooltipText: '${textKey:ButtonMoveDown}',
                  stackable: false,
                  horizontalAlignment: 1
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

export type ManageBookmarksFormWidgetMap = {
  'MainBox': GroupBox;
  'OkMenu': OkMenu;
  'CancelMenu': CancelMenu;
  'GroupBox': GroupBox;
  'BookmarksTableField': TableField;
  'BookmarksTable': BookmarksTable;
} & BookmarksTableWidgetMap;

export class BookmarksTable extends Table {
  declare widgetMap: BookmarksTableWidgetMap;
  declare columnMap: BookmarksTableColumnMap;
}

export type BookmarksTableWidgetMap = {
  'EditMenu': Menu;
  'DeleteMenu': Menu;
  'MoveRowUpMenu': Menu;
  'MoveRowDownMenu': Menu;
};

export type BookmarksTableColumnMap = {
  'BookmarkColumn': Column<BookmarkDo>;
  'NameColumn': Column;
  'SharedColumn': BooleanColumn;
  'OwnerColumn': Column;
};
