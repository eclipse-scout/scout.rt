/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ChartTableControl} from '../../../src/index';
import {TableSpecHelper} from '@eclipse-scout/core/testing';
import {Event, InitModelOf, Table, TableControl} from '@eclipse-scout/core';

describe('ChartTableControl', () => {
  let $sandbox: JQuery, session: SandboxSession, helper: TableSpecHelper, $div: JQuery;

  beforeEach(() => {
    $div = $('<div></div>');
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    helper = new TableSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  function createModelFromTable(table: Table): InitModelOf<ChartTableControl> {
    let model = createSimpleModel('ChartTableControl', session);
    let defaults = {
      enabled: true,
      visible: true,
      selected: true,
      table: table
    };

    model = $.extend({}, defaults, model);
    return model;
  }

  function createModel(rows: number, columns: number): InitModelOf<ChartTableControl> {
    let tableModel = helper.createModelFixture(rows, columns);
    let table = helper.createTable(tableModel);

    return createModelFromTable(table);
  }

  function createChartTableControl(model: InitModelOf<ChartTableControl>): SpecChartTableControl {
    const chartTableControl = new SpecChartTableControl();
    chartTableControl.init(model);
    chartTableControl.table._setTableControls([chartTableControl]);
    return chartTableControl;
  }

  class SpecChartTableControl extends ChartTableControl {
    declare table: SpecTable;
    declare _tableUpdatedHandler: (e: Event<Table>) => void;

    override _renderContent($parent: JQuery) {
      super._renderContent($parent);
    }
  }

  class SpecTable extends Table {

    override _setTableControls(controls: TableControl[]) {
      super._setTableControls(controls);
    }
  }

  describe('renderDefaultChart', () => {

    it('does not draw a chart, if there are multiple columns in the table before all are removed', () => {
      const chartTableControl = createChartTableControl(createModel(2, 2));
      chartTableControl.table.render();
      expectEmptyChart(chartTableControl, false);
      chartTableControl.table.columns = [];
      chartTableControl._tableUpdatedHandler(null);
      jasmine.clock().tick(0);
      expectEmptyChart(chartTableControl, true);
    });

    it('does not draw a chart, if there are no columns', () => {
      const chartTableControl = createChartTableControl(createModel(0, 0));
      chartTableControl.table.render();
      expectEmptyChart(chartTableControl, true);
    });

    it('draws a chart for a single column', () => {
      const chartTableControl = createChartTableControl(createModel(1, 1));
      chartTableControl.table.render();
      expectEmptyChart(chartTableControl, false);
    });

    it('draws a chart for multiple columns', () => {
      const chartTableControl = createChartTableControl(createModel(2, 2));
      chartTableControl.table.render();
      expectEmptyChart(chartTableControl, false);
    });

    it('draws a chart with date grouping for a date column', () => {
      let columns = helper.createModelColumns(1, 'DateColumn');
      let c0 = helper.createModelCell('0', new Date());
      let rows = [helper.createModelRow(null, [c0])];
      let model = helper.createModel(columns, rows);
      let table = helper.createTable(model);
      const chartTableControl = createChartTableControl(createModelFromTable(table));
      chartTableControl._renderContent($div);

      expect(chartTableControl.chartGroup1.id).toBe(columns[0].id); // first column selected
      expect(chartTableControl.chartGroup1.modifier).toBe(256); // with date modifier
      expectEmptyChart(chartTableControl, false);
    });

    it('renders header texts that contain HTML as plain text', () => {
      let model = createModel(1, 1);
      let firstCol = model.table.columns[0];
      firstCol.headerHtmlEnabled = true;
      firstCol.text = '<b>Plain</b><br>Text';
      const chartTableControl = createChartTableControl(model);
      chartTableControl._renderContent($sandbox);
      let $result = $sandbox.find('.select-axis.selected');
      expect($result.length).toBe(2);
      expect($result.get(0).innerHTML).toBe('Plain Text');
    });
  });

  describe('renderAfterColumnUpdate', () => {

    it('does not draw a chart, if columns are updated, but rows are not updated yet', () => {
      const chartTableControl = createChartTableControl(createModel(2, 1));
      // remove first column
      chartTableControl.table.columns.shift();
      // rows are not yet updated
      chartTableControl._renderContent($div);
      expectEmptyChart(chartTableControl, true);
    });
  });

  function expectEmptyChart(chartTableControl: ChartTableControl, empty: boolean) {
    expect(chartTableControl.chart).toBeDefined();
    expect(((((chartTableControl.chart.config.data || {}).datasets || [])[0] || {}).data || []).length === 0).toBe(empty);
  }
});
