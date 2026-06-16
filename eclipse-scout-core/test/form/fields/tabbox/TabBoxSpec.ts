/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, keys, scout, StringField, TabBox, TabBoxModel, TabItem} from '../../../../src/index';
import {JQueryTesting, TabBoxSpecHelper} from '../../../../src/testing/index';

describe('TabBox', () => {
  let session: SandboxSession;
  let helper: TabBoxSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TabBoxSpecHelper(session);
  });

  describe('render', () => {
    let tabBox: TabBox;

    beforeEach(() => {
      let tabItem = helper.createTabItem();
      tabBox = helper.createTabBoxWith([tabItem]);
    });

    it('does NOT call layout for the selected tab on initialization', () => {
      spyOn(session.layoutValidator, 'invalidateTree').and.callThrough();
      tabBox.render();
      expect(session.layoutValidator.invalidateTree).not.toHaveBeenCalled();
    });

    it('must not create LogicalGridData for tab items', () => {
      tabBox.render();
      expect(tabBox.tabItems[0].htmlComp.layoutData).toBe(null);
    });

  });

  describe('remove', () => {

    it('does not fail if there was no selected tab', () => {
      let tabBox = scout.create(TabBox, {parent: session.desktop});
      tabBox.render();
      tabBox.remove();
      expect().nothing();
    });

  });

  describe('selection', () => {

    it('should select tabs by ID', () => {
      let tabItemA = helper.createTabItem({
        id: 'Foo'
      });
      let tabItemB = helper.createTabItem({
        id: 'Bar'
      });
      let tabBox = helper.createTabBoxWith([tabItemA, tabItemB]);
      tabBox.setSelectedTab('Foo');
      expect(tabBox.selectedTab).toBe(tabItemA);
      tabBox.setSelectedTab('Bar');
      expect(tabBox.selectedTab).toBe(tabItemB);
    });

  });

  describe('key handling', () => {

    it('supports left/right keys to select a tab-item', () => {
      let tabItemA = helper.createTabItem({
        label: 'tab 01'
      });
      let tabItemB = helper.createTabItem({
        label: 'tab 02'
      });
      let tabBox = helper.createTabBoxWith([tabItemA, tabItemB]);
      tabBox.render();

      tabItemA.focus();
      // check right/left keys
      expect(tabBox.selectedTab).toBe(tabItemA);
      JQueryTesting.triggerKeyDownCapture(tabBox.header.tabArea.$container, keys.RIGHT);
      expect(tabBox.selectedTab).toBe(tabItemB);
      JQueryTesting.triggerKeyDownCapture(tabBox.header.tabArea.$container, keys.LEFT);
      expect(tabBox.selectedTab).toBe(tabItemA);

      // make sure that nothing happens when first or last tab is selected and left/right is pressed
      tabBox.setSelectedTab(tabItemA);
      JQueryTesting.triggerKeyDownCapture(tabBox.header.tabArea.$container, keys.LEFT);
      expect(tabBox.selectedTab).toBe(tabItemA); // still A

      tabBox.setSelectedTab(tabItemB);
      JQueryTesting.triggerKeyDownCapture(tabBox.header.tabArea.$container, keys.RIGHT);
      expect(tabBox.selectedTab).toBe(tabItemB); // still B
    });

  });

  describe('first class', () => {
    let tabBox;

    beforeEach(() => {
      tabBox = scout.create(TabBox, {
        parent: session.desktop,
        tabItems: [{
          objectType: TabItem,
          label: 'first'
        }, {
          objectType: TabItem,
          label: 'second'
        }]
      });
      // set the tab-item to inline-block to ensure correct width calculation (e.g. PhantomJS)
      $('<style>' +
        '.tab-item { display: inline-block;}' +
        '</style>').appendTo($('#sandbox'));
    });

    it('is added to the first tab item', () => {
      tabBox.render();
      tabBox.validateLayout();
      expect(tabBox.header.tabArea.tabs[0].$container).toHaveClass('first');
      expect(tabBox.header.tabArea.tabs[1].$container).not.toHaveClass('first');
    });

    it('is added to the first visible tab item', () => {
      tabBox = scout.create(TabBox, {
        parent: session.desktop,
        tabItems: [{
          objectType: TabItem,
          label: 'first',
          visible: false
        }, {
          objectType: TabItem,
          label: 'second'
        }, {
          objectType: TabItem,
          label: 'third'
        }]
      });
      tabBox.render();
      tabBox.validateLayout();
      expect(tabBox.header.tabArea.tabs[0].$container.isVisible()).toBe(false);
      expect(tabBox.header.tabArea.tabs[1].$container).toHaveClass('first');
      expect(tabBox.header.tabArea.tabs[2].$container).not.toHaveClass('first');
    });

    it('is correctly updated when visibility changes', () => {
      tabBox.render();
      tabBox.validateLayout();
      tabBox.tabItems[0].setVisible(false);
      tabBox.validateLayout();
      expect(tabBox.header.tabArea.tabs[0].$container.isVisible()).toBe(false);
      expect(tabBox.header.tabArea.tabs[1].$container).toHaveClass('first');
    });

  });

  describe('aria properties', () => {
    let tabBox: TabBox, tabItem1: TabItem, tabItem2: TabItem;

    beforeEach(() => {
      tabItem1 = helper.createTabItem();
      tabItem2 = helper.createTabItem();
      tabBox = helper.createTabBoxWith([tabItem1, tabItem2]);
    });

    it('has aria role tablist', () => {
      tabBox.render();
      expect(tabBox.header.tabArea.$container).toHaveAttr('role', 'tablist');
    });

    it('has a content area with aria role tabpanel', () => {
      tabBox.render();
      expect(tabBox._$tabContent).toHaveAttr('role', 'tabpanel');
    });

    it('has a content area with aria-labelledby pointing to selected tab', () => {
      tabBox.render();
      let selectedId = tabBox.selectedTab.getTab().$container.attr('id');
      expect(tabBox._$tabContent.attr('aria-labelledby')).toBe(selectedId);

      tabBox.setSelectedTab(tabItem2);
      selectedId = tabBox.selectedTab.getTab().$container.attr('id');
      expect(tabBox._$tabContent.attr('aria-labelledby')).toBe(selectedId);

      tabBox.setSelectedTab(tabItem1);
      selectedId = tabBox.selectedTab.getTab().$container.attr('id');
      expect(tabBox._$tabContent.attr('aria-labelledby')).toBe(selectedId);

      tabBox.setSelectedTab(null);
      expect(tabBox._$tabContent).not.toHaveAttr('aria-labelledby');
    });

    it('has tabs with aria role tab', () => {
      tabBox.render();
      tabBox.header.tabArea.tabs.forEach(tab => {
        expect(tab.$container).toHaveAttr('role', 'tab');
      });

      tabBox.insertTabItem({objectType: TabItem});
      expect(arrays.last(tabBox.header.tabArea.tabs).$container).toHaveAttr('role', 'tab');
    });

    it('has tabs with aria-controls pointing to content', () => {
      tabBox.render();

      let panelId = tabBox._$tabContent.attr('id');
      tabBox.header.tabArea.tabs.forEach(tab => {
        expect(tab.$container).toHaveAttr('aria-controls', panelId);
      });

      tabBox.insertTabItem({objectType: TabItem});
      expect(arrays.last(tabBox.header.tabArea.tabs).$container).toHaveAttr('aria-controls', panelId);
    });

    it('has selected tab with aria-selected property set to true', () => {
      tabBox.render();
      expect(tabItem1.getTab().$container).toHaveAttr('aria-selected', 'true');
      expect(tabItem2.getTab().$container.attr('aria-selected')).toBeFalsy();

      tabBox.setSelectedTab(tabItem2);
      expect(tabItem1.getTab().$container.attr('aria-selected')).toBeFalsy();
      expect(tabItem2.getTab().$container).toHaveAttr('aria-selected', 'true');

      tabBox.setSelectedTab(tabItem1);
      expect(tabItem1.getTab().$container).toHaveAttr('aria-selected', 'true');
      expect(tabItem2.getTab().$container.attr('aria-selected')).toBeFalsy();

      tabBox.setSelectedTab(null);
      expect(tabItem1.getTab().$container.attr('aria-selected')).toBeFalsy();
      expect(tabItem2.getTab().$container.attr('aria-selected')).toBeFalsy();
    });
  });

  describe('mark strategy', () => {
    function createTabBox(model?: TabBoxModel): TabBox {
      return scout.create(TabBox, {
        parent: session.desktop,
        tabItems: [{
          objectType: TabItem,
          fields: [{
            objectType: StringField
          }, {
            objectType: StringField
          }]
        }, {
          objectType: TabItem,
          fields: [{
            objectType: StringField
          }, {
            objectType: StringField
          }]
        }],
        ...model
      });
    }

    it('can be set initially', () => {
      let tabBox = scout.create(TabBox, {
        parent: session.desktop
      });
      expect(tabBox.markStrategy).toBe(TabBox.MarkStrategy.NOT_EMPTY);

      tabBox = scout.create(TabBox, {
        parent: session.desktop,
        markStrategy: TabBox.MarkStrategy.SAVE_NEEDED
      });
      expect(tabBox.markStrategy).toBe(TabBox.MarkStrategy.SAVE_NEEDED);

      tabBox = scout.create(TabBox, {
        parent: session.desktop,
        markStrategy: null
      });
      expect(tabBox.markStrategy).toBe(null);
    });

    it('updates marked state if strategy changes', () => {
      let tabBox = createTabBox();
      expect(tabBox.tabItems[0].marked).toBeFalse();

      (tabBox.tabItems[0].fields[0] as StringField).setValue('value 2');
      expect(tabBox.tabItems[0].marked).toBeTrue();

      tabBox.setMarkStrategy(TabBox.MarkStrategy.SAVE_NEEDED);
      expect(tabBox.tabItems[0].marked).toBeTrue();

      tabBox.tabItems[0].fields[0].markAsSaved();
      expect(tabBox.tabItems[0].marked).toBeFalse();

      tabBox.setMarkStrategy(TabBox.MarkStrategy.NOT_EMPTY);
      expect(tabBox.tabItems[0].marked).toBeTrue();

      tabBox.setMarkStrategy(null);
      expect(tabBox.tabItems[0].marked).toBeTrue(); // null means it does not change the marked state so it can be set manually (e.g. by Scout Classic)
    });

    describe('notEmpty', () => {
      it('marks tab if at least one field is not empty', () => {
        let tabBox = scout.create(TabBox, {
          parent: session.desktop,
          tabItems: [{
            objectType: TabItem,
            fields: [{
              objectType: StringField
            }, {
              objectType: StringField
            }]
          }, {
            objectType: TabItem,
            fields: [{
              objectType: StringField
            }, {
              objectType: StringField,
              value: 'value'
            }]
          }]
        });
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeTrue();
      });

      it('marks tab item if it changes to non-empty', () => {
        let tabBox = createTabBox();
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        (tabBox.tabItems[0].fields[0] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        (tabBox.tabItems[1].fields[1] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        (tabBox.tabItems[1].fields[0] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        (tabBox.tabItems[1].fields[1] as StringField).setValue(null);
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        (tabBox.tabItems[1].fields[0] as StringField).setValue(null);
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        (tabBox.tabItems[0].fields[0] as StringField).setValue(null);
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();
      });

      it('removes marking if non-empty fields are deleted', () => {
        let tabBox = createTabBox();
        let stringField = tabBox.tabItems[0].fields[0] as StringField;
        stringField.setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        tabBox.tabItems[0].deleteField(stringField);
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();
      });

      it('adds marking if a non-empty field is added', () => {
        let tabBox = createTabBox();
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        tabBox.tabItems[0].insertField({
          objectType: StringField,
          value: 'a value'
        });
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeFalse();
      });

      it('updates marking if a tab item is added', () => {
        let tabBox = createTabBox();
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        let tabItem = scout.create(TabItem, {
          parent: tabBox,
          fields: [{
            objectType: StringField
          }]
        });
        (tabItem.fields[0] as StringField).setValue('value');
        expect(tabItem.empty).toBe(false);

        tabBox.insertTabItem(tabItem);
        expect(tabItem.marked).toBe(true);

        tabItem.setOwner(session.desktop);
        tabBox.deleteTabItem(tabItem);

        (tabItem.fields[0] as StringField).setValue(null);
        expect(tabItem.empty).toBe(true);

        tabBox.insertTabItem(tabItem);
        expect(tabItem.marked).toBe(false);
      });

      it('ignores visibility of fields', () => {
        // One could argue that a tab item must not be marked if it only contains invisible non-empty fields
        // But: we do not know whether the invisible field is not relevant or just invisible to improve usability, e.g. hidden to save space or reduce complexity
        let tabBox = scout.create(TabBox, {
          parent: session.desktop,
          tabItems: [{
            objectType: TabItem,
            fields: [{
              objectType: StringField
            }, {
              objectType: StringField
            }]
          }, {
            objectType: TabItem,
            fields: [{
              objectType: StringField
            }, {
              objectType: StringField,
              value: 'value',
              visible: false
            }]
          }]
        });
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        tabBox.tabItems[1].fields[1].setVisible(true);
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        (tabBox.tabItems[1].fields[0] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        tabBox.tabItems[1].fields[1].setVisible(false);
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        tabBox.tabItems[1].fields[0].setVisible(false);
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeTrue();
      });
    });

    describe('saveNeeded', () => {
      it('marks tab item if at least one field requires saving', () => {
        let tabBox = createTabBox({markStrategy: TabBox.MarkStrategy.SAVE_NEEDED});
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        (tabBox.tabItems[0].fields[0] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        (tabBox.tabItems[1].fields[1] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        (tabBox.tabItems[1].fields[0] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        (tabBox.tabItems[1].fields[1] as StringField).markAsSaved();
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        (tabBox.tabItems[1].fields[0] as StringField).markAsSaved();
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        tabBox.tabItems[0].markAsSaved();
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        (tabBox.tabItems[0].fields[0] as StringField).setValue('value 2');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        (tabBox.tabItems[0].fields[0] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();
      });

      it('does not mark tab if a field has a value initially', () => {
        let tabBox = scout.create(TabBox, {
          parent: session.desktop,
          markStrategy: TabBox.MarkStrategy.SAVE_NEEDED,
          tabItems: [{
            objectType: TabItem,
            fields: [{
              objectType: StringField
            }, {
              objectType: StringField
            }]
          }, {
            objectType: TabItem,
            fields: [{
              objectType: StringField
            }, {
              objectType: StringField,
              value: 'value'
            }]
          }]
        });
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();
      });

      it('removes marking if saveNeeded fields are deleted', () => {
        let tabBox = createTabBox({markStrategy: TabBox.MarkStrategy.SAVE_NEEDED});
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        let stringField = tabBox.tabItems[0].fields[0] as StringField;
        stringField.setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        tabBox.tabItems[0].deleteField(stringField);
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();
      });

      it('adds marking if a saveNeeded field is added', () => {
        let tabBox = createTabBox({markStrategy: TabBox.MarkStrategy.SAVE_NEEDED});
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        let stringField = scout.create(StringField, {parent: tabBox.tabItems[0]});
        stringField.setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeFalse();
      });

      it('updates marking if a tab item is added', () => {
        let tabBox = createTabBox({markStrategy: TabBox.MarkStrategy.SAVE_NEEDED});
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeFalse();

        let tabItem = scout.create(TabItem, {
          parent: tabBox,
          fields: [{
            objectType: StringField
          }]
        });
        (tabItem.fields[0] as StringField).setValue('value');
        expect(tabItem.empty).toBe(false);

        tabBox.insertTabItem(tabItem);
        expect(tabItem.marked).toBe(true);

        tabItem.setOwner(session.desktop);
        tabBox.deleteTabItem(tabItem);

        (tabItem.fields[0] as StringField).setValue(null);
        expect(tabItem.empty).toBe(true);

        tabBox.insertTabItem(tabItem);
        expect(tabItem.marked).toBe(false);
      });
    });

    describe('null', () => {
      it('does not mark tabs at all', () => {
        let tabBox = createTabBox({markStrategy: null});
        expect(tabBox.tabItems[0].marked).toBeFalse();

        (tabBox.tabItems[0].fields[0] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeFalse();
      });

      it('does not change existing mark states', () => {
        let tabBox = scout.create(TabBox, {
          parent: session.desktop,
          markStrategy: null,
          tabItems: [{
            objectType: TabItem,
            marked: true,
            fields: [{
              objectType: StringField
            }, {
              objectType: StringField
            }]
          }, {
            objectType: TabItem,
            marked: true,
            fields: [{
              objectType: StringField
            }, {
              objectType: StringField,
              value: 'value'
            }]
          }]
        });
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        (tabBox.tabItems[1].fields[0] as StringField).setValue('value');
        expect(tabBox.tabItems[0].marked).toBeTrue();
        expect(tabBox.tabItems[1].marked).toBeTrue();

        tabBox.tabItems[0].setMarked(false);
        expect(tabBox.tabItems[0].marked).toBeFalse();
        expect(tabBox.tabItems[1].marked).toBeTrue();
      });
    });
  });
});
