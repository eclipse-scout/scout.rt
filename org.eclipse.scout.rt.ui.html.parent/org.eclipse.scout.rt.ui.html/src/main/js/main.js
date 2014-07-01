scout.init = function(userAgent, objectFactories) {
  var tabId = '' + new Date().getTime();
  $('.scout').each(function() {
    var portletPartId = $(this).data('partid') || '0', sessionPartId = [ portletPartId, tabId ].join('.');
    var session = new scout.Session($(this), sessionPartId, userAgent);
    session.init();
    if (!objectFactories) {
      objectFactories = scout.defaultObjectFactories;
    }
    session.objectFactory.register(objectFactories);
  });
};

/**
 * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Details_of_the_Object_Model
 */
scout.inherits = function(childCtor, parentCtor) {
  childCtor.prototype = Object.create(parentCtor.prototype);
  childCtor.prototype.constructor = childCtor;
  childCtor.parent = parentCtor;
};

/**
 * Implements the 'debounce' pattern. The given function fx is executed after a certain delay
 * (in milliseconds), but if the same function is called a second time within the waiting time,
 * the timer is reset. The default value for 'delay' is 250 ms.
 */
scout.debounce = function(fx, delay) {
  var delayer = null;
  delay = (typeof delay !== 'undefined') ? delay : 250; // default
  return function () {
    var that = this;
    var args = arguments;
    // Cancel a previously scheduled delayer function
    clearTimeout(delayer);
    // Schedule a new delayer function
    delayer = setTimeout(function () {
      fx.apply(that, args);
    }, delay);
  };
};
