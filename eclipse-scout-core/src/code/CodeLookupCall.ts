/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Code, codes, LookupCallModel, LookupRow, Predicate, scout, StaticLookupCall, strings} from '../index';

export interface CodeLookupCallModel extends LookupCallModel<string> {
  /**
   * CodeTypeId {@link CodeType.id}
   */
  codeType: string;
}

export default class CodeLookupCall extends StaticLookupCall<string> {
  declare model: CodeLookupCallModel;
  codeType: string;

  constructor() {
    super();
    this.codeType = null;
  }

  protected override _lookupRowByKey(key: string): LookupRow<string> {
    let codeType = codes.codeType(this.codeType, true);
    if (!codeType) {
      return null;
    }
    return this._createLookupRow(codeType.optGet(key));
  }

  protected override _lookupRowsByAll(): LookupRow<string>[] {
    return this._collectLookupRows();
  }

  protected override _lookupRowsByText(text: string): LookupRow<string>[] {
    return this._collectLookupRows(lookupRow => {
      let lookupRowText = lookupRow.text || '';
      return strings.startsWith(lookupRowText.toLowerCase(), text.toLowerCase());
    });
  }

  protected override _lookupRowsByRec(rec: string): LookupRow<string>[] {
    return this._collectLookupRows(lookupRow => lookupRow.parentKey === rec);
  }

  protected _collectLookupRows(predicate?: Predicate<LookupRow<string>>): LookupRow<string>[] {
    let codeType = codes.codeType(this.codeType, true);
    if (!codeType) {
      return [];
    }
    let lookupRows = [];
    codeType.visitChildren(code => {
      let lookupRow = this._createLookupRow(code);
      if (!predicate || predicate(lookupRow)) {
        lookupRows.push(lookupRow);
      }
    });
    return lookupRows;
  }

  protected _createLookupRow(code: Code): LookupRow<string> {
    if (!code) {
      return null;
    }
    return scout.create(LookupRow, {
      key: code.id,
      text: code.text(this.session.locale),
      parentKey: code.parent && code.parent.id
    }) as LookupRow<string>;
  }
}
