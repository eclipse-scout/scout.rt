scout.Composite = function() {
  scout.Composite.parent.call(this);

  this.widgets = [];
  this._addAdapterProperties(['widgets']);
};
scout.inherits(scout.Composite, scout.Widget);

scout.Composite.prototype._render = function() {
  this.$container = this.$parent.appendDiv();
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

scout.Composite.prototype._renderProperties = function() {
  scout.Composite.parent.prototype._renderProperties.call(this);
  this._renderWidgets();
};

scout.Composite.prototype.setWidgets = function(widgets) {
  this.setProperty('widgets', widgets);
};

scout.Composite.prototype._renderWidgets = function() {
  this.widgets.forEach(function(widget) {
    widget.render();
  }, this);
  this.invalidateLayoutTree();
};
