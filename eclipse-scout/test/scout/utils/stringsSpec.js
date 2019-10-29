/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import * as strings from '../../../src/scout/util/strings';

describe('strings', () => {

  describe('hasText', () => {

    it('can check if string has text', () => {
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

  describe('repeat', () => {

    it('can repeat strings', () => {
      expect(strings.repeat()).toBe(undefined);
      expect(strings.repeat('')).toBe('');
      expect(strings.repeat('X')).toBe('');
      expect(strings.repeat('X', 1)).toBe('X');
      expect(strings.repeat('Y', 7)).toBe('YYYYYYY');
      expect(strings.repeat('X', -7)).toBe('');
      expect(strings.repeat(4, 4)).toBe('4444');
    });

  });

  describe('padZeroLeft', () => {

    it('can pad strings with 0', () => {
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

  describe('startsWith', () => {

    it('can check if a string starts with another', () => {
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

  describe('count', () => {

    it('can count occurrences', () => {
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

  describe('encode', () => {

    it('encodes html', () => {
      expect(strings.encode()).toBeUndefined();
      expect(strings.encode('hello')).toBe('hello');
      expect(strings.encode('<b>hello</b>')).toBe('&lt;b&gt;hello&lt;/b&gt;');
      expect(strings.encode(123)).toBe('123');
    });
  });

  describe('join', () => {

    it('joins strings', () => {
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

    it('join works with array as second parameter', () => {
      expect(strings.join('-', ['hello', 'world'])).toBe('hello-world');
    });

  });

  describe('box', () => {

    it('boxes strings', () => {
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

  describe('uppercaseFirstLetter', () => {

    it('converts first letter to uppercase', () => {
      expect(strings.uppercaseFirstLetter()).toBe(undefined);
      expect(strings.uppercaseFirstLetter(null)).toBe(null);
      expect(strings.uppercaseFirstLetter(0)).toBe('0');
      expect(strings.uppercaseFirstLetter('0')).toBe('0');
      expect(strings.uppercaseFirstLetter('hans müller')).toBe('Hans müller');
      expect(strings.uppercaseFirstLetter('Hans Müller')).toBe('Hans Müller');
      expect(strings.uppercaseFirstLetter('äöü sind Umlaute')).toBe('Äöü sind Umlaute');
    });

  });

  describe('quote', () => {

    it('quotes special characters for regexp', () => {
      expect(strings.quote()).toBe(undefined);
      expect(strings.quote(null)).toBe(null);
      expect(strings.quote('bla')).toBe('bla');
      expect(strings.quote('foo. bar.')).toBe('foo\\. bar\\.');
      expect(strings.quote('ein * am Himmel')).toBe('ein \\* am Himmel');
      expect(strings.quote(123)).toBe('123');
    });

  });

  describe('asString', () => {

    it('converts input to string', () => {
      expect(strings.asString()).toBe(undefined);
      expect(strings.asString(null)).toBe(null);
      expect(strings.asString('bla')).toBe('bla');
      expect(strings.asString(false)).toBe('false');
      expect(strings.asString(-4747)).toBe('-4747');
      expect(strings.asString(0.123)).toBe('0.123');
      expect(strings.asString({})).toBe('[object Object]');
    });

  });

});
