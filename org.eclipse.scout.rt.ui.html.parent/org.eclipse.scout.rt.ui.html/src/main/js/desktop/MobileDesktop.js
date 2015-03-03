scout.MobileDesktop = function() {
  scout.MobileDesktop.parent.call(this);

};
scout.inherits(scout.MobileDesktop, scout.Desktop);

/**
 * @override
 */
scout.MobileDesktop.prototype._render = function($parent) {
  this.$parent = $parent;

  this.navigation = new scout.DesktopNavigation(this);
  this.navigation.render($parent);
  this.navigation.onOutlineChanged(this.outline);
};
