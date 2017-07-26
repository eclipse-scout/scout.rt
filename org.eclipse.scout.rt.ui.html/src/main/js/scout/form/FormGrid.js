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
/**
 * Dummy grid for the form which actually just creates the actual grid data for the root group box from the hints.
 */
scout.FormGrid = function() {
  scout.FormGrid.parent.call(this);
};
scout.inherits(scout.FormGrid, scout.LogicalGrid);

scout.FormGrid.prototype._validate = function(form) {
  // The form does not have a real logical grid but needs the gridData anyway (widthInPixel, heightInPixel, see GroupBoxLayout).
  // Grid.w is not relevant for the form, no need to pass a gridColumnCount
  form.rootGroupBox.gridData = scout.GridData.createFromHints(form.rootGroupBox);
};
