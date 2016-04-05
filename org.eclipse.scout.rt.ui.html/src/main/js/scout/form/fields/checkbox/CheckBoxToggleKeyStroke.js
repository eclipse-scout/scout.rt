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
scout.CheckBoxToggleKeyStroke = function(checkbox) {
  scout.CheckBoxToggleKeyStroke.parent.call(this);
  this.field = checkbox;
  this.which = [scout.keys.SPACE];
  if (!(this.field.owner instanceof scout.Table)) {
    this.which.push(scout.keys.ENTER);
  }
  this.renderingHints.render = false;
  this.stopPropagation = true;
};
scout.inherits(scout.CheckBoxToggleKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.CheckBoxToggleKeyStroke.prototype.handle = function(event) {
  this.field.toggleChecked();
};
