/**
 * The MenuBarPopup is a special Popup that is used in the menu-bar. It is tightly coupled with a menu-item and shows a header
 * which has a different size than the popup-body.
 */
scout.MenuBarPopup = function(menu, session) {
  scout.MenuBarPopup.parent.call(this, session);
  this.menu = menu;
  this.$head;
  this.$deco;
};
scout.inherits(scout.MenuBarPopup, scout.Popup);

scout.MenuBarPopup.prototype._render = function($parent) {
  scout.MenuBarPopup.parent.prototype._render.call(this, $parent);
  this.$head = $.makeDiv('popup-head');
  this.$deco = $.makeDiv('popup-deco');
  this.$container
    .prepend(this.$head)
    .append(this.$deco);

  var text = this.menu.$container.text(),
    dataIcon = this.menu.$container.attr('data-icon');

  this.$head.text(text);
  if (dataIcon) {
    this.$head.attr('data-icon', dataIcon);
  }
  if (dataIcon && text) {
    this.$head.addClass('menu-textandicon');
  }
  this._copyCssClass('has-submenu');
  this._copyCssClass('taskbar');
  this._copyCssClass('button');

  var menus = this.menu.childActions || this.menu.menus;
  if (!menus || menus.length === 0) {
    return;
  }

  for (var i = 0; i < menus.length; i++) {
    var menu = menus[i];
    if (!menu.visible) {
      continue;
    }
    if (menu.separator) {
      continue;
    }
    menu.sendAboutToShow();
    this.$body.appendDiv('menu-item')
      .text(menu.text)
      .on('click', '', this.onMenuItemClicked.bind(this, menu));
  }

  this.alignTo();
};

scout.MenuBarPopup.prototype.onMenuItemClicked = function(menu) {
  this.closePopup();
  menu.sendDoAction();
};

scout.MenuBarPopup.prototype._copyCssClass = function(className) {
  if (this.menu.$container.hasClass(className)) {
    this.$head.addClass(className);
  }
};

scout.MenuBarPopup.prototype.alignTo = function() {
  var pos = this.menu.$container.offset(),
    headSize = scout.graphics.getSize(this.$head, true),
    bodyWidth = scout.graphics.getSize(this.$body, true).width;

  // body min-width
  if (bodyWidth < headSize.width) {
    this.$body.width(headSize.width - 2);
    bodyWidth = headSize.width;
  }

  // horiz. alignment
  var left = pos.left,
    top = pos.top,
    headInsets = scout.graphics.getInsets(this.$head),
    bodyTop = headSize.height;

  $.log.debug('bodyWidth=' + bodyWidth + ' pos=[left' + pos.left + ' top=' + pos.top + '] headSize=' + headSize +
    ' headInsets=' + headInsets + ' left=' + left + ' top=' + top);
  this.$body.cssTop(bodyTop);
  this.$deco.cssTop(bodyTop);
  if (this.menu.$container.hasClass('right-aligned')) {
    // when we use float:right, browser uses fractions of pixels, that's why we must
    // use the subPixelCorr variable. It corrects some visual pixel-shifting issues.
    var widthDiff = bodyWidth - headSize.width,
      subPixelCorr = left - Math.floor(left);
    left -= widthDiff + headInsets.left;
    this.$head.cssLeft(widthDiff);
    this.$body.cssLeft(subPixelCorr);
    this.$deco.cssLeft(widthDiff + 1).width(headSize.width - 2 + subPixelCorr);
    $.log.debug('right alignment: widthDiff=' + widthDiff + ' subPixelCorr=' + subPixelCorr);
  } else {
    left -= headInsets.left;
    this.$head.cssLeft(0);
    this.$deco.cssLeft(1).width(headSize.width - 2);
  }

  this.setLocation(new scout.Point(left, top));
};

scout.MenuBarPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.PopupMenuItemKeyStrokeAdapter(this);
};

scout.MenuBarPopup.prototype._onMouseDown = function(event) {
  if(this.$head && this.$head[0]===event.target){
    this.closePopup();
  }
};
