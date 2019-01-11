/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ResponsiveHandler = function(widget, options) {
  options = options || {};

  this.widget = widget;
  this.compactThreshold = scout.nvl(options.compactThreshold, -1);
  this.condensedThreshold = scout.nvl(options.condensedThreshold, -1);

  this.oldState = scout.ResponsiveManager.ResponsiveState.NORMAL;
  this.state = scout.ResponsiveManager.ResponsiveState.NORMAL;
  this.allowedStates = [scout.ResponsiveManager.ResponsiveState.NORMAL, scout.ResponsiveManager.ResponsiveState.COMPACT];

  // Event handlers
  this._destroyHandler = this._onDestroy.bind(this);
};

scout.ResponsiveHandler.prototype.init = function() {
  this.widget.one('destroy', this._destroyHandler);
};

scout.ResponsiveHandler.prototype.destroy = function() {
  this.widget.off('destroy', this._destroyHandler);
};

scout.ResponsiveHandler.prototype.getCompactThreshold = function() {
  return this.compactThreshold;
};

scout.ResponsiveHandler.prototype.getCondensedThreshold = function() {
  return this.condensedThreshold;
};

scout.ResponsiveHandler.prototype.active = function() {
  return true;
};

scout.ResponsiveHandler.prototype.setAllowedStates = function(allowedStates) {
  this.allowedStates = allowedStates;
};

scout.ResponsiveHandler.prototype.acceptState = function(newState) {
  return scout.arrays.containsAny(this.allowedStates, newState);
};

/* --- TRANSFORMATIONS ------------------------------------------------------------- */

scout.ResponsiveHandler.prototype._fromNormalToOtherState = function() {
  var ResponsiveState = scout.ResponsiveManager.ResponsiveState;
  return this.oldState === ResponsiveState.NORMAL &&
    (this.state === ResponsiveState.CONDENSED || this.state === ResponsiveState.COMPACT);
};

scout.ResponsiveHandler.prototype._storeFieldProperty = function(widget, property, value) {
  widget._setProperty('responsive-' + property, value);
};

scout.ResponsiveHandler.prototype._hasFieldProperty = function(widget, property) {
  return widget.hasOwnProperty('responsive-' + property);
};

scout.ResponsiveHandler.prototype._getFieldProperty = function(widget, property) {
  return widget['responsive-' + property];
};

scout.ResponsiveHandler.prototype.transform = function(newState, force) {
  if (this.state === newState && !force) {
    return false;
  }

  this.oldState = this.state;
  this.state = newState;
  this._transform();
  return true;
};

scout.ResponsiveHandler.prototype._transform = function() {};

/* --- HANDLERS ------------------------------------------------------------- */
scout.ResponsiveHandler.prototype._onDestroy = function(event) {
  this.destroy();
};
