scout.PageWithNodes = function(outline) {
  scout.PageWithNodes.parent.call(this, outline);
  this.nodeType = "nodes";
};
scout.inherits(scout.PageWithNodes, scout.Page);
