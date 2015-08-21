/**
 * The MenuBarPopup is a special Popup that is used in the menu-bar. It is tightly coupled with a menu-item and shows a header
 * which has a different size than the popup-body.
 */
scout.MenuBarPopup = function(menu, session, options) {
  options = options || {};
  options.triggerPopupOpenEvent = scout.helpers.nvl(options.triggerPopupOpenEvent, true);
  options.$anchor = menu.$container;
  scout.MenuBarPopup.parent.call(this, session, options);
  this.menu = menu;
  this.$headBlueprint = this.menu.$container;
  this.ignoreEvent = options.ignoreEvent;
  this._headVisible = true;
};
scout.inherits(scout.MenuBarPopup, scout.ContextMenuPopup);

/**
 * @override ContextMenuPopup.js
 */
scout.MenuBarPopup.prototype._getMenuItems = function() {
  return this.menu.childActions || this.menu.menus;
};

/**
 * @override Popup.js
 */
scout.MenuBarPopup.prototype.close = function(event) {
  if (!event || !this.ignoreEvent || event.originalEvent !== this.ignoreEvent.originalEvent) {
    scout.MenuBarPopup.parent.prototype.close.call(this);
  }
};

/**
 * @override PopupWithHead.js
 */
scout.MenuBarPopup.prototype._renderHead = function() {
  scout.MenuBarPopup.parent.prototype._renderHead.call(this);

  // FIXME AWE throws exception if this.menu is a button because button is not rendered (MenuButtonAdapter is)
  if (this.menu.$container.parent().hasClass('main-menubar')) {
    this.$head.addClass('in-main-menubar');
  }

  if (scout.Action.ActionStyle.TASK_BAR === this.menu.actionStyle) {
    this._copyCssClassToHead('taskbar-tool-item');
    this._copyCssClassToHead('unfocusable');
    this.$head.addClass('selected');
  } else {
    this._copyCssClassToHead('button');
    this._copyCssClassToHead('menu-textandicon');
  }
  this._copyCssClassToHead('has-submenu');
};

/**
 * @override Popup.js
 */
scout.MenuBarPopup.prototype._position = function($container) {
  var openingDirectionX, openingDirectionY, left, top, overlap, pos;

  this.alignTo();
  pos = $container.offset();
  overlap = this.overlap($container, {
    x: pos.left,
    y: pos.top
  });
  if (overlap.y > 0) {
    // switch opening direction
    openingDirectionY = 'up';
  }
  if (overlap.x > 0) {
    // switch opening direction
    openingDirectionX = 'left';
  }
  if (openingDirectionX || openingDirectionY) {
    // Align again if openingDirection has to be switched
    this.alignTo(openingDirectionX, openingDirectionY);
  }
};

scout.MenuBarPopup.prototype.alignTo = function(openingDirectionX, openingDirectionY) {
  var pos, headSize, bodySize, bodyWidth, widthDiff, subPixelCorr, $blueprintChildren, left, top, headInsets, menuInsets,
    bodyTop = 0,
    headTop = 0,
    decoTop = 0;

  if (this.menu.$container.hasClass('right-aligned')) {
    openingDirectionX = 'left';
  }
  openingDirectionX = openingDirectionX || 'right';
  openingDirectionY = openingDirectionY || 'down';
  this.$container.removeClass('up down');
  this.$body.removeClass('up down');
  this.$container.addClass(openingDirectionY);
  this.$body.addClass(openingDirectionY);

  // Make sure the elements inside the header have the same style as to blueprint (menu)
  // This makes it possible to position the content in the header (icon, text) exactly on top of the content of the blueprint
  this.$head.copyCss(this.menu.$container, 'line-height');
  $blueprintChildren = this.menu.$container.children();
  this.$head.children().each(function(i) {
    var $headChild = $(this);
    var $blueprintChild = $blueprintChildren.eq(i);
    $headChild.copyCss($blueprintChild, 'margin padding line-height border vertical-align');
  });

  headSize = scout.graphics.getSize(this.$head, true);
  bodySize = scout.graphics.getSize(this.$body, true);
  bodyWidth = bodySize.width;

  // body min-width
  if (bodyWidth < headSize.width) {
    this.$body.width(headSize.width - 2);
    bodyWidth = headSize.width;
  }

  pos = this.menu.$container.offset();
  left = pos.left;
  headInsets = scout.graphics.getInsets(this.$head);
  menuInsets = scout.graphics.getInsets(this.menu.$container);
  top = pos.top - headInsets.top + menuInsets.top;

  if (openingDirectionY === 'up') {
    top -= bodySize.height;
    headTop = bodySize.height;
    decoTop = headTop - 1; // -1 is body border (the purpose of deco is to hide the body border)
  } else if (openingDirectionY === 'down') {
    bodyTop += headSize.height;
    decoTop = bodyTop;
  }

  $.log.debug('bodyWidth=' + bodyWidth + ' pos=[left' + pos.left + ' top=' + pos.top + '] headSize=' + headSize +
    ' headInsets=' + headInsets + ' left=' + left + ' top=' + top);
  this.$head.cssTop(headTop);
  this.$body.cssTop(bodyTop);
  this.$deco.cssTop(decoTop);

  if (openingDirectionX === 'left') {
    // when we use float:right, browser uses fractions of pixels, that's why we must
    // use the subPixelCorr variable. It corrects some visual pixel-shifting issues.
    widthDiff = bodyWidth - headSize.width;
    subPixelCorr = left - Math.floor(left);
    left -= widthDiff + headInsets.left - menuInsets.left;
    this.$head.cssLeft(widthDiff);
    this.$body.cssLeft(subPixelCorr);
    this.$deco.cssLeft(widthDiff + this.$head.cssBorderLeftWidth())
      .width(headSize.width - this.$head.cssBorderWidthX() + subPixelCorr);
  } else {
    left -= headInsets.left;
    this.$head.cssLeft(0);
    this.$deco.cssLeft(1)
      .width(headSize.width - 2);
  }

  this.setLocation(new scout.Point(left, top));
};
