/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, CodeTypeModel, FullModelOf, InitModelOf, ObjectOrModel, ObjectWithType, scout, TreeVisitor, TreeVisitResult} from '../index';

export class CodeType<TCodeId> implements ObjectWithType {

  declare model: CodeTypeModel<TCodeId>;

  objectType: string;
  id: string;
  modelClass: string;
  codes: Code<TCodeId>[];
  codeMap: Record<string, Code<TCodeId>>;

  constructor() {
    this.codes = [];
    this.codeMap = {};
  }

  init(model: InitModelOf<this>) {
    scout.assertParameter('id', model.id);
    this.id = model.id;
    this.modelClass = model.modelClass;

    if (model.codes) {
      for (let i = 0; i < model.codes.length; i++) {
        this._initCode(model.codes[i]);
      }
    }
  }

  protected _initCode(modelCode: FullModelOf<Code<TCodeId>>, parent?: Code<TCodeId>) {
    let code = scout.create(modelCode);
    this.add(code, parent);
    if (modelCode.children) {
      for (let i = 0; i < modelCode.children.length; i++) {
        this._initCode(modelCode.children[i], code);
      }
    }
  }

  add(code: Code<TCodeId>, parent?: Code<TCodeId>) {
    this.codes.push(code);
    this.codeMap[code.id + ''] = code;
    if (parent) {
      parent.children.push(code);
      code.parent = parent;
    }
  }

  /**
   * @throw Error if code does not exist
   */
  get(codeId: TCodeId): Code<TCodeId> {
    let code = this.optGet(codeId);
    if (!code) {
      throw new Error('No code found for id=' + codeId);
    }
    return code;
  }

  /**
   * Same as {@link get}, but does not throw an error if the code does not exist.
   *
   * @returns code for the given codeId or undefined if code does not exist
   */
  optGet(codeId: TCodeId): Code<TCodeId> {
    return this.codeMap[codeId + ''];
  }

  getCodes(rootOnly?: boolean): Code<TCodeId>[] {
    if (rootOnly) {
      let rootCodes = [];
      for (let i = 0; i < this.codes.length; i++) {
        if (!this.codes[i].parent) {
          rootCodes.push(this.codes[i]);
        }
      }
      return rootCodes;
    }
    return this.codes;
  }

  /**
   * Visits all codes and their children.
   * <p>
   * In order to abort visiting, the visitor can return true or TreeVisitResult.TERMINATE.
   * To only abort the visiting of a subtree, the visitor can return SKIP_SUBTREE.
   * </p>
   */
  visitChildren(visitor: TreeVisitor<Code<TCodeId>>): boolean | TreeVisitResult {
    let rootCodes = this.getCodes(true);
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

  static ensure<TCodeId>(codeType: ObjectOrModel<CodeType<TCodeId>>): CodeType<TCodeId> {
    if (!codeType) {
      return codeType as CodeType<TCodeId>;
    }
    if (codeType instanceof CodeType) {
      return codeType;
    }
    return scout.create(CodeType, codeType) as CodeType<TCodeId>;
  }
}
