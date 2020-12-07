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
import {keys, KeyStroke} from '../../index';

export default class ShrinkNavigationKeyStroke extends KeyStroke {

  constructor(handle) {
    super();
    this.field = handle;
    this.desktop = handle.session.desktop;
    this.ctrl = true;
    // FF und Safari use different key codes for this key. Safari even changes the code when ctrl is pressed (it stays the same with cmd).
    this.which = [keys.forBrowser(keys.ANGULAR_BRACKET), keys.forBrowser(keys.ANGULAR_BRACKET, 'ctrl')];
    this.renderingHints.$drawingArea = ($drawingArea, event) => this.desktop.$container;
  }

  _isEnabled() {
    let enabled = super._isEnabled();
    return enabled && this.field.leftVisible;
  }

  handle(event) {
    this.desktop.shrinkNavigation();
  }

  _postRenderKeyBox($drawingArea, $keyBox) {
    let handleOffset, keyBoxLeft, keyBoxTop,
      handle = this.field;

    $keyBox.addClass('navigation-handle-key-box left');

    handleOffset = handle.$left.offsetTo(this.desktop.$container);
    keyBoxLeft = handleOffset.left - $keyBox.outerWidth(true);
    keyBoxTop = handleOffset.top;

    $keyBox.cssLeft(keyBoxLeft)
      .cssTop(keyBoxTop);
  }
}
