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
import {IFrame, scout} from '../../src/index';

describe('IFrame', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('setLocation', () => {
    it('sets the location of the iframe', () => {
      let iframe = scout.create(IFrame, {
        parent: session.desktop
      });
      iframe.render();
      expect(iframe.location).toBe(null);
      expect(iframe.$iframe.attr('src')).toBe('about:blank');

      iframe.setLocation('http://www.bing.com');
      expect(iframe.location).toBe('http://www.bing.com');
      expect(iframe.$iframe.attr('src')).toBe('http://www.bing.com');
    });

    it('sets the location to about:blank if location is empty', () => {
      let iframe = scout.create(IFrame, {
        parent: session.desktop,
        location: 'http://www.bing.com'
      });
      iframe.render();
      expect(iframe.location).toBe('http://www.bing.com');
      expect(iframe.$iframe.attr('src')).toBe('http://www.bing.com');

      iframe.setLocation(null);
      expect(iframe.location).toBe(null);
      expect(iframe.$iframe.attr('src')).toBe('about:blank');
    });
  });
});
