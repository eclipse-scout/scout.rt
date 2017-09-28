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
scout.OutlineKeyStrokeContext = function(outline) {
  scout.OutlineKeyStrokeContext.parent.call(this);
  this._outline = outline;
};

scout.inherits(scout.OutlineKeyStrokeContext, scout.KeyStrokeContext);

/**
 * Returns true if this event is handled by this context, and if so sets the propagation flags accordingly.
 */
scout.OutlineKeyStrokeContext.prototype.accept = function(event) {
  return !this._outline.inBackground && !this.isFormMenuOpen() && scout.OutlineKeyStrokeContext.parent.prototype.accept.call(this, event);
};

scout.OutlineKeyStrokeContext.prototype.isFormMenuOpen = function() {
  var menus = this._outline.session.desktop.menus;
  return menus.some(function(menu) {
    return menu.popup && menu.popup.$container && menu.popup.$container.isAttached();
  }, this);
};
