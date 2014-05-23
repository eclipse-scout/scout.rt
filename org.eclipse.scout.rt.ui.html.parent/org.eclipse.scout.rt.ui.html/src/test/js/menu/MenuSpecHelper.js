var MenuSpecHelper = function(session) {
  this.session = session;
};

MenuSpecHelper.prototype.createModel = function(id, text, icon, types) {
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
