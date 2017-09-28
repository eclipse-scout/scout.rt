/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.StringFieldLayout = function(stringField) {
  scout.StringFieldLayout.parent.call(this, stringField);
};
scout.inherits(scout.StringFieldLayout, scout.FormFieldLayout);

scout.StringFieldLayout.prototype._layoutClearableIcon = function(formField, fieldBounds, right, top) {
  if (formField.$icon && formField.$icon.isVisible()) {
    right += scout.graphics.prefSize(formField.$icon, true).width;
  }
  scout.StringFieldLayout.parent.prototype._layoutClearableIcon.call(this, formField, fieldBounds, right, top);
};
