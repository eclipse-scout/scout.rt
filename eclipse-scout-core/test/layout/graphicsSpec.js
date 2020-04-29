/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {graphics} from '../../src/index';

describe('graphics', () => {

  beforeEach(() => {
    setFixtures(sandbox());
  });

  describe('bounds', () => {
    let $div;

    beforeEach(() => {
      $div = $('<div>')
        .css('position', 'absolute')
        .css('left', '6px')
        .css('top', '7px')
        .css('width', '8px')
        .css('height', '9px')
        .css('margin', '10px')
        .appendTo($('#sandbox'));
    });

    it('returns rectangle with position from JQuery.position()', () => {
      let rect = graphics.bounds($div);
      expect(rect.x).toBe(6);
      expect(rect.y).toBe(7);
    });

    it('returns rectangle with size from JQuery.outerWidth/Height', () => {
      let rect = graphics.bounds($div);
      expect(rect.width).toBe(8);
      expect(rect.height).toBe(9);
    });

    it('returns rectangle with size from  JQuery.outerWidth/Height() including margin if includeMargin is true', () => {
      let rect = graphics.bounds($div, {
        includeMargin: true
      });
      expect(rect.width).toBe(8 + 2 * 10);
      expect(rect.height).toBe(9 + 2 * 10);

      // check convenience short-hand version
      let rect2 = graphics.bounds($div, true);
      expect(rect2.width).toBe(rect.width);
      expect(rect2.height).toBe(rect.height);
    });
  });
});
