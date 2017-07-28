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
scout.Popup = function() {
  scout.Popup.parent.call(this);

  this._mouseDownHandler = null;
  this._scrollHandler = null;
  this._popupOpenHandler = null;
  this._glassPaneRenderer = null;
  this.anchorBounds;
  this.$anchor;
  this.windowPaddingX = 10;
  this.windowPaddingY = 5;
  this.withGlassPane = false;
  this.withFocusContext = true;
  this.initialFocus = function() {
    return scout.focusRule.AUTO;
  };
  this.focusableContainer = false;
  this.openingDirectionX = 'right';
  this.openingDirectionY = 'down';
  this.scrollType = 'remove';
  // hints for the layout to control whether the size should be adjusted if the popup does not fit into the window
  // Popup is getting moved if it overlaps a border (and not switched as done for y axis) -> do not adjust its size
  this.trimWidth = false;
  this.trimHeight = true;
  // If true, anchor is considered when computing the position and size of the popup
  this.boundToAnchor = true;
  // If true, the attached mouse down handler will NOT close the popup if the anchor was clicked, the anchor is responsible to close it.
  // This is necessary because the mousedown listener is attached to the capture phase and therefore executed before any other.
  // If anchor was clicked, popup would already be closed and then opened again -> popup could never be closed by clicking the anchor
  this.closeOnAnchorMouseDown = true;
};
scout.inherits(scout.Popup, scout.Widget);

/**
 * @param options:
 *          initialFocus: a function that returns the element to be focused or a <code>scout.focusRule</code>. Default returns <code>scout.focusRule.AUTO</code>
 *          focusableContainer: a boolean whether or not the container of the Popup is focusable
 */
scout.Popup.prototype._init = function(options) {
  scout.Popup.parent.prototype._init.call(this, options);

  if (options.location) {
    this.anchorBounds = new scout.Rectangle(options.location.x, options.location.y, 0, 0);
  }
  if (this.withGlassPane) {
    this._glassPaneRenderer = new scout.GlassPaneRenderer(this);
  }
};

/**
 * @override
 */
scout.Popup.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.Popup.prototype._initKeyStrokeContext = function() {
  scout.Popup.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(this._createCloseKeyStroke());
};

/**
 * Override this method to provide a key stroke which closes the popup.
 * The default impl. returns a CloseKeyStroke which handles the ESC key.
 */
scout.Popup.prototype._createCloseKeyStroke = function() {
  return new scout.CloseKeyStroke(this);
};

scout.Popup.prototype._createLayout = function() {
  return new scout.PopupLayout(this);
};

scout.Popup.prototype.open = function($parent) {
  this._triggerPopupOpenEvent();

  this._open($parent);

  // Focus the popup
  // It is important that this happens after layouting and positioning, otherwise we'd focus an element
  // that is currently not on the screen. Which would cause the whole desktop to
  // be shifted for a few pixels.
  if (this.withFocusContext) {
    this.session.focusManager.installFocusContext(this.$container, this.initialFocus());
  }
};

scout.Popup.prototype._open = function($parent) {
  this.render($parent);
  this.revalidateLayout();
  this.position();
};

scout.Popup.prototype.render = function($parent) {
  var $popupParent = $parent || this.entryPoint();
  scout.Popup.parent.prototype.render.call(this, $popupParent);
};

scout.Popup.prototype._render = function() {
  this.$container = this.$parent.appendDiv('popup');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.validateRoot = true;
  this.htmlComp.setLayout(this._createLayout());
};

scout.Popup.prototype._renderProperties = function() {
  scout.Popup.parent.prototype._renderProperties.call(this);
  this._renderWithFocusContext();
  this._renderWithGlassPane();
};

scout.Popup.prototype._postRender = function() {
  scout.Popup.parent.prototype._postRender.call(this);

  this.size();
  this._attachCloseHandler();
};

scout.Popup.prototype._remove = function() {
  if (this._glassPaneRenderer) {
    this._glassPaneRenderer.removeGlassPanes();
  }
  if (this.withFocusContext) {
    this.session.focusManager.uninstallFocusContext(this.$container);
  }
  // remove all clean-up handlers
  this._detachCloseHandler();
  scout.Popup.parent.prototype._remove.call(this);
};

scout.Popup.prototype._renderWithFocusContext = function() {
  // Add programmatic 'tabindex' if the $container itself should be focusable (used by context menu popups with no focusable elements)
  if (this.withFocusContext && this.focusableContainer) {
    this.$container.attr('tabindex', -1);
  }
};

scout.Popup.prototype._renderWithGlassPane = function() {
  if (this._glassPaneRenderer) {
    this._glassPaneRenderer.renderGlassPanes();
  }
};

scout.Popup.prototype._isRemovalPrevented = function() {
  // Never prevent. Default returns true if removal is pending by an animation, but popups should be closed before the animation starts
  return false;
};

scout.Popup.prototype.close = function() {
  this.trigger('close');
  this.destroy();
};

/**
 * Install listeners to close the popup once clicking outside the popup,
 * or changing the anchor's scroll position, or another popup is opened.
 */
scout.Popup.prototype._attachCloseHandler = function() {
  // Install mouse close handler
  // The listener needs to be executed in the capturing phase -> prevents that _onMouseDown will be executed right after the popup gets opened using mouse down, otherwise the popup would be closed immediately
  this._mouseDownHandler = this._onMouseDown.bind(this);
  this.$container.document(true).addEventListener('mousedown', this._mouseDownHandler, true); // true=the event handler is executed in the capturing phase

  // Install popup open close handler
  this._popupOpenHandler = this._onPopupOpen.bind(this);
  this.session.desktop.on('popupOpen', this._popupOpenHandler);

  // Install scroll close handler
  if (this.$anchor && this.boundToAnchor && this.scrollType) {
    this._scrollHandler = this._onAnchorScroll.bind(this);
    scout.scrollbars.onScroll(this.$anchor, this._scrollHandler);
  }
};

scout.Popup.prototype._detachCloseHandler = function() {
  // Uninstall scroll close handler
  if (this._scrollHandler) {
    scout.scrollbars.offScroll(this._scrollHandler);
    this._scrollHandler = null;
  }

  // Uninstall popup open close handler
  if (this._popupOpenHandler) {
    this.session.desktop.off('popupOpen', this._popupOpenHandler);
    this._popupOpenHandler = null;
  }

  // Uninstall mouse close handler
  if (this._mouseDownHandler) {
    this.$container.document(true).removeEventListener('mousedown', this._mouseDownHandler, true);
    this._mouseDownHandler = null;
  }
};

scout.Popup.prototype._onMouseDown = function(event) {
  // in some cases the mousedown handler is executed although it has been already
  // detached on the _remove() method. However, since we're in the middle of
  // processing the mousedown event, it's too late to detach the event and we must
  // deal with that situation by checking the rendered flag. Otherwise we would
  // run into an error later, since the $container is not available anymore.
  if (!this.rendered) {
    return;
  }
  if (this._isMouseDownOutside(event)) {
    this._onMouseDownOutside(event);
  }
};

scout.Popup.prototype._isMouseDownOutside = function(event) {
  var $target = $(event.target),
    targetWidget;

  if (!this.closeOnAnchorMouseDown && this._isMouseDownOnAnchor(event)) {
    // 1. Often times, click on the anchor opens and 2. click closes the popup
    // If we were closing the popup here, it would not be possible to achieve the described behavior anymore -> let anchor handle open and close.
    return false;
  }

  targetWidget = scout.Widget.getWidgetFor($target);

  // close the popup only if the click happened outside of the popup and its children
  // It is not sufficient to check the dom hierarchy using $container.has($target)
  // because the popup may open other popups which probably is not a dom child but a sibling
  // Also ignore clicks if the popup is covert by a glasspane
  return !this.isOrHas(targetWidget) && !this.session.focusManager.isElementCovertByGlassPane(this.$container[0]);
};

scout.Popup.prototype._isMouseDownOnAnchor = function(event) {
  return !!this.$anchor && this.$anchor.isOrHas(event.target);
};

/**
 * Method invoked once a mouse down event occurs outside the popup.
 */
scout.Popup.prototype._onMouseDownOutside = function(event) {
  this.close();
};

/**
 * Method invoked once the 'options.$anchor' is scrolled.
 */
scout.Popup.prototype._onAnchorScroll = function(event) {
  if (!this.rendered) {
    // Scroll events may be fired delayed, even if scroll listener are already removed.
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
};

/**
 * Method invoked once a popup is opened.
 */
scout.Popup.prototype._onPopupOpen = function(event) {
  // Make sure child popups don't close the parent popup, we must check parent hierarchy in both directions
  // Use case: Opening of a context menu or cell editor in a form popup
  // Also, popups covered by a glass pane (a modal dialog is open) must never be closed
  // Use case: popup opens a modal dialog. User clicks on a smartfield on this dialog -> underlying popup must not get closed
  var closable =
    !this.isOrHas(event.popup) &&
    !event.popup.isOrHas(this);
  if (this.rendered) {
    closable = closable && !this.session.focusManager.isElementCovertByGlassPane(this.$container[0]);
  }
  if (closable) {
    this.close();
  }
};

scout.Popup.prototype.prefLocation = function(openingDirectionY) {
  var x, y, anchorBounds, height, openingDirectionX;
  var $container = this.$container;
  if (!this.boundToAnchor || (!this.anchorBounds && !this.$anchor)) {
    return;
  }
  openingDirectionX = 'right'; // always use right at the moment
  openingDirectionY = openingDirectionY || this.openingDirectionY;
  $container.removeClass('up down left right');
  $container.addClass(openingDirectionY + ' ' + openingDirectionX);
  height = $container.outerHeight(true);

  anchorBounds = this.getAnchorBounds();
  x = anchorBounds.x;
  y = anchorBounds.y;
  if (openingDirectionY === 'up') {
    y -= height;
  } else if (openingDirectionY === 'down') {
    y += anchorBounds.height;
  }

  // this.$parent might not be at (0,0) of the document
  var parentOffset = this.$parent.offset();
  x -= parentOffset.left;
  y -= parentOffset.top;

  return {
    x: x,
    y: y
  };
};

scout.Popup.prototype.getAnchorBounds = function() {
  var anchorBounds = this.anchorBounds;
  if (!anchorBounds) {
    anchorBounds = scout.graphics.offsetBounds(this.$anchor, {
      exact: true
    });
  }
  return anchorBounds;
};

scout.Popup.prototype.overlap = function(location) {
  var $container = this.$container;
  if (!$container || !location) {
    return;
  }
  var overlapX, overlapY,
    height = $container.outerHeight(),
    width = $container.outerWidth(),
    left = location.x,
    top = location.y;

  overlapX = left + width + this.windowPaddingX - $container.entryPoint().outerWidth(true);
  overlapY = top + height + this.windowPaddingY - $container.entryPoint().outerHeight(true);
  return {
    x: overlapX,
    y: overlapY
  };
};

scout.Popup.prototype.adjustLocation = function(location, switchIfNecessary) {
  var openingDirection, left, top,
    overlap = this.overlap(location);

  switchIfNecessary = scout.nvl(switchIfNecessary, true);
  if (overlap.y > 0 && switchIfNecessary) {
    // switch opening direction
    openingDirection = 'up';
    location = this.prefLocation(openingDirection);
  }
  left = location.x;
  top = location.y;
  if (overlap.x > 0) {
    // Move popup to the left until it gets fully visible
    left -= overlap.x;
  }
  return {
    x: left,
    y: top
  };
};

scout.Popup.prototype.size = function() {
  var size = this.prefSize(this.$container);
  if (!size) {
    return;
  }
  scout.graphics.setSize(this.$container, size);
};

scout.Popup.prototype.prefSize = function($container) {
  return null;
};

scout.Popup.prototype.position = function(switchIfNecessary) {
  this._validateVisibility();
  this._position(switchIfNecessary);
};

scout.Popup.prototype._position = function(switchIfNecessary) {
  var location = this.prefLocation();
  if (!location) {
    return;
  }
  location = this.adjustLocation(location, switchIfNecessary);
  this.setLocation(location);
};

scout.Popup.prototype.setLocation = function(location) {
  this.$container
    .css('left', location.x)
    .css('top', location.y);
  this._triggerLocationChange();
};

/**
 * Popups with an anchor must only be visible if the anchor is in view (prevents that the popup points at an invisible anchor)
 */
scout.Popup.prototype._validateVisibility = function() {
  if (!this.boundToAnchor || !this.$anchor) {
    return;
  }
  var inView = this._isInView();
  var needsLayouting = this.$container.isVisible() !== inView && inView;
  this.$container.toggleClass('invisible', !inView); // Use visibility: hidden to not break layouting / size measurement
  if (needsLayouting) {
    this.revalidateLayout();
  }
};

scout.Popup.prototype._isInView = function() {
  if (!this.boundToAnchor || !this.$anchor) {
    return;
  }
  var anchorBounds = this.getAnchorBounds();
  return scout.scrollbars.isLocationInView(anchorBounds.center(), this.$anchor.scrollParent());
};

scout.Popup.prototype._triggerLocationChange = function() {
  this.trigger('locationChange');
};

/**
 * Fire event that this popup is about to open.
 */
scout.Popup.prototype._triggerPopupOpenEvent = function() {
  this.session.desktop.trigger('popupOpen', {
    popup: this
  });
};

scout.Popup.prototype.belongsTo = function($anchor) {
  return this.$anchor[0] === $anchor[0];
};

scout.Popup.prototype.isOpen = function() {
  return this.rendered;
};

scout.Popup.prototype.ensureOpen = function() {
  if (!this.isOpen()) {
    this.open();
  }
};
