scout.DesktopNavigationPopup = function(desktopNavigation, session) {
  scout.DesktopNavigationPopup.parent.call(this, session);
  this.desktopNavigation = desktopNavigation;
  this.$menuItem = desktopNavigation.$menuButton;
  this.$head;
  this.$deco;
  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
};
scout.inherits(scout.DesktopNavigationPopup, scout.Popup);

scout.DesktopNavigationPopup.prototype._render = function($parent) {
  scout.DesktopNavigationPopup.parent.prototype._render.call(this, $parent);
  this._renderHead();

  this.desktopNavigation.desktop.viewButtons.forEach(function(viewButton) {
    viewButton.render(this.$body);
    viewButton.$container.on('click', '', this._onViewButtonClick.bind(this, viewButton));
    this.addChild(viewButton);
  }.bind(this));
  this.alignTo();
};

scout.DesktopNavigationPopup.prototype._onViewButtonClick = function(viewButton) {
  viewButton.doAction();
  this.remove();
};

scout.DesktopNavigationPopup.prototype._renderHead = function() {
  scout.DesktopNavigationPopup.parent.prototype._renderHead.call(this);
  this._copyCssClassToHead('navigation-tab-outline-button');
  this.$head.addClass('navigation-header');
};

scout.DesktopNavigationPopup.prototype._copyCssClassToHead = function(className) {
  if (this.$menuItem.hasClass(className)) {
    this.$head.addClass(className);
  }
};

scout.DesktopNavigationPopup.prototype.alignTo = function() {


  var pos = this.$menuItem.offset(),
    headSize = scout.graphics.getSize(this.$head, true);
  // horiz. alignment
  var left = pos.left,
    top = pos.top,
    bodyTop = headSize.height;
  $.log.debug(' pos=[left' + pos.left + ' top=' + pos.top + '] headSize=' + headSize + ' left=' + left + ' top=' + top);
  this.$body.cssTop(bodyTop);
  var offsetBounds;
  if(this.$menuItem.parents('.navigation-breadcrumb').length > 0){
    offsetBounds = scout.graphics.offsetBounds(this.desktopNavigation.activeTab.$tab.parent());
    this.$head.cssWidth(offsetBounds.width/2);
    this.$deco.cssWidth(offsetBounds.width/2-2);
  }
  else{
    offsetBounds = scout.graphics.offsetBounds(this.desktopNavigation.activeTab.$tab);
    this.$deco.cssWidth(headSize.width - 2);
  }

  this.$body.cssWidth(offsetBounds.width - 2);
  this.$deco.cssTop(bodyTop);
  this.$head.cssLeft(0);
  this.$deco.cssLeft(1);
  this.setLocation(new scout.Point(left, top));
};

scout.DesktopNavigationPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.DesktopNavigationPopupKeyStrokeAdapter(this);
};
