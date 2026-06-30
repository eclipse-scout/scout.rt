/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
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
  headerVisible: true,
  saveNeededVisible: false,
  rootGroupBox: {
    objectType: GroupBox,
    gridDataHints: {
      widthInPixel: 750,
      heightInPixel: 350
    },
    fields: [{
      id: 'ProfilesBox',
      objectType: GroupBox,
      label: '${textKey:SavedSettings}',
      labelVisible: false,
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
          headerEnabled: false,
          autoResizeColumns: true,
          defaultRowAction: 'LoadConfigMenu',
          cssClass: 'table-organizer-profiles-table',
          columns: [{
            id: 'ConfigNameColumn',
            objectType: Column,
            text: '${textKey:SavedSettings}',
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
      labelVisible: false,
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
          headerEnabled: false,
          autoResizeColumns: true,
          scrollToSelection: true, // To reveal selection when moving rows
          defaultRowAction: 'ModifyColumnMenu',
          textFilterEnabled: false,
          columns: [{
            id: 'KeyColumn',
            objectType: Column<Column>,
            displayable: false
          }, {
            id: 'TitleColumn',
            objectType: Column,
            text: '${textKey:Column}',
            width: 120
          }, {
            id: 'StatusColumn',
            objectType: Column,
            text: '${textKey:Status}',
            htmlEnabled: true,
            width: 70,
            fixedWidth: true,
            cssClass: 'table-organizer-column-status-cell'
          }, {
            id: 'WidthColumn',
            objectType: Column,
            text: '${textKey:Width}',
            autoOptimizeWidth: true,
            // Size of width in Italian.
            // This column should not take too much space, so if another language has a larger word it will show ellipsis.
            autoOptimizeMaxWidth: 85,
            horizontalAlignment: 1
          }],
          menus: [{
            id: 'AddColumnMenu',
            objectType: Menu,
            iconId: icons.PLUS,
            tooltipText: '${textKey:AddColumn}'
          }, {
            id: 'RemoveColumnMenu',
            objectType: Menu,
            iconId: icons.MINUS,
            keyStroke: 'delete',
            tooltipText: '${textKey:RemoveColumn}'
          }, {
            id: 'ModifyColumnMenu',
            objectType: Menu,
            iconId: icons.PENCIL,
            tooltipText: '${textKey:ModifyColumn}'
          }, {
            id: 'MoveColumnUpMenu',
            objectType: Menu,
            iconId: icons.ANGLE_UP,
            keyStroke: 'alt-up',
            horizontalAlignment: 1,
            tooltipText: '${textKey:MoveColumnForward}'
          }, {
            id: 'MoveColumnDownMenu',
            objectType: Menu,
            iconId: icons.ANGLE_DOWN,
            keyStroke: 'alt-down',
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
  'RemoveColumnMenu': Menu;
  'ModifyColumnMenu': Menu;
  'MoveColumnUpMenu': Menu;
  'MoveColumnDownMenu': Menu;
};

export type ColumnsTable0ColumnMap = {
  'KeyColumn': Column<Column>;
  'TitleColumn': Column;
  'StatusColumn': Column;
  'WidthColumn': Column;
};
