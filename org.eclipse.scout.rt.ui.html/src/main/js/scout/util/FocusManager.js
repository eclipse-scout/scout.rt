scout.FocusManager = function() {
  this._sessionFocusContexts = {};
  var that = this;
  this.validatingFocus = false;
};

scout.FocusManager.prototype.installManagerForSession = function(session, options) {
  this._sessionFocusContexts[session.uiSessionId] = {
    session: session,
    focusContexts: []
  };

  // Set default options
  if (options.focusFirstPart === undefined) {
    options.focusFirstPart = true;
  }

  var $container = session.$entryPoint;
  var portletPartId = $container.data('partid') || '0';
  var firstPartFocusApplied = false;
  //Make container focusable and install focus context
  $container.attr('tabindex', portletPartId);

  this.installFocusContext($container, session.uiSessionId, undefined, true);
  if (options.focusFirstPart && !firstPartFocusApplied) {
    firstPartFocusApplied = true;
    $container[0].focus();
  }

};

scout.FocusManager.prototype.getFirstFocusableElement = function($container,$focusableElements) {
  var focused = false;
  if(!$focusableElements){
    $focusableElements = $container.find(':focusable');
  }
  var $firstDefaultButton;
  for (var i = 0; i < $focusableElements.length; i++) {
    var menuParents = $($focusableElements[i]).parents('div.menubar');
    var tabParents = $($focusableElements[i]).parents('div.tab-area');
    if (!$firstDefaultButton && $($focusableElements[i]).is('.default-button')) {
      $firstDefaultButton = $focusableElements[i];
    }
    if (menuParents.length === 0 && tabParents.length === 0) {
      focused = true;
      return $focusableElements.get(i);
    }
  }

  if (!focused) {
    if ($firstDefaultButton) {
      $firstDefaultButton[0].focus();
    } else if ($focusableElements && $focusableElements.length > 0) {
      return $focusableElements.first();
    } else {
      //if nothing is found to focus then focus root container
      return $container;
    }
  }
};

scout.FocusManager.prototype.focusFirstElement = function($container,$focusableElements) {
  var focusableElement = this.getFirstFocusableElement($container,$focusableElements);
  if (focusableElement) {
    focusableElement.focus();
  }
};

scout.FocusManager.prototype.installFocusContext = function($container, uiSessionId, $firstFocusElement, isRoot) {
  // Ensure $container is focusable (-1 = only programmatically)
  if ($container.attr('tabindex') === undefined) {
    $container.attr('tabindex', '-1');
  }

  // Set initial focus
  if ($firstFocusElement === 'auto') {
    $firstFocusElement = $(this.getFirstFocusableElement($container));
  }
  var focusContext = new scout.FocusContext($container, $firstFocusElement, uiSessionId, isRoot);

  $.log.trace('install focuscontext ');
  focusContext.activate(true);

  return this;
};

scout.FocusManager.prototype.disposeActiveFocusContext = function(uiSessionId) {
  //get last focus context and dispose it
  var oldFocusContext = this._sessionFocusContexts[uiSessionId].focusContexts[this._sessionFocusContexts[uiSessionId].focusContexts.length - 1];
  if (oldFocusContext) {
    //dispose old
    oldFocusContext.dispose();
  }
};

scout.FocusManager.prototype.activateFocusContext = function(focusContext) {
  var focusContexts = this._sessionFocusContexts[focusContext._uiSessionId].focusContexts;
  var index = focusContexts.indexOf(focusContext);
  if (index > -1) {
    focusContexts.splice(index, 1);
  }
  focusContexts.push(focusContext);
};
scout.FocusManager.prototype.uninstallFocusContextForContainer = function($container, uiSessionId) {
  $.log.trace('focuscontext for container started');
  var focusContext = this._findFocusContext($container, uiSessionId);
  if (focusContext) {
    $.log.trace('focuscontext for container. uninstall now');
    focusContext.uninstall();
  }
};

scout.FocusManager.prototype._findFocusContext = function($container, uiSessionId) {
  var contexts = this._sessionFocusContexts[uiSessionId].focusContexts;
  $.log.trace('find focuscontext for container');
  if (contexts && contexts.length > 0) {
    for (var i = contexts.length - 1; i >= 0; i--) {
      if (contexts[i]._$container === $container) {
        $.log.trace('find focuscontext for container. Context found.');
        return contexts[i];
      }
    }
  }
};

scout.FocusManager.prototype.uninstallFocusContext = function(focusContext, uiSessionId) {
  if (focusContext.isUninstalled) {
    return;
  }
  var focusContexts = this._sessionFocusContexts[uiSessionId].focusContexts;

  var index = focusContexts.indexOf(focusContext);
  var oldLength = focusContexts.length;
  if (index > -1) {
    focusContexts.splice(index, 1);
  }
  var prevFocusContext = focusContexts[focusContexts.length - 1];
  if (index === oldLength - 1 && prevFocusContext) {
    //when focuscontext was on top(active) install old focusContext and set focus to focused element
    $.log.trace('focuscontext uninstalled');
    setTimeout(function() {
      $.log.trace('activated runned after timeout');
      prevFocusContext.activate(false);
    }.bind(this), 0);
  }
};

scout.FocusManager.prototype.checkFocusContextIsActive = function(focusContext) {
  var focusContexts = this._sessionFocusContexts[focusContext._uiSessionId].focusContexts;
  var index = focusContexts.indexOf(focusContext);
  return index === focusContexts.length - 1;
};
scout.FocusManager.prototype.validateFocus = function(uiSessionId, caller) {
  $.log.trace('validate focus, caller: ' + caller);
  if (this._sessionFocusContexts[uiSessionId].focusContexts.length > 0) {
    var context = this._sessionFocusContexts[uiSessionId].focusContexts[this._sessionFocusContexts[uiSessionId].focusContexts.length - 1];
    context._validateFocus();
  }
};

scout.FocusContext = function($container, $focusedElement, uiSessionId, isRoot) {
  this._$container = $container;
  this.name = 'name' + $container.attr('class');
  this._$focusedElement = $focusedElement;
  this._uiSessionId = uiSessionId;
  this._isRoot = isRoot;
  this.isUninstalled = false;
  this.focusinListener = this._validateFocusInEvent.bind(this);
  this._$container.on('focusin', this.focusinListener);
  this.removeContainerListener = this.uninstall.bind(this);
  this._$container.on('remove', this.removeContainerListener);
  this.removeListener;
  this.hideListener;
  this.focusOutListener;
  this.keyDownListener;
};

scout.FocusContext.prototype.uninstall = function(event) {
  if (this.isUninstalled) {
    return;
  }
  this._$container.off('keydown', this.keyDownListener);
  this._$container.off('focusin', this.focusinListener);
  if (this._$focusedElement) {
    this._$focusedElement.off('hide', this.hideListener);
    this._$focusedElement.off('remove', this.removeListener);
    this._$focusedElement.off('focusout', this.focusoutListener);
  }
  this._$container.off('remove', this.removeContainerListener);
  scout.focusManager.uninstallFocusContext(this, this._uiSessionId);
  this.isUninstalled = true;
};

scout.FocusContext.prototype.dispose = function() {
  this._$container.unbind('keydown', this.keyDownListener);
};

scout.FocusContext.prototype.handleTab = function(event) {
  if (event.which === scout.keys.TAB) {
    var activeElement = document.activeElement;
    var $focusableElements = this._$container.find(':tabbable');
    var $firstFocusableElement = $focusableElements.first();
    var $lastFocusableElement = $focusableElements.last();

    // Forward (TAB)
    if (!event.shiftKey) {
      // If the last focusable element is focused, or the focus is on the container, set the focus to the first focusable element
      if (activeElement === $lastFocusableElement[0] || activeElement === this._$container[0]) {
        $.suppressEvent(event);
        $firstFocusableElement[0].focus();
      }
    }
    // Backward (Shift+TAB)
    else {
      // If the first focusable element is focused, or the focus is on the container, set the focus to the last focusable element
      if (activeElement === this._$container[0] || activeElement === $firstFocusableElement[0]) {
        $.suppressEvent(event);
        $lastFocusableElement[0].focus();
      }
    }
  }
};

scout.FocusContext.prototype.activate = function(disposeOld) {
  this.keyDownListener = this.handleTab.bind(this);
  this._$container.on('keydown', this.keyDownListener);
  $.log.trace('activate event focus context: ' + this.name);
  if (disposeOld) {
    scout.focusManager.disposeActiveFocusContext(this._uiSessionId);
  }
  scout.focusManager.activateFocusContext(this);
  if (this._$focusedElement && this._$focusedElement.length > 0) {

    $.log.trace('activated context: ' + this.name);
  }
  this._validateFocus();
};

scout.FocusContext.prototype._validateFocusInEvent = function(event) {
  $.log.trace('_validateFocusInEvent' + ' context: ' + this.name + ' eventTarget: ' + event.target);
  this._validateFocus();
  $.suppressEvent(event);
};

scout.FocusContext.prototype.onRemoveField = function() {
  scout.focusManager.validateFocus.bind(scout.focusManager, this._uiSessionId, 'remove');
};
scout.FocusContext.prototype.onHideField = function() {
  scout.focusManager.validateFocus.bind(scout.focusManager, this._uiSessionId, 'hide');
};

scout.FocusContext.prototype.bindHideListener = function() {
  //ensure only one of each listenertype exists.
  var $focusedElement = this._$focusedElement;
  $focusedElement.off('hide', this.hideListener);
  this.hideListener = this.onHideField.bind(this);
  $focusedElement.on('hide', this.hideListener);
  $focusedElement.off('remove', this.removeListener);
  this.removeListener = this.onRemoveField.bind(this);
  $focusedElement.on('remove', this.removeListener);
  $focusedElement.off('focusout', this.focusoutListener);
  this.focusoutListener = this.onFieldFocusOff.bind(this, $focusedElement);
  $focusedElement.on('focusout', this.focusoutListener);
  $.log.trace('hidelistner bound on ' + $focusedElement.attr('class') + ' context: ' + this.name);
};

scout.FocusContext.prototype.onFieldFocusOff = function($focusedElement) {
  $.log.trace('hidelistner unbound on ' + $focusedElement.attr('class'));
  $focusedElement.off('hide', this.hideListener);
  $focusedElement.off('remove', this.removeListener);
  $focusedElement.off('focusout', this.focusoutListener);
};

scout.FocusContext.prototype._validateFocus = function() {
  if (scout.focusManager.validatingFocus) {
    return;
  }
  scout.focusManager.validatingFocus = true;
  $.log.trace('_validate focus called');
  //If somehow, this scout div gets the focus, ensure it is set to the correct focus context.
  // For example, if glasspanes are active, the focus should _only_ be applied to the top-most glasspane.
  var activeElement = document.activeElement;
  //activate Context if it is not on top
  if (!scout.focusManager.checkFocusContextIsActive(this)) {
    $.log.trace(this._$focusedElement ? 'set focus on top if its not on top' + this._$focusedElement.attr('class') + ' context: ' + this.name : 'set focus on top if its not on top context: ' + this.name);
    this.activate(true);
  }

  // If there are glasspanes under this element do not update focus on this element. Glasspane should always install own focus contexts.
  // The problem is when glasspane is started by clicking a nonfocusable element the focuscontext for the glasspane is installed after a focusvalidation on the last focuscontext.
  // This is prevented by the following div.
  var $focusContext = this._$container;
  //TODO bsh if glasspane is consolidated in desktop use desktop to find glasspane
  var $glasspanes = this._$container.find('.glasspane');
  if ($glasspanes.length > 0) {
    scout.focusManager.validatingFocus = false;
    return;
  }

  // If any non-focusable element inside the $container got the focus...
  $.log.trace(!$(activeElement).is(':focusable') + ':' + $(activeElement).attr('class'));
  var $focusableElements = this._$container.find(':focusable');
  if (activeElement === this._$container[0]) {
    $.log.trace('_fuu ' + ' context: ' + this.name);
    //if we are on scout div and there are elements on this div which can gain the focus then it's not allowed to focus scout div. focus last element
    if (this._$focusedElement && this._$focusedElement.length > 0 && this._$focusedElement.is(':focusable') ) {
      this._$focusedElement[0].focus();
      $.log.trace(this._$focusedElement ? '_validate focus  focused ' + this._$focusedElement.attr('class') + ' context: ' + this.name : '_validate focus  focused context: ' + this.name);
    } else {
      $.log.trace('_validate first focused ' + ' context: ' + this.name);
      scout.focusManager.focusFirstElement($focusContext,$focusableElements);
    }
  }
  // If any non-focusable element inside the $focusContext got the focus...
  else if (activeElement === $focusContext[0]) {
    // ... do nothing and swallow the event
    $.log.trace('... do nothing and swallow the event' + ' context: ' + this.name);
  }
  // If the active element is inside or equal to the focus context or if activeElement is no longer focusable.
  else if (!$focusContext[0].contains(activeElement) || !$(activeElement).is(':focusable')) {
    // ...set the focus to the first focusable element inside the context element
    $.log.trace(this._$focusedElement ? 'If the active element is inside or equal to the focus context...   ' + this._$focusedElement.attr('class') + ' context: ' + this.name : 'If the active element is inside or equal to the focus context... ' + this.name);
    $.log.trace(' ...set the focus to the first focusable element inside the context element ' + ' context: ' + this.name);
    if ($focusableElements.length === 0) {
      $.log.trace(' ...set focus on container ' + ' context: ' + this.name);
      $focusContext[0].focus();
    } else if (this._$focusedElement && this._$focusedElement.length > 0 && this._$focusedElement.is(':focusable')) {
      $.log.trace(' ...reset focus ' + ' context: ' + this.name);
      this._$focusedElement[0].focus();
    } else {
      $.log.trace(' ...set focus on first element ' + ' context: ' + this.name);
      scout.focusManager.focusFirstElement($focusContext,$focusableElements);
    }
  }
  //if active element is no longer focusable
  else {
    $.log.trace('do nothing');
  }
  if ($focusableElements.filter(function() {
    return this === document.activeElement;
  }).length === 1) {
    this._$focusedElement = $(document.activeElement);
    $.log.trace(this._$focusedElement ? 'focused element :' + this._$focusedElement.attr('class') : 'focused element with undefined class');
    this.bindHideListener();
  }
//update scrollbar
  if(this._$focusedElement){
    //TODO nbu update scrollbar.
    scout.scrollbars.update(this._$focusedElement.parent().parent().parent().parent());
//    scout.scrollbars.scrollTo(this._$container, this._$focusedElement);
  }
  scout.focusManager.validatingFocus = false;
};

//Singleton
scout.focusManager = new scout.FocusManager();


