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
import fs from 'fs';
import jscodeshift from 'jscodeshift';
import {crlfToLf, defaultRecastOptions, findParentPath, insertMissingImportsForTypes, mapType, removeEmptyLinesBetweenImports} from './common.js';

const j = jscodeshift.withParser('ts');

/**
 * @type import('ts-migrate-server').Plugin<{paramTypeMap?: object, moduleMap?: object, defaultParamType?: string}>
 */
const widgetColumnMapPlugin = {
  name: 'widget-column-map-plugin',

  async run({text, fileName, options}) {
    let className = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.'));

    if (!className || !className.endsWith('Model')) {
      return text;
    }

    let root = j(text);
    let widgets = new Map(),
      tables = new Map();

    // parse model and find all objects containing an id and objectType property
    // noinspection JSCheckFunctionSignatures
    root.find(j.ExportDefaultDeclaration)
      .find(j.ArrowFunctionExpression)
      .find(j.ObjectExpression, node => findObjectProperty(node, 'id') && findObjectProperty(node, 'objectType'))
      .forEach(path => {
        let node = path.node,
          idAndObjectType = getIdAndObjectType(node),
          objectType = idAndObjectType.objectType;
        if (isWidget(objectType)) {
          // remember id and objectType of all widget nodes
          widgets.set(node, idAndObjectType);
        }
        if (isColumn(objectType)) {
          // collect all column infos for one table
          let tablePath = findParentTablePath(path);
          if (!tablePath) {
            return;
          }
          let tableInfo = tables.get(tablePath.node);
          if (!tableInfo) {
            let tableFieldPath = findParentTableFieldPath(tablePath);
            tableInfo = {
              // create a table class name from the id of the table or the id of the tableField
              tableClassName: createTableClassName(getId(tablePath.node), tableFieldPath && getId(tableFieldPath.node)),
              columns: new Map()
            };
            tables.set(tablePath.node, tableInfo);
          }
          // remember id and objectType of all column nodes
          let columns = tableInfo.columns;
          columns.set(node, idAndObjectType);
        }
      });

    if (widgets.size > 0) { // only check size of widgets, if there are entries in tables then there are entries in widgets
      let body = root.get().node.program.body;

      // get/create widgetMap
      let widgetName = className.substring(0, className.lastIndexOf('Model')),
        widgetMapName = `${widgetName}WidgetMap`,
        widgetMapType = getOrCreateExportedType(widgetMapName, root, body),
        widgetMapMembers = getMembers(widgetMapType),
        widgetMapProperties = [];

      // create a property for every entry of widgets
      widgets.forEach(({id, objectType}, node) => {
        let tableInfo = tables.get(node);
        if (tableInfo) {
          // add specific table class if available, will be created later on
          objectType = tableInfo.tableClassName;
        }
        widgetMapProperties.push(createMapProperty(id, objectType));
      });

      // set/replace properties of widgetMap
      widgetMapMembers.splice(0, widgetMapMembers.length, ...widgetMapProperties);

      // create table class and columnMap for every table
      tables.forEach((tableInfo, node) => {
        // get/create columnMap
        let columnMapName = `${tableInfo.tableClassName}ColumnMap`,
          columnMapType = getOrCreateExportedType(columnMapName, root, body),
          columnMapMembers = getMembers(columnMapType),
          columnMapProperties = [];

        // create a property for every entry of tableInfo.columns
        tableInfo.columns.forEach(({id, objectType}) => {
          columnMapProperties.push(createMapProperty(id, objectType));
        });

        // set/replace properties of columnMap
        columnMapMembers.splice(0, columnMapMembers.length, ...columnMapProperties);

        // get/create tableClass
        let tableClassName = tableInfo.tableClassName,
          tableClass = getOrCreateExportedClass(tableClassName, root, body),
          tableMembers = tableClass.body.body,
          tableSuperClass = widgets.get(node).objectType,
          columnMapProperty = createClassProperty('columnMap', columnMapName);

        // set superClass to objectType from model
        tableClass.superClass = j.identifier(tableSuperClass);

        // declare columnMap property
        columnMapProperty.declare = true;
        tableMembers.splice(0, tableMembers.length, columnMapProperty);
      });

      let mainWidgetFileNameWithoutFileExtension = fileName.substring(0, fileName.lastIndexOf('Model')),
        mainWidgetFileName, mainWidgetRoot;

      // look for main widget as .js
      mainWidgetFileName = mainWidgetFileNameWithoutFileExtension + '.js';
      mainWidgetRoot = getMainWidgetRoot(mainWidgetFileName);

      // look for main widget as .ts
      if (!mainWidgetRoot) {
        mainWidgetFileName = mainWidgetFileNameWithoutFileExtension + '.ts';
        mainWidgetRoot = getMainWidgetRoot(mainWidgetFileName);
      }

      if (mainWidgetRoot) {
        if (mainWidgetFileName.endsWith('.js')) {
          // add widgetMap property with @type in .js case
          // noinspection JSCheckFunctionSignatures
          mainWidgetRoot
            .find(j.ClassDeclaration, {id: {name: widgetName}})
            .find(j.ClassMethod, {kind: 'constructor'})
            .forEach(/** NodePath<namedTypes.ClassMethod, namedTypes.ClassMethod> */path => {
              let node = path.node,
                constructorMembers = node.body.body,
                widgetMapAssignmentExpression = findConstructorAssignmentExpression(constructorMembers, 'widgetMap');
              if (!widgetMapAssignmentExpression) {
                // create an assignment expression after the super call
                let superCall = findConstructorSuperCall(constructorMembers),
                  superCallIndex = constructorMembers.indexOf(superCall) + 1;
                widgetMapAssignmentExpression = createAssignmentExpressionWithNull('widgetMap');
                constructorMembers.splice(superCallIndex, 0, widgetMapAssignmentExpression);
              }
              // add type comment
              widgetMapAssignmentExpression.comments = [createJsDocTypeComment(widgetMapName)];
            });
        }
        if (mainWidgetFileName.endsWith('.ts')) {
          // declare widgetMap property in .ts case
          // noinspection JSCheckFunctionSignatures
          mainWidgetRoot
            .find(j.ClassDeclaration, {id: {name: widgetName}})
            .forEach(/** NodePath<namedTypes.ClassDeclaration, namedTypes.ClassDeclaration> */path => {
              let node = path.node,
                classMembers = node.body.body,
                widgetMapProperty = findClassProperty(classMembers, 'widgetMap');
              if (widgetMapProperty) {
                widgetMapProperty.typeAnnotation = createTypeAnnotation(widgetMapName);
              } else {
                widgetMapProperty = createClassProperty('widgetMap', widgetMapName);
                classMembers.splice(0, 0, widgetMapProperty);
              }
              widgetMapProperty.declare = true;

              // remove widgetMap assignment from constructor
              let classConstructor = findConstructor(classMembers),
                constructorMembers = classConstructor ? classConstructor.body.body : [],
                widgetMapAssignmentExpression = classConstructor ? findConstructorAssignmentExpression(constructorMembers, 'widgetMap') : null;
              if (widgetMapAssignmentExpression) {
                constructorMembers.splice(constructorMembers.indexOf(widgetMapAssignmentExpression), 1);
              }
            });

          // insert missing imports
          insertMissingImportsForTypes(j, mainWidgetRoot, [mapType(j, `tempModule.${widgetMapName}`)], {tempModule: `./${className}`}, mainWidgetFileName);
        }

        // write file
        fs.writeFileSync(mainWidgetFileName, crlfToLf(removeEmptyLinesBetweenImports(mainWidgetRoot.toSource(defaultRecastOptions))));
      }
    }

    return root.toSource(defaultRecastOptions);
  }
};

function getMainWidgetRoot(mainWidgetFileName) {
  try {
    let mainWidgetBuffer = fs.readFileSync(mainWidgetFileName);
    return j(mainWidgetBuffer.toString());
  } catch (error) {
    // nop
  }
  return null;
}

function findObjectProperty(objectNode, propertyName) {
  return objectNode.properties.find(
    n =>
      n.type === 'ObjectProperty' &&
      n.key.type === 'Identifier' &&
      n.key.name === propertyName
  );
}

function findClassProperty(classMembers, propertyName) {
  return classMembers.find(n =>
    n.type === 'ClassProperty' &&
    n.key.type === 'Identifier' &&
    n.key.name === propertyName);
}

function findConstructor(classMembers) {
  return classMembers.find(n =>
    n.type === 'ClassMethod' &&
    n.kind === 'constructor'
  );
}

function findConstructorAssignmentExpression(constructorMembers, propertyName) {
  return constructorMembers.find(n =>
    n.type === 'ExpressionStatement' &&
    n.expression.type === 'AssignmentExpression' &&
    n.expression.left.type === 'MemberExpression' &&
    n.expression.left.object.type === 'ThisExpression' &&
    n.expression.left.property.type === 'Identifier' &&
    n.expression.left.property.name === propertyName
  );
}

function findConstructorSuperCall(constructorMembers) {
  return constructorMembers.find(n =>
    n.type === 'ExpressionStatement' &&
    n.expression.type === 'CallExpression' &&
    n.expression.callee.type === 'Super'
  );
}

function findParentTablePath(columnPath) {
  return findParentPathByObjectType(columnPath, isTable);
}

function findParentTableFieldPath(tablePath) {
  return findParentPathByObjectType(tablePath, isTableField);
}

function findParentPathByObjectType(path, objectTypePredicate) {
  return findParentPath(path, p => p.node.type === 'ObjectExpression' && objectTypePredicate((findObjectProperty(p.node, 'objectType') || {value: {}}).value.name));
}

function getId(node) {
  let idProperty = findObjectProperty(node, 'id');
  return ((idProperty || {}).value || {}).value;
}

function getObjectType(node) {
  let objectTypeProperty = findObjectProperty(node, 'objectType');
  return ((objectTypeProperty || {}).value || {}).name;
}

function getIdAndObjectType(node) {
  let id = getId(node),
    objectType = getObjectType(node);
  return {id, objectType};
}

function createTableClassName(tableId, tableFieldId) {
  if (tableId && tableId !== 'Table') {
    return tableId.replaceAll('.', '');
  }
  if (tableFieldId) {
    return tableFieldId.replaceAll('.', '') + 'Table';
  }
  throw new Error('At least one of tableId, tableFieldId must be set');
}

function getOrCreateExportedType(name, root, body) {
  let candidates = root
    .find(j.TSTypeAliasDeclaration)
    .filter(/** NodePath<TSTypeAliasDeclaration, TSTypeAliasDeclaration> */path => path.node.id.name === name);
  if (candidates.length) {
    return candidates.get().node;
  }
  let type = j.tsTypeAliasDeclaration(j.identifier(name), j.tsTypeLiteral([]));
  body.push(j.exportNamedDeclaration(type));
  return type;
}

function getMembers(type) {
  if (type.typeAnnotation.type === 'TSIntersectionType') {
    return (type.typeAnnotation.types.find(t => t.type === 'TSTypeLiteral') || {members: []}).members;
  }
  return type.typeAnnotation.members;
}

function getOrCreateExportedClass(name, root, body) {
  let candidates = root
    .find(j.ClassDeclaration)
    .filter(/** NodePath<ClassDeclaration, ClassDeclaration> */path => path.node.id.name === name);
  if (candidates.length) {
    return candidates.get().node;
  }
  let type = j.classDeclaration(j.identifier(name), j.classBody([]), null);
  body.push(j.exportNamedDeclaration(type));
  return type;
}

function createMapProperty(id, objectType) {
  let identifier = j.identifier(`'${id}'`),
    // add trailing ; to type, otherwise there is no ; at the end of the line when you use tsPropertySignature
    typeAnnotation = createTypeAnnotation(`${addGenericIfNecessary(objectType)};`);
  return j.tsPropertySignature(identifier, typeAnnotation);
}

function createClassProperty(id, objectType) {
  let identifier = j.identifier(id),
    typeAnnotation = createTypeAnnotation(addGenericIfNecessary(objectType));
  return j.classProperty(identifier, null, typeAnnotation);
}

function createTypeAnnotation(objectType) {
  return j.tsTypeAnnotation(j.tsTypeReference(j.identifier(objectType)));
}

function createAssignmentExpressionWithNull(propertyName) {
  let identifier = j.identifier(propertyName),
    member = j.memberExpression(j.thisExpression(), identifier),
    assignment = j.assignmentExpression('=', member, j.nullLiteral());
  return j.expressionStatement(assignment);
}

function createJsDocTypeComment(type) {
  return j.commentBlock(`* @type ${type} `);
}

function isWidget(objectType) {
  return objectType && !isColumn(objectType)
    && !objectType.endsWith('TableRow')
    && !objectType.endsWith('TreeNode')
    && !objectType.endsWith('Page')
    && objectType !== 'Status'
    && !objectType.endsWith('CodeType')
    && !objectType.endsWith('LookupCall');
}

function isColumn(objectType) {
  return objectType && objectType.endsWith('Column');
}

function isTable(objectType) {
  return objectType && objectType.endsWith('Table');
}

function isTableField(objectType) {
  return objectType && objectType.endsWith('TableField');
}

function needsGeneric(objectType) {
  return objectType && (objectType === 'SmartField'
    || objectType === 'SmartColumn'
    || objectType === 'ListBox'
    || objectType === 'TreeBox'
    || objectType === 'ModeSelectorField'
    || objectType === 'RadioButtonGroup'
    || objectType === 'RadioButton');
}

function addGenericIfNecessary(objectType) {
  if (needsGeneric(objectType)) {
    return `${objectType}<any>`;
  }
  return objectType;
}

export default widgetColumnMapPlugin;
