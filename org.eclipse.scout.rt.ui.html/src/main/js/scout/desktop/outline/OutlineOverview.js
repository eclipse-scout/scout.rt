scout.OutlineOverview = function() {
  scout.OutlineOverview.parent.call(this);

  this.outline;
  this.attached;
};
scout.inherits(scout.OutlineOverview, scout.Widget);

scout.OutlineOverview.prototype._init = function(options) {
  scout.OutlineOverview.parent.prototype._init.call(this, options);

  this.outline = options.outline;
  this.id = options.id; // actually only necessary for a spec...
};

scout.OutlineOverview.prototype._render = function($parent) {
  this._$parent = $parent;
  this.$container = $parent.appendDiv('outline-overview');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.$content = this.$container.appendDiv('outline-overview-content');
  this.$content.appendDiv('outline-overview-icon').icon(this.outline.iconId);
  this.$content.appendDiv('outline-overview-title').text(this.outline.title);
  this.attached = true;
};

/**
 * Method required for objects that act as 'outlineContent'
 */
scout.OutlineOverview.prototype.attach = function() {
  if (this.attached || !this.rendered) {
    return;
  }

  this._$parent.append(this.$container);

  var htmlParent = this.htmlComp.getParent();
  this.htmlComp.setSize(htmlParent.getSize());

  this.session.detachHelper.afterAttach(this.$container);

  this.attached = true;
};

/**
 * Method required for objects that act as 'outlineContent'
 */
scout.OutlineOverview.prototype.detach = function() {
  if (!this.attached || !this.rendered) {
    return;
  }

  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.detach();

  this.attached = false;
};

scout.OutlineOverview.prototype.onResize = function() {

};
