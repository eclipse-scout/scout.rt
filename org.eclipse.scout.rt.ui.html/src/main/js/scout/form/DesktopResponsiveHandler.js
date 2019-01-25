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
scout.DesktopResponsiveHandler = function() {
  scout.DesktopResponsiveHandler.parent.call(this);

  this.compactThreshold = 500;
  this.allowedStates = [scout.ResponsiveManager.ResponsiveState.NORMAL, scout.ResponsiveManager.ResponsiveState.COMPACT];
};
scout.inherits(scout.DesktopResponsiveHandler, scout.ResponsiveHandler);

/* --- TRANSFORMATIONS ------------------------------------------------------------- */
/**
 * @Override
 */
scout.DesktopResponsiveHandler.prototype._transform = function() {
  scout.DesktopResponsiveHandler.parent.prototype._transform.call(this);

  if (this.state === scout.ResponsiveManager.ResponsiveState.COMPACT) {
    this._storeFieldProperty(this.widget, 'navigationVisible', this.widget.navigationVisible);
    this.widget.setNavigationVisible(false);
  } else {
    if (this._hasFieldProperty(this.widget, 'navigationVisible')) {
      this.widget.setNavigationVisible(this._getFieldProperty(this.widget, 'navigationVisible'));
    }
  }
};
