/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

const ts = require('typescript');

module.exports = class ModuleDetector {

  constructor(node) {
    this.sourceFile = this._findSourceFile(node);
    const imports = this._findImportDeclarations(this.sourceFile);
    this._moduleByTypeMap = this._computeImportMap(imports); // Map only contains types in 'other' modules (modules different from the one of the source file)
  }

  detectModuleOf(typeNode) {
    const name = typeNode?.typeName?.escapedText;
    return this._moduleByTypeMap.get(name);
  }

  _computeImportMap(imports) {
    const moduleByTypeMap = new Map();
    for (let imp of imports) {
      const moduleName = imp.moduleSpecifier?.text; // e.g. '@eclipse-scout/core' or './index'
      const isExternalModule = !moduleName?.startsWith('.'); // only store imports to other modules
      if (isExternalModule) {
        this._putExternalNamedBindings(imp.importClause?.namedBindings, moduleByTypeMap, moduleName);
      }
    }
    return moduleByTypeMap;
  }

  _putExternalNamedBindings(namedBindings, moduleByTypeMap, moduleName) {
    const importElements = namedBindings?.elements;
    if (Array.isArray(importElements)) {
      // multi import e.g.: import {a, b, c as d} from 'whatever'
      for (let importElement of importElements) {
        const name = importElement?.name?.escapedText;
        if (!name) {
          continue;
        }
        moduleByTypeMap.set(name, moduleName);
      }
    } else {
      // single import e.g.: import * as self from './index';
      const name = namedBindings?.name?.escapedText;
      if (name) {
        moduleByTypeMap.set(name, moduleName);
      }
    }
  }

  _findImportDeclarations(sourceFile) {
    return sourceFile.statements.filter(s => ts.isImportDeclaration(s));
  }

  _findSourceFile(node) {
    while (!ts.isSourceFile(node)) {
      node = node.parent;
    }
    return node;
  }
};
