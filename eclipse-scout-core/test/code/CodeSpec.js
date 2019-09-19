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
import {Code, codes, Locale, texts} from '../../src/index';


describe('Code', function() {

  afterEach(function() {
    // cleanup
    delete texts.get('de').map['__code.123xy'];
    delete texts.get('en').map['__code.123xy'];
    delete texts.get('default').map['__code.123xy'];
  });

  describe('init', function() {

    it('registers texts if texts property is set', function() {
      var model = {
        id: '123xy',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };
      expect(texts.get('de').get('__code.123xy')).toBe('[undefined text: __code.123xy]');
      expect(texts.get('en').get('__code.123xy')).toBe('[undefined text: __code.123xy]');

      var code = new Code();
      code.init(model);
      expect(texts.get('de').get('__code.123xy')).toBe('Code 123xy auf Deutsch');
      expect(texts.get('en').get('__code.123xy')).toBe('Code 123xy in English');
    });

    it('uses the language configured by codes.defaultLanguage as default', function() {
      var model = {
        id: '123xy',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };

      var code = new Code();
      code.init(model);
      expect(codes.defaultLanguage).toBe('en');
      // fr is not defined -> use English / defaultLanguage
      expect(texts.get('fr').get('__code.123xy')).toBe('Code 123xy in English');
    });

    it('fails if text and texts are set', function() {
      var model = {
        id: '123xy',
        text: 'a text',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };

      var code = new Code();
      var func = code.init.bind(code, model);
      expect(func).toThrowError();
    });
  });

  describe('text', function() {

    it('returns the text for the given languageTag (with texts property)', function() {
      var model = {
        id: '123xy',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };

      var code = new Code();
      code.init(model);
      expect(code.text('de')).toBe('Code 123xy auf Deutsch');
      expect(code.text('en')).toBe('Code 123xy in English');
      expect(code.text('fr')).toBe('Code 123xy in English'); // default
    });

    it('returns the text for the given locale (with texts property)', function() {
      var model = {
        id: '123xy',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };

      var code = new Code();
      code.init(model);
      expect(code.text(new Locale({
        languageTag: 'de'
      }))).toBe('Code 123xy auf Deutsch');
      expect(code.text(new Locale({
        languageTag: 'en'
      }))).toBe('Code 123xy in English');
      expect(code.text(new Locale({
        languageTag: 'fr'
      }))).toBe('Code 123xy in English');
    });

    it('returns the text for the given languageTag (with text property)', function() {
      var model = {
        id: '123xy',
        text: 'a text'
      };

      var code = new Code();
      code.init(model);
      expect(code.text('de')).toBe('a text');
      expect(code.text('en')).toBe('a text');
    });

    it('returns the text for the given languageTag (with text property including a text key)', function() {
      var model = {
        id: '123xy',
        text: '${textKey:MyCodeKey}'
      };

      var code = new Code();
      code.init(model);
      expect(code.text('de')).toBe('[undefined text: MyCodeKey]');

      texts.get('de').add('MyCodeKey', 'übersetzter Text');
      expect(code.text('de')).toBe('übersetzter Text');
    });
  });
});
