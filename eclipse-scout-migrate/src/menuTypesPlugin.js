/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

// noinspection SpellCheckingInspection
import jscodeshift from 'jscodeshift';
import {defaultMenuTypesMap, defaultModuleMap, defaultRecastOptions, insertMissingImportsForTypes, mapType} from './common.js';

const j = jscodeshift.withParser('ts');

/**
 * @type import('ts-migrate-server').Plugin<{menuTypesMap?: object, moduleMap?: object}>
 */
const menuTypesPlugin = {
  name: 'menu-types-plugin',

  async run({text, fileName, options}) {
    let root = j(text),
      referencedTypes = new Set();
    const menuTypesMap = {...defaultMenuTypesMap, ...options.menuTypesMap},
      moduleMap = {...defaultModuleMap, ...options.moduleMap};

    // noinspection JSCheckFunctionSignatures
    root.find(j.ObjectProperty, {key: {name: 'menuTypes'}})
      .forEach(/** NodePath<ObjectProperty, ObjectProperty> */path => {
        let node = path.node;
        if (!node.value || node.value.type !== 'ArrayExpression') {
          return;
        }
        let elements = node.value.elements;
        for (let i = 0; i < elements.length; i++) {
          let element = elements[i];
          if (!element || element.type !== 'StringLiteral') {
            continue;
          }
          let menuType = getMenuTypeForStringMenuType(element.value, menuTypesMap, referencedTypes);
          if (!menuType) {
            continue;
          }
          elements.splice(i, 1, menuType);
        }
      });

    insertMissingImportsForTypes(j, root, Array.from(referencedTypes), moduleMap, fileName);
    return root.toSource(defaultRecastOptions);
  }
};

function getMenuTypeForStringMenuType(menuTypeString, menuTypeMap, referencedTypes) {
  let menuTypeInfo = menuTypeMap[menuTypeString];
  if (!menuTypeInfo) {
    return null;
  }
  let {objectType, menuTypes, menuType} = menuTypeInfo;
  if (!objectType || !menuTypes || !menuType) {
    return null;
  }

  // noinspection DuplicatedCode
  let module = 'scout';
  let name = objectType;
  if (objectType.indexOf('.') > -1) {
    [module, name] = objectType.split('.');
  }
  let type = mapType(j, module + '.' + name);
  referencedTypes.add(type);

  return j.memberExpression(j.memberExpression(j.identifier(name), j.identifier(menuTypes)), j.identifier(menuType));
}

export default menuTypesPlugin;
