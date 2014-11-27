/**
 * See javadoc for Session.js
 */
scout.init = function(initOptions) {
  var tabId = '' + new Date().getTime();
  window.scout.sessions = []; // FIXME BSH Detach | Needed for detaching windows, but can we do this better???
  $('.scout').each(function() {
    var portletPartId = $(this).data('partid') || '0';
    var jsonSessionId = [portletPartId, tabId].join(':');
    var session = new scout.Session($(this), jsonSessionId, initOptions);
    session.init();
    window.scout.sessions.push(session);
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
