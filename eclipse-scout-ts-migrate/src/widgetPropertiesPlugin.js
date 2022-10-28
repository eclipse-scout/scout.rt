import jscodeshift from 'jscodeshift';
import {defaultModuleMap, defaultParamTypeMap, defaultRecastOptions, defaultReturnTypeMap, findClassName, findClassProperty, findParentClassBody, findParentPath, findTypeByName, getNameForType, insertMissingImportsForTypes, mapType, methodFilter} from './common.js';

const j = jscodeshift.withParser('ts');
let referencedTypes;

/**
 * @type import('ts-migrate-server').Plugin<{paramTypeMap?: object, moduleMap?: object, defaultParamType?: string}>
 */
const widgetPropertiesPlugin = {
  name: 'widget-properties-plugin',

  async run({text, options}) {
    let root = j(text);
    const paramTypeMap = {...defaultParamTypeMap, ...options.paramTypeMap};
    const moduleMap = {...defaultModuleMap, ...options.moduleMap};
    referencedTypes = new Set();

    let result = new Map();

    // Find all widget properties for each class in file
    root.find(j.Declaration)
      .filter(path => path.node.type === j.ClassMethod.name && path.node.kind === 'constructor')
      .find(j.Identifier)
      .filter(path => path.node.name === '_addWidgetProperties' || path.node.name === '_addPreserveOnPropertyChangeProperties')
      .forEach(path => {
        let argument = path.parentPath.parentPath.value.arguments[0];
        let className = findClassName(path);
        let widgetProperties = result.get(className);
        if (!widgetProperties) {
          widgetProperties = [];
          result.set(className, widgetProperties);
        }
        if (path.node.name === '_addPreserveOnPropertyChangeProperties') {
          // Don't add resp. remove preserve properties
          if (argument.type === j.StringLiteral.name) {
            removeFromArray(widgetProperties, argument.value);
          } else if (argument.type === j.ArrayExpression.name) {
            for (let prop of argument.elements.map(elem => elem.value)) {
              removeFromArray(widgetProperties, prop);
            }
          }
        } else {
          if (argument.type === j.StringLiteral.name) {
            widgetProperties.push(argument.value);
          } else if (argument.type === j.ArrayExpression.name) {
            widgetProperties.push(...argument.elements.map(element => element.value));
          }
        }
      });

    // Find all setters, use data type of the property and wrap it with the ChildRef type
    root.find(j.ClassMethod)
      .filter(path => path.node.key.name.startsWith('set'))
      .forEach(path => {
        let className = findClassName(path);
        let widgetProperties = result.get(className);
        if (!widgetProperties) {
          return;
        }
        let setter = path.node;
        let setterName = setter.key.name;
        let propertyName = setterName.substring(3, 4).toLowerCase() + setterName.substring(4);
        if (!widgetProperties.includes(propertyName)) {
          return;
        }
        let property = findClassProperty(findParentClassBody(path), propertyName);
        if (!property) {
          return;
        }
        let typeAnnotation = property.typeAnnotation?.typeAnnotation;
        if (!typeAnnotation) {
          return;
        }
        let newType;
        if (typeAnnotation.type === j.TSArrayType.name) {
          newType = mapType(j, 'scout.ChildRef[]');
          newType.type.elementType.typeParameters = j.tsTypeParameterInstantiation([typeAnnotation.elementType]);
        } else {
          newType = mapType(j, 'scout.ChildRef');
          newType.type.typeParameters = j.tsTypeParameterInstantiation([typeAnnotation]);
        }
        setter.params[0].typeAnnotation = j.tsTypeAnnotation(newType.type);
        referencedTypes.add(newType);
      });

    insertMissingImportsForTypes(j, root, Array.from(referencedTypes), moduleMap);
    return root.toSource(defaultRecastOptions);
  }
};

function removeFromArray(array, elem) {
  let index = array.indexOf(elem);
  if (index > -1) {
    array.splice(index, 1);
  }
}

export default widgetPropertiesPlugin;
