/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {strings, TableFilter} from '../index';

export default class TextTableFilter extends TableFilter {

  /**
   * @param {function} [textSupplier] - An optional function that extracts the text from
   *          a table row. The default expects the row object to have a "lookupRow"
   *          property and returns the "text" property.
   */
  constructor(textSupplier) {
    super();
    this.acceptedText = null;
    this.textSupplier = textSupplier || (row => row.lookupRow && row.lookupRow.text);
    this.active = true;
    this.alwaysAcceptedRowIds = [];
  }

  accept(row) {
    if (!this.active || strings.empty(this.acceptedText) || this.alwaysAcceptedRowIds.indexOf(row.id) !== -1) {
      return true;
    }
    let text = this.textSupplier(row);
    if (strings.empty(text)) {
      return false;
    }
    return strings.contains(text.toLowerCase(), this.acceptedText.toLowerCase());
  }

  setAcceptedText(acceptedText) {
    this.acceptedText = acceptedText;
  }

  setActive(active) {
    this.active = active;
  }

  setAlwaysAcceptedRowIds(...rowIds) {
    this.alwaysAcceptedRowIds = rowIds;
  }
}
