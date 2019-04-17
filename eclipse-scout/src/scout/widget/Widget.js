import * as $ from 'jquery';
import * as arrays from '../utils/arrays';
import * as scout from '../scout';
import EventSupport from './../EventSupport';
import * as strings from '../utils/strings';
import Event from '../utils/Event';
import DeferredGlassPaneTarget from '../glasspane/DeferredGlassPaneTarget';
import EventDelegator from '../utils/EventDelegator';

export default class Widget {

  constructor() {
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
     * The 'rendering' flag is set the true while the _inital_ rendering is performed.
     * It is used to to something different in a _render* method when the method is
     * called for the first time.
     */
    this.rendering = false;
    this.removing = false;
    this.removalPending = false;

    /**
     * The 'rendered' flag is set the true when initial rendering of the widget is completed.
     */
    this.rendered = false;
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
    this.disabledStyle = DisabledStyle.DEFAULT;
    this.visible = true;
    this.focused = false;
    this.loading = false;
    this.cssClass = null;
    this.scrollTop = null;
    this.scrollLeft = null;

    this.$container;

    // If set to true, remove won't remove the element immediately but after the animation has been finished
    // This expects a css animation which may be triggered by the class 'animate-remove'
    // If browser does not support css animation, remove will be executed immediately
    this.animateRemoval = false;
    this.animateRemovalClass = 'animate-remove';

    this._widgetProperties = [];
    this._cloneProperties = ['visible', 'enabled', 'inheritAccessibility', 'cssClass'];
    this.eventDelegators = [];
    this._preserveOnPropertyChangeProperties = [];
    this._postRenderActions = [];
    this._parentDestroyHandler = this._onParentDestroy.bind(this);
    this._parentRemovingWhileAnimatingHandler = this._onParentRemovingWhileAnimating.bind(this);
    this._scrollHandler = this._onScroll.bind(this);
    this.events = this._createEventSupport();
    this.loadingSupport = this._createLoadingSupport();
    this.keyStrokeContext = this._createKeyStrokeContext();
    // Widgets using scout.LogicalGridLayout may have a grid to calculate the grid data of the children
    this.logicalGrid;
  }

  init(model) {
    var staticModel = this._jsonModel();
    if (staticModel) {
      model = $.extend({}, staticModel, model);
    }
    this._init(model);
    this._initKeyStrokeContext();
    this.initialized = true;
    this.trigger('init');
  };

  _init(model) {
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

    this._eachProperty(model, function(propertyName, value, isWidgetProperty) {
      if (value === undefined) {
        // Don't set the value if it is undefined, compared to null which is allowed explicitly ($.extend works in the same way)
        return;
      }
      if (isWidgetProperty) {
        value = this._prepareWidgetProperty(propertyName, value);
      }
      this._initProperty(propertyName, value);
    }.bind(this));

    this._setCssClass(this.cssClass);
    this._setLogicalGrid(this.logicalGrid);
    this._setEnabled(this.enabled);
  };

  /**
   * This function sets the property value. Override this function when you need special init behavior for certain properties.
   * For instance you could not simply set the property value, but extend an already existing value.
   */
  _initProperty(propertyName, value) {
    this[propertyName] = value;
  };

  /**
   * Default implementation simply returns undefined. A Subclass
   * may override this method to load or extend a JSON model with scout.models.getModel or scout.models.extend.
   */
  _jsonModel() {};

  /**
   * Creates the widgets using the given models, or returns the widgets if the given models already are widgets.
   * @returns an array of created widgets if models was an array. Or the created widget if models is not an array.
   */
  _createChildren(models) {
    if (!models) {
      return null;
    }

    if (!Array.isArray(models)) {
      return this._createChild(models);
    }

    var widgets = [];
    models.forEach(function(model, i) {
      widgets[i] = this._createChild(model);
    }, this);
    return widgets;
  };

  _createChild(model) {
    if (model instanceof Widget) {
      return model;
    }
    if (typeof model === 'string') {
      // Special case: If only an ID is supplied, try to (locally) resolve the corresponding widget
      var existingWidget = this.widget(model);
      if (!existingWidget) {
        throw new Error('Referenced widget not found: ' + model);
      }
      return existingWidget;
    }
    model.parent = this;
    return scout.create(model);
  };

  _initKeyStrokeContext() {
    if (!this.keyStrokeContext) {
      return;
    }
    this.keyStrokeContext.$scopeTarget = function() {
      return this.$container;
    }.bind(this);
    this.keyStrokeContext.$bindTarget = function() {
      return this.$container;
    }.bind(this);
  };

  destroy() {
    if (this.destroyed) {
      // Already destroyed, do nothing
      return;
    }
    this.destroying = true;
    if (this.rendered && (this.animateRemoval || this._isRemovalPrevented())) {
      // Do not destroy yet if the removal happens animated
      // Also don't destroy if the removal is pending to keep the parent / child link until removal finishes
      this.one('remove', function() {
        this.destroy();
      }.bind(this));
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
  };

  /**
   * Override this function to do clean-up (like removing listeners) when the widget is destroyed.
   * The default impl. does nothing.
   */
  _destroy() {
    // NOP
  };

  /**
   * @param widgets may be an object or array of objects
   */
  _destroyChildren(widgets) {
    if (!widgets) {
      return;
    }

    widgets = arrays.ensure(widgets);
    widgets.forEach(function(widget) {
      this._destroyChild(widget);
    }, this);
  };

  _destroyChild(child) {
    if (child.owner !== this) {
      return;
    }
    child.destroy();
  };

  /**
   * @param [$parent] The jQuery element which is used as $parent when rendering this widget.
   * It will be put onto the widget and is therefore accessible as this.$parent in the _render method.
   * If not specified, the $container of the parent is used.
   */
  render($parent) {
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
    this.$parent = $parent || this.parent.$container;
    this._render();
    this._renderProperties();
    this._renderInspectorInfo();
    this._linkWithDOM();
    //this.session.keyStrokeManager.installKeyStrokeContext(this.keyStrokeContext);
    this.rendering = false;
    this.rendered = true;
    this.attached = true;
    this.trigger('render');
    this._postRender();
  };

  /**
   * This method creates the UI through DOM manipulation. At this point we should not apply model
   * properties on the UI, since sub-classes may need to contribute to the DOM first. You must not
   * apply model values to the UI here, since this is done in the _renderProperties method later.
   * The default impl. does nothing.
   */
  _render() {
    // NOP
  };

  /**
   * This method calls the UI setter methods after the _render method has been executed.
   * Here values of the model are applied to the DOM / UI.
   */
  _renderProperties() {
    this._renderEnabled();
    this._renderVisible();
    this._renderFocused();
    this._renderCssClass();
    this._renderLoading();
    this._renderScrollTop();
    this._renderScrollLeft();
  };

  /**
   * Method invoked once rendering completed and 'rendered' flag is set to 'true'.<p>
   * By default executes every action of this._postRenderActions
   */
  _postRender() {
    var actions = this._postRenderActions;
    this._postRenderActions = [];
    actions.forEach(function(action) {
      action();
    });
  };

  remove() {
    if (!this.rendered || this._isRemovalPrevented()) {
      return;
    }
    if (this.animateRemoval) {
      this._removeAnimated();
    } else {
      this._removeInternal();
    }
  };

  /**
   * Will be called by {@link #remove()}. If true is returned, the widget won't be removed.<p>
   * By default it just delegates to {@link #_isRemovalPending}. May be overridden to customize it.
   */
  _isRemovalPrevented() {
    return this._isRemovalPending();
  };

  /**
   * Returns true if the removal of this or an ancestor widget is pending. Checking the ancestor is omitted if the parent is being removed.
   * This may be used to prevent a removal if an ancestor will be removed (e.g by an animation)
   */
  _isRemovalPending() {
    if (this.removalPending) {
      return true;
    }
    var parent = this.parent;
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
  };

  _removeInternal() {
    if (!this.rendered) {
      return;
    }

    this.removing = true;
    this.removalPending = false;
    this.trigger('removing');

    // remove children in reverse order.
    this.children.slice().reverse().forEach(function(child) {
      // Only remove the child if this widget is the current parent (if that is not the case this widget is the owner)
      if (child.parent === this) {
        child.remove();
      }
    }, this);
    this._cleanup();
    this._remove();
    this.$parent = null;
    this.rendered = false;
    this.attached = false;
    this.removing = false;
    this.trigger('remove');
  };

  /**
   * Adds class 'animate-remove' to container which can be used to trigger the animation.
   * After the animation is executed, the element gets removed using this._removeInternal.
   */
  _removeAnimated() {
    this._removeInternal();
  };

  _onParentRemovingWhileAnimating() {
    this._removeInternal();
  };

  _renderInspectorInfo() {};

  /**
   * Links $container with the widget.
   */
  _linkWithDOM() {
    if (this.$container) {
      this.$container.data('widget', this);
    }
  };

  /**
   * Called right before _remove is called.
   * Default calls LayoutValidator.cleanupInvalidComponents to make sure that child components are removed from the invalid components list.
   * Also uninstalls key stroke context, loading support and scrollbars.
   */
  _cleanup() {
    this.parent.off('removing', this._parentRemovingWhileAnimatingHandler);
    if (this.loadingSupport) {
      this.loadingSupport.remove();
    }
    this._uninstallScrollbars();
    if (this.$container) {
      this.session.layoutValidator.cleanupInvalidComponents(this.$container);
    }
  };

  _remove() {
    if (this.$container) {
      this.$container.remove();
      this.$container = null;
    }
  };

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
  };

  setParent(parent) {
    scout.assertParameter('parent', parent);
    if (parent === this.parent) {
      return;
    }

    if (this.parent) {
      // Don't link to new parent yet if removal is still pending.
      // After the animation the parent will remove its children.
      // If they are already linked to a new parent, removing the children is not possible anymore.
      // This may lead to an 'Already rendered' exception if the new parent wants to render its children.
      if (this.parent._isRemovalPending()) {
        this.parent.one('remove', function() {
          this.setParent(parent);
        }.bind(this));
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
    this.parent = parent;
    this.parent._addChild(this);
    if (this.initialized) {
      this.recomputeEnabled(this.parent.enabledComputed);
    }
    this.parent.one('destroy', this._parentDestroyHandler);
  };

  _addChild(child) {
    if (this.children.indexOf(child) === -1) {
      this.children.push(child);
    }
  };

  _removeChild(child) {
    arrays.remove(this.children, child);
  };

  /**
   * @returns a list of all ancestors
   */
  ancestors() {
    var ancestors = [];
    var parent = this.parent;
    while (parent) {
      ancestors.push(parent);
      parent = parent.parent;
    }
    return ancestors;
  };

  /**
   * @returns true if the given widget is the same as this or a descendant
   */
  isOrHas(widget) {
    if (widget === this) {
      return true;
    }
    return this.has(widget);
  };

  /**
   * @returns true if the given widget is a descendant
   */
  has(widget) {
    while (widget) {
      if (widget.parent === this) {
        return true;
      }
      widget = widget.parent;
    }

    return false;
  };
  /*
      getForm() {
          return scout.Form.findForm(this);
      };

      findNonWrappedForm() {
          return scout.Form.findNonWrappedForm(this);
      };
  */
  /**
   * @returns the desktop linked to the current session.
   * If desktop is still initializing it might not be available yet, in that case it searches the parent hierarchy for it.
   */
  findDesktop() {
    if (this.session.desktop) {
      return this.session.desktop;
    }
    return this.findParent(function(parent) {
      return parent instanceof Desktop;
    });
  };

  /**
   * Changes the enabled property of this form field to the given value.
   *
   * @param enabled
   *          Required. The new enabled value
   * @param updateParents
   *          (optional) If true, the enabled property of all parent form fields are
   *          updated to same value as well. Default is false.
   * @param updateChildren
   *          (optional) If true the enabled property of all child form fields (recursive)
   *          are updated to same value as well. Default is false.
   */
  setEnabled(enabled, updateParents, updateChildren) {
    this.setProperty('enabled', enabled);

    if (enabled && updateParents && this.parent) {
      this.parent.setEnabled(true, true, false);
    }

    if (updateChildren) {
      this.visitChildren(function(field) {
        field.setEnabled(enabled);
      });
    }
  };

  _setEnabled(enabled) {
    this._setProperty('enabled', enabled);
    this.recomputeEnabled();
  };

  recomputeEnabled(parentEnabled) {
    if (parentEnabled === undefined) {
      parentEnabled = true;
      if (this.parent && this.parent.initialized && this.parent.enabledComputed !== undefined) {
        parentEnabled = this.parent.enabledComputed;
      }
    }

    var enabledComputed = this._computeEnabled(this.inheritAccessibility, parentEnabled);
    this.setProperty('enabledComputed', enabledComputed);

    // Manually call _renderEnabled(), because _renderEnabledComputed() does not exist
    if (this.rendered) {
      this._renderEnabled(); // refresh
    }

    this.children.forEach(function(child) {
      child.recomputeEnabled(enabledComputed);
    });
  };

  _computeEnabled(inheritAccessibility, parentEnabled) {
    return this.enabled && (inheritAccessibility ? parentEnabled : true);
  };

  _renderEnabled() {
    if (!this.$container) {
      return;
    }
    this.$container.setEnabled(this.enabledComputed);
    this._renderDisabledStyle();
  };

  setInheritAccessibility(inheritAccessibility) {
    this.setProperty('inheritAccessibility', inheritAccessibility);
  };

  _setInheritAccessibility(inheritAccessibility) {
    this._setProperty('inheritAccessibility', inheritAccessibility);
    this.recomputeEnabled();
  };

  setDisabledStyle(disabledStyle) {
    this.setProperty('disabledStyle', disabledStyle);

    this.children.forEach(function(child) {
      child.setDisabledStyle(disabledStyle);
    });
  };

  _renderDisabledStyle() {
    this._renderDisabledStyleInternal(this.$container);
  };

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
      $element.toggleClass('read-only', this.disabledStyle === DisabledStyle.READ_ONLY);
    }
  };

  setVisible(visible) {
    this.setProperty('visible', visible);
  };

  /**
   * @returns whether the widget is visible or not. May depend on other conditions than the visible property only
   */
  isVisible() {
    return this.visible;
  };

  _renderVisible() {
    if (!this.$container) {
      return;
    }
    this.$container.setVisible(this.isVisible());
    this.invalidateParentLogicalGrid();
  };

  /**
   * This function does not set the focus to the field. It toggles the 'focused' class on the field container if present.
   * Objects using widget as prototype must call this function onBlur and onFocus to ensure the class gets toggled.
   *
   *  Use scout.Widget.focus to set the focus to the widget.
   */
  setFocused(focused) {
    this.setProperty('focused', focused);
  };

  _renderFocused() {
    if (this.$container) {
      this.$container.toggleClass('focused', this.focused);
    }
  };

  _setCssClass(cssClass) {
    if (this.rendered) {
      this._removeCssClass();
    }
    this._setProperty('cssClass', cssClass);
  };

  _removeCssClass() {
    if (!this.$container) {
      return;
    }
    this.$container.removeClass(this.cssClass);
  };

  _renderCssClass() {
    if (!this.$container) {
      return;
    }
    this.$container.addClass(this.cssClass);
  };

  setCssClass(cssClass) {
    this.setProperty('cssClass', cssClass);
  };

  addCssClass(cssClass) {
    var cssClasses = this.cssClassAsArray();
    var cssClassesToAdd = Widget.cssClassAsArray(cssClass);
    cssClassesToAdd.forEach(function(newCssClass) {
      if (cssClasses.indexOf(newCssClass) >= 0) {
        return;
      }
      cssClasses.push(newCssClass);
    }, this);
    this.setProperty('cssClass', arrays.format(cssClasses, ' '));
  };

  removeCssClass(cssClass) {
    var cssClasses = this.cssClassAsArray();
    var cssClassesToRemove = Widget.cssClassAsArray(cssClass);
    if (arrays.removeAll(cssClasses, cssClassesToRemove)) {
      this.setProperty('cssClass', arrays.format(cssClasses, ' '));
    }
  };

  toggleCssClass(cssClass, condition) {
    if (condition) {
      this.addCssClass(cssClass);
    } else {
      this.removeCssClass(cssClass);
    }
  };

  cssClassAsArray() {
    return Widget.cssClassAsArray(this.cssClass);
  };

  /**
   * Creates nothing by default. If a widget needs loading support, override this method and return a loading support.
   */
  _createLoadingSupport() {
    return null;
  };

  setLoading(loading) {
    this.setProperty('loading', loading);
  };

  isLoading() {
    return this.loading;
  };

  _renderLoading() {
    if (!this.loadingSupport) {
      return;
    }
    this.loadingSupport.renderLoading();
  };

  //--- Layouting / HtmlComponent methods ---

  pack() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.pack();
  };

  invalidateLayout() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.invalidateLayout();
  };

  validateLayout() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.validateLayout();
  };

  revalidateLayout() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.revalidateLayout();
  };

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
  };

  validateLayoutTree() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.validateLayoutTree();
  };

  revalidateLayoutTree(invalidateParents) {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.revalidateLayoutTree(invalidateParents);
  };

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
  };

  /**
   * If the widget uses a logical grid layout, the grid may be validated using this method.
   * <p>
   * If the grid is not dirty, nothing happens.
   */
  validateLogicalGrid() {
    if (this.logicalGrid) {
      this.logicalGrid.validate(this);
    }
  };

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
  };

  /**
   * Invalidates the logical grid of the parent widget. Typically done when the visibility of the widget changes.
   * @param {boolean} [invalidateLayout] true, to invalidate the layout of the parent of this.htmlComp, false if not. Default is true.
   */
  invalidateParentLogicalGrid(invalidateLayout) {
    if (!this.rendered || !this.htmlComp) {
      return;
    }
    this.parent.invalidateLogicalGrid(false);
    if (scout.nvl(invalidateLayout, true)) {
      var htmlCompParent = this.htmlComp.getParent();
      if (htmlCompParent) {
        htmlCompParent.invalidateLayoutTree();
      }
    }
  };

  revalidateLogicalGrid(invalidateLayout) {
    this.invalidateLogicalGrid(invalidateLayout);
    this.validateLogicalGrid();
  };

  setLogicalGrid(logicalGrid) {
    this.setProperty('logicalGrid', logicalGrid);
  };

  _setLogicalGrid(logicalGrid) {
    if (typeof logicalGrid === 'string') {
      logicalGrid = scout.create(logicalGrid);
    }
    this._setProperty('logicalGrid', logicalGrid);
    this.invalidateLogicalGrid();
  };

  //--- Event handling methods ---
  _createEventSupport() {
    return new EventSupport();
  };

  trigger(type, event) {
    event = event || {};
    event.source = this;
    this.events.trigger(type, event);
  };

  one(type, func) {
    this.events.one(type, func);
  };

  on(type, func) {
    return this.events.on(type, func);
  };

  off(type, func) {
    this.events.off(type, func);
  };

  addListener(listener) {
    this.events.addListener(listener);
  };

  removeListener(listener) {
    this.events.removeListener(listener);
  };

  /**
   * Adds an event handler using {@link #one()} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   */
  when(type) {
    return this.events.when(type);
  };

  /**
   * @param $element (optional) element from which the entryPoint will be resolved. If not set this.parent.$container is used.
   * @returns the entry-point for this Widget. If the widget is part of the main-window it returns this.session.$entryPoint,
   * for popup-window this function will return the body of the document in the popup window.
   */
  entryPoint($element) {
    $element = scout.nvl($element, this.parent.$container);
    if (!$element.length) {
      throw new Error('Cannot resolve entryPoint, $element.length is 0 or undefined');
    }
    return $element.entryPoint();
  };

  window(domElement) {
    var $el = this.$container || this.$parent;
    return $el ? $el.window(domElement) : (domElement ? null : $(null));
  };

  document(domElement) {
    var $el = this.$container || this.$parent;
    return $el ? $el.document(domElement) : (domElement ? null : $(null));
  };

  /**
   * This method attaches the detached $container to the DOM.
   */
  attach() {
    if (this.attached || !this.rendered) {
      return;
    }
    this._attach();
    this._afterAttach();
    this._triggerChildrenAfterAttach(this);
  };

  _triggerChildrenAfterAttach(parent) {
    this.children.forEach(function(child) {
      child._afterAttach();
      child._triggerChildrenAfterAttach(parent);
    });
  };

  _afterAttach() {
    // NOP
  };

  _attach() {
    this.attached = true;
  };

  /**
   * This method calls detach() on all child-widgets. It is used to store some data
   * before a DOM element is detached and propagate the detach 'event' to all child-
   * widgets, because when a DOM element is detached - child elements are not notified
   */
  detach() {
    if (this.rendering) {
      // Defer the execution of detach. If it was detached while rendering the attached flag would be wrong.
      this._postRenderActions.push(this.detach.bind(this));
    }
    if (!this.attached || !this.rendered || this._isRemovalPending()) {
      return;
    }

    this._triggerChildrenBeforeDetach(this);
    this._beforeDetach();
    this._detach();
  };

  /**
   * Override this method to do something when Widget is detached. Typically you
   * will call this.$container.detach() here and use the DetachHelper to store
   * additional state (focus, scrollbars) for the detached element. The default
   * implementation sets this.attached to false.
   */
  _detach() {
    this.attached = false;
  };

  _triggerChildrenBeforeDetach() {
    this.children.forEach(function(child) {
      child._beforeDetach();
      child._triggerChildrenBeforeDetach(parent);
    });
  };

  _beforeDetach(parent) {
    // NOP
  };

  /**
   * Does nothing by default. If a widget needs keystroke support override this method and return a keystroke context, e.g. the default scout.KeyStrokeContext.
   */
  _createKeyStrokeContext() {
    return null;
  };

  updateKeyStrokes(newKeyStrokes, oldKeyStrokes) {
    this.unregisterKeyStrokes(oldKeyStrokes);
    this.registerKeyStrokes(newKeyStrokes);
  };

  registerKeyStrokes(keyStrokes) {
    //this.keyStrokeContext.registerKeyStrokes(keyStrokes);
  };

  unregisterKeyStrokes(keyStrokes) {
    //this.keyStrokeContext.unregisterKeyStrokes(keyStrokes);
  };

  /**
   * Triggers a property change for a single property. The event is only triggered when
   * old and new value are the same.
   */
  triggerPropertyChange(propertyName, oldValue, newValue) {
    scout.assertParameter('propertyName', propertyName);
    var event = new Event({
      propertyName: propertyName,
      oldValue: oldValue,
      newValue: newValue
    });
    this.trigger('propertyChange', event);
    return event;
  };

  /**
   * Sets the value of the property 'propertyName' to 'newValue' and then fires a propertyChange event for that property.
   */
  _setProperty(propertyName, newValue) {
    scout.assertParameter('propertyName', propertyName);
    var oldValue = this[propertyName];
    if (scout.equals(oldValue, newValue)) {
      return;
    }
    this[propertyName] = newValue;
    var event = this.triggerPropertyChange(propertyName, oldValue, newValue);
    if (event.defaultPrevented) {
      // Revert to old value if property change should be prevented
      this[propertyName] = oldValue;
    }
  };

  /**
   * Sets a new value for a specific property. If the new value is the same value as the old one, nothing is performed.
   * Otherwise the following phases are executed:
   * <p>
   * 1. Preparation: If the property is a widget property, several actions are performed in _prepareWidgetProperty().
   * 2. DOM removal: If the property is a widget property and the widget is rendered, the changed widget(s) are removed unless the property should not be preserved (see _preserveOnPropertyChangeProperties).
   *    If there is a custom remove function (e.g. _removeXY where XY is the property name), it will be called instead of removing the widgets directly.
   * 3. Model update: If there is a custom set function (e.g. _setXY where XY is the property name), it will be called. Otherwise the default set function _setProperty is called.
   * 4. DOM rendering: If the widget is rendered and there is a custom render function (e.g. _renderXY where XY is the property name), it will be called. Otherwise nothing happens.
   */
  setProperty(propertyName, value) {
    if (scout.equals(this[propertyName], value)) {
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

  _prepareProperty(propertyName, value) {
    if (!this.isWidgetProperty(propertyName)) {
      return value;
    }
    return this._prepareWidgetProperty(propertyName, value);
  };

  _prepareWidgetProperty(propertyName, widgets) {
    // Create new child widget(s)
    widgets = this._createChildren(widgets);

    var oldWidgets = this[propertyName];
    if (oldWidgets && Array.isArray(widgets)) {
      // If new value is an array, old value has to be one as well
      // Only destroy those which are not in the new array
      oldWidgets = arrays.diff(oldWidgets, widgets);
    }

    // Destroy old child widget(s)
    if (!this.isPreserveOnPropertyChangeProperty(propertyName)) {
      this._destroyChildren(oldWidgets);
    }

    // Link to new parent
    this.link(widgets);

    return widgets;
  };

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
    var widgets = this[propertyName];
    if (!widgets) {
      return;
    }
    var removeFuncName = '_remove' + strings.uppercaseFirstLetter(propertyName);
    if (this[removeFuncName]) {
      this[removeFuncName]();
    } else {
      this._internalRemoveWidgets(widgets);
    }
  };

  /**
   * Removes the given widgets
   */
  _internalRemoveWidgets(widgets) {
    widgets = arrays.ensure(widgets);
    widgets.forEach(function(widget) {
      widget.remove();
    });
  };

  _callSetProperty(propertyName, value) {
    var setFuncName = '_set' + strings.uppercaseFirstLetter(propertyName);
    if (this[setFuncName]) {
      this[setFuncName](value);
    } else {
      this._setProperty(propertyName, value);
    }
  };

  _callRenderProperty(propertyName) {
    var renderFuncName = '_render' + strings.uppercaseFirstLetter(propertyName);
    if (!this[renderFuncName]) {
      return;
    }
    this[renderFuncName]();
  };

  /**
   * Sets this widget as parent of the given widget(s).
   *
   * @param widgets may be a widget or array of widgets
   */
  link(widgets) {
    if (!widgets) {
      return;
    }

    widgets = arrays.ensure(widgets);
    widgets.forEach(function(child) {
      child.setParent(this);
    }, this);
  };

  /**
   * Method required for widgets which are supposed to be directly covered by a glasspane.<p>
   *
   * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box or file-chooser is shown with this widget as its 'displayParent'.<br>
   * If the widget is not rendered yet, a scout.DerredGlassPaneTarget is returned.<br>
   * In both cases the method _glassPaneTargets is called which may be overridden by the actual widget.
   */
  glassPaneTargets(element) {
    if (this.rendered) {
      return this._glassPaneTargets(element);
    }

    return DeferredGlassPaneTarget.createFor(this, this._glassPaneTargets.bind(this, element));
  };

  _glassPaneTargets() {
    return [this.$container];
  };

  toString() {
    var attrs = '';
    attrs += 'id=' + this.id;
    attrs += ' objectType=' + this.objectType;
    attrs += ' rendered=' + this.rendered;
    /*if (this.$container) {
        attrs += ' $container=' + scout.graphics.debugOutput(this.$container);
    }*/
    return 'Widget[' + attrs.trim() + ']';
  };

  /**
   * Returns the ancestors as string delimited by '\n'.
   * @param [count] the number of ancestors to be processed. Default is -1 which means all.
   */
  ancestorsToString(count) {
    var str = '',
      ancestors = this.ancestors();

    count = scout.nvl(count, -1);
    ancestors.some(function(ancestor, i) {
      if (count > -1 && i >= count) {
        return true;
      }
      if (i > 0 && i < ancestors.length - 1) {
        str += '\n';
      }
      str += ancestor.toString();
    });
    return str;
  };

  resolveTextKeys(properties) {
    properties.forEach(function(property) {
      //let key = property || 'text';
      //this[key] = property;
      //scout.texts.resolveTextProperty(this, property);
    }, this);
  };

  resolveIconIds(properties) {
    properties.forEach(function(property) {
      //scout.icons.resolveIconProperty(this, property);
    }, this);
  };

  resolveConsts(configs) {
    configs.forEach(function(config) {
      // scout.objects.resolveConstProperty(this, config);
    }, this);
  };

  _addWidgetProperties(properties) {
    this._addProperties('_widgetProperties', properties);
  };

  isWidgetProperty(propertyName) {
    return this._widgetProperties.indexOf(propertyName) > -1;
  };

  _addCloneProperties(properties) {
    this._addProperties('_cloneProperties', properties);
  };

  isCloneProperty(propertyName) {
    return this._cloneProperties.indexOf(propertyName) > -1;
  };

  _addPreserveOnPropertyChangeProperties(properties) {
    this._addProperties('_preserveOnPropertyChangeProperties', properties);
  };

  isPreserveOnPropertyChangeProperty(propertyName) {
    return this._preserveOnPropertyChangeProperties.indexOf(propertyName) > -1;
  };

  _addProperties(propertyName, properties) {
    properties = arrays.ensure(properties);
    properties.forEach(function(property) {
      if (this[propertyName].indexOf(property) > -1) {
        throw new Error(propertyName + ' already contains the property ' + property);
      }
      this[propertyName].push(property);
    }, this);
  };

  _eachProperty(model, func) {
    var propertyName, value, i;

    // Loop through primitive properties
    for (propertyName in model) {
      if (this._widgetProperties.indexOf(propertyName) > -1) {
        continue; // will be handled below
      }
      value = model[propertyName];
      func(propertyName, value);
    }

    //Loop through adapter properties (any order will do).
    for (i = 0; i < this._widgetProperties.length; i++) {
      propertyName = this._widgetProperties[i];
      value = model[propertyName];
      if (value === undefined) {
        continue;
      }

      func(propertyName, value, true);
    }
  };

  _removeWidgetProperties(properties) {
    if (Array.isArray(properties)) {
      arrays.removeAll(this._widgetProperties, properties);
    } else {
      arrays.remove(this._widgetProperties, properties);
    }
  };

  /**
   * Clones the widget and mirrors the events, see this.clone() and this.mirror() for details.
   */
  cloneAndMirror(model) {
    return this.clone(model, {
      delegateAllPropertiesToClone: true
    });
  };

  /**
   * @returns the original widget from which this one was cloned. If it is not a clone, itself is returned.
   */
  original() {
    var original = this;
    while (original.cloneOf) {
      original = original.cloneOf;
    }
    return original;
  };

  /**
   * Clones the widget and returns the clone. Only the properties defined in this._cloneProperties are copied to the clone.
   * The parameter model has to contain at least the property 'parent'.
   *
   * OPTION                          DEFAULT VALUE   DESCRIPTION
   * --------------------------------------------------------------------------------------------------------
   * delegatePropertiesToClone       []              An array of all properties to be delegated from the original
   *                                                 to the to the clone when changed on the original widget.
   *
   * delegatePropertiesToOriginal    []              An array of all properties to be delegated from the clone
   *                                                 to the original when changed on the clone widget.
   *
   * excludePropertiesToOriginal     []              An array of all properties to be excluded from delegating
   *                                                 from the clone to the original in any cases.
   *
   * delegateEventsToOriginal        []              An array of all events to be delegated from the clone to
   *                                                 the original when fired on the clone widget.
   *
   * delegateAllPropertiesToClone    false           True to delegate all property changes from the original to
   *                                                 the clone.
   *
   * delegateAllPropertiesToOriginal false           True to delegate all property changes from the clone to
   *                                                 the original.
   *
   * @param model The model used to create the clone is a combination of the clone properties and this model.
   * Therefore this model may be used to override the cloned properties or to add additional properties.
   * @param options Options used for the clone widgets. See above.
   *
   */
  clone(model, options) {
    var clone, cloneModel;
    model = model || {};
    options = options || {};

    cloneModel = scout.extractProperties(this, model, this._cloneProperties);
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
  };

  _deepCloneProperties(clone, properties, options) {
    if (!properties) {
      return clone;
    }
    properties = arrays.ensure(properties);
    properties.forEach(function(property) {
      var propertyValue = this[property],
        clonedProperty = null;
      if (propertyValue === undefined) {
        throw new Error('Property \'' + property + '\' is undefined. Deep copy not possible.');
      }
      if (this._widgetProperties.indexOf(property) > -1) {
        if (Array.isArray(propertyValue)) {
          clonedProperty = propertyValue.map(function(val) {
            return val.clone({
              parent: clone
            }, options);
          }.bind(this));

        } else {
          clonedProperty = propertyValue.clone({
            parent: clone
          }, options);
        }
      } else {
        if (Array.isArray(propertyValue)) {
          clonedProperty = propertyValue.map(function(val) {
            return val;
          });

        } else {
          clonedProperty = propertyValue;
        }
      }
      clone[property] = clonedProperty;
    }.bind(this));
  };

  /**
   * Delegates every property change event from the original widget to this cloned widget by calling the appropriate setter.
   * If no target is set it works only if this widget is a clone.
   */
  mirror(options, target) {
    target = target || this.cloneOf;
    if (!target) {
      throw new Error('No target for mirroring.');
    }
    this._mirror(target, options);
  };

  _mirror(clone, options) {
    var eventDelegator = arrays.find(this.eventDelegators, function(eventDelegator) {
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
    clone.one('destroy', function() {
      this._unmirror(clone);
    }.bind(this));
  };

  unmirror(target) {
    target = target || this.cloneOf;
    if (!target) {
      throw new Error('No target for unmirroring.');
    }
    this._unmirror(target);
  };

  _unmirror(target) {
    var eventDelegatorIndex = arrays.findIndex(this.eventDelegators, function(eventDelegator) {
        return eventDelegator.clone === target;
      }),
      eventDelegator = (eventDelegatorIndex > -1) ? (this.eventDelegators.splice(eventDelegatorIndex, 1)[0]) : null;
    if (!eventDelegator) {
      return;
    }
    if (eventDelegator.originalToClone) {
      eventDelegator.originalToClone.destroy();
    }
    if (eventDelegator.cloneToOriginal) {
      eventDelegator.cloneToOriginal.destroy();
    }
  };

  _onParentDestroy(event) {
    if (this.destroyed) {
      return;
    }
    // If the parent is destroyed but the widget not make sure it gets a new parent
    // This ensures the old one may be properly garbage collected
    this.setParent(this.owner);
  };

  callSetter(propertyName, value) {
    var setterFuncName = 'set' + strings.uppercaseFirstLetter(propertyName);
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
  widget(widgetId) {
    return findWidgetRec(this);

    // ------ Helper functions -----

    function findWidgetRec(widget) {
      if (widget.id === widgetId) {
        return widget;
      }
      for (var i = 0; i < widget.children.length; i++) {
        var result = findWidgetRec(widget.children[i]);
        if (result) {
          return result;
        }
      }
      return null; // not found
    }
  };

  /**
   * @returns the parent for which the given function returns true.
   */
  findParent(func) {
    var parent = this.parent;
    while (parent) {
      if (func(parent)) {
        return parent;
      }
      parent = parent.parent;
    }
    return parent;
  };

  /**
   * Tries to set the focus on the widget.
   * <p>
   * By default the focus is set on the container but this may vary from widget to widget.
   * @returns true if the element could be focused, false if not
   */
  focus() {
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this));
      return false;
    }

    return false;
    //return this.session.focusManager.requestFocus(this.getFocusableElement());
  };

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
  };

  /**
   * @returns whether the widget is the currently active element
   */
  isFocused() {
    return this.rendered && scout.isActiveElement(this.getFocusableElement());
  };

  /**
   * @return true if the element is focusable, false if not.
   */
  isFocusable() {
    if (!this.rendered || !this.visible) {
      return false;
    }
    var elem = this.getFocusableElement();
    if (elem) {
      return $.ensure(elem).is(':focusable');
    }
    return false;
  };

  /**
   * This method returns the HtmlElement to be used when {@link #focus()} is called.
   * It can be overridden, in case the widget needs to return something other than this.$container[0].
   */
  getFocusableElement() {
    if (this.rendered && this.$container) {
      return this.$container[0];
    }
    return null;
  };

  _installScrollbars(options) {
    var $scrollable = this.get$Scrollable();
    if (!$scrollable) {
      throw new Error('Scrollable is not defined, cannot install scrollbars');
    }
    if ($scrollable.data('scrollable')) {
      // already installed
      return;
    }
    /*options = options || {};
    var defaults = {
        parent: this
    };
    options = $.extend({}, defaults, options);
    scout.scrollbars.install($scrollable, options);*/
    $scrollable.on('scroll', this._scrollHandler);
  };

  _uninstallScrollbars() {
    var $scrollable = this.get$Scrollable();
    if (!$scrollable || !$scrollable.data('scrollable')) {
      return;
    }
    //scout.scrollbars.uninstall($scrollable, this.session);
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
  };

  _onScroll() {
    var $scrollable = this.get$Scrollable();
    this.scrollTop = $scrollable[0].scrollTop;
    this.scrollLeft = $scrollable[0].scrollLeft;
  };

  setScrollTop(scrollTop) {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().setScrollTop(scrollTop);
      return;
    }
    if (this.scrollTop === scrollTop) {
      return;
    }
    this.scrollTop = scrollTop;
    if (this.rendered) {
      this._renderScrollTop();
    }
  };

  _renderScrollTop() {
    var $scrollable = this.get$Scrollable();
    if (!$scrollable || this.scrollTop === null) {
      // Don't do anything for non scrollable elements. Also, reading $scrollable[0].scrollTop must not be done while rendering because it would provoke a reflow
      return;
    }
    if (this.rendering || (this.htmlComp && !this.htmlComp.layouted && !this.htmlComp.layouting)) {
      // If the widget is not layouted yet (which is always true while rendering), the scroll position cannot be updated -> do it after the layout
      // If scroll top is set while layouting, layout obviously wants to set it -> do it
      this.session.layoutValidator.schedulePostValidateFunction(this._renderScrollTop.bind(this));
      return;
    }
    // scout.scrollbars.scrollTop($scrollable, this.scrollTop);
  };

  setScrollLeft(scrollLeft) {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().setScrollLeft(scrollLeft);
      return;
    }
    if (this.scrollLeft === scrollLeft) {
      return;
    }
    this.scrollLeft = scrollLeft;
    if (this.rendered) {
      this._renderScrollLeft();
    }
  };

  _renderScrollLeft() {
    var $scrollable = this.get$Scrollable();
    if (!$scrollable || this.scrollLeft === null) {
      // Don't do anything for non scrollable elements. Also, reading $scrollable[0].scrollLeft must not be done while rendering because it would provoke a reflow
      return;
    }
    if (this.rendering || (this.htmlComp && !this.htmlComp.layouted && !this.htmlComp.layouting)) {
      // If the widget is not layouted yet (which is always true while rendering), the scroll position cannot be updated -> do it after the layout
      // If scroll left is set while layouting, layout obviously wants to set it -> do it
      this.session.layoutValidator.schedulePostValidateFunction(this._renderScrollLeft.bind(this));
      return;
    }
    // scout.scrollbars.scrollLeft($scrollable, this.scrollLeft);
  };

  /**
   * Returns the jQuery element which is supposed to be scrollable. This element will be used by the scroll functions like {@link #_installScrollbars}, {@link #setScrollTop}, {@link #setScrollLeft}, {@link #scrollToBottom} etc..
   * The element won't be used unless {@link #_installScrollbars} is called.
   * If the widget is mainly a wrapper for a scrollable widget and does not have a scrollable element by itself, you can use @{link #getDelegateScrollable} instead.
   * @return {$}
   */
  get$Scrollable() {
    return this.$container;
  };

  getDelegateScrollable() {
    return null;
  };

  scrollToTop() {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().scrollToTop();
      return;
    }
    var $scrollable = this.get$Scrollable();
    if (!$scrollable) {
      return;
    }
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.scrollToTop.bind(this));
      return;
    }
    // scout.scrollbars.scrollTop($scrollable, 0);
  };

  scrollToBottom() {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().scrollToBottom();
      return;
    }
    var $scrollable = this.get$Scrollable();
    if (!$scrollable) {
      return;
    }
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.scrollToBottom.bind(this));
      return;
    }
    // scout.scrollbars.scrollToBottom($scrollable);
  };

  /**
   * Brings the widget into view by scrolling the first scrollable parent.
   */
  reveal() {
    if (!this.rendered) {
      return;
    }
    /*var $scrollParent = this.$container.scrollParent();
    if ($scrollParent.length === 0) {
        // No scrollable parent found -> scrolling is not possible
        return;
    }
    scout.scrollbars.scrollTo($scrollParent, this.$container);*/
  };

  /**
   * Visits every child of this widget in pre-order (top-down).<br>
   * This widget itself is not visited! Only child widgets are visited recursively.
   * <p>
   * The children with a different parent are excluded.<br>
   * This makes sure the child is not visited twice if the owner and the parent are not the same
   * (in that case the widget would be in the children list of the owner and of the parent).
   */
  visitChildren(visitor) {
    this.children.forEach(function(child) {
      if (child.parent === this) {
        visitor(child);
        child.visitChildren(visitor);
      }
    }, this);
  };

  /**
   * @returns {boolean} Whether or not the widget is rendered (or rendering) and the DOM $container isAttached()
   */
  isAttachedAndRendered() {
    return (this.rendered || this.rendering) && this.$container.isAttached();
  };

  static cssClassAsArray(cssClass) {
    var cssClasses = [],
      cssClassesStr = cssClass || '';

    cssClassesStr = cssClassesStr.trim();
    if (cssClassesStr.length > 0) {
      cssClasses = cssClassesStr.split(' ');
    }
    return cssClasses;
  };

  static updateFirstLastMarker(widgets) {
    widgets.filter(function(widget, i, widgets) {
      return widget.rendered && widget.isVisible();
    }).forEach(function(widget, i, widgets) {
      widget.$container.toggleClass('first', i === 0);
      widget.$container.toggleClass('last', i === widgets.length - 1);
    });
  };
}

/**
 * Enum used to define different styles used when the field is disabled.
 */
const DisabledStyle = Object.freeze({
  DEFAULT: 0,
  READ_ONLY: 1
});
