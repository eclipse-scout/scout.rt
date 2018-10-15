/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.lookupField = {

  /**
   * Creates a table-row for the given lookup-row.
   *
   * @returns {object} table-row model
   */
  createTableRow: function(lookupRow) {
    var
      cell = scout.create('Cell', {
        text: lookupRow.text
      }),
      row = {
        cells: [cell],
        lookupRow: lookupRow
      };
    if (lookupRow.iconId) {
      cell.iconId = lookupRow.iconId;
    }
    if (lookupRow.tooltipText) {
      cell.tooltipText = lookupRow.tooltipText;
    }
    if (lookupRow.backgroundColor) {
      cell.backgroundColor = lookupRow.backgroundColor;
    }
    if (lookupRow.foregroundColor) {
      cell.foregroundColor = lookupRow.foregroundColor;
    }
    if (lookupRow.font) {
      cell.font = lookupRow.font;
    }
    if (lookupRow.enabled === false) {
      row.enabled = false;
    }
    if (lookupRow.active === false) {
      row.active = false;
    }
    if (lookupRow.cssClass) {
      row.cssClass = lookupRow.cssClass;
    }
    return row;
  }

};
