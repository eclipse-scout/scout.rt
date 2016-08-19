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
scout.FormFieldAdapter = function() {
  scout.FormFieldAdapter.parent.call(this);

  /**
   * Set this property to true when the form-field should stay enabled in offline case.
   * By default the field will be disabled.
   */
  this.enabledWhenOffline = false;

  this._addAdapterProperties(['keyStrokes', 'menus']);
};
scout.inherits(scout.FormFieldAdapter, scout.ModelAdapter);

scout.FormFieldAdapter.prototype._goOffline = function() {
  if (this.enabledWhenOffline) {
    return;
  }
  this._enabledBeforeOffline = this.widget.enabled;
  this.widget.setEnabled(false);
};

scout.FormFieldAdapter.prototype._goOnline = function() {
  if (this.enabledWhenOffline) {
    return;
  }
  this.widget.setEnabled(this._enabledBeforeOffline);
};
