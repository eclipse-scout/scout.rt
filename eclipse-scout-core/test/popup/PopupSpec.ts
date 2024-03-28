/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, HtmlComponent, Popup, scout, StringField, Widget, WidgetPopup} from '../../src/index';
import {JQueryTesting} from '../../src/testing';

describe('Popup', () => {
  let session: SandboxSession, $desktop: JQuery;

  beforeEach(() => {
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
      '.popup.with-uneven-margin {margin: 5px 15px 15px 5px;}' +
      '.popup.with-dyn-margin.top {margin-bottom: 5px;}' +
      '.popup.with-dyn-margin.bottom {margin-top: 5px;}' +
      '.popup.scalable {min-width: 0; min-height: 0; max-width: 500px; max-height: 500px;}' +
      '.anchor {position: absolute; left: 70px; top: 70px; width: 80px; height: 80px; background-color: red;}' +
      '.wrapping-block {display: inline-block; vertical-align: middle; width: 25px; height: 50px; background-color: rgba(0, 255, 0, 0.5);}' +
      '.large-block {display: block; width: 25px; height: 25px; background-color: rgba(0, 255, 0, 0.5);}' +
      '</style>').appendTo($('#sandbox'));
  });

  class WrappingContent extends Widget {
    numBlocks: number;

    constructor() {
      super();
      this.numBlocks = 2;
    }

    override _render() {
      this.$container = this.$parent.appendDiv();
      this.htmlComp = HtmlComponent.install(this.$container, this.session);
      for (let i = 0; i < this.numBlocks; i++) {
        this.$container.appendDiv('wrapping-block');
      }
    }
  }

  class LargeContent extends Widget {
    numBlocks: number;

    constructor() {
      super();
      this.numBlocks = 2;
    }

    override _render() {
      this.$container = this.$parent.appendDiv();
      this.htmlComp = HtmlComponent.install(this.$container, this.session);
      for (let i = 0; i < this.numBlocks; i++) {
        this.$container.appendDiv('large-block');
      }
    }
  }

  let entryPointSizeFunc = () => new Dimension($desktop.width(), $desktop.height());

  afterEach(() => {
    removePopups(session);
  });
  describe('modal', () => {
    it('has the "modal" css class if set to true', () => {
      let popup = scout.create(Popup, {
        parent: session.desktop,
        modal: true
      });
      popup.render();
      expect(popup.$container.hasClass('modal')).toBe(true);
    });

    it('has not the "modal" css class if set to false', () => {
      let popup = scout.create(Popup, {
        parent: session.desktop,
        modal: false
      });
      popup.render();
      expect(popup.$container.hasClass('modal')).toBe(false);
    });

    it('does not closes on mouse down outside', () => {
      let popup = scout.create(Popup, {
        parent: session.desktop,
        modal: true
      });
      popup.open();
      JQueryTesting.triggerMouseDownCapture($desktop);
      expect(popup.destroyed).toBe(false);

      popup.setModal(false);
      JQueryTesting.triggerMouseDownCapture($desktop);
      expect(popup.destroyed).toBe(true);
    });

    it('can be activated after the popup was opened', () => {
      let popup = scout.create(Popup, {
        parent: session.desktop,
        modal: false
      });
      popup.open();
      expect(popup.$container.hasClass('modal')).toBe(false);

      popup.setModal(true);
      expect(popup.$container.hasClass('modal')).toBe(true);
    });

    it('overrules the "withGlassPane" and the close properties, but restores their values afterwards', () => {
      let popup = scout.create(Popup, {
        parent: session.desktop,
        modal: false,
        withGlassPane: false,
        closeOnMouseDownOutside: false,
        closeOnAnchorMouseDown: true,
        closeOnOtherPopupOpen: false
      });
      popup.open();
      popup.setModal(true);
      // modal overrules these properties
      expect(popup.withGlassPane).toBe(true);
      expect(popup.closeOnMouseDownOutside).toBe(false);
      expect(popup.closeOnAnchorMouseDown).toBe(false);
      expect(popup.closeOnOtherPopupOpen).toBe(false);

      popup.setModal(false);
      // properties are restored
      expect(popup.withGlassPane).toBe(false);
      expect(popup.closeOnMouseDownOutside).toBe(false);
      expect(popup.closeOnAnchorMouseDown).toBe(true);
      expect(popup.closeOnOtherPopupOpen).toBe(false);
    });

    it('keeps track of the changes to the "withGlassPane" and the close properties and restores their values afterwards', () => {
      let popup = scout.create(Popup, {
        parent: session.desktop,
        modal: true,
        withGlassPane: false,
        closeOnMouseDownOutside: false,
        closeOnAnchorMouseDown: true,
        closeOnOtherPopupOpen: false
      });
      popup.open();
      // modal overrules these properties
      expect(popup.withGlassPane).toBe(true);
      expect(popup.closeOnMouseDownOutside).toBe(false);
      expect(popup.closeOnAnchorMouseDown).toBe(false);
      expect(popup.closeOnOtherPopupOpen).toBe(false);

      popup.setWithGlassPane(true);
      popup.setCloseOnMouseDownOutside(true);
      popup.setCloseOnAnchorMouseDown(false);
      popup.setCloseOnOtherPopupOpen(false);
      // setters do not have an immediate impact on the properties
      expect(popup.withGlassPane).toBe(true);
      expect(popup.closeOnMouseDownOutside).toBe(false);
      expect(popup.closeOnAnchorMouseDown).toBe(false);
      expect(popup.closeOnOtherPopupOpen).toBe(false);

      popup.setModal(false);
      // properties are restored
      expect(popup.withGlassPane).toBe(true);
      expect(popup.closeOnMouseDownOutside).toBe(true);
      expect(popup.closeOnAnchorMouseDown).toBe(false);
      expect(popup.closeOnOtherPopupOpen).toBe(false);
    });
  });
  describe('withGlassPane', () => {
    it('shows a glass pane if set to true', () => {
      let popup = scout.create(Popup, {
        parent: session.desktop,
        withGlassPane: true
      });
      popup.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(1);
      expect(popup.$container.children('.glasspane').length).toBe(0);
    });

    it('does not show a glass pane if set to false', () => {
      let popup = scout.create(Popup, {
        parent: session.desktop
      });
      popup.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(0);
      expect(popup.$container.children('.glasspane').length).toBe(0);
    });

    it('does not get covered with glasspane when multiple popups are open', () => {
      let popup = scout.create(Popup, {
        parent: session.desktop,
        withGlassPane: true
      });
      popup.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(1);
      expect(popup.$container.children('.glasspane').length).toBe(0);

      let popup2 = scout.create(Popup, {
        parent: session.desktop,
        withGlassPane: true
      });
      popup2.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(2);
      expect(popup.$container.children('.glasspane').length).toBe(1); // First popup needs to be covered
      expect(popup2.$container.children('.glasspane').length).toBe(0); // Current popup must not be covered

      let popup3 = scout.create(Popup, {
        parent: session.desktop
      });
      popup3.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(2);
      expect(popup.$container.children('.glasspane').length).toBe(1); // First popup needs to be covered
      expect(popup2.$container.children('.glasspane').length).toBe(0); // Still no glass pane because popup3 does not set withGlassPane
      expect(popup3.$container.children('.glasspane').length).toBe(0); // Current popup must not be covered

      let popup4 = scout.create(Popup, {
        parent: session.desktop,
        withGlassPane: true
      });
      popup4.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(3);
      expect(popup.$container.children('.glasspane').length).toBe(2); // First popup needs to be covered
      expect(popup2.$container.children('.glasspane').length).toBe(1); // Gets glasspane
      expect(popup3.$container.children('.glasspane').length).toBe(1); // Gets glasspane
      expect(popup4.$container.children('.glasspane').length).toBe(0); // Current popup must not be covered
    });
  });

  describe('horizontalAlignment', () => {
    describe('RIGHT', () => {
      it('opens on the right of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('opens on the right of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssLeft()).toBe(70 + 80);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });

      it('moves to left when overlapping right window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('moves to left when overlapping right window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

    describe('LEFT', () => {
      it('opens on the left of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('opens on the left of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssLeft()).toBe(70 - 50 - 20);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });

      it('moves to right when overlapping left window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        $anchor.cssLeft(70 - 25);
        let popup = scout.create(Popup, {
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

      it('moves to right when overlapping left window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        $anchor.cssLeft(70 - 25);
        let popup = scout.create(Popup, {
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

    describe('LEFTEDGE', () => {
      it('opens on the left edge of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('opens on the left edge of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssLeft()).toBe(70 - 5);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });

    describe('RIGHTEDGE', () => {
      it('opens on the right edge of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('opens on the left edge of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssLeft()).toBe(70 + 80 - 50 - 5);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });

    describe('CENTER', () => {
      it('opens on the center of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
          parent: session.desktop,
          horizontalAlignment: Popup.Alignment.CENTER,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 / 2 - 50 / 2);
        expect(popup.$container.cssWidth()).toBe(50);
      });

      it('opens on the center of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssLeft()).toBe(70 + 80 / 2 - 50 / 2 - 5);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssMarginX()).toBe(20);
      });
    });
  });

  describe('verticalAlignment', () => {
    describe('BOTTOM', () => {
      it('opens on the bottom of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('opens on the bottom of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssTop()).toBe(70 + 80);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });

      it('moves to top when overlapping bottom window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('moves to top when overlapping bottom window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

    describe('TOP', () => {
      it('opens on the top of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('opens on the top of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssTop()).toBe(70 - 50 - 20);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });

      it('moves to bottom when overlapping top window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        $anchor.cssTop(70 - 25);
        let popup = scout.create(Popup, {
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

      it('moves to bottom when overlapping top window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        $anchor.cssTop(70 - 25);
        let popup = scout.create(Popup, {
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

    describe('TOPEDGE', () => {
      it('opens on the top edge of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.TOPEDGE,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('opens on the left edge of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssTop()).toBe(70 - 5);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });

    describe('BOTTOMEDGE', () => {
      it('opens on the bottom edge of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.BOTTOMEDGE,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 - 50);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('opens on the bottom edge of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssTop()).toBe(70 + 80 - 50 - 5);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });

    describe('CENTER', () => {
      it('opens on the center of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
          parent: session.desktop,
          verticalAlignment: Popup.Alignment.CENTER,
          $anchor: $anchor
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 / 2 - 50 / 2);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('opens on the center of the anchor considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

        popup.removeCssClass('with-margin');
        popup.addCssClass('with-uneven-margin');
        popup.position();
        expect(popup.$container.cssTop()).toBe(70 + 80 / 2 - 50 / 2 - 5);
        expect(popup.$container.cssHeight()).toBe(50);
        expect(popup.$container.cssMarginY()).toBe(20);
      });
    });
  });

  describe('trimWidth', () => {
    describe('reduces width if there is not enough space', () => {
      it('on the left', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.LEFT,
          verticalAlignment: Popup.Alignment.CENTER,
          trimWidth: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          content: {
            objectType: WrappingContent,
            numBlocks: 3
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(0);
        expect(popup.$container.cssWidth()).toBe(50);
        expect(popup.$container.cssHeight()).toBe(100);
      });

      it('on the right', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.CENTER,
          trimWidth: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          content: {
            objectType: WrappingContent,
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

    describe('with hAlign = LEFTEDGE', () => {
      it('does not unnecessarily trim if the popup could be displayed completely', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable',
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          verticalAlignment: Popup.Alignment.BOTTOM,
          horizontalSwitch: true,
          trimWidth: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          content: {
            objectType: WrappingContent,
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

  describe('trimHeight', () => {
    describe('reduces height if there is not enough space', () => {
      it('on the bottom', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.CENTER,
          verticalAlignment: Popup.Alignment.BOTTOM,
          trimHeight: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          content: {
            objectType: LargeContent,
            numBlocks: 3
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('on the top', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.CENTER,
          verticalAlignment: Popup.Alignment.TOP,
          trimHeight: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          content: {
            objectType: LargeContent,
            numBlocks: 3
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(0);
        expect(popup.$container.cssHeight()).toBe(50);
      });

      it('on the center', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.CENTER,
          verticalAlignment: Popup.Alignment.CENTER,
          trimHeight: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          content: {
            objectType: LargeContent,
            numBlocks: 10
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(0);
        expect(popup.$container.cssHeight()).toBe(200);
        expect(popup.$container.cssMarginY()).toBe(20);
      });

      it('on the edge', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.LEFT,
          verticalAlignment: Popup.Alignment.TOPEDGE,
          trimHeight: true,
          verticalSwitch: false,
          $anchor: $anchor,
          windowPaddingY: 0,
          content: {
            objectType: LargeContent,
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

    describe('with vAlign = TOPEDGE', () => {
      it('does not unnecessarily trim if the popup could be displayed completely', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable',
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.TOPEDGE,
          verticalSwitch: true,
          trimHeight: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          content: {
            objectType: LargeContent,
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

  describe('verticalSwitch', () => {
    describe('with verticalAlign = bottom', () => {
      it('switches to top when overlapping bottom window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('switches to top when overlapping bottom window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('switches to top when overlapping bottom window border with dynamic margins', () => {
        // Don't switch
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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
        popup = scout.create(Popup, {
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

    describe('with verticalAlign = top', () => {
      it('switches to bottom when overlapping top window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        $anchor.cssTop(70 - 25);
        let popup = scout.create(Popup, {
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

      it('switches to top when overlapping bottom window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        $anchor.cssTop(70 - 25);
        let popup = scout.create(Popup, {
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

    describe('with verticalAlign = topedge', () => {
      it('switches to bottomedge when overlapping bottom window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable',
          verticalAlignment: Popup.Alignment.TOPEDGE,
          verticalSwitch: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          content: {
            objectType: LargeContent,
            numBlocks: 5
          }
        });
        $desktop.cssHeight(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssTop()).toBe(70 + 80 - 125);
        expect(popup.$container.cssHeight()).toBe(125);
      });

      it('switches to bottomedge when overlapping bottom window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          verticalAlignment: Popup.Alignment.TOPEDGE,
          verticalSwitch: true,
          $anchor: $anchor,
          windowPaddingY: 0,
          content: {
            objectType: LargeContent,
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

  describe('horizontalSwitch', () => {
    describe('with horizontalAlign = right', () => {
      it('switches to left when overlapping right window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('switches to left when overlapping right window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

      it('move popup if it overlaps the desktop bottom without and with horizontal switch', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let desktopHeight = 70 + 80 + 20;
        $desktop.cssHeight(desktopHeight);

        // without switch
        $desktop.cssWidth(70 + 80 + 70);
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.TOPEDGE,
          horizontalSwitch: true,
          verticalSwitch: false,
          $anchor: $anchor,
          withArrow: true,
          scrollType: 'position',
          content: {
            objectType: LargeContent,
            numBlocks: 5
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssWidth()).toBe(25);
        expect(popup.$container.cssHeight()).toBe(5 * 25);
        expect(popup.$container.cssMarginX()).toBe(20);
        expect(popup.$container.cssMarginY()).toBe(20);
        expect(popup.$container.cssLeft()).toBe(70 + 80); // anchor-position + anchor-width
        expect(popup.$container.cssTop()).toBe(desktopHeight - (5 * 25) - (2 * 10)); // desktop-height - popup-height - margin
        popup.close();

        // with switch
        $desktop.cssWidth(70 + 80 + 20);
        popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.RIGHT,
          verticalAlignment: Popup.Alignment.TOPEDGE,
          horizontalSwitch: true,
          verticalSwitch: false,
          $anchor: $anchor,
          scrollType: 'position',
          withArrow: true,
          content: {
            objectType: LargeContent,
            numBlocks: 5
          }
        });
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 - 25 - (2 * 10)); // anchor-position - popup-width - margin
        expect(popup.$container.cssTop()).toBe(desktopHeight - (5 * 25) - (2 * 10)); // desktop-height - popup-height - margin
        popup.close();
      });
    });

    describe('with horizontalAlign = left', () => {
      it('switches to right when overlapping left window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        $anchor.cssLeft(70 - 25);
        let popup = scout.create(Popup, {
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

      it('switches to right when overlapping left window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        $anchor.cssLeft(70 - 25);
        let popup = scout.create(Popup, {
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

    describe('with horizontalAlign = rightedge', () => {
      it('switches to rightedge when overlapping right window border', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable',
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          horizontalSwitch: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          content: {
            objectType: WrappingContent,
            numBlocks: 5
          }
        });
        $desktop.cssWidth(70 + 80 + 25);
        popup.getWindowSize = entryPointSizeFunc;
        popup.open();
        expect(popup.$container.cssLeft()).toBe(70 + 80 - 125);
        expect(popup.$container.cssWidth()).toBe(125);
      });

      it('switches to rightedge when overlapping right window border considering margin', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(WidgetPopup, {
          parent: session.desktop,
          cssClass: 'scalable with-margin',
          horizontalAlignment: Popup.Alignment.LEFTEDGE,
          horizontalSwitch: true,
          $anchor: $anchor,
          windowPaddingX: 0,
          content: {
            objectType: WrappingContent,
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

  describe('withArrow', () => {
    describe('and hAlign LEFT, vAlign CENTER', () => {
      it('opens popup to the left, arrow points to the right into the center of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

    describe('and hAlign RIGHT, vAlign CENTER', () => {
      it('opens popup to the right, arrow points to the left into the center of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

    describe('and hAlign CENTER, vAlign TOP', () => {
      it('opens popup to the top, arrow points to the bottom into the center of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

    describe('and hAlign CENTER, vAlign BOTTOM', () => {
      it('opens popup to the bottom, arrow points to the top into the center of the anchor', () => {
        let $anchor = $desktop.appendDiv('anchor');
        let popup = scout.create(Popup, {
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

  describe('open popup delayed/immediately', () => {

    it('open popup not until parent is rendered and layouted', () => {
      let stringField = scout.create(StringField, {
        parent: session.desktop
      });
      let popup = scout.create(Popup, {
        parent: stringField
      });
      let popupOpen = false;
      session.desktop.on('popupOpen', event => {
        popupOpen = true;
      });
      popup.open();
      expect(popupOpen).toBe(false);
      expect(popup.rendered).toBe(false);
      stringField.render();
      expect(popupOpen).toBe(false);
      expect(popup.rendered).toBe(false);
      stringField.validateLayoutTree();
      expect(popupOpen).toBe(true);
      expect(popup.rendered).toBe(true);
    });

    it('open popup immediately when $parent is provided', () => {
      let stringField = scout.create(StringField, {
        parent: session.desktop
      });
      let popup = scout.create(Popup, {
        parent: stringField
      });
      stringField.render();
      popup.open();
      expect(popup.rendered).toBe(true);
    });

  });
});
