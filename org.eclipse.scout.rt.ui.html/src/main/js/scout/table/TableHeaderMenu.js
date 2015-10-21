// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

// FIXME CRU: implement buttons to show/hide, add/remove columns depending on 'custom' property.
scout.TableHeaderMenu = function() {
  scout.TableHeaderMenu.parent.call(this);
  this.tableHeader;
  this.table;
  this.$headerItem;
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
  var table = this.table,
    session = this.session,
    $headerItem = this.$anchor,
    column = $headerItem.data('column'),
    pos = table.columns.indexOf(column),
    that = this;

  this.$parent = $parent;
  this.column = column;

  $headerItem.select(true);

  this.tableHeader.$container.on('scroll', this._tableHeaderScrollHandler);

  // create container
  var $menuHeader = $parent.appendDiv('table-header-menu');
  this.$container = $menuHeader;
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
  this.$whiter = $menuHeader.appendDiv('table-header-menu-whiter');

  if (this.withFocusContext && this.focusableContainer) {
    this.$container.attr('tabindex', -1);
  }

  // create buttons in command for order
  if (table.header.columns.length > 1) {
    this.$moving = $menuHeader.appendDiv('header-group');
    this.$moving.appendDiv('header-text')
      .data('label', session.text('ui.Move'));

    this.$moving.appendDiv('header-command move-top')
      .data('label', session.text('ui.toBegin'))
      .click(moveTop);
    this.$moving.appendDiv('header-command move-up')
      .data('label', session.text('ui.forward'))
      .click(moveUp);
    this.$moving.appendDiv('header-command move-down')
      .data('label', session.text('ui.backward'))
      .click(moveDown);
    this.$moving.appendDiv('header-command move-bottom')
      .data('label', session.text('ui.toEnd'))
      .click(moveBottom);
  }

  // create buttons in command for sorting
  if (table.sortEnabled) {
    this.$sorting = $menuHeader.appendDiv('header-group');
    this.$sorting.appendDiv('header-text')
      .data('label', session.text('ColumnSorting'));

    if (!table.hasPermanentHeadOrTailSortColumns()) {
      var $sortAsc = this.$sorting.appendDiv('header-command toggle sort-asc')
        .data('label', session.text('ui.ascending'))
        .click(this.remove.bind(this))
        .click(function() {
          sort('asc', false, $(this).hasClass('selected'));
        });
      var $sortDesc = this.$sorting.appendDiv('header-command toggle sort-desc')
        .data('label', session.text('ui.descending'))
        .click(this.remove.bind(this))
        .click(function() {
          sort('desc', false, $(this).hasClass('selected'));
        });
    }

    var $sortAscAdd = this.$sorting.appendDiv('header-command toggle sort-asc-add')
      .data('label', session.text('ui.ascendingAdditionally'))
      .click(this.remove.bind(this))
      .click(function() {
        sort('asc', true, $(this).hasClass('selected'));
      });
    var $sortDescAdd = this.$sorting.appendDiv('header-command toggle sort-desc-add')
      .data('label', session.text('ui.descendingAdditionally'))
      .click(this.remove.bind(this))
      .click(function() {
        sort('desc', true, $(this).hasClass('selected'));
      });

    sortSelect();
  }

  // create buttons in command for grouping, if there is at least one number column
  var containsNumberColumn = scout.arrays.find(table.columns, function(column) {
    return column.type === 'number';
  });

  if (containsNumberColumn) {
    if (column.type !== 'number') {

      this.$grouping = $menuHeader.appendDiv('header-group');
      this.$grouping.appendDiv('header-text')
      .data('label', session.text('ui.Grouping'));

      var $groupColumn = this.$grouping.appendDiv('header-command toggle group-column')
        .data('label', session.text('ui.groupingApply'))
        .click(this.remove.bind(this))
        .click(groupSort);

      var $groupColumnAdditional = this.$grouping.appendDiv('header-command toggle group-column-additional')
        .data('label', session.text('ui.additionally'))
        .click(this.remove.bind(this))
        .click(groupSortAdditional);
    } else {

      this.$aggregation = $menuHeader.appendDiv('header-group');
      this.$aggregation.appendDiv('header-text')
      .data('label', session.text('ui.Aggregation'));

      //functions for number columns:
      var sumIcon = '\u03a3',
        avgIcon = '\u00D8',
        maxIcon = '\uf077',
        minIcon = '\uf078';

      var $aggregationFunctionSum = this.$aggregation.appendDiv('header-command group-function')
        .data('label', session.text('ui.Sum'))
        .attr('data-icon', sumIcon)
        .click(this.remove.bind(this))
        .click(function() {
          setAggregationFunction('sum');
        });

      var $aggregationFunctionAvg = this.$aggregation.appendDiv('header-command group-function')
        .data('label', session.text('ui.Average'))
        .attr('data-icon', avgIcon)
        .click(this.remove.bind(this))
        .click(function() {
          setAggregationFunction('avg');
        });

      var $aggregationFunctionMin = this.$aggregation.appendDiv('header-command group-function')
        .data('label', session.text('ui.Minimum'))
        .attr('data-icon', minIcon)
        .click(this.remove.bind(this))
        .click(function() {
          setAggregationFunction('min');
        });

      var $aggregationFunctionMax = this.$aggregation.appendDiv('header-command group-function')
        .data('label', session.text('ui.Maximum'))
        .attr('data-icon', maxIcon)
        .click(this.remove.bind(this))
        .click(function() {
          setAggregationFunction('max');
        });
    }

    groupSelect();
  }

  // create buttons in command for coloring
  if (column.type === 'number') {
    this.$coloring = $menuHeader.appendDiv('header-group');
    this.$coloring.appendDiv('header-text')
      .data('label', session.text('ui.ColorCells'));

    this.$coloring.appendDiv('header-command color-red')
      .data('label', session.text('ui.fromRedToGreen'))
      .click(this.remove.bind(this))
      .click(colorRed);
    this.$coloring.appendDiv('header-command color-green')
      .data('label', session.text('ui.fromGreenToRed'))
      .click(this.remove.bind(this))
      .click(colorGreen);
    this.$coloring.appendDiv('header-command color-bar')
      .data('label', session.text('ui.withBarGraph'))
      .click(this.remove.bind(this))
      .click(colorBar);
    this.$coloring.appendDiv('header-command color-remove')
      .data('label', session.text('ui.remove'))
      .click(this.remove.bind(this))
      .click(colorRemove);
  }

  // filter
  this.filter = this.table.getFilter(this.column.id);
  if (!this.filter) {
    this.filter = scout.create('ColumnUserFilter', {
      session: this.session,
      table: this.table,
      column: this.column
    });
  }
  // always recalculate available values to make sure new/updated/deleted rows are considered
  this.filter.calculateCube();
  this._renderFilterGroup();

  // name all label elements
  $('.header-text').each(function(i, elem) {
    var $elem = $(elem);
    $elem.text($elem.data('label'));
  });

  // set events to buttons
  $menuHeader
    .on('mouseenter click', '.header-command', enterCommand)
    .on('mouseleave', '.header-command', leaveCommand);

  // copy flags to menu
  if ($headerItem.hasClass('sort-asc')) {
    $menuHeader.addClass('sort-asc');
  }
  if ($headerItem.hasClass('sort-desc')) {
    $menuHeader.addClass('sort-desc');
  }
  if ($headerItem.hasClass('filter')) {
    $menuHeader.addClass('filter');
  }

  // event handling
  function enterCommand() {
    var $command = $(this),
      $text = $command.siblings('.header-text'),
      text = ($command.hasClass('selected') && $command.hasClass('toggle')) ? session.text('ui.remove') : $command.data('label');

    $text.text($text.data('label') + ' ' + text);
  }

  function leaveCommand() {
    var $command = $(this),
      $text = $command.siblings('.header-text');

    $text.text($text.data('label'));
  }

  function moveTop() {
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

  function moveBottom() {
    table.moveColumn(column, pos, table.header.findHeaderItems().length - 1);
    pos = table.columns.indexOf(column);
  }

  function sort(direction, multiSort, remove) {
    table.sort(column, direction, multiSort, remove);

    sortSelect();
  }

  function sortSelect() {
    if (!table.sortEnabled) {
      return;
    }

    var addIcon = '\uF067',
      sortCount = getSortColumnCount();

    $('.header-command', that.$sorting).removeClass('selected');

    //TODO: fko cgu: use column properties instead of css class?
    if (sortCount === 1 && !table.hasPermanentHeadOrTailSortColumns()) {
      if ($headerItem.hasClass('sort-asc')) {
        $sortAsc.addClass('selected');
        addIcon = null;
      } else if ($headerItem.hasClass('sort-desc')) {
        $sortDesc.addClass('selected');
        addIcon = null;
      }
    } else if (sortCount > 1 || table.hasPermanentHeadOrTailSortColumns()) {
      if ($headerItem.hasClass('sort-asc')) {
        $sortAscAdd.addClass('selected');
        addIcon = column.sortIndex + 1;
      } else if ($headerItem.hasClass('sort-desc')) {
        $sortDescAdd.addClass('selected');
        addIcon = column.sortIndex + 1;
      }
    } else {
      addIcon = null;
    }

    if (addIcon) {
      $sortAscAdd.show().attr('data-icon', addIcon);
      $sortDescAdd.show().attr('data-icon', addIcon);
    } else {
      if (!table.hasPermanentHeadOrTailSortColumns()) {
        $sortAscAdd.hide();
        $sortDescAdd.hide();
      }
    }
  }

  function getSortColumnCount() {
    var sortCount = 0;

    for (var i = 0; i < table.columns.length; i++) {
      if (table.columns[i].sortActive) {
        sortCount++;
      }
    }

    return sortCount;
  }

  function getGroupColumnCount() {
    var groupCount = 0;

    for (var i = 0; i < table.columns.length; i++) {
      if (table.columns[i].grouped) {
        groupCount++;
      }
    }

    return groupCount;
  }

  function groupSort() {
    groupColumn($(this), column, 'asc', false);
  }

  function groupSortAdditional() {
    groupColumn($(this), column, 'asc', true);
  }

  function setAggregationFunction(func) {
    table.changeAggregation(column, func);
  }

  function groupColumn($command, column, direction, additional) {
    var remove = $command.isSelected();
    table.groupColumn(column, additional, direction, remove);
  }

  function doGroup($command) {
    if ($command.isSelected()) {
      table.removeGrouping();
    } else {
      table.group();
    }

    sortSelect();
    groupSelect();
  }

  function groupSelect() {

    var iconPlus = '\uF067',
      groupCount = getGroupColumnCount(),
      func = column.aggregationFunction;

    if ($groupColumnAdditional) {
      $groupColumnAdditional.removeClass('selected');
    }

    if ($groupColumnAdditional) {
      if (groupCount === 0 || column.grouped) {
        $groupColumnAdditional.hide();
      } else {
        $groupColumnAdditional.show().attr('data-icon', iconPlus);
      }
    }

    if ($groupColumn && column.grouped) {
      $groupColumn.addClass('selected');
      $groupColumn.show();
    }

    if(func){
      if(func === 'sum'){
        $aggregationFunctionSum.addClass('selected');
      }
      else if(func === 'avg'){
        $aggregationFunctionAvg.addClass('selected');
      }
      else if(func === 'min'){
        $aggregationFunctionMin.addClass('selected');
      }
      else if(func === 'max'){
        $aggregationFunctionMax.addClass('selected');
      }
    }

  }

  function colorRed() {
    table.colorData(column, 'red');
  }

  function colorGreen() {
    table.colorData(column, 'green');
  }

  function colorBar() {
    table.colorData(column, 'bar');
  }

  function colorRemove() {
    table.colorData(column, 'remove');
  }

};

scout.TableHeaderMenu.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$filteringContainer, this.session);
  this.tableHeader.$container.off('scroll', this._tableHeaderScrollHandler);
  this.$headerItem.select(false);
  scout.TableHeaderMenu.parent.prototype._remove.call(this);
};

scout.TableHeaderMenu.prototype._renderFilterGroup = function() {
  this.$filtering = this.$container.appendDiv('header-group-filter');
  this.$filtering.appendDiv('header-text')
    .data('label', this.session.text('ui.FilterBy'));

  this.$filteringContainer = this.$filtering.appendDiv('header-filter-container');
  this.filter.availableValues.forEach(function(availableValue, index, arr) {
    var $filteringItem = this.$filteringContainer.appendDiv('header-filter').text(availableValue.text)
      .data('key', availableValue.key)
      .on('click', this._onFilterClick.bind(this));

    if (this.filter.selectedValues.indexOf(availableValue.key) > -1) {
      $filteringItem.addClass('selected');
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

scout.TableHeaderMenu.prototype.isOpenFor = function($headerItem) {
  return this.rendered && this.belongsTo($headerItem);
};

scout.TableHeaderMenu.prototype._onMouseDownOutside = function(event) {
  // close popup only if source of event is not $headerItem or one of it's children.
  if (this.$headerItem.isOrHas(event.target)) {
    return;
  }

  this.close();
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
  containerBounds = scout.graphics.offsetBounds(this.$container),

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
