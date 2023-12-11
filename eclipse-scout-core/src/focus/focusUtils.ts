/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
    let $element = $(element);
    return !$element.hasClass('unfocusable') && !$element.closest('.unfocusable').length;
  },

  /**
   * @returns whether the given element has a parent which is focusable by mouse.
   */
  containsParentFocusableByMouse(element: HTMLElement | JQuery, entryPoint: JQuery): boolean {
    let $focusableParentElements = $(element)
      .parentsUntil('.focus-boundary', ':focusable') // Stay inside focus boundaries (e.g. search forms should not consider parent table)
      .not(entryPoint) /* Exclude $entryPoint as all elements are its descendants. However, the $entryPoint is only focusable to provide Portlet support. */
      .filter(() => focusUtils.isFocusableByMouse(this));
    return $focusableParentElements.length > 0;
  },

  /**
   * @returns  whether the given element contains content which is selectable to the user, e.g. to be copied into clipboard.
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
    let $element = $.ensure(element);
    return $element.attr('draggable') === 'true' || $element.parents('[draggable="true"]').length > 0;
  },

  /**
   * Returns true if the given HTML element is the active element in its own document, false otherwise
   * @param element
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
   */
  restoreFocusLater($entryPoint: JQuery) {
    // queueMicrotask does not work, it looks like the microtask will be executed before the focus change.
    // requestAnimationFrame also prevents flickering (compared to setTimeout)
    let doc = $entryPoint.document(true);
    let prevFocusedElement = doc.activeElement as HTMLElement;
    requestAnimationFrame(() => {
      let focusedElement = doc.activeElement;
      if (focusedElement === $entryPoint[0]) {
        prevFocusedElement.focus();
      }
    });
  }
};
