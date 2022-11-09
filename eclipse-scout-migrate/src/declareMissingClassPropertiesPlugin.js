/*
 * Based on https://github.com/airbnb/ts-migrate/blob/master/packages/ts-migrate-plugins/src/plugins/declare-missing-class-properties.ts (MIT License)
 * See readme.md for details.
 */
import jscodeshift from 'jscodeshift';
import {validateAnyAliasOptions} from 'ts-migrate-plugins/build/src/utils/validateOptions.js';
import {isDiagnosticWithLinePosition} from 'ts-migrate-plugins/build/src/utils/type-guards.js';
import {defaultModuleMap, defaultParamTypeMap, defaultRecastOptions, findClassProperty, findIndex, findParentClassBody, findParentPath, getTypeFor, inConstructor, insertMissingImportsForTypes, removeEmptyLinesBetweenImports, transformCommentLinesToJsDoc} from './common.js';

const j = jscodeshift.withParser('ts');
let root;
let referencedTypes;
const propertyComparator = (a, b) => {
  // Move $ and _ variables to the end
  if (a.startsWith('_') && !b.startsWith('_')) {
    return 1;
  }
  if (b.startsWith('_') && !a.startsWith('_')) {
    return -1;
  }
  if (a.startsWith('$') && !b.startsWith('$')) {
    return 1;
  }
  if (b.startsWith('$') && !a.startsWith('$')) {
    return -1;
  }
  return 0;
};

/**
 * @type import('ts-migrate-server').Plugin<{ anyAlias?: string, typeMap?: object, moduleMap?: object}>
 */
const declareMissingClassPropertiesPlugin = {
  name: 'declare-missing-class-properties',

  async run({text, fileName, getLanguageService, options, sourceFile}) {
    const diagnostics = getLanguageService()
      .getSemanticDiagnostics(fileName)
      .filter(isDiagnosticWithLinePosition)
      .filter(diagnostic => diagnostic.code === 2339 || diagnostic.code === 2551); // 2339: Property_0_does_not_exist_on_type_1, 2551: Property_0_does_not_exist_on_type_1_Did_you_mean_

    root = j(text);

    const toAdd = [];
    const typeMap = {...defaultParamTypeMap, ...options.typeMap};
    const moduleMap = {...defaultModuleMap, ...options.moduleMap};
    referencedTypes = new Set();

    // Diagnostics are errors reported by typescript -> the plugin only processes missing properties
    diagnostics.forEach(diagnostic => {
      root
        .find(j.Identifier)
        .filter(
          path =>
            (path.node).start === diagnostic.start &&
            (path.node).end === diagnostic.start + diagnostic.length &&
            path.parentPath.node.type === 'MemberExpression' &&
            path.parentPath.node.object.type === 'ThisExpression' &&
            path.parentPath.parentPath.node.type === 'AssignmentExpression' && path.parentPath.parentPath.node.operator === '=' &&
            inConstructor(path)
        )
        .forEach(path => {
          const classBody = findParentClassBody(path);
          if (!classBody) {
            return;
          }
          let item = toAdd.find(cur => cur.classBody === classBody);
          if (!item) {
            item = {classBody, propertyNames: new Map()};
            toAdd.push(item);
          }

          let assignment = findParentPath(path, parentPath => parentPath.node.type === 'AssignmentExpression');
          let typeDesc = getTypeFor(j, path.node.name, assignment.node.right, Object.values(typeMap));
          if (typeDesc && typeDesc.module) {
            referencedTypes.add(typeDesc);
          }
          let property = {
            type: typeDesc.type
          };
          if (assignment.parentPath.node.comments) {
            property.comments = assignment.parentPath.node.comments;
            // Remove comment from property assignment
            assignment.parentPath.node.comments = null;
          }
          item.propertyNames.set(path.node.name, property);
        });
    });

    toAdd.forEach(({classBody, propertyNames: properties}) => {
      const /** @type {string[]}*/ propertyNames = Array.from(properties.keys())
        .filter(propertyName => {
          const existingProperty = findClassProperty(classBody, propertyName);
          return existingProperty == null;
        })
        .sort(propertyComparator);

      // Insert before constructor
      let index = findIndex(classBody.node.body, node => node.type === 'ClassMethod' && node.kind === 'constructor');
      if (index < 0) {
        index = 0;
      }
      classBody.node.body.splice(
        index,
        0,
        ...propertyNames.map(propertyName => {
          let propertyDesc = properties.get(propertyName);
          let prop = j.classProperty(
            j.identifier(propertyName),
            null,
            j.tsTypeAnnotation(propertyDesc.type)
          );
          let comments = transformCommentLinesToJsDoc(j, propertyDesc.comments);
          if (comments) {
            prop.comments = comments;
          }
          return prop;
        })
      );
    });

    insertMissingImportsForTypes(j, root, Array.from(referencedTypes), moduleMap, sourceFile.fileName);
    return removeEmptyLinesBetweenImports(root.toSource(defaultRecastOptions));
  },

  validate: validateAnyAliasOptions
};

export default declareMissingClassPropertiesPlugin;
