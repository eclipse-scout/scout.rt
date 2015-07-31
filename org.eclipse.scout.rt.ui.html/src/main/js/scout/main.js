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
  return 'ui' + (++scout._uniqueIdSeqNo).toString();
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
  window.onerror = function(errorMessage, fileName, lineNumber, columnNumber, error) {
    try {
      // TODO Log error to server?
      var logStr = errorMessage + ' at ' + fileName + ':' + lineNumber;
      if (error && error.stack) {
        logStr = error.stack;
      }
      $.log.error(logStr);
      if (window.console) {
        window.console.log(logStr);
      }
      // FIXME Improve this!
      if (scout.sessions.length > 0) {
        var session = scout.sessions[0];
        var errorCode = getJsErrorCode(error);
        var boxOptions = {
          header: session.optText('ui.UnexpectedProblem', 'Internal UI Error'),
          body: scout.strings.join('\n\n',
              session.optText('ui.InternalUiErrorMsg', errorMessage, ' (' + session.optText('ui.ErrorCodeX', 'Code ' + errorCode, errorCode) + ')'),
              session.optText('ui.UiInconsistentMsg', '')),
          yesButtonText: session.optText('ui.Reload', 'Reload'),
          yesButtonAction: function() {
            scout.reloadPage();
          },
          noButtonText: session.optText('ui.Ignore', 'Ignore'),
          hiddenText: logStr
        };
        session.showFatalMessage(boxOptions, errorCode);
      }
    } catch (err) {
      throw new Error('Error in global JavaScript error handler: ' + err.message + ' (original error: ' + errorMessage + ' at ' + fileName + ':' + lineNumber + ')');
    }

    // ----- Helper functions -----

    /**
     * Generate a "cool looking" error code from the JS error object, that
     * does not reveal too much technical information, but at least indicates
     * that a JS runtime error has occurred. (In contrast, fatal errors from
     * the server have numeric error codes.)
     */
    function getJsErrorCode(error) {
      if (error) {
        if (error.name === 'EvalError') {
          return 'E1';
        }
        if (error.name === 'InternalError') {
          return 'I2';
        }
        if (error.name === 'RangeError') {
          return 'A3';
        }
        if (error.name === 'ReferenceError') {
          return 'R4';
        }
        if (error.name === 'SyntaxError') {
          return 'S5';
        }
        if (error.name === 'TypeError') {
          return 'T6';
        }
        if (error.name === 'URIError') {
          return 'U7';
        }
      }
      return 'J0';
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

/**
 * Reloads the entire browser window.
 *
 * @param redirectUrl
 *          The new URL to load. If not specified, the current location is used (window.location).
 * @param suppressUnload
 *          If this argument is set to true, 'unload' events are not fired on the window. This can
 *          be used to disable sending the session 'unload' event to the server.
 */
scout.reloadPage = function(redirectUrl, suppressUnload) {
  // Hide everything (on entire page, not only $entryPoint)
  $('body').html('');

  // Make sure the unload handler does not get triggered since the server initiated the logout and already disposed the session
  if (suppressUnload) {
    $(window).off('unload.' + this.id);
  }

  // Reload window (using setTimeout, to overcome drawing issues in IE)
  setTimeout(function() {
    if (redirectUrl) {
      window.location.href = redirectUrl;
    } else {
      window.location.reload();
    }
  });
};
