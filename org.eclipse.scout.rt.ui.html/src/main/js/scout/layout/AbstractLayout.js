/**
 * Abstract layout class with functions used by all layout algorithms.
 * Subclasses of AbstactLayout.js must implement the following functions:
 * - layout
 * - preferredLayoutSize
 */
scout.AbstractLayout = function() { //
};

/**
 * Called when layout is invalidated. An implementation should delete cached layout-information
 * when it is invalidated.
 *
 * May be implemented by sub-class.
 */
scout.AbstractLayout.prototype.invalidate = function() { //
};

/**
 * Layouts children of the given $container, according to the implemented layout algorithm.
 * The implementation should call setSize or setBounds on its children.
 *
 * Must be implemented by sub-class.
 */
scout.AbstractLayout.prototype.layout = function($container) { //
};

/**
 * Returns the preferred size of the given $container.
 *
 * @return scout.Dimension preferred size
 */
scout.AbstractLayout.prototype.preferredLayoutSize = function($container) {
  return scout.graphics.prefSize($container);
};
