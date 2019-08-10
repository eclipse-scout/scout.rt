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
scout.TableNavigationExpandKeyStroke = function(table) {
  scout.TableNavigationExpandKeyStroke.parent.call(this, table);
  this.field = table;
  this.which = [scout.keys.ADD, scout.keys.RIGHT];
  this.renderingHints.text = '+';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var row = this.field.selectedRows[0];
    if (row) {
      return row.$row;
    }
  }.bind(this);
};
scout.inherits(scout.TableNavigationExpandKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationExpandKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TableNavigationExpandKeyStroke.parent.prototype._accept.call(this, event),
    selectedRow = this.field.selectedRows[0];
  if (!accepted) {
    return false;
  }
  if (!selectedRow) {
    return false;
  }
  return selectedRow._expandable;
};

scout.TableNavigationExpandKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    selectedRow = this.field.selectedRows[0],
    visibleChildRows;
  if (!selectedRow) {
    return;
  }
  if (selectedRow._expandable) {
    if (selectedRow.expanded) {
      // select first child
      visibleChildRows = this.field.visibleChildRows(selectedRow);
      table.selectRow(visibleChildRows[0]);
      table.selectionHandler.lastActionRow = visibleChildRows[0];
    } else {
      // expand
      table.expandRow(selectedRow);
    }
  }
};
