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
scout.TabAreaLeftKeyStroke = function(tabArea) {
  scout.TabAreaLeftKeyStroke.parent.call(this);
  this.field = tabArea;
  this.which = [scout.keys.LEFT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
  this.keyStrokeMode = scout.KeyStroke.Mode.DOWN;
};
scout.inherits(scout.TabAreaLeftKeyStroke, scout.KeyStroke);

scout.TabAreaLeftKeyStroke.prototype.handle = function(event) {
  this.field.selectPreviousTab(true);
};
