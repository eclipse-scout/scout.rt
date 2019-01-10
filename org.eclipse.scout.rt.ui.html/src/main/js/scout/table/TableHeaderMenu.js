/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableHeaderMenu = function() {
  scout.TableHeaderMenu.parent.call(this);
  this.column = null;
  this.tableHeader = null;
  this.table = null;
  this.filter = null;
  this.filterCheckedMode = scout.TableHeaderMenu.CheckedMode.ALL;
  this.filterSortMode = scout.TableHeaderMenu.SortMode.ALPHABETICALLY;
  this.hasFilterTable = false;
  this.hasFilterFields = false;

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
};
scout.inherits(scout.TableHeaderMenu, scout.Popup);

scout.TableHeaderMenu.CheckedMode = {
  ALL: {
    checkAll: true,
    text: 'ui.SelectAllFilter'
  },
  NONE: {
    checkAll: false,
    text: 'ui.SelectNoneFilter'
  }
};

scout.TableHeaderMenu.SortMode = {
  ALPHABETICALLY: {
    text: 'ui.SortAlphabeticallyFilter',
    cssClass: 'table-header-menu-toggle-sort-order-alphabetically'
  },
  AMOUNT: {
    text: 'ui.SortByAmountFilter',
    cssClass: 'table-header-menu-toggle-sort-order-amount'
  }
};

scout.TableHeaderMenu.prototype._init = function(options) {
  options.scrollType = options.scrollType || 'none';
  scout.TableHeaderMenu.parent.prototype._init.call(this, options);

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
  this.filter.on('filterFieldsChanged', this._updateFilterTable.bind(this)); // TODO [7.0] awe: (filter) off handler?
  this._updateFilterTableCheckedMode();

  this.hasFilterTable = this.filter.availableValues.length > 0;
  this.hasFilterFields = this.filter.hasFilterFields;

  if (this.hasFilterTable) {
    this._tableFilterHandler = this._onFilterTableChanged.bind(this);
    this.table.on('filterAdded', this._tableFilterHandler);
    this.table.on('filterRemoved', this._tableFilterHandler);
    this._filterTableRowsCheckedHandler = this._onFilterTableRowsChecked.bind(this);
  }
};

scout.TableHeaderMenu.prototype._createLayout = function() {
  return new scout.TableHeaderMenuLayout(this);
};

scout.TableHeaderMenu.prototype._render = function() {
  this.leftGroups = [];
  this.$rightGroups = [];

  this.$headerItem.select(true);

  this.$container = this.$parent.appendDiv('table-header-menu');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
  this.$body = this.$container.appendDiv('table-header-menu-body');
  scout.HtmlComponent.install(this.$body, this.session);
  this._installScrollbars({
    axis: 'y'
  });
  this.$columnActions = this.$body.appendDiv('table-header-menu-actions');

  // only add right column if filter has a filter-table or filter-fields
  if (this.hasFilterTable || this.hasFilterFields) {
    this.$columnFilters = this.$body.appendDiv('table-header-menu-filters');
    var htmlColumnFilters = scout.HtmlComponent.install(this.$columnFilters, this.session);
    htmlColumnFilters.setLayout(new scout.RowLayout());
  }

  this.tableHeader.$container.on('scroll', this._tableHeaderScrollHandler);
  this.$whiter = this.$container.appendDiv('table-header-menu-whiter');

  if (this.withFocusContext && this.focusableContainer) {
    this.$container.attr('tabindex', -1);
  }

  // -- Left column -- //
  // Moving
  var movableColumns = this.table.visibleColumns().filter(function(column) {
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
  if (this.column instanceof scout.NumberColumn) {
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
};

/**
 * @override
 */
scout.TableHeaderMenu.prototype.get$Scrollable = function() {
  return this.$body;
};

scout.TableHeaderMenu.prototype._updateFirstLast = function() {
  addFirstLastClass(this.leftGroups.filter(function(group) {
    return group.isVisible();
  }));
  addFirstLastClass(this.$rightGroups);

  function addFirstLastClass(groups) {
    groups.forEach(function(group, index, arr) {
      toggleCssClass(group, 'first', index === 0);
      toggleCssClass(group, 'last', index === arr.length - 1);
    }, this);
  }

  // Note: we should refactor code for filter-fields and filter-table so they could also
  // work with a model-class (like the button menu groups). Currently this would cause
  // to much work.
  function toggleCssClass(group, cssClass, condition) {
    var $container = group instanceof scout.TableHeaderMenuGroup ? group.$container : group;
    $container.toggleClass(cssClass, condition);
  }
};

scout.TableHeaderMenu.prototype._remove = function() {
  if (this.filterTable) {
    this.filterTable.off('rowsChecked', this._filterTableRowsCheckedHandler);
  }
  this.tableHeader.$container.off('scroll', this._tableHeaderScrollHandler);
  this.$headerItem.select(false);
  this.table.off('columnMoved', this._onColumnMovedHandler);
  this.table.off('filterAdded', this._tableFilterHandler);
  this.table.off('filterRemoved', this._tableFilterHandler);
  scout.TableHeaderMenu.parent.prototype._remove.call(this);

  // table may have been removed in the meantime
  if (this.table.rendered) {
    this.table.$container.removeClass('focused');
  }
};

scout.TableHeaderMenu.prototype._renderMovingGroup = function() {
  var table = this.table,
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
  this.toBeginButton.on('action', function() {
    table.moveColumn(column, pos, 0);
    pos = table.visibleColumns().indexOf(column);
  });

  this.forwardButton = scout.create('TableHeaderMenuButton', {
    parent: this.moveGroup,
    text: '${textKey:ui.forward}',
    cssClass: 'move move-up'
  });
  this.forwardButton.on('action',  function() {
    table.moveColumn(column, pos, Math.max(pos - 1, 0));
    pos = table.visibleColumns().indexOf(column);
  });

  this.backwardButton = scout.create('TableHeaderMenuButton', {
    parent: this.moveGroup,
    text: '${textKey:ui.backward}',
    cssClass: 'move move-down'
  });
  this.backwardButton.on('action', function() {
    table.moveColumn(column, pos, Math.min(pos + 1, table.header.findHeaderItems().length - 1));
    pos = table.visibleColumns().indexOf(column);
  });

  this.toEndButton = scout.create('TableHeaderMenuButton', {
    parent: this.moveGroup,
    text: '${textKey:ui.toEnd}',
    cssClass: 'move move-bottom'
  });
  this.toEndButton.on('action', function() {
    table.moveColumn(column, pos, table.header.findHeaderItems().length - 1);
    pos = table.visibleColumns().indexOf(column);
  });

  this.moveGroup.render(this.$columnActions);
  return this.moveGroup;
};

scout.TableHeaderMenu.prototype._onColumnMoved = function() {
  var table = this.table,
    column = this.column;

  if (this.moveGroup) {
    var backwardEnabled = scout.arrays.find(table.visibleColumns(), function(column) {
        return !column.fixedPosition;
      }) !== column,
      forwardEnabled = scout.arrays.find(table.visibleColumns().slice().reverse(), function(column) {
        return !column.fixedPosition;
      }) !== column;

    this.toBeginButton.setEnabled(backwardEnabled);
    this.forwardButton.setEnabled(backwardEnabled);
    this.backwardButton.setEnabled(forwardEnabled);
    this.toEndButton.setEnabled(forwardEnabled);
  }

  this.hierarchyGroup.setVisible(this.table.isTableNodeColumn(column));
  this._updateFirstLast();
};

scout.TableHeaderMenu.prototype._isColumnActionsGroupVisible = function() {
  return this.table.columnAddable || this.column.removable || this.column.modifiable;
};

scout.TableHeaderMenu.prototype._renderColumnActionsGroup = function() {
  var column = this.column,
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
};

scout.TableHeaderMenu.prototype.onColumnActionsChanged = function(event) {
  this.addColumnButton.setVisible(event.addVisible);
  this.removeColumnButton.setVisible(event.removeVisible);
  this.modifyColumnButton.setVisible(event.modifyVisible);
  var groupVisible = this.columnActionsGroup.children.some(function(button) {
    return button.visible;
  });
  this.columnActionsGroup.setVisible(groupVisible);
};

scout.TableHeaderMenu.prototype._renderSortingGroup = function() {
  var table = this.table,
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
};

scout.TableHeaderMenu.prototype._updateSortingSelectedState = function() {
  if (!this.table.sortEnabled) {
    return;
  }

  var addIcon,
    showAddCommands = false,
    sortCount = this._sortColumnCount();

  this.sortingGroup.children.forEach(function(button) {
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
};

scout.TableHeaderMenu.prototype._renderGroupingGroup = function() {
  var menuPopup = this,
    table = this.table,
    column = this.column,
    groupCount = this._groupColumnCount();

  var group = scout.create('TableHeaderMenuGroup', {
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
    var direction = (column.sortIndex >= 0 && !column.sortAscending) ? 'desc' : 'asc';
    menuPopup.close();
    table.groupColumn(column, this.additional, direction, !this.selected);
  }
};

scout.TableHeaderMenu.prototype._renderHierarchyGruop = function() {
  var table = this.table,
    menuPopup = this;
  this.hierarchyGroup = scout.create('TableHeaderMenuGroup', {
    parent: this,
    textKey: 'ui.Hierarchy',
    visible: this.table.isTableNodeColumn(this.column)
  });

  var collapseAllButton = scout.create('TableHeaderMenuButton', {
    parent: this.hierarchyGroup,
    text: '${textKey:ui.CollapseAll}',
    cssClass: 'hierarchy-collapse-all',
    enabled: !!scout.arrays.find(table.rows, function(row) {
      return row.expanded && !scout.arrays.empty(row.childRows);
    })
  });
  collapseAllButton.on('action', function() {
    menuPopup.close();
    table.collapseAll();
  });

  var expandAllButton = scout.create('TableHeaderMenuButton', {
    parent: this.hierarchyGroup,
    text: '${textKey:ui.ExpandAll}',
    cssClass: 'hierarchy-expand-all',
    enabled: !!scout.arrays.find(table.rows, function(row) {
      return !row.expanded && !scout.arrays.empty(row.childRows);
    })
  });
  expandAllButton.on('action', function() {
    menuPopup.close();
    table.expandAll();
  });

  this.hierarchyGroup.render(this.$columnActions);
  return this.hierarchyGroup;
};

scout.TableHeaderMenu.prototype._renderAggregationGroup = function() {
  var table = this.table,
    column = this.column,
    aggregation = column.aggregationFunction,
    menuPopup = this,
    group = scout.create('TableHeaderMenuGroup', {
      parent: this,
      textKey: 'ui.Aggregation'
    }),
    allowedAggregationFunctions = scout.arrays.ensure(column.allowedAggregationFunctions),
    isAggregationNoneAllowed = allowedAggregationFunctions.indexOf('none') !== -1;

  createHeaderMenuButtonForAggregationFunction('${textKey:ui.Sum}', 'sum');
  createHeaderMenuButtonForAggregationFunction('${textKey:ui.Average}', 'avg');
  createHeaderMenuButtonForAggregationFunction('${textKey:ui.Minimum}', 'min');
  createHeaderMenuButtonForAggregationFunction('${textKey:ui.Maximum}', 'max');

  group.children.forEach(function(button) {
    button.setSelected(button.aggregation === aggregation);
  });
  group.render(this.$columnActions);
  return group;

  function createHeaderMenuButtonForAggregationFunction(text, aggregation) {
    if (allowedAggregationFunctions.indexOf(aggregation) !== -1) {
      var aggrButton = scout.create('TableHeaderMenuButton', {
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
};

scout.TableHeaderMenu.prototype._renderColoringGroup = function() {
  var table = this.table,
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

  if (scout.device.supportsCssGradient()) {
    this.barChartButton = scout.create('TableHeaderMenuButton', {
      parent: group,
      text: '${textKey:ui.withBarChart}',
      cssClass: 'color color-bar-chart',
      backgroundEffect: 'barChart',
      toggleAction: true
    });
    this.barChartButton.on('action', onClick.bind(this.barChartButton));
  }

  group.children.forEach(function(button) {
    button.setSelected(button.backgroundEffect === backgroundEffect);
  });
  group.render(this.$columnActions);
  return group;

  function onClick() {
    menuPopup.close();
    table.setColumnBackgroundEffect(column, !this.selected ? null : this.backgroundEffect);
  }
};

scout.TableHeaderMenu.prototype._renderFilterTable = function() {
  var $filterActions;

  this.$filterTableGroup = this.$columnFilters
    .appendDiv('table-header-menu-group first');
  var htmlComp = scout.HtmlComponent.install(this.$filterTableGroup, this.session);
  htmlComp.setLayout(new scout.RowLayout());

  $filterActions = this.$filterTableGroup
    .appendDiv('table-header-menu-filter-actions');

  this.$filterSortOrder = $filterActions
    .appendDiv('table-header-menu-toggle-sort-order')
    .on('click', this._onSortModeClick.bind(this))
    .addClass(this.filterSortMode.cssClass);

  this.$filterToggleChecked = $filterActions
    .appendDiv('table-header-menu-filter-toggle-checked')
    .text(this.session.text(this.filterCheckedMode.text))
    .on('click', this._onFilterCheckedModeClick.bind(this));

  this.$filterTableGroupTitle = this.$filterTableGroup
    .appendDiv('table-header-menu-group-text')
    .text(this._filterByText());
  scout.HtmlComponent.install(this.$filterTableGroupTitle, this.session);

  this.filterTable = scout.create('Table', {
    parent: this,
    headerVisible: false,
    multiSelect: false,
    autoResizeColumns: true,
    checkable: true,
    checkableStyle: scout.Table.CheckableStyle.TABLE_ROW,
    // column-texts are not visible since header is not visible
    columns: [{
      objectType: 'Column',
      text: 'filter-value',
      width: 160,
      sortActive: true,
      sortIndex: 1
    }, {
      objectType: 'NumberColumn',
      text: 'aggregate-count',
      width: 40
    }, {
      objectType: 'NumberColumn',
      displayable: false,
      sortActive: true,
      sortIndex: 0
    }]
  });
  this.filterTable.on('rowsChecked', this._filterTableRowsCheckedHandler);
  var tableRow, tableRows = [];
  this.filter.availableValues.forEach(function(filterValue) {
    tableRow = {
      cells: [
        scout.create('Cell', {
          value: filterValue.text,
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
  this.filterTable.htmlComp.pixelBasedSizing = true;

  // must do this in a setTimeout, since table/popup is not visible yet (same as Table#revealSelection).
  setTimeout(this.filterTable.revealChecked.bind(this.filterTable));

  return this.$filterTableGroup;
};

/**
 * @returns the title-text used for the filter-table
 */
scout.TableHeaderMenu.prototype._filterByText = function() {
  var text = this.session.text('ui.Filter'),
    numSelected = this.filter.selectedValues.length,
    numFilters = this.filter.availableValues.length;

  if (numSelected && numFilters) {
    text += ' ' + this.session.text('ui.FilterInfoXOfY', numSelected, numFilters);
  } else if (numFilters) {
    text += ' ' + this.session.text('ui.FilterInfoCount', numFilters);
  }
  return text;
};

scout.TableHeaderMenu.prototype._onFilterCheckedModeClick = function() {
  var checkedMode = scout.TableHeaderMenu.CheckedMode;
  var checkAll = this.filterCheckedMode.checkAll;
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
};

scout.TableHeaderMenu.prototype._onSortModeClick = function() {
  var sortMode = scout.TableHeaderMenu.SortMode;
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
};

scout.TableHeaderMenu.prototype._updateFilterTable = function() {
  if (this.filter.filterActive()) {
    this.table.addFilter(this.filter);
  } else {
    this.table.removeFilterByKey(this.column.id);
  }
  // callback to table
  this.table.filter();
};

scout.TableHeaderMenu.prototype._updateFilterTableActions = function() {
  // checked mode
  this.$filterToggleChecked.text(this.session.text(this.filterCheckedMode.text));
  // sort mode
  var sortMode = scout.TableHeaderMenu.SortMode;
  var sortAlphabetically = this.filterSortMode === scout.TableHeaderMenu.SortMode.ALPHABETICALLY;
  this.$filterSortOrder.toggleClass(sortMode.ALPHABETICALLY.cssClass, sortAlphabetically);
  this.$filterSortOrder.toggleClass(sortMode.AMOUNT.cssClass, !sortAlphabetically);
};

scout.TableHeaderMenu.prototype._renderFilterFields = function() {
  this.filterFieldsGroupBox = scout.create('GroupBox:FilterFields', {
    parent: this,
    column: this.column,
    filter: this.filter
  });
  this.$filterFieldsGroup = this.$columnFilters.appendDiv('table-header-menu-group');
  var htmlComp = scout.HtmlComponent.install(this.$filterFieldsGroup, this.session);
  htmlComp.setLayout(new scout.RowLayout());
  var $filterFieldsText = this.$filterFieldsGroup
    .appendDiv('table-header-menu-group-text')
    .text(this.filter.filterFieldsTitle());
  htmlComp = scout.HtmlComponent.install($filterFieldsText, this.session);
  this.filterFieldsGroupBox.render(this.$filterFieldsGroup);
  return this.$filterFieldsGroup;
};

scout.TableHeaderMenu.prototype.isOpenFor = function($headerItem) {
  return this.rendered && this.belongsTo($headerItem);
};

scout.TableHeaderMenu.prototype._countColumns = function(propertyName) {
  return this.table.visibleColumns().reduce(function(sum, column) {
    return sum + (column[propertyName] ? 1 : 0);
  }, 0);
};

scout.TableHeaderMenu.prototype._sortColumnCount = function() {
  return this._countColumns('sortActive');
};

scout.TableHeaderMenu.prototype._groupColumnCount = function() {
  return this._countColumns('grouped');
};

scout.TableHeaderMenu.prototype._computeWhitherWidth = function() {
  var $tableHeaderContainer = this.tableHeader.$container,
    headerItemWidth = this.$headerItem.outerWidth() - this.$headerItem.cssBorderWidthX(),
    containerWidth = this.$container.outerWidth() - this.$container.cssBorderWidthX(),
    tableHeaderWidth = $tableHeaderContainer.outerWidth() - this.tableHeader.menuBar.$container.outerWidth();

  // if container is wider than header item -> use header item width, otherwise use container width
  var whitherWidth = Math.min(headerItemWidth, containerWidth);
  // if container is positioned at the right side, header item may not be fully visible (under the menubar or partly invisible due to scrolling)
  whitherWidth = Math.min(whitherWidth, tableHeaderWidth - this.$headerItem.position().left);
  var clipLeft = $tableHeaderContainer.offset().left - this.$headerItem.offset().left - this.tableHeader.table.$container.cssBorderLeftWidth();
  if (clipLeft > 0) {
    whitherWidth -= clipLeft;
  }
  return whitherWidth;
};

scout.TableHeaderMenu.prototype._renderCompact = function() {
  this.$body.toggleClass('compact', this.compact);
  this.invalidateLayoutTree();
};

scout.TableHeaderMenu.prototype.setCompact = function(compact) {
  this.setProperty('compact', compact);
};

scout.TableHeaderMenu.prototype._onLocationChange = function(event) {
  var inView, containerBounds,
    isLocationInView = scout.scrollbars.isLocationInView,
    headerItemBounds = scout.graphics.offsetBounds(this.$headerItem),
    $tableHeaderContainer = this.tableHeader.$container;

  this.$container.setVisible(true);
  containerBounds = scout.graphics.offsetBounds(this.$container);

  // menu must only be visible if the header item is in view (menu gets repositioned when the table gets scrolled -> make sure it won't be displayed outside of the table)
  // check left side of the header item (necessary if header item is moved outside on the left side of the table)
  inView = isLocationInView(new scout.Point(headerItemBounds.x, headerItemBounds.y), $tableHeaderContainer);
  if (!inView) {
    // if left side of the header is not in view, check if right side of the header and the menu, both must be visible)
    // check right side of the header item (necessary if header item is moved outside on the right side of the table)
    inView = isLocationInView(new scout.Point(headerItemBounds.x + headerItemBounds.width, headerItemBounds.y + headerItemBounds.height), $tableHeaderContainer);
    // check right side of the menu (necessary if header item is larger than menu, and if header item is moved outside on the left side of the table)
    inView = inView && isLocationInView(new scout.Point(containerBounds.x + containerBounds.width, containerBounds.y), $tableHeaderContainer);
  }
  this.$container.setVisible(inView);

  // make sure whither is correctly positioned and sized
  // (bounds must be computed after setVisible, if it was hidden before bounds are not correct)
  containerBounds = scout.graphics.offsetBounds(this.$container);
  this.$whiter
    // if header is clipped on the left side, position whither at the left of the visible part of the header (same applies for width, see _computeWhitherWidth)
    .cssLeft(Math.max(headerItemBounds.x - containerBounds.x, $tableHeaderContainer.offset().left - containerBounds.x - this.tableHeader.table.$container.cssBorderLeftWidth()))
    .width(this._computeWhitherWidth());
};

scout.TableHeaderMenu.prototype._onAnchorScroll = function(event) {
  this.position();
};

scout.TableHeaderMenu.prototype._onFilterTableRowsChecked = function(event) {
  this.filter.selectedValues = [];
  this.filterTable.rows.forEach(function(row) {
    if (row.checked) {
      this.filter.selectedValues.push(row.dataMap.filterValue.key);
    }
  }, this);
  this._updateFilterTable();
};

scout.TableHeaderMenu.prototype._onFilterTableChanged = function() {
  this.$filterTableGroupTitle.text(this._filterByText());
  this._updateFilterTableCheckedMode();
  this._updateFilterTableActions();
};

// When no filter value is selected, we change the selection mode to ALL
// since it makes no sense to choose NONE when no value is currently selected
scout.TableHeaderMenu.prototype._updateFilterTableCheckedMode = function() {
  if (this.filter.selectedValues.length === 0) {
    this.filterCheckedMode = scout.TableHeaderMenu.CheckedMode.ALL;
  } else {
    this.filterCheckedMode = scout.TableHeaderMenu.CheckedMode.NONE;
  }
};

scout.TableHeaderMenu.prototype._onMouseDownOutside = function(event) {
  // close popup only if source of event is not $headerItem or one of it's children.
  if (this.$headerItem.isOrHas(event.target)) {
    return;
  }
  this.close();
};

/**
 * Called by table header
 */
scout.TableHeaderMenu.prototype.onColumnResized = function() {
  // Adjust whiter with if size gets changed while menu is open (may caused by TableHeader._adjustColumnMinWidth)
  this.$whiter.width(this._computeWhitherWidth());
};
