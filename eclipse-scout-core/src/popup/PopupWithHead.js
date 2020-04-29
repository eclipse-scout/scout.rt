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
import {graphics, HtmlComponent, NullLayout, Point, Popup, PopupWithHeadLayout, scout, scrollbars} from '../index';
import $ from 'jquery';

export default class PopupWithHead extends Popup {

  constructor() {
    super();
    this.$head;
    this.$body;
    this.$deco;
    this.$parent;
    this.horizontalSwitch = true;
    this.verticalSwitch = true;
    this._headVisible = true;
    this.windowResizeType = 'remove';
  }

  _createLayout() {
    return new PopupWithHeadLayout(this);
  }

  _init(options) {
    super._init(options);

    // Compared to a regular popup, the popup is aligned with the head and either opened left or right,
    // -> the width should be adjusted as well if it does not fit into the window
    this.trimWidth = scout.nvl(options.trimWidth, this._headVisible);
  }

  _render() {
    super._render();

    this._$createBody();

    if (this._headVisible) {
      this._renderHead();
    }
  }

  _postRender() {
    super._postRender();
    scrollbars.update(this.$body);
  }

  _$createBody() {
    this.$body = this.$container.appendDiv('popup-body');
    // Complete the layout hierarchy between the popup and the menu items
    let htmlBody = HtmlComponent.install(this.$body, this.session);
    htmlBody.setLayout(this._createBodyLayout());

    this._modifyBody();
  }

  _createBodyLayout() {
    return new NullLayout();
  }

  rerenderHead() {
    this._removeHead();
    this._renderHead();
  }

  /**
   * Copies html from this.$headBlueprint, if set.
   */
  _renderHead() {
    this.$deco = this.$container.makeDiv('popup-deco');
    this.$head = this.$container
      .makeDiv('popup-head menu-item')
      .on('mousedown', '', this._onHeadMouseDown.bind(this));
    this.$container
      .prepend(this.$head)
      .append(this.$deco);
    if (this.$headBlueprint) {
      this.$head.html(this.$headBlueprint.html());
      this._copyStyleToHeadChildren();
    }
  }

  /**
   * Sets CSS classes or CSS-properties on the body.
   */
  _modifyBody() {
    // NOP
  }

  _removeHead() {
    if (this.$head) {
      this.$head.remove();
    }
    if (this.$deco) {
      this.$deco.remove();
    }
  }

  _copyCssClassToHead(className) {
    if (this.$headBlueprint && this.$headBlueprint.hasClass(className)) {
      this.$head.addClass(className);
    }
  }

  _copyStyleToHeadChildren() {
    let $blueprintChildren = this.$headBlueprint.children();
    this.$head.children().each(function(i) {
      let $headChild = $(this);
      let $blueprintChild = $blueprintChildren.eq(i);
      $headChild.copyCss($blueprintChild, 'margin padding line-height border vertical-align font-size display width height');
    });
  }

  _onHeadMouseDown(event) {
    if (this.$head && this.$head.isOrHas(event.target)) {
      this.close();
    }
  }

  /**
   * @override
   */
  _isMouseDownOutside(event) {
    if (this.$headBlueprint && this.$headBlueprint.isOrHas(event.target)) {
      // click on the head still belongs to the popup -> not an outside click -> do not close popup
      return false;
    }

    return super._isMouseDownOutside(event);
  }

  appendToBody($element) {
    this.$body.append($element);
  }

  addClassToBody(clazz) {
    this.$body.addClass(clazz);
  }

  /**
   * @override Popup.js
   */
  _position(switchIfNecessary) {
    let $container = this.$container;
    if (!$container) {
      // When layouting the menu bar, menus are removed at first and then added anew.
      return;
    }
    let horizontalAlignment, verticalAlignment, overlap, pos;
    if (!this._headVisible) {
      // If head is not visible, use default implementation and adjust $body to $container
      super._position(switchIfNecessary);
      this.$body.removeClass(this._alignClasses());
      verticalAlignment = Popup.Alignment.TOP;
      if ($container.hasClass(Popup.Alignment.BOTTOM)) {
        verticalAlignment = Popup.Alignment.BOTTOM;
      }
      horizontalAlignment = Popup.Alignment.LEFTEDGE;
      if ($container.hasClass(Popup.Alignment.RIGHTEDGE)) {
        horizontalAlignment = Popup.Alignment.RIGHTEDGE;
      }
      this.$body.addClass(verticalAlignment + ' ' + horizontalAlignment);
      return;
    }
    this._positionImpl();

    let horizontalSwitch = scout.nvl(switchIfNecessary, this.horizontalSwitch);
    let verticalSwitch = scout.nvl(switchIfNecessary, this.verticalSwitch);
    if (horizontalSwitch || verticalSwitch) {
      pos = $container.offset();
      overlap = this.overlap({
        x: pos.left,
        y: pos.top
      }, false);
      // this.$parent might not be at (0,0) of the document
      let parentOffset = this.$parent.offset();
      overlap.x -= parentOffset.left;
      overlap.y -= parentOffset.top;
      if (overlap.y > 0 && verticalSwitch) {
        // switch opening direction
        verticalAlignment = Popup.Alignment.TOP;
      }
      if (overlap.x > 0 && horizontalSwitch) {
        // switch opening direction
        horizontalAlignment = Popup.Alignment.RIGHTEDGE;
      }
      if (horizontalAlignment || verticalAlignment) {
        // Align again if openingDirection has to be switched
        this._positionImpl(horizontalAlignment, verticalAlignment);
      }
    }
  }

  _positionImpl(horizontalAlignment, verticalAlignment) {
    let pos, headSize, bodySize, bodyWidth, widthDiff, left, top, headInsets, menuInsets,
      bodyTop = 0,
      headTop = 0,
      decoTop = 0;

    horizontalAlignment = horizontalAlignment || this.horizontalAlignment;
    verticalAlignment = verticalAlignment || this.verticalAlignment;
    this.$container.removeClass(this._alignClasses());
    this.$body.removeClass(this._alignClasses());
    this.$container.addClass(verticalAlignment + ' ' + horizontalAlignment);
    this.$body.addClass(verticalAlignment + ' ' + horizontalAlignment);

    // Make sure the elements inside the header have the same style as to blueprint (menu)
    // This makes it possible to position the content in the header (icon, text) exactly on top of the content of the blueprint
    this.$head.copyCssIfGreater(this.$headBlueprint, 'padding');
    this.$head.copyCss(this.$headBlueprint, 'line-height');
    this.$head.height(this.$headBlueprint.height());
    this.$head.width(this.$headBlueprint.width());

    this._copyStyleToHeadChildren();

    headSize = graphics.size(this.$head, true);
    bodySize = graphics.size(this.$body, true);
    bodySize.width = Math.max(bodySize.width, headSize.width);
    bodyWidth = bodySize.width;

    pos = this.$headBlueprint.offset();
    // this.$parent might not be at (0,0) of the document
    let parentOffset = this.$parent.offset();
    pos.left -= parentOffset.left;
    pos.top -= parentOffset.top;

    left = pos.left;
    headInsets = graphics.insets(this.$head);
    menuInsets = graphics.insets(this.$headBlueprint);
    top = pos.top - headInsets.top + menuInsets.top;

    if (verticalAlignment === Popup.Alignment.TOP) {
      top -= bodySize.height;
      headTop = bodyTop + bodySize.height;
      decoTop = headTop - 1; // -1 is body border (the purpose of deco is to hide the body border)
      this.$container.cssMarginBottom(headSize.height);
      this.$container.css('margin-top', '');
    } else if (verticalAlignment === Popup.Alignment.BOTTOM) {
      headTop -= headSize.height;
      this.$container.cssMarginTop(headSize.height);
      this.$container.css('margin-bottom', '');
    }

    $.log.isTraceEnabled() && $.log.trace('bodyWidth=' + bodyWidth + ' pos=[left=' + pos.left + ' top=' + pos.top + '] headSize=' + headSize +
      ' headInsets=' + headInsets + ' left=' + left + ' top=' + top);
    this.$head.cssTop(headTop);
    this.$body.cssTop(bodyTop);
    this.$deco.cssTop(decoTop);

    if (horizontalAlignment === Popup.Alignment.RIGHTEDGE) {
      widthDiff = bodyWidth - headSize.width;
      left -= widthDiff + headInsets.left - menuInsets.left;
      this.$head.cssLeft(widthDiff);
      this.$body.cssLeft(0);
      this.$deco.cssLeft(widthDiff + this.$head.cssBorderLeftWidth())
        .width(headSize.width - this.$head.cssBorderWidthX());
    } else {
      left = left - headInsets.left + menuInsets.left;
      this.$head.cssLeft(0);
      this.$deco.cssLeft(1)
        .width(headSize.width - 2);
    }

    this.horizontalAlignment = horizontalAlignment;
    this.verticalAlignment = verticalAlignment;
    this.setLocation(new Point(left, top));

    // Explicitly write the (rounded, by jQuery) sizes to the elements to prevent rounding issues
    graphics.setSize(this.$head, headSize);
    graphics.setSize(this.$body, bodySize);
  }

  _glassPaneTargets() {
    return [this.$container, this.$head];
  }
}
