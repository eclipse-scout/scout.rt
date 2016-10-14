/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
   * @param modelsUrl relative URL points to the *-models.json file. Example: 'myproject-models.json'.
   */
  bootstrap: function(modelsUrl) {
    scout.assertParameter('modelsUrl', modelsUrl);
    return $.ajaxJson(modelsUrl)
      .done(this.init.bind(this));
  },

  init: function(data) {
    this.modelMap = data;
  },

  /**
   * returns a json model from modelMap. The model needs to be of type 'model'
   * @param modelId the id of the model
   * @param parent Optional: if this parameter is given, a property 'parent' will be assigned to this parameter
   * @returns {Object}
   */
  getModel: function(modelId, parent) {
    var model = scout.models.get(modelId, 'model');
    if (parent) { // FIXME [awe] 6.1 - review with cyrill - in some cases (without subclasses) we still need the parent parameter (but it is more a convenience feature)
      model.parent = parent;
    }
    return model;
  },

  /**
   * returns a json extension from modelMap. The extension needs to be of type 'extension'
   * @param extensionId the id of the extension
   * @returns {Object}
   */
  getExtension: function(extensionId) {
    return scout.models.get(extensionId, 'extension');
  },

  /**
   * returns a copy of the json file in modelMap
   * @param id json file id
   * @param type value of type property
   * @returns {Object}
   */
  _get: function(id, type) {
    var json = this.modelMap[id];
    if (!json) {
      throw new Error('No JSON file defined for id=\'' + id + '\'. ' +
        'Check if file .json is listed in your *-module.json ' +
        'and if id matches the filename of the file .json file');
    }
    if(!json.type || json.type != type) {
      throw new Error(id + ' is not of type \''+ type + '\'');
    }
    return $.extend(true, {}, json);
  },

  /**
   * extends parentModel with the contents of extension. parentModel will be changed and returned.
   * @param extension extension to the parentModel. Needs a property id on the parentModel to find the extension point.
   * Syntax of the extension:
   * Adding or overriding a property:
   * {
   *  "id": "..."
   *  "type": "extension"
   *  "extensions": [{
   *      "operation": "appendTo"
   *      "target": {
   *      "id": "someObjectID"
   *      }
   *      "extension": {
   *        "propertyX": "xyz"
   *      }
   *     }
   *    ]
   *  }
   * Adding new Object to the tree:
   * {
   *  "id": "..."
   *  "type": "extension"
   *  "extensions": [{
   *      "operation": "insert"
   *      "target": {
   *      "id": "someObjectID"
   *      "property": "collectionOfsomeObject"
   *      "before": "somObjectIDInPropertyArray"    //(alternative "index":0)
   *      }
   *      "extension": {
   *          "id": "newObjectID",
   *           "propertyx": "someThing",
   *           "collectiony": [...]
   *      }
   *     }
   *    ]
   *  }
   *
   *  to extend the root object directly, target.root: true can be used instead of target.id
   * @param parentModel object which contains id's as properties
   * @returns parentModel extended by extension
   */
  extend: function(extension, parentModel) {
    if(typeof extension === 'string') {
        extension = scout.models.getExtension(extension);
    }
    scout.assertParameter('extensions', extension.extensions);
    extension.extensions.forEach(function(extensionConfig) {
      scout.assertParameter('operation', extensionConfig.operation);
      var target = extensionConfig.target;
      scout.assertParameter('target', target);
      var targetObject;
      if (target.root) {
        targetObject = parentModel;
      } else {
        targetObject = scout.objects.findChildObjectByKey(parentModel, 'id', target.id);
      }
      if (!targetObject) {
        throw new Error('Extensions ' + extension.id + ':Target not found: {id: ' + target.id + '}');
      }
      if (extensionConfig.operation === 'appendTo') {
        $.extend(targetObject, extensionConfig.extension);
      } else if (extensionConfig.operation === 'insert') {
        targetObject[target.property] = targetObject[target.property] || [];
        var targetArray = targetObject[target.property];
        var insertAt = targetArray.length;
        if (target.before) {
          insertAt = scout.arrays.findIndex(targetArray,function(element) {
            return element.id === target.before;
          });
          if (insertAt === -1) {
            insertAt = targetArray.length;
          }
        }
        if ($.isNumeric(target.index)) {
          insertAt = target.index;
        }
        scout.arrays.insert(targetArray, extensionConfig.extension, insertAt);
      }
    }.bind(this));
    return parentModel;
  }

};
