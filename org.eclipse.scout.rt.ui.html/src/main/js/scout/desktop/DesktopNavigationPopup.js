/** ---- DesktopNavigationPopup ----
 * A popup as used in the menu-bar. The popup is tightly coupled with a menu-item and shows a header
 * which has a different size than the popup-body.
 */
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
  this.$head = $.makeDiv('popup-head');
  this.$deco = $.makeDiv('popup-deco');
  this.$container
    .prepend(this.$head)
    .append(this.$deco);

  this.$head.attr('data-icon', '\uF0C9');
  this.$head.addClass('navigation-header');
  this._copyCssClass('navigation-tab-outline-button');
  this._copyCssClass('.navigation-header');

  if (!this.desktopNavigation.desktop.viewButtons || this.desktopNavigation.desktop.viewButtons.length === 0) {
    return;
  }
  var i,
    onMenuItemClicked = function(menu) {
      menu.doAction();
      this.remove();
    };
  for (i = 0; i < this.desktopNavigation.desktop.viewButtons.length; i++) {
    var menu = this.desktopNavigation.desktop.viewButtons[i];
    menu.$container = this.$body.appendDiv('outline-menu-item')
      .text(menu.text)
      .on('click', '', onMenuItemClicked.bind(this, menu));
  }
  this.alignTo();
};

scout.DesktopNavigationPopup.prototype._copyCssClass = function(className) {
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
  var offsetBounds = scout.graphics.offsetBounds(this.desktopNavigation.activeTab.$tab);
  this.$body.cssWidth(offsetBounds.width - 2);
  this.$deco.cssTop(bodyTop);
  this.$head.cssLeft(0);
  this.$deco.cssLeft(1).width(headSize.width - 2);
  this.setLocation(new scout.Point(left, top));
};

scout.DesktopNavigationPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.DesktopNavigationPopupKeyStrokeAdapter(this);
};

scout.DesktopNavigationPopup.prototype._onMouseDown = function(event) {
  if(this.$head && this.$head[0]===event.target){
    this.closePopup();
  }
};
