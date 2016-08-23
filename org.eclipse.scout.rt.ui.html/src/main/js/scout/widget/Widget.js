/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Widget = function() {
  this.session;

  /**
   * The owner is responsible that its children are destroyed when the owner is being destroyed.
   */
  this.owner;
  this.parent;
  this.children = [];
  this.initialized = false;

  /**
   * The 'rendering' flag is set the true while the _inital_ rendering is performed.
   * It is used to to something different in a _render* method when the method is
   * called for the first time.
   */
  this.rendering = false;

  /**
   * The 'rendered' flag is set the true when initial rendering of the widget is completed.
   */
  this.rendered = false;
  this.attached = false;
  this.destroyed = false;

  this.enabled = true;
  this.visible = true;

  this.$container;
  // If set to true, remove won't remove the element immediately but after the animation has been finished
  // This expects a css animation which may be triggered by the class 'removed'
  // If browser does not support css animation, remove will be executed immediately
  this.animateRemoval;

  // FIXME [6.1] CGU, AWE durch propertyConfig ersetzen oder renamen auf widgetProperties
  this._adapterProperties = [];
  this._cloneProperties = ['parent', 'session']; // FIXME [awe, cgu] discuss: when not cloned automatically we need to pass 'parent' in clone method
  this._preserveOnPropertyChangeProperties = []; // FIXME [awe, cgu] 6.1 - migrieren zu propertyConfig und
  // dafür sorgen dass die config nur noch pro Klasse und nicht pro Instanz gemacht wird (memory)


  // FIXME [awe, cgu] 6.1 discuss: wenn alle widgets events und keyStrokeContext haben sollen braucht es die add methoden nicht mehr
  this._addKeyStrokeContextSupport();
  this._addEventSupport();

  this._parentDestroyHandler = this._onParentDestroy.bind(this);
  this._postRenderActions = [];
  this._addCloneProperties(['visible', 'enabled']);
};

scout.Widget.prototype.init = function(model) {
  this._init(model);
  this._initKeyStrokeContext(this.keyStrokeContext);
  this.initialized = true;
  this.trigger('initialized');
};

/**
 * @param options
 * - parent (required): The parent widget
 * - session (optional): If not specified the session of the parent is used
 */
scout.Widget.prototype._init = function(model) {
  model = model || {};
  if (!model.parent) {
    throw new Error('Parent expected: ' + this);
  }
  this.setOwner(model.owner || model.parent);
  this.setParent(model.parent);

  this.session = model.session || this.parent.session;
  if (!this.session) {
    throw new Error('Session expected: ' + this);
  }
  this.animateRemoval = scout.nvl(model.animateRemoval, false);

  this._eachProperty(model, function(propertyName, value, isAdapterProperty) {
    if (isAdapterProperty) {
      value = this._prepareWidgetProperty(propertyName, value);
    }
    this[propertyName] = value;
  }.bind(this));
};

scout.Widget.prototype.createFromProperty = function(propertyName, value) {
  // FIXME [6.1] awe Was ist das für ein Fall? Manchmal existiert das Widget schon (Menu 133 BusinessForm MainBox)
  if (value instanceof scout.Widget) {
    return value;
  }
  value.parent = this;
  return scout.create(value);
};

scout.Widget.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  // NOP
};

scout.Widget.prototype.destroy = function() {
  if (this.destroyed) {
    // Already destroyed, do nothing
    return;
  }

  if (this.animateRemoval && this.rendered) {
    this.on('remove', function() {
      this.destroy();
    }.bind(this));
    this.remove();
    return;
  }

  // Destroy children in reverse order
  this.children.slice().reverse().forEach(function(child) {
    this._destroyChild(child);
  }, this);

  this.remove();

  // Disconnect from owner and parent
  this.owner.removeChild(this);
  this.owner = null;
  this.parent.removeChild(this);
  this.parent.off('destroy', this._parentDestroyHandler);
  this.parent = null;

  this.destroyed = true;

  // Inform listeners
  this.trigger('destroy');
};

scout.Widget.prototype._destroyChild = function(child) {
  if (child.owner !== this) {
    return;
  }
  child.destroy();
};

scout.Widget.prototype.render = function($parent) {
  $.log.trace('Rendering widget: ' + this);
  if (!this.initialized) {
    throw new Error('Not initialized: ' + this);
  }
  if (this.rendered) {
    throw new Error('Already rendered: ' + this);
  }
  if (this.destroyed) {
    throw new Error('Widget is destroyed: ' + this);
  }
  this.rendering = true;
  this._renderInternal($parent);
  this._linkWithDOM();
  this.session.keyStrokeManager.installKeyStrokeContext(this.keyStrokeContext);
  this.rendering = false;
  this.rendered = true;
  this.attached = true;
  this._postRender();
};

// Currently only necessary for ModelAdapter
scout.Widget.prototype._renderInternal = function($parent) {
  this.$parent = $parent;
  this._render($parent);
  this._renderProperties();
  scout.inspector.applyInfo(this);
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
 * Here values of the model are applied to the DOM / UI.
 */
scout.Widget.prototype._renderProperties = function() {
  this._renderEnabled();
  this._renderVisible();
};

/**
 * Method invoked once rendering completed and 'rendered' flag is set to 'true'.<p>
 * By default executes every action of this._postRenderActions
 */
scout.Widget.prototype._postRender = function() {
  this._postRenderActions.forEach(function(action) {
    action();
  });
  this._postRenderActions = [];
};

scout.Widget.prototype.remove = function() {
  if (!this.rendered || this._isRemovalPending()) {
    return;
  }
  if (this.animateRemoval) {
    this._removeAnimated();
  } else {
    this._removeInternal();
  }
};

/**
 * Returns true if the removal of this or an ancestor widget is pending. Checking the ancestor is omitted if the parent is being removed.
 * This may be used to prevent a removal if an ancestor will be removed (e.g by an animation)
 */
scout.Widget.prototype._isRemovalPending = function() {
  if (this.removalPending) {
    return true;
  }
  var parent = this.parent;
  if (!parent || parent.removing) {
    // If parent is being removed, no need to check the ancestors because removing is already in progress
    return false;
  }
  while (parent) {
    if (parent.removalPending) {
      return true;
    }
    parent = parent.parent;
  }
  return false;
};

scout.Widget.prototype._removeInternal = function() {
  if (!this.rendered) {
    return;
  }

  $.log.trace('Removing widget: ' + this);
  this.removing = true;

  // remove children in reverse order.
  this.children.slice().reverse().forEach(function(child) {
    child.remove();
  });
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.keyStrokeContext);
  this._cleanup();
  this._remove();
  this.$parent = null;
  this.rendered = false;
  this.attached = false;
  this.removing = false;
  this.trigger('remove');
};

/**
 * Adds class 'removed' to container which can be used to trigger the animation.
 * After the animation is executed, the element gets removed using this._removeInternal.
 */
scout.Widget.prototype._removeAnimated = function() {
  if (!scout.device.supportsCssAnimation() || !this.$container) {
    // Cannot remove animated, remove regularly
    this._removeInternal();
    return;
  }

  // Destroy open popups first, they are not animated
  this.session.desktop.destroyPopupsFor(this);

  this.removalPending = true;
  // Don't execute immediately to make sure nothing interferes with the animation (e.g. layouting) which could make it laggy
  setTimeout(function() {
    this.$container.addClass('removed');
    this.$container.oneAnimationEnd(function() {
      this.removalPending = false;
      this._removeInternal();
    }.bind(this));
  }.bind(this));
};

/**
 * Links $container with the widget.
 */
scout.Widget.prototype._linkWithDOM = function() {
  if (this.$container) {
    this.$container.data('widget', this);
  }
};

/**
 * Called right before _remove is called.
 * Default calls LayoutValidator.cleanupInvalidComponents to make sure that child components are removed from the invalid components list.
 */
scout.Widget.prototype._cleanup = function() {
  if (this.$container) {
    this.session.layoutValidator.cleanupInvalidComponents(this.$container);
  }
};

scout.Widget.prototype._remove = function() {
  if (this.$container) {
    this.$container.remove();
    this.$container = null;
  }
};

scout.Widget.prototype.setOwner = function(owner) {
  scout.objects.mandatoryParameter('owner', owner);
  if (owner === this.owner) {
    return;
  }

  if (this.owner) {
    // Remove from old owner
    this.owner.removeChild(this);
  }
  this.owner = owner;
  this.owner.addChild(this);
};

scout.Widget.prototype.setParent = function(parent) {
  scout.objects.mandatoryParameter('parent', parent);
  if (parent === this.parent) {
    return;
  }

  if (this.parent) {
    this.parent.off('destroy', this._parentDestroyHandler);

    if (this.parent !== this.owner) {
      // Remove from old parent if getting relinked
      // If the old parent is still the owner, don't remove it because owner stays responsible for destryoing it
      this.parent.removeChild(this);
    }
  }
  this.parent = parent;
  this.parent.addChild(this);
  this.parent.one('destroy', this._parentDestroyHandler);
};

scout.Widget.prototype.addChild = function(child) {
  $.log.trace('addChild(' + child + ') to ' + this);
  if (this.children.indexOf(child) === -1) {
    this.children.push(child);
  }
};

scout.Widget.prototype.removeChild = function(child) {
  $.log.trace('removeChild(' + child + ') from ' + this);
  scout.arrays.remove(this.children, child);
};

/**
 * @returns true if the given widget is the same as this or a descendant
 */
scout.Widget.prototype.isOrHas = function(widget) {
  if (widget === this) {
    return true;
  }
  return this.has(widget);
};

/**
 * @returns true if the given widget is a descendant
 */
scout.Widget.prototype.has = function(widget) {
  while (widget) {
    if (widget.parent === this) {
      return true;
    }
    widget = widget.parent;
  }

  return false;
};


scout.Widget.prototype.setEnabled = function(enabled) {
  this.setProperty('enabled', enabled);
};

scout.Widget.prototype._renderEnabled = function() {
  if (!this.$container) {
    return;
  }
  this.$container.setEnabled(this.enabled);
};

scout.Widget.prototype.setVisible = function(visible) {
  this.setProperty('visible', visible);
};

scout.Widget.prototype._renderVisible = function() {
  if (!this.$container) {
    return;
  }
  this.$container.setVisible(this.visible);
};

/**
 * Adds default loading support to the widget. The default loading support hides
 * the whole field $container when the field is in loading state. Override this
 * method if you want to hide something else for a special field.
 */
scout.Widget.prototype.addLoadingSupport = function() {
  this.loadingSupport = new scout.LoadingSupport({widget: this});
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

scout.Widget.prototype.trigger = function(type, event) {
  if (!this.events) {
    return;
  }

  event = event || {};
  event.source = this;
  this.events.trigger(type, event);
};

scout.Widget.prototype.one = function(type, func) {
  this.events.one(type, func);
};

scout.Widget.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.Widget.prototype.off = function(type, func) {
  this.events.off(type, func);
};

/**
 * @param $element (optional) element from which the entryPoint will be resolved. If not set this.$container is used.
 * @returns the entry-point for this Widget. If the widget is part of the main-window it returns this.session.$entryPoint,
 * for popup-window this function will return the body of the document in the popup window.
 */
scout.Widget.prototype.entryPoint = function($element) {
  $element = scout.nvl($element, this.$container);
  if (!$element.length) {
    throw new Error('Cannot resolve entryPoint, $element.length is 0 or undefined');
  }
  return $element.entryPoint();
};

/**
 * This method attaches the detached $container to the DOM.
 */
scout.Widget.prototype.attach = function() {
  if (this.attached || !this.rendered) {
    return;
  }
  this._attach();
  this._triggerChildrenAfterAttach(this);
};

scout.Widget.prototype._triggerChildrenAfterAttach = function(parent) {
  this.children.forEach(function(child) {
    child._afterAttach(parent);
    child._triggerChildrenAfterAttach(parent);
  });
};

scout.Widget.prototype._afterAttach = function(parent) {
  // NOP
};

/**
 * Override this method to do something when Widget is attached again. Typically
 * you will append this.$container to this.$parent. The default implementation
 * sets this.attached to true.
 *
 * @param the event.target property is used to decide if a Widget must attach
 *   its $container. When the parent of the Widget already attaches, the Widget
 *   itself must _not_ attach its own $container. That's why we should only
 *   attach when event.target is === this.
 */
scout.Widget.prototype._attach = function(event) {
  this.attached = true;
};

/**
 * This method calls detach() on all child-widgets. It is used to store some data
 * before a DOM element is detached and propagate the detach "event" to all child-
 * widgets, because when a DOM element is detached - child elements are not notified
 */
scout.Widget.prototype.detach = function() {
  if (this.rendering) {
    // Defer the execution of detach. If it was detached while rendering the attached flag would be wrong.
    this._postRenderActions.push(this.detach.bind(this));
  }
  if (!this.attached || !this.rendered || this._isRemovalPending()) {
    return;
  }

  this._triggerChildrenBeforeDetach(this);
  this._detach();
};

/**
 * Override this method to do something when Widget is detached. Typically you
 * will call this.$container.detach() here and use the DetachHelper to store
 * additional state (focus, scrollbars) for the detached element. The default
 * implementation sets this.attached to false.
 *
 * @param the event.target property is used to decide if a Widget must detach
 *   its $container. When the parent of the Widget already detaches, the Widget
 *   itself must _not_ detach its own $container. That's why we should only
 *   detach when event.target is === this.
 */
scout.Widget.prototype._detach = function() {
  this.attached = false;
};

scout.Widget.prototype._triggerChildrenBeforeDetach = function(parent) {
  this.children.forEach(function(child) {
    child._beforeDetach(parent);
    child._triggerChildrenBeforeDetach(parent);
  });
};

scout.Widget.prototype._beforeDetach = function(parent) {
  // NOP
};

/**
 * Call this function in the constructor of your widget if you need keystroke context support.
 **/
scout.Widget.prototype._addKeyStrokeContextSupport = function() {
  this.keyStrokeContext = this._createKeyStrokeContext();
  if (this.keyStrokeContext) {
    this.keyStrokeContext.$scopeTarget = function() {
      return this.$container;
    }.bind(this);
    this.keyStrokeContext.$bindTarget = function() {
      return this.$container;
    }.bind(this);
  }
};

/**
 * Creates a new keystroke context.
 * This method is intended to be overwritten by subclasses to provide another keystroke context.
 */
scout.Widget.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

scout.Widget.prototype.updateKeyStrokes = function(newKeyStrokes, oldKeyStrokes) {
  this.unregisterKeyStrokes(oldKeyStrokes);
  this.registerKeyStrokes(newKeyStrokes);
};

scout.Widget.prototype.registerKeyStrokes = function(keyStrokes) {
  keyStrokes = scout.arrays.ensure(keyStrokes);
  keyStrokes.forEach(function(keyStroke) {
    this.keyStrokeContext.registerKeyStroke(keyStroke);
  }, this);
};

scout.Widget.prototype.unregisterKeyStrokes = function(keyStrokes) {
  keyStrokes = scout.arrays.ensure(keyStrokes);
  keyStrokes.forEach(function(keyStroke) {
    this.keyStrokeContext.unregisterKeyStroke(keyStroke);
  }, this);
};

scout.Widget.prototype._fireBulkPropertyChange = function(oldProperties, newProperties) {
  var propertyChangeEvent = {
    newProperties: newProperties,
    oldProperties: oldProperties,
    changedProperties: []
  };
  // To allow a listener to react only to properties that have really changed their value, we
  // calculate the list of "changedProperties". This may be relevant, when the value on the model
  // changes from A to B and back to A, which emits a property change event when in fact, the
  // property has not really changed for the UI.
  for (var prop in newProperties) {
    if (newProperties[prop] !== oldProperties[prop]) {
      propertyChangeEvent.changedProperties.push(prop);
    }
  }
  this.trigger('propertyChange', propertyChangeEvent);
};

/**
 * Fires a property change for a single property.
 */
scout.Widget.prototype._firePropertyChange = function(propertyName, oldValue, newValue) {
  if (!propertyName) {
    return;
  }
  var oldProperties = {},
    newProperties = {};
  oldProperties[propertyName] = oldValue;
  newProperties[propertyName] = newValue;
  this._fireBulkPropertyChange(oldProperties, newProperties);
};

/**
 * Sets the value of the property 'propertyName' to 'newValue' and then fires a propertyChange event for that property.
 */
scout.Widget.prototype._setProperty = function(propertyName, newValue) {
  if (!propertyName) {
    return;
  }
  var oldValue = this[propertyName];
  this[propertyName] = newValue;
  this._firePropertyChange(propertyName, oldValue, newValue);
};

/**
 * Sets a new value for a specific property. If the new value is the same value as the old one, nothing is performed.
 * Otherwise the following phases are executed:
 * <p>
 * 1. Preparation: If the property is a widget property, several actions are performed in _prepareWidgetProperty().
 * 2. DOM removal: If the widget is rendered and there is a custom remove function (e.g. _removeXY where XY is the property name), it will be called. Otherwise the default remove function _removeProperty is called.
 * 3. Model update: If there is a custom sync function (e.g. _syncXY where XY is the property name), it will be called. Otherwise the default sync function _setProperty is called.
 * 4. DOM rendering: If the widget is rendered and there is a custom render function (e.g. _renderXY where XY is the property name), it will be called.
 */
scout.Widget.prototype.setProperty = function(propertyName, value) {
  if (scout.objects.equals(this[propertyName], value)) {
    return;
  }

  value = this._prepareProperty(propertyName, value);
  if (this.rendered) {
    this._callRemoveProperty(propertyName);
  }
  this._callSetProperty(propertyName, value);
  if (this.rendered) {
    this._callRenderProperty(propertyName);
  }
};

scout.Widget.prototype._prepareProperty = function(propertyName, value) {
  if (!this._isAdapterProperty(propertyName)) {
    return value;
  }
  return this._prepareWidgetProperty(propertyName, value);
};

scout.Widget.prototype._prepareWidgetProperty = function(propertyName, widgets) {
  // Create new child widget(s)
  widgets = this._ensureType(propertyName, widgets);

  var oldWidgets = this[propertyName];
  if (oldWidgets && Array.isArray(widgets)) {
    // if new value is an array, old value has to be one as well
    // copy to prevent modification of original
    oldWidgets = oldWidgets.slice();

    // only destroy those which are not in the new array
    scout.arrays.removeAll(oldWidgets, widgets);
  }

  // Destroy old child widget(s)
  if (!this._isPreserveOnPropertyChangeProperty(propertyName)) {
    this._destroyWidgets(oldWidgets);
  }

  // Link to new parent
  this.link(widgets);

  return widgets;
};

scout.Widget.prototype._callRemoveProperty = function(propertyName) {
  var removeFuncName = '_remove' + scout.strings.toUpperCaseFirstLetter(propertyName);
  if (this[removeFuncName]) {
    this[removeFuncName]();
  } else {
    this._removeProperty(propertyName);
  }
};

/**
 * Does nothing if the property is not a widget property.<p>
 * If it is a widget property, it removes the existing widgets. Render has to be implemented by the widget itself.
 */
scout.Widget.prototype._removeProperty = function(propertyName) {
  var widgets = this[propertyName];
  if (!widgets) {
    return;
  }
  if (!this._isAdapterProperty(propertyName)) {
    return;
  }
  if (this._isPreserveOnPropertyChangeProperty(propertyName)) {
    return;
  }

  // Remove existing widgets on property change. Render should be implemented by the widget itself
  widgets = scout.arrays.ensure(widgets);
  widgets.forEach(function(widget) {
    widget.remove();
  });
};

scout.Widget.prototype._callSetProperty = function(propertyName, value) {
  var syncFuncName = '_sync' + scout.strings.toUpperCaseFirstLetter(propertyName);
  if (this[syncFuncName]) {
    this[syncFuncName](value); // FIXME [6.1] CGU rename to _setFuncName
  } else {
    this._setProperty(propertyName, value);
  }
};

scout.Widget.prototype._callRenderProperty = function(propertyName) {
  var renderFuncName = '_render' + scout.strings.toUpperCaseFirstLetter(propertyName);
  if (!this[renderFuncName]) { // FIXME [6.1] cgu remove this error and remove every empty render function
    throw new Error('Render function ' + renderFuncName + ' does not exist in ' + this.toString());
  }
  this[renderFuncName]();
};

/**
 * @param widgets may be an object or array of objects
 */
scout.Widget.prototype._destroyWidgets = function(widgets) {
  if (!widgets) {
    return;
  }

  widgets = scout.arrays.ensure(widgets);
  widgets.forEach(function(widget, i) {
    this._destroyChild(widget);
  }, this);
};

/**
 * Sets this widget as parent of the given widget(s).
 *
 * @param widgets may be a widget or array of widgets
 */
scout.Widget.prototype.link = function(widgets) {
  if (!widgets) {
    return;
  }

  widgets = scout.arrays.ensure(widgets);
  widgets.forEach(function(child, i) {
    child.setParent(this);
  }, this);
};

scout.Widget.prototype._ensureType = function(propertyName, value) {
  if (value === null || value === undefined) {
    return null;
  }

  if (Array.isArray(value)) {
    var returnValues = [];
    value.forEach(function(elementValue, i) {
      returnValues[i] = this._ensureType(propertyName, elementValue);
    }, this);
    return returnValues;
  }
  // FIXME [6.1] cgu rename to createChild? remove propertyName
  return this.createFromProperty(propertyName, value);
};

/**
 * Method required for widgets which are supposed to be directly covered by a glasspane.<p>
 *
 * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box or file-chooser is shown with this widget as its 'displayParent'.<br>
 * If the widget is not rendered yet, a scout.DerredGlassPaneTarget is returned.<br>
 * In both cases the method _glassPaneTargets is called which may be overridden by the actual widget.
 */
scout.Widget.prototype.glassPaneTargets = function() {
  if (this.rendered) {
    return this._glassPaneTargets();
  }

  return scout.DeferredGlassPaneTarget.createFor(this, this._glassPaneTargets.bind(this));
};

scout.Widget.prototype._glassPaneTargets = function() {
  return [this.$container];
};

scout.Widget.prototype.toString = function() {
  return 'Widget[rendered=' + this.rendered +
    (this.$container ? ' $container=' + scout.graphics.debugOutput(this.$container) : '') + ']';
};

scout.Widget.prototype.resolveTextKeys = function(properties) {
  properties.forEach(function(property) {
    scout.texts.resolveTextProperty(this, property);
  }, this);
};

scout.Widget.prototype.resolveIconIds = function(properties) {
  properties.forEach(function(property) {
    this[property] = scout.icons.resolveIconId(this[property]);
  }, this);
};

// FIXME CGU [6.1] temporary, rename, extract to Composite.js?
scout.Widget.prototype._addAdapterProperties = function(properties) {
  this._addProperties('_adapterProperties', properties);
};

scout.Widget.prototype._isAdapterProperty = function(propertyName) {
  return this._adapterProperties.indexOf(propertyName) > -1;
};

scout.Widget.prototype._addCloneProperties = function(properties) {
  this._addProperties('_cloneProperties', properties);
};

scout.Widget.prototype._isCloneProperty = function(propertyName) {
  return this._cloneProperties.indexOf(propertyName) > -1;
};

scout.Widget.prototype._addPreserveOnPropertyChangeProperties = function(properties) {
  this._addProperties('_preserveOnPropertyChangeProperties', properties);
};

scout.Widget.prototype._isPreserveOnPropertyChangeProperty = function(propertyName) {
  return this._preserveOnPropertyChangeProperties.indexOf(propertyName) > -1;
};

scout.Widget.prototype._addProperties = function(propertyName, properties) {
  if (Array.isArray(properties)) {
    this[propertyName] = this[propertyName].concat(properties);
  } else {
    this[propertyName].push(properties);
  }
};

scout.Widget.prototype._eachProperty = function(model, func) {
  var propertyName, value, i;

  // Loop through primitive properties
  for (propertyName in model) {
    if (this._adapterProperties.indexOf(propertyName) > -1) {
      continue; // will be handled below
    }
    value = model[propertyName];
    func(propertyName, value);
  }

  //Loop through adapter properties (any order will do).
  for (i = 0; i < this._adapterProperties.length; i++) {
    propertyName = this._adapterProperties[i];
    value = model[propertyName];
    if (value === undefined) {
      continue;
    }

    func(propertyName, value, true);
  }
};

scout.Widget.prototype._removeAdapterProperties = function(properties) {
  if (Array.isArray(properties)) {
    scout.arrays.removeAll(this._adapterProperties, properties);
  } else {
    scout.arrays.remove(this._adapterProperties, properties);
  }
};

// FIXME CGU [6.1] temporary, remove after model adapter separation
scout.Widget.prototype._send = function(type, data) {
  data = $.extend({}, data); // create a copy, so we don't change the original data unintentionally
  data.sendToServer = true;
  this.trigger(type, data);
};

scout.Widget.prototype.cloneAndMirror = function(model) {
  var clone = this.clone(model);
  clone.mirror();
  return clone;
};

/**
 * @returns the original widget from which this one was cloned. If it is not a clone, itself is returned.
 */
scout.Widget.prototype.original = function() {
  var original = this;
  while (original.cloneOf) {
    original = original.cloneOf;
  }
  return original;
};

scout.Widget.prototype.clone = function(model) {
  var clone, cloneModel;
  model = model || {};

  cloneModel = scout.objects.extractProperties(this, model, this._cloneProperties);
  clone = scout.create(this.objectType, cloneModel);
  clone.cloneOf = this;

  return clone;
};

scout.Widget.prototype.mirror = function() {
  this._mirror(this.cloneOf);
  this.children.forEach(function(childClone) {
    if (childClone.cloneOf) {
      childClone.mirror(childClone.cloneOf);
    }
  });
};

scout.Widget.prototype._mirror = function(source) {
  if (this._mirrorListener) {
    return;
  }
  this._mirrorListener = {
    func: this._onMirrorEvent.bind(this)
  };
  source.events.addListener(this._mirrorListener);
  this.one('destroy', function() {
    this.unmirror(source);
  }.bind(this));
};

scout.Widget.prototype.unmirror = function() {
  this.children.forEach(function(childClone) {
    if (childClone.cloneOf) {
      childClone.unmirror(childClone.cloneOf);
    }
  });
  this._unmirror(this.cloneOf);
};

scout.Widget.prototype._unmirror = function(source) {
  if (!this._mirrorListener) {
    return;
  }
  source.events.removeListener(this._mirrorListener);
  this._mirrorListener = null;
};

scout.Widget.prototype._onMirrorEvent = function(event) {
  if (event.type === 'propertyChange') {
    this._onMirrorPropertyChange(event);
  }
};

scout.Widget.prototype._onMirrorPropertyChange = function(event) {
  event.changedProperties.forEach(function(property) {
    this.callSetter(property, event.newProperties[property]);
  }, this);
};

scout.Widget.prototype._onParentDestroy = function(event) {
  if (this.destroyed) {
    return;
  }
  // If the parent is destroyed but the widget not make sure it gets a new parent
  // This ensures the old one may be properly garbage collected
  this.setParent(this.owner);
};

scout.Widget.prototype.callSetter = function(propertyName, value) {
  var setterFuncName = 'set' + scout.strings.toUpperCaseFirstLetter(propertyName);
  if (this[setterFuncName]) {
    this[setterFuncName](value);
  } else {
    this.setProperty(propertyName, value);
  }
};

/**
 * Traverses the object-tree (children) of this widget and searches for a widget with the given ID.
 * Returns the widget with the requested ID or null if no widget has been found.
 * @param widgetId
 */
scout.Widget.prototype.getWidgetById = function(widgetId) {
  return getRecWidgetById(this, widgetId);

  function getRecWidgetById(widget, widgetId) {
    if (widget.id === widgetId) {
      return widget;
    }
    var i, child;
    if (widget.children && widget.children.length > 0) {
      for (i = 0; i < widget.children.length; i++) {
        child = widget.children[i];
        if (child.id === widgetId) {
          return child;
        } else {
          child = getRecWidgetById(child, widgetId);
          if (child) {
            return child;
          }
        }
      }
    } else {
      return null;
    }
  }
};

scout.Widget.prototype.requestFocus = function() {
  this.session.focusManager.requestFocus(this.$container);
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

scout.Widget.getWidgetFor = function($elem) {
  while ($elem && $elem.length > 0) {
    var widget = $elem.data('widget');
    if (widget) {
      return widget;
    }
    $elem = $elem.parent();
  }
  return null;
};
