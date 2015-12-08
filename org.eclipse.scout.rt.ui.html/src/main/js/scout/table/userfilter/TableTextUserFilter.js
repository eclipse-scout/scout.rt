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
scout.TableTextUserFilter = function(table, column) {
  scout.TableTextUserFilter.parent.call(this, table);
  this.filterType = scout.TableTextUserFilter.Type;
};
scout.inherits(scout.TableTextUserFilter, scout.TableUserFilter);

scout.TableTextUserFilter.Type = 'text';

scout.TableTextUserFilter.prototype.createAddFilterEventData = function() {
  var data = scout.ColumnUserFilter.parent.prototype.createAddFilterEventData.call(this);
  return $.extend(data, {
    text: this.text
  });
};

scout.TableTextUserFilter.prototype.createLabel = function() {
  return this.text;
};

scout.TableTextUserFilter.prototype.accept = function(row) {
  var rowText = '';
  for (var i = 0; i < row.cells.length; i++) {
    var cell = row.cells[i];
    rowText += cell.text;
  }
  rowText = rowText.toLowerCase();
  return rowText.indexOf(this.text) > -1;
};
