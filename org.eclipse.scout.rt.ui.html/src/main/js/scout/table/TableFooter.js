scout.TableFooter = function(table) {
  scout.TableFooter.parent.call(this);
  this.init(table.session);

  this._table = table;
  this.filterKeyStrokeAdapter = new scout.FilterInputKeyStrokeAdapter(this._table);
};
scout.inherits(scout.TableFooter, scout.Widget);

scout.TableFooter.CONTAINER_SIZE = 345;

scout.TableFooter.prototype._render = function($parent) {
  var filter;
  $parent = $parent || this._table.$container;

  this.$container = $parent.appendDiv('table-footer');
  this.$controlContainer = this.$container.appendDiv('control-container').hide();
  this._addResize(this.$controlContainer);
  this.$controlContent = this.$controlContainer.appendDiv('control-content');
  this.$controlGroup = this.$container.appendDiv('control-group');

  // --- info section ---
  this._$controlInfo = this.$container
    .appendDiv('control-info');

  // load info ("X rows loaded, click to reload")
  this._$infoLoad = this._$controlInfo
    .appendDiv('table-info-load')
    .on('click', '', this._onClickInfoLoad.bind(this));

  // filter info ("X rows filtered by Y, click to remove filter")
  this._$infoFilter = this._$controlInfo
    .appendDiv('table-info-filter')
    .on('click', '', this._onClickInfoFilter.bind(this));

  // selection info ("X rows selected, click to select all/none")
  this._$infoSelection = this._$controlInfo
    .appendDiv('table-info-selection')
    .on('click', '', this._onClickInfoSelection.bind(this));

  // table status
  this._$infoTableStatus = this._$controlInfo
    .appendDiv('table-info-status')
    .on('mousedown', this._onStatusMousedown.bind(this));
  this._$infoTableStatusIcon = $('<span>')
    .addClass('font-icon icon')
    .appendTo(this._$infoTableStatus);

  // --- text filter ---

  this._$filterField = scout.fields.new$TextField()
    .addClass('control-filter')
    .appendTo(this.$container)
    .on('input paste', '', $.debounce(this._onFilterInput.bind(this)))
    .placeholder(this.session.text('ui.FilterBy_'));
  filter = this._table.getFilter(scout.TextUserTableFilter.Type);
  if (filter) {
    this._$filterField.val(filter.text);
  }

  // ------

  this._updateTableControls();
  this._updateInfoLoad();
  this._updateInfoLoadVisibility();
  this._updateInfoTableStatus();
  this._updateInfoTableStatusVisibility();
  this._updateInfoFilter();
  this._updateInfoFilterVisibility();
  this._updateInfoSelection();
  this._updateInfoSelectionVisibility();

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_DRAWN, function(event) {
    this._updateInfoLoad();
    this._updateInfoLoadVisibility();
  }.bind(this));

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_FILTERED, function(event) {
    this._updateInfoFilter();
    this._updateInfoFilterVisibility();
  }.bind(this));

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_SELECTED, function(event) {
    var numRows = 0;
    if (event.rows) {
      numRows = event.rows.length;
    }
    this._updateInfoSelection(numRows, event.allSelected);
    this._updateInfoSelectionVisibility();
  }.bind(this));

  this._table.events.on(scout.Table.GUI_EVENT_STATUS_CHANGED, function(event) {
    this._updateInfoTableStatus();
    this._updateInfoTableStatusVisibility();
  }.bind(this));

  this._installKeyStrokeAdapter();
};

scout.TableFooter.prototype._remove = function() {
  scout.TableFooter.parent.prototype._remove.call(this);
  this._hideTableStatusTooltip();
};

scout.TableFooter.prototype._installKeyStrokeAdapter = function() {
  scout.TableFooter.parent.prototype._installKeyStrokeAdapter.call(this);
  scout.keyStrokeUtils.installAdapter(this.session, this.filterKeyStrokeAdapter, this._$filterField);
};

scout.TableFooter.prototype._uninstallKeyStrokeAdapter = function() {
  scout.TableFooter.parent.prototype._uninstallKeyStrokeAdapter.call(this);
  scout.keyStrokeUtils.uninstallAdapter(this.filterKeyStrokeAdapter);
};

scout.TableFooter.prototype._onFilterInput = function(event) {
  var filter,
    $input = $(event.currentTarget),
    filterText = $input.val();

  if (filterText) {
    filter = new scout.TextUserTableFilter();
    filter.init({
      table: this._table
    }, this.session);
    filter.text = filterText.toLowerCase();
    this._table.addFilter(filter);
  } else if (!filterText) {
    this._table.removeFilter(scout.TextUserTableFilter.Type);
  }

  this._table.filter();
  event.stopPropagation();
};

scout.TableFooter.prototype.update = function() {
  this._updateTableControls();
  this._updateInfoLoadVisibility();
  this._updateInfoTableStatusVisibility();
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
  var $info = this._$infoLoad;
  var numRows = this._table.rows.length;

  $info.empty();
  $info.text(this.session.text('ui.NumRowsLoaded', this._computeCountInfo(numRows)));
  $('<br>').appendTo($info);
  $('<span>').addClass('info-button').text(this.session.text('ui.ReloadData')).appendTo($info);
};

scout.TableFooter.prototype._updateInfoFilter = function() {
  var $info = this._$infoFilter;
  var numRowsFiltered = this._table.filteredRows().length;
  var filteredBy = this._table.filteredBy().join(', '); // filteredBy() returns an array

  $info.empty();
  if (filteredBy) {
    $info.text(this.session.text('ui.NumRowsFilteredBy', this._computeCountInfo(numRowsFiltered), filteredBy));
  } else {
    $info.text(this.session.text('ui.NumRowsFiltered', this._computeCountInfo(numRowsFiltered)));
  }
  $('<br>').appendTo($info);
  $('<span>').addClass('info-button').text(this.session.text('ui.RemoveFilter')).appendTo($info);
};

scout.TableFooter.prototype._updateInfoSelection = function(numSelectedRows, all) {
  var $info = this._$infoSelection;
  var numRows = this._table.rows.length;

  numSelectedRows = scout.helpers.nvl(numSelectedRows, this._table.selectedRows.length);
  all = scout.helpers.nvl(all, (numRows === numSelectedRows));

  $info.empty();
  $info.text(this.session.text('ui.NumRowsSelected', this._computeCountInfo(numSelectedRows)));
  $('<br>').appendTo($info);
  $('<span>').addClass('info-button').text(this.session.text(all ? 'ui.SelectNone' : 'ui.SelectAll')).appendTo($info);
};

scout.TableFooter.prototype._updateInfoTableStatus = function() {
  var $info = this._$infoTableStatus;
  var tableStatus = this._table.tableStatus;
  if (tableStatus) {
    var isInfo = (tableStatus.severity > scout.status.Severity.OK);
    var isWarning = (tableStatus.severity > scout.status.Severity.INFO);
    var isError = (tableStatus.severity > scout.status.Severity.WARNING);
    $info.toggleClass('has-error', isError);
    $info.toggleClass('has-warning', isWarning && !isError);
    $info.toggleClass('has-info', isInfo && !isWarning && !isError);
  }
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
  var visible = (this._table.tableStatusVisible && this._table.multiSelect);
  this._setInfoVisible(this._$infoSelection, visible);
};

scout.TableFooter.prototype._updateInfoTableStatusVisibility = function() {
  var visible = (this._table.tableStatusVisible && this._table.tableStatus);
  if (visible) {
    // If the uiState of the tableStatus was not yet manually changed, or the user
    // explicitly activated it (relevant when changing pages), show the tooltip
    // when the "info visible" animation has finished. Otherwise, we don't show
    // the tooltip to not disturb the user.
    var complete = null;
    if (!this._table.tableStatus.uiState || this._table.tableStatus.uiState === 'user-shown') {
      this._$infoTableStatus.addClass('tooltip-active'); // color icon before animation starts
      complete = function() {
        this._showTableStatusTooltip();
      }.bind(this);
    }
    this._setInfoVisible(this._$infoTableStatus, true, complete);
  }
  else {
    this._hideTableStatusTooltip();
    this._setInfoVisible(this._$infoTableStatus, false);
  }
};

scout.TableFooter.prototype._setInfoVisible = function($info, visible, complete) {
  if (!complete) {
    complete = function() {
      this._updateTableStatusTooltipPosition();
    }.bind(this);
  }
  if (visible) {
    $info.css('display', 'inline-block').widthToContent(undefined, complete); // undefined = default duration
  } else {
    $info.animateAVCSD('width', 0, function() {
      $(this).hide();
      complete();
    });
  }
};

scout.TableFooter.prototype._onClickInfoLoad = function() {
  this._table.reload();
};

scout.TableFooter.prototype._onClickInfoFilter = function() {
  this._table.resetFilter();
  this._$filterField.val('');
};

scout.TableFooter.prototype._onClickInfoSelection = function() {
  this._table.toggleSelection();
};

scout.TableFooter.prototype._computeCountInfo = function(n) {
  if (scout.helpers.nvl(n, 0) === 0) {
    return this.session.text('ui.TableRowCount0');
  } else if (n === 1) {
    return this.session.text('ui.TableRowCount1');
  } else {
    return this.session.text('ui.TableRowCount', n);
  }
};

/* open, close and resize of the container */

scout.TableFooter.prototype._revalidateTableLayout = function() {
  this._table.htmlComp.revalidateLayoutTree();
};

scout.TableFooter.prototype.openControlContainer = function(control) {
  var insets = scout.graphics.getInsets(this.$controlContainer),
    contentHeight = scout.TableFooter.CONTAINER_SIZE - insets.top - insets.bottom;

  // adjust content
  this.$controlContent.outerHeight(contentHeight);

  // open container, stop existing (close) animations before
  // use delay to make sure form is rendered and layouted with new size
  this.$controlContainer.stop(true).show().delay(1).animate({
    height: scout.TableFooter.CONTAINER_SIZE
  }, {
    duration: this.rendered ? 500 : 0,
    progress: this._revalidateTableLayout.bind(this),
    complete: function() {
      control.onControlContainerOpened();
    }
  });
  this.open = true;
};

scout.TableFooter.prototype.closeControlContainer = function(control) {
  scout.keyStrokeUtils.uninstallAdapter(this.tableControlKeyStrokeAdapter);
  this.$controlContainer.stop(true).show().animate({
    height: 0
  }, {
    duration: 500,
    progress: this._revalidateTableLayout.bind(this)
  });

  this.$controlContainer.promise().done(function() {
    this.$controlContainer.hide();
    control.onControlContainerClosed();
  }.bind(this));

  this.open = false;

};

scout.TableFooter.prototype._addResize = function($parent) {
  this._$controlResize = $parent.appendDiv('control-resize')
    .on('mousedown', '', resize.bind(this));

  function resize(event) {
    $(window)
      .on('mousemove.tablefooter', resizeMove.bind(this))
      .one('mouseup', resizeEnd.bind(this));
    $('body').addClass('row-resize');

    function resizeMove(event) {
      var newHeight = this._table.$container.height() - event.pageY;
      this.$controlContainer.height(newHeight);
      this.$controlContent.outerHeight(newHeight);
      this._revalidateTableLayout();
      this.onResize();
    }

    function resizeEnd() {
      if (this.$controlContainer.height() < 100) {
        this.selectedControl.setSelected(false);
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

scout.TableFooter.prototype._onStatusMousedown = function(event) {
  // Toggle tooltip
  if (this._tableStatusTooltip && this._tableStatusTooltip.rendered) {
    this._table.tableStatus.uiState = 'user-hidden';
    this._hideTableStatusTooltip();
  } else {
    this._table.tableStatus.uiState = 'user-shown';
    this._showTableStatusTooltip();
  }
  $.suppressEvent(event);
};

scout.TableFooter.prototype._hideTableStatusTooltip = function() {
  clearTimeout(this._autoHideTableStatusTooltipTimeoutId);
  if (this._tableStatusTooltip && this._tableStatusTooltip.rendered) {
    this._tableStatusTooltip.remove();
    this._tableStatusTooltip = null;
  }
};

scout.TableFooter.prototype._showTableStatusTooltip = function() {
  // Remove existing tooltip (might have the wrong css class)
  if (this._tableStatusTooltip && this._tableStatusTooltip.rendered) {
    this._tableStatusTooltip.remove();
  }

  var tableStatus = this._table.tableStatus;
  var text = (tableStatus ? tableStatus.message : null);
  if (!scout.strings.hasText(text)) {
    return; // Refuse to show empty tooltip
  }

  var isInfo = (tableStatus.severity > scout.status.Severity.OK);
  var isWarning = (tableStatus.severity > scout.status.Severity.INFO);
  var isError = (tableStatus.severity > scout.status.Severity.WARNING);

  // Create new tooltip
  var opts = {
    text: text,
    cssClass: (isError ? 'tooltip-error' : (isWarning ? 'tooltip-warning' : (isInfo ? 'tooltip-info' : ''))),
    autoRemove: (!isError),
    $anchor: this._$infoTableStatusIcon
  };
  this._tableStatusTooltip = new scout.Tooltip(this.session, opts);
  this._tableStatusTooltip.render();

  // Adjust icon style
  this._$infoTableStatus.addClass('tooltip-active');
  this._tableStatusTooltip.on('remove', function() {
    this._$infoTableStatus.removeClass('tooltip-active');
  }.bind(this));

  // Auto-hide unimportant messages
  clearTimeout(this._autoHideTableStatusTooltipTimeoutId);
  if (!isError && !this._table.tableStatus.uiState) {
    // Remember auto-hidden, in case the user changes outline before timeout elapses
    this._table.tableStatus.uiState = 'auto-hidden';
    this._autoHideTableStatusTooltipTimeoutId = setTimeout(function() {
      this._hideTableStatusTooltip();
    }.bind(this), 5000);
  }
};

scout.TableFooter.prototype._updateTableStatusTooltipPosition = function() {
  if (this._tableStatusTooltip && this._tableStatusTooltip.rendered) {
    this._tableStatusTooltip.position();
  }
};
