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
import {KeyStroke} from '../../index';
import {keys} from '../../index';

export default class ShrinkNavigationKeyStroke extends KeyStroke {

constructor(handle) {
  super();
  this.field = handle;
  this.desktop = handle.session.desktop;
  this.ctrl = true;
  this.which = [keys.ANGULAR_BRACKET];
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.desktop.$container;
  }.bind(this);
}


_isEnabled() {
  var enabled = super._isEnabled();
  return enabled && this.field.leftVisible;
}

handle(event) {
  this.desktop.shrinkNavigation();
}

_postRenderKeyBox($drawingArea, $keyBox) {
  var handleOffset, keyBoxLeft, keyBoxTop,
    handle = this.field;

  $keyBox.addClass('navigation-handle-key-box left');

  handleOffset = handle.$left.offsetTo(this.desktop.$container);
  keyBoxLeft = handleOffset.left - $keyBox.outerWidth(true);
  keyBoxTop = handleOffset.top;

  $keyBox.cssLeft(keyBoxLeft)
    .cssTop(keyBoxTop);
}
}
