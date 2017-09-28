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
describe("TextMap", function() {

  var texts = new scout.TextMap({
    NoOptions: 'Keine Übereinstimmung',
    NumOptions: '{0} Optionen',
    Greeting: 'Hello {0}, my name is {2}, {1}.',
    WrongInput: 'Wrong input: {0}. Try to replace {0} with {1}.',
    Empty: '',
    Null: null
  });

  var textsChild = new scout.TextMap({
    ChildKey: 'A Child Key',
    DuplicateKey: 'Child Duplicate Key'
  });

  var textsParent = new scout.TextMap({
    ParentKey: 'A Parent Key',
    DuplicateKey: 'Parent Duplicate Key'
  });
  textsChild.setParent(textsParent);

  describe("get", function() {

    it("returns correct text for key", function() {
      expect(texts.get('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it("may return empty text", function() {
      expect(texts.get('Empty')).toBe('');
    });

    it("may return null text", function() {
      expect(texts.get('Null')).toBe(null);
    });

    it("replaces arguments in text", function() {
      expect(texts.get('NumOptions', 3)).toBe('3 Optionen');
    });

    it("may replace multiple arguments", function() {
      expect(texts.get('Greeting', 'Computer', 'nice to meet you', 'User')).toBe('Hello Computer, my name is User, nice to meet you.');
    });

    it("returns a text containing undefinied if the key is not found", function() {
      expect(texts.get('DoesNotExist')).toBe('[undefined text: DoesNotExist]');
    });

    it("does a parent lookup if key is not found", function() {
      expect(textsChild.get('ChildKey')).toBe('A Child Key');
      expect(textsChild.get('ParentKey')).toBe('A Parent Key');
      expect(textsChild.get('DuplicateKey')).toBe('Child Duplicate Key');

      expect(textsParent.get('ChildKey')).toBe('[undefined text: ChildKey]');
      expect(textsParent.get('ParentKey')).toBe('A Parent Key');
      expect(textsParent.get('DuplicateKey')).toBe('Parent Duplicate Key');
    });

    it("returns a text containing undefinied if neither child nor parent contains the key", function() {
      expect(textsChild.get('abc')).toBe('[undefined text: abc]');
    });

  });

  describe("optGet", function() {

    it("returns undefined if key is not found", function() {
      expect(texts.optGet('DoesNotExist')).toBe(undefined);
    });

    it("returns default value if key is not found", function() {
      expect(texts.optGet('DoesNotExist', '#Default', 'Any argument')).toBe('#Default');
    });

    it("returns text if key is found", function() {
      expect(texts.optGet('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it("returns text if key is found, with arguments", function() {
      expect(texts.optGet('NumOptions', '#Default', 7)).toBe('7 Optionen');
    });

    it("replaces the same placeholder if used multiple times", function() {
      expect(texts.get('WrongInput', 'red', 'blue')).toBe('Wrong input: red. Try to replace red with blue.');
    });

    it("does a parent lookup if key is not found", function() {
      expect(textsChild.optGet('ChildKey')).toBe('A Child Key');
      expect(textsChild.optGet('ParentKey')).toBe('A Parent Key');

      expect(textsParent.optGet('ChildKey')).toBeUndefined();
      expect(textsParent.optGet('ChildKey', 'default value')).toBe('default value');
      expect(textsParent.optGet('ParentKey')).toBe('A Parent Key');
    });

  });

});
