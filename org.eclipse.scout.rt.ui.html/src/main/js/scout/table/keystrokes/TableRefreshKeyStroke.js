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
scout.TableRefreshKeyStroke = function(table) {
  scout.TableRefreshKeyStroke.parent.call(this);
  this.field = table;
  this.which = [scout.keys.F5];
  this.renderingHints.offset = 14;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.footer ? this.field.footer._$infoLoad.find('.table-info-button') : null;
  }.bind(this);
};
scout.inherits(scout.TableRefreshKeyStroke, scout.KeyStroke);

scout.TableRefreshKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TableRefreshKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && this.field.hasReloadHandler;
};

scout.TableRefreshKeyStroke.prototype.handle = function(event) {
  this.field.reload();
};
