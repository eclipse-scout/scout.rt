/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {graphics, HtmlComponent, Widget} from '../index';
import $ from 'jquery';

export default class Splitter extends Widget {

  constructor() {
    super();
    this.splitHorizontal = true;
    this.$anchor = null; // optional
    this.$root = null; // optional (fallback is this.$parent)
    this.position = null; // current splitter position in pixels, updated by updatePosition()
    this.orientation = 'top'; // Direction set to position the splitter inside the root element ('top', 'right', 'bottom' or 'left')
    this.layoutData = null;

    this._cursorOffset = 0; // distance from cursor to splitter, makes resizing smoother by preventing initial 'jump'
    this._mouseDownHandler = this._onMouseDown.bind(this);
    this._$window = null;
    this._$body = null;
  }

  _init(model) {
    super._init(model);
    this.setPosition(this.position);
  }

  _render() {
    this.$container = this.$parent.appendDiv('splitter')
      .addClass(this.splitHorizontal ? 'x-axis' : 'y-axis');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this._$window = this.$parent.window();
    this._$body = this.$parent.body();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderPosition();
  }

  _renderEnabled() {
    super._renderEnabled();
    if (this.enabledComputed) {
      this.$container.on('mousedown', this._mouseDownHandler);
    } else {
      this.$container.off('mousedown', this._mouseDownHandler);
    }
  }

  _renderVisible() {
    super._renderVisible();
    if (this.initialized) {
      this._renderPosition();
    }
  }

  setLayoutData(layoutData) {
    super.setLayoutData(layoutData);
    this.layoutData = layoutData;
  }

  getLayoutData() {
    return this.layoutData;
  }

  /**
   * Sets the splitter position to the specified newSize (in pixels). If the newSize is
   * not specified, the size is calculated automatically by reading the this.$anchor
   * element's bounds.
   *
   * @returns {number} the effective position in pixel.
   */
  setPosition(position) {
    if (!$.isNumeric(position)) {
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
  _derivePositionFromAnchor() {
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

  _setPosition(position) {
    if (!$.isNumeric(position)) {
      return;
    }
    if (position === this.position) {
      return;
    }
    this.position = position;
    let event = {
      position: position
    };
    this.trigger('positionChange', event);
    if (this.rendered) {
      this._renderPosition();
    }
  }

  _renderPosition() {
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

  _onMouseDown(event) {
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

  _getSplitterPosition(event) {
    let rootBounds = graphics.offsetBounds(this.$root || this.$parent);
    if (this.splitHorizontal) {
      let x = event.pageX + this._cursorOffset.left - rootBounds.x;
      return (this.orientation === 'right' ? rootBounds.width - x : x);
    }
    let y = event.pageY + this._cursorOffset.top - rootBounds.y;
    return (this.orientation === 'bottom' ? rootBounds.height - y : y);
  }

  _onMouseMove(event) {
    let splitterPosition = this._getSplitterPosition(event);
    // fire event
    let moveEvent = {
      position: splitterPosition,
      defaultPrevented: false,
      preventDefault: function() {
        this.defaultPrevented = true;
      },
      setPosition: function(position) {
        this.position = position;
      }
    };
    this.trigger('move', moveEvent);
    if (moveEvent.defaultPrevented) {
      return;
    }
    this._setPosition(moveEvent.position);
  }

  _onMouseUp(event) {
    // Remove listeners and reset cursor
    this._$window.off('mousemove.splitter');
    this._$body.removeClass((this.splitHorizontal ? 'col-resize' : 'row-resize'));
    $('iframe').removeClass('dragging-in-progress');
    this.trigger('moveEnd', {
      position: this.position
    });
  }
}
