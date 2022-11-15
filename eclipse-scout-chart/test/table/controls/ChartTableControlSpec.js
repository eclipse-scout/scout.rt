/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ChartTableControl} from '../../../src/index';
import {TableSpecHelper} from '@eclipse-scout/core/testing';

/* global sandboxSession, createSimpleModel*/
describe('ChartTableControl', () => {
  let $sandbox, session, chartTableControl, helper, $div;

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

  function createModelFromTable(table) {
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

  function createModel(rows, columns) {
    let tableModel = helper.createModelFixture(rows, columns);
    let table = helper.createTable(tableModel);

    return createModelFromTable(table);
  }

  function createChartTableControl(model) {
    chartTableControl = new ChartTableControl();
    chartTableControl.init(model);
    chartTableControl.table._setTableControls([chartTableControl]);
    return chartTableControl;
  }

  describe('renderDefaultChart', () => {

    it('does not draw a chart, if there are multiple columns in the table before all are removed', () => {
      chartTableControl = createChartTableControl(createModel(2, 2));
      chartTableControl.table.render();
      expectEmptyChart(false);
      chartTableControl.table.columns = [];
      chartTableControl._tableUpdatedHandler();
      jasmine.clock().tick();
      expectEmptyChart(true);
    });

    it('does not draw a chart, if there are no columns', () => {
      chartTableControl = createChartTableControl(createModel(0, 0));
      chartTableControl.table.render();
      expectEmptyChart(true);
    });

    it('draws a chart for a single column', () => {
      chartTableControl = createChartTableControl(createModel(1, 1));
      chartTableControl.table.render();
      expectEmptyChart(false);
    });

    it('draws a chart for multiple columns', () => {
      chartTableControl = createChartTableControl(createModel(2, 2));
      chartTableControl.table.render();
      expectEmptyChart(false);
    });

    it('draws a chart with date grouping for a date column', () => {
      let columns = helper.createModelColumns(1, 'DateColumn');
      let c0 = helper.createModelCell(0, new Date());
      let rows = [helper.createModelRow(null, [c0])];
      let model = helper.createModel(columns, rows);
      let table = helper.createTable(model);
      chartTableControl = createChartTableControl(createModelFromTable(table));
      chartTableControl._renderContent($div);

      expect(chartTableControl.chartGroup1.id).toBe(columns[0].id); // first column selected
      expect(chartTableControl.chartGroup1.modifier).toBe(256); // with date modifier
      expectEmptyChart(false);
    });

    it('renders header texts that contain HTML as plain text', () => {
      let model = createModel(1, 1);
      let firstCol = model.table.columns[0];
      firstCol.headerHtmlEnabled = true;
      firstCol.text = '<b>Plain</b><br>Text';
      chartTableControl = createChartTableControl(model);
      chartTableControl._renderContent($sandbox);
      let $result = $sandbox.find('.select-axis.selected');
      expect($result.length).toBe(2);
      expect($result.get(0).innerHTML).toBe('Plain Text');
    });

  });

  describe('renderAfterColumnUpdate', () => {

    it('does not draw a chart, if columns are updated, but rows are not updated yet', () => {
      chartTableControl = createChartTableControl(createModel(2, 1));
      // remove first column
      chartTableControl.table.columns.shift();
      // rows are not yet updated
      chartTableControl._renderContent($div);
      expectEmptyChart(true);
    });

  });

  function expectEmptyChart(empty) {
    expect(chartTableControl.chart).toBeDefined();
    expect(((((chartTableControl.chart.config.data || {}).datasets || [])[0] || {}).data || []).length === 0).toBe(empty);
  }

});
