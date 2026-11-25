/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';
import CustomMatcherFactories = jasmine.CustomMatcherFactories;
import CustomMatcherResult = jasmine.CustomMatcherResult;

declare global {
  namespace jasmine {
    interface Matchers<T> {
      /**
       * Checks if the element has the given CSS class. Accepts a jQuery object or a DOM element.
       */
      toHaveClass(className: string): void;

      /**
       * Checks if the element has the given attribute. If an expected value is passed, the attribute must have exactly this value.
       */
      toHaveAttr(attributeName: string, expectedAttributeValue?: any): void;

      /**
       * Checks if the element has the given property. If an expected value is passed, the property must have exactly this value.
       */
      toHaveProp(propertyName: string, expectedPropertyValue?: any): void;

      /**
       * Checks if the element has the given jQuery data. If an expected value is passed, the data must have exactly this value.
       */
      toHaveData(key: string, expectedValue?: any): void;

      /**
       * Checks if the element has the given id.
       */
      toHaveId(id: string): void;

      /**
       * Checks if the computed CSS properties of the element have the given values.
       */
      toHaveCss(css: Record<string, string>): void;

      /**
       * Checks if the element is the active element of its document.
       */
      toBeFocused(): void;

      /**
       * Checks if the element matches the jQuery selector ':visible'.
       */
      toBeVisible(): void;

      /**
       * Checks if the element matches the jQuery selector ':hidden'.
       */
      toBeHidden(): void;

      /**
       * Checks if the element has no child nodes (including text).
       */
      toBeEmpty(): void;

      /**
       * Checks if the element matches the selector ':checked'.
       */
      toBeChecked(): void;

      /**
       * Checks if the element matches the selector ':selected'.
       */
      toBeSelected(): void;

      /**
       * Checks if the element matches the selector ':disabled'.
       */
      toBeDisabled(): void;

      /**
       * Checks if the element is attached to the document.
       */
      toBeInDOM(): void;

      /**
       * Checks if the jQuery object contains at least one element.
       */
      toExist(): void;

      /**
       * Checks if the jQuery object contains the given number of elements.
       */
      toHaveLength(length: number): void;

      /**
       * Checks if at least one element matches the given selector.
       */
      toBeMatchedBy(selector: string): void;

      /**
       * Checks if the element contains a descendant matching the given selector.
       */
      toContainElement(selector: string): void;

      /**
       * Checks if the text of the element (or its trimmed text) equals the given text or matches the given regular expression.
       */
      toHaveText(text: string | RegExp): void;

      /**
       * Checks if the trimmed text of the element contains the given text or matches the given regular expression.
       */
      toContainText(text: string | RegExp): void;

      /**
       * Checks if the inner HTML of the element equals the given HTML.
       */
      toHaveHtml(html: string): void;

      /**
       * Checks if the inner HTML of the element contains the given HTML.
       */
      toContainHtml(html: string): void;

      /**
       * Checks if the value of the form element equals the given value.
       */
      toHaveValue(value: string): void;
    }
  }
}

function result(pass: boolean): CustomMatcherResult {
  return {pass};
}

function hasValue(actualValue: any, expectedValue: any): boolean {
  if (expectedValue === undefined) {
    return actualValue !== undefined;
  }
  return actualValue === expectedValue;
}

/**
 * Normalizes the HTML the same way the browser does (e.g. tag name case, attribute quotes).
 */
function normalizeHtml(html: string): string {
  return $('<div>').append(html).html();
}

function matchesText(text: string, expected: string | RegExp): boolean {
  if (expected instanceof RegExp) {
    return expected.test(text);
  }
  return text === expected;
}

function matcher(compare: (actual: any, ...expected: any[]) => boolean): () => { compare(actual: any, ...expected: any[]): CustomMatcherResult } {
  return () => ({
    compare: (actual, ...expected) => result(compare(actual, ...expected))
  });
}

/**
 * Matchers for jQuery objects and DOM elements.
 */
export const jqueryMatchers: CustomMatcherFactories = {
  toHaveClass: matcher((actual, className: string) => $(actual).hasClass(className)),
  toHaveAttr: matcher((actual, attributeName: string, expectedValue?: any) => hasValue($(actual).attr(attributeName), expectedValue)),
  toHaveProp: matcher((actual, propertyName: string, expectedValue?: any) => hasValue($(actual).prop(propertyName), expectedValue)),
  toHaveData: matcher((actual, key: string, expectedValue?: any) => hasValue($(actual).data(key), expectedValue)),
  toHaveId: matcher((actual, id: string) => $(actual).attr('id') === id),
  toHaveCss: matcher((actual, css: Record<string, string>) => Object.entries(css).every(([property, value]) => {
    const $actual = $(actual);
    if (value === 'auto' && $actual[0]?.style[property] === 'auto') {
      return true;
    }
    return $actual.css(property) === value;
  })),
  toBeFocused: matcher(actual => {
    const element = $(actual)[0];
    return !!element && element === element.ownerDocument.activeElement;
  }),
  toBeVisible: matcher(actual => $(actual).is(':visible')),
  toBeHidden: matcher(actual => $(actual).is(':hidden')),
  toBeEmpty: matcher(actual => $(actual).is(':empty')),
  toBeChecked: matcher(actual => $(actual).is(':checked')),
  toBeSelected: matcher(actual => $(actual).is(':selected')),
  toBeDisabled: matcher(actual => $(actual).is(':disabled')),
  toBeInDOM: matcher(actual => {
    const element = $(actual)[0];
    return !!element && $.contains(document.documentElement, element);
  }),
  toExist: matcher(actual => $(actual).length > 0),
  toHaveLength: matcher((actual, length: number) => $(actual).length === length),
  toBeMatchedBy: matcher((actual, selector: string) => $(actual).filter(selector).length > 0),
  toContainElement: matcher((actual, selector: string) => $(actual).find(selector).length > 0),
  toHaveText: matcher((actual, text: string | RegExp) => {
    const actualText = $(actual).text();
    return matchesText(actualText, text) || matchesText(actualText.trim(), text);
  }),
  toContainText: matcher((actual, text: string | RegExp) => {
    const actualText = $(actual).text().trim();
    return text instanceof RegExp ? text.test(actualText) : actualText.includes(text);
  }),
  toHaveHtml: matcher((actual, html: string) => $(actual).html() === normalizeHtml(html)),
  toContainHtml: matcher((actual, html: string) => ($(actual).html() || '').includes(normalizeHtml(html))),
  toHaveValue: matcher((actual, value: string) => $(actual).val() === value)
};

/**
 * Compares jQuery objects and DOM elements by the elements they contain: a jQuery object equals another jQuery object or a DOM element if they contain the same elements.
 * Returns undefined for all other values so that the default comparison is used.
 */
export function jqueryEqualityTester(a: any, b: any): boolean | undefined {
  if (!a || !b) {
    return undefined;
  }
  if (isJQuery(a) || a instanceof Node) {
    return equalElements($(a), b);
  }
  if (isJQuery(b) || b instanceof Node) {
    return equalElements($(b), a);
  }
  return undefined;
}

function isJQuery(obj: any): boolean {
  return obj instanceof $;
}

function equalElements($a: JQuery<any>, b: any): boolean {
  if (isJQuery(b)) {
    return $a.length === b.length && $a.is(b);
  }
  return $a.is(b);
}
