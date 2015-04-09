//TODO nbu add listener for hide and show on forms. and reset focus();
scout.FocusManager = function() {
  this._sessionFocusContexts = {};
  var that = this;
};

scout.FocusManager.prototype.installManagerForSession = function(jsonSession, options) {
  this._sessionFocusContexts[jsonSession.jsonSessionId] = {
    session: jsonSession,
    focusContexts: []
  };

  // Set default options
  if (options.focusFirstPart === undefined) {
    options.focusFirstPart = true;
  }

  var $container = jsonSession.$entryPoint;
  var portletPartId = $container.data('partid') || '0';
  var firstPartFocusApplied = false;
  //Make container focusable and install focus context
  $container.attr('tabindex', portletPartId);

  this.installFocusContext($container, jsonSession.jsonSessionId, undefined, true);
  if (options.focusFirstPart && !firstPartFocusApplied) {
    firstPartFocusApplied = true;
    $container.focus();
  }

};

scout.FocusManager.prototype.installFocusContext = function($container, jsonSessionId, $firstFocusElement, isRoot) {
  // Ensure $container is focusable (-1 = only programmatically)
  if ($container.attr('tabindex') === undefined) {
    $container.attr('tabindex', '-1');
  }

  // Set initial focus
  if ($firstFocusElement === 'auto') {
    $firstFocusElement = $container.find(':focusable').first();
  }
  if ($firstFocusElement) {
    $firstFocusElement.focus();
  }
  var focusContext = new scout.FocusContext($container, $firstFocusElement, jsonSessionId, isRoot);

  this.disposeActiveFocusContext(jsonSessionId);

  focusContext.activate();

  return this;
};

scout.FocusManager.prototype.disposeActiveFocusContext = function(jsonSessionId) {
  //get last focus context and dispose it
  var oldFocusContext = this._sessionFocusContexts[jsonSessionId].focusContexts[this._sessionFocusContexts[jsonSessionId].focusContexts.length - 1];
  if (oldFocusContext) {
    //dispose old
    oldFocusContext.dispose();
  }
};

scout.FocusManager.prototype.activateFocusContext = function(focusContext) {
  var focusContexts = this._sessionFocusContexts[focusContext._jsonSessionId].focusContexts;
  var index = focusContexts.indexOf(focusContext);
  if (index > -1) {
    focusContexts.splice(index, 1);
  }
  focusContexts.push(focusContext);
};

scout.FocusManager.prototype.uninstallFocusContext = function(focusContext, jsonSessionId) {
  var focusContexts = this._sessionFocusContexts[jsonSessionId].focusContexts;

  var index = focusContexts.indexOf(focusContext);
  var oldLength = focusContexts.length;
  if (index > -1) {
    focusContexts.splice(index, 1);
  }
  if (index === oldLength - 1 && focusContexts[focusContexts.length - 1]) {
    //when focuscontext was on top(active) install old focusContext and set focus to focused element
    focusContexts[focusContexts.length - 1].activate();
  }
};

scout.FocusManager.prototype.checkFocusContextIsActive = function(focusContext) {
  var focusContexts = this._sessionFocusContexts[focusContext._jsonSessionId].focusContexts;
  var index = focusContexts.indexOf(focusContext);
  return index === focusContexts.length - 1;
};

scout.FocusContext = function($container, $focusedElement, jsonSessionId, isRoot) {
  this._$container = $container;
  this._$focusedElement = $focusedElement;
  this._jsonSessionId = jsonSessionId;
  this._isRoot = isRoot;
  this._$container.bind('focusin.focusContext', this._validateFocus.bind(this));
  this._$container.bind('remove', this.uninstall.bind(this));
};

scout.FocusContext.prototype.uninstall = function(event) {
  scout.focusManager.uninstallFocusContext(this, this._jsonSessionId);
  this._$container.unbind('keydown.focusContext');
  this._$container.unbind('focusin.focusContext');
};

scout.FocusContext.prototype.dispose = function() {
  this._$container.unbind('keydown.focusContext');
};

scout.FocusContext.prototype.handleTab = function(event) {
  if (event.which === scout.keys.TAB) {
    var activeElement = document.activeElement;

    var $focusableElements = this._$container.find(':focusable');
    var $firstFocusableElement = $focusableElements.first();
    var $lastFocusableElement = $focusableElements.last();

    // Forward (TAB)
    if (!event.shiftKey) {
      // If the last focusable element is focused, or the focus is on the container, set the focus to the first focusable element
      if (activeElement === $lastFocusableElement[0] || activeElement === this._$container[0]) {
        $.suppressEvent(event);
        $firstFocusableElement.focus();
      }
    }
    // Backward (Shift+TAB)
    else {
      // If the first focusable element is focused, or the focus is on the container, set the focus to the last focusable element
      if (activeElement === this._$container[0] || activeElement === $firstFocusableElement[0]) {
        $.suppressEvent(event);
        $lastFocusableElement.focus();
      }
    }
  }
};

scout.FocusContext.prototype.activate = function() {

  this._$container.bind('keydown.focusContext', this.handleTab.bind(this));
  if (this._$focusedElement) {
    this._$focusedElement.focus();
  }
  //TODO nbu uncomment when todo "activate Context if it is not on top" is done
  //  scout.focusManager.disposeActiveFocusContext(this._jsonSessionId);
  scout.focusManager.activateFocusContext(this);
};

scout.FocusContext.prototype._validateFocus = function(event) {
  //TODO nbu check if focused field is visible
  //TODO nbu add hide listener
  //If somehow, this scout div gets the focus, ensure it is set to the correct focus context.
  // For example, if glasspanes are active, the focus should _only_ be applied to the top-most glasspane.
  var activeElement = document.activeElement;

  //TODO nbu activate Context if it is not on top
  //  if(!scout.focusManager.checkFocusContextIsActive(this)){
  //    this.activate();
  //  }

  // If there are glasspanes, find the top-most one. Otherwise, use the scout div as context.
  var $focusContext = this._$container;
  var $glasspanes = this._$container.find('.glasspane');
  if ($glasspanes.length > 0) {
    $focusContext = $glasspanes.last();
  }

  // If any non-focusable element inside the $container got the focus...
  if (activeElement === this._$container[0]) {
    var $focusableElements = this._$container.find(':focusable');
    if (this._isRoot && $focusableElements.length > 0) {
      //if we are on scout div and there are elements on this div which can gain the focus then it's not allowed to focus scout div. focus last element
      if (this._$focusedElement) {
        this._$focusedElement.focus();
      } else {
        $focusContext.find(':focusable').first().focus();
      }
    }
    // ...ensure that the focus is on $focusContext (and not, for example, on glasspanes in the background)
    else if ($focusContext[0] !== this._$container[0]) {
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
  if ($focusContext.find(':focusable').filter(function() {
    return this === document.activeElement;
  }).length === 1) {
    this._$focusedElement = $(document.activeElement);
  }

  $.suppressEvent(event);
};

//Singleton
scout.focusManager = new scout.FocusManager();
