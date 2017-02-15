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
describe("scout.strings", function() {

  describe("nl2br", function() {

    it("can convert newlines to br tags", function() {
      expect(scout.strings.nl2br()).toBe(undefined);
      expect(scout.strings.nl2br(null)).toBe(null);
      expect(scout.strings.nl2br('')).toBe('');
      expect(scout.strings.nl2br('Hello')).toBe('Hello');
      expect(scout.strings.nl2br('Hello\nGoodbye')).toBe('Hello<br>Goodbye');
      expect(scout.strings.nl2br('Hello\nGoodbye\n')).toBe('Hello<br>Goodbye<br>');
      expect(scout.strings.nl2br('Hello\n\nGoodbye')).toBe('Hello<br><br>Goodbye');
      expect(scout.strings.nl2br('Hello\n\r\nGoodbye')).toBe('Hello<br><br>Goodbye');
      expect(scout.strings.nl2br(123)).toBe('123');
    });

    it("encodes html, if the parameter is set to true (default)", function() {
      expect(scout.strings.nl2br('<b>Hello</b>\nGoodbye')).toBe('&lt;b&gt;Hello&lt;/b&gt;<br>Goodbye');
      expect(scout.strings.nl2br('Hello\n<br>\nGoodbye')).toBe('Hello<br>&lt;br&gt;<br>Goodbye');
    });

  });

  describe("hasText", function() {

    it("can check if string has text", function() {
      expect(scout.strings.hasText()).toBe(false);
      expect(scout.strings.hasText('')).toBe(false);
      expect(scout.strings.hasText(' ')).toBe(false);
      expect(scout.strings.hasText('Hello')).toBe(true);
      expect(scout.strings.hasText('       .      ')).toBe(true);
      expect(scout.strings.hasText('       \n      ')).toBe(false);
      expect(scout.strings.hasText('       \n      \nn')).toBe(true);
      expect(scout.strings.hasText(123)).toBe(true);
      expect(scout.strings.hasText(0)).toBe(true);
    });

  });

  describe("repeat", function() {

    it("can repeat strings", function() {
      expect(scout.strings.repeat()).toBe(undefined);
      expect(scout.strings.repeat('')).toBe('');
      expect(scout.strings.repeat('X')).toBe('');
      expect(scout.strings.repeat('X', 1)).toBe('X');
      expect(scout.strings.repeat('X', 7)).toBe('XXXXXXX');
      expect(scout.strings.repeat('X', -7)).toBe('');
      expect(scout.strings.repeat(4, 4)).toBe('4444');
    });

  });

  describe("padZeroLeft", function() {

    it("can pad strings with 0", function() {
      expect(scout.strings.padZeroLeft()).toBe(undefined);
      expect(scout.strings.padZeroLeft('')).toBe('');
      expect(scout.strings.padZeroLeft('X')).toBe('X');
      expect(scout.strings.padZeroLeft('X', 1)).toBe('X');
      expect(scout.strings.padZeroLeft('X', 7)).toBe('000000X');
      expect(scout.strings.padZeroLeft('X', -7)).toBe('X');
      expect(scout.strings.padZeroLeft(123, 4)).toBe('0123');
      expect(scout.strings.padZeroLeft(12345, 4)).toBe('12345');
    });

  });

  describe("startsWith", function() {

    it("can check if a string starts with another", function() {
      expect(scout.strings.startsWith('abc', 'a')).toBe(true);
      expect(scout.strings.startsWith('abc', 'b')).toBe(false);
      expect(scout.strings.startsWith('äabc', 'ä')).toBe(true);
      expect(scout.strings.startsWith('äabc', 'Ä')).toBe(false);
      expect(scout.strings.startsWith('abc', '')).toBe(true);
      expect(scout.strings.startsWith('', '')).toBe(true);
      expect(scout.strings.startsWith()).toBe(false);
      expect(scout.strings.startsWith(undefined, 'hello')).toBe(false);
      expect(scout.strings.startsWith('Der Himmel ist blau!', 'Der')).toBe(true);
      expect(scout.strings.startsWith('¿Vive usted en España?', 'Vive')).toBe(false);
      expect(scout.strings.startsWith('¿Vive usted en España?', '¿Vive')).toBe(true);
      expect(scout.strings.startsWith(456, 4)).toBe(true);
      expect(scout.strings.startsWith(456, 5)).toBe(false);
      expect(scout.strings.startsWith(456, '4')).toBe(true);
      expect(scout.strings.startsWith('456', 4)).toBe(true);
      expect(scout.strings.startsWith(true, 't')).toBe(true);
      expect(scout.strings.startsWith({}, {})).toBe(true);
      expect(scout.strings.startsWith('xyz', {})).toBe(false);
      expect(scout.strings.startsWith({a: 2}, {a: 3})).toBe(true); // because both objects return the same toString()
      expect(scout.strings.startsWith(['a', 'b'], 'a')).toBe(true); // because arrays are converted to string
      expect(scout.strings.startsWith('xyz', [])).toBe(true); // because arrays are converted to string
    });

  });

  describe("endsWith", function() {

    it("can check if a string ends with another", function() {
      expect(scout.strings.endsWith('abc', 'c')).toBe(true);
      expect(scout.strings.endsWith('abc', 'b')).toBe(false);
      expect(scout.strings.endsWith('abcä', 'ä')).toBe(true);
      expect(scout.strings.endsWith('abcä', 'Ä')).toBe(false);
      expect(scout.strings.endsWith('abc', '')).toBe(true);
      expect(scout.strings.endsWith('', '')).toBe(true);
      expect(scout.strings.endsWith()).toBe(false);
      expect(scout.strings.endsWith(undefined, 'hello')).toBe(false);
      expect(scout.strings.endsWith('Der Himmel ist blau!', 'blau')).toBe(false);
      expect(scout.strings.endsWith('Der Himmel ist blau!', 'blau!')).toBe(true);
      expect(scout.strings.endsWith(1234, 4)).toBe(true);
      expect(scout.strings.endsWith(1234, 5)).toBe(false);
      expect(scout.strings.endsWith(1234, '4')).toBe(true);
      expect(scout.strings.endsWith('1234', 4)).toBe(true);
    });

  });

  describe("count", function() {

    it("can count occurrences", function() {
      expect(scout.strings.count()).toBe(0);
      expect(scout.strings.count('hello')).toBe(0);
      expect(scout.strings.count('hello', 'xxx')).toBe(0);
      expect(scout.strings.count('hello', 'l')).toBe(2);
      expect(scout.strings.count('hello', 'll')).toBe(1);
      expect(scout.strings.count('hello', 'H')).toBe(0);
      expect(scout.strings.count('hello', 'h')).toBe(1);
      expect(scout.strings.count('hello! this a test. :-)', '  ')).toBe(0);
      expect(scout.strings.count('hello! this a test. :-)', ' ')).toBe(4);
      expect(scout.strings.count('{"validJson": true, "example": "ümlauts"}', 'ü')).toBe(1);
      expect(scout.strings.count('{"validJson": true, "example": "ümlauts"}', '"')).toBe(6);
      expect(scout.strings.count('the bird is the word', 'rd')).toBe(2);
      expect(scout.strings.count(98138165, 1)).toBe(2);
    });

  });

  describe("encode", function() {

    it("encodes html", function() {
      expect(scout.strings.encode()).toBeUndefined();
      expect(scout.strings.encode('hello')).toBe('hello');
      expect(scout.strings.encode('<b>hello</b>')).toBe('&lt;b&gt;hello&lt;/b&gt;');
      expect(scout.strings.encode(123)).toBe('123');
    });

    it("does not try to encode empty strings", function() {
      scout.strings._encodeElement = null;
      spyOn(document, "createElement").and.callThrough();
      expect(scout.strings.encode('')).toBe('');
      expect(document.createElement).not.toHaveBeenCalled();

      expect(scout.strings.encode('hi')).toBe('hi');
      expect(document.createElement).toHaveBeenCalled();
    });

    it("caches the html element used for encoding", function() {
      scout.strings._encodeElement = null;
      spyOn(document, "createElement").and.callThrough();

      expect(scout.strings.encode('hi')).toBe('hi');
      expect(document.createElement).toHaveBeenCalled();

      document.createElement.calls.reset();
      expect(scout.strings.encode('there')).toBe('there');
      expect(document.createElement).not.toHaveBeenCalled();
    });

  });

  describe("join", function() {

    it("joins strings", function() {
      expect(scout.strings.join()).toBe('');
      expect(scout.strings.join('')).toBe('');
      expect(scout.strings.join(' ')).toBe('');
      expect(scout.strings.join('hello')).toBe('');
      expect(scout.strings.join('hello', undefined)).toBe('');
      expect(scout.strings.join('hello', 'world')).toBe('world');
      expect(scout.strings.join('hello', 'world', '!')).toBe('worldhello!');
      expect(scout.strings.join(' ', 'hello', 'world', '!')).toBe('hello world !');
      expect(scout.strings.join(' ', 'hello', undefined, '!')).toBe('hello !');
      expect(scout.strings.join(' ', 'hello', null, '!')).toBe('hello !');
      expect(scout.strings.join(' ', 'hello', '', '!')).toBe('hello !');
      expect(scout.strings.join('  ', ' ', '', ' ')).toBe('    ');
      expect(scout.strings.join(undefined, 'one', 'two', 'three')).toBe('onetwothree');
      expect(scout.strings.join('', 'one', 'two', 'three')).toBe('onetwothree');
      expect(scout.strings.join(2, 0, 0, 0)).toBe('02020');
    });

    it("join works with array as second parameter", function() {
      expect(scout.strings.join('-', ['hello', 'world'])).toBe('hello-world');
    });

  });

  describe("box", function() {

    it("boxes strings", function() {
      expect(scout.strings.box()).toBe('');
      expect(scout.strings.box('(')).toBe('');
      expect(scout.strings.box('(', undefined)).toBe('');
      expect(scout.strings.box('(', 'x')).toBe('(x');
      expect(scout.strings.box(undefined, 'x')).toBe('x');
      expect(scout.strings.box('(', 'x', ')')).toBe('(x)');
      expect(scout.strings.box('   (', 'x ', ')')).toBe('   (x )');
      expect(scout.strings.box(' (', 'x  ')).toBe(' (x  ');
      expect(scout.strings.box('(', 'x', ')', 'y')).toBe('(x)');
      expect(scout.strings.box('', 'x', '')).toBe('x');
      expect(scout.strings.box('a', ' ', 'b')).toBe('');
      expect(scout.strings.box(0, -3, 7)).toBe('0-37');
    });

  });

  describe("lowercaseFirstLetter", function() {

    it("converts first letter to lowercase", function() {
      expect(scout.strings.lowercaseFirstLetter()).toBe(undefined);
      expect(scout.strings.lowercaseFirstLetter(null)).toBe(null);
      expect(scout.strings.lowercaseFirstLetter(0)).toBe('0');
      expect(scout.strings.lowercaseFirstLetter('0')).toBe('0');
      expect(scout.strings.lowercaseFirstLetter('hans müller')).toBe('hans müller');
      expect(scout.strings.lowercaseFirstLetter('Hans Müller')).toBe('hans Müller');
      expect(scout.strings.lowercaseFirstLetter('ÄÖÜ sind Umlaute')).toBe('äÖÜ sind Umlaute');
    });

  });

  describe("quote", function() {

    it("quotes special characters for regexp", function() {
      expect(scout.strings.quote()).toBe(undefined);
      expect(scout.strings.quote(null)).toBe(null);
      expect(scout.strings.quote('bla')).toBe('bla');
      expect(scout.strings.quote('foo. bar.')).toBe('foo\\. bar\\.');
      expect(scout.strings.quote('ein * am Himmel')).toBe('ein \\* am Himmel');
      expect(scout.strings.quote(123)).toBe('123');
    });

  });

  describe("asString", function() {

    it("converts input to string", function() {
      expect(scout.strings.asString()).toBe(undefined);
      expect(scout.strings.asString(null)).toBe(null);
      expect(scout.strings.asString('bla')).toBe('bla');
      expect(scout.strings.asString(false)).toBe('false');
      expect(scout.strings.asString(-4747)).toBe('-4747');
      expect(scout.strings.asString(0.123)).toBe('0.123');
      expect(scout.strings.asString({})).toBe('[object Object]');
    });

  });

  describe("plainText", function() {
    it("converts html to plain text", function() {
      var htmlText = '<b>hello</b>';
      expect(scout.strings.plainText(htmlText)).toBe('hello');

      htmlText = '<b>hello</b> world! <span class="xyz">Some more html...</span>';
      expect(scout.strings.plainText(htmlText)).toBe('hello world! Some more html...');
    });

    it("does not try to get plaintext of empty strings", function() {
      scout.strings.plainTextElement = null;
      spyOn(document, "createElement").and.callThrough();
      expect(scout.strings.plainText('')).toBe('');
      expect(document.createElement).not.toHaveBeenCalled();
    });

    it("caches the html element used for getting plain text", function() {
      scout.strings.plainTextElement = null;
      spyOn(document, "createElement").and.callThrough();

      expect(scout.strings.plainText('hi')).toBe('hi');
      expect(document.createElement).toHaveBeenCalled();

      document.createElement.calls.reset();
      expect(scout.strings.plainText('there')).toBe('there');
      expect(document.createElement).not.toHaveBeenCalled();
    });

    it("considers upper and lower case tags", function() {
      var htmlText = '<B>hello</B>';
      expect(scout.strings.plainText(htmlText)).toBe('hello');

      htmlText = '<b>hello</b> world! <SPAN class="xyz">Some more html...</SPAN>';
      expect(scout.strings.plainText(htmlText)).toBe('hello world! Some more html...');
    });

    it("converts br, p, div into new lines", function() {
      var htmlText = '<b>1. line</b><br><i>2. line</i>';
      expect(scout.strings.plainText(htmlText)).toBe('1. line\n2. line');

      htmlText = '<b>1. line</b><br/><i>2. line</i>';
      expect(scout.strings.plainText(htmlText)).toBe('1. line\n2. line');

      htmlText = '<p><b>1. line</b></p><i>2. line</i>';
      expect(scout.strings.plainText(htmlText)).toBe('1. line\n2. line');

      htmlText = '<div><b>1. line</b></div><i>2. line</i>';
      expect(scout.strings.plainText(htmlText)).toBe('1. line\n2. line');
    });

    it("converts li, tr into new lines", function() {
      var htmlText = '<ul><li><b>1. line</b></li><li><i>2. line</i></li></ul>';
      expect(scout.strings.plainText(htmlText)).toBe('1. line\n2. line\n');

      htmlText = '<table><tr><td><b>1. line</b></td></tr><tr><td><i>2. line</i></td></tr></table>';
      expect(scout.strings.plainText(htmlText)).toBe('1. line\n2. line\n');
    });

    it("converts td into whitespaces", function() {
      var htmlText = '<table><tr><td>1. cell</td><td>2. cell</td></tr></table>';
      expect(scout.strings.plainText(htmlText)).toBe('1. cell 2. cell\n');

      htmlText =
        '<table>' +
        '  <tr><td>1. cell</td><td>2. cell</td></tr></table>' +
        '  <tr><td>1. cell(r2)</td><td>2. cell(r2)</td></tr>' +
        '</table>';
      expect(scout.strings.plainText(htmlText)).toBe('1. cell 2. cell\n1. cell(r2) 2. cell(r2)\n');
    });

    it("converts &nbsp;, &amp;, &gt;, &lt;", function() {
      var htmlText = '<b>first&nbsp;word</b>&nbsp;next word';
      expect(scout.strings.plainText(htmlText)).toBe('first word next word');

      htmlText = '<b>first&amp;word</b>&amp;next word';
      expect(scout.strings.plainText(htmlText)).toBe('first&word&next word');

      htmlText = '<b>first&gt;word</b>&lt;next word';
      expect(scout.strings.plainText(htmlText)).toBe('first>word<next word');

      htmlText = '<b>first&lt;word</b>&gt;next word';
      expect(scout.strings.plainText(htmlText)).toBe('first<word>next word');
    });

    it("preserves tabs", function() {
      var htmlText = '\t\t';
      expect(scout.strings.plainText(htmlText)).toBe('\t\t');
    });

  });

  describe("insertAt", function() {

    it("can insert strings into other strings", function() {
      expect(scout.strings.insertAt()).toBe(undefined);
      expect(scout.strings.insertAt(null)).toBe(null);
      expect(scout.strings.insertAt('')).toBe('');
      expect(scout.strings.insertAt('Hello')).toBe('Hello');
      expect(scout.strings.insertAt('Hello', '_')).toBe('Hello');
      expect(scout.strings.insertAt('Hello', '_', 0)).toBe('_Hello');
      expect(scout.strings.insertAt('Hello', '_', 3)).toBe('Hel_lo');
      expect(scout.strings.insertAt('Hello', '_', 200)).toBe('Hello_');
      expect(scout.strings.insertAt('Hello', '_', 'bla')).toBe('Hello');
    });

  });

  describe("nvl", function() {

    it("returns an empty string when input is null or undefined", function() {
      expect(scout.strings.nvl(null)).toBe('');
      expect(scout.strings.nvl(undefined)).toBe('');
      expect(scout.strings.nvl('')).toBe('');
      expect(scout.strings.nvl('foo')).toBe('foo');
    });

  });

  describe("countCodePoints", function() {

    it("returns the number of codepoints in a string", function() {
      expect(scout.strings.countCodePoints('')).toBe(0);
      expect(scout.strings.countCodePoints('foo')).toBe(3);
      expect(scout.strings.countCodePoints('\uD83D\uDC4D')).toBe(1); //\uD83D\uDC4D is Unicode Character 'THUMBS UP SIGN' (U+1F44D)
    });

  });

});
