var FormFieldSpecHelper = function(session) {
  this.session = session;
};

FormFieldSpecHelper.prototype.createModel = function(id) {
  if (id === undefined) {
    id = createUniqueAdapterId();
  }

  var model =  {
    "id": id,
    "enabled": true,
    "visible": true
  };

  return model;
};

FormFieldSpecHelper.prototype.createGroupBoxModel = function(id) {
  if (id === undefined) {
    id = createUniqueAdapterId();
  }

  var model =  this.createModel(id);

  return model;
};
