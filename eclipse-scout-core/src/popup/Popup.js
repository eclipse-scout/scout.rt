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
import {CloseKeyStroke, DialogLayout, Dimension, Event, FocusRule, GlassPaneRenderer, graphics, HtmlComponent, Insets, KeyStrokeContext, Point, PopupLayout, Rectangle, scout, scrollbars, strings, Widget, widgets} from '../index';
import $ from 'jquery';

export default class Popup extends Widget {

  constructor() {
    super();

    this._documentMouseDownHandler = null;
    this._anchorScrollHandler = null;
    this._anchorLocationChangeHandler = null;
    this._popupOpenHandler = null;
    this._glassPaneRenderer = null;
    this.anchorBounds = null;
    this.animateOpening = false;
    this.animateResize = false;
    this.anchor = null;
    this.$anchor = null;
    this.windowPaddingX = 10;
    this.windowPaddingY = 5;
    this.withGlassPane = false;
    this._withGlassPane = null;
    this.withFocusContext = true;
    this.initialFocus = () => FocusRule.AUTO;
    this.focusableContainer = false;

    // The alignment defines how the popup is positioned around the anchor.
    // If there is no anchor or anchor bounds the alignment has no effect.
    this.horizontalAlignment = Popup.Alignment.LEFTEDGE;
    this.verticalAlignment = Popup.Alignment.BOTTOM;

    // Gives the current alignment after applying horizontal and vertical switch options
    this.calculatedHorizontalAlignment = this.horizontalAlignment;
    this.calculatedVerticalAlignment = this.verticalAlignment;

    // If switch is enabled, the alignment will be changed if the popup overlaps a window border.
    this.horizontalSwitch = false;
    this.verticalSwitch = true;

    // Hints for the layout to control whether the size should be adjusted if the popup does not fit into the window.
    // Before trimming is applied the popup will be switched, if the switch option is enabled.
    // If neither switch nor trim is enabled, the popup will be moved until its right border is visible.
    this.trimWidth = false;
    this.trimHeight = true;

    // Defines what should happen when the scroll parent is scrolled. It is also used if the anchor changes its location (needs to support the locationChange event)
    this.scrollType = 'remove';
    this.windowResizeType = null;

    // If true, the anchor is considered when computing the position and size of the popup
    this.boundToAnchor = true;

    // If true, an arrow is shown pointing to the anchor. If there is no anchor, no arrow will be visible.
    // Please note: some alignment combinations are not supported, which are: LEFT or RIGHT + BOTTOM or TOP
    this.withArrow = false;

    // If false, the attached mouse down handler will NOT close the popup if the anchor was clicked, the anchor is responsible to close it.
    // This is necessary because the mousedown listener is attached to the capture phase and therefore executed before any other.
    // If anchor was clicked, popup would already be closed and then opened again -> popup could never be closed by clicking the anchor
    this.closeOnAnchorMouseDown = true;
    this._closeOnAnchorMouseDown = null;

    // Defines whether the popup should be closed on a mouse click outside of the popup
    this.closeOnMouseDownOutside = true;
    this._closeOnMouseDownOutside = null;

    // Defines whether the popup should be closed whenever another popup opens.
    this.closeOnOtherPopupOpen = true;
    this._closeOnOtherPopupOpen = null;

    // Defines whether the popup should behave like a modal form. If true, the properties closeOnAnchorMouseDown, closeOnMouseDownOutside
    // and closeOnOtherPopupOpen ore overruled and set to false. The property withGlassPane is overruled too and set to true.
    this.modal = false;

    this._openLater = false;

    this.$arrow = null;
    this.$arrowOverlay = null;
    this._windowResizeHandler = this._onWindowResize.bind(this);
    this._anchorRenderHandler = this._onAnchorRender.bind(this);
    this._addWidgetProperties(['anchor']);
    this._addPreserveOnPropertyChangeProperties(['anchor']);
  }

  // Note that these strings are also used as CSS classes
  static Alignment = {
    /**
     * The entire popup is positioned horizontally left of the anchor.
     */
    LEFT: 'left',
    /**
     * With arrow: The arrow at the left edge of the popup is aligned horizontally with the center of the anchor.
     * <p>
     * Without arrow: The left edges of both the popup and the anchor are aligned horizontally.
     */
    LEFTEDGE: 'leftedge',
    /**
     * The entire popup is positioned vertically above the anchor.
     */
    TOP: 'top',
    /**
     * With arrow: The arrow at the top edge of the popup is aligned vertically with the center of the anchor.
     * <p>
     * Without arrow: The top edges of both the popup and the anchor are aligned vertically.
     */
    TOPEDGE: 'topedge',
    /**
     * The centers of both the popup and the anchor are aligned in the respective dimension.
     */
    CENTER: 'center',
    /**
     * The entire popup is positioned horizontally to the right of the anchor.
     */
    RIGHT: 'right',
    /**
     * With arrow: The arrow at the right edge of the popup is aligned horizontally with the center of the anchor.
     * <p>
     * Without arrow: The right edges of both the popup and the anchor are aligned horizontally.
     */
    RIGHTEDGE: 'rightedge',
    /**
     * The entire popup is positioned vertically below the anchor.
     */
    BOTTOM: 'bottom',
    /**
     * With arrow: The arrow at the bottom edge of the popup is aligned vertically with the center of the anchor.
     * <p>
     * Without arrow: The bottom edges of both the popup and the anchor are aligned vertically.
     */
    BOTTOMEDGE: 'bottomedge'
  };

  static SwitchRule = {};

  /**
   * @param options:
   *          initialFocus: a function that returns the element to be focused or a <code>FocusRule</code>. Default returns <code>FocusRule.AUTO</code>
   *          focusableContainer: a boolean whether or not the container of the Popup is focusable
   */
  _init(options) {
    super._init(options);

    if (options.location) {
      this.anchorBounds = new Rectangle(options.location.x, options.location.y, 0, 0);
    }
    this._setAnchor(this.anchor);
    this._setModal(this.modal);
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(this._createCloseKeyStroke());
  }

  /**
   * Override this method to provide a key stroke which closes the popup.
   * The default impl. returns a CloseKeyStroke which handles the ESC key.
   * @return KeyStroke
   */
  _createCloseKeyStroke() {
    return new CloseKeyStroke(this);
  }

  /**
   * @return {PopupLayout|AbstractLayout}
   */
  _createLayout() {
    return new PopupLayout(this);
  }

  _openWithoutParent() {
    // resolve parent for entry-point (don't change the actual property)
    if (this.parent.destroyed) {
      return;
    }
    if (this.parent.rendered || this.parent.rendering) {
      this.open(this._getDefaultOpen$Parent());
      return;
    }

    // This is important for popups rendered in another (native) browser window. The DOM in the popup window
    // is rendered later, so we must wait until that window is rendered and layouted. See popup-window.html.
    this.parent.one('render', () => {
      this.session.layoutValidator.schedulePostValidateFunction(() => {
        if (this.destroyed || this.rendered) {
          return;
        }
        this.open();
      });
    });
  }

  /**
   * Only called if parent.rendered or parent.rendering
   * @return {$}
   */
  _getDefaultOpen$Parent() {
    return this.parent.entryPoint();
  }

  open($parent) {
    if (!$parent) {
      this._openWithoutParent();
      return;
    }

    this._triggerPopupOpenEvent();

    this._open($parent);
    if (this._openLater) {
      return;
    }

    if (!this.animateOpening) {
      // It is important that focusing happens after layouting and positioning, otherwise we'd focus an element
      // that is currently not on the screen. Which would cause the whole desktop to
      // be shifted for a few pixels.
      this.validateFocus();
      return;
    }
    // Give the browser time to layout properly before starting the animation to make sure it will be smooth.
    // The before-animate-open class will make the popup invisible (cannot use the invisible class because it is already used by _validateVisibility)
    this.$container.addClass('before-animate-open');
    setTimeout(() => {
      if (!this.rendered || this.removing) {
        return;
      }
      this.$container.removeClass('before-animate-open');
      this.validateFocus(); // Need to be done after popup is visible again because focus cannot be set on invisible elements.
      this.$container.addClassForAnimation('animate-open');
    });
  }

  validateFocus() {
    if (!this.withFocusContext) {
      return;
    }
    let context = this.session.focusManager.getFocusContext(this.$container);
    context.ready();
    if (!context.lastValidFocusedElement) {
      // No widget requested focus -> try to determine the initial focus
      this._requestInitialFocus();
    }
  }

  _requestInitialFocus() {
    let initialFocusElement = this.session.focusManager.evaluateFocusRule(this.$container, this.initialFocus());
    if (!initialFocusElement) {
      return;
    }
    this.session.focusManager.requestFocus(initialFocusElement);
  }

  _open($parent) {
    this.render($parent);
    if (this._openLater) {
      return;
    }
    this.revalidateLayout();
    this.position();
  }

  render($parent) {
    let $popupParent = $parent || this.entryPoint();
    // when the parent is detached it is not possible to render the popup -> do it later
    if (!$popupParent || !$popupParent.length || !$popupParent.isAttached()) {
      this._openLater = true;
      return;
    }
    super.render($popupParent);
  }

  _render() {
    this.$container = this.$parent.appendDiv('popup');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.validateRoot = true;
    this.htmlComp.setLayout(this._createLayout());
    this.$container.window().on('resize', this._windowResizeHandler);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderAnchor();
    this._renderWithArrow();
    this._renderWithFocusContext();
    this._renderWithGlassPane();
    this._renderModal();
  }

  _postRender() {
    super._postRender();

    this._attachCloseHandlers();
    this._attachAnchorHandlers();
    this._handleGlassPanes();
  }

  _onAttach() {
    super._onAttach();
    if (this._openLater && !this.rendered) {
      this._openLater = false;
      // Don't animate the opening when parent is attached. It doesn't look right if popups "pop up" when they are not really opening but only displayed again.
      // The same applies for detaching, see _renderOnDetach
      let currentAnimateOpening = this.animateOpening;
      this.animateOpening = false;
      this.open();
      this.animateOpening = currentAnimateOpening;
    }
  }

  _renderOnDetach() {
    this._openLater = true;
    // If parent is detached, popup should be removed immediately, otherwise animation would still be visible even though parent has already gone.
    super.removeImmediately();
    super._renderOnDetach();
  }

  remove() {
    let currentAnimateRemoval = this.animateRemoval;
    if ((this.boundToAnchor && this.$anchor) && !this._isAnchorInView()) {
      this.animateRemoval = false;
    }
    super.remove();
    this.animateRemoval = currentAnimateRemoval;
  }

  _remove() {
    this.$container.window().off('resize', this._windowResizeHandler);
    if (this._glassPaneRenderer) {
      this._glassPaneRenderer.removeGlassPanes();
    }
    if (this.withFocusContext) {
      this.session.focusManager.uninstallFocusContext(this.$container);
    }
    if (this.$arrow) {
      this.$arrow.remove();
      this.$arrow = null;
    }

    if (this.anchor) {
      // reopen when the anchor gets rendered again
      this.anchor.one('render', this._anchorRenderHandler);
    }

    // remove all clean-up handlers
    this._detachAnchorHandlers();
    this._detachCloseHandlers();
    super._remove();
  }

  _destroy() {
    if (this.anchor) {
      this.anchor.off('render', this._anchorRenderHandler);
    }
    super._destroy();
  }

  _renderWithFocusContext() {
    if (!this.withFocusContext) {
      return;
    }
    // Add programmatic 'tabindex' if the $container itself should be focusable (used by context menu popups with no focusable elements)
    if (this.focusableContainer) {
      this.$container.attr('tabindex', -1);
    }
    // Don't allow an element to be focused while the popup is opened.
    // The popup will focus the element as soon as the opening is finished (see open());
    // The context needs to be already installed so that child elements don't try to focus an element outside of this context
    this.session.focusManager.installFocusContext(this.$container, FocusRule.PREPARE);
  }

  setModal(modal) {
    this.setProperty('modal', modal);
  }

  _setModal(modal) {
    this._setProperty('modal', modal);
    if (modal) {
      widgets.preserveAndSetProperty(() => this.setProperty('withGlassPane', true), () => this.withGlassPane, this, '_withGlassPane');
      widgets.preserveAndSetProperty(() => this.setProperty('closeOnAnchorMouseDown', false), () => this.closeOnAnchorMouseDown, this, '_closeOnAnchorMouseDown');
      widgets.preserveAndSetProperty(() => this.setProperty('closeOnMouseDownOutside', false), () => this.closeOnMouseDownOutside, this, '_closeOnMouseDownOutside');
      widgets.preserveAndSetProperty(() => this.setProperty('closeOnOtherPopupOpen', false), () => this.closeOnOtherPopupOpen, this, '_closeOnOtherPopupOpen');
    } else {
      widgets.resetProperty(v => this.setWithGlassPane(v), this, '_withGlassPane');
      widgets.resetProperty(v => this.setCloseOnAnchorMouseDown(v), this, '_closeOnAnchorMouseDown');
      widgets.resetProperty(v => this.setCloseOnMouseDownOutside(v), this, '_closeOnMouseDownOutside');
      widgets.resetProperty(v => this.setCloseOnOtherPopupOpen(v), this, '_closeOnOtherPopupOpen');
    }
  }

  _renderModal() {
    this.$container.toggleClass('modal', this.modal);
  }

  setWithGlassPane(withGlassPane) {
    if (!this.modal) {
      this.setProperty('withGlassPane', withGlassPane);
    } else {
      this._withGlassPane = withGlassPane;
    }
  }

  _renderWithGlassPane() {
    if (this.withGlassPane && !this._glassPaneRenderer) {
      this._glassPaneRenderer = new GlassPaneRenderer(this);
      this._glassPaneRenderer.renderGlassPanes();
    } else if (!this.withGlassPane && this._glassPaneRenderer) {
      this._glassPaneRenderer.removeGlassPanes();
      this._glassPaneRenderer = null;
    }
  }

  setCloseOnMouseDownOutside(closeOnMouseDownOutside) {
    if (!this.modal) {
      this.setProperty('closeOnMouseDownOutside', closeOnMouseDownOutside);
    } else {
      this._closeOnMouseDownOutside = closeOnMouseDownOutside;
    }
  }

  _renderCloseOnMouseDownOutside() {
    // The listener needs to be executed in the capturing phase -> prevents that _onDocumentMouseDown will be executed right after the popup gets opened using mouse down, otherwise the popup would be closed immediately
    if (this.closeOnMouseDownOutside && !this._documentMouseDownHandler) {
      this._documentMouseDownHandler = this._onDocumentMouseDown.bind(this);
      this.$container.document(true).addEventListener('mousedown', this._documentMouseDownHandler, true); // true=the event handler is executed in the capturing phase
    } else if (!this.closeOnMouseDownOutside && this._documentMouseDownHandler) {
      this.$container.document(true).removeEventListener('mousedown', this._documentMouseDownHandler, true);
      this._documentMouseDownHandler = null;
    }
  }

  setCloseOnAnchorMouseDown(closeOnAnchorMouseDown) {
    if (!this.modal) {
      this.setProperty('closeOnAnchorMouseDown', closeOnAnchorMouseDown);
    } else {
      this._closeOnAnchorMouseDown = closeOnAnchorMouseDown;
    }
  }

  setCloseOnOtherPopupOpen(closeOnOtherPopupOpen) {
    if (!this.modal) {
      this.setProperty('closeOnOtherPopupOpen', closeOnOtherPopupOpen);
    } else {
      this._closeOnOtherPopupOpen = closeOnOtherPopupOpen;
    }
  }

  _renderCloseOnOtherPopupOpen() {
    if (this.closeOnOtherPopupOpen && !this._popupOpenHandler) {
      this._popupOpenHandler = this._onPopupOpen.bind(this);
      this.session.desktop.on('popupOpen', this._popupOpenHandler);
    } else if (!this.closeOnOtherPopupOpen && this._popupOpenHandler) {
      this.session.desktop.off('popupOpen', this._popupOpenHandler);
      this._popupOpenHandler = null;
    }
  }

  setWithArrow(withArrow) {
    this.setProperty('withArrow', withArrow);
  }

  _renderWithArrow() {
    if (this.$arrow) {
      this.$arrow.remove();
      this.$arrow = null;
    }
    if (this.$arrowOverlay) {
      this.$arrowOverlay.remove();
      this.$arrowOverlay = null;
    }
    if (this.withArrow) {
      this.$arrowOverlay = this.$container.prependDiv('popup-arrow-overlay');
      this.$arrow = this.$container.prependDiv('popup-arrow');
      this._updateArrowClass();
    }
    this.$container.toggleClass('with-arrow', this.withArrow);
    this.invalidateLayoutTree();
  }

  _updateArrowClass(verticalAlignment, horizontalAlignment) {
    if (this.$arrow) {
      this.$arrow.removeClass(this._alignClasses());
      this.$arrow.addClass(this._computeArrowPositionClass(verticalAlignment, horizontalAlignment));
    }
  }

  _computeArrowPositionClass(verticalAlignment, horizontalAlignment) {
    let Alignment = Popup.Alignment;
    let cssClass = '';
    horizontalAlignment = horizontalAlignment || this.horizontalAlignment;
    verticalAlignment = verticalAlignment || this.verticalAlignment;
    switch (horizontalAlignment) {
      case Alignment.LEFT:
        cssClass = Alignment.RIGHT;
        break;
      case Alignment.RIGHT:
        cssClass = Alignment.LEFT;
        break;
      default:
        cssClass = horizontalAlignment;
        break;
    }

    switch (verticalAlignment) {
      case Alignment.BOTTOM:
        cssClass += ' ' + Alignment.TOP;
        break;
      case Alignment.TOP:
        cssClass += ' ' + Alignment.BOTTOM;
        break;
      default:
        cssClass += ' ' + verticalAlignment;
        break;
    }
    return cssClass;
  }

  _animateRemovalWhileRemovingParent() {
    if (!this.$anchor) {
      // Allow remove animations for popups without an anchor
      return true;
    }
    // If parent is the anchor, prevent remove animation to ensure popup will be removed together with the anchor
    return widgets.get(this.$anchor) !== this.parent;
  }

  _isRemovalPrevented() {
    // If removal of a parent is pending due to an animation then don't return true to make sure popups are closed before the parent animation starts.
    // However, if the popup itself is removed by an animation, removal should be prevented to ensure remove() won't run multiple times.
    return this.removalPending;
  }

  close() {
    if (this.destroyed || this.destroying) {
      // Already closed, do nothing
      return;
    }
    let event = new Event();
    this.trigger('close', event);
    if (!event.defaultPrevented) {
      this.destroy();
    }
  }

  /**
   * Install listeners to close the popup once clicking outside the popup,
   * or changing the anchor's scroll position, or another popup is opened.
   */
  _attachCloseHandlers() {
    // Install mouse close handler
    this._renderCloseOnMouseDownOutside();
    // Install popup open close handler
    this._renderCloseOnOtherPopupOpen();
  }

  _attachAnchorHandlers() {
    if (!this.$anchor || !this.boundToAnchor || !this.scrollType) {
      return;
    }
    // Attach a scroll handler to each scrollable parent of the anchor
    this._anchorScrollHandler = this._onAnchorScroll.bind(this);
    scrollbars.onScroll(this.$anchor, this._anchorScrollHandler);

    // Attach a location change handler as well (will only work if the anchor is a widget which triggers a locationChange event, e.g. another Popup)
    let anchor = scout.widget(this.$anchor);
    if (anchor) {
      this._anchorLocationChangeHandler = this._onAnchorLocationChange.bind(this);
      anchor.on('locationChange', this._anchorLocationChangeHandler);
    }
  }

  _detachAnchorHandlers() {
    if (this._anchorScrollHandler) {
      scrollbars.offScroll(this._anchorScrollHandler);
      this._anchorScrollHandler = null;
    }
    if (this._anchorLocationChangeHandler) {
      let anchor = scout.widget(this.$anchor);
      if (anchor) {
        anchor.off('locationChange', this._anchorLocationChangeHandler);
        this._anchorLocationChangeHandler = null;
      }
    }
  }

  _detachCloseHandlers() {
    // Uninstall popup open close handler
    if (this._popupOpenHandler) {
      this.session.desktop.off('popupOpen', this._popupOpenHandler);
      this._popupOpenHandler = null;
    }

    // Uninstall mouse close handler
    if (this._documentMouseDownHandler) {
      this.$container.document(true).removeEventListener('mousedown', this._documentMouseDownHandler, true);
      this._documentMouseDownHandler = null;
    }
  }

  _onDocumentMouseDown(event) {
    // in some cases the mousedown handler is executed although it has been already
    // detached on the _remove() method. However, since we're in the middle of
    // processing the mousedown event, it's too late to detach the event and we must
    // deal with that situation by checking the rendered flag. Otherwise we would
    // run into an error later, since the $container is not available anymore.
    // Use the internal flag because popup should be closed even if the parent removal is pending due to a remove animation
    if (!this._rendered) {
      return;
    }
    if (this._isMouseDownOutside(event)) {
      this._onMouseDownOutside(event);
    }
  }

  _isMouseDownOutside(event) {
    let $target = $(event.target),
      targetWidget;

    if (!this.closeOnAnchorMouseDown && this._isMouseDownOnAnchor(event)) {
      // 1. Often times, click on the anchor opens and 2. click closes the popup
      // If we were closing the popup here, it would not be possible to achieve the described behavior anymore -> let anchor handle open and close.
      return false;
    }

    targetWidget = scout.widget($target);

    // close the popup only if the click happened outside of the popup and its children
    // It is not sufficient to check the dom hierarchy using $container.has($target)
    // because the popup may open other popups which probably is not a dom child but a sibling
    // Also ignore clicks if the popup is covert by a glasspane
    return !this.isOrHas(targetWidget) && !this.session.focusManager.isElementCovertByGlassPane(this.$container[0]);
  }

  _isMouseDownOnAnchor(event) {
    return !!this.$anchor && this.$anchor.isOrHas(event.target);
  }

  /**
   * Method invoked once a mouse down event occurs outside the popup.
   */
  _onMouseDownOutside(event) {
    this.close();
  }

  /**
   * Method invoked once the 'options.$anchor' is scrolled.
   */
  _onAnchorScroll(event) {
    if (!this.rendered) {
      // Scroll events may be fired delayed, even if scroll listeners are already removed.
      return;
    }
    this._handleAnchorPositionChange();
  }

  _handleAnchorPositionChange(event) {
    if (scout.isOneOf(this.scrollType, 'position', 'layoutAndPosition') && this.isOpeningAnimationRunning()) {
      // If the popup is opened with an animation which transforms the popup the sizes used by prefSize and position will likely be wrong.
      // In that case it is not possible to layout and position it correctly -> do nothing.
      return;
    }

    if (this.scrollType === 'position') {
      this.position();
    } else if (this.scrollType === 'layoutAndPosition') {
      this.revalidateLayout();
      this.position();
    } else if (this.scrollType === 'remove') {
      this.close();
    }
  }

  isOpeningAnimationRunning() {
    return this.rendered && this.animateOpening && this.$container.hasClass('animate-open');
  }

  _onAnchorLocationChange(event) {
    this._handleAnchorPositionChange();
  }

  /**
   * Method invoked once a popup is opened.
   */
  _onPopupOpen(event) {
    // Make sure child popups don't close the parent popup, we must check parent hierarchy in both directions
    // Use case: Opening of a context menu or cell editor in a form popup
    // Also, popups covered by a glass pane (a modal dialog is open) must never be closed
    // Use case: popup opens a modal dialog. User clicks on a smartfield on this dialog -> underlying popup must not get closed
    let closable = !this.isOrHas(event.popup) && !event.popup.isOrHas(this);
    if (this.rendered) {
      closable = closable && !this.session.focusManager.isElementCovertByGlassPane(this.$container[0]);
    }
    if (closable) {
      this.close();
    }
  }

  setHorizontalAlignment(horizontalAlignment) {
    this.setProperty('horizontalAlignment', horizontalAlignment);
  }

  _renderHorizontalAlignment() {
    this._updateArrowClass();
    this.invalidateLayoutTree();
  }

  setVerticalAlignment(verticalAlignment) {
    this.setProperty('verticalAlignment', verticalAlignment);
  }

  _renderVerticalAlignment() {
    this._updateArrowClass();
    this.invalidateLayoutTree();
  }

  setHorizontalSwitch(horizontalSwitch) {
    this.setProperty('horizontalSwitch', horizontalSwitch);
  }

  _renderHorizontalSwitch() {
    this.invalidateLayoutTree();
  }

  setVerticalSwitch(verticalSwitch) {
    this.setProperty('verticalSwitch', verticalSwitch);
  }

  _renderVerticalSwitch() {
    this.invalidateLayoutTree();
  }

  setTrimWidth(trimWidth) {
    this.setProperty('trimWidth', trimWidth);
  }

  _renderTrimWidth() {
    this.invalidateLayoutTree();
  }

  setTrimHeight(trimHeight) {
    this.setProperty('trimHeight', trimHeight);
  }

  _renderTrimHeight() {
    this.invalidateLayoutTree();
  }

  prefLocation(verticalAlignment, horizontalAlignment) {
    if (!this.boundToAnchor || (!this.anchorBounds && !this.$anchor)) {
      return this._prefLocationWithoutAnchor();
    }
    return this._prefLocationWithAnchor(verticalAlignment, horizontalAlignment);
  }

  _prefLocationWithoutAnchor() {
    return DialogLayout.positionContainerInWindow(this.$container);
  }

  _prefLocationWithAnchor(verticalAlignment, horizontalAlignment) {
    let $container = this.$container;
    horizontalAlignment = horizontalAlignment || this.horizontalAlignment;
    verticalAlignment = verticalAlignment || this.verticalAlignment;
    let anchorBounds = this.getAnchorBounds();
    let size = graphics.size($container, {exact: true});
    let margins = graphics.margins($container);
    let Alignment = Popup.Alignment;

    let arrowBounds = null;
    if (this.$arrow) {
      // Ensure the arrow has the correct class
      this._updateArrowClass(verticalAlignment, horizontalAlignment);
      // Remove margin added by moving logic, otherwise the bounds would not be correct
      graphics.setMargins(this.$arrow, new Insets());
      arrowBounds = graphics.bounds(this.$arrow);
    }

    $container.removeClass(this._alignClasses());
    $container.addClass(verticalAlignment + ' ' + horizontalAlignment);

    let widthWithMargin = size.width + margins.horizontal();
    let width = size.width;
    let x = anchorBounds.x;
    if (horizontalAlignment === Alignment.LEFT) {
      x -= widthWithMargin;
    } else if (horizontalAlignment === Alignment.LEFTEDGE) {
      if (this.withArrow) {
        x += anchorBounds.width / 2 - arrowBounds.center().x - margins.left;
      } else {
        x = anchorBounds.x - margins.left;
      }
    } else if (horizontalAlignment === Alignment.CENTER) {
      x += anchorBounds.width / 2 - width / 2 - margins.left;
    } else if (horizontalAlignment === Alignment.RIGHT) {
      x += anchorBounds.width;
    } else if (horizontalAlignment === Alignment.RIGHTEDGE) {
      if (this.withArrow) {
        x += anchorBounds.width / 2 - arrowBounds.center().x - margins.right;
      } else {
        x = anchorBounds.x + anchorBounds.width - width - margins.right;
      }
    }

    let heightWithMargin = size.height + margins.vertical();
    let height = size.height;
    let y = anchorBounds.y;
    if (verticalAlignment === Alignment.TOP) {
      y -= heightWithMargin;
    } else if (verticalAlignment === Alignment.TOPEDGE) {
      if (this.withArrow) {
        y += anchorBounds.height / 2 - arrowBounds.center().y - margins.top;
      } else {
        y = anchorBounds.y - margins.top;
      }
    } else if (verticalAlignment === Alignment.CENTER) {
      y += anchorBounds.height / 2 - height / 2 - margins.top;
    } else if (verticalAlignment === Alignment.BOTTOM) {
      y += anchorBounds.height;
    } else if (verticalAlignment === Alignment.BOTTOMEDGE) {
      if (this.withArrow) {
        y += anchorBounds.height / 2 - arrowBounds.center().y - margins.bottom;
      } else {
        y = anchorBounds.y + anchorBounds.height - height - margins.bottom;
      }
    }

    // this.$parent might not be at (0,0) of the document
    let parentOffset = this.$parent.offset();
    x -= parentOffset.left;
    y -= parentOffset.top;

    return new Point(x, y);
  }

  _alignClasses() {
    let Alignment = Popup.Alignment;
    return strings.join(' ', Alignment.LEFT, Alignment.LEFTEDGE, Alignment.CENTER, Alignment.RIGHT, Alignment.RIGHTEDGE,
      Alignment.TOP, Alignment.TOPEDGE, Alignment.CENTER, Alignment.BOTTOM, Alignment.BOTTOMEDGE);
  }

  getAnchorBounds() {
    let anchorBounds = this.anchorBounds;
    if (!this.$anchor) {
      // Use manually set anchor bounds
      return anchorBounds;
    }
    let realAnchorBounds = graphics.offsetBounds(this.$anchor, {
      exact: true
    });
    if (!anchorBounds) {
      // Use measured anchor bounds
      anchorBounds = realAnchorBounds;
    } else {
      // Fill incomplete anchorBounds from measured anchor bounds. This allows setting one
      // coordinate to a fixed value (e.g. the current mouse cursor position) while still
      // aligning the other coordinate to the $anchor element.
      //
      // Implementation note:
      // A coordinate is considered "undefined", when it is 0. Technically, this is not 100%
      // correct, but will give the desired result in most of the cases. If would require too
      // many code changes to correctly set missing values to undefined/null.
      if (!anchorBounds.x) {
        anchorBounds.x = realAnchorBounds.x;
        anchorBounds.width = realAnchorBounds.width;
      }
      if (!anchorBounds.y) {
        anchorBounds.y = realAnchorBounds.y;
        anchorBounds.height = realAnchorBounds.height;
      }
    }
    return anchorBounds;
  }

  getWindowSize() {
    let $window = this.$parent.window();
    return new Dimension($window.width(), $window.height());
  }

  /**
   * @returns Point the amount of overlap at the window borders.
   * A positive value indicates that it is overlapping the right / bottom border, a negative value indicates that it is overlapping the left / top border.
   * Prefers the right and bottom over the left and top border, meaning if a positive value is returned it does not mean that the left border is overlapping as well.
   */
  overlap(location, includeMargin) {
    let $container = this.$container;
    if (!$container || !location) {
      return null;
    }
    includeMargin = scout.nvl(includeMargin, true);
    let containerSize = graphics.size($container, {exact: true, includeMargin: includeMargin});
    let width = containerSize.width;
    let height = containerSize.height;
    let popupBounds = new Rectangle(location.x, location.y, width, height);
    let bounds = graphics.offsetBounds($container.entryPoint(), true);

    let overlapX = popupBounds.right() + this.windowPaddingX - bounds.width;
    if (overlapX < 0) {
      overlapX = Math.min(popupBounds.x - this.windowPaddingX - bounds.x, 0);
    }
    let overlapY = popupBounds.bottom() + this.windowPaddingY - bounds.height;
    if (overlapY < 0) {
      overlapY = Math.min(popupBounds.y - this.windowPaddingY - bounds.y, 0);
    }
    return new Point(overlapX, overlapY);
  }

  adjustLocation(location, switchIfNecessary) {
    this.calculatedVerticalAlignment = this.verticalAlignment;
    this.calculatedHorizontalAlignment = this.horizontalAlignment;
    let overlap = this.overlap(location);

    // Reset arrow style
    if (this.$arrow) {
      this._updateArrowClass(this.calculatedVerticalAlignment, this.calculatedHorizontalAlignment);
      graphics.setMargins(this.$arrow, new Insets());
    }

    location = location.clone();
    // Ignore very small overlaps (e.g. 0.3px). This could happen if anchor position is fractional and popup has a margin that is not
    // Example: anchor has top: 10px and margin-top: 10px, browser renders it at 9.984px (due to zoom) but margin stays at 10px
    // -> location.y would be -0.16px resulting in a popup switch so that the popup will be displayed outside of the window
    if (Math.abs(overlap.y) >= 1) {
      let verticalSwitch = scout.nvl(switchIfNecessary, this.verticalSwitch);
      if (verticalSwitch) {
        // Switch vertical alignment
        this.calculatedVerticalAlignment = Popup.SwitchRule[this.calculatedVerticalAlignment];
        location.y = this.prefLocation(this.calculatedVerticalAlignment).y;
      } else {
        // Move popup to the top until it gets fully visible (if switch is disabled)
        location.y -= overlap.y;
      }
    }
    // Reason for >= 1 see above
    if (Math.abs(overlap.x) >= 1) {
      let horizontalSwitch = scout.nvl(switchIfNecessary, this.horizontalSwitch);
      if (horizontalSwitch) {
        // Switch horizontal alignment
        this.calculatedHorizontalAlignment = Popup.SwitchRule[this.calculatedHorizontalAlignment];
        location.x = this.prefLocation(this.calculatedVerticalAlignment, this.calculatedHorizontalAlignment).x;
      } else {
        // Move popup to the left until it gets fully visible (if switch is disabled)
        location.x -= overlap.x;
      }
    }

    // Also move arrow so that it still points to the center of the anchor
    if (this.$arrow) {
      if (overlap.y !== 0 && (this.$arrow.hasClass(Popup.Alignment.LEFT) || this.$arrow.hasClass(Popup.Alignment.RIGHT))) {
        if (overlap.y > 0) {
          this.$arrow.cssMarginTop(overlap.y);
        } else {
          this.$arrow.cssMarginBottom(-overlap.y);
        }
      }
      if (overlap.x !== 0 && (this.$arrow.hasClass(Popup.Alignment.TOP) || this.$arrow.hasClass(Popup.Alignment.BOTTOM))) {
        if (overlap.x > 0) {
          this.$arrow.cssMarginLeft(overlap.x);
        } else {
          this.$arrow.cssMarginRight(-overlap.x);
        }
      }
    }

    return location;
  }

  position(switchIfNecessary) {
    if (!this.rendered) {
      return;
    }
    this._validateVisibility();
    this._position(switchIfNecessary);
  }

  _position(switchIfNecessary) {
    let location = this.prefLocation();
    if (!location) {
      return;
    }
    location = this.adjustLocation(location, switchIfNecessary);
    this.setLocation(location);
  }

  setLocation(location) {
    if (!this.rendered) {
      return;
    }
    this.$container
      .css('left', location.x)
      .css('top', location.y);
    this._triggerLocationChange();
  }

  /**
   * Popups with an anchor must only be visible if the anchor is in view (prevents that the popup points at an invisible anchor)
   */
  _validateVisibility() {
    if (!this.boundToAnchor || !this.$anchor) {
      return;
    }
    let inView = this._isAnchorInView();
    let needsLayouting = this.$container.hasClass('invisible') === inView && inView;
    this.$container.toggleClass('invisible', !inView); // Use visibility: hidden to not break layouting / size measurement
    if (needsLayouting) {
      let currentAnimateResize = this.animateResize;
      this.animateResize = false;
      this.revalidateLayout();
      this.animateResize = currentAnimateResize;
      if (this.withFocusContext) {
        this.session.focusManager.validateFocus();
      }
    }
  }

  _isAnchorInView() {
    if (!this.boundToAnchor || !this.$anchor) {
      return;
    }
    let anchorBounds = this.getAnchorBounds();
    return scrollbars.isLocationInView(anchorBounds.center(), this.$anchor.scrollParents());
  }

  _triggerLocationChange() {
    this.trigger('locationChange');
  }

  /**
   * Fire event that this popup is about to open.
   */
  _triggerPopupOpenEvent() {
    this.session.desktop.trigger('popupOpen', {
      popup: this
    });
  }

  belongsTo($anchor) {
    return this.$anchor[0] === $anchor[0];
  }

  set$Anchor($anchor) {
    if (this.$anchor) {
      this._detachAnchorHandlers();
    }
    this.setProperty('$anchor', $anchor);
    if (this.rendered) {
      this._attachAnchorHandlers();
      this.revalidateLayout();
      if (!this.animateResize) { // PopupLayout will move it -> don't break move animation
        this.position();
      }
    }
  }

  isOpen() {
    return this.rendered;
  }

  ensureOpen() {
    if (!this.isOpen()) {
      this.open();
    }
  }

  setAnchor(anchor) {
    this.setProperty('anchor', anchor);
  }

  _setAnchor(anchor) {
    if (anchor) {
      this.setParent(anchor);
    }
    this._setProperty('anchor', anchor);
  }

  _onAnchorRender() {
    this.session.layoutValidator.schedulePostValidateFunction(() => {
      if (this.rendered || this.destroyed) {
        return;
      }
      if (this.anchor && !this.anchor.rendered) {
        // Anchor was removed again while this function was scheduled -> wait again for rendering
        this.anchor.one('render', this._anchorRenderHandler);
        return;
      }
      let currentAnimateOpening = this.animateOpening;
      this.animateOpening = false;
      this.open();
      this.animateOpening = currentAnimateOpening;
    });
  }

  _renderAnchor() {
    if (this.anchor) {
      this.set$Anchor(this.anchor.$container);
    }
  }

  _onWindowResize() {
    if (!this.rendered) {
      // may already be removed if a parent popup is closed during the resize event
      return;
    }
    if (this.windowResizeType === 'position') {
      this.position();
    } else if (this.windowResizeType === 'layoutAndPosition') {
      this.revalidateLayoutTree(false);
      this.position();
    } else if (this.windowResizeType === 'remove') {
      this.close();
    }
  }

  _handleGlassPanes() {
    let parentCoveredByGlassPane = this.session.focusManager.isElementCovertByGlassPane(this.parent.$container);
    // if a popup is covered by a glass pane the glass pane's need to be re-rendered to ensure a glass pane is also painted over the popup
    if (parentCoveredByGlassPane) {
      this.session.focusManager.rerenderGlassPanes();
    }
  }
}

((() => {
  // Initialize switch rules (wrapped in IIFE to have local function scope for the variables)
  let SwitchRule = Popup.SwitchRule;
  let Alignment = Popup.Alignment;
  SwitchRule[Alignment.LEFT] = Alignment.RIGHT;
  SwitchRule[Alignment.LEFTEDGE] = Alignment.RIGHTEDGE;
  SwitchRule[Alignment.TOP] = Alignment.BOTTOM;
  SwitchRule[Alignment.TOPEDGE] = Alignment.BOTTOMEDGE;
  SwitchRule[Alignment.CENTER] = Alignment.CENTER;
  SwitchRule[Alignment.RIGHT] = Alignment.LEFT;
  SwitchRule[Alignment.RIGHTEDGE] = Alignment.LEFTEDGE;
  SwitchRule[Alignment.BOTTOM] = Alignment.TOP;
  SwitchRule[Alignment.BOTTOMEDGE] = Alignment.TOPEDGE;
})());
