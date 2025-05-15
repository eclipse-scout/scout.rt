/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, comparators, Device, Form, FormModel, GroupBox, icons, Menu, Table, TableField} from '../../index';

export default (): FormModel => ({
  objectType: Form,
  title: '${textKey:TableOrganize}',
  rootGroupBox: {
    objectType: GroupBox,
    gridDataHints: {
      widthInPixel: 600,
      heightInPixel: 350
    },
    fields: [{
      id: 'ProfilesBox',
      objectType: GroupBox,
      label: '${textKey:SavedSettings}',
      statusVisible: false,
      gridColumnCount: 1,
      gridDataHints: {
        w: 1
      },
      fields: [{
        id: 'ProfilesTableField',
        objectType: TableField,
        labelVisible: false,
        statusVisible: false,
        gridDataHints: {
          useUiHeight: Device.get().type === Device.Type.MOBILE
        },
        table: {
          id: 'ProfilesTable',
          objectType: Table,
          headerVisible: false,
          autoResizeColumns: true,
          defaultRowAction: 'LoadConfigMenu',
          columns: [{
            id: 'ConfigNameColumn',
            objectType: Column,
            sortIndex: 1,
            comparator: comparators.ALPHANUMERIC
          }, {
            id: 'DefaultConfigColumn',
            objectType: Column<boolean>,
            displayable: false,
            sortIndex: 0,
            sortAscending: false
          }],
          menus: [{
            id: 'NewConfigMenu',
            objectType: Menu,
            text: '${textKey:New}'
          }, {
            id: 'LoadConfigMenu',
            objectType: Menu,
            text: '${textKey:Load}',
            menuTypes: [Table.MenuType.SingleSelection],
            keyStroke: 'enter'
          }, {
            id: 'UpdateConfigMenu',
            objectType: Menu,
            text: '${textKey:Update}',
            menuTypes: [Table.MenuType.SingleSelection]
          }, {
            id: 'DeleteConfigMenu',
            objectType: Menu,
            text: '${textKey:DeleteMenu}',
            menuTypes: [Table.MenuType.SingleSelection, Table.MenuType.MultiSelection],
            keyStroke: 'delete'
          }, {
            id: 'RenameConfigMenu',
            objectType: Menu,
            text: '${textKey:Rename}',
            menuTypes: [Table.MenuType.SingleSelection],
            keyStroke: 'ctrl-enter'
          }]
        }
      }]
    }, {
      id: 'ColumnsBox',
      objectType: GroupBox,
      label: '${textKey:Columns}',
      statusVisible: false,
      menuBarPosition: GroupBox.MenuBarPosition.TITLE,
      gridColumnCount: 1,
      gridDataHints: {
        w: 1
      },
      fields: [{
        id: 'ColumnsTableField',
        objectType: TableField,
        labelVisible: false,
        statusVisible: false,
        table: {
          id: 'ColumnsTable',
          objectType: Table,
          cssClass: 'no-menubar-separators',
          headerVisible: false,
          autoResizeColumns: true,
          scrollToSelection: true, // To reveal selection when moving rows
          columns: [{
            id: 'KeyColumn',
            objectType: Column<Column>,
            displayable: false
          }, {
            id: 'TitleColumn',
            objectType: Column,
            text: '${textKey:Title}'
          }],
          menus: [{
            id: 'AddColumnMenu',
            objectType: Menu,
            iconId: icons.PLUS,
            menuTypes: [Table.MenuType.EmptySpace, Table.MenuType.SingleSelection, Table.MenuType.MultiSelection],
            tooltipText: '${textKey:AddColumn}'
          }, {
            id: 'RemoveColumnMenu',
            objectType: Menu,
            iconId: icons.MINUS,
            keyStroke: 'delete',
            menuTypes: [Table.MenuType.SingleSelection, Table.MenuType.MultiSelection],
            tooltipText: '${textKey:RemoveColumn}'
          }, {
            id: 'ModifyColumnMenu',
            objectType: Menu,
            iconId: icons.PENCIL,
            menuTypes: [Table.MenuType.SingleSelection],
            tooltipText: '${textKey:ModifyColumn}'
          }, {
            id: 'MoveColumnUpMenu',
            objectType: Menu,
            iconId: icons.ANGLE_UP,
            keyStroke: 'alt-up',
            menuTypes: [Table.MenuType.SingleSelection, Table.MenuType.MultiSelection],
            horizontalAlignment: 1,
            tooltipText: '${textKey:MoveColumnForward}'
          }, {
            id: 'MoveColumnDownMenu',
            objectType: Menu,
            iconId: icons.ANGLE_DOWN,
            keyStroke: 'alt-down',
            menuTypes: [Table.MenuType.SingleSelection, Table.MenuType.MultiSelection],
            horizontalAlignment: 1,
            tooltipText: '${textKey:MoveColumnBackward}'
          }]
        }
      }]
    }]
  }
});

/* **************************************************************************
* GENERATED WIDGET MAPS
* **************************************************************************/

export type TableOrganizerFormWidgetMap = {
  'ProfilesBox': GroupBox;
  'ProfilesTableField': TableField;
  'ProfilesTable': ProfilesTable;
  'ColumnsBox': GroupBox;
  'ColumnsTableField': TableField;
  'ColumnsTable': ColumnsTable0;
} & ProfilesTableWidgetMap & ColumnsTable0WidgetMap;

export class ProfilesTable extends Table {
  declare widgetMap: ProfilesTableWidgetMap;
  declare columnMap: ProfilesTableColumnMap;
}

export type ProfilesTableWidgetMap = {
  'NewConfigMenu': Menu;
  'LoadConfigMenu': Menu;
  'UpdateConfigMenu': Menu;
  'DeleteConfigMenu': Menu;
  'RenameConfigMenu': Menu;
};

export type ProfilesTableColumnMap = {
  'ConfigNameColumn': Column;
  'DefaultConfigColumn': Column<boolean>;
};

export class ColumnsTable0 extends Table {
  declare widgetMap: ColumnsTable0WidgetMap;
  declare columnMap: ColumnsTable0ColumnMap;
}

export type ColumnsTable0WidgetMap = {
  'AddColumnMenu': Menu;
  'ModifyColumnMenu': Menu;
  'RemoveColumnMenu': Menu;
  'MoveColumnUpMenu': Menu;
  'MoveColumnDownMenu': Menu;
};

export type ColumnsTable0ColumnMap = {
  'KeyColumn': Column<Column>;
  'TitleColumn': Column;
};
