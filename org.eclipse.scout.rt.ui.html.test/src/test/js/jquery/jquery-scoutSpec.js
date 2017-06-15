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
describe('jquery-scout', function() {

  var $e;

  /**
   * We must append $e to the DOM, because otherwise test would fail in some browsers (Chrome, PhantomJS).
   */
  beforeEach(function() {
    setFixtures(sandbox());
    $e = $('<div>');
    $e.appendTo($('#sandbox'));
  });

  describe('isEnabled', function() {

    it('is only false when class disabled is set', function() {
      expect($e.isEnabled()).toBe(true);
      $e.addClass('disabled');
      expect($e.isEnabled()).toBe(false);
      $e.removeClass('disabled');
      expect($e.isEnabled()).toBe(true);
    });

  });

  describe('isVisible', function() {

    it('returns true when display != none and visiblity != hidden', function() {
      expect($e.isVisible()).toBe(true);
      $e.css('display', 'none');
      expect($e.isVisible()).toBe(false);
      $e.css('display', '');
      expect($e.isVisible()).toBe(true);
      $e.css('visibility', 'hidden');
      expect($e.isVisible()).toBe(false);
      $e.css('visibility', '');
      expect($e.isVisible()).toBe(true);
    });

  });

  describe('setEnabled', function() {

    it('DIV does not have disabled attribute', function() {
      $e.setEnabled(false);
      expect($e.hasClass('disabled')).toBe(true);
      expect($e.attr('disabled')).toBeUndefined();
      $e.setEnabled(true);
      expect($e.hasClass('disabled')).toBe(false);
      expect($e.attr('disabled')).toBeUndefined();
    });

    it('INPUT must have disabled attribute', function() {
      $e = $('<input>');
      $e.setEnabled(false);
      expect($e.hasClass('disabled')).toBe(true);
      expect($e.attr('disabled')).toBe('disabled');
      $e.setEnabled(true);
      expect($e.hasClass('disabled')).toBe(false);
      expect($e.attr('disabled')).toBeUndefined();
    });

  });

  describe('toggleAttr', function() {

    it('toggles attribute', function() {
      expect($e.attr('test')).toBeUndefined();
      $e.toggleAttr('test');
      expect($e.attr('test')).toBe('test');
      $e.toggleAttr('test');
      expect($e.attr('test')).toBeUndefined();
      $e.toggleAttr('test', false);
      expect($e.attr('test')).toBeUndefined();
      $e.toggleAttr('test', true);
      expect($e.attr('test')).toBe('test');
      $e.toggleAttr('test', true);
      expect($e.attr('test')).toBe('test');
      $e.toggleAttr('test');
      expect($e.attr('test')).toBeUndefined();
      $e.toggleAttr('test', true, 1);
      expect($e.attr('test')).toBe('1');
      $e.toggleAttr('test', true, 'one');
      expect($e.attr('test')).toBe('one');
      $e.toggleAttr('test', false, 'bla');
      expect($e.attr('test')).toBeUndefined();
    });

  });

  describe('icon', function() {

    it('sets and removes icons', function() {
      // Set and remove font icon
      $e.icon();
      expect($e.children().length).toBe(0);
      expect($e.data('$icon')).toBeUndefined();
      $e.icon('font:X');
      expect($e.children().length).toBe(1);
      expect($e.data('$icon')[0]).toBe($e.children('span')[0]);
      expect($e.data('$icon').hasClass('font-icon')).toBe(true);
      expect($e.data('$icon').hasClass('image-icon')).toBe(false);
      expect($e.data('$icon').hasClass('icon')).toBe(true);
      $e.icon(null);
      expect($e.children().length).toBe(0);
      expect($e.data('$icon')).toBeUndefined();

      // Set and remove picture icon
      $e.icon('hello');
      expect($e.children().length).toBe(1);
      expect($e.data('$icon')[0]).toBe($e.children('img')[0]);
      expect($e.data('$icon').hasClass('font-icon')).toBe(false);
      expect($e.data('$icon').hasClass('image-icon')).toBe(true);
      expect($e.data('$icon').hasClass('icon')).toBe(true);
      $e.icon(null);
      expect($e.children().length).toBe(0);
      expect($e.data('$icon')).toBeUndefined();

      // Set font icon, then change to picture icon, then back to font icon
      $e.icon('font:X');
      expect($e.children().length).toBe(1);
      expect($e.data('$icon')[0]).toBe($e.children('span')[0]);
      expect($e.data('$icon').hasClass('font-icon')).toBe(true);
      expect($e.data('$icon').hasClass('image-icon')).toBe(false);
      expect($e.data('$icon').hasClass('icon')).toBe(true);
      $e.icon('hello');
      expect($e.children().length).toBe(1);
      expect($e.data('$icon')[0]).toBe($e.children('img')[0]);
      expect($e.data('$icon').hasClass('font-icon')).toBe(false);
      expect($e.data('$icon').hasClass('image-icon')).toBe(true);
      expect($e.data('$icon').hasClass('icon')).toBe(true);
      $e.icon('font:X');
      expect($e.children().length).toBe(1);
      expect($e.data('$icon')[0]).toBe($e.children('span')[0]);
      expect($e.data('$icon').hasClass('font-icon')).toBe(true);
      expect($e.data('$icon').hasClass('image-icon')).toBe(false);
      expect($e.data('$icon').hasClass('icon')).toBe(true);

      // Reset
      $e.icon();
      expect($e.children().length).toBe(0);
      expect($e.data('$icon')).toBeUndefined();
    });

  });

  describe('textOrNbsp', function() {

    it('sets text or nbsp', function() {
      $e.textOrNbsp();
      expect($e.html()).toBe('&nbsp;');
      $e.empty();
      expect($e.html()).toBe('');
      $e.textOrNbsp('');
      expect($e.html()).toBe('&nbsp;');
      $e.textOrNbsp('hello');
      expect($e.html()).toBe('hello');
      $e.textOrNbsp(' ');
      expect($e.html()).toBe('&nbsp;');
      $e.textOrNbsp('hello  <b>world</b>');
      expect($e.html()).toBe('hello  &lt;b&gt;world&lt;/b&gt;');
      $e.textOrNbsp('\n \t      ');
      expect($e.html()).toBe('&nbsp;');
      $e.textOrNbsp(' hello\nworld');
      expect($e.html()).toBe(' hello\nworld');
      $e.textOrNbsp(null);
      expect($e.html()).toBe('&nbsp;');
      $e.textOrNbsp('Company & Co.');
      expect($e.html()).toBe('Company &amp; Co.');

      // Empty class
      $e.textOrNbsp();
      expect($e.html()).toBe('&nbsp;');
      expect($e.hasClass('empty')).toBe(false);
      $e.textOrNbsp('bla', 'EMPTY');
      expect($e.html()).toBe('bla');
      expect($e.hasClass('empty')).toBe(false);
      expect($e.hasClass('EMPTY')).toBe(false);
      $e.textOrNbsp('', 'EMPTY');
      expect($e.html()).toBe('&nbsp;');
      expect($e.hasClass('empty')).toBe(false);
      expect($e.hasClass('EMPTY')).toBe(true);
    });

  });

  describe('htmlOrNbsp', function() {

    it('sets html or nbsp', function() {
      $e.htmlOrNbsp();
      expect($e.html()).toBe('&nbsp;');
      $e.empty();
      expect($e.html()).toBe('');
      $e.htmlOrNbsp('');
      expect($e.html()).toBe('&nbsp;');
      $e.htmlOrNbsp('hello');
      expect($e.html()).toBe('hello');
      $e.htmlOrNbsp(' ');
      expect($e.html()).toBe('&nbsp;');
      $e.htmlOrNbsp('hello  <b>world</b>');
      expect($e.html()).toBe('hello  <b>world</b>');
      $e.htmlOrNbsp('\n \t      ');
      expect($e.html()).toBe('&nbsp;');
      $e.htmlOrNbsp(' hello\nworld');
      expect($e.html()).toBe(' hello\nworld');
      $e.htmlOrNbsp(scout.strings.nl2br(' hello\nworld'));
      expect($e.html()).toBe(' hello<br>world');
      $e.htmlOrNbsp(null);
      expect($e.html()).toBe('&nbsp;');
      $e.htmlOrNbsp('Company & Co.');
      expect($e.html()).toBe('Company &amp; Co.');

      // Empty class
      $e.htmlOrNbsp();
      expect($e.html()).toBe('&nbsp;');
      expect($e.hasClass('empty')).toBe(false);
      $e.htmlOrNbsp('bla', 'EMPTY');
      expect($e.html()).toBe('bla');
      expect($e.hasClass('empty')).toBe(false);
      expect($e.hasClass('EMPTY')).toBe(false);
      $e.htmlOrNbsp('', 'EMPTY');
      expect($e.html()).toBe('&nbsp;');
      expect($e.hasClass('empty')).toBe(false);
      expect($e.hasClass('EMPTY')).toBe(true);
    });

  });

  describe('nvl', function() {

    it('can return alternative element', function() {
      var $f = $('<div>');
      $f.appendTo($('#sandbox'));
      var $g = $('<div>'); // not in DOM

      expect($e.nvl()).toBe($e);
      expect($e.nvl($f)).toBe($e);
      expect($e.nvl($g)).toBe($e);
      expect($g.nvl($e)).toBe($g);

      expect($('.does-not-exist').nvl($e)).toBe($e);
      expect($('.does-not-exist').nvl($g)).toBe($g);

      var $result = $('.does').nvl($('.not')).nvl(null).nvl($('.exist')).nvl();
      expect($result instanceof $).toBe(true);
      expect($result.length).toBe(0);
      $result = $result.nvl($result).nvl($f).nvl(null);
      expect($result instanceof $).toBe(true);
      expect($result.length).toBe(1);
      expect($result).toBe($f);
    });

  });

  describe('elementFromPoint', function() {

    beforeEach(function() {
      $('<style>.invisible {visibility: hidden !important;}</style>').appendTo($('#sandbox'));
    });

    it('returns the element from point but only if it is a child', function() {
      var $container = $('<div>')
        .attr('style', 'position: absolute; left: 10px; top: 10px; width: 100px; height: 100px')
        .appendTo($('#sandbox'));
      var $elem = $('<div>')
        .attr('style', 'position: absolute; left: 10px; top: 10px; width: 50px; height: 50px')
        .appendTo($container);
      expect($container.elementFromPoint(20, 20)[0]).toBe($elem[0]);
      expect($container.elementFromPoint(19, 19)[0]).toBe($container[0]);
      expect($container.elementFromPoint(9, 9).length).toBe(0);

      $elem.appendTo($('#sandbox'));
      expect($container.elementFromPoint(10, 10)[0]).toBe($container[0]);
      expect($elem.elementFromPoint(10, 10)[0]).toBe($elem[0]);
      expect($container.elementFromPoint(9, 9).length).toBe(0);
    });

    it('considers the selector', function() {
      var $container = $('<div>')
        .addClass('outer')
        .attr('style', 'position: absolute; left: 10px; top: 10px; width: 100px; height: 100px')
        .appendTo($('#sandbox'));
      var $elem = $('<div>')
        .addClass('inner')
        .attr('style', 'position: absolute; left: 10px; top: 10px; width: 50px; height: 50px')
        .appendTo($container);
      expect($container.elementFromPoint(20, 20, '.inner')[0]).toBe($elem[0]);
      expect($container.elementFromPoint(20, 20, '.outer')[0]).toBe($container[0]);
      expect($container.elementFromPoint(20, 20, '.asdf').length).toBe(0);
    });

    it('returns the document element if no element matches and document is used as container', function() {
      var $container = $('<div>')
        .attr('style', 'position: absolute; left: 10px; top: 10px; width: 100px; height: 100px')
        .appendTo($('#sandbox'));
      var $elem = $('<div>')
        .attr('style', 'position: absolute; left: 10px; top: 10px; width: 50px; height: 50px')
        .appendTo($container);
      expect($(document).elementFromPoint(20, 20)[0]).toBe($elem[0]);
      expect($(document).elementFromPoint(19, 19)[0]).toBe($container[0]);
      $('body').addClass('invisible');
      // Probably very unlikely that someone uses this method on an empty document, but who knows...
      expect($(document).elementFromPoint(9, 9)[0]).toBe(document.documentElement);
      $('body').removeClass('invisible');
    });

  });

  describe('cssPxValue', function() {

    it('is behaves differently with different types of arguments', function() {
      var $test = $('#sandbox').appendDiv('test');

      expect($test.cssPxValue('width') > 0).toBe(true);
      $test.css('width', 'auto');
      expect($test.cssPxValue('width') > 0).toBe(true); // cssPxValue always returns a number

      expect($test.cssPxValue('width', 11)).toBe($test); // setter
      expect($test.cssPxValue('width')).toBe(11);
      expect($test.cssPxValue('width', '')).toBe($test); // setter
      expect($test.cssPxValue('width') > 0).toBe(true);
      expect($test.cssPxValue('width', '12px')).toBe($test); // setter
      expect($test.cssPxValue('width')).toBe(12);
      expect($test.cssPxValue('width', null)).toBe($test); // setter
      expect($test.cssPxValue('width') > 0).toBe(true);
      expect($test.cssPxValue('width', '12px')).toBe($test); // setter
      expect($test.cssPxValue('width')).toBe(12);
      expect($test.cssPxValue('width', 'calc(100% - 10px)')).toBe($test); // setter
      expect($test.cssPxValue('width') > 0).toBe(true);
    });

  });

});
