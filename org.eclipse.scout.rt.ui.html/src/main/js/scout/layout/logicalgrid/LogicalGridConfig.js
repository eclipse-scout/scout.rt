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
scout.LogicalGridConfig = function() {
  this.widget = null;
};

scout.LogicalGridConfig.prototype.setWidget = function(widget) {
  this.widget = widget;
};

scout.LogicalGridConfig.prototype.getGridColumnCount = function() {
  return this.widget.gridColumnCount;
};

scout.LogicalGridConfig.prototype.getGridWidgets = function() {
  return this.widget.widgets;
};
