/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {
  ActivateBookmarkPathParam, BaseDoEntity, BookmarkDo, BookmarkDoBuilder, BookmarkSupport, BookmarkTableRowIdentifierDo, BookmarkTableRowIdentifierStringComponentDo, BooleanColumn, Column, dates, Desktop, NodeBookmarkPageDo, NumberColumn,
  NumberColumnUserFilter, NumberColumnUserFilterStateDo, Outline, OutlineBookmarkDefinitionDo, PageBookmarkDefinitionDo, PageIdDummyPageParamDo, ResetMenu, scout, SearchMenu, Table, TableBookmarkPageDo, TableClientUiPreferenceProfileDo,
  TableClientUiPreferencesDo, TableColumnClientUiPreferenceDo, TableTextUserFilter, TableTextUserFilterStateDo, TableUiPreferences, TreeNodesInsertedEvent, UuidPool
} from '../../src/index';
import {
  createSpecSearchDo, FRUIT_1_KEY, FRUIT_2_KEY, FRUIT_3_KEY, FRUIT_4_KEY, FRUIT_5_KEY, goToOutline, SPEC_NODE_PAGE_1_UUID, SPEC_NODE_PAGE_2_UUID, SPEC_NODE_PAGE_3_UUID, SPEC_NODE_PAGE_4_UUID, SPEC_OUTLINE_1_ID, SPEC_OUTLINE_1_UUID,
  SPEC_OUTLINE_2_ID, SPEC_OUTLINE_2_UUID, SPEC_OUTLINE_3_ID, SPEC_OUTLINE_3_UUID, SPEC_TABLE_PAGE_1_UUID, SPEC_TABLE_PAGE_2_UUID, SPEC_TABLE_PAGE_3_TABLE_COLUMN_1_UUID, SPEC_TABLE_PAGE_3_TABLE_COLUMN_2_UUID,
  SPEC_TABLE_PAGE_3_TABLE_COLUMN_3_UUID, SPEC_TABLE_PAGE_3_TABLE_COLUMN_4_UUID, SPEC_TABLE_PAGE_3_TABLE_COLUMN_5_UUID, SPEC_TABLE_PAGE_3_TABLE_UUID, SPEC_TABLE_PAGE_3_UUID, specDesktopModel, SpecNodePage1, SpecNodePage2, SpecNodePage3,
  SpecNodePage4, SpecPageParamDo, SpecSearchDo, SpecSearchForm, SpecTablePage1, SpecTablePage2, SpecTablePage3
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

    session.textMap.add('Yes', 'Yes');
    session.textMap.add('No', 'No');
  });

  // ---------------------------------------------------------------

  describe('createBookmark', () => {

    it('can create an outline-only bookmark', async () => {
      let outline = goToOutline(desktop, SPEC_OUTLINE_2_ID);
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Outline 2');
      expect(outline.nodes.length).toBe(2);
      expect(outline.nodes[0]).toBeInstanceOf(SpecNodePage2);
      expect(outline.nodes[1]).toBeInstanceOf(SpecNodePage3);
      expect(outline.selectedNode()).toBe(null);

      // -----

      let bookmark1 = await bookmarkSupport.createBookmark() as BookmarkDo;

      expect(bookmark1).toBeInstanceOf(BookmarkDo);
      expect(bookmark1.id).toBeUndefined();
      expect(bookmark1.title).toBe('Outline 2');
      expect(bookmark1.description).toBeUndefined();
      expect(bookmark1.definition).toBeInstanceOf(OutlineBookmarkDefinitionDo);
      let bookmarkDefinition1 = bookmark1.definition as OutlineBookmarkDefinitionDo;
      expect(bookmarkDefinition1.outlineId).toBe(SPEC_OUTLINE_2_UUID);
      expect(bookmarkDefinition1.pagePath.length).toBe(0);
      expect(bookmarkDefinition1.bookmarkedPage).toBe(null);

      // This case does not really make sense, but we test it anyway
      let bookmark2 = await bookmarkSupport.createBookmark({
        createOutline: false
      }) as BookmarkDo;

      expect(bookmark2).toBeInstanceOf(BookmarkDo);
      expect(bookmark1.id).toBeUndefined();
      expect(bookmark1.title).toBe('Outline 2');
      expect(bookmark1.description).toBeUndefined();
      expect(bookmark2.definition).toBeInstanceOf(PageBookmarkDefinitionDo); // <--
      let bookmarkDefinition2 = bookmark2.definition as PageBookmarkDefinitionDo;
      expect(bookmarkDefinition2.bookmarkedPage).toBe(null);
    });

    it('can create a page-only bookmark', async () => {
      let outline = goToOutline(desktop, SPEC_OUTLINE_2_ID);
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Outline 2');
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
      }) as BookmarkDo;

      expect(bookmark).toBeInstanceOf(BookmarkDo);
      expect(bookmark.id).toBeUndefined();
      expect(bookmark.title).toBe('Node Page 1');
      expect(bookmark.description).toBe('Node Page 1');
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
      expect(outline.title).toBe('Outline 1');
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
      page.ensureLoadChildren();
      await page.detailTable.when('propertyChange:loading');
      expect(page.detailTable.rows.length).toBe(8);

      // Change search filter
      let searchForm = page.getSearchForm() as SpecSearchForm;
      searchForm.widget('TextField').setValue('n'); // 'Green', 'Magenta', 'Cyan'
      searchForm.widget('SearchMenu').doAction();
      await page.detailTable.when('reload');
      await page.ensureLoadChildren();
      expect(page.detailTable.rows.length).toBe(3);
      expect(page.detailTable.visibleRows.length).toBe(3);

      // Add table filter
      let textFilter = scout.create(TableTextUserFilter, {
        session: session,
        table: page.detailTable,
        text: 'a' // 'Magenta', 'Cyan'
      });
      page.detailTable.addFilter(textFilter);
      expect(page.detailTable.rows.length).toBe(3);
      expect(page.detailTable.visibleRows.length).toBe(2);

      // Change column settings
      let colorColumn = scout.assertInstance(page.detailTable.columnById('ColorColumn'), Column);
      let hexColumn = scout.assertInstance(page.detailTable.columnById('HexColumn'), Column);
      let primaryColumn = scout.assertInstance(page.detailTable.columnById('PrimaryColumn'), BooleanColumn);
      let usageColumn = scout.assertInstance(page.detailTable.columnById('UsageColumn'), NumberColumn);
      page.detailTable.moveColumn(colorColumn, 1);
      colorColumn.setWidth(333);
      primaryColumn.setWidth(77);
      primaryColumn.setVisible(false);

      let columnFilter = scout.create(NumberColumnUserFilter, {
        session: session,
        table: page.detailTable,
        column: usageColumn,
        numberFrom: 10 // 'Magenta'
      });
      page.detailTable.addFilter(columnFilter);
      expect(page.detailTable.rows.length).toBe(3);
      expect(page.detailTable.visibleRows.length).toBe(1);

      page.detailTable.sort(hexColumn);

      // -----

      let bookmark = await bookmarkSupport.createBookmark() as BookmarkDo;

      expect(bookmark).toBeInstanceOf(BookmarkDo);
      expect(bookmark.id).toBeUndefined();
      expect(bookmark.title).toBe('Outline 1 - Table Page 3');
      expect(bookmark.description).toBe('Table Page 3\n  Text: n\n  Show hidden values: No');
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
      expect((bookmarkedPage.searchData as BaseDoEntity).toPojo()).toEqual(scout.create(SpecSearchDo, {
        text: 'n', showHiddenValues: false, languages: [], creationDate: null
      }).toPojo());

      expect(bookmarkedPage.tablePreferences).toBeTruthy();
      expect(bookmarkedPage.tablePreferences.tableId).toBe(`${SPEC_TABLE_PAGE_3_TABLE_UUID}|${SPEC_TABLE_PAGE_3_UUID}`);
      expect(bookmarkedPage.tablePreferences.userPreferenceContext).toBe(undefined);
      expect(bookmarkedPage.tablePreferences.tileMode).toBe(false);
      expect(bookmarkedPage.tablePreferences.tileGlobalKey).toBe(undefined);
      expect(bookmarkedPage.tablePreferences.tablePreferenceProfiles).toBeInstanceOf(Map);
      expect(bookmarkedPage.tablePreferences.tablePreferenceProfiles.size).toBe(1);
      let bookmarkedTableProfile = bookmarkedPage.tablePreferences.tablePreferenceProfiles.get(TableUiPreferences.PROFILE_ID_BOOKMARK);
      expect(bookmarkedTableProfile).toBeTruthy();
      expect(bookmarkedTableProfile.tableCustomizerData).toBe(undefined);
      expect(bookmarkedTableProfile.columns).toEqual([
        scout.create(TableColumnClientUiPreferenceDo, {
          columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_3_UUID, // HexColumn
          viewIndex: 0,
          visible: true,
          width: 100,
          sortOrder: 0,
          sortAscending: true,
          groupingActive: false
        }),
        scout.create(TableColumnClientUiPreferenceDo, {
          columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_2_UUID, // ColorColumn
          viewIndex: 1,
          visible: true,
          width: 333,
          sortOrder: -1,
          sortAscending: true,
          groupingActive: false
        }),
        scout.create(TableColumnClientUiPreferenceDo, {
          columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_4_UUID, // PrimaryColumn
          viewIndex: 2,
          visible: false,
          width: 77,
          sortOrder: -1,
          sortAscending: false,
          groupingActive: false
        }),
        scout.create(TableColumnClientUiPreferenceDo, {
          columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_5_UUID, // UsageColumn
          viewIndex: 3,
          visible: true,
          width: 60,
          sortOrder: -1,
          sortAscending: true,
          groupingActive: false,
          aggregationFunctionId: 'sum',
          backgroundEffectId: null
        })
      ]);
      expect(bookmarkedTableProfile.userFilters).toEqual([
        scout.create(TableTextUserFilterStateDo, {
          text: 'a'
        }),
        scout.create(NumberColumnUserFilterStateDo, {
          columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_5_UUID,
          selectedValues: new Set(),
          numberFrom: 10,
          numberTo: null
        })
      ]);
    });

    it('can create a bookmark for a top-level table page without search form', async () => {
      let outline = desktop.outline;
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Outline 1');
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
      page.ensureLoadChildren();
      await page.detailTable.when('propertyChange:loading');
      expect(page.detailTable.rows.length).toBe(3);

      // -----

      let bookmark = await bookmarkSupport.createBookmark() as BookmarkDo;

      expect(bookmark).toBeInstanceOf(BookmarkDo);
      expect(bookmark.id).toBeUndefined();
      expect(bookmark.title).toBe('Outline 1 - Table Page 1');
      expect(bookmark.description).toBe('Table Page 1');
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
      expect(outline.title).toBe('Outline 2');
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
      page2.setSearchFilter(scout.create(SpecSearchDo, {text: 'i', showHiddenValues: true})); // Matches 'Pineapple' and 'Kiwi'
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
      jasmine.clock().install();
      page4.setSearchFilter(scout.create(SpecSearchDo, {text: 'n', languages: [100, 300], creationDate: dates.create('1999-12-31')})); // Matches 'Banana', 'Pineapple' and 'Lemon'
      jasmine.clock().tick(300); // wait for list box update the display text
      jasmine.clock().uninstall();
      page4.reloadPage();
      await page4.ensureLoadChildren();
      expect(page4.detailTable.rows.length).toBe(3);
      expect(page4.childNodes.length).toBe(3);
      expect(page4.childNodes[0]).toBeInstanceOf(SpecNodePage4); // 'Banana'
      expect(page4.childNodes[1]).toBeInstanceOf(SpecNodePage4); // 'Pineapple'
      expect(page4.childNodes[2]).toBeInstanceOf(SpecNodePage4); // 'Lemon'
      page4.detailTable.selectRows([page4.detailTable.rows[0], page4.detailTable.rows[2]]);

      // -----

      let bookmark = await bookmarkSupport.createBookmark() as BookmarkDo;

      expect(bookmark).toBeInstanceOf(BookmarkDo);
      expect(bookmark.id).toBeUndefined();
      expect(bookmark.title).toBe('Outline 2 - Node Page 3 - Table Page 2 - Kiwi - Table Page 2');
      expect(bookmark.description).toBe('' +
        'Node Page 3\n' +
        '  Table Page 2\n' +
        '    Text: i\n' +
        '    Show hidden values: Yes\n' +
        '    Kiwi\n' +
        '      Table Page 2\n' +
        '        Text: n\n' +
        '        Show hidden values: No\n' +
        '        Languages: English, Italian\n' +
        '        Creation date: 31.12.1999');
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
      expect((pagePathElement2.searchData as BaseDoEntity).toPojo()).toEqual(scout.create(SpecSearchDo, {
        text: 'i', showHiddenValues: true, languages: [], creationDate: null
      }).toPojo());
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
      expect((bookmarkedPage.searchData as BaseDoEntity).toPojo()).toEqual(scout.create(SpecSearchDo, {
        text: 'n', showHiddenValues: false, languages: [100, 300], creationDate: dates.create('1999-12-31')
      }).toPojo());
      expect(bookmarkedPage.expandedChildRow).toBe(null);
      expect(bookmarkedPage.selectedChildRows.length).toBe(0); // selected rows are not exported by default
    });

    it('can handle errors', async () => {
      // Provoke 'missing-outline' error
      desktop.setOutline(null);

      let spy = spyOn(bookmarkSupport, 'handleCreateBookmarkError').and.returnValue($.resolvedPromise());

      // a) handleError=true (default) --> error should be handled internally
      let bookmark = await bookmarkSupport.createBookmark();
      expect(bookmarkSupport.handleCreateBookmarkError).toHaveBeenCalledWith(BookmarkDoBuilder.ERROR_MISSING_OUTLINE);
      expect(bookmark).toBe(null);
      spy.calls.reset();

      // b) handleError=false (default) --> error should be thrown
      try {
        await bookmarkSupport.createBookmark(undefined, {
          handleErrors: false
        });
        fail('Expected to fail');
      } catch (error) {
        expect(error).toBe(BookmarkDoBuilder.ERROR_MISSING_OUTLINE);
      }
      expect(bookmarkSupport.handleCreateBookmarkError).not.toHaveBeenCalled();
    });

    it('does not include invisible root node in page path', async () => {
      let outline = goToOutline(desktop, SPEC_OUTLINE_3_ID);
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Outline 3');
      expect(outline.nodes.length).toBe(1);
      expect(outline.nodes[0]).toBeInstanceOf(SpecNodePage4);
      expect(outline.nodes[0].compactRoot).toBe(true); // <--
      expect(outline.selectedNode()).toBe(null);

      let page1 = outline.nodes[0] as SpecNodePage4;
      outline.drillDown(page1);
      await page1.ensureLoadChildren();
      expect(page1.childNodes.length).toBe(2);
      expect(page1.childNodes[0]).toBeInstanceOf(SpecNodePage2);
      expect(page1.childNodes[0].compactRoot).toBe(false);
      expect(page1.childNodes[1]).toBeInstanceOf(SpecTablePage2);
      expect(page1.childNodes[1].compactRoot).toBe(false);

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
      expect(page2.childNodes[0].compactRoot).toBe(false);
      expect(page2.childNodes[0].pageParam).toBeInstanceOf(SpecPageParamDo);
      expect((page2.childNodes[0].pageParam as SpecPageParamDo).fooId).toBe(FRUIT_3_KEY);
      expect(page2.childNodes[1]).toBeInstanceOf(SpecNodePage4);
      expect(page2.childNodes[1].pageParam).toBeInstanceOf(SpecPageParamDo);
      expect((page2.childNodes[1].pageParam as SpecPageParamDo).fooId).toBe(FRUIT_5_KEY);

      let page3 = page2.childNodes[1] as SpecNodePage4;
      outline.drillDown(page3);
      await page3.ensureLoadChildren();

      // -----

      let bookmark = await bookmarkSupport.createBookmark() as BookmarkDo;

      expect(bookmark).toBeInstanceOf(BookmarkDo);
      expect(bookmark.id).toBeUndefined();
      expect(bookmark.title).toBe('Outline 3 - Table Page 2 - Kiwi');
      expect(bookmark.description).toBe('Table Page 2\n  Text: i\n  Show hidden values: No\n  Kiwi');
      expect(bookmark.definition).toBeInstanceOf(OutlineBookmarkDefinitionDo);
      let bookmarkDefinition = bookmark.definition as OutlineBookmarkDefinitionDo;
      expect(bookmarkDefinition.outlineId).toBe(SPEC_OUTLINE_3_UUID);
      expect(bookmarkDefinition.pagePath.length).toBe(1); // <--

      let pagePathElement1 = bookmarkDefinition.pagePath[0] as TableBookmarkPageDo;
      expect(pagePathElement1).toBeInstanceOf(TableBookmarkPageDo);
      expect(pagePathElement1.displayText).toBe('Table Page 2');
      expect(pagePathElement1.pageParam).toBeInstanceOf(PageIdDummyPageParamDo);
      expect((pagePathElement1.pageParam as PageIdDummyPageParamDo).pageId).toBe(SPEC_TABLE_PAGE_2_UUID);
      expect(pagePathElement1.searchFilterComplete).toBe(true);
      expect(pagePathElement1.searchData).toBeInstanceOf(BaseDoEntity);
      expect((pagePathElement1.searchData as BaseDoEntity).toPojo()).toEqual(scout.create(SpecSearchDo, {
        text: 'i', showHiddenValues: false, languages: [], creationDate: null
      }).toPojo());
      expect(pagePathElement1.expandedChildRow).toBeInstanceOf(BookmarkTableRowIdentifierDo);
      expect(pagePathElement1.expandedChildRow.toPojo()).toEqual(scout.create(BookmarkTableRowIdentifierDo, {
        keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_5_KEY})]
      }).toPojo());
      expect(pagePathElement1.selectedChildRows.length).toBe(0); // selected rows are not exported by default

      let bookmarkedPage = bookmarkDefinition.bookmarkedPage as NodeBookmarkPageDo;
      expect(bookmarkedPage).toBeInstanceOf(NodeBookmarkPageDo);
      expect(bookmarkedPage.displayText).toBe('Kiwi');
      expect(bookmarkedPage.pageParam).toBeInstanceOf(SpecPageParamDo);
      expect((bookmarkedPage.pageParam as SpecPageParamDo).fooId).toBe(FRUIT_5_KEY);
    });
  });

  describe('activateBookmark', () => {

    it('can activate an outline-only bookmark', async () => {
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
      await BookmarkSupport.get(session).activateBookmark(bookmark);

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
        await BookmarkSupport.get(session).activateBookmark(bookmark, {handleErrors: false});
        fail('Expected to fail');
      } catch (error) {
        expect(error).toBe(BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE);
      }
    });

    it('can activate a top-level page', async () => {
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
      await BookmarkSupport.get(session).activateBookmark(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecNodePage1);

      let page = desktop.outline.selectedNode() as SpecNodePage1;
      expect(page.childrenLoaded).toBe(true);
      expect(page.expanded).toBe(true); // Node pages are always expanded, see BookmarkSupport._revealPage
      expect(page.childNodes.length).toBe(0);

      expect(page.parentNode).toBeUndefined();
    });

    it('can restore table ui preferences when activating a table page', async () => {
      // Assert old state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.selectedNode()).toBe(null);
      expect(desktop.outline.nodes.length).toBe(3);

      // Activate bookmark
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_1_UUID,
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_3_UUID}),
            displayText: 'Table Page 3',
            expandedChildRow: null,
            selectedChildRows: [],
            searchFilterComplete: true,
            searchData: scout.create(SpecSearchDo, {text: 'n'}),
            tablePreferences: scout.create(TableClientUiPreferencesDo, {
              tableId: `${SPEC_TABLE_PAGE_3_TABLE_UUID}|${SPEC_TABLE_PAGE_3_UUID}`,
              tileMode: false,
              tablePreferenceProfiles: new Map([
                [TableUiPreferences.PROFILE_ID_BOOKMARK, scout.create(TableClientUiPreferenceProfileDo, {
                  columns: [
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_1_UUID, // KeyColumn
                      viewIndex: 0,
                      visible: false,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_3_UUID, // HexColumn
                      viewIndex: 1,
                      visible: true,
                      width: 100,
                      sortOrder: 0,
                      sortAscending: true,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_2_UUID, // ColorColumn
                      viewIndex: 2,
                      visible: true,
                      width: 333,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_4_UUID, // PrimaryColumn
                      viewIndex: 3,
                      visible: false,
                      width: 77,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_5_UUID, // UsageColumn
                      viewIndex: 4,
                      visible: true,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false,
                      aggregationFunctionId: 'sum',
                      backgroundEffectId: null
                    })
                  ],
                  userFilters: [
                    scout.create(TableTextUserFilterStateDo, {
                      text: 'a'
                    }),
                    scout.create(NumberColumnUserFilterStateDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_5_UUID,
                      selectedValues: new Set(),
                      numberFrom: 10,
                      numberTo: null
                    })
                  ]
                })]
              ])
            })
          }),
          pagePath: []
        })
      });
      await BookmarkSupport.get(session).activateBookmark(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecTablePage3);

      let page = desktop.outline.selectedNode() as SpecTablePage3;
      expect(page.childrenLoaded).toBe(true);
      expect(page.expanded).toBe(false); // Table pages are not expanded, see BookmarkSupport._revealPage
      expect(page.childNodes.length).toBe(0);

      expect(page.parentNode).toBeUndefined();

      let table = page.detailTable;
      let colorColumn = scout.assertInstance(page.detailTable.columnById('ColorColumn'), Column);
      let hexColumn = scout.assertInstance(page.detailTable.columnById('HexColumn'), Column);
      let primaryColumn = scout.assertInstance(page.detailTable.columnById('PrimaryColumn'), BooleanColumn);
      let usageColumn = scout.assertInstance(page.detailTable.columnById('UsageColumn'), NumberColumn);

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(3);
      expect(table.rows.length).toBe(3);
      expect(table.visibleRows.length).toBe(1);
      expect(table.visibleColumns()).toEqual([hexColumn, colorColumn, usageColumn]);
      expect(colorColumn.width).toBe(333);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(77);
      expect(usageColumn.width).toBe(60);

      // Change state and reset -> original state from page model is restored (not state from bookmark!)
      hexColumn.setVisible(false);
      primaryColumn.setVisible(true);
      primaryColumn.setWidth(444);
      table.sort(colorColumn, 'desc');
      table.moveColumn(usageColumn, 0);

      table.resetToInitialUiPreferences();
      let searchForm = page.getSearchForm() as SpecSearchForm;
      searchForm.widget('ResetMenu').doAction();
      await page.detailTable.when('reload');
      await page.ensureLoadChildren();

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.rows.length).toBe(8);
      expect(table.visibleRows.length).toBe(8);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(table.filterCount()).toBe(0);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(60);
    });

    it('can activate a nested page', async () => {
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
      await BookmarkSupport.get(session).activateBookmark(bookmark);

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
        await BookmarkSupport.get(session).activateBookmark(bookmark, {handleErrors: false});
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
        await BookmarkSupport.get(session).activateBookmark(bookmark, {handleErrors: false});
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
        await BookmarkSupport.get(session).activateBookmark(bookmark, {handleErrors: false});
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
      await BookmarkSupport.get(session).activateBookmark(bookmark);

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
      await BookmarkSupport.get(session).activateBookmark(bookmark);

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

    it('can activate and reset an already loaded page', async () => {
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
      await BookmarkSupport.get(session).activateBookmark(bookmark);

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
      await BookmarkSupport.get(session).activateBookmark(bookmark);

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

    it('reloads page only if necessary', async () => {
      // Activate bookmark
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_2_UUID,
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
            displayText: 'Table Page 2'
          }),
          pagePath: [
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_3_UUID}),
              displayText: 'Node Page 3'
            }),
            scout.create(TableBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
              displayText: 'Table Page 2'
            }),
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(SpecPageParamDo, {fooId: FRUIT_5_KEY}), // Kiwi
              displayText: 'Kiwi'
            })
          ]
        })
      });
      await BookmarkSupport.get(session).activateBookmark(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_2_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecTablePage2);

      let selectedPage = desktop.outline.selectedNode() as SpecTablePage2;
      expect(selectedPage.childrenLoaded).toBe(true);
      expect(selectedPage.expanded).toBe(false);
      expect(selectedPage.childNodes.length).toBe(5);
      expect(selectedPage.detailTable.rows.length).toBe(5);

      let nodesInsertedEvents: TreeNodesInsertedEvent[] = [];
      desktop.outline.on('nodesInserted', event => {
        nodesInsertedEvents.push(event);
      });

      // Open same bookmark again
      await BookmarkSupport.get(session).activateBookmark(bookmark);

      // Assert same page and same data, nothing has been reloaded
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecTablePage2);
      selectedPage = desktop.outline.selectedNode() as SpecTablePage2;
      expect(selectedPage.childrenLoaded).toBe(true);
      expect(selectedPage.expanded).toBe(false);
      expect(selectedPage.childNodes.length).toBe(5);
      expect(selectedPage.detailTable.rows.length).toBe(5);
      expect(nodesInsertedEvents.length).toBe(0);

      // Selected table page changed -> Only this page must be reloaded
      selectedPage.setChildrenLoaded(false);
      await BookmarkSupport.get(session).activateBookmark(bookmark);
      expect(nodesInsertedEvents.length).toBe(1);
      expect(nodesInsertedEvents[0].parentNode).toBe(selectedPage);

      // Parent table page changed -> child node page and table page must be reloaded
      nodesInsertedEvents = [];
      selectedPage.parentNode.parentNode.setChildrenLoaded(false);
      await BookmarkSupport.get(session).activateBookmark(bookmark);
      expect(nodesInsertedEvents.length).toBe(3);
      expect(nodesInsertedEvents[0].parentNode).toBe(selectedPage.parentNode.parentNode);
      expect(nodesInsertedEvents[1].parentNode.text).toBe(selectedPage.parentNode.text);
      expect(nodesInsertedEvents[2].parentNode.text).toBe(selectedPage.text);
    });

    it('reloads page if search filter differs', async () => {
      // Activate bookmark
      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(OutlineBookmarkDefinitionDo, {
          outlineId: SPEC_OUTLINE_2_UUID,
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
            displayText: 'Table Page 2',
            searchFilterComplete: true,
            searchData: createSpecSearchDo({text: 'le'})
          }),
          pagePath: [
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_3_UUID}),
              displayText: 'Node Page 3'
            }),
            scout.create(TableBookmarkPageDo, {
              pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
              displayText: 'Table Page 2',
              searchFilterComplete: true,
              searchData: createSpecSearchDo({text: 'kiwi'})
            }),
            scout.create(NodeBookmarkPageDo, {
              pageParam: scout.create(SpecPageParamDo, {fooId: FRUIT_5_KEY}), // Kiwi
              displayText: 'Kiwi'
            })
          ]
        })
      });
      await BookmarkSupport.get(session).activateBookmark(bookmark);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_2_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecTablePage2);

      let selectedPage = desktop.outline.selectedNode() as SpecTablePage2;
      expect(selectedPage.childrenLoaded).toBe(true);
      expect(selectedPage.expanded).toBe(false);
      expect(selectedPage.childNodes.length).toBe(3);
      expect(selectedPage.detailTable.rows.length).toBe(3); // Apple, Pineapple, Lemon

      let parentTablePage = selectedPage.parentNode.parentNode as SpecTablePage2;
      expect(parentTablePage.childrenLoaded).toBe(true);
      expect(parentTablePage.expanded).toBe(true);
      expect(parentTablePage.childNodes.length).toBe(1);
      expect(parentTablePage.detailTable.rows.length).toBe(1);

      let nodesInsertedEvents: TreeNodesInsertedEvent[] = [];
      desktop.outline.on('nodesInserted', event => {
        nodesInsertedEvents.push(event);
      });

      // Open same bookmark again
      await BookmarkSupport.get(session).activateBookmark(bookmark);

      // Assert same page and same data, nothing has been reloaded
      expect(nodesInsertedEvents.length).toBe(0);
      selectedPage = desktop.outline.selectedNode() as SpecTablePage2;
      expect(selectedPage.childrenLoaded).toBe(true);
      expect(selectedPage.expanded).toBe(false);
      expect(selectedPage.childNodes.length).toBe(3);
      expect(selectedPage.detailTable.rows.length).toBe(3);
      parentTablePage = selectedPage.parentNode.parentNode as SpecTablePage2;
      expect(parentTablePage.childrenLoaded).toBe(true);
      expect(parentTablePage.expanded).toBe(true);
      expect(parentTablePage.childNodes.length).toBe(1);
      expect(parentTablePage.detailTable.rows.length).toBe(1);

      // Select parent table page and adjust search
      desktop.outline.selectNode(parentTablePage);
      let searchForm = parentTablePage.getSearchForm() as SpecSearchForm;
      searchForm.widget('TextField').setValue('apple');
      searchForm.widget('SearchMenu').doAction();
      await parentTablePage.detailTable.when('reload');
      await parentTablePage.detailTable.when('propertyChange:loading');
      expect(parentTablePage.detailTable.rows.length).toBe(2);
      expect(parentTablePage.childNodes.length).toBe(2);

      // Parent table must be reloaded
      nodesInsertedEvents = [];
      await BookmarkSupport.get(session).activateBookmark(bookmark);
      expect(nodesInsertedEvents.length).toBe(3);
      expect(nodesInsertedEvents[0].parentNode).toBe(selectedPage.parentNode.parentNode);
      expect(nodesInsertedEvents[1].parentNode.text).toBe(selectedPage.parentNode.text);
      expect(nodesInsertedEvents[2].parentNode.text).toBe(selectedPage.text);

      // Select parent table page and activate bookmark again without changing search form
      nodesInsertedEvents = [];
      desktop.outline.selectNode(parentTablePage);
      await BookmarkSupport.get(session).activateBookmark(bookmark);

      // Assert that nothing has been reloaded
      expect(nodesInsertedEvents.length).toBe(0);
      selectedPage = desktop.outline.selectedNode() as SpecTablePage2;
      expect(selectedPage.childrenLoaded).toBe(true);
      expect(selectedPage.expanded).toBe(false);
      expect(selectedPage.childNodes.length).toBe(3);
      expect(selectedPage.detailTable.rows.length).toBe(3);
      parentTablePage = selectedPage.parentNode.parentNode as SpecTablePage2;
      expect(parentTablePage.childrenLoaded).toBe(true);
      expect(parentTablePage.expanded).toBe(true);
      expect(parentTablePage.childNodes.length).toBe(1);
      expect(parentTablePage.detailTable.rows.length).toBe(1);
    });

    it('can handle errors', async () => {
      // Provoke 'wrong-definition-type' error
      let bookmark = scout.create(BookmarkDo);

      let spy = spyOn(bookmarkSupport, 'handleActivateBookmarkError').and.returnValue($.resolvedPromise());

      // a) handleError=true (default) --> error should be handled internally
      await bookmarkSupport.activateBookmark(bookmark);
      expect(bookmarkSupport.handleActivateBookmarkError).toHaveBeenCalledWith(BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE);
      spy.calls.reset();

      // b) handleError=false (default) --> error should be thrown
      try {
        await bookmarkSupport.activateBookmark(bookmark, {
          handleErrors: false
        });
        fail('Expected to fail');
      } catch (error) {
        expect(error).toBe(BookmarkSupport.ERROR_WRONG_DEFINITION_TYPE);
      }
      expect(bookmarkSupport.handleActivateBookmarkError).not.toHaveBeenCalled();
    });
  });

  describe('activateBookmarkPath', () => {

    it('can activate a partial bookmark from a given start location', async () => {
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
      let activateBookmarkPathParam: ActivateBookmarkPathParam = {
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
            searchData: createSpecSearchDo({text: 'le'})
          }),
          scout.create(NodeBookmarkPageDo, {
            pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_NODE_PAGE_4_UUID}),
            displayText: 'Lemon'
          })
        ]
      };
      await BookmarkSupport.get(session).activateBookmarkPath(activateBookmarkPathParam);

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
      let activateBookmarkParam: ActivateBookmarkPathParam = {
        parentOutline: outline,
        parentPage: page,
        parentBookmarkPage: null, // irrelevant for this test
        pagePath: [] // irrelevant for this test
      };
      try {
        await BookmarkSupport.get(session).activateBookmarkPath(activateBookmarkParam, {handleErrors: false});
        fail('Expected to fail');
      } catch (error) {
        expect(error).toBe(BookmarkSupport.ERROR_PAGE_WRONG_OUTLINE);
      }
    });

    it('reloads page on partial activation if pathPath is empty', async () => {
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
      let activateBookmarkPathParam: ActivateBookmarkPathParam = {
        parentOutline: outline,
        parentPage: page2,
        parentBookmarkPage: scout.create(TableBookmarkPageDo, {
          pageParam: scout.create(PageIdDummyPageParamDo, {pageId: SPEC_TABLE_PAGE_2_UUID}),
          displayText: 'Table Page 2'
        }),
        pagePath: [] // Normally, BookmarkSupport._resolvePage calls ensureLoadChildren, but if path is empty, BookmarkSupport._revealPage does it
      };
      await BookmarkSupport.get(session).activateBookmarkPath(activateBookmarkPathParam);

      // Assert new state of desktop
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_2_ID);
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecTablePage2);

      let selectedPage = desktop.outline.selectedNode() as SpecTablePage2;
      expect(selectedPage.childrenLoaded).toBe(true);
      expect(selectedPage.expanded).toBe(false);
      expect(selectedPage.childNodes.length).toBe(5);
      expect(selectedPage.detailTable.rows.length).toBe(5);

      let nodesInsertedEvents: TreeNodesInsertedEvent[] = [];
      desktop.outline.on('nodesInserted', event => {
        nodesInsertedEvents.push(event);
      });

      // Activate same bookmark path again
      await BookmarkSupport.get(session).activateBookmarkPath(activateBookmarkPathParam);

      // Assert same page and same data, nothing has been reloaded
      expect(desktop.outline.selectedNode()).toBeInstanceOf(SpecTablePage2);
      selectedPage = desktop.outline.selectedNode() as SpecTablePage2;
      expect(selectedPage.childrenLoaded).toBe(true);
      expect(selectedPage.expanded).toBe(false);
      expect(selectedPage.childNodes.length).toBe(5);
      expect(selectedPage.detailTable.rows.length).toBe(5);
      expect(nodesInsertedEvents.length).toBe(0);

      // Selected table page changed
      selectedPage.setChildrenLoaded(false);
      await BookmarkSupport.get(session).activateBookmarkPath(activateBookmarkPathParam);
      expect(nodesInsertedEvents.length).toBe(1);
      expect(nodesInsertedEvents[0].parentNode).toBe(selectedPage);
    });
  });

  describe('applyBookmarkToPage', () => {

    it('restores table ui preferences to bookmark state if saveState=true', async () => {
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

      // -----

      let table = page.detailTable;
      let colorColumn = scout.assertInstance(page.detailTable.columnById('ColorColumn'), Column);
      let hexColumn = scout.assertInstance(page.detailTable.columnById('HexColumn'), Column);
      let primaryColumn = scout.assertInstance(page.detailTable.columnById('PrimaryColumn'), BooleanColumn);
      let usageColumn = scout.assertInstance(page.detailTable.columnById('UsageColumn'), NumberColumn);

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(60);
      expect(table.initialUiPreferences).toBeInstanceOf(TableClientUiPreferenceProfileDo);

      // -----

      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(PageBookmarkDefinitionDo, {
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            searchData: createSpecSearchDo({text: 'yell'}),
            tablePreferences: scout.create(TableClientUiPreferencesDo, {
              tableId: `${SPEC_TABLE_PAGE_3_TABLE_UUID}|${SPEC_TABLE_PAGE_3_UUID}`,
              tablePreferenceProfiles: new Map([
                [TableUiPreferences.PROFILE_ID_BOOKMARK, scout.create(TableClientUiPreferenceProfileDo, {
                  columns: [
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_1_UUID, // KeyColumn
                      viewIndex: 0,
                      visible: false,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_3_UUID, // HexColumn
                      viewIndex: 1,
                      visible: true,
                      width: 100,
                      sortOrder: 0,
                      sortAscending: true,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_2_UUID, // ColorColumn
                      viewIndex: 2,
                      visible: true,
                      width: 333,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_4_UUID, // PrimaryColumn
                      viewIndex: 3,
                      visible: false,
                      width: 77,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_5_UUID, // UsageColumn
                      viewIndex: 4,
                      visible: true,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false,
                      aggregationFunctionId: 'avg',
                      backgroundEffectId: 'colorGradient2'
                    })
                  ],
                  userFilters: [
                    scout.create(TableTextUserFilterStateDo, {
                      text: 'a'
                    })
                  ]
                })]
              ])
            })
          })
        })
      });

      await BookmarkSupport.get(session).applyBookmarkToPage(page, bookmark);

      // -----

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(3);
      expect(table.visibleColumns()).toEqual([hexColumn, colorColumn, usageColumn]);
      expect(colorColumn.width).toBe(333);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(77);
      expect(usageColumn.width).toBe(60);

      // -----

      expect(searchForm.widget('TextField').value).toBe('yell');

      searchForm.widget('ResetMenu').doAction();
      await searchForm.when('reset');
      expect(searchForm.widget('TextField').value).toBe('yell'); // <--

      // -----

      // Change state and reset -> state from bookmark is restored (because it was applied with saveState=true)
      hexColumn.setVisible(false);
      colorColumn.setWidth(987);
      primaryColumn.setWidth(444);
      table.moveColumn(usageColumn, 0);

      page.detailTable.resetToInitialUiPreferences();

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(3);
      expect(table.visibleColumns()).toEqual([hexColumn, colorColumn, usageColumn]);
      expect(colorColumn.width).toBe(333);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(77);
      expect(usageColumn.width).toBe(60);
    });

    it('restores table ui preferences to initial table state if saveState=false', async () => {
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

      // -----

      let table = page.detailTable;
      let colorColumn = scout.assertInstance(page.detailTable.columnById('ColorColumn'), Column);
      let hexColumn = scout.assertInstance(page.detailTable.columnById('HexColumn'), Column);
      let primaryColumn = scout.assertInstance(page.detailTable.columnById('PrimaryColumn'), BooleanColumn);
      let usageColumn = scout.assertInstance(page.detailTable.columnById('UsageColumn'), NumberColumn);

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(60);
      expect(table.initialUiPreferences).toBeInstanceOf(TableClientUiPreferenceProfileDo);

      // -----

      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(PageBookmarkDefinitionDo, {
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            searchData: createSpecSearchDo({text: 'yell'}),
            tablePreferences: scout.create(TableClientUiPreferencesDo, {
              tableId: `${SPEC_TABLE_PAGE_3_TABLE_UUID}|${SPEC_TABLE_PAGE_3_UUID}`,
              tablePreferenceProfiles: new Map([
                [TableUiPreferences.PROFILE_ID_BOOKMARK, scout.create(TableClientUiPreferenceProfileDo, {
                  columns: [
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_1_UUID, // KeyColumn
                      viewIndex: 0,
                      visible: false,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_3_UUID, // HexColumn
                      viewIndex: 1,
                      visible: true,
                      width: 100,
                      sortOrder: 0,
                      sortAscending: true,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_2_UUID, // ColorColumn
                      viewIndex: 2,
                      visible: true,
                      width: 333,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_4_UUID, // PrimaryColumn
                      viewIndex: 3,
                      visible: false,
                      width: 77,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_5_UUID, // UsageColumn
                      viewIndex: 4,
                      visible: true,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false,
                      aggregationFunctionId: 'avg',
                      backgroundEffectId: 'colorGradient2'
                    })
                  ],
                  userFilters: [
                    scout.create(TableTextUserFilterStateDo, {
                      text: 'a'
                    })
                  ]
                })]
              ])
            })
          })
        })
      });

      await BookmarkSupport.get(session).applyBookmarkToPage(page, bookmark, false); // <-- saveState=false

      // -----

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(3);
      expect(table.visibleColumns()).toEqual([hexColumn, colorColumn, usageColumn]);
      expect(colorColumn.width).toBe(333);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(77);
      expect(usageColumn.width).toBe(60);

      // -----

      expect(searchForm.widget('TextField').value).toBe('yell');

      searchForm.widget('ResetMenu').doAction();
      await searchForm.when('reset');
      expect(searchForm.widget('TextField').value).toBe(null); // reset to default

      // -----

      // Change state and reset -> initial state from table is restored (because bookmark was applied with saveState=false)
      hexColumn.setVisible(false);
      colorColumn.setWidth(987);
      primaryColumn.setWidth(444);
      table.moveColumn(usageColumn, 0);

      page.detailTable.resetToInitialUiPreferences();

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(60);
    });

    it('can still restore table ui preferences when bookmark is applied with saveState=true that has no table preferences', async () => {
      // Original saved state is restored when bookmark does not contain table preferences

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

      // -----

      let table = page.detailTable;
      let colorColumn = scout.assertInstance(page.detailTable.columnById('ColorColumn'), Column);
      let hexColumn = scout.assertInstance(page.detailTable.columnById('HexColumn'), Column);
      let primaryColumn = scout.assertInstance(page.detailTable.columnById('PrimaryColumn'), BooleanColumn);
      let usageColumn = scout.assertInstance(page.detailTable.columnById('UsageColumn'), NumberColumn);

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(60);
      expect(table.initialUiPreferences).toBeInstanceOf(TableClientUiPreferenceProfileDo);

      // -----

      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(PageBookmarkDefinitionDo, {
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            searchData: createSpecSearchDo({text: 'yell'}),
            tablePreferences: null // <--
          })
        })
      });

      await BookmarkSupport.get(session).applyBookmarkToPage(page, bookmark);

      // -----

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(60);

      // -----

      expect(searchForm.widget('TextField').value).toBe('yell');

      searchForm.widget('ResetMenu').doAction();
      await searchForm.when('reset');
      expect(searchForm.widget('TextField').value).toBe('yell'); // <--

      // -----

      // Change state and reset -> original state from table is restored (because there was no state in the bookmark)
      hexColumn.setVisible(false);
      colorColumn.setWidth(987);
      primaryColumn.setWidth(444);
      table.moveColumn(usageColumn, 0);

      page.detailTable.resetToInitialUiPreferences();

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(60);
    });

    it('can still restore table ui preferences when bookmark is applied with saveState=true that has no table preferences and table has uiPreferencesEnabled=false', async () => {
      // Special case for pages that explicitly don't install uiPreferences support and apply a bookmark with
      // saveState=true that does not contain table preferences -> just assume the current state as saved

      let outline = desktop.outline;
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(outline.nodes[2]).toBeInstanceOf(SpecTablePage3);

      let page = outline.nodes[2] as SpecTablePage3;
      page._initDetailTableUiPreferences = () => {
        // NOP -> uiPreferencesEnabled will stay false
      };
      outline.drillDown(page);
      await page.ensureLoadChildren();
      expect(page.detailTable.rows.length).toBe(8);

      let searchForm = scout.assertInstance(page.getSearchForm(), SpecSearchForm);
      searchForm.widget('TextField').setValue('bl'); // Matches 'Black' and 'Blue'
      searchForm.widget('SearchMenu').doAction();
      await page.detailTable.when('reload');
      await page.ensureLoadChildren();
      expect(page.detailTable.rows.length).toBe(2);

      // -----

      let table = page.detailTable;
      let colorColumn = scout.assertInstance(page.detailTable.columnById('ColorColumn'), Column);
      let hexColumn = scout.assertInstance(page.detailTable.columnById('HexColumn'), Column);
      let primaryColumn = scout.assertInstance(page.detailTable.columnById('PrimaryColumn'), BooleanColumn);
      let usageColumn = scout.assertInstance(page.detailTable.columnById('UsageColumn'), NumberColumn);

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(60);
      expect(table.initialUiPreferences).toBeFalsy();

      // -----

      usageColumn.setWidth(888);

      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(PageBookmarkDefinitionDo, {
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            searchData: createSpecSearchDo({text: 'yell'}),
            tablePreferences: null // <--
          })
        })
      });

      await BookmarkSupport.get(session).applyBookmarkToPage(page, bookmark);

      // -----

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(888);
      expect(table.initialUiPreferences).toBeInstanceOf(TableClientUiPreferenceProfileDo);

      // -----

      expect(searchForm.widget('TextField').value).toBe('yell');

      searchForm.widget('ResetMenu').doAction();
      await searchForm.when('reset');
      expect(searchForm.widget('TextField').value).toBe('yell'); // <--

      // -----

      // Change state and reset -> saved state is restored (because there was no state in the bookmark)
      hexColumn.setVisible(false);
      colorColumn.setWidth(987);
      primaryColumn.setWidth(444);
      table.moveColumn(usageColumn, 0);

      page.detailTable.resetToInitialUiPreferences();

      expect(table.columns.length).toBe(5);
      expect(table.visibleColumns().length).toBe(4);
      expect(table.visibleColumns()).toEqual([colorColumn, hexColumn, primaryColumn, usageColumn]);
      expect(colorColumn.width).toBe(222);
      expect(hexColumn.width).toBe(100);
      expect(primaryColumn.width).toBe(60);
      expect(usageColumn.width).toBe(888);
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
            searchData: createSpecSearchDo({text: 'yell'}),
            tablePreferences: scout.create(TableClientUiPreferencesDo, {
              tableId: `${SPEC_TABLE_PAGE_3_TABLE_UUID}|${SPEC_TABLE_PAGE_3_UUID}`,
              tablePreferenceProfiles: new Map([
                [TableUiPreferences.PROFILE_ID_BOOKMARK, scout.create(TableClientUiPreferenceProfileDo, {
                  columns: [
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_1_UUID, // KeyColumn
                      viewIndex: 0,
                      visible: false,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_3_UUID, // HexColumn
                      viewIndex: 1,
                      visible: true,
                      width: 100,
                      sortOrder: 0,
                      sortAscending: true,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_2_UUID, // ColorColumn
                      viewIndex: 2,
                      visible: true,
                      width: 333,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_4_UUID, // PrimaryColumn
                      viewIndex: 3,
                      visible: false,
                      width: 77,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_5_UUID, // UsageColumn
                      viewIndex: 4,
                      visible: true,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false,
                      aggregationFunctionId: 'avg',
                      backgroundEffectId: 'colorGradient2'
                    })
                  ],
                  userFilters: [
                    scout.create(TableTextUserFilterStateDo, {
                      text: 'a'
                    })
                  ]
                })]
              ])
            })
          })
        })
      });
      await BookmarkSupport.get(session).applyBookmarkToPageAndReload(page, bookmark);

      expect(page.detailTable.rows.length).toBe(1);
      expect(page.detailTable.visibleRows.length).toBe(0);
      expect(page.detailTable.filterCount()).toBe(1);
      expect(searchForm.widget('TextField').value).toBe('yell');

      searchForm.widget('ResetMenu').doAction();
      await searchForm.when('reset');
      expect(searchForm.widget('TextField').value).toBe('yell'); // <--

      page.detailTable.resetToInitialUiPreferences();
      expect(page.detailTable.visibleRows.length).toBe(0);
      expect(page.detailTable.filterCount()).toBe(1);
    });

    it('applies the search data while allowing the user to reset the search form and table preferences if saveState is false', async () => {
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
            searchData: createSpecSearchDo({text: 'cy'}),
            tablePreferences: scout.create(TableClientUiPreferencesDo, {
              tableId: `${SPEC_TABLE_PAGE_3_TABLE_UUID}|${SPEC_TABLE_PAGE_3_UUID}`,
              tablePreferenceProfiles: new Map([
                [TableUiPreferences.PROFILE_ID_BOOKMARK, scout.create(TableClientUiPreferenceProfileDo, {
                  columns: [
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_1_UUID, // KeyColumn
                      viewIndex: 0,
                      visible: false,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_3_UUID, // HexColumn
                      viewIndex: 1,
                      visible: true,
                      width: 100,
                      sortOrder: 0,
                      sortAscending: true,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_2_UUID, // ColorColumn
                      viewIndex: 2,
                      visible: true,
                      width: 333,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_4_UUID, // PrimaryColumn
                      viewIndex: 3,
                      visible: false,
                      width: 77,
                      sortOrder: -1,
                      groupingActive: false
                    }),
                    scout.create(TableColumnClientUiPreferenceDo, {
                      columnId: SPEC_TABLE_PAGE_3_TABLE_COLUMN_5_UUID, // UsageColumn
                      viewIndex: 4,
                      visible: true,
                      width: 60,
                      sortOrder: -1,
                      groupingActive: false,
                      aggregationFunctionId: 'avg',
                      backgroundEffectId: 'colorGradient2'
                    })
                  ],
                  userFilters: [
                    scout.create(TableTextUserFilterStateDo, {
                      text: 'e'
                    })
                  ]
                })]
              ])
            })
          })
        })
      });
      await BookmarkSupport.get(session).applyBookmarkToPageAndReload(page, bookmark, false); // <--

      expect(page.detailTable.rows.length).toBe(1);
      expect(page.detailTable.visibleRows.length).toBe(0);
      expect(page.detailTable.filterCount()).toBe(1);
      expect(searchForm.widget('TextField').value).toBe('cy');

      searchForm.widget('ResetMenu').doAction();
      await searchForm.when('reset');
      expect(searchForm.widget('TextField').value).toBe(null); // <--

      page.detailTable.resetToInitialUiPreferences();
      page.detailTable.filter(); // FIXME bsh Remove this when #426270 is fixed
      expect(page.detailTable.rows.length).toBe(8);
      expect(page.detailTable.visibleRows.length).toBe(8);
      expect(page.detailTable.filterCount()).toBe(0);
    });
  });
});
