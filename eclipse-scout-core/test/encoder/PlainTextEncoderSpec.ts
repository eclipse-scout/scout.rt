/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {PlainTextEncoder} from '../../src/encoder/PlainTextEncoder';

describe('PlainTextEncoder', () => {

  let encoder = new PlainTextEncoder();

  it('converts HTML to plain text', () => {
    expect(encoder.encode(null)).toBe(null);
    expect(encoder.encode('')).toBe('');

    let htmlText = '<b>hello</b>';
    expect(encoder.encode(htmlText)).toBe('hello');

    htmlText = '<b>hello</b> world! <span class="xyz">Some more html...</span>';
    expect(encoder.encode(htmlText)).toBe('hello world! Some more html...');
  });

  it('considers upper and lower case tags', () => {
    let htmlText = '<B>hello</B>';
    expect(encoder.encode(htmlText)).toBe('hello');

    htmlText = '<b>hello</b> world! <SPAN class="xyz">Some more html...</SPAN>';
    expect(encoder.encode(htmlText)).toBe('hello world! Some more html...');
  });

  it('removes style and script tags', () => {
    let htmlText = '<h1>Lorem ipsum dolor</h1>\n'
      + '<style>\n'
      + 'p {\n'
      + '  color: #26b72b;\n'
      + '}\n'
      + '</style>'
      + '<p style="color: blue">Donec mattis metus lorem. Aenean posuere tincidunt enim.</p>'
      + '<script>alert(\'Hello World!\');</script>'
      + '<p style="color: green">Pellentesque eu euismod eros, '
      + '<script>alert(\'Hello World 2!\');</script><script>alert(\'Hello World 3!\');</script>'
      + '<script type=\'text/javascript\'>\n'
      + '  document.write(123);\n'
      + '</script>'
      + '<style media=\'print\'>\n'
      + 'p {\n'
      + '  color: #26b72b;\n'
      + '}\n'
      + '</style>'
      + 'in ullamcorper erat.</p>';

    expect(encoder.encode(htmlText)).toBe('Lorem ipsum dolor\n' +
      'Donec mattis metus lorem. Aenean posuere tincidunt enim.\n' +
      'Pellentesque eu euismod eros, in ullamcorper erat.');
  });

  it('converts br, p, div into new lines', () => {
    let htmlText = '<b>1. line</b><br><i>2. line</i>';
    expect(encoder.encode(htmlText)).toBe('1. line\n2. line');

    htmlText = '<b>1. line</b><br/><i>2. line</i>';
    expect(encoder.encode(htmlText)).toBe('1. line\n2. line');

    htmlText = '<p><b>1. line</b></p><i>2. line</i>';
    expect(encoder.encode(htmlText)).toBe('1. line\n2. line');

    htmlText = '<div><b>1. line</b></div><i>2. line</i>';
    expect(encoder.encode(htmlText)).toBe('1. line\n2. line');

    htmlText = '<div>a\nb<br>c</div>';
    expect(encoder.encode(htmlText)).toBe('a b\nc');
    expect(encoder.encode(htmlText, {trim: false})).toBe('a b\nc\n');
  });

  it('converts li, tr into new lines', () => {
    let htmlText = '<ul><li><b>1. line</b></li><li><i>2. line</i></li></ul>';
    expect(encoder.encode(htmlText)).toBe('1. line\n2. line');

    htmlText = '<table><tr><td><b>1. line</b></td></tr><tr><td><i>2. line</i></td></tr></table>';
    expect(encoder.encode(htmlText)).toBe('1. line\n2. line');
  });

  it('converts td into whitespaces', () => {
    let htmlText = '<table><tr><td>1. cell</td><td>2. cell</td></tr></table>';
    expect(encoder.encode(htmlText)).toBe('1. cell 2. cell');
    expect(encoder.encode(htmlText, {trim: false})).toBe('1. cell 2. cell\n');

    htmlText =
      '<table>' +
      '  <tr><td>1. cell</td><td>2. cell</td></tr></table>' +
      '  <tr><td>1. cell(r2)</td><td>2. cell(r2)</td></tr>' +
      '</table>';
    expect(encoder.encode(htmlText)).toBe('1. cell 2. cell\n1. cell(r2) 2. cell(r2)');
  });

  it('converts &nbsp;, &amp;, &gt;, &lt;', () => {
    let htmlText = '<b>first&nbsp;word</b>&nbsp;next word';
    expect(encoder.encode(htmlText)).toBe('first word next word');

    htmlText = '<b>first&amp;word</b>&amp;next word';
    expect(encoder.encode(htmlText)).toBe('first&word&next word');

    htmlText = '<b>first&gt;word</b>&lt;next word';
    expect(encoder.encode(htmlText)).toBe('first>word<next word');

    htmlText = '<b>first&lt;word</b>&gt;next word';
    expect(encoder.encode(htmlText)).toBe('first<word>next word');
  });

  it('preserves tabs', () => {
    let htmlText = 'a\t\tb';
    expect(encoder.encode(htmlText)).toBe('a\t\tb');

    htmlText = '\t\t';
    expect(encoder.encode(htmlText)).toBe('');
    expect(encoder.encode(htmlText, {trim: false})).toBe('\t\t');
  });

  it('removes leading and trailing newlines if configured', () => {
    let htmlText = '\n\nHello!\n\n';
    expect(encoder.encode(htmlText, {trim: false})).toBe('Hello!');
    expect(encoder.encode(htmlText, {trim: true})).toBe('Hello!');

    htmlText = '<br><br>Hello!<br><br>';
    expect(encoder.encode(htmlText, {trim: false})).toBe('\n\nHello!\n\n');
    expect(encoder.encode(htmlText, {trim: true})).toBe('Hello!');

    htmlText = '<br><br> Hello! <br><br>';
    expect(encoder.encode(htmlText, {trim: false})).toBe('\n\nHello!\n\n');
    expect(encoder.encode(htmlText, {trim: true})).toBe('Hello!');

    htmlText = '  <br>  <br>  Hello!  <br>  <br>  ';
    expect(encoder.encode(htmlText, {trim: false})).toBe('\n\nHello!\n\n');
    expect(encoder.encode(htmlText, {trim: true})).toBe('Hello!');
  });

  it('leaves multiple newlines alone unless configured', () => {
    let htmlText = 'Hello!<br><br><br>I like coding!';
    expect(encoder.encode(htmlText)).toBe('Hello!\n\n\nI like coding!');
    expect(encoder.encode(htmlText, {compact: false})).toBe('Hello!\n\n\nI like coding!');
    expect(encoder.encode(htmlText, {compact: true})).toBe('Hello!\n\nI like coding!');
  });

  it('converts decimal NCR to Unicode', () => {
    let htmlText = '' +
      '<h1>Emojis</h1>\n' +
      '<p>Face with Tears of Joy Emoji: &#128514;</p>' +
      '<p>Party Popper Emoji: &#127881;</p>' +
      '<p>Man Technologist: Medium-light Skin Tone: &#128104;&#127996;&zwj;&#128187;</p>';
    expect(encoder.encode(htmlText)).toBe('' +
      'Emojis\n' +
      'Face with Tears of Joy Emoji: \uD83D\uDE02\n' +
      'Party Popper Emoji: \uD83C\uDF89\n' +
      'Man Technologist: Medium-light Skin Tone: \uD83D\uDC68\uD83C\uDFFC\u200D\uD83D\uDCBB'
    );
  });

  it('converts hexadecimal NCRs to unicode character', () => {
    // Emojis
    expect(encoder.encode('&#x1f600;')).toBe('\uD83D\uDE00'); // Grinning Face
    expect(encoder.encode('&#x1f60e;')).toBe('\uD83D\uDE0E'); // Smiling Face with Sunglasses

    // Other characters
    expect(encoder.encode('&#39;&#x27;&apos;')).toBe('\u0027\u0027\u0027');
    expect(encoder.encode('&#x68;&#x69;')).toBe('hi');
  });

  it('removes font icons if configured', () => {
    let htmlText = '<span class="table-cell-icon font-icon"></span><span class="text">Text</span>';
    expect(encoder.encode(htmlText)).toBe('Text');
    expect(encoder.encode(htmlText, {removeFontIcons: true})).toBe('Text');

    htmlText = '<span aria-label="Text" class="table-cell-icon font-icon"></span><span class="text">Text</span>';
    expect(encoder.encode(htmlText)).toBe('Text');
    expect(encoder.encode(htmlText, {removeFontIcons: true})).toBe('Text');

    htmlText = '<span class="table-cell-icon font-icon" aria-label="Text"></span><span class="text">Text</span>';
    expect(encoder.encode(htmlText)).toBe('Text');
    expect(encoder.encode(htmlText, {removeFontIcons: true})).toBe('Text');

    htmlText = '<span\nclass="font-icon xy-icon"></span>\n<span class="text">Text</span>';
    expect(encoder.encode(htmlText)).toBe(' Text');
    expect(encoder.encode(htmlText, {removeFontIcons: true})).toBe('Text');
  });

  it('ignores ">" in attribute values', () => {
    let htmlText = '<a href="" rel="noreferrer noopener" title="Donec: > mattis >> metus lorem" style="color:rgb(0, 0, 0);text-decoration:none;">Lorem ipsum dolor</a>';
    expect(encoder.encode(htmlText)).toBe('Lorem ipsum dolor');

    htmlText = '<a href=\'\' rel=\'noreferrer noopener\' title=\'Donec: > mattis >> metus lorem\' style=\'color:rgb(0, 0, 0);text-decoration:none;\'>Lorem ipsum dolor</a>';
    expect(encoder.encode(htmlText)).toBe('Lorem ipsum dolor');
  });

  it('recognizes only valid tag syntax', () => {
    let htmlText = '< a href=""/>';
    expect(encoder.encode(htmlText)).toBe('< a href=""/>');

    htmlText = '< a href="">Lorem ipsum< /a>';
    expect(encoder.encode(htmlText)).toBe('< a href="">Lorem ipsum< /a>');

    htmlText = '<\ta href="">Lorem ipsum<\t/a>';
    expect(encoder.encode(htmlText)).toBe('<\ta href="">Lorem ipsum<\t/a>');

    htmlText = '<>Lorem ipsum<>';
    expect(encoder.encode(htmlText)).toBe('<>Lorem ipsum<>');

    htmlText = '<12 href="">Lorem ipsum</12>';
    expect(encoder.encode(htmlText)).toBe('Lorem ipsum');
  });

  it('removes comments correctly', () => {
    let htmlText = 'a<!-- this is a comment -->b';
    expect(encoder.encode(htmlText)).toBe('ab');

    htmlText = 'a<!-- this is a comment\n -->b';
    expect(encoder.encode(htmlText)).toBe('ab');

    htmlText = 'a<!--this is a comment-->b';
    expect(encoder.encode(htmlText)).toBe('ab');

    htmlText = 'a<!-- this is a comment --!>b';
    expect(encoder.encode(htmlText)).toBe('ab');

    htmlText = 'a<!-- this ->is a --c>omment --!>b';
    expect(encoder.encode(htmlText)).toBe('ab');

    htmlText = 'a<!-- this is a comment --!>b-->';
    expect(encoder.encode(htmlText)).toBe('ab-->');

    htmlText = 'a<!-- this is a comment -->b--!>';
    expect(encoder.encode(htmlText)).toBe('ab--!>');

    htmlText = 'a<!-- this is a comment -->b<!-- and this too-->c';
    expect(encoder.encode(htmlText)).toBe('abc');

    htmlText = 'a<!-- this is a com<!--ment -->b<!-- and this too-->c';
    expect(encoder.encode(htmlText)).toBe('abc');
  });

  it('removes attribute values correctly', () => {
    let htmlText = '';
    expect(encoder.removeAttributeValues(htmlText)).toBe('');

    htmlText = '<span title=\'Some text > and <\\span>\'>test<\\span>';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<span title=\'\'>test<\\span>');

    htmlText = '<span title="Some text > and <\\span>">test<\\span>';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<span title="">test<\\span>');

    htmlText = '<span>test<\\span title=\'attribute is invalid in end tag, but we delete it as well. > and <\\span>\'>';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<span>test<\\span title=\'\'>');

    htmlText = '<span>test<\\span title="attribute is invalid in end tag, but we delete it as well. > and <\\span>">';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<span>test<\\span title="">');

    htmlText = '<abc attr= "someText>123';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<abc attr= "');

    htmlText = '<abc attr= "someText>123"';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<abc attr= ""');

    htmlText = '<12 attr="someText>123"';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<12 attr=""');

    htmlText = '<12 "someText>123"';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<12 ""');

    htmlText = '<12 "someText\'>123"';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<12 ""');

    htmlText = '<12 "someText<\'>123"';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<12 ""');

    htmlText = '< abc attr=\'someText\'>';
    expect(encoder.removeAttributeValues(htmlText)).toBe('< abc attr=\'someText\'>');

    htmlText = '<\tabc attr=\'someText\'>';
    expect(encoder.removeAttributeValues(htmlText)).toBe('<\tabc attr=\'someText\'>');
  });

  it('trims lines, but preserves other white-space', () => {
    expect(encoder.encode('hello')).toBe('hello');
    expect(encoder.encode('one\ntwo')).toBe('one two');
    expect(encoder.encode('one\r\ntwo')).toBe('one two');
    expect(encoder.encode('one\rtwo')).toBe('onetwo');
    expect(encoder.encode('one<br/>\r\ntwo')).toBe('one\ntwo');
    expect(encoder.encode('one   two')).toBe('one two');
    expect(encoder.encode('one&nbsp;&nbsp; two')).toBe('one\u00a0\u00a0 two');
    expect(encoder.encode('one &#9; two')).toBe('one \t two');
    expect(encoder.encode(' one <br>\n two  <br>\n   three ')).toBe('one\ntwo\nthree');
    expect(encoder.encode('a<br>\n&nbsp;&nbsp;b<br>\n&nbsp;&nbsp;&nbsp;&nbsp;c')).toBe('a\n\u00A0\u00A0b\n\u00A0\u00A0\u00A0\u00A0c');
  });

  it('decodes special characters', () => {
    expect(encoder.encode('&amp;amp;')).toBe('&amp;');
    expect(encoder.encode('&amp;&amp;amp;amp;')).toBe('&&amp;amp;');
    expect(encoder.encode('Hell&ouml;!')).toBe('Hellö!');
    expect(encoder.encode('a&lt;br&gt;b')).toBe('a<br>b');
  });

  // -----------

  // Tests to check that PlainTextEncoder.ts and HtmlHelper.java behave as similarly as possible.
  describe('HtmlHelperTest', () => {

    it('testToPlainText()', () => {
      // Note: Some of the expected results are not really what the user would expect,
      // but what the toPlainText() method currently returns. They are marked with "[?]"
      // below. Sometimes in the future, it should be considered to change them.
      //
      // Tests marked with [?!] have differently in the browser!

      expect(encoder.encode(null)).toBe(null);
      expect(encoder.encode('')).toBe('');

      // Text only
      expect(encoder.encode('hello')).toBe('hello');
      expect(encoder.encode('one\ntwo')).toBe('one two');
      expect(encoder.encode('one\r\ntwo')).toBe('one two');
      expect(encoder.encode('one\rtwo')).toBe('onetwo');
      expect(encoder.encode('hell&lt;')).toBe('hell<');
      expect(encoder.encode('hell&ouml;')).toBe('hellö');
      expect(encoder.encode('one&#9;two')).toBe('one\ttwo');
      expect(encoder.encode('one &#9; two')).toBe('one \t two');
      expect(encoder.encode('one   two')).toBe('one two');
      expect(encoder.encode('one&nbsp;&nbsp; two')).toBe('one\u00A0\u00A0 two');
      expect(encoder.encode('one&#160;&#xa0;&#Xa0;&#xA0;two')).toBe('one\u00A0\u00A0\u00A0\u00A0two'); // HTML5 spec allows for mixed case hex values.
      expect(encoder.encode('one&#x9;&#X9;two')).toBe('one\t\ttwo'); // HTML5 spec allows for mixed case hex values.
      expect(encoder.encode('<div class="rte-line">Unter<u>rasch</u>u<span class="rte-highlight" style="background-color: rgb(255, 219, 157)">ngs</span>feier<br></div>')).toBe('Unterraschungsfeier'); // Formating tags within a single word.
      expect(encoder.encode('<h1>Header 1</h1><h1>Header 2</h1>')).toBe('Header 1\nHeader 2'); // Headers
      expect(encoder.encode('<ul><li>List 1</li><li>List 2</li></ul><table><tr><th>TableHeader 1</th><th>TableHeader 2</th></tr><tr><td>Data 1</td><td>Data 2</td></tr></table>'))
        .toBe('List 1\nList 2\nTableHeader 1 TableHeader 2\nData 1 Data 2'); // List and tables
      expect(encoder.encode('<a href="" rel="noreferrer noopener" title="Donec: > mattis >> metus lorem" style="color:rgb(0, 0, 0);text-decoration:none;">Lorem ipsum dolor</a>')).toBe('Lorem ipsum dolor');
      expect(encoder.encode('<a href=\'\' rel=\'noreferrer noopener\' title=\'Donec: > mattis >> metus lorem\' style=\'color:rgb(0, 0, 0);text-decoration:none;\'>Lorem ipsum dolor</a>')).toBe('Lorem ipsum dolor');
      expect(encoder.encode('1 < 2<a href=\'\'/>')).toBe('1 < 2');
      expect(encoder.encode('< a href=""/>')).toBe('< a href=""/>');
      expect(encoder.encode('< a href="">Lorem ipsum< /a>')).toBe('< a href="">Lorem ipsum< /a>');
      expect(encoder.encode('<\ta href="">Lorem ipsum<\t/a>')).toBe('<\ta href="">Lorem ipsum<\t/a>');
      expect(encoder.encode('<>Lorem ipsum</>')).toBe('<>Lorem ipsum'); // [?] same behavior as browser (chrome)
      expect(encoder.encode('<\\>Lorem')).toBe('Lorem'); // [?] browser (chrome) DOES display <\>
      expect(encoder.encode('<12 href="">Lorem ipsum</12>')).toBe('Lorem ipsum'); // [?] browser does not display </12>, but DOES display <12> (and also <12 href="">)

      // Simple documents
      expect(encoder.encode('<html>')).toBe('');
      expect(encoder.encode('<html></html>')).toBe('');
      expect(encoder.encode('<html><head></html>')).toBe('');
      expect(encoder.encode('<html><head>one</html>')).toBe('one');
      expect(encoder.encode('<html><head>one & two</html>')).toBe('one & two');
      expect(encoder.encode('<html><head>one &amp; two</html>')).toBe('one & two');
      expect(encoder.encode('<html><head>one &amp; two</head><body>three</html>')).toBe('one & two\nthree'); // [?] invalid <body>, has no end tag
      expect(encoder.encode('&amp;amp;')).toBe('&amp;');
      expect(encoder.encode('&<span>amp;</span>')).toBe('&'); // [?] tags are removed before entities are decoded (cannot be handled correctly without a proper parser)
      expect(encoder.encode('&amp;auml;')).toBe('&auml;');
      expect(encoder.encode('&amp;#x42;')).toBe('&#x42;');
      expect(encoder.encode('<html><head>one &amp; two</head><body>three</body></html>')).toBe('one & two\nthree'); // [?!] does not handle <head> differently than any other tag
      expect(encoder.encode('<html><body><div class="rte-line">Unter<u>rasch</u>u<span class="rte-highlight" style="background-color: rgb(255, 219, 157)">ngs</span>feier<br></div></body></html>')).toBe('Unterraschungsfeier');
      expect(encoder.encode('<p>Z1</p><span></span><p>Z2</p>')).toBe('Z1\nZ2');
      expect(encoder.encode('<html><body><div><div><p>Guten Tag<o:p></o:p></p></div><div><p><o:p>&nbsp;</o:p></span></p></div><div><p><span>Zeile 2<o:p></o:p></span></p></div></div></body></html>')).toBe('Guten Tag\n\n\u00A0\n\nZeile 2');
      expect(encoder.encode('&#8217;')).toBe('’');
      expect(encoder.encode('&#43;')).toBe('+');
      expect(encoder.encode('&#x2B;')).toBe('+');
      expect(encoder.encode('&#X2B;')).toBe('+');
      expect(encoder.encode('&#X2Bs;')).toBe('+s;'); // [?!] browser seems to be lenient

      // Line breaks
      expect(encoder.encode('a<br>b')).toBe('a\nb');
      expect(encoder.encode('a <br/> b')).toBe('a\nb');
      expect(encoder.encode('a    <br/> b')).toBe('a\nb');
      expect(encoder.encode('a&nbsp;<br/> b')).toBe('a\u00A0\nb');
      expect(encoder.encode('a  &nbsp;  <br/>  b  ')).toBe('a \u00A0\nb');
      expect(encoder.encode('a<br>&nbsp;&nbsp;b<br>&nbsp;&nbsp;&nbsp;&nbsp;c')).toBe('a\n\u00A0\u00A0b\n\u00A0\u00A0\u00A0\u00A0c');
      expect(encoder.encode('<br/>line')).toBe('line');
      expect(encoder.encode('<br/>line', {trim: false})).toBe('\nline');
      expect(encoder.encode('<p>line1<br>\nx</p><p>line2</p>')).toBe('line1\nx\nline2');
      expect(encoder.encode('<div>line1\nx</div><div>line2</div>')).toBe('line1 x\nline2');
      expect(encoder.encode('<div>line1<br/></div><div>line2<br/></div>')).toBe('line1\nline2');
      expect(encoder.encode('<div>a\nb<br>c</div>')).toBe('a b\nc');
      expect(encoder.encode('<div>a\nb<br>c</div>', {trim: false})).toBe('a b\nc\n');

      // Tables
      expect(encoder.encode('<table><tr><td>one</td><td>two</td></tr><tr><td>three</td><td>four</td></tr></table>')).toBe('one two\nthree four');
      expect(encoder.encode('<table><tr><td><b>1. line</b></td></tr><tr><td><i>2. line</i></td></tr></table>')).toBe('1. line\n2. line');
      expect(encoder.encode('<table><tr><td><b>1. line</b></td></tr><tr><td><i>2. line</i></td></tr></table>', {trim: false})).toBe('1. line\n2. line\n');

      // Styles and Scripts
      expect(
        encoder.encode(
          '<h1>Lorem ipsum dolor</h1>\n'
          + '<style>\n'
          + 'p {\n'
          + '  color: #26b72b;\n'
          + '}\n'
          + '</style>'
          + '<p style="color: blue">Donec mattis metus lorem. Aenean posuere tincidunt enim.</p>\n'
          + '<script>alert(\'Hello World!\');</script>'
          + '<p style="color: green">Pellentesque eu euismod eros, '
          + '<script>alert(\'Hello World 2!\');</script><script>alert(\'Hello World 3!\');</script>'
          + '<script type=\'text/javascript\'>\n'
          + '  document.write(123);\n'
          + '</script>'
          + '<style type=\'text/css\'>\n'
          + 'p {\n'
          + '  color: #26b72b;\n'
          + '}\n'
          + '</style>'
          + 'in ullamcorper erat.</p>')
      ).toBe(
        'Lorem ipsum dolor\n'
        + 'Donec mattis metus lorem. Aenean posuere tincidunt enim.\n'
        + 'Pellentesque eu euismod eros, in ullamcorper erat.'
      );

      // Emojis
      expect(encoder.encode(''
        + '<h1>Emojis</h1>\n'
        + '<p>Face with Tears of Joy Emoji: &#128514;</p>\n'
        + '<p>Party Popper Emoji: &#127881;</p>\n'
        + '<p>Man Technologist: Medium-light Skin Tone: &#128104;&#127996;&zwj;&#128187;</p>')
      ).toBe(''
        + 'Emojis\n'
        + 'Face with Tears of Joy Emoji: \uD83D\uDE02\n'
        + 'Party Popper Emoji: \uD83C\uDF89\n'
        + 'Man Technologist: Medium-light Skin Tone: \uD83D\uDC68\uD83C\uDFFC\u200D\uD83D\uDCBB'
      );

      // Comments
      expect(encoder.encode('a<!-- this is a comment -->b')).toBe('ab');
      expect(encoder.encode('a<!-- this is a comment\n -->b')).toBe('ab');
      expect(encoder.encode('a<!--this is a comment-->b')).toBe('ab');
      expect(encoder.encode('a<!-- this is a comment --!>b')).toBe('ab');
      expect(encoder.encode('a<!-- this ->is a --c>omment --!>b')).toBe('ab');
      expect(encoder.encode('a<!-- this is a comment --!>b-->')).toBe('ab-->');
      expect(encoder.encode('a<!-- this is a comment -->b--!>')).toBe('ab--!>');
      expect(encoder.encode('a<!-- this is a comment -->b<!-- and this too-->c')).toBe('abc');
      expect(encoder.encode('a<!-- this is a com<!--ment -->b<!-- and this too-->c')).toBe('abc');
    });

    it('testRemoveAttributeValues()', () => {
      expect(encoder.removeAttributeValues('')).toBe('');
      expect(encoder.removeAttributeValues('<span title=\'Some text > and <\\span>\'>test<\\span>')).toBe('<span title=\'\'>test<\\span>');
      expect(encoder.removeAttributeValues('<span title="Some text > and <\\span>">test<\\span>')).toBe('<span title="">test<\\span>');
      expect(encoder.removeAttributeValues('<span>test<\\span title=\'attribute is invalid in end tag, but we delete it as well. > and <\\span>\'>')).toBe('<span>test<\\span title=\'\'>');
      expect(encoder.removeAttributeValues('<span>test<\\span title="attribute is invalid in end tag, but we delete it as well. > and <\\span>">')).toBe('<span>test<\\span title="">');
      expect(encoder.removeAttributeValues('<abc attr= "someText>123')).toBe('<abc attr= "');
      expect(encoder.removeAttributeValues('<abc attr= "someText>123"')).toBe('<abc attr= ""');
      expect(encoder.removeAttributeValues('<12 attr="someText>123"')).toBe('<12 attr=""');
      expect(encoder.removeAttributeValues('<12 "someText>123"')).toBe('<12 ""');
      expect(encoder.removeAttributeValues('<12 "someText\'>123"')).toBe('<12 ""');
      expect(encoder.removeAttributeValues('<12 "someText<\'>123"')).toBe('<12 ""');
      expect(encoder.removeAttributeValues('< abc attr=\'someText\'>')).toBe('< abc attr=\'someText\'>');
      expect(encoder.removeAttributeValues('<\tabc attr=\'someText\'>')).toBe('<\tabc attr=\'someText\'>');
    });

    it('testHexEncodedCharacters()', () => {
      // Emojis
      expect(encoder.encode('&#x1f600;')).toBe('\uD83D\uDE00'); // Grinning Face
      expect(encoder.encode('&#x1f60e;')).toBe('\uD83D\uDE0E'); // Smiling Face with Sunglasses

      // Other characters
      expect(encoder.encode('&#39;&#x27;&apos;')).toBe('\u0027\u0027\u0027');
      expect(encoder.encode('&#x68;&#x69;')).toBe('hi');
    });
  });
});
