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
/**
 * The focus manager ensures proper focus handling based on focus contexts.
 *
 * A focus context is bound to a $container. Once a context is activated, that container defines the tab cycle,
 * meaning that only child elements of that container can be entered by tab. Also, the context ensures proper
 * focus gaining, meaning that only focusable elements can gain focus. A focusable element is defined as an element,
 * which is natively focusable and which is not covert by a glass pane. Furthermore, if a context is unintalled,
 * the previously active focus context is activated and its focus position restored.
 */
scout.FocusManager = function(options) {
  var defaults = {
    // Auto focusing of elements is bad with on screen keyboards -> deactivate to prevent unwanted popping up of the keyboard
    active: !scout.device.supportsTouch(),
    // Preventing blur is bad on touch devices because every touch on a non input field is supposed to close the keyboard which does not happen if preventDefault is used on mouse down
    restrictedFocusGain: !scout.device.supportsTouch()
  };
  $.extend(this, defaults, options);

  if (!this.session) {
    throw new Error('Session expected');
  }

  this._focusContexts = [];
  this._glassPaneTargets = [];

  // Make $entryPoint focusable and install focus context.
  var $mainEntryPoint = this.session.$entryPoint;
  var portletPartId = $mainEntryPoint.data('partid') || '0';
  $mainEntryPoint.attr('tabindex', portletPartId);

  // Restricted focus gain means that not every click outside of the active element necessarily focuses another element but the active element stays focused
  // See _acceptFocusChangeOnMouseDown for details
  if (this.restrictedFocusGain) {
    this.installTopLevelMouseHandlers($mainEntryPoint);
  }
  this.installFocusContext($mainEntryPoint, scout.focusRule.AUTO);
};

scout.FocusManager.prototype.installTopLevelMouseHandlers = function($container) {
  // Install 'mousedown' on top-level $container to accept or prevent focus gain
  $container.on('mousedown', function(event) {
    if (!this._acceptFocusChangeOnMouseDown($(event.target))) {
      event.preventDefault();
    } else {
      // Because in IE DOM elements are focusable without tabindex we have to handle it here -> select next parent with tabindex.
      this._handleIEEvent(event);
    }
    return true;
  }.bind(this));
};

/**
 * Note: this method is a collection of bugfixes for focus problems which only occur on
 * Internet Explorer. Focus handling in IE is different from focus-handling in Chrome and Firefox.
 * Basically this method emulates the focus behavior of the other two browsers, by setting the
 * focus programmatically to the correct element. And yes, the method is ugly since it deals with
 * a lot of specific cases. However: distributing the IE specific bugfix code over several classes
 * wouldn't be much better.
 */
scout.FocusManager.prototype._handleIEEvent = function(event) {
  if (scout.device.browser !== scout.Device.Browser.INTERNET_EXPLORER) {
    return;
  }

  var
    $elementToFocus,
    $element = $(event.target);

  // table fix - required because IE focuses the table-cell element (unlike Chrome and Firefox)
  // that means in IE, the table-cell is focused. But all our styles apply to a focused table
  // that's why we must set the focus programmatically to the closest table in the DOM.
  if ($element.is('.table-cell') || $element.closest('.table-cell').length > 0) {
    this.requestFocus($element.closest('.table'));
    event.preventDefault();
    return;
  }

  // tree fix - same issue as in table
  if ($element.is('.tree-node') || $element.closest('.tree-node').length > 0) {
    this.requestFocus($element.closest('.tree'));
    event.preventDefault();
    return;
  }

  var userSelect = $element.css('user-select');
  var selectableElements =
    'div:not(.desktop),[tabindex]:not([tabindex=-1]),radio,a[href],area[href],input:not([disabled]),' +
    'select:not([disabled]),textarea:not([disabled]),button:not([disabled]),iframe';

  // other fixes (NBU)
  if ($element.not(selectableElements).length === 0) {
    return;
  }

  if ($element.closest('[contenteditable="true"]').length === 0 &&
    ((userSelect && userSelect === 'none') ||
      (!userSelect && $element.closest('div').not('[unselectable="on"]').length === 0)) /* IE 9 has no user-select */ ) {
    $elementToFocus = $element.closest(selectableElements);
    if ($elementToFocus && $elementToFocus.not('[unselectable="on"]').length > 0) {
      this.requestFocus($elementToFocus.get(0));
    }
    event.preventDefault();
  }
};

/**
 * Activates or deactivates focus management.
 *
 * If deactivated, the focus manager still validates the current focus, but never gains focus nor enforces a valid focus position.
 * Once activated, the current focus position is revalidated.
 */
scout.FocusManager.prototype.activate = function(activate) {
  if (this.active !== activate) {
    this.active = activate;
    if ($.log.isDebugEnabled()) {
      $.log.debug('Focus manager active: ' + this.active);
    }
    if (this.active) {
      this.validateFocus();
    }
  }
};

/**
 * Installs a new focus context for the given $container, and sets the $container's initial focus, either by
 * the given rule, or tries to gain focus for the given element.
 */
scout.FocusManager.prototype.installFocusContext = function($container, focusRuleOrElement) {
  var elementToFocus;
  if (!focusRuleOrElement || focusRuleOrElement === scout.focusRule.AUTO) {
    elementToFocus = this.findFirstFocusableElement($container);
  } else if (focusRuleOrElement === scout.focusRule.NONE) {
    elementToFocus = null;
  } else {
    elementToFocus = focusRuleOrElement;
  }

  // Create and register the focus context.
  var focusContext = new scout.FocusContext($container, this);
  this._pushIfAbsendElseMoveTop(focusContext);

  if (elementToFocus) {
    focusContext._validateAndSetFocus(elementToFocus);
  }
};

/**
 * Uninstalls the focus context for the given $container, and activates the last active context.
 * This method has no effect, if there is no focus context installed for the given $container.
 */
scout.FocusManager.prototype.uninstallFocusContext = function($container) {
  var focusContext = this._findFocusContext($container);
  if (!focusContext) {
    return;
  }

  // Filter to exclude the current focus context's container and any of its child elements to gain focus.
  var filter = scout.filters.outsideFilter(focusContext.$container);

  // Remove and dispose the current focus context.
  scout.arrays.remove(this._focusContexts, focusContext);
  focusContext._dispose();

  // Activate last active focus context.
  var activeFocusContext = this._findActiveContext();
  if (activeFocusContext) {
    activeFocusContext._validateAndSetFocus(activeFocusContext._lastValidFocusedElement, filter);
  }
};

/**
 * Returns whether there is a focus context installed for the given $container.
 */
scout.FocusManager.prototype.isFocusContextInstalled = function($container) {
  return !!this._findFocusContext($container);
};

/**
 * Checks if the given element is accessible, meaning not covert by a glasspane.
 *
 * @param element a HTMLElement or a jQuery collection
 */
scout.FocusManager.prototype.isElementCovertByGlassPane = function(element) {
  if (!this._glassPaneTargets.length) {
    return false; // no glasspanes active.
  }

  // Checks whether the element is a child of a glasspane target.
  // If so, the some-iterator returns immediately with true.
  return this._glassPaneTargets.some(function($glassPaneTarget) {
    return $(element).closest($glassPaneTarget).length !== 0;
  });
};

/**
 * Registers the given glasspane target, so that the focus cannot be gained on the given target nor on its child elements.
 */
scout.FocusManager.prototype.registerGlassPaneTarget = function(glassPaneTarget) {
  this._glassPaneTargets.push(glassPaneTarget);
  this.validateFocus();
};

/**
 * Unregisters the given glasspane target, so that the focus can be gained again for the target or one of its child controls.
 */
scout.FocusManager.prototype.unregisterGlassPaneTarget = function(glassPaneTarget) {
  scout.arrays.remove(this._glassPaneTargets, glassPaneTarget);
  this.validateFocus();
};

/**
 * Enforces proper focus on the currently active focus context.
 *
 * @param filter
 *        Filter to exclude elements to gain focus.
 */
scout.FocusManager.prototype.validateFocus = function(filter) {
  var activeContext = this._findActiveContext();
  if (activeContext) {
    activeContext._validateAndSetFocus(activeContext._lastValidFocusedElement, filter);
  }
};

/**
 * Requests the focus for the given element, but only if being a valid focus location.
 *
 * @return true if focus was gained, or false otherwise.
 */
scout.FocusManager.prototype.requestFocus = function(element, filter) {
  element = element instanceof $ ? element[0] : element;

  var activeContext = this._findActiveContext();
  if (activeContext) {
    activeContext._validateAndSetFocus(element, filter);
  }

  return scout.focusUtils.isActiveElement(element);
};

/**
 * Finds the first focusable element of the given $container, or null if not found.
 */
scout.FocusManager.prototype.findFirstFocusableElement = function($container, filter) {
  var firstElement, firstDefaultButton, firstButton, i, candidate, $candidate, $menuParents, $tabParents, $boxButtons,
    $entryPoint = $container.entryPoint(),
    $candidates = $container
    .find(':focusable')
    .addBack(':focusable') /* in some use cases, the container should be focusable as well, e.g. context menu without focusable children */
    .not($entryPoint) /* $entryPoint should never be a focusable candidate. However, if no focusable candidate is found, 'FocusContext._validateAndSetFocus' focuses the $entryPoint as a fallback. */
    .filter(filter || scout.filters.returnTrue);

  for (i = 0; i < $candidates.length; i++) {
    candidate = $candidates[i];
    $candidate = $(candidate);

    // Check whether the candidate is accessible and not covert by a glass pane.
    if (this.isElementCovertByGlassPane(candidate)) {
      continue;
    }
    // Check if the element (or one of its parents) does not want to be the first focusable element
    // FIXME awe, bsh: (focus) replace this concept with Form#renderInitialFocusEnabled: currently we cannot set
    // that property on the model (only by JS). Add a model-property so we can do this on the model.
    // May be useful for ticket-form too, since that form currently focuses a random link in the
    // history table.
    if ($candidate.is('.prevent-initial-focus') || $candidate.closest('.prevent-initial-focus').length > 0) {
      continue;
    }

    if (!firstElement && !($candidate.hasClass('button') || $candidate.hasClass('menu-item'))) {
      firstElement = candidate;
    }

    if (!firstDefaultButton && $candidate.is('.default-menu')) {
      firstDefaultButton = candidate;
    }

    $menuParents = $candidate.parents('.menubar');
    $tabParents = $candidate.parents('.tab-area');
    $boxButtons = $candidate.parents('.box-buttons');
    if (($menuParents.length > 0 || $tabParents.length > 0 || $boxButtons.length > 0) && !firstButton && ($candidate.hasClass('button') || $candidate.hasClass('menu-item'))) {
      firstButton = candidate;
    } else if (!$menuParents.length && !$tabParents.length && !$boxButtons.length && typeof candidate.focus === 'function') { //inline buttons and menues are selectable before choosing button or menu from bar
      return candidate;
    }
  }
  if (firstDefaultButton) {
    return firstDefaultButton;
  } else if (firstButton) {
    if (firstButton !== firstElement && firstElement) {
      var $tabParentsButton = $(firstButton).parents('.tab-area'),
        $firstItem = $(firstElement),
        $tabParentsFirstElement = $(firstElement).parents('.tab-area');
      if ($tabParentsFirstElement.length > 0 && $tabParentsButton.length > 0 && $firstItem.is('.tab-item')) {
        return firstElement;
      }
    }
    return firstButton;
  }
  return firstElement;
};

/**
 * Returns the currently active focus context, or null if not applicable.
 */
scout.FocusManager.prototype._findActiveContext = function() {
  return scout.arrays.last(this._focusContexts);
};

/**
 * Returns the focus context which is associated with the given $container, or null if not applicable.
 */
scout.FocusManager.prototype._findFocusContext = function($container) {
  return scout.arrays.find(this._focusContexts, function(focusContext) {
    return focusContext.$container === $container;
  });
};

/**
 * Returns whether to accept a 'mousedown event'.
 */
scout.FocusManager.prototype._acceptFocusChangeOnMouseDown = function($element) {
  // 1. Prevent focus gain when glasspane is clicked.
  //    Even if the glasspane is not focusable, this check is required because the glasspane might be contained in a focusable container
  //    like table. Use case: outline modality with table-page as 'outlineContent'.
  if ($element.hasClass('glasspane')) {
    return false;
  }

  // 2. Prevent focus gain if covert by glasspane.
  if (this.isElementCovertByGlassPane($element)) {
    return false;
  }

  // 3. Prevent focus gain on elements excluded to gain focus by mouse, e.g. buttons.
  if (!scout.focusUtils.isFocusableByMouse($element)) {
    return false;
  }

  // 4. Allow focus gain on focusable elements.
  if ($element.is(':focusable')) {
    return true;
  }

  // 5. Allow focus gain on elements with selectable content, e.g. the value of a label field.
  if (scout.focusUtils.isSelectableText($element)) {
    return true;
  }

  // 6. Allow focus gain on elements with a focusable parent, e.g. when clicking on a row in a table.
  if (scout.focusUtils.containsParentFocusableByMouse($element, $element.entryPoint())) {
    return true;
  }

  return false;
};

/**
 * Registers the given focus context, or moves it on top if already registered.
 */
scout.FocusManager.prototype._pushIfAbsendElseMoveTop = function(focusContext) {
  scout.arrays.remove(this._focusContexts, focusContext);
  this._focusContexts.push(focusContext);
};
