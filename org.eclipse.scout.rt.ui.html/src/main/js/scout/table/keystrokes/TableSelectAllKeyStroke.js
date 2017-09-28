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
scout.TableSelectAllKeyStroke = function(table) {
  scout.TableSelectAllKeyStroke.parent.call(this);
  this.field = table;
  this.ctrl = true;
  this.which = [scout.keys.A];
  this.renderingHints.offset = 14;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.footer ? this.field.footer._$infoSelection.find('.table-info-button') : null;
  }.bind(this);
};
scout.inherits(scout.TableSelectAllKeyStroke, scout.KeyStroke);

scout.TableSelectAllKeyStroke.prototype.handle = function(event) {
  var table = this.field;
  table.toggleSelection();
  table.selectionHandler.lastActionRow = null;
};
