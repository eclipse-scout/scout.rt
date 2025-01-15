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
const ModuleDetector = require('./ModuleDetector');
const CONSTANT_PATTERN = new RegExp('^[A-Z_0-9]+$');

/**
 * TypeScript transformer for DataObject which injects @Reflect.metadata('scout.m.t', dataType) decorators to all DO attributes.
 * These decorators store the data type information from the TS property declaration.
 * This allows to transfer the TypeScript data type information to the runtime which is used when (de)serializing a data object.
 *
 * See https://github.com/itsdouges/typescript-transformer-handbook
 */
module.exports = class DataObjectTransformer {

  constructor(program, context, namespaceResolver) {
    this.program = program;
    this.context = context;
    this.moduleDetector = null; // created on first use
    this.doInventoryAddStatements = [];
    this.namespaceResolver = namespaceResolver;
  }

  transform(node) {
    if (ts.isSourceFile(node)) {
      let transformedFile = this._visitChildren(node); // step into top level source files
      if (this.doInventoryAddStatements.length) {
        // add auto-register DO statements to the source file end
        const statements = [...transformedFile.statements, ...this.doInventoryAddStatements];
        transformedFile = ts.factory.updateSourceFile(node, statements,
          transformedFile.isDeclarationFile, transformedFile.referencedFiles, transformedFile.typeReferenceDirectives, transformedFile.hasNoDefaultLib, transformedFile.libReferenceDirectives);
        this.doInventoryAddStatements = []; // clear added statements
      }
      this.moduleDetector = null; // forget cached types for this file
      return transformedFile;
    }
    if (ts.isClassDeclaration(node)) {
      const typeNameDecorator = node.modifiers?.find(m => ts.isDecorator(m) && m.expression?.expression?.escapedText === 'typeName');
      if (typeNameDecorator) {
        // it is a data object: remember DataObjectInventory.add statement to add at the end to the source file
        const className = node.localSymbol.escapedName;
        const typeName = this._getTypeNameFromDecorator(typeNameDecorator);
        const namespace = this._detectExportInfoFor(node).namespace; // detectExportInfoOf() will not find anything because a ClassDeclaration node is passed here. But this is fine as the namespace of the own module is required here.
        this.doInventoryAddStatements.push(this._createDoInventoryAddStatement(className, typeName, namespace));
        return this._visitChildren(node); // step into DO with typeName decorator
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
        ...this._createMetaDataDecoratorsFor(node) // newly added
      ];
      return ts.factory.replaceDecoratorsAndModifiers(node, newModifiers);
    }
    return node; // no need to step into
  }

  /**
   * Reads the value passed to the typeName decorator. Supports direct string literals and references to constants
   * @param typeNameDecorator {ts.Decorator}
   * @returns {string|null}
   */
  _getTypeNameFromDecorator(typeNameDecorator) {
    const decoratorArgument = typeNameDecorator.expression?.arguments?.[0];
    if (decoratorArgument && ts.isStringLiteral(decoratorArgument)) {
      return decoratorArgument.text;
    }

    // might be a reference to a constant
    let constant = decoratorArgument?.flowNode?.node;
    if (constant && ts.isVariableDeclaration(constant)) {
      const initializer = constant.initializer;
      if (initializer && ts.isStringLiteral(initializer)) {
        return initializer.text;
      }
    }

    return null; // rely on the auto-detection of the Scout RT.
  }

  /**
   * @param node {ts.PropertyDeclaration} the node for the DO property.
   * @returns {ts.Decorator[]}
   */
  _createMetaDataDecoratorsFor(node) {
    if (!node.type) {
      return [];
    }
    return [this._createMetaDataDecorator(this._createTypeNode(node.type))];
  }

  /**
   * Creates the value node to be added to the @Reflect.metadata('scout.m.t', valueNode) decorator.
   * @param typeNode {ts.TypeNode} The data type node of a DO property.
   * @returns {ts.StringLiteral|ts.Identifier|ts.ObjectLiteralExpression}
   *   A constructor reference identifier for primitives like number, string, boolean, object, unknown, any, void, etc.
   *   A constructor reference identifier for built-in types like Map, Set, Date.
   *   A RawFieldMetaData object literal for arrays (having Array type and the element type as type arguments: Array<ElementType>).
   *   A string literal for string literal types as e.g. used in IDs: UuId<'scout.SimpleUuid'>.
   *   A string literal for custom type references not having type arguments in the form of a Scout objectType: 'myApp.MySpecialDo'.
   *   A RawFieldMetaData object literal for type references having type arguments.
   */
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
    // bigint is not yet supported as it is only part of ES2020 while Scout still uses ES2019

    if (ts.isArrayTypeNode(typeNode)) {
      // treat Obj[] like Array<Obj>
      const objectType = ts.factory.createIdentifier('Array');
      const elementType = this._createTypeNode(typeNode.elementType);
      return this._createFieldMetaData(objectType, [elementType]);
    }

    if (ts.isTypeReferenceNode(typeNode)) {
      const objectType = this._createTypeReferenceNode(typeNode);
      if (!typeNode.typeArguments?.length) {
        // no type arguments: directly use the type reference (constructor ref or objectType string)
        return objectType;
      }

      // types with typeArguments like Map<string, number> or Array<MyObject>
      const typeArgsNodes = typeNode.typeArguments.map(typeArg => this._createTypeNode(typeArg));
      return this._createFieldMetaData(objectType, typeArgsNodes);
    }

    // // literal types like e.g. in IDs: UuId<'scout.SimpleUuid'>
    if (ts.isLiteralTypeNode(typeNode)) {
      if (ts.isStringLiteral(typeNode.literal)) {
        return ts.factory.createStringLiteral(typeNode.literal.text);
      }
    }

    return ts.factory.createIdentifier('Object'); // e.g. any, void, unknown
  }

  /**
   * Creates a field metadata object. See RawFieldMetaData type.
   * <pre>
   *   {
   *     objectType: 'myApp.MySpecialDo',
   *     typeArgs: [...]
   *   }
   * </pre>
   * @returns {ts.ObjectLiteralExpression}
   */
  _createFieldMetaData(objectType, typeArgsNodes) {
    return ts.factory.createObjectLiteralExpression([
      ts.factory.createPropertyAssignment('objectType', objectType),
      ts.factory.createPropertyAssignment('typeArgs', ts.factory.createArrayLiteralExpression(typeArgsNodes, false))
    ], false);
  }

  /**
   * Creates a node for a type reference.
   *
   * This is directly the constructor reference for built-in types like Date, Number, String, Map, Set, etc.
   * Or a string with the Scout objectType for the type (e.g. 'myApp.MySpecialDo').
   *
   * @param node {ts.TypeReferenceNode}
   * @returns {ts.StringLiteral|ts.Identifier}
   */
  _createTypeReferenceNode(node) {
    const name = node.typeName.escapedText;
    if (global[name]) {
      return ts.factory.createIdentifier(name); // Use directly the constructor for known types like Date, Number, String, Boolean, Map, Set, Array
    }
    if ('Record' === name) {
      return ts.factory.createStringLiteral(name);
    }

    const exportInfo = this._detectExportInfoFor(node);
    const qualifiedName = this._createObjectType(exportInfo.namespace, exportInfo.exportName);
    // use objectType as string because e.g. of TS interfaces (which do not exist at RT) and that overwrites in ObjectFactory are taken into account.
    return ts.factory.createStringLiteral(qualifiedName);
  }

  /**
   * Creates the qualified object type (e.g. 'myApp.MyDataObject').
   * @param namespace {string} the namespace of the class
   * @param className {string} the name of the class
   * @returns {string} The objectType
   */
  _createObjectType(namespace, className) {
    return (!namespace || namespace === 'scout') ? className : namespace + '.' + className;
  }

  /**
   * 1. Checks in which Node module the type referenced by typeNode is declared.
   * 2. Resolves the Scout JS namespace and exported name of the referenced type.
   *
   * @param typeNode {ts.TypeReferenceNode} The type reference for which the namespace it is declared in should be resolved.
   * @returns {{namespace: string|null;exportName: string|null}} The namespace of the module that contains the given type.
   */
  _detectExportInfoFor(typeNode) {
    if (!this.moduleDetector) {
      this.moduleDetector = new ModuleDetector(typeNode);
    }
    const exportInfo = this.moduleDetector.detectExportInfoOf(typeNode);
    const namespace = this.namespaceResolver.resolveNamespace(exportInfo?.module, this.moduleDetector.sourceFile.fileName);
    return {namespace, exportName: exportInfo?.exportName || typeNode?.typeName?.escapedText};
  }

  /**
   * Creates a metadata decorator:
   * <pre>
   *   @Reflect.metadata('scout.m.t', valueNode)
   * </pre>
   * @param valueNode The value of the decorator.
   * @returns {ts.Decorator}
   */
  _createMetaDataDecorator(valueNode) {
    const reflect = ts.factory.createIdentifier('Reflect');
    const reflectMetaData = ts.factory.createPropertyAccessExpression(reflect, ts.factory.createIdentifier('metadata'));
    const keyNode = ts.factory.createStringLiteral('scout.m.t');
    const call = ts.factory.createCallExpression(reflectMetaData, undefined, [keyNode, valueNode]);
    return ts.factory.createDecorator(call);
  }

  /**
   * Skips properties which are static, protected or the name starts with '$' or '_'.
   * Furthermore, constant like properties (consisting of uppercase chars, numbers and underscore) are skipped as well.
   * @returns {boolean}
   */
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

  /**
   * Creates an ExpressionStatement calling DataObjectInventory.add for the given data object name.
   * Creates the following call:
   * <pre>
   * window['scout']['DataObjectInventory'].get().add(className, typeName, objectType);
   * </pre>
   * @param className {string} The local class name of the data object.
   * @param typeName {string|null} The typeName value of the data object (as detected from the @typeName decorator)
   * @param namespace {string|null} The namespace of the current module. Used to build the objectType of the class.
   * @returns {ts.ExpressionStatement}
   */
  _createDoInventoryAddStatement(className, typeName, namespace) {
    if (!className) {
      throw new Error('DataObjectInventory.add not supported for anonymous data objects.');
    }
    const win = ts.factory.createIdentifier('window');
    const scout = ts.factory.createElementAccessExpression(win, ts.factory.createStringLiteral('scout'));
    const doInventory = ts.factory.createElementAccessExpression(scout, ts.factory.createStringLiteral('DataObjectInventory'));
    const get = ts.factory.createPropertyAccessExpression(doInventory, 'get');
    const getCall = ts.factory.createCallExpression(get, undefined, undefined);
    const add = ts.factory.createPropertyAccessExpression(getCall, 'add');

    const objectType = this._createObjectType(namespace, className);
    const addCall = ts.factory.createCallExpression(add, undefined, [
      ts.factory.createIdentifier(className),
      typeName ? ts.factory.createStringLiteral(typeName) : ts.factory.createNull(),
      ts.factory.createStringLiteral(objectType)
    ]);
    return ts.factory.createExpressionStatement(addCall);
  }
};
