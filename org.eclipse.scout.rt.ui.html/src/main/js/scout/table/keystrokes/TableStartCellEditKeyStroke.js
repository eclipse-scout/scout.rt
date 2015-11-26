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
scout.TableStartCellEditKeyStroke = function(table) {
  scout.TableStartCellEditKeyStroke.parent.call(this);
  this.field = table;
  this.ctrl = true;
  this.which = [scout.keys.ENTER];
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var editPosition = event._editPosition;
    return this.field.$cell(editPosition.column, editPosition.row.$row);
  }.bind(this);
};
scout.inherits(scout.TableStartCellEditKeyStroke, scout.KeyStroke);

scout.TableStartCellEditKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TableStartCellEditKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var selectedRows = this.field.selectedRows;
  if (!selectedRows.length) {
    return false;
  }

  var position = this.field.nextEditableCellPosForRow(0, selectedRows[0]);
  if (position) {
    event._editPosition = position;
    return true;
  } else {
    return false;
  }
};

scout.TableStartCellEditKeyStroke.prototype.handle = function(event) {
  var editPosition = event._editPosition;
  this.field.prepareCellEdit(editPosition.row.id, editPosition.column.id, true);
};
