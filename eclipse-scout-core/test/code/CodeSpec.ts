/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, codes, Locale, texts} from '../../src/index';

describe('Code', () => {

  afterEach(() => {
    // cleanup
    delete texts.get('de').map['__code.123xy'];
    delete texts.get('en').map['__code.123xy'];
    delete texts.get('default').map['__code.123xy'];
  });

  describe('init', () => {

    it('registers texts if texts property is set', () => {
      let model = {
        id: '123xy',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };
      expect(texts.get('de').get('__code.123xy')).toBe('[undefined text: __code.123xy]');
      expect(texts.get('en').get('__code.123xy')).toBe('[undefined text: __code.123xy]');

      let code = new Code();
      code.init(model);
      expect(texts.get('de').get('__code..123xy')).toBe('Code 123xy auf Deutsch');
      expect(texts.get('en').get('__code..123xy')).toBe('Code 123xy in English');
    });

    it('uses the language configured by codes.defaultLanguage as default', () => {
      let model = {
        id: '123xy',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };

      let code = new Code();
      code.init(model);
      expect(codes.defaultLanguage).toBe('en');
      // fr is not defined -> use English / defaultLanguage
      expect(texts.get('fr').get('__code..123xy')).toBe('Code 123xy in English');
    });

    it('fails if text and texts are set', () => {
      let model = {
        id: '123xy',
        text: 'a text',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };

      let code = new Code();
      let func = code.init.bind(code, model);
      expect(func).toThrowError();
    });
  });

  describe('text', () => {

    it('returns the text for the given languageTag (with texts property)', () => {
      let model = {
        id: '123xy',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };

      let code = new Code();
      code.init(model);
      expect(code.text('de')).toBe('Code 123xy auf Deutsch');
      expect(code.text('en')).toBe('Code 123xy in English');
      expect(code.text('fr')).toBe('Code 123xy in English'); // default
    });

    it('returns the text for the given locale (with texts property)', () => {
      let model = {
        id: '123xy',
        texts: {
          de: 'Code 123xy auf Deutsch',
          en: 'Code 123xy in English'
        }
      };

      let code = new Code();
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

    it('returns the text for the given languageTag (with text property)', () => {
      let model = {
        id: '123xy',
        text: 'a text'
      };

      let code = new Code();
      code.init(model);
      expect(code.text('de')).toBe('a text');
      expect(code.text('en')).toBe('a text');
    });

    it('returns the text for the given languageTag (with text property including a text key)', () => {
      let model = {
        id: '123xy',
        text: '${textKey:MyCodeKey}'
      };

      let code = new Code();
      code.init(model);
      expect(code.text('de')).toBe('[undefined text: MyCodeKey]');

      texts.get('de').add('MyCodeKey', 'übersetzter Text');
      expect(code.text('de')).toBe('übersetzter Text');
    });
  });
});
