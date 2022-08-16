import jscodeshift from 'jscodeshift';
import {defaultModuleMap, defaultParamTypeMap, defaultRecastOptions, defaultReturnTypeMap, findTypeByName, insertMissingImportsForTypes, methodFilter} from './common.js';

const j = jscodeshift.withParser('ts');
let referencedTypes;

/**
 * @type import('ts-migrate-server').Plugin<{paramTypeMap?: object, returnTypeMap?: object, moduleMap?: object}>
 */
const methodsPlugin = {
  name: 'methods-plugin',

  async run({text, options}) {
    let root = j(text);
    const paramTypeMap = {...defaultParamTypeMap, ...options.paramTypeMap};
    const returnTypeMap = {...defaultReturnTypeMap, ...options.returnTypeMap};
    const moduleMap = {...defaultModuleMap, ...options.moduleMap};
    referencedTypes = new Set();

    root.find(j.Declaration)
      .filter(path => methodFilter(j, path))
      .forEach(expression => {
        let node = expression.node;
        if (node.params) {
          for (let param of node.params) {
            processParamType(param, Object.values(paramTypeMap));
          }
        }
        processReturnType(node, Object.values(returnTypeMap));
      });

    insertMissingImportsForTypes(j, root, Array.from(referencedTypes), moduleMap);
    return root.toSource(defaultRecastOptions);
  }
};

function processReturnType(func, typeMaps) {
  let name = func.key ? func.key.name : func.id.name;
  if (func.returnType) {
    return;
  }
  let typeDesc = findTypeByName(j, typeMaps, name);
  if (typeDesc) {
    func.returnType = j.tsTypeAnnotation(typeDesc.type);
    if (typeDesc.module) {
      referencedTypes.add(typeDesc);
    }
  }
}

function processParamType(param, typeMaps) {
  let name = param.name;
  if (param.typeAnnotation) {
    return;
  }
  let typeDesc = findTypeByName(j, typeMaps, name);
  if (typeDesc) {
    param.typeAnnotation = j.tsTypeAnnotation(typeDesc.type);
    if (typeDesc.module) {
      referencedTypes.add(typeDesc);
    }
  }
}

export default methodsPlugin;
