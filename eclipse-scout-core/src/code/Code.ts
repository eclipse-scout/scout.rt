/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CodeModel, codes, InitModelOf, Locale, ObjectWithType, scout, texts, TreeVisitor, TreeVisitResult} from '../index';

export class Code<TCodeId> implements ObjectWithType {
  declare model: CodeModel<TCodeId>;

  objectType: string;
  active: boolean;
  id: TCodeId;
  parent?: Code<TCodeId>;
  children: Code<TCodeId>[];
  sortCode: number;
  modelClass: string;

  protected _text: string; // e.g. "${textKey:key}"

  constructor() {
    this.children = [];
  }

  init(model: InitModelOf<this>) {
    scout.assertParameter('id', model.id);

    this.active = model.active;
    this.id = model.id;
    this.sortCode = model.sortCode;
    this._text = model.text;
    this.modelClass = model.modelClass;

    // If model contains a text map, generate a text key and add the texts to the text maps of the session
    if (model.texts) {
      if (this._text) {
        throw new Error('Either set texts or text property, not both.');
      }
      let key = codes.registerTexts(this, model.texts);
      // Convert to ${textKey:key} so that text() may resolve it
      this._text = texts.buildKey(key);
    }
  }

  /**
   * @param vararg The language tag or the {@link Locale} to load the text for.
   */
  text(vararg: string | Locale): string {
    if (typeof vararg === 'object') {
      return texts.resolveText(this._text, vararg.languageTag);
    }
    return texts.resolveText(this._text, vararg);
  }

  visitChildren(visitor: TreeVisitor<Code<TCodeId>>): boolean | TreeVisitResult {
    for (let i = 0; i < this.children.length; i++) {
      let child = this.children[i];
      let visitResult = visitor(child);
      if (visitResult === true || visitResult === TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
      if (visitResult !== TreeVisitResult.SKIP_SUBTREE) {
        visitResult = child.visitChildren(visitor);
        if (visitResult === true || visitResult === TreeVisitResult.TERMINATE) {
          return TreeVisitResult.TERMINATE;
        }
      }
    }
  }
}
