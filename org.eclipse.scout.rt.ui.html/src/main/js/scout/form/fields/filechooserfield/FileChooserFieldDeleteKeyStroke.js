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
scout.FileChooserFieldDeleteKeyStroke = function(field) {
  scout.FileChooserFieldDeleteKeyStroke.parent.call(this);
  this.field = field;
  this.which = [scout.keys.DELETE];
  this.stopPropagation = true;

  this.renderingHints.hAlign = scout.hAlign.LEFT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$fieldContainer;
  }.bind(this);
};
scout.inherits(scout.FileChooserFieldDeleteKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.FileChooserFieldDeleteKeyStroke.prototype.handle = function(event) {
  this.field.fileInput.clear();
};
