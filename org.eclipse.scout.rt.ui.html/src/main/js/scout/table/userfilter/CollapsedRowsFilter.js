/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.CollapsedRowsFilter = function() {};
scout.inherits(scout.CollapsedRowsFilter, scout.TableFilter);

scout.CollapsedRowsFilter.FILTER_KEY = 'CollapsedRowsFilter';

scout.CollapsedRowsFilter.prototype.createKey = function() {
  return scout.CollapsedRowsFilter.FILTER_KEY;
};

scout.CollapsedRowsFilter.prototype.accept = function(row) {
  var parentRow = row.parentRow;
  while (parentRow) {
    if (!parentRow.expanded) {
      return false;
    }
    parentRow = parentRow.parentRow;
  }
  return true;
};
