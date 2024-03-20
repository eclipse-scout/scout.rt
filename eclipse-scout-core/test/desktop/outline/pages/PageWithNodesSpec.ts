/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Outline, Page, PageWithNodes, scout} from '../../../../src/index';
import {OutlineSpecHelper, TableSpecHelper} from '../../../../src/testing/index';

describe('PageWithNodes', () => {

  let session: SandboxSession;
  let helper: OutlineSpecHelper;
  let outline: Outline;
  let tableHelper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new OutlineSpecHelper(session);
    outline = helper.createOutline();
    tableHelper = new TableSpecHelper(session);

    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  it('updates the detail table when child pages are changed', () => {
    let page1 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 1'
    });
    outline.insertNode(page1);
    outline.selectNode(page1);
    jasmine.clock().tick(1);

    expect(page1.detailTable).toBeTruthy();
    expect(page1.detailTable.rows.length).toBe(0);

    // Check that pages are linked with table rows
    let page2 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 2'
    });
    let page3 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Pag 3'
    });
    outline.insertNodes([page2, page3], page1);
    expect(page1.detailTable.rows.length).toBe(2);
    expect(page1.detailTable.rows[0].cells[0].text).toBe('Page 2');
    expect(page1.detailTable.rows[1].cells[0].text).toBe('Pag 3');
    expect(page1.detailTable.selectedRows.length).toBe(0);
    expect(page2.row).toBe(page1.detailTable.rows[0]);
    expect(page3.row).toBe(page1.detailTable.rows[1]);
    expect(page2.row.page).toBe(page2);
    expect(page3.row.page).toBe(page3);

    // Check that changes in nodes are propagated to the table
    page3.setText('Page 3');
    outline.updateNode(page3);
    expect(page1.detailTable.rows[1].cells[0].text).toBe('Page 3');
    expect(page1.detailTable.selectedRows.length).toBe(0);

    // Check that selected node pages cause the selection in the detail table of the parent node to be updated
    outline.selectNode(page3);
    jasmine.clock().tick(1);
    expect(page3.detailTable).toBeTruthy();
    expect(page3.detailTable.rows.length).toBe(0);
    expect(page1.detailTable.selectedRows.length).toBe(1); // <--
    expect(page1.detailTable.selectedRows[0]).toBe(page1.detailTable.rows[1]);

    // Check that detail table is also updated if node is not selected
    expect(page2.detailTable).toBeFalsy();
    outline.selectNode(page2); // select to create outline content
    jasmine.clock().tick(1);
    outline.selectNode(page3);
    jasmine.clock().tick(1);
    expect(page2.detailTable.rows.length).toBe(0);
    let page4 = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 4'
    });
    outline.insertNode(page4, page2);
    expect(page2.detailTable.rows.length).toBe(1);
    expect(page2.detailTable.rows[0].cells[0].text).toBe('Page 4');
    expect(page2.detailTable.selectedRows.length).toBe(0);
    outline.deleteAllChildNodes(page2);
    expect(page2.detailTable.rows.length).toBe(0);

    // Check that deleted nodes are no longer selected in the parent page
    outline.deleteNode(page3);
    expect(page1.detailTable.rows.length).toBe(1); // <--
    expect(page1.detailTable.selectedRows.length).toBe(0); // <--
  });

  it('updates childrenLoaded flag', () => {
    let page = scout.create(PageWithNodes, {
      parent: outline,
      text: 'Page 1',
      reloadable: true,
      childNodes: [
        {
          objectType: PageWithNodes,
          text: 'Page 2'
        },
        {
          objectType: PageWithNodes,
          text: 'Page 3'
        }
      ]
    });
    outline.insertNode(page);
    outline.selectNode(page);
    jasmine.clock().tick(1);
    let detailTable = page.detailTable;
    expect(detailTable).toBeTruthy();

    expect(page.childrenLoaded).toBe(true);
    expect(detailTable.loading).toBe(false);

    detailTable.reload();
    expect(page.childrenLoaded).toBe(false);
    expect(detailTable.loading).toBe(false); // currently not implemented for NodePages

    jasmine.clock().tick(1);
    expect(page.childrenLoaded).toBe(true);
    expect(detailTable.loading).toBe(false);
  });

  describe('reloadPage', () => {

    it('does not reload child pages if pages are static', () => {
      let page = scout.create(PageWithNodes, {
        parent: outline,
        text: 'Page 1',
        reloadable: true,
        childNodes: [
          {
            objectType: PageWithNodes,
            text: 'Page 2'
          },
          {
            objectType: PageWithNodes,
            text: 'Page 3'
          }
        ]
      });
      outline.insertNode(page);
      outline.selectNode(page);
      jasmine.clock().tick(1);
      let detailTable = page.detailTable;
      expect(detailTable).toBeTruthy();

      expect(Object.entries(outline.nodesMap).length).toBe(3);
      let cp1a = page.childNodes[0];
      let cp2a = page.childNodes[1];
      expect(cp1a.text).toBe('Page 2');
      expect(cp2a.text).toBe('Page 3');

      expect(page.reloadable).toBe(true);
      detailTable.reload();
      jasmine.clock().tick(1);

      expect(Object.entries(outline.nodesMap).length).toBe(3);
      let cp1b = page.childNodes[0];
      let cp2b = page.childNodes[1];
      expect(cp1b.text).toBe('Page 2');
      expect(cp2b.text).toBe('Page 3');
      expect(cp1b).toBe(cp1a);
      expect(cp2b).toBe(cp2a);
    });

    it('does not reload child pages if reloadable=false', () => {
      let counter = 100;

      class SpecPageWithNodes extends PageWithNodes {
        protected override _createChildPages(): JQuery.Promise<Page[]> {
          let pages = [
            scout.create(PageWithNodes, {
              parent: outline,
              text: 'Page ' + counter++
            }),
            scout.create(PageWithNodes, {
              parent: outline,
              text: 'Page ' + counter++
            })
          ];
          return $.resolvedPromise(pages);
        }
      }

      let page = scout.create(SpecPageWithNodes, {
        parent: outline
      });
      outline.insertNode(page);
      outline.selectNode(page);
      jasmine.clock().tick(1);
      let detailTable = page.detailTable;
      expect(detailTable).toBeTruthy();

      expect(Object.entries(outline.nodesMap).length).toBe(3);
      let cp1a = page.childNodes[0];
      let cp2a = page.childNodes[1];
      expect(cp1a.text).toBe('Page 100');
      expect(cp2a.text).toBe('Page 101');
      expect(detailTable.rows.length).toBe(2);
      expect(detailTable.rows[0].cells[0].text).toBe('Page 100');
      expect(detailTable.rows[1].cells[0].text).toBe('Page 101');

      expect(page.reloadable).toBe(false);
      detailTable.reload();
      jasmine.clock().tick(1);

      expect(Object.entries(outline.nodesMap).length).toBe(3);
      let cp1b = page.childNodes[0];
      let cp2b = page.childNodes[1];
      expect(cp1b.text).toBe('Page 100');
      expect(cp2b.text).toBe('Page 101');
      expect(cp1b).toBe(cp1a);
      expect(cp2b).toBe(cp2a);
      expect(detailTable.rows.length).toBe(2);
      expect(detailTable.rows[0].cells[0].text).toBe('Page 100');
      expect(detailTable.rows[1].cells[0].text).toBe('Page 101');

      // Page _will_ reload if called explicitly
      page.reloadPage();
      jasmine.clock().tick(1);

      expect(Object.entries(outline.nodesMap).length).toBe(3);
      let cp1c = page.childNodes[0];
      let cp2c = page.childNodes[1];
      expect(cp1c.text).toBe('Page 102');
      expect(cp2c.text).toBe('Page 103');
      expect(cp1c).not.toBe(cp1a);
      expect(cp2c).not.toBe(cp2a);
      expect(cp1c).not.toBe(cp1b);
      expect(cp2c).not.toBe(cp2b);
      expect(detailTable.rows.length).toBe(2);
      expect(detailTable.rows[0].cells[0].text).toBe('Page 102');
      expect(detailTable.rows[1].cells[0].text).toBe('Page 103');
    });

    it('reloads child pages if reloadable=true', () => {
      let counter = 100;

      class SpecPageWithNodes extends PageWithNodes {
        protected override _createChildPages(): JQuery.Promise<Page[]> {
          let pages = [
            scout.create(PageWithNodes, {
              parent: outline,
              text: 'Page ' + counter++
            }),
            scout.create(PageWithNodes, {
              parent: outline,
              text: 'Page ' + counter++
            })
          ];
          return $.resolvedPromise(pages);
        }
      }

      let page = scout.create(SpecPageWithNodes, {
        parent: outline,
        reloadable: true
      });
      outline.insertNode(page);
      outline.selectNode(page);
      jasmine.clock().tick(1);
      let detailTable = page.detailTable;
      expect(detailTable).toBeTruthy();

      expect(Object.entries(outline.nodesMap).length).toBe(3);
      let cp1a = page.childNodes[0];
      let cp2a = page.childNodes[1];
      expect(cp1a.text).toBe('Page 100');
      expect(cp2a.text).toBe('Page 101');
      expect(detailTable.rows.length).toBe(2);
      expect(detailTable.rows[0].cells[0].text).toBe('Page 100');
      expect(detailTable.rows[1].cells[0].text).toBe('Page 101');

      expect(page.reloadable).toBe(true);
      detailTable.reload();
      jasmine.clock().tick(1);

      expect(Object.entries(outline.nodesMap).length).toBe(3);
      let cp1b = page.childNodes[0];
      let cp2b = page.childNodes[1];
      expect(cp1b.text).toBe('Page 102');
      expect(cp2b.text).toBe('Page 103');
      expect(cp1b).not.toBe(cp1a);
      expect(cp2b).not.toBe(cp2a);
      expect(detailTable.rows.length).toBe(2);
      expect(detailTable.rows[0].cells[0].text).toBe('Page 102');
      expect(detailTable.rows[1].cells[0].text).toBe('Page 103');

      // It also works when reloading the page directly
      page.reloadPage();
      jasmine.clock().tick(1);

      expect(Object.entries(outline.nodesMap).length).toBe(3);
      let cp1c = page.childNodes[0];
      let cp2c = page.childNodes[1];
      expect(cp1c.text).toBe('Page 104');
      expect(cp2c.text).toBe('Page 105');
      expect(cp1c).not.toBe(cp1a);
      expect(cp2c).not.toBe(cp2a);
      expect(cp1c).not.toBe(cp1b);
      expect(cp2c).not.toBe(cp2b);
      expect(detailTable.rows.length).toBe(2);
      expect(detailTable.rows[0].cells[0].text).toBe('Page 104');
      expect(detailTable.rows[1].cells[0].text).toBe('Page 105');
    });
  });
});
