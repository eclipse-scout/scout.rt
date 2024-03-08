/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CodeModel, CodeType, FullModelOf, InitModelOf, Locale, ModelOf, ObjectWithType, scout, texts, TreeVisitor, TreeVisitResult} from '../index';

export class Code<TCodeId> implements ObjectWithType {
  declare model: CodeModel<TCodeId>;

  id: TCodeId;
  objectType: string;
  modelClass: string;
  active: boolean;
  enabled: boolean;
  iconId: string;
  tooltipText: string;
  backgroundColor: string;
  foregroundColor: string;
  font: string;
  cssClass: string;
  extKey: string;
  value: number;
  partitionId: number;
  sortCode: number;
  children: Code<TCodeId>[];
  parent?: Code<TCodeId>;
  codeType: CodeType<TCodeId, Code<TCodeId>, any>;

  protected _text: string; // e.g. "${textKey:key}"

  constructor() {
    this.active = true;
    this.enabled = true;
    this.partitionId = 0;
    this.sortCode = -1;
    this.children = [];
    this.codeType = null;
  }

  init(model: InitModelOf<this>) {
    scout.assertParameter('id', model.id);

    this.id = model.id;
    this.modelClass = model.modelClass;
    this.active = scout.nvl(model.active, this.active);
    this.enabled = scout.nvl(model.enabled, this.enabled);
    this.iconId = model.iconId;
    this.tooltipText = model.tooltipText;
    this.backgroundColor = model.backgroundColor;
    this.foregroundColor = model.foregroundColor;
    this.font = model.font;
    this.cssClass = model.cssClass;
    this.extKey = model.extKey;
    this.value = model.value;
    this.partitionId = scout.nvl(model.partitionId, this.partitionId);
    this.sortCode = scout.nvl(model.sortCode, this.sortCode);
    this.codeType = model.codeType;
    this._text = model.text;

    // If model contains a text map, generate a text key and add the texts to the text maps of the session
    if (model.texts) {
      if (this._text) {
        throw new Error('Either set texts or text property, not both.');
      }
      let codeTypeId = scout.nvl(this.codeType?.id, '');
      let key = '__code.' + codeTypeId + '.' + this.id;
      texts.registerTexts(key, model.texts);
      this._text = texts.buildKey(key);
    }

    if (model.children) {
      for (let i = 0; i < model.children.length; i++) {
        let childCodeModel = model.children[i];
        childCodeModel.codeType = this.codeType;
        let code = Code.ensure(childCodeModel);
        if (code) {
          code.parent = this;
          this.children.push(code);
        }
      }
    }
    this._initCodeTypeField(this.codeType, model.fieldName);
  }

  protected _initCodeTypeField(codeType: CodeType<TCodeId, Code<TCodeId>, any>, fieldName: string) {
    if (!codeType || !fieldName) {
      return;
    }
    if (!codeType.hasOwnProperty(fieldName)) {
      return; // property is not declared
    }
    let existing = codeType[fieldName];
    if (!existing) {
      codeType[fieldName] = this;
    }
  }

  /**
   * Gets the text of this Code in the given language.
   *
   * @param vararg The language tag or the {@link Locale} to load the text for.
   */
  text(vararg: string | Locale): string {
    let languageTag: string;
    if (typeof vararg === 'object') {
      languageTag = vararg.languageTag;
    } else {
      languageTag = vararg;
    }
    return texts.resolveText(this._text, languageTag);
  }

  /**
   * Visits all children of this Code recursively without visiting this Code itself.
   */
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

  static ensure<TCodeId>(code: ModelOf<Code<TCodeId>> | Code<TCodeId>): Code<TCodeId> {
    if (!code) {
      return null;
    }
    if (code instanceof Code) {
      return code;
    }
    if (!code.objectType) {
      code.objectType = Code;
    }
    let codeModel = code as FullModelOf<Code<TCodeId>>;
    return scout.create(codeModel) as Code<TCodeId>;
  }
}
