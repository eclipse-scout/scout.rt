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
import {scout, TreeVisitResult} from '../index';

export default class CodeType {

  constructor() {
    this.id;
    this.codes = [];
    this.codeMap = {};
  }

  init(model) {
    scout.assertParameter('id', model.id);
    this.id = model.id;
    this.modelClass = model.modelClass;

    if (model.codes) {
      for (let i = 0; i < model.codes.length; i++) {
        this._initCode(model.codes[i]);
      }
    }
  }

  _initCode(modelCode, parent) {
    let code = scout.create(modelCode);
    this.add(code, parent);
    if (modelCode.children) {
      for (let i = 0; i < modelCode.children.length; i++) {
        this._initCode(modelCode.children[i], code);
      }
    }
  }

  add(code, parent) {
    this.codes.push(code);
    this.codeMap[code.id] = code;
    if (parent) {
      parent.children.push(code);
      code.parent = parent;
    }
  }

  /**
   * @param codeId
   * @returns {Code}
   * @throw {Error) if code does not exist
   */
  get(codeId) {
    let code = this.optGet(codeId);
    if (!code) {
      throw new Error('No code found for id=' + codeId);
    }
    return code;
  }

  /**
   * Same as <code>get</code>, but does not throw an error if the code does not exist.
   *
   * @param codeId
   * @returns {Code} code for the given codeId or undefined if code does not exist
   */
  optGet(codeId) {
    return this.codeMap[codeId];
  }

  /**
   * @param {boolean} rootOnly
   * @returns {Array<string>}
   */
  getCodes(rootOnly) {
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
   * Visits all codes and theirs children.
   * <p>
   * In order to abort visiting, the visitor can return true or TreeVisitResult.TERMINATE.
   * To only abort the visiting of a sub tree, the visitor can return SKIP_SUBTREE.
   * </p>
   * @returns {boolean} true if the visitor aborted the visiting, false if the visiting completed without aborting
   */
  visit(visitor) {
    let codes = this.codes.filter(code => {
      // Only consider root codes
      return !code.parent;
    });
    for (let i = 0; i < codes.length; i++) {
      let code = codes[i];
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

  static ensure(codeType) {
    if (!codeType) {
      return codeType;
    }
    if (codeType instanceof CodeType) {
      return codeType;
    }
    return scout.create('CodeType', codeType);
  }
}
