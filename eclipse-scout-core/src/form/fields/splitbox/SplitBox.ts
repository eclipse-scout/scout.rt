/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  CollapseHandle, CollapseHandleHorizontalAlignment, CompositeField, Dimension, EnumObject, FormField, graphics, GroupBox, HtmlComponent, HtmlEnvironment, InitModelOf, KeyStroke, ObjectUuidProvider, PropertyChangeEvent, scout,
  SplitBoxCollapseKeyStroke, SplitBoxEventMap, SplitBoxFirstCollapseKeyStroke, SplitBoxLayout, SplitBoxModel, SplitBoxSecondCollapseKeyStroke
} from '../../../index';
import $ from 'jquery';

export class SplitBox extends CompositeField {
  declare model: SplitBoxModel;
  declare eventMap: SplitBoxEventMap;
  declare self: SplitBox;

  firstField: FormField;
  secondField: FormField;
  collapsibleField: FormField;
  fieldCollapsed: boolean;
  toggleCollapseKeyStroke: SplitBoxCollapseKeyStroke;
  firstCollapseKeyStroke: SplitBoxFirstCollapseKeyStroke;
  secondCollapseKeyStroke: SplitBoxSecondCollapseKeyStroke;
  splitHorizontal: boolean;
  splitterEnabled: boolean;
  splitterPosition: number;
  minSplitterPosition: number;
  splitterPositionType: SplitBoxSplitterPositionType;
  fieldMinimized: boolean;
  minimizeEnabled: boolean;
  htmlSplitArea: HtmlComponent;
  collapseHandle: CollapseHandle;

  protected _oldSplitterPositionType: string;
  protected _$splitArea: JQuery;
  protected _$splitter: JQuery;
  protected _$window: JQuery<Window>;
  protected _$body: JQuery<Body>;

  constructor() {
    super();
    this._addWidgetProperties(['firstField', 'secondField', 'collapsibleField']);
    this._addPreserveOnPropertyChangeProperties(['collapsibleField']);

    this.firstField = null;
    this.secondField = null;
    this.collapsibleField = null;
    this.fieldCollapsed = false;
    this.toggleCollapseKeyStroke = null;
    this.firstCollapseKeyStroke = null;
    this.secondCollapseKeyStroke = null;
    this.splitHorizontal = true;
    this.splitterEnabled = true;
    this.splitterPosition = 0.5;
    this.minSplitterPosition = 0;
    this.splitterPositionType = SplitBox.SplitterPositionType.RELATIVE_FIRST;
    this.fieldMinimized = false;
    this.minimizeEnabled = true;
    this._$splitArea = null;
    this._$splitter = null;
  }

  static SplitterPositionType = {
    RELATIVE_FIRST: 'relativeFirst',
    RELATIVE_SECOND: 'relativeSecond',
    ABSOLUTE_FIRST: 'absoluteFirst',
    ABSOLUTE_SECOND: 'absoluteSecond'
  } as const;

  /** @deprecated use SplitBox.SplitterPositionType instead */
  static SPLITTER_POSITION_TYPE_RELATIVE_FIRST = SplitBox.SplitterPositionType.RELATIVE_FIRST;
  /** @deprecated use SplitBox.SplitterPositionType instead */
  static SPLITTER_POSITION_TYPE_RELATIVE_SECOND = SplitBox.SplitterPositionType.RELATIVE_SECOND;
  /** @deprecated use SplitBox.SplitterPositionType instead */
  static SPLITTER_POSITION_TYPE_ABSOLUTE_FIRST = SplitBox.SplitterPositionType.ABSOLUTE_FIRST;
  /** @deprecated use SplitBox.SplitterPositionType instead */
  static SPLITTER_POSITION_TYPE_ABSOLUTE_SECOND = SplitBox.SplitterPositionType.ABSOLUTE_SECOND;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setToggleCollapseKeyStroke(model.toggleCollapseKeyStroke);
    this._setFirstCollapseKeyStroke(model.firstCollapseKeyStroke);
    this._setSecondCollapseKeyStroke(model.secondCollapseKeyStroke);
    this._updateCollapseHandle();
    this._initResponsive();
  }

  /**
   * Set the group boxes of the split box to responsive if not set otherwise.
   */
  protected _initResponsive() {
    this.getFields().forEach(field => {
      if (field instanceof GroupBox && field.responsive === null) {
        field.setResponsive(true);
      }
    });
  }

  protected override _render() {
    this.addContainer(this.$parent, 'split-box');
    // This widget does not support label, mandatoryIndicator and status

    // Create split area
    this._$splitArea = this.$parent.makeDiv('split-area');
    this.addField(this._$splitArea);
    this.htmlSplitArea = HtmlComponent.install(this._$splitArea, this.session);
    this.htmlSplitArea.setLayout(new SplitBoxLayout(this));
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
        this.secondField.render(this._$splitArea);
        this.secondField.$container
          .addClass('second-field')
          .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
        this.secondField.on('propertyChange', onInnerFieldPropertyChange.bind(this));

        this._$splitter = this._$splitArea.appendDiv('splitter')
          .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis')
          .on('mousedown', resizeSplitter.bind(this));
      }
    }
    this._updateFieldVisibilityClasses();

    // --- Helper functions ---

    function resizeSplitter(event: JQuery.MouseDownEvent): boolean {
      if (event.which !== 1) {
        return; // only handle left mouse button
      }
      let mousePosition: { x: number; y: number },
        splitAreaPosition: JQuery.Coordinates,
        splitAreaSize: Dimension,
        splitterSize: Dimension,
        splitterPosition: JQuery.Coordinates,
        $tempSplitter: JQuery;
      if (this.splitterEnabled) {
        // Update mouse position (see resizeMove() for details)
        mousePosition = {
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
        splitAreaPosition = this._$splitArea.offset();
        splitAreaSize = graphics.size(this._$splitArea, true);
        splitterPosition = this._$splitter.offset();
        splitterSize = graphics.size(this._$splitter, true);

        // Create temporary splitter
        $tempSplitter = this._$splitArea.appendDiv('temp-splitter')
          .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
        if (this.splitHorizontal) { // "|"
          $tempSplitter.cssLeft(splitterPosition.left - splitAreaPosition.left);
        } else { // "--"
          $tempSplitter.cssTop(splitterPosition.top - splitAreaPosition.top);
        }
        this._$splitter.addClass('dragging');
      }

      let newSplitterPosition: number = this.splitterPosition;
      let SNAP_SIZE = 10;

      function resizeMove(event: JQuery.MouseMoveEvent) {
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
          let targetSplitterPositionLeft = event.pageX - splitAreaPosition.left;

          // De-normalize minimum splitter position to allowed splitter range in pixel [minSplitterPositionLeft, maxSplitterPositionLeft]
          let minSplitterPositionLeft: number;
          let maxSplitterPositionLeft: number;

          // Splitter width plus margin on right side, if temporary splitter position is x, the splitter div position is x-splitterOffset
          let splitterOffset = Math.floor((splitterSize.width + HtmlEnvironment.get().fieldMandatoryIndicatorWidth) / 2);

          if (this.splitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_FIRST) {
            minSplitterPositionLeft = scout.nvl(this.minSplitterPosition, 0);
            // allow to move the splitter to right side, leaving minimal space for splitter div without right margin (=total splitter size minus offset)
            maxSplitterPositionLeft = splitAreaSize.width - splitterSize.width + splitterOffset;
          } else if (this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_FIRST) {
            minSplitterPositionLeft = (splitAreaSize.width - splitterSize.width) * scout.nvl(this.minSplitterPosition, 0);
            // allow to move the splitter to right side, leaving minimal space for splitter div without right margin (=total splitter size minus offset)
            maxSplitterPositionLeft = splitAreaSize.width - splitterSize.width + splitterOffset;
          } else if (this.splitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_SECOND) {
            minSplitterPositionLeft = 0;
            // allow to move the splitter to right side, leaving minimal space for splitter div without right margin, reserving space for minimum splitter size
            maxSplitterPositionLeft = splitAreaSize.width - splitterSize.width + splitterOffset - scout.nvl(this.minSplitterPosition, 0);
          } else if (this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_SECOND) {
            minSplitterPositionLeft = 0;
            // allow to move the splitter to right side, leaving minimal space for splitter div without right margin, reserving space for minimum splitter size
            maxSplitterPositionLeft = splitAreaSize.width - splitterSize.width + splitterOffset - Math.floor(scout.nvl(this.minSplitterPosition, 0) * (splitAreaSize.width - splitterSize.width));
          }

          // Snap to begin and end
          let tempSplitterOffsetX = splitterOffset;

          if (targetSplitterPositionLeft < (minSplitterPositionLeft + splitterOffset + SNAP_SIZE)) { // snap left if minimum position is reached (+ snap range)
            targetSplitterPositionLeft = minSplitterPositionLeft; // set splitter directly to left minimal bound
            tempSplitterOffsetX = 0; // setting splitter to left minimal bound, does not require an additional offset
          } else if (targetSplitterPositionLeft > (maxSplitterPositionLeft - SNAP_SIZE)) {
            targetSplitterPositionLeft = maxSplitterPositionLeft;
          }

          // Update temporary splitter
          $tempSplitter.cssLeft(targetSplitterPositionLeft - tempSplitterOffsetX);

          // Normalize target position (available splitter area is (splitAreaSize.width - splitterSize.width))
          newSplitterPosition = (targetSplitterPositionLeft - tempSplitterOffsetX);
          if (this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_FIRST) {
            newSplitterPosition = newSplitterPosition / (splitAreaSize.width - splitterSize.width);
          } else if (this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_SECOND) {
            newSplitterPosition = 1 - (newSplitterPosition / (splitAreaSize.width - splitterSize.width));
          } else if (this.splitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_SECOND) {
            newSplitterPosition = splitAreaSize.width - splitterSize.width - newSplitterPosition;
          }
        } else { // "--"
          // Calculate target splitter position (in area)
          let targetSplitterPositionTop = event.pageY - splitAreaPosition.top;

          // Snap to begin and end
          let tempSplitterOffsetY = Math.floor(splitterSize.height / 2);
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
          if (this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_FIRST) {
            newSplitterPosition = newSplitterPosition / (splitAreaSize.height - splitterSize.height);
          } else if (this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_SECOND) {
            newSplitterPosition = 1 - (newSplitterPosition / (splitAreaSize.height - splitterSize.height));
          } else if (this.splitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_SECOND) {
            newSplitterPosition = splitAreaSize.height - newSplitterPosition - splitterSize.height;
          }
        }
      }

      function resizeEnd(event: JQuery.MouseUpEvent) {
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

    function onInnerFieldPropertyChange(event: PropertyChangeEvent<any, FormField>) {
      if (event.propertyName === 'visible') {
        this._updateFieldVisibilityClasses();
        // Mark layout as invalid
        this.htmlSplitArea.invalidateLayoutTree(false);
      }
    }
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSplitterPosition();
    this._renderSplitterEnabled();
    this._renderCollapsibleField(); // renders collapsibleField _and_ fieldCollapsed
    this._renderCollapseHandle(); // renders collapseHandle _and_ toggleCollapseKeyStroke _and_ firstCollapseKeyStroke _and_ secondCollapseKeyStroke
    this._renderFieldMinimized();
  }

  protected override _remove() {
    this._$splitArea = null;
    this._$splitter = null;
    super._remove();
  }

  protected _setSplitterPosition(splitterPosition: number) {
    this._setProperty('splitterPosition', splitterPosition);
    // If splitter position is explicitly set by an event, no recalculation is necessary
    this._oldSplitterPositionType = null;
  }

  protected _renderSplitterPosition() {
    this.newSplitterPosition(this.splitterPosition, false); // do not update (override) field minimized if new position is set by model
  }

  protected _setSplitterPositionType(splitterPositionType: string) {
    if (this.rendered && !this._oldSplitterPositionType) {
      this._oldSplitterPositionType = this.splitterPositionType;
      // We need to recalculate the splitter position. Because this requires the proper
      // size of the split box, this can only be done in _renderSplitterPositionType().
    }
    this._setProperty('splitterPositionType', splitterPositionType);
  }

  protected _renderSplitterPositionType() {
    if (this._oldSplitterPositionType) {
      // splitterPositionType changed while the split box was rendered --> convert splitterPosition
      // to the target type such that the current position in screen does not change.
      let splitAreaSize = this.htmlSplitArea.size(),
        splitterPosition = this.splitterPosition,
        splitterSize = graphics.size(this._$splitter, true),
        minSplitterPosition = this.minSplitterPosition,
        totalSize = 0;
      if (this.splitHorizontal) { // "|"
        totalSize = splitAreaSize.width - splitterSize.width;
      } else { // "--"
        totalSize = splitAreaSize.height - splitterSize.height;
      }

      // Convert value depending on the old and new type system
      let oldIsRelative = this._isSplitterPositionTypeRelative(this._oldSplitterPositionType);
      let newIsRelative = this._isSplitterPositionTypeRelative(this.splitterPositionType);
      let oldIsAbsolute = !oldIsRelative;
      let newIsAbsolute = !newIsRelative;
      if (oldIsRelative && newIsAbsolute) {
        // From relative to absolute
        if ((this._oldSplitterPositionType === SplitBox.SplitterPositionType.RELATIVE_FIRST && this.splitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_SECOND) ||
          (this._oldSplitterPositionType === SplitBox.SplitterPositionType.RELATIVE_SECOND && this.splitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_FIRST)) {
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
        if ((this._oldSplitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_FIRST && this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_SECOND) ||
          (this._oldSplitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_SECOND && this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_FIRST)) {
          splitterPosition = (totalSize - splitterPosition) / totalSize; // changed from first to second field or from second to first field, invert splitter position
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
  }

  protected _isSplitterPositionTypeRelative(positionType: string): boolean {
    return (positionType === SplitBox.SplitterPositionType.RELATIVE_FIRST)
      || (positionType === SplitBox.SplitterPositionType.RELATIVE_SECOND);
  }

  protected _renderSplitterEnabled() {
    if (this._$splitter) {
      this._$splitter.setEnabled(this.splitterEnabled);
    }
  }

  setFieldCollapsed(collapsed: boolean) {
    this.setProperty('fieldCollapsed', collapsed);
    this._updateCollapseHandleButtons();
  }

  protected _renderFieldCollapsed() {
    this._renderCollapsibleField();
  }

  setCollapsibleField(field: FormField) {
    this.setProperty('collapsibleField', field);
    this._updateCollapseHandle();
  }

  protected _updateCollapseHandle() {
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
      let horizontalAlignment: CollapseHandleHorizontalAlignment = CollapseHandle.HorizontalAlignment.LEFT;
      if (this.collapsibleField !== this.firstField) {
        horizontalAlignment = CollapseHandle.HorizontalAlignment.RIGHT;
      }

      if (!this.collapseHandle) {
        // create new collapse handle
        this.collapseHandle = scout.create(CollapseHandle, {
          parent: this,
          horizontalAlignment: horizontalAlignment
        });
        this.collapseHandle.on('action', this.collapseHandleButtonPressed.bind(this));
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
        this.collapseHandle.setHorizontalAlignment(horizontalAlignment);
      }

      this._updateCollapseHandleButtons();
    } else {
      if (this.collapseHandle) {
        this.collapseHandle.destroy();
        this.collapseHandle = null;
      }
    }
  }

  protected _updateCollapseHandleButtons() {
    if (!this.collapseHandle) {
      return;
    }
    let leftVisible: boolean, rightVisible: boolean,
      collapsed = this.fieldCollapsed,
      minimized = this.fieldMinimized,
      minimizable = this._isMinimizable(),
      positionTypeFirstField = ((this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_FIRST) || (this.splitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_FIRST)),
      positionNotAccordingCollapsibleField = (positionTypeFirstField && this.collapsibleField === this.secondField) || (!positionTypeFirstField && this.collapsibleField === this.firstField);

    if (positionTypeFirstField) {
      if (positionNotAccordingCollapsibleField) {
        leftVisible = (!minimized && minimizable) || collapsed; // left = decrease collapsible field size. Decrease field in this order [minimized <- default <- collapsed]
        rightVisible = !collapsed; // right = increase collapsible field size. Increase field in this order [minimized -> default -> collapsed]
      } else {
        leftVisible = !collapsed; // left = increase collapsible field size. Increase field in this order [default <- minimized <- collapsed]
        rightVisible = collapsed || (minimized && minimizable); // right = decrease collapsible field size. Decrease field in this order [default -> minimized -> collapsed]
      }
    } else {
      if (positionNotAccordingCollapsibleField) {
        leftVisible = !collapsed; // left = decrease collapsible field size. Decrease field in this order [collapsed <- default <- minimized]
        rightVisible = (!minimized && minimizable) || collapsed; // right = increase collapsible field size. Increase field in this order [collapsed -> default -> minimized]
      } else {
        leftVisible = collapsed || (minimized && minimizable); // left = decrease collapsible field size. Decrease field in this order [collapsed <- minimized <- default]
        rightVisible = !collapsed; // right = increase collapsible field size. Increase field in this order [collapsed -> minimized -> default]
      }
    }

    this.collapseHandle.setLeftVisible(leftVisible);
    this.collapseHandle.setRightVisible(rightVisible);

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
  }

  getEffectiveSplitterPosition(): number {
    if (this._isMinimizable() && this.fieldMinimized) {
      return this.minSplitterPosition;
    }
    return this.splitterPosition;
  }

  setMinSplitterPosition(minSplitterPosition: number) {
    this.setProperty('minSplitterPosition', minSplitterPosition);
    this._updateCollapseHandleButtons();
  }

  protected _renderMinSplitterPosition() {
    // minimum splitter position is considered automatically when layout is updated
    if (this.rendered) { // don't invalidate layout on initial rendering
      this.htmlSplitArea.invalidateLayoutTree(false);
    }
  }

  setFieldMinimized(minimized: boolean) {
    this.setProperty('fieldMinimized', minimized);
    this._updateCollapseHandleButtons();
  }

  protected _renderFieldMinimized() {
    this.$container.removeClass('first-field-minimized second-field-minimized');
    if (this.firstField) {
      this.firstField.$container.removeClass('minimized');
    }
    if (this.secondField) {
      this.secondField.$container.removeClass('minimized');
    }
    if (this.collapsibleField && this.fieldMinimized) {
      this.collapsibleField.$container.addClass('minimized');
      this.$container.toggleClass('first-field-minimized', this.firstField === this.collapsibleField);
      this.$container.toggleClass('second-field-minimized', this.secondField === this.collapsibleField);
    }

    // field minimized state is considered automatically when layout is updated
    if (this.rendered) { // don't invalidate layout on initial rendering
      this.htmlSplitArea.invalidateLayoutTree(false);
    }
  }

  setMinimizeEnabled(enabled: boolean) {
    this.setProperty('minimizeEnabled', enabled);
    if (this._isMinimizable() && this._isSplitterPositionInMinimalRange(this.splitterPosition)) {
      this.setFieldMinimized(true);
    }

    this._updateCollapseHandleButtons();
  }

  protected _renderMinimizeEnabled() {
    // minimize enabled is considered automatically when layout is updated
    if (this.rendered) { // don't invalidate layout on initial rendering
      this.htmlSplitArea.invalidateLayoutTree(false);
    }
  }

  protected _isMinimizable(): boolean {
    return !!this.minSplitterPosition && this.minimizeEnabled;
  }

  protected _renderCollapsibleField() {
    this.$container.removeClass('first-field-collapsed second-field-collapsed');
    if (this.firstField) {
      this.firstField.$container.removeClass('collapsed');
    }
    if (this.secondField) {
      this.secondField.$container.removeClass('collapsed');
    }
    if (this.collapsibleField && this.fieldCollapsed) {
      this.collapsibleField.$container.addClass('collapsed');
      this.$container.toggleClass('first-field-collapsed', this.firstField === this.collapsibleField);
      this.$container.toggleClass('second-field-collapsed', this.secondField === this.collapsibleField);
    }
    if (this.rendered) { // don't invalidate layout on initial rendering
      this.htmlSplitArea.invalidateLayoutTree(false);
    }
  }

  protected _setToggleCollapseKeyStroke(keyStroke: string) {
    if (keyStroke) {
      if (this.toggleCollapseKeyStroke instanceof KeyStroke) {
        this.unregisterKeyStrokes(this.toggleCollapseKeyStroke);
      }
      this.toggleCollapseKeyStroke = new SplitBoxCollapseKeyStroke(this, keyStroke);
      if (this.collapseHandle) {
        this.registerKeyStrokes(this.toggleCollapseKeyStroke);
      }
    }
  }

  protected _setFirstCollapseKeyStroke(keyStroke: string) {
    if (keyStroke) {
      if (this.firstCollapseKeyStroke instanceof KeyStroke) {
        this.unregisterKeyStrokes(this.firstCollapseKeyStroke);
      }
      this.firstCollapseKeyStroke = new SplitBoxFirstCollapseKeyStroke(this, keyStroke);
      if (this.collapseHandle) {
        this.registerKeyStrokes(this.firstCollapseKeyStroke);
      }
    }
  }

  protected _setSecondCollapseKeyStroke(keyStroke: string) {
    if (keyStroke) {
      if (this.secondCollapseKeyStroke instanceof KeyStroke) {
        this.unregisterKeyStrokes(this.secondCollapseKeyStroke);
      }
      this.secondCollapseKeyStroke = new SplitBoxSecondCollapseKeyStroke(this, keyStroke);
      if (this.collapseHandle) {
        this.registerKeyStrokes(this.secondCollapseKeyStroke);
      }
    }
  }

  protected _renderCollapseHandle() {
    if (this.collapseHandle) {
      this.collapseHandle.render();
    }
  }

  newSplitterPosition(newSplitterPosition: number, updateFieldMinimizedState: boolean) {
    if (this._isSplitterPositionTypeRelative(this.splitterPositionType)) {
      // Ensure range 0..1
      newSplitterPosition = Math.max(0, Math.min(1, newSplitterPosition));
    } else {
      // Ensure not negative
      newSplitterPosition = Math.max(0, newSplitterPosition);
    }

    // Ensure splitter within allowed range, toggle field minimized state if new splitter position is within minimal range
    if (this._isMinimizable() && this._isSplitterPositionInMinimalRange(newSplitterPosition)) {
      this.setFieldMinimized(true);
      return;
    }

    // Set new value (send to server if changed
    let positionChanged = (this.splitterPosition !== newSplitterPosition);
    this.splitterPosition = newSplitterPosition;

    if (positionChanged) {
      this.trigger('positionChange', {
        position: newSplitterPosition
      });

      if (updateFieldMinimizedState) {
        this._updateFieldMinimized();
      }
    }

    this._updateCollapseHandleButtons();

    // Mark layout as invalid
    this.htmlSplitArea.invalidateLayoutTree(false);
  }

  protected _updateFieldMinimized() {
    if (this._isMinimizable()) {
      this.setFieldMinimized(this._isSplitterPositionInMinimalRange(this.splitterPosition));
    } else {
      this.setFieldMinimized(false);
    }
  }

  protected _isSplitterPositionInMinimalRange(newSplitterPosition: number): boolean {
    if (!this._isMinimizable()) {
      return false;
    }
    return newSplitterPosition <= this.minSplitterPosition;
  }

  toggleFieldCollapsed() {
    this.setFieldCollapsed(!this.fieldCollapsed);
  }

  collapseHandleButtonPressed(event: { left?: boolean; right?: boolean } /* CollapseHandleActionEvent */) {
    let collapsed = this.fieldCollapsed,
      minimized = this.fieldMinimized,
      minimizable = this._isMinimizable(),
      positionTypeFirstField = ((this.splitterPositionType === SplitBox.SplitterPositionType.RELATIVE_FIRST) || (this.splitterPositionType === SplitBox.SplitterPositionType.ABSOLUTE_FIRST)),
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
  }

  getFields(): FormField[] {
    let fields: FormField[] = [];
    if (this.firstField) {
      fields.push(this.firstField);
    }
    if (this.secondField) {
      fields.push(this.secondField);
    }
    return fields;
  }

  protected _updateFieldVisibilityClasses() {
    if (!this.rendered && !this.rendering) {
      return;
    }
    let hasFirstField = (this.firstField && this.firstField.visible);
    let hasSecondField = (this.secondField && this.secondField.visible);
    let hasTwoFields = hasFirstField && hasSecondField;
    let hasOneField = !hasTwoFields && (hasFirstField || hasSecondField);

    // Mark container if only one field is visible (i.e. there is no splitter)
    this.$container.toggleClass('single-field', hasOneField);
  }
}

export type SplitBoxSplitterPositionType = EnumObject<typeof SplitBox.SplitterPositionType>;

ObjectUuidProvider.UuidPathSkipWidgets.add(SplitBox);
