scout.model = {

  modelMap: {},

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
