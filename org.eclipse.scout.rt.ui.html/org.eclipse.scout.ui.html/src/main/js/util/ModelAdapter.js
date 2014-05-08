scout.ModelAdapter = function(session, model) {
  this.model = model;
  this.session = session;
  this.$container;

  //check for undefined is necessary for inheritance
  if (session && model) {
    this.session.widgetMap[model.id] = this;
  }
};

// TODO AWE/CGU: evtl. in render re-namen
scout.ModelAdapter.prototype.attach = function($parent) {
  if (!this.$container) {
    this._render($parent);
  } else {
    this.$container.appendTo($parent);
  }
};

scout.ModelAdapter.prototype.detach = function() {
  this.$container.detach();
};

scout.ModelAdapter.prototype.dispose = function() {
  if (this.$container) {
    this.$container.remove();
    this.$container = null;
  }
  this.session.widgetMap[this.model.id] = null;
};

