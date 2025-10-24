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
  AbstractLayout, Action, aria, arrays, Cell, Column, ColumnUserFilter, ColumnUserFilterValues, Device, EnumObject, EventHandler, FilterFieldsGroupBox, graphics, HtmlComponent, InitModelOf, ListBoxAriaRules, NumberColumn,
  NumberColumnAggregationFunction, NumberField, Popup, RowLayout, scout, SomeRequired, Table, TableHeader, TableHeaderMenuButton, TableHeaderMenuEventMap, TableHeaderMenuGroup, TableHeaderMenuGroupItem, TableHeaderMenuLayout,
  TableHeaderMenuModel, TableRow, TableRowModel, TableRowsCheckedEvent, ValueField
} from '../index';

export class TableHeaderMenu extends Popup implements TableHeaderMenuModel {
  declare model: TableHeaderMenuModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'column' | 'tableHeader'>;
  declare eventMap: TableHeaderMenuEventMap;
  declare self: TableHeaderMenu;

  column: Column<any>;
  tableHeader: TableHeader;
  compact: boolean;
  table: Table;
  filterTable: Table;
  filter: ColumnUserFilter;
  filterCheckedMode: TableHeaderMenuCheckedMode;
  filterSortMode: TableHeaderMenuSortMode;
  hasFilterTable: boolean;
  hasFilterFields: boolean;
  leftGroups: TableHeaderMenuGroup[];
  moveGroup: TableHeaderMenuGroup;
  hierarchyGroup: TableHeaderMenuGroup;
  toBeginButton: TableHeaderMenuButton;
  forwardButton: TableHeaderMenuButton;
  backwardButton: TableHeaderMenuButton;
  toEndButton: TableHeaderMenuButton;
  sortingGroup: TableHeaderMenuGroup;
  sortAscButton: TableHeaderMenuButton;
  sortDescButton: TableHeaderMenuButton;
  sortAscAddButton: TableHeaderMenuButton;
  sortDescAddButton: TableHeaderMenuButton;
  columnActionsGroup: TableHeaderMenuGroup;
  addColumnButton: TableHeaderMenuButton;
  removeColumnButton: TableHeaderMenuButton;
  modifyColumnButton: TableHeaderMenuButton;
  groupButton: TableHeaderMenuButton;
  groupAddButton: TableHeaderMenuButton;
  barChartButton: TableHeaderMenuButton;
  colorGradient1Button: TableHeaderMenuButton;
  colorGradient2Button: TableHeaderMenuButton;
  collapseAllButton: TableHeaderMenuButton;
  expandAllButton: TableHeaderMenuButton;
  sumButton: TableHeaderMenuButton;
  averageButton: TableHeaderMenuButton;
  minimumButton: TableHeaderMenuButton;
  maximumButton: TableHeaderMenuButton;
  filterFieldsGroupBox: FilterFieldsGroupBox;
  filterToggleCheckedAction: Action;
  filterSortOrderAction: Action;

  $rightGroups: JQuery[];
  $headerItem: JQuery;
  $columnActions: JQuery;
  $columnFilters: JQuery;
  $filterTableGroup: JQuery;
  $filterTableGroupTitle: JQuery;
  $filterFieldsGroup: JQuery;
  $body: JQuery;

  protected _onColumnMovedHandler: () => void;
  protected _tableFilterHandler: () => void;
  protected _tableHeaderScrollHandler: (event: JQuery.ScrollEvent) => void;
  protected _filterTableRowsCheckedHandler: EventHandler<TableRowsCheckedEvent>;

  constructor() {
    super();
    this.column = null;
    this.tableHeader = null;
    this.table = null;
    this.filter = null;
    this.filterCheckedMode = TableHeaderMenu.CheckedMode.ALL;
    this.filterSortMode = TableHeaderMenu.SortMode.ALPHABETICALLY;
    this.hasFilterTable = false;
    this.hasFilterFields = false;
    this.animateOpening = true;
    this.animateRemoval = true;
    this.focusableContainer = true;

    this.leftGroups = [];

    this.moveGroup = null;
    this.toBeginButton = null;
    this.forwardButton = null;
    this.backwardButton = null;
    this.toEndButton = null;
    this.sortingGroup = null;
    this.sortDescButton = null;
    this.sortAscAddButton = null;
    this.sortDescAddButton = null;
    this.columnActionsGroup = null;
    this.addColumnButton = null;
    this.removeColumnButton = null;
    this.modifyColumnButton = null;
    this.groupButton = null;
    this.groupAddButton = null;
    this.barChartButton = null;
    this.colorGradient1Button = null;
    this.colorGradient2Button = null;
    this.collapseAllButton = null;
    this.expandAllButton = null;
    this.sumButton = null;
    this.averageButton = null;
    this.minimumButton = null;
    this.maximumButton = null;

    this.$rightGroups = [];

    this._onColumnMovedHandler = this._onColumnMoved.bind(this);
    this._tableHeaderScrollHandler = this._onAnchorScroll.bind(this);

    // Make sure the actions are not disabled even if the table is disabled
    // To disable the menu use headerEnabled or headerMenusEnabled
    this.inheritAccessibility = false;
  }

  static CheckedMode = {
    ALL: {
      checkAll: true,
      text: 'ui.SelectAllFilter'
    },
    NONE: {
      checkAll: false,
      text: 'ui.SelectNoneFilter'
    }
  } as const;

  static SortMode = {
    ALPHABETICALLY: {
      text: 'ui.SortAlphabeticallyFilter',
      cssClass: 'table-header-menu-toggle-sort-order-alphabetically'
    },
    AMOUNT: {
      text: 'ui.SortByAmountFilter',
      cssClass: 'table-header-menu-toggle-sort-order-amount'
    }
  } as const;

  protected override _init(options: InitModelOf<this>) {
    options.scrollType = options.scrollType || 'none';
    super._init(options);

    this.tableHeader = options.tableHeader;
    this.column = options.column;
    this.table = this.tableHeader.table;
    this.$headerItem = this.$anchor;

    this.table.on('columnMoved', this._onColumnMovedHandler);
    // Filtering
    this.filter = this.table.getFilter(this.column.id) as ColumnUserFilter;
    if (!this.filter) {
      this.filter = this.column.createFilter();
    }
    // always recalculate available values to make sure new/updated/deleted rows are considered
    this.filter.calculate();
    this.filter.on('filterFieldsChanged', this._updateFilterTable.bind(this));
    this._updateFilterTableCheckedMode();

    this.hasFilterTable = this.filter.availableValues.length > 0;
    this.hasFilterFields = this.filter.hasFilterFields;

    if (this.hasFilterTable) {
      this._tableFilterHandler = this._onFilterTableChanged.bind(this);
      this.table.on('filterAdded', this._tableFilterHandler);
      this.table.on('filterRemoved', this._tableFilterHandler);
      this._filterTableRowsCheckedHandler = this._onFilterTableRowsChecked.bind(this);
    }
  }

  protected override _createLayout(): AbstractLayout {
    return new TableHeaderMenuLayout(this);
  }

  protected override _render() {
    this.leftGroups = [];
    this.$rightGroups = [];

    this.$headerItem.setSelected(true);

    this.$container = this.$parent.appendDiv('popup table-header-menu');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
    this.$body = this.$container.appendDiv('table-header-menu-body');
    HtmlComponent.install(this.$body, this.session);
    this._installScrollbars({
      axis: 'y',
      scrollShadow: 'none'
    });
    this.$columnActions = this.$body.appendDiv('table-header-menu-actions');
    HtmlComponent.install(this.$columnActions, this.session);

    // only add right column if filter has a filter-table or filter-fields
    if (this.hasFilterTable || this.hasFilterFields) {
      this.$columnFilters = this.$body.appendDiv('table-header-menu-filters');
      let htmlColumnFilters = HtmlComponent.install(this.$columnFilters, this.session);
      htmlColumnFilters.setLayout(new RowLayout());
    }

    this.tableHeader.$container.on('scroll', this._tableHeaderScrollHandler);

    // -- Left column -- //
    // Moving
    let movableColumns = this.table.visibleColumns().filter(column => !column.fixedPosition);
    if (movableColumns.length > 1 && !this.column.fixedPosition) {
      this.leftGroups.push(this._renderMovingGroup());
    }
    // Sorting
    if (this.table.sortEnabled) {
      this.leftGroups.push(this._renderSortingGroup());
    }
    // Add/remove/change columns
    if (this._isColumnActionsGroupVisible()) {
      this.leftGroups.push(this._renderColumnActionsGroup());
    }
    // Grouping
    // column.grouped check necessary to make ungroup possible, even if grouping is not possible anymore
    if (this.table.isGroupingPossible(this.column) || this.column.grouped) {
      this.leftGroups.push(this._renderGroupingGroup());
    }

    // Expand/Collapse
    this.leftGroups.push(this._renderHierarchyGroup());

    // Width
    if (!this.column.fixedWidth) {
      this.leftGroups.push(this._renderWidthGroup());
    }

    // Aggregation
    if (this.table.isAggregationPossible(this.column)) {
      this.leftGroups.push(this._renderAggregationGroup());
    }
    // Coloring
    if (this.column instanceof NumberColumn) {
      this.leftGroups.push(this._renderColoringGroup());
    }

    // -- Right column -- //
    // Filter table
    if (this.hasFilterTable) {
      this.$rightGroups.push(this._renderFilterTable());
    }
    // Filter fields
    if (this.hasFilterFields) {
      this.$rightGroups.push(this._renderFilterFields());
    }

    this._onColumnMoved();

    // Set table style to focused, so that it looks as it still has the focus.
    if (this.table.enabled) {
      this.table.$container.addClass('focused');
    }
  }

  override validateFocus() {
    if (this.filterFieldsGroupBox) {
      this.filterFieldsGroupBox.focus();
    }
    // Super call will focus container if no element has been focused yet
    super.validateFocus();
  }

  override get$Scrollable(): JQuery {
    return this.$body;
  }

  protected _updateFirstLast() {
    addFirstLastClass(this.leftGroups.filter(group => group.visible));
    addFirstLastClass(this.$rightGroups);

    function addFirstLastClass(groups: (JQuery | TableHeaderMenuGroup)[]) {
      groups.forEach((group, index, arr) => {
        toggleCssClass(group, 'first', index === 0);
        toggleCssClass(group, 'last', index === arr.length - 1);
      });
    }

    // Note: we should refactor code for filter-fields and filter-table so they could also
    // work with a model-class (like the button menu groups). Currently, this would cause too much work.
    function toggleCssClass(group: JQuery | TableHeaderMenuGroup, cssClass: string, condition: boolean) {
      let $container = group instanceof TableHeaderMenuGroup ? group.$container : group;
      $container.toggleClass(cssClass, condition);
    }
  }

  protected override _remove() {
    if (this.filterTable) {
      this.filterTable.off('rowsChecked', this._filterTableRowsCheckedHandler);
    }
    if (this.tableHeader.rendered) {
      this.tableHeader.$container.off('scroll', this._tableHeaderScrollHandler);
    }
    this.$headerItem.setSelected(false);
    this.table.off('columnMoved', this._onColumnMovedHandler);
    this.table.off('filterAdded', this._tableFilterHandler);
    this.table.off('filterRemoved', this._tableFilterHandler);
    super._remove();

    // table may have been removed in the meantime
    if (this.table.rendered) {
      this.table.$container.removeClass('focused');
    }
  }

  protected _renderMovingGroup(): TableHeaderMenuGroup {
    let table = this.table;
    let column = this.column;

    this.moveGroup = scout.create(TableHeaderMenuGroup, {
      parent: this,
      textKey: 'ui.Move',
      cssClass: 'first'
    });
    this.toBeginButton = scout.create(TableHeaderMenuButton, {
      parent: this.moveGroup,
      text: '${textKey:ui.toBegin}',
      cssClass: 'move move-top'
    });
    this.toBeginButton.on('action', () => {
      table.moveColumn(column, 0);
    });

    this.forwardButton = scout.create(TableHeaderMenuButton, {
      parent: this.moveGroup,
      text: '${textKey:ui.forward}',
      cssClass: 'move move-up'
    });
    this.forwardButton.on('action', () => {
      let pos = table.visibleColumns().indexOf(column);
      table.moveColumn(column, Math.max(pos - 1, 0));
    });

    this.backwardButton = scout.create(TableHeaderMenuButton, {
      parent: this.moveGroup,
      text: '${textKey:ui.backward}',
      cssClass: 'move move-down'
    });
    this.backwardButton.on('action', () => {
      let pos = table.visibleColumns().indexOf(column);
      table.moveColumn(column, Math.min(pos + 1, table.header.findHeaderItems().length - 1));
    });

    this.toEndButton = scout.create(TableHeaderMenuButton, {
      parent: this.moveGroup,
      text: '${textKey:ui.toEnd}',
      cssClass: 'move move-bottom'
    });
    this.toEndButton.on('action', () => {
      table.moveColumn(column, table.header.findHeaderItems().length - 1);
    });

    this.moveGroup.render(this.$columnActions);
    return this.moveGroup;
  }

  protected _onColumnMoved() {
    let column = this.column;

    if (this.moveGroup) {
      let forwardEnabled = this.table.organizer.isColumnMovableToLeft(column);
      let backwardEnabled = this.table.organizer.isColumnMovableToRight(column);
      this.toBeginButton.setEnabled(forwardEnabled);
      this.forwardButton.setEnabled(forwardEnabled);
      this.backwardButton.setEnabled(backwardEnabled);
      this.toEndButton.setEnabled(backwardEnabled);
    }

    this.hierarchyGroup.setVisible(this.table.isTableNodeColumn(column));
    this._updateFirstLast();
  }

  protected _isColumnActionsGroupVisible(): boolean {
    return this.table.isColumnAddable() || this.table.isColumnRemovable(this.column) || this.table.isColumnModifiable(this.column);
  }

  protected _renderColumnActionsGroup(): TableHeaderMenuGroup {
    this.columnActionsGroup = scout.create(TableHeaderMenuGroup, {
      parent: this,
      textKey: 'ui.Column'
    });

    this.addColumnButton = scout.create(TableHeaderMenuButton, {
      parent: this.columnActionsGroup,
      text: '${textKey:ui.addColumn}',
      cssClass: 'add-column',
      visible: this.table.isColumnAddable()
    });
    this.addColumnButton.on('action', () => {
      this.close();
      this.table.organizer.addColumn(this.column);
    });

    this.removeColumnButton = scout.create(TableHeaderMenuButton, {
      parent: this.columnActionsGroup,
      text: '${textKey:ui.removeColumn}',
      cssClass: 'remove-column',
      visible: this.table.isColumnRemovable(this.column)
    });
    this.removeColumnButton.on('action', () => {
      this.close();
      this.table.organizer.removeColumns([this.column]);
    });

    this.modifyColumnButton = scout.create(TableHeaderMenuButton, {
      parent: this.columnActionsGroup,
      text: '${textKey:ui.changeColumn}',
      cssClass: 'change-column',
      visible: this.table.isColumnModifiable(this.column)
    });
    this.modifyColumnButton.on('action', () => {
      this.close();
      this.table.organizer.modifyColumn(this.column);
    });

    this.columnActionsGroup.render(this.$columnActions);
    return this.columnActionsGroup;
  }

  protected _renderSortingGroup(): TableHeaderMenuGroup {
    let table = this.table,
      column = this.column,
      menuPopup = this;

    this.sortingGroup = scout.create(TableHeaderMenuGroup, {
      parent: this,
      textKey: 'ColumnSorting'
    });

    if (!table.hasPermanentHeadOrTailSortColumns()) {
      this.sortAscButton = scout.create(TableHeaderMenuButton, {
        parent: this.sortingGroup,
        text: '${textKey:ui.ascending}',
        cssClass: 'sort sort-asc',
        direction: 'asc',
        toggleAction: true
      });
      this.sortAscButton.on('action', onSortClick.bind(this.sortAscButton));

      this.sortDescButton = scout.create(TableHeaderMenuButton, {
        parent: this.sortingGroup,
        text: '${textKey:ui.descending}',
        cssClass: 'sort sort-desc',
        direction: 'desc',
        toggleAction: true
      });
      this.sortDescButton.on('action', onSortClick.bind(this.sortDescButton));
    }

    this.sortAscAddButton = scout.create(TableHeaderMenuButton, {
      parent: this.sortingGroup,
      text: '${textKey:ui.ascendingAdditionally}',
      cssClass: 'sort sort-asc-add',
      direction: 'asc',
      toggleAction: true
    });
    this.sortAscAddButton.on('action', onSortAdditionalClick.bind(this.sortAscAddButton));

    this.sortDescAddButton = scout.create(TableHeaderMenuButton, {
      parent: this.sortingGroup,
      text: '${textKey:ui.descendingAdditionally}',
      cssClass: 'sort sort-desc-add',
      direction: 'desc',
      toggleAction: true
    });
    this.sortDescAddButton.on('action', onSortAdditionalClick.bind(this.sortDescAddButton));

    this._updateSortingSelectedState();
    this.sortingGroup.render(this.$columnActions);
    return this.sortingGroup;

    function onSortClick() {
      menuPopup.close();
      sort(this.direction, false, !this.selected);
    }

    function onSortAdditionalClick() {
      menuPopup.close();
      sort(this.direction, true, !this.selected);
    }

    function sort(direction: 'asc' | 'desc', multiSort: boolean, remove: boolean) {
      table.sort(column, direction, multiSort, remove);
      menuPopup._updateSortingSelectedState();
    }
  }

  protected _updateSortingSelectedState() {
    if (!this.table.sortEnabled) {
      return;
    }

    let showAddCommands = false,
      sortCount = this._sortColumnCount();

    this.sortingGroup.children.forEach((button: TableHeaderMenuButton) => button.setSelected(false));

    if (sortCount === 1 && !this.table.hasPermanentHeadOrTailSortColumns()) {
      if (this.column.sortActive) {
        if (this.column.sortAscending) {
          this.sortAscButton.setSelected(true);
        } else {
          this.sortDescButton.setSelected(true);
        }
      } else {
        showAddCommands = true;
      }
    } else if (sortCount > 1 || this.table.hasPermanentHeadOrTailSortColumns()) {
      showAddCommands = true;
      if (this.column.sortActive) {
        if (this.column.sortAscending) {
          this.sortAscAddButton.setSelected(true);
        } else {
          this.sortDescAddButton.setSelected(true);
        }
        let addIcon = (this.column.sortIndex + 1) + '';
        this.sortAscAddButton.setIconId(addIcon);
        this.sortDescAddButton.setIconId(addIcon);
      }
    }

    this.sortAscAddButton.setVisible(showAddCommands);
    this.sortDescAddButton.setVisible(showAddCommands);
  }

  protected _renderGroupingGroup(): TableHeaderMenuGroup {
    let menuPopup = this,
      table = this.table,
      column = this.column,
      groupCount = this._groupColumnCount();

    let group = scout.create(TableHeaderMenuGroup, {
      parent: this,
      textKey: 'ui.Grouping'
    });

    this.groupButton = scout.create(TableHeaderMenuButton, {
      parent: group,
      text: '${textKey:ui.groupingApply}',
      cssClass: 'group',
      additional: false,
      toggleAction: true
    });
    this.groupButton.on('action', groupColumn.bind(this.groupButton));

    this.groupAddButton = scout.create(TableHeaderMenuButton, {
      parent: group,
      text: '${textKey:ui.additionally}',
      cssClass: 'group-add',
      additional: true,
      toggleAction: true
    });
    this.groupAddButton.on('action', groupColumn.bind(this.groupAddButton));

    if (groupCount === 0) {
      this.groupAddButton.setVisible(false);
    } else if (groupCount === 1 && this.column.grouped) {
      this.groupButton.setSelected(true);
      this.groupAddButton.setVisible(false);
    } else if (groupCount > 1) {
      this.groupAddButton.setVisible(true);
    }

    if (table.hasPermanentHeadOrTailSortColumns() && groupCount > 0) {
      // If table has permanent head columns, other columns may not be grouped exclusively -> only enable add button (equally done for sort buttons)
      this.groupButton.setVisible(false);
      this.groupAddButton.setVisible(true);
    }

    if (this.column.grouped) {
      if (groupCount === 1) {
        this.groupAddButton.setSelected(true);
      } else if (groupCount > 1) {
        this.groupAddButton.setSelected(true);
        this.groupAddButton.setIconId((this.column.sortIndex + 1) + '');
      }
    }

    group.render(this.$columnActions);
    return group;

    function groupColumn() {
      let direction: 'asc' | 'desc' = (column.sortIndex >= 0 && !column.sortAscending) ? 'desc' : 'asc';
      menuPopup.close();
      table.group(column, direction, this.additional, !this.selected);
    }
  }

  protected _renderHierarchyGroup(): TableHeaderMenuGroup {
    let table = this.table, menuPopup = this;
    this.hierarchyGroup = scout.create(TableHeaderMenuGroup, {
      parent: this,
      textKey: 'ui.Hierarchy',
      visible: this.table.isTableNodeColumn(this.column)
    });

    this.collapseAllButton = scout.create(TableHeaderMenuButton, {
      parent: this.hierarchyGroup,
      text: '${textKey:ui.CollapseAll}',
      cssClass: 'hierarchy-collapse-all',
      enabled: !!arrays.find(table.rows, row => row.expanded && !arrays.empty(row.childRows))
    });
    this.collapseAllButton.on('action', () => {
      menuPopup.close();
      table.collapseAll();
    });

    this.expandAllButton = scout.create(TableHeaderMenuButton, {
      parent: this.hierarchyGroup,
      text: '${textKey:ui.ExpandAll}',
      cssClass: 'hierarchy-expand-all',
      enabled: !!arrays.find(table.rows, row => !row.expanded && !arrays.empty(row.childRows))
    });
    this.expandAllButton.on('action', () => {
      menuPopup.close();
      table.expandAll();
    });

    this.hierarchyGroup.render(this.$columnActions);
    return this.hierarchyGroup;
  }

  protected _renderWidthGroup(): TableHeaderMenuGroup {
    let group = scout.create(TableHeaderMenuGroup, {
      parent: this,
      textKey: 'Width'
    });
    let optimizeWidthButton = scout.create(TableHeaderMenuButton, {
      parent: group,
      text: '${textKey:ui.optimizeWidth}',
      cssClass: 'optimize-width'
    });
    optimizeWidthButton.on('action', () => {
      this.column.resizeToFit();
      this.close();
    });
    let optimizeWidthAllButton = scout.create(TableHeaderMenuButton, {
      parent: group,
      text: '${textKey:ui.optimizeWidthAll}',
      cssClass: 'optimize-widths'
    });
    optimizeWidthAllButton.on('action', () => {
      this.table.visibleColumns().forEach(column => column.resizeToFit());
      this.close();
    });
    let widthField = scout.create(NumberField, {
      parent: group,
      cssClass: 'table-header-menu-command width no-mandatory-indicator',
      label: '${textKey:Width}',
      labelVisible: false,
      clearable: ValueField.Clearable.NEVER,
      value: this.column.width,
      minValue: this.column.minWidth,
      gridData: { // Don't use hints because parent has no logical grid but FormField._updateElementInnerAlignment expects that
        horizontalAlignment: 0
      }
    }) as NumberField & TableHeaderMenuGroupItem;
    widthField.on('propertyChange:value', () => {
      this.table.resizeColumn(this.column, widthField.value);
    });
    widthField.computeGroupSuffix = () => this.session.text('ui.adjust');
    group.render(this.$columnActions);
    HtmlComponent.install(group.$container, this.session);
    return group;
  }

  protected _renderAggregationGroup(): TableHeaderMenuGroup {
    let table = this.table,
      column = this.column as NumberColumn,
      aggregation = column.aggregationFunction,
      menuPopup = this,
      group = scout.create(TableHeaderMenuGroup, {
        parent: this,
        textKey: 'ui.Aggregation'
      }),
      allowedAggregationFunctions = arrays.ensure(column.allowedAggregationFunctions),
      isAggregationNoneAllowed = allowedAggregationFunctions.indexOf('none') !== -1;

    this.sumButton = createHeaderMenuButtonForAggregationFunction('${textKey:ui.Sum}', 'sum');
    this.averageButton = createHeaderMenuButtonForAggregationFunction('${textKey:ui.Average}', 'avg');
    this.minimumButton = createHeaderMenuButtonForAggregationFunction('${textKey:ui.Minimum}', 'min');
    this.maximumButton = createHeaderMenuButtonForAggregationFunction('${textKey:ui.Maximum}', 'max');

    group.children.forEach((button: TableHeaderMenuButton) => button.setSelected(button.aggregation === aggregation));
    group.render(this.$columnActions);
    return group;

    function createHeaderMenuButtonForAggregationFunction(text: string, aggregation: NumberColumnAggregationFunction): TableHeaderMenuButton {
      if (allowedAggregationFunctions.indexOf(aggregation) !== -1) {
        let aggrButton = scout.create(TableHeaderMenuButton, {
          parent: group,
          text: text,
          cssClass: 'aggregation-function ' + aggregation,
          aggregation: aggregation,
          toggleAction: isAggregationNoneAllowed
        });
        aggrButton.on('action', onClick.bind(aggrButton));
        return aggrButton;
      }
      return null;
    }

    function onClick() {
      menuPopup.close();
      table.changeAggregation(column, this.aggregation === aggregation ? 'none' : this.aggregation);
    }
  }

  protected _renderColoringGroup(): TableHeaderMenuGroup {
    let table = this.table,
      column = this.column as NumberColumn,
      menuPopup = this,
      backgroundEffect = column.backgroundEffect,
      group = scout.create(TableHeaderMenuGroup, {
        parent: this,
        textKey: 'ui.Coloring'
      });

    this.colorGradient1Button = scout.create(TableHeaderMenuButton, {
      parent: group,
      text: '${textKey:ui.fromRedToGreen}',
      cssClass: 'color color-gradient1',
      backgroundEffect: 'colorGradient1',
      toggleAction: true
    });
    this.colorGradient1Button.on('action', onClick.bind(this.colorGradient1Button));

    this.colorGradient2Button = scout.create(TableHeaderMenuButton, {
      parent: group,
      text: '${textKey:ui.fromGreenToRed}',
      cssClass: 'color color-gradient2',
      backgroundEffect: 'colorGradient2',
      toggleAction: true
    });
    this.colorGradient2Button.on('action', onClick.bind(this.colorGradient2Button));

    if (Device.get().supportsCssGradient()) {
      this.barChartButton = scout.create(TableHeaderMenuButton, {
        parent: group,
        text: '${textKey:ui.withBarChart}',
        cssClass: 'color color-bar-chart',
        backgroundEffect: 'barChart',
        toggleAction: true
      });
      this.barChartButton.on('action', onClick.bind(this.barChartButton));
    }

    group.children.forEach((button: TableHeaderMenuButton) => button.setSelected(button.backgroundEffect === backgroundEffect));
    group.render(this.$columnActions);
    return group;

    function onClick() {
      menuPopup.close();
      table.setColumnBackgroundEffect(column, !this.selected ? null : this.backgroundEffect);
    }
  }

  protected _renderFilterTable(): JQuery {
    this.$filterTableGroup = this.$columnFilters.appendDiv('table-header-menu-group first');
    let htmlComp = HtmlComponent.install(this.$filterTableGroup, this.session);
    htmlComp.setLayout(new RowLayout());

    this.$filterTableGroupTitle = this.$filterTableGroup
      .appendDiv('table-header-menu-group-text')
      .text(this._filterByText());
    HtmlComponent.install(this.$filterTableGroupTitle, this.session);

    let $filterActions = this.$filterTableGroup.appendDiv('actions');
    this.filterSortOrderAction = scout.create(Action, {
      parent: this,
      tooltipText: '${textKey:ui.SortByNumber}',
      cssClass: `button borderless table-header-menu-toggle-sort-order ${this.filterSortMode.cssClass}`
    });
    this.filterSortOrderAction.render($filterActions);
    this.filterSortOrderAction.on('action', this._onSortModeClick.bind(this));

    this.filterToggleCheckedAction = scout.create(Action, {
      parent: this,
      text: this.session.text(this.filterCheckedMode.text),
      tooltipText: '${textKey:ui.SelectAll}',
      cssClass: 'button borderless table-header-menu-filter-toggle-checked'
    });
    this.filterToggleCheckedAction.render($filterActions);
    this.filterToggleCheckedAction.on('action', this._onFilterCheckedModeClick.bind(this));

    this.filterTable = this._createFilterTable();
    this.filterTable.ariaRules = new ListBoxAriaRules();
    this.filterTable.on('rowsChecked', this._filterTableRowsCheckedHandler);
    let tableRows: TableRowModel[] = [];
    this.filter.availableValues.forEach(filterValue => {
      let tableRow: TableRowModel = {
        cells: [
          scout.create(Cell, {
            text: (this.filter.column instanceof NumberColumn) ? filterValue.text : null,
            value: (this.filter.column instanceof NumberColumn) ? filterValue.key : filterValue.text,
            iconId: filterValue.iconId,
            htmlEnabled: filterValue.htmlEnabled,
            cssClass: filterValue.cssClass
          }),
          filterValue.count,
          filterValue.key === null ? 1 : 0 // empty cell should always be at the bottom
        ],
        checked: this.filter.selectedValues.indexOf(filterValue.key) > -1,
        dataMap: {
          filterValue: filterValue
        }
      };
      tableRows.push(tableRow);
    });
    this.filterTable.insertRows(tableRows);
    this.filterTable.render(this.$filterTableGroup);
    aria.linkElementWithLabel(this.filterTable.get$Focusable(), this.$filterTableGroupTitle);
    // must do this in a setTimeout, since table/popup is not visible yet (same as Table#revealSelection).
    setTimeout(this.filterTable.revealChecked.bind(this.filterTable));

    return this.$filterTableGroup;
  }

  protected _createFilterTable(): Table {
    let objectType = Column<any>;
    if (this.column instanceof NumberColumn) {
      objectType = NumberColumn;
    }

    return scout.create(Table, {
      parent: this,
      headerVisible: false,
      multiSelect: false,
      autoResizeColumns: true,
      checkable: true,
      cssClass: 'table-header-menu-filter-table',
      checkableStyle: Table.CheckableStyle.TABLE_ROW,
      // column-texts are not visible since header is not visible
      columns: [{
        objectType: objectType,
        text: 'filter-value',
        width: 120,
        sortIndex: 1,
        horizontalAlignment: -1
      }, {
        objectType: NumberColumn,
        text: 'aggregate-count',
        cssClass: 'table-header-menu-filter-number-column',
        width: 50,
        minWidth: 32,
        autoOptimizeWidth: true
      }, {
        objectType: NumberColumn,
        displayable: false,
        sortIndex: 0
      }]
    });
  }

  /**
   * @returns the title-text used for the filter-table
   */
  protected _filterByText(): string {
    let text = this.session.text('ui.Filter'),
      numSelected = this.filter.selectedValues.length,
      numFilters = this.filter.availableValues.length;

    if (numSelected && numFilters) {
      text += ' ' + this.session.text('ui.FilterInfoXOfY', numSelected + '', numFilters + '');
    } else if (numFilters) {
      text += ' ' + this.session.text('ui.FilterInfoCount', numFilters + '');
    }
    return text;
  }

  protected _onFilterCheckedModeClick() {
    let checkedMode = TableHeaderMenu.CheckedMode;
    let checkAll = this.filterCheckedMode.checkAll;
    this.filter.selectedValues = [];
    if (this.filterCheckedMode === checkedMode.ALL) {
      this.filterToggleCheckedAction.setTooltipText(this.session.text('ui.SelectNone'));
      this.filterCheckedMode = checkedMode.NONE;
      this.filter.availableValues.forEach(filterValue => this.filter.selectedValues.push(filterValue.key));
    } else {
      this.filterToggleCheckedAction.setTooltipText(this.session.text('ui.SelectAll'));
      this.filterCheckedMode = checkedMode.ALL;
    }
    this.filterTable.checkAll(checkAll);
    this._updateFilterTableActions();
  }

  protected _onSortModeClick() {
    let sortMode = TableHeaderMenu.SortMode;
    if (this.filterSortMode === sortMode.ALPHABETICALLY) {
      // sort by amount
      this.filterTable.sort(this.filterTable.columns[1], 'desc');
      this.filterSortMode = sortMode.AMOUNT;
      this.filterSortOrderAction.setTooltipText(this.session.text('ui.SortAlphabetically'));
    } else {
      // sort alphabetically (first by invisible column to make sure empty cells are always at the bottom)
      this.filterTable.sort(this.filterTable.columns[2], 'asc');
      this.filterTable.sort(this.filterTable.columns[0], 'asc', true);
      this.filterSortOrderAction.setTooltipText(this.session.text('ui.SortByNumber'));
      this.filterSortMode = sortMode.ALPHABETICALLY;
    }
    this._updateFilterTableActions();
  }

  protected _updateFilterTable() {
    if (this.filter.filterActive()) {
      this.table.addFilter(this.filter);
    } else {
      this.table.removeFilterByKey(this.column.id);
    }
  }

  protected _updateFilterTableActions() {
    // checked mode
    this.filterToggleCheckedAction.setText(this.session.text(this.filterCheckedMode.text));
    // sort mode
    let sortMode = TableHeaderMenu.SortMode;
    let sortAlphabetically = this.filterSortMode === TableHeaderMenu.SortMode.ALPHABETICALLY;
    this.filterSortOrderAction.$container.toggleClass(sortMode.ALPHABETICALLY.cssClass, sortAlphabetically);
    this.filterSortOrderAction.$container.toggleClass(sortMode.AMOUNT.cssClass, !sortAlphabetically);
  }

  protected _renderFilterFields(): JQuery {
    this.filterFieldsGroupBox = scout.create(FilterFieldsGroupBox, {
      parent: this,
      filter: this.filter
    });
    this.$filterFieldsGroup = this.$columnFilters.appendDiv('table-header-menu-group');
    let htmlComp = HtmlComponent.install(this.$filterFieldsGroup, this.session);
    htmlComp.setLayout(new RowLayout());
    let $filterFieldsText = this.$filterFieldsGroup
      .appendDiv('table-header-menu-group-text')
      .text(this.filter.filterFieldsTitle());
    htmlComp = HtmlComponent.install($filterFieldsText, this.session);
    this.filterFieldsGroupBox.render(this.$filterFieldsGroup);
    this.filterFieldsGroupBox.linkFieldsWithTitle($filterFieldsText);
    return this.$filterFieldsGroup;
  }

  isOpenFor($headerItem: JQuery): boolean {
    return this.rendered && this.belongsTo($headerItem);
  }

  protected _sortColumnCount(): number {
    return this.table.visibleSortColumnsCount();
  }

  protected _groupColumnCount(): number {
    return this.table.visibleGroupColumnsCount();
  }

  protected _renderCompact() {
    this.$body.toggleClass('compact', this.compact);
    this.invalidateLayoutTree();
  }

  setCompact(compact: boolean) {
    this.setProperty('compact', compact);
  }

  protected override _position(switchIfNecessary?: boolean) {
    let headerItemBounds = graphics.offsetBounds(this.$headerItem);
    let containerBounds = graphics.offsetBounds(this.tableHeader.$container);

    // Hide menu when the header item is not in view (menu gets repositioned when the table gets scrolled).
    // This cannot be done in _isAnchorInView() because the TableHeaderLayout would mark the menu as 'compact' when scrolling outside the right side of the table.
    let inView = headerItemBounds.x < containerBounds.right() && headerItemBounds.right() > containerBounds.x;
    this.$container.setVisible(inView);

    super._position(switchIfNecessary);
  }

  protected override _onAnchorScroll(event: JQuery.ScrollEvent) {
    this.position();
  }

  protected _onFilterTableRowsChecked(event: TableRowsCheckedEvent) {
    this.filter.selectedValues = [];
    this.filterTable.rows.forEach((row: TableHeaderMenuTableRow) => {
      if (row.checked) {
        this.filter.selectedValues.push(row.dataMap.filterValue.key);
      }
    });
    this._updateFilterTable();
  }

  protected _onFilterTableChanged() {
    this.$filterTableGroupTitle.text(this._filterByText());
    this._updateFilterTableCheckedMode();
    this._updateFilterTableActions();
  }

  // When no filter value is selected, we change the selection mode to ALL
  // since it makes no sense to choose NONE when no value is currently selected
  protected _updateFilterTableCheckedMode() {
    if (this.filter.selectedValues.length === 0) {
      this.filterCheckedMode = TableHeaderMenu.CheckedMode.ALL;
    } else {
      this.filterCheckedMode = TableHeaderMenu.CheckedMode.NONE;
    }
  }

  protected override _onMouseDownOutside(event: MouseEvent) {
    // close popup only if source of event is not $headerItem or one of its children.
    if (this.$headerItem.isOrHas(event.target as HTMLElement)) {
      return;
    }
    this.close();
  }
}

export type TableHeaderMenuCheckedMode = EnumObject<typeof TableHeaderMenu.CheckedMode>;
export type TableHeaderMenuSortMode = EnumObject<typeof TableHeaderMenu.SortMode>;
export type TableHeaderMenuTableRow = TableRow & { dataMap: Record<string, ColumnUserFilterValues> };
