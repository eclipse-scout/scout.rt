/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, DeferredGlassPaneTarget, Desktop, Device, Event, EventDelegator, EventSupport, filters, focusUtils, Form, graphics, icons, inspector, objects, scout, scrollbars, strings, texts, TreeVisitResult} from '../index';
import $ from 'jquery';

export default class Widget {
  constructor() {
    this.id = null;
    this.objectType = null;
    this.session = null;

    /**
     * The owner is responsible that its children are destroyed when the owner is being destroyed.
     */
    this.owner = null;
    /**
     * The parent is typically the same as the owner.
     * But the widget may be used by another widget (like a popup), in that case the parent will be changed to the popup but the owner stays the same.
     * This means the popup is now the temporary parent, when the popup is destroyed its widgets are not because the popup is not the owner.
     * Example: ViewMenuPopup uses the ViewButtons as menu items. These view buttons are owned by the desktop and must therefore not be destroyed
     * when the popup closes, otherwise they could not be reused the second time the popup opens.
     */
    this.parent = null;
    this.children = [];
    this.initialized = false;

    /**
     * Will be set on the clone after a widget has been cloned.
     * @type {Widget}
     */
    this.cloneOf = null;

    /**
     * The 'rendering' flag is set the true while the _initial_ rendering is performed.
     * It is used to to something different in a _render* method when the method is
     * called for the first time.
     */
    this.rendering = false;
    this.removing = false;
    this.removalPending = false;

    /**
     * The 'rendered' flag is set the true when initial rendering of the widget is completed.
     */
    this._rendered = false;
    this.attached = false;
    this.destroyed = false;
    this.destroying = false;

    this.enabled = true;
    /**
     * The computed enabled state. The difference to the 'enabled' property is that this member
     * also considers the enabled-states of the parent widgets.
     */
    this.enabledComputed = true;
    this.inheritAccessibility = true;
    this.disabledStyle = Widget.DisabledStyle.DEFAULT;
    this.visible = true;
    this.focused = false;
    this.loading = false;
    this.cssClass = null;
    this.scrollTop = null;
    this.scrollLeft = null;

    /** @type {$} */
    this.$parent = null;
    /** @type {$} */
    this.$container = null;

    /**
     * Widgets creating a HtmlComponent for the main $container should assign it to this variable.
     * This enables the execution of layout related operations like invalidateLayoutTree directly on the widget.
     * @type {HtmlComponent}
     */
    this.htmlComp = null;

    /**
     * If set to true, remove won't remove the element immediately but after the animation has been finished
     * This expects a css animation which may be triggered by the class 'animate-remove'
     * If browser does not support css animation, remove will be executed immediately
     */
    this.animateRemoval = false;
    this.animateRemovalClass = 'animate-remove';

    this._widgetProperties = [];
    this._cloneProperties = ['visible', 'enabled', 'inheritAccessibility', 'cssClass'];
    this.eventDelegators = [];
    this._preserveOnPropertyChangeProperties = [];
    this._postRenderActions = [];
    this._focusInListener = this._onFocusIn.bind(this);
    this._parentDestroyHandler = this._onParentDestroy.bind(this);
    this._parentRemovingWhileAnimatingHandler = this._onParentRemovingWhileAnimating.bind(this);
    this._scrollHandler = this._onScroll.bind(this);
    this.events = this._createEventSupport();
    this.events.registerSubTypePredicate('propertyChange', (event, propertyName) => {
      return event.propertyName === propertyName;
    });
    this.loadingSupport = this._createLoadingSupport();
    this.keyStrokeContext = this._createKeyStrokeContext();
    // Widgets using LogicalGridLayout may have a grid to calculate the grid data of the children
    this.logicalGrid = null;

    // focus tracking
    this.trackFocus = false;
    this._$lastFocusedElement = null;
    this._storedFocusedWidget = null;

    this._glassPaneContributions = [];
  }

  /**
   * Enum used to define different styles used when the field is disabled.
   */
  static DisabledStyle = {
    DEFAULT: 0,
    READ_ONLY: 1
  };

  /**
   * Initializes the widget instance. All properties of the model parameter (object) are set as properties on the widget instance.
   * Calls {@link Widget#_init} and triggers an <em>init</em> event when initialization has been completed.
   *
   * @param {object} model
   */
  init(model) {
    let staticModel = this._jsonModel();
    if (staticModel) {
      model = $.extend({}, staticModel, model);
    }
    model = model || {};
    model = this._prepareModel(model);
    this._init(model);
    this._initKeyStrokeContext();
    this.recomputeEnabled();
    this.initialized = true;
    this.trigger('init');
  }

  /**
   * Default implementation simply returns the unmodified model. A Subclass
   * may override this method to alter the JSON model before the widgets
   * are created out of the widgetProperties in the model.
   */
  _prepareModel(model) {
    return model;
  }

  /**
   * Initializes the widget instance. All properties of the model parameter (object) are set as properties on the widget instance.
   * Override this function to initialize widget specific properties in sub-classes.
   *
   * @param {object} model Properties:<ul>
   *   <li>parent (required): parent widget</li>
   *   <li>session (optional): If not specified, session of parent widget is used</li></ul>
   */
  _init(model) {
    if (!model.parent) {
      throw new Error('Parent expected: ' + this);
    }
    this.setOwner(model.owner || model.parent);
    this.setParent(model.parent);

    this.session = model.session || this.parent.session;
    if (!this.session) {
      throw new Error('Session expected: ' + this);
    }

    this._eachProperty(model, (propertyName, value, isWidgetProperty) => {
      if (value === undefined) {
        // Don't set the value if it is undefined, compared to null which is allowed explicitly ($.extend works in the same way)
        return;
      }
      if (isWidgetProperty) {
        value = this._prepareWidgetProperty(propertyName, value);
      }
      this._initProperty(propertyName, value);
    });

    this._setCssClass(this.cssClass);
    this._setLogicalGrid(this.logicalGrid);
    this._setEnabled(this.enabled);
  }

  /**
   * This function sets the property value. Override this function when you need special init behavior for certain properties.
   * For instance you could not simply set the property value, but extend an already existing value.
   */
  _initProperty(propertyName, value) {
    this[propertyName] = value;
  }

  /**
   * Default implementation simply returns undefined. A Subclass
   * may override this method to load or extend a JSON model with models.getModel or models.extend.
   */
  _jsonModel() {
  }

  /**
   * Creates the widgets using the given models, or returns the widgets if the given models already are widgets.
   * @returns {Widget[]|Widget} an array of created widgets if models was an array. Or the created widget if models is not an array.
   */
  _createChildren(models) {
    if (!models) {
      return null;
    }

    if (!Array.isArray(models)) {
      return this._createChild(models);
    }

    let widgets = [];
    models.forEach(function(model, i) {
      widgets[i] = this._createChild(model);
    }, this);
    return widgets;
  }

  /**
   * Calls {@link scout.create} for the given model, or if model is already a Widget simply returns the widget.
   *
   * @param model {Object|Widget}
   * @returns {AnyWidget}
   */
  _createChild(model) {
    if (model instanceof Widget) {
      return model;
    }
    if (typeof model === 'string') {
      // Special case: If only an ID is supplied, try to (locally) resolve the corresponding widget
      let existingWidget = this.widget(model);
      if (!existingWidget) {
        throw new Error('Referenced widget not found: ' + model);
      }
      return existingWidget;
    }
    model.parent = this;
    return scout.create(model);
  }

  _initKeyStrokeContext() {
    if (!this.keyStrokeContext) {
      return;
    }
    this.keyStrokeContext.$scopeTarget = () => this.$container;
    this.keyStrokeContext.$bindTarget = () => this.$container;
  }

  destroy() {
    if (this.destroyed) {
      // Already destroyed, do nothing
      return;
    }
    this.destroying = true;
    if (this._rendered && (this.animateRemoval || this._isRemovalPrevented())) {
      // Do not destroy yet if the removal happens animated
      // Also don't destroy if the removal is pending to keep the parent / child link until removal finishes
      this.one('remove', () => {
        this.destroy();
      });
      this.remove();
      return;
    }

    // Destroy children in reverse order
    this._destroyChildren(this.children.slice().reverse());
    this.remove();
    this._destroy();

    // Disconnect from owner and parent
    this.owner._removeChild(this);
    this.owner = null;
    this.parent._removeChild(this);
    this.parent.off('destroy', this._parentDestroyHandler);
    this.parent = null;

    this.destroying = false;
    this.destroyed = true;
    this.trigger('destroy');
  }

  /**
   * Override this function to do clean-up (like removing listeners) when the widget is destroyed.
   * The default impl. does nothing.
   */
  _destroy() {
    // NOP
  }

  /**
   * @param {Widget[]|Widget} widgets may be an object or array of objects
   */
  _destroyChildren(widgets) {
    if (!widgets) {
      return;
    }

    widgets = arrays.ensure(widgets);
    widgets.forEach(function(widget, i) {
      this._destroyChild(widget);
    }, this);
  }

  _destroyChild(child) {
    if (child.owner !== this) {
      return;
    }
    child.destroy();
  }

  /**
   * @param [$parent] The jQuery element which is used as {@link Widget.$parent} when rendering this widget.
   * It will be put onto the widget and is therefore accessible as this.$parent in the {@link _render} method.
   * If not specified, the {@link Widget.$container} of the parent is used.
   */
  render($parent) {
    $.log.isTraceEnabled() && $.log.trace('Rendering widget: ' + this);
    if (!this.initialized) {
      throw new Error('Not initialized: ' + this);
    }
    if (this._rendered) {
      throw new Error('Already rendered: ' + this);
    }
    if (this.destroyed) {
      throw new Error('Widget is destroyed: ' + this);
    }
    this.rendering = true;
    this.$parent = $parent || this.parent.$container;
    this._render();
    this._renderProperties();
    this._renderInspectorInfo();
    this._linkWithDOM();
    this.session.keyStrokeManager.installKeyStrokeContext(this.keyStrokeContext);
    this.rendering = false;
    this.rendered = true;
    this.attached = true;
    this.trigger('render');
    this.restoreFocus();
    this._postRender();
  }

  /**
   * Creates the UI by creating html elements and appending them to the DOM.
   * <p>
   * A typical widget creates exactly one container element and stores it to {@link Widget.$container}.
   * If it needs JS based layouting, it creates a {@link HtmlComponent} for that container and stores it to {@link Widget.htmlComp}.
   * <p>
   * The rendering of individual properties should be done in the corresponding render methods of the properties, called by {@link _renderProperties} instead of doing it here.
   * This has the advantage that the render methods can also be called on property changes, allowing individual widget parts to be dynamically re-rendered.
   * <p>
   * The default implementation does nothing.
   */
  _render() {
    // NOP
  }

  /**
   * Returns whether it is allowed to render something on the widget.
   * Rendering is only possible if the widget itself is rendered and not about to be removed.
   * <p>
   * While the removal is pending, no rendering must happen to get a smooth remove animation.
   * It also prevents errors on property changes because {@link remove} won't be executed as well.
   * Preventing removal but allowing rendering could result in already rendered exceptions.
   *
   * @return {boolean} true if the widget is rendered and not being removed by an animation
   *
   * @see isRemovalPending
   */
  get rendered() {
    return this._rendered && !this.isRemovalPending();
  }

  set rendered(rendered) {
    this._rendered = rendered;
  }

  /**
   * Calls the render methods for each property that needs to be rendered during the rendering process initiated by {@link render}.
   * Each widget has to override this method and call the render methods for its own properties, after doing the super call.
   * <p>
   * This method is called right after {@link _render} has been executed.
   */
  _renderProperties() {
    this._renderCssClass();
    this._renderEnabled();
    this._renderVisible();
    this._renderTrackFocus();
    this._renderFocused();
    this._renderLoading();
    this._renderScrollTop();
    this._renderScrollLeft();
  }

  /**
   * Method invoked once rendering completed and 'rendered' flag is set to 'true'.<p>
   * By default executes every action of this._postRenderActions
   */
  _postRender() {
    let actions = this._postRenderActions;
    this._postRenderActions = [];
    actions.forEach(action => {
      action();
    });
  }

  /**
   * Removes the widget and all its children from the DOM.
   * <p>
   * It traverses down the widget hierarchy and calls {@link _remove} for each widget from the bottom up (depth first search).
   * <p>
   * If the property {@link Widget.animateRemoval} is set to true, the widget won't be removed immediately.
   * Instead it waits for the remove animation to complete so it's content is still visible while the animation runs.
   * During that time, {@link isRemovalPending} returns true.
   */
  remove() {
    if (!this._rendered || this._isRemovalPrevented()) {
      return;
    }
    if (this.animateRemoval) {
      this._removeAnimated();
    } else {
      this._removeInternal();
    }
  }

  /**
   * Removes the element without starting the remove animation or waiting for the remove animation to complete.
   * If the remove animation is running it will stop immediately because the element is removed. There will no animationend event be triggered.
   *<p>
   * <b>Important</b>: You should only use this method if your widget uses remove animations (this.animateRemoval = true)
   * and you deliberately want to not execute or abort it. Otherwise you should use the regular {@link remove} method.
   */
  removeImmediately() {
    this._removeInternal();
  }

  /**
   * Will be called by {@link #remove()}. If true is returned, the widget won't be removed.<p>
   * By default it just delegates to {@link #isRemovalPending}. May be overridden to customize it.
   */
  _isRemovalPrevented() {
    return this.isRemovalPending();
  }

  /**
   * @deprecated use isRemovalPending instead. Will be removed with 23.0
   */
  _isRemovalPending() {
    return this.isRemovalPending();
  }

  /**
   * Returns true if the removal of this or an ancestor widget is pending. Checking the ancestor is omitted if the parent is being removed.
   * This may be used to prevent a removal if an ancestor will be removed (e.g by an animation)
   */
  isRemovalPending() {
    if (this.removalPending) {
      return true;
    }
    let parent = this.parent;
    if (!parent || parent.removing || parent.rendering) {
      // If parent is being removed or rendered, no need to check the ancestors because removing / rendering is already in progress
      return false;
    }
    while (parent) {
      if (parent.removalPending) {
        return true;
      }
      parent = parent.parent;
    }
    return false;
  }

  _removeInternal() {
    if (!this._rendered) {
      return;
    }

    $.log.isTraceEnabled() && $.log.trace('Removing widget: ' + this);
    this.removing = true;
    this.removalPending = false;
    this.trigger('removing');
    // transform last focused element into a scout widget
    if (this.$container) {
      this.$container.off('focusin', this._focusInListener);
    }
    if (this._$lastFocusedElement) {
      this._storedFocusedWidget = scout.widget(this._$lastFocusedElement);
      this._$lastFocusedElement = null;
    }
    // remove children in reverse order.
    this.children.slice().reverse()
      .forEach(function(child) {
        // Only remove the child if this widget is the current parent (if that is not the case this widget is the owner)
        if (child.parent === this) {
          child.remove();
        }
      }, this);

    if (!this._rendered) {
      // The widget may have been removed already by one of the above remove() calls (e.g. by a remove listener)
      // -> don't try to do it again, it might fail
      return;
    }
    this._cleanup();
    this._remove();
    this.$parent = null;
    this.rendered = false;
    this.attached = false;
    this.removing = false;
    this.trigger('remove');
  }

  /**
   * Adds class 'animate-remove' to container which can be used to trigger the animation.
   * After the animation is executed, the element gets removed using this._removeInternal.
   */
  _removeAnimated() {
    let animateRemovalWhileRemovingParent = this._animateRemovalWhileRemovingParent();
    if ((this.parent.removing && !animateRemovalWhileRemovingParent) || !Device.get().supportsCssAnimation() || !this.$container || this.$container.isDisplayNone()) {
      // Cannot remove animated, remove regularly
      this._removeInternal();
      return;
    }

    // Remove open popups first, they would be positioned wrongly during the animation
    // Normally they would be closed automatically by a user interaction (click),
    this.session.desktop.removePopupsFor(this);

    this.removalPending = true;
    // Don't execute immediately to make sure nothing interferes with the animation (e.g. layouting) which could make it laggy
    setTimeout(() => {
      // check if the container has been removed in the meantime
      if (!this._rendered) {
        return;
      }
      if (!this.animateRemovalClass) {
        throw new Error('Missing animate removal class. Cannot remove animated.');
      }
      if (!this.$container.isVisible() || !this.$container.isEveryParentVisible() || !this.$container.isAttached()) {
        // If element is not visible, animationEnd would never fire -> remove it immediately
        this._removeInternal();
        return;
      }
      this.$container.addClass(this.animateRemovalClass);
      this.$container.oneAnimationEnd(() => {
        this._removeInternal();
      });
    });

    // If the parent is being removed while the animation is running, the animationEnd event will never fire
    // -> Make sure remove is called nevertheless. Important: remove it before the parent is removed to maintain the regular remove order
    if (!animateRemovalWhileRemovingParent) {
      this.parent.one('removing', this._parentRemovingWhileAnimatingHandler);
    }
  }

  _animateRemovalWhileRemovingParent() {
    // By default, remove animation is prevented when parent is being removed
    return false;
  }

  _onParentRemovingWhileAnimating() {
    this._removeInternal();
  }

  _renderInspectorInfo() {
    if (!this.session.inspector) {
      return;
    }
    inspector.applyInfo(this);
  }

  /**
   * Links $container with the widget.
   */
  _linkWithDOM() {
    if (this.$container) {
      this.$container.data('widget', this);
    }
  }

  /**
   * Called right before _remove is called.
   * Default calls LayoutValidator.cleanupInvalidComponents to make sure that child components are removed from the invalid components list.
   * Also uninstalls key stroke context, loading support and scrollbars.
   */
  _cleanup() {
    this.parent.off('removing', this._parentRemovingWhileAnimatingHandler);
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.keyStrokeContext);
    if (this.loadingSupport) {
      this.loadingSupport.remove();
    }
    this._uninstallScrollbars();
    if (this.$container) {
      this.session.layoutValidator.cleanupInvalidComponents(this.$container);
    }
  }

  _remove() {
    if (this.$container) {
      this.$container.remove();
      this.$container = null;
    }
  }

  setOwner(owner) {
    scout.assertParameter('owner', owner);
    if (owner === this.owner) {
      return;
    }

    if (this.owner) {
      // Remove from old owner
      this.owner._removeChild(this);
    }
    this.owner = owner;
    this.owner._addChild(this);
  }

  setParent(parent) {
    scout.assertParameter('parent', parent);
    if (parent === this.parent) {
      return;
    }
    if (this.rendered && !parent.rendered) {
      $.log.isInfoEnabled() && $.log.info('rendered child ' + this + ' is added to not rendered parent ' + parent + '. Removing child.', new Error('origin'));
      this.remove();
    }

    if (this.parent) {
      // Don't link to new parent yet if removal is still pending.
      // After the animation the parent will remove its children.
      // If they are already linked to a new parent, removing the children is not possible anymore.
      // This may lead to an "Already rendered" exception if the new parent wants to render its children.
      if (this.parent.isRemovalPending()) {
        this.parent.one('remove', () => {
          this.setParent(parent);
        });
        return;
      }

      this.parent.off('destroy', this._parentDestroyHandler);
      this.parent.off('removing', this._parentRemovingWhileAnimatingHandler);

      if (this.parent !== this.owner) {
        // Remove from old parent if getting relinked
        // If the old parent is still the owner, don't remove it because owner stays responsible for destroying it
        this.parent._removeChild(this);
      }
    }
    let oldParent = this.parent;
    this.parent = parent;
    this.parent._addChild(this);
    this.trigger('hierarchyChange', {
      oldParent: oldParent,
      parent: parent
    });
    if (this.initialized) {
      this.recomputeEnabled(this.parent.enabledComputed);
    }
    this.parent.one('destroy', this._parentDestroyHandler);
  }

  _addChild(child) {
    $.log.isTraceEnabled() && $.log.trace('addChild(' + child + ') to ' + this);
    arrays.pushSet(this.children, child);
  }

  _removeChild(child) {
    $.log.isTraceEnabled() && $.log.trace('removeChild(' + child + ') from ' + this);
    arrays.remove(this.children, child);
  }

  /**
   * @returns {Widget[]} a list of all ancestors
   */
  ancestors() {
    let ancestors = [];
    let parent = this.parent;
    while (parent) {
      ancestors.push(parent);
      parent = parent.parent;
    }
    return ancestors;
  }

  /**
   * @returns {boolean} true if the given widget is the same as this or a descendant
   */
  isOrHas(widget) {
    if (widget === this) {
      return true;
    }
    return this.has(widget);
  }

  /**
   * @returns {boolean} true if the given widget is a descendant
   */
  has(widget) {
    while (widget) {
      if (widget.parent === this) {
        return true;
      }
      widget = widget.parent;
    }

    return false;
  }

  /**
   * @returns {Form} the form the widget belongs to (returns the first parent which is a {@link Form}.
   */
  getForm() {
    return Form.findForm(this);
  }

  /**
   * @returns {Form} the first form which is not an inner form of a wrapped form field
   */
  findNonWrappedForm() {
    return Form.findNonWrappedForm(this);
  }

  /**
   * @returns {Desktop} the desktop linked to the current session.
   * If desktop is still initializing it might not be available yet, in that case it searches the parent hierarchy for it.
   */
  findDesktop() {
    if (this.session.desktop) {
      return this.session.desktop;
    }
    return this.findParent(parent => {
      return parent instanceof Desktop;
    });
  }

  /**
   * Changes the enabled property of this form field to the given value.
   *
   * @param {boolean} enabled
   *          Required. The new enabled value
   * @param {boolean} [updateParents]
   *          (optional) If true, the enabled property of all parent form fields are
   *          updated to same value as well. Default is false.
   * @param {boolean} [updateChildren]
   *          (optional) If true the enabled property of all child form fields (recursive)
   *          are updated to same value as well. Default is false.
   */
  setEnabled(enabled, updateParents, updateChildren) {
    this.setProperty('enabled', enabled);

    if (enabled && updateParents && this.parent) {
      this.parent.setEnabled(true, true, false);
    }

    if (updateChildren) {
      this.visitChildren(field => {
        field.setEnabled(enabled);
      });
    }
  }

  _setEnabled(enabled) {
    this._setProperty('enabled', enabled);
    if (this.initialized) {
      this.recomputeEnabled();
    }
  }

  recomputeEnabled(parentEnabled) {
    if (parentEnabled === undefined) {
      parentEnabled = true;
      if (this.parent && this.parent.initialized && this.parent.enabledComputed !== undefined) {
        parentEnabled = this.parent.enabledComputed;
      }
    }

    let enabledComputed = this._computeEnabled(this.inheritAccessibility, parentEnabled);
    this._updateEnabledComputed(enabledComputed);
  }

  _updateEnabledComputed(enabledComputed, enabledComputedForChildren) {
    if (this.enabledComputed === enabledComputed && enabledComputedForChildren === undefined) {
      // no change for this instance. there is no need to propagate to children
      // exception: the enabledComputed for the children differs from the one for me. In this case the propagation is necessary.
      return;
    }

    this.setProperty('enabledComputed', enabledComputed);

    // Manually call _renderEnabled(), because _renderEnabledComputed() does not exist
    if (this.rendered) {
      this._renderEnabled();
    }

    let computedStateForChildren = scout.nvl(enabledComputedForChildren, enabledComputed);
    this._childrenForEnabledComputed().forEach(child => {
      if (child.inheritAccessibility) {
        child.recomputeEnabled(computedStateForChildren);
      }
    });
  }

  _childrenForEnabledComputed() {
    return this.children;
  }

  _computeEnabled(inheritAccessibility, parentEnabled) {
    return this.enabled && (inheritAccessibility ? parentEnabled : true);
  }

  _renderEnabled() {
    if (!this.$container) {
      return;
    }
    this.$container.setEnabled(this.enabledComputed);
    this._renderDisabledStyle();
  }

  setInheritAccessibility(inheritAccessibility) {
    this.setProperty('inheritAccessibility', inheritAccessibility);
  }

  _setInheritAccessibility(inheritAccessibility) {
    this._setProperty('inheritAccessibility', inheritAccessibility);
    if (this.initialized) {
      this.recomputeEnabled();
    }
  }

  setDisabledStyle(disabledStyle) {
    this.setProperty('disabledStyle', disabledStyle);

    this.children.forEach(child => {
      child.setDisabledStyle(disabledStyle);
    });
  }

  _renderDisabledStyle() {
    this._renderDisabledStyleInternal(this.$container);
  }

  /**
   * This function is used by subclasses to render the read-only class for a given $field.
   * Some fields like DateField have two input fields and thus cannot use the this.$field property.
   */
  _renderDisabledStyleInternal($element) {
    if (!$element) {
      return;
    }
    if (this.enabledComputed) {
      $element.removeClass('read-only');
    } else {
      $element.toggleClass('read-only', this.disabledStyle === Widget.DisabledStyle.READ_ONLY);
    }
  }

  /**
   * @param {boolean} visible true, to make the widget visible, false to hide it
   */
  setVisible(visible) {
    this.setProperty('visible', visible);
  }

  /**
   * @returns {boolean} whether the widget is visible or not. May depend on other conditions than the visible property only
   */
  isVisible() {
    return this.visible;
  }

  _renderVisible() {
    if (!this.$container) {
      return;
    }
    this.$container.setVisible(this.isVisible());
    this.invalidateParentLogicalGrid();
  }

  /**
   * @returns {boolean} true if every parent within the hierarchy is visible.
   */
  isEveryParentVisible() {
    let parent = this.parent;
    while (parent) {
      if (!parent.isVisible()) {
        return false;
      }
      parent = parent.parent;
    }

    return true;
  }

  /**
   * This function does not set the focus to the field. It toggles the 'focused' class on the field container if present.
   * Objects using widget as prototype must call this function onBlur and onFocus to ensure the class gets toggled.
   *
   *  Use Widget.focus to set the focus to the widget.
   */
  setFocused(focused) {
    this.setProperty('focused', focused);
  }

  _renderFocused() {
    if (this.$container) {
      this.$container.toggleClass('focused', this.focused);
    }
  }

  _setCssClass(cssClass) {
    if (this.rendered) {
      this._removeCssClass();
    }
    this._setProperty('cssClass', cssClass);
  }

  _removeCssClass() {
    if (!this.$container) {
      return;
    }
    this.$container.removeClass(this.cssClass);
  }

  _renderCssClass() {
    if (!this.$container) {
      return;
    }
    this.$container.addClass(this.cssClass);
    if (this.htmlComp) {
      // Replacing css classes may enlarge or shrink the widget (e.g. setting the font weight to bold makes the text bigger) -> invalidate layout
      this.invalidateLayoutTree();
    }
  }

  setCssClass(cssClass) {
    this.setProperty('cssClass', cssClass);
  }

  addCssClass(cssClass) {
    let cssClasses = this.cssClassAsArray();
    let cssClassesToAdd = Widget.cssClassAsArray(cssClass);
    cssClassesToAdd.forEach(newCssClass => {
      if (cssClasses.indexOf(newCssClass) >= 0) {
        return;
      }
      cssClasses.push(newCssClass);
    }, this);
    this.setProperty('cssClass', arrays.format(cssClasses, ' '));
  }

  removeCssClass(cssClass) {
    let cssClasses = this.cssClassAsArray();
    let cssClassesToRemove = Widget.cssClassAsArray(cssClass);
    if (arrays.removeAll(cssClasses, cssClassesToRemove)) {
      this.setProperty('cssClass', arrays.format(cssClasses, ' '));
    }
  }

  toggleCssClass(cssClass, condition) {
    if (condition) {
      this.addCssClass(cssClass);
    } else {
      this.removeCssClass(cssClass);
    }
  }

  cssClassAsArray() {
    return Widget.cssClassAsArray(this.cssClass);
  }

  /**
   * Creates nothing by default. If a widget needs loading support, override this method and return a loading support.
   * @returns {LoadingSupport}
   */
  _createLoadingSupport() {
    return null;
  }

  setLoading(loading) {
    this.setProperty('loading', loading);
  }

  isLoading() {
    return this.loading;
  }

  _renderLoading() {
    if (!this.loadingSupport) {
      return;
    }
    this.loadingSupport.renderLoading();
  }

  // --- Layouting / HtmlComponent methods ---

  pack() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.pack();
  }

  invalidateLayout() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.invalidateLayout();
  }

  validateLayout() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.validateLayout();
  }

  revalidateLayout() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.revalidateLayout();
  }

  /**
   * @param [invalidateParents] optional, default is true
   */
  invalidateLayoutTree(invalidateParents) {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.invalidateLayoutTree(invalidateParents);
  }

  validateLayoutTree() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.validateLayoutTree();
  }

  revalidateLayoutTree(invalidateParents) {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.revalidateLayoutTree(invalidateParents);
  }

  /**
   * The layout data contains hints for the layout of the parent container to layout this individual child widget inside the container.<br>
   * Note: this is not the same as the LayoutConfig. The LayoutConfig contains constraints for the layout itself and is therefore set on the parent container directly.
   * <p>
   * Example: The parent container uses a LogicalGridLayout to layout its children. Every child has a LogicalGridLayoutData to tell the layout how this specific child should be layouted.
   * The parent may have a LogicalGridLayoutConfig to specify constraints which affect either only the container or every child in the container.
   */
  setLayoutData(layoutData) {
    if (!this.rendered) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.layoutData = layoutData;
  }

  /**
   * If the widget uses a logical grid layout, the grid may be validated using this method.
   * <p>
   * If the grid is not dirty, nothing happens.
   */
  validateLogicalGrid() {
    if (this.logicalGrid) {
      this.logicalGrid.validate(this);
    }
  }

  /**
   * Marks the logical grid as dirty.<br>
   * Does nothing, if there is no logical grid.
   * @param {boolean} [invalidateLayout] true, to invalidate the layout afterwards, false if not. Default is true.
   */
  invalidateLogicalGrid(invalidateLayout) {
    if (!this.initialized) {
      return;
    }
    if (!this.logicalGrid) {
      return;
    }
    this.logicalGrid.setDirty(true);
    if (scout.nvl(invalidateLayout, true)) {
      this.invalidateLayoutTree();
    }
  }

  /**
   * Invalidates the logical grid of the parent widget. Typically done when the visibility of the widget changes.
   * @param {boolean} [invalidateLayout] true, to invalidate the layout of the parent of this.htmlComp, false if not. Default is true.
   */
  invalidateParentLogicalGrid(invalidateLayout) {
    this.parent.invalidateLogicalGrid(false);
    if (!this.rendered || !this.htmlComp) {
      return;
    }
    if (scout.nvl(invalidateLayout, true)) {
      let htmlCompParent = this.htmlComp.getParent();
      if (htmlCompParent) {
        htmlCompParent.invalidateLayoutTree();
      }
    }
  }

  revalidateLogicalGrid(invalidateLayout) {
    this.invalidateLogicalGrid(invalidateLayout);
    this.validateLogicalGrid();
  }

  setLogicalGrid(logicalGrid) {
    this.setProperty('logicalGrid', logicalGrid);
  }

  /**
   * @param logicalGrid an instance of {@link LogicalGrid} or a string representing the object type of a logical grid.
   */
  _setLogicalGrid(logicalGrid) {
    if (typeof logicalGrid === 'string') {
      logicalGrid = scout.create(logicalGrid);
    }
    this._setProperty('logicalGrid', logicalGrid);
    this.invalidateLogicalGrid();
  }

  // --- Event handling methods ---
  _createEventSupport() {
    return new EventSupport();
  }

  trigger(type, event) {
    event = event || {};
    event.source = this;
    this.events.trigger(type, event);
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   * The function will only be called once. After that it is automatically de-registered using {@link off}.
   *
   * @param {string} type One or more event names separated by space.
   * @param {function} handler Event handler executed when the event is triggered. An event object is passed to the function as first parameter
   */
  one(type, handler) {
    this.events.one(type, handler);
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   *
   * @param {string} type One or more event names separated by space.
   * @param {function} handler Event handler executed when the event is triggered. An event object is passed to the function as first parameter.
   **/
  on(type, handler) {
    return this.events.on(type, handler);
  }

  /**
   * De-registers the given event handler for the event specified by the type param.
   *
   * @param {string} type One or more event names separated by space.<br/>
   *      Important: the string must be equal to the one used for {@link on} or {@link one}. This also applies if a string containing multiple types separated by space was used.
   * @param {function} [handler] The exact same event handler that was used for registration using {@link on} or {@link one}.
   *      If no handler is specified, all handlers are de-registered for the given type.
   */
  off(type, handler) {
    this.events.off(type, handler);
  }

  addListener(listener) {
    this.events.addListener(listener);
  }

  removeListener(listener) {
    this.events.removeListener(listener);
  }

  /**
   * Adds an event handler using {@link one} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   * @returns {Promise}
   */
  when(type) {
    return this.events.when(type);
  }

  /**
   * @returns {$} the entry-point for this Widget or its parent. If the widget is part of the main-window it returns this.session.$entryPoint,
   * for popup-window this function will return the body of the document in the popup window.
   */
  entryPoint() {
    let $element = scout.nvl(this.$container, this.parent.$container);
    if (!$element || !$element.length) {
      throw new Error('Cannot resolve entryPoint, $element.length is 0 or undefined');
    }
    return $element.entryPoint();
  }

  window(domElement) {
    let $el = this.$container || this.$parent;
    return $el ? $el.window(domElement) : domElement ? null : $(null);
  }

  document(domElement) {
    let $el = this.$container || this.$parent;
    return $el ? $el.document(domElement) : domElement ? null : $(null);
  }

  /**
   * This method attaches the detached $container to the DOM.
   */
  attach() {
    if (this.attached || !this.rendered) {
      return;
    }
    this._attach();
    this._installFocusContext();
    this.restoreFocus();
    this.attached = true;
    this._postAttach();
    this._onAttach();
    this._triggerChildrenOnAttach(this);
  }

  /**
   * Override this method to do something when Widget is attached again. Typically
   * you will append this.$container to this.$parent.
   */
  _attach() {
    // NOP
  }

  /**
   * Override this method to do something after this widget is attached.
   * This function is not called on any child of the attached widget.
   */
  _postAttach() {
    // NOP
  }

  _triggerChildrenOnAttach(parent) {
    this.children.forEach(child => {
      child._onAttach();
      child._triggerChildrenOnAttach(parent);
    });
  }

  /**
   * Override this method to do something after this widget or any parent of it is attached.
   * This function is called whether or not the widget is rendered.
   */
  _onAttach() {
    if (this.rendered) {
      this._renderOnAttach();
    }
  }

  /**
   * Override this method to do something after this widget or any parent of it is attached.
   * This function is only called when this widget is rendered.
   */
  _renderOnAttach() {
    this._renderScrollTop();
    this._renderScrollLeft();
  }

  /**
   * This method calls detach() on all child-widgets. It is used to store some data
   * before a DOM element is detached and propagate the detach "event" to all child-
   * widgets, because when a DOM element is detached - child elements are not notified
   */
  detach() {
    if (this.rendering) {
      // Defer the execution of detach. If it was detached while rendering the attached flag would be wrong.
      this._postRenderActions.push(this.detach.bind(this));
    }
    if (!this.attached || !this.rendered) {
      return;
    }

    this._beforeDetach();
    this._onDetach();
    this._triggerChildrenOnDetach(this);
    this._detach();
    this.attached = false;
  }

  /**
   * This function is called before a widget gets detached. The function is only called on the detached widget and NOT on
   * any of its children.
   */
  _beforeDetach(parent) {
    if (!this.$container) {
      return;
    }

    let activeElement = this.$container.document(true).activeElement;
    let isFocused = this.$container.isOrHas(activeElement);
    let focusManager = this.session.focusManager;

    if (focusManager.isFocusContextInstalled(this.$container)) {
      this._uninstallFocusContext();
    } else if (isFocused) {
      // exclude the container or any of its child elements to gain focus
      focusManager.validateFocus(filters.outsideFilter(this.$container));
    }
  }

  _triggerChildrenOnDetach() {
    this.children.forEach(child => {
      child._onDetach();
      child._triggerChildrenOnDetach(parent);
    });
  }

  /**
   * This function is called before a widget or any of its parent getting detached.
   * This function is thought to be overridden.
   */
  _onDetach() {
    if (this.rendered) {
      this._renderOnDetach();
    }
  }

  _renderOnDetach() {
    // NOP
  }

  /**
   * Override this method to do something when Widget is detached. Typically you
   * will call this.$container.detach(). The default
   * implementation sets this.attached to false.
   */
  _detach() {
  }

  _uninstallFocusContext() {
    // NOP
  }

  _installFocusContext() {
    // NOP
  }

  /**
   * Does nothing by default. If a widget needs keystroke support override this method and return a keystroke context, e.g. the default KeyStrokeContext.
   * @returns {KeyStrokeContext}
   */
  _createKeyStrokeContext() {
    return null;
  }

  updateKeyStrokes(newKeyStrokes, oldKeyStrokes) {
    this.unregisterKeyStrokes(oldKeyStrokes);
    this.registerKeyStrokes(newKeyStrokes);
  }

  registerKeyStrokes(keyStrokes) {
    this.keyStrokeContext.registerKeyStrokes(keyStrokes);
  }

  unregisterKeyStrokes(keyStrokes) {
    this.keyStrokeContext.unregisterKeyStrokes(keyStrokes);
  }

  /**
   * Triggers a property change for a single property.
   */
  triggerPropertyChange(propertyName, oldValue, newValue) {
    scout.assertParameter('propertyName', propertyName);
    let event = new Event({
      propertyName: propertyName,
      oldValue: oldValue,
      newValue: newValue
    });
    this.trigger('propertyChange', event);
    return event;
  }

  /**
   * Sets the value of the property 'propertyName' to 'newValue' and then fires a propertyChange event for that property.
   * @return {boolean} true if the property was changed, false if not.
   */
  _setProperty(propertyName, newValue) {
    scout.assertParameter('propertyName', propertyName);
    let oldValue = this[propertyName];
    if (objects.equals(oldValue, newValue)) {
      return false;
    }
    this[propertyName] = newValue;
    let event = this.triggerPropertyChange(propertyName, oldValue, newValue);
    if (event.defaultPrevented) {
      // Revert to old value if property change should be prevented
      this[propertyName] = oldValue;
      return false; // not changed
    }
    return true;
  }

  /**
   * Sets a new value for a specific property. If the new value is the same value as the old one, nothing is performed.
   * Otherwise the following phases are executed:
   * <p>
   * 1. Preparation: If the property is a widget property, several actions are performed in _prepareWidgetProperty().
   * 2. DOM removal: If the property is a widget property and the widget is rendered, the changed widget(s) are removed unless the property should not be preserved (see _preserveOnPropertyChangeProperties).
   *    If there is a custom remove function (e.g. _removeXY where XY is the property name), it will be called instead of removing the widgets directly.
   * 3. Model update: If there is a custom set function (e.g. _setXY where XY is the property name), it will be called. Otherwise the default set function _setProperty is called.
   * 4. DOM rendering: If the widget is rendered and there is a custom render function (e.g. _renderXY where XY is the property name), it will be called. Otherwise nothing happens.
   * @return {boolean} true if the property was changed, false if not.
   */
  setProperty(propertyName, value) {
    if (objects.equals(this[propertyName], value)) {
      return false;
    }

    value = this._prepareProperty(propertyName, value);
    if (this.rendered) {
      this._callRemoveProperty(propertyName);
    }
    this._callSetProperty(propertyName, value);
    if (this.rendered) {
      this._callRenderProperty(propertyName);
    }
    return true;
  }

  _prepareProperty(propertyName, value) {
    if (!this.isWidgetProperty(propertyName)) {
      return value;
    }
    return this._prepareWidgetProperty(propertyName, value);
  }

  _prepareWidgetProperty(propertyName, widgets) {
    // Create new child widget(s)
    widgets = this._createChildren(widgets);

    let oldWidgets = this[propertyName];
    if (oldWidgets && Array.isArray(widgets)) {
      // If new value is an array, old value has to be one as well
      // Only destroy those which are not in the new array
      oldWidgets = arrays.diff(oldWidgets, widgets);
    }

    if (!this.isPreserveOnPropertyChangeProperty(propertyName)) {
      // Destroy old child widget(s)
      this._destroyChildren(oldWidgets);

      // Link to new parent
      this.link(widgets);
    }

    return widgets;
  }

  /**
   * Does nothing if the property is not a widget property.<p>
   * If it is a widget property, it removes the existing widgets. Render has to be implemented by the widget itself.
   */
  _callRemoveProperty(propertyName) {
    if (!this.isWidgetProperty(propertyName)) {
      return;
    }
    if (this.isPreserveOnPropertyChangeProperty(propertyName)) {
      return;
    }
    let widgets = this[propertyName];
    if (!widgets) {
      return;
    }
    let removeFuncName = '_remove' + strings.toUpperCaseFirstLetter(propertyName);
    if (this[removeFuncName]) {
      this[removeFuncName]();
    } else {
      this._internalRemoveWidgets(widgets);
    }
  }

  /**
   * Removes the given widgets
   */
  _internalRemoveWidgets(widgets) {
    widgets = arrays.ensure(widgets);
    widgets.forEach(widget => {
      widget.remove();
    });
  }

  _callSetProperty(propertyName, value) {
    let setFuncName = '_set' + strings.toUpperCaseFirstLetter(propertyName);
    if (this[setFuncName]) {
      this[setFuncName](value);
    } else {
      this._setProperty(propertyName, value);
    }
  }

  _callRenderProperty(propertyName) {
    let renderFuncName = '_render' + strings.toUpperCaseFirstLetter(propertyName);
    if (!this[renderFuncName]) {
      return;
    }
    this[renderFuncName]();
  }

  /**
   * Sets this widget as parent of the given widget(s).
   *
   * @param {Widget[]|Widget} widgets may be a widget or array of widgets
   */
  link(widgets) {
    if (!widgets) {
      return;
    }

    widgets = arrays.ensure(widgets);
    widgets.forEach(function(child, i) {
      child.setParent(this);
    }, this);
  }

  /**
   * Method required for widgets which are supposed to be directly covered by a glasspane.<p>
   *
   * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box or file-chooser is shown with this widget as its 'displayParent'.<br>
   * If the widget is not rendered yet, a scout.DeferredGlassPaneTarget is returned.<br>
   * In both cases the method _glassPaneTargets is called which may be overridden by the actual widget.
   * @param {Widget} element widget that requested a glass pane
   * @returns [$]|[DeferredGlassPaneTarget]
   */
  glassPaneTargets(element) {
    let resolveGlassPanes = element => {
      // contributions
      let targets = arrays.flatMap(this._glassPaneContributions, cont => {
        let $elements = cont(element);
        if ($elements) {
          return arrays.ensure($elements);
        }
        return [];
      });
      return targets.concat(this._glassPaneTargets(element));
    };
    if (this.rendered) {
      return resolveGlassPanes(element);
    }

    return DeferredGlassPaneTarget.createFor(this, resolveGlassPanes.bind(this, element));
  }

  /**
   *
   * @param {Widget} element widget that requested a glass pane
   * @returns [$]
   */
  _glassPaneTargets(element) {
    // since popups are rendered outside the DOM of the widget parent-child hierarchy, get glassPaneTargets of popups belonging to this widget separately.
    return [this.$container].concat(
      this.session.desktop.getPopupsFor(this)
        .filter(popup => !element.has(popup))
        .reduce((acc, popup) => acc.concat(popup.glassPaneTargets()), []));
  }

  addGlassPaneContribution(contribution) {
    this._glassPaneContributions.push(contribution);
    this.trigger('glassPaneContributionAdded', {
      contribution: contribution
    });
  }

  /**
   * @param [contribution] a function which returns glass pane targets (jQuery elements)
   */
  removeGlassPaneContribution(contribution) {
    arrays.remove(this._glassPaneContributions, contribution);
    this.trigger('glassPaneContributionRemoved', {
      contribution: contribution
    });
  }

  toString() {
    let attrs = '';
    attrs += 'id=' + this.id;
    attrs += ' objectType=' + this.objectType;
    attrs += ' rendered=' + this.rendered;
    if (this.$container) {
      attrs += ' $container=' + graphics.debugOutput(this.$container);
    }
    return 'Widget[' + attrs.trim() + ']';
  }

  /**
   * Returns the ancestors as string delimited by '\n'.
   * @param {number} [count] the number of ancestors to be processed. Default is -1 which means all.
   */
  ancestorsToString(count) {
    let str = '',
      ancestors = this.ancestors();

    count = scout.nvl(count, -1);
    ancestors.some((ancestor, i) => {
      if (count > -1 && i >= count) {
        return true;
      }
      if (i > 0 && i < ancestors.length - 1) {
        str += '\n';
      }
      str += ancestor.toString();
      return false;
    });
    return str;
  }

  resolveTextKeys(properties) {
    properties.forEach(function(property) {
      texts.resolveTextProperty(this, property);
    }, this);
  }

  resolveIconIds(properties) {
    properties.forEach(function(property) {
      icons.resolveIconProperty(this, property);
    }, this);
  }

  resolveConsts(configs) {
    configs.forEach(function(config) {
      objects.resolveConstProperty(this, config);
    }, this);
  }

  /**
   * A so called widget property is a property with a widget as value incl. automatic resolution of that widget.
   * This means the property not only accepts the actual widget, but also a widget model or a widget reference (id)
   * and then either creates a new widget based on the model or resolves the id and uses the referenced widget as value.
   * Furthermore it will take care of its lifecycle which means, the widget will automatically be removed and destroyed (as long as the parent is also the owner).
   * <p>
   * If only the resolve operations without the lifecycle actions should be performed, you need to add the property to the list _preserveOnPropertyChangeProperties as well.
   */
  _addWidgetProperties(properties) {
    this._addProperties('_widgetProperties', properties);
  }

  isWidgetProperty(propertyName) {
    return this._widgetProperties.indexOf(propertyName) > -1;
  }

  _addCloneProperties(properties) {
    this._addProperties('_cloneProperties', properties);
  }

  isCloneProperty(propertyName) {
    return this._cloneProperties.indexOf(propertyName) > -1;
  }

  /**
   * Properties in this list won't be affected by the automatic lifecycle actions performed for regular widget properties.
   * This means, the widget won't be removed, destroyed and also not linked, which means the parent stays the same.
   * But the resolve operations are still applied, as for regular widget properties.
   * <p>
   * The typical use case for such properties is referencing another widget without taking care of that widget.
   */
  _addPreserveOnPropertyChangeProperties(properties) {
    this._addProperties('_preserveOnPropertyChangeProperties', properties);
  }

  isPreserveOnPropertyChangeProperty(propertyName) {
    return this._preserveOnPropertyChangeProperties.indexOf(propertyName) > -1;
  }

  _addProperties(propertyName, properties) {
    properties = arrays.ensure(properties);
    properties.forEach(function(property) {
      if (this[propertyName].indexOf(property) > -1) {
        throw new Error(propertyName + ' already contains the property ' + property);
      }
      this[propertyName].push(property);
    }, this);
  }

  _eachProperty(model, func) {
    let propertyName, value, i;

    // Loop through primitive properties
    for (propertyName in model) {
      if (this._widgetProperties.indexOf(propertyName) > -1) {
        continue; // will be handled below
      }
      value = model[propertyName];
      func(propertyName, value);
    }

    // Loop through adapter properties (any order will do).
    for (i = 0; i < this._widgetProperties.length; i++) {
      propertyName = this._widgetProperties[i];
      value = model[propertyName];
      if (value === undefined) {
        continue;
      }

      func(propertyName, value, true);
    }
  }

  _removeWidgetProperties(properties) {
    if (Array.isArray(properties)) {
      arrays.removeAll(this._widgetProperties, properties);
    } else {
      arrays.remove(this._widgetProperties, properties);
    }
  }

  /**
   * Clones the widget and mirrors the events, see this.clone() and this.mirror() for details.
   */
  cloneAndMirror(model) {
    return this.clone(model, {
      delegateAllPropertiesToClone: true
    });
  }

  /**
   * @returns {AnyWidget} the original widget from which this one was cloned. If it is not a clone, itself is returned.
   */
  original() {
    let original = this;
    while (original.cloneOf) {
      original = original.cloneOf;
    }
    return original;
  }

  /**
   * Clones the widget and returns the clone. Only the properties defined in this._cloneProperties are copied to the clone.
   * The parameter model has to contain at least the property 'parent'.
   *
   * @param model The model used to create the clone is a combination of the clone properties and this model.
   * Therefore this model may be used to override the cloned properties or to add additional properties.
   * @param {object} [options] Options passed to the mirror function.
   * @param {[string]} [options.delegatePropertiesToClone] An array of all properties to be delegated from the original to the clone when changed on the original widget. Default is [].
   * @param {[string]} [options.delegatePropertiesToOriginal] An array of all properties to be delegated from the clone to the original when changed on the clone widget. Default is [].
   * @param {[string]} [options.excludePropertiesToOriginal] An array of all properties to be excluded from delegating from the clone to the original in any cases. Default is [].
   * @param {[string]} [options.delegateEventsToOriginal] An array of all events to be delegated from the clone to the original when fired on the clone widget. Default is [].
   * @param {boolean} [options.delegateAllPropertiesToClone] True to delegate all property changes from the original to the clone. Default is false.
   * @param {boolean} [options.delegateAllPropertiesToOriginal] True to delegate all property changes from the clone to the original. Default is false.
   */
  clone(model, options) {
    let clone, cloneModel;
    model = model || {};
    options = options || {};

    cloneModel = objects.extractProperties(this, model, this._cloneProperties);
    clone = scout.create(this.objectType, cloneModel);
    clone.cloneOf = this;
    this._mirror(clone, options);

    if (this.logicalGrid) {
      // Create a new logical grid to make sure it does not influence the original widget
      // This also creates the correct grid config for the specific widget
      clone.setLogicalGrid(this.logicalGrid.objectType);
    } else {
      // Remove the grid if the original does not have one either
      clone.setLogicalGrid(null);
    }

    return clone;
  }

  _deepCloneProperties(clone, properties, options) {
    if (!properties) {
      return clone;
    }
    properties = arrays.ensure(properties);
    properties.forEach(property => {
      let propertyValue = this[property],
        clonedProperty = null;
      if (propertyValue === undefined) {
        throw new Error('Property \'' + property + '\' is undefined. Deep copy not possible.');
      }
      if (this._widgetProperties.indexOf(property) > -1) {
        if (Array.isArray(propertyValue)) {
          clonedProperty = propertyValue.map(val => {
            return val.clone({
              parent: clone
            }, options);
          });
        } else {
          clonedProperty = propertyValue.clone({
            parent: clone
          }, options);
        }
      } else if (Array.isArray(propertyValue)) {
        clonedProperty = propertyValue.map(val => {
          return val;
        });
      } else {
        clonedProperty = propertyValue;
      }
      clone[property] = clonedProperty;
    });
  }

  /**
   * Delegates every property change event from the original widget to this cloned widget by calling the appropriate setter.
   * If no target is set it works only if this widget is a clone.
   * @param {object} [options]
   */
  mirror(options, target) {
    target = target || this.cloneOf;
    if (!target) {
      throw new Error('No target for mirroring.');
    }
    this._mirror(target, options);
  }

  _mirror(clone, options) {
    let eventDelegator = arrays.find(this.eventDelegators, eventDelegator => {
      return eventDelegator.clone === clone;
    });
    if (eventDelegator) {
      throw new Error('_mirror can only be called on not mirrored widgets. call unmirror first.');
    }
    options = options || {};
    eventDelegator = {
      clone: clone,
      originalToClone: EventDelegator.create(this, clone, {
        delegateProperties: options.delegatePropertiesToClone,
        delegateAllProperties: options.delegateAllPropertiesToClone
      }),
      cloneToOriginal: EventDelegator.create(clone, this, {
        delegateProperties: options.delegatePropertiesToOriginal,
        delegateAllProperties: options.delegateAllPropertiesToOriginal,
        excludeProperties: options.excludePropertiesToOriginal,
        delegateEvents: options.delegateEventsToOriginal
      })
    };
    this.eventDelegators.push(eventDelegator);
    clone.one('destroy', () => {
      this._unmirror(clone);
    });
  }

  unmirror(target) {
    target = target || this.cloneOf;
    if (!target) {
      throw new Error('No target for unmirroring.');
    }
    this._unmirror(target);
  }

  _unmirror(target) {
    let eventDelegatorIndex = arrays.findIndex(this.eventDelegators, eventDelegator => {
        return eventDelegator.clone === target;
      }),
      eventDelegator = eventDelegatorIndex > -1 ? this.eventDelegators.splice(eventDelegatorIndex, 1)[0] : null;
    if (!eventDelegator) {
      return;
    }
    if (eventDelegator.originalToClone) {
      eventDelegator.originalToClone.destroy();
    }
    if (eventDelegator.cloneToOriginal) {
      eventDelegator.cloneToOriginal.destroy();
    }
  }

  _onParentDestroy(event) {
    if (this.destroyed) {
      return;
    }
    // If the parent is destroyed but the widget not make sure it gets a new parent
    // This ensures the old one may be properly garbage collected
    this.setParent(this.owner);
  }

  callSetter(propertyName, value) {
    let setterFuncName = 'set' + strings.toUpperCaseFirstLetter(propertyName);
    if (this[setterFuncName]) {
      this[setterFuncName](value);
    } else {
      this.setProperty(propertyName, value);
    }
  }

  /**
   * Traverses the object-tree (children) of this widget and searches for a widget with the given ID.
   * Returns the widget with the requested ID or null if no widget has been found.
   *
   * @param {string} widgetId
   * @returns {AnyWidget} the found widget for the given id
   */
  widget(widgetId) {
    if (predicate(this)) {
      return this;
    }
    return this.findChild(predicate);

    function predicate(widget) {
      if (widget.id === widgetId) {
        return widget;
      }
    }
  }

  /**
   * Similar to widget(), but uses "breadth-first" strategy, i.e. it checks all children of the
   * same depth (level) before it advances to the next level. If multiple widgets with the same
   * ID exist, the one with the smallest distance to this widget is returned.
   *
   * Example:
   *
   *    Widget ['MyWidget']                     #1
   *    +- GroupBox ['LeftBox']                 #2
   *       +- StringField ['NameField']         #3
   *       +- StringField ['CityField']         #4
   *       +- GroupBox ['InnerBox']             #5
   *          +- GroupBox ['LeftBox']           #6
   *             +- DateField ['StartDate']     #7
   *          +- GroupBox ['RightBox']          #8
   *             +- DateField ['EndDate']       #9
   *    +- GroupBox ['RightBox']                #10
   *       +- StringField ['NameField']         #11
   *       +- DateField ['StartDate']           #12
   *
   *   CALL:                                    RESULT:
   *   ---------------------------------------------------------------------------------------------
   *   this.widget('RightBox')                  #8               (might not be the expected result)
   *   this.nearestWidget('RightBox')           #10
   *
   *   this.widget('NameField')                 #3
   *   this.nearestWidget('NameField')          null             (because no direct child has the requested id)
   *   this.nearestWidget('NameField', true)    #3               (because #3 and #11 have the same distance)
   *
   *   this.widget('StartDate')                 #7
   *   this.nearestWidget('StartDate', true)    #12              (#12 has smaller distance than #7)
   *
   * @param {string} widgetId
   *          The ID of the widget to find.
   * @param {boolean} [deep=false]
   *          If false, only this widget and the next level are checked. This is the default.
   *          If true, the entire tree is traversed.
   * @return {Widget} the first found widget, or null if no widget was found.
   */
  nearestWidget(widgetId, deep) {
    if (this.id === widgetId) {
      return this;
    }
    let widgets = this.children.slice(); // list of widgets to check
    while (widgets.length) {
      let widget = widgets.shift();
      if (widget.id === widgetId) {
        return widget; // found
      }
      if (deep) {
        for (let i = 0; i < widget.children.length; i++) {
          let child = widget.children[i];
          if (child.parent === widget) { // same check as in visitChildren()
            widgets.push(child);
          }
        }
      }
    }
    return null;
  }

  /**
   * @returns {AnyWidget} the first parent for which the given function returns true.
   */
  findParent(predicate) {
    let parent = this.parent;
    while (parent) {
      if (predicate(parent)) {
        return parent;
      }
      parent = parent.parent;
    }
    return parent;
  }

  /**
   * @returns {AnyWidget} the first child for which the given function returns true.
   */
  findChild(predicate) {
    let foundChild = null;
    this.visitChildren(child => {
      if (predicate(child)) {
        foundChild = child;
        return true;
      }
    });
    return foundChild;
  }

  setTrackFocus(trackFocus) {
    this.setProperty('trackFocus', trackFocus);
  }

  _renderTrackFocus() {
    if (!this.$container) {
      return;
    }
    if (this.trackFocus) {
      this.$container.on('focusin', this._focusInListener);
    } else {
      this.$container.off('focusin', this._focusInListener);
    }
  }

  restoreFocus() {
    if (this._$lastFocusedElement) {
      this.session.focusManager.requestFocus(this._$lastFocusedElement);
    } else if (this._storedFocusedWidget) {
      this._storedFocusedWidget.focus();
      this._storedFocusedWidget = null;
    }
  }

  /**
   * Method invoked once a 'focusin' event is fired by this context's $container or one of its child controls.
   */
  _onFocusIn(event) {
    // do not track focus events during rendering to avoid initial focus to be restored.
    if (this.rendering) {
      return;
    }
    let $target = $(event.target);
    if (this.$container.has($target)) {
      this._$lastFocusedElement = $target;
    }
  }

  /**
   * Tries to set the focus on the widget.
   * <p>
   * By default the focus is set on the container but this may vary from widget to widget.
   *
   * @param {object} [options]
   * @param {boolean} [options.preventScroll] prevents scrolling to new focused element (defaults to false)
   * @returns {boolean} true if the element could be focused, false if not
   */
  focus(options) {
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this, options));
      return false;
    }
    return this.session.focusManager.requestFocus(this.getFocusableElement(), null, options);
  }

  /**
   * Calls {@link focus()} and prevents the default behavior of the event if the focusing was successful.
   */
  focusAndPreventDefault(event) {
    if (this.focus()) {
      // Preventing blur is bad for touch devices because it prevents that the keyboard can close.
      // In that case focus() will return false because focus manager is disabled.
      event.preventDefault();
      return true;
    }
    return false;
  }

  /**
   * @returns {boolean} whether the widget is the currently active element
   */
  isFocused() {
    return this.rendered && focusUtils.isActiveElement(this.getFocusableElement());
  }

  /**
   * @param {boolean} [checkTabbable=true] if true, the widget has to be tabbable, not only focusable.
   * @return {boolean} true if the element is focusable (and tabbable, unless checkTabbable is set to false), false if not.
   */
  isFocusable(checkTabbable) {
    if (!this.rendered || !this.visible) {
      return false;
    }
    let elem = this.getFocusableElement();
    if (!elem) {
      return false;
    }
    let $elem = $.ensure(elem);
    if (!$elem.is(':focusable')) {
      return false;
    }
    if (scout.nvl(checkTabbable, true)) {
      return $elem.is(':tabbable');
    }
    return true;
  }

  /**
   * This method returns the HtmlElement to be used when {@link #focus()} is called.
   * It can be overridden, in case the widget needs to return something other than this.$container[0].
   */
  getFocusableElement() {
    if (this.rendered && this.$container) {
      return this.$container[0];
    }
    return null;
  }

  /**
   * @param {object} [options]
   */
  _installScrollbars(options) {
    let $scrollable = this.get$Scrollable();
    if (!$scrollable) {
      throw new Error('Scrollable is not defined, cannot install scrollbars');
    }
    if ($scrollable.data('scrollable')) {
      // already installed
      return;
    }
    options = options || {};
    let defaults = {
      parent: this
    };
    options = $.extend({}, defaults, options);
    scrollbars.install($scrollable, options);
    $scrollable.on('scroll', this._scrollHandler);
  }

  _uninstallScrollbars() {
    let $scrollable = this.get$Scrollable();
    if (!$scrollable || !$scrollable.data('scrollable')) {
      return;
    }
    scrollbars.uninstall($scrollable, this.session);
    $scrollable.off('scroll', this._scrollHandler);
    if (!this.removing) {
      // If scrollbars are removed on the fly and not because the widget is removing, reset scroll positions to initial state
      // Only reset if position is 0 to preserve the position (uninstalling does not reset the position of the scrollable either)
      if ($scrollable[0].scrollTop === 0) {
        this.scrollTop = null;
      }
      if ($scrollable[0].scrollLeft === 0) {
        this.scrollLeft = null;
      }
    }
  }

  _onScroll() {
    let $scrollable = this.get$Scrollable();
    this._setProperty('scrollTop', $scrollable[0].scrollTop);
    this._setProperty('scrollLeft', $scrollable[0].scrollLeft);
  }

  setScrollTop(scrollTop) {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().setScrollTop(scrollTop);
      return;
    }
    this.setProperty('scrollTop', scrollTop);
  }

  _renderScrollTop() {
    let $scrollable = this.get$Scrollable();
    if (!$scrollable || this.scrollTop === null) {
      // Don't do anything for non scrollable elements. Also, reading $scrollable[0].scrollTop must not be done while rendering because it would provoke a reflow
      return;
    }
    if (this.rendering || this.htmlComp && !this.htmlComp.layouted && !this.htmlComp.layouting) {
      // If the widget is not layouted yet (which is always true while rendering), the scroll position cannot be updated -> do it after the layout
      // If scroll top is set while layouting, layout obviously wants to set it -> do it
      this.session.layoutValidator.schedulePostValidateFunction(this._renderScrollTop.bind(this));
      return;
    }
    scrollbars.scrollTop($scrollable, this.scrollTop);
  }

  setScrollLeft(scrollLeft) {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().setScrollLeft(scrollLeft);
      return;
    }
    this.setProperty('scrollLeft', scrollLeft);
  }

  _renderScrollLeft() {
    let $scrollable = this.get$Scrollable();
    if (!$scrollable || this.scrollLeft === null) {
      // Don't do anything for non scrollable elements. Also, reading $scrollable[0].scrollLeft must not be done while rendering because it would provoke a reflow
      return;
    }
    if (this.rendering || this.htmlComp && !this.htmlComp.layouted && !this.htmlComp.layouting) {
      // If the widget is not layouted yet (which is always true while rendering), the scroll position cannot be updated -> do it after the layout
      // If scroll left is set while layouting, layout obviously wants to set it -> do it
      this.session.layoutValidator.schedulePostValidateFunction(this._renderScrollLeft.bind(this));
      return;
    }
    scrollbars.scrollLeft($scrollable, this.scrollLeft);
  }

  /**
   * Returns the jQuery element which is supposed to be scrollable. This element will be used by the scroll functions like {@link #_installScrollbars}, {@link #setScrollTop}, {@link #setScrollLeft}, {@link #scrollToBottom} etc..
   * The element won't be used unless {@link #_installScrollbars} is called.
   * If the widget is mainly a wrapper for a scrollable widget and does not have a scrollable element by itself, you can use @{link #getDelegateScrollable} instead.
   * @return {$}
   */
  get$Scrollable() {
    return this.$container;
  }

  hasScrollShadow(position) {
    return scrollbars.hasScrollShadow(this.get$Scrollable(), position);
  }

  /**
   * If the widget is mainly a wrapper for another widget, it is often the case that the other widget is scrollable and not the wrapper.
   * In that case implement this method and return the other widget so that the calls to the scroll functions can be delegated.
   * @return {Widget}
   */
  getDelegateScrollable() {
    return null;
  }

  scrollToTop(options) {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().scrollToTop();
      return;
    }
    let $scrollable = this.get$Scrollable();
    if (!$scrollable) {
      return;
    }
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.scrollToTop.bind(this));
      return;
    }
    scrollbars.scrollTop($scrollable, 0, options);
  }

  scrollToBottom(options) {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().scrollToBottom();
      return;
    }
    let $scrollable = this.get$Scrollable();
    if (!$scrollable) {
      return;
    }
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.scrollToBottom.bind(this));
      return;
    }
    scrollbars.scrollToBottom($scrollable, options);
  }

  /**
   * Brings the widget into view by scrolling the first scrollable parent.
   */
  reveal(options) {
    if (!this.rendered) {
      return;
    }
    let $scrollParent = this.$container.scrollParent();
    if ($scrollParent.length === 0) {
      // No scrollable parent found -> scrolling is not possible
      return;
    }
    scrollbars.scrollTo($scrollParent, this.$container, options);
  }

  /**
   * Visits every child of this widget in pre-order (top-down).<br>
   * This widget itself is not visited! Only child widgets are visited recursively.
   * <p>
   * The children with a different parent are excluded.<br>
   * This makes sure the child is not visited twice if the owner and the parent are not the same
   * (in that case the widget would be in the children list of the owner and of the parent).
   * <p>
   * In order to abort visiting, the visitor can return true.
   *
   * @param {function(AnyWidget):boolean|TreeVisitResult|null} visitor
   * @returns {boolean} true if the visitor aborted the visiting, false if the visiting completed without aborting
   */
  visitChildren(visitor) {
    for (let i = 0; i < this.children.length; i++) {
      let child = this.children[i];
      if (child.parent === this) {
        let treeVisitResult = visitor(child);
        if (treeVisitResult === true || treeVisitResult === TreeVisitResult.TERMINATE) {
          // Visitor wants to abort the visiting
          return TreeVisitResult.TERMINATE;
        } else if (treeVisitResult !== TreeVisitResult.SKIP_SUBTREE) {
          treeVisitResult = child.visitChildren(visitor);
          if (treeVisitResult === true || treeVisitResult === TreeVisitResult.TERMINATE) {
            return TreeVisitResult.TERMINATE;
          }
        }
      }
    }
  }

  /**
   * @returns {boolean} Whether or not the widget is rendered (or rendering) and the DOM $container isAttached()
   */
  isAttachedAndRendered() {
    return (this.rendered || this.rendering) && this.$container.isAttached();
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static cssClassAsArray(cssClass) {
    let cssClasses = [],
      cssClassesStr = cssClass || '';

    cssClassesStr = cssClassesStr.trim();
    if (cssClassesStr.length > 0) {
      cssClasses = cssClassesStr.split(' ');
    }
    return cssClasses;
  }
}
