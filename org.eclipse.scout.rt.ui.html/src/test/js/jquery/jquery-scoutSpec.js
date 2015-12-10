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
describe('scout-jquery', function() {

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
      expect($e.data('$icon').hasClass('icon')).toBe(true);
      $e.icon(null);
      expect($e.children().length).toBe(0);
      expect($e.data('$icon')).toBeUndefined();

      // Set and remove picture icon
      $e.icon('hello');
      expect($e.children().length).toBe(1);
      expect($e.data('$icon')[0]).toBe($e.children('img')[0]);
      expect($e.data('$icon').hasClass('font-icon')).toBe(false);
      expect($e.data('$icon').hasClass('icon')).toBe(true);
      $e.icon(null);
      expect($e.children().length).toBe(0);
      expect($e.data('$icon')).toBeUndefined();

      // Set font icon, then change to picture icon, then back to font icon
      $e.icon('font:X');
      expect($e.children().length).toBe(1);
      expect($e.data('$icon')[0]).toBe($e.children('span')[0]);
      expect($e.data('$icon').hasClass('font-icon')).toBe(true);
      expect($e.data('$icon').hasClass('icon')).toBe(true);
      $e.icon('hello');
      expect($e.children().length).toBe(1);
      expect($e.data('$icon')[0]).toBe($e.children('img')[0]);
      expect($e.data('$icon').hasClass('font-icon')).toBe(false);
      expect($e.data('$icon').hasClass('icon')).toBe(true);
      $e.icon('font:X');
      expect($e.children().length).toBe(1);
      expect($e.data('$icon')[0]).toBe($e.children('span')[0]);
      expect($e.data('$icon').hasClass('font-icon')).toBe(true);
      expect($e.data('$icon').hasClass('icon')).toBe(true);

      // Reset
      $e.icon();
      expect($e.children().length).toBe(0);
      expect($e.data('$icon')).toBeUndefined();
    });

  });

 });
