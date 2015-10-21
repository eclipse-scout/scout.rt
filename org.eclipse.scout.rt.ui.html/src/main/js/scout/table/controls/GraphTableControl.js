// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GraphTableControl = function() {
  scout.GraphTableControl.parent.call(this);
};

scout.inherits(scout.GraphTableControl, scout.TableControl);


scout.GraphTableControl.prototype._renderContent = function($parent) {
  var model = $.extend({}, this.graph);
  model.parent = this;
  this.graphImpl = scout.create(scout.Graph, model);

  this.graphImpl.render($parent);
};

scout.GraphTableControl.prototype._renderGraph = function(graph) {
  if (this.contentRendered) {
    this.removeContent();
  }
  this.renderContent();
};

scout.GraphTableControl.prototype._removeContent = function($parent) {
  this.graphImpl.remove();
};

scout.GraphTableControl.prototype.isContentAvailable = function() {
  return !!this.graph;
};

scout.GraphTableControl.prototype.onResize = function() {
  if (this.contentRendered) {
    this.graphImpl.onResize();
  }
};
