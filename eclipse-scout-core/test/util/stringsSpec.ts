/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {strings} from './../../src/util/strings';

describe('strings', () => {

  describe('nl2br', () => {

    it('can convert newlines to br tags', () => {
      // @ts-expect-error
      expect(strings.nl2br()).toBe(undefined);
      expect(strings.nl2br(null)).toBe(null);
      expect(strings.nl2br('')).toBe('');
      expect(strings.nl2br('Hello')).toBe('Hello');
      expect(strings.nl2br('Hello\nGoodbye')).toBe('Hello<br>Goodbye');
      expect(strings.nl2br('Hello\nGoodbye\n')).toBe('Hello<br>Goodbye<br>');
      expect(strings.nl2br('Hello\n\nGoodbye')).toBe('Hello<br><br>Goodbye');
      expect(strings.nl2br('Hello\n\r\nGoodbye')).toBe('Hello<br><br>Goodbye');
      // @ts-expect-error
      expect(strings.nl2br(123)).toBe('123');
    });

    it('encodes html, if the parameter is set to true (default)', () => {
      expect(strings.nl2br('<b>Hello</b>\nGoodbye')).toBe('&lt;b&gt;Hello&lt;/b&gt;<br>Goodbye');
      expect(strings.nl2br('Hello\n<br>\nGoodbye')).toBe('Hello<br>&lt;br&gt;<br>Goodbye');
    });

  });

  describe('hasText', () => {

    it('can check if string has text', () => {
      // @ts-expect-error
      expect(strings.hasText()).toBe(false);
      expect(strings.hasText('')).toBe(false);
      expect(strings.hasText(' ')).toBe(false);
      expect(strings.hasText('Hello')).toBe(true);
      expect(strings.hasText('       .      ')).toBe(true);
      expect(strings.hasText('       \n      ')).toBe(false);
      expect(strings.hasText('       \n      \nn')).toBe(true);
      // @ts-expect-error
      expect(strings.hasText(123)).toBe(true);
      // @ts-expect-error
      expect(strings.hasText(0)).toBe(true);
    });

  });

  describe('repeat', () => {

    it('can repeat strings', () => {
      // @ts-expect-error
      expect(strings.repeat()).toBe(undefined);
      // @ts-expect-error
      expect(strings.repeat('')).toBe('');
      // @ts-expect-error
      expect(strings.repeat('X')).toBe('');
      expect(strings.repeat('X', 1)).toBe('X');
      expect(strings.repeat('Y', 7)).toBe('YYYYYYY');
      expect(strings.repeat('X', -7)).toBe('');
      // @ts-expect-error
      expect(strings.repeat(4, 4)).toBe('4444');
    });

  });

  describe('padZeroLeft', () => {

    it('can pad strings with 0', () => {
      // @ts-expect-error
      expect(strings.padZeroLeft()).toBe(undefined);
      // @ts-expect-error
      expect(strings.padZeroLeft('')).toBe('');
      // @ts-expect-error
      expect(strings.padZeroLeft('X')).toBe('X');
      expect(strings.padZeroLeft('X', 1)).toBe('X');
      expect(strings.padZeroLeft('X', 7)).toBe('000000X');
      expect(strings.padZeroLeft('X', -7)).toBe('X');
      expect(strings.padZeroLeft(123, 4)).toBe('0123');
      expect(strings.padZeroLeft(12345, 4)).toBe('12345');
    });

  });

  describe('startsWith', () => {

    it('can check if a string starts with another', () => {
      expect(strings.startsWith('abc', 'a')).toBe(true);
      expect(strings.startsWith('abc', 'b')).toBe(false);
      expect(strings.startsWith('äabc', 'ä')).toBe(true);
      expect(strings.startsWith('äabc', 'Ä')).toBe(false);
      expect(strings.startsWith('abc', '')).toBe(true);
      expect(strings.startsWith('', '')).toBe(true);
      // @ts-expect-error
      expect(strings.startsWith()).toBe(false);
      expect(strings.startsWith(undefined, 'hello')).toBe(false);
      expect(strings.startsWith('Der Himmel ist blau!', 'Der')).toBe(true);
      expect(strings.startsWith('¿Vive usted en España?', 'Vive')).toBe(false);
      expect(strings.startsWith('¿Vive usted en España?', '¿Vive')).toBe(true);
      // @ts-expect-error
      expect(strings.startsWith(456, 4)).toBe(true);
      // @ts-expect-error
      expect(strings.startsWith(456, 5)).toBe(false);
      // @ts-expect-error
      expect(strings.startsWith(456, '4')).toBe(true);
      // @ts-expect-error
      expect(strings.startsWith('456', 4)).toBe(true);
      // @ts-expect-error
      expect(strings.startsWith(true, 't')).toBe(true);
      // @ts-expect-error
      expect(strings.startsWith({}, {})).toBe(true);
      // @ts-expect-error
      expect(strings.startsWith('xyz', {})).toBe(false);
      // @ts-expect-error
      expect(strings.startsWith({a: 2}, {a: 3})).toBe(true); // because both objects return the same toString()
      // @ts-expect-error
      expect(strings.startsWith(['a', 'b'], 'a')).toBe(true); // because arrays are converted to string
      // @ts-expect-error
      expect(strings.startsWith('xyz', [])).toBe(true); // because arrays are converted to string
    });

  });

  describe('endsWith', () => {

    it('can check if a string ends with another', () => {
      expect(strings.endsWith('abc', 'c')).toBe(true);
      expect(strings.endsWith('abc', 'b')).toBe(false);
      expect(strings.endsWith('abcä', 'ä')).toBe(true);
      expect(strings.endsWith('abcä', 'Ä')).toBe(false);
      expect(strings.endsWith('abc', '')).toBe(true);
      expect(strings.endsWith('', '')).toBe(true);
      // @ts-expect-error
      expect(strings.endsWith()).toBe(false);
      expect(strings.endsWith(undefined, 'hello')).toBe(false);
      expect(strings.endsWith('Der Himmel ist blau!', 'blau')).toBe(false);
      expect(strings.endsWith('Der Himmel ist blau!', 'blau!')).toBe(true);
      // @ts-expect-error
      expect(strings.endsWith(1234, 4)).toBe(true);
      // @ts-expect-error
      expect(strings.endsWith(1234, 5)).toBe(false);
      // @ts-expect-error
      expect(strings.endsWith(1234, '4')).toBe(true);
      // @ts-expect-error
      expect(strings.endsWith('1234', 4)).toBe(true);
    });

  });

  describe('count', () => {

    it('can count occurrences', () => {
      // @ts-expect-error
      expect(strings.count()).toBe(0);
      // @ts-expect-error
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
      // @ts-expect-error
      expect(strings.count(98138165, 1)).toBe(2);
    });

  });

  describe('join', () => {

    it('joins strings', () => {
      // @ts-expect-error
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
      // @ts-expect-error
      expect(strings.join(2, 0, 0, 0)).toBe('02020');
    });

    it('join works with array as second parameter', () => {
      // @ts-expect-error
      expect(strings.join('-', ['hello', 'world'])).toBe('hello-world');
    });

  });

  describe('box', () => {

    it('boxes strings', () => {
      // @ts-expect-error
      expect(strings.box()).toBe('');
      // @ts-expect-error
      expect(strings.box('(')).toBe('');
      expect(strings.box('(', undefined)).toBe('');
      expect(strings.box('(', 'x')).toBe('(x');
      expect(strings.box(undefined, 'x')).toBe('x');
      expect(strings.box('(', 'x', ')')).toBe('(x)');
      expect(strings.box('   (', 'x ', ')')).toBe('   (x )');
      expect(strings.box(' (', 'x  ')).toBe(' (x  ');
      // @ts-expect-error
      expect(strings.box('(', 'x', ')', 'y')).toBe('(x)');
      expect(strings.box('', 'x', '')).toBe('x');
      expect(strings.box('a', ' ', 'b')).toBe('');
      // @ts-expect-error
      expect(strings.box(0, -3, 7)).toBe('0-37');
    });

  });

  describe('length', () => {

    it('counts the length', () => {
      // @ts-expect-error
      expect(strings.length()).toBe(0);
      expect(strings.length(null)).toBe(0);
      expect(strings.length('')).toBe(0);
      // @ts-expect-error
      expect(strings.length(0)).toBe(1); // "0"
      // @ts-expect-error
      expect(strings.length(true)).toBe(4); // "true"
      // @ts-expect-error
      expect(strings.length({})).toBe(15); // "[object Object]"
      expect(strings.length(' ')).toBe(1);
      expect(strings.length(' xyz ')).toBe(5);
      expect(strings.length('äöü')).toBe(3);
    });

  });

  describe('trim', () => {

    it('trims white space', () => {
      // @ts-expect-error
      expect(strings.trim()).toBe(undefined);
      expect(strings.trim(null)).toBe(null);
      expect(strings.trim('')).toBe('');
      // @ts-expect-error
      expect(strings.trim(0)).toBe('0');
      // @ts-expect-error
      expect(strings.trim(true)).toBe('true');
      // @ts-expect-error
      expect(strings.trim({})).toBe('[object Object]');
      expect(strings.trim('0')).toBe('0');
      expect(strings.trim(' ')).toBe('');
      expect(strings.trim(' xyz ')).toBe('xyz');
      expect(strings.trim('  abc  def')).toBe('abc  def');
    });

  });

  describe('toLowerCase', () => {

    it('converts text to lower case', () => {
      // @ts-expect-error
      expect(strings.toLowerCase()).toBe(undefined);
      expect(strings.toLowerCase(null)).toBe(null);
      expect(strings.toLowerCase('')).toBe('');
      // @ts-expect-error
      expect(strings.toLowerCase(0)).toBe('0');
      // @ts-expect-error
      expect(strings.toLowerCase(true)).toBe('true');
      // @ts-expect-error
      expect(strings.toLowerCase({})).toBe('[object object]');
      expect(strings.toLowerCase('0')).toBe('0');
      expect(strings.toLowerCase(' ')).toBe(' ');
      expect(strings.toLowerCase(' XYZ ')).toBe(' xyz ');
      expect(strings.toLowerCase('Abc deF')).toBe('abc def');
      expect(strings.toLowerCase('$TEST')).toBe('$test');
      expect(strings.toLowerCase('Äöü sind Umlaute')).toBe('äöü sind umlaute');
    });

  });

  describe('toUpperCaseFirstLetter', () => {

    it('converts first letter to upper case', () => {
      // @ts-expect-error
      expect(strings.toUpperCaseFirstLetter()).toBe(undefined);
      expect(strings.toUpperCaseFirstLetter(null)).toBe(null);
      expect(strings.toUpperCaseFirstLetter('')).toBe('');
      // @ts-expect-error
      expect(strings.toUpperCaseFirstLetter(0)).toBe('0');
      // @ts-expect-error
      expect(strings.toUpperCaseFirstLetter(true)).toBe('True');
      // @ts-expect-error
      expect(strings.toUpperCaseFirstLetter({})).toBe('[object Object]');
      expect(strings.toUpperCaseFirstLetter('0')).toBe('0');
      expect(strings.toUpperCaseFirstLetter(' ')).toBe(' ');
      expect(strings.toUpperCaseFirstLetter(' xyz ')).toBe(' xyz ');
      expect(strings.toUpperCaseFirstLetter('abc def')).toBe('Abc def');
      expect(strings.toUpperCaseFirstLetter('ABC DEF')).toBe('ABC DEF');
      expect(strings.toUpperCaseFirstLetter('aBC dEF')).toBe('ABC dEF');
      expect(strings.toUpperCaseFirstLetter('$test')).toBe('$test');
      expect(strings.toUpperCaseFirstLetter('äöü sind Umlaute')).toBe('Äöü sind Umlaute');
    });

  });

  describe('toLowerCaseFirstLetter', () => {

    it('converts first letter to lower case', () => {
      // @ts-expect-error
      expect(strings.toLowerCaseFirstLetter()).toBe(undefined);
      expect(strings.toLowerCaseFirstLetter(null)).toBe(null);
      expect(strings.toLowerCaseFirstLetter('')).toBe('');
      // @ts-expect-error
      expect(strings.toLowerCaseFirstLetter(0)).toBe('0');
      // @ts-expect-error
      expect(strings.toLowerCaseFirstLetter(true)).toBe('true');
      // @ts-expect-error
      expect(strings.toLowerCaseFirstLetter({})).toBe('[object Object]');
      expect(strings.toLowerCaseFirstLetter('0')).toBe('0');
      expect(strings.toLowerCaseFirstLetter(' ')).toBe(' ');
      expect(strings.toLowerCaseFirstLetter(' XYZ ')).toBe(' XYZ ');
      expect(strings.toLowerCaseFirstLetter('ABC DEF')).toBe('aBC DEF');
      expect(strings.toLowerCaseFirstLetter('abc def')).toBe('abc def');
      expect(strings.toLowerCaseFirstLetter('Abc Def')).toBe('abc Def');
      expect(strings.toLowerCaseFirstLetter('$TEST')).toBe('$TEST');
      expect(strings.toLowerCaseFirstLetter('ÄÖÜ sind Umlaute')).toBe('äÖÜ sind Umlaute');
    });

  });

  describe('quote', () => {

    it('quotes special characters for regexp', () => {
      // @ts-expect-error
      expect(strings.quote()).toBe(undefined);
      expect(strings.quote(null)).toBe(null);
      expect(strings.quote('bla')).toBe('bla');
      expect(strings.quote('foo. bar.')).toBe('foo\\. bar\\.');
      expect(strings.quote('ein * am Himmel')).toBe('ein \\* am Himmel');
      // @ts-expect-error
      expect(strings.quote(123)).toBe('123');
    });

  });

  describe('asString', () => {

    it('converts input to string', () => {
      // @ts-expect-error
      expect(strings.asString()).toBe(undefined);
      expect(strings.asString(null)).toBe(null);
      expect(strings.asString('bla')).toBe('bla');
      expect(strings.asString(false)).toBe('false');
      expect(strings.asString(-4747)).toBe('-4747');
      expect(strings.asString(0.123)).toBe('0.123');
      expect(strings.asString({})).toBe('[object Object]');
    });

  });

  describe('insertAt', () => {

    it('can insert strings into other strings', () => {
      // @ts-expect-error
      expect(strings.insertAt()).toBe(undefined);
      // @ts-expect-error
      expect(strings.insertAt(null)).toBe(null);
      // @ts-expect-error
      expect(strings.insertAt('')).toBe('');
      // @ts-expect-error
      expect(strings.insertAt('Hello')).toBe('Hello');
      // @ts-expect-error
      expect(strings.insertAt('Hello', '_')).toBe('Hello');
      expect(strings.insertAt('Hello', '_', 0)).toBe('_Hello');
      expect(strings.insertAt('Hello', '_', 3)).toBe('Hel_lo');
      expect(strings.insertAt('Hello', '_', 200)).toBe('Hello_');
      // @ts-expect-error
      expect(strings.insertAt('Hello', '_', 'bla')).toBe('Hello');
    });

  });

  describe('nvl', () => {

    it('returns an empty string when input is null or undefined', () => {
      expect(strings.nvl(null)).toBe('');
      expect(strings.nvl(undefined)).toBe('');
      expect(strings.nvl('')).toBe('');
      expect(strings.nvl('foo')).toBe('foo');
    });

    it('should throw an error when called with more than one parameter', () => {
      expect(() => {
        // @ts-expect-error
        strings.nvl(null, 'foo');
      }).toThrow();
    });

  });

  describe('countCodePoints', () => {

    it('returns the number of code points in a string', () => {
      expect(strings.countCodePoints('')).toBe(0);
      expect(strings.countCodePoints('foo')).toBe(3);
      expect(strings.countCodePoints('\uD83D\uDC4D')).toBe(1); // \uD83D\uDC4D is Unicode Character 'THUMBS UP SIGN' (U+1F44D)
    });

  });

  describe('splitMax', () => {

    it('returns not more than limit elements', () => {
      // @ts-expect-error
      expect(strings.splitMax()).toEqual([]);
      // @ts-expect-error
      expect(strings.splitMax('')).toEqual(['']);
      // @ts-expect-error
      expect(strings.splitMax('abc')).toEqual(['abc']);
      // @ts-expect-error
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

  describe('removePrefix and removeSuffix', () => {

    it('removePrefix', () => {
      expect(strings.removePrefix('crm.CodeType', 'crm.')).toBe('CodeType');
      expect(strings.removePrefix('crm.CodeType', 'foo.')).toBe('crm.CodeType');
    });

    it('removeSuffix', () => {
      expect(strings.removeSuffix('avatar.gif', '.gif')).toBe('avatar');
      expect(strings.removeSuffix('avatar.gif', '.exe')).toBe('avatar.gif');
    });

  });

  describe('truncateText', () => {
    it('returns the truncated text', () => {
      let loremIpsum = 'Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.';
      expect(strings.truncateText(loremIpsum, 120)).toBe(loremIpsum);
      expect(strings.truncateText(loremIpsum + ' ', 117)).toBe(loremIpsum);
      expect(strings.truncateText(' ' + loremIpsum, 117)).toBe(loremIpsum);
      expect(strings.truncateText(loremIpsum, 100)).toBe('Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et...');
      expect(strings.truncateText(loremIpsum, 50)).toBe('Lorem ipsum dolor sit amet, consectetur adipisi...');
      expect(strings.truncateText(loremIpsum, 0)).toBe(loremIpsum);
    });

    it('accepts a custom measureText function', () => {
      let measureText = text => ({
        width: text.length - 3
      });
      let loremIpsum = 'Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.';
      expect(strings.truncateText(loremIpsum, 120, measureText)).toBe(loremIpsum);
      expect(strings.truncateText(loremIpsum, 100, measureText)).toBe('Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dol...');
      expect(strings.truncateText(loremIpsum, 0, measureText)).toBe(loremIpsum);
    });

    it('does not fail if a number is passed', () => {
      expect(strings.truncateText(1234567 as any, 5)).toBe('12...');
    });
  });
});
