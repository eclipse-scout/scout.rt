/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AggregateTableControl, arrays, Column, Device, NumberColumn, Range, scout, Table} from '../../src/index';
import {SpecTable, SpecTableAdapter, TableModelWithCells, TableRowModelWithCells, TableSpecHelper} from '../../src/testing/index';

describe('Table Grouping', () => {

  let session: SandboxSession, helper: TableSpecHelper, model: TableModelWithCells, table: SpecTable;
  let column0: Column, column1: Column, column2: Column, column3: NumberColumn, column4: NumberColumn, rows: TableRowModelWithCells[], columns, adapter: SpecTableAdapter;
  let $colHeaders, $header0, $header1;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();

  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  function prepareTable(withAdapter?: boolean) {
    columns = [
      helper.createModelColumn('col0'),
      helper.createModelColumn('col1'),
      helper.createModelColumn('col2'),
      helper.createModelColumn('col3', NumberColumn),
      helper.createModelColumn('col4', NumberColumn)
    ];
    columns[0].index = 0;
    columns[1].index = 1;
    columns[2].index = 2;
    columns[3].index = 3;
    columns[4].index = 4;
    rows = helper.createModelRows(5, 8);
    model = helper.createModel(columns, rows);
    if (withAdapter) {
      adapter = helper.createTableAdapter(model);
      table = adapter.createWidget(model, session.desktop);
    } else {
      table = helper.createTable(model);
    }
    column0 = table.columns[0];
    column1 = table.columns[1];
    column2 = table.columns[2];
    column3 = table.columns[3] as NumberColumn;
    column4 = table.columns[4] as NumberColumn;
    column3.setAggregationFunction('sum');
    column4.setAggregationFunction('sum');
  }

  /**
   * col0  |  col1  |  col2  |  col3  |  col4
   * ----------------------------------------
   * a     |  c     |  e     |  1     |  3
   * a     |  c     |  f     |  2     |  6
   * a     |  d     |  e     |  3     |  9
   * a     |  d     |  f     |  4     |  12
   * b     |  c     |  e     |  5     |  15
   * b     |  c     |  f     |  6     |  18
   * b     |  d     |  e     |  7     |  21
   * b     |  d     |  f     |  8     |  24
   */
  function prepareContent() {
    let column0Values = ['a', 'b'],
      column1Values = ['c', 'd'],
      column2Values = ['e', 'f'],
      value, text, j;

    for (let i = 0; i < rows.length; i++) {
      value = column0Values[Math.floor(i / 4)];
      text = value.toString();
      rows[i].cells[0] = column0.initCell(helper.createModelCell(text, value));

      value = column1Values[(Math.floor(i / 2)) % 2];
      text = value.toString();
      rows[i].cells[1] = column1.initCell(helper.createModelCell(text, value));

      value = column2Values[i % 2];
      text = value.toString();
      rows[i].cells[2] = column2.initCell(helper.createModelCell(text, value));

      j = i + 1;
      rows[i].cells[3].value = j;
      rows[i].cells[3].text = j.toString();
      rows[i].cells[4].value = j * 3;
      rows[i].cells[4].text = (j * 3).toString();
    }

  }

  function render(table) {
    table.render();
    $colHeaders = table.header.$container.find('.table-header-item');
    $header0 = $colHeaders.eq(0);
    $header1 = $colHeaders.eq(1);
  }

  function addGrouping(table, column, multi) {
    table.groupColumn(column, multi, 'asc', false);
  }

  function removeGrouping(table, column) {
    table.groupColumn(column, '', 'asc', true);
  }

  function assertGroupingProperty(table, ...args) {
    let i, expectGrouped = arrays.init(5, false);
    for (i = 0; i < args.length; i++) {
      expectGrouped[args[i]] = true;
    }

    for (i = 0; i < 5; i++) {
      if (expectGrouped[i]) {
        expect(table.columns[i].grouped).toBeTruthy();
      } else {
        expect(table.columns[i].grouped).toBeFalsy();
      }
    }
  }

  function find$aggregateRows(table) {
    return table.$data.find('.table-aggregate-row');
  }

  function assertGroupingValues(table, column, values) {
    let i, c, $sumCell;
    c = table.columns.indexOf(column);
    expect(find$aggregateRows(table).length).toBe(values.length);

    for (i = 0; i < values.length; i++) {
      $sumCell = find$aggregateRows(table).eq(i).children().eq(c);
      $sumCell.find('.table-cell-icon').remove();
      expect($sumCell.text()).toBe(values[i]);
    }
  }

  function createAndRegisterColumnFilter(table, column, selectedValues) {
    return helper.createAndRegisterColumnFilter({
      table: table,
      session: session,
      column: column,
      selectedValues: selectedValues
    });
  }

  it('renders an aggregate row for each group', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    expect(table._aggregateRows.length).toBe(0);
    addGrouping(table, column0, false);
    expect(find$aggregateRows(table).length).toBe(2);
    expect(table._aggregateRows.length).toBe(2);
  });

  it('updates aggregate rows when column structure changes', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    let tableControl = scout.create(AggregateTableControl, {
      parent: table,
      table: table,
      selected: true
    });
    table.setTableControls([tableControl]);
    prepareContent();
    render(table);

    // 0: col0<String> | 1: col1<String> | 2: col2<String> | 3: col3<Number> | 4: col4<Number>
    expect(table._aggregateRows.length).toBe(0);
    expect(tableControl.aggregateRow.length).toBe(5);
    expect(tableControl.aggregateRow[0]).toBeUndefined();
    expect(tableControl.aggregateRow[1]).toBeUndefined();
    expect(tableControl.aggregateRow[2]).toBeUndefined();
    expect(tableControl.aggregateRow[3]).toBeDefined();
    expect(tableControl.aggregateRow[4]).toBeDefined();

    // Add grouping
    addGrouping(table, column0, false);
    // 0: col0<String> [G] | 1: col1<String> | 2: col2<String> | 3: col3<Number> | 4: col4<Number>
    expect(table._aggregateRows.length).toBe(2);
    expect(table._aggregateRows[0].contents.length).toBe(5);
    expect(table._aggregateRows[0].contents[0]).toBeUndefined();
    expect(table._aggregateRows[0].contents[1]).toBeUndefined();
    expect(table._aggregateRows[0].contents[2]).toBeUndefined();
    expect(table._aggregateRows[0].contents[3]).toBeDefined();
    expect(table._aggregateRows[0].contents[4]).toBeDefined();
    expect(tableControl.aggregateRow.length).toBe(5);
    expect(tableControl.aggregateRow[0]).toBeUndefined();
    expect(tableControl.aggregateRow[1]).toBeUndefined();
    expect(tableControl.aggregateRow[2]).toBeUndefined();
    expect(tableControl.aggregateRow[3]).toBeDefined();
    expect(tableControl.aggregateRow[4]).toBeDefined();

    // Move last column between first and second column
    table.moveColumn(table.columns[4], 4, 1);
    // 0: col0<String> [G] | 1: col4<Number> | 2: col1<String> | 3: col2<String> | 4: col3<Number>
    expect(table._aggregateRows.length).toBe(2);
    expect(table._aggregateRows[0].contents.length).toBe(5);
    expect(table._aggregateRows[0].contents[0]).toBeUndefined();
    expect(table._aggregateRows[0].contents[1]).toBeDefined();
    expect(table._aggregateRows[0].contents[2]).toBeUndefined();
    expect(table._aggregateRows[0].contents[3]).toBeUndefined();
    expect(table._aggregateRows[0].contents[4]).toBeDefined();
    expect(tableControl.aggregateRow.length).toBe(5);
    expect(tableControl.aggregateRow[0]).toBeUndefined();
    expect(tableControl.aggregateRow[1]).toBeDefined();
    expect(tableControl.aggregateRow[2]).toBeUndefined();
    expect(tableControl.aggregateRow[3]).toBeUndefined();
    expect(tableControl.aggregateRow[4]).toBeDefined();

    // Set the third column to displayable=false
    table.columns[2].setDisplayable(false);
    // 0: col0<String> [G] | 1: col4<Number> | 2: ( col1<String> ) | 3: col2<String> | 4: col3<Number>
    expect(table._aggregateRows.length).toBe(2);
    expect(table._aggregateRows[0].contents.length).toBe(4);
    expect(table._aggregateRows[0].contents[0]).toBeUndefined();
    expect(table._aggregateRows[0].contents[1]).toBeDefined();
    expect(table._aggregateRows[0].contents[2]).toBeUndefined();
    expect(table._aggregateRows[0].contents[3]).toBeDefined();
    expect(tableControl.aggregateRow.length).toBe(4);
    expect(tableControl.aggregateRow[0]).toBeUndefined();
    expect(tableControl.aggregateRow[1]).toBeDefined();
    expect(tableControl.aggregateRow[2]).toBeUndefined();
    expect(tableControl.aggregateRow[3]).toBeDefined();

    // Hide the second column
    table.columns[1].setVisible(false);
    // 0: col0<String> [G] | (1: col4<Number> ) | (2: col1<String> ) | 3: col2<String> | 4: col3<Number>
    expect(table._aggregateRows.length).toBe(2);
    expect(table._aggregateRows[0].contents.length).toBe(3);
    expect(table._aggregateRows[0].contents[0]).toBeUndefined();
    expect(table._aggregateRows[0].contents[1]).toBeUndefined();
    expect(table._aggregateRows[0].contents[2]).toBeDefined();
    expect(tableControl.aggregateRow.length).toBe(3);
    expect(tableControl.aggregateRow[0]).toBeUndefined();
    expect(tableControl.aggregateRow[1]).toBeUndefined();
    expect(tableControl.aggregateRow[2]).toBeDefined();

    // Show the previously hidden column as second-last column
    table.organizer.showColumns([table.columns[1]], table.columns[3]);
    // 0: col0<String> [G] | 1: col1<String> | 2: col2<String> | 3: col4<Number> | 4: col3<Number>
    expect(table._aggregateRows.length).toBe(2);
    expect(table._aggregateRows[0].contents.length).toBe(4);
    expect(table._aggregateRows[0].contents[0]).toBeUndefined();
    expect(table._aggregateRows[0].contents[1]).toBeUndefined();
    expect(table._aggregateRows[0].contents[2]).toBeDefined();
    expect(table._aggregateRows[0].contents[3]).toBeDefined();
    expect(tableControl.aggregateRow.length).toBe(4);
    expect(tableControl.aggregateRow[0]).toBeUndefined();
    expect(tableControl.aggregateRow[1]).toBeUndefined();
    expect(tableControl.aggregateRow[2]).toBeDefined();
    expect(tableControl.aggregateRow[3]).toBeDefined();

    // Table control is deselected when all aggregated columns are hidden
    table.organizer.hideColumn(table.columns[3]);
    table.organizer.hideColumn(table.columns[4]);
    // 0: col0<String> [G] | (1: col1<String> ) | 2: col2<String> | (3: col4<Number> ) | ( 4: col3<Number> )
    expect(table._aggregateRows.length).toBe(2);
    expect(table._aggregateRows[0].contents.length).toBe(2);
    expect(table._aggregateRows[0].contents[0]).toBeUndefined();
    expect(table._aggregateRows[0].contents[1]).toBeUndefined();
    expect(tableControl.selected).toBe(false);
  });

  it('considers groupingStyle -> aggregate rows must be rendered previous to the grouped rows', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    table.groupingStyle = Table.GroupingStyle.TOP;
    prepareContent();
    render(table);
    addGrouping(table, column0, false);

    let // check in the DOM if the aggregate row comes previous to the first
      // row of the group
      $mixedRows = table.$data.children('.table-row,.table-aggregate-row'),
      $aggregateRows = table.$data.find('.table-aggregate-row'),
      aggrRow1Pos = $mixedRows.index($aggregateRows.get(0)),
      aggrRow2Pos = $mixedRows.index($aggregateRows.get(1)),
      rowFirstPos = $mixedRows.index(table.$data.find('.table-row.first')),
      rowLastPos = $mixedRows.index(table.$data.find('.table-row.last'));

    expect(aggrRow1Pos < rowFirstPos).toBe(true);
    expect(aggrRow2Pos < rowLastPos).toBe(true);
  });

  it('considers view range -> only renders an aggregate row for rendered rows', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    table.viewRangeSize = 4;
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    expect(table._aggregateRows.length).toBe(0);
    addGrouping(table, column0, false); // -> 2 groups with 4 rows each

    // Only the first group should be rendered
    expect(table._aggregateRows.length).toBe(2);
    expect(find$aggregateRows(table).length).toBe(1);
    expect(table.$rows().length).toBe(4);
    expect(table.$rows(true).length).toBe(5);
    expect(table._aggregateRows[0].$row).toBeTruthy();
    expect(table._aggregateRows[1].$row).toBeFalsy();
  });

  it('considers view range -> doesn\'t render an aggregate row if the last row of the group is not rendered', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    table.viewRangeSize = 3;
    table.groupingStyle = Table.GroupingStyle.BOTTOM;
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    expect(table._aggregateRows.length).toBe(0);
    addGrouping(table, column0, false); // -> 2 groups with 4 rows each

    // Only 3 rows of the first group are rendered -> don't display aggregate
    // row
    expect(table._aggregateRows.length).toBe(2);
    expect(find$aggregateRows(table).length).toBe(0);
    expect(table.$rows().length).toBe(3);
    expect(table.$rows(true).length).toBe(3);
    expect(table._aggregateRows[0].$row).toBeFalsy();
    expect(table._aggregateRows[1].$row).toBeFalsy();

    spyOn(table, '_calculateCurrentViewRange').and.returnValue(new Range(1, 4));
    table._renderViewport();

    // Last row is rendered -> aggregate row needs to be rendered as well
    expect(table._aggregateRows.length).toBe(2);
    expect(find$aggregateRows(table).length).toBe(1);
    expect(table.$rows().length).toBe(3);
    expect(table.$rows(true).length).toBe(4);
    expect(table._aggregateRows[0].$row).toBeTruthy();
    expect(table._aggregateRows[1].$row).toBeFalsy();
  });

  it('regroups if rows get inserted', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    expect(find$aggregateRows(table).length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['10', '26']);
    assertGroupingValues(table, column4, ['30', '78']);

    // add new row for group 1
    let rows = [{
      cells: ['a', 'xyz', 'xyz', 10, 20]
    }];
    table.insertRows(rows);

    expect(find$aggregateRows(table).length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['20', '26']);
    assertGroupingValues(table, column4, ['50', '78']);
  });

  it('regroups if rows get inserted, event is from server and table was empty', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable(true);
    render(table);
    table.deleteAllRows();
    expect(table.rows.length).toBe(0);
    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    expect(find$aggregateRows(table).length).toBe(0);

    // add new row for group 1
    let rows = [{
      cells: ['a', 'xyz', 'xyz', 10, 20]
    }];
    table.insertRows(rows);

    expect(find$aggregateRows(table).length).toBe(1);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['10']);
    assertGroupingValues(table, column4, ['20']);
  });

  it('does not regroup if rows get inserted, event is from server and table was not empty', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable(true);
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    expect(find$aggregateRows(table).length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['10', '26']);
    assertGroupingValues(table, column4, ['30', '78']);

    // add new row for group 1
    let rows = [{
      cells: ['a', 'xyz', 'xyz', 10, 20]
    }];
    table.insertRows(rows);

    // Still wrong grouping because group was not executed. There will be a rowOrderChanged event which will do it, see comments in table.insertRows
    expect(find$aggregateRows(table).length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['10', '26']);
    assertGroupingValues(table, column4, ['30', '78']);
  });

  it('regroups when a filter is applied', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    expect(find$aggregateRows(table).length).toBe(2);

    createAndRegisterColumnFilter(table, column1, ['c']);

    assertGroupingValues(table, column3, ['3', '11']);
    assertGroupingValues(table, column4, ['9', '33']);

  });

  it('regroups if rows get deleted', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    expect(find$aggregateRows(table).length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['10', '26']);
    assertGroupingValues(table, column4, ['30', '78']);

    table.deleteRow(table.rows[0]);
    expect(find$aggregateRows(table).length).toBe(2);
    expect(table._aggregateRows.length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['9', '26']);
    assertGroupingValues(table, column4, ['27', '78']);

    table.deleteRows([table.rows[0], table.rows[1], table.rows[2]]);
    expect(find$aggregateRows(table).length).toBe(1);
    expect(table._aggregateRows.length).toBe(1);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['26']);
    assertGroupingValues(table, column4, ['78']);
  });

  it('removes aggregate rows if all rows get deleted', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    expect(find$aggregateRows(table).length).toBe(2);
    expect(table._aggregateRows.length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['10', '26']);
    assertGroupingValues(table, column4, ['30', '78']);

    table.deleteAllRows();
    expect(find$aggregateRows(table).length).toBe(0);
    expect(table._aggregateRows.length).toBe(0);
    assertGroupingProperty(table, 0);
  });

  it('regroups if rows get updated', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    expect(find$aggregateRows(table).length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['10', '26']);
    assertGroupingValues(table, column4, ['30', '78']);

    let row = {
      id: table.rows[1].id,
      cells: ['a', 'xyz', 'xyz', 10, 20]
    };
    table.updateRows([row]);
    expect(find$aggregateRows(table).length).toBe(2);
    expect(table._aggregateRows.length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['18', '26']);
    assertGroupingValues(table, column4, ['44', '78']);
  });

  it('may group column 0 only', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    expect(find$aggregateRows(table).length).toBe(2);
    assertGroupingProperty(table, 0);
    assertGroupingValues(table, column3, ['10', '26']);
    assertGroupingValues(table, column4, ['30', '78']);
    removeGrouping(table, column0);
    expect(find$aggregateRows(table).length).toBe(0);
    assertGroupingProperty(table);
  });

  it('may group column 1 only', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column1, false);
    expect(find$aggregateRows(table).length).toBe(2);
    assertGroupingProperty(table, 1);
    assertGroupingValues(table, column3, ['14', '22']);
    assertGroupingValues(table, column4, ['42', '66']);
    removeGrouping(table, column1);
    expect(find$aggregateRows(table).length).toBe(0);
    assertGroupingProperty(table);
  });

  it('may group columns 0 (avg) and 1 (sum)', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);
    column3.setAggregationFunction('avg');
    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    addGrouping(table, column1, true);
    expect(find$aggregateRows(table).length).toBe(4);
    assertGroupingProperty(table, 0, 1);
    assertGroupingValues(table, column3, ['1.5', '3.5', '5.5', '7.5']);
    assertGroupingValues(table, column4, ['9', '21', '33', '45']);
    removeGrouping(table, column0);
    removeGrouping(table, column1);
    expect(find$aggregateRows(table).length).toBe(0);
    assertGroupingProperty(table);

  });

  it('may group columns 0, 1 and 2', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    addGrouping(table, column1, true);
    addGrouping(table, column2, true);
    expect(find$aggregateRows(table).length).toBe(8);
    assertGroupingProperty(table, 0, 1, 2);
    assertGroupingValues(table, column3, ['1', '2', '3', '4', '5', '6', '7', '8']);
    assertGroupingValues(table, column4, ['3', '6', '9', '12', '15', '18', '21', '24']);
    removeGrouping(table, column0);
    removeGrouping(table, column1);
    removeGrouping(table, column2);
    expect(find$aggregateRows(table).length).toBe(0);
    assertGroupingProperty(table);

  });

  // vary order
  it('may group columns 2 and 1', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column2, false);
    addGrouping(table, column1, true);
    expect(find$aggregateRows(table).length).toBe(4);
    assertGroupingProperty(table, 2, 1);
    assertGroupingValues(table, column3, ['6', '10', '8', '12']);
    assertGroupingValues(table, column4, ['18', '30', '24', '36']);
    removeGrouping(table, column1);
    removeGrouping(table, column2);
    expect(find$aggregateRows(table).length).toBe(0);
    assertGroupingProperty(table);

  });

  it('may group column 1 only after grouping column 0 first', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    addGrouping(table, column2, true);
    addGrouping(table, column1, false);
    expect(find$aggregateRows(table).length).toBe(2);
    assertGroupingProperty(table, 1);
    assertGroupingValues(table, column3, ['14', '22']);
    assertGroupingValues(table, column4, ['42', '66']);
    removeGrouping(table, column1);
    expect(find$aggregateRows(table).length).toBe(0);
    assertGroupingProperty(table);
  });

  it('may group column 1 and 2 after grouping column 0 first', () => {
    if (!Device.get().supportsInternationalization()) {
      return;
    }
    prepareTable();
    prepareContent();
    render(table);

    expect(find$aggregateRows(table).length).toBe(0);
    addGrouping(table, column0, false);
    addGrouping(table, column2, true);
    addGrouping(table, column1, true);
    removeGrouping(table, column0);
    expect(find$aggregateRows(table).length).toBe(4);
    assertGroupingProperty(table, 1, 2);
    assertGroupingValues(table, column3, ['6', '10', '8', '12']);
    assertGroupingValues(table, column4, ['18', '30', '24', '36']);
    removeGrouping(table, column1);
    removeGrouping(table, column2);
    expect(find$aggregateRows(table).length).toBe(0);
    assertGroupingProperty(table);
  });

});
