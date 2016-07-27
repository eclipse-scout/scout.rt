/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/*jshint sub:true */
describe("scout.textProperties", function() {

  var model = {
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

  describe("init", function() {

    it("creates Texts objects for each language tag given in the model", function() {
      scout.textProperties.init(model);
      expect(scout.textProperties.textsByLocale['default']._exists('defaultKey1')).toBe(true);
      expect(scout.textProperties.textsByLocale['default']._exists('deKey1')).toBe(false);

      expect(scout.textProperties.textsByLocale['de']._exists('deKey1')).toBe(true);
      expect(scout.textProperties.textsByLocale['de']._exists('deCHKey1')).toBe(false);

      expect(scout.textProperties.textsByLocale['de-CH']._exists('deCHKey1')).toBe(true);
      expect(scout.textProperties.textsByLocale['de-CH']._exists('deKey1')).toBe(false);
    });

    it("links Texts objects according the sub tags of the language tag", function() {
      scout.textProperties.init(model);
      expect(scout.textProperties.textsByLocale['default'].parent).toBeUndefined();
      expect(scout.textProperties.textsByLocale['de'].parent).toBe(scout.textProperties.textsByLocale['default']);
      expect(scout.textProperties.textsByLocale['de-CH'].parent).toBe(scout.textProperties.textsByLocale['de']);
    });

  });

  describe("get", function() {

    it("returns the Texts for the given language tag", function() {
      scout.textProperties.init(model);
      var texts = scout.textProperties.get('de');
      expect(texts instanceof scout.Texts).toBe(true);
      expect(texts._exists('deKey1')).toBe(true);
      expect(texts._exists('deCHKey')).toBe(false);
    });

    it("returns a Texts object with correct linking", function() {
      scout.textProperties.init(model);
      var texts = scout.textProperties.get('de-CH');
      expect(texts.get('theKey')).toBe('deCH');
      expect(texts.get('deCHKey1')).toBe('deCH1');
      expect(texts.get('deKey1')).toBe('de1');
      expect(texts.get('defaultKey1')).toBe('default1');

      expect(texts.optGet('theKey')).toBe('deCH');
      expect(texts.optGet('deCHKey1')).toBe('deCH1');
      expect(texts.optGet('deKey1')).toBe('de1');
      expect(texts.optGet('defaultKey1')).toBe('default1');

      expect(texts.exists('theKey')).toBe(true);
      expect(texts.exists('deCHKey1')).toBe(true);
      expect(texts.exists('deKey1')).toBe(true);
      expect(texts.exists('defaultKey1')).toBe(true);
    });

    it("creates an empty Texts object with correct linking if language tag is unknown", function() {
      scout.textProperties.init(model);
      var texts = scout.textProperties.get('de-AT');
      expect(texts instanceof scout.Texts).toBe(true);
      expect(Object.keys(texts._textMap).length).toBe(0);
      expect(texts.parent).toBe(scout.textProperties.textsByLocale['de']);
    });

  });

});
