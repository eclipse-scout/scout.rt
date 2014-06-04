scout.ModelAdapter = function() {
  this.model;
  this.session;
  this.parent;
  this.children = [];
};

scout.ModelAdapter.prototype.init = function(model, session) {
  this.model = model;
  this.session = session;
  this.session.registerModelAdapter(this);
};

// TODO AWE/CGU: evtl. in render() re-namen
scout.ModelAdapter.prototype.attach = function($parent) {
  if (!this.isRendered()) {
    this.render($parent);
  } else {
    this.$container.appendTo($parent);
  }
};

scout.ModelAdapter.prototype.detach = function() {
  if (this.isRendered()) {
    this.$container.detach();
  }

  this.dispose();
};

scout.ModelAdapter.prototype.render = function($parent) {
  if(this.isRendered()) {
    throw "Already rendered.";
  }

  this._render($parent);
  this._applyModel();
};

scout.ModelAdapter.prototype.isRendered = function() {
  return this.$container && this.$container.parent().length > 0; //FIXME CGU maybe better to remove every child? currently, parent is set to null, $container of children still set
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

scout.ModelAdapter.prototype.remove = function() {
  if (this.isRendered()) {
    this.$container.remove();
    this.$container = null;
  }

  this.dispose();
};

scout.ModelAdapter.prototype.dispose = function() {
  // NOP
};

scout.ModelAdapter.prototype.destroy = function() {
  this.session.unregisterModelAdapter(this);
};

scout.ModelAdapter.prototype.addChild = function(childAdapter) {
  this.children.push(childAdapter); //FIXME CGU when to remove child?
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

