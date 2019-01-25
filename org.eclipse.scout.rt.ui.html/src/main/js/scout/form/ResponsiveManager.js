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
scout.ResponsiveManager = function() {
  this.active = true;
  this.globalState = null;

  this._responsiveHandlers = [];
};

scout.ResponsiveManager.ResponsiveState = {
  NORMAL: 'normal',
  CONDENSED: 'condensed',
  COMPACT: 'compact'
};

scout.ResponsiveManager.prototype.init = function() {};

scout.ResponsiveManager.prototype.destroy = function() {
  this._responsiveHandlers.forEach(function(handler) {
    handler.destroy();
  }.bind(this));
};

/**
 * Sets the responsive manager to active or inactive globally. Default is active.
 */
scout.ResponsiveManager.prototype.setActive = function(active) {
  this.active = active;
};

/**
 * Set a global responsive state. This state will always be set. Resizing will no longer result in a different responsive state.
 *
 * @param {string} responsive state (scout.ResponsiveManager.ResponsiveState)
 */
scout.ResponsiveManager.prototype.setGlobalState = function(globalState) {
  this.globalState = globalState;
};

/**
 * Checks if the form is smaller than the preferred width of the form. If this is reached, the fields will
 * be transformed to ensure better readability.
 */
scout.ResponsiveManager.prototype.handleResponsive = function(target, width) {
  if (!this.active) {
    return false;
  }

  if (!target.responsiveHandler || !target.responsiveHandler.active()) {
    return false;
  }

  var newState;
  var state = target.responsiveHandler.state;
  if (this.globalState) {
    newState = this.globalState;
  } else if (width < target.responsiveHandler.getCompactThreshold() && target.responsiveHandler.acceptState(scout.ResponsiveManager.ResponsiveState.COMPACT)) {
    newState = scout.ResponsiveManager.ResponsiveState.COMPACT;
  } else {
    if (state === scout.ResponsiveManager.ResponsiveState.COMPACT) {
      target.responsiveHandler.transform(scout.ResponsiveManager.ResponsiveState.CONDENSED);
    }
    if (width < target.responsiveHandler.getCondensedThreshold() && target.responsiveHandler.acceptState(scout.ResponsiveManager.ResponsiveState.CONDENSED)) {
      newState = scout.ResponsiveManager.ResponsiveState.CONDENSED;
    } else {
      newState = scout.ResponsiveManager.ResponsiveState.NORMAL;
    }
  }

  return target.responsiveHandler.transform(newState);
};

scout.ResponsiveManager.prototype.reset = function(target, force) {
  if (!this.active) {
    return;
  }

  if ((!target.responsiveHandler || !target.responsiveHandler.active()) && !force) {
    return false;
  }

  target.responsiveHandler.transform(scout.ResponsiveManager.ResponsiveState.NORMAL, force);
};

scout.ResponsiveManager.prototype.registerHandler = function(target, handler) {
  if (target.responsiveHandler) {
    target.responsiveHandler.destroy();
  }
  target.responsiveHandler = handler;
};

scout.ResponsiveManager.prototype.unregisterHandler = function(target) {
  if (target.responsiveHandler) {
    target.responsiveHandler.destroy();
    target.responsiveHandler = null;
  }
};

scout.responsiveManager = new scout.ResponsiveManager();
