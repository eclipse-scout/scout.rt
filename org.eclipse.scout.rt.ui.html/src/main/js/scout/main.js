// FIXME AWE/CGU: think about refactoring this code to a Scout.js class and use an
// instance of that new class in scout-module.js for the last line of code:
// }(window.scout = window.scout || {}, jQuery));
scout.sessions = [];

/**
 * Main initialization function.<p>
 *
 * Calls scout._bootstrap and scout._init.<p>
 * During the bootstrap phase additional scripts may get loaded required for a successful session startup.
 * The actual initialization does not get started before these bootstrap scripts are loaded.
 */
scout.init = function(options) {
  var deferreds = scout._bootstrap(options.bootstrap);
  $.when.apply($, deferreds)
    .done(scout._init.bind(scout, options.session));
};

/**
 * Executes the default bootstrap functions and returns an array of deferred objects.<p>
 * The actual session startup begins only when every of these deferred objects are completed.
 * This gives the possibility to dynamically load additional scripts or files which are mandatory for a successful session startup.
 */
scout._bootstrap = function(options) {
  return [
    scout.logging.bootstrap(),
    scout.device.bootstrap(),
    scout.defaultValues.bootstrap(),
    scout.fonts.bootstrap(options.fonts)
  ];
};

/**
 * Initializes a session for each html element with class '.scout' and stores them in scout.sessions.
 */
scout._init = function(options) {
  options = options || {};
  if (!this._checkBrowserCompability(options)) {
    return;
  }
  $('noscript').remove(); // cleanup DOM

  this._installGlobalJavascriptErrorHandler();
  this._installGlobalMouseDownInterceptor();

  var tabId = scout.dates.timestamp();
  $('.scout').each(function() {
    var $entryPoint = $(this);
    options.portletPartId = options.portletPartId || $entryPoint.data('partid') || '0';
    options.uiSessionId = options.uiSessionId || scout.strings.join(':', options.portletPartId, tabId);
    var session = new scout.Session($entryPoint, options);
    session.init();
    scout.sessions.push(session);
  });
};

scout._uniqueIdSeqNo = 0;

/**
 * Returns a new unique ID to be used for Widgets/Adapters created by the UI
 * without a model delivered by the server-side client.
 *
 */
scout.createUniqueId = function() {
  return 'ui' + (++scout._uniqueIdSeqNo).toString();
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
 * Creates a new object instance.<p>
 * Depending on the first parameter, either the object factory or the constructor function is used.
 * - String (objectType):
 *   Creates a new instance using the object factory.
 * - Constructor function:
 *   Creates a new instance using the given constructor function and calls init(options) of that instance.<p>
 *   Used mainly for widgets but may actually be used for any objects which have a init method with one parameter.
 */
scout.create = function(constructorOrObjectType, options) {
  if (typeof constructorOrObjectType === 'string') {
    options.objectType = constructorOrObjectType;
    return scout._createLocalObject(options);
  } else {
    var Constructor = constructorOrObjectType;
    var obj = new Constructor();
    obj.init(options);
    return obj;
  }
};

/**
 * Creates a new object instance based on the given model by using the object factory.
 * This method should be used when you create Widgets or Adapters in the UI without a
 * model from the server-side client.
 *
 * The required properties are 'objectType', 'session' and 'parent'. A unique ID is generated automatically,
 * when it is not provided by the model.
 */
scout._createLocalObject =  function(model) {
  var session;
  if (typeof model !== 'object') {
    throw new Error('model must be an object');
  }
  session = model.session || model.parent.session;
  if (!model.objectType) {
    throw new Error('missing property objectType');
  }
  if (model.id === undefined) {
    model.id = scout.createUniqueId();
  }
  model._register = false;
  return session.objectFactory.create(model);
};

scout._checkBrowserCompability = function(options) {
  var device = scout.device;
  $.log.info('Detected browser ' + device.browser + ' version ' + device.browserVersion);
  if (!scout.helpers.nvl(options.checkBrowserCompatibility, true) || device.isSupportedBrowser()) {
    // No check requested or browser is supported
    return true;
  }

  var session = this;
  $('.scout').each(function() {
    var $entryPoint = $(this),
      $box = $entryPoint.appendDiv('box-with-logo small'),
      newOptions = scout.objects.valueCopy(options);

    newOptions.checkBrowserCompatibility = false;
    $box.load('unsupported-browser.html', function() {
      $box.find('button').on('click', function() {
        $box.remove();
        session.init(newOptions);
      });
    });
  });
  return false;
};

scout._installGlobalJavascriptErrorHandler = function() {
  window.onerror = function(errorMessage, fileName, lineNumber, columnNumber, error) {
    try {
      var errorCode = getJsErrorCode(error);
      var logStr = errorMessage + ' at ' + fileName + ':' + lineNumber;
      if (error && error.stack) {
        logStr = error.stack;
      }
      logStr += ' (' + 'Code ' + errorCode + ')';
      $.log.error(logStr);
      if (window.console) {
        window.console.log(logStr);
      }
      // FIXME Improve this!
      if (scout.sessions.length > 0) {
        var session = scout.sessions[0];
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
        session.sendLogRequest(logStr);
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
 * Installs a global 'mousedown' interceptor to invoke 'aboutToBlurByMouseDown' on value field before anything else gets executed.
 */
scout._installGlobalMouseDownInterceptor = function() {
  document.addEventListener('mousedown', function(event) {
    scout.ValueField.invokeValueFieldAboutToBlurByMouseDown(event.target || event.srcElement);
  }, true); // true=the event handler is executed in the capturing phase
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
  } else {
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
