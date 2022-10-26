/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {
  Action, AnyWidget, arrays, DeferredGlassPaneTarget, Desktop, Device, DisplayParent, EnumObject, Event, EventDelegator, EventHandler, filters, focusUtils, Form, graphics, HtmlComponent, icons, inspector, KeyStroke, KeyStrokeContext,
  LayoutData, LoadingSupport, LogicalGrid, ModelAdapter, objects, ObjectWithType, Predicate, PropertyEventEmitter, scout, scrollbars, Session, strings, texts, TreeVisitResult, WidgetEventMap, WidgetModel
} from '../index';
import * as $ from 'jquery';
import {ObjectType} from '../ObjectFactory';
import {EventMapOf, EventModel} from '../events/EventEmitter';
import {ScrollbarInstallOptions, ScrollOptions, ScrollToOptions} from '../scrollbar/scrollbars';
import {Optional, RefModel} from '../types';

export type DisabledStyle = EnumObject<typeof Widget.DisabledStyle>;
export type GlassPaneTarget = JQuery | HTMLElement | DeferredGlassPaneTarget;
export type GlassPaneContribution = (widget: Widget) => GlassPaneTarget | GlassPaneTarget[];
export type WidgetOrModel = Widget | WidgetModel;
export type TreeVisitor<T> = (element: T) => boolean | TreeVisitResult | void;

export interface CloneOptions {
  /** An array of all properties to be delegated from the original to the clone when changed on the original widget. Default is []. */
  delegatePropertiesToClone?: string[];
  /** An array of all properties to be delegated from the clone to the original when changed on the clone widget. Default is []. */
  delegatePropertiesToOriginal?: string[];
  /** An array of all properties to be excluded from delegating from the clone to the original in any cases. Default is []. */
  excludePropertiesToOriginal?: string[];
  /** An array of all events to be delegated from the clone to the original when fired on the clone widget. Default is []. */
  delegateEventsToOriginal?: string[];
  /** True to delegate all property changes from the original to the clone. Default is false. */
  delegateAllPropertiesToClone?: boolean;
  /** True to delegate all property changes from the clone to the original. Default is false. */
  delegateAllPropertiesToOriginal?: boolean;
}

interface EventDelegatorForCloning {
  clone: Widget;
  originalToClone: EventDelegator;
  cloneToOriginal: EventDelegator;
}

export default class Widget extends PropertyEventEmitter implements WidgetModel, ObjectWithType {
  declare model: WidgetModel;
  declare eventMap: WidgetEventMap;
  animateRemoval: boolean;
  animateRemovalClass: string;
  attached: boolean;
  children: Widget[];
  cloneOf: Widget;
  cssClass: string;
  destroyed: boolean;
  destroying: boolean;
  disabledStyle: DisabledStyle;
  enabled: boolean;
  enabledComputed: boolean;
  eventDelegators: EventDelegatorForCloning[];
  focused: boolean;
  htmlComp: HtmlComponent;
  id: string;
  inheritAccessibility: boolean;
  initialized: boolean;
  keyStrokeContext: KeyStrokeContext;
  loading: boolean;
  loadingSupport: LoadingSupport;
  logicalGrid: LogicalGrid;
  modelClass: string;
  classId: string;
  objectType: string;
  owner: Widget;
  parent: Widget;
  removalPending: boolean;
  removing: boolean;
  rendering: boolean;
  scrollLeft: number;
  scrollTop: number;
  session: Session;
  trackFocus: boolean;
  visible: boolean;
  modelAdapter: ModelAdapter;
  displayParent: DisplayParent;
  $container: JQuery;
  $parent: JQuery;
  protected _$lastFocusedElement: JQuery;
  protected _cloneProperties: string[];
  protected _focusInListener: (event: FocusEvent | JQuery.FocusInEvent) => void;
  protected _glassPaneContributions: GlassPaneContribution[];
  protected _parentDestroyHandler: EventHandler;
  protected _parentRemovingWhileAnimatingHandler: EventHandler;
  protected _postRenderActions: (() => void)[];
  protected _preserveOnPropertyChangeProperties: string[];
  protected _rendered: boolean;
  protected _scrollHandler: (event: JQuery.ScrollEvent<HTMLElement>) => void;
  protected _storedFocusedWidget: Widget;
  protected _widgetProperties: string[];

  constructor() {
    super();

    this.id = null;
    this.objectType = null;
    this.session = null;

    // Inspector infos (are only available for remote widgets)
    this.modelClass = null;
    this.classId = null;

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

    this.$parent = null;
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
  } as const;

  /**
   * Initializes the widget instance. All properties of the model parameter (object) are set as properties on the widget instance.
   * Calls {@link Widget#_init} and triggers an <em>init</em> event when initialization has been completed.
   */
  init(model: WidgetModel) {
    if (model.loadJsonModel !== false) {
      let staticModel = this._jsonModel();
      if (staticModel) {
        model = $.extend({}, staticModel, model);
      }
    }
    model = model || {} as WidgetModel;
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
  protected _prepareModel(model: WidgetModel) {
    return model;
  }

  /**
   * Initializes the widget instance. All properties of the model parameter (object) are set as properties on the widget instance.
   * Override this function to initialize widget specific properties in sub-classes.
   */
  protected _init(model: WidgetModel) {
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
  protected _initProperty(propertyName: string, value: any) {
    this[propertyName] = value;
  }

  /**
   * Default implementation simply returns undefined. A Subclass
   * may override this method to load or extend a JSON model with models.getModel or models.extend.
   */
  protected _jsonModel(): RefModel<WidgetModel> {
    return null;
  }

  protected _createChildren(models: WidgetOrModel): Widget;
  protected _createChildren(models: WidgetOrModel[]): Widget[];
  protected _createChildren(models: WidgetOrModel | WidgetOrModel[]): Widget | Widget[];
  /**
   * Creates the widgets using the given models, or returns the widgets if the given models already are widgets.
   * @returns an array of created widgets if models was an array. Or the created widget if models is not an array.
   */
  protected _createChildren(models: WidgetOrModel | WidgetOrModel[]): Widget | Widget[] {
    if (!models) {
      return null;
    }

    if (!Array.isArray(models)) {
      return this._createChild(models);
    }

    let widgets = [];
    models.forEach((model, i) => {
      widgets[i] = this._createChild(model);
    });
    return widgets;
  }

  /**
   * Calls {@link scout.create} for the given model, or if model is already a Widget simply returns the widget.
   */
  protected _createChild(model: WidgetModel | Widget | string): Widget {
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
    return scout.create(model as WidgetModel & { objectType: ObjectType<Widget> });
  }

  protected _initKeyStrokeContext() {
    if (!this.keyStrokeContext) {
      return;
    }
    this.keyStrokeContext.$scopeTarget = () => this.$container;
    this.keyStrokeContext.$bindTarget = () => this.$container;
  }

  /**
   * Destroys the widget including all owned children.<br>
   * After destroying, the widget should and cannot be used anymore. Every attempt to render the widget will result in a 'Widget is destroyed' error.
   * <p>
   * While destroying, the widget will remove itself and its children from the DOM by calling {@link remove}.
   * After removing, {@link _destroy} is called which can be used to remove listeners and to do other cleanup tasks.
   * Finally, the widget detaches itself from its parent and owner, sets the property {@link destroyed} to true and triggers a 'destroy' event.
   * <p>
   * <b>Notes</b>
   * <ul>
   *   <li>Children that have a different owner won't be destroyed, just removed.</li>
   *   <li>The function does nothing if the widget is already destroyed.</li>
   *   <li>If a remove animation is running or pending, the destroying will be delayed until the removal is done.</li>
   * </ul>
   */
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
    // noinspection JSVoidFunctionReturnValueUsed Obviously an IntelliJ bug, it assumes reverse is from Animation rather than from Array
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
   * The default implementation does nothing.
   */
  protected _destroy() {
    // NOP
  }

  protected _destroyChildren(widgets: Widget[] | Widget) {
    if (!widgets) {
      return;
    }

    widgets = arrays.ensure(widgets);
    widgets.forEach(widget => this._destroyChild(widget));
  }

  protected _destroyChild(child: Widget) {
    if (child.owner !== this) {
      return;
    }
    child.destroy();
  }

  /**
   * Creates the UI by creating HTML elements and appending them to the DOM.
   * <p>
   * The actual rendering happens in the methods {@link _render}, which appends the main container to the parent element, and {@link _renderProperties}, which calls the methods for each property that needs to be rendered.
   * After the rendering, the created {@link this.$container} will be linked with the widget, so the widget can be found by using {@link scout.widget}.
   * Finally, the widget sets the property {@link rendered} to true and triggers a 'render' event.
   *
   * @param $parent jQuery element which is used as {@link this.$parent} when rendering this widget.
   * It will be put onto the widget and is therefore accessible as {@link this.$parent} in the {@link _render} method.
   * If not specified, the {@link this.$container} of the parent is used.
   */
  render($parent?: JQuery) {
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
   * Creates the UI by creating HTML elements and appending them to the DOM.
   * <p>
   * A typical widget creates exactly one container element and stores it to {@link this.$container}.
   * If it needs JS based layouting, it creates a {@link HtmlComponent} for that container and stores it to {@link this.htmlComp}.
   * <p>
   * The rendering of individual properties should be done in the corresponding render methods of the properties, called by {@link _renderProperties} instead of doing it here.
   * This has the advantage that the render methods can also be called on property changes, allowing individual widget parts to be dynamically re-rendered.
   * <p>
   * The default implementation does nothing.
   */
  protected _render() {
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
   * @returns true if the widget is rendered and not being removed by an animation
   *
   * @see isRemovalPending
   */
  get rendered(): boolean {
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
  protected _renderProperties() {
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
  protected _postRender() {
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
   * Instead, it waits for the remove animation to complete so it's content is still visible while the animation runs.
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
   * <p>
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
  protected _isRemovalPrevented(): boolean {
    return this.isRemovalPending();
  }

  /**
   * @deprecated use isRemovalPending instead. Will be removed with 23.0
   */
  protected _isRemovalPending(): boolean {
    return this.isRemovalPending();
  }

  /**
   * Returns true if the removal of this or an ancestor widget is pending. Checking the ancestor is omitted if the parent is being removed.
   * This may be used to prevent a removal if an ancestor will be removed (e.g by an animation)
   */
  isRemovalPending(): boolean {
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

  protected _removeInternal() {
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
    // noinspection JSVoidFunctionReturnValueUsed Obviously an IntelliJ bug, it assumes reverse is from Animation rather than from Array
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
  protected _removeAnimated() {
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
    // Don't execute immediately to make sure nothing interferes with the animation (e.g. layouting) which could make it lag
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

  protected _animateRemovalWhileRemovingParent(): boolean {
    // By default, remove animation is prevented when parent is being removed
    return false;
  }

  protected _onParentRemovingWhileAnimating() {
    this._removeInternal();
  }

  protected _renderInspectorInfo() {
    if (!this.session.inspector) {
      return;
    }
    inspector.applyInfo(this);
  }

  /**
   * Links $container with the widget.
   */
  protected _linkWithDOM() {
    if (this.$container) {
      this.$container.data('widget', this);
    }
  }

  /**
   * Called right before _remove is called.<br>
   * Default calls {@link LayoutValidator.cleanupInvalidComponents} to make sure that child components are removed from the invalid components list.
   * Also uninstalls key stroke context, loading support and scrollbars.
   */
  protected _cleanup() {
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

  protected _remove() {
    if (this.$container) {
      this.$container.remove();
      this.$container = null;
    }
  }

  setOwner(owner: Widget) {
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

  setParent(parent: Widget) {
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

  protected _addChild(child: Widget) {
    $.log.isTraceEnabled() && $.log.trace('addChild(' + child + ') to ' + this);
    arrays.pushSet(this.children, child);
  }

  protected _removeChild(child: Widget) {
    $.log.isTraceEnabled() && $.log.trace('removeChild(' + child + ') from ' + this);
    arrays.remove(this.children, child);
  }

  /**
   * @returns a list of all ancestors
   */
  ancestors(): Widget[] {
    let ancestors = [];
    let parent = this.parent;
    while (parent) {
      ancestors.push(parent);
      parent = parent.parent;
    }
    return ancestors;
  }

  /**
   * @returns true if the given widget is the same as this or a descendant
   */
  isOrHas(widget: Widget): boolean {
    if (widget === this) {
      return true;
    }
    return this.has(widget);
  }

  /**
   * Checks if the current widget contains the given widget.<br>
   * For a good performance, it visits the ancestors of the given widget rather than the descendants of the current widget.
   *
   * @returns true if the given widget is a descendant
   */
  has(widget: Widget): boolean {
    while (widget) {
      if (widget.parent === this) {
        return true;
      }
      widget = widget.parent;
    }
    return false;
  }

  /**
   * @returns the form the widget belongs to (returns the first parent which is a {@link Form}).
   */
  getForm(): Form {
    return Form.findForm(this);
  }

  /**
   * @returns the first form which is not an inner form of a wrapped form field
   */
  findNonWrappedForm(): Form {
    return Form.findNonWrappedForm(this);
  }

  /**
   * @returns the desktop linked to the current session.
   * If the desktop is still initializing, it might not be available yet, in that case, it searches the parent hierarchy for it.
   */
  findDesktop(): Desktop {
    if (this.session.desktop) {
      return this.session.desktop;
    }
    return this.findParent(parent => parent instanceof Desktop) as Desktop;
  }

  /**
   * Changes the enabled property of this form field to the given value.
   *
   * @param enabled
   *          Required. The new enabled value
   * @param updateParents
   *          If true, the enabled property of all parent form fields are
   *          updated to same value as well. Default is false.
   * @param updateChildren
   *          If true, the enabled property of all child form fields (recursive)
   *          are updated to same value as well. Default is false.
   */
  setEnabled(enabled: boolean, updateParents?: boolean, updateChildren?: boolean) {
    this.setProperty('enabled', enabled);

    if (enabled && updateParents && this.parent) {
      this.parent.setEnabled(true, true, false);
    }

    if (updateChildren) {
      this.visitChildren(widget => {
        widget.setEnabled(enabled);
      });
    }
  }

  protected _setEnabled(enabled: boolean) {
    this._setProperty('enabled', enabled);
    if (this.initialized) {
      this.recomputeEnabled();
    }
  }

  recomputeEnabled(parentEnabled?: boolean) {
    if (parentEnabled === undefined) {
      parentEnabled = true;
      if (this.parent && this.parent.initialized && this.parent.enabledComputed !== undefined) {
        parentEnabled = this.parent.enabledComputed;
      }
    }

    let enabledComputed = this._computeEnabled(this.inheritAccessibility, parentEnabled);
    this._updateEnabledComputed(enabledComputed);
  }

  protected _updateEnabledComputed(enabledComputed: boolean, enabledComputedForChildren?: boolean) {
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

  protected _childrenForEnabledComputed(): Widget[] {
    return this.children;
  }

  protected _computeEnabled(inheritAccessibility: boolean, parentEnabled: boolean) {
    return this.enabled && (inheritAccessibility ? parentEnabled : true);
  }

  protected _renderEnabled() {
    if (!this.$container) {
      return;
    }
    this.$container.setEnabled(this.enabledComputed);
    this._renderDisabledStyle();
  }

  setInheritAccessibility(inheritAccessibility: boolean) {
    this.setProperty('inheritAccessibility', inheritAccessibility);
  }

  protected _setInheritAccessibility(inheritAccessibility: boolean) {
    this._setProperty('inheritAccessibility', inheritAccessibility);
    if (this.initialized) {
      this.recomputeEnabled();
    }
  }

  setDisabledStyle(disabledStyle: DisabledStyle) {
    this.setProperty('disabledStyle', disabledStyle);

    this.children.forEach(child => {
      child.setDisabledStyle(disabledStyle);
    });
  }

  protected _renderDisabledStyle() {
    this._renderDisabledStyleInternal(this.$container);
  }

  protected _renderDisabledStyleInternal($element: JQuery<HTMLElement>) {
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
   * @param visible true, to make the widget visible, false to hide it.
   */
  setVisible(visible: boolean) {
    this.setProperty('visible', visible);
  }

  /**
   * @returns whether the widget is visible or not. May depend on other conditions than the visible property only
   */
  isVisible(): boolean {
    return this.visible;
  }

  protected _renderVisible() {
    if (!this.$container) {
      return;
    }
    this.$container.setVisible(this.isVisible());
    this.invalidateParentLogicalGrid();
  }

  /**
   * @returns true if every parent within the hierarchy is visible.
   */
  isEveryParentVisible(): boolean {
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
   *  Use {@link focus} to set the focus to the widget.
   */
  setFocused(focused: boolean) {
    this.setProperty('focused', focused);
  }

  protected _renderFocused() {
    if (this.$container) {
      this.$container.toggleClass('focused', this.focused);
    }
  }

  protected _setCssClass(cssClass: string) {
    if (this.rendered) {
      this._removeCssClass();
    }
    this._setProperty('cssClass', cssClass);
  }

  protected _removeCssClass() {
    if (!this.$container) {
      return;
    }
    this.$container.removeClass(this.cssClass);
  }

  protected _renderCssClass() {
    if (!this.$container) {
      return;
    }
    this.$container.addClass(this.cssClass);
    if (this.htmlComp) {
      // Replacing css classes may enlarge or shrink the widget (e.g. setting the font weight to bold makes the text bigger) -> invalidate layout
      this.invalidateLayoutTree();
    }
  }

  /**
   * @param cssClass may contain multiple css classes separated by space.
   */
  setCssClass(cssClass: string) {
    this.setProperty('cssClass', cssClass);
  }

  /**
   * @param cssClass may contain multiple css classes separated by space.
   */
  addCssClass(cssClass: string) {
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

  /**
   * @param cssClass may contain multiple css classes separated by space.
   */
  removeCssClass(cssClass: string) {
    let cssClasses = this.cssClassAsArray();
    let cssClassesToRemove = Widget.cssClassAsArray(cssClass);
    if (arrays.removeAll(cssClasses, cssClassesToRemove)) {
      this.setProperty('cssClass', arrays.format(cssClasses, ' '));
    }
  }

  /**
   * @param cssClass may contain multiple css classes separated by space.
   */
  toggleCssClass(cssClass: string, condition: boolean) {
    if (condition) {
      this.addCssClass(cssClass);
    } else {
      this.removeCssClass(cssClass);
    }
  }

  cssClassAsArray(): string[] {
    return Widget.cssClassAsArray(this.cssClass);
  }

  /**
   * Creates nothing by default. If a widget needs loading support, override this method and return a loading support.
   */
  protected _createLoadingSupport(): LoadingSupport {
    return null;
  }

  setLoading(loading: boolean) {
    this.setProperty('loading', loading);
  }

  isLoading(): boolean {
    return this.loading;
  }

  protected _renderLoading() {
    if (!this.loadingSupport) {
      return;
    }
    this.loadingSupport.renderLoading();
  }

  // --- Layouting / HtmlComponent methods ---

  /**
   * Delegates the pack request to {@link HtmlComponent#pack}.
   */
  pack() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.pack();
  }

  /**
   * Delegates the invalidateLayout request to {@link HtmlComponent#invalidateLayout}.
   */
  invalidateLayout() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.invalidateLayout();
  }

  /**
   * Delegates the validateLayout request to {@link HtmlComponent#validateLayout}.
   */
  validateLayout() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.validateLayout();
  }

  /**
   * Delegates the revalidateLayout request to {@link HtmlComponent#revalidateLayout}.
   */
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
   * Delegates the invalidation request to {@link HtmlComponent#invalidateLayoutTree}.
   * @param invalidateParents Default is true
   */
  invalidateLayoutTree(invalidateParents?) {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.invalidateLayoutTree(invalidateParents);
  }

  /**
   * Delegates the invalidation request to {@link HtmlComponent#validateLayoutTree}.
   */
  validateLayoutTree() {
    if (!this.rendered || this.removing) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.validateLayoutTree();
  }

  /**
   * Delegates the invalidation request to {@link HtmlComponent#revalidateLayoutTree}.
   * @param invalidateParents Default is true
   */
  revalidateLayoutTree(invalidateParents?) {
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
   * Example: The parent container uses a {@link LogicalGridLayout} to layout its children. Every child has a {@link LogicalGridData} to tell the layout how this specific child should be layouted.
   * The parent may have a {@link LogicalGridLayoutConfig} to specify constraints which affect either only the container or every child in the container.
   */
  setLayoutData(layoutData: LayoutData) {
    if (!this.rendered) {
      return;
    }
    if (!this.htmlComp) {
      throw new Error('Function expects a htmlComp property');
    }
    this.htmlComp.layoutData = layoutData;
  }

  /**
   * If the widget uses a {@link LogicalGridLayout}, the grid may be validated using this method.
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
   * @param invalidateLayout true, to invalidate the layout afterwards using {@link invalidateLayoutTree}, false if not. Default is true.
   */
  invalidateLogicalGrid(invalidateLayout?: boolean) {
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
   * @param invalidateLayout true, to invalidate the layout of the parent of {@link this.htmlComp}, false if not. Default is true.
   */
  invalidateParentLogicalGrid(invalidateLayout?: boolean) {
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

  revalidateLogicalGrid(invalidateLayout?: boolean) {
    this.invalidateLogicalGrid(invalidateLayout);
    this.validateLogicalGrid();
  }

  /**
   * @param logicalGrid an instance of {@link LogicalGrid} or a string representing the objectType of a logical grid.
   */
  setLogicalGrid(logicalGrid: LogicalGrid | string) {
    this.setProperty('logicalGrid', logicalGrid);
  }

  protected _setLogicalGrid(logicalGrid: LogicalGrid | string) {
    if (typeof logicalGrid === 'string') {
      logicalGrid = scout.create(logicalGrid);
    }
    this._setProperty('logicalGrid', logicalGrid);
    this.invalidateLogicalGrid();
  }

  /**
   * @returns the entry-point for this Widget or its parent. If the widget is part of the main-window it returns {@link this.session.$entryPoint},
   * for popup-window this function will return the body of the document in the popup window.
   */
  entryPoint(): JQuery {
    let $element = scout.nvl(this.$container, this.parent.$container);
    if (!$element || !$element.length) {
      throw new Error('Cannot resolve entryPoint, $element.length is 0 or undefined');
    }
    return $element.entryPoint();
  }

  window<T extends boolean>(domElement?: T): T extends true ? Window : JQuery<Window> {
    let $el = this.$container || this.$parent;
    // @ts-ignore $() is not of type JQuery<Document>
    return $el ? $el.window(domElement) : domElement ? null : $();
  }

  document<T extends boolean>(domElement?: T): T extends true ? Document : JQuery<Document> {
    let $el = this.$container || this.$parent;
    // @ts-ignore $() is not of type JQuery<Document>
    return $el ? $el.document(domElement) : domElement ? null : $();
  }

  /**
   * This method attaches the detached {@link this.$container} to the DOM.
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
   * Override this method to do something when the widget is attached again. Typically,
   * you will append {@link this.$container} to {@link this.$parent}.
   */
  protected _attach() {
    // NOP
  }

  /**
   * Override this method to do something after this widget is attached.
   * This function is not called on any child of the attached widget.
   */
  protected _postAttach() {
    // NOP
  }

  protected _triggerChildrenOnAttach(parent: Widget) {
    this.children.forEach(child => {
      child._onAttach();
      child._triggerChildrenOnAttach(parent);
    });
  }

  /**
   * Override this method to do something after this widget or any parent of it is attached.
   * This function is called regardless of whether the widget is rendered or not.
   */
  protected _onAttach() {
    if (this.rendered) {
      this._renderOnAttach();
    }
  }

  /**
   * Override this method to do something after this widget or any parent of it is attached.
   * This function is only called when this widget is rendered.
   */
  protected _renderOnAttach() {
    this._renderScrollTop();
    this._renderScrollLeft();
  }

  /**
   * Detaches the element and all its children from the DOM.<br>
   * Compared to {@link remove}, the state of the HTML elements are preserved, so they can be attached again without the need to render them again from scratch.
   * {@link attach}/{@link detach} is faster in general than {@link render}/{@link remove}, but it is much more error prone and should therefore only be used very carefully. Rule of thumb: Don't use it, use {@link remove} instead.<br>
   * The main problem with attach/detach is that a widget can change its model anytime. If this happens for a removed widget, only the model will change, and when rendered again, the recent model is used to create the HTML elements.
   * If the same happens when a widget is detached, the widget is still considered rendered and the model applied to the currently detached elements.
   * This may or may not work because a detached element for example does not have a size or a position.
   *
   * @see _beforeDetach
   * @see _onDetach
   * @see _renderOnDetach
   * @see _detach
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
    this._triggerChildrenOnDetach();
    this._detach();
    this.attached = false;
  }

  /**
   * This function is called before a widget gets detached. The function is only called on the detached widget and NOT on
   * any of its children.
   */
  protected _beforeDetach() {
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

  protected _triggerChildrenOnDetach() {
    this.children.forEach(child => {
      child._onDetach();
      child._triggerChildrenOnDetach();
    });
  }

  /**
   * This function is called before a widget or any of its parent getting detached.
   * This function is thought to be overridden.
   */
  protected _onDetach() {
    if (this.rendered) {
      this._renderOnDetach();
    }
  }

  protected _renderOnDetach() {
    // NOP
  }

  /**
   * Override this method to do something when the widget is detached.
   * Typically you will call 'this.$container.detach()'. The default implementation does nothing.
   */
  protected _detach() {
    // NOP
  }

  protected _uninstallFocusContext() {
    // NOP
  }

  protected _installFocusContext() {
    // NOP
  }

  /**
   * Does nothing by default. If a widget needs keystroke support override this method and return a keystroke context, e.g. the default KeyStrokeContext.
   */
  protected _createKeyStrokeContext(): KeyStrokeContext {
    return null;
  }

  updateKeyStrokes(newKeyStrokes: KeyStroke | KeyStroke[] | Action | Action[], oldKeyStrokes?: KeyStroke | KeyStroke[] | Action | Action[]) {
    this.unregisterKeyStrokes(oldKeyStrokes);
    this.registerKeyStrokes(newKeyStrokes);
  }

  registerKeyStrokes(keyStrokes: KeyStroke | KeyStroke[] | Action | Action[]) {
    this.keyStrokeContext.registerKeyStrokes(keyStrokes);
  }

  unregisterKeyStrokes(keyStrokes: KeyStroke | KeyStroke[] | Action | Action[]) {
    this.keyStrokeContext.unregisterKeyStrokes(keyStrokes);
  }

  /**
   * Sets a new value for a specific property. If the new value is the same value as the old one, nothing is performed.
   * Otherwise, the following phases are executed:
   *
   * 1. Preparation: If the property is a widget property, several actions are performed in \_prepareWidgetProperty().
   * 2. DOM removal: If the property is a widget property and the widget is rendered, the changed widget(s) are removed unless the property should not be preserved (see {@link _preserveOnPropertyChangeProperties}).
   *    If there is a custom remove function (e.g. \_removeXY where XY is the property name), it will be called instead of removing the widgets directly.
   * 3. Model update: If there is a custom set function (e.g. \_setXY where XY is the property name), it will be called. Otherwise the default set function {@link _setProperty} is called.
   * 4. DOM rendering: If the widget is rendered and there is a custom render function (e.g. \_renderXY where XY is the property name), it will be called. Otherwise nothing happens.
   * @returns true, if the property was changed, false if not.
   */
  override setProperty(propertyName, value): boolean {
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

  protected _prepareProperty(propertyName: string, value) {
    if (!this.isWidgetProperty(propertyName)) {
      return value;
    }
    return this._prepareWidgetProperty(propertyName, value);
  }

  protected _prepareWidgetProperty(propertyName: string, models: Widget | WidgetModel): Widget;
  protected _prepareWidgetProperty(propertyName: string, models: Widget[] | WidgetModel[]): Widget[];
  protected _prepareWidgetProperty(propertyName: string, models: WidgetOrModel | WidgetOrModel[]): Widget | Widget[] {
    // Create new child widget(s)
    let widgets = this._createChildren(models);

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
  protected _callRemoveProperty(propertyName: string) {
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
  protected _internalRemoveWidgets(widgets: Widget[]) {
    widgets = arrays.ensure(widgets);
    widgets.forEach(widget => {
      widget.remove();
    });
  }

  protected _callRenderProperty(propertyName: string) {
    let renderFuncName = '_render' + strings.toUpperCaseFirstLetter(propertyName);
    if (!this[renderFuncName]) {
      return;
    }
    this[renderFuncName]();
  }

  /**
   * Sets this widget as parent of the given widget(s).
   *
   * @param widgets may be a widget or array of widgets
   */
  link(widgets: Widget[] | Widget) {
    if (!widgets) {
      return;
    }

    widgets = arrays.ensure(widgets);
    widgets.forEach(child => child.setParent(this));
  }

  /**
   * Method required for widgets which are supposed to be directly covered by a glasspane.<p>
   *
   * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box or file-chooser is shown with this widget as its 'displayParent'.<br>
   * If the widget is not rendered yet, a scout.DeferredGlassPaneTarget is returned.<br>
   * In both cases the method _glassPaneTargets is called which may be overridden by the actual widget.
   * @param element widget that requested a glass pane
   */
  glassPaneTargets(element?: Widget): GlassPaneTarget[] {
    let resolveGlassPanes: (element: Widget) => GlassPaneTarget[] = element => {
      // contributions
      let targets = arrays.flatMap(this._glassPaneContributions, (cont: GlassPaneContribution) => {
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
   * @param element widget that requested a glass pane
   */
  protected _glassPaneTargets(element: Widget): GlassPaneTarget[] {
    // since popups are rendered outside the DOM of the widget parent-child hierarchy, get glassPaneTargets of popups belonging to this widget separately.
    return [this.$container].concat(
      this.session.desktop.getPopupsFor(this)
        .filter(popup => !element.has(popup))
        .reduce((acc, popup) => acc.concat(popup.glassPaneTargets()), []));
  }

  addGlassPaneContribution(contribution: GlassPaneContribution) {
    this._glassPaneContributions.push(contribution);
    this.trigger('glassPaneContributionAdded', {
      contribution: contribution
    });
  }

  /**
   * @param contribution a function which returns glass pane targets (jQuery elements)
   */
  removeGlassPaneContribution(contribution: GlassPaneContribution) {
    arrays.remove(this._glassPaneContributions, contribution);
    this.trigger('glassPaneContributionRemoved', {
      contribution: contribution
    });
  }

  override toString(): string {
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
   * @param count the number of ancestors to be processed. Default is -1 which means all.
   */
  ancestorsToString(count?: number) {
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

  resolveTextKeys(properties: string[]) {
    properties.forEach(property => {
      texts.resolveTextProperty(this, property);
    });
  }

  resolveIconIds(properties: string[]) {
    properties.forEach(property => {
      icons.resolveIconProperty(this, property);
    });
  }

  resolveConsts(configs: { property: string; constType: any }[]) {
    configs.forEach(config => objects.resolveConstProperty(this, config));
  }

  /**
   * A so called widget property is a property with a widget as value incl. automatic resolution of that widget.
   * This means the property not only accepts the actual widget, but also a widget model or a widget reference (id)
   * and then either creates a new widget based on the model or resolves the id and uses the referenced widget as value.
   * Furthermore it will take care of its lifecycle which means, the widget will automatically be removed and destroyed (as long as the parent is also the owner).
   * <p>
   * If only the resolve operations without the lifecycle actions should be performed, you need to add the property to the list _preserveOnPropertyChangeProperties as well.
   */
  protected _addWidgetProperties(properties: string | string[]) {
    this._addProperties('_widgetProperties', properties);
  }

  isWidgetProperty(propertyName: string): boolean {
    return this._widgetProperties.indexOf(propertyName) > -1;
  }

  protected _addCloneProperties(properties: string | string[]) {
    this._addProperties('_cloneProperties', properties);
  }

  isCloneProperty(propertyName: string): boolean {
    return this._cloneProperties.indexOf(propertyName) > -1;
  }

  /**
   * Properties in this list won't be affected by the automatic lifecycle actions performed for regular widget properties.
   * This means, the widget won't be removed, destroyed and also not linked, which means the parent stays the same.
   * But the resolve operations are still applied, as for regular widget properties.
   * <p>
   * The typical use case for such properties is referencing another widget without taking care of that widget.
   */
  protected _addPreserveOnPropertyChangeProperties(properties: string | string[]) {
    this._addProperties('_preserveOnPropertyChangeProperties', properties);
  }

  isPreserveOnPropertyChangeProperty(propertyName: string): boolean {
    return this._preserveOnPropertyChangeProperties.indexOf(propertyName) > -1;
  }

  protected _addProperties(propertyName: string, properties: string | string[]) {
    properties = arrays.ensure(properties);
    properties.forEach(property => {
      if (this[propertyName].indexOf(property) > -1) {
        throw new Error(propertyName + ' already contains the property ' + property);
      }
      this[propertyName].push(property);
    });
  }

  protected _eachProperty(model: WidgetModel, func: (propertyName: string, value, isWidgetProperty?: boolean) => void) {
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

  protected _removeWidgetProperties(properties: string | string[]) {
    if (Array.isArray(properties)) {
      arrays.removeAll(this._widgetProperties, properties);
    } else {
      arrays.remove(this._widgetProperties, properties);
    }
  }

  /**
   * Clones the widget and mirrors the events, see {@link clone} and {@link mirror} for details.
   */
  cloneAndMirror(model: WidgetModel) {
    return this.clone(model, {
      delegateAllPropertiesToClone: true
    });
  }

  /**
   * @returns the original widget from which this one was cloned. If it is not a clone, itself is returned.
   */
  original(): AnyWidget {
    let original: Widget = this;
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
   * Therefore, this model may be used to override the cloned properties or to add additional properties.
   * @param options Options passed to the mirror function.
   */
  clone(model: WidgetModel, options?: CloneOptions): this {
    let clone, cloneModel;
    // @ts-ignore
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

  protected _deepCloneProperties(clone: Widget, properties: string | string[], options: CloneOptions) {
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
   */
  mirror(options?: CloneOptions, target?: Widget) {
    target = target || this.cloneOf;
    if (!target) {
      throw new Error('No target for mirroring.');
    }
    this._mirror(target, options);
  }

  protected _mirror(clone: Widget, options: CloneOptions) {
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

  unmirror(target: Widget) {
    target = target || this.cloneOf;
    if (!target) {
      throw new Error('No target for unmirroring.');
    }
    this._unmirror(target);
  }

  protected _unmirror(target: Widget) {
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

  protected _onParentDestroy() {
    if (this.destroyed) {
      return;
    }
    // If the parent is destroyed but the widget not make sure it gets a new parent
    // This ensures the old one may be properly garbage collected
    this.setParent(this.owner);
  }

  widget<T extends Widget>(widgetId: string, type: new() => T): T;
  widget(widgetId: string): AnyWidget;

  /**
   * Traverses the object-tree (children) of this widget and searches for a widget with the given ID.
   * Returns the widget with the requested ID or null if no widget has been found.
   * @param widgetId the id of the widget to look for
   * @param type the type of the widget to look for. When specified, the return value will be cast to that type. This parameter has no effect at runtime.
   */
  widget<T extends Widget>(widgetId: string, type?: new() => T): T {
    if (predicate(this)) {
      // @ts-ignore
      return this;
    }
    return this.findChild(predicate) as T;

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
   * @param widgetId
   *          The ID of the widget to find.
   * @param deep
   *          If false, only this widget and the next level are checked. This is the default.
   *          If true, the entire tree is traversed.
   * @returns the first found widget, or null if no widget was found.
   */
  nearestWidget(widgetId: string, deep?: boolean): Widget {
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
   * @returns the first parent for which the given function returns true.
   */
  findParent(predicate: Predicate<Widget>): AnyWidget {
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
   * @returns the first child for which the given function returns true.
   */
  findChild(predicate): AnyWidget {
    let foundChild = null;
    this.visitChildren(child => {
      if (predicate(child)) {
        foundChild = child;
        return true;
      }
    });
    return foundChild;
  }

  setTrackFocus(trackFocus: boolean) {
    this.setProperty('trackFocus', trackFocus);
  }

  protected _renderTrackFocus() {
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
  protected _onFocusIn(event: FocusEvent | JQuery.FocusInEvent) {
    // do not track focus events during rendering to avoid initial focus to be restored.
    if (this.rendering) {
      return;
    }
    let $target = $(event.target);
    // @ts-ignore
    if (this.$container.has($target)) { // FIXME TS does this work? according to signature has accepts only elements, not JQuery elements
      this._$lastFocusedElement = $target;
    }
  }

  /**
   * Tries to set the focus on the widget.
   * <p>
   * By default the focus is set on the container but this may vary from widget to widget.
   *
   * @param options.preventScroll prevents scrolling to new focused element (defaults to false)
   * @returns true if the element could be focused, false if not
   */
  focus(options?: { preventScroll?: boolean }): boolean {
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this, options));
      return false;
    }
    return this.session.focusManager.requestFocus(this.getFocusableElement(), null, options);
  }

  /**
   * Calls {@link focus()} and prevents the default behavior of the event if the focusing was successful.
   */
  focusAndPreventDefault(event: JQuery.Event): boolean {
    if (this.focus()) {
      // Preventing blur is bad for touch devices because it prevents that the keyboard can close.
      // In that case focus() will return false because focus manager is disabled.
      event.preventDefault();
      return true;
    }
    return false;
  }

  /**
   * @returns whether the widget is the currently active element
   */
  isFocused(): boolean {
    return this.rendered && focusUtils.isActiveElement(this.getFocusableElement());
  }

  /**
   * @param checkTabbable if true, the widget has to be tabbable, not only focusable. Default is true.
   * @returns true if the element is focusable (and tabbable, unless checkTabbable is set to false), false if not.
   */
  isFocusable(checkTabbable?: boolean): boolean {
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
   * This method returns the {@link HTMLElement} to be used when {@link focus} is called.
   * It can be overridden, in case the widget needs to return something other than this.$container[0].
   */
  getFocusableElement(): HTMLElement | JQuery {
    if (this.rendered && this.$container) {
      return this.$container[0];
    }
    return null;
  }

  protected _installScrollbars(options?: Optional<ScrollbarInstallOptions, 'parent'>) {
    let $scrollable = this.get$Scrollable();
    if (!$scrollable) {
      throw new Error('Scrollable is not defined, cannot install scrollbars');
    }
    if ($scrollable.data('scrollable')) {
      // already installed
      return;
    }
    options = options || {parent: this};
    let defaults = {
      parent: this
    };
    let opts = $.extend({}, defaults, options);
    scrollbars.install($scrollable, opts);
    $scrollable.on('scroll', this._scrollHandler);
  }

  protected _uninstallScrollbars() {
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

  protected _onScroll(event: JQuery.ScrollEvent) {
    let $scrollable = this.get$Scrollable();
    this._setProperty('scrollTop', $scrollable[0].scrollTop);
    this._setProperty('scrollLeft', $scrollable[0].scrollLeft);
  }

  setScrollTop(scrollTop: number) {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().setScrollTop(scrollTop);
      return;
    }
    this.setProperty('scrollTop', scrollTop);
  }

  protected _renderScrollTop() {
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

  setScrollLeft(scrollLeft: number) {
    if (this.getDelegateScrollable()) {
      this.getDelegateScrollable().setScrollLeft(scrollLeft);
      return;
    }
    this.setProperty('scrollLeft', scrollLeft);
  }

  protected _renderScrollLeft() {
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
   * Returns the jQuery element which is supposed to be scrollable. This element will be used by the scroll functions like {@link _installScrollbars}, {@link setScrollTop}, {@link setScrollLeft}, {@link scrollToBottom} etc..
   * The element won't be used unless {@link _installScrollbars} is called.
   * If the widget is mainly a wrapper for a scrollable widget and does not have a scrollable element by itself, you can use @{link #getDelegateScrollable} instead.
   */
  get$Scrollable(): JQuery {
    return this.$container;
  }

  hasScrollShadow(position: string): boolean { // FIXME TS define available positions
    return scrollbars.hasScrollShadow(this.get$Scrollable(), position);
  }

  /**
   * If the widget is mainly a wrapper for another widget, it is often the case that the other widget is scrollable and not the wrapper.
   * In that case implement this method and return the other widget so that the calls to the scroll functions can be delegated.
   */
  getDelegateScrollable(): Widget {
    return null;
  }

  scrollToTop(options?: ScrollOptions) {
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

  scrollToBottom(options?: ScrollOptions) {
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
   * @param options
   *          an optional options object. Short-hand version: If a string is passed instead
   *          of an object, the value is automatically converted to the option {@link ScrollToOptions.align}.
   */
  reveal(options: ScrollToOptions | string) {
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
   * @returns true if the visitor aborted the visiting, false if the visiting completed without aborting
   */
  visitChildren(visitor: TreeVisitor<Widget>): boolean | TreeVisitResult {
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
   * @returns Whether or not the widget is rendered (or rendering) and the DOM $container isAttached()
   */
  isAttachedAndRendered(): boolean {
    return (this.rendered || this.rendering) && this.$container.isAttached();
  }

  override trigger<K extends string & keyof EventMapOf<Widget>>(type: K, eventOrModel?: Event | EventModel<EventMapOf<Widget>[K]>): EventMapOf<Widget>[K] {
    return super.trigger(type, eventOrModel);
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static cssClassAsArray(cssClass: string): string[] {
    let cssClasses = [],
      cssClassesStr = cssClass || '';

    cssClassesStr = cssClassesStr.trim();
    if (cssClassesStr.length > 0) {
      cssClasses = cssClassesStr.split(' ');
    }
    return cssClasses;
  }
}
