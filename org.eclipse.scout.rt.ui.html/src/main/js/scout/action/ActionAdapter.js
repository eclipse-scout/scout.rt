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
scout.ActionAdapter = function() {
  scout.ActionAdapter.parent.call(this);
  this._addRemoteProperties(['selected']);
};
scout.inherits(scout.ActionAdapter, scout.ModelAdapter);

scout.Action.prototype._goOffline = function() {
  this._enabledBeforeOffline = this.widget.enabled;
  this.widget.setEnabled(false);
};

scout.Action.prototype._goOnline = function() {
  this.widget.setEnabled(this._enabledBeforeOffline);
};
