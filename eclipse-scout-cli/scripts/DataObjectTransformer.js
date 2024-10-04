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
const ModuleDetector = require('./ModuleDetector');
const CONSTANT_PATTERN = new RegExp('^[A-Z_0-9]+$');

/**
 * See https://github.com/itsdouges/typescript-transformer-handbook
 */
module.exports = class DataObjectTransformer {

  constructor(program, context, namespaceResolver) {
    this.program = program;
    this.context = context;
    this.moduleDetector = null; // created on first use
    this.namespaceResolver = namespaceResolver;
  }

  transform(node) {
    if (ts.isSourceFile(node)) {
      const transformedFile = this._visitChildren(node); // step into top level source files
      this.moduleDetector = null; // forget cached types for this file
      return transformedFile;
    }
    if (ts.isClassDeclaration(node)) {
      const typeNameDecorator = node.modifiers?.find(m => ts.isDecorator(m) && m.expression?.expression?.escapedText === 'typeName');
      if (typeNameDecorator) {
        return this._visitChildren(node); // step into DO with typeName annotation
      }
      return node; // no need to step into
    }
    if (ts.isImportDeclaration(node) || ts.isExportDeclaration(node) || ts.isVariableStatement(node) || ts.isIdentifier(node) || ts.isTypeReferenceNode(node)
      || ts.isPropertySignature(node) || ts.isStringLiteral(node) || ts.isInterfaceDeclaration(node) || ts.isPropertyAssignment(node) || ts.isObjectLiteralExpression(node)
      || ts.isPropertyAccessExpression(node) || ts.isTypeAliasDeclaration(node) || ts.isParameter(node) || ts.isEnumDeclaration(node)
      || ts.isCallExpression(node) || ts.isExpressionStatement(node) || ts.isDecorator(node) || node.kind === ts.SyntaxKind.ExportKeyword) {
      return node; // no need to step into
    }

    if (ts.isPropertyDeclaration(node) && !this._isSkipProperty(node)) {
      const newModifiers = [
        ...(node.modifiers || []), // existing
        ...this._createMetaDataAnnotationsFor(node) // newly added
      ];
      return ts.factory.replaceDecoratorsAndModifiers(node, newModifiers);
    }
    return node; // no need to step into
  }

  _createMetaDataAnnotationsFor(node) {
    const metaDataAnnotations = [];
    metaDataAnnotations.push(this._createMetaDataAnnotation('t', this._createTypeNode(node.type)));
    return metaDataAnnotations;
  }

  _createTypeNode(typeNode) {
    if (typeNode.kind === ts.SyntaxKind.NumberKeyword) {
      // primitive number
      return ts.factory.createIdentifier('Number');
    }
    if (typeNode.kind === ts.SyntaxKind.StringKeyword) {
      // primitive string
      return ts.factory.createIdentifier('String');
    }
    if (typeNode.kind === ts.SyntaxKind.BooleanKeyword) {
      // primitive boolean
      return ts.factory.createIdentifier('Boolean');
    }
    // bigint is not yet supported as it is only part of ES2020 while Scout still supports ES2019

    if (ts.isArrayTypeNode(typeNode)) {
      // treat Obj[] like Array<Obj>
      const objectType = ts.factory.createIdentifier('Array');
      const elementType = this._createTypeNode(typeNode.elementType);
      return this._createFieldMetaData(objectType, [elementType]);
    }
    if (ts.isTypeReferenceNode(typeNode)) {
      const objectType = this._createTypeReferenceNode(typeNode);
      if (!typeNode.typeArguments?.length) {
        // no type arguments: directly use the type reference
        return objectType;
      }

      // types with typeArguments like Map<string, number> or Array<MyObject>
      const typeArgsNodes = typeNode.typeArguments.map(typeArg => this._createTypeNode(typeArg));
      return this._createFieldMetaData(objectType, typeArgsNodes);
    }
    if (ts.isLiteralTypeNode(typeNode)) {
      if (ts.isStringLiteral(typeNode.literal)) {
        // literal types like e.g. in IDs: UuId<'scout.SimpleUuid'>
        return ts.factory.createStringLiteral(typeNode.literal.text);
      }
    }
    return ts.factory.createIdentifier('Object'); // e.g. any, void, unknown
  }

  _createFieldMetaData(objectType, typeArgsNodes) {
    return ts.factory.createObjectLiteralExpression([
      ts.factory.createPropertyAssignment('objectType', objectType),
      ts.factory.createPropertyAssignment('typeArgs', ts.factory.createArrayLiteralExpression(typeArgsNodes, false))
    ], false);
  }

  _createTypeReferenceNode(node) {
    const name = node.typeName.escapedText;
    if (global[name]) {
      return ts.factory.createIdentifier(name); // Use directly the constructor for known types like Date, Number, String, Boolean, Map, Set, Array
    }

    const namespace = this._detectNamespaceFor(node);
    const qualifiedName = (!namespace || namespace === 'scout') ? name : namespace + '.' + name;
    // use objectType as string because e.g. of TS interfaces (which do not exist at RT) and that overwrites in ObjectFactory are taken into account.
    return ts.factory.createStringLiteral(qualifiedName);
  }

  _detectNamespaceFor(typeNode) {
    if (!this.moduleDetector) {
      this.moduleDetector = new ModuleDetector(typeNode);
    }
    const moduleName = this.moduleDetector.detectModuleOf(typeNode);
    return this.namespaceResolver.resolveNamespace(moduleName, this.moduleDetector.sourceFile.fileName);
  }

  _createMetaDataAnnotation(key/* string */, valueNode) {
    const reflect = ts.factory.createIdentifier('Reflect');
    const reflectMetaData = ts.factory.createPropertyAccessExpression(reflect, ts.factory.createIdentifier('metadata'));
    const keyNode = ts.factory.createStringLiteral('scout.m.' + key);
    const call = ts.factory.createCallExpression(reflectMetaData, undefined, [keyNode, valueNode]);
    return ts.factory.createDecorator(call);
  }

  _isSkipProperty(node) {
    const propertyName = node.symbol?.escapedName;
    if (!propertyName || propertyName.startsWith('_') || propertyName.startsWith('$') || CONSTANT_PATTERN.test(propertyName)) {
      return true;
    }
    return !!node.modifiers?.some(n => n.kind === ts.SyntaxKind.StaticKeyword || n.kind === ts.SyntaxKind.ProtectedKeyword);
  }

  _visitChildren(node) {
    return ts.visitEachChild(node, n => this.transform(n), this.context);
  }
};
