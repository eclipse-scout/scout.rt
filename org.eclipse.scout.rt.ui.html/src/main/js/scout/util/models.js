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

  get: function(modelId, parent) {
    scout.assertParameter('parent', parent);
    var model = this.modelMap[modelId];
    if (!model) {
      throw new Error('No JSON model defined for modelId=\'' + modelId + '\'. ' +
          'Check if model .json is listed in your *-module.json ' +
          'and if modelId matches the filename of the model .json file');
    }
    model.parent = parent;
    return model;
  }

};
