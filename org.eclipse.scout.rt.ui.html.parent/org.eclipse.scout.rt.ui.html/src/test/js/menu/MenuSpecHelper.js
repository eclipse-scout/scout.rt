var MenuSpecHelper = function(session) {
  this.session = session;
};

MenuSpecHelper.prototype.createModel = function(id, text, icon, types) {
  if (id === undefined) {
    id = createUniqueAdapterId();
  }

  return {
    "id": id,
    "text": text,
    "iconId": icon,
    visible: true,
    separator: false,
    menuTypes: types,
    "objectType": "Menu"
  };
};

MenuSpecHelper.prototype.createMenu = function(model) {
  var menu = new scout.Menu();
  menu.init(model, this.session);
  this.session.registerModelAdapter(menu); //FIXME CGU remove after moving to constructor
  return menu;
};
