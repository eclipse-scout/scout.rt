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
  this.$container;
  // If set to true, remove won't remove the element immediately but after the animation has been finished
  // This expects a css animation which may be triggered by the class 'removed'
  // If browser does not support css animation, remove will be executed immediately
  this.animateRemoval;
};

scout.Widget.prototype.init = function(options) {
  this._init(options);
  this._initKeyStrokeContext(this.keyStrokeContext);
  this.initialized = true;
  this.trigger('initialized');
};

/**
 * @param options
 * - parent (required): The parent widget
 * - session (optional): If not specified the session of the parent is used
 */
scout.Widget.prototype._init = function(options) {
  options = options || {};
  if (!options.parent) {
    throw new Error('Parent expected: ' + this);
  }
  this.setParent(options.parent);

  this.session = options.session || this.parent.session;
  if (!this.session) {
    throw new Error('Session expected: ' + this);
  }
  this.animateRemoval = scout.nvl(options.animateRemoval, false);
};

scout.Widget.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  // NOP
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
  this._link();
  this.session.keyStrokeManager.installKeyStrokeContext(this.keyStrokeContext);
  if (this.parent) {
    this.parent.addChild(this);
  }
  this.rendering = false;
  this.rendered = true;
  this.attached = true;
  this._postRender();
};

// Currently only necessary for ModelAdapter
scout.Widget.prototype._renderInternal = function($parent) {
  this._$parent = $parent;
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
 * Here values of the model are applied to the DOM / UI. The default impl. does nothing.
 */
scout.Widget.prototype._renderProperties = function() {
  // NOP
};

/**
 * Method invoked once rendering completed and 'rendered' flag is set to 'true'.
 */
scout.Widget.prototype._postRender = function() {
  // NOP
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
  if (this.parent) {
    this.parent.removeChild(this);
  }
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

  // Remove open popups first, they are not animated
  this.session.desktop.removePopupsFor(this);

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
scout.Widget.prototype._link = function() {
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

scout.Widget.prototype.setParent = function(parent) {
  if (this.parent) {
    // Remove from old parent if getting relinked
    this.parent.removeChild(this);
  }
  this.parent = parent;
  if (this.parent) { //prevent trying to set child on undefined
    this.parent.addChild(this);
  }
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
 * you will append this.$container to this._$parent. The default implementation
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

scout.Widget.prototype.toString = function() {
  return 'Widget[rendered=' + this.rendered +
    (this.$container ? ' $container=' + scout.graphics.debugOutput(this.$container) : '') + ']';
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
