/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.models = {

  modelMap: {},

  /**
   * @param {string} url
   *       relative URL points to the *-models.json file. Example: 'res/myproject-all.json'.
   */
  bootstrap: function(url) {
    var promise = url ? $.ajaxJson(url) : $.resolvedPromise({});
    return promise.then(this._preInit.bind(this, url));
  },

  _preInit: function(url, data) {
    if (data && data.error) {
      // The result may contain a json error (e.g. session timeout) -> abort processing
      throw {
        error: data.error,
        url: url
      };
    }
    this.init(data);
  },

  init: function(data) {
    this.modelMap = data;
  },

  /**
   * Returns a new instance of a model from the global modelMap.
   *
   * @param {string} modelId
   *          The id of the model.
   * @param {Object} parent
   *          Optional parent that is set on the returned object.
   * @returns {Object}
   */
  getModel: function(modelId, parent) {
    var model = this._get(modelId, 'model');
    if (parent) {
      model.parent = parent;
    }
    return model;
  },

  /**
   * Returns a new instance of of an extension from the global modelMap.
   *
   * @param {string} extensionId
   *          The id of the extension.
   * @returns {Object}
   */
  getExtension: function(extensionId) {
    return this._get(extensionId, 'extension');
  },

  /**
   * Returns a copy of the object in the global modelMap.
   *
   * @param id
   *          ID of the requested object (model or extension)
   * @param type
   *          Expected type of the requested object ('model' or 'extension')
   * @returns {Object}
   */
  _get: function(id, type) {
    var model = this.modelMap[id];
    if (!model) {
      throw new Error('No model map entry found for id \'' + id + '\'');
    }
    if (model.type !== type) {
      throw new Error('Model \'' + id + '\' is not of type \'' + type + '\'');
    }
    return $.extend(true, {}, model);
  },

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
   * @param extension
   *          extension to the parentModel
   * @param parentModel
   *          object which contains id's as properties
   * @returns parentModel extended by extension
   */
  extend: function(extension, parentModel) {
    if (typeof extension === 'string') {
      extension = this.getExtension(extension);
    }

    scout.assertParameter('extensions', extension.extensions);
    extension.extensions.forEach(function(extensionConfig) {
      var operation = scout.assertParameter('operation', extensionConfig.operation);
      var target = scout.assertParameter('target', extensionConfig.target);

      var targetObject;
      if (target.root) {
        targetObject = parentModel;
      } else {
        targetObject = scout.objects.findChildObjectByKey(parentModel, 'id', target.id);
      }
      if (!targetObject) {
        throw new Error('Extension target not found: [extension: ' + extension.id + ', target: ' + target.id + ']');
      }

      if (operation === 'appendTo') {
        $.extend(targetObject, extensionConfig.extension);
      } else if (operation === 'insert') {
        targetObject[target.property] = targetObject[target.property] || [];
        var targetArray = targetObject[target.property];
        var extensionArray = scout.arrays.ensure(extensionConfig.extension);
        this._bindExtensionsToBeforeOrAfter(target, extensionArray);
        var insertAt = this._findExtensionIndex(target, targetArray);
        scout.arrays.insertAll(targetArray, extensionArray, insertAt);
      }
    }.bind(this));

    return parentModel;
  },

  /**
   * Finds the index in the target array which is given through the target.
   *
   * @param target
   *          target information to search the index (either fixed index or a "before" or "after" tag).
   * @param targetArray
   *          array to search the extension index in.
   * @returns extension index between 0 and targetArray.length or targetArray.length if no index is found.
   */
  _findExtensionIndex: function(target, targetArray) {
    var insertAt = targetArray.length;
    if (target.before) {
      insertAt = scout.arrays.findIndex(targetArray, function(element) {
        return element.id === target.before || element.groupedWith === target.before;
      });
      if (insertAt === -1) {
        insertAt = targetArray.length;
      }
    }
    else if (target.after) {
      insertAt = scout.arrays.findIndex(targetArray, function(element) {
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
  },

  /**
   * Adds the groupedWith tag to all given extensions.
   *
   * @param target
   *          target to bind the extensions to.
   * @param extensionsArray
   *          extensions to bind
   */
  _bindExtensionsToBeforeOrAfter: function(target, extensionsArray) {
    var beforeOrAfter = target.before || target.after;
    if (beforeOrAfter && target.groupWithTarget) {
      extensionsArray.forEach(function(element) {
        element.groupedWith = beforeOrAfter;
      });
    }
  }

};
