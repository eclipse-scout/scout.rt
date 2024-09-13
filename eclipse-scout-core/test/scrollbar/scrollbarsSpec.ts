/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {graphics, NullWidget, Rectangle, scrollbars} from '../../src/index';

describe('scrollbars', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createScrollable(): JQuery {
    return $('<div>')
      .css('height', '50px')
      .css('width', '200px')
      .css('position', 'absolute')
      .appendTo($('#sandbox'));
  }

  function createContent($parent: JQuery): JQuery {
    return $('<div>')
      .text('element')
      .css('height', '100px')
      .appendTo($parent);
  }

  describe('onScroll', () => {

    it('attaches handler to scrolling parents which execute when scrolling', () => {
      let exec = false;
      let handler = () => {
        exec = true;
      };
      let $container = createScrollable();
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session
      });
      let $element = createContent($container);

      scrollbars.onScroll($element, handler);
      $container.scroll();
      expect(exec).toBe(true);
      scrollbars.uninstall($container, session);
    });
  });

  describe('offScroll', () => {

    it('detaches handler from scrolling parents', () => {
      let exec = false;
      let handler = () => {
        exec = true;
      };
      let $container = createScrollable();
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session
      });
      let $element = createContent($container);

      scrollbars.onScroll($element, handler);
      $container.scroll();
      expect(exec).toBe(true);

      exec = false;
      scrollbars.offScroll(handler);
      $container.scroll();
      expect(exec).toBe(false);
      scrollbars.uninstall($container, session);
    });

  });

  describe('isLocationInView', () => {
    let $scrollable, scrollableBounds, $element;

    beforeEach(() => {
      $scrollable = createScrollable();
      scrollableBounds = graphics.offsetBounds($scrollable);
      $element = $('<div>')
        .css('height', '10px')
        .css('width', '10px')
        .css('position', 'absolute')
        .appendTo($('#sandbox'));
    });

    it('returns true if the given location is inside the given $scrollable', () => {
      $element
        .cssLeft(scrollableBounds.x)
        .cssTop(scrollableBounds.y);
      let bounds = graphics.offsetBounds($element);
      expect(scrollbars.isLocationInView(bounds, $scrollable)).toBe(true);
    });

    it('returns false if x of the given location is outside of the given $scrollable (smaller)', () => {
      $element
        .cssLeft(scrollableBounds.x - 1)
        .cssTop(scrollableBounds.y);
      let bounds = graphics.offsetBounds($element);
      expect(scrollbars.isLocationInView(bounds, $scrollable)).toBe(false);
    });

    it('returns false if y of the given location is outside of the given $scrollable (smaller)', () => {
      $element
        .cssLeft(scrollableBounds.x)
        .cssTop(scrollableBounds.y - 1);
      let bounds = graphics.offsetBounds($element);
      expect(scrollbars.isLocationInView(bounds, $scrollable)).toBe(false);
    });

    it('returns false if x of the given location is outside of the given $scrollable (greater)', () => {
      $element
        .cssLeft(scrollableBounds.x + scrollableBounds.width + 1)
        .cssTop(scrollableBounds.y);
      let bounds = graphics.offsetBounds($element);
      expect(scrollbars.isLocationInView(bounds, $scrollable)).toBe(false);
    });

    it('returns false if y of the given location is outside of the given $scrollable (greater)', () => {
      $element
        .cssLeft(scrollableBounds.x)
        .cssTop(scrollableBounds.y + scrollableBounds.height + 1);
      let bounds = graphics.offsetBounds($element);
      expect(scrollbars.isLocationInView(bounds, $scrollable)).toBe(false);
    });

  });

  describe('intersectViewport', () => {
    let $scrollable: JQuery, $scrollable2: JQuery, $scrollables: JQuery, scrollableBounds: Rectangle, scrollableBounds2: Rectangle, $element: JQuery;

    beforeEach(() => {
      $scrollable = createScrollable();
      scrollableBounds = graphics.offsetBounds($scrollable);
      $scrollable2 = $scrollable.appendDiv()
        .css('height', '30px')
        .css('width', '100px')
        .css('position', 'absolute');
      scrollableBounds2 = graphics.offsetBounds($scrollable2);
      $element = $('<div>')
        .css('height', '10px')
        .css('width', '10px')
        .css('position', 'absolute')
        .appendTo($('#sandbox'));
      $scrollables = $();
      $scrollables.push($scrollable, $scrollable2);
    });

    it('returns the intersection of the rectangle and all scrollables', () => {
      // Inside both scrollables
      $element
        .cssLeft(scrollableBounds.x)
        .cssTop(scrollableBounds.y);
      let bounds = graphics.offsetBounds($element);
      expect(scrollbars.intersectViewport(bounds, $scrollables)).toEqual(bounds);

      // Outside first scrollable
      $element
        .cssLeft(scrollableBounds2.x)
        .cssTop(scrollableBounds2.bottom());
      bounds = graphics.offsetBounds($element);
      expect(scrollbars.intersectViewport(bounds, $scrollables)).toEqual(new Rectangle());

      // Overlapping into scrollable 2
      $element
        .cssLeft(scrollableBounds2.x)
        .cssTop(scrollableBounds2.bottom() - 5);
      bounds = graphics.offsetBounds($element);
      expect(scrollbars.intersectViewport(bounds, $scrollables)).toEqual(new Rectangle(bounds.x, scrollableBounds2.bottom() - 5, bounds.width, bounds.height - 5));
    });

    it('returns the given rectangle if no scrollables are provided', () => {
      // If no scrollables can be found, the rectangle is fully visible -> return the bounds as they are
      // This behavior is consistent to isLocationInView which returns true in that case
      $element
        .cssLeft(scrollableBounds.x)
        .cssTop(scrollableBounds.y);
      let bounds = graphics.offsetBounds($element);
      expect(scrollbars.intersectViewport(bounds, null)).toEqual(bounds);
      expect(scrollbars.intersectViewport(bounds, $())).toEqual(bounds);
    });
  });

  describe('render', () => {

    it('ensures parent has position absolute or relative', () => {
      // Create scrollable without explicit position
      let $scrollable = $('<div>')
        .css('height', '50px')
        .css('width', '200px')
        .appendTo($('#sandbox'));
      expect($scrollable.css('position')).toBe('static');

      // Install scrollbars --> position should have been set automatically by Scrollbar._render()
      scrollbars.install($scrollable, {
        parent: new NullWidget(),
        session: session
      });
      expect($scrollable.css('position')).toBe('relative');

      // Clear
      scrollbars.uninstall($scrollable, session);
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
      scrollbars.install($scrollable, {
        parent: new NullWidget(),
        session: session
      });
      expect($scrollable.css('position')).toBe('');

      // Simulate "attach" lifecycle of widget
      $scrollable.appendTo($('#sandbox'));
      $scrollable.data('scrollbars').forEach(scrollbar => {
        scrollbar.attached = false;
        scrollbar.attach();
      });
      // Position should now have been set automatically by Scrollbar._renderOnAttach()
      expect($scrollable.css('position')).toBe('relative');
      scrollbars.uninstall($scrollable, session);
    });
  });

  describe('scrollShadow', () => {
    it('is installed automatically', () => {
      let $container = createScrollable();
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session
      });
      createContent($container);

      expect($container.data('scroll-shadow')[0]).toBe($container.next()[0]);
      scrollbars.uninstall($container, session);
    });

    it('is uninstalled properly on uninstall', () => {
      let $container = createScrollable();
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session
      });
      expect($container.data('scroll-shadow')[0]).toBe($container.next()[0]);
      expect($container.data('scroll-shadow-style')).toBeDefined();
      expect($container.data('scroll-shadow-handler')).toBeDefined();

      scrollbars.uninstall($container, session);
      expect($container.next()[0]).toBeUndefined();
      expect($container.data('scroll-shadow')).toBeUndefined();
      expect($container.data('scroll-shadow-style')).toBeUndefined();
      expect($container.data('scroll-shadow-handler')).toBeUndefined();
    });

    it('gets class top if container is scrolled down', () => {
      let $container = createScrollable();
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session
      });
      createContent($container);

      let $scrollShadow = $container.data('scroll-shadow');
      expect($scrollShadow).not.toHaveClass('top');

      $container[0].scrollTop = 10;
      $container.scroll(); // trigger scroll event
      expect($scrollShadow).toHaveClass('top');

      $container[0].scrollTop = 0;
      $container.scroll();
      expect($scrollShadow).not.toHaveClass('top');

      scrollbars.uninstall($container, session);
    });

    it('is moved along with the element', () => {
      let $container = createScrollable();
      let $another = session.$entryPoint.appendDiv();
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session
      });
      createContent($container);

      let $scrollShadow = $container.data('scroll-shadow');
      expect($scrollShadow[0]).toBe($container.next()[0]);
      expect($scrollShadow.next()[0]).toBe($another[0]);

      // Move scrollable to the end -> shadow has to be moved as well
      $container.insertAfter($another);

      // MutationObserver is sometimes not executed when running headless, even when using setTimeout -> call processing explicitly
      // @ts-expect-error
      scrollbars._processDomMutation({addedNodes: [$container[0]]});
      expect($scrollShadow[0]).toBe($container.next()[0]);
      expect($scrollShadow.next()[0]).toBeFalsy();
      scrollbars.uninstall($container, session);
    });

    it('changes its visibility based on the visibility of the scrollable', () => {
      let $container = createScrollable();
      $container.setVisible(false);
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session
      });
      createContent($container);

      let $scrollShadow = $container.data('scroll-shadow');
      scrollbars._onScrollableVisibleChange($container[0], false);
      expect($scrollShadow.isVisible()).toBe(false);

      $container.setVisible(true);
      scrollbars._onScrollableVisibleChange($container[0], true);
      expect($scrollShadow.isVisible()).toBe(true);

      $container.setVisible(false);
      scrollbars._onScrollableVisibleChange($container[0], false);
      expect($scrollShadow.isVisible()).toBe(false);
      scrollbars.uninstall($container, session);
    });
  });

  describe('install', () => {

    it('js-only: does not add tabindex', () => {
      let $container = createScrollable();
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session,
        nativeScrollbars: false,
        hybridScrollbars: false
      });
      expect($container.attr('tabindex')).toBe(undefined);
      scrollbars.uninstall($container, session);
      expect($container.attr('tabindex')).toBe(undefined);
    });

    it('hybrid: adds tabindex', () => {
      let $container = createScrollable();
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session,
        nativeScrollbars: false,
        hybridScrollbars: true
      });
      expect($container.attr('tabindex')).toBe('-2');
      scrollbars.uninstall($container, session);
      expect($container.attr('tabindex')).toBe('-2'); // not removed on uninstall
    });

    it('native-only: adds tabindex', () => {
      let $container = createScrollable();
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session,
        nativeScrollbars: true,
        hybridScrollbars: false
      });
      expect($container.attr('tabindex')).toBe('-2');
      scrollbars.uninstall($container, session);
      expect($container.attr('tabindex')).toBe('-2'); // not removed on uninstall
    });

    it('never adds tabindex if already present', () => {
      let $container = createScrollable();

      $container.attr('tabindex', '1');
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session,
        nativeScrollbars: false,
        hybridScrollbars: false
      });
      expect($container.attr('tabindex')).toBe('1');
      scrollbars.uninstall($container, session);
      expect($container.attr('tabindex')).toBe('1');

      $container.attr('tabindex', '2');
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session,
        nativeScrollbars: false,
        hybridScrollbars: true
      });
      expect($container.attr('tabindex')).toBe('2');
      scrollbars.uninstall($container, session);
      expect($container.attr('tabindex')).toBe('2');

      $container.attr('tabindex', '3');
      scrollbars.install($container, {
        parent: new NullWidget(),
        session: session,
        nativeScrollbars: true,
        hybridScrollbars: false
      });
      expect($container.attr('tabindex')).toBe('3');
      scrollbars.uninstall($container, session);
      expect($container.attr('tabindex')).toBe('3');
    });
  });
});
