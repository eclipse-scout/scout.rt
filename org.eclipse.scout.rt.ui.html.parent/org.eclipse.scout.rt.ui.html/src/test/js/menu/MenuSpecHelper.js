var MenuSpecHelper = function(session) {
  this.session = session;
};

MenuSpecHelper.prototype.createModel = function(text, icon, types) {
  var model = createSimpleModel('Menu');
  $.extend(model, {
    "text": text,
    "iconId": icon,
    visible: true,
    separator: false,
    menuTypes: types
  });
  return model;
};

MenuSpecHelper.prototype.createMenu = function(model) {
  var menu = new scout.Menu();
  menu.init(model, this.session);
  return menu;
};
