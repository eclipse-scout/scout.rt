scout.TableFooter = function() {
  scout.TableFooter.parent.call(this);
};
scout.inherits(scout.TableFooter, scout.Widget);

scout.TableFooter.CONTAINER_SIZE = 345;

scout.TableFooter.prototype._init = function(options) {
  scout.TableFooter.parent.prototype._init.call(this, options);
  this._table = options.table;

  // Keystroke context for the search field.
  // TODO [dwi] migrate search-field to widget, so that this keystroke code is not in table footer class anymore.
  this.searchFieldKeyStrokeContext = new scout.InputFieldKeyStrokeContext();
  this.searchFieldKeyStrokeContext.$bindTarget = function() {
    return this._$textFilter;
  }.bind(this);
  this.searchFieldKeyStrokeContext.$scopeTarget = function() {
    return this._$textFilter;
  }.bind(this);
};

scout.TableFooter.prototype._render = function($parent) {
  var filter;
  $parent = $parent || this._table.$container;

  this.$container = $parent.appendDiv('table-footer');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.TableFooterLayout(this));

  // --- container for an open control ---
  this.$controlContainer = this.$container.appendDiv('table-control-container').hide();
  this._addResizer(this.$controlContainer);
  this.$controlContent = this.$controlContainer.appendDiv('table-control-content');

  // --- table controls section ---
  this._$controls = this.$container.appendDiv('table-controls');

  // --- info section ---
  this._$info = this.$container
    .appendDiv('table-info');

  // text filter
  this._$textFilter = scout.fields.new$TextField()
    .addClass('table-text-filter')
    .appendTo(this._$info)
    .on('input paste', '', $.debounce(this._onFilterInput.bind(this)))
    .placeholder(this.session.text('ui.FilterBy_'));
  filter = this._table.getFilter(scout.TableTextUserFilter.Type);
  if (filter) {
    this._$textFilter.val(filter.text);
  }

  // load info ("X rows loaded, click to reload")
  this._$infoLoad = this._$info
    .appendDiv('table-info-item table-info-load')
    .on('click', '', this._onInfoLoadClick.bind(this));

  // filter info ("X rows filtered by Y, click to remove filter")
  this._$infoFilter = this._$info
    .appendDiv('table-info-item table-info-filter')
    .on('click', '', this._onInfoFilterClick.bind(this));

  // selection info ("X rows selected, click to select all/none")
  this._$infoSelection = this._$info
    .appendDiv('table-info-item table-info-selection')
    .on('click', '', this._onInfoSelectionClick.bind(this));

  // table status
  this._$infoTableStatus = this._$info
    .appendDiv('table-info-item table-info-status')
    .on('mousedown', this._onStatusMousedown.bind(this));
  this._$infoTableStatusIcon = $('<span>')
    .addClass('font-icon icon')
    .appendTo(this._$infoTableStatus);

  // ------

  this._renderControls();
  this._renderInfo();
  this._updateInfoVisibility();

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_DRAWN, function(event) {
    this._renderInfoLoad();
  }.bind(this));

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_FILTERED, function(event) {
    this._renderInfoFilter();
  }.bind(this));

  this._table.events.on('addFilter', function(event) {
    this._renderInfoFilter();
    this._updateInfoFilterVisibility();
    if (event.filter.filterType === scout.TableTextUserFilter.Type) {
      this._$textFilter.val(event.filter.text);
    }
  }.bind(this));

  this._table.events.on('removeFilter', function(event) {
    this._renderInfoFilter();
    this._updateInfoFilterVisibility();
    if (event.filter.filterType === scout.TableTextUserFilter.Type) {
      this._$textFilter.val('');
    }
  }.bind(this));

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_SELECTED, function(event) {
    var numRows = 0;
    if (event.rows) {
      numRows = event.rows.length;
    }
    this._renderInfoSelection(numRows, event.allSelected);
  }.bind(this));

  this._table.events.on(scout.Table.GUI_EVENT_STATUS_CHANGED, function(event) {
    this._renderInfoTableStatus();
    this._updateInfoTableStatusVisibility();
  }.bind(this));

  this.session.keyStrokeManager.installKeyStrokeContext(this.searchFieldKeyStrokeContext);
};

scout.TableFooter.prototype._remove = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.searchFieldKeyStrokeContext);
  scout.TableFooter.parent.prototype._remove.call(this);
  this._hideTableStatusTooltip();
};

scout.TableFooter.prototype._onFilterInput = function(event) {
  var filter,
    $input = $(event.currentTarget),
    filterText = $input.val();

  if (filterText) {
    filter = scout.create('TableTextUserFilter', {
      session: this.session,
      table: this._table
    });
    filter.text = filterText.toLowerCase();
    this._table.addFilter(filter);
  } else if (!filterText) {
    this._table.removeFilterByKey(scout.TableTextUserFilter.Type);
  }

  this._table.filter();
  this.validateLayoutTree();
  event.stopPropagation();
};

scout.TableFooter.prototype._renderControls = function() {
  var controls = this._table.tableControls;
  if (controls) {
    controls.forEach(function(control) {
      control.tableFooter = this;
      control.table = this._table;
      control.setParent(this);
      control.render(this._$controls);
    }.bind(this));
  } else {
    this._$controls.empty();
  }
};

scout.TableFooter.prototype._renderInfo = function() {
  this._renderInfoLoad();
  this._renderInfoTableStatus();
  this._renderInfoFilter();
  this._renderInfoSelection();
};

scout.TableFooter.prototype._renderInfoLoad = function() {
  var $info = this._$infoLoad;
  var numRows = this._table.rows.length;

  $info.empty();
  if (!this._compactStyle) {
    $info.appendSpan().text(this.session.text('ui.NumRowsLoaded', this._computeCountInfo(numRows)));
    $info.appendBr();
    $info.appendSpan('table-info-button').text(this.session.text('ui.ReloadData')).appendTo($info);
  } else {
    $info.appendSpan().text(this.session.text('ui.NumRowsLoadedMin'));
    $info.appendBr();
    $info.appendSpan().text(this._computeCountInfo(numRows));
  }

  if (!this.htmlComp.layouting) {
    this.invalidateLayoutTree(false);
  }
};

scout.TableFooter.prototype._renderInfoFilter = function() {
  var $info = this._$infoFilter;
  var numRowsFiltered = this._table.filteredRows().length;
  var filteredBy = this._table.filteredBy().join(', '); // filteredBy() returns an array

  $info.empty();
  if (!this._compactStyle) {
    if (filteredBy) {
      $info.appendSpan().text(this.session.text('ui.NumRowsFilteredBy', this._computeCountInfo(numRowsFiltered), filteredBy));
    } else {
      $info.appendSpan().text(this.session.text('ui.NumRowsFiltered', this._computeCountInfo(numRowsFiltered)));
    }
    $info.appendBr();
    $info.appendSpan('table-info-button').text(this.session.text('ui.RemoveFilter')).appendTo($info);
  } else {
    $info.appendSpan().text(this.session.text('ui.NumRowsFilteredMin'));
    $info.appendBr();
    $info.appendSpan().text(this._computeCountInfo(numRowsFiltered));
  }

  if (!this.htmlComp.layouting) {
    this.invalidateLayoutTree(false);
  }
};

scout.TableFooter.prototype._renderInfoSelection = function(numRowsSelected, all) {
  var $info = this._$infoSelection;
  var numRows = this._table.rows.length;

  numRowsSelected = scout.helpers.nvl(numRowsSelected, this._table.selectedRows.length);
  all = scout.helpers.nvl(all, (numRows === numRowsSelected));

  $info.empty();
  if (!this._compactStyle) {
    $info.appendSpan().text(this.session.text('ui.NumRowsSelected', this._computeCountInfo(numRowsSelected)));
    $info.appendBr();
    $info.appendSpan('table-info-button').text(this.session.text(all ? 'ui.SelectNone' : 'ui.SelectAll')).appendTo($info);
  } else {
    $info.appendSpan().text(this.session.text('ui.NumRowsSelectedMin'));
    $info.appendBr();
    $info.appendSpan().text(this._computeCountInfo(numRowsSelected));
  }

  if (!this.htmlComp.layouting) {
    this.invalidateLayoutTree(false);
  }
};

scout.TableFooter.prototype._renderInfoTableStatus = function() {
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

  if (!this.htmlComp.layouting) {
    this.invalidateLayoutTree(false);
  }
};

scout.TableFooter.prototype._updateInfoVisibility = function() {
  this._updateInfoFilterVisibility();
  this._updateInfoSelectionVisibility();
  this._updateInfoTableStatusVisibility();
};

scout.TableFooter.prototype._updateInfoFilterVisibility = function() {
  var visible = this._table.filteredBy().length > 0;
  this._setInfoVisible(this._$infoFilter, visible);
};

scout.TableFooter.prototype._updateInfoSelectionVisibility = function() {
  var visible = this._table.multiSelect;
  this._setInfoVisible(this._$infoSelection, visible);
};

scout.TableFooter.prototype._updateInfoTableStatusVisibility = function() {
  var visible = this._table.tableStatus;
  if (visible) {
    // If the uiState of the tableStatus was not yet manually changed, or the user
    // explicitly activated it (relevant when changing pages), show the tooltip
    // when the "info visible" animation has finished. Otherwise, we don't show
    // the tooltip to not disturb the user.
    var complete = null;
    if (this._table.tableStatus.uiState !== 'user-hidden') {
      this._$infoTableStatus.addClass('tooltip-active'); // color icon before animation starts
      complete = function() {
        this._showTableStatusTooltip();
      }.bind(this);
    }
    this._setInfoVisible(this._$infoTableStatus, true, complete);
  } else {
    this._hideTableStatusTooltip();
    this._setInfoVisible(this._$infoTableStatus, false);
  }
};

scout.TableFooter.prototype._setInfoVisible = function($info, visible, complete) {
  if ($info.isVisible() === visible) {
    if (complete) {
      complete();
    }
    return;
  }
  var animate = this.rendered; // Animate only on a user interaction, no while the table gets rendered
  if (!animate) {
    $info.setVisible(visible);
    return;
  }
  if (visible) {
    $info.cssWidth(0).show();
    var animationOpts = {
      progress: this.revalidateLayout.bind(this),
      complete: function() {
        this.revalidateLayout();
        if (complete) {
          complete();
        }
      }.bind(this)
    };
    // Save complete function so that layout may use it
    $info.data('animationComplete', animationOpts.complete);
    $info.widthToContent(animationOpts);
  } else {
    // Mark element as hiding so that the layout does not try to resize it
    $info.data('hiding', true);
    $info.stop().animate({
      width: 0
    }, {
      progress: this.revalidateLayout.bind(this),
      complete: function() {
        $info.removeData('hiding');
        $info.hide();
      }
    });
  }
};

scout.TableFooter.prototype._onInfoLoadClick = function() {
  this._table.reload();
};

scout.TableFooter.prototype._onInfoFilterClick = function() {
  this._table.resetFilter();
};

scout.TableFooter.prototype._onInfoSelectionClick = function() {
  this._table.toggleSelection();
};

scout.TableFooter.prototype._computeCountInfo = function(n) {
  if (scout.helpers.nvl(n, 0) === 0) {
    if (this._compactStyle) {
      return this.session.text('ui.TableRowCount', 0);
    } else {
      return this.session.text('ui.TableRowCount0');
    }
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

scout.TableFooter.prototype._addResizer = function($parent) {
  this._$controlResize = $parent.appendDiv('table-control-resize')
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
    parent: this,
    text: text,
    cssClass: (isError ? 'tooltip-error' : (isWarning ? 'tooltip-warning' : (isInfo ? 'tooltip-info' : ''))),
    autoRemove: (!isError),
    $anchor: this._$infoTableStatusIcon
  };
  this._tableStatusTooltip = scout.create(scout.Tooltip, opts);
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
