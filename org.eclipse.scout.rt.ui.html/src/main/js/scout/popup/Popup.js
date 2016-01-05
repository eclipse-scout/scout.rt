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

  this._addKeyStrokeContextSupport();
  this._addEventSupport();
  this._mouseDownHandler;
  this._scrollHandler;
  this._popupOpenHandler;
  this.openEvent;
  this.anchorBounds;
  this.$anchor;
  this.windowPaddingX;
  this.windowPaddingY;
  this.withFocusContext;
  this.initialFocus;
  this.focusableContainer;
  this.openingDirectionX;
  this.openingDirectionY;
};
scout.inherits(scout.Popup, scout.Widget);

/**
 * @param options:
 *          initialFocus: a function that returns the element to be focused or a <code>scout.focusRule</code>. Default returns <code>scout.focusRule.AUTO</code>
 *          focusableContainer: a boolean whether or not the container of the Popup is focusable
 */
scout.Popup.prototype._init = function(options) {
  scout.Popup.parent.prototype._init.call(this, options);

  this.anchorBounds = options.anchorBounds;
  if (options.location) {
    this.anchorBounds = new scout.Rectangle(options.location.x, options.location.y, 0, 0);
  }
  this.$anchor = options.$anchor;
  this.windowPaddingX = scout.nvl(options.windowPaddingX, 10);
  this.windowPaddingY = scout.nvl(options.windowPaddingY, 5);
  this.openingDirectionX = options.openingDirectionX || 'right';
  this.openingDirectionY = options.openingDirectionY || 'down';
  this.withFocusContext = scout.nvl(options.installFocusContext, true);
  this.initialFocus = scout.nvl(options.initialFocus, function() {
    return scout.focusRule.AUTO;
  });
  this.focusableContainer = scout.nvl(options.focusableContainer, false);
};

scout.Popup.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  keyStrokeContext.registerKeyStroke(new scout.CloseKeyStroke(this));
};

scout.Popup.prototype._createLayout = function() {
  return new scout.PopupLayout(this);
};

scout.Popup.prototype.open = function($parent, event) {
  this._open($parent, event);

  // Focus the popup
  // It is important that this happens after layouting and positioning, otherwise we'd focus an element
  // that is currently not on the screen. Which would cause the whole desktop to
  // be shifted for a few pixels.
  if (this.withFocusContext) {
    this.session.focusManager.installFocusContext(this.$container, this.initialFocus());
  }

  this._triggerPopupOpenEvent();
};

scout.Popup.prototype._uninstallAllChildScrollbars = function() {
  var $scrollables = scout.scrollbars.getScrollables(this.session),
    handledScrollables = [];
  $scrollables.forEach(function($scrollable) {
    if (this.$container.has($scrollable).length > 0) {
      var options = scout.scrollbars.getScrollableOptions($scrollable);
      handledScrollables.push({
        $scrollable: $scrollable,
        options: options
      });
      scout.scrollbars.uninstall($scrollable, this.session);
    }

  }.bind(this));
  return handledScrollables;
};

scout.Popup.prototype._open = function($parent, event) {
  this.render($parent, event);
  this.revalidateLayout();
  this.position();
};

scout.Popup.prototype.render = function($parent, event) {
  this.openEvent = event;
  var $popupParent = $parent || this.entryPoint(this.parent.$container);
  scout.Popup.parent.prototype.render.call(this, $popupParent);
};

scout.Popup.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('popup');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.validateRoot = true;
  this.htmlComp.setLayout(this._createLayout());

  // Add programmatic 'tabindex' if the $container itself should be focusable (used by context menu popups with no focusable elements)
  if (this.withFocusContext && this.focusableContainer) {
    this.$container.attr('tabindex', -1);
  }
};

scout.Popup.prototype._postRender = function() {
  this.size();
  this._attachCloseHandler();
  scout.scrollbars.update(this.$body);
};

scout.Popup.prototype._remove = function() {
  if (this.withFocusContext) {
    this.session.focusManager.uninstallFocusContext(this.$container);
  }
  // remove all clean-up handlers
  this._detachCloseHandler();
  scout.Popup.parent.prototype._remove.call(this);
};

scout.Popup.prototype.close = function(event) {
  if ((event && this.openEvent && event.originalEvent !== this.openEvent.originalEvent) || !event || !this.openEvent) {
    this._trigger('close', event);
    this.remove();
  }
};

/**
 * Install listeners to close the popup once clicking outside the popup,
 * or changing the anchor's scroll position, or another popup is opened.
 */
scout.Popup.prototype._attachCloseHandler = function() {
  // Install mouse close handler
  this._mouseDownHandler = this._onMouseDown.bind(this);
  this.$container.document().on('mousedown', this._mouseDownHandler);

  // Install popup open close handler
  this._popupOpenHandler = this._onPopupOpen.bind(this);
  this.session.desktop.on('popupopen', this._popupOpenHandler);

  // Install scroll close handler
  if (this.$anchor) {
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
    this.session.desktop.off('popupopen', this._popupOpenHandler);
    this._popupOpenHandler = null;
  }

  // Uninstall mouse close handler
  if (this._mouseDownHandler) {
    this.$container.document().off('mousedown', this._mouseDownHandler);
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
    targetWidget = scout.Widget.getWidgetFor($target);

  // close the popup only if the click happened outside of the popup and its children
  // It is not sufficient to check the dom hierarchy using $container.has($target)
  // because the popup may open other popups which probably is not a dom child but a sibling
  // Also ignore clicks if the popup is covert by a glasspane
  return !this.isOrHasWidget(targetWidget) && !this.session.focusManager.isElementCovertByGlassPane(this.$container[0]);
};

/**
 * Method invoked once a mouse down event occurs outside the popup.
 */
scout.Popup.prototype._onMouseDownOutside = function(event) {
  this.close(event);
};

/**
 * Method invoked once the 'options.$anchor' is scrolled.
 */
scout.Popup.prototype._onAnchorScroll = function(event) {
  this.close(event);
};

/**
 * Method invoked once a popup is opened.
 */
scout.Popup.prototype._onPopupOpen = function(event) {
  // Make sure child popups don't close the parent popup, we must check parent hierarchy in both directions
  // Use case: Opening of a context menu or cell editor in a form popup
  // Also, popups covered by a glass pane (a modal dialog is open) must never be closed
  // Use case: popup opens a modal dialog. User clicks on a smartfield on this dialog -> underlying popup must not get closed
  if (!this.isOrHasWidget(event.popup) &&
    !event.popup.isOrHasWidget(this) &&
    !this.session.focusManager.isElementCovertByGlassPane(this.$container[0])) {
    this.close(event);
  }
};

scout.Popup.prototype.prefLocation = function($container, openingDirectionY) {
  var x, y, anchorBounds, height, openingDirectionX;
  if (!this.anchorBounds && !this.$anchor) {
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

  return {
    x: x,
    y: y
  };
};

scout.Popup.prototype.getAnchorBounds = function() {
  var anchorBounds = this.anchorBounds;
  if (!anchorBounds) {
    anchorBounds = this.$anchor && scout.graphics.offsetBounds(this.$anchor);
  }
  return anchorBounds;
};

scout.Popup.prototype.overlap = function($container, location) {
  if (!$container || !location) {
    return;
  }
  var overlapX, overlapY,
    height = $container.outerHeight(),
    width = $container.outerWidth(),
    left = location.x,
    top = location.y,
    $window = $container.window();

  overlapX = left + width + this.windowPaddingX - $window.width();
  overlapY = top + height + this.windowPaddingY - $window.height();
  return {
    x: overlapX,
    y: overlapY
  };
};

scout.Popup.prototype.adjustLocation = function($container, location, switchIfNecessary) {
  var openingDirection, left, top,
    overlap = this.overlap($container, location);

  switchIfNecessary = scout.nvl(switchIfNecessary, true);
  if (overlap.y > 0 && switchIfNecessary) {
    // switch opening direction
    openingDirection = 'up';
    location = this.prefLocation($container, openingDirection);
  }
  left = location.x,
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
  this._position(this.$container, switchIfNecessary);
};

scout.Popup.prototype._position = function($container, switchIfNecessary) {
  var location = this.prefLocation($container);
  if (!location) {
    return;
  }
  location = this.adjustLocation($container, location, switchIfNecessary);
  this.setLocation(location);
};

scout.Popup.prototype.setLocation = function(location) {
  this.$container
    .css('left', location.x)
    .css('top', location.y);
  this._triggerLocationChanged();
};

scout.Popup.prototype._triggerLocationChanged = function() {
  this.trigger('locationChanged');
};

/**
 * Fire event that this popup is about to open.
 */
scout.Popup.prototype._triggerPopupOpenEvent = function() {
  this.session.desktop._trigger('popupopen', {
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
