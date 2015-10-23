scout.GraphLayout = function(graph) {
  scout.GraphLayout.parent.call(this);
  this.graph = graph;
};
scout.inherits(scout.GraphLayout, scout.AbstractLayout);

scout.GraphLayout.prototype.layout = function($container) {
  this.graph.updateGraph({
    rebuild: false,
    debounce: 200
  });
};
