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
const CONSTANT_PATTERN = new RegExp('^[A-Z_0-9]+$');

function dataObjectTransformer(program, context) {
  const transformer = new DataObjectTransformer(program, context);
  return node => ts.visitNode(node, node => transformer.visit(node));
}

/**
 * See https://github.com/itsdouges/typescript-transformer-handbook
 */
class DataObjectTransformer {

  constructor(program, context) {
    this.program = program;
    this.context = context;
    this._namespace = null;
  }

  visit(node) {
    if (ts.isSourceFile(node)) {
      return this._visitChildren(node); // step into top level source files
    }
    if (ts.isClassDeclaration(node)) {
      const typeNameDecorator = node.modifiers?.find(m => ts.isDecorator(m) && m.expression?.expression?.escapedText === 'typeName');
      if (typeNameDecorator) {
        this._namespace = this._parseNamespace(typeNameDecorator);
        if (this._namespace) {
          const modifiedSubTree = this._visitChildren(node); // step into DO with typeName annotation and namespace
          this._namespace = null; // reset for next class declaration in same file
          return modifiedSubTree;
        }
        return node; // no need to step into
      }
      return node; // no need to step into
    }
    if (ts.isImportDeclaration(node) || ts.isExportDeclaration(node) || ts.isVariableStatement(node) || ts.isIdentifier(node) || ts.isTypeReferenceNode(node)
      || ts.isPropertySignature(node) || ts.isStringLiteral(node) || ts.isInterfaceDeclaration(node) || ts.isPropertyAssignment(node) || ts.isObjectLiteralExpression(node)
      || ts.isPropertyAccessExpression(node) || ts.isTypeAliasDeclaration(node) || ts.isParameter(node) || ts.isEnumDeclaration(node)
      || ts.isCallExpression(node) || ts.isExpressionStatement(node) || ts.isDecorator(node) || node.kind === ts.SyntaxKind.ExportKeyword) {
      return node; // no need to step into
    }

    if (ts.isPropertyDeclaration(node) && this._namespace /* only if inside a DO with namespace */ && !this._isSkipProperty(node)) {
      const newModifiers = [
        ...(node.modifiers || []), // existing
        ...this._createMetaDataAnnotationsFor(node) // newly added
      ];
      return ts.factory.replaceDecoratorsAndModifiers(node, newModifiers);
    }
    return node; // no need to step into
  }

  _parseNamespace(typeNameDecorator) {
    // FIXME mvi [js-bookmark] assumes the namespace of the dataobject typeName is the same as the namespace of the Scout JS module! Is this true?
    // FIXME mvi [js-bookmark] otherwise parse from:
    //  - for contributions: webpack config.output.library or webpack.config.file (ContributionBuildConfig)
    //  - normal module: the 'index' file (search for ObjectFactory.get().registerNamespace)
    const decoratorArgs = typeNameDecorator.expression?.arguments;
    if (!decoratorArgs?.length) {
      return null;
    }
    const typeName = decoratorArgs[0].text;
    if (!typeName) {
      return null;
    }
    const firstDotPos = typeName.indexOf('.');
    if (firstDotPos < 1) {
      return null;
    }
    return typeName.substring(0, firstDotPos);
  }

  _createMetaDataAnnotationsFor(node) {
    const metaDataAnnotations = [];
    let type = node.type;
    if (ts.isArrayTypeNode(type)) {
      let dimension = 1;
      type = type.elementType;
      while (ts.isArrayTypeNode(type)) {
        dimension++;
        type = type.elementType;
      }
      const arrayMetaAnnotation = this._createMetaDataAnnotation('a', ts.factory.createNumericLiteral(dimension));
      metaDataAnnotations.push(arrayMetaAnnotation);
    }
    metaDataAnnotations.push(this._createMetaDataAnnotation('t', this._createTypeNode(type)));
    return metaDataAnnotations;
  }

  _createTypeNode(node) {
    if (node.kind === ts.SyntaxKind.NumberKeyword) {
      // primitive number
      return ts.factory.createIdentifier('Number');
    }
    if (node.kind === ts.SyntaxKind.StringKeyword) {
      // primitive string
      return ts.factory.createIdentifier('String');
    }
    if (node.kind === ts.SyntaxKind.BooleanKeyword) {
      // primitive boolean
      return ts.factory.createIdentifier('Boolean');
    }
    // FIXME mvi [js-bookmark] handle Record and Partial?
    if (ts.isTypeReferenceNode(node)) {
      const name = node.typeName.escapedText;
      if (global[name]) {
        return ts.factory.createIdentifier(name); // e.g. Date, Number, String, Boolean
      }
      const qualifiedName = this._namespace === 'scout' ? name : this._namespace + '.' + name;
      return ts.factory.createStringLiteral(qualifiedName);
    }
    return ts.factory.createIdentifier('Object'); // e.g. any, void
  }

  _createMetaDataAnnotation(key/* string */, valueNode) {
    const reflect = ts.factory.createIdentifier('Reflect');
    const reflectMetaData = ts.factory.createPropertyAccessExpression(reflect, ts.factory.createIdentifier('metadata'));
    const keyNode = ts.factory.createStringLiteral('scout.meta.' + key);
    const call = ts.factory.createCallExpression(reflectMetaData, undefined, [keyNode, valueNode]);
    return ts.factory.createDecorator(call);
  }

  _isSkipProperty(node) {
    const propertyName = node.symbol?.escapedName;
    if (!propertyName || propertyName.startsWith('_') || propertyName.startsWith('$') || CONSTANT_PATTERN.test(propertyName)) {
      return true;
    }
    if (!node.modifiers) {
      return false;
    }
    const isStaticOrProtected = node.modifiers.some(n => n.kind === ts.SyntaxKind.StaticKeyword || n.kind === ts.SyntaxKind.ProtectedKeyword);
    if (isStaticOrProtected) {
      return true;
    }
    return node.modifiers.some(n => ts.isDecorator(n) && n.expression?.escapedText === 'dataObjectAttribute');
  }

  _visitChildren(node) {
    return ts.visitEachChild(node, n => this.visit(n), this.context);
  }
}

module.exports = {
  dataObjectTransformer
};
