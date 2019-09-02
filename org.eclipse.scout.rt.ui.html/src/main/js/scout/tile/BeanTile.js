scout.BeanTile = function() {
  scout.BeanTile.parent.call(this);

  this.bean = null;
};
scout.inherits(scout.BeanTile, scout.Tile);

scout.BeanTile.prototype._render = function() {
  this.$container = this.$parent.appendDiv('bean-tile');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

scout.BeanTile.prototype._renderProperties = function() {
  scout.BeanTile.parent.prototype._renderProperties.call(this);
  this._renderBean();
};

scout.BeanTile.prototype._renderBean = function() {
  // to be implemented by the subclass
};

scout.BeanTile.prototype.triggerAppLinkAction = function(ref) {
  this.trigger('appLinkAction', {
    ref: ref
  });
};

scout.BeanTile.prototype._onAppLinkAction = function(event) {
  var $target = $(event.delegateTarget);
  var ref = $target.data('ref');
  this.triggerAppLinkAction(ref);
};
