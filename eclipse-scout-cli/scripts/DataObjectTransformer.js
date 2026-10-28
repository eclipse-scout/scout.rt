/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

const ts = require('typescript');
const CONSTANT_PATTERN = new RegExp('^[A-Z_0-9]+$');
const BUILT_IN_TYPES = new Set(['Array', 'Blob', 'Boolean', 'Date', 'File', 'Map', 'Number', 'Object', 'Set', 'String']);
const READONLY_TYPES = new Map([['ReadonlyArray', 'Array'], ['ReadonlyMap', 'Map'], ['ReadonlySet', 'Set']]);
const WRAPPER_TYPES = new Set(['NonNullable', 'Partial', 'Readonly', 'Required']); // utility types which are replaced by their type argument
// bigint is not supported
const PRIMITIVE_KEYWORDS = new Map([[ts.SyntaxKind.StringKeyword, 'String'], [ts.SyntaxKind.NumberKeyword, 'Number'], [ts.SyntaxKind.BooleanKeyword, 'Boolean']]);
const MAX_EVALUATION_DEPTH = 10;

/**
 * TypeScript transformer for DataObject which injects @Reflect.metadata('scout.m.t', dataType) decorators to all DO attributes.
 * These decorators store the data type information from the TS property declaration.
 * This allows to transfer the TypeScript data type information to the runtime which is used when (de)serializing a data object.
 *
 * See https://github.com/itsdouges/typescript-transformer-handbook
 */
module.exports = class DataObjectTransformer {

  constructor(program, context, moduleResolver) {
    this.program = program;
    this.context = context;
    this.currentSourceFile = null;
    this.doInventoryAddStatements = [];
    this.currentClassTypeParams = null;
    this.resolvingTypeParams = new Set(); // to detect recursive type parameter constraints
    this.resolvingTypeAliases = new Set(); // to detect recursive type aliases
    this.moduleResolver = moduleResolver;
  }

  transform(node) {
    if (ts.isSourceFile(node)) {
      this.currentSourceFile = node;
      let transformedFile = this._visitChildren(node); // step into top level source files
      if (this.doInventoryAddStatements.length) {
        // add auto-register DO statements to the source file end
        const statements = [...transformedFile.statements, ...this.doInventoryAddStatements];
        transformedFile = ts.factory.updateSourceFile(node, statements,
          transformedFile.isDeclarationFile, transformedFile.referencedFiles, transformedFile.typeReferenceDirectives, transformedFile.hasNoDefaultLib, transformedFile.libReferenceDirectives);
        this.doInventoryAddStatements = []; // clear added statements
      }
      this.currentSourceFile = null;
      return transformedFile;
    }
    if (ts.isClassDeclaration(node)) {
      const typeNameDecorator = node.modifiers?.find(m => ts.isDecorator(m) && m.expression?.expression?.escapedText === 'typeName');
      if (typeNameDecorator) {
        // it is a data object: remember DataObjectInventory.add statement to add at the end to the source file
        const className = node.symbol.escapedName;
        const classConstants = node.members
          .filter(n => ts.isPropertyDeclaration(n))
          .filter(n => n.modifiers?.some(m => m.kind === ts.SyntaxKind.StaticKeyword));
        const typeName = this._getTypeNameFromDecorator(typeNameDecorator, className, classConstants);
        const namespace = this._detectExportInfoFor(node).namespace; // detectExportInfoOf() will not find anything because a ClassDeclaration node is passed here. But this is fine as the namespace of the own module is required here.
        this.doInventoryAddStatements.push(this._createDoInventoryAddStatement(className, typeName, namespace));

        try {
          this.currentClassTypeParams = this._buildTypeParamMap(node);
          return this._visitChildren(node); // step into DO with typeName decorator
        } finally {
          this.currentClassTypeParams = null; // reset for next class
        }
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
      const metaDataDecorators = this._createMetaDataDecoratorsFor(node);
      if (!metaDataDecorators?.length) {
        return node; // nothing to add
      }
      const newModifiers = [
        ...(node.modifiers || []), // existing
        ...metaDataDecorators // newly added
      ];
      return ts.factory.replaceDecoratorsAndModifiers(node, newModifiers);
    }
    return node; // no need to step into
  }

  /**
   * @param node {ts.ClassDeclaration}
   * @returns Map<string, ts.TypeNode|null> The constraint (or default type if there is no constraint) by type parameter name. Null if the type parameter has neither.
   */
  _buildTypeParamMap(node) {
    const mappings = node.typeParameters
      ?.map(typeParameter => [typeParameter?.name?.escapedText, typeParameter.constraint || typeParameter.default || null])
      ?.filter(mapping => !!mapping[0]);
    return new Map(mappings);
  }

  /**
   * Reads the value passed to the typeName decorator. Supports direct string literals and references to constants
   * @param typeNameDecorator {ts.Decorator}
   * @param className {string} The name of the containing class. Currently only references to the same own class are supported
   * @param classConstants {ts.PropertyDeclaration[]} All static property declarations of the containing class.
   * @returns {string|null}
   */
  _getTypeNameFromDecorator(typeNameDecorator, className, classConstants) {
    const decoratorArgument = typeNameDecorator.expression?.arguments?.[0];
    if (!decoratorArgument) {
      return null;
    }

    if (ts.isStringLiteral(decoratorArgument)) {
      return decoratorArgument.text;
    }

    // might be a reference to a static constant inside the current class (like @typeName(ThirdInSameFileDo.TYPE_NAME))
    if (ts.isPropertyAccessExpression(decoratorArgument) && decoratorArgument.expression?.escapedText === className) {
      const constantRef = classConstants.find(c => c.name?.escapedText === decoratorArgument.name?.escapedText);
      if (constantRef) {
        return constantRef.initializer?.text;
      }
    }

    // might be a reference to a constant (like export const TYPE_NAME = 'my.TestDo');
    let constant = decoratorArgument.flowNode?.node;
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
    const typeNode = node.type ? this._createTypeNode(node.type) : this._createTypeNodeFromInitializer(node.initializer);
    if (!typeNode) {
      return null; // unsupported types like union-types or any or unknown
    }
    return [this._createMetaDataDecorator(typeNode)];
  }

  /**
   * Creates the value node to be added to the @Reflect.metadata('scout.m.t', valueNode) decorator for a property without type declaration.
   * The data type is detected from the initializer expression (e.g. prop = 10; or prop = new Set<string>();).
   * @param initializer {ts.Expression|undefined} The initializer of a DO property.
   * @returns {ts.StringLiteral|ts.NullLiteral|ts.Identifier|ts.ObjectLiteralExpression|null} see _createTypeNode
   */
  _createTypeNodeFromInitializer(initializer) {
    if (!initializer) {
      return null;
    }
    if (ts.isParenthesizedExpression(initializer)) {
      return this._createTypeNodeFromInitializer(initializer.expression);
    }
    if (ts.isAsExpression(initializer) || ts.isSatisfiesExpression(initializer)) {
      return this._createTypeNode(initializer.type);
    }
    if (ts.isTemplateExpression(initializer)) {
      return ts.factory.createIdentifier('String');
    }
    const primitiveTypes = this._evaluatePrimitiveValue(initializer);
    if (primitiveTypes) {
      return this._toPrimitiveTypeNode(primitiveTypes);
    }
    if (ts.isNewExpression(initializer) && ts.isIdentifier(initializer.expression)) {
      // treat new Obj<T>() like a declaration of type Obj<T>
      return this._createTypeNode(ts.factory.createTypeReferenceNode(initializer.expression, initializer.typeArguments));
    }
    if (ts.isArrayLiteralExpression(initializer)) {
      return this._createFieldMetaData(ts.factory.createIdentifier('Array'), [this._createArrayLiteralElementTypeNode(initializer)]);
    }
    return null; // e.g. null, undefined, function calls, object literals
  }

  /**
   * @param arrayLiteral {ts.ArrayLiteralExpression}
   * @returns {ts.StringLiteral|ts.NullLiteral|ts.Identifier|ts.ObjectLiteralExpression} The element type if all elements have the same detectable type, a null literal otherwise.
   */
  _createArrayLiteralElementTypeNode(arrayLiteral) {
    const printer = ts.createPrinter();
    let elementType = null;
    let elementTypeText = null;
    for (const element of arrayLiteral.elements) {
      const type = this._createTypeNodeFromInitializer(element);
      if (!type) {
        return ts.factory.createNull();
      }
      const typeText = printer.printNode(ts.EmitHint.Expression, type, arrayLiteral.getSourceFile());
      if (elementTypeText !== null && elementTypeText !== typeText) {
        return ts.factory.createNull(); // mixed element types
      }
      elementType = type;
      elementTypeText = typeText;
    }
    return elementType ?? ts.factory.createNull();
  }

  /**
   * Creates the value node to be added to the @Reflect.metadata('scout.m.t', valueNode) decorator.
   * @param typeNode {ts.TypeNode} The data type node of a DO property.
   * @returns {ts.StringLiteral|ts.NullLiteral|ts.Identifier|ts.ObjectLiteralExpression|null}
   *   A constructor reference identifier for primitives like number, string, boolean.
   *   A constructor reference identifier for primitive literal types, unions of such literal types and enums (e.g. String for 'a' | 'b').
   *   A constructor reference identifier for built-in types like Map, Set, Date.
   *   A RawFieldMetaData object literal for arrays (having Array type and the element type as type arguments: Array<ElementType>).
   *   A string literal for custom type references not having type arguments in the form of a Scout objectType: 'myApp.MySpecialDo'.
   *   A RawFieldMetaData object literal for type references having type arguments.
   *   The result for the aliased type for type aliases.
   *   The result for the remaining type for unions with null or undefined (e.g. Date | null).
   *   null for unsupported types like any, void, unknown, unions and other global types (e.g. Promise).
   */
  _createTypeNode(typeNode) {
    if (PRIMITIVE_KEYWORDS.has(typeNode.kind) || ts.isLiteralTypeNode(typeNode) || ts.isTypeQueryNode(typeNode)) {
      // number, string, boolean, literal types (e.g. 'a') or typeof MY_CONSTANT
      return this._createPrimitiveTypeNode(typeNode);
    }
    if (ts.isParenthesizedTypeNode(typeNode)) {
      return this._createTypeNode(typeNode.type);
    }
    if (ts.isTypeOperatorNode(typeNode) && typeNode.operator === ts.SyntaxKind.ReadonlyKeyword) {
      // treat readonly Obj[] like Obj[]
      return this._createTypeNode(typeNode.type);
    }
    if (ts.isUnionTypeNode(typeNode)) {
      return this._createUnionTypeNode(typeNode);
    }
    if (ts.isArrayTypeNode(typeNode)) {
      // treat Obj[] like Array<Obj>
      const objectType = ts.factory.createIdentifier('Array');
      let elementType = this._createTypeNode(typeNode.elementType);
      if (!elementType) {
        elementType = ts.factory.createNull(); // e.g. union types ((string | number)[]) or any (any[])
      }
      return this._createFieldMetaData(objectType, [elementType]);
    }
    if (ts.isTypeReferenceNode(typeNode)) {
      return this._createTypeReferenceTypeNode(typeNode);
    }
    return null; // e.g. any, void, unknown
  }

  /**
   * @param typeNode {ts.TypeReferenceNode}
   * @returns {ts.StringLiteral|ts.NullLiteral|ts.Identifier|ts.ObjectLiteralExpression|null} see _createTypeNode
   */
  _createTypeReferenceTypeNode(typeNode) {
    const name = typeNode.typeName.escapedText;
    if (this.currentClassTypeParams?.has(name)) {
      return this._createTypeParamTypeNode(name);
    }

    if (this._isDeclaredInFile(typeNode, name)) {
      const declaration = this._findDeclaration(typeNode, name);
      if (declaration && ts.isEnumDeclaration(declaration)) {
        return this._toPrimitiveTypeNode(this._evaluateEnum(declaration));
      }
      if (declaration && ts.isTypeAliasDeclaration(declaration)) {
        return this._createTypeAliasTypeNode(typeNode, declaration);
      }
    } else if (WRAPPER_TYPES.has(name)) {
      const typeArg = typeNode.typeArguments?.[0];
      return typeArg ? this._createTypeNode(typeArg) : null;
    }

    const objectType = this._createTypeReferenceNode(typeNode);
    if (!objectType || !typeNode.typeArguments?.length) {
      // no type arguments: directly use the type reference (constructor ref or objectType string)
      return objectType;
    }

    // types with typeArguments like Map<string, number> or Array<MyObject>
    const typeArgsNodes = typeNode.typeArguments.map(typeArg => this._createTypeNode(typeArg) ?? ts.factory.createNull());
    return this._createFieldMetaData(objectType, typeArgsNodes);
  }

  /**
   * Resolves a type parameter of the current DO class to its constraint or default type.
   * @param name {string} The name of the type parameter
   * @returns {ts.StringLiteral|ts.NullLiteral|ts.Identifier|ts.ObjectLiteralExpression|null} see _createTypeNode.
   * null if the type parameter has neither a constraint nor a default or if the constraint is recursive (e.g. T extends Comparable<T>).
   */
  _createTypeParamTypeNode(name) {
    const typeParamType = this.currentClassTypeParams.get(name);
    if (!typeParamType || this.resolvingTypeParams.has(name)) {
      return null;
    }
    this.resolvingTypeParams.add(name);
    try {
      return this._createTypeNode(typeParamType);
    } finally {
      this.resolvingTypeParams.delete(name);
    }
  }

  /**
   * @param typeNode {ts.UnionTypeNode}
   * @returns {ts.StringLiteral|ts.NullLiteral|ts.Identifier|ts.ObjectLiteralExpression|null}
   *   The result for the remaining type if the union only consists of one type besides null and undefined (e.g. Date | null).
   *   A constructor reference identifier if the union only consists of primitive types of the same kind (e.g. String for 'a' | 'b').
   *   null otherwise.
   */
  _createUnionTypeNode(typeNode) {
    const types = typeNode.types.filter(type => !this._isNullOrUndefinedType(type));
    if (types.length === 1) {
      return this._createTypeNode(types[0]);
    }
    return this._createPrimitiveTypeNode(typeNode);
  }

  _isNullOrUndefinedType(typeNode) {
    return typeNode.kind === ts.SyntaxKind.UndefinedKeyword || typeNode.kind === ts.SyntaxKind.NullKeyword
      || (ts.isLiteralTypeNode(typeNode) && typeNode.literal.kind === ts.SyntaxKind.NullKeyword);
  }

  /**
   * @param typeNode {ts.TypeReferenceNode} The reference to the type alias.
   * @param declaration {ts.TypeAliasDeclaration}
   * @returns {ts.StringLiteral|ts.NullLiteral|ts.Identifier|ts.ObjectLiteralExpression|null} The result for the aliased type.
   *   For generic type aliases (e.g. EnumObject<typeof MyEnum>) only primitive types can be detected. null for recursive type aliases.
   */
  _createTypeAliasTypeNode(typeNode, declaration) {
    if (declaration.typeParameters?.length) {
      return this._createPrimitiveTypeNode(typeNode);
    }
    if (this.resolvingTypeAliases.has(declaration)) {
      return null;
    }
    const classTypeParams = this.currentClassTypeParams;
    this.currentClassTypeParams = null; // the type parameters of the DO class are not visible in the type alias
    this.resolvingTypeAliases.add(declaration);
    try {
      return this._createTypeNode(declaration.type);
    } finally {
      this.resolvingTypeAliases.delete(declaration);
      this.currentClassTypeParams = classTypeParams;
    }
  }

  /**
   * Detects if the given type node resolves to a primitive type, a primitive literal type or a union of such types.
   * @param typeNode {ts.TypeNode}
   * @returns {ts.Identifier|null} see _toPrimitiveTypeNode
   */
  _createPrimitiveTypeNode(typeNode) {
    return this._toPrimitiveTypeNode(this._evaluatePrimitiveType(typeNode, new Map(), 0));
  }

  /**
   * @param primitiveTypes {Set<string>|null} see _evaluatePrimitiveType
   * @returns {ts.Identifier|null} String, Number or Boolean if all types have the same primitive type. null otherwise.
   */
  _toPrimitiveTypeNode(primitiveTypes) {
    return primitiveTypes?.size === 1 ? ts.factory.createIdentifier([...primitiveTypes][0]) : null;
  }

  /**
   * Computes the primitive types a type node can have.
   * Supports primitive and literal types, unions, enums, type aliases (also generic ones), typeof of constants and indexed access types of the form T[keyof T].
   *
   * @param typeNode {ts.TypeNode}
   * @param typeArgs {Map<string, {typeNode: ts.TypeNode, typeArgs: Map}>} The type arguments of the generic type aliases currently evaluated.
   * @param depth {number} The current evaluation depth to stop on recursive types.
   * @returns {Set<string>|null} The names of the primitive types ('String', 'Number', 'Boolean'). null if the type is not primitive.
   */
  _evaluatePrimitiveType(typeNode, typeArgs, depth) {
    if (!typeNode || depth > MAX_EVALUATION_DEPTH) {
      return null;
    }
    depth++;
    const keywordKind = PRIMITIVE_KEYWORDS.get(typeNode.kind);
    if (keywordKind) {
      return new Set([keywordKind]);
    }
    if (this._isNullOrUndefinedType(typeNode)) {
      return new Set();
    }
    if (ts.isParenthesizedTypeNode(typeNode)) {
      return this._evaluatePrimitiveType(typeNode.type, typeArgs, depth);
    }
    if (ts.isLiteralTypeNode(typeNode)) {
      return this._evaluatePrimitiveValue(typeNode.literal);
    }
    if (ts.isUnionTypeNode(typeNode)) {
      return this._mergePrimitiveTypes(typeNode.types.map(type => this._evaluatePrimitiveType(type, typeArgs, depth)));
    }
    if (ts.isTypeQueryNode(typeNode)) {
      // typeof MY_CONSTANT
      return this._evaluateValueType(this._resolveValue(typeNode.exprName), depth);
    }
    if (ts.isIndexedAccessTypeNode(typeNode)) {
      // T[keyof T]: the types of all values of T
      const indexType = typeNode.indexType;
      if (ts.isTypeOperatorNode(indexType) && indexType.operator === ts.SyntaxKind.KeyOfKeyword && this._isSameTypeReference(typeNode.objectType, indexType.type)) {
        return this._evaluateObjectValueTypes(typeNode.objectType, typeArgs, depth);
      }
      return null;
    }
    if (ts.isTypeReferenceNode(typeNode)) {
      const name = typeNode.typeName.escapedText;
      const typeArg = typeArgs.get(name);
      if (typeArg) {
        return this._evaluatePrimitiveType(typeArg.typeNode, typeArg.typeArgs, depth);
      }
      const declaration = name ? this._findDeclaration(typeNode, name) : null;
      if (declaration && ts.isEnumDeclaration(declaration)) {
        return this._evaluateEnum(declaration);
      }
      if (declaration && ts.isTypeAliasDeclaration(declaration)) {
        return this._evaluatePrimitiveType(declaration.type, this._buildTypeArgMap(declaration, typeNode, typeArgs), depth);
      }
    }
    return null;
  }

  /**
   * @param typeNode {ts.TypeNode} The object type: a type parameter of a generic type alias or typeof of a constant object, enum or static class property.
   * @param typeArgs {Map} see _evaluatePrimitiveType
   * @param depth {number} see _evaluatePrimitiveType
   * @returns {Set<string>|null} The primitive types of all property values of the object type.
   */
  _evaluateObjectValueTypes(typeNode, typeArgs, depth) {
    if (depth > MAX_EVALUATION_DEPTH) {
      return null;
    }
    depth++;
    if (ts.isTypeReferenceNode(typeNode)) {
      const typeArg = typeArgs.get(typeNode.typeName.escapedText);
      return typeArg ? this._evaluateObjectValueTypes(typeArg.typeNode, typeArg.typeArgs, depth) : null;
    }
    if (!ts.isTypeQueryNode(typeNode)) {
      return null;
    }
    const value = this._resolveValue(typeNode.exprName);
    if (value?.declaration && ts.isEnumDeclaration(value.declaration)) {
      return this._evaluateEnum(value.declaration);
    }
    if (value?.typeNode && ts.isTypeLiteralNode(value.typeNode)) {
      // declared type (e.g. in declaration files): {readonly A: 'a'; readonly B: 'b'}
      return this._mergePrimitiveTypes(value.typeNode.members.map(member => ts.isPropertySignature(member) ? this._evaluatePrimitiveType(member.type, new Map(), depth) : null));
    }
    const objectLiteral = value?.expression;
    if (!objectLiteral || !ts.isObjectLiteralExpression(objectLiteral)) {
      return null;
    }
    return this._mergePrimitiveTypes(objectLiteral.properties.map(property => ts.isPropertyAssignment(property) ? this._evaluatePrimitiveValue(this._unwrapExpression(property.initializer)) : null));
  }

  /**
   * @param value {{typeNode?: ts.TypeNode, expression?: ts.Expression}|null} see _resolveValue
   * @param depth {number} see _evaluatePrimitiveType
   * @returns {Set<string>|null} The primitive types of the declared type or of the initializer if there is no declared type.
   */
  _evaluateValueType(value, depth) {
    if (value?.typeNode) {
      return this._evaluatePrimitiveType(value.typeNode, new Map(), depth);
    }
    return value?.expression ? this._evaluatePrimitiveValue(value.expression) : null;
  }

  /**
   * @param expression {ts.Expression} An initializer or a literal of a literal type.
   * @returns {Set<string>|null}
   */
  _evaluatePrimitiveValue(expression) {
    if (ts.isStringLiteral(expression) || ts.isNoSubstitutionTemplateLiteral(expression)) {
      return new Set(['String']);
    }
    if (ts.isNumericLiteral(expression) || (ts.isPrefixUnaryExpression(expression) && ts.isNumericLiteral(expression.operand))) {
      return new Set(['Number']);
    }
    if (expression.kind === ts.SyntaxKind.TrueKeyword || expression.kind === ts.SyntaxKind.FalseKeyword) {
      return new Set(['Boolean']);
    }
    return null;
  }

  /**
   * @param declaration {ts.EnumDeclaration}
   * @returns {Set<string>|null} The primitive types of all enum members. null for enums with computed values.
   */
  _evaluateEnum(declaration) {
    if (!declaration.members.length) {
      return null;
    }
    return this._mergePrimitiveTypes(declaration.members.map(member => member.initializer
      ? this._evaluatePrimitiveValue(member.initializer)
      : new Set(['Number'])));
  }

  /**
   * @param primitiveTypes {(Set<string>|null)[]}
   * @returns {Set<string>|null} The union of all given primitive types. null if one of them is not primitive.
   */
  _mergePrimitiveTypes(primitiveTypes) {
    if (primitiveTypes.some(primitiveType => !primitiveType)) {
      return null;
    }
    return new Set(primitiveTypes.flatMap(primitiveType => [...primitiveType]));
  }

  /**
   * @param declaration {ts.TypeAliasDeclaration} A generic type alias.
   * @param typeNode {ts.TypeReferenceNode} The reference to the type alias containing the type arguments.
   * @param typeArgs {Map} The type arguments the type arguments of the reference are evaluated with.
   * @returns {Map} The type arguments by type parameter name of the type alias.
   */
  _buildTypeArgMap(declaration, typeNode, typeArgs) {
    const typeArgMap = new Map();
    declaration.typeParameters?.forEach((typeParameter, index) => {
      const typeArg = typeNode.typeArguments?.[index];
      if (typeArg) {
        typeArgMap.set(typeParameter.name.escapedText, {typeNode: typeArg, typeArgs});
      } else if (typeParameter.default) {
        typeArgMap.set(typeParameter.name.escapedText, {typeNode: typeParameter.default, typeArgs: typeArgMap});
      }
    });
    return typeArgMap;
  }

  _isSameTypeReference(typeNode1, typeNode2) {
    return ts.isTypeReferenceNode(typeNode1) && ts.isTypeReferenceNode(typeNode2) && !!typeNode1.typeName.escapedText
      && typeNode1.typeName.escapedText === typeNode2.typeName.escapedText;
  }

  /**
   * Resolves the value of an expression used in typeof (e.g. typeof MY_CONSTANT, typeof MyEnum, typeof MyClass.CONSTANT or typeof MY_OBJECT.property).
   * The declared type is preferred over the initializer, as declaration files (*.d.ts) of built dependencies only contain the declared types of objects.
   * @param entityName {ts.EntityName}
   * @returns {{declaration?: ts.Declaration, typeNode?: ts.TypeNode, expression?: ts.Expression}|null}
   *   declaration: the declaration for enums and classes.
   *   typeNode: the declared type of constants and properties.
   *   expression: the initializer of constants and properties without declared type.
   */
  _resolveValue(entityName) {
    if (ts.isIdentifier(entityName)) {
      const declaration = this._findDeclaration(entityName, entityName.escapedText);
      if (!declaration) {
        return null;
      }
      if (ts.isVariableDeclaration(declaration)) {
        return this._createValue(declaration.type, declaration.initializer);
      }
      return {declaration};
    }
    if (!ts.isQualifiedName(entityName)) {
      return null;
    }
    const left = this._resolveValue(entityName.left);
    const name = entityName.right.escapedText;
    if (left?.declaration && ts.isClassDeclaration(left.declaration)) {
      const member = left.declaration.members.find(m => ts.isPropertyDeclaration(m) && m.name?.escapedText === name
        && m.modifiers?.some(modifier => modifier.kind === ts.SyntaxKind.StaticKeyword));
      if (!member) {
        return null;
      }
      return this._createValue(member.type, member.initializer);
    }
    if (left?.typeNode && ts.isTypeLiteralNode(left.typeNode)) {
      const member = left.typeNode.members.find(m => ts.isPropertySignature(m) && m.name?.escapedText === name);
      return member?.type ? {typeNode: member.type} : null;
    }
    if (left?.expression && ts.isObjectLiteralExpression(left.expression)) {
      const property = left.expression.properties.find(p => ts.isPropertyAssignment(p) && p.name?.escapedText === name);
      return property ? this._createValue(null, property.initializer) : null;
    }
    return null;
  }

  _createValue(typeNode, initializer) {
    if (typeNode) {
      return {typeNode};
    }
    return initializer ? {expression: this._unwrapExpression(initializer)} : null;
  }

  _unwrapExpression(expression) {
    while (expression && (ts.isAsExpression(expression) || ts.isSatisfiesExpression(expression) || ts.isParenthesizedExpression(expression))) {
      expression = expression.expression;
    }
    return expression || null;
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
   * @returns {ts.StringLiteral|ts.Identifier|null} null for global types which are not supported (e.g. Promise).
   */
  _createTypeReferenceNode(node) {
    const name = node.typeName.escapedText;
    if (!this._isDeclaredInFile(node, name)) {
      // global type
      const builtInName = READONLY_TYPES.get(name) || name;
      if (BUILT_IN_TYPES.has(builtInName)) {
        return ts.factory.createIdentifier(builtInName); // Use directly the constructor for known types like Date, Number, String, Boolean, Map, Set, Array
      }
      if ('Record' === name) {
        return ts.factory.createStringLiteral(name);
      }
      return null;
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
    const typeReferenceResolver = this._getTypeReferenceResolver(typeNode);
    const exportInfo = typeReferenceResolver.detectExportInfoOf(typeNode);
    const exportName = exportInfo?.exportName || typeNode?.typeName?.escapedText;
    let declaringFile = typeReferenceResolver.sourceFile.fileName;
    if (exportInfo?.module) {
      // imported from another file: use the file the module name resolves to
      declaringFile = this.moduleResolver.resolveModuleFile(exportInfo.module, declaringFile, this.context.getCompilerOptions())?.resolvedFileName;
    }
    const namespace = declaringFile ? this.moduleResolver.resolveNamespace(declaringFile, this.currentSourceFile.fileName) : null;
    return {namespace, exportName};
  }

  /**
   * @param node {ts.Node}
   * @param name {string}
   * @returns {boolean} true if the name is imported or declared in the file containing the given node.
   */
  _isDeclaredInFile(node, name) {
    return this._getTypeReferenceResolver(node).isDeclaredInFile(name);
  }

  /**
   * @param node {ts.Node}
   * @param name {string} A name imported or declared in the file containing the given node.
   * @returns {ts.Declaration|null} see TypeReferenceResolver.findDeclaration
   */
  _findDeclaration(node, name) {
    return this._getTypeReferenceResolver(node).findDeclaration(name);
  }

  /**
   * @param node {ts.Node}
   * @returns {TypeReferenceResolver} The resolver for the file containing the given node. This is not the current file for nodes of declarations in other files (e.g. imported type aliases).
   */
  _getTypeReferenceResolver(node) {
    const sourceFile = this.moduleResolver.findSourceFile(node) || this.currentSourceFile;
    return this.moduleResolver.getTypeReferenceResolverOfSourceFile(sourceFile, this.context.getCompilerOptions());
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
