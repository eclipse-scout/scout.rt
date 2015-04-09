/**
 * Abstract layout class with functions used by all layout algorithms.
 */
scout.AbstractLayout = function() {
  this.validityBasedOnParentSize = new scout.Dimension();
};

scout.AbstractLayout.prototype.invalidate = function() {
  // may be implemented by subclasses
};
