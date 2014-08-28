scout.TableFooter = function(table, $parent, session) {
  var that = this, i, control, $group;

  this._table = table;

  this._$tableControl = $parent.appendDIV('table-control');
  this.$controlContainer = this._$tableControl.appendDIV('control-container');
  this._addResize(this.$controlContainer);

  this._$controlLabel = this._$tableControl.appendDiv(undefined, 'control-label');
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

  // filter
  var filter = table.getFilter(scout.MapTableControl.FILTER_KEY),
    filterText;

  if (filter) {
    filterText = filter.text;
  }

  $('<input class="control-filter"></input>')
    .appendTo(this._$tableControl)
    .on('input paste', '', $.debounce(filterEnter))
    .val(filterText);

  // info section
  this._$infoSelect = this._$tableControl.appendDiv('InfoSelect').on('click', '', this._table.toggleSelection.bind(this._table));
  this._$infoFilter = this._$tableControl.appendDiv('InfoFilter').on('click', '', this._table.resetFilter.bind(this._table));
  this._$infoLoad = this._$tableControl.appendDiv('InfoLoad').on('click', '', this._table.sendReload.bind(this._table));

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_DRAWN, function(event) {
    that._setInfoLoad(event.numRows);
  });

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_SELECTED, function(event) {
    var numRows = 0;
    if (event.$rows) {
      numRows = event.$rows.length;
    }
    that._setInfoSelect(numRows, event.allSelected);
  });

  this._table.events.on(scout.Table.GUI_EVENT_ROWS_FILTERED, function(event) {
    if (event.filterName.length) {
      that._setInfoFilter(event.numRows, event.filterName.join(', '));
    } else {
      that._$infoFilter.animateAVCSD('width', 0, function() {
        $(this).hide();
      });
    }
  });

  this._table.events.on(scout.Table.GUI_EVENT_FILTER_RESETTED, function(event) {
    //hide info section
    that._$infoFilter.animateAVCSD('width', 0, function() {
      $(this).hide();
    });
  });

  // control buttons have mouse over effects
  $('body').on('mouseenter', '#control_graph, #control_chart, #control_map',
    function() {
      $('#control_label').text($(this).data('label'));
    });

  $('body').on('mouseleave', '#control_graph, #control_chart, #control_map',
    function() {
      $('#control_label').text('');
    });

  // TODO cru: move, clean
  function filterEnter(event) {
    var $input = $(this);
    $input.val($input.val().toLowerCase());

    var filter = table.getFilter(scout.MapTableControl.FILTER_KEY);
    if (!filter && $input.val()) {
      filter = {
        accept: function($row) {
          var rowText = $row.text().toLowerCase();
          return rowText.indexOf(this.text) > -1;
        }
      };
      table.registerFilter(scout.MapTableControl.FILTER_KEY, filter);
    } else if (!$input.val()) {
      table.unregisterFilter(scout.MapTableControl.FILTER_KEY);
    }

    if (filter) {
      filter.text = $input.val();
      filter.label = filter.text;
    }

    table.filter();
    event.stopPropagation();
  }


};

scout.TableFooter.prototype._setInfoLoad = function(count) {
  this._$infoLoad.html(this._findInfo(count) + ' geladen</br>Daten neu laden');
  this._$infoLoad.show().widthToContent();
};

scout.TableFooter.prototype._setInfoMore = function( /*count*/ ) {};

scout.TableFooter.prototype._setInfoFilter = function(count, origin) {
  this._$infoFilter.html(this._findInfo(count) + ' gefiltert' + (origin ? ' durch ' + origin : '') + '</br>Filter entfernen');
  this._$infoFilter.show().widthToContent();
};

scout.TableFooter.prototype._setInfoSelect = function(count, all) {
  var allText = all ? 'Keine' : 'Alle';
  this._$infoSelect.html(this._findInfo(count) + ' selektiert</br>' + (allText) + ' selektieren'); //FIXME get translations from server
  this._$infoSelect.show().widthToContent();
};

scout.TableFooter.prototype._findInfo = function(n) {
  if (n === 0) {
    return 'Keine Zeile';
  } else if (n == 1) {
    return 'Eine Zeile';
  } else {
    return n + ' Zeilen';
  }
};

scout.TableFooter.prototype._updateControlLabel = function($control) {
  var close = $control.hasClass('selected') ? ' schliessen' : '';
  this._$controlLabel.text($control.data('label') + close);
};

scout.TableFooter.prototype._resetControlLabel = function() {
  this._$controlLabel.text('');
};

scout.TableFooter.prototype._addGroup = function(title) {
  var $group = $.makeDiv(undefined, 'control-group').attr('data-title', title);
  this._$controlLabel.before($group);
  this._controlGroups[title] = $group;
  return $group;
};

/* open, close and resize of the container */

scout.TableFooter.prototype.openTableControl = function() {
  var SIZE_CONTAINER = 340;

  //adjust table
  this._resizeData(SIZE_CONTAINER);

  //adjust container
  this.$controlContainer.show().animateAVCSD('height', SIZE_CONTAINER, null, null, 500);

  this.open = true;
};

scout.TableFooter.prototype.closeTableControl = function(control) {
  //adjust table and container
  this._resizeData(0);

  // adjust container
  this.$controlContainer.animateAVCSD('height', 0, null, null, 500);
  this.$controlContainer.promise().done(function() {
    this.$controlContainer.hide();
    control.onClosed();
  }.bind(this));

  // adjust control
  this._resetControlLabel();

  this.open = false;
};

scout.TableFooter.prototype._resizeData = function(sizeContainer) {
  var that = this;

  // new size of container and table data
  var sizeMenubar = parseFloat(that._table.menubar.$container.css('height')),
    sizeHeader = parseFloat(that._table._$header.css('height')),
    sizeFooter = parseFloat(that._$tableControl.css('height')),
    newOffset = sizeMenubar + sizeHeader + sizeFooter + sizeContainer;

  var oldH = this._table.$data.height(),
    newH = this._table.$data.css('height', 'calc(100% - ' + newOffset + 'px)').height();

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
  this._$controlResize = $parent.appendDIV('control-resize')
    .on('mousedown', '', resize);
  var that = this;

  function resize (event){
    $('body').addClass('row-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event) {
      var h = that._table.$container.height() - event.pageY;
      that._resizeData(h);
      that.$controlContainer.height(h);
      that._table.updateScrollbar();
    }

    function resizeEnd() {
      if (that.$controlContainer.height() < 75) {
        $('.selected', that._$tableControl).click();
      }

      $('body').off('mousemove')
        .removeClass('row-resize');
    }

    return false;
  }
};


