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
scout.model = {

  modelMap: {},

  // FIXME [awe] 6.1 als zwischenlösung hier alle einzelnen .json files auflisten und laden
  // in der finalen lösungen haben wir einen builder analog defaultValues
  bootstrap: function() {
    var that = this;

    return $.ajax({
      url: 'res/model.json',
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8'
    }).done(that._onModelDone.bind(that))
      .fail(that._onModelFail.bind(that));
  },

  _onModelDone: function(data) {
    this.modelMap = data;
  },

  _onModelFail: function(jqXHR, textStatus, errorThrown) {
    throw new Error('Error while loading model: ' + errorThrown);
  },

  getModel: function(modelId, parent) {
    if (!parent) {
      throw new Error('missing argument parent');
    }
    var model = this.modelMap[modelId];
    model.parent = parent;
    return model;
  }

};
