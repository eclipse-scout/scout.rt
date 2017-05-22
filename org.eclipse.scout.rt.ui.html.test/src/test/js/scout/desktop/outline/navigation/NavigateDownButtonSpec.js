/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('NavigateDownButton', function() {

  var session, outline, menu, node;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    outline = scout.create('Outline', {
      parent: session.desktop
    });
    node = scout.create('Page', {
      parent: outline
    });
    menu = scout.create('NavigateDownButton', {
      parent: session.desktop,
      outline: outline,
      node: node
    });
  });

  it('_toggleDetail is always false', function() {
    expect(menu._toggleDetail()).toBe(false);
  });

  it('_isDetail returns true or false depending on the state of the detail-form and detail-table', function() {
    // true when both detailForm and detailTable are visible
    node.detailForm = {};
    node.detailFormVisible = true;
    node.detailFormVisibleByUi = true;
    node.detailTable = scout.create('Table', {
      parent: new scout.NullWidget(),
      session: session
    });
    node.detailTableVisible = true;
    expect(menu._isDetail()).toBe(true);

    // false when detailForm is absent, even when if detailFormVisible=true
    delete node.detailForm;
    expect(menu._isDetail()).toBe(false);
    node.detailForm = {};

    // false when detailTable is absent, even when if detailTableVisible=true
    delete node.detailTable;
    expect(menu._isDetail()).toBe(false);
    node.detailTable = scout.create('Table', {
      parent: new scout.NullWidget(),
      session: session
    });

    // false when hidden by UI
    node.detailFormVisibleByUi = false;
    expect(menu._isDetail()).toBe(false);
    node.detailFormVisibleByUi = true;

    // false when property says to
    node.detailFormVisible = false;
    expect(menu._isDetail()).toBe(false);
    node.detailFormVisible = true;
    node.detailTableVisible = false;
    expect(menu._isDetail()).toBe(false);
  });

  describe('_buttonEnabled', function() {

    it('is disabled when node is a leaf', function() {
      node.leaf = true; // node is a leaf
      expect(menu._buttonEnabled()).toBe(false);
    });

    it('is enabled when node is not a leaf and we\'re currently displaying the detail', function() {
      node.leaf = false; // node is not a leaf
      menu._isDetail = function() { // currently we're displaying the detail-form
        return true;
      };
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is only enabled when detail-table has exactly one selected row', function() {
      node.leaf = false; // node is not a leaf
      menu._isDetail = function() { // currently we're not displaying the detail-form
        return false;
      };
      node.detailTable = scout.create('Table', {
        parent: new scout.NullWidget(),
        session: session
      });
      expect(menu._buttonEnabled()).toBe(false);

      node.detailTable.selectedRows = [{
        id: '1',
        page: {}
      }];
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is not enabled when selected row is not linked to a page', function() {
      node.leaf = false; // node is not a leaf
      menu._isDetail = function() { // currently we're not displaying the detail-form
        return false;
      };
      node.detailTable = scout.create('Table', {
        parent: new scout.NullWidget(),
        session: session
      });
      expect(menu._buttonEnabled()).toBe(false);

      node.detailTable.selectedRows = [{
        id: '1'
      }];
      expect(menu._buttonEnabled()).toBe(false);

      node.detailTable.selectedRows[0].page = {};
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is enabled when selected row is linked to a page later when page is inserted (remote case)', function() {
      linkWidgetAndAdapter(outline, 'OutlineAdapter');
      node.leaf = false; // node is not a leaf
      menu._isDetail = function() { // currently we're not displaying the detail-form
        return false;
      };
      node.detailTable = scout.create('Table', {
        parent: new scout.NullWidget(),
        session: session
      });
      outline.insertNode(node);
      expect(menu._buttonEnabled()).toBe(false);

      var page = scout.create('Page', {
        parent: outline,
        parentNode: node
      });
      var row = scout.create('TableRow', {
        parent: node.detailTable,
        nodeId: page.id
      });
      node.detailTable.insertRow(row);
      node.detailTable.selectRow(row);
      expect(menu._buttonEnabled()).toBe(false);

      outline.insertNode(page);
      expect(menu._buttonEnabled()).toBe(true);
    });
  });

  it('_drill drills down to first selected row in the detail table', function() {
    node.detailTable = scout.create('Table', {
      parent: new scout.NullWidget(),
      session: session,
      rows: [{
        id: '123',
        nodeId: '123'
      }]
    });
    var drillNode = scout.create('Page', {
      parent: outline
    });
    var firstRow = node.detailTable.rows[0];
    drillNode.linkWithRow(firstRow);
    node.detailTable.selectRows(firstRow);
    outline.nodesMap = {
      '123': drillNode
    };

    spyOn(outline, 'selectNodes');
    menu._drill();
    expect(outline.selectNodes).toHaveBeenCalledWith(drillNode);
  });

});
