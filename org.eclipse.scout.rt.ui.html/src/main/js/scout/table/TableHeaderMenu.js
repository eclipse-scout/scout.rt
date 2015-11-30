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
// FIXME CRU: implement buttons to show/hide, add/remove columns depending on 'custom' property.
scout.TableHeaderMenu = function() {
  scout.TableHeaderMenu.parent.call(this);
  this.tableHeader;
  this.table;
  this.$headerItem;
  this.openAnimated = true;
  this.$columnActions;
  this.$columnFilters;

  this._tableHeaderScrollHandler = this._onAnchorScroll.bind(this);
  this.on('locationChanged', this._onLocationChanged.bind(this));
};
scout.inherits(scout.TableHeaderMenu, scout.Popup);

scout.TableHeaderMenu.prototype._init = function(options) {
  scout.TableHeaderMenu.parent.prototype._init.call(this, options);

  this.tableHeader = options.tableHeader;
  this.table = this.tableHeader.table;
  this.$headerItem = this.$anchor;
};

scout.TableHeaderMenu.prototype._createLayout = function() {
  return new scout.TableHeaderMenuLayout(this);
};

scout.TableHeaderMenu.prototype._render = function($parent) {
  var $headerItem = this.$anchor,
    column = $headerItem.data('column');

  this.$parent = $parent;
  this.column = column;
  $headerItem.select(true);

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
  if (this.table.columns.length > 1) {
    this._renderMovingGroup();
  }

  // Sorting
  if (this.table.sortEnabled) {
    this._renderSortingGroup();
    this._renderSelectedSorting();
  }

  // Grouping and aggregation
  var containsNumberColumn = scout.arrays.find(this.table.columns, function(column) {
    return column.type === 'number';
  });
  if (containsNumberColumn) {
    if (column.type !== 'number') {
      this._renderGroupingGroup();
      this._renderSelectedGrouping();
    } else {
      this._renderAggregationGroup();
      this._renderSelectedAggregation();
    }
  }

  // Coloring
  if (column.type === 'number') {
    this._renderColoringGroup();
    this._renderSelectedColoring();
  }

  // Filtering
  // FIXME AWE: (filter) rename existing filters to histogram (as in old scout versions)
  this.filter = this.table.getFilter(this.column.id);
  if (!this.filter) {
    this.filter = scout.create('ColumnUserFilter', {
      session: this.session,
      table: this.table,
      column: this.column
    });
  }
  // always recalculate available values to make sure new/updated/deleted rows are considered
  this.filter.calculate();
  this._renderFilterGroup();

  // FIXME AWE: (filter) free-text or from/to fields
  this._renderFilterField();

  // name all label elements
  $('.table-header-menu-group-text', this.$container).each(function(i, elem) {
    var $elem = $(elem);
    $elem.text($elem.data('label'));
  });

  this.$container
    .on('mouseenter click', '.table-header-menu-command', this._onCommandMouseenterClick.bind(this))
    .on('mouseleave', '.table-header-menu-command', this._onCommandMouseleave.bind(this));

  this.tableHeader.$container.on('scroll', this._tableHeaderScrollHandler);
};

scout.TableHeaderMenu.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$filteringContainer, this.session);
  this.tableHeader.$container.off('scroll', this._tableHeaderScrollHandler);
  this.$headerItem.select(false);
  scout.TableHeaderMenu.parent.prototype._remove.call(this);
};

scout.TableHeaderMenu.prototype._renderMovingGroup = function() {
  var table = this.table,
    column = this.column,
    pos = table.columns.indexOf(column);

  this.$moving = this.$columnActions.appendDiv('table-header-menu-group');
  this.$moving.appendDiv('table-header-menu-group-text')
    .data('label', this.session.text('ui.Move'));

  this.$moving.appendDiv('table-header-menu-command move move-top')
    .data('label', this.session.text('ui.toBegin'))
    .click(moveToTop);
  this.$moving.appendDiv('table-header-menu-command move move-up')
    .data('label', this.session.text('ui.forward'))
    .click(moveUp);
  this.$moving.appendDiv('table-header-menu-command move move-down')
    .data('label', this.session.text('ui.backward'))
    .click(moveDown);
  this.$moving.appendDiv('table-header-menu-command move move-bottom')
    .data('label', this.session.text('ui.toEnd'))
    .click(moveToBottom);

  function moveToTop() {
    table.moveColumn(column, pos, 0);
    pos = table.columns.indexOf(column);
  }

  function moveUp() {
    table.moveColumn(column, pos, Math.max(pos - 1, 0));
    pos = table.columns.indexOf(column);
  }

  function moveDown() {
    table.moveColumn(column, pos, Math.min(pos + 1, table.header.findHeaderItems().length - 1));
    pos = table.columns.indexOf(column);
  }

  function moveToBottom() {
    table.moveColumn(column, pos, table.header.findHeaderItems().length - 1);
    pos = table.columns.indexOf(column);
  }
};

scout.TableHeaderMenu.prototype._renderSortingGroup = function() {
  var table =
    this.table,
    column = this.column,
    that = this;

  this.$sorting = this.$columnActions.appendDiv('table-header-menu-group');
  this.$sorting.appendDiv('table-header-menu-group-text')
    .data('label', this.session.text('ColumnSorting'));

  if (!table.hasPermanentHeadOrTailSortColumns()) {
    this.$sortAsc = this.$sorting.appendDiv('table-header-menu-command toggle sort sort-asc')
      .data('label', this.session.text('ui.ascending'))
      .data('direction', 'asc')
      .click(onSortClick.bind(this));
    this.$sortDesc = this.$sorting.appendDiv('table-header-menu-command toggle sort sort-desc')
      .data('label', this.session.text('ui.descending'))
      .data('direction', 'desc')
      .click(onSortClick.bind(this));
  }

  this.$sortAscAdd = this.$sorting.appendDiv('table-header-menu-command toggle sort sort-asc-add')
    .data('label', this.session.text('ui.ascendingAdditionally'))
    .data('direction', 'asc')
    .click(onSortAdditionalClick.bind(this));
  this.$sortDescAdd = this.$sorting.appendDiv('table-header-menu-command toggle sort sort-desc-add')
    .data('label', this.session.text('ui.descendingAdditionally'))
    .data('direction', 'desc')
    .click(onSortAdditionalClick.bind(this));

  function onSortClick(event) {
    var $target = $(event.currentTarget),
      direction = $target.data('direction');

    this.remove();
    sort(direction, false, $target.isSelected());
  }

  function onSortAdditionalClick(event) {
    var $target = $(event.currentTarget),
      direction = $target.data('direction');

    this.remove();
    sort(direction, true, $target.isSelected());
  }

  function sort(direction, multiSort, remove) {
    table.sort(column, direction, multiSort, remove);

    that._renderSelectedSorting();
  }
};

scout.TableHeaderMenu.prototype._renderSelectedSorting = function() {
  if (!this.table.sortEnabled) {
    return;
  }

  var showAddCommands = false,
    sortCount = this._sortColumnCount(),
    addIcon;

  $('.table-header-menu-command', this.$sorting).select(false);

  if (sortCount === 1 && !this.table.hasPermanentHeadOrTailSortColumns()) {
    if (this.column.sortActive) {
      if (this.column.sortAscending) {
        this.$sortAsc.select(true);
      } else {
        this.$sortDesc.select(true);
      }
    } else {
      showAddCommands = true;
    }
  } else if (sortCount > 1 || this.table.hasPermanentHeadOrTailSortColumns()) {
    showAddCommands = true;
    if (this.column.sortActive) {
      if (this.column.sortAscending) {
        this.$sortAscAdd.select(true);
      } else {
        this.$sortDescAdd.select(true);
      }
      addIcon = this.column.sortIndex + 1;
      this.$sortAscAdd.attr('data-icon', addIcon);
      this.$sortDescAdd.attr('data-icon', addIcon);
    }
  }

  this.$sortAscAdd.setVisible(showAddCommands);
  this.$sortDescAdd.setVisible(showAddCommands);
};

scout.TableHeaderMenu.prototype._renderGroupingGroup = function() {
  var that = this,
    table = this.table,
    column = this.column,
    groupCount = this._groupColumnCount();

  this.$grouping = this.$columnActions.appendDiv('table-header-menu-group');
  this.$grouping.appendDiv('table-header-menu-group-text')
    .data('label', this.session.text('ui.Grouping'));

  this.$groupColumn = this.$grouping.appendDiv('table-header-menu-command toggle group')
    .data('label', this.session.text('ui.groupingApply'))
    .click(onGroupClick.bind(this));

  this.$groupColumnAdditional = this.$grouping.appendDiv('table-header-menu-command toggle group-add')
    .data('label', this.session.text('ui.additionally'))
    .click(onAdditionalGroupClick.bind(this));

  if (groupCount === 0) {
    this.$groupColumnAdditional.hide();
  } else if (groupCount === 1 && this.column.grouped) {
    this.$groupColumnAdditional.hide();
  } else if (groupCount > 1) {
    this.$groupColumnAdditional.show();
  }

  function onGroupClick(event) {
    var direction = 'asc',
      $target = $(event.currentTarget);
    if (column.sortIndex >= 0 && (!column.sortAscending)) {
      direction = 'desc';
    }
    this.remove();
    groupColumn($target, column, direction, false);
  }

  function onAdditionalGroupClick(event) {
    var direction = 'asc',
      $target = $(event.currentTarget);
    if (column.sortIndex >= 0 && (!column.sortAscending)) {
      direction = 'desc';
    }
    this.remove();
    groupColumn($target, column, direction, true);
  }

  function groupColumn($command, column, direction, additional) {
    var remove = $command.isSelected();
    table.groupColumn(column, additional, direction, remove);
  }
};

scout.TableHeaderMenu.prototype._renderSelectedGrouping = function() {
  var groupCount = this._groupColumnCount();

  this.$groupColumn.select(false);
  this.$groupColumnAdditional.select(false);

  if (this.column.grouped) {
    if (groupCount === 1) {
      this.$groupColumn.select(true);
    } else if (groupCount > 1) {
      this.$groupColumnAdditional.select(true)
        .attr('data-icon', this.column.sortIndex + 1);
    }
  }
};

scout.TableHeaderMenu.prototype._renderAggregationGroup = function() {
  var table = this.table,
    column = this.column;
  this.$aggregation = this.$columnActions.appendDiv('table-header-menu-group');
  this.$aggregation.appendDiv('table-header-menu-group-text')
    .data('label', this.session.text('ui.Aggregation'));

  this.$aggregationFunctionSum = this.$aggregation.appendDiv('table-header-menu-command aggregation-function sum')
    .data('label', this.session.text('ui.Sum'))
    .click(changeAggregation.bind(this, 'sum'));

  this.$aggregationFunctionAvg = this.$aggregation.appendDiv('table-header-menu-command aggregation-function avg')
    .data('label', this.session.text('ui.Average'))
    .click(changeAggregation.bind(this, 'avg'));

  this.$aggregationFunctionMin = this.$aggregation.appendDiv('table-header-menu-command aggregation-function min')
    .data('label', this.session.text('ui.Minimum'))
    .click(changeAggregation.bind(this, 'min'));

  this.$aggregationFunctionMax = this.$aggregation.appendDiv('table-header-menu-command aggregation-function max')
    .data('label', this.session.text('ui.Maximum'))
    .click(changeAggregation.bind(this, 'max'));

  function changeAggregation(func) {
    this.remove();
    table.changeAggregation(column, func);
  }
};

scout.TableHeaderMenu.prototype._renderSelectedAggregation = function() {
  var func = this.column.aggregationFunction;
  if (func) {
    if (func === 'sum') {
      this.$aggregationFunctionSum.select(true);
    } else if (func === 'avg') {
      this.$aggregationFunctionAvg.select(true);
    } else if (func === 'min') {
      this.$aggregationFunctionMin.select(true);
    } else if (func === 'max') {
      this.$aggregationFunctionMax.select(true);
    }
  }
};

scout.TableHeaderMenu.prototype._renderColoringGroup = function() {
  var table = this.table,
    column = this.column;
  this.$coloring = this.$columnActions.appendDiv('table-header-menu-group');
  this.$coloring.appendDiv('table-header-menu-group-text')
    .data('label', this.session.text('ui.Coloring'));

  this.$coloring.appendDiv('table-header-menu-command toggle color color-gradient1')
    .data('label', this.session.text('ui.fromRedToGreen'))
    .data('backgroundEffect', 'colorGradient1')
    .click(onBackgroundEffectClick.bind(this));

  this.$coloring.appendDiv('table-header-menu-command toggle color color-gradient2')
    .data('label', this.session.text('ui.fromGreenToRed'))
    .data('backgroundEffect', 'colorGradient2')
    .click(onBackgroundEffectClick.bind(this));

  if (scout.device.supportsCssGradient()) {
    this.$coloring.appendDiv('table-header-menu-command toggle color color-bar-chart')
      .data('label', this.session.text('ui.withBarChart'))
      .data('backgroundEffect', 'barChart')
      .click(onBackgroundEffectClick.bind(this));
  }

  function onBackgroundEffectClick(event) {
    var $target = $(event.currentTarget),
      effect = $target.data('backgroundEffect');

    if ($target.isSelected()) {
      effect = null;
    }
    this.remove();
    table.setColumnBackgroundEffect(column, effect);
  }
};

scout.TableHeaderMenu.prototype._renderSelectedColoring = function() {
  var bgEffect = this.column.backgroundEffect;

  $('.table-header-menu-command', this.$coloring).each(function(index, elem) {
    var $elem = $(elem),
      effect = $elem.data('backgroundEffect');
    $elem.select(effect === this.column.backgroundEffect);
  }.bind(this));
};

scout.TableHeaderMenu.prototype._renderFilterGroup = function() {
  this.$filtering = this.$columnFilters.appendDiv('table-header-menu-group-filter');
  this.$filtering.appendDiv('table-header-menu-group-text')
    .data('label', this.session.text('ui.FilterBy'));

  this.$filteringContainer = this.$filtering.appendDiv('table-header-menu-filter-container');
  this.filter.availableValues.forEach(function(availableValue, index, arr) {
    var $filteringItem = this.$filteringContainer.appendDiv('table-header-menu-filter').text(availableValue.text)
      .data('key', availableValue.key)
      .on('click', this._onFilterClick.bind(this));

    if (this.filter.selectedValues.indexOf(availableValue.key) > -1) {
      $filteringItem.select(true);
    }
    if (index === arr.length - 1) {
      // mark last element
      $filteringItem.addClass('last');
    }
  }, this);

  scout.scrollbars.install(this.$filteringContainer, {
    parent: this
  });
};

scout.TableHeaderMenu.prototype._renderFilterField = function() {
  this.$filteringField = this.$columnFilters.appendDiv('table-header-menu-filter-field');
  this.$filteringField.appendDiv('table-header-menu-group-text')
    .data('label', this.session.text('ui.FilterText'));

  var freeTextField = scout.create('StringField', {
    parent: this,
    session: this.session,
    labelVisible: false,
    statusVisible: false,
    maxLength: 100
  });
  freeTextField.render(this.$filteringField);

  // FIXME AWE: (filter) property mandatoryVisible? or padding/margin hack?
  freeTextField.$mandatory.remove();
  freeTextField.$mandatory = null;
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
  var groupCount = 0,
    i;

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

scout.TableHeaderMenu.prototype._onFilterClick = function(event) {
  var $clicked = $(event.currentTarget);
  $clicked.select(!$clicked.isSelected());

  // find selected values
  this.filter.selectedValues = [];
  this.$filtering.find('.selected').each(function(i, elem) {
    this.filter.selectedValues.push($(elem).data('key'));
  }.bind(this));

  if (this.filter.selectedValues.length > 0) {
    this.table.addFilter(this.filter);
  } else {
    this.table.removeFilterByKey(this.column.id);
  }

  // callback to table
  this.table.filter();
};

scout.TableHeaderMenu.prototype._onMouseDownOutside = function(event) {
  // close popup only if source of event is not $headerItem or one of it's children.
  if (this.$headerItem.isOrHas(event.target)) {
    return;
  }

  this.close();
};

scout.TableHeaderMenu.prototype._onCommandMouseenterClick = function(event) {
  var $command = $(event.currentTarget),
    $text = $command.siblings('.table-header-menu-group-text'),
    text = ($command.isSelected() && $command.hasClass('toggle')) ? this.session.text('ui.remove') : $command.data('label');

  $text.text($text.data('label') + ' ' + text);
};

scout.TableHeaderMenu.prototype._onCommandMouseleave = function(event) {
  var $command = $(event.currentTarget),
    $text = $command.siblings('.table-header-menu-group-text');

  $text.text($text.data('label'));
};
