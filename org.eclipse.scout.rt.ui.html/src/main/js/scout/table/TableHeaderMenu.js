/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
  this.tableHeader;
  this.table;
  this.$headerItem;
  this.$columnActions;
  this.$columnFilters;
  this.filterCheckedMode = scout.TableHeaderMenu.CheckedMode.ALL;

  this._tableHeaderScrollHandler = this._onAnchorScroll.bind(this);
  this.on('locationChanged', this._onLocationChanged.bind(this));
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

scout.TableHeaderMenu.prototype._init = function(options) {
  scout.TableHeaderMenu.parent.prototype._init.call(this, options);

  this.tableHeader = options.tableHeader;
  this.column = options.column;
  this.table = this.tableHeader.table;
  this.$headerItem = this.$anchor;

  this._tableFilterHandler = this._onFilterTableChanged.bind(this);
  this.table.on('addFilter', this._tableFilterHandler);
  this.table.on('removeFilter', this._tableFilterHandler);
  this._filterTableCheckedRowsHandler = this._onFilterTableCheckedRows.bind(this);

  // Filtering
  this.filter = this.table.getFilter(this.column.id);
  if (!this.filter) {
    this.filter = this.column.createFilter();
  }
  // always recalculate available values to make sure new/updated/deleted rows are considered
  this.filter.calculate();
  this.filter.on('filterFieldsChanged', this._updateFilterTable.bind(this)); // FIXME AWE: (filter) off handler?
  this._updateFilterTableCheckedMode();
};

scout.TableHeaderMenu.prototype._createLayout = function() {
  return new scout.TableHeaderMenuLayout(this);
};

scout.TableHeaderMenu.prototype._render = function($parent) {
  this.$parent = $parent;
  this.$headerItem.select(true);

  this.$container = $parent.appendDiv('table-header-menu');
  this.$columnActions = this.$container.appendDiv('table-header-menu-actions');
  this.$columnFilters = this.$container.appendDiv('table-header-menu-filters');

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
  this.$whiter = this.$container.appendDiv('table-header-menu-whiter');

  if (this.withFocusContext && this.focusableContainer) {
    this.$container.attr('tabindex', -1);
  }

  // Moving
  var movableColumns = this.table.columns.filter(function(column) {
    return !column.fixedPosition;
  });
  if (movableColumns.length > 1 && !this.column.fixedPosition) {
    this._renderMovingGroup();
  }
  // Sorting
  if (this.table.sortEnabled) {
    this._renderSortingGroup();
  }
  // Add/remove/change columns
  this._renderColumnActionsGroup();
  // Grouping and aggregation
  if (this.table.containsNumberColumn()) {
    if (this.column instanceof scout.NumberColumn) {
      this._renderAggregationGroup();
    } else {
      this._renderGroupingGroup();
    }
  }
  // Coloring
  if (this.column instanceof scout.NumberColumn) {
    this._renderColoringGroup();
  }

  this._renderFilterTable();
  this._renderFilterFields();

  this.tableHeader.$container.on('scroll', this._tableHeaderScrollHandler);
};

scout.TableHeaderMenu.prototype._remove = function() {
  this.filterTable.off('rowsChecked', this._filterTableCheckedRowsHandler);
  this.tableHeader.$container.off('scroll', this._tableHeaderScrollHandler);
  this.$headerItem.select(false);
  this.table.off('addFilter', this._tableFilterHandler);
  this.table.off('removeFilter', this._tableFilterHandler);
  scout.TableHeaderMenu.parent.prototype._remove.call(this);
};

scout.TableHeaderMenu.prototype._renderMovingGroup = function() {
  var table = this.table,
    column = this.column,
    pos = table.columns.indexOf(column);

  var group = scout.create('TableHeaderMenuGroup', {
    parent: this,
    textKey: 'ui.Move',
    cssClass: 'first'});
  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.toBegin',
    cssClass: 'move move-top',
    clickHandler: function() {
      table.moveColumn(column, pos, 0);
      pos = table.columns.indexOf(column);
    }});
  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.forward',
    cssClass: 'move move-up',
    clickHandler: function() {
      table.moveColumn(column, pos, Math.max(pos - 1, 0));
      pos = table.columns.indexOf(column);
    }});
  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.backward',
    cssClass: 'move move-down',
    clickHandler: function() {
      table.moveColumn(column, pos, Math.min(pos + 1, table.header.findHeaderItems().length - 1));
      pos = table.columns.indexOf(column);
    }});
  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.toEnd',
    cssClass: 'move move-bottom',
    clickHandler: function() {
      table.moveColumn(column, pos, table.header.findHeaderItems().length - 1);
      pos = table.columns.indexOf(column);
    }});

  group.render(this.$columnActions);
};

scout.TableHeaderMenu.prototype._renderColumnActionsGroup = function() {
  // When no button of this group is visible, don't render group at all
  if (!this.table.addColumnEnabled &&
      !this.column.removable &&
      !this.column.modifiable) {
    return;
  }

  var column = this.column,
    menuPopup = this;

  this.columnActionsGroup = scout.create('TableHeaderMenuGroup', {
    parent: this,
    textKey: 'ui.Column'});

  this.addColumnButton = scout.create('TableHeaderMenuButton', {
    parent: this.columnActionsGroup,
    textKey: 'ui.addColumn',
    cssClass: 'add-column',
    visible: this.table.columnAddable,
    clickHandler: onClick.bind(this, 'add')});
  this.removeColumnButton = scout.create('TableHeaderMenuButton', {
    parent: this.columnActionsGroup,
    textKey: 'ui.removeColumn',
    cssClass: 'remove-column',
    visible: this.column.removable,
    clickHandler: onClick.bind(this, 'remove')});
  this.modifyColumnButton = scout.create('TableHeaderMenuButton', {
    parent: this.columnActionsGroup,
    textKey: 'ui.changeColumn',
    cssClass: 'change-column',
    visible: this.column.modifiable,
    clickHandler: onClick.bind(this, 'modify')});

  this.columnActionsGroup.render(this.$columnActions);

  function onClick(action) {
    menuPopup.remove();
    this.table._send('columnOrganizeAction', {
      action: action,
      columnId: column.id
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
    textKey: 'ColumnSorting'});

  if (!table.hasPermanentHeadOrTailSortColumns()) {
    this.sortAscButton = scout.create('TableHeaderMenuButton', {
      parent: this.sortingGroup,
      textKey: 'ui.ascending',
      cssClass: 'sort sort-asc',
      direction: 'asc',
      clickHandler: onSortClick});
    this.sortDescButton = scout.create('TableHeaderMenuButton', {
      parent: this.sortingGroup,
      textKey: 'ui.descending',
      cssClass: 'sort sort-desc',
      direction: 'desc',
      clickHandler: onSortClick});
  }

  this.sortAscAddButton = scout.create('TableHeaderMenuButton', {
    parent: this.sortingGroup,
    textKey: 'ui.ascendingAdditionally',
    cssClass: 'sort sort-asc-add',
    direction: 'asc',
    clickHandler: onSortAdditionalClick});
  this.sortDescAddButton = scout.create('TableHeaderMenuButton', {
    parent: this.sortingGroup,
    textKey: 'ui.descendingAdditionally',
    cssClass: 'sort sort-desc-add',
    direction: 'desc',
    clickHandler: onSortAdditionalClick});

  this._updateSortingSelectedState();
  this.sortingGroup.render(this.$columnActions);

  function onSortClick() {
    menuPopup.remove();
    sort(this.direction, false, this.selected);
  }

  function onSortAdditionalClick() {
    menuPopup.remove();
    sort(this.direction, true, this.selected);
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
      this.sortAscAddButton.setIcon(addIcon);
      this.sortDescAddButton.setIcon(addIcon);
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
    textKey: 'ui.Grouping'});

  var groupButton = scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.groupingApply',
    cssClass: 'toggle group',
    additional: false,
    clickHandler: groupColumn});
  var groupAddButton = scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.additionally',
    cssClass: 'toggle group-add',
    additional: true,
    clickHandler: groupColumn});

  if (groupCount === 0) {
    groupAddButton.setVisible(false);
  } else if (groupCount === 1 && this.column.grouped) {
    groupAddButton.setVisible(false);
  } else if (groupCount > 1) {
    groupAddButton.setVisible(true);
  }

  if (this.column.grouped) {
    if (groupCount === 1) {
      groupAddButton.setSelected(true);
    } else if (groupCount > 1) {
      groupAddButton.setSelected(true);
      groupAddButton.setIcon(this.column.sortIndex + 1);
    }
  }

  group.render(this.$columnActions);

  function groupColumn() {
    var direction = (column.sortIndex >= 0 && !column.sortAscending) ? 'desc' : 'asc';
    menuPopup.remove();
    table.groupColumn(column, this.additional, direction, this.selected);
  }
};

scout.TableHeaderMenu.prototype._renderAggregationGroup = function() {
  var table = this.table,
    column = this.column,
    aggregation = column.aggregationFunction,
    menuPopup = this,
    group = scout.create('TableHeaderMenuGroup', {
      parent: this,
      textKey: 'ui.Aggregation'});

  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.Sum',
    cssClass: 'aggregation-function sum',
    aggregation: 'sum',
    clickHandler: onClick});
  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.Average',
    cssClass: 'aggregation-function avg',
    aggregation: 'avg',
    clickHandler: onClick});
  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.Minimum',
    cssClass: 'aggregation-function min',
    aggregation: 'min',
    clickHandler: onClick});
  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.Maximum',
    cssClass: 'aggregation-function max',
    aggregation: 'max',
    clickHandler: onClick});

  group.children.forEach(function(button) {
    button.setSelected(button.aggregation === aggregation);
  });
  group.render(this.$columnActions);

  function onClick() {
    menuPopup.remove();
    table.changeAggregation(column, this.aggregation);
  }
};

scout.TableHeaderMenu.prototype._renderColoringGroup = function() {
  var table = this.table,
    column = this.column,
    menuPopup = this,
    backgroundEffect = column.backgroundEffect,
    group = scout.create('TableHeaderMenuGroup', {
      parent: this,
      textKey: 'ui.Coloring'});

  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.fromRedToGreen',
    cssClass: 'color color-gradient1',
    backgroundEffect: 'colorGradient1',
    clickHandler: onClick});
  scout.create('TableHeaderMenuButton', {
    parent: group,
    textKey: 'ui.fromGreenToRed',
    cssClass: 'color color-gradient2',
    backgroundEffect: 'colorGradient2',
    clickHandler: onClick});
  if (scout.device.supportsCssGradient()) {
    scout.create('TableHeaderMenuButton', {
      parent: group,
      textKey: 'ui.withBarChart',
      cssClass: 'color color-bar-chart',
      backgroundEffect: 'barChart',
      clickHandler: onClick});
  }

  group.children.forEach(function(button) {
    button.setSelected(button.backgroundEffect === backgroundEffect);
  });
  group.render(this.$columnActions);

  function onClick() {
    menuPopup.remove();
    table.setColumnBackgroundEffect(column, this.selected ? null : this.backgroundEffect);
    this.toggle(); // toggle selected state of button
  }
};

scout.TableHeaderMenu.prototype._renderFilterTable = function() {
  var $filterActions;

  this.$filterTableGroup = this.$columnFilters.appendDiv('table-header-menu-group first');

  $filterActions = this.$filterTableGroup
    .appendDiv('table-header-menu-filter-actions');

  this.$filterToggleChecked = $filterActions
    .appendDiv('table-header-menu-filter-toggle-checked')
    .text(this.session.text(this.filterCheckedMode.text))
    .on('click', this._onClickFilterCheckedMode.bind(this));

  this.$filterTableGroupTitle = this.$filterTableGroup
    .appendDiv('table-header-menu-group-text')
    .text(this._filterByText());

  this.filterTable = scout.create('Table', {
    parent: this,
    headerVisible: false,
    autoResizeColumns: true,
    checkable: true,
    checkableStyle: scout.Table.CheckableStyle.TABLE_ROW,
    // column-texts are not visible since header is not visible
    columns: [
      scout.create('Column', {
        index: 0,
        text: 'filter-value',
        width: 160,
        session: this.session
      }),
      scout.create('Column', {
        index: 1,
        type: 'number',
        horizontalAlignment: 1,
        text: 'aggregate-count',
        width: 40,
        session: this.session
      })]
  });
  this.filterTable.on('rowsChecked', this._filterTableCheckedRowsHandler);

  var tableRow, tableRows = [];
  this.filter.availableValues.forEach(function(filterValue) {
    tableRow = {
      cells: [filterValue.text, filterValue.count],
      checked: this.filter.selectedValues.indexOf(filterValue.key) > -1,
      dataMap: {
        filterValue: filterValue
      }
    };
    tableRows.push(tableRow);
  }, this);
  this.filterTable.insertRows(tableRows);
  this.filterTable.render(this.$filterTableGroup);
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

scout.TableHeaderMenu.prototype._onClickFilterCheckedMode = function() {
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
  this._updateFilterTable();
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
  this.$filterToggleChecked.text(this.session.text(this.filterCheckedMode.text));
};

scout.TableHeaderMenu.prototype._renderFilterFields = function() {
  this.filterFieldsGroupBox = scout.create('GroupBox.FilterFields', {
    parent: this,
    column: this.column,
    filter: this.filter
  });
  this.$filterFieldsGroup = this.$columnFilters.appendDiv('table-header-menu-group');
  this.$filterFieldsGroup
    .appendDiv('table-header-menu-group-text')
    .text(this.filter.filterFieldsTitle());
  this.filterFieldsGroupBox.render(this.$filterFieldsGroup);
};

scout.TableHeaderMenu.prototype.isOpenFor = function($headerItem) {
  return this.rendered && this.belongsTo($headerItem);
};

scout.TableHeaderMenu.prototype._sortColumnCount = function() {
  var i, sortCount = 0;
  for (i = 0; i < this.table.columns.length; i++) {
    if (this.table.columns[i].sortActive) {
      sortCount++;
    }
  }
  return sortCount;
};

scout.TableHeaderMenu.prototype._groupColumnCount = function() {
  var i, groupCount = 0;
  for (i = 0; i < this.table.columns.length; i++) {
    if (this.table.columns[i].grouped) {
      groupCount++;
    }
  }
  return groupCount;
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

scout.TableHeaderMenu.prototype._onLocationChanged = function(event) {
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

scout.TableHeaderMenu.prototype._onFilterTableCheckedRows = function(event) {
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
