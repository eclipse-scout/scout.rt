scout.TableFooter = function(table, $parent, session) {
  this._table = table;

  this._$tableControl = $parent.appendDiv('TableControl');

  this.$controlContainer = this._$tableControl.appendDiv('ControlContainer');
  this._$controlResizeTop = this._$tableControl.appendDiv('ControlResizeTop');
  this._$controlResizeBottom = this._$tableControl.appendDiv('ControlResizeBottom');

  var that = this;
  this._$controlLabel = this._$tableControl.appendDiv(undefined, 'control-label');
  this._controlGroups = {};

  this._$infoSelect = this._$tableControl.appendDiv('InfoSelect').on('click', '', this._table.toggleSelection.bind(this._table));
  this._$infoFilter = this._$tableControl.appendDiv('InfoFilter').on('click', '', this._table.resetFilter.bind(this._table));
  this._$infoLoad = this._$tableControl.appendDiv('InfoLoad').on('click', '', this._table.drawData.bind(this._table));

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
  $("body").on("mouseenter", "#control_graph, #control_chart, #control_map",
    function() {
      $('#control_label').text($(this).data('label'));
    });

  $("body").on("mouseleave", "#control_graph, #control_chart, #control_map",
    function() {
      $('#control_label').text('');
    });
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

/**
 * @param control object with label and action().
 */
scout.TableFooter.prototype.addGroup = function(title) {
  var $group = $.makeDiv(undefined, 'control-group').attr('data-title', title);
  this._$controlLabel.before($group);
  this._controlGroups[title] = $group;
  return $group;
};

scout.TableFooter.prototype.addControl = function(control) {
  var classes = 'control ';
  if (control.cssClass) {
    classes += control.cssClass;
  }

  var $group = this._controlGroups[control.group];
  if (!$group) {
    $group = this.addGroup(control.group);
  }

  var $control = $group.appendDiv(undefined, classes)
    .data('control', control);

  //Link button with scout.TableControl
  control.$controlButton = $control;

  control._setEnabled(control.enabled);
};

scout.TableFooter.prototype.openTableControl = function() {
  // allow resizing
  this._$tableControl.addClass('resize-on');

  //adjust table
  this._table.$data.animateAVCSD('height',
    parseFloat(this._table.$container.css('height')) - 444,
    function() {
      $(this).css('height', 'calc(100% - 430px');
    },
    this._table.updateScrollbar.bind(this._table),
    500);

  // visual: update label, size container and control
  this.$controlContainer.height(340);
  this._$tableControl.animateAVCSD('height', 400, null, null, 500);

  // set events for resizing
  this._$controlResizeTop.on('mousedown', '', resizeControl);
  this._$controlResizeBottom.on('mousedown', '', resizeControl);

  this.open = true;

  var that = this;

  function resizeControl(event) {
    $('body').addClass('row-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    var offset = (this.id == 'ControlResizeTop') ? 102 : 152;

    function resizeMove(event) {
      var h = that._table.$container.outerHeight() - event.pageY + offset;
      if (that._table.$container.outerHeight() < h + 50) {
        //Don't overlap table header
        return false;
      }

      that._$tableControl.height(h);
      that._table.$data.height('calc(100% - ' + (h + 30) + 'px)');
      that.$controlContainer.height(h - 60);
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

scout.TableFooter.prototype.closeTableControl = function(control) {
  // reset handling resize
  this._$controlResizeTop.off('mousedown');
  this._$controlResizeBottom.off('mousedown');

  // classes: unselect and stop resizing
  this._$tableControl.removeClass('resize-on');

  var that = this;
  //adjust table
  this._table.$data.animateAVCSD('height',
    parseFloat(that._table.$container.css('height')) - 93,
    function() {
      $(this).css('height', 'calc(100% - 85px');
    },
    that._table.updateScrollbar.bind(that._table),
    500);

  // visual: reset label and close control
  this._resetControlLabel();
  this._$tableControl.animateAVCSD('height', 50, null, null, 500);

  this._$tableControl.promise().done(function() {
    control.remove();
  });
  this.open = false;
};
