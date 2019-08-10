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
scout.TabAreaRightKeyStroke = function(tabArea) {
  scout.TabAreaRightKeyStroke.parent.call(this);
  this.field = tabArea;
  this.which = [scout.keys.RIGHT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
  this.keyStrokeMode = scout.KeyStrokeMode.DOWN;
};
scout.inherits(scout.TabAreaRightKeyStroke, scout.KeyStroke);

scout.TabAreaRightKeyStroke.prototype.handle = function(event) {
  this.field.selectNextTab(true);
};
