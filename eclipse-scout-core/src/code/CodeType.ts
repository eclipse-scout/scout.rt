/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, codes, CodeTypeModel, FullModelOf, InitModelOf, Locale, ObjectOrModel, ObjectWithType, scout, texts, TreeVisitor, TreeVisitResult} from '../index';

export class CodeType<TCodeId = string, TCodeClass extends Code<TCodeId> = Code<TCodeId>, TCodeTypeId = string> implements ObjectWithType {

  declare model: CodeTypeModel<TCodeId, TCodeClass, TCodeTypeId>;

  id: TCodeTypeId;
  objectType: string;
  modelClass: string;
  iconId: string;
  hierarchical: boolean;
  maxLevel: number;
  codeMap: Map<TCodeId, TCodeClass>; // all codes recursively

  protected _textKey: string;
  protected _textKeyPlural: string;

  constructor() {
    this.maxLevel = 2147483647; // default from Scout Classic
    this.hierarchical = false;
    this.codeMap = new Map();
  }

  init(model: InitModelOf<this>) {
    scout.assertParameter('id', model.id);
    this.id = model.id;
    this.modelClass = model.modelClass;
    this.iconId = model.iconId;
    this.hierarchical = !!model.hierarchical;
    if (model.maxLevel) {
      this.maxLevel = model.maxLevel;
    }
    this._textKey = this._registerTexts('texts', model.texts);
    this._textKeyPlural = this._registerTexts('textsPlural', model.textsPlural);

    if (model.codes) {
      for (let i = 0; i < model.codes.length; i++) {
        let codeModel = model.codes[i];
        codeModel.codeType = this;
        let code = Code.ensure(codeModel) as TCodeClass;
        if (code) {
          this._add(code);
        }
      }
    }
  }

  protected _add(code: TCodeClass) {
    this.codeMap.set(code.id, code);
    code.visitChildren((c: TCodeClass) => {
      this.codeMap.set(c.id, c);
    });
  }

  protected _registerTexts(suffix: string, textMap: Record<string, string>): string {
    if (!textMap) {
      return;
    }
    let key = '__codeType.' + this.id + '.' + suffix;
    codes.registerTexts(key, textMap);
    return texts.buildKey(key);
  }

  /**
   * @param vararg The language tag or the {@link Locale} to load the text for.
   */
  text(vararg: string | Locale): string {
    return this._text(this._textKey, vararg);
  }

  /**
   * @param vararg The language tag or the {@link Locale} to load the plural text for.
   */
  textPlural(vararg: string | Locale): string {
    return this._text(this._textKeyPlural, vararg);
  }

  protected _text(key: string, vararg: string | Locale): string {
    let languageTag: string;
    if (typeof vararg === 'object') {
      languageTag = vararg.languageTag;
    } else {
      languageTag = vararg;
    }
    return texts.resolveText(key, languageTag);
  }

  /**
   * Gets the codes of this CodeType.
   * @param rootOnly true if only the root Codes should be returned. The default value is false.
   * @return the root Codes of this CodeType if rootOnly is true and all Codes recursively otherwise.
   */
  codes(rootOnly?: boolean): TCodeClass[] {
    if (!rootOnly) {
      // all codes recursively
      return [...this.codeMap.values()];
    }

    let rootCodes: TCodeClass[] = [];
    for (let code of this.codeMap.values()) {
      if (!code.parent) {
        rootCodes.push(code);
      }
    }
    return rootCodes;
  }

  /**
   * Gets the Code with given id. All codes recursively are searched.
   * @param codeId The Code id to search
   * @return The Code with given id or null.
   */
  get(codeId: TCodeId): TCodeClass {
    return this.codeMap.get(codeId);
  }

  /**
   * Visits all codes and their children recursively.
   * <p>
   * In order to abort visiting, the visitor can return true or TreeVisitResult.TERMINATE.
   * To only abort the visiting of a subtree, the visitor can return SKIP_SUBTREE.
   * </p>
   */
  visitChildren(visitor: TreeVisitor<TCodeClass>): boolean | TreeVisitResult {
    let rootCodes = this.codes(true);
    for (let i = 0; i < rootCodes.length; i++) {
      let code = rootCodes[i];
      let visitResult = visitor(code);
      if (visitResult === true || visitResult === TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
      if (visitResult !== TreeVisitResult.SKIP_SUBTREE) {
        visitResult = code.visitChildren(visitor);
        if (visitResult === true || visitResult === TreeVisitResult.TERMINATE) {
          return TreeVisitResult.TERMINATE;
        }
      }
    }
  }

  static ensure<TCodeClass extends Code<TCodeId>, TCodeId, TCodeTypeId>(codeType: ObjectOrModel<CodeType<TCodeId, TCodeClass, TCodeTypeId>>): CodeType<TCodeId, TCodeClass, TCodeTypeId> {
    if (!codeType) {
      return null;
    }
    if (codeType instanceof CodeType) {
      return codeType;
    }
    if (!codeType.objectType) {
      codeType.objectType = CodeType;
    }
    let codeTypeModel = codeType as FullModelOf<CodeType<TCodeId, TCodeClass, TCodeTypeId>>;
    return scout.create(codeTypeModel) as CodeType<TCodeId, TCodeClass, TCodeTypeId>;
  }
}
