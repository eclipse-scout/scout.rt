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
  ActivateBookmarkRequest, BaseDoEntity, BookmarkDo, BookmarkSupport, BookmarkTableRowIdentifierDo, BookmarkTableRowIdentifierStringComponentDo, Desktop, NodeBookmarkPageDo, Outline, OutlineBookmarkDefinitionDo, PageBookmarkDefinitionDo,
  PageIdDummyPageParamDo, ResetMenu, scout, SearchMenu, Table, TableBookmarkPageDo, TableTextUserFilter, UuidPool
} from '../../src/index';
import {
  FRUIT_1_KEY, FRUIT_2_KEY, FRUIT_3_KEY, FRUIT_4_KEY, FRUIT_5_KEY, goToOutline, SPEC_NODE_PAGE_1_UUID, SPEC_NODE_PAGE_2_UUID, SPEC_NODE_PAGE_3_UUID, SPEC_NODE_PAGE_4_UUID, SPEC_OUTLINE_1_ID, SPEC_OUTLINE_1_UUID, SPEC_OUTLINE_2_ID,
  SPEC_OUTLINE_2_UUID, SPEC_TABLE_PAGE_1_UUID, SPEC_TABLE_PAGE_2_UUID, SPEC_TABLE_PAGE_3_UUID, specDesktopModel, SpecNodePage1, SpecNodePage2, SpecNodePage3, SpecNodePage4, SpecPageParamDo, SpecSearchDo, SpecSearchForm, SpecTablePage1,
  SpecTablePage2, SpecTablePage3
} from './bookmark-fixtures';

describe('BookmarkSupport', () => {
  let session: SandboxSession;
  let desktop: Desktop;
  let bookmarkSupport: BookmarkSupport;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: specDesktopModel(),
      renderDesktop: false
    });
    desktop = session.desktop;
    bookmarkSupport = BookmarkSupport.get(session);
  });

  // ---------------------------------------------------------------

  describe('createBookmark', () => {

    it('can create an outline-only bookmark', async () => {
      let outline = goToOutline(desktop, SPEC_OUTLINE_2_ID);
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Spec Outline 2');
      expect(outline.nodes.length).toBe(2);
      expect(outline.nodes[0]).toBeInstanceOf(SpecNodePage2);
      expect(outline.nodes[1]).toBeInstanceOf(SpecNodePage3);
      expect(outline.selectedNode()).toBe(null);

      // -----

      let bookmark1 = await bookmarkSupport.createBookmark();
      expect(bookmark1).toBeInstanceOf(BookmarkDo);
      expect(bookmark1.key).toBeUndefined();
      expect(bookmark1.titles).toBeUndefined();
      expect(bookmark1.description).toBeUndefined();
      expect(bookmark1.definition).toBeInstanceOf(OutlineBookmarkDefinitionDo);
      let bookmarkDefinition1 = bookmark1.definition as OutlineBookmarkDefinitionDo;
      expect(bookmarkDefinition1.outlineId).toBe(SPEC_OUTLINE_2_UUID);
      expect(bookmarkDefinition1.pagePath.length).toBe(0);
      expect(bookmarkDefinition1.bookmarkedPage).toBe(null);

      // This case does not really make sense, but we test it anyway
      let bookmark2 = await bookmarkSupport.createBookmark({
        createOutline: false
      });

      expect(bookmark2).toBeInstanceOf(BookmarkDo);
      expect(bookmark2.key).toBeUndefined();
      expect(bookmark2.titles).toBeUndefined();
      expect(bookmark2.description).toBeUndefined();
      expect(bookmark2.definition).toBeInstanceOf(PageBookmarkDefinitionDo); // <--
      let bookmarkDefinition2 = bookmark2.definition as PageBookmarkDefinitionDo;
      expect(bookmarkDefinition2.bookmarkedPage).toBe(null);
    });

    it('can create a page-only bookmark', async () => {
      let outline = goToOutline(desktop, SPEC_OUTLINE_2_ID);
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Spec Outline 2');
      expect(outline.nodes.length).toBe(2);
      expect(outline.nodes[0]).toBeInstanceOf(SpecNodePage2);
      expect(outline.nodes[1]).toBeInstanceOf(SpecNodePage3);
      expect(outline.selectedNode()).toBe(null);

      let page1 = outline.nodes[1] as SpecNodePage3;
      outline.drillDown(page1);
      await page1.ensureLoadChildren();
      expect(page1.childNodes.length).toBe(3);
      expect(page1.childNodes[0]).toBeInstanceOf(SpecNodePage1);
      expect(page1.childNodes[1]).toBeInstanceOf(SpecTablePage2);
      expect(page1.childNodes[2]).toBeInstanceOf(SpecNodePage2);

      let page2 = page1.childNodes[0] as SpecNodePage1;
      outline.drillDown(page2);
      await page2.ensureLoadChildren();

      // -----

      let bookmark = await bookmarkSupport.createBookmark({
        createOutline: false
      });

      expect(bookmark).toBeInstanceOf(BookmarkDo);
      expect(bookmark.key).toBeUndefined();
      expect(bookmark.titles).toBeUndefined();
      expect(bookmark.description).toBeUndefined();
      expect(bookmark.definition).toBeInstanceOf(PageBookmarkDefinitionDo); // <--
      let bookmarkDefinition = bookmark.definition as PageBookmarkDefinitionDo;

      let bookmarkedPage = bookmarkDefinition.bookmarkedPage as NodeBookmarkPageDo;
      expect(bookmarkedPage).toBeInstanceOf(NodeBookmarkPageDo);
      expect(bookmarkedPage.displayText).toBe('Node Page 1');
      expect(bookmarkedPage.pageParam).toBeInstanceOf(PageIdDummyPageParamDo);
      expect((bookmarkedPage.pageParam as PageIdDummyPageParamDo).pageId).toBe(SPEC_NODE_PAGE_1_UUID);
    });

    it('can create a bookmark for a top-level table page', async () => {
      let outline = desktop.outline;
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Spec Outline 1');
      expect(outline.nodes.length).toBe(3);
      expect(outline.nodes[0]).toBeInstanceOf(SpecNodePage1);
      expect(outline.nodes[1]).toBeInstanceOf(SpecTablePage1);
      expect(outline.nodes[2]).toBeInstanceOf(SpecTablePage3);
      expect(outline.selectedNode()).toBe(null);
      let page = scout.assertInstance(outline.nodes[2], SpecTablePage3);
      expect(page.getSearchForm()).toBe(null);
      expect(page.getSearchFilter()).toBe(undefined);
      outline.selectNodes(page);
      expect(page.getSearchForm()).toBeInstanceOf(SpecSearchForm);

      expect(page.detailTable).toBeInstanceOf(Table);
      expect(page.detailTable.loading).toBe(true);
      expect(page.detailTable.rows.length).toBe(0);
      await page.ensureLoadChildren();
      expect(page.detailTable.loading).toBe(false);
      expect(page.detailTable.rows.length).toBe(8);

      // Change search filter
      let searchForm = page.getSearchForm() as SpecSearchForm;
      searchForm.widget('TextField').setValue('red');
      searchForm.widget('SearchMenu').doAction();
      await page.detailTable.when('reload');
      await page.ensureLoadChildren();
      expect(page.detailTable.rows.length).toBe(1);

      // -----

      let bookmark = await bookmarkSupport.createBookmark();

      expect(bookmark).toBeInstanceOf(BookmarkDo);
      expect(bookmark.key).toBeUndefined();
      expect(bookmark.titles).toBeUndefined();
      expect(bookmark.description).toBeUndefined();
      expect(bookmark.definition).toBeInstanceOf(OutlineBookmarkDefinitionDo);
      let bookmarkDefinition = bookmark.definition as OutlineBookmarkDefinitionDo;
      expect(bookmarkDefinition.outlineId).toBe(SPEC_OUTLINE_1_UUID);
      expect(bookmarkDefinition.pagePath).toEqual([]);
      expect(bookmarkDefinition.bookmarkedPage).toBeInstanceOf(TableBookmarkPageDo);
      let bookmarkedPage = bookmarkDefinition.bookmarkedPage as TableBookmarkPageDo;
      expect(bookmarkedPage.displayText).toBe('Table Page 3');
      expect(bookmarkedPage.pageParam).toBeInstanceOf(PageIdDummyPageParamDo);
      expect((bookmarkedPage.pageParam as PageIdDummyPageParamDo).pageId).toBe(SPEC_TABLE_PAGE_3_UUID);
      expect(bookmarkedPage.searchData).toBeInstanceOf(BaseDoEntity);
      expect((bookmarkedPage.searchData as BaseDoEntity).toPojo()).toEqual(scout.create(SpecSearchDo, {text: 'red'}).toPojo());
    });

    it('can create a bookmark for a top-level table page without search form', async () => {
      let outline = desktop.outline;
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Spec Outline 1');
      expect(outline.nodes.length).toBe(3);
      expect(outline.nodes[0]).toBeInstanceOf(SpecNodePage1);
      expect(outline.nodes[1]).toBeInstanceOf(SpecTablePage1);
      expect(outline.nodes[2]).toBeInstanceOf(SpecTablePage3);
      expect(outline.selectedNode()).toBe(null);
      let page = scout.assertInstance(outline.nodes[1], SpecTablePage1);
      outline.selectNodes(page);
      expect(page.detailTable).toBeInstanceOf(Table);
      expect(page.detailTable.loading).toBe(true);
      expect(page.detailTable.rows.length).toBe(0);
      await page.ensureLoadChildren();
      expect(page.detailTable.loading).toBe(false);
      expect(page.detailTable.rows.length).toBe(3);

      // -----

      let bookmark = await bookmarkSupport.createBookmark();

      expect(bookmark).toBeInstanceOf(BookmarkDo);
      expect(bookmark.key).toBeUndefined();
      expect(bookmark.titles).toBeUndefined();
      expect(bookmark.description).toBeUndefined();
      expect(bookmark.definition).toBeInstanceOf(OutlineBookmarkDefinitionDo);
      let bookmarkDefinition = bookmark.definition as OutlineBookmarkDefinitionDo;
      expect(bookmarkDefinition.outlineId).toBe(SPEC_OUTLINE_1_UUID);
      expect(bookmarkDefinition.pagePath).toEqual([]);
      expect(bookmarkDefinition.bookmarkedPage).toBeInstanceOf(TableBookmarkPageDo);
      let bookmarkedPage = bookmarkDefinition.bookmarkedPage as TableBookmarkPageDo;
      expect(bookmarkedPage.displayText).toBe('Table Page 1');
      expect(bookmarkedPage.pageParam).toBeInstanceOf(PageIdDummyPageParamDo);
      expect((bookmarkedPage.pageParam as PageIdDummyPageParamDo).pageId).toBe(SPEC_TABLE_PAGE_1_UUID);
      expect(bookmarkedPage.searchData).toBe(null);
    });

    it('can create a bookmark for a nested node page', async () => {
      let outline = goToOutline(desktop, SPEC_OUTLINE_2_ID);
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Spec Outline 2');
      expect(outline.nodes.length).toBe(2);
      expect(outline.nodes[0]).toBeInstanceOf(SpecNodePage2);
      expect(outline.nodes[1]).toBeInstanceOf(SpecNodePage3);
      expect(outline.selectedNode()).toBe(null);

      let page1 = outline.nodes[1] as SpecNodePage3;
      outline.drillDown(page1);
      await page1.ensureLoadChildren();
      expect(page1.childNodes.length).toBe(3);
      expect(page1.childNodes[0]).toBeInstanceOf(SpecNodePage1);
      expect(page1.childNodes[1]).toBeInstanceOf(SpecTablePage2);
      expect(page1.childNodes[2]).toBeInstanceOf(SpecNodePage2);

      let page2 = page1.childNodes[1] as SpecTablePage2;
      outline.drillDown(page2);
      await page2.ensureLoadChildren();
      expect(page2.detailTable.rows.length).toBe(5);
      page2.setSearchFilter(scout.create(SpecSearchDo, {text: 'i'})); // Matches 'Pineapple' and 'Kiwi'
      page2.reloadPage();
      await page2.ensureLoadChildren();
      expect(page2.detailTable.rows.length).toBe(2);
      expect(page2.childNodes.length).toBe(2);
      expect(page2.childNodes[0]).toBeInstanceOf(SpecNodePage4);
      expect(page2.childNodes[0].pageParam).toBeInstanceOf(SpecPageParamDo);
      expect((page2.childNodes[0].pageParam as SpecPageParamDo).fooId).toBe(FRUIT_3_KEY);
      expect(page2.childNodes[1]).toBeInstanceOf(SpecNodePage4);
      expect(page2.childNodes[1].pageParam).toBeInstanceOf(SpecPageParamDo);
      expect((page2.childNodes[1].pageParam as SpecPageParamDo).fooId).toBe(FRUIT_5_KEY);

      let page3 = page2.childNodes[1] as SpecNodePage4;
      outline.drillDown(page3);
      await page3.ensureLoadChildren();
      expect(page3.childNodes.length).toBe(2);
      expect(page3.childNodes[0]).toBeInstanceOf(SpecNodePage2);
      expect(page3.childNodes[1]).toBeInstanceOf(SpecTablePage2);
      expect(page3.detailTable.rows.length).toBe(2);
      expect(page3.detailTable.rows[0].page).toBe(page3.childNodes[0]);
      expect(page3.detailTable.rows[1].page).toBe(page3.childNodes[1]);
      page3.detailTable.addFilter(scout.create(TableTextUserFilter, {
        session: session,
        table: page3.detailTable,
        text: 'ble' // Matches 'Table Page 2'
      }));
      expect(page3.detailTable.filteredRows().length).toBe(1);
      expect(page3.detailTable.filteredRows()[0].page).toBeInstanceOf(SpecTablePage2);

      let page4 = page3.detailTable.filteredRows()[0].page as SpecTablePage2;
      outline.drillDown(page4);
      await page4.ensureLoadChildren();
      expect(page4.detailTable.rows.length).toBe(5);
      page4.setSearchFilter(scout.create(SpecSearchDo, {text: 'n'})); // Matches 'Banana', 'Pineapple' and 'Lemon'
      page4.reloadPage();
      await page4.ensureLoadChildren();
      expect(page4.detailTable.rows.length).toBe(3);
      expect(page4.childNodes.length).toBe(3);
      expect(page4.childNodes[0]).toBeInstanceOf(SpecNodePage4); // 'Banana'
      expect(page4.childNodes[1]).toBeInstanceOf(SpecNodePage4); // 'Pineapple'
      expect(page4.childNodes[2]).toBeInstanceOf(SpecNodePage4); // 'Lemon'
      page4.detailTable.selectRows([page4.detailTable.rows[0], page4.detailTable.rows[2]]);

      // -----

      let bookmark = await bookmarkSupport.createBookmark();

      expect(bookmark).toBeInstanceOf(BookmarkDo);
      expect(bookmark.key).toBeUndefined();
      expect(bookmark.titles).toBeUndefined();
      expect(bookmark.description).toBeUndefined();
      expect(bookmark.definition).toBeInstanceOf(OutlineBookmarkDefinitionDo);
      let bookmarkDefinition = bookmark.definition as OutlineBookmarkDefinitionDo;
      expect(bookmarkDefinition.outlineId).toBe(SPEC_OUTLINE_2_UUID);
      expect(bookmarkDefinition.pagePath.length).toBe(3);

      let pagePathElement1 = bookmarkDefinition.pagePath[0] as NodeBookmarkPageDo;
      expect(pagePathElement1).toBeInstanceOf(NodeBookmarkPageDo);
      expect(pagePathElement1.displayText).toBe('Node Page 3');
      expect(pagePathElement1.pageParam).toBeInstanceOf(PageIdDummyPageParamDo);
      expect((pagePathElement1.pageParam as PageIdDummyPageParamDo).pageId).toBe(SPEC_NODE_PAGE_3_UUID);
      let pagePathElement2 = bookmarkDefinition.pagePath[1] as TableBookmarkPageDo;
      expect(pagePathElement2).toBeInstanceOf(TableBookmarkPageDo);
      expect(pagePathElement2.displayText).toBe('Table Page 2');
      expect(pagePathElement2.pageParam).toBeInstanceOf(PageIdDummyPageParamDo);
      expect((pagePathElement2.pageParam as PageIdDummyPageParamDo).pageId).toBe(SPEC_TABLE_PAGE_2_UUID);
      expect(pagePathElement2.searchFilterComplete).toBe(true);
      expect(pagePathElement2.searchData).toBeInstanceOf(BaseDoEntity);
      expect((pagePathElement2.searchData as BaseDoEntity).toPojo()).toEqual(scout.create(SpecSearchDo, {text: 'i'}).toPojo());
      expect(pagePathElement2.expandedChildRow).toBeInstanceOf(BookmarkTableRowIdentifierDo);
      expect(pagePathElement2.expandedChildRow.toPojo()).toEqual(scout.create(BookmarkTableRowIdentifierDo, {
        keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_5_KEY})]
      }).toPojo());
      expect(pagePathElement2.selectedChildRows.length).toBe(0); // selected rows are not exported by default
      let pagePathElement3 = bookmarkDefinition.pagePath[2] as NodeBookmarkPageDo;
      expect(pagePathElement3).toBeInstanceOf(NodeBookmarkPageDo);
      expect(pagePathElement3.displayText).toBe('Kiwi');
      expect(pagePathElement3.pageParam).toBeInstanceOf(SpecPageParamDo);
      expect((pagePathElement3.pageParam as SpecPageParamDo).fooId).toBe(FRUIT_5_KEY);

      let bookmarkedPage = bookmarkDefinition.bookmarkedPage as TableBookmarkPageDo;
      expect(bookmarkedPage).toBeInstanceOf(TableBookmarkPageDo);
      expect(bookmarkedPage.displayText).toBe('Table Page 2');
      expect(bookmarkedPage.pageParam).toBeInstanceOf(PageIdDummyPageParamDo);
      expect((bookmarkedPage.pageParam as PageIdDummyPageParamDo).pageId).toBe(SPEC_TABLE_PAGE_2_UUID);
      expect(bookmarkedPage.searchData).toBeInstanceOf(BaseDoEntity);
      expect((bookmarkedPage.searchData as BaseDoEntity).toPojo()).toEqual(scout.create(SpecSearchDo, {text: 'n'}).toPojo());
      expect(bookmarkedPage.expandedChildRow).toBe(null);
      expect(bookmarkedPage.selectedChildRows.length).toBe(0); // selected rows are not exported by default
    });
  });

  describe('openBookmarkInOutline', () => {

    it('can open an outline-only bookmark', async () => {
      // Assert old state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.selectedNode()).toBe(null);

      // Activate bookmark
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_2_UUID,
          bookmarkedPage: null,
          pagePath: []
        })
      });
      await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_2_ID);
      expect(desktop.outline.selectedNode()).toBe(null);
    });

    it('cannot open a page-only bookmark', async () => {
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(PageBookmarkDefinitionDo, {
          bookmarkedPage: scout.create(NodeBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_1_UUID}),
            displayText: 'Node Page 1'
          })
        })
      });
      try {
        await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);
        fail('Expected to fail');
      } catch (error) {
        expect(error).toBe(BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE);
      }
    });

    it('can open a top-level page', async () => {
      // Assert old state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.selectedNode()).toBe(null);
      expect(desktop.outline.nodes.length).toBe(3);

      // Activate bookmark
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_1_UUID,
          bookmarkedPage: scout.create(NodeBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_1_UUID}),
            displayText: 'Node Page 1'
          }),
          pagePath: []
        })
      });
      await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecNodePage1);

      let page = desktop.outline.selectedNode() as SpecNodePage1;
      expect(page.childrenLoaded).toBe(true);
      expect(page.expanded).toBe(true);
      expect(page.childNodes.length).toBe(0);

      expect(page.parentNode).toBeUndefined();
    });

    it('can open a nested page', async () => {
      // Assert old state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.selectedNode()).toBe(null);
      expect(desktop.outline.nodes.length).toBe(3);

      // Activate bookmark
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_2_UUID,
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
            displayText: 'Table Page 2',
            selectedChildRows: [
              scout.create(BookmarkTableRowIdentifierDo, {
                keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_2_KEY})] // Banana
              }),
              scout.create(BookmarkTableRowIdentifierDo, {
                keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_4_KEY})] // Lemon
              })
            ],
            searchFilterComplete: true,
            searchData: scout.create(SpecSearchDo, {text: 'n'})
          }),
          pagePath: [
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_3_UUID}),
              displayText: 'Node Page 3'
            }),
            scout.create(TableBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
              displayText: 'Table Page 2',
              expandedChildRow: scout.create(BookmarkTableRowIdentifierDo, {
                keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_5_KEY})] // Kiwi
              }),
              selectedChildRows: [
                scout.create(BookmarkTableRowIdentifierDo, {
                  keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_5_KEY})] // Kiwi
                })
              ],
              searchFilterComplete: true,
              searchData: scout.create(SpecSearchDo, {text: 'i'})
            }),
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(SpecPageParamDo, {fooId: FRUIT_5_KEY}), // Kiwi
              displayText: 'Kiwi'
            })
          ]
        })
      });
      await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_2_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecTablePage2);

      let page1 = desktop.outline.selectedNode() as SpecTablePage2;
      expect(page1).toBeInstanceOf(SpecTablePage2);
      expect(page1.childrenLoaded).toBe(true);
      expect(page1.expanded).toBe(false);
      expect(page1.childNodes.length).toBe(3);
      expect(page1.detailTable.rows.length).toBe(3);
      expect(page1.detailTable.rows[0].getKeyValues()).toEqual([FRUIT_2_KEY]);
      expect(page1.detailTable.rows[1].getKeyValues()).toEqual([FRUIT_3_KEY]);
      expect(page1.detailTable.rows[2].getKeyValues()).toEqual([FRUIT_4_KEY]);
      expect(page1.detailTable.selectedRows).toEqual([page1.detailTable.rows[0], page1.detailTable.rows[2]]);
      expect((page1.getSearchForm() as SpecSearchForm).widget('TextField').value).toBe('n');

      let page2 = page1.parentNode as SpecNodePage4;
      expect(page2).toBeInstanceOf(SpecNodePage4);
      expect(page2.childrenLoaded).toBe(true);
      expect(page2.expanded).toBe(true);
      expect(page2.childNodes.length).toBe(2); // node pages don't store user filter
      expect(page2.detailTable).toBe(null); // not created yet
      desktop.outline.selectNode(page2);
      expect(page2.detailTable).toBeInstanceOf(Table); // created lazily
      expect(page2.detailTable.rows.length).toEqual(2);

      let page3 = page2.parentNode as SpecTablePage2;
      expect(page3).toBeInstanceOf(SpecTablePage2);
      expect(page3.childrenLoaded).toBe(true);
      expect(page3.expanded).toBe(true);
      expect(page3.childNodes.length).toBe(2);
      expect(page3.detailTable.rows.length).toBe(2);
      expect(page3.detailTable.rows[0].getKeyValues()).toEqual([FRUIT_3_KEY]);
      expect(page3.detailTable.rows[1].getKeyValues()).toEqual([FRUIT_5_KEY]);
      expect(page3.detailTable.selectedRows).toEqual([page3.detailTable.rows[1]]);
      expect((page3.getSearchForm() as SpecSearchForm).widget('TextField').value).toBe('i');

      let page4 = page3.parentNode as SpecNodePage3;
      expect(page4).toBeInstanceOf(SpecNodePage3);
      expect(page4.childrenLoaded).toBe(true);
      expect(page4.expanded).toBe(true);
      expect(page4.childNodes.length).toBe(3);
      expect(page4.detailTable).toBe(null); // not created yet
      desktop.outline.selectNode(page4);
      expect(page4.detailTable).toBeInstanceOf(Table); // created lazily
      expect(page4.detailTable.rows.length).toEqual(3);

      expect(page4.parentNode).toBeUndefined();
    });

    it('shows an error if the outline cannot be found', async () => {
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: UuidPool.ZERO_UUID, // <--
          bookmarkedPage: scout.create(NodeBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_1_UUID}),
            displayText: 'Node Page 1'
          }),
          pagePath: []
        })
      });
      try {
        await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);
        fail('Expected to fail');
      } catch (error) {
        expect(error).toBe(BookmarkSupport.ERROR_OUTLINE_NOT_FOUND);
      }
    });

    it('shows an error if definition type is wrong', async () => {
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(PageBookmarkDefinitionDo, {
          bookmarkedPage: scout.create(NodeBookmarkPageDo)
        })
      });
      try {
        await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);
        fail('Expected to fail');
      } catch (error) {
        expect(error).toBe(BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE);
      }
    });

    it('shows an error if the no page can be found', async () => {
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_1_UUID,
          bookmarkedPage: scout.create(NodeBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_4_UUID}), // <--
            displayText: 'Node Page 4'
          }),
          pagePath: []
        })
      });
      try {
        await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);
        fail('Expected to fail');
      } catch (error) {
        expect(error).toBe(BookmarkSupport.ERROR_PAGE_NOT_FOUND);
      }
    });

    it('stops mid-way if an intermediate page cannot be found', async () => {
      // Assert old state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.selectedNode()).toBe(null);
      expect(desktop.outline.nodes.length).toBe(3);

      // Activate bookmark
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_1_UUID,
          bookmarkedPage: scout.create(NodeBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_3_UUID}),
            displayText: 'Node Page 3'
          }),
          pagePath: [
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_1_UUID}),
              displayText: 'Node Page 1'
            }),
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_2_UUID}),
              displayText: 'Node Page 2'
            })
          ]
        })
      });
      await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecNodePage1);

      let page = desktop.outline.selectedNode() as SpecNodePage1;
      expect(page).toBeInstanceOf(SpecNodePage1);
      expect(page.childrenLoaded).toBe(true);
      expect(page.expanded).toBe(true);
      expect(page.childNodes.length).toBe(0);
      expect(page.detailTable.rows.length).toBe(0);
      expect(page.detailTable.tableStatus.isError()).toBe(true);
    });

    it('can reset a user filter to find the specified child node', async () => {
      // Prepare a filter
      let outline = goToOutline(desktop, SPEC_OUTLINE_2_ID);
      let page1 = scout.assertInstance(outline.nodes[1], SpecNodePage3);
      outline.drillDown(page1);
      await page1.ensureLoadChildren();
      let page2 = scout.assertInstance(page1.childNodes[1], SpecTablePage2);
      outline.drillDown(page2);
      await page2.ensureLoadChildren();
      expect(page2.detailTable.rows.length).toBe(5);
      page2.detailTable.addFilter(scout.create(TableTextUserFilter, {
        session: session,
        table: page2.detailTable,
        text: 'Bana'
      }));
      expect(page2.detailTable.filteredRows().length).toBe(1);
      let page3 = scout.assertInstance(outline.nodes[0], SpecNodePage2);
      outline.drillDown(page3);
      await page3.ensureLoadChildren();

      // Open bookmark
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_2_UUID,
          bookmarkedPage: scout.create(NodeBookmarkPageDo, {
            pageParam: scout.create(SpecPageParamDo, {fooId: FRUIT_5_KEY}), // Kiwi
            displayText: 'Kiwi'
          }),
          pagePath: [
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_3_UUID}),
              displayText: 'Node Page 3'
            }),
            scout.create(TableBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
              displayText: 'Table Page 2',
              expandedChildRow: scout.create(BookmarkTableRowIdentifierDo, {
                keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_5_KEY})] // Kiwi
              }),
              selectedChildRows: [
                scout.create(BookmarkTableRowIdentifierDo, {
                  keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_5_KEY})] // Kiwi
                })
              ],
              searchFilterComplete: true,
              searchData: scout.create(SpecSearchDo, {text: 'i'})
            })
          ]
        })
      });
      await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_2_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecNodePage4);

      let page4 = outline.selectedNode() as SpecNodePage4;
      expect(page4).toBeInstanceOf(SpecNodePage4);
      expect(page4.childrenLoaded).toBe(true);
      expect(page4.expanded).toBe(true);
      expect(page4.childNodes.length).toBe(2);
      expect(page4.detailTable.rows.length).toEqual(2);

      let page5 = page4.parentNode as SpecTablePage2;
      expect(page5).toBeInstanceOf(SpecTablePage2);
      expect(page5.childrenLoaded).toBe(true);
      expect(page5.expanded).toBe(true);
      expect(page5.childNodes.length).toBe(2);
      expect(page5.detailTable.rows.length).toBe(2);
      expect(page5.detailTable.rows[0].getKeyValues()).toEqual([FRUIT_3_KEY]);
      expect(page5.detailTable.rows[1].getKeyValues()).toEqual([FRUIT_5_KEY]);
      expect(page5.detailTable.selectedRows).toEqual([page5.detailTable.rows[1]]);
      expect((page5.getSearchForm() as SpecSearchForm).widget('TextField').value).toBe('i');
      expect(page5.detailTable.getFilter(TableTextUserFilter.TYPE)).toBe(null); // <--

      let page6 = page5.parentNode as SpecNodePage3;
      expect(page6).toBeInstanceOf(SpecNodePage3);
      expect(page6.childrenLoaded).toBe(true);
      expect(page6.expanded).toBe(true);
      expect(page6.childNodes.length).toBe(3);

      expect(page6.parentNode).toBeUndefined();
    });

    it('can open and reset an already loaded page', async () => {
      // Activate bookmark
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_2_UUID,
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
            displayText: 'Table Page 2',
            searchFilterComplete: true,
            searchData: scout.create(SpecSearchDo, {text: 'apple'})
          }),
          pagePath: [
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_3_UUID}),
              displayText: 'Node Page 3'
            })
          ]
        })
      });
      await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_2_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecTablePage2);

      let page1 = desktop.outline.selectedNode() as SpecTablePage2;
      expect(page1.childrenLoaded).toBe(true);
      expect(page1.expanded).toBe(false);
      expect(page1.childNodes.length).toBe(2);
      expect(page1.detailTable.rows.length).toBe(2);
      expect(page1.detailTable.rows[0].getKeyValues()).toEqual([FRUIT_1_KEY]);
      expect(page1.detailTable.rows[1].getKeyValues()).toEqual([FRUIT_3_KEY]);
      let searchForm1 = scout.assertInstance(page1.getSearchForm(), SpecSearchForm);
      expect(searchForm1.widget('TextField').value).toBe('apple');
      let childNode1 = page1.childNodes[0];
      let childNode2 = page1.childNodes[1];

      // Change search filter
      searchForm1.widget('TextField').setValue('le');
      searchForm1.widget('SearchMenu').doAction();
      await page1.detailTable.when('reload');
      await page1.ensureLoadChildren();
      expect(page1.detailTable.rows.length).toBe(3);
      expect(page1.detailTable.rows[0].getKeyValues()).toEqual([FRUIT_1_KEY]);
      expect(page1.detailTable.rows[1].getKeyValues()).toEqual([FRUIT_3_KEY]);
      expect(page1.detailTable.rows[2].getKeyValues()).toEqual([FRUIT_4_KEY]);
      expect(page1.childNodes[0]).not.toBe(childNode1);
      expect(page1.childNodes[1]).not.toBe(childNode2);

      // Open same bookmark again
      await BookmarkSupport.get(session).openBookmarkInOutline(bookmark);

      // Assert same page, but search data has been reset and data reloaded
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecTablePage2);
      let page2 = desktop.outline.selectedNode() as SpecTablePage2;
      expect(page2.childrenLoaded).toBe(true);
      expect(page2.expanded).toBe(false);
      expect(page2.childNodes.length).toBe(2);
      expect(page2.detailTable.rows.length).toBe(2);
      expect(page2.detailTable.rows[0].getKeyValues()).toEqual([FRUIT_1_KEY]);
      expect(page2.detailTable.rows[1].getKeyValues()).toEqual([FRUIT_3_KEY]);
      let searchForm2 = scout.assertInstance(page2.getSearchForm(), SpecSearchForm);
      expect(searchForm2.widget('TextField').value).toBe('apple');
      expect(page2).toBe(page1);
      expect(searchForm2).toBe(searchForm1);
      expect(page1.childNodes[0]).not.toBe(childNode1);
      expect(page1.childNodes[1]).not.toBe(childNode2);
    });
  });

  describe('openBookmarkLocal', () => {

    it('can open a partial bookmark from a given start location', async () => {
      // Go to the start page
      let outline = goToOutline(desktop, SPEC_OUTLINE_2_ID);
      let page1 = scout.assertInstance(outline.nodes[1], SpecNodePage3);
      outline.drillDown(page1);
      await page1.ensureLoadChildren();
      let page2 = scout.assertInstance(page1.childNodes[1], SpecTablePage2);
      outline.drillDown(page2);
      await page2.ensureLoadChildren();
      expect(page2.detailTable.rows.length).toBe(5);

      // Activate bookmark
      let activateBookmarkRequest: ActivateBookmarkRequest = {
        parentOutline: outline,
        parentPage: page2,
        parentBookmarkPage: scout.create(TableBookmarkPageDo, {
          pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
          displayText: 'Table Page 2',
          expandedChildRow: scout.create(BookmarkTableRowIdentifierDo, {
            keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_2_KEY})] // Banana
          }),
          selectedChildRows: [
            scout.create(BookmarkTableRowIdentifierDo, {
              keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_2_KEY})] // Banana
            })
          ],
          searchFilterComplete: true,
          searchData: null
        }),
        pagePath: [
          scout.create(NodeBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_4_UUID}),
            displayText: 'Banana'
          }),
          scout.create(TableBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
            displayText: 'Table Page 2',
            expandedChildRow: scout.create(BookmarkTableRowIdentifierDo, {
              keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_4_KEY})] // Lemon
            }),
            selectedChildRows: [
              scout.create(BookmarkTableRowIdentifierDo, {
                keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_4_KEY})] // Lemon
              })
            ],
            searchFilterComplete: true,
            searchData: scout.create(SpecSearchDo, {text: 'le'})
          }),
          scout.create(NodeBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_4_UUID}),
            displayText: 'Lemon'
          })
        ]
      };
      await BookmarkSupport.get(session).openBookmarkLocal(activateBookmarkRequest);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_2_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecNodePage4);

      let page3 = outline.selectedNode() as SpecNodePage4;
      expect(page3).toBeInstanceOf(SpecNodePage4);
      expect(page3.childrenLoaded).toBe(true);
      expect(page3.expanded).toBe(true);
      expect(page3.childNodes.length).toBe(2);
      expect(page3.detailTable.rows.length).toEqual(2);

      let page4 = page3.parentNode as SpecTablePage2;
      expect(page4).toBeInstanceOf(SpecTablePage2);
      expect(page4.childrenLoaded).toBe(true);
      expect(page4.expanded).toBe(true);
      expect(page4.childNodes.length).toBe(3);
      expect(page4.detailTable.rows.length).toBe(3);
      expect(page4.detailTable.rows[0].getKeyValues()).toEqual([FRUIT_1_KEY]);
      expect(page4.detailTable.rows[1].getKeyValues()).toEqual([FRUIT_3_KEY]);
      expect(page4.detailTable.rows[2].getKeyValues()).toEqual([FRUIT_4_KEY]);
      expect(page4.detailTable.selectedRows).toEqual([page4.detailTable.rows[2]]);
      expect((page4.getSearchForm() as SpecSearchForm).widget('TextField').value).toBe('le');

      let page5 = page4.parentNode as SpecNodePage4;
      expect(page5).toBeInstanceOf(SpecNodePage4);
      expect(page5.childrenLoaded).toBe(true);
      expect(page5.expanded).toBe(true);
      expect(page5.childNodes.length).toBe(2);
      expect(page5.detailTable).toBe(null); // not created yet

      expect(page5.parentNode).toBe(page2); // page2 was the starting point
    });

    it('fails if the given page does not belong to the given outline', async () => {
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.nodes[0]).toBeInstanceOf(SpecNodePage1);
      let page = scout.assertInstance(desktop.outline.nodes[0], SpecNodePage1);

      let outline = goToOutline(desktop, SPEC_OUTLINE_2_ID);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_2_ID);

      // Activate bookmark
      let activateBookmarkRequest: ActivateBookmarkRequest = {
        parentOutline: outline,
        parentPage: page,
        parentBookmarkPage: null, // irrelevant for this test
        pagePath: [] // irrelevant for this test
      };
      try {
        await BookmarkSupport.get(session).openBookmarkLocal(activateBookmarkRequest);
        fail('Expected to fail');
      } catch (error) {
        expect(error).toBe(BookmarkSupport.ERROR_PAGE_WRONG_OUTLINE);
      }
    });
  });

  describe('applyBookmarkToPageAndReload', () => {

    it('replaces the search data and reloads the table', async () => {
      let outline = desktop.outline;
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(outline.nodes[2]).toBeInstanceOf(SpecTablePage3);

      let page = outline.nodes[2] as SpecTablePage3;
      outline.drillDown(page);
      await page.ensureLoadChildren();
      expect(page.detailTable.rows.length).toBe(8);

      let searchForm = scout.assertInstance(page.getSearchForm(), SpecSearchForm);
      searchForm.widget('TextField').setValue('bl'); // Matches 'Black' and 'Blue'
      searchForm.widget('SearchMenu').doAction();
      await page.detailTable.when('reload');
      await page.ensureLoadChildren();
      expect(page.detailTable.rows.length).toBe(2);

      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(PageBookmarkDefinitionDo, {
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            searchData: scout.create(SpecSearchDo, {text: 'yell'})
          })
        })
      });
      await BookmarkSupport.get(session).applyBookmarkToPageAndReload(page, bookmark);

      expect(page.detailTable.rows.length).toBe(1);
      expect(searchForm.widget('TextField').value).toBe('yell');
      searchForm.widget('ResetMenu').doAction();
      await searchForm.when('reset');
      expect(searchForm.widget('TextField').value).toBe('yell'); // <--
    });

    it('applies the search data while allowing the user to reset the search form if saveSearchForm is false', async () => {
      let outline = desktop.outline;
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(outline.nodes[2]).toBeInstanceOf(SpecTablePage3);

      let page = outline.nodes[2] as SpecTablePage3;
      outline.drillDown(page);
      await page.ensureLoadChildren();
      expect(page.detailTable.rows.length).toBe(8);

      let searchForm = scout.assertInstance(page.getSearchForm(), SpecSearchForm);
      expect(searchForm.widget('TextField').value).toBe(null);

      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(PageBookmarkDefinitionDo, {
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            searchData: scout.create(SpecSearchDo, {text: 'cy'})
          })
        })
      });
      await BookmarkSupport.get(session).applyBookmarkToPageAndReload(page, bookmark, false); // <--

      expect(page.detailTable.rows.length).toBe(1);
      expect(searchForm.widget('TextField').value).toBe('cy');
      searchForm.widget('ResetMenu').doAction();
      await searchForm.when('reset');
      expect(searchForm.widget('TextField').value).toBe(null); // <--
    });

    it('can restore table filters', async () => {
      // FIXME bsh: Add test case with filters
      // expect(page2.childNodes.filter(node => node.filterAccepted).length).toBe(1);
      // expect(page2.detailTable.filteredRows().length).toEqual(1);
      // expect(page2.detailTable.getFilter(TableTextUserFilter.TYPE)).toBeInstanceOf(TableTextUserFilter);
      // expect((page2.detailTable.getFilter(TableTextUserFilter.TYPE) as TableTextUserFilter).text).toBe('ble');
      expect().nothing();
    });

    it('can restore chart table control config', async () => {
      // FIXME bsh: Add test case with filters
      // expect(page2.childNodes.filter(node => node.filterAccepted).length).toBe(1);
      // expect(page2.detailTable.filteredRows().length).toEqual(1);
      // expect(page2.detailTable.getFilter(TableTextUserFilter.TYPE)).toBeInstanceOf(TableTextUserFilter);
      // expect((page2.detailTable.getFilter(TableTextUserFilter.TYPE) as TableTextUserFilter).text).toBe('ble');
      expect().nothing();
    });
  });
});
