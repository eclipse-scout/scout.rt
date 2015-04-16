//TODO nbu add listener for hide and show on forms. and reset focus();
scout.FocusManager = function() {
  this._sessionFocusContexts = {};
  var that = this;
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
    $container.focus();
  }

};

scout.FocusManager.prototype.installFocusContext = function($container, uiSessionId, $firstFocusElement, isRoot) {
  // Ensure $container is focusable (-1 = only programmatically)
  if ($container.attr('tabindex') === undefined) {
    $container.attr('tabindex', '-1');
  }

  // Set initial focus
  if ($firstFocusElement === 'auto') {
    $firstFocusElement = $container.find(':focusable').first();
  }
//  if ($firstFocusElement) {
//    $firstFocusElement.focus();
//  }
  var focusContext = new scout.FocusContext($container, $firstFocusElement, uiSessionId, isRoot);

  $.log.warn('install focuscontext ');
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
  $.log.warn('focuscontext for container started');
  var focusContext = this._findFocusContext($container, uiSessionId);
  if (focusContext) {
    $.log.warn('focuscontext for container. uninstall now');
    focusContext.uninstall();
  }
};

scout.FocusManager.prototype._findFocusContext = function($container, uiSessionId) {
  var contexts = this._sessionFocusContexts[uiSessionId].focusContexts;
  $.log.warn('find focuscontext for container');
  if (contexts && contexts.length > 0) {
    for (var i = contexts.length-1; i >= 0; i--) {
      if (contexts[i]._$container === $container) {
        $.log.warn('find focuscontext for container. Context found.');
        return contexts[i];
      }
    }
  }
};


scout.FocusManager.prototype.uninstallFocusContext = function(focusContext, uiSessionId) {
  var focusContexts = this._sessionFocusContexts[uiSessionId].focusContexts;

  var index = focusContexts.indexOf(focusContext);
  var oldLength = focusContexts.length;
  if (index > -1) {
    focusContexts.splice(index, 1);
  }
  if (index === oldLength - 1 && focusContexts[focusContexts.length - 1]) {
    //when focuscontext was on top(active) install old focusContext and set focus to focused element
    $.log.warn('focuscontext uninstalled') ;
    focusContexts[focusContexts.length - 1].activate(false);
  }
};

scout.FocusManager.prototype.checkFocusContextIsActive = function(focusContext) {
  var focusContexts = this._sessionFocusContexts[focusContext._uiSessionId].focusContexts;
  var index = focusContexts.indexOf(focusContext);
  return index === focusContexts.length - 1;
};
scout.FocusManager.prototype.validateFocus = function(uiSessionId, caller) {
  $.log.warn('validate focus, caller: '+caller);
//  if (this._sessionFocusContexts[uiSessionId].focusContexts.length > 0) {
//    var context = this._sessionFocusContexts[uiSessionId].focusContexts[this._sessionFocusContexts[uiSessionId].focusContexts.length - 1];
//    $.log.warn('validate focus, caller: '+caller);
//    context._validateFocus();
//  }
};

scout.FocusContext = function($container, $focusedElement, uiSessionId, isRoot) {
  this._$container = $container;
  this.name = 'name'+$container.attr('class');
  this._$focusedElement = $focusedElement;
  this._uiSessionId = uiSessionId;
  this._isRoot = isRoot;
  this._$container.bind('focusin.focusContext', this._validateFocusInEvent.bind(this));
  this._$container.bind('remove', this.uninstall.bind(this));
};

scout.FocusContext.prototype.uninstall = function(event) {
  this._$container.unbind('keydown.focusContext');
  this._$container.unbind('focusin.focusContext');
  scout.focusManager.uninstallFocusContext(this, this._uiSessionId);
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

scout.FocusContext.prototype.activate = function(disposeOld) {
  this._$container.bind('keydown.focusContext', this.handleTab.bind(this));
  $.log.warn('activate event focus context: ' +this.name);
  if(disposeOld){
    scout.focusManager.disposeActiveFocusContext(this._uiSessionId);
  }
  scout.focusManager.activateFocusContext(this);
  if( this._$focusedElement && this._$focusedElement.length>0){

    $.log.warn('activated context: ' +this.name);
  }
  if (this._$focusedElement && this._$focusedElement.length>0 && !this._$focusedElement.is(":focus")) {
    $.log.warn('activate context set focus on element focus ' + this._$focusedElement.attr('class') + ' already focused '+this._$focusedElement.is(":focus")+ ' context: ' +this.name);
    this._$focusedElement.focus();
  }
};

scout.FocusContext.prototype._validateFocusInEvent = function(event) {
  $.log.warn('_validateFocusInEvent'+ ' context: ' +this.name + ' eventTarget: ' +event.target);
  this._validateFocus();
  $.suppressEvent(event);
};

scout.FocusContext.prototype.bindHideListener = function() {
  var $focusedElement =  this._$focusedElement;
  $focusedElement.bind('hide.focusContext', scout.focusManager.validateFocus.bind( scout.focusManager,this._uiSessionId, 'hide'));
  $focusedElement.bind('remove.focusContext', scout.focusManager.validateFocus.bind(scout.focusManager, this._uiSessionId, 'remove'));
  $focusedElement.bind('focusout.unbindListener.focusContext', function(){
    $.log.warn('hidelistner unbound on ' + $focusedElement.attr('class'));
    $focusedElement.unbind('remove.focusContext');
    $focusedElement.unbind('hide.focusContext');
    $focusedElement.unbind('focusout.unbindListener.focusContext');
  });
  $.log.warn('hidelistner bound on ' + $focusedElement.attr('class')+ ' context: ' +this.name);
};

scout.FocusContext.prototype._validateFocus = function() {
  $.log.warn('_validate focus called');
  //If somehow, this scout div gets the focus, ensure it is set to the correct focus context.
  // For example, if glasspanes are active, the focus should _only_ be applied to the top-most glasspane.
  var activeElement = document.activeElement;
  if( this._$focusedElement && this._$focusedElement.length>0){

    $.log.warn('_validate focus ' + document.activeElement.id+ ' context: ' +this.name + ' activeElement =' +document.activeElement.id);
  }
  //activate Context if it is not on top
  if (!scout.focusManager.checkFocusContextIsActive(this)) {
    $.log.warn('set focus on top if its not on top' + this._$focusedElement.attr('class')+ ' context: ' +this.name);
    this.activate(true);
  }

  // If there are glasspanes, find the top-most one. Otherwise, use the scout div as context.
  var $focusContext = this._$container;
  var $glasspanes = this._$container.find('.glasspane');
  if ($glasspanes.length > 0) {
    $focusContext = $glasspanes.last();
  }

  // If any non-focusable element inside the $container got the focus...
  if (activeElement === this._$container[0]) {
    var $focusableElements = this._$container.find(':focusable');
    $.log.warn('_fuu '+ ' context: ' +this.name);
      //if we are on scout div and there are elements on this div which can gain the focus then it's not allowed to focus scout div. focus last element
      if (this._$focusedElement && this._$focusedElement.length>0 && this._$focusedElement.is(':focusable')) {
        this._$focusedElement.focus();
        $.log.warn('_validate focus  focused ' + this._$focusedElement.attr('class')+ ' context: ' +this.name);
      } else {
        $.log.warn('_validate first focused '+ ' context: ' +this.name);
        $focusContext.find(':focusable').first().focus();
      }
  }
  // If any non-focusable element inside the $focusContext got the focus...
  else if (activeElement === $focusContext[0]) {
    // ... do nothing and swallow the event
    $.log.warn('... do nothing and swallow the event'+ ' context: ' +this.name);
  }
  // If the active element is inside or equal to the focus context...
  else if (!$focusContext[0].contains(activeElement)) {
    // ...set the focus to the first focusable element inside the context element
    if($focusContext.find(':focusable').length===0){
      $focusContext.focus();
    }
    else{
      $focusContext.find(':focusable').first().focus();
    }
    $.log.warn(' ...set the focus to the first focusable element inside the context element '+ ' context: ' +this.name);
  }

  if (this._$container.find(':focusable').filter(function() {
    return this === document.activeElement;
  }).length === 1 ) {
    this._$focusedElement = $(document.activeElement);
    $.log.warn('focused element :' + this._$focusedElement.attr('class'));
    this.bindHideListener();
  }
};

//Singleton
scout.focusManager = new scout.FocusManager();
