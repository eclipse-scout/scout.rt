scout.MobileOutline = function() {
  scout.MobileOutline.parent.call(this);
  this._breadcrumb = true;
};
scout.inherits(scout.MobileOutline, scout.Outline);

scout.MobileOutline.prototype._render = function($parent) {
  scout.MobileOutline.parent.prototype._render.call(this, $parent);

  //FIXME CGU really?
  $parent.addClass('navigation-breadcrumb');
};

scout.MobileOutline.prototype._showDefaultDetailForm = function() {

};

scout.MobileOutline.prototype._updateOutlineTab = function(node) {

};
