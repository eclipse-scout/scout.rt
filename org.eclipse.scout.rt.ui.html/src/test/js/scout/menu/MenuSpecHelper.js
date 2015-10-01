var MenuSpecHelper = function(session) {
  this.session = session;
};

MenuSpecHelper.prototype.createModel = function(text, iconId, menuTypes) {
  var model = createSimpleModel('Menu', this.session);
  $.extend(model, {
    text: text,
    iconId: iconId,
    menuTypes: menuTypes,
    visible: true
  });
  return model;
};

MenuSpecHelper.prototype.createMenu = function(model) {
  model.objectType = model.objectType || 'Menu';
  model.session = this.session;
  model.parent = this.session.desktop;
  return scout.create('Menu', model);
};
