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
import {ObjectFactory, objects, OutlineOverview, scout, Status, TileOutlineOverview, Tree} from '../../../src/index';
import {FormSpecHelper, MenuSpecHelper, OutlineSpecHelper, TreeSpecHelper} from '@eclipse-scout/testing';

describe('Outline', () => {
  let helper, menuHelper, formHelper, session;

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

  function createNodesDeletedEvent(model, nodeIds, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      nodeIds: nodeIds,
      type: 'nodesDeleted'
    };
  }

  function createAllChildNodesDeletedEvent(model, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      type: 'allChildNodesDeleted'
    };
  }

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
      let messageBox = scout.create('MessageBox', model);

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
      };
      outline._renderMenus = () => {
      };

      // don't change the visibleByUi flag when selection is != 1
      outline.selectNodes([]);
      expect(node.detailFormVisibleByUi).toBe(false);

      // set the visibleByUi flag to true when selection is exactly 1
      outline.selectNodes([node]);
      expect(node.detailFormVisibleByUi).toBe(true);
    });

  });

  describe('updateDetailMenus', () => {

    it('adds the empty space menus of the detail table to the detail menu bar', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      outline.setCompact(true);
      outline.setEmbedDetailContent(true);
      let node0 = outline.nodes[0];
      node0.detailTable.menus = [
        menuHelper.createMenu({
          menuTypes: ['Table.SingleSelection']
        }), menuHelper.createMenu({
          menuTypes: ['Table.EmptySpace']
        })
      ];
      expect(outline.detailMenuBarVisible).toBe(false);
      expect(outline.detailMenuBar.menuItems.length).toBe(0);

      outline.selectNodes(node0);
      expect(outline.detailMenuBarVisible).toBe(true);
      expect(outline.detailMenuBar.menuItems.length).toBe(1);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[1]);
    });

    it('adds the single selection menus of the parent detail table to the detail menu bar', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      outline.setCompact(true);
      outline.setEmbedDetailContent(true);
      let node0 = outline.nodes[0];
      node0.detailTable.menus = [
        menuHelper.createMenu({
          menuTypes: ['Table.SingleSelection']
        }), menuHelper.createMenu({
          menuTypes: ['Table.EmptySpace']
        })
      ];
      expect(outline.detailMenuBarVisible).toBe(false);
      expect(outline.detailMenuBar.menuItems.length).toBe(0);

      outline.selectNodes(node0.childNodes[0]);
      expect(outline.detailMenuBarVisible).toBe(true);
      expect(outline.detailMenuBar.menuItems.length).toBe(1);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[0]);
    });

    it('attaches a listener to the detail table to get dynamic menu changes', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      outline.setCompact(true);
      outline.setEmbedDetailContent(true);
      let node0 = outline.nodes[0];
      expect(outline.detailMenuBarVisible).toBe(false);
      expect(outline.detailMenuBar.menuItems.length).toBe(0);

      outline.selectNodes(node0);
      expect(outline.detailMenuBarVisible).toBe(false);
      expect(outline.detailMenuBar.menuItems.length).toBe(0);

      // Menus change on table -> detail menu bar needs to be updated as well
      let menu = menuHelper.createModel('menu', '', ['Table.EmptySpace']);
      node0.detailTable.setMenus([menu]);
      expect(outline.detailMenuBarVisible).toBe(true);
      expect(outline.detailMenuBar.menuItems.length).toBe(1);
      expect(outline.detailMenuBar.menuItems[0]).toBe(node0.detailTable.menus[0]);
    });

    it('removes the listener from the detail tables on selection changes and destroy', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      outline.setCompact(true);
      outline.setEmbedDetailContent(true);
      let node0 = outline.nodes[0];
      let node1 = outline.nodes[1];
      let initialListenerCount = node0.detailTable.events._eventListeners.length;

      outline.selectNodes(node0);
      let selectionListenerCount = node0.detailTable.events._eventListeners.length;
      expect(selectionListenerCount).toBe(initialListenerCount + 3); // destroy and propertyChange listener

      outline.selectNodes(node1);
      selectionListenerCount = node0.detailTable.events._eventListeners.length;
      expect(selectionListenerCount).toBe(initialListenerCount + 1); // listeners removed

      outline.selectNodes(node0);
      selectionListenerCount = node0.detailTable.events._eventListeners.length;
      expect(selectionListenerCount).toBe(initialListenerCount + 3); // listeners attached again

      outline.nodes[0].detailTable.destroy();
      expect(node0.detailTable.events._eventListeners.length).toBe(0); // every listener should be removed now
    });

    it('makes sure table does not update the menu parent for empty space menus', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      outline.setCompact(true);
      outline.setEmbedDetailContent(true);
      outline.render();
      let node0 = outline.nodes[0];
      let emptySpaceMenu = menuHelper.createMenu({
        menuTypes: ['Table.EmptySpace']
      });
      let emptySpaceMenu2 = menuHelper.createMenu({
        menuTypes: ['Table.EmptySpace']
      });
      node0.detailTable.menus = [emptySpaceMenu];

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
      outline.setCompact(true);
      outline.setEmbedDetailContent(true);
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
        menuTypes: ['Table.SingleSelection']
      });
      let singleSelectionMenu2 = menuHelper.createMenu({
        menuTypes: ['Table.SingleSelection']
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

    it('does not fail if same menus are set again', () => {
      let outline = helper.createOutlineWithOneDetailTable();
      outline.setCompact(true);
      outline.setEmbedDetailContent(true);
      outline.render();
      let node0 = outline.nodes[0];
      let emptySpaceMenu = menuHelper.createMenu({
        menuTypes: ['Table.EmptySpace']
      });
      let emptySpaceMenu2 = menuHelper.createMenu({
        menuTypes: ['Table.EmptySpace']
      });
      node0.detailTable.menus = [emptySpaceMenu, emptySpaceMenu2];

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
      outline.setCompact(true);
      outline.setEmbedDetailContent(true);
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

    describe('click on a node inside the detail content', () => {

      it('does not modify the outline', () => {
        let outline = helper.createOutline(helper.createModelFixture(3, 2));
        outline.setCompact(true);
        outline.setEmbedDetailContent(true);
        let node0 = outline.nodes[0];
        outline.render();
        outline.selectNodes(outline.nodes[1]);

        // The outline node contains a tree as detail node (real life case would be a form with a tree field, but this is easier to test)
        let treeHelper = new TreeSpecHelper(session);
        let treeModel = treeHelper.createModelFixture(3, 3);
        treeModel.nodes[0].id = ObjectFactory.get().createUniqueId(); // tree helper doesn't use unique ids -> do it here
        let tree = treeHelper.createTree(treeModel);
        outline.setDetailContent(tree);

        spyOn(outline, 'selectNodes');
        spyOn(tree, 'selectNodes');

        tree.nodes[0].$node.triggerMouseDown();

        // Outline must not react to clicks on tree nodes of the detail content tree
        expect(outline.selectNodes).not.toHaveBeenCalled();
        expect(tree.selectNodes).toHaveBeenCalledWith(tree.nodes[0]);
      });

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
        objectType: 'OutlineOverview'
      };
      let outline = helper.createOutline(model);
      session.desktop.setOutline(outline);
      expect(outline.outlineOverview instanceof OutlineOverview).toBe(true);
      expect(outline.outlineOverview instanceof TileOutlineOverview).toBe(false);

      let outlineOverview = scout.create('TileOutlineOverview', {parent: outline});
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
});
