/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, Form, GroupBox, MessageBox, ObjectOrModel, objects, ObjectUuidProvider, OutlineOverview, Page, PageWithNodes, PageWithTable, scout, Status, Table, TableRow, TileOutlineOverview, Tree, TreeField} from '../../../src/index';
import {FormSpecHelper, JQueryTesting, MenuSpecHelper, OutlineSpecHelper, TreeSpecHelper} from '../../../src/testing/index';

describe('Outline', () => {
  let helper: OutlineSpecHelper;
  let menuHelper: MenuSpecHelper;
  let formHelper: FormSpecHelper;
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new OutlineSpecHelper(session);
    menuHelper = new MenuSpecHelper(session);
    formHelper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('collapsing', () => {
    // Regression test for erroneous behavior of MessageBoxController
    it('still allows a messagebox to be shown', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      session.desktop.outline = outline;

      let model = {
        session: session,
        parent: session.desktop,
        severity: Status.Severity.ERROR
      };
      let messageBox = scout.create(MessageBox, model);

      // This collapses the registered outline
      session.desktop.setNavigationVisible(false);

      messageBox.setDisplayParent(outline);
      outline.messageBoxController.registerAndRender(messageBox);

      expect(messageBox.rendered).toBe(true);
    });
  });

  describe('dispose', () => {
    let model, tree, node0;

    beforeEach(() => {
      // A large tree is used to properly test recursion
      model = helper.createModelFixture(3, 2, true);
      tree = helper.createOutline(model);
      node0 = tree.nodes[0];
    });

    it('calls onNodeDeleted for every node to be able to cleanup', () => {
      spyOn(tree, '_onNodeDeleted');
      tree.destroy();
      expect(tree._onNodeDeleted.calls.count()).toBe(39);
    });

    it('calls onNodeDeleted for every node (which was not already deleted before) to be able to cleanup', () => {
      spyOn(tree, '_onNodeDeleted');

      tree.deleteNodes([node0]);
      expect(tree._onNodeDeleted.calls.count()).toBe(13);

      tree._onNodeDeleted.calls.reset();
      tree.destroy();
      expect(tree._onNodeDeleted.calls.count()).toBe(26);
    });

  });

  describe('deleteNodes', () => {
    let model, tree, node0;

    beforeEach(() => {
      // A large tree is used to properly test recursion
      model = helper.createModelFixture(3, 2, true);
      tree = helper.createOutline(model);
      node0 = tree.nodes[0];
    });

    it('calls onNodeDeleted for every node to be able to cleanup', () => {
      spyOn(tree, '_onNodeDeleted');

      tree.deleteNodes([node0]);
      expect(tree._onNodeDeleted.calls.count()).toBe(13);
    });

  });

  describe('deleteAllChildNodes', () => {
    let model, tree;

    beforeEach(() => {
      // A large tree is used to properly test recursion
      model = helper.createModelFixture(3, 2, true);
      tree = helper.createOutline(model);
    });

    it('calls onNodeDeleted for every node to be able to cleanup', () => {
      spyOn(tree, '_onNodeDeleted');

      tree.deleteAllChildNodes();
      expect(tree._onNodeDeleted.calls.count()).toBe(39);
      expect(objects.countOwnProperties(tree.nodesMap)).toBe(0);
    });

  });

  describe('navigateToTop', () => {

    it('collapses all nodes in bread crumb mode', () => {
      let model = helper.createModelFixture(1, 1);
      let tree = helper.createOutline(model);
      let node0 = tree.nodes[0];

      tree.displayStyle = Tree.DisplayStyle.BREADCRUMB;
      tree.render();

      tree.selectNodes(node0);

      expect(tree.selectedNodes.indexOf(node0) > -1).toBe(true);
      expect(node0.expanded).toBe(true);

      tree.navigateToTop();

      expect(tree.selectedNodes.length).toBe(0);
      expect(node0.expanded).toBe(false);
    });
  });

  describe('selectNodes', () => {
    let model, outline, node;

    beforeEach(() => {
      model = helper.createModelFixture(3, 2, true);
      outline = helper.createOutline(model);
      node = outline.nodes[0];
    });

    it('handle navigateUp only once', () => {
      outline.selectNodes(node);
      outline.navigateUpInProgress = true;
      outline.selectNodes([]);
      expect(outline.navigateUpInProgress).toBe(false);
    });

    // we must override the _render* methods for this test-case, since we had to
    // implement a lot more of set-up code to make these methods work.
    it('otherwise handle single selection (or do nothing when selection is != 1 node)', () => {
      node.detailFormVisibleByUi = false;
      outline.navigateUpInProgress = false;
      outline._renderSelection = () => {
        // nop
      };
      outline._renderMenus = () => {
        // nop
      };

      // don't change the visibleByUi flag when selection is != 1
      outline.selectNodes([]);
      expect(node.detailFormVisibleByUi).toBe(false);

      // set the visibleByUi flag to true when selection is exactly 1
      outline.selectNodes([node]);
      expect(node.detailFormVisibleByUi).toBe(true);
    });

    it('can only select 1 node', () => {
      const node1 = outline.nodes[1];

      expect(outline.selectedNodes).toEqual([]);

      outline.selectNodes([node]);
      expect(outline.selectedNodes).toEqual([node]);

      outline.selectNodes([node1]);
      expect(outline.selectedNodes).toEqual([node1]);

      outline.selectNodes([node, node1]);
      expect(outline.selectedNodes).toEqual([node]);

      outline.selectNodes([node1, node]);
      expect(outline.selectedNodes).toEqual([node1]);

      outline.selectNodes();
      expect(outline.selectedNodes).toEqual([]);
    });
  });

  describe('updateDetailMenus', () => {

    it('adds the empty space menus of the detail table to the detail menu bar', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      helper.setMobileFlags(outline);
      let node0 = outline.nodes[0];
      node0.detailTable.setMenus([
        menuHelper.createMenu({
          menuTypes: [Table.MenuType.SingleSelection]
        }), menuHelper.createMenu()
      ]);
      expect(outline.detailMenuBarVisible).toBe(false);
      expect(outline.detailMenuBar.menuItems.length).toBe(0);

      outline.selectNodes(node0);
      expect(outline.detailMenuBarVisible).toBe(true);
      expect(outline.detailMenuBar.menuItems.length).toBe(1);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[1]);
    });

    it('adds the single selection menus of the parent detail table to the detail menu bar', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      helper.setMobileFlags(outline);
      let node0 = outline.nodes[0];
      node0.detailTable.setMenus([
        menuHelper.createMenu({
          menuTypes: [Table.MenuType.SingleSelection]
        }), menuHelper.createMenu()
      ]);
      expect(outline.detailMenuBarVisible).toBe(false);
      expect(outline.detailMenuBar.menuItems.length).toBe(0);

      outline.selectNodes(node0.childNodes[0]);
      expect(outline.detailMenuBarVisible).toBe(true);
      expect(outline.detailMenuBar.menuItems.length).toBe(1);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[0]);
    });

    it('attaches a listener to the detail table to get dynamic menu changes', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      helper.setMobileFlags(outline);
      let node0 = outline.nodes[0];
      expect(outline.detailMenuBarVisible).toBe(false);
      expect(outline.detailMenuBar.menuItems.length).toBe(0);

      outline.selectNodes(node0);
      expect(outline.detailMenuBarVisible).toBe(false);
      expect(outline.detailMenuBar.menuItems.length).toBe(0);

      // Menus change on table -> detail menu bar needs to be updated as well
      let menu = menuHelper.createModel('menu', '');
      node0.detailTable.setMenus([menu]);
      expect(outline.detailMenuBarVisible).toBe(true);
      expect(outline.detailMenuBar.menuItems.length).toBe(1);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[0]);
    });

    it('removes the listener from the detail tables on selection changes and destroy', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      outline.setNavigateButtonsVisible(false); // hide for this test, so their listeners won't interfere
      helper.setMobileFlags(outline);
      let node0 = outline.nodes[0];
      let node1 = outline.nodes[1];
      // @ts-expect-error
      let eventListeners = node0.detailTable.events._eventListeners;
      let initialListenerCount = eventListeners.length;

      outline.selectNodes(node0);
      let selectionListenerCount = eventListeners.length;
      expect(selectionListenerCount).toBe(initialListenerCount + 2); // destroy and propertyChange listener

      outline.selectNodes(node1);
      selectionListenerCount = eventListeners.length;
      expect(selectionListenerCount).toBe(initialListenerCount); // listeners removed

      outline.selectNodes(node0);
      selectionListenerCount = eventListeners.length;
      expect(selectionListenerCount).toBe(initialListenerCount + 2); // listeners attached again

      outline.nodes[0].detailTable.destroy();
      expect(eventListeners.length).toBe(0); // every listener should be removed now
    });

    it('makes sure table does not update the menu parent for empty space menus', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      helper.setMobileFlags(outline);
      outline.render();
      let node0 = outline.nodes[0];
      let emptySpaceMenu = menuHelper.createMenu();
      let emptySpaceMenu2 = menuHelper.createMenu();
      node0.detailTable.setMenus([emptySpaceMenu]);

      // Select node -> empty space menus are active
      outline.selectNodes(node0);
      expect(outline.detailMenuBar.menuItems.length).toBe(1);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[0]);
      expect(outline.detailMenuBar.menuItems[0].parent).toBe(outline.detailMenuBar.menuboxLeft);

      node0.detailTable.setMenus([emptySpaceMenu, emptySpaceMenu2]);
      expect(outline.detailMenuBar.menuItems.length).toBe(2);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[0]);
      expect(outline.detailMenuBar.menuItems[0].parent).toBe(outline.detailMenuBar.menuboxLeft);
      expect(outline.detailMenuBar.menuItems[1]).toBe(node0.detailTable.menus[1]);
      expect(outline.detailMenuBar.menuItems[1].parent).toBe(outline.detailMenuBar.menuboxLeft);

      // MenuBarLayout would throw an exception if parent is table, because table is not rendered and therefore the menu item is not rendered as well
      outline.validateLayout();
    });

    it('makes sure table does not update the menu parent for single selection menus', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      helper.setMobileFlags(outline);
      outline.render();
      let node0 = outline.nodes[0];
      // Select child node -> single selection menus are active
      // A row is necessary to make sure single selection menus are not filtered out
      node0.detailTable.insertRow({
        cells: [null, null]
      });
      node0.detailTable.selectRow(node0.detailTable.rows[0]);
      outline.selectNodes(node0.childNodes[0]);
      expect(outline.detailMenuBar.menuItems.length).toBe(0);

      let singleSelectionMenu = menuHelper.createMenu({
        menuTypes: [Table.MenuType.SingleSelection]
      });
      let singleSelectionMenu2 = menuHelper.createMenu({
        menuTypes: [Table.MenuType.SingleSelection]
      });
      node0.detailTable.setMenus([singleSelectionMenu, singleSelectionMenu2]);
      expect(outline.detailMenuBar.menuItems.length).toBe(2);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[0]);
      expect(outline.detailMenuBar.menuItems[0].parent).toBe(outline.detailMenuBar.menuboxLeft);
      expect(outline.detailMenuBar.menuItems[1]).toBe(node0.detailTable.menus[1]);
      expect(outline.detailMenuBar.menuItems[1].parent).toBe(outline.detailMenuBar.menuboxLeft);

      // MenuBarLayout would throw an exception if parent is table, because table is not rendered and therefore the menu items are not rendered as well
      outline.validateLayout();
    });

    it('makes sure group box does not update the menu parent if menu visibility changes', () => {
      let outline = helper.createOutlineWithOneDetailForm();
      helper.setMobileFlags(outline);
      outline.render();
      let node0 = outline.nodes[0];
      let menu0 = menuHelper.createMenu({
        horizontalAlignment: 1,
        visible: false
      });
      let menu1 = menuHelper.createMenu({
        horizontalAlignment: 1
      });
      node0.detailForm.rootGroupBox.setMenus([menu0, menu1]);

      outline.selectNodes(node0);
      expect(outline.nodeMenuBar.menuItems.length).toBe(2);
      expect(outline.nodeMenuBar.menuItems[0]).toBe(menu0);
      expect(outline.nodeMenuBar.menuItems[0].parent).toBe(outline.nodeMenuBar.menuboxRight);
      expect(outline.nodeMenuBar.menuItems[1]).toBe(menu1);
      expect(outline.nodeMenuBar.menuItems[1].parent).toBe(outline.nodeMenuBar.menuboxRight);

      menu0.setVisible(true);
      // GroupBox MenuBar must not contain the menu items because they were moved to the nodeMenuBar
      expect(node0.detailForm.rootGroupBox.menuBar.menuItems.length).toBe(0);
      // Parents must not be changed
      expect(outline.nodeMenuBar.menuItems[0].parent).toBe(outline.nodeMenuBar.menuboxRight);
      expect(outline.nodeMenuBar.menuItems[1].parent).toBe(outline.nodeMenuBar.menuboxRight);
      // Unfortunately this test case could not reproduce the exception we had in the real app
      // The problem was that the menu item was removed due to relinking the parent, same as for the other test cases
      // But we did not find the exact constellation to reproduce it.
      // But the main problem was that the menuBar contained the menuItems even though they were moved to the nodeMenuBar
      outline.validateLayout();
    });

    it('does not fail if same menus are set again', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      helper.setMobileFlags(outline);
      outline.render();
      let node0 = outline.nodes[0];
      let emptySpaceMenu = menuHelper.createMenu();
      let emptySpaceMenu2 = menuHelper.createMenu();
      node0.detailTable.setMenus([emptySpaceMenu, emptySpaceMenu2]);

      // Select node -> empty space menus are active
      outline.selectNodes(node0);
      expect(outline.detailMenuBar.menuItems.length).toBe(2);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[0]);
      expect(outline.detailMenuBar.menuItems[0].parent).toBe(outline.detailMenuBar.menuboxLeft);
      expect(outline.detailMenuBar.menuItems[1]).toBe(node0.detailTable.menus[1]);
      expect(outline.detailMenuBar.menuItems[1].parent).toBe(outline.detailMenuBar.menuboxLeft);

      // Set same menus again. Table is temporarily used as parent which means that menu will be removed because table is not rendered
      // Setting the same menus again would normally not trigger a rerendering, but in this case it has to
      node0.detailTable.setMenus([emptySpaceMenu, emptySpaceMenu2]);
      expect(outline.detailMenuBar.menuItems.length).toBe(2);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[0]);
      expect(outline.detailMenuBar.menuItems[0].parent).toBe(outline.detailMenuBar.menuboxLeft);
      expect(outline.detailMenuBar.menuItems[1]).toBe(node0.detailTable.menus[1]);
      expect(outline.detailMenuBar.menuItems[1].parent).toBe(outline.detailMenuBar.menuboxLeft);

      // Ensure no exception is thrown
      outline.validateLayout();
    });

  });

  describe('detailContent', () => {

    it('is shown when a node is selected', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      helper.setMobileFlags(outline);
      outline.render();
      let node0 = outline.nodes[0];
      node0.childNodes[0].detailForm = new FormSpecHelper(session).createFormWithOneField({
        modal: false
      });
      expect(outline.detailContent).toBe(null);

      outline.selectNodes(node0.childNodes[0]);
      expect(outline.detailContent).toBe(node0.childNodes[0].detailForm);
      expect(outline.detailContent.rendered).toBe(true);

      outline.selectNodes(node0);
      expect(outline.detailContent).toBe(null);

      outline.selectNodes(node0.childNodes[0]);
      expect(outline.detailContent).toBe(node0.childNodes[0].detailForm);
      expect(outline.detailContent.rendered).toBe(true);
    });

    it('can select a node when scrolled first', () => {
      // Reduce the viewport size so that only some rows are rendered
      $('<style>' +
        '.tree-node {height: 20px; }' +
        '.tree-data {height: 60px !important; overflow: hidden;}' +
        '</style>').appendTo($('#sandbox'));
      let outline = helper.createOutlineWithOneDetailTable();
      helper.setMobileFlags(outline);
      outline.render();
      outline.htmlComp.validateRoot = true; // Ensure layout calls will not be swallowed by DesktopLayout because there is no bench
      let node0 = outline.nodes[0];
      let newNodes = helper.createModelNodes(10);
      newNodes.forEach(n => {
        n.id = 'new-' + n.id;
      }); // Ensure the new nodes have a unique id
      outline.insertNodes(newNodes, node0);
      let childNode9 = outline.nodes[0].childNodes[9];
      childNode9.detailForm = new FormSpecHelper(session).createFormWithOneField({modal: false});

      outline.selectNodes(node0);
      outline.scrollTo(childNode9);
      outline.selectNodes(childNode9);
      expect(outline.detailContent).toBe(childNode9.detailForm);
      expect(outline.detailContent.rendered).toBe(true);
    });

    describe('click on a node inside the detail content', () => {

      it('does not modify the outline', () => {
        let outline = helper.createOutline(helper.createModelFixture(3, 2));
        helper.setMobileFlags(outline);
        outline.render();
        outline.selectNodes(outline.nodes[1]);

        // The outline node contains a tree as detail node (real life case would be a form with a tree field, but this is easier to test)
        let treeHelper = new TreeSpecHelper(session);
        let treeModel = treeHelper.createModelFixture(3, 3);
        treeModel.nodes[0].id = ObjectUuidProvider.createUiId(); // tree helper doesn't use unique ids -> do it here
        let tree = treeHelper.createTree(treeModel);
        let form = scout.create(Form, {
          parent: session.desktop,
          rootGroupBox: {
            objectType: GroupBox,
            fields: [{
              objectType: TreeField,
              tree: tree
            }]
          }
        });
        outline.setDetailContent(form);

        spyOn(outline, 'selectNodes');
        spyOn(tree, 'selectNodes');

        JQueryTesting.triggerMouseDown(tree.nodes[0].$node);

        // Outline must not react to clicks on tree nodes of the detail content tree
        expect(outline.selectNodes).not.toHaveBeenCalled();
        expect(tree.selectNodes).toHaveBeenCalledWith(tree.nodes[0]);
      });
    });

    it('is updated when page changes', () => {
      const outline = helper.createOutlineWithOneDetailTable();
      helper.setMobileFlags(outline);
      outline.render();
      const page = outline.nodes[0].childNodes[0];
      page.setDetailForm(new FormSpecHelper(session).createFormWithOneField({
        modal: false
      }));

      expect(outline.detailContent).toBe(null);

      outline.selectNodes(page);
      expect(outline.detailContent).toBe(page.detailForm);

      const oldDetailForm = page.detailForm;
      page.setDetailForm(new FormSpecHelper(session).createFormWithOneField({
        modal: false,
        enabled: false
      }));
      expect(outline.detailContent).toBe(page.detailForm);
      expect(outline.detailContent).not.toBe(oldDetailForm);
    });
  });

  describe('outlineOverview', () => {

    beforeEach(() => {
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true
        }
      });
    });

    it('is displayed when no node is selected', () => {
      let outline = helper.createOutline(helper.createModelFixture(3, 2));
      session.desktop.setOutline(outline);
      expect(outline.outlineOverview.rendered).toBe(true);

      outline.selectNodes(outline.nodes[0]);
      expect(outline.outlineOverview.rendered).toBe(false);
    });

    it('is displayed selected page is deleted', () => {
      let outline = helper.createOutline(helper.createModelFixture(3, 2));
      session.desktop.setOutline(outline);
      expect(outline.outlineOverview.rendered).toBe(true);

      outline.selectNodes(outline.nodes[0]);
      expect(outline.outlineOverview.rendered).toBe(false);

      outline.deleteNode(outline.nodes[0]);
      expect(outline.outlineOverview.rendered).toBe(true);

      outline.selectNodes(outline.nodes[0].childNodes[0]);
      expect(outline.outlineOverview.rendered).toBe(false);

      // Delete parent of selected node
      outline.deleteNode(outline.nodes[0]);
      expect(outline.outlineOverview.rendered).toBe(true);
    });

    it('is displayed if all pages are deleted', () => {
      let outline = helper.createOutline(helper.createModelFixture(3, 2));
      session.desktop.setOutline(outline);
      expect(outline.outlineOverview.rendered).toBe(true);

      outline.selectNodes(outline.nodes[0]);
      expect(outline.outlineOverview.rendered).toBe(false);

      outline.deleteAllNodes();
      expect(outline.outlineOverview.rendered).toBe(true);
    });

    it('is not displayed if outlineOverviewVisible is false', () => {
      let outline = helper.createOutline(helper.createModelFixture(3, 2));
      session.desktop.setOutline(outline);
      expect(outline.outlineOverview.rendered).toBe(true);

      outline.setOutlineOverviewVisible(false);
      expect(outline.outlineOverview).toBe(null);

      outline.setOutlineOverviewVisible(true);
      expect(outline.outlineOverview.rendered).toBe(true);
    });

    it('uses the TileOutlineOverview by default', () => {
      let outline = helper.createOutline(helper.createModelFixture(3, 2));
      session.desktop.setOutline(outline);
      expect(outline.outlineOverview instanceof TileOutlineOverview).toBe(true);
    });

    it('may be replaced by another OutlineOverview', () => {
      let model = helper.createModelFixture(3, 2);
      model.outlineOverview = {
        objectType: OutlineOverview
      };
      let outline = helper.createOutline(model);
      session.desktop.setOutline(outline);
      expect(outline.outlineOverview instanceof OutlineOverview).toBe(true);
      expect(outline.outlineOverview instanceof TileOutlineOverview).toBe(false);

      let outlineOverview = scout.create(TileOutlineOverview, {parent: outline});
      outline.setOutlineOverview(outlineOverview);
      expect(outline.outlineOverview instanceof TileOutlineOverview).toBe(true);
      expect(outline.outlineOverview).toBe(outlineOverview);
    });

    it('is replaced by the default detail form if there is one', () => {
      let outline = helper.createOutline(helper.createModelFixture(3, 2));
      session.desktop.setOutline(outline);
      expect(outline.outlineOverview.rendered).toBe(true);

      let form = formHelper.createFormWithOneField();
      outline.setDefaultDetailForm(form);
      expect(outline.outlineOverview).toBe(null);
      expect(outline.defaultDetailForm.rendered).toBe(true);
    });
  });

  describe('defaultDetailForm', () => {
    beforeEach(() => {
      session = sandboxSession({
        desktop: {
          navigationVisible: true,
          headerVisible: true,
          benchVisible: true
        }
      });
    });

    it('is set to null if getting destroyed', () => {
      let outline = helper.createOutline();
      session.desktop.setOutline(outline);
      expect(outline.defaultDetailForm).toBe(null);
      expect(outline.outlineOverview.rendered).toBe(true);

      let form = formHelper.createFormWithOneField();
      outline.setDefaultDetailForm(form);
      expect(outline.defaultDetailForm).toBe(form);
      expect(form.rendered).toBe(true);
      expect(outline.outlineOverview).toBe(null);

      form.close();
      expect(form.destroyed).toBe(true);
      expect(outline.defaultDetailForm).toBe(null);
      expect(outline.outlineOverview.rendered).toBe(true);
    });
  });

  describe('drillDown', () => {

    class SpecPageWithNodes extends PageWithNodes {

      protected override _createChildPages(): JQuery.Promise<Page[]> {
        let childPages = [
          scout.create(SpecPageWithTable, {
            parent: this.getOutline(),
            text: 'Table Page',
            detailTable: {
              objectType: Table,
              columns: [
                {
                  id: 'StringColumn',
                  objectType: Column
                }
              ]
            }
          }),
          scout.create(SpecPageWithNodes, {
            parent: this.getOutline(),
            text: 'Node Page'
          })
        ];
        return $.resolvedPromise(childPages);
      }
    }

    class SpecPageWithTable extends PageWithTable {

      override createChildPage(row) {
        return scout.create(SpecPageWithNodes, {
          parent: this.getOutline()
        });
      }

      protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
        let data = [
          {string: 'string 1'},
          {string: 'string 2'},
          {string: 'string 3'}
        ];
        return $.resolvedPromise(data);
      }

      protected override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
        return tableData.map(row => {
          return {
            data: row,
            cells: [row.string]
          };
        });
      }
    }

    it('selects the given page', () => {
      let model = helper.createModelFixture(3, 2, false);
      let outline = helper.createOutline(model);

      expect(outline.selectedNode()).toBe(null);
      expect(outline.nodes[0].expanded).toBe(false);
      expect(outline.nodes[1].expanded).toBe(false);

      // Supports null
      outline.drillDown(null);
      expect(outline.selectedNode()).toBe(null);
      expect(outline.nodes[0].expanded).toBe(false);
      expect(outline.nodes[1].expanded).toBe(false);

      // Select first node, does not automatically expand children (because parent is not a table page)
      outline.drillDown(outline.nodes[0]);
      expect(outline.selectedNode()).toBe(outline.nodes[0]);
      expect(outline.nodes[0].expanded).toBe(false);
      expect(outline.nodes[1].expanded).toBe(false);
      expect(outline.nodes[2].expanded).toBe(false);

      // Select second node, and specify expansion explicitly
      outline.drillDown(outline.nodes[1], true);
      expect(outline.selectedNode()).toBe(outline.nodes[1]);
      expect(outline.nodes[0].expanded).toBe(false);
      expect(outline.nodes[1].expanded).toBe(true);
      expect(outline.nodes[1].childNodes[0].expanded).toBe(false);
      expect(outline.nodes[1].childNodes[1].expanded).toBe(false);
      expect(outline.nodes[1].childNodes[2].expanded).toBe(false);
      expect(outline.nodes[2].expanded).toBe(false);

      // Select child page of third node, parent node should be expanded automatically
      outline.drillDown(outline.nodes[2].childNodes[1]);
      expect(outline.selectedNode()).toBe(outline.nodes[2].childNodes[1]);
      expect(outline.nodes[0].expanded).toBe(false);
      expect(outline.nodes[1].expanded).toBe(true);
      expect(outline.nodes[1].childNodes[0].expanded).toBe(false);
      expect(outline.nodes[1].childNodes[1].expanded).toBe(false);
      expect(outline.nodes[1].childNodes[2].expanded).toBe(false);
      expect(outline.nodes[2].expanded).toBe(true);
      expect(outline.nodes[2].childNodes[0].expanded).toBe(false);
      expect(outline.nodes[2].childNodes[1].expanded).toBe(false);
      expect(outline.nodes[2].childNodes[2].expanded).toBe(false);
    });

    it('automatically expands the node when the parent page is a table page', async () => {
      jasmine.clock().uninstall();

      let model = helper.createModel([]);
      let outline = helper.createOutline(model);

      // NodePage [nodePage1]
      // +- TablePage [tablePage1]
      // |  +- NodePage [nodePage2]
      // |  +- NodePage [nodePage3]
      // |  |  (...)
      // |  +- NodePage [nodePage4]
      // |     +- TablePage [tablePage2]
      // |     |  +- NodePage [nodePage6]
      // |     |  +- NodePage [nodePage7]
      // |     |  +- NodePage [nodePage8]
      // |     +- NodePage [nodePage5]
      // +- (...)

      expect(outline.selectedNode()).toBe(null);

      let nodePage1 = scout.create(SpecPageWithNodes, {
        parent: outline
      });
      outline.insertNode(nodePage1);
      await nodePage1.ensureLoadChildren();

      // Drill-down node page (auto expand -> true)
      outline.drillDown(nodePage1);
      expect(outline.selectedNode()).toBe(nodePage1);
      expect(nodePage1.expanded).toBe(true);
      expect(nodePage1.childNodes.length).toBe(2);

      // Drill-down table page (explicit expand -> true)
      let tablePage1 = nodePage1.childNodes[0];
      outline.drillDown(tablePage1, true);
      expect(outline.selectedNode()).toBe(tablePage1);
      expect(tablePage1.expanded).toBe(true);

      await tablePage1.ensureLoadChildren();
      expect(tablePage1.childNodes.length).toBe(3);
      let nodePage2 = tablePage1.childNodes[0];
      let nodePage3 = tablePage1.childNodes[1];
      let nodePage4 = tablePage1.childNodes[2];

      // Drill-down node page (auto expand -> true)
      outline.drillDown(nodePage3);
      expect(outline.selectedNode()).toBe(nodePage3);
      expect(nodePage2.expanded).toBe(false);
      expect(nodePage3.expanded).toBe(true); // <--
      expect(nodePage4.expanded).toBe(false);

      await nodePage4.ensureLoadChildren();
      expect(nodePage4.childNodes.length).toBe(2);
      let tablePage2 = nodePage4.childNodes[0];
      let nodePage5 = nodePage4.childNodes[1];

      // Drill-down table page (auto expand -> false)
      outline.drillDown(tablePage2);
      expect(outline.selectedNode()).toBe(tablePage2);
      expect(tablePage2.expanded).toBe(false); // <--
      expect(nodePage5.expanded).toBe(false);

      await tablePage2.ensureLoadChildren();
      expect(tablePage2.childNodes.length).toBe(3);
      let nodePage6 = tablePage2.childNodes[0];
      let nodePage7 = tablePage2.childNodes[1];
      let nodePage8 = tablePage2.childNodes[2];

      // Drill-down node page (explicit expand -> false)
      outline.drillDown(nodePage7, false); // <--
      expect(outline.selectedNode()).toBe(nodePage7);
      expect(nodePage6.expanded).toBe(false);
      expect(nodePage7.expanded).toBe(false); // <--
      expect(nodePage8.expanded).toBe(false);
    });
  });
});
