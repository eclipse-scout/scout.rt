/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke} from '../../index';

export default class TableToggleRowKeyStroke extends KeyStroke {

  constructor(table) {
    super();
    this.field = table;

    this.which = [keys.SPACE];
    this.stopPropagation = true;
    this.renderingHints.render = false;
  }

  _accept(event) {
    let accepted = super._accept(event);
    return accepted &&
      this.field.checkable &&
      this.field.selectedRows.length;
  }

  handle(event) {
    let selectedRows = this.field.selectedRows.filter(row => {
      return row.enabled;
    });
    // Toggle checked state to 'true', except if every row is already checked
    let checked = selectedRows.some(row => {
      return !row.checked;
    });
    selectedRows.forEach(function(row) {
      this.field.checkRow(row, checked);
    }, this);
  }
}
