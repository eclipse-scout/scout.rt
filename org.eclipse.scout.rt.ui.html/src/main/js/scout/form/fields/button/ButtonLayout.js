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
/**
 * Button Layout, for fields with a button.
 */
scout.ButtonLayout = function(button) {
  scout.ButtonLayout.parent.call(this, button);
  this.button = button;
};
scout.inherits(scout.ButtonLayout, scout.FormFieldLayout);

scout.ButtonLayout.prototype.naturalSize = function(formField, options) {
  return scout.graphics.prefSize(this.button.$field, options);
  // XXX Why this layout???
};
