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
  this.$parent;
  this._headVisible = true;
  this.resizeHandler = this.onResize.bind(this);
};
scout.inherits(scout.PopupWithHead, scout.Popup);

scout.PopupWithHead.prototype._createLayout = function() {
  return new scout.PopupWithHeadLayout(this);
};

scout.PopupWithHead.prototype._init = function(options) {
  scout.PopupWithHead.parent.prototype._init.call(this, options);

  // Compared to a regular popup, the popup is aligned with the head and either opened left or right,
  // -> the width should be adjusted as well if it does not fit into the window
  this.trimWidth = scout.nvl(options.trimWidth, true);
};

scout.PopupWithHead.prototype._render = function($parent) {
  scout.PopupWithHead.parent.prototype._render.call(this, $parent);
  this.$parent.window().on('resize', this.resizeHandler);

  this.$body = this._$createNewBody();
  if (this._headVisible) {
    this._renderHead();
  }
};

scout.PopupWithHead.prototype.onResize = function() {
  this.$parent.window().off('resize', this.resizeHandler);
  this.close();
};

scout.PopupWithHead.prototype._remove = function($parent) {
  scout.PopupWithHead.parent.prototype._remove.call(this, $parent);
  this.$parent.window().off('resize', this.resizeHandler);
};

scout.PopupWithHead.prototype._$createNewBody = function() {
  this.$body = this.$container.appendDiv('popup-body');
  this._modifyBody();
  return this.$body;
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
    .makeDiv('popup-head menu-item')
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
  if (!this.$container) {
    //when layouting menu bar menues are first removed and then newly added prevent this.$contaier = null;
    return;
  }
  var openingDirectionX, openingDirectionY, overlap, pos;
  if (!this._headVisible) {
    // If head is not visible, use default implementation and adjust $body to $container
    scout.PopupWithHead.parent.prototype._position.call(this, $container);
    this.$body.removeClass('up down left right');
    openingDirectionY = 'up';
    if ($container.hasClass('down')) {
      openingDirectionY = 'down';
    }
    openingDirectionX = 'right';
    if ($container.hasClass('left')) {
      openingDirectionX = 'left';
    }
    this.$body.addClass(openingDirectionY + ' ' + openingDirectionX);
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
    // this.$parent might not be at (0,0) of the document
    var parentOffset = this.$parent.offset();
    overlap.x -= parentOffset.left;
    overlap.y -= parentOffset.top;
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

  openingDirectionX = openingDirectionX || this.openingDirectionX;
  openingDirectionY = openingDirectionY || this.openingDirectionY;
  this.$container.removeClass('up down left right');
  this.$body.removeClass('up down left right');
  this.$container.addClass(openingDirectionY + ' ' + openingDirectionX);
  this.$body.addClass(openingDirectionY + ' ' + openingDirectionX);

  // Make sure the elements inside the header have the same style as to blueprint (menu)
  // This makes it possible to position the content in the header (icon, text) exactly on top of the content of the blueprint
  this.$head.copyCssIfGreater(this.$headBlueprint, 'padding');
  this.$head.height(this.$headBlueprint.height());

  $blueprintChildren = this.$headBlueprint.children();
  this.$head.children().each(function(i) {
    var $headChild = $(this);
    var $blueprintChild = $blueprintChildren.eq(i);
    $headChild.copyCss($blueprintChild, 'margin padding line-height border vertical-align font-size display');
  });

  headSize = scout.graphics.getSize(this.$head, true);
  bodySize = scout.graphics.getSize(this.$body, true);
  bodySize.width = Math.max(bodySize.width, headSize.width);
  bodyWidth = bodySize.width;

  pos = this.$headBlueprint.offset();
  // this.$parent might not be at (0,0) of the document
  var parentOffset = this.$parent.offset();
  pos.left -= parentOffset.left;
  pos.top -= parentOffset.top;

  left = pos.left;
  headInsets = scout.graphics.getInsets(this.$head);
  menuInsets = scout.graphics.getInsets(this.$headBlueprint);
  top = pos.top - headInsets.top + menuInsets.top;

  if (openingDirectionY === 'up') {
    top -= bodySize.height;
    headTop = bodyTop + bodySize.height;
    decoTop = headTop - 1; // -1 is body border (the purpose of deco is to hide the body border)
    this.$container.cssMarginBottom(headSize.height);
    this.$container.css('margin-top', '');
  } else if (openingDirectionY === 'down') {
    headTop -= headSize.height;
    this.$container.cssMarginTop(headSize.height);
    this.$container.css('margin-bottom', '');
  }

  $.log.debug('bodyWidth=' + bodyWidth + ' pos=[left=' + pos.left + ' top=' + pos.top + '] headSize=' + headSize +
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
    left = left - headInsets.left + menuInsets.left;
    this.$head.cssLeft(0);
    this.$deco.cssLeft(1)
      .width(headSize.width - 2);
  }

  this.openingDirectionX = openingDirectionX;
  this.openingDirectionY = openingDirectionY;
  this.setLocation(new scout.Point(left, top));

  // Explicitly write the (rounded, by jQuery) sizes to the elements to prevent rounding issues
  scout.graphics.setSize(this.$head, headSize);
  scout.graphics.setSize(this.$body, bodySize);
};
