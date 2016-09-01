scout.Composite = function() {
  scout.Composite.parent.call(this);

  this.widgets = [];
  this._addAdapterProperties(['widgets']);
};
scout.inherits(scout.Composite, scout.Widget);

scout.Composite.prototype._render = function($parent) {
  this.$container = $parent.appendDiv();
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
};

scout.Composite.prototype._renderProperties = function() {
  this._renderWidgets();
};

scout.Composite.prototype.setWidgets = function(widgets) {
  this.setProperty('widgets', widgets);
};

scout.Composite.prototype._renderWidgets = function($parent) {
  this.widgets.forEach(function(widget) {
    widget.render(this.$container);
  }, this);
  this.invalidateLayoutTree();
};
