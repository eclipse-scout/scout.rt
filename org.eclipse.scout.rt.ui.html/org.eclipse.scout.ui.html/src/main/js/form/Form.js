Scout.Form = function(scout, $parent, model) {
  this.model = model;
  this.scout = scout;

  this.scout.widgetMap[model.id] = this;

  this._$container = $parent.appendDiv();
  this._$container.html(JSON.stringify(model));
};

Scout.Form.prototype.hide = function() {
  if (this._$container) {
    this._$container.remove();
  }
};

Scout.Form.prototype.dispose = function() {
  this.hide();
  this.scout.widgetMap[this.model.id] = null;
  this._$container = null;
};

Scout.Form.prototype.onModelCreate = function() {};

Scout.Form.prototype.onModelAction = function(event) {
  if (event.type_ == 'formClosed') {
    this._dispose();
  } else {
    log("Model event not handled. Widget: Form. Event: " + event.type_ + ".");
  }
};
