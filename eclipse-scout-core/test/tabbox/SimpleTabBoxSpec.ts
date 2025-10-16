/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GroupBox, ObjectIdProvider, scout, SimpleTab, SimpleTabArea, SimpleTabBox} from '../../src/index';

describe('SimpleTabBox', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  it('renders the tabs in the correct order', () => {
    let tabBox = scout.create(SimpleTabBox, {
      parent: session.desktop
    });
    let view1 = scout.create(GroupBox, {
      parent: tabBox,
      title: 'One'
    });
    let view2 = scout.create(GroupBox, {
      parent: tabBox,
      title: 'Two'
    });
    let view3 = scout.create(GroupBox, {
      parent: tabBox,
      title: 'Three'
    });
    tabBox.addView(view1);
    tabBox.addView(view2, false);
    tabBox.addView(view3, false);

    expect(tabBox.tabArea.tabs.length).toBe(3);
    expect(tabBox.tabArea.tabs[0].view).toBe(view1);
    expect(tabBox.tabArea.tabs[1].view).toBe(view2);
    expect(tabBox.tabArea.tabs[2].view).toBe(view3);
    expect(tabBox.tabArea.tabs[0].title).toBe('One');
    expect(tabBox.tabArea.tabs[1].title).toBe('Two');
    expect(tabBox.tabArea.tabs[2].title).toBe('Three');
    expect(tabBox.tabArea.tabs[0].selected).toBe(true);
    expect(tabBox.tabArea.tabs[1].selected).toBe(false);
    expect(tabBox.tabArea.tabs[2].selected).toBe(false);

    // -----

    tabBox.render();
    expect(tabBox.$tabArea).toBeTruthy();
    expect(tabBox.$tabArea.children().length).toBe(3);
    expect(tabBox.$tabArea.children().eq(0)).toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(0).text().trim()).toBe('One');
    expect(tabBox.$tabArea.children().eq(1)).not.toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(1).text().trim()).toBe('Two');
    expect(tabBox.$tabArea.children().eq(2)).not.toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(2).text().trim()).toBe('Three');

    tabBox.remove();
    expect(tabBox.$tabArea).not.toBeTruthy;

    tabBox.render();
    expect(tabBox.$tabArea).toBeTruthy();
    expect(tabBox.$tabArea.children().length).toBe(3);
    expect(tabBox.$tabArea.children().eq(0)).toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(0).text().trim()).toBe('One');
    expect(tabBox.$tabArea.children().eq(1)).not.toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(1).text().trim()).toBe('Two');
    expect(tabBox.$tabArea.children().eq(2)).not.toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(2).text().trim()).toBe('Three');
  });

  describe('uuid', () => {
    it('is set to the tab', () => {
      let tabArea = scout.create(SimpleTabArea, {
        parent: session.desktop,
        tabs: [{
          objectType: SimpleTab,
          uuid: '1'
        }, {
          objectType: SimpleTab,
          classId: '2'
        }]
      });
      tabArea.render();
      expect(tabArea.tabs[0].buildUuid()).toBe('1');
      expect(tabArea.tabs[1].buildUuid()).toBe('2');
    });

    it('is taken from view and prefixed', () => {
      let view1 = scout.create(GroupBox, {
        parent: session.desktop,
        uuid: 'one'
      });
      let view2 = scout.create(GroupBox, {
        parent: session.desktop,
        classId: 'two'
      });
      let view3 = scout.create(GroupBox, {
        parent: session.desktop,
        classId: 'three'
      });
      let tabArea = scout.create(SimpleTabArea, {
        parent: session.desktop,
        tabs: [{
          objectType: SimpleTab,
          view: view1
        }, {
          objectType: SimpleTab,
          view: view2
        }, {
          objectType: SimpleTab,
          view: view3,
          uuid: '3'
        }]
      });
      tabArea.render();
      expect(tabArea.tabs[0].uuid).toBe(`tab${ObjectIdProvider.DEPENDENT_UUID_DELIMITER}one`);
      expect(tabArea.tabs[1].uuid).toBe(`tab${ObjectIdProvider.DEPENDENT_UUID_DELIMITER}two`);
      expect(tabArea.tabs[2].uuid).toBe('3'); // Expect that explicit id is not overridden
    });
  });

  describe('aria properties', () => {
    let tabBox: SimpleTabBox;

    beforeEach(() => {
      tabBox = scout.create(SimpleTabBox, {
        parent: session.desktop
      });
      let view1 = scout.create(GroupBox, {
        parent: tabBox,
        title: 'One'
      });
      let view2 = scout.create(GroupBox, {
        parent: tabBox,
        title: 'Two'
      });
      tabBox.addView(view1);
      tabBox.addView(view2, false);
      tabBox.render();
    });

    it('tab area has aria role tablist', () => {
      expect(tabBox.tabArea.$container).toHaveAttr('role', 'tablist');
    });

    it('tab area has aria orientation vertical if position is left or right', () => {
      tabBox = scout.create(SimpleTabBox, {
        parent: session.desktop,
        tabArea: {
          objectType: SimpleTabArea,
          position: SimpleTabArea.Position.LEFT
        }
      });
      tabBox.render();
      expect(tabBox.tabArea.$container).toHaveAttr('aria-orientation', 'vertical');

      tabBox.tabArea.setPosition(SimpleTabArea.Position.BOTTOM);
      expect(tabBox.tabArea.$container).not.toHaveAttr('aria-orientation');

      tabBox.tabArea.setPosition(SimpleTabArea.Position.RIGHT);
      expect(tabBox.tabArea.$container).toHaveAttr('aria-orientation', 'vertical');

      tabBox.tabArea.setPosition(SimpleTabArea.Position.TOP);
      expect(tabBox.tabArea.$container).not.toHaveAttr('aria-orientation');
    });

    it('tab content has aria role tabpanel', () => {
      expect(tabBox.$viewContent).toHaveAttr('role', 'tabpanel');
    });

    it('tabs have aria role tab', () => {
      tabBox.tabArea.tabs.forEach(tab => {
        expect(tab.$container).toHaveAttr('role', 'tab');
      });
    });

    it('selected tab has aria-selected property set to true', () => {
      expect(tabBox.tabArea.tabs[0].$container).toHaveAttr('aria-selected', 'true');
      expect(tabBox.tabArea.tabs[1].$container).not.toHaveAttr('aria-selected');

      tabBox.activateView(tabBox.getViews()[1]);
      expect(tabBox.tabArea.tabs[0].$container).not.toHaveAttr('aria-selected');
      expect(tabBox.tabArea.tabs[1].$container).toHaveAttr('aria-selected', 'true');

      tabBox.activateView(null);
      expect(tabBox.tabArea.tabs[0].$container).not.toHaveAttr('aria-selected');
      expect(tabBox.tabArea.tabs[1].$container).not.toHaveAttr('aria-selected');
    });
  });
});
