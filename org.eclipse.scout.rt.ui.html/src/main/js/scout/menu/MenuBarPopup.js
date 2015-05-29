/**
 * The MenuBarPopup is a special Popup that is used in the menu-bar. It is tightly coupled with a menu-item and shows a header
 * which has a different size than the popup-body.
 */
scout.MenuBarPopup = function(menu, session) {
  scout.MenuBarPopup.parent.call(this, session);
  this.menu = menu;
};
scout.inherits(scout.MenuBarPopup, scout.ContextMenuPopup);

/**
 *  @override
 */
scout.MenuBarPopup.prototype._getMenuItems = function() {
  return this.menu.childActions || this.menu.menus;
};

/**
 *  @override
 */
scout.MenuBarPopup.prototype._beforeRenderMenuItems = function() {
  this._renderHead();
};

/**
 *  @override
 */
scout.MenuBarPopup.prototype._afterRenderMenuItems = function() {
  this.alignTo();
};

scout.MenuBarPopup.prototype._renderHead = function() {
  this.headText = this.menu.$container.text();
  this.headIcon = this.menu.$container.attr('data-icon');
  scout.MenuBarPopup.parent.prototype._renderHead.call(this);
  if (scout.Action.ActionStyle.TASK_BAR === this.menu.actionStyle) {
    this._copyCssClassToHead('taskbar-tool-item');
    this.$head.addClass('selected');
  } else {
    if (this.headIcon && this.headText) {
      this.$head.addClass('menu-textandicon');
    }
    this._copyCssClassToHead('button');
  }
  this._copyCssClassToHead('has-submenu');
};

scout.MenuBarPopup.prototype.onMenuItemClicked = function(menu) {
  this.closePopup();
  menu.sendDoAction();
};

scout.MenuBarPopup.prototype._copyCssClassToHead = function(className) {
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
    top = pos.top - 5,
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
  } else if (this.menu.$container.hasClass('taskbar-tool-item')) {
    top = pos.top;
    bodyTop = headSize.height - 1;
    this.$body.cssTop(bodyTop);
    this.$deco.cssTop(bodyTop);
  } else {
    left -= headInsets.left;
  }

  this.$head.cssLeft(0);
  this.$deco.cssLeft(1).width(headSize.width - 2);
  this.setLocation(new scout.Point(left, top));
};

scout.MenuBarPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.PopupMenuItemKeyStrokeAdapter(this);
};
