/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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
  this._addWidgetProperties(['firstField', 'secondField', 'collapsibleField']);
  this._addPreserveOnPropertyChangeProperties(['collapsibleField']);

  this.fieldCollapsed = false;
  this.collapsibleField;
  this.toggleCollapseKeyStroke;
  this.firstCollapseKeyStroke;
  this.secondCollapseKeyStroke;
  this.splitHorizontal = true; // true = split x-axis, false = split y-axis
  this.splitterEnabled = true;
  this.splitterPosition = 0.5;
  this.minSplitterPosition;
  this.splitterPositionType = scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST;
  this.fieldMinimized = false;

  this._$splitArea;
  this._$splitter;
};
scout.inherits(scout.SplitBox, scout.CompositeField);

scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST = 'relativeFirst';
scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND = 'relativeSecond';
scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST = 'absoluteFirst';
scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND = 'absoluteSecond';

scout.SplitBox.prototype._init = function(model) {
  scout.SplitBox.parent.prototype._init.call(this, model);
  this._setToggleCollapseKeyStroke(this.toggleCollapseKeyStroke);
  this._setFirstCollapseKeyStroke(this.firstCollapseKeyStroke);
  this._setSecondCollapseKeyStroke(this.secondCollapseKeyStroke);
  this._updateCollapseHandle();
};

scout.SplitBox.prototype._render = function() {
  this.addContainer(this.$parent, 'split-box');
  // This widget does not support label, mandatoryIndicator and status

  // Create split area
  this._$splitArea = this.$parent.makeDiv('split-area');
  this.addField(this._$splitArea);
  this.htmlSplitArea = scout.HtmlComponent.install(this._$splitArea, this.session);
  this.htmlSplitArea.setLayout(new scout.SplitBoxLayout(this));
  this._$window = this.$parent.window();
  this._$body = this.$parent.body();

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
        x: event.pageX,
        y: event.pageY
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
      var splitAreaSize = scout.graphics.size(this._$splitArea, true);
      var splitterPosition = this._$splitter.offset();
      var splitterSize = scout.graphics.size(this._$splitter, true);

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
    var SNAP_SIZE = 10;

    function resizeMove(event) {
      if (event.pageX === mousePosition.x && event.pageY === mousePosition.y) {
        // Chrome bug: https://code.google.com/p/chromium/issues/detail?id=161464
        // When holding the mouse, but not moving it, a 'mousemove' event is fired every second nevertheless.
        return;
      }
      mousePosition = {
        x: event.pageX,
        y: event.pageY
      };

      if (this.splitHorizontal) { // "|"
        // Calculate target splitter position (in area)
        var targetSplitterPositionLeft = event.pageX - splitAreaPosition.left;

        // De-normalize minimum splitter position to allowed splitter range in pixel [minSplitterPositionLeft, maxSplitterPositionLeft]
        var minSplitterPositionLeft;
        var maxSplitterPositionLeft;

        // Splitter width plus margin on right side, if temporary splitter position is x, the splitter div position is x-splitterOffset
        var splitterOffset = Math.floor((splitterSize.width + scout.HtmlEnvironment.fieldMandatoryIndicatorWidth) / 2);

        if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST) {
          minSplitterPositionLeft = scout.nvl(this.minSplitterPosition, 0);
          // allow to move the splitter to right side, leaving minimal space for splitter div without right margin (=total splitter size minus offset)
          maxSplitterPositionLeft = splitAreaSize.width - splitterSize.width + splitterOffset;
        } else if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) {
          minSplitterPositionLeft = (splitAreaSize.width - splitterSize.width) * scout.nvl(this.minSplitterPosition, 0);
          // allow to move the splitter to right side, leaving minimal space for splitter div without right margin (=total splitter size minus offset)
          maxSplitterPositionLeft = splitAreaSize.width - splitterSize.width + splitterOffset;
        } else if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          minSplitterPositionLeft = 0;
          // allow to move the splitter to right side, leaving minimal space for splitter div without right margin, reserving space for minimum splitter size
          maxSplitterPositionLeft = splitAreaSize.width - splitterSize.width + splitterOffset - scout.nvl(this.minSplitterPosition, 0);
        } else if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND) {
          minSplitterPositionLeft = 0;
          // allow to move the splitter to right side, leaving minimal space for splitter div without right margin, reserving space for minimum splitter size
          maxSplitterPositionLeft = splitAreaSize.width - splitterSize.width + splitterOffset - Math.floor(scout.nvl(this.minSplitterPosition, 0) * (splitAreaSize.width - splitterSize.width));
        }

        // Snap to begin and end
        var tempSplitterOffsetX = splitterOffset;

        if (targetSplitterPositionLeft < (minSplitterPositionLeft + splitterOffset + SNAP_SIZE)) {  // snap left if minimum position is reached (+ snap range)
          targetSplitterPositionLeft = minSplitterPositionLeft;  // set splitter directly to left minimal bound
          tempSplitterOffsetX = 0;  // setting splitter to left minimal bound, does not require an additional offset
        } else if (targetSplitterPositionLeft > (maxSplitterPositionLeft - SNAP_SIZE)) {
          targetSplitterPositionLeft = maxSplitterPositionLeft;
        }

        // Update temporary splitter
        $tempSplitter.cssLeft(targetSplitterPositionLeft - tempSplitterOffsetX);

        // Normalize target position (available splitter area is (splitAreaSize.width - splitterSize.width))
        newSplitterPosition = (targetSplitterPositionLeft - tempSplitterOffsetX);
        if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) {
          newSplitterPosition = newSplitterPosition / (splitAreaSize.width - splitterSize.width);
        } else if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND) {
          newSplitterPosition = 1 - (newSplitterPosition / (splitAreaSize.width - splitterSize.width));
        } else if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) {
          newSplitterPosition = splitAreaSize.width - splitterSize.width - newSplitterPosition;
        }
      } else { // "--"
        // Calculate target splitter position (in area)
        var targetSplitterPositionTop = event.pageY - splitAreaPosition.top;

        // Snap to begin and end
        var tempSplitterOffsetY = Math.floor(splitterSize.height / 2);
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
        newSplitterPosition = targetSplitterPositionTop - tempSplitterOffsetY;
        if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) {
          newSplitterPosition = newSplitterPosition / (splitAreaSize.height - splitterSize.height);
        } else if (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND) {
          newSplitterPosition = 1 - (newSplitterPosition / (splitAreaSize.height - splitterSize.height));
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
        this.newSplitterPosition(newSplitterPosition, true);
      }
    }

    return false;
  }

  function onInnerFieldPropertyChange(event) {
    if (event.propertyName === 'visible') {
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
  this._renderCollapseHandle(); // renders collapseHandle _and_ toggleCollapseKeyStroke _and_ firstCollapseKeyStroke _and_ secondCollapseKeyStroke
  this._renderFieldMinimized();
};

scout.SplitBox.prototype._setSplitterPosition = function(splitterPosition) {
  this._setProperty('splitterPosition', splitterPosition);
  // If splitter position is explicitly set by an event, no recalculation is necessary
  this._oldSplitterPositionType = null;
};

scout.SplitBox.prototype._renderSplitterPosition = function() {
  this.newSplitterPosition(this.splitterPosition, false);  // do not update (override) field minimized if new position is set by model
};

scout.SplitBox.prototype._setSplitterPositionType = function(splitterPositionType) {
  if (this.rendered && !this._oldSplitterPositionType) {
    this._oldSplitterPositionType = this.splitterPositionType;
    // We need to recalculate the splitter position. Because this requires the proper
    // size of the split box, this can only be done in _renderSplitterPositionType().
  }
  this._setProperty('splitterPositionType', splitterPositionType);
};

scout.SplitBox.prototype._renderSplitterPositionType = function() {
  if (this._oldSplitterPositionType) {
    // splitterPositionType changed while the split box was rendered --> convert splitterPosition
    // to the target type such that the current position in screen does not change.
    var splitAreaSize = this.htmlSplitArea.size(),
      splitterPosition = this.splitterPosition,
      splitterSize = scout.graphics.size(this._$splitter, true),
      minSplitterPosition = this.minSplitterPosition,
      totalSize = 0;
    if (this.splitHorizontal) { // "|"
      totalSize = splitAreaSize.width - splitterSize.width;
    } else { // "--"
      totalSize = splitAreaSize.height - splitterSize.height;
    }

    // Convert value depending on the old and new type system
    var oldIsRelative = this._isSplitterPositionTypeRelative(this._oldSplitterPositionType);
    var newIsRelative = this._isSplitterPositionTypeRelative(this.splitterPositionType);
    var oldIsAbsolute = !oldIsRelative;
    var newIsAbsolute = !newIsRelative;
    if (oldIsRelative && newIsAbsolute) {
      // From relative to absolute
      if ((this._oldSplitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST && this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND) ||
          (this._oldSplitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND && this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST)) {
        splitterPosition = totalSize - (totalSize * splitterPosition); // changed from first to second field or from second to first field, invert splitter position
      } else {
        splitterPosition = totalSize * splitterPosition;
      }
      // convert minimum splitter position
      if (minSplitterPosition) {
        minSplitterPosition = totalSize * minSplitterPosition;
      }
    } else if (oldIsAbsolute && newIsRelative) {
      // From absolute to relative
      if ((this._oldSplitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST && this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND) ||
          (this._oldSplitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND && this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST)) {
        splitterPosition = (totalSize - splitterPosition) / totalSize;  // changed from first to second field or from second to first field, invert splitter position
      } else {
        splitterPosition = splitterPosition / totalSize;
      }

      // convert minimum splitter position
      if (minSplitterPosition) {
        minSplitterPosition = minSplitterPosition / totalSize;
      }
    } else if (oldIsAbsolute && newIsAbsolute) {
      splitterPosition = (totalSize - splitterPosition);
      // do not convert minimum splitter position, unit did not change
    } else { // oldIsRelative && newIsRelative
      splitterPosition = 1 - splitterPosition;
      // do not convert minimum splitter position, unit did not change
    }
    // set new minimum splitter position
    this.setMinSplitterPosition(minSplitterPosition);

    // Set as new splitter position
    this._oldSplitterPositionType = null;
    this.newSplitterPosition(splitterPosition, true);
  }
};

scout.SplitBox.prototype._isSplitterPositionTypeRelative = function(positionType) {
  return (positionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) || (positionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_SECOND);
};

scout.SplitBox.prototype._renderSplitterEnabled = function() {
  if (this._$splitter) {
    this._$splitter.setEnabled(this.splitterEnabled);
  }
};

scout.SplitBox.prototype.setFieldCollapsed = function(collapsed) {
  this.setProperty('fieldCollapsed', collapsed);
  this._updateCollapseHandleButtons();
};

scout.SplitBox.prototype._renderFieldCollapsed = function() {
  this._renderCollapsibleField();
};

scout.SplitBox.prototype.setCollapsibleField = function(field) {
  this.setProperty('collapsibleField', field);
  this._updateCollapseHandle();
};

scout.SplitBox.prototype._updateCollapseHandle = function() {
  // always unregister key stroke first (although it may have been added by _setToggleCollapseKeyStroke before)
  if (this.toggleCollapseKeyStroke) {
    this.unregisterKeyStrokes(this.toggleCollapseKeyStroke);
  }
  if (this.firstCollapseKeyStroke) {
    this.unregisterKeyStrokes(this.firstCollapseKeyStroke);
  }
  if (this.secondCollapseKeyStroke) {
    this.unregisterKeyStrokes(this.secondCollapseKeyStroke);
  }

  if (this.collapsibleField) {
    var horizontalAlignment = scout.CollapseHandle.HorizontalAlignment.LEFT;
    if (this.collapsibleField !== this.firstField) {
      horizontalAlignment = scout.CollapseHandle.HorizontalAlignment.RIGHT;
    }

    if (!this._collapseHandle) {
      // create new collapse handle
      this._collapseHandle = scout.create('CollapseHandle', {
        parent: this,
        horizontalAlignment: horizontalAlignment
      });
      this._collapseHandle.on('action', this.collapseHandleButtonPressed.bind(this));
      if (this.toggleCollapseKeyStroke) {
        this.registerKeyStrokes(this.toggleCollapseKeyStroke);
      }
      if (this.firstCollapseKeyStroke) {
        this.registerKeyStrokes(this.firstCollapseKeyStroke);
      }
      if (this.secondCollapseKeyStroke) {
        this.registerKeyStrokes(this.secondCollapseKeyStroke);
      }
      if (this.rendered) {
        this._renderCollapseHandle();
      }
    } else {
      // update existing collapse handle
      this._collapseHandle.setHorizontalAlignment(horizontalAlignment);
    }

    this._updateCollapseHandleButtons();
  } else {
     if (this._collapseHandle) {
       this._collapseHandle.destroy();
       this._collapseHandle = null;
     }
  }
};

scout.SplitBox.prototype._updateCollapseHandleButtons = function() {
  if (!this._collapseHandle) {
    return;
  }
  var leftVisible, rightVisible,
    collapsed = this.fieldCollapsed,
    minimized = this.fieldMinimized,
    minimizable = !!this.minSplitterPosition,
    positionTypeFirstField = ((this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) || (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST)),
    positionNotAccordingCollapsibleField = (positionTypeFirstField && this.collapsibleField === this.secondField) || (!positionTypeFirstField && this.collapsibleField === this.firstField);

    if (positionTypeFirstField) {
      if (positionNotAccordingCollapsibleField) {
        leftVisible  = (!minimized && minimizable) || collapsed; // left = decrease collapsible field size. Decrease field in this order [minimized <- default <- collapsed]
        rightVisible = !collapsed; // right = increase collapsible field size. Increase field in this order [minimized -> default -> collapsed]
      } else {
        leftVisible  = !collapsed; // left = increase collapsible field size. Increase field in this order [default <- minimized <- collapsed]
        rightVisible =  collapsed || (minimized && minimizable); // right = decrease collapsible field size. Decrease field in this order [default -> minimized -> collapsed]
      }
    } else {
      if (positionNotAccordingCollapsibleField) {
        leftVisible  = !collapsed; // left = decrease collapsible field size. Decrease field in this order [collapsed <- default <- minimized]
        rightVisible = (!minimized && minimizable) || collapsed; // right = increase collapsible field size. Increase field in this order [collapsed -> default -> minimized]
      } else {
        leftVisible =  collapsed || (minimized && minimizable); // left = decrease collapsible field size. Decrease field in this order [collapsed <- minimized <- default]
        rightVisible  = !collapsed; // right = increase collapsible field size. Increase field in this order [collapsed -> minimized -> default]
      }
    }

  this._collapseHandle.setLeftVisible(leftVisible);
  this._collapseHandle.setRightVisible(rightVisible);

  // update allowed keystrokes
  if (this.firstCollapseKeyStroke) {
    if (leftVisible) {
      this.registerKeyStrokes(this.firstCollapseKeyStroke);
    } else {
      this.unregisterKeyStrokes(this.firstCollapseKeyStroke);
    }
  }
  if (this.secondCollapseKeyStroke) {
    if (rightVisible) {
      this.registerKeyStrokes(this.secondCollapseKeyStroke);
    } else {
      this.unregisterKeyStrokes(this.secondCollapseKeyStroke);
    }
  }
};

scout.SplitBox.prototype.getEffectiveSplitterPosition = function() {
  if (this.minSplitterPosition && this.fieldMinimized) {
    return this.minSplitterPosition;
  } else {
    return this.splitterPosition;
  }
};

scout.SplitBox.prototype.setMinSplitterPosition = function(minSplitterPosition) {
  this.setProperty('minSplitterPosition', minSplitterPosition);
  this._updateCollapseHandleButtons();
};

scout.SplitBox.prototype._renderMinSplitterPosition = function() {
  // minimum splitter position is considered automatically when layout is updated
  if (this.rendered) { // don't invalidate layout on initial rendering
    this.htmlSplitArea.invalidateLayoutTree(false);
  }
};

scout.SplitBox.prototype.setFieldMinimized = function(minimized) {
  this.setProperty('fieldMinimized', minimized);
  this._updateCollapseHandleButtons();
};

scout.SplitBox.prototype._renderFieldMinimized = function() {
  if (this.firstField) {
    this.firstField.$container.removeClass('minimized');
  }
  if (this.secondField) {
    this.secondField.$container.removeClass('minimized');
  }
  if (this.collapsibleField && this.fieldMinimized) {
    this.collapsibleField.$container.addClass('minimized');
  }

  // field minimized state is considered automatically when layout is updated
  if (this.rendered) { // don't invalidate layout on initial rendering
    this.htmlSplitArea.invalidateLayoutTree(false);
  }
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

scout.SplitBox.prototype._setToggleCollapseKeyStroke = function(keyStroke) {
  if (keyStroke) {
    if (this.toggleCollapseKeyStroke instanceof scout.KeyStroke) {
      this.unregisterKeyStrokes(this.collapseKeyStroke);
    }
    this.toggleCollapseKeyStroke = new scout.SplitBoxCollapseKeyStroke(this, keyStroke);
    if (this._collapseHandle) {
      this.registerKeyStrokes(this.toggleCollapseKeyStroke);
    }
  }
};

scout.SplitBox.prototype._setFirstCollapseKeyStroke = function(keyStroke) {
  if (keyStroke) {
    if (this.firstCollapseKeyStroke instanceof scout.KeyStroke) {
      this.unregisterKeyStrokes(this.firstCollapseKeyStroke);
    }
    this.firstCollapseKeyStroke = new scout.SplitBoxFirstCollapseKeyStroke(this, keyStroke);
    if (this._collapseHandle) {
      this.registerKeyStrokes(this.firstCollapseKeyStroke);
    }
  }
};

scout.SplitBox.prototype._setSecondCollapseKeyStroke = function(keyStroke) {
  if (keyStroke) {
    if (this.secondCollapseKeyStroke instanceof scout.KeyStroke) {
      this.unregisterKeyStrokes(this.secondCollapseKeyStroke);
    }
    this.secondCollapseKeyStroke = new scout.SplitBoxSecondCollapseKeyStroke(this, keyStroke);
    if (this._collapseHandle) {
      this.registerKeyStrokes(this.secondCollapseKeyStroke);
    }
  }
};

scout.SplitBox.prototype._renderCollapseHandle = function() {
  if (this._collapseHandle) {
    this._collapseHandle.render();
  }
};

scout.SplitBox.prototype.newSplitterPosition = function(newSplitterPosition, updateFieldMinimizedState) {
  if (this._isSplitterPositionTypeRelative(this.splitterPositionType)) {
    // Ensure range 0..1
    newSplitterPosition = Math.max(0, Math.min(1, newSplitterPosition));
  } else {
    // Ensure not negative
    newSplitterPosition = Math.max(0, newSplitterPosition);
  }

  // Ensure splitter within allowed range, toggle field minimized state if new splitter position is within minimal range
  if (this.minSplitterPosition && this._isSplitterPositionInMinimalRange(newSplitterPosition)) {
    this.setFieldMinimized(true);
    return;
  }

  // Set new value (send to server if changed
  var positionChanged = (this.splitterPosition !== newSplitterPosition);
  this.splitterPosition = newSplitterPosition;

  if (positionChanged) {
    this.trigger('positionChange', {
      position: newSplitterPosition
    });
  }

  if (updateFieldMinimizedState) {
    this._updateFieldMinimized();
  }
  this._updateCollapseHandleButtons();

  // Mark layout as invalid
  this.htmlSplitArea.invalidateLayoutTree(false);
};

scout.SplitBox.prototype._updateFieldMinimized = function() {
  if (this.minSplitterPosition) {
    this.setFieldMinimized(this._isSplitterPositionInMinimalRange(this.splitterPosition));
  } else {
    this.setFieldMinimized(false);
  }
};

scout.SplitBox.prototype._isSplitterPositionInMinimalRange = function(newSplitterPosition) {
  if (!this.minSplitterPosition) {
    return false;
  }
  return newSplitterPosition <= this.minSplitterPosition;
};

scout.SplitBox.prototype.toggleFieldCollapsed = function() {
  this.setFieldCollapsed(!this.fieldCollapsed);
};

scout.SplitBox.prototype.collapseHandleButtonPressed = function(event) {
  var collapsed = this.fieldCollapsed,
    minimized = this.fieldMinimized,
    minimizable = !!this.minSplitterPosition,
    positionTypeFirstField = ((this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_RELATIVE_FIRST) || (this.splitterPositionType === scout.SplitBox.SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST)),
    increaseField = (!!event.left && !positionTypeFirstField) || (!!event.right && positionTypeFirstField);

  if ((positionTypeFirstField && this.collapsibleField === this.secondField) || (!positionTypeFirstField && this.collapsibleField === this.firstField)) {
    // Splitter is not positioned according (absolute or relative) to collapsible field
    // - Mode toggles to increase collapsible field size: field collapsed --> field default --> field minimized
    // - Mode toggles to decrease collapsible field size: field collapsed <-- field default <-- field minimized
    if (increaseField) {
      if (collapsed) {
        // not possible, button is not visible (field is collapsed and cannot further increase its size)
      } else if (minimized && minimizable) {
        this.setFieldMinimized(false);
      } else {
        this.setFieldCollapsed(true);
      }
    } else {
      if (collapsed) {
        this.setFieldCollapsed(false);
      } else if (minimized) {
        // not possible, button is not visible (field is minimized and cannot further decrease its size)
      } else if (minimizable) {
        this.setFieldMinimized(true);
      }
    }
  } else {
    // Splitter is positioned according (absolute or relative) to collapsible field
    // - Mode toggles to increase collapsible field size: field collapsed --> field minimized --> field default
    // - Mode toggles to decrease collapsible field size: field collapsed <-- field minimized <-- field default
    if (increaseField) {
      if (collapsed) {
        this.setFieldCollapsed(false);
      } else if (minimized) {
        this.setFieldMinimized(false);
      } else {
        // not possible, button is not visible (field has default size and cannot further increase its size)
      }
    } else {
      if (collapsed) {
        // not possible, button is not visible (field is collapsed and cannot further decrease its size)
      } else if (minimized || !minimizable) {
        this.setFieldCollapsed(true);
      } else {
        this.setFieldMinimized(true);
      }
    }
  }
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
