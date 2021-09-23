/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {icons, Insets, Popup, Resizable, scout, WidgetPopupLayout} from '../index';
import $ from 'jquery';
import graphics from '../layout/graphics';

export default class WidgetPopup extends Popup {

  constructor() {
    super();
    this.animateOpening = true;
    this.animateResize = true;
    this.animateRemoval = true;
    this.closable = false;
    this.closeAction = null;
    this.movable = false;
    this.resizable = false;
    this.resizeModes = null;
    this.minSize = null;
    this.widget = null;
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
    this.windowResizeType = 'layoutAndPosition';
    this.$dragHandle = null;
    // Action bar on the top right corner, is used to display the close action but may also be used by popups to add custom actions.
    // The bar will only be added automatically if the popup is closable.
    this.$actions = null;
    this._moveHandler = this._onMove.bind(this);
    this._resizeHandler = this._onResize.bind(this);
    this._autoPositionOrig = null;
    this._addWidgetProperties(['widget']);
  }

  _createLayout() {
    return new WidgetPopupLayout(this);
  }

  _init(model) {
    super._init(model);
    this._setClosable(this.closable);
  }

  _render() {
    super._render();
    this.$container.addClass('widget-popup');
  }

  _renderProperties() {
    super._renderProperties();
    this._renderWidget();
    this._renderClosable();
    this._renderMovable();
    this._renderResizable();
  }

  _remove() {
    this.$dragHandle = null;
    this.$actions = null;
    super._remove();
  }

  setWidget(widget) {
    this.setProperty('widget', widget);
  }

  _renderWidget() {
    if (!this.widget) {
      return;
    }
    this.widget.render();
    this.widget.$container.addClass('popup-widget');
    this.invalidateLayoutTree();
  }

  setClosable(closable) {
    this.setProperty('closable', closable);
  }

  _setClosable(closable) {
    this._setProperty('closable', closable);
    if (this.closable) {
      if (this.closeAction) {
        return;
      }
      this.closeAction = this._createCloseAction();
      this.closeAction.on('action', this._onCloseAction.bind(this));
    } else {
      if (!this.closeAction) {
        return;
      }
      this.closeAction.destroy();
      this.closeAction = null;
    }
  }

  _createCloseAction() {
    return scout.create('Action', {
      parent: this,
      cssClass: 'close-action menu-item',
      iconId: icons.REMOVE
    });
  }

  _renderClosable() {
    if (this.closeAction) {
      if (!this.$actions) {
        this.$actions = this.$container.appendDiv('actions');
      }
      this.closeAction.render(this.$actions);
    } else {
      if (this.$actions && this.$actions.children().length === 0) {
        this.$actions.remove();
        this.$actions = null;
      }
    }
  }

  _onCloseAction() {
    this.close();
  }

  setResizable(resizable) {
    this.setProperty('resizable', resizable);
  }

  _renderResizable() {
    if (this.resizable) {
      this.$container
        .resizable({modes: this._determineResizeModes(), $parent: this.$container, boundaries: this._calculateResizeBoundaries()})
        .on('resize', this._resizeHandler);
    } else {
      this.$container
        .unresizable()
        .off('resize', this._resizeHandler);
      this.invalidateLayoutTree(); // Resize popup to preferred size
    }
  }

  _calculateResizeBoundaries() {
    let resizeBoundaries = new Insets();
    if (!this.$arrow) {
      return resizeBoundaries;
    }
    let anchorMiddle = this._getAnchorMiddlePoint();
    if (!anchorMiddle) {
      return resizeBoundaries;
    }
    let arrowSize = graphics.size(this.$arrow);
    if (this._isVerticallyAligned()) {
      resizeBoundaries.top = anchorMiddle.y - arrowSize.height;
      resizeBoundaries.bottom = anchorMiddle.y + arrowSize.height;
    } else {
      resizeBoundaries.left = anchorMiddle.x - arrowSize.width;
      resizeBoundaries.right = anchorMiddle.x + arrowSize.width;
    }
    return resizeBoundaries;
  }

  _determineResizeModes() {
    if (!this.$arrow || this.resizeModes) {
      return this.resizeModes;
    }
    if (this.calculatedVerticalAlignment === Popup.Alignment.TOP) {
      return [Resizable.MODES.WEST, Resizable.MODES.EAST, Resizable.MODES.NORTH];
    }
    if (this.calculatedVerticalAlignment === Popup.Alignment.BOTTOM) {
      return [Resizable.MODES.WEST, Resizable.MODES.EAST, Resizable.MODES.SOUTH];
    }
    if (this.calculatedHorizontalAlignment === Popup.Alignment.LEFT) {
      return [Resizable.MODES.NORTH, Resizable.MODES.SOUTH, Resizable.MODES.WEST];
    }
    if (this.calculatedHorizontalAlignment === Popup.Alignment.RIGHT) {
      return [Resizable.MODES.NORTH, Resizable.MODES.SOUTH, Resizable.MODES.EAST];
    }
    return this.resizeModes;
  }

  _onResize(event) {
    let autoSizeOrig = this.htmlComp.layout.autoSize;
    this.htmlComp.layout.autoSize = false;
    this.htmlComp.revalidateLayout();
    this.htmlComp.layout.autoSize = autoSizeOrig;
    this._updateArrowPosition();
    return false;
  }

  /**
   * @Override
   */
  position(switchIfNecessary) {
    if (!this.rendered) {
      return;
    }
    super.position(switchIfNecessary);
    if (this.resizable) {
      let resizable = this.$container.data('resizable');
      resizable.setBoundaries(this._calculateResizeBoundaries());
      resizable.setModes(this._determineResizeModes());
      this._updateArrowPosition();
    }
  }

  /**
   * @Override
   */
  _updateArrowClass(verticalAlignment, horizontalAlignment) {
    super._updateArrowClass(verticalAlignment, horizontalAlignment);
    if (this.$arrow) {
      // make sure to remove css positioning which could have been applied in _updateArrowPosition()
      // otherwise the original positioning functions of popup.js may not work correctly anymore
      this.$arrow.cssLeft(null);
      this.$arrow.cssTop(null);
    }
  }

  _updateArrowPosition() {
    if (!this.$arrow) {
      return;
    }
    let anchorMiddlePoint = this._getAnchorMiddlePoint();
    if (!anchorMiddlePoint) {
      return;
    }
    this.$arrow.removeClass('leftedge rightedge topedge bottomedge');
    let arrowSize = graphics.size(this.$arrow);
    let arrowMargins = graphics.margins(this.$arrow);
    if (this._isVerticallyAligned()) {
      let verticalMarginShift = arrowMargins.top + arrowMargins.bottom;
      this.$arrow.cssTop(anchorMiddlePoint.y - (verticalMarginShift + this.$container.position().top + (arrowSize.height / 2)));
    } else {
      let horizontalMarginShift = arrowMargins.left + arrowMargins.right;
      this.$arrow.cssLeft(anchorMiddlePoint.x - (horizontalMarginShift + this.$container.position().left + (arrowSize.width / 2)));
    }
  }

  _getAnchorMiddlePoint() {
    let anchorBounds = this.getAnchorBounds();
    return anchorBounds ? anchorBounds.center() : null;
  }

  _isVerticallyAligned() {
    return scout.isOneOf(this.calculatedHorizontalAlignment, Popup.Alignment.LEFT, Popup.Alignment.RIGHT);
  }

  setMovable(movable) {
    this.setProperty('movable', movable);
  }

  _renderMovable() {
    if (this.movable) {
      if (this.$dragHandle) {
        return;
      }
      this.$dragHandle = this.$container.prependDiv('drag-handle');
      this.$container.draggable(this.$dragHandle, $.throttle(this._moveHandler, 1000 / 60)); // 60fps
    } else {
      if (!this.$dragHandle) {
        return;
      }
      this.$container.undraggable(this.$dragHandle);
      this.$dragHandle.remove();
      this.$dragHandle = null;
      this.htmlComp.layout.autoPosition = this._autoPositionOrig;
      this._autoPositionOrig = null;
      this.invalidateLayoutTree(); // move popup to preferred position
    }
  }

  _onMove(newOffset) {
    // Disable automatic positioning as soon as the user drags the popup
    if (this._autoPositionOrig === null) {
      this._autoPositionOrig = this.htmlComp.layout.autoPosition;
      this.htmlComp.layout.autoPosition = false;
    }
    this.trigger('move', newOffset);
  }

  set$Anchor($anchor) {
    if (this._autoPositionOrig && $anchor && this.$anchor !== $anchor) {
      // If a new anchor is set, the popup is positioned automatically -> reset flag to make animation work
      this.htmlComp.layout.autoPosition = this._autoPositionOrig;
      this._autoPositionOrig = null;
    }
    super.set$Anchor($anchor);
  }
}
