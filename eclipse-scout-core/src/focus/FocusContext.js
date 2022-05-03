/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {filters, focusUtils, graphics, keys, Point, scrollbars} from '../index';
import $ from 'jquery';

/**
 * A focus context is associated with a $container, and controls how to focus elements within that $container.
 */
export default class FocusContext {

  constructor($container, focusManager) {
    this.$container = $container;
    this.focusManager = focusManager;

    this.lastValidFocusedElement = null; // variable to store the last valid focus position; used to restore focus once being re-activated.
    this.focusedElement = null;
    this.prepared = false;

    // Notice: every listener is installed on $container and not on $field level, except 'remove' listener because it does not bubble.
    this._keyDownListener = this._onKeyDown.bind(this);
    this._focusInListener = this._onFocusIn.bind(this);
    this._focusOutListener = this._onFocusOut.bind(this);
    this._unfocusableListener = this._onUnfocusable.bind(this);
    this._removeListener = this._onRemove.bind(this);
  }

  ready() {
    if (this.prepared) {
      return;
    }
    this.$container
      .on('keydown', this._keyDownListener)
      .on('focusin', this._focusInListener)
      .on('focusout', this._focusOutListener)
      .on('hide disable', this._unfocusableListener);
    this.prepared = true;

    if (this.lastValidFocusedElement) {
      // If a widget requested the focus while focus context was not ready, lastValidFocusedElement is set to that widget but the widget itself is not focused.
      // -> Ensure that widget is focused
      this.restoreFocus();
    }
  }

  dispose() {
    if (!this.prepared) {
      return;
    }
    this.$container
      .off('keydown', this._keyDownListener)
      .off('focusin', this._focusInListener)
      .off('focusout', this._focusOutListener)
      .off('hide disable', this._unfocusableListener);
    $(this.focusedElement).off('remove', this._removeListener);
  }

  /**
   * Method invoked once a 'keydown' event is fired to control proper tab cycle.
   */
  _onKeyDown(event) {
    if (event.which === keys.TAB) {
      let activeElement = this.$container.activeElement(true),
        $focusableElements = this.$container.find(':tabbable:visible'),
        firstFocusableElement = $focusableElements.first()[0],
        lastFocusableElement = $focusableElements.last()[0],
        activeElementIndex = $focusableElements.index(activeElement),
        focusedElement;

      // Forward Tab
      if (!event.shiftKey) {
        // If the last focusable element is focused, or the focus is on the container, set the focus to the first focusable element
        if (firstFocusableElement && (activeElement === lastFocusableElement || activeElement === this.$container[0])) {
          $.suppressEvent(event);
          this.validateAndSetFocus(firstFocusableElement);
          focusedElement = firstFocusableElement;
        } else if (activeElementIndex < $focusableElements.length - 1) {
          focusedElement = $focusableElements.get(activeElementIndex + 1);
          // Note: event is _not_ suppressed here --> will be handled by browser
        }
      } else { // Backward Tab (Shift+TAB)
        // If the first focusable element is focused, or the focus is on the container, set the focus to the last focusable element
        if (lastFocusableElement && (activeElement === firstFocusableElement || activeElement === this.$container[0])) {
          $.suppressEvent(event);
          this.validateAndSetFocus(lastFocusableElement);
          focusedElement = lastFocusableElement;
        } else if (activeElementIndex > 0) {
          focusedElement = $focusableElements.get(activeElementIndex - 1);
          // Note: event is _not_ suppressed here --> will be handled by browser
        }
      }
      if (!focusedElement) {
        return;
      }

      let $focusableElement = $(focusedElement);
      $focusableElement.addClass('keyboard-navigation');

      // Check if new focused element is currently visible, otherwise scroll the container
      let containerBounds = graphics.offsetBounds($focusableElement);
      let $scrollable = $focusableElement.scrollParent();
      if (!scrollbars.isLocationInView(new Point(containerBounds.x, containerBounds.y), $scrollable)) {
        scrollbars.scrollTo($scrollable, $focusableElement);
      }
    }
  }

  /**
   * Method invoked once a 'focusin' event is fired by this context's $container or one of its child controls.
   */
  _onFocusIn(event) {
    let $target = $(event.target);
    $target.on('remove', this._removeListener);
    this.focusedElement = event.target;

    // Do not update current focus context nor validate focus if target is $entryPoint.
    // That is because focusing the $entryPoint is done whenever no control is currently focusable, e.g. due to glass panes.
    if (event.target === this.$container.entryPoint(true)) {
      return;
    }

    // Make this context the active context (nothing done if already active) and validate the focus event.
    this.focusManager._pushIfAbsendElseMoveTop(this);
    this.validateAndSetFocus(event.target);
    event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event. Otherwise, that 'parent context' would be activated as well.
  }

  /**
   * Method invoked once a 'focusout' event is fired by this context's $container or one of its child controls.
   */
  _onFocusOut(event) {
    $(event.target).off('remove', this._removeListener);
    $(this.focusedElement).removeClass('keyboard-navigation');
    this.focusedElement = null;
    event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event. Otherwise, that 'parent context' would be activated as well.
  }

  /**
   * Method invoked once a child element of this context's $container is removed.
   */
  _onRemove(event) {
    // This listener is installed on the focused element only.
    this.validateAndSetFocus(null, filters.notSameFilter(event.target));
    event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event.
  }

  /**
   * Function invoked once a child element of this context's $container is hidden or disabled
   * and it cannot have the focus anymore. In that case we need to look for a new focusable
   * element.
   */
  _onUnfocusable(event) {
    if ($(event.target).isOrHas(this.lastValidFocusedElement)) {
      this.validateAndSetFocus(null, filters.notSameFilter(event.target));
      event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event.
    }
  }

  /**
   * Focuses the given element if being a child of this context's container and matches the given filter (if provided).
   *
   * @param {HTMLElement|$} [element]
   *        the element to focus, or null to focus the context's first focusable element matching the given filter.
   * @param {function} [filter]
   *        filter that controls which element should be focused, or null to accept all focusable candidates.
   * @param {object} [options]
   * @param {boolean} [options.preventScroll] prevents scrolling to new focused element (defaults to false)
   */
  validateAndSetFocus(element, filter, options) {
    // Ensure the element to be a child element, or set it to null otherwise.
    if (element && !$.contains(this.$container[0], element)) {
      element = null;
    }

    let elementToFocus = null;
    if (!element) {
      elementToFocus = this.focusManager.findFirstFocusableElement(this.$container, filter);
    } else if (!filter || filter.call(element)) {
      elementToFocus = element;
    } else {
      elementToFocus = this.focusManager.findFirstFocusableElement(this.$container, filter);
    }

    // Store the element to be focused, and regardless of whether currently covert by a glass pane or the focus manager is not active. That is for later focus restore.
    this.lastValidFocusedElement = elementToFocus;

    // Focus the element.
    this._focus(elementToFocus, options);
  }

  /**
   * Calls {@link #validateAndSetFocus} with {@link #lastValidFocusedElement}.
   */
  validateFocus(filter) {
    this.validateAndSetFocus(this.lastValidFocusedElement, filter);
  }

  /**
   * Restores the focus on the last valid focused element. Does nothing, if there is no last valid focused element.
   */
  restoreFocus() {
    if (this.lastValidFocusedElement) {
      this._focus(this.lastValidFocusedElement);
    }
  }

  /**
   * Focuses the requested element.
   *
   * @param {HTMLElement} element
   *        the element to focus, or null to focus the context's first focusable element matching the given filter.
   * @param {object} [options]
   * @param {boolean} [options.preventScroll] prevents scrolling to new focused element (defaults to false)
   */
  _focus(elementToFocus, options) {
    options = options || {};
    // Only focus element if focus manager is active
    if (!this.focusManager.active) {
      return;
    }
    if (!this.prepared) {
      return;
    }

    // Check whether the element is covert by a glasspane
    if (this.focusManager.isElementCovertByGlassPane(elementToFocus)) {
      let activeElement = this.$container.activeElement(true);
      if (elementToFocus && (!activeElement || !this.focusManager.isElementCovertByGlassPane(activeElement))) {
        // If focus should be removed (blur), don't break here and try to focus the root element
        // Otherwise, if desired element cannot be focused then break and leave the focus where it is, unless the currently focused element is covered by a glass pane
        return false;
      }
      elementToFocus = null;
    }

    // Focus $entryPoint if current focus is to be blurred.
    // Otherwise, the HTML body would be focused which makes global keystrokes (like backspace) not to work anymore.
    elementToFocus = elementToFocus || this.$container.entryPoint(true);

    // If element may not be focused (example SVG element in IE) -> use the entryPoint as fallback
    // $elementToFocus.focus() would trigger a focus event even the element won't be focused -> loop
    // In that case the focus function does not exist on the svg element
    if (!elementToFocus.focus) {
      elementToFocus = this.$container.entryPoint(true);
    }

    // Only focus element if different to current focused element
    if (focusUtils.isActiveElement(elementToFocus)) {
      return;
    }

    // Focus the requested element
    elementToFocus.focus({
      preventScroll: scout.nvl(options.preventScroll, false)
    });

    $.log.isDebugEnabled() && $.log.debug('Focus set to ' + graphics.debugOutput(elementToFocus));
  }
}
