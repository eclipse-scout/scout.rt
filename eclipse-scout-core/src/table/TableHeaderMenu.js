/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Device, graphics, HtmlComponent, NumberColumn, Point, Popup, RowLayout, scout, scrollbars, Table, TableHeaderMenuGroup, TableHeaderMenuLayout} from '../index';

export default class TableHeaderMenu extends Popup {

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

    this.$rightGroups = [];
    this.$headerItem = null;
    this.$columnActions = null;
    this.$columnFilters = null;
    this.$filterTableGroup = null;
    this.$filterToggleChecked = null;
    this.$filterTableGroupTitle = null;
    this.$filterSortOrder = null;
    this.$filterFieldsGroup = null;

    this._onColumnMovedHandler = this._onColumnMoved.bind(this);
    this._tableHeaderScrollHandler = this._onAnchorScroll.bind(this);
    this.on('locationChange', this._onLocationChange.bind(this));

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
  };

  static SortMode = {
    ALPHABETICALLY: {
      text: 'ui.SortAlphabeticallyFilter',
      cssClass: 'table-header-menu-toggle-sort-order-alphabetically'
    },
    AMOUNT: {
      text: 'ui.SortByAmountFilter',
      cssClass: 'table-header-menu-toggle-sort-order-amount'
    }
  };

  _init(options) {
    options.scrollType = options.scrollType || 'none';
    super._init(options);

    this.tableHeader = options.tableHeader;
    this.column = options.column;
    this.table = this.tableHeader.table;
    this.$headerItem = this.$anchor;

    this.table.on('columnMoved', this._onColumnMovedHandler);
    // Filtering
    this.filter = this.table.getFilter(this.column.id);
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

  _createLayout() {
    return new TableHeaderMenuLayout(this);
  }

  _render() {
    this.leftGroups = [];
    this.$rightGroups = [];

    this.$headerItem.select(true);

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

    // only add right column if filter has a filter-table or filter-fields
    if (this.hasFilterTable || this.hasFilterFields) {
      this.$columnFilters = this.$body.appendDiv('table-header-menu-filters');
      let htmlColumnFilters = HtmlComponent.install(this.$columnFilters, this.session);
      htmlColumnFilters.setLayout(new RowLayout());
    }

    this.tableHeader.$container.on('scroll', this._tableHeaderScrollHandler);

    // -- Left column -- //
    // Moving
    let movableColumns = this.table.visibleColumns().filter(column => {
      return !column.fixedPosition;
    });
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
    this.leftGroups.push(this._renderHierarchyGruop());

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

  validateFocus() {
    if (this.filterFieldsGroupBox) {
      this.filterFieldsGroupBox.focus();
    }
    // Super call will focus container if no element has been focused yet
    super.validateFocus();
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$body;
  }

  _updateFirstLast() {
    addFirstLastClass(this.leftGroups.filter(group => {
      return group.isVisible();
    }));
    addFirstLastClass(this.$rightGroups);

    function addFirstLastClass(groups) {
      groups.forEach((group, index, arr) => {
        toggleCssClass(group, 'first', index === 0);
        toggleCssClass(group, 'last', index === arr.length - 1);
      }, this);
    }

    // Note: we should refactor code for filter-fields and filter-table so they could also
    // work with a model-class (like the button menu groups). Currently this would cause
    // to much work.
    function toggleCssClass(group, cssClass, condition) {
      let $container = group instanceof TableHeaderMenuGroup ? group.$container : group;
      $container.toggleClass(cssClass, condition);
    }
  }

  _remove() {
    if (this.filterTable) {
      this.filterTable.off('rowsChecked', this._filterTableRowsCheckedHandler);
    }
    if (this.tableHeader.rendered) {
      this.tableHeader.$container.off('scroll', this._tableHeaderScrollHandler);
    }
    this.$headerItem.select(false);
    this.table.off('columnMoved', this._onColumnMovedHandler);
    this.table.off('filterAdded', this._tableFilterHandler);
    this.table.off('filterRemoved', this._tableFilterHandler);
    super._remove();

    // table may have been removed in the meantime
    if (this.table.rendered) {
      this.table.$container.removeClass('focused');
    }
  }

  _renderMovingGroup() {
    let table = this.table,
      column = this.column,
      pos = table.visibleColumns().indexOf(column);

    this.moveGroup = scout.create('TableHeaderMenuGroup', {
      parent: this,
      textKey: 'ui.Move',
      cssClass: 'first'
    });
    this.toBeginButton = scout.create('TableHeaderMenuButton', {
      parent: this.moveGroup,
      text: '${textKey:ui.toBegin}',
      cssClass: 'move move-top'
    });
    this.toBeginButton.on('action', () => {
      table.moveColumn(column, pos, 0);
      pos = table.visibleColumns().indexOf(column);
    });

    this.forwardButton = scout.create('TableHeaderMenuButton', {
      parent: this.moveGroup,
      text: '${textKey:ui.forward}',
      cssClass: 'move move-up'
    });
    this.forwardButton.on('action', () => {
      table.moveColumn(column, pos, Math.max(pos - 1, 0));
      pos = table.visibleColumns().indexOf(column);
    });

    this.backwardButton = scout.create('TableHeaderMenuButton', {
      parent: this.moveGroup,
      text: '${textKey:ui.backward}',
      cssClass: 'move move-down'
    });
    this.backwardButton.on('action', () => {
      table.moveColumn(column, pos, Math.min(pos + 1, table.header.findHeaderItems().length - 1));
      pos = table.visibleColumns().indexOf(column);
    });

    this.toEndButton = scout.create('TableHeaderMenuButton', {
      parent: this.moveGroup,
      text: '${textKey:ui.toEnd}',
      cssClass: 'move move-bottom'
    });
    this.toEndButton.on('action', () => {
      table.moveColumn(column, pos, table.header.findHeaderItems().length - 1);
      pos = table.visibleColumns().indexOf(column);
    });

    this.moveGroup.render(this.$columnActions);
    return this.moveGroup;
  }

  _onColumnMoved() {
    let table = this.table,
      column = this.column;

    if (this.moveGroup) {
      let visibleColumns = table.visibleColumns();
      let columnIndex = table.visibleColumns().indexOf(column);
      let forwardEnabled = visibleColumns[columnIndex - 1] && !visibleColumns[columnIndex - 1].fixedPosition;
      let backwardEnabled = visibleColumns[columnIndex + 1] && !visibleColumns[columnIndex + 1].fixedPosition;

      this.toBeginButton.setEnabled(forwardEnabled);
      this.forwardButton.setEnabled(forwardEnabled);
      this.backwardButton.setEnabled(backwardEnabled);
      this.toEndButton.setEnabled(backwardEnabled);
    }

    this.hierarchyGroup.setVisible(this.table.isTableNodeColumn(column));
    this._updateFirstLast();
  }

  _isColumnActionsGroupVisible() {
    return this.table.columnAddable || this.column.removable || this.column.modifiable;
  }

  _renderColumnActionsGroup() {
    let column = this.column,
      menuPopup = this;

    this.columnActionsGroup = scout.create('TableHeaderMenuGroup', {
      parent: this,
      textKey: 'ui.Column'
    });

    this.addColumnButton = scout.create('TableHeaderMenuButton', {
      parent: this.columnActionsGroup,
      text: '${textKey:ui.addColumn}',
      cssClass: 'add-column',
      visible: this.table.columnAddable
    });
    this.addColumnButton.on('action', onClick.bind(this, 'add'));

    this.removeColumnButton = scout.create('TableHeaderMenuButton', {
      parent: this.columnActionsGroup,
      text: '${textKey:ui.removeColumn}',
      cssClass: 'remove-column',
      visible: this.column.removable
    });
    this.removeColumnButton.on('action', onClick.bind(this, 'remove'));

    this.modifyColumnButton = scout.create('TableHeaderMenuButton', {
      parent: this.columnActionsGroup,
      text: '${textKey:ui.changeColumn}',
      cssClass: 'change-column',
      visible: this.column.modifiable
    });
    this.modifyColumnButton.on('action', onClick.bind(this, 'modify'));

    this.columnActionsGroup.render(this.$columnActions);
    return this.columnActionsGroup;

    function onClick(action) {
      menuPopup.close();
      this.table.trigger('columnOrganizeAction', {
        action: action,
        column: column
      });
    }
  }

  onColumnActionsChanged(event) {
    this.addColumnButton.setVisible(event.addVisible);
    this.removeColumnButton.setVisible(event.removeVisible);
    this.modifyColumnButton.setVisible(event.modifyVisible);
    let groupVisible = this.columnActionsGroup.children.some(button => {
      return button.visible;
    });
    this.columnActionsGroup.setVisible(groupVisible);
  }

  _renderSortingGroup() {
    let table = this.table,
      column = this.column,
      menuPopup = this;

    this.sortingGroup = scout.create('TableHeaderMenuGroup', {
      parent: this,
      textKey: 'ColumnSorting'
    });

    if (!table.hasPermanentHeadOrTailSortColumns()) {
      this.sortAscButton = scout.create('TableHeaderMenuButton', {
        parent: this.sortingGroup,
        text: '${textKey:ui.ascending}',
        cssClass: 'sort sort-asc',
        direction: 'asc',
        toggleAction: true
      });
      this.sortAscButton.on('action', onSortClick.bind(this.sortAscButton));

      this.sortDescButton = scout.create('TableHeaderMenuButton', {
        parent: this.sortingGroup,
        text: '${textKey:ui.descending}',
        cssClass: 'sort sort-desc',
        direction: 'desc',
        toggleAction: true
      });
      this.sortDescButton.on('action', onSortClick.bind(this.sortDescButton));
    }

    this.sortAscAddButton = scout.create('TableHeaderMenuButton', {
      parent: this.sortingGroup,
      text: '${textKey:ui.ascendingAdditionally}',
      cssClass: 'sort sort-asc-add',
      direction: 'asc',
      toggleAction: true
    });
    this.sortAscAddButton.on('action', onSortAdditionalClick.bind(this.sortAscAddButton));

    this.sortDescAddButton = scout.create('TableHeaderMenuButton', {
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

    function sort(direction, multiSort, remove) {
      table.sort(column, direction, multiSort, remove);
      menuPopup._updateSortingSelectedState();
    }
  }

  _updateSortingSelectedState() {
    if (!this.table.sortEnabled) {
      return;
    }

    let addIcon,
      showAddCommands = false,
      sortCount = this._sortColumnCount();

    this.sortingGroup.children.forEach(button => {
      button.setSelected(false);
    });

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
        addIcon = this.column.sortIndex + 1;
        this.sortAscAddButton.setIconId(addIcon);
        this.sortDescAddButton.setIconId(addIcon);
      }
    }

    this.sortAscAddButton.setVisible(showAddCommands);
    this.sortDescAddButton.setVisible(showAddCommands);
  }

  _renderGroupingGroup() {
    let menuPopup = this,
      table = this.table,
      column = this.column,
      groupCount = this._groupColumnCount();

    let group = scout.create('TableHeaderMenuGroup', {
      parent: this,
      textKey: 'ui.Grouping'
    });

    this.groupButton = scout.create('TableHeaderMenuButton', {
      parent: group,
      text: '${textKey:ui.groupingApply}',
      cssClass: 'group',
      additional: false,
      toggleAction: true
    });
    this.groupButton.on('action', groupColumn.bind(this.groupButton));

    this.groupAddButton = scout.create('TableHeaderMenuButton', {
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
        this.groupAddButton.setIconId(this.column.sortIndex + 1);
      }
    }

    group.render(this.$columnActions);
    return group;

    function groupColumn() {
      let direction = (column.sortIndex >= 0 && !column.sortAscending) ? 'desc' : 'asc';
      menuPopup.close();
      table.groupColumn(column, this.additional, direction, !this.selected);
    }
  }

  _renderHierarchyGruop() {
    let table = this.table,
      menuPopup = this;
    this.hierarchyGroup = scout.create('TableHeaderMenuGroup', {
      parent: this,
      textKey: 'ui.Hierarchy',
      visible: this.table.isTableNodeColumn(this.column)
    });

    let collapseAllButton = scout.create('TableHeaderMenuButton', {
      parent: this.hierarchyGroup,
      text: '${textKey:ui.CollapseAll}',
      cssClass: 'hierarchy-collapse-all',
      enabled: !!arrays.find(table.rows, row => {
        return row.expanded && !arrays.empty(row.childRows);
      })
    });
    collapseAllButton.on('action', () => {
      menuPopup.close();
      table.collapseAll();
    });

    let expandAllButton = scout.create('TableHeaderMenuButton', {
      parent: this.hierarchyGroup,
      text: '${textKey:ui.ExpandAll}',
      cssClass: 'hierarchy-expand-all',
      enabled: !!arrays.find(table.rows, row => {
        return !row.expanded && !arrays.empty(row.childRows);
      })
    });
    expandAllButton.on('action', () => {
      menuPopup.close();
      table.expandAll();
    });

    this.hierarchyGroup.render(this.$columnActions);
    return this.hierarchyGroup;
  }

  _renderAggregationGroup() {
    let table = this.table,
      column = this.column,
      aggregation = column.aggregationFunction,
      menuPopup = this,
      group = scout.create('TableHeaderMenuGroup', {
        parent: this,
        textKey: 'ui.Aggregation'
      }),
      allowedAggregationFunctions = arrays.ensure(column.allowedAggregationFunctions),
      isAggregationNoneAllowed = allowedAggregationFunctions.indexOf('none') !== -1;

    createHeaderMenuButtonForAggregationFunction('${textKey:ui.Sum}', 'sum');
    createHeaderMenuButtonForAggregationFunction('${textKey:ui.Average}', 'avg');
    createHeaderMenuButtonForAggregationFunction('${textKey:ui.Minimum}', 'min');
    createHeaderMenuButtonForAggregationFunction('${textKey:ui.Maximum}', 'max');

    group.children.forEach(button => {
      button.setSelected(button.aggregation === aggregation);
    });
    group.render(this.$columnActions);
    return group;

    function createHeaderMenuButtonForAggregationFunction(text, aggregation) {
      if (allowedAggregationFunctions.indexOf(aggregation) !== -1) {
        let aggrButton = scout.create('TableHeaderMenuButton', {
          parent: group,
          text: text,
          cssClass: 'aggregation-function ' + aggregation,
          aggregation: aggregation,
          toggleAction: isAggregationNoneAllowed
        });
        aggrButton.on('action', onClick.bind(aggrButton));
      }
    }

    function onClick() {
      menuPopup.close();
      table.changeAggregation(column, this.aggregation === aggregation ? 'none' : this.aggregation);
    }
  }

  _renderColoringGroup() {
    let table = this.table,
      column = this.column,
      menuPopup = this,
      backgroundEffect = column.backgroundEffect,
      group = scout.create('TableHeaderMenuGroup', {
        parent: this,
        textKey: 'ui.Coloring'
      });

    this.colorGradient1Button = scout.create('TableHeaderMenuButton', {
      parent: group,
      text: '${textKey:ui.fromRedToGreen}',
      cssClass: 'color color-gradient1',
      backgroundEffect: 'colorGradient1',
      toggleAction: true
    });
    this.colorGradient1Button.on('action', onClick.bind(this.colorGradient1Button));

    this.colorGradient2Button = scout.create('TableHeaderMenuButton', {
      parent: group,
      text: '${textKey:ui.fromGreenToRed}',
      cssClass: 'color color-gradient2',
      backgroundEffect: 'colorGradient2',
      toggleAction: true
    });
    this.colorGradient2Button.on('action', onClick.bind(this.colorGradient2Button));

    if (Device.get().supportsCssGradient()) {
      this.barChartButton = scout.create('TableHeaderMenuButton', {
        parent: group,
        text: '${textKey:ui.withBarChart}',
        cssClass: 'color color-bar-chart',
        backgroundEffect: 'barChart',
        toggleAction: true
      });
      this.barChartButton.on('action', onClick.bind(this.barChartButton));
    }

    group.children.forEach(button => {
      button.setSelected(button.backgroundEffect === backgroundEffect);
    });
    group.render(this.$columnActions);
    return group;

    function onClick() {
      menuPopup.close();
      table.setColumnBackgroundEffect(column, !this.selected ? null : this.backgroundEffect);
    }
  }

  _renderFilterTable() {
    let $filterActions;

    this.$filterTableGroup = this.$columnFilters
      .appendDiv('table-header-menu-group first');
    let htmlComp = HtmlComponent.install(this.$filterTableGroup, this.session);
    htmlComp.setLayout(new RowLayout());

    $filterActions = this.$filterTableGroup
      .appendDiv('table-header-menu-filter-actions');

    this.$filterSortOrder = $filterActions
      .appendDiv('link table-header-menu-toggle-sort-order')
      .on('click', this._onSortModeClick.bind(this))
      .addClass(this.filterSortMode.cssClass);

    this.$filterToggleChecked = $filterActions
      .appendDiv('link table-header-menu-filter-toggle-checked')
      .text(this.session.text(this.filterCheckedMode.text))
      .on('click', this._onFilterCheckedModeClick.bind(this));

    this.$filterTableGroupTitle = this.$filterTableGroup
      .appendDiv('table-header-menu-group-text')
      .text(this._filterByText());
    HtmlComponent.install(this.$filterTableGroupTitle, this.session);

    this.filterTable = this._createFilterTable();
    this.filterTable.on('rowsChecked', this._filterTableRowsCheckedHandler);
    let tableRow, tableRows = [];
    this.filter.availableValues.forEach(function(filterValue) {
      tableRow = {
        cells: [
          scout.create('Cell', {
            text: (this.filter.column.objectType === 'NumberColumn') ? filterValue.text : null,
            value: (this.filter.column.objectType === 'NumberColumn') ? filterValue.key : filterValue.text,
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
    }, this);
    this.filterTable.insertRows(tableRows);
    this.filterTable.render(this.$filterTableGroup);

    // must do this in a setTimeout, since table/popup is not visible yet (same as Table#revealSelection).
    setTimeout(this.filterTable.revealChecked.bind(this.filterTable));

    return this.$filterTableGroup;
  }

  _createFilterTable() {
    let objectType = 'Column';
    if (this.column.objectType === 'NumberColumn') {
      objectType = this.column.objectType;
    }

    return scout.create('Table', {
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
        sortActive: true,
        sortIndex: 1,
        horizontalAlignment: -1
      }, {
        objectType: 'NumberColumn',
        text: 'aggregate-count',
        cssClass: 'table-header-menu-filter-number-column',
        width: 50,
        minWidth: 32,
        autoOptimizeWidth: true
      }, {
        objectType: 'NumberColumn',
        displayable: false,
        sortActive: true,
        sortIndex: 0
      }]
    });
  }

  /**
   * @returns {string} the title-text used for the filter-table
   */
  _filterByText() {
    let text = this.session.text('ui.Filter'),
      numSelected = this.filter.selectedValues.length,
      numFilters = this.filter.availableValues.length;

    if (numSelected && numFilters) {
      text += ' ' + this.session.text('ui.FilterInfoXOfY', numSelected, numFilters);
    } else if (numFilters) {
      text += ' ' + this.session.text('ui.FilterInfoCount', numFilters);
    }
    return text;
  }

  _onFilterCheckedModeClick() {
    let checkedMode = TableHeaderMenu.CheckedMode;
    let checkAll = this.filterCheckedMode.checkAll;
    this.filter.selectedValues = [];
    if (this.filterCheckedMode === checkedMode.ALL) {
      this.filterCheckedMode = checkedMode.NONE;
      this.filter.availableValues.forEach(function(filterValue) {
        this.filter.selectedValues.push(filterValue.key);
      }, this);
    } else {
      this.filterCheckedMode = checkedMode.ALL;
    }
    this.filterTable.checkAll(checkAll);
    this._updateFilterTableActions();
  }

  _onSortModeClick() {
    let sortMode = TableHeaderMenu.SortMode;
    if (this.filterSortMode === sortMode.ALPHABETICALLY) {
      // sort by amount
      this.filterTable.sort(this.filterTable.columns[1], 'desc');
      this.filterSortMode = sortMode.AMOUNT;
    } else {
      // sort alphabetically (first by invisible column to make sure empty cells are always at the bottom)
      this.filterTable.sort(this.filterTable.columns[2], 'asc');
      this.filterTable.sort(this.filterTable.columns[0], 'asc', true);
      this.filterSortMode = sortMode.ALPHABETICALLY;
    }
    this._updateFilterTableActions();
  }

  _updateFilterTable() {
    if (this.filter.filterActive()) {
      this.table.addFilter(this.filter);
    } else {
      this.table.removeFilterByKey(this.column.id);
    }
  }

  _updateFilterTableActions() {
    // checked mode
    this.$filterToggleChecked.text(this.session.text(this.filterCheckedMode.text));
    // sort mode
    let sortMode = TableHeaderMenu.SortMode;
    let sortAlphabetically = this.filterSortMode === TableHeaderMenu.SortMode.ALPHABETICALLY;
    this.$filterSortOrder.toggleClass(sortMode.ALPHABETICALLY.cssClass, sortAlphabetically);
    this.$filterSortOrder.toggleClass(sortMode.AMOUNT.cssClass, !sortAlphabetically);
  }

  _renderFilterFields() {
    this.filterFieldsGroupBox = scout.create('GroupBox:FilterFields', {
      parent: this,
      column: this.column,
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
    return this.$filterFieldsGroup;
  }

  isOpenFor($headerItem) {
    return this.rendered && this.belongsTo($headerItem);
  }

  _countColumns(propertyName) {
    return this.table.visibleColumns().reduce((sum, column) => {
      return sum + (column[propertyName] ? 1 : 0);
    }, 0);
  }

  _sortColumnCount() {
    return this._countColumns('sortActive');
  }

  _groupColumnCount() {
    return this._countColumns('grouped');
  }

  _renderCompact() {
    this.$body.toggleClass('compact', this.compact);
    this.invalidateLayoutTree();
  }

  setCompact(compact) {
    this.setProperty('compact', compact);
  }

  _onLocationChange(event) {
    let inView, containerBounds,
      isLocationInView = scrollbars.isLocationInView,
      headerItemBounds = graphics.offsetBounds(this.$headerItem),
      $tableHeaderContainer = this.tableHeader.$container;

    this.$container.setVisible(true);
    containerBounds = graphics.offsetBounds(this.$container);

    // menu must only be visible if the header item is in view (menu gets repositioned when the table gets scrolled -> make sure it won't be displayed outside of the table)
    // check left side of the header item (necessary if header item is moved outside on the left side of the table)
    inView = isLocationInView(new Point(headerItemBounds.x, headerItemBounds.y), $tableHeaderContainer);
    if (!inView) {
      // if left side of the header is not in view, check if right side of the header and the menu, both must be visible)
      // check right side of the header item (necessary if header item is moved outside on the right side of the table)
      inView = isLocationInView(new Point(headerItemBounds.x + headerItemBounds.width, headerItemBounds.y + headerItemBounds.height), $tableHeaderContainer);
      // check right side of the menu (necessary if header item is larger than menu, and if header item is moved outside on the left side of the table)
      inView = inView && isLocationInView(new Point(containerBounds.x + containerBounds.width, containerBounds.y), $tableHeaderContainer);
    }
    this.$container.setVisible(inView);
  }

  _onAnchorScroll(event) {
    this.position();
  }

  _onFilterTableRowsChecked(event) {
    this.filter.selectedValues = [];
    this.filterTable.rows.forEach(function(row) {
      if (row.checked) {
        this.filter.selectedValues.push(row.dataMap.filterValue.key);
      }
    }, this);
    this._updateFilterTable();
  }

  _onFilterTableChanged() {
    this.$filterTableGroupTitle.text(this._filterByText());
    this._updateFilterTableCheckedMode();
    this._updateFilterTableActions();
  }

  // When no filter value is selected, we change the selection mode to ALL
  // since it makes no sense to choose NONE when no value is currently selected
  _updateFilterTableCheckedMode() {
    if (this.filter.selectedValues.length === 0) {
      this.filterCheckedMode = TableHeaderMenu.CheckedMode.ALL;
    } else {
      this.filterCheckedMode = TableHeaderMenu.CheckedMode.NONE;
    }
  }

  _onMouseDownOutside(event) {
    // close popup only if source of event is not $headerItem or one of it's children.
    if (this.$headerItem.isOrHas(event.target)) {
      return;
    }
    this.close();
  }
}
