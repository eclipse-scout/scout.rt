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
  ActivateBookmarkRequest, BaseDoEntity, BookmarkDo, BookmarkSupport, BookmarkTableRowIdentifierDo, BookmarkTableRowIdentifierStringComponentDo, BooleanColumn, Column, Desktop, Form, FormModel, FormTableControl, GroupBox, icons,
  NodeBookmarkPageDo, NumberColumn, ObjectOrModel, Outline, OutlineBookmarkDefinitionDo, OutlineViewButton, Page, PageBookmarkDefinitionDo, PageIdDummyPageParamDo, PageParamDo, PageWithNodes, PageWithTable, ResetMenu, scout, SearchMenu,
  StringField, strings, Table, TableBookmarkPageDo, TableRow, TableTextUserFilter, typeName, UuidPool
} from '../../src/index';

describe('BookmarkSupport', () => {
  let session: SandboxSession;
  let desktop: Desktop;
  let bookmarkSupport: BookmarkSupport;

  const SPEC_OUTLINE_1_ID = 'SpecOutline1';
  const SPEC_OUTLINE_2_ID = 'SpecOutline2';
  const SPEC_OUTLINE_1_UUID = '8841b967-4801-47bb-87a9-a5f6d54b4014';
  const SPEC_OUTLINE_2_UUID = 'a6379a66-c844-4ec7-8e7e-1f854dc7e81e';
  const SPEC_NODE_PAGE_1_UUID = '9e4a69e7-73a5-44fd-8d68-ebb6a50f07ba';
  const SPEC_NODE_PAGE_2_UUID = 'c7f9ad97-d80a-429b-8701-0378cad9307f';
  const SPEC_NODE_PAGE_3_UUID = '80e022bf-5b00-491d-818e-3c4054d7fcc3';
  const SPEC_NODE_PAGE_4_UUID = 'df79375c-047b-47cf-8323-360652ee97ae';
  const SPEC_TABLE_PAGE_1_UUID = 'e9320869-aead-46a5-a67e-25491f8823de';
  const SPEC_TABLE_PAGE_2_UUID = '56c699e5-5692-4a21-9595-e7dac5ee568e';
  const SPEC_TABLE_PAGE_3_UUID = 'daf22921-71eb-4382-b500-854225e71622';
  const FRUIT_1_KEY = '1'; // Apple
  const FRUIT_2_KEY = '2'; // Banana
  const FRUIT_3_KEY = '3'; // Pineapple
  const FRUIT_4_KEY = '4'; // Lemon
  const FRUIT_5_KEY = '5'; // Kiwi

  // ---------------------------------------------------------------
  //
  //   SpecOutline1
  //   +- SpecNodePage1 [leaf, SpecDetailForm]
  //   +- SpecTablePage1 [leaf] (Letters)
  //   +- SpecTablePage3 [leaf, SpecSearchForm] (Colors)
  //
  //   SpecOutline2
  //   +- SpecNodePage2 [leaf, SpecDetailForm]
  //   +- SpecNodePage3
  //      +- SpecNodePage1 [leaf, SpecDetailForm]
  //      +- SpecTablePage2 [SpecSearchForm] (Fruits)
  //      |  +- SpecNodePage4
  //      |     +- SpecNodePage2 [leaf, SpecDetailForm]
  //      |     +- (rec:SpecTablePage2)
  //      +- SpecNodePage2 [leaf, SpecDetailForm]
  //
  // ---------------------------------------------------------------

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        navigationVisible: true,
        headerVisible: true,
        benchVisible: true,
        viewButtons: [
          {
            id: 'SpecOutline1ViewButton',
            objectType: OutlineViewButton,
            outline: {
              id: SPEC_OUTLINE_1_ID,
              uuid: SPEC_OUTLINE_1_UUID,
              objectType: Outline,
              title: 'Spec Outline 1',
              nodes: [
                {objectType: SpecNodePage1},
                {objectType: SpecTablePage1},
                {objectType: SpecTablePage3}
              ]
            },
            selected: true,
            displayStyle: 'MENU',
            text: 'Spec Outline 1'
          },
          {
            id: 'SpecOutline2ViewButton',
            objectType: OutlineViewButton,
            outline: {
              id: SPEC_OUTLINE_2_ID,
              uuid: SPEC_OUTLINE_2_UUID,
              objectType: Outline,
              title: 'Spec Outline 2',
              nodes: [
                {objectType: SpecNodePage2},
                {objectType: SpecNodePage3}
              ]
            },
            displayStyle: 'MENU',
            text: 'Spec Outline 2'
          }
        ],
        outline: SPEC_OUTLINE_1_ID
      },
      renderDesktop: false
    });
    desktop = session.desktop;
    bookmarkSupport = BookmarkSupport.get(session);
  });

  @typeName('SpecPageParam')
  class SpecPageParamDo extends PageParamDo {
    fooId: string;
  }

  @typeName('SpecSearch')
  class SpecSearchDo extends BaseDoEntity {
    text: string;
  }

  class SpecNodePage1 extends PageWithNodes {

    constructor() {
      super();
      this.uuid = SPEC_NODE_PAGE_1_UUID;
      this.text = 'Node Page 1';
      this.leaf = true;
    }

    protected override _createDetailForm(): Form {
      return scout.create(SpecDetailForm, {
        parent: this.outline
      });
    }
  }

  class SpecNodePage2 extends PageWithNodes {

    constructor() {
      super();
      this.uuid = SPEC_NODE_PAGE_2_UUID;
      this.text = 'Node Page 3';
      this.leaf = true;
    }

    protected override _createDetailForm(): Form {
      return scout.create(SpecDetailForm, {
        parent: this.outline
      });
    }
  }

  class SpecNodePage3 extends PageWithNodes {

    constructor() {
      super();
      this.uuid = SPEC_NODE_PAGE_3_UUID;
      this.text = 'Node Page 3';
    }

    protected override _createChildPages(): JQuery.Promise<Page[]> {
      return $.resolvedPromise([
        scout.create(SpecNodePage1, {parent: this.outline}),
        scout.create(SpecTablePage2, {parent: this.outline}),
        scout.create(SpecNodePage2, {parent: this.outline})
      ]);
    }
  }

  class SpecNodePage4 extends PageWithNodes {

    constructor() {
      super();
      this.uuid = SPEC_NODE_PAGE_4_UUID;
      this.text = 'Node Page 4';
    }

    protected override _createChildPages(): JQuery.Promise<Page[]> {
      return $.resolvedPromise([
        scout.create(SpecNodePage2, {parent: this.outline}),
        scout.create(SpecTablePage2, {parent: this.outline})
      ]);
    }
  }

  class SpecTablePage1 extends PageWithTable {

    constructor() {
      super();
      this.uuid = SPEC_TABLE_PAGE_1_UUID;
      this.text = 'Table Page 1';
      this.leaf = true;
    }

    protected override _createDetailTable(): Table {
      return scout.create(Table, {
        parent: this.outline,
        columns: [{
          id: 'LetterColumn',
          objectType: Column,
          text: 'Letter'
        }]
      });
    }

    protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
      let data = ['A', 'B', 'C'];
      return $.resolvedPromise(data);
    }

    protected override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
      return tableData.map(rowData => {
        return scout.create(TableRow, {
          parent: this.detailTable,
          cells: [rowData]
        });
      });
    }
  }

  class SpecTablePage2 extends PageWithTable {

    constructor() {
      super();
      this.uuid = SPEC_TABLE_PAGE_2_UUID;
      this.text = 'Table Page 2';
    }

    protected override _createDetailTable(): Table {
      return scout.create(Table, {
        parent: this.outline,
        columns: [{
          id: 'KeyColumn',
          objectType: Column,
          primaryKey: true,
          displayable: false
        }, {
          id: 'NameColumn',
          objectType: Column,
          text: 'Name',
          summary: true
        }, {
          id: 'AmountColumn',
          objectType: NumberColumn,
          text: 'Amount'
        }],
        tableControls: [{
          id: 'SearchFormTableControl',
          objectType: FormTableControl,
          iconId: icons.SEARCH,
          form: {
            id: 'SearchForm',
            objectType: SpecSearchForm
          }
        }]
      });
    }

    protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
      let data = [
        {key: FRUIT_1_KEY, name: 'Apple', amount: 42},
        {key: FRUIT_2_KEY, name: 'Banana', amount: 37},
        {key: FRUIT_3_KEY, name: 'Pineapple', amount: 29},
        {key: FRUIT_4_KEY, name: 'Lemon', amount: 58},
        {key: FRUIT_5_KEY, name: 'Kiwi', amount: 33}
      ];
      if (searchFilter instanceof SpecSearchDo && searchFilter.text) {
        data = data.filter(d => new RegExp(strings.quote(searchFilter.text), 'i').test(d.name));
      }
      return $.resolvedPromise(data);
    }

    protected override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
      return tableData.map(rowData => {
        return scout.create(TableRow, {
          parent: this.detailTable,
          cells: [rowData.key, rowData.name, rowData.amount]
        });
      });
    }

    protected override _createChildPage(row: TableRow): Page {
      let pageParam = scout.create(SpecPageParamDo, {
        fooId: this.detailTable.columnById('KeyColumn').cellValue(row)
      });
      return scout.create(SpecNodePage4, {
        parent: this.outline,
        pageParam: pageParam
      });
    }
  }

  class SpecTablePage3 extends PageWithTable {

    constructor() {
      super();
      this.uuid = SPEC_TABLE_PAGE_3_UUID;
      this.text = 'Table Page 3';
      this.leaf = true;
    }

    protected override _createDetailTable(): Table {
      return scout.create(Table, {
        parent: this.outline,
        columns: [{
          id: 'KeyColumn',
          objectType: Column,
          primaryKey: true,
          displayable: false
        }, {
          id: 'ColorColumn',
          objectType: Column,
          text: 'Color',
          summary: true
        }, {
          id: 'PrimaryColumn',
          objectType: BooleanColumn,
          text: 'Primary color'
        }],
        tableControls: [{
          id: 'SearchFormTableControl',
          objectType: FormTableControl,
          iconId: icons.SEARCH,
          form: {
            id: 'SearchForm',
            objectType: SpecSearchForm
          }
        }]
      });
    }

    protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
      let data = [
        {key: '#000000', label: 'Black', primary: false},
        {key: '#ff0000', label: 'Red', primary: true},
        {key: '#00ff00', label: 'Green', primary: true},
        {key: '#0000ff', label: 'Blue', primary: true},
        {key: '#ffff00', label: 'Yellow', primary: false},
        {key: '#ff00ff', label: 'Magenta', primary: false},
        {key: '#00ffff', label: 'Cyan', primary: false},
        {key: '#ffffff', label: 'White', primary: false}
      ];
      if (searchFilter instanceof SpecSearchDo && searchFilter.text) {
        data = data.filter(d => new RegExp(strings.quote(searchFilter.text), 'i').test(d.label));
      }
      return $.resolvedPromise(data);
    }

    protected override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
      return tableData.map(rowData => {
        return scout.create(TableRow, {
          parent: this.detailTable,
          cells: [rowData.key, rowData.label, rowData.primary]
        });
      });
    }
  }

  class SpecSearchForm extends Form {
    declare data: SpecSearchDo;
    declare widgetMap: {
      'TextField': StringField;
      'SearchMenu': SearchMenu;
      'ResetMenu': ResetMenu;
    };

    protected override _jsonModel(): FormModel {
      return {
        rootGroupBox: {
          id: 'MainBox',
          objectType: GroupBox,
          fields: [{
            id: 'TextField',
            objectType: StringField,
            label: 'Text'
          }],
          menus: [{
            id: 'SearchMenu',
            objectType: SearchMenu
          }, {
            id: 'ResetMenu',
            objectType: ResetMenu
          }]
        }
      };
    }

    override importData() {
      if (!this.data) {
        return;
      }
      this.widget('TextField').setValue(this.data.text);
    }

    override exportData(): any {
      return scout.create(SpecSearchDo, {
        text: this.widget('TextField').value
      });
    }
  }

  class SpecDetailForm extends Form {
  }

  function getOutline(outlineId: string): Outline {
    return scout.assertValue(desktop.getOutlines().find(outline => outline.id === outlineId), `Outline not found: ${outlineId}`);
  }

  function goToOutline(outlineId: string): Outline {
    let outline = getOutline(outlineId);
    desktop.setOutline(outline);
    desktop.bringOutlineToFront();
    return outline;
  }

  // ---------------------------------------------------------------

  describe('createBookmark', () => {

    it('can create a bookmark for a top-level table page without search form', async () => {
      let outline = desktop.outline;
      expect(outline).toBeInstanceOf(Outline);
      expect(outline.title).toBe('Spec Outline 1');
      expect(outline.nodes.length).toBe(3);
      expect(outline.nodes[0]).toBeInstanceOf(SpecNodePage1);
      expect(outline.nodes[1]).toBeInstanceOf(SpecTablePage1);
      expect(outline.nodes[2]).toBeInstanceOf(SpecTablePage3);
      expect(outline.selectedNode()).toBe(null);
      let page = outline.nodes[1];
      outline.selectNodes(page);
      expect(page.detailTable).toBeInstanceOf(Table);
      expect(page.detailTable.loading).toBe(true);
      expect(page.detailTable.rows.length).toBe(0);
      await page.ensureLoadChildren();
      expect(page.detailTable.loading).toBe(false);
      expect(page.detailTable.rows.length).toBe(3);

      // -----

      let bookmark = await bookmarkSupport.createBookmark(desktop.outline.activePage());
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
      expect(bookmarkedPage.pageParam).toBeInstanceOf(PageIdDummyPageParamDo);
      expect((bookmarkedPage.pageParam as PageIdDummyPageParamDo).pageId).toBe(SPEC_TABLE_PAGE_1_UUID);
      expect(bookmarkedPage.searchData).toBeUndefined();
    });

    it('can create a bookmark for a nested node page', async () => {
      let outline = goToOutline(SPEC_OUTLINE_2_ID);
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

      let bookmark = await bookmarkSupport.createBookmark(desktop.outline.activePage());
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
      expect(pagePathElement2.selectedChildRows.length).toBe(1);
      expect(pagePathElement2.selectedChildRows[0].toPojo()).toEqual(scout.create(BookmarkTableRowIdentifierDo, {
        keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_5_KEY})]
      }).toPojo());
      let pagePathElement3 = bookmarkDefinition.pagePath[2] as NodeBookmarkPageDo;
      expect(pagePathElement3).toBeInstanceOf(NodeBookmarkPageDo);
      // expect(pagePathElement3.displayText).toBe('Kiwi'); FIXME bsh [js-bookmark] Enable when implemented
      expect(pagePathElement3.pageParam).toBeInstanceOf(SpecPageParamDo);
      expect((pagePathElement3.pageParam as SpecPageParamDo).fooId).toBe(FRUIT_5_KEY);

      let bookmarkedPage = bookmarkDefinition.bookmarkedPage as TableBookmarkPageDo;
      expect(bookmarkedPage).toBeInstanceOf(TableBookmarkPageDo);
      expect(bookmarkedPage.pageParam).toBeInstanceOf(PageIdDummyPageParamDo);
      expect((bookmarkedPage.pageParam as PageIdDummyPageParamDo).pageId).toBe(SPEC_TABLE_PAGE_2_UUID);
      expect(bookmarkedPage.searchData).toBeInstanceOf(BaseDoEntity);
      expect((bookmarkedPage.searchData as BaseDoEntity).toPojo()).toEqual(scout.create(SpecSearchDo, {text: 'n'}).toPojo());
      expect(bookmarkedPage.expandedChildRow).toBeUndefined();
      expect(bookmarkedPage.selectedChildRows.length).toBe(2);
      expect(bookmarkedPage.selectedChildRows[0].toPojo()).toEqual(scout.create(BookmarkTableRowIdentifierDo, {
        keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_2_KEY})]
      }).toPojo());
      expect(bookmarkedPage.selectedChildRows[1].toPojo()).toEqual(scout.create(BookmarkTableRowIdentifierDo, {
        keyComponents: [scout.create(BookmarkTableRowIdentifierStringComponentDo, {key: FRUIT_4_KEY})]
      }).toPojo());
    });
  });

  describe('openBookmarkInOutline', () => {

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
      expect(page2.childNodes.length).toBe(2);
      // expect(page2.childNodes.filter(node => node.filterAccepted).length).toBe(1);
      expect(page2.detailTable.rows.length).toEqual(2);
      // expect(page2.detailTable.filteredRows().length).toEqual(1);
      // expect(page2.detailTable.getFilter(TableTextUserFilter.TYPE)).toBeInstanceOf(TableTextUserFilter);
      // expect((page2.detailTable.getFilter(TableTextUserFilter.TYPE) as TableTextUserFilter).text).toBe('ble');

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
      let outline = goToOutline(SPEC_OUTLINE_2_ID);
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
  });

  describe('openBookmarkLocal', () => {

    it('can open a partial bookmark from a given start location', async () => {
      // Go to the start page
      let outline = goToOutline(SPEC_OUTLINE_2_ID);
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
      expect(page5.detailTable.rows.length).toEqual(2);

      expect(page5.parentNode).toBe(page2); // page2 was the starting point
    });

    it('fails if the given page does not belong to the given outline', async () => {
      expect(desktop.outline).toBeInstanceOf(Outline);
      expect(desktop.outline.id).toBe(SPEC_OUTLINE_1_ID);
      expect(desktop.outline.nodes[0]).toBeInstanceOf(SpecNodePage1);
      let page = scout.assertInstance(desktop.outline.nodes[0], SpecNodePage1);

      let outline = goToOutline(SPEC_OUTLINE_2_ID);
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

  describe('applyBookmarkToPage', () => {

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
      page.detailTable.reload(Table.ReloadReason.SEARCH); // FIXME bsh [js-bookmark] This should not be necessary!
      await page.ensureLoadChildren();
      expect(page.detailTable.rows.length).toBe(2);

      let bookmark = scout.create(BookmarkDo, {
        definition: scout.create(PageBookmarkDefinitionDo, {
          bookmarkedPage: scout.create(TableBookmarkPageDo, {
            searchData: scout.create(SpecSearchDo, {text: 'yell'})
          })
        })
      });
      await BookmarkSupport.get(session).applyBookmarkToPage(page, bookmark);

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
      await BookmarkSupport.get(session).applyBookmarkToPage(page, bookmark, false); // <--

      expect(page.detailTable.rows.length).toBe(1);
      expect(searchForm.widget('TextField').value).toBe('cy');
      searchForm.widget('ResetMenu').doAction();
      await searchForm.when('reset');
      expect(searchForm.widget('TextField').value).toBe(null); // <--
    });
  });
});
