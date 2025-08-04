/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Widget} from '../index';

export class GlassPane extends Widget {
  protected _active: boolean;

  protected override _render() {
    this.$container = this.$parent.appendDiv('glasspane');

    let cssPosition = this.$parent.css('position');
    if (cssPosition === 'static') {
      this.$parent.css('position', 'relative');
    }

    this.activate();
  }

  protected override _remove() {
    this.deactivate();
    super._remove();
  }

  /**
   * Adds the class `glasspane-parent` to the parent which disables `pointer-events` to prevent mouse interactions with the elements underneath.
   * Also registers the `$parent` as glasspane target in the focus manager so it cannot gain focus and keystrokes are blocked.
   */
  activate() {
    if (this._active) {
      return;
    }
    this.$parent.addClass('glasspane-parent');
    this.session.focusManager.registerGlassPaneTarget(this.$parent);
    this.$container.removeClass('deactivated');
    this._active = true;
  }

  /**
   * Reverts the changes in {@link activate} and adds the class `deactivated` to the glasspane
   * so that it doesn't have any effect on focus, keystrokes and mouse interactions anymore but is still rendered.
   *
   * This is useful if the glasspane should be deactivated but persist for some time, e.g. during a remove animation.
   */
  deactivate() {
    if (!this._active) {
      return;
    }
    this.$parent.removeClass('glasspane-parent');
    this.session.focusManager.unregisterGlassPaneTarget(this.$parent);
    if (!this.removing) {
      this.$container.addClass('deactivated');
    }
    this._active = false;
  }
}
