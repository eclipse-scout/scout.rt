// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.SplitBox = function() {
  scout.SplitBox.parent.call(this);
  this.splitHorizontal; // true = split x-axis, false = split y-axis
  this._addAdapterProperties(['firstField', 'secondField']);

  this._$splitArea;
  this._$splitter;
};
scout.inherits(scout.SplitBox, scout.CompositeField);

scout.SplitBox.prototype._render = function($parent) {
  this.addContainer($parent, 'split-box');
  // This widget does not support label, mandatoryIndicator and status

  // Prepare split area
  this._$splitArea = $.makeDiv('split-area');
  var htmlComp = new scout.HtmlComponent(this._$splitArea, this.session);
  htmlComp.setLayout(new scout.SplitBoxLayout(this));
  // Add fields and splitter
  if (this.firstField) {
    this.firstField.render(this._$splitArea);
    this.firstField.$container
      .addClass('first-field')
      .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');

    if (this.secondField) {
      this._$splitter = $.makeDiv('splitter')
        .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis')
        .on('mousedown', resizeSplitter)
        .appendTo(this._$splitArea);

      this.secondField.render(this._$splitArea);
      this.secondField.$container
        .addClass('second-field')
        .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
    }
  }
  // Add splitArea as field
  this.addField(this._$splitArea);

  // --- Helper functions ---

  var that = this;
  function resizeSplitter() {
    // Add listeners (we add them to the window to make sure we get the mouseup event even when the cursor it outside the window)
    $(window)
      .on('mousemove.splitbox', resizeMove)
      .one('mouseup', resizeEnd);
    // Ensure the correct cursor is always shown while moving
    $('body').addClass(that.splitHorizontal ? 'col-resize' : 'row-resize');

    // Get initial area and splitter bounds
    var splitAreaPosition = that._$splitArea.offset();
    var splitAreaSize = scout.graphics.getSize(that._$splitArea, true);
    var splitterPosition = that._$splitter.offset();
    var splitterSize = scout.graphics.getSize(that._$splitter, true);

    // Create temporary splitter
    var $tempSplitter = $.makeDiv('temp-splitter')
      .addClass(that.splitHorizontal ? 'x-axis' : 'y-axis')
      .appendTo(that._$splitArea);
    if (that.splitHorizontal) { // "|"
      $tempSplitter.cssLeft(splitterPosition.left - splitAreaPosition.left);
    } else { // "--"
      $tempSplitter.cssTop(splitterPosition.top - splitAreaPosition.top);
    }
    that._$splitter.addClass('dragging');

    var newSplitterPosition = that.splitterPosition;
    var SNAP_SIZE = 25;

    function resizeMove(event) {
      if (that.splitHorizontal) { // "|"
        // Calculate target splitter position (in area)
        var targetSplitterPositionLeft = event.pageX - splitAreaPosition.left;

        // Snap to begin and end
        var tempSplitterOffsetX = splitterSize.width / 2;
        if (targetSplitterPositionLeft < SNAP_SIZE) {
          targetSplitterPositionLeft = 0;
          tempSplitterOffsetX = 0;
        }
        else if (splitAreaSize.width - targetSplitterPositionLeft < SNAP_SIZE) {
          targetSplitterPositionLeft = splitAreaSize.width;
          tempSplitterOffsetX = splitterSize.width;
        }

        // Update temporary splitter
        $tempSplitter.cssLeft(targetSplitterPositionLeft - tempSplitterOffsetX);
        // Normalize target position
        newSplitterPosition = targetSplitterPositionLeft / splitAreaSize.width;
      } else { // "--"
        // Calculate target splitter position (in area)
        var targetSplitterPositionTop = event.pageY - splitAreaPosition.top;

        // Snap to begin and end
        var tempSplitterOffsetY = splitterSize.height / 2;
        if (targetSplitterPositionTop < SNAP_SIZE) {
          targetSplitterPositionTop = 0;
          tempSplitterOffsetY = 0;
        }
        else if (splitAreaSize.height - targetSplitterPositionTop < SNAP_SIZE) {
          targetSplitterPositionTop = splitAreaSize.height;
          tempSplitterOffsetY = splitterSize.height;
        }

        // Update temporary splitter
        $tempSplitter.cssTop(targetSplitterPositionTop- tempSplitterOffsetY);
        // Normalize target position
        newSplitterPosition = targetSplitterPositionTop / splitAreaSize.height;
      }
    }

    function resizeEnd(event) {
      // Remove listeners and reset cursor
      $(window).off('mousemove.splitbox');
      $('body').removeClass((that.splitHorizontal ? 'col-resize' : 'row-resize'));

      // Remove temporary splitter
      $tempSplitter.remove();
      that._$splitter.removeClass('dragging');

      // Update split box
      that.newSplitterPosition(newSplitterPosition);
    }

    return false;
  }
};

scout.SplitBox.prototype._renderProperties = function() {
  scout.SplitBox.parent.prototype._renderProperties.call(this);
  this._renderSplitterPosition(this.splitterPosition);
};

scout.SplitBox.prototype._renderSplitterPosition = function() {
  this.newSplitterPosition(this.splitterPosition);
};

scout.SplitBox.prototype.newSplitterPosition = function(newSplitterPosition) {
  // Ensure range 0..1
  newSplitterPosition = Math.max(0, Math.min(1, newSplitterPosition));

  // Update model TODO BSH Scout-model? How to cache position?
  this.splitterPosition = newSplitterPosition;

  // Update layout
  scout.HtmlComponent.get(this._$splitArea).revalidate();
};

/**
 * @override CompositeField.js
 */
scout.SplitBox.prototype.getFields = function() {
  var fields = [];
  if (this.firstField) {
    fields.push(this.firstField);
  }
  if (this.secondField) {
    fields.push(this.secondField);
  }
  return fields;
};
