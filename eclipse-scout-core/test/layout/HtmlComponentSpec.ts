/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, HtmlComponent, HtmlCompPrefSizeOptions, Insets} from '../../src/index';

describe('HtmlComponent', () => {
  setFixtures(sandbox());
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  let jqueryMock = {
    data: htmlComp => {
      // nop
    }
  } as JQuery;

  class LayoutMock extends AbstractLayout {
    override layout() {
      // nop
    }
  }

  class StaticLayout extends AbstractLayout {

    prefSize: Dimension;

    constructor() {
      super();
      this.prefSize = new Dimension();
    }

    override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
      return this.prefSize;
    }
  }

  let addWidthHeightMock = jqueryMock => {
    jqueryMock.width = val => {
      if (val !== undefined) {
        return jqueryMock;
      }
    };
    jqueryMock.height = val => {
      if (val !== undefined) {
        return jqueryMock;
      }
    };
    jqueryMock.outerWidth = withMargins => 6;
    jqueryMock.outerHeight = withMargins => 7;
    jqueryMock[0] = {};
    jqueryMock[0].getBoundingClientRect = () => ({
      width: 6,
      height: 7
    });
    jqueryMock.isDisplayNone = () => false;
  };

  describe('install', () => {

    it('does NOT set data \'htmlComponent\' when constructor is called', () => {
      spyOn(jqueryMock, 'data');
      new HtmlComponent(jqueryMock, session);
      expect(jqueryMock.data).not.toHaveBeenCalled();
    });

    it('sets data \'htmlComponent\' when install() is called', () => {
      spyOn(jqueryMock, 'data');
      let htmlComp = HtmlComponent.install(jqueryMock, session);
      // @ts-expect-error
      expect(jqueryMock.data).toHaveBeenCalledWith('htmlComponent', htmlComp);
    });

  });

  describe('size', () => {

    addWidthHeightMock(jqueryMock);

    it('returns getBoundingClientRect() of JQuery comp', () => {
      let htmlComp = HtmlComponent.install(jqueryMock, session);
      let size = htmlComp.size();
      expect(size.width).toBe(6);
      expect(size.height).toBe(7);
    });
  });

  describe('setSize', () => {
    let $comp;
    let htmlComp;

    // return size(6, 7)
    addWidthHeightMock(jqueryMock);

    beforeEach(() => {
      $comp = $('<div>').appendTo(session.$entryPoint);
      htmlComp = HtmlComponent.install($comp, session);
      htmlComp.layout = new LayoutMock();
    });

    it('accepts Dimension as single argument', () => {
      spyOn($comp, 'css').and.callThrough();
      htmlComp.setSize(new Dimension(6, 7));
      let size = htmlComp.size();
      expect(size.width).toBe(6);
      expect(size.height).toBe(7);
      expect($comp.css).toHaveBeenCalledWith('width', '6px');
      expect($comp.css).toHaveBeenCalledWith('height', '7px');
    });

    it('calls invalidate on layout when size has changed', () => {
      spyOn(htmlComp.layout, 'invalidate');
      htmlComp.setSize(new Dimension(1, 2));
      expect(htmlComp.layout.invalidate).toHaveBeenCalled();
    });

  });

  describe('insets', () => {

    it('reads padding, margin and border correctly', () => {
      let jqueryObj = $('<div>').css({
        marginTop: '1px',
        marginRight: '2px',
        marginBottom: '3px',
        marginLeft: '4px',
        paddingTop: '5px',
        paddingRight: '6px',
        paddingBottom: '7px',
        paddingLeft: '8px',
        borderStyle: 'solid',
        borderTopWidth: '9px',
        borderRightWidth: '10px',
        borderBottomWidth: '11px',
        borderLeftWidth: '12px'
      });
      let htmlComp = HtmlComponent.install(jqueryObj, session);
      let expected = new Insets(15, 18, 21, 24);
      let actual = htmlComp.insets({
        includeMargin: true
      });
      expect(actual).toEqual(expected);
      let actual2 = htmlComp.insets(true);
      expect(actual2).toEqual(expected);
    });

  });

  describe('validateLayout', () => {
    let $comp;
    let $child;
    let htmlComp;
    let htmlChild;

    beforeEach(() => {
      $comp = $('<div>').appendTo(session.$entryPoint);
      $child = $comp.appendDiv();
      htmlComp = HtmlComponent.install($comp, session);
      htmlChild = HtmlComponent.install($child, session);
    });

    it('calls htmlComp.layout', () => {
      spyOn(htmlComp.layout, 'layout').and.callThrough();
      htmlComp.validateLayout();
      expect(htmlComp.layout.layout).toHaveBeenCalled();
    });

    it('calls layout of the child component', () => {
      spyOn(htmlChild.layout, 'layout').and.callThrough();
      htmlComp.validateLayout();
      expect(htmlChild.layout.layout).toHaveBeenCalled();
    });

    it('does not layout invisible components', () => {
      $comp.setVisible(false);
      spyOn(htmlComp.layout, 'layout').and.callThrough();
      htmlComp.validateLayout();
      expect(htmlComp.layout.layout).not.toHaveBeenCalled();
    });

    it('does not layout components with an invisible parent', () => {
      $comp.setVisible(false);
      spyOn(htmlChild.layout, 'layout').and.callThrough();
      htmlComp.validateLayout();
      expect(htmlChild.layout.layout).not.toHaveBeenCalled();
    });

    it('does not call parents() too many times', () => {
      spyOn(htmlComp.$comp, 'parents').and.callThrough();
      spyOn(htmlChild.$comp, 'parents').and.callThrough();
      htmlComp.validateLayout();
      expect(htmlComp.$comp.parents).toHaveBeenCalled();
      expect(htmlChild.$comp.parents).not.toHaveBeenCalled();
    });

    it('does not layout components with an animating parent (CSS)', () => {
      $comp.addClass('animate-test');
      spyOn(htmlChild.layout, 'layout').and.callThrough();
      htmlChild.validateLayout();
      expect(htmlChild.layout.layout).not.toHaveBeenCalled();

      // Simulate end of animation
      $comp.removeClass('animate-test');
      $comp.trigger('animationend');
      expect(htmlChild.layout.layout).toHaveBeenCalled();
    });

    it('does not layout animated components (CSS)', () => {
      $comp.addClass('animate-test');
      spyOn(htmlComp.layout, 'layout').and.callThrough();
      htmlComp.validateLayout();
      expect(htmlComp.layout.layout).not.toHaveBeenCalled();

      // Simulate end of animation
      $comp.removeClass('animate-test');
      $comp.trigger('animationend');
      expect(htmlComp.layout.layout).toHaveBeenCalled();
    });

    it('does not layout components with an animating parent (JS)', () => {
      let deferred = $.Deferred();
      let promise = deferred.promise();
      $comp.data('animate-promise', promise);
      // use always(), so it will be executed immediately when the promise is resolved (otherwise, we might get an endless loop)
      promise.always(() => $comp.removeData('animate-promise'));

      spyOn(htmlChild.layout, 'layout').and.callThrough();
      htmlChild.validateLayout();
      expect(htmlChild.layout.layout).not.toHaveBeenCalled();

      // Simulate end of animation
      deferred.resolve();
      expect(htmlChild.layout.layout).toHaveBeenCalled();
    });

    it('does not layout animated components (JS)', () => {
      let deferred = $.Deferred();
      let promise = deferred.promise();
      $comp.data('animate-promise', promise);
      // use always(), so it will be executed immediately when the promise is resolved (otherwise, we might get an endless loop)
      promise.always(() => $comp.removeData('animate-promise'));

      spyOn(htmlComp.layout, 'layout').and.callThrough();
      htmlComp.validateLayout();
      expect(htmlComp.layout.layout).not.toHaveBeenCalled();

      // Simulate end of animation
      deferred.resolve();
      expect(htmlComp.layout.layout).toHaveBeenCalled();
    });
  });

  describe('prefSize', () => {
    let $comp;
    let htmlComp;

    beforeEach(() => {
      $comp = $('<div>').appendTo(session.$entryPoint);
      $comp.css({
        minWidth: '10px',
        maxWidth: '20px',
        minHeight: '5px',
        maxHeight: '30px'
      });
      htmlComp = HtmlComponent.install($comp, session);
      htmlComp.setLayout(new StaticLayout());
    });

    it('returns preferred size of the component', () => {
      htmlComp.layout.prefSize = new Dimension(15, 13);
      let size = htmlComp.prefSize();
      expect(size.width).toBe(15);
      expect(size.height).toBe(13);
    });

    it('considers max width/height set by CSS', () => {
      htmlComp.layout.prefSize = new Dimension(500, 500);
      let size = htmlComp.prefSize();
      expect(size.width).toBe(20);
      expect(size.height).toBe(30);
    });

    it('considers min width/height set by CSS', () => {
      htmlComp.layout.prefSize = new Dimension(2, 3);
      let size = htmlComp.prefSize();
      expect(size.width).toBe(10);
      expect(size.height).toBe(5);
    });

    it('returns zero size for invisible components', () => {
      htmlComp.validateLayout();
      spyOn(htmlComp.layout, 'preferredLayoutSize').and.callThrough();

      htmlComp.layout.prefSize = new Dimension(15, 13);
      let size = htmlComp.prefSize();
      expect(size.width).toBe(15);
      expect(size.height).toBe(13);
      expect(htmlComp.layout.preferredLayoutSize.calls.count()).toEqual(1);

      $comp.setVisible(false);
      size = htmlComp.prefSize();
      expect(size.width).toBe(0);
      expect(size.height).toBe(0);
      expect(htmlComp.layout.preferredLayoutSize.calls.count()).toEqual(1); // should return (0,0) directly

      $comp.setVisible(true);
      size = htmlComp.prefSize();
      expect(size.width).toBe(15);
      expect(size.height).toBe(13);
      expect(htmlComp.layout.preferredLayoutSize.calls.count()).toEqual(1); // should return previously cached size
    });
  });

});
