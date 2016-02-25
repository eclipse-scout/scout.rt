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
scout.DesktopBench = function() {
  scout.DesktopBench.parent.call(this);
};
scout.inherits(scout.DesktopBench, scout.Widget);

scout.DesktopBench.prototype._init = function(model) {
  scout.DesktopBench.parent.prototype._init.call(this, model);
  this.desktop = this.session.desktop;
};

scout.DesktopBench.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.DesktopBench.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.$container;
  this.desktopKeyStrokeContext.registerKeyStroke(this.desktop.keyStrokes);
};

scout.DesktopBench.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-bench');
  this.$container.toggleClass('has-header', this.desktop._hasHeader());
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopBenchLayout(this));
  this.htmlComp.pixelBasedSizing = false;

  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.DesktopBench.prototype._remove = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.DesktopBench.parent.prototype._remove.call(this);
};
