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

  getModel: function(modelId) {
    return scout.models.get(modelId, 'model');
  },

  getExtension: function(extensionId) {
    return scout.models.get(extensionId, 'extension');
  },

  get: function(id, type) {
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
          insertAt = targetArray.findIndex(function(element) {
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
