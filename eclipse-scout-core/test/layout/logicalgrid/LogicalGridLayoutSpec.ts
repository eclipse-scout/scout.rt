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
// eslint-disable-next-line max-classes-per-file
import {AbstractLayout, Dimension, GridData, HtmlComponent, LogicalGridData, LogicalGridLayout, Widget} from '../../../src/index';

describe('LogicalGridLayout', () => {
  let session;
  let containerPadding = 5;
  let childMargin = 2;
  let rowHeight = 20;
  let columnWidth = 40;
  let hgap = 3;
  let vgap = 1;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  class StaticLayout extends AbstractLayout {
    constructor() {
      super();
      this.prefSize = new Dimension();
    }

    preferredLayoutSize($container, options) {
      return this.prefSize;
    }
  }

  class LglContainer extends Widget {
    _render() {
      this.$container = this.$parent.appendDiv();
      this.$container.css({
        padding: containerPadding
      });
      this.htmlComp = HtmlComponent.install(this.$container, this.session);
      this.htmlComp.setLayout(new LogicalGridLayout(this, {
        rowHeight: rowHeight,
        columnWidth: columnWidth,
        hgap: hgap,
        vgap: vgap
      }));
    }
  }

  class LglChild extends Widget {
    constructor() {
      super();
      this.gridData = new GridData();
    }

    _render() {
      this.$container = this.$parent.appendDiv();
      this.$container.css({
        margin: childMargin
      });
      this.htmlComp = HtmlComponent.install(this.$container, this.session);
      this.htmlComp.setLayout(new StaticLayout());
    }
  }

  function createLglChild(model) {
    let defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    let lglChild = new LglChild();
    lglChild.init(model);
    lglChild.render();
    lglChild.setLayoutData(new LogicalGridData(lglChild));
    return lglChild;
  }

  describe('prefSize', () => {
    let lglContainer;

    beforeEach(() => {
      lglContainer = new LglContainer();
      lglContainer.init({
        parent: session.desktop
      });
      lglContainer.render();
    });

    it('returns row height and column width incl. insets', () => {
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.useUiHeight = false;
      lglChild.htmlComp.layout.prefSize = new Dimension(10, 20);
      let prefSize = lglContainer.htmlComp.prefSize();
      expect(prefSize.height).toBe(rowHeight + containerPadding * 2);
      expect(prefSize.width).toBe(columnWidth + containerPadding * 2);
    });

    it('returns pref size of its child incl. insets if useUiHeight is true', () => {
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.useUiHeight = true;
      lglChild.htmlComp.layout.prefSize = new Dimension(10, 20);
      let prefSize = lglContainer.htmlComp.prefSize();
      expect(prefSize.height).toBe(20 + childMargin * 2 + containerPadding * 2);
      expect(prefSize.width).toBe(columnWidth + containerPadding * 2);
    });

    it('returns max pref size of its children at on same row incl. insets if useUiHeight is true', () => {
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.useUiHeight = true;
      lglChild.htmlComp.layout.prefSize = new Dimension(10, 20);

      let lglChild2 = createLglChild({
        parent: lglContainer
      });
      lglChild2.gridData.x = 1;
      lglChild2.gridData.y = 0;
      lglChild2.gridData.useUiHeight = true;
      lglChild2.htmlComp.layout.prefSize = new Dimension(10, 88);

      let prefSize = lglContainer.htmlComp.prefSize({
        widthHint: 100
      });
      expect(prefSize.height).toBe(88 + childMargin * 2 + containerPadding * 2);
      expect(prefSize.width).toBe(columnWidth * 2 + containerPadding * 2 + hgap);
    });

    it('passes widthHint to its children', () => {
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.useUiHeight = true;
      lglChild.htmlComp.layout.preferredLayoutSize = ($container, options) => {
        if (options.widthHint === 100 - childMargin * 2 - containerPadding * 2) {
          return new Dimension(75, 75);
        }
        return new Dimension();
      };
      let prefSize = lglContainer.htmlComp.prefSize({
        widthHint: 100
      });
      expect(prefSize.height).toBe(75 + childMargin * 2 + containerPadding * 2);
      expect(prefSize.width).toBe(columnWidth + containerPadding * 2);
    });

    it('considers hgap when passing widthHint if there are multiple children on the same row', () => {
      let widthHint;
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.useUiHeight = true;
      lglChild.htmlComp.layout.prefSize = new Dimension(10, 20);
      lglChild.htmlComp.layout.preferredLayoutSize = function($container, options) {
        widthHint = options.widthHint;
        return this.prefSize;
      };

      let widthHint2;
      let lglChild2 = createLglChild({
        parent: lglContainer
      });
      lglChild2.gridData.x = 1;
      lglChild2.gridData.y = 0;
      lglChild2.gridData.useUiHeight = true;
      lglChild2.htmlComp.layout.prefSize = new Dimension(10, 88);
      lglChild2.htmlComp.layout.preferredLayoutSize = function($container, options) {
        widthHint2 = options.widthHint;
        return this.prefSize;
      };

      let prefSize = lglContainer.htmlComp.prefSize({
        widthHint: 100
      });
      expect(widthHint).toBe(Math.ceil((100 - containerPadding * 2 - hgap) / 2) - childMargin * 2);
      expect(widthHint2).toBe(Math.floor((100 - containerPadding * 2 - hgap) / 2) - childMargin * 2);
    });

    it('considers weightX when passing widthHint if there are multiple children on the same row', () => {
      let widthHint;
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.useUiHeight = true;
      lglChild.gridData.weightX = 0;
      lglChild.htmlComp.layout.prefSize = new Dimension(100, 88);
      lglChild.htmlComp.layout.preferredLayoutSize = function($container, options) {
        widthHint = options.widthHint;
        return this.prefSize;
      };

      let widthHint2;
      let lglChild2 = createLglChild({
        parent: lglContainer
      });
      lglChild2.gridData.x = 1;
      lglChild2.gridData.y = 0;
      lglChild2.gridData.useUiHeight = true;
      lglChild2.gridData.weightX = 1;
      lglChild2.htmlComp.layout.prefSize = new Dimension(120, 50);
      lglChild2.htmlComp.layout.preferredLayoutSize = function($container, options) {
        widthHint2 = options.widthHint;
        return this.prefSize;
      };

      let prefSize = lglContainer.htmlComp.prefSize({
        widthHint: 400
      });
      // First column has weightX = 0 -> should be as width as the configured columnWidth
      expect(widthHint).toBe(columnWidth - childMargin * 2);
      expect(widthHint2).toBe(400 - containerPadding * 2 - hgap - childMargin * 2 - columnWidth);
    });

    it('uses widthInPixel as widthHint', () => {
      let widthHint;
      let prefSize1Called = false;
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.widthInPixel = 300;
      lglChild.gridData.weightX = 0;
      lglChild.htmlComp.layout.prefSize = new Dimension(100, 88);
      lglChild.htmlComp.layout.preferredLayoutSize = function($container, options) {
        prefSize1Called = true;
        widthHint = options.widthHint;
        return this.prefSize;
      };

      let widthHint2;
      let lglChild2 = createLglChild({
        parent: lglContainer
      });
      lglChild2.gridData.x = 1;
      lglChild2.gridData.y = 0;
      lglChild2.gridData.useUiHeight = true;
      lglChild2.gridData.weightX = 1;
      lglChild2.htmlComp.layout.prefSize = new Dimension(120, 50);
      lglChild2.htmlComp.layout.preferredLayoutSize = function($container, options) {
        widthHint2 = options.widthHint;
        return this.prefSize;
      };

      let prefSize = lglContainer.htmlComp.prefSize({
        widthHint: 400
      });
      // First column has a fixed width -> pref size must not be called
      expect(prefSize1Called).toBe(false);
      expect(widthHint2).toBe(400 - containerPadding * 2 - hgap - childMargin * 2 - 300);
      expect(prefSize.height).toBe(50 + childMargin * 2 + containerPadding * 2); // First component does not use uiHeight -> will be cut
    });

    it('uses preferred width as widthHint if fill horizontal is false', () => {
      let widthHint;
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.fillHorizontal = false;
      lglChild.gridData.useUiHeight = true;
      lglChild.htmlComp.layout.prefSize = new Dimension(120, 50);
      lglChild.htmlComp.layout.preferredLayoutSize = function($container, options) {
        widthHint = options.widthHint;
        return this.prefSize;
      };

      let prefSize = lglContainer.htmlComp.prefSize({
        widthHint: 400
      });
      expect(widthHint).toBe(120); // Needs to use pref width as width hint (cell bounds are larger than the child)
      expect(prefSize.height).toBe(50 + childMargin * 2 + containerPadding * 2);
    });

    it('uses container width as widthHint if fill horizontal is false and pref width is bigger', () => {
      let widthHint;
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.fillHorizontal = false;
      lglChild.gridData.useUiHeight = true;
      lglChild.htmlComp.layout.prefSize = new Dimension(500, 50);
      lglChild.htmlComp.layout.preferredLayoutSize = function($container, options) {
        widthHint = options.widthHint;
        return this.prefSize;
      };

      let prefSize = lglContainer.htmlComp.prefSize({
        widthHint: 400
      });
      expect(widthHint).toBe(400 - containerPadding * 2 - childMargin * 2);
      expect(prefSize.height).toBe(50 + childMargin * 2 + containerPadding * 2);
    });

    it('does not mess comp size up if fill horizontal and fill vertical are false', () => {
      let widthHint;
      let lglChild = createLglChild({
        parent: lglContainer
      });
      lglChild.gridData.x = 0;
      lglChild.gridData.y = 0;
      lglChild.gridData.fillHorizontal = false;
      lglChild.gridData.fillVertical = false;
      lglChild.htmlComp.layout.prefSize = new Dimension(120, 50);
      lglChild.htmlComp.layout.preferredLayoutSize = function($container, options) {
        widthHint = options.widthHint;
        return this.prefSize;
      };

      let prefSize = lglContainer.htmlComp.prefSize({
        widthHint: 400
      });
      expect(widthHint).toBe(120);
      expect(lglContainer.htmlComp.layout.info.compSize[0].width).toBe(120 + childMargin * 2);
      expect(lglContainer.htmlComp.layout.info.compSize[0].height).toBe(50 + childMargin * 2);
    });
  });
});
