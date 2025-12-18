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
  arrays, Column, Form, GroupBox, InitModelOf, MaxRowCountContributionDo, NumberColumn, NumberField, ObjectOrModel, Outline, Page, PageWithNodes, PageWithTable, ResetMenu, scout, SearchFormTableControl, SearchMenu, SmartColumn,
  StaticLookupCall, StringField, Table, TableRow, WidgetModel
} from '../../../../src/index';
import {OutlineSpecHelper, TableSpecHelper} from '../../../../src/testing/index';

describe('PageWithTable', () => {
  let session: SandboxSession;
  let helper: OutlineSpecHelper;
  let outline: Outline;
  let page: SpecPageWithTable;
  let tableHelper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new OutlineSpecHelper(session);
    outline = helper.createOutline();
    tableHelper = new TableSpecHelper(session);

    page = scout.create(SpecPageWithTable, {
      parent: outline,
      detailTable: {
        objectType: Table
      }
    });
    outline.insertNodes([page], null);
    outline.render();
    outline.selectNode(page);
    page.detailTable.render();

    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  class SpecPageWithTable extends PageWithTable {
    override _createSearchFilter(): any {
      return super._createSearchFilter();
    }

    override _withMaxRowCountContribution(request: any): any {
      return super._withMaxRowCountContribution(request);
    }

    override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
      return super._transformTableDataToTableRows(tableData);
    }

    override _loadTableData(searchFilter: any): JQuery.Promise<any> {
      return super._loadTableData(searchFilter);
    }

    override _createChildPage(row: TableRow): Page {
      return super._createChildPage(row);
    }
  }

  interface SearchData {
    strVal: string;
    numVal: number;
  }

  class SearchForm extends Form {
    declare data: SearchData;

    protected override _jsonModel(): WidgetModel {
      return {
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: StringField,
            label: 'str field'
          }, {
            objectType: NumberField,
            label: 'num field'
          }],
          menus: [
            {
              objectType: SearchMenu
            },
            {
              objectType: ResetMenu
            }
          ]
        }
      };
    }

    override importData() {
      this.findChild(StringField).setValue(this.data.strVal);
      this.findChild(NumberField).setValue(this.data.numVal);
    }

    override exportData(): any {
      return {
        strVal: this.findChild(StringField).value,
        numVal: this.findChild(NumberField).value
      } as SearchData;
    }
  }

  class SearchFormWithInitialValues extends SearchForm {
    protected override _init(model: InitModelOf<this>) {
      super._init(model);
      this.findChild(StringField).setValue('initial');
    }
  }

  it('updates the page on table reload', () => {
    let counter = 0;
    page._loadTableData = searchFilter => {
      counter++;
      return $.resolvedPromise();
    };
    page.detailTable.reload(); // this should trigger the _loadTableData of the page

    expect(page.detailTable.hasReloadHandler).toBe(true);
    expect(counter).toBe(1);
  });

  it('row limits are exported', () => {
    page.detailTable.setMaxRowCount(123);
    let searchFilter = {
      _contributions: {
        _type: 'whatever'
      }
    };
    let requestWithLimit = page._withMaxRowCountContribution(searchFilter);
    expect(requestWithLimit._contributions.length).toBe(2);
    let maxRowCountContributionDo: MaxRowCountContributionDo = requestWithLimit._contributions[1];
    expect(maxRowCountContributionDo.hint).toBe(123);
    expect(maxRowCountContributionDo._type).toBe('scout.MaxRowCountContribution');
  });

  it('row limits are imported', () => {
    page._loadTableData = searchFilter => {
      return $.resolvedPromise({
        _contributions: [{
          _type: 'scout.LimitedResultInfoContribution',
          limitedResult: true,
          maxRowCount: 456,
          estimatedRowCount: 1111
        }]
      });
    };
    page._transformTableDataToTableRows = data => undefined;
    page.detailTable.reload();
    jasmine.clock().tick(10);
    expect(page.detailTable.maxRowCount).toBe(456);
    expect(page.detailTable.estimatedRowCount).toBe(1111);
    expect(page.detailTable.tableStatus.message).toBe('[undefined text: MaxOutlineRowWarningWithEstimatedRowCount]');
  });

  it('should handle errors in _onLoadTableDataDone', () => {
    page._loadTableData = searchFilter => $.resolvedPromise([{
      rowId: 1,
      parentRow: 666, // does not exist -> causes an error in Table.js#insertRows
      cells: []
    }]);
    expect(page.detailTable.tableStatus).toBe(undefined);
    page.detailTable.reload();
    jasmine.clock().tick(3);

    // expect error to be set as tableStatus
    let keys = Object.keys(page.detailTable.tableStatus);
    expect(arrays.containsAll(keys, ['message', 'code', 'severity'])).toBe(true);
  });

  it('does not fail when cells with null values are inserted into a smart column ', done => {
    class DummyLookupCall extends StaticLookupCall<string> {
      override _data() {
        return [
          ['key0', 'Key 0'],
          ['key1', 'Key 1']
        ];
      }
    }

    class SamplePageWithTable extends PageWithTable {
      protected override _createChildPage(row) {
        return scout.create(Page, {
          parent: this.outline
        });
      }

      protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
        let data = [{
          string: 'string 1',
          smartValue: null
        }, {
          string: 'string 2',
          smartValue: null
        }, {
          string: 'string 3',
          smartValue: 'key0'
        }, {
          string: 'string 4',
          smartValue: 'key1'
        }, {
          string: 'string 5',
          smartValue: 'key0'
        }];
        return $.resolvedPromise(data);
      }

      protected override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
        return tableData
          .map(row => {
            return {
              data: row,
              cells: [
                row.string,
                row.smartValue
              ]
            };
          });
      }
    }

    jasmine.clock().uninstall();
    let lookupCall = new DummyLookupCall();
    lookupCall.init({session: session});
    let samplePage = new SamplePageWithTable();
    samplePage.init({
      parent: outline,
      detailTable: {
        objectType: Table,
        columns: [
          {
            id: 'StringColumn',
            objectType: Column,
            sortIndex: 0
          },
          {
            id: 'SmartColumn',
            objectType: SmartColumn,
            lookupCall: lookupCall
          }
        ]
      }
    });
    outline.insertNode(samplePage);
    outline.selectNode(samplePage);
    samplePage.detailTable.when('propertyChange:loading').then(event => {
      // Loading is set to true when update buffer finishes
      expect(samplePage.detailTable.rows[0].cells[0].text).toEqual('string 1');
      expect(samplePage.detailTable.rows[0].cells[1].text).toEqual('');
      expect(samplePage.detailTable.rows[2].cells[0].text).toEqual('string 3');
      expect(samplePage.detailTable.rows[2].cells[1].text).toEqual('Key 0');
      done();
    })
      .catch(fail);
  });

  it('restores the selection after reload if possible', () => {
    const tablePage = scout.create(SpecPageWithTable, {
      parent: outline,
      detailTable: {
        objectType: Table,
        columns: [
          {
            objectType: NumberColumn,
            displayable: false,
            visible: false,
            primaryKey: true
          },
          {
            objectType: Column
          }
        ]
      }
    });
    outline.insertNodes([tablePage], null);
    outline.selectNode(tablePage);

    const table = tablePage.detailTable;
    table.render();

    const data = [
      [0, 'Row 0'],
      [1, 'Row 1'],
      [2, 'Row 2'],
      [3, 'Row 3'],
      [4, 'Row 4']
    ];
    let searchIds = [0, 1, 2, 3, 4];

    tablePage._createSearchFilter = () => searchIds;
    tablePage._loadTableData = searchFilter => {
      return $.resolvedPromise(data
        .filter(d => arrays.contains(searchFilter, d[0]))
        .map(cells => ({
          cells: [...cells]
        })));
    };
    tablePage._createChildPage = row => scout.create(PageWithNodes, {
      parent: outline,
      computeTextForRow: r => r.cells[1].text
    });

    expect(outline.selectedNode()).toBe(tablePage);

    table.reload();
    jasmine.clock().tick(3);
    expect(outline.selectedNode()).toBe(tablePage);

    table.selectRow(table.getRowByKey([2]));
    expect(outline.selectedNode()).toBe(tablePage);

    table.doRowAction(table.getRowByKey([2]));
    expect(outline.selectedNode()).toBe(tablePage.childNodes[2]);
    expect(outline.selectedNode().text).toBe('Row 2');

    table.reload();
    jasmine.clock().tick(3);
    expect(outline.selectedNode()).toBe(tablePage.childNodes[2]);
    expect(outline.selectedNode().text).toBe('Row 2');

    searchIds = [1, 3];
    table.reload();
    jasmine.clock().tick(3);
    expect(outline.selectedNode()).toBe(tablePage);

    table.selectRow(table.getRowByKey([3]));
    table.doRowAction(table.getRowByKey([3]));
    expect(outline.selectedNode()).toBe(tablePage.childNodes[1]);
    expect(outline.selectedNode().text).toBe('Row 3');

    searchIds = [0, 2, 3];
    data[3][1] = 'Updated row 3';
    table.reload();
    jasmine.clock().tick(3);
    expect(outline.selectedNode()).toBe(tablePage.childNodes[2]);
    expect(outline.selectedNode().text).toBe('Updated row 3');

    outline.selectNode(page);
    expect(outline.selectedNode()).toBe(page);

    table.reload();
    jasmine.clock().tick(3);
    expect(outline.selectedNode()).toBe(page);
  });

  it('childPages text is updated from the summary columns of the table', () => {
    const tablePage = scout.create(SpecPageWithTable, {
      parent: outline,
      detailTable: tableHelper.createTable(tableHelper.createModel(tableHelper.createModelColumns(5), []))
    });

    const data = [
      {cells: ['a', 'b', 'c', 'd', 'e']},
      {cells: ['1', '2', '3', '4', '5']}
    ];
    tablePage._loadTableData = () => $.resolvedPromise(data);
    tablePage._createChildPage = () => scout.create(PageWithNodes, {parent: outline});

    const table = tablePage.detailTable;
    table.reload();
    jasmine.clock().tick(3);

    const [pageAbc, page123] = table.rows.map(r => r.page);

    expect(pageAbc.text).toBe('a');
    expect(page123.text).toBe('1');

    table.columns[1].setSummary(true);
    expect(pageAbc.text).toBe('b');
    expect(page123.text).toBe('2');

    table.columns[4].setSummary(true);
    expect(pageAbc.text).toBe('b e');
    expect(page123.text).toBe('2 5');

    table.columns[0].setSummary(true);
    table.columns[1].setSummary(false);
    table.columns[3].setSummary(true);
    expect(pageAbc.text).toBe('a d e');
    expect(page123.text).toBe('1 4 5');
  });

  it('updates childrenLoaded flag', () => {
    let page = scout.create(SpecPageWithTable, {
      parent: outline,
      detailTable: {
        objectType: Table
      }
    });
    outline.insertNode(page);
    expect(page.childrenLoaded).toBe(false);

    outline.selectNode(page);
    jasmine.clock().tick(1);
    let detailTable = page.detailTable;
    expect(detailTable).toBeTruthy();

    expect(page.childrenLoaded).toBe(true);
    expect(detailTable.loading).toBe(false);

    detailTable.reload();
    expect(page.childrenLoaded).toBe(true); // same as before, because reloading the table does not call loadChildren()
    expect(detailTable.loading).toBe(true);

    jasmine.clock().tick(1);
    expect(page.childrenLoaded).toBe(true);
    expect(detailTable.loading).toBe(false);

    page.reloadPage();
    expect(page.childrenLoaded).toBe(false); // <--
    expect(detailTable.loading).toBe(true);

    jasmine.clock().tick(1);
    expect(page.childrenLoaded).toBe(true);
    expect(detailTable.loading).toBe(false);
  });

  it('calls _initDetailTableUiPreferences after _initDetailTable', () => {
    let observedTestValue = 0;

    class SpecPageWithTable1 extends PageWithTable {
      testValue = 0;

      protected override _initDetailTable(table: Table) {
        super._initDetailTable(table);
        this.testValue = 1;
      }

      protected override _initDetailTableUiPreferences(table: Table) {
        observedTestValue = this.testValue;
      }
    }

    class SpecPageWithTable2 extends SpecPageWithTable1 {
      protected override _initDetailTable(table: Table) {
        super._initDetailTable(table);
        this.testValue = 2;
      }
    }

    let page1 = scout.create(SpecPageWithTable1, {
      parent: outline,
      detailTable: {
        objectType: Table
      }
    });
    expect(page1.testValue).toBe(0);
    expect(observedTestValue).toBe(0);
    page1.ensureDetailTable();
    expect(page1.testValue).toBe(1);
    expect(observedTestValue).toBe(1);

    let page2 = scout.create(SpecPageWithTable2, {
      parent: outline,
      detailTable: {
        objectType: Table
      }
    });
    expect(page2.testValue).toBe(0);
    expect(observedTestValue).toBe(1);
    page2.ensureDetailTable();
    expect(page2.testValue).toBe(2);
    expect(observedTestValue).toBe(2);
  });

  it('collapses lazy expanded nodes on reload', () => {
    class LazyPageWithTable extends PageWithTable {
      constructor() {
        super();
        this.lazyExpandingEnabled = true;
      }

      protected override _createDetailTable(): Table {
        return scout.create(Table, {
          parent: this.outline,
          columns: [{
            id: 'ColorColumn',
            objectType: Column,
            primaryKey: true
          }]
        });
      }

      protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
        return $.resolvedPromise(['Red', 'Green', 'Blue']);
      }

      protected override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
        return tableData.map(color => {
          return {
            cells: [color]
          };
        });
      }

      protected override _createChildPage(row): Page {
        return scout.create(Page, {
          parent: this.outline,
          text: this.detailTable.columnById('ColorColumn').cellValue(row)
        });
      }
    }

    let page = scout.create(LazyPageWithTable, {
      parent: outline
    });
    outline.insertNode(page);
    outline.selectNode(page);
    jasmine.clock().tick(1);

    expect(page.childNodes.length).toBe(3);
    expect(page.expanded).toBe(false);
    expect(page.expandedLazy).toBe(false);
    expect(page.detailTable).toBeInstanceOf(Table);
    expect(page.detailTable.rows.length).toBe(3);
    expect(page.detailTable.hasReloadHandler).toBe(true);

    // -----

    // Drill down to child node -> page should be expanded lazily

    outline.drillDown(page.childNodes[0]);
    expect(page.expanded).toBe(true);
    expect(page.expandedLazy).toBe(true);

    // -----

    // Reload page -> should be collapsed automatically

    outline.selectNode(page);
    page.detailTable.reload();
    jasmine.clock().tick(1);

    expect(page.childNodes.length).toBe(3);
    expect(page.expanded).toBe(false);
    expect(page.expandedLazy).toBe(false);

    // -----

    // Expand page non-lazily and reload -> page should stay expanded

    page.setExpanded(true);
    expect(page.expanded).toBe(true);
    expect(page.expandedLazy).toBe(false);

    page.detailTable.reload();
    jasmine.clock().tick(1);
    expect(page.childNodes.length).toBe(3);
    expect(page.expanded).toBe(true);
    expect(page.expandedLazy).toBe(false);
  });

  describe('getSearchFilterText', () => {
    it('only considers the texts of the actual search filter', async () => {
      jasmine.clock().uninstall();
      page.detailTable.setTableControls([{
        objectType: SearchFormTableControl,
        form: {
          objectType: SearchForm
        }
      }]);
      let searchForm = page.getSearchForm();
      let stringField = searchForm.findChild(StringField);
      let numberField = searchForm.findChild(NumberField);
      stringField.setValue('str');
      numberField.setValue(3);

      let searchMenu = searchForm.findChild(SearchMenu);
      searchMenu.doAction();
      await page.getSearchForm().when('search');
      expect(await page.getSearchFilterText()).toBe('str field: str\nnum field: 3');

      stringField.setValue(null);
      numberField.setValue(4);
      expect(await page.getSearchFilterText()).toBe('str field: str\nnum field: 3'); // Still the same because user did not hit search yet

      searchMenu.doAction();
      await page.getSearchForm().when('search');
      expect(await page.getSearchFilterText()).toBe('num field: 4');

      let resetMenu = searchForm.findChild(ResetMenu);
      resetMenu.doAction();
      await page.getSearchForm().when('reset');
      expect(await page.getSearchFilterText()).toBe('');
    });
  });

  it('keeps the search form data and used search filter in sync', async () => {
    jasmine.clock().uninstall();
    page.detailTable.setTableControls([{
      objectType: SearchFormTableControl,
      form: {
        objectType: SearchFormWithInitialValues
      }
    }]);
    let usedFilter: SearchData;
    page._loadTableData = (searchFilter: SearchData) => {
      usedFilter = searchFilter;
      return $.resolvedPromise();
    };

    page.loadTableData();
    expect(usedFilter.strVal).toBe('initial');
    expect(usedFilter.numVal).toBe(null);

    let searchForm = page.getSearchForm();
    let searchFormData = searchForm.data as SearchData;
    expect(searchFormData.strVal).toBe('initial');
    expect(searchFormData.numVal).toBe(null);

    let stringField = searchForm.findChild(StringField);
    let numberField = searchForm.findChild(NumberField);
    stringField.setValue('new');
    numberField.setValue(2);
    expect(searchFormData.strVal).toBe('initial'); // Search not executed yet
    expect(searchFormData.numVal).toBe(null);

    let searchMenu = searchForm.findChild(SearchMenu);
    searchMenu.doAction();
    await page.getSearchForm().when('search');
    searchFormData = searchForm.data as SearchData;
    expect(usedFilter.strVal).toBe('new');
    expect(usedFilter.numVal).toBe(2);
    expect(searchFormData.strVal).toBe('new');
    expect(searchFormData.numVal).toBe(2);
  });
});
