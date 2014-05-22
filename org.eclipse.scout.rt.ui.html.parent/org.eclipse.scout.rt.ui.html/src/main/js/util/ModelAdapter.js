scout.ModelAdapter = function(model, session) {
  this.model = model;
  this.session = session;
  this.$container;

  //check for undefined is necessary for inheritance
  // TODO AWE: (inheritance, ask C.GU) kann es den fall hier überhaupt noch geben, bzw. den else fall?
  if (session && model) {
    this.session.widgetMap[model.id] = this;
  }
};

// TODO AWE/CGU: evtl. in render() re-namen
scout.ModelAdapter.prototype.attach = function($parent) {
  if (!this.$container) {
    this._render($parent);
    this._applyModel();
  } else {
    this.$container.appendTo($parent);
  }
};

/**
 * The _render method creates the UI through DOM manipulation. At this point we should not apply model
 * properties on the UI, since sub-classes may need to contribute to the DOM first.
 */
scout.ModelAdapter.prototype._render = function() {
  // NOP
};

/**
 * Applies model properties on the DOM UI created by the _render() method before.
 */
scout.ModelAdapter.prototype._applyModel = function() {
  // NOP
};

scout.ModelAdapter.prototype.detach = function() {
  // TODO AWE: (ask C.GU) wird hier bewusst detach anstatt remove verwendet? warum?
  // siehe: http://api.jquery.com/detach/
  this.$container.detach();
};

scout.ModelAdapter.prototype.dispose = function() {
  if (this.$container) {
    this.$container.remove();
    this.$container = null;
  }
  this.session.widgetMap[this.model.id] = null;
};

