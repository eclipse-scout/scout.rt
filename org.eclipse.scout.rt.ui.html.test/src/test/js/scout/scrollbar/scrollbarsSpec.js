/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("scrollbars", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createScrollable() {
    return $('<div>')
      .css('height', '50px')
      .css('width', '200px')
      .css('position', 'absolute')
      .appendTo($('#sandbox'));
  }

  function createContent($parent) {
    return $('<div>')
      .text('element')
      .css('height', '100px')
      .appendTo($parent);
  }

  describe("onScroll", function() {

    it("attaches handler to scrolling parents which execute when scrolling", function() {
      var exec = false;
      var handler = function() {
        exec = true;
      };
      var $container = createScrollable();
      var $content = scout.scrollbars.install($container, {
        parent: new scout.NullWidget(),
        session: session
      });
      var $element = createContent($content);

      scout.scrollbars.onScroll($element, handler);
      $container.scroll();
      expect(exec).toBe(true);
    });

  });

  describe("offScroll", function() {

    it("detaches handler from scrolling parents", function() {
      var exec = false;
      var handler = function() {
        exec = true;
      };
      var $container = createScrollable();
      var $content = scout.scrollbars.install($container, {
        parent: new scout.NullWidget(),
        session: session
      });
      var $element = createContent($content);

      scout.scrollbars.onScroll($element, handler);
      $container.scroll();
      expect(exec).toBe(true);

      exec = false;
      scout.scrollbars.offScroll(handler);
      $container.scroll();
      expect(exec).toBe(false);
    });

  });

  describe("isLocationInView", function() {
    var $scrollable, scrollableBounds, $element;

    beforeEach(function() {
      $scrollable = createScrollable();
      scrollableBounds = scout.graphics.offsetBounds($scrollable);
      $element = $('<div>')
        .css('height', '10px')
        .css('width', '10px')
        .css('position', 'absolute')
        .appendTo($('#sandbox'));
    });

    it("returns true if the given location is inside the given $scrollable", function() {
      $element
        .cssLeft(scrollableBounds.x)
        .cssTop(scrollableBounds.y);
      var bounds = scout.graphics.offsetBounds($element);
      expect(scout.scrollbars.isLocationInView(bounds, $scrollable)).toBe(true);
    });

    it("returns false if x of the given location is outside of the given $scrollable (smaller)", function() {
      $element
        .cssLeft(scrollableBounds.x - 1)
        .cssTop(scrollableBounds.y);
      var bounds = scout.graphics.offsetBounds($element);
      expect(scout.scrollbars.isLocationInView(bounds, $scrollable)).toBe(false);
    });

    it("returns false if y of the given location is outside of the given $scrollable (smaller)", function() {
      $element
        .cssLeft(scrollableBounds.x)
        .cssTop(scrollableBounds.y - 1);
      var bounds = scout.graphics.offsetBounds($element);
      expect(scout.scrollbars.isLocationInView(bounds, $scrollable)).toBe(false);
    });

    it("returns false if x of the given location is outside of the given $scrollable (greater)", function() {
      $element
        .cssLeft(scrollableBounds.x + scrollableBounds.width + 1)
        .cssTop(scrollableBounds.y);
      var bounds = scout.graphics.offsetBounds($element);
      expect(scout.scrollbars.isLocationInView(bounds, $scrollable)).toBe(false);
    });

    it("returns false if y of the given location is outside of the given $scrollable (greater)", function() {
      $element
        .cssLeft(scrollableBounds.x)
        .cssTop(scrollableBounds.y + scrollableBounds.height + 1);
      var bounds = scout.graphics.offsetBounds($element);
      expect(scout.scrollbars.isLocationInView(bounds, $scrollable)).toBe(false);
    });

  });

  describe("render", function() {

    it("ensures parent has position absolute or relative", function() {
      // Create scrollable without explicit position
      var $scrollable = $('<div>')
        .css('height', '50px')
        .css('width', '200px')
        .appendTo($('#sandbox'));
      expect($scrollable.css('position')).toBe('static');

      // Install scrollbars --> position should have been set automatically by Scrollbar._render()
      scout.scrollbars.install($scrollable, {
        parent: new scout.NullWidget(),
        session: session
      });
      expect($scrollable.css('position')).toBe('relative');

      // Clear
      scout.scrollbars.uninstall($scrollable, session);
      $scrollable.remove();

      // ---------------------------

      // Create a new scrollable without explicit position
      $scrollable = $('<div>')
        .css('height', '50px')
        .css('width', '200px')
        .appendTo($('#sandbox'));
      expect($scrollable.css('position')).toBe('static');

      // Detach the scrollable
      $scrollable.detach();
      expect($scrollable.css('position')).toBe('');

      // Install scrollbars into the detached scrollable --> position should not be set yet
      scout.scrollbars.install($scrollable, {
        parent: new scout.NullWidget(),
        session: session
      });
      expect($scrollable.css('position')).toBe('');

      // Simulate "attach" lifecycle of widget
      $scrollable.appendTo($('#sandbox'));
      $scrollable.data('scrollbars').forEach(function(scrollbar) {
        scrollbar.attached = false;
        scrollbar.attach();
      });
      // Position should now have been set automatically by Scrollbar._afterAttach()
      expect($scrollable.css('position')).toBe('relative');
    });

  });

});
