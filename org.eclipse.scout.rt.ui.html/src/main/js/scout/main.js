/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
// TODO AWE/CGU: think about refactoring this code to a Scout.js class and use an
// instance of that new class in scout-module.js for the last line of code:
// }(window.scout = window.scout || {}, jQuery));

scout.sessions = [];

/**
 * @memberOf scout
 *
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
 * The individual bootstrap functions may return null or undefined, a single deferred or multiple deferreds as an array.
 */
scout._bootstrap = function(options) {
  options = options || {};
  var deferredValues = [
    scout.logging.bootstrap(),
    scout.device.bootstrap(),
    scout.defaultValues.bootstrap(),
    scout.fonts.bootstrap(options.fonts)
  ];

  var deferreds = [];
  deferredValues.forEach(function(value) {
    if (Array.isArray(value)) {
      deferreds.concat(value);
    } else if (value) {
      deferreds.push(value);
    }
  });
  return deferreds;
};

/**
 * Initializes a session for each html element with class '.scout' and stores them in scout.sessions.
 */
scout._init = function(options) {
  options = options || {};
  if (!this._checkBrowserCompability(options)) {
    return;
  }

  scout.polyfills.install(window);
  scout.prepareDOM();
  this._installGlobalJavascriptErrorHandler();
  this._installGlobalMouseDownInterceptor(document);

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

/**
 * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Details_of_the_Object_Model
 */
scout.inherits = function(childCtor, parentCtor) {
  childCtor.prototype = Object.create(parentCtor.prototype);
  childCtor.prototype.constructor = childCtor;
  childCtor.parent = parentCtor;
};


/**
 * If 'value' is undefined or null, 'defaultValue' is returned. Otherwise, 'value' is returned.
 */
scout.nvl = function(value, defaultValue) {
  if (value === undefined || value === null) {
    return defaultValue;
  }
  return value;
};

scout.isOneOf = function() {
  if (arguments && arguments.length >= 2) {
    var value = arguments[0];
    var argsToCheck;
    if (arguments.length === 2 && Array.isArray(arguments[1])) {
      argsToCheck = arguments[1];
    } else {
      argsToCheck = Array.prototype.slice.call(arguments, 1);
    }
    return argsToCheck.indexOf(value) !== -1;
  }
  return false;
};

/**
 * Creates a new object instance.<p> Delegates the create call to scout.ObjectFactory#create.
 */
scout.create = function(vararg, options) {
  return scout.objectFactory.create(vararg, options);
};

/**
 * Prepares the DOM for scout. This should be called once while initializing scout.
 *
 * This is used by main.js, login.js and logout.js.
 *
 * Currently it does the following:
 * - Remove the <noscript> tag (obviously there is no need for it).
 * - If the browser is Google Chrome, add a special meta header to prevent automatic translation.
 */
scout.prepareDOM = function() {
  // Cleanup DOM
  $('noscript').remove();

  // Prevent "Do you want to translate this page?" in Google Chrome
  if (scout.device.browser === scout.Device.Browser.CHROME) {
    var metaNoTranslate = '<meta name="google" content="notranslate" />';
    var $title = $('head > title');
    if ($title.length === 0) {
      // Add to end of head
      $('head').append(metaNoTranslate);
    } else {
      $title.after(metaNoTranslate);
    }
  }
};

scout._checkBrowserCompability = function(options) {
  var device = scout.device;
  $.log.info('Detected browser ' + device.browser + ' version ' + device.browserVersion);
  if (!scout.nvl(options.checkBrowserCompatibility, true) || device.isSupportedBrowser()) {
    // No check requested or browser is supported
    return true;
  }

  $('.scout').each(function() {
    var $entryPoint = $(this),
      $box = $entryPoint.appendDiv('box-with-logo small'),
      newOptions = scout.objects.valueCopy(options);

    newOptions.checkBrowserCompatibility = false;
    $box.load('unsupported-browser.html', function() {
      $box.find('button').on('click', function() {
        $box.remove();
        scout._init(newOptions);
      });
    });
  });
  return false;
};

/**
 * Note: we do not install an error handler on popup-windows because everything is controlled by the main-window
 * so exceptions will also occur in that window. This also means, the fatal message-box will be displayed in the
 * main-window, even when a popup-window is opened and active.
 */
scout._installGlobalJavascriptErrorHandler = function() {
  window.onerror = function(errorMessage, fileName, lineNumber, columnNumber, error) {
    try {
      var errorCode = getJsErrorCode(error),
        logStr = errorMessage + ' at ' + fileName + ':' + lineNumber;
      if (error && error.stack) {
        logStr = error.stack;
      }
      logStr += ' (' + 'Code ' + errorCode + ')';
      $.log.error(logStr);
      if (window.console) {
        window.console.log(logStr);
      }
      // FIXME BSH: Improve this! Accessing session at index 0 is not a good idea when a window has multiple scout instances (portlet use-case)
      if (scout.sessions.length > 0) {
        var session = scout.sessions[0],
          boxOptions = {
            header: session.optText('ui.UnexpectedProblem', 'Internal UI Error'),
            body: scout.strings.join('\n\n',
              session.optText('ui.InternalUiErrorMsg', errorMessage, ' (' + session.optText('ui.ErrorCodeX', 'Code ' + errorCode, errorCode) + ')'),
              session.optText('ui.UiInconsistentMsg', '')),
            yesButtonText: session.optText('ui.Reload', 'Reload'),
            yesButtonAction: scout.reloadPage,
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
scout._installGlobalMouseDownInterceptor = function(myDocument) {
  myDocument.addEventListener('mousedown', function(event) {
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
 * Options:
 *   [schedule]
 *     If true, the page reload is not executed in the current thread but scheduled using setTimeout().
 *     This is useful if the caller wants to execute some other code before the reload. The default is false.
 *   [clearBody]
 *     If true, the body is cleared first before the reload is performed. This is useful to prevent
 *     showing "old" content in the browser until the new content arrives. The default is true.
 *   [redirectUrl]
 *      The new URL to load. If not specified, the current location is used (window.location).
 *   [suppressUnload]
 *      If this argument is set to true, 'unload' events are not fired on the window. This can
 *      be used to disable sending the session 'unload' event to the server. The default is false.
 */
scout.reloadPage = function(options) {
  options = options || {};
  if (options.schedule) {
    setTimeout(reloadPageImpl);
  } else {
    reloadPageImpl();
  }

  // ----- Helper functions -----

  function reloadPageImpl() {
    // Hide everything (on entire page, not only $entryPoint)
    if (scout.nvl(options.clearBody, true)) {
      $('body').html('');
    }

    // Make sure the unload handler does not get triggered since the server initiated the logout and already disposed the session
    if (options.suppressUnload) {
      $(window).off('unload.' + this.id);
    }

    // Reload window (using setTimeout, to overcome drawing issues in IE)
    setTimeout(function() {
      if (options.redirectUrl) {
        window.location.href = options.redirectUrl;
      } else {
        window.location.reload();
      }
    });
  }
};
