/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Desktop, DesktopNavigationHandle, keys, KeyStroke, scout, ScoutKeyboardEvent} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class ShrinkNavigationKeyStroke extends KeyStroke {
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

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
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
