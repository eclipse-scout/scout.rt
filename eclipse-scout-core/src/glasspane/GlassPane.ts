/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Widget} from '../index';
import $ from 'jquery';
import MouseDownEvent = JQuery.MouseDownEvent;

export class GlassPane extends Widget {

  protected override _render() {
    this.$container = this.$parent
      .appendDiv('glasspane')
      .on('mousedown', this._onMouseDown.bind(this));

    this.$parent.addClass('glasspane-parent');
    let cssPosition = this.$parent.css('position');
    if (cssPosition === 'static') {
      this.$parent.css('position', 'relative');
    }

    // Register 'glassPaneTarget' in focus manager.
    this.session.focusManager.registerGlassPaneTarget(this.$parent);
  }

  protected override _remove() {
    this.$parent.removeClass('glasspane-parent');
    this.session.focusManager.unregisterGlassPaneTarget(this.$parent);
    super._remove();
  }

  protected _onMouseDown(event: MouseDownEvent<HTMLDivElement>) {
    // Won't be executed if pointer events is set to none. But acts as safety net if pointer events are not supported or even removed by the user
    $.suppressEvent(event);
  }
}
