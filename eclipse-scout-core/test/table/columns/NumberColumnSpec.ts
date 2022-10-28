/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {LocaleSpecHelper, TableSpecHelper} from '../../../src/testing/index';
import {Cell, DecimalFormat, Locale, NumberColumn, Range, RemoteEvent, Status, styles} from '../../../src/index';

describe('NumberColumn', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;
  let locale: Locale;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('background effect', () => {
    let rgbLevel0 = 'rgb(255, 175, 175)';
    let rgbLevel50 = 'rgb(213, 195, 161)';
    let rgbLevel100 = 'rgb(171, 214, 147)';
    let barChartColor = 'rgb(128, 193, 208)';
    let imageLevel50 = 'linear-gradient(to left, ' + barChartColor + ' 0%, ' + barChartColor + ' 50%, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0) 100%)';
    let defaultBackgroundColor;

    beforeEach(() => {
      styles.put('column-background-effect-gradient1-start', {
        backgroundColor: rgbLevel0
      });
      styles.put('column-background-effect-gradient1-end', {
        backgroundColor: rgbLevel100
      });
      styles.put('column-background-effect-gradient2-start', {
        backgroundColor: rgbLevel100
      });
      styles.put('column-background-effect-gradient2-end', {
        backgroundColor: rgbLevel0
      });
      styles.put('column-background-effect-bar-chart', {
        backgroundColor: barChartColor
      });

      let $div = $('<div>').appendTo(session.$entryPoint);
      defaultBackgroundColor = $div.css('background-color');
    });

    afterEach(() => {
      styles.clearCache();
    });

    describe('colorGradient1', () => {
      it('colors cells from red to green', () => {
        let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        let table = helper.createTable(model);
        let column0 = table.columns[0] as NumberColumn;
        table.render();

        table.setColumnBackgroundEffect(column0, 'colorGradient1');
        expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
        expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel100);
      });

      it('colors cells according to rounded values', () => {
        let model = helper.createModelSingleColumnByValues([0, 0.005, 0.006, 0.02], 'NumberColumn');
        let table = helper.createTable(model);
        let column0 = table.columns[0] as NumberColumn;
        table.render();

        column0.decimalFormat = new DecimalFormat(locale, {
          pattern: '#.00'
        });
        table.setColumnBackgroundEffect(column0, 'colorGradient1');
        expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
        expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel50);
        expect(table.$cell(column0, table.rows[3].$row).css('background-color')).toBe(rgbLevel100);
      });
    });

    it('considers view range -> only colors rendered cells', () => {
      let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0] as NumberColumn;
      table.viewRangeSize = 2;
      table.render();

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
      expect(table.rows[2].$row).toBeFalsy();

      spyOn(table, '_calculateCurrentViewRange').and.returnValue(new Range(1, 3));
      table._renderViewport();

      expect(table.rows[0].$row).toBeFalsy();
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel100);
    });

    it('updates colors if row gets deleted', () => {
      let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0] as NumberColumn;
      table.render();

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      table.deleteRow(table.rows[2]);
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel100);
    });

    it('updates colors if row gets inserted', () => {
      let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0] as NumberColumn;
      table.render();

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      let row = helper.createModelRowByValues(undefined, 200);
      table.insertRow(row);
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(234, 185, 168)');
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe('rgb(213, 195, 161)');
      expect(table.$cell(column0, table.rows[3].$row).css('background-color')).toBe(rgbLevel100);
    });

    it('updates colors if row gets updated', () => {
      let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      (model.columns[0] as NumberColumn).backgroundEffect = 'colorGradient1';
      let table = helper.createTable(model);
      let column0 = table.columns[0];
      table.render();

      // Change row 0 value to 150, row 1 now has the lowest values
      let rows = helper.createModelRows(1, 1);
      rows[0].id = table.rows[0].id;
      rows[0].cells[0].value = 150;
      table.updateRows(rows);

      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel100);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel50);
    });

    it('colors cells if table gets rendered', () => {
      let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      (model.columns[0] as NumberColumn).backgroundEffect = 'colorGradient1';
      let table = helper.createTable(model);
      let column0 = table.columns[0];

      table.render();
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel100);
    });

    it('restores existing background color if background effect gets removed', () => {
      let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      (model.rows[1].cells[0] as Cell).backgroundColor = 'ff0000';
      let table = helper.createTable(model);
      let column0 = table.columns[0] as NumberColumn;
      table.render();

      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(255, 0, 0)');
      expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe('none');

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);

      table.setColumnBackgroundEffect(column0, null);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(255, 0, 0)');
      expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe('none');
    });

    describe('barChart', () => {
      it('does not overwrite existing background color', () => {
        let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        (model.rows[1].cells[0] as Cell).backgroundColor = 'ff0000';
        let table = helper.createTable(model);
        let column0 = table.columns[0] as NumberColumn;
        table.render();

        table.setColumnBackgroundEffect(column0, 'barChart');
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(255, 0, 0)');
        expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe(imageLevel50);
      });
    });

    describe('setBackgroundEffect', () => {
      it('changes the background effect', () => {
        let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        let table = helper.createTable(model);
        let column0 = table.columns[0] as NumberColumn;
        table.render();

        // initial: No effect
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(defaultBackgroundColor);
        expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe('none');

        table.setColumnBackgroundEffect(column0, 'colorGradient1');
        expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
        expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel100);

        table.setColumnBackgroundEffect(column0, 'colorGradient2');
        expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel100);
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
        expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel0);

        table.setColumnBackgroundEffect(column0, 'barChart');
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(defaultBackgroundColor);
        expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe(imageLevel50);

        // set to null: no effect
        table.setColumnBackgroundEffect(column0, null);
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(defaultBackgroundColor);
        expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe('none');
      });

      it('sends columnBackgroundEffectChanged event', () => {
        let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        let table = helper.createTable(model);
        let column0 = table.columns[0] as NumberColumn;
        table.render();

        linkWidgetAndAdapter(table, 'TableAdapter');
        table.setColumnBackgroundEffect(column0, 'barChart');

        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(1);

        let event = new RemoteEvent(table.id, 'columnBackgroundEffectChanged', {
          columnId: column0.id,
          backgroundEffect: 'barChart'
        });
        expect(mostRecentJsonRequest()).toContainEvents(event);
      });

      it('does not send columnBackgroundEffectChanged if server triggered it', () => {
        let model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        let table = helper.createTable(model);
        let column0 = table.columns[0] as NumberColumn;
        table.render();

        linkWidgetAndAdapter(table, 'TableAdapter');
        // @ts-ignore
        table.modelAdapter._onColumnBackgroundEffectChanged({
          eventParts: [{
            columnId: column0.id,
            backgroundEffect: 'barChart'
          }]
        });
        expect(column0.backgroundEffect).toBe('barChart');

        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(0);

        // It has to be sent if effect differs from what the server sent
        table.setColumnBackgroundEffect(column0, 'colorGradient1');
        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(1);
      });
    });
  });

  describe('calculateMinMaxValues', () => {
    it('calculates the min/max values based on rounded values', () => {
      let model = helper.createModelSingleColumnByValues([0.005, 0.006], 'NumberColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0] as NumberColumn;
      table.render();

      column0.decimalFormat = new DecimalFormat(locale, {
        pattern: '#.00'
      });

      column0.calculateMinMaxValues();
      expect(column0.calcMinValue).toBe(0.01);
      expect(column0.calcMaxValue).toBe(0.01);
    });
  });

  describe('format', () => {
    it('updates the value and the display text if the multiplier changes', () => {
      let model = helper.createModelSingleColumnByValues([0.05, 0.06], 'NumberColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0] as NumberColumn;
      table.render();

      column0.setDecimalFormat({
        pattern: '###0.###',
        multiplier: 100
      });

      expect(column0.decimalFormat.multiplier).toBe(100);
      expect(column0.cell(table.rows[0]).text).toBe('5');
      expect(column0.cell(table.rows[0]).value).toBe(0.05);

      column0.setDecimalFormat({
        pattern: '###0.###',
        multiplier: 1
      });

      expect(column0.decimalFormat.multiplier).toBe(1);
      expect(column0.cell(table.rows[0]).text).toBe('0.05');
      expect(column0.cell(table.rows[0]).value).toBe(0.05);
    });
  });

  describe('errorStatus on cell', () => {
    it('gets errorStatus from editor', () => {
      let model = helper.createModelSingleColumnByValues([3], 'NumberColumn');
      let table = helper.createTable(model);
      table.columns[0].setEditable(true);
      let column0 = table.columns[0] as NumberColumn;
      // just values between 2 and 4 are valid
      column0.minValue = 2;
      column0.maxValue = 4;
      table.render();
      expect(column0.cell(table.rows[0]).text).toBe('3');
      expect(column0.cell(table.rows[0]).value).toBe(3);
      expect(table.rows[0].cells[0].errorStatus).toBe(null);

      // set an invalid value
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      table.cellEditorPopup.cell.field.setValue(5);
      table.completeCellEdit();
      expect(table.rows[0].cells[0].errorStatus instanceof Status).toBe(true);
      expect(column0.cell(table.rows[0]).text).toBe('5');

      // set a valid value
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      table.cellEditorPopup.cell.field.setValue(2);
      table.completeCellEdit();
      expect(column0.cell(table.rows[0]).text).toBe('2');
      expect(column0.cell(table.rows[0]).value).toBe(2);
      expect(table.rows[0].cells[0].errorStatus).toBe(null);
    });
  });

  it('isContentValid', () => {
    let table = helper.createTable({
      columns: [{
        objectType: NumberColumn,
        mandatory: true
      }]
    });
    table.insertRow({
      cells: [null]
    });
    let column = table.columns[0];
    let row = table.rows[0];
    expect(column.isContentValid(row).valid).toBe(false);
    column.setCellValue(row, 0);
    expect(column.isContentValid(row).valid).toBe(true);
    column.setCellValue(row, 1);
    expect(column.isContentValid(row).valid).toBe(true);
  });

});
