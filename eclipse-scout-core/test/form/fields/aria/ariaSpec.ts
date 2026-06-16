/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// noinspection HtmlWrongAttributeValue

import {aria} from '../../../../src';

describe('aria', () => {
  describe('supportsLabel', () => {
    it('returns false if element has an unsupported role', () => {
      expect(aria.supportsLabel(undefined)).toBe(false);
      expect(aria.supportsLabel($('<div role="caption"></div>'))).toBe(false);
      expect(aria.supportsLabel($('<div role="code"></div>'))).toBe(false);
      expect(aria.supportsLabel($('<div role="generic"></div>'))).toBe(false);
      expect(aria.supportsLabel($('<div role="none"></div>'))).toBe(false);
    });

    it('returns false if an element has an implicit unsupported role', () => {
      expect(aria.supportsLabel($('<div></div>'))).toBe(false);
      expect(aria.supportsLabel($('<span></span>'))).toBe(false);
      expect(aria.supportsLabel($('<caption></caption>'))).toBe(false);
      expect(aria.supportsLabel($('<code></code>'))).toBe(false);
      expect(aria.supportsLabel($('<i></i>'))).toBe(false);
    });

    it('returns true for interactive widgets', () => {
      expect(aria.supportsLabel($('<input>'))).toBe(true);
      expect(aria.supportsLabel($('<button>'))).toBe(true);
      expect(aria.supportsLabel($('<div role="button"></div>'))).toBe(true);
      expect(aria.supportsLabel($('<div role="tree"></div>'))).toBe(true);
      expect(aria.supportsLabel($('<span role="input"></span>'))).toBe(true);
    });
  });
});
