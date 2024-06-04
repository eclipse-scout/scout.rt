/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BooleanColumn, Cell, Column, DefaultTableAccessibilityRenderer, ListBoxTableAccessibilityRenderer, NumberColumn, scout, Table, Widget} from '../../../src/index';
import {JQueryTesting, SpecTable, TableSpecHelper} from '../../../src/testing/index';

describe('Column', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  /**
   * Test assumes that default values for horiz. alignment are set on cell object.
   */
  it('considers horizontal alignment', () => {
    let model = helper.createModelFixture(3, 2);
    model.columns[1].horizontalAlignment = 0;
    model.columns[2].horizontalAlignment = 1;

    (model.rows[0].cells[1] as Cell).horizontalAlignment = 0;
    (model.rows[0].cells[2] as Cell).horizontalAlignment = 1;
    (model.rows[1].cells[1] as Cell).horizontalAlignment = 0;
    (model.rows[1].cells[2] as Cell).horizontalAlignment = 1;

    let table = helper.createTable(model);
    table.render();

    let $headerItems = table.header.$container.find('.table-header-item');
    let $headerItem0 = $headerItems.eq(0);
    let $headerItem1 = $headerItems.eq(1);
    let $headerItem2 = $headerItems.eq(2);
    let $rows = table.$rows();
    let $cells0 = $rows.eq(0).find('.table-cell');
    let $cells1 = $rows.eq(1).find('.table-cell');

    // Left
    expect($headerItem0).toHaveClass('halign-left');
    expect($headerItem0).not.toHaveClass('halign-center');
    expect($headerItem0).not.toHaveClass('halign-right');
    expect($cells0.eq(0)).toHaveClass('halign-left');
    expect($cells0.eq(0)).not.toHaveClass('halign-center');
    expect($cells0.eq(0)).not.toHaveClass('halign-right');
    expect($cells1.eq(0)).toHaveClass('halign-left');
    expect($cells1.eq(0)).not.toHaveClass('halign-center');
    expect($cells1.eq(0)).not.toHaveClass('halign-right');

    // Center
    expect($headerItem1).not.toHaveClass('halign-left');
    expect($headerItem1).toHaveClass('halign-center');
    expect($headerItem1).not.toHaveClass('halign-right');
    expect($cells0.eq(1)).not.toHaveClass('halign-left');
    expect($cells0.eq(1)).toHaveClass('halign-center');
    expect($cells0.eq(1)).not.toHaveClass('halign-right');
    expect($cells1.eq(1)).not.toHaveClass('halign-left');
    expect($cells1.eq(1)).toHaveClass('halign-center');
    expect($cells1.eq(1)).not.toHaveClass('halign-right');

    // Right
    expect($headerItem2).not.toHaveClass('halign-left');
    expect($headerItem2).not.toHaveClass('halign-center');
    expect($headerItem2).toHaveClass('halign-right');
    expect($cells0.eq(2)).not.toHaveClass('halign-left');
    expect($cells0.eq(2)).not.toHaveClass('halign-center');
    expect($cells0.eq(2)).toHaveClass('halign-right');
    expect($cells1.eq(2)).not.toHaveClass('halign-left');
    expect($cells1.eq(2)).not.toHaveClass('halign-center');
    expect($cells1.eq(2)).toHaveClass('halign-right');
  });

  it('converts linebreak into <br> in header cells', () => {
    let model = helper.createModelFixture(3, 2);
    model.columns[0].text = 'header text';
    model.columns[1].text = 'header text\nNew line';
    let table = helper.createTable(model);
    table.render();

    let $headerItems = table.header.$container.find('.table-header-item');
    let $headerItem0Text = $headerItems.eq(0).children('.table-header-item-text');
    let $headerItem1Text = $headerItems.eq(1).children('.table-header-item-text');

    expect($headerItem0Text.html()).toBe('header text');
    expect($headerItem1Text.html()).toBe('header text<br>New line');
  });

  it('considers custom css class of a column', () => {
    let model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';

    let table = helper.createTable(model);
    table.render();

    let $headerItems = table.header.$container.find('.table-header-item');
    let $headerItem0 = $headerItems.eq(0);
    let $rows = table.$rows();
    let $cells0 = $rows.eq(0).find('.table-cell');
    let $cells1 = $rows.eq(1).find('.table-cell');

    expect($headerItem0).not.toHaveClass('abc');
    expect($cells0.eq(0)).toHaveClass('abc');
    expect($cells0.eq(1)).not.toHaveClass('abc');
    expect($cells1.eq(0)).toHaveClass('abc');
    expect($cells1.eq(1)).not.toHaveClass('abc');
  });

  it('considers custom css class of a column, as well for checkbox columns', () => {
    let model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';
    model.columns[0].objectType = BooleanColumn;

    let table = helper.createTable(model);
    table.render();

    let $headerItems = table.header.$container.find('.table-header-item');
    let $headerItem0 = $headerItems.eq(0);
    let $rows = table.$rows();
    let $cells0 = $rows.eq(0).find('.table-cell');
    let $cells1 = $rows.eq(1).find('.table-cell');

    expect($headerItem0).not.toHaveClass('abc');
    expect($cells0.eq(0)).toHaveClass('abc');
    expect($cells0.eq(1)).not.toHaveClass('abc');
    expect($cells1.eq(0)).toHaveClass('abc');
    expect($cells1.eq(1)).not.toHaveClass('abc');
  });

  it('considers custom css class of a cell, if both are set only the cell class is used', () => {
    let model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';
    (model.rows[0].cells[0] as Cell).cssClass = 'custom-cell-0';

    let table = helper.createTable(model);
    table.render();

    let $headerItems = table.header.$container.find('.table-header-item');
    let $headerItem0 = $headerItems.eq(0);
    let $rows = table.$rows();
    let $cells0 = $rows.eq(0).find('.table-cell');
    let $cells1 = $rows.eq(1).find('.table-cell');

    expect($headerItem0).not.toHaveClass('abc');
    expect($cells0.eq(0)).not.toHaveClass('abc');
    expect($cells0.eq(0)).toHaveClass('custom-cell-0');
    expect($cells0.eq(1)).not.toHaveClass('abc');
    expect($cells1.eq(0)).toHaveClass('abc');
    expect($cells1.eq(0)).not.toHaveClass('custom-cell-0');
    expect($cells1.eq(1)).not.toHaveClass('abc');
  });

  it('considers htmlEnabled of a cell', () => {
    let model = helper.createModelFixture(3, 2);
    (model.rows[0].cells[0] as Cell).text = '<b>hi</b>';
    (model.rows[0].cells[0] as Cell).htmlEnabled = false;
    (model.rows[0].cells[1] as Cell).text = '<b>hi</b>';
    (model.rows[0].cells[1] as Cell).htmlEnabled = true;

    let table = helper.createTable(model);
    table.render();

    let $rows = table.$rows();
    let $cells0 = $rows.eq(0).find('.table-cell');

    expect($cells0.eq(0).text()).toBe('<b>hi</b>');
    expect($cells0.eq(1).text()).toBe('hi');
  });

  it('caches encoded text of a cell to improve performance', () => {
    let model = helper.createModelFixture(3, 1);
    (model.rows[0].cells[0] as Cell).text = '<b>hi</b>';
    (model.rows[0].cells[0] as Cell).htmlEnabled = false;

    let table = helper.createTable(model);
    expect(table.rows[0].cells[0].text).toBe('<b>hi</b>');
    expect(table.rows[0].cells[0]['_cachedEncodedText']).toBeFalsy();

    table.render();

    expect(table.rows[0].cells[0].text).toBe('<b>hi</b>');
    expect(table.rows[0].cells[0].encodedText()).toBe('&lt;b&gt;hi&lt;/b&gt;');
    expect(table.rows[0].$row.find('.table-cell').eq(0).text()).toBe('<b>hi</b>');
    let firstElement = table.rows[0].cells[0]['_cachedEncodedText'];
    expect(firstElement).not.toBeFalsy();

    // re render -> encode must not be called again
    table.remove();
    table.render();
    expect(firstElement).toBe(table.rows[0].cells[0]['_cachedEncodedText']);
  });

  describe('multilineText', () => {
    it('replaces\n with br, but only if htmlEnabled is false', () => {
      let model = helper.createModelFixture(3, 2);
      model.multilineText = true;
      (model.rows[0].cells[0] as Cell).text = '<br>hello\nyou';
      (model.rows[0].cells[0] as Cell).htmlEnabled = false;
      (model.rows[0].cells[1] as Cell).text = '<br>hello\nyou';
      (model.rows[0].cells[1] as Cell).htmlEnabled = true;

      let table = helper.createTable(model);
      table.render();

      let $rows = table.$rows();
      let $cells0 = $rows.eq(0).find('.table-cell');

      expect($cells0.eq(0).html()).toBe('<span class="text">&lt;br&gt;hello<br>you</span>');
      // No replacement, provided html should be left untouched
      expect($cells0.eq(1).html()).toBe('<br>hello\nyou');
    });
  });

  describe('textWrap', () => {
    let table, model, $rows, $cells0, $cell0_0;

    beforeEach(() => {
      model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
    });

    it('wraps text if column.textWrap and table.multilineText are true', () => {
      table.multilineText = true;
      table.columns[0].textWrap = true;
      table.render();
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).not.toHaveClass('white-space-nowrap');
    });

    it('does not wrap text if column.textWrap is false and table.multilineText is true', () => {
      table.multilineText = true;
      table.columns[0].textWrap = false;
      table.render();
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');
    });

    it('does not wrap text if column.textWrap is true and table.multilineText is false', () => {
      table.multilineText = false;
      table.columns[0].textWrap = true;
      table.render();
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');
    });

    it('can be toggled on the fly', () => {
      table.multilineText = true;
      table.columns[0].textWrap = false;
      table.render();
      $cells0 = table.$rows().eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');

      table.columns[0].setTextWrap(true);
      $cells0 = table.$rows().eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).not.toHaveClass('white-space-nowrap');

      table.columns[0].setTextWrap(false);
      $cells0 = table.$rows().eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');
    });
  });

  describe('initCell', () => {
    let table, model;

    beforeEach(() => {
      model = helper.createModelFixture(1, 0);
      table = helper.createTable(model);
    });

    it('sets the value and the text', () => {
      table.insertRows([{
        cells: ['cell 1']
      }]);
      let row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('cell 1');
    });

    it('calls formatValue to format the text', () => {
      table.columns[0]._formatValue = value => value.toUpperCase();
      table.insertRows([{
        cells: ['cell 1']
      }]);
      let row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('CELL 1');
    });

    it('calls formatValue to format the text, also for cell objects', () => {
      table.columns[0]._formatValue = value => value.toUpperCase();
      table.insertRows([{
        cells: [scout.create(Cell, {
          value: 'cell 1'
        })]
      }]);
      let row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('CELL 1');
    });

    it('does not format the value if a text is provided', () => {
      table.columns[0]._formatValue = value => value.toUpperCase();
      table.insertRows([{
        cells: [scout.create(Cell, {
          value: 'cell 1',
          text: 'cell text 1'
        })]
      }]);
      let row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('cell text 1');
    });

    it('sets the value to null if only text is provided', () => {
      table.columns[0]._formatValue = value => value.toUpperCase();
      table.insertRows([{
        cells: [scout.create(Cell, {
          text: 'cell text 1'
        })]
      }]);
      let row = table.rows[0];
      expect(row.cells[0].value).toEqual(null);
      expect(row.cells[0].text).toEqual('cell text 1');
    });

  });

  describe('setCellValue', () => {
    let table, model;

    beforeEach(() => {
      model = helper.createModelFixture(2, 0);
      table = helper.createTable(model);
    });

    it('sets the value and the text', () => {
      table.insertRows([{
        cells: ['cell 1', 'cell 2']
      }]);
      let row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('cell 1');

      table.setCellValue(table.columns[0], row, 'new cell value');
      expect(row.cells[0].value).toEqual('new cell value');
      expect(row.cells[0].text).toEqual('new cell value');
    });

    it('calls formatValue to format the text', () => {
      table.columns[0]._formatValue = value => value.toUpperCase();
      table.insertRows([{
        cells: ['cell 1', 'cell 2']
      }]);
      let row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('CELL 1');

      table.setCellValue(table.columns[0], row, 'new cell value');
      expect(row.cells[0].value).toEqual('new cell value');
      expect(row.cells[0].text).toEqual('NEW CELL VALUE');
    });

  });

  describe('cell getters', () => {

    it('cell() should return cell of given row', () => {
      let model = helper.createModelFixture(3, 2);
      let table = helper.createTable(model);
      let row1 = table.rows[1];
      let column1 = table.columns[1];
      let cell1 = row1.cells[1];
      expect(column1.cell(row1)).toBe(cell1);
    });

    it('selectedCell() should return cell from selected row', () => {
      let model = helper.createModelFixture(3, 2);
      let table = helper.createTable(model);
      let row1 = table.rows[1];
      let column1 = table.columns[1];
      let cell1 = row1.cells[1];
      table.selectRows([row1]);
      expect(column1.selectedCell()).toBe(cell1);
    });

    it('cellValue() and cellText() return the appropriate values', () => {
      let model = helper.createModel([
        helper.createModelColumn('col0', Column),
        helper.createModelColumn('col1', NumberColumn)
      ], [
        helper.createModelRow('row0', [helper.createModelCell('Text0', 'Value0'), helper.createModelCell('Number0', 0)]),
        helper.createModelRow('row1', [helper.createModelCell('Text1'), helper.createModelCell('Number1', 1)])
      ]);
      let table = helper.createTable(model);
      let row0 = table.rows[0];
      let row1 = table.rows[1];
      let column0 = table.columns[0];
      let column1 = table.columns[1];

      expect(column0.cellValue(row0)).toBe('Value0');
      expect(column1.cellValue(row0)).toBe(0);
      expect(column0.cellValue(row1)).toBeNull();
      expect(column1.cellValue(row1)).toBe(1);

      expect(column0.cellText(row0)).toBe('Text0');
      expect(column1.cellText(row0)).toBe('Number0');
      expect(column0.cellText(row1)).toBe('Text1');
      expect(column1.cellText(row1)).toBe('Number1');

      expect(column0.cellValueOrText(row0)).toBe('Text0');
      expect(column1.cellValueOrText(row0)).toBe(0);
      expect(column0.cellValueOrText(row1)).toBe('Text1');
      expect(column1.cellValueOrText(row1)).toBe(1);
    });

  });

  describe('autoOptimizeWidth', () => {

    it('will resize the column to fit its content after layouting', () => {
      let model = helper.createModelFixture(3, 2);
      model.columns[1].autoOptimizeWidth = true;
      let table = helper.createTable(model);
      let resizeToFitSpy = spyOn(table, 'resizeToFit').and.callThrough();
      table.render();
      expect(table.columns[0].autoOptimizeWidth).toBe(false);
      expect(table.columns[1].autoOptimizeWidth).toBe(true);
      expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
      expect(table.resizeToFit).not.toHaveBeenCalled();

      table.validateLayout();
      expect(table.columns[1].autoOptimizeWidth).toBe(true);
      expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
      expect(resizeToFitSpy.calls.count()).toBe(1);
    });

    it('also works if there is no header', () => {
      let model = helper.createModelFixture(3, 2);
      model.columns[1].autoOptimizeWidth = true;
      model.headerVisible = false;
      let table = helper.createTable(model);
      let resizeToFitSpy = spyOn(table, 'resizeToFit').and.callThrough();
      table.render();
      expect(table.resizeToFit).not.toHaveBeenCalled();

      table.validateLayout();
      expect(table.columns[1].autoOptimizeWidth).toBe(true);
      expect(resizeToFitSpy.calls.count()).toBe(1);
    });

    it('considers images', () => {
      let model = helper.createModelFixture(3, 2);
      model.columns[1].autoOptimizeWidth = true;
      (model.rows[0].cells[1] as Cell).iconId = 'fancyIcon.png';
      let table = helper.createTable(model);
      let resizeToFit = spyOn(table, 'resizeToFit').and.callThrough();
      let _resizeToFit = spyOn(table, '_resizeToFit').and.callThrough();
      table.render();
      expect(table.resizeToFit).not.toHaveBeenCalled();
      expect(table._resizeToFit).not.toHaveBeenCalled();

      table.validateLayout();
      expect(resizeToFit.calls.count()).toBe(1);
      // _resizeToFit must not be called yet because fancyImage.png is not loaded yet
      expect(_resizeToFit.calls.count()).toBe(0);

      // Simulate image load event
      JQueryTesting.triggerImageLoadCapture(table.columns[1].optimalWidthMeasurer.$measurement.find('img'));
      // Image has been loaded and the promise is resolved -> _resizeToFit will be called
      expect(resizeToFit.calls.count()).toBe(1);
      expect(_resizeToFit.calls.count()).toBe(1);
    });

    describe('autoOptimizeWidthRequired', () => {
      it('will be set to true if a row is updated and the content changed', () => {
        let model = helper.createModelFixture(3, 2);
        model.columns[0].autoOptimizeWidth = true;
        model.columns[1].autoOptimizeWidth = true;
        let table = helper.createTable(model);
        table.render();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        let resizeToFitSpy = spyOn(table, 'resizeToFit').and.callThrough();

        // Update row but don't change relevant content -> no need to update the width
        table.updateRow({
          id: table.rows[1].id,
          cells: ['cell1_0', 'cell1_1', 'abc']
        });
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        table.validateLayout();
        expect(table.resizeToFit).not.toHaveBeenCalled();

        // Update content of second column -> resize that column but not the other
        table.updateRow({
          id: table.rows[1].id,
          cells: ['cell1_0', 'new content', 'abc']
        });
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
        table.validateLayout();
        expect(resizeToFitSpy.calls.count()).toBe(1);
      });

      it('will be set to true if a row is inserted', () => {
        let model = helper.createModelFixture(3, 2);
        model.columns[0].autoOptimizeWidth = true;
        model.columns[1].autoOptimizeWidth = true;
        let table = helper.createTable(model);
        table.render();
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        let resizeToFitSpy = spyOn(table, 'resizeToFit').and.callThrough();

        table.insertRow({cells: ['a', 'b', 'c']});
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
        table.validateLayout();
        expect(resizeToFitSpy.calls.count()).toBe(2);
      });

      it('will be set to true if a row is deleted', () => {
        let model = helper.createModelFixture(3, 2);
        model.columns[0].autoOptimizeWidth = true;
        model.columns[1].autoOptimizeWidth = true;
        let table = helper.createTable(model);
        table.render();
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        let resizeToFitSpy = spyOn(table, 'resizeToFit').and.callThrough();

        table.deleteRow(table.rows[1]);
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
        table.validateLayout();
        expect(resizeToFitSpy.calls.count()).toBe(2);
      });

      it('will be set to true if all rows are deleted', () => {
        let model = helper.createModelFixture(3, 2);
        model.columns[0].autoOptimizeWidth = true;
        model.columns[1].autoOptimizeWidth = true;
        let table = helper.createTable(model);
        table.render();
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        let resizeToFitSpy = spyOn(table, 'resizeToFit').and.callThrough();

        table.deleteAllRows();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
        table.validateLayout();
        expect(resizeToFitSpy.calls.count()).toBe(2);
      });

      it('will be set to true if autoOptimizeWidth is set dynamically', () => {
        let model = helper.createModelFixture(3, 2);
        let table = helper.createTable(model);
        table.render();
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        let resizeToFitSpy = spyOn(table, 'resizeToFit').and.callThrough();

        table.columns[0].setAutoOptimizeWidth(true);
        expect(table.columns[0].autoOptimizeWidth).toBe(true);
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        table.validateLayout();
        expect(resizeToFitSpy.calls.count()).toBe(1);

        table.columns[0].setAutoOptimizeWidth(false);
        expect(table.columns[0].autoOptimizeWidth).toBe(false);
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        table.validateLayout();
        expect(resizeToFitSpy.calls.count()).toBe(1);
      });
    });
  });

  describe('visible', () => {

    it('is multi dimensional', () => {
      let table = scout.create(Table, {
        parent: session.desktop,
        columns: [{
          objectType: Column
        }]
      });
      let column = table.columns[0];
      expect(column.getProperty('visible-default')).toBe(true);
      expect(column.getProperty('visible-granted')).toBe(true);
      expect(column.visibleGranted).toBe(true);
      expect(column.visible).toBe(true);

      column.setVisible({
        granted: false,
        default: true
      });
      expect(column.getPropertyDimension('visible', 'default')).toBe(true);
      expect(column.getProperty('visible-granted')).toBe(false);
      expect(column.visibleGranted).toBe(false);
      expect(column.visible).toBe(false);

      column.setVisible(true);
      column.setVisibleGranted(false);
      expect(column.getProperty('visible-default')).toBe(true);
      expect(column.getProperty('visible-granted')).toBe(false);
      expect(column.visibleGranted).toBe(false);
      expect(column.visible).toBe(false);

      column.setVisible({});
      expect(column.getProperty('visible-default')).toBe(true);
      expect(column.getProperty('visible-granted')).toBe(true);
      expect(column.visibleGranted).toBe(true);
      expect(column.visible).toBe(true);
    });
  });

  describe('displayable', () => {

    it('if set to false, column may not be made visible', () => {
      let model = helper.createModelFixture(3, 2);
      let table = helper.createTable(model);
      expect(table.columns[0].getProperty('visible-default')).toBe(true);
      expect(table.columns[0].displayable).toBe(true);
      expect(table.columns[0].visible).toBe(true);
      expect(table.visibleColumns().length).toBe(3);

      table.columns[0].setDisplayable(false);
      expect(table.columns[0].getProperty('visible-default')).toBe(true);
      expect(table.columns[0].displayable).toBe(false);
      expect(table.columns[0].visible).toBe(false);
      expect(table.visibleColumns().length).toBe(2);

      table.columns[0].setDisplayable(true);
      expect(table.columns[0].getProperty('visible-default')).toBe(true);
      expect(table.columns[0].displayable).toBe(true);
      expect(table.columns[0].visible).toBe(true);
      expect(table.visibleColumns().length).toBe(3);
    });

    it('can be set on initial model', () => {
      let table = scout.create(Table, {
        parent: session.desktop,
        columns: [{
          objectType: Column,
          displayable: false
        }]
      });
      expect(table.columns[0].getProperty('visible-default')).toBe(true);
      expect(table.columns[0].getProperty('visible-displayable')).toBe(false);
      expect(table.columns[0].displayable).toBe(false);
      expect(table.columns[0].visible).toBe(false);

      // Alternative way
      table = scout.create(Table, {
        parent: session.desktop,
        columns: [{
          objectType: Column,
          visible: {
            displayable: false
          }
        }]
      });
      expect(table.columns[0].getProperty('visible-default')).toBe(true);
      expect(table.columns[0].getProperty('visible-displayable')).toBe(false);
      expect(table.columns[0].displayable).toBe(false);
      expect(table.columns[0].visible).toBe(false);
    });

    it('overrides displayable dimension on init model ', () => {
      let table = scout.create(Table, {
        parent: session.desktop,
        columns: [{
          objectType: Column,
          visible: {
            displayable: true
          },
          displayable: false
        }]
      });
      expect(table.columns[0].getProperty('visible-default')).toBe(true);
      expect(table.columns[0].getProperty('visible-displayable')).toBe(false);
      expect(table.columns[0].displayable).toBe(false);
      expect(table.columns[0].visible).toBe(false);
    });
  });

  it('isContentValid', () => {
    let table = helper.createTable({
      columns: [{
        objectType: Column,
        mandatory: true
      }]
    });
    table.insertRow({
      cells: [null]
    });
    let column = table.columns[0];
    let row = table.rows[0];
    expect(column.isContentValid(row).valid).toBe(false);
    column.setCellValue(row, 'foo');
    expect(column.isContentValid(row).valid).toBe(true);
  });

  describe('setWidth', () => {
    it('does not fail if table is not rendered', () => {
      let table = helper.createTable({
        columns: [{
          objectType: Column,
          width: 30
        }],
        rows: [{
          cells: ['hi']
        }]
      });
      table.columns[0].setWidth(50);
      expect(table.columns[0].width).toBe(50);

      table.render();
      let $cells = table.rows[0].$row.children('.table-cell');
      expect($cells.eq(0).cssWidth()).toBe(50);
    });
  });

  describe('aria properties', () => {

    it('has cells with aria role gridcell if DefaultTableAccessibilityRenderer is used', () => {
      let model = helper.createModelSingleColumnByValues(['cell1'], 'Column');
      let table = helper.createTable(model);
      table.accessibilityRenderer = new DefaultTableAccessibilityRenderer();
      table.render();
      expect(table.$cell(table.columns[0], table.rows[0].$row)).toHaveAttr('role', 'gridcell');
    });

    it('has cells with no aria role if ListBoxTableAccessibilityRenderer is used', () => {
      let model = helper.createModelSingleColumnByValues(['cell1'], 'Column');
      let table = helper.createTable(model);
      table.accessibilityRenderer = new ListBoxTableAccessibilityRenderer();
      table.render();
      expect(table.$cell(table.columns[0], table.rows[0].$row)).not.toHaveAttr('role');
    });
  });

  describe('uuid', () => {

    it('uuidPath for remote column includes parent', () => {
      const remoteTable = getRemoteTable();
      const remoteCol = remoteTable.columns[0];

      // remote case (Scout Classic): classId is sent from backend
      expect(remoteCol.classId).toBe('column-class-id'); // must be the own id only (without table or its parents)
      expect(remoteTable.classId).toBe('table-class-id_parent-widget-class-id'); // table contains the ids of its parents
      expect(remoteCol.uuidPath()).toBe('column-class-id|table-class-id_parent-widget-class-id'); // uuidPath of the column should include the parent table
      expect(remoteTable.uuidPath()).toBe('table-class-id_parent-widget-class-id'); // uuidPath of the table should include its parent
    });

    function getRemoteTable(): SpecTable {
      const remoteParent = scout.create(Widget, {
        parent: session.desktop,
        classId: 'parent-widget-class-id' // root widget classId
      });
      return helper.createTable({
        parent: remoteParent,
        classId: 'table-class-id_parent-widget-class-id', // classId of the table includes its parents (see AbstractTable.classId)
        columns: [{
          objectType: Column,
          classId: 'column-class-id' // classId without parent Table is sent from backend (see InspectorObjectIdProvider.getIdForColumn)
        }]
      });
    }

    it('uuidPath for local column includes parent', () => {
      // Scout JS: uuid is part of the model
      const localTable = getLocalTable();
      const localCol = localTable.columns[0];
      expect(localCol.uuid).toBe('column-uuid'); // must be the own uuid only (without table or its parents)
      expect(localTable.uuid).toBe('table-uuid'); // must be the own uuid only (without table or its parents)
      expect(localCol.uuidPath()).toBe('column-uuid|table-uuid|parent-widget-uuid'); // uuidPath of the column should include the parents
      expect(localTable.uuidPath()).toBe('table-uuid|parent-widget-uuid'); // uuidPath of the table should include its parent
    });

    it('BookmarkAdapter.buildId returns id without parent for local and remote case', () => {
      const localTable = getLocalTable();
      const localCol = localTable.columns[0];
      const remoteTable = getRemoteTable();
      const remoteCol = remoteTable.columns[0];

      expect(remoteCol.getBookmarkAdapter().buildId()).toBe('column-class-id'); // must be the column classId only without its parents
      expect(remoteTable.getBookmarkAdapter().buildId()).toBe('table-class-id_parent-widget-class-id'); // must be with parent classIds
      expect(localCol.getBookmarkAdapter().buildId()).toBe('column-uuid'); // must be the column uuid only without its parents
      expect(localTable.getBookmarkAdapter().buildId()).toBe('table-uuid|parent-widget-uuid'); // must be with parent classIds
    });

    function getLocalTable(): SpecTable {
      const localParent = scout.create(Widget, {
        parent: session.desktop,
        uuid: 'parent-widget-uuid' // root widget uuid
      });
      return helper.createTable({
        parent: localParent,
        uuid: 'table-uuid', // uuid of the table
        columns: [{
          objectType: Column,
          uuid: 'column-uuid' // uuid of the column
        }]
      });
    }
  });
});
