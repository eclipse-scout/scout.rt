/**
 * Filters to be used in streaming API (Array.prototype.filter()).
 */
scout.Filters = function() {
};

/**
 * Returns a function that always evaluates to 'true'.
 */
scout.Filters.returnTrue = function() {
  return true;
};

/**
 * Returns a function that always evaluates to 'false'.
 */
scout.Filters.returnFalse = function() {
  return false;
};

/**
 * Returns a filter to accept only elements which are located outside the given container, meaning not the container itself nor one of its children.
 *
 * @param DOM or jQuery container.
 */
scout.Filters.outsideFilter = function (container) {
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
scout.Filters.notSameFilter = function(element) {
  element = element instanceof jQuery ? element[0] : element;
  return function() {
    return this !== element;
  };
};
