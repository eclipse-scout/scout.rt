// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GraphTableControl = function() {
  scout.GraphTableControl.parent.call(this);
  this._addAdapterProperties(['graph']);
};
scout.inherits(scout.GraphTableControl, scout.TableControl);

scout.GraphTableControl.prototype._renderContent = function($parent) {
  this.$parent = $parent;
  if (this.graph) {
    this.graph.render(this.$parent);
  }
};

  scout.GraphTableControl.prototype._removeContent = function() {
  if (this.graph) {
    this.graph.remove();
  }
};

scout.GraphTableControl.prototype._renderGraph = function() {
  this.renderContent();
};

scout.GraphTableControl.prototype._removeGraph = function() {
  this.removeContent();
};

scout.GraphTableControl.prototype.isContentAvailable = function() {
  return !!this.graph;
};

scout.GraphTableControl.prototype.onResize = function() {
  if (this.contentRendered && this.graph) {
    this.graph.htmlComp.revalidateLayout();
  }
};
