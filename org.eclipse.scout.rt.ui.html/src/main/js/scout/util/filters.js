/**
 * Filters to be used in streaming API (Array.prototype.filter()).
 */
scout.filters = function() {
};

/**
 * Returns a function that always evaluates to 'true'.
 */
scout.filters.returnTrue = function() {
  return true;
};

/**
 * Returns a function that always evaluates to 'false'.
 */
scout.filters.returnFalse = function() {
  return false;
};

/**
 * Returns a filter to accept only elements which are located outside the given container, meaning not the container itself nor one of its children.
 *
 * @param DOM or jQuery container.
 */
scout.filters.outsideFilter = function (container) {
  container = container instanceof jQuery ? container[0] : container;
  return function() {
    return this !== container && !$.contains(container, this);
  };
};

/**
 * Returns a filter to accept only elements which are not the given element.
 *
 * @param DOM or jQuery element.
 */
scout.filters.notSameFilter = function(element) {
  element = element instanceof jQuery ? element[0] : element;
  return function() {
    return this !== element;
  };
};
