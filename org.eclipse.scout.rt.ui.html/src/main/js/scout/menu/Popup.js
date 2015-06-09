scout.Popup = function(session) {
  scout.Popup.parent.call(this);
  this.$body;
  this.$head;
  this.$deco;
  this._mouseDownHandler;
  this.session = session;
  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
};
scout.inherits(scout.Popup, scout.Widget);

/**
 * The popup is always appended to the HTML document body.
 * That way we never have z-index issues with the rendered popups.
 */
scout.Popup.prototype.render = function($parent) {
  scout.Popup.parent.prototype.render.call(this, $parent);
  setTimeout(function() {
    // $container maybe null if removed directly after render
    if (this.$container) {
      this.$container.installFocusContext('auto', this.session.uiSessionId);
    }
  }.bind(this), 0);
  this._attachCloseHandler();
};

scout.Popup.prototype.remove = function() {
  if (!this.rendered) {
    return;
  }
  this.$container.uninstallFocusContext(this.session.uiSessionId);
  scout.Popup.parent.prototype.remove.call(this);
  // remove all clean-up handlers
  this._detachCloseHandler();
  this.rendered = false;
};

scout.Popup.prototype._render = function($parent) {
  if (!$parent) {
    $parent = this.session.$entryPoint;
  }
  this.$body = $.makeDiv('popup-body');
  this.$container = $.makeDiv('popup')
    .append(this.$body)
    .appendTo($parent);
};

scout.Popup.prototype.rerenderHead = function() {
  this._removeHead();
  this._renderHead();
};

/**
 * Will not be called by this._render, sub classes have explicitly call this method
 */
scout.Popup.prototype._renderHead = function() {
  this.$head = $.makeDiv('popup-head');
  this.$deco = $.makeDiv('popup-deco');
  this.$container
    .prepend(this.$head)
    .append(this.$deco);
  this.$head.on('mousedown', '', this._onHeadMouseDown.bind(this));
  this.$head.text(this.headText);
  if (this.headIcon) {
    this.$head.attr('data-icon', this.headIcon);
  }
};

scout.Popup.prototype._removeHead = function() {
  if (this.$head) {
    this.$head.remove();
  }
  if (this.$deco) {
    this.$deco.remove();
  }
};

scout.Popup.prototype.closePopup = function() {
  this.remove();
};

/**
 * click outside container, scroll down closes popup
 */
scout.Popup.prototype._attachCloseHandler = function() {
  this._mouseDownHandler = this._onMouseDown.bind(this);
  $(document).on('mousedown', this._mouseDownHandler);

  if (this.$anchor) {
    this._scrollHandler = this._onAnchorScroll.bind(this);
    scout.scrollbars.onScroll(this.$anchor, this._scrollHandler);
  }
};

scout.Popup.prototype._detachCloseHandler = function() {
  if (this._scrollHandler) {
    scout.scrollbars.offScroll(this._scrollHandler);
    this._$scrollParents = null;
  }
  if (this._mouseDownHandler) {
    $(document).off('mousedown', this._mouseDownHandler);
    this._mouseDownHandler = null;
  }
};

scout.Popup.prototype._onMouseDown = function(event) {
  var $target = $(event.target);
  // close the popup only if the click happened outside of the popup
  if (this.$container && this.$container.has($target).length === 0) {
    this._onMouseDownOutside(event);
  }
};

scout.Popup.prototype._onMouseDownOutside = function(event) {
  this.closePopup();
};

scout.Popup.prototype._onHeadMouseDown = function(event) {
  if (this.$head && this.$head[0] === event.target) {
    this.closePopup();
  }
};

scout.Popup.prototype._onAnchorScroll = function(event) {
  this.remove();
};

scout.Popup.prototype.appendToBody = function($element) {
  this.$body.append($element);
};

scout.Popup.prototype.addClassToBody = function(clazz) {
  this.$body.addClass(clazz);
};

scout.Popup.prototype.setLocation = function(location) {
  this.$container
    .css('left', location.x)
    .css('top', location.y);
};

scout.Popup.prototype._createKeyStrokeAdapter = function() {
  return new scout.PopupKeyStrokeAdapter(this);
};
