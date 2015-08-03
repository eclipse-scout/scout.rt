/**
 * A focus context is associated with a $container, and controls how to focus elements within that $container.
 *
 * - controls the focus tab cycle;
 * - validates whether child controls can gain the focus;
 * - ensures proper focus once a focused control gets removed;
 */
scout.FocusContext = function($container, session) {
  this._$container = $container;
  this.session = session;
  this._lastFocusedElement = null; // variable to store the last valid focus position; used to restore focus once being re-activated.
  this._focusedField = null;

  // Notice: Any listener is installed on $container and not on $field level, except 'remove' listener because does not bubble.
  this._keyDownListener = this._onKeyDownEvent.bind(this);
  this._focusInListener = this._onFocusInEvent.bind(this);
  this._focusOutListener = this._onFocusOutEvent.bind(this);
  this._hideListener = this._onHideEvent.bind(this);
  this._removeListener = this._onRemoveEvent.bind(this);

  this._$container
    .on('keydown', this._keyDownListener)
    .on('focusin', this._focusInListener)
    .on('focusout', this._focusOutListener)
    .on('hide', this._hideListener);
};

scout.FocusContext.prototype._dispose = function() {
  // Remove all listeners.
  this._$container
    .off('keydown', this._keyDownListener)
    .off('focusin', this._focusInListener)
    .off('focusout', this._focusOutListener)
    .off('hide', this._hideListener);
  $(this._focusedField).off('remove', this._removeListener);

  // Focus the $entryPoint if the focus is currently on an element managed by this context.
  // That is to not blur the focus once elements of this context are removed.
  if (this._focusedField) {
    this._focus(this.session.$entryPoint[0]);
    this._focusedField = null;
  }
};

/**
 * Method invoked once a 'keyDown' event is fired to control proper tab cycle.
 */
scout.FocusContext.prototype._onKeyDownEvent = function(event) {
  if (event.which === scout.keys.TAB) {
    var activeElement = document.activeElement,
        $focusableElements = this._$container.find(':tabbable'),
        firstFocusableElement = $focusableElements.first()[0],
        lastFocusableElement = $focusableElements.last()[0];

    // Forward Tab
    if (!event.shiftKey) {
      // If the last focusable element is focused, or the focus is on the container, set the focus to the first focusable element
      if (firstFocusableElement && (activeElement === lastFocusableElement || activeElement === this._$container[0])) {
        $.suppressEvent(event);
        this._validateAndSetFocus(firstFocusableElement);
      }
    }
    // Backward Tab (Shift+TAB)
    else {
      // If the first focusable element is focused, or the focus is on the container, set the focus to the last focusable element
      if (lastFocusableElement && (activeElement === this._$container[0] || activeElement === firstFocusableElement)) {
        $.suppressEvent(event);
        this._validateAndSetFocus(lastFocusableElement);
      }
    }
  }
};

/**
 * Method invoked once a 'FocusIn' event is fired by this context's $container or one of its child controls.
 */
scout.FocusContext.prototype._onFocusInEvent = function(event) {
  $(event.target).on('remove', this._removeListener);
  this._focusedField = event.target;

  // Do not update current focus context nor validate focus if target is $entryPoint.
  // That is because focusing the $entryPoint is done whenever no control is currently focusable, e.g. due to glasspanes.
  if (event.target === this.session.$entryPoint[0]) {
    return;
  }

  // Make this context the active context (nothing done if already active) and validate the focus event.
  scout.focusManager._registerContextIfAbsentElseMoveTop(this);
  this._validateAndSetFocus(event.target);

  event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event. Otherwise, that 'parent context' would be activated as well.
};

/**
 * Method invoked once a 'FocusOut' event is fired by this context's $container or one of its child controls.
 */
scout.FocusContext.prototype._onFocusOutEvent = function(event) {
  $(event.target).off('remove', this._removeListener);
  this._focusedField = null;

  event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event. Otherwise, that 'parent context' would be activated as well.
};


/**
 * Method invoked once a child element of this context's $container is removed.
 */
scout.FocusContext.prototype._onRemoveEvent = function(event) {
  // This listener is installed on the focused element only.

  this._validateAndSetFocus(event.target, true); // Trigger validation because the focused element was removed.
  event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event.
};

/**
 * Method invoked once a child element of this context's $container is hidden.
 */
scout.FocusContext.prototype._onHideEvent = function(event) {
  if ($(event.target).isOrHas(this._lastFocusedElement)) {
    this._validateAndSetFocus(event.target, true); // Trigger validation because the focused element was hidden.
    event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event.
  }
};

/**
 * Tries to set the focus according to the following policy:
 *
 * 1. Focuses the given element if being a child control of this context's $container, and is not covert by a glasspane;
 * 2. If not: Focuses the first valid child control of this context's $container;
 * 3. If not: Removes the focus from the currently active element (blur);
 *
 */
scout.FocusContext.prototype._validateAndSetFocus = function(element, blurElement) {
  var elementToFocus = null;
  if (blurElement) {
    elementToFocus = scout.focusManager.findFirstFocusableElement(this.session, this._$container, function() {
      return this !== element;
    });
  } else {
    if (element && this._isChildElement(element)) {
      elementToFocus = element;
    } else {
      elementToFocus = scout.focusManager.findFirstFocusableElement(this.session, this._$container);
    }
  }

  // Store the element to be focused, and regardless of whether currently covert by a glass pane or the focus manager is not active. That is for later focus restore.
  this._lastFocusedElement = elementToFocus;

  // Focus the element.
  this._focus(elementToFocus);
};

/**
 * Focuses the requested element.
 */
scout.FocusContext.prototype._focus = function(elementToFocus) {
  // Only focus element if focus manager is active
  if (!scout.focusManager.active(this.session.uiSessionId)) {
    return;
  }

  // Check whether the element is covert by a glasspane
  if (scout.focusManager._isElementCovertByGlassPane(elementToFocus, this.session.uiSessionId)) {
    elementToFocus = null;
  }

  // Focus $entryPoint if current focus is to be blured.
  // Otherwise, the HTML body would be focused which makes global keystrokes (like backspace) not to work anymore.
  elementToFocus = elementToFocus || this.session.$entryPoint[0];

  // Only focus element if different to current focused element
  if (document.activeElement === elementToFocus) {
    return;
  }

  // Focus the requested element.
  $(elementToFocus).focus();
  if ($.log.isDebugEnabled()) {
    $.log.debug('Focus set to ' + scout.graphics.debugOutput(elementToFocus));
  }
};

/**
 * Checks whether the given element is a child control of this context's $container.
 */
scout.FocusContext.prototype._isChildElement = function(element) {
  return $(element)
      .not(this._$container) // element must not be $container
      .closest(this._$container)
      .length > 0;
};
