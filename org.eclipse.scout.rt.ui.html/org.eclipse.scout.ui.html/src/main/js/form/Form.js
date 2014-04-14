scout.Form = function(session, $parent, model) {
  this.model = model;
  this.session = session;

  this.session.widgetMap[model.id] = this;

  // cru > cgu: sorry
  //this._$container = $parent.appendDiv();
  //this._$container.html(JSON.stringify(model));
};

scout.Form.prototype.hide = function() {
  if (this._$container) {
    this._$container.remove();
  }
};

scout.Form.prototype.dispose = function() {
  this.hide();
  this.session.widgetMap[this.model.id] = null;
  this._$container = null;
};

scout.Form.prototype.onModelCreate = function() {};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type_ == 'formClosed') {
    this._dispose();
  } else {
    $.log("Model event not handled. Widget: Form. Event: " + event.type_ + ".");
  }
};
