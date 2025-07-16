/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Action, arrays, Cell, Column, Event, Form, InitModelOf, MoveTableRowMenuHelper, scout, ShowInvisibleColumnsForm, StringField, strings, Table, TableCompleteCellEditEvent, TableOrganizerFormWidgetMap, TableRow, TableRowModel,
  TableRowsSelectedEvent, TableStartCellEditEvent, tableUiPreferences, TableUiPreferences, WidgetModel
} from '../../index';
import TableOrganizerFormModel, {ColumnsTable0, ProfilesTable} from './TableOrganizerFormModel';

export class TableOrganizerForm extends Form {
  declare widgetMap: TableOrganizerFormWidgetMap;

  table: Table;
  profilesTable: ProfilesTable;
  columnsTable: ColumnsTable0;
  keyColumn: Column<Column>;

  protected override _jsonModel(): WidgetModel {
    return TableOrganizerFormModel();
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.table = this.findParent(Table);
    this.profilesTable = this.widget('ProfilesTable');
    this.columnsTable = this.widget('ColumnsTable');
    this.keyColumn = this.columnsTable.columnById('KeyColumn');

    this.widget('NewConfigMenu').on('action', event => this._addNewConfig());
    this.widget('LoadConfigMenu').on('action', event => this._loadConfig());
    this.widget('UpdateConfigMenu').on('action', event => this._updateConfig());
    this.widget('DeleteConfigMenu').on('action', event => this._deleteConfigs());
    this.widget('RenameConfigMenu').on('action', event => this._renameConfig());
    this.profilesTable.on('rowsSelected', event => this._onProfilesTableRowsSelected(event));
    this.profilesTable.on('startCellEdit', event => this._onProfilesTableStartCellEdit(event));
    this.profilesTable.on('completeCellEdit', event => this._onProfilesTableCompleteCellEdit(event));

    this.widget('AddColumnMenu').on('action', event => this._onAddColumnMenuAction(event));
    this.widget('ModifyColumnMenu').on('action', event => this._onModifyColumnMenuAction(event));
    this.widget('RemoveColumnMenu').on('action', event => this._onRemoveColumnMenuAction(event));
    this.columnsTable.on('rowsSelected', event => this._onColumnsTableRowsSelected(event));
    this.columnsTable.columnById('WidthColumn').setVisible(!this.table.autoResizeColumns);
    this._installColumnUpDownMenus();

    // Pages in a bookmark outline should only have one profile (the one stored in the bookmark) -> disable menu to create new profiles
    if (strings.startsWith(this.table.userPreferenceContext, `${TableUiPreferences.PROFILE_ID_BOOKMARK}:`)) {
      this.widget('NewConfigMenu').setEnabled(false);
    }
  }

  protected override _load(): JQuery.Promise<any> {
    this._reloadProfilesTable();
    this._reloadColumnsTable();
    this._updateColumnMenus();
    return super._load();
  }

  protected _reloadProfilesTable() {
    const rows: TableRowModel[] = [{cells: [this.session.text('DefaultSettings'), true]}];

    // Create a row for each preference profile, except the GLOBAL profile (this represents the current state and cannot be activated explicitly)
    let prefs = tableUiPreferences.get(this.table);
    if (prefs?.tablePreferenceProfiles) {
      [...prefs.tablePreferenceProfiles.keys()]
        .filter(key => key !== TableUiPreferences.PROFILE_ID_GLOBAL)
        .forEach(key => rows.push({cells: [key]}));
    }

    this.profilesTable.replaceRows(rows);
  }

  protected _onProfilesTableRowsSelected(event: TableRowsSelectedEvent<ProfilesTable>) {
    this._updateProfileMenus();
    if (this.profilesTable.selectedRows.length) {
      this.columnsTable.deselectAll();
    }
  }

  protected _onProfilesTableStartCellEdit(event: TableStartCellEditEvent<string, ProfilesTable>) {
    (event.field as StringField).selectAll();
  }

  protected _onProfilesTableCompleteCellEdit(event: TableCompleteCellEditEvent<string>) {
    event.cell.setEditable(false);
    this.profilesTable.updateRow(event.row);

    let oldConfigName = this.profilesTable.columnById('ConfigNameColumn').cellValue(event.row);
    let newConfigName = event.field.value;
    tableUiPreferences.renameProfile(this.table, oldConfigName, newConfigName);
  }

  protected _updateProfileMenus() {
    const defaultConfigSelected = this.profilesTable.columnById('DefaultConfigColumn').selectedCellValues().includes(true);
    this.widget('UpdateConfigMenu').setVisible(!defaultConfigSelected);
    this.widget('DeleteConfigMenu').setVisible(!defaultConfigSelected);
    this.widget('RenameConfigMenu').setVisible(!defaultConfigSelected);
  }

  protected _addNewConfig() {
    let configName = this._newConfigName();

    let profile = tableUiPreferences.createProfile(this.table, {includeUserFilters: true});
    tableUiPreferences.storeProfile(this.table, configName, profile);

    let row = scout.create(TableRow, {parent: this.profilesTable});
    this.profilesTable.columnById('ConfigNameColumn').setCellValue(row, configName);
    this.profilesTable.columnById('DefaultConfigColumn').setCellValue(row, false);
    this.profilesTable.insertRow(row);
    this._renameConfig(row);
  }

  protected _newConfigName(): string {
    let profileNo = 1;
    const baseName = this.session.text('New');
    const existingNames = this.profilesTable.columnById('ConfigNameColumn').cellValues();
    while (existingNames.includes(`${baseName} ${profileNo}`)) {
      profileNo++;
    }
    return `${baseName} ${profileNo}`;
  }

  protected _loadConfig(row?: TableRow) {
    row = scout.nvl(row, this.profilesTable.selectedRow());

    let configName = this.profilesTable.columnById('ConfigNameColumn').cellValue(row);
    let defaultConfig = this.profilesTable.columnById('DefaultConfigColumn').cellValue(row);
    if (defaultConfig) {
      this.table.resetToInitialUiPreferences();
    } else {
      let prefs = tableUiPreferences.get(this.table);
      let profile = tableUiPreferences.getProfile(prefs, configName);
      tableUiPreferences.applyProfile(this.table, profile);
      // Store activated profile as current state
      tableUiPreferences.storeGlobalProfile(this.table);
    }

    this._reloadColumnsTable();
  }

  protected _updateConfig(row?: TableRow) {
    row = scout.nvl(row, this.profilesTable.selectedRow());

    if (this.profilesTable.columnById('DefaultConfigColumn').cellValue(row)) {
      return;
    }
    let configName = this.profilesTable.columnById('ConfigNameColumn').cellValue(row);
    let profile = tableUiPreferences.createProfile(this.table);
    tableUiPreferences.storeProfile(this.table, configName, profile);
  }

  protected _renameConfig(row?: TableRow) {
    row = scout.nvl(row, this.profilesTable.selectedRow());

    const column = this.profilesTable.columnById('ConfigNameColumn');
    column.cell(row).setEditable(true);
    this.profilesTable.updateRow(row);
    this.profilesTable.focusCell(column, row);
  }

  protected _deleteConfigs(rows?: TableRow[]) {
    rows = scout.nvl(rows, this.profilesTable.selectedRows);

    const configNameColumn = this.profilesTable.columnById('ConfigNameColumn');
    const defaultConfigColumn = this.profilesTable.columnById('DefaultConfigColumn');
    rows = rows.filter(row => !defaultConfigColumn.cellValue(row));
    rows.forEach(row => {
      let configName = configNameColumn.cellValue(row);
      tableUiPreferences.removeProfile(this.table, configName);
    });
    this.profilesTable.deleteRows(rows);
  }

  protected _reloadColumnsTable() {
    const columns = this.table.visibleColumns(false);
    const rows = columns.map(column => {
      return {
        cells: [
          column,
          ShowInvisibleColumnsForm.createColumnTitleCell(column),
          scout.create(Cell, {value: this._computeColumnStatus(column), tooltipText: this._computeColumnStatusTooltip(column)}),
          column.width
        ]
      } as TableRowModel;
    });
    this.columnsTable.replaceRows(rows);
  }

  protected _computeColumnStatus(column: Column<any>): string {
    let filtered = column.filtered;
    let groupSymbol = 'G';
    let filterSymbol = 'F';
    let sortSymbol = column.sortAscending ? '↑' : '↓';
    let sortIndex = column.sortIndex + 1;
    let sortCount = this.table.visibleSortColumnsCount();
    let $container = $('<div>');
    let $status = $container.appendDiv('status');
    let $left = $status.appendSpan('group-filter');
    $left.appendDiv().text(groupSymbol).toggleClass('hidden', !column.grouped);
    $left.appendDiv().text(filterSymbol).toggleClass('hidden', !filtered);
    if (!column.grouped && !filtered) {
      // Reserve space if neither grouped nor filtered to align with the status on the other rows
      $left.children().removeClass('hidden').addClass('invisible');
    }
    $status.appendSpan('sort-direction').text(sortSymbol).toggleClass('invisible', !column.sortActive);
    $status.appendSpan('sort-index').text(sortIndex).toggleClass('invisible', !column.sortActive || sortCount <= 1);
    return $container.html();
  }

  protected _computeColumnStatusTooltip(column: Column<any>): string {
    let result = [];
    if (column.grouped) {
      result.push(this.session.text('Grouped'));
    }
    if (column.filtered) {
      result.push(this.session.text('Filtered'));
    }
    if (column.sortActive) {
      result.push(this.session.text('Sorted'));
    }
    return result.join('\n');
  }

  protected _updateColumnMenus() {
    let selectedColumns = this.keyColumn.selectedCellValues();
    let columnAddable = this.table.organizer.isColumnAddable();
    let columnRemovable = false;
    let columnModifiable = false;
    for (const column of selectedColumns) {
      if (this.table.organizer.isColumnModifiable(column)) {
        columnModifiable = true;
      }
      if (this._isColumnRemovable(column)) {
        columnRemovable = true;
      }
    }
    // Add and remove menus are either used to show and hide columns or to add and remove custom columns
    this.widget('AddColumnMenu').setVisible(columnAddable);
    this.widget('ModifyColumnMenu').setVisible(columnModifiable);
    this.widget('RemoveColumnMenu').setVisible(columnRemovable);
  }

  protected _isColumnRemovable(column: Column<any>) {
    return this.table.organizer.isColumnRemovable(column, true);
  }

  protected async _onAddColumnMenuAction(event: Event<Action>): Promise<void> {
    let oldColumns = this.table.visibleColumns();

    await this.table.organizer.addColumn(arrays.last(this.keyColumn.selectedCellValues()));
    this._reloadColumnsTable();

    // Select inserted columns
    let insertedColumns = arrays.diff(this.table.visibleColumns(), oldColumns);
    this.columnsTable.selectRows(this.columnsTable.rows.filter(row => insertedColumns.includes(this.keyColumn.cellValue(row))));
    this.columnsTable.focus();
  }

  protected async _onModifyColumnMenuAction(event: Event<Action>) {
    await this.table.organizer.modifyColumn(this.keyColumn.selectedCellValue());
    this._reloadColumnsTable();
  }

  protected _onRemoveColumnMenuAction(event: Event<Action>) {
    this.table.organizer.removeColumns(this.keyColumn.selectedCellValues().filter(column => this._isColumnRemovable(column)));
    this._reloadColumnsTable();
  }

  protected _onColumnsTableRowsSelected(event: TableRowsSelectedEvent) {
    this._updateColumnMenus();
    if (this.columnsTable.selectedRows.length) {
      this.profilesTable.deselectAll();
    }
  }

  protected _installColumnUpDownMenus() {
    const moveRowUpMenu = this.widget('MoveColumnUpMenu');
    const moveRowDownMenu = this.widget('MoveColumnDownMenu');
    scout.create(MoveTableRowMenuHelper).install({
      table: this.columnsTable,
      moveRowUpMenu,
      moveRowDownMenu,
      alwaysShowMenus: true,
      rowFilter: (row, direction) => {
        let column = this.keyColumn.cellValue(row);
        if (direction === 'up') {
          return this.table.organizer.isColumnMovableToLeft(column);
        }
        return this.table.organizer.isColumnMovableToRight(column);
      }
    });
    this.columnsTable.on('rowOrderChanged', event => {
      let newVisibleColumns = this.keyColumn.cellValues();
      this.table.organizer.moveColumns(newVisibleColumns);
      this._updateColumnMenus();
    });
  }
}
