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
scout.CloseKeyStroke = function(field, $drawingArea) {
  scout.CloseKeyStroke.parent.call(this);
  this.field = field;
  this.which = [scout.keys.ESC];
  this.renderingHints.render = true;
  this.stopPropagation = true;
  this.renderingHints = {
    render: !!$drawingArea,
    $drawingArea: $drawingArea
  };
};
scout.inherits(scout.CloseKeyStroke, scout.KeyStroke);

scout.CloseKeyStroke.prototype.handle = function(event) {
  this.field.close();
};
