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
import {icons} from '../index';
import {Popup} from '../index';
import {WidgetPopupLayout} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';

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
  this.widget = null;
  this.windowPaddingX = 0;
  this.windowPaddingY = 0;
  this.windowResizeType = 'layoutAndPosition';
  this.$dragHandle = null;
  this._moveHandler = this._onMove.bind(this);
  this._resizeHandler = this._onResize.bind(this);
  this._autoPositionOrig = null;
  this._addWidgetProperties(['widget']);
}


_createLayout() {
  return new WidgetPopupLayout(this);
}

_init(model) {
  super._init( model);
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
    cssClass: 'close-action',
    iconId: icons.REMOVE
  });
}

_renderClosable() {
  if (this.closeAction) {
    this.closeAction.render();
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
      .resizable()
      .on('resize', this._resizeHandler);
  } else {
    this.$container
      .unresizable()
      .off('resize', this._resizeHandler);
    this.invalidateLayoutTree(); // Resize popup to preferred size
  }
}

_onResize(event) {
  var autoSizeOrig = this.htmlComp.layout.autoSize;
  this.htmlComp.layout.autoSize = false;
  this.htmlComp.revalidateLayout();
  this.htmlComp.layout.autoSize = autoSizeOrig;
  return false;
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
  super.set$Anchor( $anchor);
}
}
