scout.Widget = function() {
  this.parent;
  this.session;
  this.children = [];
  this.rendered = false;
  this.destroyed = false;
  this.$container;
};

scout.Widget.prototype.init = function(session) {
  this.session = session;
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
  this.session.keyStrokeManager.installKeyStrokeContext(this.keyStrokeContext);
  this.rendered = true;
  this._postRender();
};

// Currently only necessary for ModelAdapter
scout.Widget.prototype._renderInternal = function($parent) {
  this._render($parent);
  this._renderProperties();
  this._renderModelClass();
  this._renderClassId();
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
  if ($target) {
    $target.toggleAttr('data-modelclass', !!this.modelClass, this.modelClass);
  }
};

scout.Widget.prototype._renderClassId = function($target) {
  $target = $target || this.$container;
  if ($target) {
    $target.toggleAttr('data-classid', !!this.classId, this.classId);
  }
};

/**
 * Method invoked once rendering completed and 'rendered' flag is set to 'true'.
 */
scout.Widget.prototype._postRender = function() {
  // NOP
};


scout.Widget.prototype.remove = function() {
  if (!this.rendered) {
    return;
  }
  $.log.trace('Removing widget: ' + this);

  // remove children in reverse order.
  this.children.slice().reverse().forEach(function(child) {
    child.remove();
  });
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.keyStrokeContext);
  this._remove();
  this.rendered = false;
  this._trigger('remove');
};

scout.Widget.prototype._trigger = function(type, event) {
  if (this.events) {
    this.events.trigger(type, event);
  }
};

scout.Widget.prototype._remove = function() {
  if (this.$container) {
    this.$container.remove();
    this.$container = null;
  }
};

scout.Widget.prototype.addChild = function(child) {
  $.log.trace('addChild(' + child + ') to ' + this);
  this.children.push(child);
};

scout.Widget.prototype.removeChild = function(child) {
  $.log.trace('removeChild(' + child + ') from ' + this);
  scout.arrays.remove(this.children, child);
};

//--- Layouting / HtmlComponent methods ---

scout.Widget.prototype.pack = function() {
  if (!this.rendered) {
    return;
  }
  if (!this.htmlComp) {
    throw new Error('Function expects a htmlComp property');
  }
  this.htmlComp.pack();
};

scout.Widget.prototype.invalidateLayout = function() {
  if (!this.rendered) {
    return;
  }
  if (!this.htmlComp) {
    throw new Error('Function expects a htmlComp property');
  }
  this.htmlComp.invalidateLayout();
};

scout.Widget.prototype.validateLayout = function() {
  if (!this.rendered) {
    return;
  }
  if (!this.htmlComp) {
    throw new Error('Function expects a htmlComp property');
  }
  this.htmlComp.validateLayout();
};

scout.Widget.prototype.revalidateLayout = function() {
  if (!this.rendered) {
    return;
  }
  if (!this.htmlComp) {
    throw new Error('Function expects a htmlComp property');
  }
  this.htmlComp.revalidateLayout();
};

scout.Widget.prototype.invalidateLayoutTree = function(invalidateParents) {
  if (!this.rendered) {
    return;
  }
  if (!this.htmlComp) {
    throw new Error('Function expects a htmlComp property');
  }
  this.htmlComp.invalidateLayoutTree(invalidateParents);
};

scout.Widget.prototype.validateLayoutTree = function() {
  if (!this.rendered) {
    return;
  }
  if (!this.htmlComp) {
    throw new Error('Function expects a htmlComp property');
  }
  this.htmlComp.validateLayoutTree();
};

scout.Widget.prototype.revalidateLayoutTree = function() {
  if (!this.rendered) {
    return;
  }
  if (!this.htmlComp) {
    throw new Error('Function expects a htmlComp property');
  }
  this.htmlComp.revalidateLayoutTree();
};

//--- Event handling methods ---

/**
 * Call this function in the constructor of your widget if you need event support.
 **/
scout.Widget.prototype._addEventSupport = function() {
  this.events = new scout.EventSupport();
};

/**
 * Call this function in the constructor of your widget if you need keystroke context support.
 **/
scout.Widget.prototype._addKeyStrokeContextSupport = function() {
  this.keyStrokeContext = this._createKeyStrokeContext();
  this.keyStrokeContext.$scopeTarget = function() {
    return this.$container;
  }.bind(this);
  this.keyStrokeContext.$bindTarget = function() {
    return this.$container;
  }.bind(this);
};

/**
 * Creates a new keystroke context.
 * This method is intended to be overwritten by subclasses to provide another keystroke context.
 */
scout.Widget.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

scout.Widget.prototype.trigger = function(type, event) {
  event = event || {};
  event.source = this;
  this.events.trigger(type, event);
};

scout.Widget.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.Widget.prototype.off = function(type, func) {
  this.events.off(type, func);
};

scout.Widget.prototype.toString = function() {
  return 'Widget[rendered=' +  this.rendered +
      (this.$container ? ' $container=' + scout.graphics.debugOutput(this.$container) : '') + ']';
};
