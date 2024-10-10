/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';

/**
 * Utility methods for focus.
 */
export const focusUtils = {
  /**
   * @returns whether the given element is focusable by mouse.
   */
  isFocusableByMouse(element: HTMLElement | JQuery): boolean {
    return $.ensure(element).closest('.unfocusable').length === 0;
  },

  /**
   * @returns whether the element must not gain the focus, even if it has a tabindex. This is only true for elements with tabindex="-2".
   */
  isFocusPrevented(element: HTMLElement | JQuery): boolean {
    return Number($.ensure(element).attr('tabindex')) === -2;
  },

  /**
   * @param $entryPoint the entry point of the current {@link Session}
   * @param nativeFocusable whether to include elements that we consider to be unfocusable but would gain the focus by the browser if we did not prevent it (elements with tabindex="-2"). Default is false.
   * @returns all parents that are focusable by mouse inside a focus boundary (marked by elements having the class .focus-boundary)
   */
  getParentsFocusableByMouse(element: HTMLElement | JQuery, $entryPoint: JQuery, nativeFocusable = false): JQuery {
    return $.ensure(element)
      .parentsUntil('.focus-boundary', nativeFocusable ? ':focusable-native' : ':focusable') // Stay inside focus boundaries (e.g. search forms should not consider parent table)
      .not($entryPoint) // Exclude $entryPoint as all elements are its descendants. However, the $entryPoint is only focusable to provide Portlet support.
      .filter((index, elem) => focusUtils.isFocusableByMouse(elem));
  },

  /**
   * @param $entryPoint the entry point of the current {@link Session}
   * @param nativeFocusable whether to include elements that we consider to be unfocusable but would gain the focus by the browser if we did not prevent it (elements with tabindex="-2"). Default is false.
   * @returns the given element if it is focusable by mouse, or the first parent that is focusable by mouse.
   */
  closestFocusableByMouse(element: HTMLElement | JQuery, $entryPoint: JQuery, nativeFocusable = false): JQuery {
    let $element = $.ensure(element);
    if ($element.is(nativeFocusable ? ':focusable-native' : ':focusable') && focusUtils.isFocusableByMouse($element)) {
      return $element;
    }
    return focusUtils.getParentsFocusableByMouse($element, $entryPoint, nativeFocusable).first();
  },

  /**
   * @returns whether the given element has a parent which is focusable by mouse.
   */
  containsParentFocusableByMouse(element: HTMLElement | JQuery, $entryPoint: JQuery): boolean {
    return focusUtils.getParentsFocusableByMouse(element, $entryPoint).length > 0;
  },

  /**
   * @returns whether the given element contains content which is selectable to the user, e.g. to be copied into clipboard.
   * It also returns true for disabled text-fields, because the user must be able to select and copy text from these text-fields.
   */
  isSelectableText(element: HTMLElement | JQuery): boolean {
    let $element = $(element);

    // Find the closest element which has a 'user-select' with a value other than 'auto'. If that value
    // is 'none', the text is not selectable. This code mimics the "inheritance behavior" of the CSS
    // property "-moz-user-select: -moz-none" as described in [1].  This does not seem to work in some
    // cases in Firefox, even with bug [2] fixed. As a workaround, we implement the desired behavior here.
    //
    // Note: Some additional CSS rules are required for events other than 'mousedown', see main.css.
    //
    // [1] https://developer.mozilla.org/en-US/docs/Web/CSS/user-select
    // [2] https://bugzilla.mozilla.org/show_bug.cgi?id=648624
    let $el = $element;
    while ($el.css('user-select') === 'auto') {
      $el = $el.parent();
      // Fix for Firefox: parent of BODY element is HtmlDocument. When calling $el.css on the HtmlDocument
      // Firefox throws an error that ownerDocument is undefined. Thus, we don't go higher than BODY element
      // and assume body is never selectable.
      if ($el.is('body')) {
        return false;
      }
    }
    if ($el.css('user-select') === 'none') {
      return false;
    }

    if ($element.is('input[disabled][type=text], textarea[disabled]')) {
      return true;
    }
    // When element or its children have text, it should be selectable.
    // The old implementation only looked at the text of the element itself
    // but not at the text of its children. With the old approach it was not
    // possible to select something inside a TD, for instance:
    //   <td><span>Foo</span></td>
    // Because TD itself has no text at all.
    // When an element has no text we return false, because if we could select
    // empty elements, we'd lose focus more often.
    return $element.text().trim().length > 0;
  },

  /**
   * @returns true if the element or one of its parents is draggable.
   */
  isDraggable(element: HTMLElement | JQuery): boolean {
    return $.ensure(element).closest('[draggable="true"]').length > 0;
  },

  /**
   * @returns true if the given HTML element is the active element in its own document, false otherwise.
   */
  isActiveElement(element: HTMLElement | JQuery): boolean {
    if (!element) {
      return false;
    }
    let activeElement: Element;
    if (element instanceof $) {
      activeElement = (element as JQuery).activeElement(true);
      element = element[0];
    } else {
      let htmlElement = element as HTMLElement;
      let ownerDocument = htmlElement instanceof Document ? htmlElement : htmlElement.ownerDocument;
      activeElement = ownerDocument.activeElement;
    }
    return activeElement === element;
  },

  /**
   * Stores the currently focused element and focuses this element again in the next animation frame if the focus changed to the entry point element.
   * This is useful if the current task would focus the entry point element which cannot be prevented.
   *
   * @param $entryPoint the entry point of the current {@link Session}
   * @param options options to be passed to the {@link HTMLElement.focus} call
   */
  restoreFocusLater($entryPoint: JQuery, options?: FocusOptions) {
    // queueMicrotask does not work, it looks like the microtask will be executed before the focus change.
    // requestAnimationFrame also prevents flickering (compared to setTimeout)
    let doc = $entryPoint.document(true);
    let prevFocusedElement = doc.activeElement as HTMLElement;
    requestAnimationFrame(() => {
      let focusedElement = doc.activeElement as HTMLElement;
      // Restore previous focus if the current active element is an element we don't want to be focused (the $entryPoint or tabindex="-2")
      if (focusedElement === $entryPoint[0] || focusUtils.isFocusPrevented(focusedElement)) {
        prevFocusedElement.focus(options);
      }
    });
  },

  /**
   * Sets the focus to the given target element just before the next repaint (using requestAnimationFrame).
   * This allows other event handlers to be fired before the focus is actually changed.
   *
   * @param target the element to be focused
   * @param options options to be passed to the {@link HTMLElement.focus} call
   */
  focusLater(target: HTMLElement | JQuery, options?: FocusOptions) {
    let $target = $.ensure(target);
    if (!$target.length) {
      return; // nothing to do
    }
    let doc = $target.document(true);
    let prevFocusedElement = doc.activeElement;
    requestAnimationFrame(() => {
      // Check if the active element is the same as before. If not, someone has changed the focus in the meantime and the
      // scheduled focusLater() request is obsolete. If the active element is considered to be unfocusable via tabindex="-2",
      // we always change the focus to the target element. (This can happen if the global mouse down handler did not suppress
      // the default behavior to avoid cancelling other events, e.g. 'dragstart').
      let focusedElement = doc.activeElement as HTMLElement;
      if (focusedElement === prevFocusedElement || focusUtils.isFocusPrevented(focusedElement)) {
        $target[0].focus(options);
      }
    });
  }
};
