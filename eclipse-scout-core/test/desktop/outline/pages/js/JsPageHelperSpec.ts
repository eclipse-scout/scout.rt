/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Column, HybridManager, HybridManagerAdapter, InitModelOf, JsPageHelper, Outline, OutlineAdapter, Page, PageParamDo, PageWithNodes, PageWithTable, RemoteEvent, RemoteRequest, scout, styles, Table, TableRow, TreeNode, TreeNodeModel,
  typeName, UuidPool
} from '../../../../../src';
import {OutlineSpecHelper} from '../../../../../src/testing';

describe('JsPageHelper', () => {
  let session: SandboxSession;
  let outlineSpecHelper: OutlineSpecHelper;
  let outline: Outline;
  let outlineAdapter: OutlineAdapter;
  let page: Page;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();

    session.desktop.addOns.push(
      scout.create(HybridManager, {parent: session.desktop}),
      scout.create(UuidPool, {parent: session.desktop})
    );

    outlineSpecHelper = new OutlineSpecHelper(session);
    outline = outlineSpecHelper.createOutline(outlineSpecHelper.createModelFixture(1, 0, true));

    linkWidgetAndAdapter(outline, OutlineAdapter);
    outlineAdapter = outline.modelAdapter as OutlineAdapter;

    page = outline.nodes[0];
  });

  describe('init', () => {

    it('ensures page is set', () => {
      expect(() => scout.create(SpecJsPageHelper, {} as InitModelOf<SpecJsPageHelper>)).toThrowError('Page not set or has wrong type');
      expect(() => scout.create(SpecJsPageHelper, {page: 'Foo' as unknown as Page})).toThrowError('Page not set or has wrong type');
    });

    it('installs listeners', () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      expect(outline.events.count('nodesUpdated', helper._nodesUpdatedHandler)).toBe(1);
      expect(outline.events.count('nodesDeleted', helper._nodesDeletedHandler)).toBe(1);
      expect(outline.events.count('allChildNodesDeleted', helper._allChildNodesDeletedHandler)).toBe(1);
      expect(outline.events.count('nodesInserted', helper._nodesInsertedHandler)).toBe(0);

      // PageWithTable needs more listeners

      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        objectType: MyPageWithTable
      });

      const myPageWithTable = outline.nodes[1] as MyPageWithTable;
      expect(myPageWithTable).toBeInstanceOf(MyPageWithTable);
      myPageWithTable.ensureDetailTable();

      const myPageWithTableHelper = myPageWithTable.jsPageHelper;

      expect(outline.events.count('nodesUpdated', myPageWithTableHelper._nodesUpdatedHandler)).toBe(1);
      expect(outline.events.count('nodesDeleted', myPageWithTableHelper._nodesDeletedHandler)).toBe(1);
      expect(outline.events.count('allChildNodesDeleted', myPageWithTableHelper._allChildNodesDeletedHandler)).toBe(1);
      expect(outline.events.count('nodesInserted', myPageWithTableHelper._nodesInsertedHandler)).toBe(1);

      const table = myPageWithTable.detailTable;
      expect(table.events.count('rowsInserted', myPageWithTableHelper._tableRowsInsertHandler)).toBe(1);

      myPageWithTable.setDetailTable(scout.create(Table, {
        parent: outline,
        columns: [
          {
            id: 'PrimaryKeyColumn',
            objectType: Column,
            displayable: false,
            primaryKey: true
          },
          {
            id: 'LabelColumn',
            objectType: Column,
            text: 'Label',
            width: 200,
            summary: true
          }
        ]
      }));

      expect(myPageWithTable.detailTable).not.toBe(table);
      expect(table.events.count('rowsInserted', myPageWithTableHelper._tableRowsInsertHandler)).toBe(0);
      expect(myPageWithTable.detailTable.events.count('rowsInserted', myPageWithTableHelper._tableRowsInsertHandler)).toBe(1);
    });
  });

  describe('destroy', () => {

    it('uninstalls listeners', () => {
      const helper = scout.create(SpecJsPageHelper, {page});
      helper.destroy();

      expect(outline.events.count('nodesUpdated', helper._nodesUpdatedHandler)).toBe(0);
      expect(outline.events.count('nodesDeleted', helper._nodesDeletedHandler)).toBe(0);
      expect(outline.events.count('allChildNodesDeleted', helper._allChildNodesDeletedHandler)).toBe(0);

      // PageWithTable

      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        objectType: MyPageWithTable
      });

      const myPageWithTable = outline.nodes[1] as MyPageWithTable;
      expect(myPageWithTable).toBeInstanceOf(MyPageWithTable);
      myPageWithTable.ensureDetailTable();

      const myPageWithTableHelper = myPageWithTable.jsPageHelper;
      myPageWithTableHelper.destroy();

      expect(outline.events.count('nodesUpdated', myPageWithTableHelper._nodesUpdatedHandler)).toBe(0);
      expect(outline.events.count('nodesDeleted', myPageWithTableHelper._nodesDeletedHandler)).toBe(0);
      expect(outline.events.count('allChildNodesDeleted', myPageWithTableHelper._allChildNodesDeletedHandler)).toBe(0);
      expect(outline.events.count('nodesInserted', myPageWithTableHelper._nodesInsertedHandler)).toBe(0);
      expect(myPageWithTable.detailTable.events.count('rowsInserted', myPageWithTableHelper._tableRowsInsertHandler)).toBe(0);
    });
  });

  describe('nodesUpdated', () => {

    it('removes linked rows', async () => {
      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        objectType: MyPageWithTable,
        childNodes: [
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '1'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '2'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '3'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '4'}}
        ]
      });

      const myPageWithTable = outline.nodes[1] as MyPageWithTable;
      expect(myPageWithTable).toBeInstanceOf(MyPageWithTable);
      myPageWithTable.ensureDetailTable();

      const helper = myPageWithTable.jsPageHelper;
      spyOn(helper, 'callLoadChildPages').and.callFake(async (idsOrPageParams: string | PageParamDo | (string | PageParamDo)[], replace?: boolean): Promise<void> => {
        helper._addChildPagesToIdMap();
      });

      await myPageWithTable.ensureLoadChildren();

      spyOn(outlineAdapter, 'sendNodesChanged').and.callFake((nodes: TreeNode[]) => undefined);

      const [page1, page2, page3, page4] = myPageWithTable.childNodes;

      outline.updateNodes([page1, page2]);
      expect(outlineAdapter.sendNodesChanged).toHaveBeenCalledWith([page1, page2]);

      outline.updateNodes([myPageWithTable, page2, page3, page4]);
      expect(outlineAdapter.sendNodesChanged).toHaveBeenCalledWith([page2, page3, page4]);
    });
  });

  describe('nodesDeleted', () => {

    it('unlinks linked rows of a table page', async () => {
      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        objectType: MyPageWithTable,
        childNodes: [
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '1'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '2'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '3'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '4'}}
        ]
      });

      const myPageWithTable = outline.nodes[1] as MyPageWithTable;
      expect(myPageWithTable).toBeInstanceOf(MyPageWithTable);
      myPageWithTable.ensureDetailTable();

      const helper = myPageWithTable.jsPageHelper;
      spyOn(helper, 'callLoadChildPages').and.callFake(async (idsOrPageParams: string | PageParamDo | (string | PageParamDo)[], replace?: boolean): Promise<void> => {
        helper._addChildPagesToIdMap();
      });

      await myPageWithTable.ensureLoadChildren();

      const row1 = myPageWithTable.detailTable.rows[0];
      const row2 = myPageWithTable.detailTable.rows[1];
      const row3 = myPageWithTable.detailTable.rows[2];
      const row4 = myPageWithTable.detailTable.rows[3];

      expect(myPageWithTable.childrenLoaded).toBeTrue();
      expect(myPageWithTable.childNodes.length).toBe(4);
      expect(myPageWithTable.detailTable.rows.length).toBe(4);
      expect(helper._childPagesById.size).toBe(4);

      const [page1, page2, page3, page4] = myPageWithTable.childNodes;

      outline.deleteNodes([page1, page2]);

      expect(myPageWithTable.childrenLoaded).toBeTrue();
      expect(myPageWithTable.childNodes).toEqual([page3, page4]);
      expect(myPageWithTable.detailTable.rows).toEqual([row1, row2, row3, row4]);
      expect(row1.page).toBeFalsy();
      expect(row2.page).toBeFalsy();
      expect(row3.page).toBe(page3);
      expect(row4.page).toBe(page4);
      expect(helper._childPagesById.size).toBe(2);

      outline.deleteNodes([page3, page4]);

      expect(myPageWithTable.childrenLoaded).toBeFalse();
      expect(myPageWithTable.childNodes.length).toBe(0);
      expect(myPageWithTable.detailTable.rows).toEqual([row1, row2, row3, row4]);
      expect(row1.page).toBeFalsy();
      expect(row2.page).toBeFalsy();
      expect(row3.page).toBeFalsy();
      expect(row4.page).toBeFalsy();
      expect(helper._childPagesById.size).toBe(0);
    });

    it('removes linked rows of a node page', async () => {
      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        objectType: MyPageWithNodes,
        childNodes: [
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '1'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '2'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '3'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '4'}}
        ]
      });

      const myPageWithNodes = outline.nodes[1] as MyPageWithNodes;
      expect(myPageWithNodes).toBeInstanceOf(MyPageWithNodes);
      myPageWithNodes.ensureDetailTable();

      const helper = myPageWithNodes.jsPageHelper;
      spyOn(helper, 'callLoadChildPages').and.callFake(async (idsOrPageParams: string | PageParamDo | (string | PageParamDo)[], replace?: boolean): Promise<void> => {
        helper._addChildPagesToIdMap();
      });

      await myPageWithNodes.ensureLoadChildren();

      const row3 = myPageWithNodes.detailTable.rows[2];
      const row4 = myPageWithNodes.detailTable.rows[3];

      expect(myPageWithNodes.childrenLoaded).toBeTrue();
      expect(myPageWithNodes.childNodes.length).toBe(4);
      expect(myPageWithNodes.detailTable.rows.length).toBe(4);
      expect(helper._childPagesById.size).toBe(4);

      const [page1, page2, page3, page4] = myPageWithNodes.childNodes;

      outline.deleteNodes([page1, page2]);

      expect(myPageWithNodes.childrenLoaded).toBeTrue();
      expect(myPageWithNodes.childNodes).toEqual([page3, page4]);
      expect(myPageWithNodes.detailTable.rows.length).toBe(2);
      expect(myPageWithNodes.detailTable.rows).not.toEqual([row3, row4]);
      expect(page1.row).toBeFalsy();
      expect(page2.row).toBeFalsy();
      expect(page3.row).toBe(myPageWithNodes.detailTable.rows[0]);
      expect(page4.row).toBe(myPageWithNodes.detailTable.rows[1]);
      expect(helper._childPagesById.size).toBe(2);

      outline.deleteNodes([page3, page4]);

      expect(myPageWithNodes.childrenLoaded).toBeFalse();
      expect(myPageWithNodes.childNodes.length).toBe(0);
      expect(myPageWithNodes.detailTable.rows.length).toBe(0);
      expect(helper._childPagesById.size).toBe(0);
    });
  });

  describe('allChildNodesDeleted', () => {

    it('unlinks all rows of a table page', async () => {
      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        objectType: MyPageWithTable,
        childNodes: [
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '1'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '2'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '3'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '4'}}
        ]
      });

      const myPageWithTable = outline.nodes[1] as MyPageWithTable;
      expect(myPageWithTable).toBeInstanceOf(MyPageWithTable);
      myPageWithTable.ensureDetailTable();

      const helper = myPageWithTable.jsPageHelper;
      spyOn(helper, 'callLoadChildPages').and.callFake(async (idsOrPageParams: string | PageParamDo | (string | PageParamDo)[], replace?: boolean): Promise<void> => {
        helper._addChildPagesToIdMap();
      });

      await myPageWithTable.ensureLoadChildren();

      const row1 = myPageWithTable.detailTable.rows[0];
      const row2 = myPageWithTable.detailTable.rows[1];
      const row3 = myPageWithTable.detailTable.rows[2];
      const row4 = myPageWithTable.detailTable.rows[3];

      expect(myPageWithTable.childrenLoaded).toBeTrue();
      expect(myPageWithTable.childNodes.length).toBe(4);
      expect(myPageWithTable.detailTable.rows.length).toBe(4);
      expect(helper._childPagesById.size).toBe(4);

      outline.deleteAllChildNodes(myPageWithTable);

      expect(myPageWithTable.childrenLoaded).toBeFalse();
      expect(myPageWithTable.childNodes.length).toBe(0);
      expect(myPageWithTable.detailTable.rows.length).toBe(4);
      expect(myPageWithTable.detailTable.rows).toEqual([row1, row2, row3, row4]);
      expect(row1.page).toBeFalsy();
      expect(row2.page).toBeFalsy();
      expect(row3.page).toBeFalsy();
      expect(row4.page).toBeFalsy();
      expect(helper._childPagesById.size).toBe(0);
    });

    it('removes all rows of a node page', async () => {
      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        objectType: MyPageWithNodes,
        childNodes: [
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '1'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '2'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '3'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '4'}}
        ]
      });

      const myPageWithTable = outline.nodes[1] as MyPageWithNodes;
      expect(myPageWithTable).toBeInstanceOf(MyPageWithNodes);
      myPageWithTable.ensureDetailTable();

      const helper = myPageWithTable.jsPageHelper;
      spyOn(helper, 'callLoadChildPages').and.callFake(async (idsOrPageParams: string | PageParamDo | (string | PageParamDo)[], replace?: boolean): Promise<void> => {
        helper._addChildPagesToIdMap();
      });

      await myPageWithTable.ensureLoadChildren();

      expect(myPageWithTable.childrenLoaded).toBeTrue();
      expect(myPageWithTable.childNodes.length).toBe(4);
      expect(myPageWithTable.detailTable.rows.length).toBe(4);
      expect(helper._childPagesById.size).toBe(4);

      outline.deleteAllChildNodes(myPageWithTable);

      expect(myPageWithTable.childrenLoaded).toBeFalse();
      expect(myPageWithTable.childNodes.length).toBe(0);
      expect(myPageWithTable.detailTable.rows.length).toBe(0);
      expect(helper._childPagesById.size).toBe(0);
    });
  });

  describe('nodesInserted', () => {

    it('toggles css class js-page-child-page-loading', () => {
      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        objectType: MyPageWithTable,
        childNodes: [
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '1'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '2'}}
        ]
      });

      const myPageWithTable = outline.nodes[1] as MyPageWithTable;
      expect(myPageWithTable).toBeInstanceOf(MyPageWithTable);

      expect(myPageWithTable.childNodes.length).toBe(2);
      for (const page of myPageWithTable.childNodes) {
        expect(styles.hasCssClass(page.cssClass, 'js-page-child-page-loading')).toBeFalse();
      }

      myPageWithTable.childrenLoaded = false;
      outline.insertNodes(myPageWithTable.childNodes, myPageWithTable);
      expect(myPageWithTable.childNodes.length).toBe(2);
      for (const page of myPageWithTable.childNodes) {
        expect(styles.hasCssClass(page.cssClass, 'js-page-child-page-loading')).toBeTrue();
      }

      myPageWithTable.childrenLoaded = true;
      outline.insertNodes(myPageWithTable.childNodes, myPageWithTable);
      expect(myPageWithTable.childNodes.length).toBe(2);
      for (const page of myPageWithTable.childNodes) {
        expect(styles.hasCssClass(page.cssClass, 'js-page-child-page-loading')).toBeFalse();
      }
    });
  });

  describe('rowsInserted', () => {

    it('sends nodesChanged event for linked pages', async () => {
      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        objectType: MyPageWithTable,
        childNodes: [
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '1'}},
          {__jsPageChildPageParam: {_type: 'scout.IdPageParam', id: '2'}}
        ]
      });

      const myPageWithTable = outline.nodes[1] as MyPageWithTable;
      expect(myPageWithTable).toBeInstanceOf(MyPageWithTable);
      myPageWithTable.ensureDetailTable();

      const helper = myPageWithTable.jsPageHelper;
      spyOn(helper, 'callLoadChildPages').and.callFake(async (idsOrPageParams: string | PageParamDo | (string | PageParamDo)[], replace?: boolean): Promise<void> => {
        helper._addChildPagesToIdMap();
      });

      spyOn(outlineAdapter, 'sendNodesChanged').and.callFake((nodes: TreeNode[]) => undefined);

      await myPageWithTable.ensureLoadChildren();

      const [page1, page2] = helper.findChildPages(['1', '2']);
      expect(outlineAdapter.sendNodesChanged).toHaveBeenCalledWith([page1, page2]);
    });
  });

  describe('callLoadChildPages', () => {
    let hybridManager: HybridManager;
    let hybridManagerAdapter: HybridManagerAdapter;

    beforeEach(() => {
      hybridManager = HybridManager.get(session);
      linkWidgetAndAdapter(hybridManager, HybridManagerAdapter);
      hybridManagerAdapter = hybridManager.modelAdapter as HybridManagerAdapter;

      jasmine.Ajax.install();
    });

    afterEach(() => {
      jasmine.Ajax.uninstall();
    });

    it('loads child pages using page params', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      // single page param

      const idSingle = '7';
      UuidPool.get(session).uuids.push(idSingle);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages([scout.create(MyPageParamDo, {id: 'foo'})]);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idSingle,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'jsPageHelper.MyPageParam', id: 'foo'}
          ],
          replace: true
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));

      // multiple page params

      const idMultiple = '13';
      UuidPool.get(session).uuids.push(idMultiple);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages([scout.create(MyPageParamDo, {id: 'foo'}), scout.create(MyPageParamDo, {id: 'bar'})]);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idMultiple,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'jsPageHelper.MyPageParam', id: 'foo'},
            {_type: 'jsPageHelper.MyPageParam', id: 'bar'}
          ],
          replace: true
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));

      // multiple page params

      const idDuplicate = '42';
      UuidPool.get(session).uuids.push(idDuplicate);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages([scout.create(MyPageParamDo, {id: 'foo'}), scout.create(MyPageParamDo, {id: 'bar'}), scout.create(MyPageParamDo, {id: 'foo'})]);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idDuplicate,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'jsPageHelper.MyPageParam', id: 'foo'},
            {_type: 'jsPageHelper.MyPageParam', id: 'bar'}
          ],
          replace: true
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));
    });

    it('loads child pages using ids', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      // single id

      const idSingle = '7';
      UuidPool.get(session).uuids.push(idSingle);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages(['foo']);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idSingle,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'scout.IdPageParam', id: 'foo'}
          ],
          replace: true
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));

      // multiple ids

      const idMultiple = '13';
      UuidPool.get(session).uuids.push(idMultiple);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages(['foo', 'bar']);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idMultiple,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'scout.IdPageParam', id: 'foo'},
            {_type: 'scout.IdPageParam', id: 'bar'}
          ],
          replace: true
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));

      // duplicate ids

      const idDuplicate = '42';
      UuidPool.get(session).uuids.push(idDuplicate);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages(['foo', 'bar', 'foo']);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idDuplicate,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'scout.IdPageParam', id: 'foo'},
            {_type: 'scout.IdPageParam', id: 'bar'}
          ],
          replace: true
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));
    });

    it('loads child pages using page params and ids', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      // id and page param mixed

      const idMixed = '42';
      UuidPool.get(session).uuids.push(idMixed);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages(['foo', scout.create(MyPageParamDo, {id: 'bar'})]);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idMixed,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'scout.IdPageParam', id: 'foo'},
            {_type: 'jsPageHelper.MyPageParam', id: 'bar'}
          ],
          replace: true
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));
    });

    it('loads child pages passing the replace flag', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      // explicitly disable replace

      const idFalse = '13';
      UuidPool.get(session).uuids.push(idFalse);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages(['foo'], false);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idFalse,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'scout.IdPageParam', id: 'foo'}
          ],
          replace: false
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));

      // explicitly enable replace

      const idTrue = '13';
      UuidPool.get(session).uuids.push(idTrue);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages(['foo'], true);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idTrue,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'scout.IdPageParam', id: 'foo'}
          ],
          replace: true
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));
    });

    it('loads child pages without replace if child pages are present during initialisation', async () => {
      outline.insertNode({
        ...outlineSpecHelper.createModelNode(null, 'bar'),
        __jsPageChildPageParam: {_type: 'scout.IdPageParam', id: 'bar'}
      }, page);

      const helper = scout.create(SpecJsPageHelper, {page});

      // if there are child pages during initialisation they are not replaced

      const idFirst = '13';
      UuidPool.get(session).uuids.push(idFirst);
      const callLoadChildPagesPromise = helper.callLoadChildPages(['foo']);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idFirst,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'scout.IdPageParam', id: 'foo'}
          ],
          replace: false
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));

      // trigger the hybridActionEnd-event to resolve the promise
      hybridManager.trigger(`hybridActionEnd:${idFirst}`);

      await callLoadChildPagesPromise;

      // every following call will replace child pages

      const idSecond = '42';
      UuidPool.get(session).uuids.push(idSecond);
      // noinspection ES6MissingAwait
      helper.callLoadChildPages(['foo']);

      await hybridManager.when('hybridAction');

      expect(sendQueuedCallsAndGetMostRecentRequest()).toContainEvents(new RemoteEvent(hybridManagerAdapter.id, 'hybridAction', {
        actionType: 'scout.LoadChildPages',
        id: idSecond,
        data: {
          _type: 'scout.LoadChildPagesHybridAction',
          pageParams: [
            {_type: 'scout.IdPageParam', id: 'foo'}
          ],
          replace: true
        },
        contextElements: {
          page: [{widget: outlineAdapter.id, element: page.id}]
        }
      }));
    });

    it('collects child pages into map', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      expect(helper._childPagesById.size).toBe(0);

      const id = '42';
      UuidPool.get(session).uuids.push(id);
      const callLoadChildPagesPromise = helper.callLoadChildPages(['foo', 'bar']);

      await hybridManager.when('hybridAction');

      sendQueuedCalls();
      session._processSuccessResponse({
        events: [
          {
            target: hybridManagerAdapter.id,
            type: 'hybridEvent',
            id,
            eventType: 'hybridActionEnd'
          },
          {
            target: outlineAdapter.id,
            type: 'nodesInserted',
            commonParentNodeId: page.id,
            nodes: [
              {
                ...outlineSpecHelper.createModelNode(null, 'foo'),
                __jsPageChildPageParam: {_type: 'scout.IdPageParam', id: 'foo'}
              },
              {
                ...outlineSpecHelper.createModelNode(null, 'bar'),
                __jsPageChildPageParam: {_type: 'scout.IdPageParam', id: 'bar'}
              }
            ]
          }
        ]
      });

      await callLoadChildPagesPromise;

      expect(helper._childPagesById.size).toBe(2);
    });
  });

  describe('loadChildPages', () => {
    let hybridManager: HybridManager;
    let hybridManagerAdapter: HybridManagerAdapter;

    beforeEach(() => {
      hybridManager = HybridManager.get(session);
      linkWidgetAndAdapter(hybridManager, HybridManagerAdapter);
      hybridManagerAdapter = hybridManager.modelAdapter as HybridManagerAdapter;

      jasmine.Ajax.install();
    });

    afterEach(() => {
      jasmine.Ajax.uninstall();
    });

    it('loads and returns pages using page params', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      const id = '42';
      UuidPool.get(session).uuids.push(id);
      const loadChildPagesPromise = helper.loadChildPages([scout.create(MyPageParamDo, {id: 'foo'}), scout.create(MyPageParamDo, {id: 'bar'})]);

      await hybridManager.when('hybridAction');

      sendQueuedCalls();
      session._processSuccessResponse({
        events: [
          {
            target: hybridManagerAdapter.id,
            type: 'hybridEvent',
            id,
            eventType: 'hybridActionEnd'
          },
          {
            target: outlineAdapter.id,
            type: 'nodesInserted',
            commonParentNodeId: page.id,
            nodes: [
              {
                ...outlineSpecHelper.createModelNode(null, 'foo'),
                __jsPageChildPageParam: {_type: 'jsPageHelper.MyPageParam', id: 'foo'}
              },
              {
                ...outlineSpecHelper.createModelNode(null, 'bar'),
                __jsPageChildPageParam: {_type: 'jsPageHelper.MyPageParam', id: 'bar'}
              }
            ]
          }
        ]
      });

      expect((await loadChildPagesPromise)?.map(page => page.text)).toEqual(['foo', 'bar']);
    });

    it('loads and returns pages using ids', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      const id = '42';
      UuidPool.get(session).uuids.push(id);
      const loadChildPagesPromise = helper.loadChildPages(['foo', 'bar']);

      await hybridManager.when('hybridAction');

      sendQueuedCalls();
      session._processSuccessResponse({
        events: [
          {
            target: hybridManagerAdapter.id,
            type: 'hybridEvent',
            id,
            eventType: 'hybridActionEnd'
          },
          {
            target: outlineAdapter.id,
            type: 'nodesInserted',
            commonParentNodeId: page.id,
            nodes: [
              {
                ...outlineSpecHelper.createModelNode(null, 'foo'),
                __jsPageChildPageParam: {_type: 'scout.IdPageParam', id: 'foo'}
              },
              {
                ...outlineSpecHelper.createModelNode(null, 'bar'),
                __jsPageChildPageParam: {_type: 'scout.IdPageParam', id: 'bar'}
              }
            ]
          }
        ]
      });

      expect((await loadChildPagesPromise)?.map(page => page.text)).toEqual(['foo', 'bar']);
    });

    it('loads and returns pages using page params and ids', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      const id = '42';
      UuidPool.get(session).uuids.push(id);
      const loadChildPagesPromise = helper.loadChildPages([scout.create(MyPageParamDo, {id: 'foo'}), 'bar']);

      await hybridManager.when('hybridAction');

      sendQueuedCalls();
      session._processSuccessResponse({
        events: [
          {
            target: hybridManagerAdapter.id,
            type: 'hybridEvent',
            id,
            eventType: 'hybridActionEnd'
          },
          {
            target: outlineAdapter.id,
            type: 'nodesInserted',
            commonParentNodeId: page.id,
            nodes: [
              {
                ...outlineSpecHelper.createModelNode(null, 'foo'),
                __jsPageChildPageParam: {_type: 'jsPageHelper.MyPageParam', id: 'foo'}
              },
              {
                ...outlineSpecHelper.createModelNode(null, 'bar'),
                __jsPageChildPageParam: {_type: 'scout.IdPageParam', id: 'bar'}
              }
            ]
          }
        ]
      });

      expect((await loadChildPagesPromise)?.map(page => page.text)).toEqual(['foo', 'bar']);
    });
  });

  describe('findChildPages', () => {
    let hybridManager: HybridManager;
    let hybridManagerAdapter: HybridManagerAdapter;

    beforeEach(() => {
      hybridManager = HybridManager.get(session);
      linkWidgetAndAdapter(hybridManager, HybridManagerAdapter);
      hybridManagerAdapter = hybridManager.modelAdapter as HybridManagerAdapter;

      jasmine.Ajax.install();
    });

    afterEach(() => {
      jasmine.Ajax.uninstall();
    });

    it('returns pages previously created using page params', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      const id = '42';
      UuidPool.get(session).uuids.push(id);
      const callLoadChildPagesPromise = helper.callLoadChildPages([scout.create(MyPageParamDo, {id: 'foo'}), scout.create(MyPageParamDo, {id: 'bar'})]);

      await hybridManager.when('hybridAction');

      sendQueuedCalls();
      session._processSuccessResponse({
        events: [
          {
            target: hybridManagerAdapter.id,
            type: 'hybridEvent',
            id,
            eventType: 'hybridActionEnd'
          },
          {
            target: outlineAdapter.id,
            type: 'nodesInserted',
            commonParentNodeId: page.id,
            nodes: [
              {
                ...outlineSpecHelper.createModelNode(null, 'foo'),
                __jsPageChildPageParam: {_type: 'jsPageHelper.MyPageParam', id: 'foo'}
              },
              {
                ...outlineSpecHelper.createModelNode(null, 'bar'),
                __jsPageChildPageParam: {_type: 'jsPageHelper.MyPageParam', id: 'bar'}
              }
            ]
          }
        ]
      });

      await callLoadChildPagesPromise;

      expect(helper.findChildPage(scout.create(MyPageParamDo, {id: 'foo'}))?.text).toBe('foo');
      expect(helper.findChildPages(scout.create(MyPageParamDo, {id: 'bar'}))?.map(page => page.text)).toEqual(['bar']);
      expect(helper.findChildPages([
        scout.create(MyPageParamDo, {id: 'bar'}),
        scout.create(MyPageParamDo, {id: 'foo'})
      ])?.map(page => page.text)).toEqual(['bar', 'foo']);
    });

    it('returns pages previously created using ids', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      const id = '42';
      UuidPool.get(session).uuids.push(id);
      const callLoadChildPagesPromise = helper.callLoadChildPages(['foo', 'bar']);

      await hybridManager.when('hybridAction');

      sendQueuedCalls();
      session._processSuccessResponse({
        events: [
          {
            target: hybridManagerAdapter.id,
            type: 'hybridEvent',
            id,
            eventType: 'hybridActionEnd'
          },
          {
            target: outlineAdapter.id,
            type: 'nodesInserted',
            commonParentNodeId: page.id,
            nodes: [
              {
                ...outlineSpecHelper.createModelNode(null, 'foo'),
                __jsPageChildPageParam: {_type: 'scout.IdPageParam', id: 'foo'}
              },
              {
                ...outlineSpecHelper.createModelNode(null, 'bar'),
                __jsPageChildPageParam: {_type: 'scout.IdPageParam', id: 'bar'}
              }
            ]
          }
        ]
      });

      await callLoadChildPagesPromise;

      expect(helper.findChildPage('foo')?.text).toBe('foo');
      expect(helper.findChildPages('bar')?.map(page => page.text)).toEqual(['bar']);
      expect(helper.findChildPages(['bar', 'foo'])?.map(page => page.text)).toEqual(['bar', 'foo']);
    });

    it('returns pages previously created using page params and ids', async () => {
      const helper = scout.create(SpecJsPageHelper, {page});

      const id = '42';
      UuidPool.get(session).uuids.push(id);
      const callLoadChildPagesPromise = helper.callLoadChildPages([scout.create(MyPageParamDo, {id: 'foo'}), 'bar']);

      await hybridManager.when('hybridAction');

      sendQueuedCalls();
      session._processSuccessResponse({
        events: [
          {
            target: hybridManagerAdapter.id,
            type: 'hybridEvent',
            id,
            eventType: 'hybridActionEnd'
          },
          {
            target: outlineAdapter.id,
            type: 'nodesInserted',
            commonParentNodeId: page.id,
            nodes: [
              {
                ...outlineSpecHelper.createModelNode(null, 'foo'),
                __jsPageChildPageParam: {_type: 'jsPageHelper.MyPageParam', id: 'foo'}
              },
              {
                ...outlineSpecHelper.createModelNode(null, 'bar'),
                __jsPageChildPageParam: {_type: 'scout.IdPageParam', id: 'bar'}
              }
            ]
          }
        ]
      });

      await callLoadChildPagesPromise;

      expect(helper.findChildPage(scout.create(MyPageParamDo, {id: 'foo'}))?.text).toBe('foo');
      expect(helper.findChildPages('bar')?.map(page => page.text)).toEqual(['bar']);
      expect(helper.findChildPages([
        'bar',
        scout.create(MyPageParamDo, {id: 'foo'})
      ])?.map(page => page.text)).toEqual(['bar', 'foo']);
    });
  });
});

function sendQueuedCalls() {
  jasmine.clock().install();
  sendQueuedAjaxCalls();
  jasmine.clock().uninstall();
}

function sendQueuedCallsAndGetMostRecentRequest(): RemoteRequest {
  sendQueuedCalls();
  return mostRecentJsonRequest();
}

class SpecJsPageHelper extends JsPageHelper {
  declare _childPagesById: Map<string, Page>;

  declare _nodesUpdatedHandler: typeof this._onNodesUpdated;
  declare _nodesDeletedHandler: typeof this._onNodesDeleted;
  declare _allChildNodesDeletedHandler: typeof this._onAllChildNodesDeleted;
  declare _nodesInsertedHandler: typeof this._onNodesInserted;
  declare _tableRowsInsertHandler: typeof this._onTableRowsInserted;

  override _addChildPagesToIdMap(childPages?: Page[]) {
    super._addChildPagesToIdMap(childPages);
  }
}

class MyPageWithTable extends PageWithTable {

  jsPageHelper: SpecJsPageHelper;

  protected override _jsonModel(): TreeNodeModel {
    return {
      lazyExpandingEnabled: true,
      detailTable: {
        objectType: Table,
        columns: [
          {
            id: 'IdColumn',
            objectType: Column,
            displayable: false,
            primaryKey: true
          },
          {
            id: 'TextColumn',
            objectType: Column,
            text: 'Text',
            width: 200,
            summary: true
          }
        ]
      }
    };
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.jsPageHelper = scout.create(SpecJsPageHelper, {page: this});
  }

  override destroy() {
    super.destroy();
    this.jsPageHelper.destroy();
  }

  protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
    return $.when(this._loadTableDataAsync(searchFilter));
  }

  protected async _loadTableDataAsync(searchFilter: any): Promise<any> {
    await this.jsPageHelper.callLoadChildPages(['1', '2', '3', '4']);
    return [
      {cells: ['1', 'one']},
      {cells: ['2', 'two']},
      {cells: ['3', 'three']},
      {cells: ['4', 'four']}
    ];
  }

  protected override _createChildPage(row: TableRow): Page {
    const id = this.detailTable.columnById('IdColumn').cellValue(row);
    return this.jsPageHelper.findChildPage(id);
  }
}

class MyPageWithNodes extends PageWithNodes {

  jsPageHelper: SpecJsPageHelper;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.jsPageHelper = scout.create(SpecJsPageHelper, {page: this});
  }

  override destroy() {
    super.destroy();
    this.jsPageHelper.destroy();
  }

  protected override _createChildPages(): JQuery.Promise<Page[]> {
    return $.when(this._createChildPagesAsync());
  }

  protected async _createChildPagesAsync(): Promise<Page[]> {
    let ids = ['1', '2', '3', '4'];
    await this.jsPageHelper.callLoadChildPages(ids);
    return this.jsPageHelper.findChildPages(ids);
  }
}

@typeName('jsPageHelper.MyPageParam')
export class MyPageParamDo extends PageParamDo {
  id: string;
}
