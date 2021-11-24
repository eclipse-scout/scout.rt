/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {LocaleSpecHelper, TableSpecHelper} from '../../../src/testing/index';
import {DecimalFormat, scout} from '../../../src/index';

describe('AggregateTableControl', () => {
  let session;
  let helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
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

  function createAggregateTC(model) {
    let defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('AggregateTableControl', model);
  }

  function $aggregateRow(tableControl) {
    return tableControl.$contentContainer;
  }

  function createFilter(acceptFunc) {
    return {
      accept: acceptFunc,
      createKey: () => {
        return 1; // dummy value
      },
      createLabel: () => ''
    };
  }

  describe('aggregate', () => {
    let model, table, column0, column1, column2, rows, tableControl;

    function prepareTable() {
      let columns = [helper.createModelColumn('col1'),
        helper.createModelColumn('col2', 'NumberColumn'),
        helper.createModelColumn('col3', 'NumberColumn')
      ];
      rows = helper.createModelRows(columns, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);

      tableControl = createAggregateTC({
        table: table
      });
      tableControl.selected = true;
      table._setTableControls([tableControl]);

      column0 = table.columns[0];
      column1 = table.columns[1];
      column1.setAggregationFunction('sum');
      column2 = table.columns[2];
      column2.setAggregationFunction('sum');
    }

    it('creates an aggregate row', () => {
      prepareTable();
      table.render();

      expect($aggregateRow(tableControl).length).toBe(1);
    });

    it('sums up numbers in a number column', () => {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render();

      let $aggrRow = $aggregateRow(tableControl);
      let $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(0).text()).toBe('\u00a0'); // nbsp
      expect($aggrCells.eq(1).text()).toBe('6');

      // show nothing if all values are missing
      prepareTable();
      rows[0].cells[1].value = null;
      rows[1].cells[1].value = null;
      rows[2].cells[1].value = null;
      table.render();

      $aggrRow = $aggregateRow(tableControl);
      $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(0).text()).toBe('\u00a0'); // nbsp
      expect($aggrCells.eq(1).text()).toBe('\u00a0'); // nbsp

      // show 0 if values add up to zero
      prepareTable();
      rows[0].cells[1].value = -10;
      rows[1].cells[1].value = 10;
      rows[2].cells[1].value = null;
      table.render();

      $aggrRow = $aggregateRow(tableControl);
      $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(0).text()).toBe('\u00a0'); // nbsp
      expect($aggrCells.eq(1).text()).toBe('0');
    });

    it('aggregation type none does not aggregate', () => {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render();
      table.changeAggregation(table.columns[1], 'none');

      let $aggrRow = $aggregateRow(tableControl);
      let $aggrCells = $aggrRow.children('.table-cell');
      expect($aggrCells.eq(0).text()).toBe('\u00a0'); // nbsp
      expect($aggrCells.eq(1).text()).toBe('\u00a0'); // nbsp
    });

    it('sums up numbers in a number column but only on filtered rows', () => {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render();

      table.addFilter(createFilter(
        row => {
          return row.id !== table.rows[2].id;
        }));
      let $aggrRow = $aggregateRow(tableControl);
      let $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(0).text()).toBe('\u00a0'); // nbsp
      expect($aggrCells.eq(1).text()).toBe('3');
    });

    it('sums up numbers in a number column and considers format pattern', () => {
      prepareTable();
      rows[0].cells[1].value = 1000;
      rows[1].cells[1].value = 1000;
      rows[2].cells[1].value = 2000;
      column1.decimalFormat = new DecimalFormat(session.locale, {
        pattern: '#.00'
      });
      table.render();

      let $aggrRow = $aggregateRow(tableControl);
      let $aggrCell = $aggrRow.children('.table-cell').eq(1);
      $aggrCell.children('.table-cell-icon').remove();
      expect($aggrCell.text()).toBe('4000.00');
    });

    it('sums up numbers in a number column and considers rounded values fo aggregation', () => {
      prepareTable();
      rows[0].cells[1].value = 0.005;
      rows[1].cells[1].value = 0.006;
      rows[2].cells[1].value = 0.005;
      column1.decimalFormat = new DecimalFormat(session.locale, {
        pattern: '#.00'
      });
      table.render();

      let $aggrRow = $aggregateRow(tableControl);
      let $aggrCell = $aggrRow.children('.table-cell').eq(1);
      $aggrCell.children('.table-cell-icon').remove();
      expect($aggrCell.text()).toBe('.03');
    });

    it('updates aggregation if a row is inserted', () => {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render();

      let $aggrRow = $aggregateRow(tableControl);
      let $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(1).text()).toBe('6');

      table.insertRow({cells: ['new row', 5, null]});
      $aggrRow = $aggregateRow(tableControl);
      $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(1).text()).toBe('11');
    });

    it('updates aggregation if a row is updated', () => {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render();

      let $aggrRow = $aggregateRow(tableControl);
      let $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(1).text()).toBe('6');

      table.columns[1].setCellValue(table.rows[0], 3);
      $aggrRow = $aggregateRow(tableControl);
      $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(1).text()).toBe('8');
    });

    it('updates aggregation if a row is deleted', () => {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render();

      let $aggrRow = $aggregateRow(tableControl);
      let $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(1).text()).toBe('6');

      table.deleteRow(table.rows[0]);
      $aggrRow = $aggregateRow(tableControl);
      $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(1).text()).toBe('5');
    });

    it('does not apply background effect', () => {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.setColumnBackgroundEffect(column1, 'colorGradient1');
      table.render();

      let $aggrRow = $aggregateRow(tableControl);
      let $aggrCells = $aggrRow.children('.table-cell');
      expect(table.$cell(column1, table.$rows().eq(0)).attr('style').indexOf('background-color') > -1).toBe(true); // Real cell must have background effect
      expect($aggrCells.eq(0).attr('style').indexOf('background-color') > -1).toBe(false); // Aggregate cell must not have background effect
    });
  });

  describe('eanbled state', () => {
    let rows, model, table, tableControl;

    function prepareTable() {
      let columns = [helper.createModelColumn('col1'),
        helper.createModelColumn('col2')
      ];
      rows = helper.createModelRows(2, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);
    }

    it('is false if there are no number columns', () => {
      prepareTable();
      tableControl = createAggregateTC({
        enabled: true,
        selected: true,
        table: table
      });
      table._setTableControls([tableControl]);
      table.render();

      expect(tableControl.enabled).toBe(false);
      expect(tableControl.selected).toBe(false);
    });

    it('is true if there is at least one number column', () => {
      let columns = [
        helper.createModelColumn('col1'),
        helper.createModelColumn('col2', 'NumberColumn')
      ];
      rows = helper.createModelRows(columns, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);
      table.columns[1].setAggregationFunction('sum');

      tableControl = createAggregateTC({
        enabled: false,
        selected: true,
        table: table
      });
      table._setTableControls([tableControl]);
      table.render();

      expect(tableControl.enabled).toBe(true);
      expect(tableControl.selected).toBe(true);
    });

    it('is false if there is a number column but without an aggregate function', () => {
      let columns = [
        helper.createModelColumn('col1'),
        helper.createModelColumn('col2', 'NumberColumn')
      ];
      rows = helper.createModelRows(columns, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);
      table.columns[1].setAggregationFunction('none');

      tableControl = createAggregateTC({
        enabled: false,
        selected: false,
        table: table
      });
      table._setTableControls([tableControl]);
      table.render();

      expect(tableControl.enabled).toBe(false);
      expect(tableControl.selected).toBe(false);
    });

  });

  describe('selected state', () => {
    let table;

    function prepareTable() {
      let columns = [helper.createModelColumn('col1'),
        helper.createModelColumn('col2')
      ];
      let rows = helper.createModelRows(2, 3);
      let model = helper.createModel(columns, rows);
      table = helper.createTable(model);
    }

    it('is false if control is not enabled initially', () => {
      prepareTable();
      let tableControl = createAggregateTC({
        enabled: false,
        selected: true,
        table: table
      });
      table.setTableControls([tableControl]);
      table.render();
      expect(tableControl.enabled).toBe(false);
      expect(tableControl.selected).toBe(false);
    });

    it('is set to false if control will be disabled', () => {
      prepareTable();
      let tableControl = createAggregateTC({
        enabled: false,
        selected: true,
        table: table
      });
      table.setTableControls([tableControl]);
      table.render();
      expect(tableControl.enabled).toBe(false);
      expect(tableControl.selected).toBe(false);

      // Setting it explicitly to true has no effect either
      tableControl.setSelected(true);
      expect(tableControl.enabled).toBe(false);
      expect(tableControl.selected).toBe(false);
    });

  });

});
