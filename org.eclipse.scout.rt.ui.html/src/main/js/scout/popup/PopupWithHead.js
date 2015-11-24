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
scout.PopupWithHead = function() {
  scout.PopupWithHead.parent.call(this);
  this.$head;
  this.$body;
  this.$deco;
  this._headVisible = true;
};
scout.inherits(scout.PopupWithHead, scout.Popup);

scout.PopupWithHead.prototype._createLayout = function() {
  return new scout.PopupWithHeadLayout(this);
};

scout.PopupWithHead.prototype._render = function($parent) {
  scout.PopupWithHead.parent.prototype._render.call(this, $parent);
  this.$body = this.$container.appendDiv('popup-body');
  if (this._headVisible) {
    this._renderHead();
  }
  this._modifyBody();
};

scout.PopupWithHead.prototype.rerenderHead = function() {
  this._removeHead();
  this._renderHead();
};

/**
 * Copies html from this.$headBlueprint, if set.
 */
scout.PopupWithHead.prototype._renderHead = function() {
  this.$deco = this.$container.makeDiv('popup-deco');
  this.$head = this.$container
    .makeDiv('popup-head')
    .on('mousedown', '', this._onHeadMouseDown.bind(this));
  this.$container
    .prepend(this.$head)
    .append(this.$deco);
  if (this.$headBlueprint) {
    this.$head.html(this.$headBlueprint.html());
    this._modifyHeadChildren();
  }
};

/**
 * Sets CSS classes or CSS-properties on the copied children in the head.
 */
scout.PopupWithHead.prototype._modifyHeadChildren = function() {
  // NOP
};

/**
 * Sets CSS classes or CSS-properties on the body.
 */
scout.PopupWithHead.prototype._modifyBody = function() {
  // NOP
};

scout.PopupWithHead.prototype._removeHead = function() {
  if (this.$head) {
    this.$head.remove();
  }
  if (this.$deco) {
    this.$deco.remove();
  }
};

scout.PopupWithHead.prototype._copyCssClassToHead = function(className) {
  if (this.$headBlueprint && this.$headBlueprint.hasClass(className)) {
    this.$head.addClass(className);
  }
};

scout.PopupWithHead.prototype._onHeadMouseDown = function(event) {
  if (this.$head && this.$head.isOrHas(event.target)) {
    this.close();
  }
};

/**
 * @override
 */
scout.PopupWithHead.prototype._isMouseDownOutside = function(event) {
  if (this.$headBlueprint && this.$headBlueprint.isOrHas(event.target)) {
    // click on the head still belongs to the popup -> not an outside click -> do not close popup
    return false;
  }

  return scout.PopupWithHead.parent.prototype._isMouseDownOutside.call(this, event);
};

scout.PopupWithHead.prototype.appendToBody = function($element) {
  this.$body.append($element);
};

scout.PopupWithHead.prototype.addClassToBody = function(clazz) {
  this.$body.addClass(clazz);
};

/**
 * @override Popup.js
 */
scout.PopupWithHead.prototype._position = function($container, switchIfNecessary) {
  var openingDirectionX, openingDirectionY, left, top, overlap, pos;
  if (!this._headVisible) {
    // If head is not visible, use default implementation
    scout.PopupWithHead.parent.prototype._position.call(this, $container);
    return;
  }
  this._positionImpl();

  switchIfNecessary = scout.nvl(switchIfNecessary, true);
  if (switchIfNecessary) {
    pos = $container.offset();
    overlap = this.overlap($container, {
      x: pos.left,
      y: pos.top
    });
    if (overlap.y > 0) {
      // switch opening direction
      openingDirectionY = 'up';
    }
    if (overlap.x > 0) {
      // switch opening direction
      openingDirectionX = 'left';
    }
    if (openingDirectionX || openingDirectionY) {
      // Align again if openingDirection has to be switched
      this._positionImpl(openingDirectionX, openingDirectionY);
    }
  }
};

scout.PopupWithHead.prototype._positionImpl = function(openingDirectionX, openingDirectionY) {
  var pos, headSize, bodySize, bodyWidth, widthDiff, subPixelCorr, $blueprintChildren, left, top, headInsets, menuInsets,
    bodyTop = 0,
    headTop = 0,
    decoTop = 0;

  if (this.$headBlueprint.hasClass('right-aligned')) {
    openingDirectionX = 'left';
  }
  openingDirectionX = openingDirectionX || this.openingDirectionX;
  openingDirectionY = openingDirectionY || this.openingDirectionY;
  this.$container.removeClass('up down left right');
  this.$body.removeClass('up down left right');
  this.$container.addClass(openingDirectionY + ' ' + openingDirectionX);
  this.$body.addClass(openingDirectionY + ' ' + openingDirectionX);

  // Make sure the elements inside the header have the same style as to blueprint (menu)
  // This makes it possible to position the content in the header (icon, text) exactly on top of the content of the blueprint
  this.$head.copyCss(this.$headBlueprint, 'line-height');
  this.$head.copyCssIfGreater(this.$headBlueprint, 'padding');

  $blueprintChildren = this.$headBlueprint.children();
  this.$head.children().each(function(i) {
    var $headChild = $(this);
    var $blueprintChild = $blueprintChildren.eq(i);
    $headChild.copyCss($blueprintChild, 'margin padding line-height border vertical-align font-size');
  });

  headSize = scout.graphics.getSize(this.$head, true);
  bodySize = scout.graphics.getSize(this.$body, true);
  bodyWidth = bodySize.width;

  // body min-width
  if (bodyWidth < headSize.width) {
    this.$body.width(headSize.width - 2);
    bodyWidth = headSize.width;
  }

  pos = this.$headBlueprint.offset();
  left = pos.left;
  headInsets = scout.graphics.getInsets(this.$head);
  menuInsets = scout.graphics.getInsets(this.$headBlueprint);
  top = pos.top - headInsets.top + menuInsets.top;

  if (openingDirectionY === 'up') {
    top -= bodySize.height;
    bodyTop -= headSize.height;
    headTop = bodyTop + bodySize.height;
    decoTop = headTop - 1; // -1 is body border (the purpose of deco is to hide the body border)
    this.$container.cssMarginBottom(headSize.height);
  } else if (openingDirectionY === 'down') {
    headTop -= headSize.height;
    this.$container.cssMarginTop(headSize.height);
  }

  $.log.debug('bodyWidth=' + bodyWidth + ' pos=[left' + pos.left + ' top=' + pos.top + '] headSize=' + headSize +
    ' headInsets=' + headInsets + ' left=' + left + ' top=' + top);
  this.$head.cssTop(headTop);
  this.$body.cssTop(bodyTop);
  this.$deco.cssTop(decoTop);

  if (openingDirectionX === 'left') {
    // when we use float:right, browser uses fractions of pixels, that's why we must
    // use the subPixelCorr variable. It corrects some visual pixel-shifting issues.
    widthDiff = bodyWidth - headSize.width;
    subPixelCorr = left - Math.floor(left);
    left -= widthDiff + headInsets.left - menuInsets.left;
    this.$head.cssLeft(widthDiff);
    this.$body.cssLeft(subPixelCorr);
    this.$deco.cssLeft(widthDiff + this.$head.cssBorderLeftWidth())
      .width(headSize.width - this.$head.cssBorderWidthX() + subPixelCorr);
  } else {
    left -= headInsets.left;
    this.$head.cssLeft(0);
    this.$deco.cssLeft(1)
      .width(headSize.width - 2);
  }

  this.setLocation(new scout.Point(left, top));
};
