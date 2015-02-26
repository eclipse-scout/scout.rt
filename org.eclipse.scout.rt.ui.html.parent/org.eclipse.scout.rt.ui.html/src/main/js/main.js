scout.sessions = [];

scout.init = function(options) {
  this._installGlobalJavascriptErrorHandler();

  var tabId = scout.dates.timestamp();
  options = options || {};
  // Set default poptions
  if (options.focusFirstPart === undefined) {
    options.focusFirstPart = true;
  }

  var firstPartFocusApplied = false;
  $('.scout').each(function() {
    var $container = $(this);

    var portletPartId = $container.data('partid') || '0';
    var jsonSessionId = [portletPartId, tabId].join(':');
    var session = new scout.Session($container, jsonSessionId, options);
    session.init();
    scout.sessions.push(session);

    // Make container focusable and install focus context
    $container.attr('tabindex', portletPartId);
    $container.installFocusContext();
    if (options.focusFirstPart && !firstPartFocusApplied) {
      firstPartFocusApplied = true;
      $container.focus();
    }

    // If somehow, this scout div gets the focus, ensure it is set to the correct focus context.
    // For example, if glasspanes are active, the focus should _only_ be applied to the top-most glasspane.
    $container.on('focusin', function(event) {
      var activeElement = document.activeElement;

      // If there are glasspanes, find the top-most one. Otherwise, use the scout div as context.
      var $focusContext = $container;
      var $glasspanes = $container.find('.glasspane');
      if ($glasspanes.length > 0) {
        $focusContext = $glasspanes.last();
      }

      // If any non-focusable element inside the $container got the focus...
      if (activeElement === $container[0]) {
        // ...ensure that the focus is on $focusContext (and not, for example, on glasspanes in the background)
        if ($focusContext[0] !== $container[0]) {
          $focusContext.focus();
        }
      }
      // If any non-focusable element inside the $focusContext got the focus...
      else if (activeElement === $focusContext[0]) {
        // ... do nothing and swallow the event
        $.suppressEvent(event);
      }
      // If the active element is inside or equal to the focus context...
      else if (!$focusContext[0].contains(activeElement)) {
        // ...set the focus to the first focusable element inside the context element
        $.suppressEvent(event);
        $focusContext.find(':focusable').first().focus();
      }
    });
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
    }
    catch (err) {
      throw new Error('Error in global JavaScript error handler: ' + errorMessage + ' at ' + fileName + ':' + lineNumber);
    }
  };
};
