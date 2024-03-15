/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Desktop, Form, graphics, keys, Menu, ObjectOrChildModel, Point, Rectangle, scout, scrollbars, Status, StatusSeverity, strings, TooltipEventMap, TooltipModel, Widget, WidgetPopup} from '../index';
import $ from 'jquery';
import KeyDownEvent = JQuery.KeyDownEvent;

export type TooltipPosition = 'top' | 'bottom';
export type TooltipDirection = 'right' | 'left';
export type TooltipScrollType = 'position' | 'remove';

export class Tooltip extends Widget implements TooltipModel {
  declare model: TooltipModel;
  declare eventMap: TooltipEventMap;
  declare self: Tooltip;

  text: string;
  severity: StatusSeverity;
  arrowPosition: number;
  arrowPositionUnit: string;
  clipOrigin: boolean;
  windowPaddingX: number;
  windowPaddingY: number;
  origin: Rectangle;
  originRelativeToParent: boolean;
  originProducer: ($anchor: JQuery) => Rectangle;
  offsetProducer: (origin: Rectangle) => Point;
  autoRemove: boolean;
  tooltipPosition: TooltipPosition;
  tooltipDirection: TooltipDirection;
  scrollType: TooltipScrollType;
  htmlEnabled: boolean;
  menus: Menu[];
  $anchor: JQuery;
  $arrow: JQuery;
  $content: JQuery;
  $menus: JQuery;

  protected _popup: Form | WidgetPopup;
  protected _openLater: boolean;
  protected _mouseDownHandler: (event: MouseEvent) => boolean;
  protected _keydownHandler: (event: KeyDownEvent) => void;
  protected _anchorScrollHandler: (event: JQuery.ScrollEvent) => void;
  protected _moveHandler: () => void;

  constructor() {
    super();

    this.text = null;
    this.severity = Status.Severity.INFO;
    this.arrowPosition = 16;
    this.arrowPositionUnit = 'px';
    this.clipOrigin = false;
    this.windowPaddingX = 10;
    this.windowPaddingY = 5;
    this.origin = null;
    this.originRelativeToParent = false;
    this.originProducer = null;
    this.offsetProducer = null;
    this.autoRemove = true;
    this.tooltipPosition = 'top';
    this.tooltipDirection = 'right';
    this.scrollType = 'position';
    this.htmlEnabled = false;
    this.menus = [];
    this.$anchor = null;
    this.$arrow = null;
    this.$content = null;
    this._addWidgetProperties(['menus']);

    this._popup = null;
    this._openLater = false;
  }

  override render($parent?: JQuery) {
    // Use entry point by default
    let $tooltipParent = $parent || this.entryPoint();
    // when the parent is detached it is not possible to render the popup -> do it later
    if (!$tooltipParent || !$tooltipParent.length || !$tooltipParent.isAttached()) {
      this._openLater = true;
      this.$parent = $tooltipParent;
      return;
    }
    super.render($tooltipParent);
  }

  protected override _render() {
    this.$container = this.$parent
      .appendDiv('tooltip')
      .data('tooltip', this);
    if (this.cssClass) {
      this.$container.addClass(this.cssClass);
    }

    this.$arrow = this.$container.appendDiv('tooltip-arrow');
    this.$content = this.$container.appendDiv('tooltip-content');
    this._renderText();
    this._renderSeverity();
    this._renderMenus();

    if (this.autoRemove) {
      // Every user action will remove the tooltip
      this._mouseDownHandler = this._onDocumentMouseDown.bind(this);
      // The listener needs to be executed in the capturing phase -> Allows for having context menus inside the tooltip, otherwise click on context menu header would close the tooltip
      this.$container.document(true).addEventListener('mousedown', this._mouseDownHandler, true); // true=the event handler is executed in the capturing phase

      this._keydownHandler = this._onDocumentKeyDown.bind(this);
      this.$container.document().on('keydown', this._keydownHandler);
    }

    if (this.$anchor && this.scrollType) {
      this._anchorScrollHandler = this._onAnchorScroll.bind(this);
      scrollbars.onScroll(this.$anchor, this._anchorScrollHandler);
    }

    // If the tooltip is rendered inside a popup (dialog form or popup), get a reference to the popup.
    this._popup = this.findParent(WidgetPopup);
    if (!this._popup) {
      this._popup = this.findParent(p => p instanceof Form && p.isDialog());
    }

    // If inside a popup, attach a listener to reposition the tooltip when the popup is moved
    if (this._popup) {
      this._moveHandler = this.position.bind(this);
      this._popup.on('move', this._moveHandler);
    }

    this.findDesktopIf$Parent()?.tooltipRendered(this);
  }

  protected override _postRender() {
    super._postRender();
    this.position();
  }

  protected override _remove() {
    if (this._mouseDownHandler) {
      this.$container.document(true).removeEventListener('mousedown', this._mouseDownHandler, true);
      this._mouseDownHandler = null;
    }
    if (this._keydownHandler) {
      this.$container.document().off('keydown', this._keydownHandler);
      this._keydownHandler = null;
    }
    if (this._anchorScrollHandler) {
      scrollbars.offScroll(this._anchorScrollHandler);
      this._anchorScrollHandler = null;
    }
    if (this._moveHandler) {
      if (this._popup) {
        this._popup.off('move', this._moveHandler);
      }
      this._moveHandler = null;
    }
    this._popup = null;
    this.$menus = null;
    super._remove();
    this.findDesktopIf$Parent()?.tooltipRemoved(this);
  }

  /**
   * Finds the desktop (see {@link Widget.findDesktop}) and checks whether the `$container` of this desktop is `this.$parent`.
   */
  findDesktopIf$Parent(): Desktop {
    if (!this.$parent) {
      return null;
    }
    const desktop = super.findDesktop();
    if (desktop?.$container[0] === this.$parent[0]) {
      return desktop;
    }
    return null;
  }

  override _destroy() {
    super._destroy();
    this.menus.forEach(menu => {
      // ContextMenu will only be removed not closed if it has a different owner.
      // Unfortunately it cannot be re-rendered again so the menu will look selected the next time the tooltip is opened -> ensure popup will be closed
      if (menu.popup) {
        menu.popup.close();
      }
    });
  }

  protected override _onAttach() {
    super._onAttach();
    if (this._openLater && !this.rendered) {
      this._openLater = false;
      this.render();
    }
  }

  protected override _renderOnDetach() {
    this._openLater = true;
    this.remove();
    super._onDetach();
  }

  protected override _isRemovalPrevented(): boolean {
    // If removal of a parent is pending due to an animation then don't return true to make sure tooltips are closed before the parent animation starts.
    // However, if the tooltip itself is removed by an animation, removal should be prevented to ensure remove() won't run multiple times.
    return this.removalPending;
  }

  setText(text: string) {
    this.setProperty('text', text);
  }

  protected _renderText() {
    let text = this.text || '';
    if (this.htmlEnabled) {
      this.$content.html(text);
    } else {
      // use nl2br to allow tooltips with line breaks
      this.$content.html(strings.nl2br(text));
    }
    this.$content.setVisible(!!text);
    this.$container.toggleClass('no-text', !text);
    if (!this.rendering) {
      this.position();
    }
  }

  setHtmlEnabled(htmlEnabled: boolean) {
    this.setProperty('htmlEnabled', htmlEnabled);
  }

  protected _renderHtmlEnabled() {
    this._renderText();
  }

  setSeverity(severity: StatusSeverity) {
    this.setProperty('severity', severity);
  }

  protected _renderSeverity() {
    this.$container.removeClass(Status.SEVERITY_CSS_CLASSES);
    this.$container.addClass(Status.cssClassForSeverity(this.severity));
  }

  setMenus(menus: ObjectOrChildModel<Menu> | ObjectOrChildModel<Menu>[]) {
    menus = arrays.ensure(menus);
    this.setProperty('menus', menus);
  }

  protected _renderMenus() {
    let maxIconWidth = 0,
      menus = this.menus;

    if (menus.length > 0 && !this.$menus) {
      this.$menus = this.$container.appendDiv('tooltip-menus');
    } else if (menus.length === 0 && this.$menus) {
      this.$menus.remove();
      this.$menus = null;
    }

    // Render menus
    menus.forEach(menu => {
      let iconWidth = 0;
      menu.render(this.$menus);
      if (menu.iconId) {
        iconWidth = menu.get$Icon().outerWidth(true);
        maxIconWidth = Math.max(iconWidth, maxIconWidth);
      }
    });

    // Align menus if there is one with an icon
    if (maxIconWidth > 0) {
      menus.forEach(menu => {
        if (!menu.iconId) {
          menu.$text.cssPaddingLeft(maxIconWidth);
        } else {
          menu.$text.cssPaddingLeft(maxIconWidth - menu.get$Icon().outerWidth(true));
        }
      });
    }

    this.$container.toggleClass('has-menus', menus.length > 0);

    if (!this.rendering) {
      // New text might be shorter or longer -> recompute position.
      // Resetting the current position first ensures that the position is computed the
      // same as if the tooltip was opened initially with the new text, even when the tooltip
      // is at the right of the screen. Otherwise, text wrapping might occur.
      this.$container.cssLeft('').cssTop('');
      this.position();
    }
  }

  set$Anchor($anchor: JQuery) {
    if ($anchor?.[0] === this.$anchor?.[0]) { // compare DOM elements
      return;
    }
    this.setProperty('$anchor', $anchor);
  }

  protected _render$anchor() {
    this.position();
  }

  position() {
    let $scrollParents = this.$anchor?.scrollParents();
    let origin = this._getOrigin();
    if ($scrollParents && this.clipOrigin) {
      // Remove the parts from the origin that are not in the viewport
      origin = scrollbars.intersectViewport(origin, $scrollParents);
    }
    let offset = this._getOffset(origin);
    let tooltipPosition = origin.point().add(offset);

    if ($scrollParents) {
      // Sticky tooltip must only be visible if the location where the tooltip points is in view (prevents that the tooltip points at an invisible anchor)
      let inView = scrollbars.isLocationInView(tooltipPosition, $scrollParents);
      this.$container.setVisible(inView);
    }

    // this.$parent might not be at (0,0) of the document
    if (!this.originRelativeToParent) {
      let parentOffset = this.$parent.offset();
      tooltipPosition.x -= parentOffset.left;
      tooltipPosition.y -= parentOffset.top;
    }

    let arrowSizeX = 7;
    let arrowSizeY = 4;

    let tooltipHeight = this.$container.outerHeight();
    let tooltipWidth = this.$container.outerWidth();

    // Compute actual arrow position if position is provided in percentage
    let arrowPosition = this.arrowPosition;
    if (this.arrowPositionUnit === '%') {
      arrowPosition = tooltipWidth * this.arrowPosition / 100;
    }
    if (this.tooltipDirection === 'left') {
      arrowPosition = tooltipWidth - arrowPosition;
    }

    let top = tooltipPosition.y - tooltipHeight - arrowSizeY;
    let left = tooltipPosition.x - arrowPosition;
    let overlapX = left + tooltipWidth + this.windowPaddingX - this.$parent.width();
    let overlapY = top - this.windowPaddingY;

    // Move tooltip to the left until it gets fully visible
    if (overlapX > 0) {
      left -= overlapX;
      arrowPosition = tooltipPosition.x - left;
    }
    // Move tooltip to the right if it overlaps the left edge
    if (left < this.windowPaddingX) {
      left = this.windowPaddingX;
      arrowPosition = tooltipPosition.x - this.windowPaddingX;
    }

    // Move tooltip to the bottom, arrow on top
    this.$arrow.removeClass('arrow-top arrow-bottom');
    if (this.tooltipPosition === 'bottom' || overlapY < 0) {
      this.$arrow.addClass('arrow-top');
      top = tooltipPosition.y + origin.height + arrowSizeY;
    } else {
      this.$arrow.addClass('arrow-bottom');
    }

    // Make sure arrow is never positioned outside the tooltip
    arrowPosition = Math.min(arrowPosition, this.$container.outerWidth() - arrowSizeX);
    arrowPosition = Math.max(arrowPosition, arrowSizeX);
    this.$arrow.cssLeft(arrowPosition - this.$arrow.cssBorderLeftWidth());
    this.$container
      .cssLeft(left)
      .cssTop(top);

    // If there are menu popups make sure they are positioned correctly
    this.menus.forEach(menu => {
      if (menu.popup) {
        menu.popup.position();
      }
    });
  }

  protected _getOrigin(): Rectangle {
    if (this.originProducer) {
      const origin = this.originProducer(this.$anchor);
      if (origin) {
        return origin;
      }
    }
    if (this.origin) {
      return this.origin;
    }
    return graphics.offsetBounds(this.$anchor);
  }

  protected _getOffset(origin: Rectangle): Point {
    if (this.offsetProducer) {
      const offset = this.offsetProducer(origin);
      if (offset) {
        return offset;
      }
    }
    if (this.origin) {
      return new Point(0, 0);
    }
    return new Point(origin.width / 2, 0);
  }

  protected _onAnchorScroll(event: JQuery.ScrollEvent) {
    if (!this.rendered) {
      // Scroll events may be fired delayed, even if scroll listener are already removed.
      return;
    }
    if (this.scrollType === 'position') {
      this.position();
    } else if (this.scrollType === 'remove') {
      this.destroy();
    }
  }

  protected _onDocumentMouseDown(event: MouseEvent): boolean {
    if (!this.rendered) {
      return false;
    }
    if (this._isMouseDownOutside(event)) {
      this._onMouseDownOutside(event);
    }
  }

  protected _isMouseDownOutside(event: MouseEvent): boolean {
    let target = event.target as HTMLElement;
    let $target = $(target),
      targetWidget = scout.widget($target);

    // Only remove the tooltip if the click is outside the container or the $anchor (= status icon)
    // Also ignore clicks if the tooltip is covert by a glasspane
    return !this.isOrHas(targetWidget) &&
      (this.$anchor && !this.$anchor.isOrHas($target[0])) &&
      !this.session.focusManager.isElementCovertByGlassPane(this.$container[0]);
  }

  /**
   * Method invoked once a mouse down event occurs outside the tooltip.
   */
  protected _onMouseDownOutside(event: MouseEvent) {
    this.destroy();
  }

  protected _onDocumentKeyDown(event: KeyDownEvent) {
    if (scout.isOneOf(event.which,
      keys.CTRL, keys.SHIFT, keys.ALT,
      keys.NUM_LOCK, keys.CAPS_LOCK, keys.SCROLL_LOCK,
      keys.WIN_LEFT, keys.WIN_RIGHT, keys.SELECT,
      keys.PAUSE, keys.PRINT_SCREEN)) {
      return;
    }

    this.destroy();
  }
}
