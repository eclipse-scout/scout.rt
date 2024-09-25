/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, DesktopNavigationHandle, keys, KeyStroke, scout, ScoutKeyboardEvent} from '../../index';

export class ShrinkNavigationKeyStroke extends KeyStroke {
  declare field: DesktopNavigationHandle;
  desktop: Desktop;

  constructor(handle: DesktopNavigationHandle) {
    super();
    this.field = handle;
    this.desktop = handle.session.desktop;
    this.ctrl = true;
    // FF und Safari use different key codes for this key.
    this.which = [keys.forBrowser(keys.ANGULAR_BRACKET)];
    this.renderingHints.$drawingArea = ($drawingArea, event) => this.desktop.$container;
    this.inheritAccessibility = false;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    // Safari changes the code when ctrl is pressed (it stays the same with cmd).
    return super._accept(event) ||
      (KeyStroke.acceptModifiers(this, event) && scout.isOneOf(event.which, keys.forBrowser(keys.ANGULAR_BRACKET, 'ctrl')));
  }

  protected override _isEnabled(): boolean {
    let enabled = super._isEnabled();
    return enabled && this.field.leftVisible;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.desktop.shrinkNavigation();
  }

  protected override _postRenderKeyBox($drawingArea: JQuery, $keyBox?: JQuery) {
    $keyBox.addClass('navigation-handle-key-box left');

    let handle = this.field;
    let handleOffset = handle.$left.offsetTo(this.desktop.$container);
    let keyBoxLeft = handleOffset.left - $keyBox.outerWidth(true);
    let keyBoxTop = handleOffset.top;

    $keyBox.cssLeft(keyBoxLeft)
      .cssTop(keyBoxTop);
  }
}
