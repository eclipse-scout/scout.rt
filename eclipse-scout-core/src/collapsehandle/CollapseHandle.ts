/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, CollapseHandleEventMap, CollapseHandleModel, EnumObject, Widget} from '../index';

export type CollapseHandleHorizontalAlignment = EnumObject<typeof CollapseHandle.HorizontalAlignment>;

export class CollapseHandle extends Widget implements CollapseHandleModel {
  declare model: CollapseHandleModel;
  declare eventMap: CollapseHandleEventMap;
  declare self: CollapseHandle;

  leftVisible: boolean;
  rightVisible: boolean;
  horizontalAlignment: CollapseHandleHorizontalAlignment;
  $left: JQuery;
  $right: JQuery;

  constructor() {
    super();
    this.leftVisible = true;
    this.rightVisible = true;
    this.horizontalAlignment = CollapseHandle.HorizontalAlignment.LEFT;
    this.$left = null;
    this.$right = null;
  }

  static HorizontalAlignment = {
    LEFT: 'left',
    RIGHT: 'right'
  } as const;

  protected override _render() {
    this.$container = this.$parent.appendDiv('collapse-handle');
    this.$container.on('mousedown', this._onMouseDown.bind(this));
    aria.role(this.$container, 'none'); // ignore this container, the buttons are important

    this.$left = this.$container.appendDiv('collapse-handle-body left');
    aria.role(this.$left, 'button');
    aria.label(this.$left, this.session.text('ui.Collapse'));
    this.$right = this.$container.appendDiv('collapse-handle-body right');
    aria.role(this.$right, 'button');
    aria.label(this.$right, this.session.text('ui.Expand'));
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderLeftVisible();
    this._renderRightVisible();
    this._renderHorizontalAlignment();
  }

  setHorizontalAlignment(alignment: CollapseHandleHorizontalAlignment) {
    this.setProperty('horizontalAlignment', alignment);
  }

  protected _renderHorizontalAlignment() {
    this.$container.removeClass('left-aligned right-aligned');
    if (this.horizontalAlignment === CollapseHandle.HorizontalAlignment.LEFT) {
      this.$container.addClass('left-aligned');
    } else if (this.horizontalAlignment === CollapseHandle.HorizontalAlignment.RIGHT) {
      this.$container.addClass('right-aligned');
    }
  }

  setLeftVisible(visible: boolean) {
    this.setProperty('leftVisible', visible);
  }

  protected _renderLeftVisible() {
    this.$left.setVisible(this.leftVisible);
    this._updateVisibilityClasses();
  }

  setRightVisible(visible: boolean) {
    this.setProperty('rightVisible', visible);
  }

  protected _renderRightVisible() {
    this.$right.setVisible(this.rightVisible);
    this._updateVisibilityClasses();
  }

  protected _updateVisibilityClasses() {
    let bothVisible = this.leftVisible && this.rightVisible;
    this.$container.toggleClass('both-visible', bothVisible);
    this.$left.toggleClass('both-visible', bothVisible);
    this.$right.toggleClass('both-visible', bothVisible);
    this.$container.toggleClass('one-visible', (this.leftVisible || this.rightVisible) && !bothVisible);
    aria.hidden(this.$left, !this.leftVisible || null);
    aria.hidden(this.$right, !this.rightVisible || null);
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent) {
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
