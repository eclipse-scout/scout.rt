/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.FocusAdjacentElementKeyStroke = function(session, field) {
  scout.FocusAdjacentElementKeyStroke.parent.call(this);
  this.session = session;
  this.field = field;
  this.which = [scout.keys.LEFT, scout.keys.RIGHT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
  this.keyStrokeMode = scout.KeyStrokeMode.DOWN;
};
scout.inherits(scout.FocusAdjacentElementKeyStroke, scout.KeyStroke);

scout.FocusAdjacentElementKeyStroke.prototype.handle = function(event) {
  var activeElement = this.field.$container.activeElement(true),
    $focusableElements = this.field.$container.find(':focusable');

  switch (event.which) { // NOSONAR
    case scout.keys.RIGHT:
      if (activeElement === $focusableElements.last()[0]) {
        this.session.focusManager.requestFocus($focusableElements.first());
      } else {
        this.session.focusManager.requestFocus($focusableElements[$focusableElements.index(activeElement) + 1]);
      }

      break;
    case scout.keys.LEFT:
      if (activeElement === $focusableElements.first()[0]) {
        this.session.focusManager.requestFocus($focusableElements.last());
      } else {
        this.session.focusManager.requestFocus($focusableElements[$focusableElements.index(activeElement) - 1]);
      }
      break;
  }
};
