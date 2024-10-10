/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TextMap, texts} from '../../src/index';

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
      // @ts-expect-error
      expect(texts._get('default')._exists('defaultKey1')).toBe(true);
      // @ts-expect-error
      expect(texts._get('default')._exists('deKey1')).toBe(false);

      // @ts-expect-error
      expect(texts._get('de')._exists('deKey1')).toBe(true);
      // @ts-expect-error
      expect(texts._get('de')._exists('deCHKey1')).toBe(false);

      // @ts-expect-error
      expect(texts._get('de-CH')._exists('deCHKey1')).toBe(true);
      // @ts-expect-error
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
      expect(textMap.exists('deKey1')).toBe(true);
      expect(textMap.exists('deCHKey')).toBe(false);
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

  describe('resolve', () => {

    it('resolves text', () => {
      texts.init(model);
      expect(texts.resolveText('${textKey:theKey}', 'de')).toBe('de');
      expect(texts.resolveText('${textKey:_DoesNotExist}', 'de')).toBe('[undefined text: _DoesNotExist]');
      expect(texts.resolveText('foo ${textKey:theKey}', 'de')).toBe('foo ${textKey:theKey}');
      expect(texts.resolveText('bar', 'de')).toBe('bar');
    });

    it('resolves text property', () => {
      texts.init(model);

      setFixtures(sandbox());
      let session = sandboxSession();

      // With session on object

      let obj = {
        session: session,
        text: '${textKey:theKey}',
        xyz: '${textKey:theKey}',
        foo: 'bar'
      };

      texts.resolveTextProperty(obj);
      expect(obj.text).toBe('deCH');
      expect(obj.xyz).toBe('${textKey:theKey}');
      expect(obj.foo).toBe('bar');

      texts.resolveTextProperty(obj, 'xyz');
      expect(obj.text).toBe('deCH');
      expect(obj.xyz).toBe('deCH');
      expect(obj.foo).toBe('bar');

      texts.resolveTextProperty(obj, 'foo');
      expect(obj.text).toBe('deCH');
      expect(obj.xyz).toBe('deCH');
      expect(obj.foo).toBe('bar');

      texts.resolveTextProperty(obj, 'doesNotExist');
      expect(obj.text).toBe('deCH');
      expect(obj.xyz).toBe('deCH');
      expect(obj.foo).toBe('bar');

      // Without session on object

      let obj2 = {
        text: '${textKey:theKey}',
        xyz: '${textKey:theKey}',
        foo: 'bar'
      };

      texts.resolveTextProperty(obj2, null, session);
      expect(obj2.text).toBe('deCH');
      expect(obj2.xyz).toBe('${textKey:theKey}');
      expect(obj2.foo).toBe('bar');

      texts.resolveTextProperty(obj2, 'xyz', session);
      expect(obj2.text).toBe('deCH');
      expect(obj2.xyz).toBe('deCH');
      expect(obj2.foo).toBe('bar');

      texts.resolveTextProperty(obj2, 'foo', session);
      expect(obj2.text).toBe('deCH');
      expect(obj2.xyz).toBe('deCH');
      expect(obj2.foo).toBe('bar');

      texts.resolveTextProperty(obj2, 'doesNotExist', session);
      expect(obj2.text).toBe('deCH');
      expect(obj2.xyz).toBe('deCH');
      expect(obj2.foo).toBe('bar');
    });
  });
});
