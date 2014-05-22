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
