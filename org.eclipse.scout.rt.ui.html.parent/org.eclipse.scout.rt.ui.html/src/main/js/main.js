/**
 * See javadoc for Session.js
 */
scout.init = function(initOptions) {
  var tabId = '' + new Date().getTime();
  initOptions = initOptions || {};

  scout.sessions = [];
  $('.scout').each(function() {
    var $focusedPart, session, jsonSessionId,
      $container = $(this),
      portletPartId = $(this).data('partid') || '0',
      focusFirstPart = initOptions.focusFirstPart !== undefined ? initOptions.focusFirstPart : true;

    jsonSessionId = [portletPartId, tabId].join(':'),
    session = new scout.Session($container, jsonSessionId, initOptions);
    session.init();
    scout.sessions.push(session);

    $container.attr('tabindex', portletPartId);
    if (focusFirstPart && !$focusedPart) {
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
