/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, codes, LookupCallModel, LookupRow, Predicate, scout, StaticLookupCall, strings} from '../index';

export class CodeLookupCall<TCodeId> extends StaticLookupCall<TCodeId> {
  declare model: CodeLookupCallModel<TCodeId>;
  codeType: string;

  constructor() {
    super();
    this.codeType = null;
  }

  protected override _lookupRowByKey(key: TCodeId): LookupRow<TCodeId> {
    let codeType = codes.codeType(this.codeType, true);
    if (!codeType) {
      return null;
    }
    return this._createLookupRow(codeType.optGet(key));
  }

  protected override _lookupRowsByAll(): LookupRow<TCodeId>[] {
    return this._collectLookupRows();
  }

  protected override _lookupRowsByText(text: string): LookupRow<TCodeId>[] {
    return this._collectLookupRows(lookupRow => {
      let lookupRowText = lookupRow.text || '';
      return strings.startsWith(lookupRowText.toLowerCase(), text.toLowerCase());
    });
  }

  protected override _lookupRowsByRec(rec: TCodeId): LookupRow<TCodeId>[] {
    return this._collectLookupRows(lookupRow => lookupRow.parentKey === rec);
  }

  protected _collectLookupRows(predicate?: Predicate<LookupRow<TCodeId>>): LookupRow<TCodeId>[] {
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

  protected _createLookupRow(code: Code<TCodeId>): LookupRow<TCodeId> {
    if (!code) {
      return null;
    }
    return scout.create(LookupRow, {
      key: code.id,
      text: code.text(this.session.locale),
      parentKey: code.parent && code.parent.id
    }) as LookupRow<TCodeId>;
  }
}

export interface CodeLookupCallModel<TCodeId> extends LookupCallModel<TCodeId> {
  /**
   * CodeTypeId {@link CodeType.id}
   */
  codeType: string;
}
