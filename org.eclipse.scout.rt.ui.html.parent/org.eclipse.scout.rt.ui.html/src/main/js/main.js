scout.sessions = [];

scout.init = function(options) {
  var tabId = scout.dates.timestamp();
  options = options || {};

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

      if (activeElement === $container[0]) {
        // if any non-focusable element inside the $container got the focus...
        if ($container[0] !== $focusContext[0]) {
          // ...ensure that the focus is on $focusContext (and not, for example, glasspanes in the background)
          $focusContext.focus();
        }
        return;
      }

      // If the active element is inside or equal to the focus context...
      if (!$focusContext[0].contains(activeElement) || $focusContext[0] === activeElement) {
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
