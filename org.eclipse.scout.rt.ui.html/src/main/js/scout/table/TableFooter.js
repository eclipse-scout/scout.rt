scout.TableFooter = function(table) {
  scout.TableFooter.parent.call(this);
  this._table = table;
  this.filterKeyStrokeAdapter = new scout.FilterInputKeyStrokeAdapter(this._table);
  this._render(table.$container);
};
scout.inherits(scout.TableFooter, scout.Widget);

scout.TableFooter.FILTER_KEY = 'TEXTFIELD';
scout.TableFooter.CONTAINER_SIZE = 345;

scout.TableFooter.prototype._render = function($parent) {
  var filter, that = this;

  this.$container = $parent.appendDiv('table-footer');
  this.$controlContainer = this.$container.appendDiv('control-container').hide();
  this._addResize(this.$controlContainer);
  this.$controlContent = this.$controlContainer.appendDiv('control-content');
  this.$controlGroup = this.$container.appendDiv('control-group');

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

  this._$filterField = scout.fields.new$TextField()
    .addClass('control-filter')
    .appendTo(this.$container)
    .on('input paste', '', $.debounce(this._onFilterInput.bind(this)))
    .placeholder(this._table.session.text('ui.FilterBy_'));
  filter = this._table.getFilter(scout.TableFooter.FILTER_KEY);
  if (filter) {
    this._$filterField.val(filter.text);
  }

  this._updateTableControls();
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
    if (event.rows) {
      numRows = event.rows.length;
    }
    that._updateInfoSelection(numRows, event.allSelected);
    that._updateInfoSelectionVisibility();
  });
};

scout.TableFooter.prototype._installKeyStrokeAdapter = function() {
  scout.TableFooter.parent.prototype._installKeyStrokeAdapter.call(this);
  scout.keyStrokeManager.installAdapter(this._$filterField, this.filterKeyStrokeAdapter);
};

scout.TableFooter.prototype._uninstallKeyStrokeAdapter = function() {
  scout.TableFooter.parent.prototype._uninstallKeyStrokeAdapter.call(this);
  scout.keyStrokeManager.uninstallAdapter(this.filterKeyStrokeAdapter);
};

scout.TableFooter.prototype._onFilterInput = function(event) {
  var $input = $(event.currentTarget),
    filterText = $input.val();

  var filter = this._table.getFilter(scout.TableFooter.FILTER_KEY);
  if (!filter && filterText) {
    filter = {
      accept: function($row) {
        var rowText = $row.text().toLowerCase();
        return rowText.indexOf(this.text) > -1;
      }
    };
    this._table.registerFilter(scout.TableFooter.FILTER_KEY, filter);
  } else if (filter && !filterText) {
    this._table.unregisterFilter(scout.TableFooter.FILTER_KEY);
  }

  if (filter) {
    filter.text = filterText.toLowerCase();
    filter.label = filterText;
  }

  this._table.filter();
  event.stopPropagation();
};

scout.TableFooter.prototype.update = function() {
  this._updateTableControls();
  this._updateInfoLoadVisibility();
  this._updateInfoSelectionVisibility();
  this._updateInfoFilterVisibility();
};

scout.TableFooter.prototype._updateTableControls = function() {
  var controls = this._table.tableControls;
  if (controls) {
    controls.forEach(function(control) {
      control.tableFooter = this;
      control.table = this._table;
      control.render(this.$controlGroup);
    }.bind(this));
  } else {
    this.$controlGroup.empty();
  }
};

scout.TableFooter.prototype._updateInfoLoad = function() {
  var numRows = this._table.rows.length;
  var info = this._table.session.text('ui.NumRowsLoaded', this._computeCountInfo(numRows));
  info += '<br><span class="info-button">' + this._table.session.text('ui.ReloadData') + '</span>';
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
  var info = this._table.session.text('ui.NumRowsFiltered', this._computeCountInfo(numRowsFiltered));
  if (filteredBy) {
    info = this._table.session.text('ui.NumRowsFilteredBy', this._computeCountInfo(numRowsFiltered), filteredBy);
  }
  info += '<br><span class="info-button">' + this._table.session.text('ui.RemoveFilter') + '</span>';
  if (this._$infoFilter.html() === info) {
    return;
  }
  // FIXME BSH Table | Code injection! Don't use html() here, build content using jQuery objects instead. Fix this in the other methods as well.
  this._$infoFilter.html(info);
};

scout.TableFooter.prototype._updateInfoSelection = function(numSelectedRows, all) {
  var numRows = this._table.rows.length;
  var selectAllText, info;

  if (numSelectedRows === undefined) {
    numSelectedRows = this._table.selectedRows.length;
  }
  if (all === undefined) {
    all = numRows === numSelectedRows;
  }

  selectAllText = this._table.session.text(all ? 'ui.SelectNone' : 'ui.SelectAll');
  info = this._table.session.text('ui.NumRowsSelected', this._computeCountInfo(numSelectedRows));
  info += '<br><span class="info-button">' + selectAllText + '</span>';
  if (this._$infoSelection.html() === info) {
    return;
  }

  this._$infoSelection.html(info);
};

scout.TableFooter.prototype._updateInfoLoadVisibility = function() {
  var visible = (this._table.tableStatusVisible);
  this._setInfoVisible(this._$infoLoad, visible);
};

scout.TableFooter.prototype._updateInfoFilterVisibility = function() {
  var visible = (this._table.tableStatusVisible && this._table.filteredBy().length > 0);
  this._setInfoVisible(this._$infoFilter, visible);
};

scout.TableFooter.prototype._updateInfoSelectionVisibility = function() {
  var tableStatusVisible = (this._table.tableStatusVisible && this._table.multiSelect);
  this._setInfoVisible(this._$infoSelection, tableStatusVisible);
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
    return this._table.session.text('ui.TableRowCount0');
  } else if (n === 1) {
    return this._table.session.text('ui.TableRowCount1');
  } else {
    return this._table.session.text('ui.TableRowCount', n);
  }
};

/* open, close and resize of the container */

scout.TableFooter.prototype._revalidateTableLayout = function() {
  this._table.htmlComp.revalidateLayoutTree();
};

scout.TableFooter.prototype.openControlContainer = function() {
  var insets = scout.graphics.getInsets(this.$controlContainer),
    contentHeight = scout.TableFooter.CONTAINER_SIZE - insets.top - insets.bottom,
    that = this;

  // adjust content
  this.$controlContent.outerHeight(contentHeight);

  // open container, stop existing (close) animations before
  this.$controlContainer.stop(true).show().animate({
    height: scout.TableFooter.CONTAINER_SIZE
  }, {
    duration: 500,
    progress: that._revalidateTableLayout.bind(that)
  });
  this.open = true;
};

scout.TableFooter.prototype.closeControlContainer = function(control) {
  var that = this;
  if (this.tableControlKeyStrokeAdapter) {
    scout.keyStrokeManager.uninstallAdapter(this.tableControlKeyStrokeAdapter);
  }
  this.$controlContainer.stop(true).show().animate({
    height: 0
  }, {
    duration: 500,
    progress: that._revalidateTableLayout.bind(that)
  });

  this.$controlContainer.promise().done(function() {
    this.$controlContainer.hide();
    control.onControlContainerClosed();
  }.bind(this));

  this.open = false;

};

scout.TableFooter.prototype._addResize = function($parent) {
  var that = this;

  this._$controlResize = $parent.appendDiv('control-resize')
    .on('mousedown', '', resize);

  function resize(event) {
    $(window)
      .on('mousemove.tablefooter', resizeMove)
      .one('mouseup', resizeEnd);
    $('body').addClass('row-resize');

    function resizeMove(event) {
      var newHeight = that._table.$container.height() - event.pageY;
      that.$controlContainer.height(newHeight);
      that.$controlContent.outerHeight(newHeight);
      that._revalidateTableLayout();
      that.onResize();
    }

    function resizeEnd() {
      if (that.$controlContainer.height() < 100) {
        that.selectedControl.setSelected(false);
      }

      $(window).off('mousemove.tablefooter');
      $('body').removeClass('row-resize');
    }

    return false;
  }
};

scout.TableFooter.prototype.onResize = function() {
  this._table.tableControls.forEach(function(control) {
    control.onResize();
  });
};
