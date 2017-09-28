/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/*jshint sub:true */
describe("scout.texts", function() {

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

  afterEach(function() {
    // clear
    scout.texts.textsByLocale = {};
  });

  describe("init", function() {

    it("creates Texts objects for each language tag given in the model", function() {
      scout.texts.init(model);
      expect(scout.texts.textsByLocale['default']._exists('defaultKey1')).toBe(true);
      expect(scout.texts.textsByLocale['default']._exists('deKey1')).toBe(false);

      expect(scout.texts.textsByLocale['de']._exists('deKey1')).toBe(true);
      expect(scout.texts.textsByLocale['de']._exists('deCHKey1')).toBe(false);

      expect(scout.texts.textsByLocale['de-CH']._exists('deCHKey1')).toBe(true);
      expect(scout.texts.textsByLocale['de-CH']._exists('deKey1')).toBe(false);
    });

    it("links Texts objects according the sub tags of the language tag", function() {
      scout.texts.init(model);
      expect(scout.texts.textsByLocale['default'].parent).toBeUndefined();
      expect(scout.texts.textsByLocale['de'].parent).toBe(scout.texts.textsByLocale['default']);
      expect(scout.texts.textsByLocale['de-CH'].parent).toBe(scout.texts.textsByLocale['de']);
    });

    it("does not override existing text maps", function() {
      scout.texts.get('de-CH').add('existingKey', 'existingText');
      scout.texts.get('de').add('existingDeKey', 'existingDeText');
      scout.texts.get('de').add('theKey', 'thePreviousText');
      expect(scout.texts.get('de-CH').get('existingKey')).toBe('existingText');
      expect(scout.texts.get('de').get('existingDeKey')).toBe('existingDeText');
      expect(scout.texts.get('de').get('theKey')).toBe('thePreviousText');

      scout.texts.init(model);
      // Texts which were registered before must still be registered
      expect(scout.texts.get('de-CH').get('existingKey')).toBe('existingText');
      expect(scout.texts.get('de').get('existingDeKey')).toBe('existingDeText');

      // New texts need to be registered as well
      expect(scout.texts.get('de-CH').get('deCHKey1')).toBe('deCH1');
      expect(scout.texts.get('de').get('deKey2')).toBe('de2');

      // Texts which were registered but are part of the new model too are replaced
      expect(scout.texts.get('de').get('theKey')).toBe('de');
    });

  });

  describe("get", function() {

    it("returns the Texts for the given language tag", function() {
      scout.texts.init(model);
      var texts = scout.texts.get('de');
      expect(texts instanceof scout.TextMap).toBe(true);
      expect(texts._exists('deKey1')).toBe(true);
      expect(texts._exists('deCHKey')).toBe(false);
    });

    it("returns a Texts object with correct linking", function() {
      scout.texts.init(model);
      var texts = scout.texts.get('de-CH');
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
      scout.texts.init(model);
      var texts = scout.texts.get('de-AT');
      expect(texts instanceof scout.TextMap).toBe(true);
      expect(Object.keys(texts.map).length).toBe(0);
      expect(texts.parent).toBe(scout.texts.textsByLocale['de']);
    });

  });

});
