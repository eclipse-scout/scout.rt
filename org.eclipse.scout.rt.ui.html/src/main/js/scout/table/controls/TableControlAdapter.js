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
scout.TableControlAdapter = function() {
  scout.TableControlAdapter.parent.call(this);
};
scout.inherits(scout.TableControlAdapter, scout.ActionAdapter);

scout.TableControlAdapter.prototype._goOffline = function() {
  if (this.widget.isContentAvailable()) {
    return;
  }
  this._enabledBeforeOffline = this.widget.enabled;
  this.widget.setEnabled(false);
};

scout.TableControlAdapter.prototype._goOnline = function() {
  if (this.widget.isContentAvailable()) {
    return;
  }
  this.widget.setEnabled(this._enabledBeforeOffline);
};
