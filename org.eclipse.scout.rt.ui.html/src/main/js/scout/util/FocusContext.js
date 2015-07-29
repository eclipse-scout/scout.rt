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

  // Notice: Any listener is installed on $container and not on $field level.
  this._keyDownListener = this._onKeyDownEvent.bind(this);
  this._focusInListener = this._onFocusInEvent.bind(this);
  this._removeListener = this._onRemoveEvent.bind(this);
  this._hideListener = this._onHideEvent.bind(this);

  this._$container
    .on('keydown', this._keyDownListener)
    .on('focusin', this._focusInListener)
    .on('hide', this._hideListener)
    .on('remove', this._removeListener);
};

scout.FocusContext.prototype._dispose = function() {
  this._$container
    .off('keydown', this._keyDownListener)
    .off('focusin', this._focusInListener)
    .off('hide', this._hideListener)
    .off('remove', this._removeListener);
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
  // Make this context the active context.
  scout.focusManager._registerContextIfAbsentElseMoveTop(this);

  // Validate the 'focusIn' event.
  this._validateAndSetFocus(event.target);

  // Prevent a possible 'parent' focus context to consume this event. Otherwise, that 'parent context' would be activated as well.
  event.stopPropagation();
};

/**
 * Method invoked once a child element of this context's $container is removed.
 */
scout.FocusContext.prototype._onRemoveEvent = function(event) {
  if ($(event.target).isOrHas(this._lastFocusedElement)) {
    this._validateAndSetFocus(); // Trigger validation because the focused element was removed.
    event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event.
  }
};

/**
 * Method invoked once a child element of this context's $container is hidden.
 */
scout.FocusContext.prototype._onHideEvent = function(event) {
  if ($(event.target).isOrHas(this._lastFocusedElement)) {
    this._validateAndSetFocus(); // Trigger validation because the focused element was hidden.
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
scout.FocusContext.prototype._validateAndSetFocus = function(element) {
  var elementToFocus = null;

  if (element && this._isChildElement(element)) {
    elementToFocus = element;
  } else {
    elementToFocus = scout.focusManager.findFirstFocusableElement(this.session, this._$container);
  }

  // Store the 'elementToFocus' even if the element is covert by a glasspane. That is for later restore once the glasspane is removed.
  this._lastFocusedElement = elementToFocus;

  // Do not gain focus if the focus manager is not active.
  if (!scout.focusManager.active(this.session.uiSessionId)) {
    return;
  }

  // Check whether the element is covert by a glasspane
  if (scout.focusManager._isElementCovertByGlassPane(element, this.session.uiSessionId)) {
    elementToFocus = null;
  }

  // When no element at all is focusable, we must focus the $entryPoint, because we don't want to focus the document body
  // because when the body is focused, the browser default keystrokes (like backspace, etc.) would be triggered.
  elementToFocus = elementToFocus || this.session.$entryPoint[0];

  // Only set the focus if different to the current focused element.
  if (document.activeElement !== elementToFocus) {
    $(elementToFocus).focus();
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
