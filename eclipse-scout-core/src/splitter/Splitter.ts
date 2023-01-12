/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {graphics, HtmlComponent, InitModelOf, LayoutData, numbers, SplitterEventMap, SplitterModel, SplitterMoveEvent, Widget} from '../index';
import $ from 'jquery';

export class Splitter extends Widget implements SplitterModel {
  declare model: SplitterModel;
  declare eventMap: SplitterEventMap;
  declare self: Splitter;

  splitHorizontal: boolean;
  position: number;
  orientation: 'top' | 'right' | 'bottom' | 'left';
  layoutData: LayoutData;
  $anchor: JQuery;
  $root: JQuery;

  /** distance from cursor to splitter, makes resizing smoother by preventing initial 'jump' */
  protected _cursorOffset: { left: number; top: number };
  protected _mouseDownHandler: (event: JQuery.MouseDownEvent) => void;
  protected _$window: JQuery<Window>;
  protected _$body: JQuery<Body>;

  constructor() {
    super();
    this.splitHorizontal = true;
    this.$anchor = null;
    this.$root = null;
    this.position = null;
    this.orientation = 'top';
    this.layoutData = null;

    this._cursorOffset = null;
    this._mouseDownHandler = this._onMouseDown.bind(this);
    this._$window = null;
    this._$body = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.setPosition(this.position);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('splitter')
      .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this._$window = this.$parent.window();
    this._$body = this.$parent.body();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderPosition();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    if (this.enabledComputed) {
      this.$container.on('mousedown', this._mouseDownHandler);
    } else {
      this.$container.off('mousedown', this._mouseDownHandler);
    }
  }

  protected override _renderVisible() {
    super._renderVisible();
    if (this.initialized) {
      this._renderPosition();
    }
  }

  override setLayoutData(layoutData: LayoutData) {
    super.setLayoutData(layoutData);
    this.layoutData = layoutData;
  }

  getLayoutData(): LayoutData {
    return this.layoutData;
  }

  /**
   * Sets the splitter position to the specified newSize (in pixels).
   * If the newSize is not specified, the size is calculated automatically by reading the this.$anchor element's bounds.
   *
   * @returns the effective position in pixel.
   */
  setPosition(position?: number): number {
    if (!numbers.isNumber(position)) {
      position = this._derivePositionFromAnchor();
    }
    if (position !== this.position) {
      this._setPosition(position);
    }
    return position;
  }

  /**
   * Derives the position from $anchor element's bounds
   */
  protected _derivePositionFromAnchor(): number {
    if (!this.$anchor) {
      return null;
    }
    let anchorBounds = graphics.offsetBounds(this.$anchor, {
      exact: true
    });
    if (this.splitHorizontal) {
      return anchorBounds.x + anchorBounds.width;
    }
    return anchorBounds.y + anchorBounds.height;
  }

  protected _setPosition(position: number) {
    if (!numbers.isNumber(position)) {
      return;
    }
    if (position === this.position) {
      return;
    }
    this.position = position;
    this.trigger('positionChange', {
      position: position
    });
    if (this.rendered) {
      this._renderPosition();
    }
  }

  protected _renderPosition() {
    if (this.position === null || !this.visible) {
      return;
    }

    let splitterSize = graphics.size(this.$container, true);
    if (this.splitHorizontal) {
      let x = this.position - (splitterSize.width / 2);
      if (this.orientation === 'right') {
        this.$container.cssRight(x);
      } else {
        this.$container.cssLeft(x);
      }
    } else {
      let y = this.position - (splitterSize.height / 2);
      if (this.orientation === 'bottom') {
        this.$container.cssBottom(y);
      } else {
        this.$container.cssTop(y);
      }
    }
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent) {
    // The calculation of the offset bounds looks a bit complicated, because we cannot
    // use "scout.graphics.offsetBounds($el, true)" here. This method would only consider
    // any margins in the size, not the position.
    let splitterMargins = graphics.margins(this.$container);
    let splitterOffsetBounds = graphics.offsetBounds(this.$container);
    splitterOffsetBounds.x -= splitterMargins.left;
    splitterOffsetBounds.y -= splitterMargins.top;
    splitterOffsetBounds.width += splitterMargins.horizontal();
    splitterOffsetBounds.height += splitterMargins.vertical();
    let splitterCenter = splitterOffsetBounds.center();

    // Add listeners (we add them to the window to make sure we get the mouseup event even when the cursor it outside the window)
    this._$window
      .on('mousemove.splitter', this._onMouseMove.bind(this))
      .one('mouseup', this._onMouseUp.bind(this));
    // Ensure the correct cursor is always shown while moving
    this._$body.addClass(this.splitHorizontal ? 'col-resize' : 'row-resize');
    $('iframe').addClass('dragging-in-progress');
    this._cursorOffset = {
      left: splitterCenter.x - event.pageX,
      top: splitterCenter.y - event.pageY
    };
    this.trigger('moveStart', {
      position: this._getSplitterPosition(event)
    });
    // Prevent text selection in a form
    event.preventDefault();
  }

  protected _getSplitterPosition(event: JQuery.MouseEventBase): number {
    let rootBounds = graphics.offsetBounds(this.$root || this.$parent);
    if (this.splitHorizontal) {
      let x = event.pageX + this._cursorOffset.left - rootBounds.x;
      return (this.orientation === 'right' ? rootBounds.width - x : x);
    }
    let y = event.pageY + this._cursorOffset.top - rootBounds.y;
    return (this.orientation === 'bottom' ? rootBounds.height - y : y);
  }

  protected _onMouseMove(event: JQuery.MouseMoveEvent<Window>) {
    let splitterPosition = this._getSplitterPosition(event);
    // fire event
    let moveEvent = this.trigger('move', {
      position: splitterPosition
    }) as SplitterMoveEvent;
    if (moveEvent.defaultPrevented) {
      return;
    }
    this._setPosition(moveEvent.position);
  }

  protected _onMouseUp(event: JQuery.MouseUpEvent<Window>) {
    // Remove listeners and reset cursor
    this._$window.off('mousemove.splitter');
    this._$body.removeClass((this.splitHorizontal ? 'col-resize' : 'row-resize'));
    $('iframe').removeClass('dragging-in-progress');
    this.trigger('moveEnd', {
      position: this.position
    });
  }
}
