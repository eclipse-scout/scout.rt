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
describe("texts", function() {

    // In production mode these texts are sent by the server in the initialize event of the session
    var texts = new scout.Texts({
      NoOptions: 'Keine Übereinstimmung',
      NumOptions: '{0} Optionen',
      Greeting: 'Hello {0}, my name is {2}, {1}.',
      WrongInput: 'Wrong input: {0}. Try to replace {0} with {1}.',
      Empty: '',
      Null: null
    });

    it("check if correct text is returned", function() {
      expect(texts.get('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it("check if empty text is returned", function() {
      expect(texts.get('Empty')).toBe('');
    });

    it("check if null text is returned", function() {
      expect(texts.get('Null')).toBe(null);
    });

    it("check if arguments are replaced in text", function() {
      expect(texts.get('NumOptions', 3)).toBe('3 Optionen');
    });

    it("check if multiple arguments are replaced in text", function() {
      expect(texts.get('Greeting', 'Computer', 'nice to meet you', 'User')).toBe('Hello Computer, my name is User, nice to meet you.');
    });

    it("check if undefined texts return an error message", function() {
      expect(texts.get('DoesNotExist')).toBe('[undefined text: DoesNotExist]');
    });

    it("optGet returns undefined if key is not found", function() {
      expect(texts.optGet('DoesNotExist')).toBe(undefined);
    });

    it("optGet returns default value if key is not found", function() {
      expect(texts.optGet('DoesNotExist', '#Default', 'Any argument')).toBe('#Default');
    });

    it("optGet returns text if key found", function() {
      expect(texts.optGet('NoOptions')).toBe('Keine Übereinstimmung');
    });

    it("optGet returns text if key found, with arguments", function() {
      expect(texts.optGet('NumOptions', '#Default', 7)).toBe('7 Optionen');
    });

    it("check if the same placeholder can be used multiple times", function() {
      expect(texts.get('WrongInput', 'red', 'blue')).toBe('Wrong input: red. Try to replace red with blue.');
    });

  });
