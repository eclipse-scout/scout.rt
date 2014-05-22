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
 * This method applies all property changes from the JSON event on this.model and delegates to _onModelPropertyChange.
 */
scout.ModelAdapter.prototype.onModelPropertyChange = function(event) {
  for (var propertyName in event) {
    // exclude 'id' and 'type_' since they're part of the interface and not part of the model
    // we could move all properties into a data property to separate interface from model data.
    if (propertyName != 'id' && propertyName != 'type_') {
      this.model[propertyName] = event[propertyName];
    }
  }
  this._onModelPropertyChange(event);
}; // TODO AWE: (form) jasmine-test this!

/**
 * This method is called by the public onModelPropertyChange method after the model has been updated.
 * Subclasses should overwrite this method to update the UI when a property has changed. The default
 * impl. des nothing.
 */
scout.ModelAdapter.prototype._onModelPropertyChange = function() {
  // NOP
};

/**
 * This method creates the UI through DOM manipulation. At this point we should not apply model
 * properties on the UI, since sub-classes may need to contribute to the DOM first. The default
 * impl. des nothing.
 */
scout.ModelAdapter.prototype._render = function() {
  // NOP
};

/**
 * This method applies model properties on the DOM UI created by the _render() method before.
 * The default impl. des nothing.
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

