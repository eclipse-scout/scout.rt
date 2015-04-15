scout.sessions = [];

scout.init = function(options) {
  this._installGlobalJavascriptErrorHandler();

  var tabId = scout.dates.timestamp();
  options = options || {};
  $('.scout').each(function() {
    var $container = $(this);

    var portletPartId = $container.data('partid') || '0';
    var uiSessionId = [portletPartId, tabId].join(':');
    var session = new scout.Session($container, uiSessionId, options);
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
