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
import * as texts from '../../src/text/texts';
import TextMap from '../../src/text/TextMap';

/* jshint sub:true */
describe('texts', () => {

  let model = {
    'default': {
      theKey: 'default',
      defaultKey1: 'default1',
      defaultKey2: 'default2'
    },
    'de': {
      theKey: 'de',
      deKey1: 'de1',
      deKey2: 'de2'
    },
    'de-CH': {
      theKey: 'deCH',
      deCHKey1: 'deCH1',
      deCHKey2: 'deCH2'
    }
  };

  beforeEach(() => {
    // clear
    texts._setTextsByLocale({});
  });

  describe('init', () => {

    it('creates Texts objects for each language tag given in the model', () => {
      texts.init(model);
      expect(texts._get('default')._exists('defaultKey1')).toBe(true);
      expect(texts._get('default')._exists('deKey1')).toBe(false);

      expect(texts._get('de')._exists('deKey1')).toBe(true);
      expect(texts._get('de')._exists('deCHKey1')).toBe(false);

      expect(texts._get('de-CH')._exists('deCHKey1')).toBe(true);
      expect(texts._get('de-CH')._exists('deKey1')).toBe(false);
    });

    it('links Texts objects according the sub tags of the language tag', () => {
      texts.init(model);
      expect(texts._get('default').parent).toBeUndefined();
      expect(texts._get('de').parent).toBe(texts._get('default'));
      expect(texts._get('de-CH').parent).toBe(texts._get('de'));
    });

    it('does not override existing text maps', () => {
      texts.get('de-CH').add('existingKey', 'existingText');
      texts.get('de').add('existingDeKey', 'existingDeText');
      texts.get('de').add('theKey', 'thePreviousText');
      expect(texts.get('de-CH').get('existingKey')).toBe('existingText');
      expect(texts.get('de').get('existingDeKey')).toBe('existingDeText');
      expect(texts.get('de').get('theKey')).toBe('thePreviousText');

      texts.init(model);
      // Texts which were registered before must still be registered
      expect(texts.get('de-CH').get('existingKey')).toBe('existingText');
      expect(texts.get('de').get('existingDeKey')).toBe('existingDeText');

      // New texts need to be registered as well
      expect(texts.get('de-CH').get('deCHKey1')).toBe('deCH1');
      expect(texts.get('de').get('deKey2')).toBe('de2');

      // Texts which were registered but are part of the new model too are replaced
      expect(texts.get('de').get('theKey')).toBe('de');
    });

  });

  describe('get', () => {

    it('returns the Texts for the given language tag', () => {
      texts.init(model);
      let textMap = texts.get('de');
      expect(textMap instanceof TextMap).toBe(true);
      expect(textMap._exists('deKey1')).toBe(true);
      expect(textMap._exists('deCHKey')).toBe(false);
    });

    it('returns a Texts object with correct linking', () => {
      texts.init(model);
      let textMap = texts.get('de-CH');
      expect(textMap.get('theKey')).toBe('deCH');
      expect(textMap.get('deCHKey1')).toBe('deCH1');
      expect(textMap.get('deKey1')).toBe('de1');
      expect(textMap.get('defaultKey1')).toBe('default1');

      expect(textMap.optGet('theKey')).toBe('deCH');
      expect(textMap.optGet('deCHKey1')).toBe('deCH1');
      expect(textMap.optGet('deKey1')).toBe('de1');
      expect(textMap.optGet('defaultKey1')).toBe('default1');

      expect(textMap.exists('theKey')).toBe(true);
      expect(textMap.exists('deCHKey1')).toBe(true);
      expect(textMap.exists('deKey1')).toBe(true);
      expect(textMap.exists('defaultKey1')).toBe(true);
    });

    it('creates an empty Texts object with correct linking if language tag is unknown', () => {
      texts.init(model);
      let textMap = texts.get('de-AT');
      expect(textMap instanceof TextMap).toBe(true);
      expect(Object.keys(textMap.map).length).toBe(0);
      expect(textMap.parent).toEqual(texts._get('de'));
    });

  });

});
