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
scout.TileTableHierarchyFilter = function(model) {
  scout.TileTableHierarchyFilter.parent.call(this);
  $.extend(this, model);
};
scout.inherits(scout.TileTableHierarchyFilter, scout.TableFilter);

scout.TileTableHierarchyFilter.prototype.createKey = function() {
  return 'TileTableHierarchyFilter';
};

scout.TileTableHierarchyFilter.prototype.accept = function(row) {
  return !row.parentRow;
};

scout.TileTableHierarchyFilter.prototype.createLabel = function() {
  return this.table.session.text('ui.TileView');
};
