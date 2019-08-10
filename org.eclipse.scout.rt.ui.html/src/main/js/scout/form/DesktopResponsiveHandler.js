/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.DesktopResponsiveHandler = function() {
  scout.DesktopResponsiveHandler.parent.call(this);

  this.compactThreshold = 500;
  this.allowedStates = [scout.ResponsiveManager.ResponsiveState.NORMAL, scout.ResponsiveManager.ResponsiveState.COMPACT];
};
scout.inherits(scout.DesktopResponsiveHandler, scout.ResponsiveHandler);

scout.DesktopResponsiveHandler.prototype.init = function(model) {
  scout.DesktopResponsiveHandler.parent.prototype.init.call(this, model);

  this._registerTransformation('navigationVisible', this._transformNavigationVisible);
  this._enableTransformation(scout.ResponsiveManager.ResponsiveState.COMPACT, 'navigationVisible');
};

/* --- TRANSFORMATIONS ------------------------------------------------------------- */

scout.DesktopResponsiveHandler.prototype._transformNavigationVisible = function(widget, apply) {
  if (apply) {
    this._storeFieldProperty(widget, 'navigationVisible', widget.navigationVisible);
    widget.setNavigationVisible(false);
  } else {
    if (this._hasFieldProperty(widget, 'navigationVisible')) {
      widget.setNavigationVisible(this._getFieldProperty(widget, 'navigationVisible'));
    }
  }
};
