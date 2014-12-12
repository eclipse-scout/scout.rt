/**
 * Abstract layout class with functions used by all layout algorithms.
 */
scout.AbstractLayout = function() {
  // Set this flag to false if the controls in your container don't depend on size changes of the container
  // (e.g. width and height is set by the css).
  // -> Layout() won't be called if the size of the container changes
  this.invalidateOnResize = true;

  this.validityBasedOnParentSize = new scout.Dimension();
};

scout.AbstractLayout.prototype.invalidate = function() {
  // may be implemented by subclasses
};
