scout.Widget = function() {
  this.parent;
  this.children = [];
  this.rendered = false;
  this.destroyed = false;
  this.$container;
};

scout.Widget.prototype.render = function($parent) {
  $.log.trace('Rendering widget: ' + this);
  if (this.rendered) {
    throw new Error('Already rendered: ' + this);
  }
  if (this.destroyed) {
    throw new Error('Widget is destroyed: ' + this);
  }
  this._renderInternal($parent);
  this._installKeyStrokeAdapter();
  this.rendered = true;
};

// Currently only necessary for ModelAdapter
scout.Widget.prototype._renderInternal = function($parent) {
  this._render($parent);
  this._renderProperties();
  this._renderModelClass();
};

/**
 * This method creates the UI through DOM manipulation. At this point we should not apply model
 * properties on the UI, since sub-classes may need to contribute to the DOM first. You must not
 * apply model values to the UI here, since this is done in the _renderProperties method later.
 * The default impl. does nothing.
 */
scout.Widget.prototype._render = function($parent) {
  // NOP
};

/**
 * This method calls the UI setter methods after the _render method has been executed.
 * Here values of the model are applied to the DOM / UI. The default impl. does nothing.
 */
scout.Widget.prototype._renderProperties = function() {
  // NOP
};

scout.Widget.prototype._renderModelClass = function($target) {
  $target = $target || this.$container;
  if ($target && this.modelClass) {
    $target.attr('data-modelClass', this.modelClass);
  }
};

scout.Widget.prototype.remove = function() {
  $.log.trace('Removing widget: ' + this);
  if (!this.rendered) {
    return;
  }

  for (var i = 0; i < this.children.length; i++) {
    var child = this.children[i];
    child.remove();
  }
  this._remove();
  this._uninstallKeyStrokeAdapter();
  this.rendered = false;
};

scout.Widget.prototype._remove = function() {
  if (this.$container) {
    this.$container.remove();
    this.$container = null;
  }
};

/**
 * Call this function in the constructor of your widget if you need event support.
 **/
scout.Widget.prototype._addEventSupport = function() {
  this.events = new scout.EventSupport();
};

scout.Widget.prototype.addChild = function(child) {
  $.log.trace('addChild(' + child + ') to ' + this);
  this.children.push(child);
};

scout.Widget.prototype.removeChild = function(child) {
  $.log.trace('removeChild(' + child + ') from ' + this);
  scout.arrays.remove(this.children, child);
};

// --- Convenience delegation functions to this.events -------------------

scout.Widget.prototype.trigger = function(type, event) {
  this.events.trigger(type, event);
};

scout.Widget.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.Widget.prototype.off = function(type, func) {
  this.events.off(type, func);
};

// --- KeyStrokeAdapter functions ----------------------------------------

scout.Widget.prototype._createKeyStrokeAdapter = function() {
  // to be implemented by subclass
};

scout.Widget.prototype._installKeyStrokeAdapter = function() {
  if (this.keyStrokeAdapter && !scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.installAdapter(this.$container, this.keyStrokeAdapter);
  }
};

scout.Widget.prototype._uninstallKeyStrokeAdapter = function() {
  if (this.keyStrokeAdapter && scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.uninstallAdapter(this.keyStrokeAdapter);
  }
};
