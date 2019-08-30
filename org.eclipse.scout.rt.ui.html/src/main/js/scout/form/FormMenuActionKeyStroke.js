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
scout.FormMenuActionKeyStroke = function(action) {
  scout.FormMenuActionKeyStroke.parent.call(this, action);
};
scout.inherits(scout.FormMenuActionKeyStroke, scout.ActionKeyStroke);

scout.FormMenuActionKeyStroke.prototype.handle = function(event) {
  this.field.toggle();
};

scout.FormMenuActionKeyStroke.prototype._postRenderKeyBox = function($drawingArea) {
  if (this.field.iconId) {
    var wIcon = $drawingArea.find('.icon').width();
    var wKeybox = $drawingArea.find('.key-box').outerWidth();
    var containerPadding = $drawingArea.cssPaddingLeft();
    var leftKeyBox = wIcon / 2 - wKeybox / 2 + containerPadding;
    $drawingArea.find('.key-box').cssLeft(leftKeyBox);
  }
};
