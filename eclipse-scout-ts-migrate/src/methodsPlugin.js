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
        removeJsDocTypes(node);
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

/**
 * Removes all types form jsdoc if the types exist as TS types
 */
function removeJsDocTypes(func) {
  let comments = func.leadingComments;
  if (!comments) {
    return;
  }
  let replaceReturnType = !!func.returnType;
  let replaceParams = [];
  if (func.params) {
    replaceParams = func.params.filter(param => !!param.typeAnnotation);
  }
  if (!replaceReturnType && replaceParams.length === 0) {
    return;
  }
  func.comments = comments.map(comment => {
    if (comment.type !== 'CommentBlock') {
      return comment;
    }
    let str = comment.value;
    str = str.replaceAll(/ +\*/g, ''); // remove * at start of lines, makes the upcoming processing easier
    let beforeReplace = str;
    if (replaceReturnType) {
      str = str.replace(/(@return[s]?) ({.*})/, '@returns'); // remove type from @return
      str = str.replace(/@returns[\s]*$/, ''); // remove empty return at the end
    }
    for (let param of replaceParams) {
      str = str.replace(new RegExp(`@param {.*} \\[?${param.name}\\]?`), `@param ${param.name}`); // remove type from @param
      str = str.replace(new RegExp(`@param \\[?${param.name}\\]? {.*}`), `@param ${param.name}`); // consider reverse typing as well (@param [var] {type} instead of @param {type} [var]
      str = str.replace(new RegExp(`@param \\[${param.name}\\]`), `@param ${param.name}`); // remove brackets even without type
      str = str.replace(new RegExp(`@param ${param.name}[\\s]*$`), ''); // remove empty param at end
      str = str.replace(new RegExp(`@param ${param.name}[\\s]*@param`), '@param'); // remove empty param before param
      str = str.replace(new RegExp(`@param ${param.name}[\\s]*@return`), '@return'); // remove empty param before return
    }
    if (beforeReplace === str) {
      // Do nothing if nothing was replaced
      return comment;
    }
    str = str.replace(/\r\n +$/, '\r\n'); // remove whitespaces at the end (keep new line)
    str = str.replaceAll(/\r\n/g, '\r\n *'); // add * again
    str = str.replace(/\r\n \*$/, '\r\n'); // Remove last * that was added by the line before. It will be added automatically by the block comment.
    if (str.trim() === '*') {
      // Remove empty comments
      return null;
    }
    return j.commentBlock(str);
  }).filter(comment => comment !== null);
}

export default methodsPlugin;
