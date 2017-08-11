/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('Column', function() {
  var session;
  var helper;
  var locale;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  /**
   * Test assumes that default values for horiz. alignment are set on cell object.
   */
  it('considers horizontal alignment', function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[1].horizontalAlignment = 0;
    model.columns[2].horizontalAlignment = 1;

    model.rows[0].cells[1].horizontalAlignment = 0;
    model.rows[0].cells[2].horizontalAlignment = 1;

    model.rows[1].cells[1].horizontalAlignment = 0;
    model.rows[1].cells[2].horizontalAlignment = 1;

    var table = helper.createTable(model);
    table.render();

    var $headerItems = table.header.$container.find('.table-header-item');
    var $headerItem0 = $headerItems.eq(0);
    var $headerItem1 = $headerItems.eq(1);
    var $headerItem2 = $headerItems.eq(2);
    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');
    var $cells1 = $rows.eq(1).find('.table-cell');

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

  it('converts linebreak into <br> in header cells', function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].text = 'header text';
    model.columns[1].text = 'header text\nNew line';
    var table = helper.createTable(model);
    table.render();

    var $headerItems = table.header.$container.find('.table-header-item');
    var $headerItem0Text = $headerItems.eq(0).children('.table-header-item-text');
    var $headerItem1Text = $headerItems.eq(1).children('.table-header-item-text');

    expect($headerItem0Text.html()).toBe('header text');
    expect($headerItem1Text.html()).toBe('header text<br>New line');
  });

  it('considers custom css class of a column', function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';

    var table = helper.createTable(model);
    table.render();

    var $headerItems = table.header.$container.find('.table-header-item');
    var $headerItem0 = $headerItems.eq(0);
    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');
    var $cells1 = $rows.eq(1).find('.table-cell');

    expect($headerItem0).not.toHaveClass('abc');
    expect($cells0.eq(0)).toHaveClass('abc');
    expect($cells0.eq(1)).not.toHaveClass('abc');
    expect($cells1.eq(0)).toHaveClass('abc');
    expect($cells1.eq(1)).not.toHaveClass('abc');
  });

  it('considers custom css class of a column, as well for checkbox columns', function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';
    model.columns[0].objectType = 'BooleanColumn';

    var table = helper.createTable(model);
    table.render();

    var $headerItems = table.header.$container.find('.table-header-item');
    var $headerItem0 = $headerItems.eq(0);
    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');
    var $cells1 = $rows.eq(1).find('.table-cell');

    expect($headerItem0).not.toHaveClass('abc');
    expect($cells0.eq(0)).toHaveClass('abc');
    expect($cells0.eq(1)).not.toHaveClass('abc');
    expect($cells1.eq(0)).toHaveClass('abc');
    expect($cells1.eq(1)).not.toHaveClass('abc');
  });

  it('considers custom css class of a cell, if both are set only the cell class is used', function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';
    model.rows[0].cells[0].cssClass = 'custom-cell-0';

    var table = helper.createTable(model);
    table.render();

    var $headerItems = table.header.$container.find('.table-header-item');
    var $headerItem0 = $headerItems.eq(0);
    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');
    var $cells1 = $rows.eq(1).find('.table-cell');

    expect($headerItem0).not.toHaveClass('abc');
    expect($cells0.eq(0)).not.toHaveClass('abc');
    expect($cells0.eq(0)).toHaveClass('custom-cell-0');
    expect($cells0.eq(1)).not.toHaveClass('abc');
    expect($cells1.eq(0)).toHaveClass('abc');
    expect($cells1.eq(0)).not.toHaveClass('custom-cell-0');
    expect($cells1.eq(1)).not.toHaveClass('abc');
  });

  it('considers htmlEnabled of a cell', function() {
    var model = helper.createModelFixture(3, 2);
    model.rows[0].cells[0].text = '<b>hi</b>';
    model.rows[0].cells[0].htmlEnabled = false;
    model.rows[0].cells[1].text = '<b>hi</b>';
    model.rows[0].cells[1].htmlEnabled = true;

    var table = helper.createTable(model);
    table.render();

    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');

    expect($cells0.eq(0).text()).toBe('<b>hi</b>');
    expect($cells0.eq(1).text()).toBe('hi');
  });

  it('caches encoded text of a cell to improve performance', function() {
    var model = helper.createModelFixture(3, 1);
    model.rows[0].cells[0].text = '<b>hi</b>';
    model.rows[0].cells[0].htmlEnabled = false;

    var table = helper.createTable(model);
    expect(table.rows[0].cells[0].text).toBe('<b>hi</b>');
    expect(table.rows[0].cells[0]._cachedEncodedText).toBeFalsy();

    spyOn(scout.strings, 'encode').and.callThrough();
    table.render();

    expect(scout.strings.encode.calls.count()).toBe(6); // header and table cells
    expect(table.rows[0].cells[0].text).toBe('<b>hi</b>');
    expect(table.rows[0].cells[0].encodedText()).toBe('&lt;b&gt;hi&lt;/b&gt;');
    expect(table.rows[0].$row.find('.table-cell').eq(0).text()).toBe('<b>hi</b>');

    // re render -> encode must not be called again
    table.remove();
    scout.strings.encode.calls.reset();
    table.render();
    expect(scout.strings.encode.calls.count()).toBe(3); // only for header cells
  });

  describe('multilineText', function() {
    it('replaces\n with br, but only if htmlEnabled is false', function() {
      var model = helper.createModelFixture(3, 2);
      model.multilineText = true;
      model.rows[0].cells[0].text = '<br>hello\nyou';
      model.rows[0].cells[0].htmlEnabled = false;
      model.rows[0].cells[1].text = '<br>hello\nyou';
      model.rows[0].cells[1].htmlEnabled = true;

      var table = helper.createTable(model);
      table.render();

      var $rows = table.$rows();
      var $cells0 = $rows.eq(0).find('.table-cell');

      expect($cells0.eq(0).html()).toBe('&lt;br&gt;hello<br>you');
      // No replacement, provided html should be left untouched
      expect($cells0.eq(1).html()).toBe('<br>hello\nyou');
    });
  });

  describe('textWrap', function() {
    var table, model, $rows, $cells0, $cell0_0, $cell0_1;

    beforeEach(function() {
      model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
    });

    it('wraps text if column.textWrap and table.multilineText are true', function() {
      table.multilineText = true;
      table.columns[0].textWrap = true;
      table.render();
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).not.toHaveClass('white-space-nowrap');
    });

    it('does not wrap text if column.textWrap is false and table.multilineText is true', function() {
      table.multilineText = true;
      table.columns[0].textWrap = false;
      table.render();
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');
    });

    it('does not wrap text if column.textWrap is true and table.multilineText is false', function() {
      table.multilineText = false;
      table.columns[0].textWrap = true;
      table.render();
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');
    });
  });

  describe('initCell', function() {
    var table, model;

    beforeEach(function() {
      model = helper.createModelFixture(1, 0);
      table = helper.createTable(model);
    });

    it('sets the value and the text', function() {
      table.insertRows([{
        cells: ['cell 1']
      }]);
      var row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('cell 1');
    });

    it('calls formatValue to format the text', function() {
      table.columns[0]._formatValue = function(value) {
        return value.toUpperCase();
      };
      table.insertRows([{
        cells: ['cell 1']
      }]);
      var row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('CELL 1');
    });

    it('calls formatValue to format the text, also for cell objects', function() {
      table.columns[0]._formatValue = function(value) {
        return value.toUpperCase();
      };
      table.insertRows([{
        cells: [scout.create('Cell', {
          value: 'cell 1'
        })]
      }]);
      var row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('CELL 1');
    });

    it('does not format the value if a text is provided', function() {
      table.columns[0]._formatValue = function(value) {
        return value.toUpperCase();
      };
      table.insertRows([{
        cells: [scout.create('Cell', {
          value: 'cell 1',
          text: 'cell text 1'
        })]
      }]);
      var row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('cell text 1');
    });

    it('sets the value to null if only text is provided', function() {
      table.columns[0]._formatValue = function(value) {
        return value.toUpperCase();
      };
      table.insertRows([{
        cells: [scout.create('Cell', {
          text: 'cell text 1'
        })]
      }]);
      var row = table.rows[0];
      expect(row.cells[0].value).toEqual(null);
      expect(row.cells[0].text).toEqual('cell text 1');
    });

  });

  describe('setCellValue', function() {
    var table, model;

    beforeEach(function() {
      model = helper.createModelFixture(2, 0);
      table = helper.createTable(model);
    });

    it('sets the value and the text', function() {
      table.insertRows([{
        cells: ['cell 1', 'cell 2']
      }]);
      var row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('cell 1');

      table.setCellValue(table.columns[0], row, 'new cell value');
      expect(row.cells[0].value).toEqual('new cell value');
      expect(row.cells[0].text).toEqual('new cell value');
    });

    it('calls formatValue to format the text', function() {
      table.columns[0]._formatValue = function(value) {
        return value.toUpperCase();
      };
      table.insertRows([{
        cells: ['cell 1', 'cell 2']
      }]);
      var row = table.rows[0];
      expect(row.cells[0].value).toEqual('cell 1');
      expect(row.cells[0].text).toEqual('CELL 1');

      table.setCellValue(table.columns[0], row, 'new cell value');
      expect(row.cells[0].value).toEqual('new cell value');
      expect(row.cells[0].text).toEqual('NEW CELL VALUE');
    });

  });

  describe('cell getters', function() {

    it('cell() should return cell of given row', function() {
      var model = helper.createModelFixture(3, 2);
      var table = helper.createTable(model);
      var row1 = table.rows[1];
      var column1 = table.columns[1];
      var cell1 = row1.cells[1];
      expect(column1.cell(row1)).toBe(cell1);
    });

    it('selectedCell() should return cell from selected row', function() {
      var model = helper.createModelFixture(3, 2);
      var table = helper.createTable(model);
      var row1 = table.rows[1];
      var column1 = table.columns[1];
      var cell1 = row1.cells[1];
      table.selectRows([row1]);
      expect(column1.selectedCell()).toBe(cell1);
    });

  });

  describe('autoOptimizeWidth', function() {

    it('will resize the column to fit its content after layouting', function() {
      var model = helper.createModelFixture(3, 2);
      model.columns[1].autoOptimizeWidth = true;
      var table = helper.createTable(model);
      spyOn(table, 'resizeToFit').and.callThrough();
      table.render();
      expect(table.columns[0].autoOptimizeWidth).toBe(false);
      expect(table.columns[1].autoOptimizeWidth).toBe(true);
      expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
      expect(table.resizeToFit).not.toHaveBeenCalled();

      table.validateLayout();
      expect(table.columns[1].autoOptimizeWidth).toBe(true);
      expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
      expect(table.resizeToFit.calls.count()).toBe(1);
    });

    describe('autoOptimizeWidthRequired', function() {
      it('will be set to true if a row is updated and the content changed', function() {
        var model = helper.createModelFixture(3, 2);
        model.columns[0].autoOptimizeWidth = true;
        model.columns[1].autoOptimizeWidth = true;
        var table = helper.createTable(model);
        table.render();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        spyOn(table, 'resizeToFit').and.callThrough();

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
        expect(table.resizeToFit.calls.count()).toBe(1);
      });

      it('will be set to true if a row is inserted', function() {
        var model = helper.createModelFixture(3, 2);
        model.columns[0].autoOptimizeWidth = true;
        model.columns[1].autoOptimizeWidth = true;
        var table = helper.createTable(model);
        table.render();
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        spyOn(table, 'resizeToFit').and.callThrough();

        table.insertRow(['a', 'b', 'c']);
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
        table.validateLayout();
        expect(table.resizeToFit.calls.count()).toBe(2);
      });

      it('will be set to true if a row is deleted', function() {
        var model = helper.createModelFixture(3, 2);
        model.columns[0].autoOptimizeWidth = true;
        model.columns[1].autoOptimizeWidth = true;
        var table = helper.createTable(model);
        table.render();
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        spyOn(table, 'resizeToFit').and.callThrough();

        table.deleteRow(table.rows[1]);
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
        table.validateLayout();
        expect(table.resizeToFit.calls.count()).toBe(2);
      });

      it('will be set to true if all rows are deleted', function() {
        var model = helper.createModelFixture(3, 2);
        model.columns[0].autoOptimizeWidth = true;
        model.columns[1].autoOptimizeWidth = true;
        var table = helper.createTable(model);
        table.render();
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        spyOn(table, 'resizeToFit').and.callThrough();

        table.deleteAllRows();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(true);
        table.validateLayout();
        expect(table.resizeToFit.calls.count()).toBe(2);
      });

      it('will be set to true if autoOptimizeWidth is set dynamically', function() {
        var model = helper.createModelFixture(3, 2);
        var table = helper.createTable(model);
        table.render();
        table.validateLayout();
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        spyOn(table, 'resizeToFit').and.callThrough();

        table.columns[0].setAutoOptimizeWidth(true);
        expect(table.columns[0].autoOptimizeWidth).toBe(true);
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(true);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        table.validateLayout();
        expect(table.resizeToFit.calls.count()).toBe(1);

        table.columns[0].setAutoOptimizeWidth(false);
        expect(table.columns[0].autoOptimizeWidth).toBe(false);
        expect(table.columns[0].autoOptimizeWidthRequired).toBe(false);
        expect(table.columns[1].autoOptimizeWidthRequired).toBe(false);
        table.validateLayout();
        expect(table.resizeToFit.calls.count()).toBe(1);
      });
    });

  });

  describe('displayable', function() {

    it('if set to false, column may not be made visible', function() {
      var model = helper.createModelFixture(3, 2);
      var table = helper.createTable(model);
      expect(table.columns[0].visible).toBe(true);
      expect(table.columns[0].displayable).toBe(true);
      expect(table.columns[0].isVisible()).toBe(true);
      expect(table.visibleColumns().length).toBe(3);

      table.columns[0].setDisplayable(false);
      expect(table.columns[0].visible).toBe(true);
      expect(table.columns[0].displayable).toBe(false);
      expect(table.columns[0].isVisible()).toBe(false);
      expect(table.visibleColumns().length).toBe(2);

      table.columns[0].setDisplayable(true);
      expect(table.columns[0].visible).toBe(true);
      expect(table.columns[0].displayable).toBe(true);
      expect(table.columns[0].isVisible()).toBe(true);
      expect(table.visibleColumns().length).toBe(3);
    });
  });
});
