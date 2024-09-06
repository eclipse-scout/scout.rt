/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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
      'Pellentesque eu euismod eros, in ullamcorper erat.\n');
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
  });

  it('converts li, tr into new lines', () => {
    let htmlText = '<ul><li><b>1. line</b></li><li><i>2. line</i></li></ul>';
    expect(encoder.encode(htmlText)).toBe('1. line\n2. line\n');

    htmlText = '<table><tr><td><b>1. line</b></td></tr><tr><td><i>2. line</i></td></tr></table>';
    expect(encoder.encode(htmlText)).toBe('1. line\n2. line\n');
  });

  it('converts td into whitespaces', () => {
    let htmlText = '<table><tr><td>1. cell</td><td>2. cell</td></tr></table>';
    expect(encoder.encode(htmlText)).toBe('1. cell 2. cell\n');

    htmlText =
      '<table>' +
      '  <tr><td>1. cell</td><td>2. cell</td></tr></table>' +
      '  <tr><td>1. cell(r2)</td><td>2. cell(r2)</td></tr>' +
      '</table>';
    expect(encoder.encode(htmlText)).toBe('1. cell 2. cell\n1. cell(r2) 2. cell(r2)\n');
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
    let htmlText = '\t\t';
    expect(encoder.encode(htmlText)).toBe('\t\t');
  });

  it('removes leading and trailing newlines if configured', () => {
    let htmlText = '\n\nHello!\n\n';
    expect(encoder.encode(htmlText, {trim: true})).toBe('Hello!');
  });

  it('leaves multiple newlines alone unless configured', () => {
    let htmlText = 'Hello!\n\n\nI like coding!';
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
      'Man Technologist: Medium-light Skin Tone: \uD83D\uDC68\uD83C\uDFFC\u200D\uD83D\uDCBB\n'
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

    htmlText = '<span\nclass="font-icon xy-icon"></span>\n<span class="text">Text</span>';
    expect(encoder.encode(htmlText)).toBe('\nText');
    expect(encoder.encode(htmlText, {removeFontIcons: true})).toBe('\nText');
  });

});
