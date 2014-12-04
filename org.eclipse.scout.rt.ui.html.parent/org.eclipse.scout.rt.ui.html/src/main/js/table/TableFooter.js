scout.TableFooter = function(table) {
  this._table = table;
  this._render(table.$container);
};

scout.TableFooter.FILTER_KEY = 'TEXTFIELD';
scout.TableFooter.CONTAINER_SIZE = 340;

scout.TableFooter.prototype._render = function($parent) {
  var that = this,
    i, control, $group, filter;

  this.$container = $parent.appendDiv('table-footer');
  this._$controlContainer = this.$container.appendDiv('control-container').hide();
  this._addResize(this._$controlContainer);
  this.$controlContent = this._$controlContainer.appendDiv('control-content');

  this._controlGroups = {};

  for (i = 0; i < this._table.controls.length; i++) {
    control = this._table.controls[i];
    $group = this._controlGroups[control.group];
    if (!$group) {
      $group = this._addGroup(control.group);
    }
    control.tableFooter = this;
    control.table = this._table;
    control.render($group);
  }

  this._$filterField = scout.fields.new$TextField()
    .addClass('control-filter')
    .appendTo(this.$container)
    .on('input paste', '', $.debounce(this._onFilterInput.bind(this)))
    .attr('placeholder', this._table.session.text('FilterBy'));
  filter = this._table.getFilter(scout.TableFooter.FILTER_KEY);
  if (filter) {
    this._$filterField.val(filter.text);
  }

  // info section
  this._$controlInfo = this.$container
    .appendDiv('control-info');

  this._$infoLoad = this._$controlInfo
    .appendDiv('table-info-load')
    .on('click', '', this._onClickInfoLoad.bind(this));
  this._$infoFilter = this._$controlInfo
    .appendDiv('table-info-filter')
    .on('click', '', this._onClickInfoFilter.bind(this));
  this._$infoSelection = this._$controlInfo
    .appendDiv('table-info-selection')
    .on('click', '', this._onClickInfoSelection.bind(this));

  this._updateInfoLoad();
  this._updateInfoLoadVisibility();
  this._updateInfoFilter();
  this._updateInfoFilterVisibility();
  this._updateInfoSelection();
  this._updateInfoSelectionVisibility();

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_DRAWN, function(event) {
    that._updateInfoLoad();
    that._updateInfoLoadVisibility();
  });
  this._table.events.on(scout.Table.GUI_EVENT_ROWS_FILTERED, function(event) {
    that._updateInfoFilter();
    that._updateInfoFilterVisibility();
  });
  this._table.events.on(scout.Table.GUI_EVENT_FILTER_RESETTED, function(event) {
    that._setInfoVisible(that._$infoFilter, false);
  });
  this._table.events.on(scout.Table.GUI_EVENT_ROWS_SELECTED, function(event) {
    var numRows = 0;
    if (event.$rows) {
      numRows = event.$rows.length;
    }
    that._updateInfoSelection(numRows, event.allSelected);
    that._updateInfoSelectionVisibility();
  });
};

scout.TableFooter.prototype.remove = function() {
  this.$container.remove();
};

scout.TableFooter.prototype._onFilterInput = function(event) {
  var $input = $(event.currentTarget);
  $input.val($input.val().toLowerCase());

  var filter = this._table.getFilter(scout.TableFooter.FILTER_KEY);
  if (!filter && $input.val()) {
    filter = {
      accept: function($row) {
        var rowText = $row.text().toLowerCase();
        return rowText.indexOf(this.text) > -1;
      }
    };
    this._table.registerFilter(scout.TableFooter.FILTER_KEY, filter);
  } else if (!$input.val()) {
    this._table.unregisterFilter(scout.TableFooter.FILTER_KEY);
  }

  if (filter) {
    filter.text = $input.val();
    filter.label = filter.text;
  }

  this._table.filter();
  event.stopPropagation();
};

scout.TableFooter.prototype.setTableStatusVisible = function(visible) {
  this._updateInfoLoadVisibility();
  this._updateInfoSelectionVisibility();
  this._updateInfoFilterVisibility();
};

scout.TableFooter.prototype._updateInfoLoad = function() {
  var numRows = this._table.rows.length;
  var info = this._table.session.text('NumRowsLoaded', this._computeCountInfo(numRows));
  info += '<br><span class="info-button">' + this._table.session.text('ReloadData') + '</span>';
  if (this._$infoLoad.html() === info) {
    return;
  }
  this._$infoLoad.html(info);
};

scout.TableFooter.prototype._updateInfoFilter = function() {
  var filteredBy = this._table.filteredBy();
  if (filteredBy.length > 0) {
    filteredBy = filteredBy.join(', ');
  }
  var numRowsFiltered = this._table.filteredRowCount;
  var info = this._table.session.text('NumRowsFiltered', this._computeCountInfo(numRowsFiltered));
  if (filteredBy) {
    info = this._table.session.text('NumRowsFilteredBy', this._computeCountInfo(numRowsFiltered), filteredBy);
  }
  info += '<br><span class="info-button">' + this._table.session.text('RemoveFilter') + '</span>';
  if (this._$infoFilter.html() === info) {
    return;
  }
  this._$infoFilter.html(info);
};

scout.TableFooter.prototype._updateInfoSelection = function(numSelectedRows, all) {
  var numRows = this._table.rows.length;
  var selectAllText, info;

  if (numSelectedRows === undefined) {
    numSelectedRows = this._table.selectedRowIds.length;
  }
  if (all === undefined) {
    all = numRows === numSelectedRows;
  }

  if (all) {
    selectAllText = this._table.session.text('SelectNone');
  } else {
    selectAllText = this._table.session.text('SelectAll');
  }

  info = this._table.session.text('NumRowsSelected', this._computeCountInfo(numSelectedRows));
  info += '<br><span class="info-button">' + selectAllText + '</span>';
  if (this._$infoSelection.html() === info) {
    return;
  }

  this._$infoSelection.html(info);
};

scout.TableFooter.prototype._updateInfoLoadVisibility = function() {
  this._setInfoVisible(this._$infoLoad, this._table.tableStatusVisible);
};

scout.TableFooter.prototype._updateInfoFilterVisibility = function() {
  var visible = this._table.tableStatusVisible && this._table.filteredBy().length > 0;
  this._setInfoVisible(this._$infoFilter, visible);
};

scout.TableFooter.prototype._updateInfoSelectionVisibility = function() {
  this._setInfoVisible(this._$infoSelection, this._table.tableStatusVisible);
};

scout.TableFooter.prototype._setInfoVisible = function($info, visible) {
  if (visible) {
    $info.css('display', 'inline-block').widthToContent();
  } else {
    $info.animateAVCSD('width', 0, function() {
      $(this).hide();
    });
  }
};

scout.TableFooter.prototype._onClickInfoLoad = function() {
  this._table.sendReload();
};

scout.TableFooter.prototype._onClickInfoFilter = function() {
  this._table.resetFilter();
  this._$filterField.val('');
};

scout.TableFooter.prototype._onClickInfoSelection = function() {
  this._table.toggleSelection();
};

scout.TableFooter.prototype._computeCountInfo = function(n) {
  if (n === 0) {
    return this._table.session.text('TableRowCount0');
  } else if (n === 1) {
    return this._table.session.text('TableRowCount1');
  } else {
    return this._table.session.text('TableRowCount', n);
  }
};

scout.TableFooter.prototype._addGroup = function(title) {
  var $group = $.makeDiv('control-group').attr('data-title', title).appendTo(this.$container);
  this._controlGroups[title] = $group;
  return $group;
};

/* open, close and resize of the container */

scout.TableFooter.prototype.openControlContainer = function() {
  var insets = scout.graphics.getInsets(this._$controlContainer);
  var contentHeight = scout.TableFooter.CONTAINER_SIZE - insets.top - insets.bottom;

  // adjust table
  this._resizeData(scout.TableFooter.CONTAINER_SIZE);

  // adjust content
  this.$controlContent.outerHeight(contentHeight);

  // open container, stop existing (close) animations before
  this._$controlContainer.stop(true).show().animateAVCSD('height', scout.TableFooter.CONTAINER_SIZE, null, null, 500);

  this.open = true;
};

scout.TableFooter.prototype.closeControlContainer = function(control) {
  // adjust table and container
  this._resizeData(0);

  // adjust container
  this._$controlContainer.animateAVCSD('height', 0, null, null, 500);
  this._$controlContainer.promise().done(function() {
    this._$controlContainer.hide();
    control.onControlContainerClosed();
  }.bind(this));

  this.open = false;
};

scout.TableFooter.prototype._resizeData = function(sizeContainer) {
  var sizeMenubar, sizeFooter, sizeHeader, newOffset,
    that = this;

  // new size of container and table data
  sizeMenubar = parseFloat(that._table.menubar.$container.css('height'));
  sizeFooter = parseFloat(that.$container.css('height'));
  if (that._table.header) {
    sizeHeader = parseFloat(that._table.header.$container.css('height'));
  }
  newOffset = sizeMenubar + sizeHeader + sizeFooter + sizeContainer;
  newOffset += this._table.$data.cssMarginTop() + this._table.$data.cssMarginBottom();

  var oldH = this._table.$data.height(),
    newH = this._table.$data.css('height', 'calc(100% - ' + newOffset + 'px)').height();

  // TODO CRU When dragging the table control downwards, the drawing of $data is extremely slow
  // TODO CRU Enforce min/max height to avoid drawing errors
  //adjust table
  this._table.$data.css('height', oldH)
    .animateAVCSD('height', newH,
      function() {
        that._table.$data.css('height', 'calc(100% - ' + newOffset + 'px)');
      },
      this._table.updateScrollbar.bind(this._table),
      500);
};

scout.TableFooter.prototype._addResize = function($parent) {
  this._$controlResize = $parent.appendDiv('control-resize')
    .on('mousedown', '', resize);
  var that = this;

  function resize(event) {
    $('body').addClass('row-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event) {
      var h = that._table.$container.height() - event.pageY;
      that._resizeData(h);
      that._$controlContainer.height(h);
      that.$controlContent.outerHeight(h);

      that._table.updateScrollbar();
      that.onResize();
    }

    function resizeEnd() {
      if (that._$controlContainer.height() < 75) {
        $('.selected', that.$container).click();
      }

      $('body').off('mousemove')
        .removeClass('row-resize');
    }

    return false;
  }
};

scout.TableFooter.prototype.onResize = function() {
  this._table.controls.forEach(function(control) {
    control.onResize();
  });
};
