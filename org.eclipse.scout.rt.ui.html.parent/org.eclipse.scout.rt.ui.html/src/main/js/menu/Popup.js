// ---- Popup ----
scout.Popup = function() {
  this.$container;
  this.$body;
};

/**
 * The popup is always appended to the HTML document body.
 * That way we never have z-index issues with the rendered popups.
 */
scout.Popup.prototype.render = function() {
  var $docBody = $('body');
  this.$body = $.makeDIV('popup-body');
  this.$container = $.makeDIV('popup').
    append(this.$body).
    appendTo($docBody);
  this._attachCloseHandler();
  return this.$container;
};

/**
 * Every user action will close menu. menu is removed in 'click' event,
 */
scout.Popup.prototype._attachCloseHandler = function() {
  $(document).one(scout.menus.CLOSING_EVENTS, this.remove.bind(this));
};

scout.Popup.prototype.appendToBody = function($element) {
  this.$body.append($element);
};

scout.Popup.prototype.addClassToBody = function(clazz) {
  this.$body.addClass(clazz);
};

scout.Popup.prototype.remove = function() {
  this.$container.remove();
  // remove all clean-up handlers
  $(document).off('.contextMenu');
};

scout.Popup.prototype.setLocation = function(location) {
  this.$container.
   css('left', location.x).
   css('top', location.y);
};

/** ---- PopupMenuItem ----
 * A popup as used in the menu-bar. The popup is tightly coupled with a menu-item and shows a header
 * which has a different size than the popup-body.
 */
scout.PopupMenuItem = function($menuItem) {
  scout.PopupMenuItem.parent.call(this);
  this.$menuItem = $menuItem;
  this.$head;
  this.deco;
};
scout.inherits(scout.PopupMenuItem, scout.Popup);

scout.PopupMenuItem.prototype.render = function($parent) {
  scout.PopupMenuItem.parent.prototype.render($parent);
  this.$head = $.makeDIV('popup-head');
  this.$deco = $.makeDIV('popup-deco');
  this.$container.
    prepend(this.$head).
    append(this.$deco);

  var text = this.$menuItem.text(),
    dataIcon = this.$menuItem.attr('data-icon');

  this.$head.text(text);
  if (dataIcon) {
    this.$head.attr('data-icon', dataIcon);
  }
  if (dataIcon && text) {
    this.$head.addClass('menu-textandicon');
  }
  if (this.$menuItem.hasClass('has-submenu')) {
    this.$head.addClass('has-submenu');
  }
  return this.$container;
};

scout.PopupMenuItem.prototype.alignTo = function() {
  var pos = this.$menuItem.offset(),
    headWidth = this.$head.outerWidth(true),
    bodyWidth = this.$body.outerWidth(true);

  // body min-width
  if (bodyWidth < headWidth) {
    this.$body.width(headWidth - 2);
    bodyWidth = headWidth;
  }

  // horiz. alignment
  var itemInsets = scout.graphics.getInsets(this.$menuItem),
    headInsets = scout.graphics.getInsets(this.$head),
    left = pos.left,
    top = pos.top + itemInsets.top - 1;

  $.log.debug('headWidth=' + headWidth + ' bodyWidth=' + bodyWidth + ' pos=[left' + pos.left + ' top=' + pos.top +
      '] itemInsets=' + itemInsets + ' headInsets=' + headInsets + ' left=' + left + ' top=' + top);
  if (this.$menuItem.hasClass('menu-right')) {
    // when we use float:right, browser uses fractions of pixels, that's why we must
    // use the subPixelCorr variable. It corrects some visual pixel-shifting issues.
    var widthDiff = bodyWidth - headWidth,
      subPixelCorr = left - Math.floor(left);
    left -= widthDiff + headInsets.left;
    this.$head.css('left', widthDiff);
    this.$body.css('left', subPixelCorr);
    this.$deco.
      css('left', widthDiff + 1).
      width(headWidth - 2 + subPixelCorr);
    $.log.debug('right alignment: widthDiff=' + widthDiff + ' subPixelCorr=' + subPixelCorr);
  } else {
    left -= headInsets.left;
    this.$head.css('left', 0);
    this.$deco.
      css('left', 1).
      width(headWidth - 2);
  }

  this.setLocation(new scout.Point(left, top));
};

