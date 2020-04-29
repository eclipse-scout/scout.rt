/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, objects, scout} from '../index';
import $ from 'jquery';

let modelMap = {};

export function init(data) {
  modelMap = data;
}

/**
 * Returns a new instance of a model supplied by the given model func
 *
 * @param {Function} modelFunc A function that returns the model instance.
 * @param {Object} [parent]
 *          Optional parent that is set on the returned object.
 * @returns {Object}
 */
export function get(modelFunc, parent) {
  let model = modelFunc();
  if (parent) {
    model.parent = parent;
  }
  return model;
}

/**
 * Returns a new instance of of an extension from the global modelMap.
 *
 * @param {string} extensionId
 *          The id of the extension.
 * @returns {Object}
 */
export function getExtension(extensionId) {
  return _get(extensionId, 'extension');
}

/**
 * Returns a copy of the object in the global modelMap.
 *
 * @param id
 *          ID of the requested object (model or extension)
 * @param type
 *          Expected type of the requested object ('model' or 'extension')
 * @returns {Object}
 */

export function _get(id, type) {
  let model = modelMap[id];
  if (!model) {
    throw new Error('No model map entry found for id \'' + id + '\'');
  }
  if (scout.nvl(model.type, 'model') !== type) {
    throw new Error('Model \'' + id + '\' is not of type \'' + type + '\'');
  }
  return $.extend(true, {}, model);
}

/**
 * Extends 'parentModel' with the contents of 'extension'.
 *
 * Parent model
 * ------------
 * The 'parentModel' argument (mandatory) will be changed and returned.
 *
 * Extension
 * ---------
 * Needs a property id on the parentModel to find the extension point.
 * Syntax of the extension:
 *
 * [appendTo]
 * Adding or overriding a property:
 *   {
 *     "id": "..."
 *     "type": "extension"
 *     "extensions": [
 *       {
 *         "operation": "appendTo"
 *         "target": {
 *           "id": "someObjectID"
 *         }
 *         "extension": {
 *           "propertyX": "xyz"
 *         }
 *       }
 *     ]
 *   }
 *
 * [insert]
 * Adding new object to the tree:
 *   {
 *     "id": "..."
 *     "type": "extension"
 *     "extensions": [
 *       {
 *         "operation": "insert",
 *         "target": {
 *           "id": "someObjectID",
 *           "property": "collectionOfsomeObject",
 *           "before": "somObjectIDInPropertyArray"    // (alternative "index": 0)
 *         },
 *         "extension": {
 *            "id": "newObjectID",
 *            "propertyx": "someThing",
 *            "collectiony": [...]
 *         }
 *       }
 *     ]
 *   }
 *
 * To extend the root object directly, "target.root: true" can be used instead of "target.id".
 *
 * To group inserted elements positions with its target use:
 *   "target": {
 *     "id": "someObjectID",
 *     "property": "collectionOfsomeObject",
 *     "before": "somObjectIDInPropertyArray",
 *     "groupWithTarget": true
 *   }
 * This will group the properties together. future extensions which use "before": "somObjectIDInPropertyArray"
 * will insert new elements before the grouped items. (Works the same with "after".)
 *
 * The extension property can be an object or an array of objects.
 *
 * @param extension {Object|string|function|}
 *          extension to the parentModel.
 * @param parentModel {Object}
 *          object which contains id's as properties
 * @returns parentModel extended by extension
 */
export function extend(extension, parentModel) {
  if (typeof extension === 'string') {
    extension = getExtension(extension);
  }

  if (typeof extension === 'function') {
    extension = extension();
  }

  scout.assertParameter('extensions', extension.extensions);
  extension.extensions.forEach(extensionConfig => {
    let operation = scout.assertParameter('operation', extensionConfig.operation);
    let target = scout.assertParameter('target', extensionConfig.target);

    let targetObject;
    if (target.root) {
      targetObject = parentModel;
    } else {
      targetObject = objects.findChildObjectByKey(parentModel, 'id', target.id);
    }
    if (!targetObject) {
      throw new Error('Extension target not found: [extension: ' + extension.id + ', target: ' + target.id + ']');
    }

    if (operation === 'appendTo') {
      $.extend(targetObject, extensionConfig.extension);
    } else if (operation === 'insert') {
      targetObject[target.property] = targetObject[target.property] || [];
      let targetArray = targetObject[target.property];
      let extensionArray = arrays.ensure(extensionConfig.extension);
      _bindExtensionsToBeforeOrAfter(target, extensionArray);
      let insertAt = _findExtensionIndex(target, targetArray);
      arrays.insertAll(targetArray, extensionArray, insertAt);
    }
  });

  return parentModel;
}

/**
 * Finds the index in the target array which is given through the target.
 *
 * @param target
 *          target information to search the index (either fixed index or a "before" or "after" tag).
 * @param targetArray
 *          array to search the extension index in.
 * @returns extension index between 0 and targetArray.length or targetArray.length if no index is found.
 */

export function _findExtensionIndex(target, targetArray) {
  let insertAt = targetArray.length;
  if (target.before) {
    insertAt = arrays.findIndex(targetArray, element => {
      return element.id === target.before || element.groupedWith === target.before;
    });
    if (insertAt === -1) {
      insertAt = targetArray.length;
    }
  } else if (target.after) {
    insertAt = arrays.findIndex(targetArray, element => {
      return element.id === target.after || element.groupedWith === target.after;
    });
    if (insertAt === -1) {
      insertAt = targetArray.length;
    } else {
      insertAt++;
    }
  }
  if ($.isNumeric(target.index)) {
    insertAt = target.index;
  }
  return insertAt;
}

/**
 * Adds the groupedWith tag to all given extensions.
 *
 * @param target
 *          target to bind the extensions to.
 * @param extensionsArray
 *          extensions to bind
 */

export function _bindExtensionsToBeforeOrAfter(target, extensionsArray) {
  let beforeOrAfter = target.before || target.after;
  if (beforeOrAfter && target.groupWithTarget) {
    extensionsArray.forEach(element => {
      element.groupedWith = beforeOrAfter;
    });
  }
}

export default {
  init,
  extend,
  get,
  getExtension,
  modelMap
};
