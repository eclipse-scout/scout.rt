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
scout.OutlineViewButtonAdapter = function() {
  scout.OutlineViewButtonAdapter.parent.call(this);
  this._addAdapterProperties('outline');
};
scout.inherits(scout.OutlineViewButtonAdapter, scout.ViewButtonAdapter);

scout.OutlineViewButtonAdapter.prototype._goOffline = function() {
  // Disable only if outline has not been loaded yet
  if (this.widget.outline) {
    return;
  }
  this._enabledBeforeOffline = this.widget.enabled;
  this.widget.setEnabled(false);
};

scout.OutlineViewButtonAdapter.prototype._goOnline = function() {
  this.widget.setEnabled(this._enabledBeforeOffline);
};
