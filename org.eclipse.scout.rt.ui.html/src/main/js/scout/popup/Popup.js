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
  this.windowPaddingX = scout.helpers.nvl(options.windowPaddingX, 10);
  this.windowPaddingY = scout.helpers.nvl(options.windowPaddingY, 5);
  this.withFocusContext = scout.helpers.nvl(options.installFocusContext, true);
  this.initialFocus = scout.helpers.nvl(options.initialFocus, function() {
    return scout.focusRule.AUTO;
  });
  this.focusableContainer = scout.helpers.nvl(options.focusableContainer, false);
};

scout.Popup.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  keyStrokeContext.registerKeyStroke(new scout.CloseKeyStroke(this));
};

scout.Popup.prototype.render = function($parent, event) {
  this.openEvent = event;
  scout.Popup.parent.prototype.render.call(this, $parent);
};

scout.Popup.prototype._render = function($parent) {
  this.$container = $.makeDiv('popup').appendTo($parent || this.session.$entryPoint);

  // Add programmatic 'tabindex' if the $container itself should be focusable (used by context menu popups with no focusable elements)
  if (this.withFocusContext && this.focusableContainer) {
    this.$container.attr('tabindex', -1);
  }
};

scout.Popup.prototype._postRender = function() {
  // position must be set _before_ focus is installed, otherwise we'd focus an element
  // that is currently not on the screen. Which would cause the whole desktop to
  // be shifted for a few pixels.
  this.size();
  this.position();

  if (this.withFocusContext) {
    this.session.focusManager.installFocusContext(this.$container, this.initialFocus());
  }

  this._triggerPopupOpenEvent();
  this._attachCloseHandler();
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
  $(document).on('mousedown', this._mouseDownHandler);

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
    $(document).off('mousedown', this._mouseDownHandler);
    this._mouseDownHandler = null;
  }
};

scout.Popup.prototype._onMouseDown = function(event) {
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
  // Make sure child popups don't close the parent popup
  // Use case: Opening of a context menu or cell editor in a form popup
  if (!this.isOrHasWidget(event.popup)) {
    this.close(event);
  }
};

scout.Popup.prototype.prefLocation = function($container, openingDirectionY) {
  var x, y, anchorBounds, height;
  if (!this.anchorBounds && !this.$anchor) {
    return;
  }
  openingDirectionY = openingDirectionY || 'down';
  $container.removeClass('up down');
  $container.addClass(openingDirectionY);
  height = $container.outerHeight(true),

  anchorBounds = this.anchorBounds;
  if (!anchorBounds) {
    anchorBounds = this.$anchor && scout.graphics.offsetBounds(this.$anchor);
  }

  x = anchorBounds.x;
  y = anchorBounds.y;
  if (openingDirectionY === 'up') {
    y -= height;
  } else if (openingDirectionY === 'down') {
    y += anchorBounds.height;
  }

  // FIXME AWE/CGU: for smartfield-popups we must adjust the popup-size
  // so the popup fits into the available space. It's simple to calculate
  // the correct size, but it's not so easy to resize the popup-container
  // since this method here is called in the middle of a layout-operation
  // of the SmartfieldPopupLayout. When we resize the contaienr here it
  // will result in an endless loop. We should:
  // 1. calc the pref. size of the popup
  // 2. find the right position for the popup (up, down)
  // 3. check if the popup size is small enough to be completely visible
  //    in the current window
  // 4. if required reduce the popup-size, so it will fit into the window
  // 5. finally set the size and layout the popup and its children.
  //
  // To avoid that the popup switches between up and down position while
  // typing, we could always find the position by using the max. popup
  // size instead by checking against the current size.
  return {
    x: x,
    y: y
  };
};

scout.Popup.prototype.overlap = function($container, location) {
  if (!$container || !location) {
    return;
  }
  var overlapX, overlapY,
    height = $container.outerHeight(),
    width = $container.outerWidth(),
    left = location.x,
    top = location.y;

  overlapX = left + width + this.windowPaddingX - $(window).width();
  overlapY = top + height + this.windowPaddingY - $(window).height();
  return {
    x: overlapX,
    y: overlapY
  };
};

scout.Popup.prototype.adjustLocation = function($container, location, anchorBounds) {
  var openingDirection, left, top,
    overlap = this.overlap($container, location);

  if (overlap.y > 0) {
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

scout.Popup.prototype.position = function() {
  this._position(this.$container);
};

scout.Popup.prototype._position = function($container) {
  var location = this.prefLocation($container);
  if (!location) {
    return;
  }
  location = this.adjustLocation($container, location);
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
