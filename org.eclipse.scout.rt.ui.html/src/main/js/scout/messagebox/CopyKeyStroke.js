/*******************************************************************************
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.CopyKeyStroke = function(field) {
  scout.CopyKeyStroke.parent.call(this);
  this.field = field;
  this.which = [scout.keys.C];
  this.ctrl = true;
  this.preventDefault = false;
  this.renderingHints.render = true;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return field.$container;
  };
};
scout.inherits(scout.CopyKeyStroke, scout.KeyStroke);

scout.CopyKeyStroke.prototype.handle = function(event) {
  this.field.copy();
};
