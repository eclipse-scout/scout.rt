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

import PlainTextEncoder from '../../src/encoder/PlainTextEncoder';

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

  it('removes font icons if configured', () => {
    let htmlText = '<span class="table-cell-icon font-icon"></span><span class="text">Text</span>';
    expect(encoder.encode(htmlText)).toBe('Text');
    expect(encoder.encode(htmlText, {removeFontIcons: true})).toBe('Text');

    htmlText = '<span\nclass="font-icon xy-icon"></span>\n<span class="text">Text</span>';
    expect(encoder.encode(htmlText)).toBe('\nText');
    expect(encoder.encode(htmlText, {removeFontIcons: true})).toBe('\nText');
  });

});
