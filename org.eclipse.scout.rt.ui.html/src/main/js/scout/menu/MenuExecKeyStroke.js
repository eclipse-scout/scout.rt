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
scout.MenuExecKeyStroke = function(menu) {
  scout.MenuExecKeyStroke.parent.call(this);
  this.field = menu;
  this.which = [scout.keys.SPACE, scout.keys.ENTER];
  this.stopPropagation = true;

  this.renderingHints.offset = 16;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$container;
  }.bind(this);
};
scout.inherits(scout.MenuExecKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.MenuExecKeyStroke.prototype.handle = function(event) {
  this.field.doAction();
};
