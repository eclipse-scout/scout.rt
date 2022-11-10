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

/* eslint-disable @typescript-eslint/indent */

import path from 'path';

export function lfToCrlf(text) {
  return text.replace(/(?!\r)\n/gm, '\r\n');
}

export function crlfToLf(text) {
  return text.replace(/\r\n/gm, '\n');
}

export function inConstructor(path) {
  return !!findParentPath(path, parentPath => parentPath.node.type === 'ClassMethod' && parentPath.node.kind === 'constructor');
}

export function findParentClassBody(path) {
  return findParentPath(path, parentPath => parentPath.node.type === 'ClassBody');
}

export function findClassName(path) {
  return findParentPath(path, parentPath => parentPath.node.type === 'ClassDeclaration').value.id.name;
}

export function findParentPath(path, predicate) {
  let cur = path;
  while (cur.node.type !== 'Program') {
    if (predicate(cur)) {
      return cur;
    }
    cur = cur.parentPath;
  }
  return undefined;
}

export function findClassProperty(classBody, propertyName) {
  return classBody.node.body.find(
    n =>
      n.type === 'ClassProperty' &&
      n.key.type === 'Identifier' &&
      n.key.name === propertyName
  );
}

/**
 * @typedef {object} TypeDesc
 * @property type
 * @property module?: string
 */

/**
 * @returns {TypeDesc}
 */
export function getTypeFor(j, name, value, typeMaps) {
  switch (value.type) {
    case 'StringLiteral':
      return {type: j.tsStringKeyword()};
    case 'BooleanLiteral':
      return {type: j.tsBooleanKeyword()};
    case 'NumericLiteral':
      return {type: j.tsNumberKeyword()};
    case 'NewExpression':
      return {type: j.tsTypeReference(j.identifier(value.callee.name))};
    case 'ArrayExpression': {
      if (value.elements.length === 0) {
        // If element is empty, find type based on name.
        return findTypeByName(j, typeMaps, name) || {type: j.tsArrayType(j.tsAnyKeyword())};
      }
      let elementType = getTypeFor(j, null, value.elements[0]).type;
      return {type: j.tsArrayType(elementType)};
    }
    default: {
      let typeDesc = findTypeByName(j, typeMaps, name);
      if (typeDesc) {
        return typeDesc;
      }
      return {type: j.tsAnyKeyword()};
    }
  }
}

export function getNameForType(j, type) {
  if (!type) {
    return null;
  }
  switch (type.type) {
    case 'TSStringKeyword':
      return 'string';
    case 'TSBooleanKeyword':
      return 'boolean';
    case 'TSNumberKeyword':
      return 'number';
    case 'TSAnyKeyword':
      return 'any';
    case 'TSTypeReference':
      return type.typeName.name;
    case 'TSArrayType': {
      return getNameForType(j, type.elementType) + '[]';
    }
    default: {
      return null;
    }
  }
}

/**
 * @returns {TypeDesc} the codeshift type for the string based names used in type maps.
 */
export function mapType(j, name) {
  if (name.endsWith('[]')) {
    name = name.substring(0, name.length - 2);
    let typeDesc = mapType(j, name);
    if (typeDesc) {
      return {type: j.tsArrayType(typeDesc.type), module: typeDesc.module};
    }
  }

  let type;
  let module;
  switch (name) {
    case 'string':
      type = j.tsStringKeyword();
      break;
    case 'boolean':
      type = j.tsBooleanKeyword();
      break;
    case 'number':
      type = j.tsNumberKeyword();
      break;
    case 'any':
      type = j.tsAnyKeyword();
      break;
    case 'void':
      type = j.tsVoidKeyword();
      break;
    default:
      if (name.indexOf('.') > -1) {
        [module, name] = name.split('.');
      }
      type = j.tsTypeReference(j.identifier(name));
      break;
  }
  return {type, module};
}

/**
 * @returns {TypeDesc|null}
 */
export function findTypeByName(j, typeMaps, name) {
  if (!name) {
    return null;
  }
  let type = _findTypeByName();
  if (type) {
    return type;
  }
  // Ignore leading _ and try again
  if (name.startsWith('_')) {
    name = name.substring(1, name.length);
  }
  return _findTypeByName();

  function _findTypeByName() {
    for (let map of typeMaps) {
      if (map.predicate(name)) {
        return mapType(j, map.type);
      }
    }
  }
}

export function methodFilter(j, path) {
  return path.node.type === j.ClassMethod.name ||
    // All exported methods in a file that is not a class (e.g. utilities)
    (!findParentClassBody(path) && path.node?.type === 'FunctionDeclaration' && path.parentPath.node.type === 'ExportNamedDeclaration');
}

export function isOneOf(value, ...args) {
  if (args.length === 0) {
    return false;
  }
  let argsToCheck = args;
  if (args.length === 1 && Array.isArray(args[0])) {
    argsToCheck = args[0];
  }
  return argsToCheck.indexOf(value) !== -1;
}

export function findIndex(arr, predicate) {
  if (!arr || !predicate) {
    return -1;
  }
  for (let i = 0; i < arr.length; i++) {
    if (predicate(arr[i], i, arr)) {
      return i;
    }
  }
  return -1;
}

export function findLastIndex(arr, predicate) {
  if (!arr || !predicate) {
    return -1;
  }
  let index = -1;
  for (let i = 0; i < arr.length; i++) {
    if (predicate(arr[i], i, arr)) {
      index = i;
    }
  }
  return index;
}

/**
 * @returns {Collection<ImportDeclaration>}
 */
export function findImportDeclarations(j, source, predicate) {
  return source
    .find(j.ImportDeclaration)
    .filter(path => predicate(path.node.source.value));
}

/**
 * Returns the {@link ImportSpecifier}s for the given specifier name in the import declaration.
 * E.g. if the specifierName is b and the import declaration `import {a, b} from 'module'`, the {@link ImportSpecifier} for b wil be returned because it is in the list.
 * @param  {Collection<ImportDeclaration>} importDeclaration
 * @param {string} specifierName
 * @returns {Collection<ImportSpecifier>}
 */
export function findImportSpecifiers(j, importDeclaration, specifierName) {
  return importDeclaration
    .find(j.ImportSpecifier)
    .filter(path => path.value.imported.name === specifierName);
}

/**
 * @param {Collection<ImportDeclaration>} importDeclaration
 * @param {string} specifierName
 */
export function hasImportSpecifier(j, importDeclaration, specifierName) {
  return !!findImportSpecifiers(j, importDeclaration, specifierName)?.length;
}

/**
 * Inserts a new import to a declaration.
 * E.g. if specifierName is b, it will add b to the list of imports (import {a} from 'module' -> import {a, b} from 'module')
 * @param {Collection<ImportDeclaration>} importDeclaration
 * @param {string} specifierName
 */
export function insertImportSpecifier(j, importDeclaration, specifierName) {
  const importSpecifier = j.importSpecifier(j.identifier(specifierName));

  importDeclaration.forEach(declaration => {
    j(declaration).replaceWith(
      j.importDeclaration(
        sortImportSpecifiers([...declaration.node.specifiers, importSpecifier]),
        declaration.node.source
      )
    );
  });
}

function getFirstNode(j, source) {
  return source.find(j.Program).get('body', 0).node;
}

function getClassName(j, source) {
  let declarations = source.find(j.ClassDeclaration);
  if (declarations.length === 0) {
    return null;
  }
  return declarations.get(0).parentPath.value.id.name;
}

export function sortImportSpecifiers(specifiers) {
  return specifiers.sort((s1, s2) => {
    if (!s1.imported || !s2.imported) {
      return 0; // one specifier is probably an ImportDefaultSpecifier
    }
    return s1.imported.name.localeCompare(s2.imported.name);
  });
}

/**
 * @param {TypeDesc[]} typeDescriptors
 * @param moduleMap
 */
export function insertMissingImportsForTypes(j, source, typeDescriptors, moduleMap, currentFilePath) {
  let modules = typeDescriptors.map(typeDesc => typeDesc.module);
  if (modules.length === 0) {
    return;
  }

  // Save the comments attached to the first node
  const firstNode = getFirstNode(j, source);
  const {comments} = firstNode;
  const className = getClassName(j, source);

  for (let i = 0; i < modules.length; i++) {
    let module = modules[i];
    let moduleName = moduleMap[module];
    let predicate = name => name === moduleName;
    if (typeof moduleName === 'function') {
      predicate = moduleName;
    } else if (typeof moduleName === 'string' && moduleName.startsWith('path:')) {
      // Get relative path from current fiel to module
      moduleName = path.relative(path.parse(currentFilePath).dir, path.resolve(moduleName.substring(moduleName.indexOf(':') + 1)));
      // Remove file ending
      moduleName = moduleName.substring(0, moduleName.lastIndexOf('.'));
      // Always use forward slashes
      moduleName = moduleName.replaceAll('\\', '/');
    }
    let declarations = findImportDeclarations(j, source, predicate);
    if (declarations.length === 0) {
      insertImportDeclaration(j, source, moduleName);
      declarations = findImportDeclarations(j, source, predicate);
    }

    let typeDesc = typeDescriptors[i];
    let typeName = getTypeName(typeDesc.type);
    if (typeName !== className && !hasImportSpecifier(j, declarations, typeName)) {
      insertImportSpecifier(j, declarations, typeName);
    }
  }

  // When the imports are replaced, the comment on the first node (likely the copy right header) is removed -> Attach comment again
  // See also https://github.com/facebook/jscodeshift/blob/master/recipes/retain-first-comment.md
  const newFirstNode = getFirstNode(j, source);
  if (newFirstNode !== firstNode) {
    firstNode.comments = null; // Delete comment if another import was added before that will receive it
    newFirstNode.comments = comments;
  }
}

export function insertImportDeclaration(j, source, moduleName) {
  if (typeof moduleName !== 'string') {
    // Not possible to add an import declaration
    return;
  }
  const declaration = j.importDeclaration(
    [],
    j.stringLiteral(moduleName)
  );

  let body = source.get().node.program.body;
  let index = -1;
  if (isFileImport(moduleName)) {
    // Put file imports at the end (after imports form node_modules)
    index = findLastIndex(body, node => node.type === 'ImportDeclaration');
  } else {
    // Put imports from node_modules before file imports
    index = findLastIndex(body, node =>
      node.type === 'ImportDeclaration' && !isFileImport(node.source.value));
  }
  if (index >= 0) {
    body.splice(index + 1, 0, declaration);
  } else {
    // Insert it at the top of the file
    body.unshift(declaration);
  }
}

function isFileImport(importName) {
  return importName.includes('./') || importName.includes('../');
}

export function removeEmptyLinesBetweenImports(text) {
  return text.replaceAll(/\r\n\r\nimport /g, '\r\nimport ');
}

function getTypeName(type) {
  if (type.type === 'TSArrayType') {
    return type.elementType.typeName.name;
  }
  return type.typeName.name;
}

export function transformCommentLinesToJsDoc(j, comments) {
  if (!comments) {
    return null;
  }
  let commentTexts = [];
  if (comments[0].type === 'CommentBlock') {
    if (comments[0].value.startsWith('*')) {
      // Already js doc
      return comments;
    }
    // Convert regular block to lines first
    commentTexts = comments[0].value.trim().split('\r\n').map(comment => ' ' + comment.trim());
  } else {
    commentTexts = comments.map(comment => comment.value);
  }
  let str = '';
  if (commentTexts.length === 1) {
    str += `*${commentTexts[0]} `;
  } else {
    str = '*\r\n';
    for (let comment of commentTexts) {
      str += ` *${comment}\r\n`;
    }
  }
  return [j.commentBlock(str)];
}

export const defaultParamTypeMap = {
  JQuery: {
    predicate: name => name.startsWith('$') || name.startsWith('_$'), // check for _ explicitly to ensure no other predicate matches (e.g. FormField)
    type: 'JQuery'
  },
  number: {
    predicate: name => isOneOf(name, 'width', 'height', 'top', 'bottom', 'right', 'left', 'x', 'y', 'length', 'maximumUploadSize', 'viewRangeSize', 'count', 'selectionStart', 'selectionEnd',
        'sortCode', 'dense', 'delay', 'maxContentLines', 'useOnlyInVisibleColumns', 'index')
      || name.endsWith('Length') || name.endsWith('Width') || name.endsWith('Height') || name.endsWith('WidthInPixel') || name.endsWith('Count')
      || name.endsWith('Top') || name.endsWith('Left') || name.endsWith('Index') || name.endsWith('ingX') || name.endsWith('ingY') || name.endsWith('Delay'),
    type: 'number'
  },
  boolean: {
    predicate: name => isOneOf(name, 'loading', 'loaded', 'toggleAction', 'compact', 'exclusiveExpand', 'active', 'visible', 'enabled', 'checked', 'selected', 'selectable', 'hasText', 'invalidate', 'modal', 'closable', 'resizable',
        'movable', 'askIfNeedSave', 'showOnOpen', 'scrollable', 'updateDisplayTextOnModify', 'autoRemove', 'mandatory', 'stackable', 'shrinkable', 'required', 'collapsed', 'collapsible', 'expanded', 'expandable',
        'editable', 'preventDoubleClick', 'autoCloseExternalWindow', 'hasDate', 'hasTime', 'focused', 'responsive', 'wrapText', 'tabbable', 'virtual', 'busy', 'trimText', 'browseAutoExpandAll', 'browseLoadIncremental', 'browseHierarchy',
        'minimized', 'maximized', 'failed', 'running', 'stopped', 'requestPending', 'pending', 'inputMasked', 'formatLower', 'formatUpper', 'marked', 'overflown', 'multiSelect', 'multiCheck', 'scrollToSelection', 'trackLocation',
        'autoFit', 'multiline', 'multilineText', 'hierarchical', 'loadIncremental', 'hidden', 'hiddenByUi', 'shown', 'withArrow', 'trimWidth', 'trimHeight', 'autoResizeColumns', 'filterAccepted', 'withPlaceholders',
        'clickable', 'empty', 'changing', 'inheritAccessibility', 'embedDetailContent', 'displayable', 'compacted', 'autoOptimizeWidth')
      || name.endsWith('Visible') || name.endsWith('Enabled') || name.endsWith('Focused') || name.endsWith('Required') || name.endsWith('Collapsed')
      || name.endsWith('Minimized') || name.endsWith('Focusable') || name.endsWith('Active') || name.endsWith('Expanded'),
    type: 'boolean'
  },
  string: {
    predicate: name => isOneOf(name, 'displayText', 'text', 'cssClass', 'displayViewId', 'title', 'subTitle', 'subtitle', 'titleSuffix', 'iconId', 'label', 'subLabel', 'imageUrl', 'logoUrl', 'titleSuffix')
      || name.endsWith('IconId') || name.endsWith('CssClass') || name.endsWith('Text'),
    type: 'string'
  },
  Date: {
    predicate: name => name.endsWith('Date') || name.endsWith('Time'),
    type: 'Date'
  },
  HtmlComponent: {
    predicate: name => isOneOf(name, 'htmlComp', 'htmlContainer', 'htmlBody', 'htmlChild'),
    type: 'scout.HtmlComponent'
  },
  Session: {
    predicate: name => name === 'session',
    type: 'scout.Session'
  },
  Desktop: {
    predicate: name => name === 'desktop',
    type: 'scout.Desktop'
  },
  Actions: {
    predicate: name => isOneOf(name, 'actions'),
    type: 'scout.Action[]'
  },
  Popup: {
    predicate: name => isOneOf(name, 'popup'),
    type: 'scout.Popup'
  },
  Popups: {
    predicate: name => isOneOf(name, 'popups'),
    type: 'scout.Popup[]'
  },
  Insets: {
    predicate: name => isOneOf(name, 'insets'),
    type: 'scout.Insets'
  },
  IconDesc: {
    predicate: name => isOneOf(name, 'iconDesc'),
    type: 'scout.IconDesc'
  },
  Accordion: {
    predicate: name => isOneOf(name, 'accordion'),
    type: 'scout.Accordion'
  },
  BreadCrumbItem: {
    predicate: name => isOneOf(name, 'breadCrumbItem'),
    type: 'scout.BreadCrumbItem'
  },
  BreadCrumbItems: {
    predicate: name => isOneOf(name, 'breadCrumbItems'),
    type: 'scout.BreadCrumbItem[]'
  },
  Menu: {
    predicate: name => isOneOf(name, 'menu', 'menuItem'),
    type: 'scout.Menu'
  },
  Menus: {
    predicate: name => isOneOf(name, 'menus', 'menuItems', 'staticMenus', 'detailMenus', 'nodeMenus'),
    type: 'scout.Menu[]'
  },
  LookupCall: {
    predicate: name => isOneOf(name, 'lookupCall'),
    type: 'scout.LookupCall'
  },
  CodeType: {
    predicate: name => isOneOf(name, 'codeType'),
    type: 'scout.CodeType'
  },
  LookupRow: {
    predicate: name => isOneOf(name, 'lookupRow'),
    type: 'scout.LookupRow'
  },
  LookupRows: {
    predicate: name => isOneOf(name, 'lookupRows'),
    type: 'scout.LookupRow[]'
  },
  LookupResult: {
    predicate: name => isOneOf(name, 'lookupResult'),
    type: 'scout.LookupResult'
  },
  FormField: {
    predicate: name => isOneOf(name, 'formField', 'field') || name.endsWith('Field'),
    type: 'scout.FormField'
  },
  FormFields: {
    predicate: name => isOneOf(name, 'formFields', 'fields') || name.endsWith('Fields'),
    type: 'scout.FormField[]'
  },
  Column: {
    predicate: name => isOneOf(name, 'column'),
    type: 'scout.Column'
  },
  Columns: {
    predicate: name => isOneOf(name, 'columns'),
    type: 'scout.Column[]'
  },
  GridData: {
    predicate: name => isOneOf(name, 'gridData', 'gridDataHints'),
    type: 'scout.GridData'
  },
  Form: {
    predicate: name => isOneOf(name, 'form', 'displayParent') || name.endsWith('Form'),
    type: 'scout.Form'
  },
  Status: {
    predicate: name => isOneOf(name, 'errorStatus'),
    type: 'scout.Status'
  },
  Outline: {
    predicate: name => isOneOf(name, 'outline'),
    type: 'scout.Outline'
  },
  OutlineOverview: {
    predicate: name => isOneOf(name, 'outlineOverview'),
    type: 'scout.OutlineOverview'
  },
  Page: {
    predicate: name => isOneOf(name, 'page'),
    type: 'scout.Page'
  },
  TabItem: {
    predicate: name => isOneOf(name, 'tabItem'),
    type: 'scout.TabItem'
  },
  TabItems: {
    predicate: name => isOneOf(name, 'tabItems'),
    type: 'scout.TabItem[]'
  },
  KeyStroke: {
    predicate: name => isOneOf(name, 'keyStroke'),
    type: 'scout.KeyStroke'
  },
  Cell: {
    predicate: name => isOneOf(name, 'cell', 'headerCell'),
    type: 'scout.Cell'
  },
  Table: {
    predicate: name => isOneOf(name, 'table', 'detailTable'),
    type: 'scout.Table'
  },
  TableControl: {
    predicate: name => isOneOf(name, 'tableControl'),
    type: 'scout.TableControl'
  },
  TableControls: {
    predicate: name => isOneOf(name, 'tableControls'),
    type: 'scout.TableControl[]'
  },
  Tree: {
    predicate: name => isOneOf(name, 'tree'),
    type: 'scout.Tree'
  },
  TileGrid: {
    predicate: name => isOneOf(name, 'tileGrid'),
    type: 'scout.TileGrid'
  },
  Tile: {
    predicate: name => isOneOf(name, 'tile', 'focusedTile'),
    type: 'scout.Tile'
  },
  Tiles: {
    predicate: name => isOneOf(name, 'tiles'),
    type: 'scout.Tile[]'
  },
  Range: {
    predicate: name => isOneOf(name, 'viewRange'),
    type: 'scout.Range'
  },
  Widget: {
    predicate: name => isOneOf(name, 'widget', 'displayParent') || name.endsWith('Widget'),
    type: 'scout.Widget'
  },
  Widgets: {
    predicate: name => isOneOf(name, 'widgets'),
    type: 'scout.Widget[]'
  }
};

export const defaultReturnTypeMap = {
  JQuery: {
    predicate: name => name.startsWith('$') || name.startsWith('get$'),
    type: 'JQuery'
  },
  Dimension: {
    predicate: name => isOneOf(name, 'prefSize', 'preferredLayoutSize'),
    type: 'scout.Dimension'
  },
  KeyStrokeContext: {
    predicate: name => name === '_createKeyStrokeContext',
    type: 'scout.KeyStrokeContext'
  },
  boolean: {
    predicate: name => isOneOf(name, 'equals') || name.match(/^is[A-Z]/) || name.match(/^has[A-Z]/),
    type: 'boolean'
  },
  string: {
    predicate: name => isOneOf(name, 'toString'),
    type: 'string'
  }
};

// Value may be a function as well
export const defaultModuleMap = {
  scout: '@eclipse-scout/core'
};

export const defaultRecastOptions = {
  quote: 'single',
  objectCurlySpacing: false
};
