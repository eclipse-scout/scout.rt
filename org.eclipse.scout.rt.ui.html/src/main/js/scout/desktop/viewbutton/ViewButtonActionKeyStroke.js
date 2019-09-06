/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.ViewButtonActionKeyStroke = function(action) {
  scout.ViewButtonActionKeyStroke.parent.call(this, action);

};
scout.inherits(scout.ViewButtonActionKeyStroke, scout.ActionKeyStroke);

scout.ViewButtonActionKeyStroke.prototype._postRenderKeyBox = function($drawingArea) {
  if (this.field.iconId && !this.field._isMenuItem) {
    var width = $drawingArea.outerWidth();
    var wKeybox = $drawingArea.find('.key-box').outerWidth();
    var leftKeyBox = width / 2 - wKeybox / 2;
    $drawingArea.find('.key-box').cssLeft(leftKeyBox);
  }
};

scout.ViewButtonActionKeyStroke.prototype.renderKeyBox = function($drawingArea, event) {
  if (this.field._isMenuItem) {
    this.renderingHints.hAlign = scout.HAlign.RIGHT;
  }
  return scout.ViewButtonActionKeyStroke.parent.prototype.renderKeyBox.call(this, $drawingArea, event);
};
