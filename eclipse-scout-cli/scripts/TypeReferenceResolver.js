/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

const ts = require('typescript');

/**
 * Class to resolve names used in type references of the TS file given.
 *
 * It parses all imports in the file and stores the imported names together with the module they are imported from (e.g. '@eclipse-scout/core' or './Test2Do', see detectExportInfoOf).
 *
 * Furthermore, it finds the declarations of names used in the file by following the imports and exports to the declaring file (see findDeclaration).
 */
module.exports = class TypeReferenceResolver {

  /**
   * @param node A node in the TS file for which the resolver should be created. All imports of this file are stored.
   * @param moduleResolver {ModuleResolver} Used to find the file of the node and to resolve and parse imported files.
   * @param compilerOptions {ts.CompilerOptions} The options used to resolve imported modules.
   */
  constructor(node, moduleResolver, compilerOptions = null) {
    this.moduleResolver = moduleResolver;
    this.sourceFile = moduleResolver.findSourceFile(node);
    this.compilerOptions = compilerOptions || {};
    const imports = this._findImportDeclarations(this.sourceFile);
    this._importsByName = this._computeImportMap(imports);
    this._localNames = this._computeLocalNames(this.sourceFile);
  }

  /**
   * @param typeNode {ts.TypeReferenceNode}
   * @returns {{module: string;exportName:string}} The name of the module (e.g. '@eclipse-scout/core' or './Test2Do') the given type is imported from and the name it was exported in this module.
   * undefined if the type is not imported (e.g. declared in this file).
   */
  detectExportInfoOf(typeNode) {
    const name = typeNode?.typeName?.escapedText;
    const importInfo = this._importsByName.get(name);
    if (!importInfo) {
      return undefined;
    }
    const exportName = importInfo.exportName === 'default' ? name : importInfo.exportName;
    return {module: importInfo.module, exportName};
  }

  /**
   * @param name {string}
   * @returns {boolean} true if the given name is imported or declared on top level in the file (in contrast to global types like Date or Promise).
   */
  isDeclaredInFile(name) {
    return this._localNames.has(name);
  }

  /**
   * Finds the declaration of a name imported or declared on top level in the file.
   * Imports and re-exports (export * from, export {a as b} from) are followed to the declaring file.
   * Depending on the package.json of a dependency, the declaring file is a TS source file or a declaration file (*.d.ts).
   *
   * @param name {string}
   * @returns {ts.Declaration|null} e.g. an EnumDeclaration, TypeAliasDeclaration, ClassDeclaration or VariableDeclaration. null if it cannot be found.
   */
  findDeclaration(name) {
    return this._findLocalDeclaration(name, new Set());
  }

  _findLocalDeclaration(name, visited) {
    const importInfo = this._importsByName.get(name);
    if (importInfo) {
      return this._findDeclarationInModule(importInfo.module, importInfo.exportName, visited);
    }
    for (const statement of this.sourceFile.statements) {
      const declaration = this._findDeclarationInStatement(statement, name);
      if (declaration) {
        return declaration;
      }
    }
    return null;
  }

  _findExport(exportName, visited) {
    const key = this.sourceFile.fileName + '#' + exportName;
    if (visited.has(key)) {
      return null; // circular exports
    }
    visited.add(key);

    const starExportModules = [];
    for (const statement of this.sourceFile.statements) {
      if (ts.isExportDeclaration(statement)) {
        const exportClause = statement.exportClause;
        const moduleName = statement.moduleSpecifier?.text;
        if (!exportClause) {
          starExportModules.push(moduleName); // export * from 'module'
          continue;
        }
        if (!ts.isNamedExports(exportClause)) {
          continue; // export * as ns from 'module'
        }
        const element = exportClause.elements.find(e => e.name.escapedText === exportName);
        if (!element) {
          continue;
        }
        const originalName = element.propertyName?.escapedText || exportName;
        if (!moduleName) {
          return this._findLocalDeclaration(originalName, visited); // export {a as b};
        }
        return this._findDeclarationInModule(moduleName, originalName, visited); // export {a as b} from 'module';
      }
      if (ts.isExportAssignment(statement)) {
        if (exportName === 'default' && ts.isIdentifier(statement.expression)) {
          return this._findLocalDeclaration(statement.expression.escapedText, visited); // export default a;
        }
        continue;
      }
      if (statement.modifiers?.some(m => m.kind === ts.SyntaxKind.ExportKeyword)) {
        if (exportName === 'default' && statement.modifiers.some(m => m.kind === ts.SyntaxKind.DefaultKeyword)) {
          return statement; // export default class A {}
        }
        const declaration = this._findDeclarationInStatement(statement, exportName);
        if (declaration) {
          return declaration;
        }
      }
    }

    for (const moduleName of starExportModules) {
      const declaration = this._findDeclarationInModule(moduleName, exportName, visited);
      if (declaration) {
        return declaration;
      }
    }
    return null;
  }

  _findDeclarationInModule(moduleName, exportName, visited) {
    const typeReferenceResolver = this.moduleResolver.getTypeReferenceResolverOfModule(moduleName, this.sourceFile.fileName, this.compilerOptions, exportName);
    return typeReferenceResolver ? typeReferenceResolver._findExport(exportName, visited) : null;
  }

  _findDeclarationInStatement(statement, name) {
    if (ts.isVariableStatement(statement)) {
      return statement.declarationList.declarations.find(d => d.name?.escapedText === name) || null;
    }
    if (ts.isImportDeclaration(statement) || ts.isImportEqualsDeclaration(statement)) {
      return null;
    }
    return statement.name?.escapedText === name ? statement : null; // classes, interfaces, type aliases, enums, functions
  }

  _computeImportMap(imports) {
    const importsByName = new Map();
    for (let imp of imports) {
      const moduleName = imp.moduleSpecifier?.text; // e.g. '@eclipse-scout/core' or './index'
      const defaultName = imp.importClause?.name?.escapedText;
      if (defaultName) {
        // default import e.g.: import a from 'whatever'
        importsByName.set(defaultName, {module: moduleName, exportName: 'default'});
      }
      this._putNamedBindings(imp.importClause?.namedBindings, importsByName, moduleName);
    }
    return importsByName;
  }

  _putNamedBindings(namedBindings, importsByName, moduleName) {
    const importElements = namedBindings?.elements;
    if (Array.isArray(importElements)) {
      // multi import e.g.: import {a, b, c as d} from 'whatever'
      for (let importElement of importElements) {
        const name = importElement?.name?.escapedText;
        if (!name) {
          continue;
        }
        const exportedName = importElement?.propertyName?.escapedText || name;
        importsByName.set(name, {module: moduleName, exportName: exportedName});
      }
    } else {
      // single import e.g.: import * as self from './index';
      const name = namedBindings?.name?.escapedText;
      if (name) {
        importsByName.set(name, {module: moduleName, exportName: name});
      }
    }
  }

  _computeLocalNames(sourceFile) {
    const names = new Set(this._importsByName.keys());
    for (const statement of sourceFile.statements) {
      if (ts.isVariableStatement(statement)) {
        statement.declarationList.declarations.forEach(declaration => names.add(declaration.name?.escapedText));
      } else if (ts.isClassDeclaration(statement) || ts.isInterfaceDeclaration(statement) || ts.isTypeAliasDeclaration(statement) || ts.isEnumDeclaration(statement)
        || ts.isFunctionDeclaration(statement) || ts.isImportEqualsDeclaration(statement) || (ts.isModuleDeclaration(statement) && ts.isIdentifier(statement.name) && !ts.isGlobalScopeAugmentation(statement))) {
        names.add(statement.name?.escapedText);
      }
    }
    names.delete(undefined);
    return names;
  }

  _findImportDeclarations(sourceFile) {
    return sourceFile.statements.filter(s => ts.isImportDeclaration(s));
  }
};
