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
  this._addAdapterProperties(['firstField', 'secondField', 'collapsibleField']);
  this._collapseKeyStroke;

  this._$splitArea;
  this._$splitter;
};
scout.inherits(scout.SplitBox, scout.CompositeField);

scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE = 'relative';
scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST = 'absoluteFirst';
scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND = 'absoluteSecond';

scout.SplitBox.prototype._init = function(model) {
  scout.SplitBox.parent.prototype._init.call(this, model);
  this._syncCollapseKeyStroke(this.collapseKeyStroke);
  this._updateCollapseHandle();
};

scout.SplitBox.prototype._render = function($parent) {
  this.addContainer($parent, 'split-box');
  // This widget does not support label, mandatoryIndicator and status

  // Create split area
  this._$splitArea = $parent.makeDiv('split-area');
  this.addField(this._$splitArea);
  this.htmlSplitArea = new scout.HtmlComponent(this._$splitArea, this.session);
  this.htmlSplitArea.setLayout(new scout.SplitBoxLayout(this));
  this._$window = $parent.window();
  this._$body = $parent.body();

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
      $('iframe').addClass('dragging-in-progress');

      // Get initial area and splitter bounds
      var splitAreaPosition = this._$splitArea.offset();
      var splitAreaSize = scout.graphics.getSize(this._$splitArea, true);
      var splitterPosition = this._$splitter.offset();
      var splitterSize = scout.graphics.getSize(this._$splitter, true);

      // Create temporary splitter
      var $tempSplitter = this._$splitArea.appendDiv('temp-splitter')
        .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
      if (this.splitHorizontal) { // "|"
        $tempSplitter.cssLeft(splitterPosition.left - splitAreaPosition.left);
      } else { // "--"
        $tempSplitter.cssTop(splitterPosition.top - splitAreaPosition.top);
      }
      this._$splitter.addClass('dragging');
    }

    var newSplitterPosition = this.splitterPosition;
    var SNAP_SIZE = 25;

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
        if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE) {
          newSplitterPosition = newSplitterPosition / (splitAreaSize.width - splitterSize.width - scout.HtmlEnvironment.fieldMandatoryIndicatorWidth);
        } else if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          newSplitterPosition = splitAreaSize.width - newSplitterPosition - splitterSize.width;
        }
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
        newSplitterPosition = targetSplitterPositionTop;
        if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE) {
          newSplitterPosition = newSplitterPosition / splitAreaSize.height;
        } else if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          newSplitterPosition = splitAreaSize.height - newSplitterPosition - splitterSize.height;
        }
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
        $('iframe').removeClass('dragging-in-progress');

        // Remove temporary splitter
        $tempSplitter.remove();
        this._$splitter.removeClass('dragging');

        // Update split box
        this.newSplitterPosition(newSplitterPosition);
        if (this.rendered) {
          // TODO CGU [6.0] remove this, should not be necessary anymore with 6.0
          this.htmlSplitArea.validateLayout(); // validate layout immediately (was invalidated by newSplitterPosition())
        }
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
  this._renderCollapsibleField(); // renders collapsibleField _and_ fieldCollapsed
  this._renderCollapseHandle(); // renders collapseHandle _and_ collapseKeyStroke
};

scout.SplitBox.prototype._syncSplitterPosition = function(splitterPosition) {
  this.splitterPosition = splitterPosition;
  // If splitter position is explicitly set by an event, no recalculation is necessary
  this._oldSplitterPositionType = null;
};

scout.SplitBox.prototype._renderSplitterPosition = function() {
  this.newSplitterPosition(this.splitterPosition);
};

scout.SplitBox.prototype._syncSplitterPositionType = function(splitterPositionType) {
  if (this.splitterPositionType !== splitterPositionType) {
    if (this.rendered && !this._oldSplitterPositionType) {
      this._oldSplitterPositionType = this.splitterPositionType;
      // We need to recalculate the splitter position. Because this requires the proper
      // size of the split box, this can only be done in _renderSplitterPositionType().
    }
    this.splitterPositionType = splitterPositionType;
  }
};

scout.SplitBox.prototype._renderSplitterPositionType = function() {
  if (this._oldSplitterPositionType) {
    // splitterPositionType changed while the split box was rendered --> convert splitterPosition
    // to the target type such that the current position in screen does not change.
    var splitAreaSize = this.htmlSplitArea.getSize(),
      splitterPosition = this.splitterPosition,
      splitterSize = scout.graphics.getVisibleSize(this._$splitter, true),
      totalSize = 0;
    if (this.splitHorizontal) { // "|"
      totalSize = splitAreaSize.width - splitterSize.width;
    } else { // "--"
      totalSize = splitAreaSize.height - splitterSize.height;
    }

    // Convert value depending on the old and new type system
    var oldIsRelative = (this._oldSplitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE);
    var newIsRelative = (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE);
    var oldIsAbsolute = !oldIsRelative;
    var newIsAbsolute = !newIsRelative;
    if (oldIsRelative && newIsAbsolute) {
      // From relative to absolute
      if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
        splitterPosition = totalSize - (totalSize * splitterPosition);
      } else {
        splitterPosition = totalSize * splitterPosition;
      }
    } else if (oldIsAbsolute && newIsRelative) {
      // From absolute to relative
      if (this._oldSplitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
        splitterPosition = (totalSize - splitterPosition) / totalSize;
      } else {
        splitterPosition = splitterPosition / totalSize;
      }
    } else if (oldIsAbsolute && newIsAbsolute) {
      splitterPosition = (totalSize - splitterPosition);
    }
    // Set as new splitter position
    this._oldSplitterPositionType = null;
    this.newSplitterPosition(splitterPosition);
  }
};

scout.SplitBox.prototype._renderSplitterEnabled = function() {
  if (this._$splitter) {
    this._$splitter.setEnabled(this.splitterEnabled);
  }
};

scout.SplitBox.prototype.setFieldCollapsed = function(collapsed) {
  if (this.fieldCollapsed === collapsed) {
    return;
  }
  this._setProperty('fieldCollapsed', collapsed);
  this._sendProperty('fieldCollapsed');
  this._updateCollapseHandleButtons();
  if (this.rendered) {
    this._renderFieldCollapsed();
  }
};

scout.SplitBox.prototype._renderFieldCollapsed = function() {
  this._renderCollapsibleField();
};

scout.SplitBox.prototype.setCollapsibleField = function(field) {
  if (this.collapsibleField === field) {
    return;
  }
  this._setProperty('collapsibleField', field);
  this._sendProperty('collapsibleField');
  this._updateCollapseHandle();
  if (this.rendered) {
    this._renderCollapsibleField();
  }
};

scout.SplitBox.prototype._updateCollapseHandle = function() {
  // always unregister key stroke first (although it may have been added by _syncCollapseKeyStroke before)
  if (this.collapseKeyStroke) {
    this.unregisterKeyStrokes(this.collapseKeyStroke);
  }
  if (this.collapsibleField) {
    if (!this._collapseHandle) {
      this._collapseHandle = scout.create('CollapseHandle', {
        parent: this
      });
      this._collapseHandle.on('action', this.toggleFieldCollapsed.bind(this));
      if (this.collapseKeyStroke) {
        this.registerKeyStrokes(this.collapseKeyStroke);
      }
    }
    this._updateCollapseHandleButtons();
  } else {
     if (this._collapseHandle) {
       this._collapseHandle.remove();
       this._collapseHandle = null;
     }
  }
};

scout.SplitBox.prototype._updateCollapseHandleButtons = function() {
  var leftVisible, rightVisible,
    collapsed = this.fieldCollapsed;
  if (this.collapsibleField === this.firstField) {
    leftVisible  = !collapsed;
    rightVisible =  collapsed;
  } else {
    leftVisible  =  collapsed;
    rightVisible = !collapsed;
  }
  this._collapseHandle.setLeftVisible(leftVisible);
  this._collapseHandle.setRightVisible(rightVisible);
};

scout.SplitBox.prototype._renderCollapsibleField = function() {
  if (this.firstField) {
    this.firstField.$container.removeClass('collapsed');
  }
  if (this.secondField) {
    this.secondField.$container.removeClass('collapsed');
  }
  if (this.collapsibleField && this.fieldCollapsed) {
    this.collapsibleField.$container.addClass('collapsed');
  }
  if (this.rendered) { // don't invalidate layout on initial rendering
    this.htmlSplitArea.invalidateLayoutTree(false);
  }
};

scout.SplitBox.prototype._syncCollapseKeyStroke = function(keyStroke) {
  if (keyStroke) {
    if (this.collapseKeyStroke instanceof scout.KeyStroke) {
      this.unregisterKeyStrokes(this.collapseKeyStroke);
    }
    this.collapseKeyStroke = new scout.SplitBoxCollapseKeyStroke(this, keyStroke);
    if (this._collapseHandle) {
      this.registerKeyStrokes(this.collapseKeyStroke);
    }
  }
  return false;
};

scout.SplitBox.prototype._renderCollapseHandle = function() {
  if (this._collapseHandle) {
    this._collapseHandle.render(this.$container);
  }
};

scout.SplitBox.prototype.newSplitterPosition = function(newSplitterPosition) {
  if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE) {
    // Ensure range 0..1
    newSplitterPosition = Math.max(0, Math.min(1, newSplitterPosition));
  } else {
    // Ensure not negative
    newSplitterPosition = Math.max(0, newSplitterPosition);
  }

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

scout.SplitBox.prototype.toggleFieldCollapsed = function() {
  this.setFieldCollapsed(!this.fieldCollapsed);
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
