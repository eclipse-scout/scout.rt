/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, CodeTypeModel, FullModelOf, InitModelOf, Locale, ObjectOrModel, ObjectWithType, scout, texts, TreeVisitor, TreeVisitResult} from '../index';

export class CodeType<TCodeId = string, TCode extends Code<TCodeId> = Code<TCodeId>, TCodeTypeId = string> implements ObjectWithType {

  declare model: CodeTypeModel<TCodeId, TCode, TCodeTypeId>;

  id: TCodeTypeId;
  objectType: string;
  modelClass: string;
  iconId: string;
  hierarchical: boolean;
  maxLevel: number;
  codeMap: Map<TCodeId, TCode>; // all codes recursively

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
        let code = Code.ensure(codeModel) as TCode;
        if (code) {
          this._add(code);
        }
      }
    }
    this._validateCodeFields();
  }

  /**
   * Override this method and add additional properties which should not be validated to be present after CodeType init.
   * @returns A Set with all public properties (fields) of this CodeType which do _not_ point to a nested Code instance.
   */
  protected _getPublicNonCodeProperties(): Set<string> {
    return new Set(['id', 'objectType', 'modelClass', 'iconId', 'hierarchical', 'maxLevel', 'codeMap']);
  }

  protected _validateCodeFields() {
    let publicNonCodeProperties = this._getPublicNonCodeProperties();
    Object.keys(this)
      .filter(property => !property.startsWith('_'))
      .filter(property => !publicNonCodeProperties.has(property))
      .forEach(property => this._ensureCodeAvailable(property));
  }

  protected _ensureCodeAvailable(fieldName: string) {
    let codeFieldValue = this[fieldName];
    if (!codeFieldValue) {
      throw new Error(`The field '${fieldName}' in CodeType with id '${this.id}' could not be initialized with a Code instance. ` +
        `If this field should not hold a Code after CodeType init, override _getPublicNonCodeProperties and add '${fieldName}' to the resulting set. ` +
        'If this field points to a Code, ensure the name of the field matches the \'fieldName\' property of the corresponding Code model.');
    }
  }

  protected _add(code: TCode) {
    this.codeMap.set(code.id, code);
    code.visitChildren((c: TCode) => {
      this.codeMap.set(c.id, c);
    }, false);
  }

  protected _registerTexts(suffix: string, textMap: Record<string, string>): string {
    if (!textMap) {
      return;
    }
    let key = '__codeType.' + this.id + '.' + suffix;
    texts.registerTexts(key, textMap);
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
   * @returns the root Codes of this CodeType if rootOnly is true and all Codes recursively otherwise.
   */
  codes(): TCode[];
  /** @deprecated use {@link CodeTypeCodesOptions} instead */
  codes(rootOnly?: boolean): TCode[];
  codes(options: CodeTypeCodesOptions): TCode[];
  codes(options: boolean | CodeTypeCodesOptions = {}): TCode[] {
    if (typeof options === 'boolean') { // legacy support
      options = {
        rootOnly: options
      };
    }

    let codes = [...this.codeMap.values()];
    if (options.rootOnly) {
      codes = codes.filter(code => !code.parent);
    }
    if (scout.nvl(options.activeOnly, true)) {
      codes = codes.filter(code => code.active);
    }
    return codes;
  }

  /**
   * Gets the Code with given id. All codes recursively are searched.
   * @param codeId The Code id to search
   * @returns The Code with given id or null.
   */
  get(codeId: TCodeId): TCode {
    return this.codeMap.get(codeId);
  }

  /**
   * Visits all codes and their children recursively.
   *
   * By default, only active codes are visited. To visit inactive codes as well, set the `activeOnly` argument to false.
   *
   * In order to abort visiting, the visitor can return true or {@link TreeVisitResult.TERMINATE}.
   * To only abort the visiting of a subtree, the visitor can return {@link TreeVisitResult.SKIP_SUBTREE}.
   */
  visitChildren(visitor: TreeVisitor<TCode>, activeOnly = true): boolean | TreeVisitResult {
    let rootCodes = this.codes({rootOnly: true, activeOnly: activeOnly});
    for (let i = 0; i < rootCodes.length; i++) {
      let code = rootCodes[i];
      let visitResult = visitor(code);
      if (visitResult === true || visitResult === TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
      if (visitResult !== TreeVisitResult.SKIP_SUBTREE) {
        visitResult = code.visitChildren(visitor, activeOnly);
        if (visitResult === true || visitResult === TreeVisitResult.TERMINATE) {
          return TreeVisitResult.TERMINATE;
        }
      }
    }
  }

  static ensure<TCode extends Code<TCodeId>, TCodeId, TCodeTypeId>(codeType: ObjectOrModel<CodeType<TCodeId, TCode, TCodeTypeId>>): CodeType<TCodeId, TCode, TCodeTypeId> {
    if (!codeType?.id) {
      return null;
    }
    if (codeType instanceof CodeType) {
      return codeType;
    }
    if (!codeType.objectType) {
      codeType.objectType = CodeType;
    }
    let codeTypeModel = codeType as FullModelOf<CodeType<TCodeId, TCode, TCodeTypeId>>;
    return scout.create(codeTypeModel, {ensureUniqueId: false}) as CodeType<TCodeId, TCode, TCodeTypeId>;
  }
}

export type CodeTypeCodesOptions = {
  rootOnly?: boolean;
  activeOnly?: boolean;
};
