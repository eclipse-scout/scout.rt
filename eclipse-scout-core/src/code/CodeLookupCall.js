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
import {codes, scout, StaticLookupCall, strings} from '../index';

export default class CodeLookupCall extends StaticLookupCall {

  constructor() {
    super();
    this.codeType = null;
  }

  _lookupRowByKey(key) {
    let codeType = codes.codeType(this.codeType, true);
    if (!codeType) {
      return null;
    }
    return this._createLookupRow(codeType.optGet(key));
  }

  _lookupRowsByAll() {
    return this._collectLookupRows();
  }

  _lookupRowsByText(text) {
    return this._collectLookupRows(lookupRow => {
      let lookupRowText = lookupRow.text || '';
      return strings.startsWith(lookupRowText.toLowerCase(), text.toLowerCase());
    });
  }

  _lookupRowsByRec(rec) {
    return this._collectLookupRows(lookupRow => {
      return lookupRow.parentKey === rec;
    });
  }

  _collectLookupRows(predicate) {
    let codeType = codes.codeType(this.codeType, true);
    if (!codeType) {
      return [];
    }
    let lookupRows = [];
    codeType.visit(code => {
      let lookupRow = this._createLookupRow(code);
      if (!predicate || predicate(lookupRow)) {
        lookupRows.push(lookupRow);
      }
    });
    return lookupRows;
  }

  _createLookupRow(code) {
    if (!code) {
      return null;
    }
    return scout.create('LookupRow', {
      key: code.id,
      text: code.text(this.session.locale),
      parentKey: code.parent && code.parent.id
    });
  }
}
