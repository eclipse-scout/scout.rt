scout.sessions = [];

scout.init = function(options) {
  var tabId = scout.dates.timestamp();
  options = options || {};

  var $focusedPart;
  $('.scout').each(function() {
    var $container = $(this);

    var portletPartId = $container.data('partid') || '0';
    var jsonSessionId = [portletPartId, tabId].join(':');
    var session = new scout.Session($container, jsonSessionId, options);
    session.init();
    scout.sessions.push(session);

    $container.attr('tabindex', portletPartId);
    if (options.focusFirstPart && !$focusedPart) {
      $focusedPart = $container;
      $focusedPart.focus();
    }
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
