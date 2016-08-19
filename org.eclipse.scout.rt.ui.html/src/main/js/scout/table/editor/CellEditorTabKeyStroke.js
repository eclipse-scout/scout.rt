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
scout.CellEditorTabKeyStroke = function(popup) {
  scout.CellEditorTabKeyStroke.parent.call(this);
  this.field = popup;
  this.which = [scout.keys.TAB];
  this.shift = undefined; // to tab forward and backward
};
scout.inherits(scout.CellEditorTabKeyStroke, scout.KeyStroke);

scout.CellEditorTabKeyStroke.prototype._accept = function(event) {
  var accepted = scout.CellEditorTabKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && !this.field.completeCellEditRequested; // Make sure events (complete, prepare) don't get sent twice since it will lead to exceptions. This may happen if user presses and holds the tab key.
};

scout.CellEditorTabKeyStroke.prototype.handle = function(event) {
  var pos,
    backwards = event.shiftKey,
    table = this.field.table,
    column = this.field.column,
    row = this.field.row;

  this.field.completeEdit();

  pos = table.nextEditableCellPos(column, row, backwards);
  if (pos) {
    table.prepareCellEdit(pos.column, pos.row);
  }
};
