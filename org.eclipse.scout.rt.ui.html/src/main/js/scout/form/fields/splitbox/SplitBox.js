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

  // Create split area
  this._$splitArea = $parent.makeDiv('split-area');
  this.addField(this._$splitArea);
  this.htmlSplitArea = new scout.HtmlComponent(this._$splitArea, this.session);
  this.htmlSplitArea.setLayout(new scout.SplitBoxLayout(this));
  this._$window = $parent.getWindow(true);
  this._$body = $parent.getBody();

  // Add fields and splitter
  if (this.firstField) {
    this.firstField.render(this._$splitArea);
    this.firstField.$container
      .addClass('first-field')
      .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
    this.firstField.on('propertyChange', onInnerFieldPropertyChange.bind(this));

    if (this.secondField) {
      this._$splitter = this._$splitArea.appendDiv('splitter')
        .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis')
        .on('mousedown', resizeSplitter.bind(this));

      this.secondField.render(this._$splitArea);
      this.secondField.$container
        .addClass('second-field')
        .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
      this.secondField.on('propertyChange', onInnerFieldPropertyChange.bind(this));
    }
  }

  // --- Helper functions ---

  function resizeSplitter(event) {
    if (event.which !== 1) {
      return; // only handle left mouse button
    }
    if (this.splitterEnabled) {
      // Update mouse position (see resizeMove() for details)
      var mousePosition = {
        x: event.clientX,
        y: event.clientY
      };

      // Add listeners (we add them to the window to make sure we get the mouseup event even when the cursor it outside the window)
      this._$window
        .on('mousemove.splitbox', resizeMove.bind(this))
        .on('mouseup.splitbox', resizeEnd.bind(this));
      // Ensure the correct cursor is always shown while moving
      this._$body.addClass(this.splitHorizontal ? 'col-resize' : 'row-resize');

      // Get initial area and splitter bounds
      var splitAreaPosition = this._$splitArea.offset();
      var splitAreaSize = scout.graphics.getSize(this._$splitArea, true);
      var splitterPosition = this._$splitter.offset();
      var splitterSize = scout.graphics.getSize(this._$splitter, true);

      // Create temporary splitter
      var $tempSplitter = this._$splitArea
        .makeDiv('temp-splitter')
        .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
      if (this.splitHorizontal) { // "|"
        $tempSplitter.cssLeft(splitterPosition.left - splitAreaPosition.left);
      } else { // "--"
        $tempSplitter.cssTop(splitterPosition.top - splitAreaPosition.top);
      }
      this._$splitter.addClass('dragging');

      var newSplitterPosition = this.splitterPosition;
      var SNAP_SIZE = 25;
    }

    function resizeMove(event) {
      if (event.clientX === mousePosition.x && event.clientY === mousePosition.y) {
        // Chrome bug: https://code.google.com/p/chromium/issues/detail?id=161464
        // When holding the mouse, but not moving it, a 'mousemove' event is fired every second nevertheless.
        return;
      }
      mousePosition = {
        x: event.clientX,
        y: event.clientY
      };

      if (this.splitHorizontal) { // "|"
        // Calculate target splitter position (in area)
        var targetSplitterPositionLeft = event.pageX - splitAreaPosition.left;

        // Snap to begin and end
        var tempSplitterOffsetX = splitterSize.width / 2;
        if (targetSplitterPositionLeft < SNAP_SIZE) {
          targetSplitterPositionLeft = 0;
          tempSplitterOffsetX = 0;
        } else if (splitAreaSize.width - targetSplitterPositionLeft < SNAP_SIZE) {
          targetSplitterPositionLeft = splitAreaSize.width;
          tempSplitterOffsetX = splitterSize.width;
        }

        // Update temporary splitter
        $tempSplitter.cssLeft(targetSplitterPositionLeft - tempSplitterOffsetX);
        // Normalize target position
        newSplitterPosition = (targetSplitterPositionLeft - tempSplitterOffsetX - scout.HtmlEnvironment.fieldMandatoryIndicatorWidth);
        newSplitterPosition /= (splitAreaSize.width - splitterSize.width - scout.HtmlEnvironment.fieldMandatoryIndicatorWidth);
      } else { // "--"
        // Calculate target splitter position (in area)
        var targetSplitterPositionTop = event.pageY - splitAreaPosition.top;

        // Snap to begin and end
        var tempSplitterOffsetY = splitterSize.height / 2;
        if (targetSplitterPositionTop < SNAP_SIZE) {
          targetSplitterPositionTop = 0;
          tempSplitterOffsetY = 0;
        } else if (splitAreaSize.height - targetSplitterPositionTop < SNAP_SIZE) {
          targetSplitterPositionTop = splitAreaSize.height;
          tempSplitterOffsetY = splitterSize.height;
        }

        // Update temporary splitter
        $tempSplitter.cssTop(targetSplitterPositionTop - tempSplitterOffsetY);
        // Normalize target position
        newSplitterPosition = targetSplitterPositionTop / splitAreaSize.height;
      }
    }

    function resizeEnd(event) {
      if (event.which !== 1) {
        return; // only handle left mouse button
      }
      // Remove listeners and reset cursor
      this._$window
        .off('mousemove.splitbox')
        .off('mouseup.splitbox');
      if ($tempSplitter) { // instead of check for this.splitterEnabled, if splitter is currently moving it must be finished correctly
        this._$body.removeClass((this.splitHorizontal ? 'col-resize' : 'row-resize'));

        // Remove temporary splitter
        $tempSplitter.remove();
        this._$splitter.removeClass('dragging');

        // Update split box
        this.newSplitterPosition(newSplitterPosition);
        this.htmlSplitArea.validateLayout(); // validate layout immediately (was invalidated by newSplitterPosition())
      }
    }

    return false;
  }

  function onInnerFieldPropertyChange(event) {
    if (event.changedProperties.indexOf('visible') !== -1) {
      // Mark layout as invalid
      this.htmlSplitArea.invalidateLayoutTree(false);
    }
  }
};

scout.SplitBox.prototype._renderProperties = function() {
  scout.SplitBox.parent.prototype._renderProperties.call(this);
  this._renderSplitterPosition();
  this._renderSplitterEnabled();
};

scout.SplitBox.prototype._renderSplitterPosition = function() {
  this.newSplitterPosition(this.splitterPosition);
};

scout.SplitBox.prototype._renderSplitterEnabled = function() {
  if (this._$splitter) {
    this._$splitter.setEnabled(this.splitterEnabled);
  }
};

scout.SplitBox.prototype.newSplitterPosition = function(newSplitterPosition) {
  // Ensure range 0..1
  newSplitterPosition = Math.max(0, Math.min(1, newSplitterPosition));

  // Set new value (send to server if changed)
  var positionChanged = (this.splitterPosition !== newSplitterPosition);
  this.splitterPosition = newSplitterPosition;
  if (positionChanged) {
    this._send('setSplitterPosition', {
      splitterPosition: newSplitterPosition
    });
  }

  // Mark layout as invalid
  this.htmlSplitArea.invalidateLayoutTree(false);
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
