scout.GraphField = function() {
  scout.GraphField.parent.call(this);
  this._addAdapterProperties(['graph']);
};
scout.inherits(scout.GraphField, scout.FormField);

scout.GraphField.prototype._render = function($parent) {
  this.addContainer($parent, 'graph-field');
  this.addLabel();
  this.addStatus();
  this._renderGraph();
};

scout.GraphField.prototype._remove = function() {
  if (this.graph) {
    this.graph.remove();
  }
};

scout.GraphField.prototype._renderGraph = function() {
  if (this.graph) {
    this.graph.render(this.$container);
    this.addField(this.graph.$container);
  }
};

scout.GraphField.prototype._removeGraph = function(oldGraph) {
  oldGraph.remove();
  this.removeField();
};
