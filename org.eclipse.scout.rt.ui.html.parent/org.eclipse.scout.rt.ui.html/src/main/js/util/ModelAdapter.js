scout.ModelAdapter = function() {
  this.session;
  this.parent;
  this.children = [];
};

scout.ModelAdapter.prototype.init = function(model, session) {
  // copy all properties from model to this adapter instance
  this._eachProperty(model, function(propertyName, value) {
    this[propertyName] = value;
  }.bind(this));
  this.session = session;
  this.session.registerModelAdapter(this);
};

// TODO AWE: analog AbstractJsonAdapter eine Liste von properties machen, für die automatisch
// ein Adapter angelegt wird (beim init und beim propertyChange).

// TODO AWE: underscore bei setter-func names entfernen

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
  if (this.isRendered()) {
    throw "Already rendered.";
  }
  this._render($parent);
  this._callSetters();
};

scout.ModelAdapter.prototype.isRendered = function() {
  return this.$container && this.$container.parent().length > 0; //FIXME CGU maybe better to remove every child? currently, parent is set to null, $container of children still set
};

/**
 * This method creates the UI through DOM manipulation. At this point we should not apply model
 * properties on the UI, since sub-classes may need to contribute to the DOM first. You must not
 * apply model values to the UI here, since this is done in the _callSetters method later.
 * The default impl. does nothing.
 */
scout.ModelAdapter.prototype._render = function() {
  // NOP
};

/**
 * This method calls the UI setter methods after the _render method has been executed.
 * Here values of the model are applied to the DOM / UI. The default impl. does nothing.
 */
scout.ModelAdapter.prototype._callSetters = function() {
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
  this.children.push(childAdapter); // FIXME CGU when to remove child?
};

scout.ModelAdapter.prototype.updateModelAdapters = function(adapters, model, parent) {
  var adapter = this.session.getOrCreateModelAdapter(model, parent);
  if (adapters.indexOf(adapter) < 0) {
    adapters.push(adapter);
  }
  return adapter;
};

scout.ModelAdapter.prototype.updateModelAdapterAndRender = function(model, parent) {
  var adapter = this.session.getOrCreateModelAdapter(model, parent);

  if (this.isRendered()) {
    if (this.adapter) {
      this.adapter.remove();
    }
    this.adapter.render(this.$container);
  }

  return adapter;
};

scout.ModelAdapter.prototype._eachProperty = function(model, func, ignore) {
  for (var propertyName in model) {
    if (ignore === undefined || ignore.indexOf(propertyName) == -1) {
      func(propertyName, model[propertyName]);
    }
  }
};

/**
 * Processes the JSON event from the server and sets dynamically properties on the adapter (-model)
 * and calls the right function to update the UI. For each property a corresponding function-name
 * must exist (property-name 'myValue', function-name 'setMyValue').
 *
 * This happes in two steps:
 * 1.) Apply properties on adapter
 * 2.) Call setter function to update UI
 *
 * You can always rely that these two steps are processed in that order, but you cannot rely that
 * individual properties are processed in a certain order.
 */
scout.ModelAdapter.prototype.onModelPropertyChange = function(event) {
  var ignore = ['id', 'type_'];
  // step 1 - apply properties on adapter
  this._eachProperty(event, function(propertyName, value) {
    this[propertyName] = value;
  }.bind(this), ignore);

  // step 2 - call setter methods to update UI
  this._eachProperty(event, function(propertyName, value) {
    var setterFuncName = '_set' + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    console.debug('call ' + setterFuncName + '(' + value + ')');
    this[setterFuncName](value);
  }.bind(this), ignore);
};  // TODO AWE: (form) jasmine-test this!

