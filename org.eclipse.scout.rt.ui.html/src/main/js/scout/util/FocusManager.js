scout.FocusManager = function() {
  this._sessionFocusContexts = {};
  var that = this;
  this.installationPending = false;
  this.active = true;
};

/**
 * Rule for initial focus gain once a focus context is installed.
 */
scout.FocusRule = {
  /**
   * Focus the first focusable field.
   */
  AUTO: 1,
  /**
   * Do not gain focus.
   */
  NONE: 2
};

scout.FocusManager.prototype.installManagerForSession = function(session, options) {
  this._sessionFocusContexts[session.uiSessionId] = {
    session: session,
    focusContexts: []
  };

  var $container = session.$entryPoint;
  var portletPartId = $container.data('partid') || '0';
  var firstPartFocusApplied = false;
  // Make container focusable and install focus context
  $container.attr('tabindex', portletPartId);

  var setInitialFocus = scout.helpers.nvl(options.setInitialFocus, true);
  this.active = scout.helpers.nvl(options.focusManagerActive, true);
  if (setInitialFocus) {
    // FIXME: AWE/NBU: mit IMO diskutieren, was die anforderungen an fokus für die office integration sind
    // a.) wir brauchen _immer_ einen focus context, weil sonst z.B. message-box handling kaputt geht
    // b.) -vermutlich- darf in office die Html app keinen initialen fokus haben (Ivan fragen)
    // Sobald diese frage geklärt ist, kann im DetachHelper#storeFocus der check für den focusContext entfernt
    // werden, weil wir denken, dass wir immer einen focusContext haben sollten.
    this.installFocusContext($container, session.uiSessionId, undefined, true);
    if (this.active) {
      $container[0].focus();
    }
  }

  // Install global 'mousedown' listener to accept or prevent focus gain.
  session.$entryPoint.on('mousedown', function(event) {
    if (!this._acceptFocusChangeOnMouseDown($(event.target), session.$entryPoint)) {
      event.preventDefault();
    }
    return true;
  }.bind(this));
};

scout.FocusManager.prototype.getFirstFocusableElement = function($container, $focusableElements) {
  if (!$container) {
    return null; // FIXME [dwi] Analyze why $container is null in some situations
  }

  if (!$focusableElements) {
    $focusableElements = $container.find(':focusable');
  }
  var $firstDefaultButton, $firstButton, i, $candidate, $menuParents, $tabParents;

  for (i = 0; i < $focusableElements.length; i++) {
    $candidate = $($focusableElements[i]);

    $menuParents = $candidate.parents('div.menubar');
    $tabParents = $candidate.parents('div.tab-area');

    if (!$firstDefaultButton && $candidate.is('.default-menu')) {
      $firstDefaultButton = $candidate;
    }
    if (!$firstButton && ($candidate.hasClass('button') || $candidate.hasClass('menu-item'))) {
      $firstButton = $candidate;
    } else if (!$menuParents.length && !$tabParents.length && typeof $candidate.focus === 'function') {
      return $candidate;
    }
  }

  if ($firstDefaultButton) {
    return $firstDefaultButton;
  } else if ($firstButton) {
    return $firstButton;
  } else if ($focusableElements && $focusableElements.length > 0) {
    return $focusableElements.first();
  } else {
    return $container; // no focusable element found -> return container as focusable element.
  }
};

scout.FocusManager.prototype.focusFirstElement = function($container, $focusableElements) {
  var focusableElement = this.getFirstFocusableElement($container, $focusableElements);
  if (focusableElement && this.active) {
    focusableElement.focus();
  }
};

/**
 * if uiSessionId is passed FocusManager activates focus of this uiSessionId
 */
scout.FocusManager.prototype.activate = function(session) {
  this.active = true;
  if (session) {
    this.validateFocus(session.uiSessionId, 'activate');
  }
};

scout.FocusManager.prototype.deactivate = function() {
  this.active = false;
};

scout.FocusManager.prototype.installFocusContextAsync = function($container, uiSessionId, $firstFocusElement, isRoot) {
  this.installationPending = true;
  setTimeout(function() {
    if ($container) {
      this.installFocusContext($container, uiSessionId, $firstFocusElement, isRoot);
    }
    this.installationPending = false;
  }.bind(this));
};

scout.FocusManager.prototype.installFocusContext = function($container, uiSessionId, $firstFocusElement, isRoot) {
  var validateFocus = ($firstFocusElement !== scout.FocusRule.NONE);
  var autoFocus = ($firstFocusElement === scout.FocusRule.AUTO);

  // Resolve initial focus
  if (autoFocus) {
    $firstFocusElement = $(this.getFirstFocusableElement($container));
  } else if (!validateFocus) {
    $firstFocusElement = null;
  }

  var focusContext = new scout.FocusContext($container, $firstFocusElement, uiSessionId, isRoot);
  $.log.trace('install focuscontext');
  focusContext.activate(true, validateFocus);
  return this;
};

scout.FocusManager.prototype.disposeActiveFocusContext = function(uiSessionId) {
  var activeFocusContext = this._getActiveContext(uiSessionId);

  if (activeFocusContext) {
    activeFocusContext.dispose();
  }
};

scout.FocusManager.prototype.uninstallFocusContextForContainer = function($container, uiSessionId) {
  $.log.trace('focuscontext for container started');
  var focusContext = this._getContextByContainer(uiSessionId, $container);
  if (focusContext) {
    $.log.trace('focuscontext for container. uninstall now');
    focusContext.uninstall();
  }
};

scout.FocusManager.prototype._addContextIfAbsentElseMoveTop = function(focusContext) {
  this._remove(focusContext);
  this._getContextsBySession(focusContext._uiSessionId).push(focusContext);
};

scout.FocusManager.prototype._remove = function(focusContext) {
  var focusContexts = this._getContextsBySession(focusContext._uiSessionId);
  var index = focusContexts.indexOf(focusContext);
  if (index > -1) {
    focusContexts.splice(index, 1);
  }
};

scout.FocusManager.prototype._getContextsBySession = function(uiSessionId) {
  return this._sessionFocusContexts[uiSessionId].focusContexts;
};

scout.FocusManager.prototype._getActiveContext = function(uiSessionId) {
  var focusContexts = this._getContextsBySession(uiSessionId);
  if (focusContexts.length) {
    return focusContexts[focusContexts.length - 1]; // the active 'focus context' is top on stack.
  } else {
    return null;
  }
};

scout.FocusManager.prototype._getContextByContainer = function(uiSessionId, $container) {
  var contexts = this._getContextsBySession(uiSessionId);
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
  var oldActiveFocusContext = this._getActiveContext(uiSessionId);
  this._remove(focusContext);
  $.log.trace('focuscontext uninstalled');

  // If the removed FocusContext was on top (active), activate the topmost FocusContext.
  var newActiveFocusContext = this._getActiveContext(uiSessionId);
  if (oldActiveFocusContext === focusContext && newActiveFocusContext && !this.installationPending) {
    setTimeout(function() {
      $.log.trace('asynchronously activate topmost focuscontext');
      newActiveFocusContext.activate(false, true);
    }.bind(this), 0);
  }
};

scout.FocusManager.prototype.validateFocus = function(uiSessionId, caller) {
  $.log.trace('validate focus, caller: ' + caller);
  var activeContext = this._getActiveContext(uiSessionId);
  if (activeContext) {
    activeContext._validateFocus();
  }
};

/**
 * Returns whether to accept a 'mouse-down-event' for focus change,
 * and is allowed if one of the following criteria match:
 *
 *  - on natively focusable elements, or elements with a tab index;
 *  - if the source is not the glasspane;
 *  - if the source is not a menu or button;
 *  - if the source contains some selectable text;
 *  - if the source is contained within a focusable DOM parent;
 */
scout.FocusManager.prototype._acceptFocusChangeOnMouseDown = function($element, $entryPoint) {
  // 1. Prevent focus gain when glasspane is clicked.
  //    Even if the glasspane is not focusable, this check is required because the glasspane might be contained in a focusable container
  //    like table. Use case: outline modality with table-page as 'outlineContent'.
  if ($element.hasClass('glasspane')) {
    return false;
  }

  // 2. Prevent focus gain on button click.
  //    That is because buttons only can gain focus by keyboard (tabbing).
  if (this._isMenuOrButton($element)) {
    return false;
  }

  // 3. Allow focus gain on focusable elements.
  if ($element.is(':focusable')) {
    return true;
  }

  // 4. Allow focus gain on elements with selectable content, e.g. the value of a label field.
  if (this._isSelectableText($element)) {
    return true;
  }

  // 5. Allow focus gain on elements with a focusable parent, e.g. when clicking on a row in a table.
  if (this._containsFocusableParent($element, $entryPoint)) {
    return true;
  }

  return false;
};

scout.FocusManager.prototype._isMenuOrButton = function($element) {
  return $element.data('menu') || $element.data('button') || $element.data('buttonOption');
};

scout.FocusManager.prototype._isSelectableText = function($element) {
  return $element
    .clone()
    .children()
    .remove()
    .end()
    .text().trim().length && $element.css('user-select') !== 'none';
};

scout.FocusManager.prototype._containsFocusableParent = function($element, $entryPoint) {
  return $element.parents(":focusable").not($entryPoint).length; // exclude entry point; is only focusable to provide Portlet support;
};

scout.FocusContext = function($container, $focusedElement, uiSessionId, isRoot) {
  this._$container = $container;
  this.name = 'name' + $container.attr('class');
  this._$focusedElement = $focusedElement;
  if (this._$focusedElement && this._$focusedElement.length > 0 && this._$container[0] === this._$focusedElement[0]) {
    this._$lastFocusedInput = this._$focusedElement;
  }
  this._uiSessionId = uiSessionId;
  this._isRoot = isRoot;
  this.isUninstalled = false;
  this.focusinListener = this._onHandleFocusIn.bind(this);
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
  this._$container.off('keydown', this.keyDownListener);
};

scout.FocusContext.prototype.handleTab = function(event) {
  if (event.which === scout.keys.TAB) {
    var activeElement = document.activeElement;
    var $focusableElements = this._$container.find(':tabbable');
    var firstFocusableElement = $focusableElements.first()[0];
    var lastFocusableElement = $focusableElements.last()[0];

    // Forward Tab
    if (!event.shiftKey) {
      // If the last focusable element is focused, or the focus is on the container, set the focus to the first focusable element
      if (firstFocusableElement && (activeElement === lastFocusableElement || activeElement === this._$container[0]) && scout.focusManager.active) {
        $.suppressEvent(event);
        firstFocusableElement.focus();
      }
    }
    // Backward Tab (Shift+TAB)
    else {
      // If the first focusable element is focused, or the focus is on the container, set the focus to the last focusable element
      if (lastFocusableElement && (activeElement === this._$container[0] || activeElement === firstFocusableElement) && scout.focusManager.active) {
        $.suppressEvent(event);
        lastFocusableElement.focus();
      }
    }
  }
};

scout.FocusContext.prototype.activate = function(disposeOld, validateFocus) {
  this.keyDownListener = this.handleTab.bind(this);
  this._$container.on('keydown', this.keyDownListener);

  $.log.trace('activate event focus context: ' + this.name);
  if (disposeOld) {
    scout.focusManager.disposeActiveFocusContext(this._uiSessionId);
  }
  scout.focusManager._addContextIfAbsentElseMoveTop(this);

  if (this._$focusedElement && this._$focusedElement.length > 0) {
    $.log.trace('activated context: ' + this.name);
  }

  if (validateFocus) {
    this._validateFocus();
  }
};

scout.FocusContext.prototype._onHandleFocusIn = function(event) {
  scout.focusManager._addContextIfAbsentElseMoveTop(this);

  $.log.trace('_onHandleFocusIn' + ' context: ' + this.name + ' eventTarget: ' + event.target);

  event.stopPropagation();
  event.preventDefault();
  event.stopImmediatePropagation();
  this._$focusedElement = $(event.target);
  this.bindHideListener();
  if (this._$container[0] === event.target) {
    return;
  }
  if (this._$focusedElement.is(':focusable')) {
    this._$lastFocusedInput = this._$focusedElement;
  }
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
  if (this.hideListener) {
    $focusedElement.off('hide', this.hideListener);
  }
  this.hideListener = this.onHideField.bind(this);
  $focusedElement.on('hide', this.hideListener);
  if (this.removeListener) {
    $focusedElement.off('remove', this.removeListener);
  }
  this.removeListener = this.onRemoveField.bind(this);
  $focusedElement.on('remove', this.removeListener);
  if (this.focusoutListener) {
    $focusedElement.off('focusout', this.focusoutListener);
  }
  this.focusoutListener = this.onFieldFocusOff.bind(this, $focusedElement);
  $focusedElement.on('focusout', this.focusoutListener);
  $.log.trace('hidelistner bound on ' + $focusedElement.attr('class') + ' id ' + $focusedElement.attr('id') + ' context: ' + this.name);
};

scout.FocusContext.prototype.onFieldFocusOff = function($focusedElement) {
  $.log.trace('hidelistner unbound on ' + $focusedElement.attr('class'));
  $focusedElement.off('hide', this.hideListener);
  $focusedElement.off('remove', this.removeListener);
  $focusedElement.off('focusout', this.focusoutListener);
};

scout.FocusContext.prototype._validateFocus = function() {
  $.log.trace('_validate focus called');
  //If somehow, this scout div gets the focus, ensure it is set to the correct focus context.
  // For example, if glasspanes are active, the focus should _only_ be applied to the top-most glasspane.
  var activeElement = document.activeElement;

  // If there are glasspanes under this element do not update focus on this element. Glasspane should always install own focus contexts.
  // The problem is when glasspane is started by clicking a nonfocusable element the focuscontext for the glasspane is installed after a focusvalidation on the last focuscontext.
  // This is prevented by the following div.
  var $focusContext = this._$container;
  //TODO [dwi] remove glasspane logic; not necessary anymore
  var $glasspanes = this._$container.find('.glasspane');
  if ($glasspanes.length > 0) {
    return;
  }

  // If any non-focusable element inside the $container got the focus...
  $.log.trace(!$(activeElement).is(':focusable') + ':' + $(activeElement).attr('class'));
  var $focusableElements = this._$container.find(':focusable');
  if (activeElement === this._$container[0]) {
    $.log.trace('container is active ' + ' context: ' + this.name);
    //if we are on scout div and there are elements on this div which can gain the focus then it's not allowed to focus scout div. focus last element
    if (this._$lastFocusedInput && this._$lastFocusedInput.length > 0 && this._$lastFocusedInput.is(':focusable') && scout.focusManager.active) {
      this._$lastFocusedInput[0].focus();
    } else if ($focusableElements && $focusableElements.length > 0) {
      $.log.trace('_validate first focused ' + ' context: ' + this.name);
      scout.focusManager.focusFirstElement($focusContext, $focusableElements);
    } else if (scout.focusManager.active) {
      $focusContext.focus();
    }

  }
  // If the active element is inside or equal to the focus context or if activeElement is no longer focusable.
  else if (!$focusContext[0].contains(activeElement) || !$(activeElement).is(':focusable')) {
    // ...set the focus to the first focusable element inside the context element
    $.log.trace(this._$focusedElement ? 'If the active element is inside or equal to the focus context...   ' + this._$focusedElement.attr('class') + ' context: ' + this.name : 'If the active element is inside or equal to the focus context... ' + this.name);
    $.log.trace(' ...set the focus to the first focusable element inside the context element ' + ' context: ' + this.name);
    if ($focusableElements.length === 0 && scout.focusManager.active) {
      $.log.trace(' ...set focus on container ' + ' context: ' + this.name);
      $focusContext[0].focus();
    } else if (this._$lastFocusedInput && this._$lastFocusedInput.length > 0 && this._$lastFocusedInput.is(':focusable') && scout.focusManager.active) {
      this._$lastFocusedInput[0].focus();
    } else {
      $.log.trace(' ...set focus on first element ' + ' context: ' + this.name);
      scout.focusManager.focusFirstElement($focusContext, $focusableElements);
    }
  }
  //if active element is no longer focusable
  else {
    $.log.trace('do nothing');
  }
};

scout.FocusContext.prototype.focusFirstFieldInContainer = function($container) {
  scout.focusManager.focusFirstElement($container);
};

//Singleton
scout.focusManager = new scout.FocusManager();
