/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {filters, FocusManager, focusUtils, graphics, keys, objects, Point, scout, scrollbars} from '../index';
import $ from 'jquery';
import KeyDownEvent = JQuery.KeyDownEvent;
import FocusInEvent = JQuery.FocusInEvent;
import FocusOutEvent = JQuery.FocusOutEvent;
import TriggeredEvent = JQuery.TriggeredEvent;

/**
 * A focus context is associated with a $container, and controls how to focus elements within that $container.
 */
export class FocusContext {
  focusManager: FocusManager;

  /** variable to store the last valid focus position; used to restore focus once being re-activated. */
  lastValidFocusedElement: HTMLElement;
  focusedElement: HTMLElement;
  prepared: boolean;
  $container: JQuery;

  /** Notice: every listener is installed on $container and not on $field level, except 'remove' listener because it does not bubble. */
  protected _keyDownListener: (e: KeyDownEvent) => void;
  protected _focusInListener: (e: FocusInEvent) => void;
  protected _focusOutListener: (e: FocusOutEvent) => void;
  protected _unfocusableListener: (e: TriggeredEvent) => void;
  protected _removeListener: (e: TriggeredEvent) => void;

  constructor($container: JQuery, focusManager: FocusManager) {
    this.$container = $container;
    this.focusManager = focusManager;
    this.lastValidFocusedElement = null;
    this.focusedElement = null;
    this.prepared = false;
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
  protected _onKeyDown(event: KeyDownEvent) {
    if (event.which === keys.TAB) {
      this.focusNextTabbable(!event.shiftKey, event);
    }
  }

  /**
   * Starting from the current `activeElement`, focuses the next or previous valid element within this focus context.
   * If the context does not contain tabbable elements or the `activeElement` is not part of this context, nothing happens.
   *
   * If a TAB key event is given and the target element is the next element in the natural DOM order, the focus is not
   * changed by this method. Instead, the focus is expected to be automatically changed by the browser (default tabbing
   * behavior).
   */
  focusNextTabbable(forward = true, event?: KeyDownEvent) {
    let $allFocusableElements = this.$container.find(':tabbable');
    let $focusableElements = $allFocusableElements.filter((index, elem) => !this.focusManager.isElementCovertByGlassPane(elem));
    if ($focusableElements.length === 0) {
      return; // no focusable elements -> nothing to do
    }

    let activeElement = this.$container.activeElement(true);
    let activeElementIndex = $focusableElements.index(activeElement);
    let firstFocusableElement = $focusableElements.first()[0];
    let lastFocusableElement = $focusableElements.last()[0];

    let elementToFocus = null;
    let explicitFocus = false;

    if (forward) {
      // --- FORWARD ---
      // If the last focusable element is currently focused, or the focus is on the container, set the focus to the first focusable element
      if (activeElement === lastFocusableElement || activeElement === this.$container[0]) {
        elementToFocus = firstFocusableElement;
        explicitFocus = true;
      } else if (activeElementIndex < $focusableElements.length - 1) {
        elementToFocus = $focusableElements.get(activeElementIndex + 1);
        // Check if next element that would be focused by the browser is covered by a glass pane. If yes, don't let the browser focus it
        explicitFocus = $allFocusableElements.get($allFocusableElements.index(activeElement) + 1) !== elementToFocus;
      }
    } else {
      // --- BACKWARD ---
      // If the first focusable element is currently focused, or the focus is on the container, set the focus to the last focusable element
      if (activeElement === firstFocusableElement || activeElement === this.$container[0]) {
        elementToFocus = lastFocusableElement;
        explicitFocus = true;
      } else if (activeElementIndex > 0) {
        elementToFocus = $focusableElements.get(activeElementIndex - 1);
        // Check if next element that would be focused by the browser is covered by a glass pane. If yes, don't let the browser focus it
        explicitFocus = $allFocusableElements.get($allFocusableElements.index(activeElement) - 1) !== elementToFocus;
      }
    }

    if (!elementToFocus) {
      return; // no valid element found
    }

    // Set focus manually if the target element does not correspond to the next element according to the
    // DOM order, or if there is currently no keyboard event in progress that will
    if (event && !explicitFocus) {
      // Don't change the focus here --> will be handled by browser
    } else {
      // Set focus manually to the target element
      this.validateAndSetFocus(elementToFocus, null, {
        selectText: elementToFocus.tagName === 'INPUT'
      });
      $.suppressEvent(event);
    }

    let $focusableElement = $(elementToFocus);
    $focusableElement.addClass('keyboard-navigation');

    // Check if new focused element is currently visible, otherwise scroll the container
    let containerBounds = graphics.offsetBounds($focusableElement);
    let $scrollable = $focusableElement.scrollParent();
    if (!scrollbars.isLocationInView(new Point(containerBounds.x, containerBounds.y), $scrollable)) {
      scrollbars.scrollTo($scrollable, $focusableElement);
    }
  }

  /**
   * Method invoked once a 'focusin' event is fired by this context's $container or one of its child controls.
   */
  protected _onFocusIn(event: FocusInEvent) {
    let $target = $(event.target);
    $target.on('remove', this._removeListener);

    let target = $target[0];
    this.focusedElement = target;

    // Do not update current focus context nor validate focus if target is $entryPoint.
    // That is because focusing the $entryPoint is done whenever no control is currently focusable, e.g. due to glass panes.
    if (target === this.$container.entryPoint(true)) {
      return;
    }

    // Make this context the active context (nothing done if already active) and validate the focus event.
    this.focusManager._pushIfAbsentElseMoveTop(this);
    this.validateAndSetFocus(target);
    event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event. Otherwise, that 'parent context' would be activated as well.
  }

  /**
   * Method invoked once a 'focusout' event is fired by this context's $container or one of its child controls.
   */
  protected _onFocusOut(event: FocusOutEvent) {
    $(event.target).off('remove', this._removeListener);
    $(this.focusedElement).removeClass('keyboard-navigation');
    this.focusedElement = null;
    event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event. Otherwise, that 'parent context' would be activated as well.
  }

  /**
   * Method invoked once a child element of this context's $container is removed.
   */
  protected _onRemove(event: TriggeredEvent) {
    // This listener is installed on the focused element only.
    this.validateAndSetFocus(null, filters.notSameFilter(event.target));
    event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event.
  }

  /**
   * Function invoked once a child element of this context's $container is hidden or disabled
   * and it cannot have the focus anymore. In that case we need to look for a new focusable
   * element.
   */
  protected _onUnfocusable(event: TriggeredEvent) {
    if ($(event.target).isOrHas(this.lastValidFocusedElement)) {
      this.validateAndSetFocus(null, filters.notSameFilter(event.target));
      event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event.
    }
  }

  /**
   * Focuses the given element if being a child of this context's container and matches the given filter (if provided).
   *
   * @param element
   *        the element to focus, or null to focus the context's first focusable element matching the given filter.
   * @param filter
   *        filter that controls which element should be focused, or null to accept all focusable candidates.
   * @param options options to customize to focus
   */
  validateAndSetFocus(element?: HTMLElement, filter?: () => boolean, options?: FocusContextFocusOptions) {
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
  validateFocus(filter?: () => boolean) {
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
   * @param element the element to focus, or null to focus the context's first focusable element matching the given filter.
   * @param options options to customize to focus
   */
  protected _focus(elementToFocus: HTMLElement, options?: FocusContextFocusOptions) {
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
        return;
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
    // If requested and possible, select the text content
    if (options.selectText && 'select' in elementToFocus && objects.isFunction(elementToFocus.select)) {
      elementToFocus.select();
    }

    $.log.isDebugEnabled() && $.log.debug('Focus set to ' + graphics.debugOutput(elementToFocus));
  }
}

export interface FocusContextFocusOptions {
  /**
   * prevents scrolling to new focused element (defaults to false)
   */
  preventScroll?: boolean;
  /**
   * automatically selects the text content of the element if supported (defaults to false)
   */
  selectText?: boolean;
}
