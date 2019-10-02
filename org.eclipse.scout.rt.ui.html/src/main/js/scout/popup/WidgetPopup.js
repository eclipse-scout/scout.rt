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
scout.WidgetPopup = function() {
  scout.WidgetPopup.parent.call(this);
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
};
scout.inherits(scout.WidgetPopup, scout.Popup);

scout.WidgetPopup.prototype._createLayout = function() {
  return new scout.WidgetPopupLayout(this);
};

scout.WidgetPopup.prototype._init = function(model) {
  scout.WidgetPopup.parent.prototype._init.call(this, model);
  this._setClosable(this.closable);
};

scout.WidgetPopup.prototype._render = function() {
  scout.WidgetPopup.parent.prototype._render.call(this);
  this.$container.addClass('widget-popup');
};

scout.WidgetPopup.prototype._renderProperties = function() {
  scout.WidgetPopup.parent.prototype._renderProperties.call(this);
  this._renderWidget();
  this._renderClosable();
  this._renderMovable();
  this._renderResizable();
};

scout.WidgetPopup.prototype._remove = function() {
  this.$dragHandle = null;
  scout.WidgetPopup.parent.prototype._remove.call(this);
};

scout.WidgetPopup.prototype.setWidget = function(widget) {
  this.setProperty('widget', widget);
};

scout.WidgetPopup.prototype._renderWidget = function() {
  if (!this.widget) {
    return;
  }
  this.widget.render();
  this.widget.$container.addClass('popup-widget');
  this.invalidateLayoutTree();
};

scout.WidgetPopup.prototype.setClosable = function(closable) {
  this.setProperty('closable', closable);
};

scout.WidgetPopup.prototype._setClosable = function(closable) {
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
};

scout.WidgetPopup.prototype._createCloseAction = function() {
  return scout.create('Action', {
    parent: this,
    cssClass: 'close-action',
    iconId: scout.icons.REMOVE
  });
};

scout.WidgetPopup.prototype._renderClosable = function() {
  if (this.closeAction) {
    this.closeAction.render();
  }
};

scout.WidgetPopup.prototype._onCloseAction = function() {
  this.close();
};

scout.WidgetPopup.prototype.setResizable = function(resizable) {
  this.setProperty('resizable', resizable);
};

scout.WidgetPopup.prototype._renderResizable = function() {
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
};

scout.WidgetPopup.prototype._onResize = function(event) {
  var autoSizeOrig = this.htmlComp.layout.autoSize;
  this.htmlComp.layout.autoSize = false;
  this.htmlComp.revalidateLayout();
  this.htmlComp.layout.autoSize = autoSizeOrig;
  return false;
};

scout.WidgetPopup.prototype.setMovable = function(movable) {
  this.setProperty('movable', movable);
};

scout.WidgetPopup.prototype._renderMovable = function() {
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
};

scout.WidgetPopup.prototype._onMove = function(newOffset) {
  // Disable automatic positioning as soon as the user drags the popup
  if (this._autoPositionOrig === null) {
    this._autoPositionOrig = this.htmlComp.layout.autoPosition;
    this.htmlComp.layout.autoPosition = false;
  }
  this.trigger('move', newOffset);
};

scout.WidgetPopup.prototype.set$Anchor = function($anchor) {
  if (this._autoPositionOrig && $anchor && this.$anchor !== $anchor) {
    // If a new anchor is set, the popup is positioned automatically -> reset flag to make animation work
    this.htmlComp.layout.autoPosition = this._autoPositionOrig;
    this._autoPositionOrig = null;
  }
  scout.WidgetPopup.parent.prototype.set$Anchor.call(this, $anchor);
};
