scout.Form = function(session, model) {
  this.base(session, model);
};
scout.Form.inheritsFrom(scout.ModelAdapter);

scout.Form.prototype._render = function($parent) {
  this.$container = $parent.appendDiv();

  var rootGroupBox = this.session.widgetMap[this.model.rootGroupBox];
  if (!rootGroupBox) {
    rootGroupBox = this.session.objectFactory.create(this.model.rootGroupBox);
  }
  rootGroupBox.attach(this.$container);
};

scout.Form.prototype.onModelCreate = function() {
};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type_ == 'formClosed') {
    this.dispose();
  }
  else {
    $.log("Model event not handled. Widget: Form. Event: " + event.type_ + ".");
  }
};
