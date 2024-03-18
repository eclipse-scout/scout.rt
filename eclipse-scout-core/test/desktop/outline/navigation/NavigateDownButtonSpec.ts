/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, NavigateDownButton, NullWidget, Outline, Page, scout, Table, TableRow} from '../../../../src/index';

describe('NavigateDownButton', () => {

  let session: SandboxSession, outline: Outline, menu: SpecNavigateDownButton, node: Page;

  class SpecNavigateDownButton extends NavigateDownButton {
    override _toggleDetail(): boolean {
      return super._toggleDetail();
    }

    override _isDetail(): boolean {
      return super._isDetail();
    }

    override _buttonEnabled(): boolean {
      return super._buttonEnabled();
    }

    override _drill() {
      super._drill();
    }

    override _getDrillNode(): Page {
      return super._getDrillNode();
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    outline = scout.create(Outline, {
      parent: session.desktop
    });
    node = scout.create(Page, {
      parent: outline
    });
    menu = scout.create(SpecNavigateDownButton, {
      parent: session.desktop,
      outline: outline,
      node: node
    });
  });

  it('_toggleDetail is always false', () => {
    expect(menu._toggleDetail()).toBe(false);
  });

  it('_isDetail returns true or false depending on the state of the detail-form and detail-table', () => {
    // true when both detailForm and detailTable are visible
    node.detailForm = new Form();
    node.detailFormVisible = true;
    node.detailFormVisibleByUi = true;
    node.detailTable = scout.create(Table, {
      parent: new NullWidget(),
      session: session
    });
    node.detailTableVisible = true;
    // false when detail table has no
    expect(menu._isDetail()).toBe(false);
    node.detailTable.insertRow({
      cells: ['Test']
    });
    expect(menu._isDetail()).toBe(true);

    // false when detailForm is absent, even when if detailFormVisible=true
    delete node.detailForm;
    expect(menu._isDetail()).toBe(false);
    node.detailForm = new Form();

    // false when detailTable is absent, even when if detailTableVisible=true
    delete node.detailTable;
    expect(menu._isDetail()).toBe(false);
    node.detailTable = scout.create(Table, {
      parent: new NullWidget(),
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

  describe('_buttonEnabled', () => {

    it('is disabled when node is a leaf', () => {
      node.leaf = true; // node is a leaf
      expect(menu._buttonEnabled()).toBe(false);
    });

    it('is enabled when node is not a leaf and we\'re currently displaying the detail', () => {
      node.leaf = false; // node is not a leaf
      menu._isDetail = () => { // currently we're displaying the detail-form
        return true;
      };
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is only enabled when detail-table has exactly one selected row', () => {
      node.leaf = false; // node is not a leaf
      menu._isDetail = () => { // currently we're not displaying the detail-form
        return false;
      };
      node.detailTable = scout.create(Table, {
        parent: new NullWidget(),
        session: session
      });
      expect(menu._buttonEnabled()).toBe(false);

      node.detailTable.rows = [{
        id: '1',
        // @ts-expect-error
        page: {}
      }];
      node.detailTable.selectedRows = [node.detailTable.rows[0]];
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is not enabled when selected row is not linked to a page', () => {
      node.leaf = false; // node is not a leaf
      menu._isDetail = () => { // currently we're not displaying the detail-form
        return false;
      };
      node.detailTable = scout.create(Table, {
        parent: new NullWidget(),
        session: session
      });
      expect(menu._buttonEnabled()).toBe(false);

      // @ts-expect-error
      node.detailTable.rows = [{
        id: '1'
      }];
      node.detailTable.selectedRows = [node.detailTable.rows[0]];
      expect(menu._buttonEnabled()).toBe(false);

      // @ts-expect-error
      node.detailTable.selectedRows[0].page = {};
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is enabled when selected row is linked to a page later when page is inserted (remote case)', () => {
      linkWidgetAndAdapter(outline, 'OutlineAdapter');
      node.leaf = false; // node is not a leaf
      menu._isDetail = () => { // currently we're not displaying the detail-form
        return false;
      };
      node.detailTable = scout.create(Table, {
        parent: new NullWidget(),
        session: session
      });
      outline.insertNode(node);
      expect(menu._buttonEnabled()).toBe(false);

      let page = scout.create(Page, {
        parent: outline,
        parentNode: node
      });
      let row = scout.create(TableRow, {
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

  it('_drill drills down to first selected row in the detail table', () => {
    node.detailTable = scout.create(Table, {
      parent: new NullWidget(),
      session: session,
      rows: [{
        id: '123',
        nodeId: '123'
      }]
    });
    let drillNode = scout.create(Page, {
      parent: outline
    });
    let firstRow = node.detailTable.rows[0];
    drillNode.linkWithRow(firstRow);
    node.detailTable.selectRows(firstRow);
    outline.nodesMap = {
      '123': drillNode
    };

    spyOn(outline, 'selectNodes');
    menu._drill();
    expect(outline.selectNodes).toHaveBeenCalledWith(drillNode);
  });

  describe('_getDrillNode', () => {

    it('_getDrillNode returns the next child node', () => {
      node.detailTable = scout.create(Table, {
        parent: new NullWidget(),
        session: session,
        rows: [
          {id: '1'},
          {id: '2'},
          {id: '3'}
        ]
      });
      let childNode1 = scout.create(Page, {
        parent: outline
      });
      let childNode2 = scout.create(Page, {
        parent: outline
      });
      node.childNodes = [childNode1, childNode2];
      let row1 = node.detailTable.rows[0];
      let row2 = node.detailTable.rows[1];
      let row3 = node.detailTable.rows[2];
      childNode1.linkWithRow(row1);
      childNode2.linkWithRow(row2);

      expect(menu._getDrillNode()).toBe(null);

      node.detailTable.selectRows(row1);
      expect(menu._getDrillNode()).toBe(childNode1);

      node.detailTable.selectRows(row2);
      expect(menu._getDrillNode()).toBe(childNode2);

      node.detailTable.selectRows(row3);
      expect(menu._getDrillNode()).toBe(null);

      // -----

      node.leaf = true;
      node.detailTable.selectRows(row1);
      expect(menu._getDrillNode()).toBe(null);

      // -----

      node.leaf = false;
      node.detailTableVisible = false;
      node.detailTable.selectRows(null);
      expect(menu._getDrillNode()).toBe(childNode1);
    });
  });
});
