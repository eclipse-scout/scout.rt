/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.EnlargeNavigationKeyStroke = function(handle) {
  scout.EnlargeNavigationKeyStroke.parent.call(this);
  this.field = handle;
  this.desktop = handle.session.desktop;
  this.ctrl = true;
  this.shift = true;
  this.which = [scout.keys.ANGULAR_BRACKET];
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.desktop.$container;
  }.bind(this);
};
scout.inherits(scout.EnlargeNavigationKeyStroke, scout.KeyStroke);

scout.EnlargeNavigationKeyStroke.prototype.handle = function(event) {
  this.desktop.enlargeNavigation();
};

scout.EnlargeNavigationKeyStroke.prototype._postRenderKeyBox = function($drawingArea, $keyBox) {
  var handleOffset, keyBoxLeft, keyBoxTop,
    handle = this.field;

  $keyBox.addClass('navigation-handle-key-box right');

  handleOffset = handle.$right.offsetTo(this.desktop.$container),
  keyBoxLeft = handleOffset.left + handle.$right.outerWidth(),
  keyBoxTop = handleOffset.top;

  $keyBox.cssLeft(keyBoxLeft)
    .cssTop(keyBoxTop);
};
