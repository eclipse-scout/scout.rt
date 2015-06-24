scout.ViewMenuPopup = function($tab, viewMenus, naviBounds, session) {
  scout.ViewMenuPopup.parent.call(this, session);
  this.$tab = $tab;
  this.viewMenus = viewMenus;
  this.naviBounds = naviBounds;
};
scout.inherits(scout.ViewMenuPopup, scout.Popup);

scout.ViewMenuPopup.prototype._render = function($parent) {
  scout.ViewMenuPopup.parent.prototype._render.call(this, $parent);
  this._renderHead();

  this.viewMenus.forEach(function(viewMenu) {
    viewMenu.render(this.$body);
    viewMenu.afterSendDoAction = this.closePopup.bind(this);
    this.addChild(viewMenu);
  }, this);
  this.alignTo();
};

scout.ViewMenuPopup.prototype._renderHead = function() {
  scout.ViewMenuPopup.parent.prototype._renderHead.call(this);
  this._copyCssClassToHead('navigation-tab-outline-button');
  this.$head.addClass('navigation-header');
};

scout.ViewMenuPopup.prototype._copyCssClassToHead = function(className) {
  if (this.$tab.hasClass(className)) {
    this.$head.addClass(className);
  }
};

scout.ViewMenuPopup.prototype.alignTo = function() {
  var pos = this.$tab.offset(),
    headSize = scout.graphics.getSize(this.$head, true);
  // horiz. alignment
  var left = pos.left,
    top = pos.top,
    bodyTop = headSize.height;
  $.log.debug(' pos=[left' + pos.left + ' top=' + pos.top + '] headSize=' + headSize + ' left=' + left + ' top=' + top);
  this.$body.cssTop(bodyTop);
  var offsetBounds;
  if(this.$tab.parents('.navigation-breadcrumb').length > 0) { // XXX
    offsetBounds = scout.graphics.offsetBounds(this.desktopNavigation.activeTab.$tab.parent());
    this.$head.cssWidth(offsetBounds.width/2);
    this.$deco.cssWidth(offsetBounds.width/2-2);
  }
  else {
    offsetBounds = scout.graphics.offsetBounds(this.$tab);
    this.$deco.cssWidth(headSize.width - 2);
  }

  this.$body.cssWidth(this.naviBounds.width - 2);
  this.$deco.cssTop(bodyTop);
  this.$head.cssLeft(0);
  this.$deco.cssLeft(1);
  this.setLocation(new scout.Point(left, top));
};

scout.ViewMenuPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.ViewMenuPopupKeyStrokeAdapter(this);
};
