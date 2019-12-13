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

import * as strings from './../../src/util/strings';

describe('strings', function() {

  describe('nl2br', function() {

    it('can convert newlines to br tags', function() {
      expect(strings.nl2br()).toBe(undefined);
      expect(strings.nl2br(null)).toBe(null);
      expect(strings.nl2br('')).toBe('');
      expect(strings.nl2br('Hello')).toBe('Hello');
      expect(strings.nl2br('Hello\nGoodbye')).toBe('Hello<br>Goodbye');
      expect(strings.nl2br('Hello\nGoodbye\n')).toBe('Hello<br>Goodbye<br>');
      expect(strings.nl2br('Hello\n\nGoodbye')).toBe('Hello<br><br>Goodbye');
      expect(strings.nl2br('Hello\n\r\nGoodbye')).toBe('Hello<br><br>Goodbye');
      expect(strings.nl2br(123)).toBe('123');
    });

    it('encodes html, if the parameter is set to true (default)', function() {
      expect(strings.nl2br('<b>Hello</b>\nGoodbye')).toBe('&lt;b&gt;Hello&lt;/b&gt;<br>Goodbye');
      expect(strings.nl2br('Hello\n<br>\nGoodbye')).toBe('Hello<br>&lt;br&gt;<br>Goodbye');
    });

  });

  describe('hasText', function() {

    it('can check if string has text', function() {
      expect(strings.hasText()).toBe(false);
      expect(strings.hasText('')).toBe(false);
      expect(strings.hasText(' ')).toBe(false);
      expect(strings.hasText('Hello')).toBe(true);
      expect(strings.hasText('       .      ')).toBe(true);
      expect(strings.hasText('       \n      ')).toBe(false);
      expect(strings.hasText('       \n      \nn')).toBe(true);
      expect(strings.hasText(123)).toBe(true);
      expect(strings.hasText(0)).toBe(true);
    });

  });

  describe('repeat', function() {

    it('can repeat strings', function() {
      expect(strings.repeat()).toBe(undefined);
      expect(strings.repeat('')).toBe('');
      expect(strings.repeat('X')).toBe('');
      expect(strings.repeat('X', 1)).toBe('X');
      expect(strings.repeat('Y', 7)).toBe('YYYYYYY');
      expect(strings.repeat('X', -7)).toBe('');
      expect(strings.repeat(4, 4)).toBe('4444');
    });

  });

  describe('padZeroLeft', function() {

    it('can pad strings with 0', function() {
      expect(strings.padZeroLeft()).toBe(undefined);
      expect(strings.padZeroLeft('')).toBe('');
      expect(strings.padZeroLeft('X')).toBe('X');
      expect(strings.padZeroLeft('X', 1)).toBe('X');
      expect(strings.padZeroLeft('X', 7)).toBe('000000X');
      expect(strings.padZeroLeft('X', -7)).toBe('X');
      expect(strings.padZeroLeft(123, 4)).toBe('0123');
      expect(strings.padZeroLeft(12345, 4)).toBe('12345');
    });

  });

  describe('startsWith', function() {

    it('can check if a string starts with another', function() {
      expect(strings.startsWith('abc', 'a')).toBe(true);
      expect(strings.startsWith('abc', 'b')).toBe(false);
      expect(strings.startsWith('äabc', 'ä')).toBe(true);
      expect(strings.startsWith('äabc', 'Ä')).toBe(false);
      expect(strings.startsWith('abc', '')).toBe(true);
      expect(strings.startsWith('', '')).toBe(true);
      expect(strings.startsWith()).toBe(false);
      expect(strings.startsWith(undefined, 'hello')).toBe(false);
      expect(strings.startsWith('Der Himmel ist blau!', 'Der')).toBe(true);
      expect(strings.startsWith('¿Vive usted en España?', 'Vive')).toBe(false);
      expect(strings.startsWith('¿Vive usted en España?', '¿Vive')).toBe(true);
      expect(strings.startsWith(456, 4)).toBe(true);
      expect(strings.startsWith(456, 5)).toBe(false);
      expect(strings.startsWith(456, '4')).toBe(true);
      expect(strings.startsWith('456', 4)).toBe(true);
      expect(strings.startsWith(true, 't')).toBe(true);
      expect(strings.startsWith({}, {})).toBe(true);
      expect(strings.startsWith('xyz', {})).toBe(false);
      expect(strings.startsWith({a: 2}, {a: 3})).toBe(true); // because both objects return the same toString()
      expect(strings.startsWith(['a', 'b'], 'a')).toBe(true); // because arrays are converted to string
      expect(strings.startsWith('xyz', [])).toBe(true); // because arrays are converted to string
    });

  });

  describe('endsWith', function() {

    it('can check if a string ends with another', function() {
      expect(strings.endsWith('abc', 'c')).toBe(true);
      expect(strings.endsWith('abc', 'b')).toBe(false);
      expect(strings.endsWith('abcä', 'ä')).toBe(true);
      expect(strings.endsWith('abcä', 'Ä')).toBe(false);
      expect(strings.endsWith('abc', '')).toBe(true);
      expect(strings.endsWith('', '')).toBe(true);
      expect(strings.endsWith()).toBe(false);
      expect(strings.endsWith(undefined, 'hello')).toBe(false);
      expect(strings.endsWith('Der Himmel ist blau!', 'blau')).toBe(false);
      expect(strings.endsWith('Der Himmel ist blau!', 'blau!')).toBe(true);
      expect(strings.endsWith(1234, 4)).toBe(true);
      expect(strings.endsWith(1234, 5)).toBe(false);
      expect(strings.endsWith(1234, '4')).toBe(true);
      expect(strings.endsWith('1234', 4)).toBe(true);
    });

  });

  describe('count', function() {

    it('can count occurrences', function() {
      expect(strings.count()).toBe(0);
      expect(strings.count('hello')).toBe(0);
      expect(strings.count('hello', 'xxx')).toBe(0);
      expect(strings.count('hello', 'l')).toBe(2);
      expect(strings.count('hello', 'll')).toBe(1);
      expect(strings.count('hello', 'H')).toBe(0);
      expect(strings.count('hello', 'h')).toBe(1);
      expect(strings.count('hello! this a test. :-)', '  ')).toBe(0);
      expect(strings.count('hello! this a test. :-)', ' ')).toBe(4);
      expect(strings.count('{"validJson": true, "example": "ümlauts"}', 'ü')).toBe(1);
      expect(strings.count('{"validJson": true, "example": "ümlauts"}', '"')).toBe(6);
      expect(strings.count('the bird is the word', 'rd')).toBe(2);
      expect(strings.count(98138165, 1)).toBe(2);
    });

  });

  describe('join', function() {

    it('joins strings', function() {
      expect(strings.join()).toBe('');
      expect(strings.join('')).toBe('');
      expect(strings.join(' ')).toBe('');
      expect(strings.join('hello')).toBe('');
      expect(strings.join('hello', undefined)).toBe('');
      expect(strings.join('hello', 'world')).toBe('world');
      expect(strings.join('hello', 'world', '!')).toBe('worldhello!');
      expect(strings.join(' ', 'hello', 'world', '!')).toBe('hello world !');
      expect(strings.join(' ', 'hello', undefined, '!')).toBe('hello !');
      expect(strings.join(' ', 'hello', null, '!')).toBe('hello !');
      expect(strings.join(' ', 'hello', '', '!')).toBe('hello !');
      expect(strings.join('  ', ' ', '', ' ')).toBe('    ');
      expect(strings.join(undefined, 'one', 'two', 'three')).toBe('onetwothree');
      expect(strings.join('', 'one', 'two', 'three')).toBe('onetwothree');
      expect(strings.join(2, 0, 0, 0)).toBe('02020');
    });

    it('join works with array as second parameter', function() {
      expect(strings.join('-', ['hello', 'world'])).toBe('hello-world');
    });

  });

  describe('box', function() {

    it('boxes strings', function() {
      expect(strings.box()).toBe('');
      expect(strings.box('(')).toBe('');
      expect(strings.box('(', undefined)).toBe('');
      expect(strings.box('(', 'x')).toBe('(x');
      expect(strings.box(undefined, 'x')).toBe('x');
      expect(strings.box('(', 'x', ')')).toBe('(x)');
      expect(strings.box('   (', 'x ', ')')).toBe('   (x )');
      expect(strings.box(' (', 'x  ')).toBe(' (x  ');
      expect(strings.box('(', 'x', ')', 'y')).toBe('(x)');
      expect(strings.box('', 'x', '')).toBe('x');
      expect(strings.box('a', ' ', 'b')).toBe('');
      expect(strings.box(0, -3, 7)).toBe('0-37');
    });

  });

  describe('lowercaseFirstLetter', function() {

    it('converts first letter to lowercase', function() {
      expect(strings.lowercaseFirstLetter()).toBe(undefined);
      expect(strings.lowercaseFirstLetter(null)).toBe(null);
      expect(strings.lowercaseFirstLetter(0)).toBe('0');
      expect(strings.lowercaseFirstLetter('0')).toBe('0');
      expect(strings.lowercaseFirstLetter('hans müller')).toBe('hans müller');
      expect(strings.lowercaseFirstLetter('Hans Müller')).toBe('hans Müller');
      expect(strings.lowercaseFirstLetter('ÄÖÜ sind Umlaute')).toBe('äÖÜ sind Umlaute');
    });

  });

  describe('uppercaseFirstLetter', function() {

    it('converts first letter to uppercase', function() {
      expect(strings.uppercaseFirstLetter()).toBe(undefined);
      expect(strings.uppercaseFirstLetter(null)).toBe(null);
      expect(strings.uppercaseFirstLetter(0)).toBe('0');
      expect(strings.uppercaseFirstLetter('0')).toBe('0');
      expect(strings.uppercaseFirstLetter('hans müller')).toBe('Hans müller');
      expect(strings.uppercaseFirstLetter('Hans Müller')).toBe('Hans Müller');
      expect(strings.uppercaseFirstLetter('äöü sind Umlaute')).toBe('Äöü sind Umlaute');
    });

  });

  describe('quote', function() {

    it('quotes special characters for regexp', function() {
      expect(strings.quote()).toBe(undefined);
      expect(strings.quote(null)).toBe(null);
      expect(strings.quote('bla')).toBe('bla');
      expect(strings.quote('foo. bar.')).toBe('foo\\. bar\\.');
      expect(strings.quote('ein * am Himmel')).toBe('ein \\* am Himmel');
      expect(strings.quote(123)).toBe('123');
    });

  });

  describe('asString', function() {

    it('converts input to string', function() {
      expect(strings.asString()).toBe(undefined);
      expect(strings.asString(null)).toBe(null);
      expect(strings.asString('bla')).toBe('bla');
      expect(strings.asString(false)).toBe('false');
      expect(strings.asString(-4747)).toBe('-4747');
      expect(strings.asString(0.123)).toBe('0.123');
      expect(strings.asString({})).toBe('[object Object]');
    });

  });

  describe('insertAt', function() {

    it('can insert strings into other strings', function() {
      expect(strings.insertAt()).toBe(undefined);
      expect(strings.insertAt(null)).toBe(null);
      expect(strings.insertAt('')).toBe('');
      expect(strings.insertAt('Hello')).toBe('Hello');
      expect(strings.insertAt('Hello', '_')).toBe('Hello');
      expect(strings.insertAt('Hello', '_', 0)).toBe('_Hello');
      expect(strings.insertAt('Hello', '_', 3)).toBe('Hel_lo');
      expect(strings.insertAt('Hello', '_', 200)).toBe('Hello_');
      expect(strings.insertAt('Hello', '_', 'bla')).toBe('Hello');
    });

  });

  describe('nvl', function() {

    it('returns an empty string when input is null or undefined', function() {
      expect(strings.nvl(null)).toBe('');
      expect(strings.nvl(undefined)).toBe('');
      expect(strings.nvl('')).toBe('');
      expect(strings.nvl('foo')).toBe('foo');
    });

    it('should throw an error when called with more than one parameter', function() {
      expect(function() {
        strings.nvl(null, 'foo');
      }).toThrow();
    });

  });

  describe('countCodePoints', function() {

    it('returns the number of codepoints in a string', function() {
      expect(strings.countCodePoints('')).toBe(0);
      expect(strings.countCodePoints('foo')).toBe(3);
      expect(strings.countCodePoints('\uD83D\uDC4D')).toBe(1); // \uD83D\uDC4D is Unicode Character 'THUMBS UP SIGN' (U+1F44D)
    });

  });

  describe('splitMax', function() {

    it('returns not more than limit elements', function() {
      expect(strings.splitMax()).toEqual([]);
      expect(strings.splitMax('')).toEqual(['']);
      expect(strings.splitMax('abc')).toEqual(['abc']);
      expect(strings.splitMax('abc', '')).toEqual(['a', 'b', 'c']);
      expect(strings.splitMax('abc', '', 1)).toEqual(['abc']);
      expect(strings.splitMax('abc', '', 0)).toEqual(['a', 'b', 'c']);
      expect(strings.splitMax('abc', '', -10)).toEqual(['a', 'b', 'c']);
      expect(strings.splitMax('abc', '', 2)).toEqual(['a', 'bc']);
      expect(strings.splitMax('abc', '', 33)).toEqual(['a', 'b', 'c']);
      expect(strings.splitMax('a-b-c', 'x', 2)).toEqual(['a-b-c']);
      expect(strings.splitMax('a-b-c', 'b', 2)).toEqual(['a-', '-c']);
      expect(strings.splitMax('a-b-c', '-', 2)).toEqual(['a', 'b-c']);
    });

  });

  describe('removePrefix and removeSuffix', function() {

    it('removePrefix', function() {
      expect(strings.removePrefix('crm.CodeType', 'crm.')).toBe('CodeType');
      expect(strings.removePrefix('crm.CodeType', 'foo.')).toBe('crm.CodeType');
    });

    it('removeSuffix', function() {
      expect(strings.removeSuffix('avatar.gif', '.gif')).toBe('avatar');
      expect(strings.removeSuffix('avatar.gif', '.exe')).toBe('avatar.gif');
    });

  });

});
