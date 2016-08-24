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
scout.DesktopNavigationHandle = function() {
  scout.DesktopNavigationHandle.parent.call(this);
};
scout.inherits(scout.DesktopNavigationHandle, scout.CollapseHandle);

scout.DesktopNavigationHandle.prototype._initKeyStrokeContext = function() {
  scout.DesktopNavigationHandle.parent.prototype._initKeyStrokeContext.call(this);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.$container;
  this.desktopKeyStrokeContext.registerKeyStroke([
    new scout.ShrinkNavigationKeyStroke(this),
    new scout.EnlargeNavigationKeyStroke(this)
  ]);
};

scout.DesktopNavigationHandle.prototype._render = function($parent) {
  scout.DesktopNavigationHandle.parent.prototype._render.call(this, $parent);
  this.$container.addClass('desktop-navigation-handle');
  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.DesktopNavigationHandle.prototype._remove = function() {
  scout.DesktopNavigationHandle.parent.prototype._remove.call(this);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
};
