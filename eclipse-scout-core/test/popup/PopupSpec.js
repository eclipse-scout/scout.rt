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
import {Dimension, HtmlComponent, Popup, scout, Widget} from '../../src/index';

describe('Popup', function() {
  var session, $desktop;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        navigationVisible: true
      }
    });
    $desktop = session.desktop.$container;
    $('<style>' +
      '.desktop {position: absolute; left: 0; top: 0; width: 220px; height: 220px; background-color: blue;}' +
      '.popup {position: absolute; min-width: 50px; min-height: 50px; max-width: 50px; max-height: 50px; background-color: white;}' +
      '.popup.with-margin {margin: 10px;}' +
      '.popup.with-dyn-margin.top {margin-bottom: 5px;}' +
      '.popup.with-dyn-margin.bottom {margin-top: 5px;}' +
      '.popup.scalable {min-width: 0; min-height: 0; max-width: 500px; max-height: 500px;}' +
      '.anchor {position: absolute; left: 70px; top: 70px; width: 80px; height: 80px; background-color: red;}' +
      '.wrapping-block {display: inline-block; vertical-align: middle; width: 25px; height: 50px; background-color: rgba(0, 255, 0, 0.5);}' +
      '.large-block {display: block; width: 25px; height: 25px; background-color: rgba(0, 255, 0, 0.5);}' +
      '</style>').appendTo($('#sandbox'));
  });

  class WrappingContent extends Widget {
    constructor() {
      super();
      this.numBlocks = 2;
    }

    _render() {
      this.$container = this.$parent.appendDiv();
      this.htmlComp = HtmlComponent.install(this.$container, this.session);
      for (var i = 0; i < this.numBlocks; i++) {
        this.$container.appendDiv('wrapping-block');
      }
    }
  }

  window.scouttests = window.scouttests || {};
  window.scouttests.WrappingContent = WrappingContent;

  class LargeContent extends Widget {
    constructor() {
      super();
      this.numBlocks = 2;
    }

    _render() {
      this.$container = this.$parent.appendDiv();
      this.htmlComp = HtmlComponent.install(this.$container, this.session);
      for (var i = 0; i < this.numBlocks; i++) {
        this.$container.appendDiv('large-block');
      }
    }
  }

  window.scouttests.LargeContent = LargeContent;

  var entryPointSizeFunc = function() {
    return new Dimension($desktop.width(), $desktop.height());
  };

  afterEach(function() {
    removePopups(session);
  });

  describe('withGlassPane', function() {
    it('shows a glass pane if set to true', function() {
      var popup = scout.create('Popup', {
        parent: session.desktop,
        withGlassPane: true
      });
      popup.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(1);
      expect(popup.$container.children('.glasspane').length).toBe(0);
    });

    it('does not show a glass pane if set to false', function() {
      var popup = scout.create('Popup', {
        parent: session.desktop
      });
      popup.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(0);
      expect(popup.$container.children('.glasspane').length).toBe(0);
    });
  });

  describe('horizontalAlignment', function() {
    describe('RIGHT', function() {
      it('opens on the right of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHT,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('opens on the right of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          horizontalAlignment: Popup.Alignment.RIGHT,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });

      it('moves to left when overlapping right window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHT,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        $desktop.cssWidth(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 - 25);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('moves to left when overlapping right window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHT,
          $anchor: $anchor,
          cssClass: 'with-margin',
          windowPaddingX: 0
        });
        $desktop.cssWidth(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 - 25 - 20);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });

    describe('LEFT', function() {
      it('opens on the left of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.LEFT,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 - 50);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('opens on the left of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          horizontalAlignment: Popup.Alignment.LEFT,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 - 50 - 20);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });

      it('moves to right when overlapping left window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        $anchor.cssLeft(70 - 25);
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.LEFT,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        $desktop.cssWidth(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(0);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('moves to right when overlapping left window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        $anchor.cssLeft(70 - 25);
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.LEFT,
          $anchor: $anchor,
          cssClass: 'with-margin',
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(0);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });

    describe('LEFTEDGE', function() {
      it('opens on the left edge of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('opens on the left edge of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 - 10);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });

    describe('RIGHTEDGE', function() {
      it('opens on the right edge of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHTEDGE,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 - 50);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('opens on the left edge of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          horizontalAlignment: Popup.Alignment.RIGHTEDGE,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 - 50 - 10);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });

    describe('CENTER', function() {
      it('opens on the center of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.CENTER,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 / 2 - 50 / 2);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('opens on the center of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          horizontalAlignment: Popup.Alignment.CENTER,
          $anchor: $anchor,
          windowPaddingY: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 / 2 - 50 / 2 - 10);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });
  });

  describe('verticalAlignment', function() {
    describe('BOTTOM', function() {
      it('opens on the bottom of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.BOTTOM,
          $anchor: $anchor,
          windowPaddingY: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('opens on the bottom of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          verticalAlignment: Popup.Alignment.BOTTOM,
          $anchor: $anchor,
          windowPaddingY: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });

      it('moves to top when overlapping bottom window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.BOTTOM,
          verticalSwitch: false,
          $anchor: $anchor,
          windowPaddingY: 0
        });
        $desktop.cssHeight(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 - 25);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('moves to top when overlapping bottom window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.BOTTOM,
          verticalSwitch: false,
          $anchor: $anchor,
          cssClass: 'with-margin',
          windowPaddingY: 0
        });
        $desktop.cssHeight(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 - 25 - 20);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });

    describe('TOP', function() {
      it('opens on the top of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.TOP,
          $anchor: $anchor,
          windowPaddingY: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 50);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('opens on the top of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          verticalAlignment: Popup.Alignment.TOP,
          $anchor: $anchor,
          windowPaddingY: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 50 - 20);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });

      it('moves to bottom when overlapping top window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        $anchor.cssTop(70 - 25);
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.TOP,
          verticalSwitch: false,
          $anchor: $anchor,
          windowPaddingY: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(0);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('moves to bottom when overlapping top window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        $anchor.cssTop(70 - 25);
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.TOP,
          verticalSwitch: false,
          $anchor: $anchor,
          cssClass: 'with-margin',
          windowPaddingY: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(0);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });

    describe('TOPEDGE', function() {
      it('opens on the top edge of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.TOPEDGE,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('opens on the left edge of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          verticalAlignment: Popup.Alignment.TOPEDGE,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 10);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });

    describe('BOTTOMEDGE', function() {
      it('opens on the bottom edge of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.BOTTOMEDGE,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 - 50);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('opens on the bottom edge of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          verticalAlignment: Popup.Alignment.BOTTOMEDGE,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 - 50 - 10);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });

    describe('CENTER', function() {
      it('opens on the center of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.CENTER,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 / 2 - 50 / 2);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('opens on the center of the anchor considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          verticalAlignment: Popup.Alignment.CENTER,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 / 2 - 50 / 2 - 10);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });
  });

  describe('trimWidth', function() {
    describe('reduces width if there is not enough space', function() {
      it('on the left', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.LEFT,
          verticalAlignment: Popup.Alignment.CENTER,
          trimWidth: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          widget: {
            objectType: 'scouttests.WrappingContent',
            numBlocks: 3
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(0);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssHeight()).toBe(100);
      });

      it('on the right', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.CENTER,
          trimWidth: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          widget: {
            objectType: 'scouttests.WrappingContent',
            numBlocks: 3
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssHeight()).toBe(100);
      });
    });

    describe('with hAlign = LEFTEDGE', function() {
      it('does not unnecessarily trim if the popup could be displayed completely', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable',
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          verticalAlignment: Popup.Alignment.BOTTOM,
          horizontalSwitch: true,
          trimWidth: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          widget: {
            objectType: 'scouttests.WrappingContent',
            numBlocks: 4
          }
        });
        $desktop.cssWidth(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70);
        expect(popup.$container.cssWidth()).toBe(100);
      });
    });
  });

  describe('trimHeight', function() {
    describe('reduces height if there is not enough space', function() {
      it('on the bottom', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.CENTER,
          verticalAlignment: Popup.Alignment.BOTTOM,
          trimHeight: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          widget: {
            objectType: 'scouttests.LargeContent',
            numBlocks: 3
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('on the top', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.CENTER,
          verticalAlignment: Popup.Alignment.TOP,
          trimHeight: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          widget: {
            objectType: 'scouttests.LargeContent',
            numBlocks: 3
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(0);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('on the center', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.CENTER,
          verticalAlignment: Popup.Alignment.CENTER,
          trimHeight: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          widget: {
            objectType: 'scouttests.LargeContent',
            numBlocks: 10
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(0);
        expect(popup.$container.cssHeight()).toBe(200);
        expect(popup.$container.cssMarginY()).toBe(20);
      });

      it('on the edge', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.LEFT,
          verticalAlignment: Popup.Alignment.TOPEDGE,
          trimHeight: true,
          verticalSwitch: false,
          $anchor: $anchor,
          windowPaddingY: 0,
          widget: {
            objectType: 'scouttests.LargeContent',
            numBlocks: 20
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(0);
        expect(popup.$container.cssHeight()).toBe(200);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });

    describe('with vAlign = TOPEDGE', function() {
      it('does not unnecessarily trim if the popup could be displayed completely', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable',
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.TOPEDGE,
          verticalSwitch: true,
          trimHeight: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          widget: {
            objectType: 'scouttests.LargeContent',
            numBlocks: 4
          }
        });
        $desktop.cssHeight(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70);
        expect(popup.$container.cssHeight()).toBe(100);
      });
    });
  });

  describe('verticalSwitch', function() {
    describe('with verticalAlign = bottom', function() {
      it('switches to top when overlapping bottom window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.BOTTOM,
          $anchor: $anchor,
          windowPaddingY: 0
        });
        $desktop.cssHeight(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 50);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('switches to top when overlapping bottom window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.BOTTOM,
          $anchor: $anchor,
          cssClass: 'with-margin',
          windowPaddingY: 0
        });
        $desktop.cssHeight(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 50 - 20);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });

      it('switches to top when overlapping bottom window border with dynamic margins', function() {
        // Don't switch
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.BOTTOM,
          $anchor: $anchor,
          cssClass: 'with-dyn-margin',
          windowPaddingY: 0
        });
        $desktop.cssHeight(70 + 80 + 5 + 50);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(5);
        popup.close();

        // Switch
        $anchor = $desktop.appendDiv('anchor');
        popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.BOTTOM,
          $anchor: $anchor,
          cssClass: 'with-dyn-margin',
          windowPaddingY: 0
        });
        $desktop.cssHeight(70 + 80 + 5 + 50 - 1); // -> switch because 1px overlap
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 50 - 5);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(5);
      });
    });

    describe('with verticalAlign = top', function() {
      it('switches to bottom when overlapping top window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        $anchor.cssTop(70 - 25);
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.TOP,
          $anchor: $anchor,
          windowPaddingY: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 25 + 80);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('switches to top when overlapping bottom window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        $anchor.cssTop(70 - 25);
        var popup = scout.create('Popup', {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.TOP,
          $anchor: $anchor,
          cssClass: 'with-margin',
          windowPaddingY: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 - 25 + 80);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });

    describe('with verticalAlign = topedge', function() {
      it('switches to bottomedge when overlapping bottom window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable',
          verticalAlignment: Popup.Alignment.TOPEDGE,
          verticalSwitch: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          widget: {
            objectType: 'scouttests.LargeContent',
            numBlocks: 5
          }
        });
        $desktop.cssHeight(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 - 125);
        expect(popup.$container.cssHeight()).toBe(125);
      });

      it('switches to bottomedge when overlapping bottom window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          verticalAlignment: Popup.Alignment.TOPEDGE,
          verticalSwitch: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          widget: {
            objectType: 'scouttests.LargeContent',
            numBlocks: 5
          }
        });
        $desktop.cssHeight(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 - 125 - 10);
        expect(popup.$container.cssHeight()).toBe(125);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });
  });

  describe('horizontalSwitch', function() {
    describe('with horizontalAlign = right', function() {
      it('switches to left when overlapping right window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHT,
          horizontalSwitch: true,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        $desktop.cssWidth(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 - 50);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('switches to left when overlapping right window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          horizontalAlignment: Popup.Alignment.RIGHT,
          horizontalSwitch: true,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        $desktop.cssWidth(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 - 50 - 20);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });

    describe('with horizontalAlign = left', function() {
      it('switches to right when overlapping left window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        $anchor.cssLeft(70 - 25);
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.LEFT,
          horizontalSwitch: true,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 - 25 + 80);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('switches to right when overlapping left window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        $anchor.cssLeft(70 - 25);
        var popup = scout.create('Popup', {
          parent: session.desktop,
          cssClass: 'with-margin',
          horizontalAlignment: Popup.Alignment.LEFT,
          horizontalSwitch: true,
          $anchor: $anchor,
          windowPaddingX: 0
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 - 25 + 80);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });

    describe('with horizontalAlign = rightedge', function() {
      it('switches to rightedge when overlapping right window border', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable',
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          horizontalSwitch: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          widget: {
            objectType: 'scouttests.WrappingContent',
            numBlocks: 5
          }
        });
        $desktop.cssWidth(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 - 125);
        expect(popup.$container.cssWidth()).toBe(125);
      });

      it('switches to rightedge when overlapping right window border considering margin', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('WidgetPopup', {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          horizontalSwitch: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          widget: {
            objectType: 'scouttests.WrappingContent',
            numBlocks: 5
          }
        });
        $desktop.cssWidth(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 - 125 - 10);
        expect(popup.$container.cssWidth()).toBe(125);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });
  });

  describe('withArrow', function() {
    describe('and hAlign LEFT, vAlign CENTER', function() {
      it('opens popup to the left, arrow points to the right into the center of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.LEFT,
          verticalAlignment: Popup.Alignment.CENTER,
          $anchor: $anchor,
          windowPaddingX: 0,
          withArrow: true
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 - 50);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssTop()).toBe(70 + 80 / 2 - 50 / 2);
        expect(popup.$container.cssHeight()).toBe(50);
      });
    });

    describe('and hAlign RIGHT, vAlign CENTER', function() {
      it('opens popup to the right, arrow points to the left into the center of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.CENTER,
          $anchor: $anchor,
          windowPaddingX: 0,
          withArrow: true
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssTop()).toBe(70 + 80 / 2 - 50 / 2);
        expect(popup.$container.cssHeight()).toBe(50);
      });
    });

    describe('and hAlign CENTER, vAlign TOP', function() {
      it('opens popup to the top, arrow points to the bottom into the center of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.CENTER,
          verticalAlignment: Popup.Alignment.TOP,
          $anchor: $anchor,
          windowPaddingY: 0,
          withArrow: true
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 / 2 - 50 / 2);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssTop()).toBe(70 - 50);
        expect(popup.$container.cssHeight()).toBe(50);
      });
    });

    describe('and hAlign CENTER, vAlign BOTTOM', function() {
      it('opens popup to the bottom, arrow points to the top into the center of the anchor', function() {
        var $anchor = $desktop.appendDiv('anchor');
        var popup = scout.create('Popup', {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.CENTER,
          verticalAlignment: Popup.Alignment.BOTTOM,
          $anchor: $anchor,
          windowPaddingY: 0,
          withArrow: true
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 / 2 - 50 / 2);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssTop()).toBe(70 + 80);
        expect(popup.$container.cssHeight()).toBe(50);
      });
    });
  });
});
