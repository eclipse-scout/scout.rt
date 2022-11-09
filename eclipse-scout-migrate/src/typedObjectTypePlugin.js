import jscodeshift from 'jscodeshift';
import {defaultModuleMap, defaultRecastOptions, insertMissingImportsForTypes, mapType, removeEmptyLinesBetweenImports} from './common.js';

const j = jscodeshift.withParser('ts');
let referencedTypes;

/**
 * @type import('ts-migrate-server').Plugin<{moduleMap?: object}>
 */
const typedObjectTypePlugin = {
  name: 'typed-object-type-plugin',

  async run({text, options, sourceFile}) {
    let root = j(text);
    const moduleMap = {...defaultModuleMap, ...options.moduleMap};
    referencedTypes = new Set();

    root.find(j.ObjectProperty)
      .filter(path =>
        path.value.key.name === 'objectType' &&
        path.value.value.type === j.StringLiteral.name)
      .forEach(path => {
        let objectType = path.value.value.value;
        path.node.value = getTypeForStringObjectType(objectType);
      });

    root.find(j.CallExpression)
      .filter(path =>
        path.value.callee.type === j.MemberExpression.name &&
        path.value.callee.object.name === 'scout' &&
        path.value.callee.property.name === 'create' &&
        path.value.arguments[0].type === j.StringLiteral.name)
      .forEach(path => {
        let objectType = path.value.arguments[0].value;
        path.node.arguments[0] = getTypeForStringObjectType(objectType);
      });

    insertMissingImportsForTypes(j, root, Array.from(referencedTypes), moduleMap, sourceFile.fileName);
    return removeEmptyLinesBetweenImports(root.toSource(defaultRecastOptions));
  }
};

function getTypeForStringObjectType(objectType) {
  let module = 'scout';
  let name = objectType;
  if (objectType.indexOf('.') > -1) {
    [module, name] = objectType.split('.');
  }
  let type = mapType(j, module + '.' + name);
  referencedTypes.add(type);
  return j.identifier(name);
}

export default typedObjectTypePlugin;
