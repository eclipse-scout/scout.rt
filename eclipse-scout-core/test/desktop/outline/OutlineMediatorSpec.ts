/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, Outline, PageWithNodes, PageWithTable, scout, Table, TableModel, TableTextUserFilter} from '../../../src/index';
import {OutlineSpecHelper, TableSpecHelper} from '../../../src/testing/index';

describe('OutlineMediator', () => {

  let session: SandboxSession;
  let tableModel: TableModel;
  let detailTable: Table;
  let page: PageWithTable;
  let firstColumn;
  let outline: Outline;
  let helper: OutlineSpecHelper;
  let tableHelper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new OutlineSpecHelper(session);
    tableHelper = new TableSpecHelper(session);
    let model = helper.createModelFixture(1, 1, false);
    outline = helper.createOutline(model);

    tableModel = tableHelper.createModelFixture(1, 0);
    detailTable = tableHelper.createTable(tableModel);
    firstColumn = detailTable.columns[0];
    page = scout.create(PageWithTable, {
      childrenLoaded: true, // <-- this flag is important, otherwise this page would try to load children on doRowAction
      alwaysCreateChildPage: true,
      parent: outline,
      detailTable: detailTable
    });
    outline.insertNodes([page], null);
  });

  it('tableRowsInserted', () => {
    detailTable.insertRow(tableHelper.createModelRow('0', ['Foo']));
    expect(page.childNodes.length).toBe(1);
    expect(page.childNodes[0].text).toBe('Foo');
  });

  it('tableRowsDeleted', () => {
    detailTable.insertRow(tableHelper.createModelRow('0', ['Foo']));
    expect(page.childNodes.length).toBe(1);
    expect(page.childNodes[0].text).toBe('Foo');

    let firstRow = detailTable.rows[0];
    detailTable.deleteRow(firstRow);
    expect(page.childNodes.length).toBe(0);
  });

  it('tableRowsUpdated', () => {
    detailTable.insertRow(tableHelper.createModelRow('0', ['Foo']));
    expect(page.childNodes.length).toBe(1);
    expect(page.childNodes[0].text).toBe('Foo');

    let firstRow = detailTable.rows[0];
    detailTable.setCellValue(firstColumn, firstRow, 'Bar');
    expect(page.childNodes.length).toBe(1);
    expect(page.childNodes[0].text).toBe('Bar');
  });

  it('tableRowAction', () => {
    detailTable.insertRow(tableHelper.createModelRow('0', ['Foo']));
    let firstRow = detailTable.rows[0];
    let pageForRow = firstRow.page;

    expect(page.expanded).toBe(false);
    expect(outline.selectedNode()).toBe(null);

    detailTable.selectRows([firstRow]);
    detailTable.doRowAction(firstRow, firstColumn);

    expect(page.expanded).toBe(true);
    expect(outline.selectedNode()).toBe(pageForRow);
  });

  it('tableRowOrderChanged', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    let modelRows = [
      tableHelper.createModelRow('0', ['Foo']),
      tableHelper.createModelRow('1', ['Bar'])
    ];
    detailTable.insertRows(modelRows);
    expect(page.childNodes[0].text).toBe('Foo');
    expect(page.childNodes[1].text).toBe('Bar');

    detailTable.sort(firstColumn, 'asc');
    expect(page.childNodes[0].text).toBe('Bar');
    expect(page.childNodes[1].text).toBe('Foo');
  });

  it('tableRowsFiltered', () => {
    let modelRows = [
      tableHelper.createModelRow('0', ['Foo']),
      tableHelper.createModelRow('1', ['Bar'])
    ];
    detailTable.insertRows(modelRows);
    outline.expandNode(page);

    expect(page.childNodes.length).toBe(2);
    let filter = scout.create(TableTextUserFilter, {
      session: session,
      table: detailTable,
      text: 'bar'
    });
    detailTable.addFilter(filter);

    expect(page.childNodes.length).toBe(2); // still 2, but
    expect(page.childNodes[0].filterAccepted).toBe(false); // filter is not accepted for 'Foo'
    expect(page.childNodes[1].filterAccepted).toBe(true); // filter is accepted for 'Bar'
  });

  it('onPageSelected', () => {
    const modelRows = [
      tableHelper.createModelRow('0', ['Foo']),
      tableHelper.createModelRow('1', ['Bar'])
    ];

    detailTable.insertRows(modelRows);

    const row0 = detailTable.rows[0];
    const row1 = detailTable.rows[1];
    const page0 = row0.page;
    const page1 = row1.page;

    expect(detailTable.selectedRows).toEqual([]);

    outline.selectNodes(page1);
    expect(detailTable.selectedRows).toEqual([row1]);

    outline.selectNodes(page0);
    expect(detailTable.selectedRows).toEqual([row0]);

    outline.selectNodes(null);
    expect(detailTable.selectedRows).toEqual([row0]);
  });

  it('onChildPagesChanged', () => {
    let spy = spyOn(outline.mediator, 'onChildPagesChanged').and.callThrough();

    const modelRows = [
      tableHelper.createModelRow('0', ['Foo']),
      tableHelper.createModelRow('1', ['Bar'])
    ];
    detailTable.insertRows(modelRows);
    expect(spy).toHaveBeenCalledOnceWith(page);
    spy.calls.reset();

    let page1 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 1'
    });
    outline.insertNode(page1);
    expect(spy).not.toHaveBeenCalled();

    let page2 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Pag 2'
    });
    outline.insertNode(page2, page1);
    expect(spy).toHaveBeenCalledOnceWith(page1);
    spy.calls.reset();

    page2.setText('Page 2');
    outline.updateNode(page2);
    expect(spy).toHaveBeenCalledOnceWith(page1);
    spy.calls.reset();

    let page3 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 3'
    });
    let page4 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 4'
    });
    outline.insertNodes([page3, page4], page2);
    expect(spy).toHaveBeenCalledOnceWith(page2);
    spy.calls.reset();

    outline.deleteNode(page3);
    expect(spy).toHaveBeenCalledOnceWith(page2);
    spy.calls.reset();

    outline.deleteNodes(page4);
    expect(spy).toHaveBeenCalledOnceWith(page2);
    spy.calls.reset();

    let page5 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 5'
    });
    let page6 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 6'
    });
    let page7 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 7'
    });
    let page8 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 8'
    });
    outline.insertNodes([page5, page6], page2);
    outline.insertNodes([page7, page8], page6);
    spy.calls.reset();

    outline.deleteAllChildNodes(page6);
    expect(spy).toHaveBeenCalledOnceWith(page6);
    spy.calls.reset();

    outline.deleteNodes(page2);
    expect(spy).toHaveBeenCalledOnceWith(page1);
    spy.calls.reset();
  });
});
