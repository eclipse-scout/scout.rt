/**
 * Popup menu to switch between outlines.
 */
scout.ViewMenuPopup = function(session, $tab, viewMenus, naviBounds, breadcrumbEnabled) {
  scout.ViewMenuPopup.parent.call(this, session, {
    focusableContainer: true
  });
  this.$tab = $tab;
  this.$headBlueprint = this.$tab;
  this.viewMenus = viewMenus;
  this._naviBounds = naviBounds;
  this._breadcrumbEnabled = breadcrumbEnabled;
  this._tooltip;
  this._tooltipDelay;
};
scout.inherits(scout.ViewMenuPopup, scout.PopupWithHead);

scout.ViewMenuPopup.MAX_MENU_WIDTH = 300;

/**
 * @override Popup.js
 */
scout.ViewMenuPopup.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.ViewMenuPopup.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  scout.menuNavigationKeyStrokes.registerKeyStrokes(keyStrokeContext, this, 'view-button-menu');
};

scout.ViewMenuPopup.prototype._render = function($parent) {
  scout.ViewMenuPopup.parent.prototype._render.call(this, $parent);

  this.viewMenus.forEach(function(viewMenu) {
    viewMenu.render(this.$body);
    viewMenu.afterSendDoAction = this.close.bind(this);
    this.addChild(viewMenu);
  }, this);
  this.alignTo();
};

/**
 * @override PopupWithHead.js
 */
scout.ViewMenuPopup.prototype._renderHead = function() {
  scout.ViewMenuPopup.parent.prototype._renderHead.call(this);

  this._copyCssClassToHead('view-button-tab');
  this._copyCssClassToHead('unfocusable');
  this.$head.removeClass('popup-head'); // FIXME AWE: use CSS class?
  this.$head.css('background-color', 'white');
  this.$head.css('color', '#006c86');
};

/**
 * @override PopupWithHead.js
 */
scout.ViewMenuPopup.prototype._modifyBody = function() {
  this.$body.removeClass('popup-body');
  this.$body.addClass('view-menu-popup-body');
};

/**
 * @override PopupWithHead.js
 */
scout.ViewMenuPopup.prototype._modifyHeadChildren = function() {
  var $icon = this.$head.find('.icon'),
    $viewMenuButton = this.$head.find('.view-menu-button');

  $icon.css('font-size', 20);
  $icon.css('display', 'inline-block');

  $viewMenuButton.addClass('menu-open');
};

scout.ViewMenuPopup.prototype.alignTo = function() {
  var pos = this.$tab.offset(),
    headSize = scout.graphics.getSize(this.$tab, true),
    bodyTop = headSize.height;

  scout.graphics.setBounds(this.$head, pos.left, pos.top, headSize.width, headSize.height);

  this.$deco.cssLeft(pos.left);
  this.$deco.cssTop(bodyTop);
  this.$deco.cssWidth(headSize.width - 1);

  this.$body.cssWidth(Math.min(scout.ViewMenuPopup.MAX_MENU_WIDTH, this._naviBounds.width));
  this.$body.cssTop(bodyTop);

  this.setLocation(new scout.Point(0, 0));
};
