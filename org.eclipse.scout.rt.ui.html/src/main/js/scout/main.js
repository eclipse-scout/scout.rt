// FIXME AWE/CGU: think about refactoring this code to a Scout.js class and use an
// instance of that new class in scout-module.js for the last line of code:
// }(window.scout = window.scout || {}, jQuery));
scout.sessions = [];

scout._uniqueIdSeqNo = 0;

/**
 * Returns a new unique ID to be used for Widgets/Adapters created by the UI
 * without a model delivered by the server-side client.
 *
 */
scout.createUniqueId = function() {
  return (++scout._uniqueIdSeqNo).toString();
};

scout.init = function(options) {
  this._installGlobalJavascriptErrorHandler();
  var tabId = scout.dates.timestamp();
  options = options || {};
  $('.scout').each(function() {
    var $container = $(this);
    options.portletPartId = options.portletPartId || $container.data('partid') || '0';
    options.uiSessionId = options.uiSessionId || [options.portletPartId, tabId].join(':');
    var session = new scout.Session($container, options);
    session.init();
    scout.sessions.push(session);
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

scout._installGlobalJavascriptErrorHandler = function() {
  window.onerror = function(errorMessage, fileName, lineNumber) {
    try {
      // TODO Log error to server?
      $.log.error(errorMessage + ' at ' + fileName + ':' + lineNumber);
      // FIXME Improve this!
      if (scout.sessions.length > 0) {
        var session = scout.sessions[0];
        session.showFatalMessage({
          title: 'Internal UI Error',
          text: errorMessage,
          yesButtonText: 'OK'
        });
      }
    } catch (err) {
      throw new Error('Error in global JavaScript error handler: ' + errorMessage + ' at ' + fileName + ':' + lineNumber);
    }
  };
};

/**
 * Helper function to get the model adapter for a given adapterId. If there is more than one
 * session, e.g. in case of portlets, the second argument specifies the session to be queried
 * (can be either the zero-based index or the partId). If the session or the adapter could
 * not be found, nothing is returned.
 */
scout.adapter = function(adapterId, sessionIndex) {
  if (!scout.sessions) {
    return;
  }
  var session;
  if (scout.sessions.length === 1) {
    session = scout.sessions[0];
  }
  else {
    sessionIndex = sessionIndex || 0;
    for (var i = 0; i < scout.sessions.length; i++) {
      if (scout.sessions[i].partId == sessionIndex) { // <-- compare with '==' is intentional!
        sessionIndex = i;
        break;
      }
    }
    session = scout.sessions[sessionIndex];
  }
  if (session && session.modelAdapterRegistry) {
    return session.modelAdapterRegistry[adapterId];
  }
};
