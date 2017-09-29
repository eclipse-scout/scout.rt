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
scout.GroupBoxGridConfig = function() {
  scout.GroupBoxGridConfig.parent.call(this);
};
scout.inherits(scout.GroupBoxGridConfig, scout.LogicalGridConfig);

scout.GroupBoxGridConfig.prototype.getGridWidgets = function() {
  return this.widget.controls;
};

scout.GroupBoxGridConfig.prototype.getGridColumnCount = function() {
  var gridColumns = -1,
    widget = this.widget;

  do {
    gridColumns = scout.nvl(widget.gridColumnCount, gridColumns);
  } while (gridColumns < 0 && widget.getParentGroupBox && (widget = widget.getParentGroupBox()));

  return gridColumns < 0 ? 2 : gridColumns;
};
