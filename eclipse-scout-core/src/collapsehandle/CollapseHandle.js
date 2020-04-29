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
import {Widget} from '../index';

export default class CollapseHandle extends Widget {

  constructor() {
    super();
    this.leftVisible = true;
    this.rightVisible = true;
    this.horizontalAlignment = CollapseHandle.HorizontalAlignment.LEFT;
  }

  static HorizontalAlignment = {
    LEFT: 'left',
    RIGHT: 'right'
  };

  _render() {
    this.$container = this.$parent.appendDiv('collapse-handle');
    this.$container.on('mousedown', this._onMouseDown.bind(this));

    this.$left = this.$container.appendDiv('collapse-handle-body left');
    this.$right = this.$container.appendDiv('collapse-handle-body right');
  }

  _renderProperties() {
    super._renderProperties();
    this._renderLeftVisible();
    this._renderRightVisible();
    this._renderHorizontalAlignment();
  }

  setHorizontalAlignment(alignment) {
    this.setProperty('horizontalAlignment', alignment);
  }

  _renderHorizontalAlignment() {
    this.$container.removeClass('left-aligned right-aligned');
    if (this.horizontalAlignment === CollapseHandle.HorizontalAlignment.LEFT) {
      this.$container.addClass('left-aligned');
    } else if (this.horizontalAlignment === CollapseHandle.HorizontalAlignment.RIGHT) {
      this.$container.addClass('right-aligned');
    }
  }

  setLeftVisible(visible) {
    this.setProperty('leftVisible', visible);
  }

  _renderLeftVisible() {
    this.$left.setVisible(this.leftVisible);
    this._updateVisibilityClasses();
  }

  setRightVisible(visible) {
    this.setProperty('rightVisible', visible);
  }

  _renderRightVisible() {
    this.$right.setVisible(this.rightVisible);
    this._updateVisibilityClasses();
  }

  _updateVisibilityClasses() {
    let bothVisible = this.leftVisible && this.rightVisible;
    this.$container.toggleClass('both-visible', bothVisible);
    this.$left.toggleClass('both-visible', bothVisible);
    this.$right.toggleClass('both-visible', bothVisible);
    this.$container.toggleClass('one-visible', (this.leftVisible || this.rightVisible) && !bothVisible);
  }

  _onLeftMouseDown(event) {
    this.trigger('action', {
      left: true
    });
  }

  _onRightMouseDown(event) {
    this.trigger('action', {
      right: true
    });
  }

  _onMouseDown(event) {
    let target = event.target;
    if (this.$left.isOrHas(target)) {
      this.trigger('action', {
        left: true
      });
      return;
    }
    if (this.$right.isOrHas(target)) {
      this.trigger('action', {
        right: true
      });
      return;
    }

    // If there is only one box visible, trigger also when container was clicked
    // Mainly used to make the pixel on the left clickable, when the handle is visible in bench mode
    if (this.$container.hasClass('one-visible')) {
      this.trigger('action', {
        left: this.leftVisible,
        right: this.rightVisible
      });
    }
  }
}
