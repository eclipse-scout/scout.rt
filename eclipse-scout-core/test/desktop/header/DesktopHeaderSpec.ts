/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, Menu, scout} from '../../../src/index';
import {FormSpecHelper, OutlineSpecHelper} from '../../../src/testing/index';

describe('DesktopHeader', () => {
  let helper: OutlineSpecHelper, session: SandboxSession, desktop: Desktop, formHelper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        benchVisible: true,
        headerVisible: true
      }
    });
    desktop = session.desktop;
    helper = new OutlineSpecHelper(session);
    formHelper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('onBenchOutlineContentChange', () => {
    let outline, header, bench, model, node0, node1, cssClassChangeHandler;

    beforeEach(() => {
      model = helper.createModelFixture(3, 2, true);
      outline = helper.createOutline(model);
      node0 = outline.nodes[0];
      node0.detailForm = formHelper.createFormWithOneField();
      node0.detailFormVisible = true;
      node0.detailForm.rootGroupBox.menuBar.setMenuItems(scout.create(Menu, {
        parent: node0.detailForm
      }));
      node1 = outline.nodes[1];
      node1.detailForm = formHelper.createFormWithOneField();
      node1.detailFormVisible = true;
      node1.detailForm.rootGroupBox.menuBar.setMenuItems(scout.create(Menu, {
        parent: node1.detailForm
      }));
      bench = desktop.bench;
      header = desktop.header;
      cssClassChangeHandler = header._outlineContentCssClassChangeHandler;
      desktop.setOutline(outline);
    });

    it('attaches listener to new outline content', () => {
      let detailForm0 = node0.detailForm;
      let detailForm0MenuBar = detailForm0.rootGroupBox.menuBar;
      let detailForm1 = node1.detailForm;
      let detailForm1MenuBar = detailForm1.rootGroupBox.menuBar;
      let listenerCount0 = detailForm0.events.count('propertyChange:cssClass', cssClassChangeHandler);
      let menuBarListenerCount0 = detailForm0MenuBar.events.count('propertyChange:visible');
      let listenerCount1 = detailForm1.events.count('propertyChange:cssClass', cssClassChangeHandler);
      let menuBarListenerCount1 = detailForm1MenuBar.events.count('propertyChange:visible');
      outline.selectNodes(node0);
      expect(detailForm0.events.count('propertyChange:cssClass', cssClassChangeHandler)).toBe(listenerCount0 + 1);
      expect(detailForm0MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount0 + 1);
      expect(detailForm1.events.count('propertyChange:cssClass', cssClassChangeHandler)).toBe(listenerCount1);
      expect(detailForm1MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount1);
    });

    it('removes listener from old outline content', () => {
      let detailForm0 = node0.detailForm;
      let detailForm0MenuBar = detailForm0.rootGroupBox.menuBar;
      let detailForm1 = node1.detailForm;
      let detailForm1MenuBar = detailForm1.rootGroupBox.menuBar;
      let listenerCount0 = detailForm0.events.count('propertyChange:cssClass', cssClassChangeHandler);
      let menuBarListenerCount0 = detailForm0MenuBar.events.count('propertyChange:visible');
      let listenerCount1 = detailForm1.events.count('propertyChange:cssClass', cssClassChangeHandler);
      let menuBarListenerCount1 = detailForm1MenuBar.events.count('propertyChange:visible');
      outline.selectNodes(node0);
      expect(detailForm0.events.count('propertyChange:cssClass', cssClassChangeHandler)).toBe(listenerCount0 + 1);
      expect(detailForm0MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount0 + 1);
      expect(detailForm1.events.count('propertyChange:cssClass', cssClassChangeHandler)).toBe(listenerCount1);
      expect(detailForm1MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount1);

      outline.selectNodes(node1);
      expect(detailForm0.events.count('propertyChange:cssClass', cssClassChangeHandler)).toBe(listenerCount0);
      expect(detailForm0MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount0);
      expect(detailForm1.events.count('propertyChange:cssClass', cssClassChangeHandler)).toBe(listenerCount1 + 1);
      expect(detailForm1MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount1 + 1);

      outline.selectNodes(node0);
      expect(detailForm0.events.count('propertyChange:cssClass', cssClassChangeHandler)).toBe(listenerCount0 + 1);
      expect(detailForm0MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount0 + 1);
      expect(detailForm1.events.count('propertyChange:cssClass', cssClassChangeHandler)).toBe(listenerCount1);
      expect(detailForm1MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount1);
    });

    it('removes listener when getting removed', () => {
      let detailForm0MenuBar = node0.detailForm.rootGroupBox.menuBar;
      let menuBarListenerCount0 = detailForm0MenuBar.events.count('propertyChange:visible');
      outline.selectNodes(node0);
      expect(detailForm0MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount0 + 1);

      desktop.setHeaderVisible(false);
      expect(detailForm0MenuBar.events.count('propertyChange:visible')).toBe(menuBarListenerCount0);
    });

  });

  describe('aria properties', () => {

    it('has aria role banner', () => {
      expect(desktop.header.$container).toHaveAttr('role', 'banner');
    });

    it('has aria-label set', () => {
      expect(desktop.header.$container.attr('aria-label')).toBeTruthy();
      expect(desktop.header.$container.attr('aria-labelledby')).toBeFalsy();
    });
  });
});
