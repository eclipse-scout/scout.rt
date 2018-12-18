/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.PopupManager = function() {
  scout.PopupManager.parent.call(this);
  this.popups = [];
  this._addWidgetProperties(['popups']);
  this._addPreserveOnPropertyChangeProperties(['popups']);
};
scout.inherits(scout.PopupManager, scout.Widget);

scout.PopupManager.prototype._init = function(model) {
  scout.PopupManager.parent.prototype._init.call(this, model);
  this.session.layoutValidator.schedulePostValidateFunction(function() {
    this._openPopups(this.popups);
  }.bind(this));
};

scout.PopupManager.prototype.setPopups = function(popups) {
  this.setProperty('popups', popups);
};

scout.PopupManager.prototype._setPopups = function(popups) {
  this._openPopups(scout.arrays.diff(popups, this.popups));
  this._destroyPopups(scout.arrays.diff(this.popups, popups));
  this._setProperty('popups', popups);
};

scout.PopupManager.prototype._openPopups = function(popups) {
  popups.forEach(function(popup) {
    popup.open(popup.session.$entryPoint);
  });
};

scout.PopupManager.prototype._destroyPopups = function(popups) {
  popups.forEach(function(popup) {
    popup.destroy();
  });
};
