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
/* global TableSpecHelper */
describe("Column", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
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
  it("considers horizontal alignment", function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[1].horizontalAlignment = 0;
    model.columns[2].horizontalAlignment = 1;

    model.rows[0].cells[1].horizontalAlignment = 0;
    model.rows[0].cells[2].horizontalAlignment = 1;

    model.rows[1].cells[1].horizontalAlignment = 0;
    model.rows[1].cells[2].horizontalAlignment = 1;

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

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

  it("considers custom css class of a column", function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

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

  it("considers custom css class of a column, as well for checkbox columns", function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';
    model.columns[0].objectType = 'BooleanColumn';

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

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

  it("considers custom css class of a cell, if both are set only the cell class is used", function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';
    model.rows[0].cells[0].cssClass = 'custom-cell-0';

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

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

  it("considers htmlEnabled of a cell", function() {
    var model = helper.createModelFixture(3, 2);
    model.rows[0].cells[0].text = '<b>hi</b>';
    model.rows[0].cells[0].htmlEnabled = false;
    model.rows[0].cells[1].text = '<b>hi</b>';
    model.rows[0].cells[1].htmlEnabled = true;

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');

    expect($cells0.eq(0).text()).toBe('<b>hi</b>');
    expect($cells0.eq(1).text()).toBe('hi');
  });

  it("caches encoded text of a cell to improve performance", function() {
    var model = helper.createModelFixture(3, 1);
    model.rows[0].cells[0].text = '<b>hi</b>';
    model.rows[0].cells[0].htmlEnabled = false;

    var table = helper.createTable(model);
    expect(table.rows[0].cells[0].text).toBe('<b>hi</b>');
    expect(table.rows[0].cells[0].encodedText).toBeFalsy();

    spyOn(scout.strings, "encode").and.callThrough();
    table.render(session.$entryPoint);

    expect(scout.strings.encode.calls.count()).toBe(6); // header and table cells
    expect(table.rows[0].cells[0].text).toBe('<b>hi</b>');
    expect(table.rows[0].cells[0].encodedText).toBe('&lt;b&gt;hi&lt;/b&gt;');
    expect(table.rows[0].$row.find('.table-cell').eq(0).text()).toBe('<b>hi</b>');

    // re render -> encode must not be called again
    table.remove();
    scout.strings.encode.calls.reset();
    table.render(session.$entryPoint);
    expect(scout.strings.encode.calls.count()).toBe(3); // only for header cells
  });

  describe("multilineText", function() {
    it("replaces\n with br, but only if htmlEnabled is false", function() {
      var model = helper.createModelFixture(3, 2);
      model.multilineText = true;
      model.rows[0].cells[0].text = '<br>hello\nyou';
      model.rows[0].cells[0].htmlEnabled = false;
      model.rows[0].cells[1].text = '<br>hello\nyou';
      model.rows[0].cells[1].htmlEnabled = true;

      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $rows = table.$rows();
      var $cells0 = $rows.eq(0).find('.table-cell');

      expect($cells0.eq(0).html()).toBe('&lt;br&gt;hello<br>you');
      // No replacement, provided html should be left untouched
      expect($cells0.eq(1).html()).toBe('<br>hello\nyou');
    });
  });

  describe("textWrap", function() {
    var table, model, $rows, $cells0, $cell0_0, $cell0_1;

    beforeEach(function() {
      model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
    });

    it("wraps text if column.textWrap and table.multilineText are true", function() {
      table.multilineText = true;
      table.columns[0].textWrap = true;
      table.render(session.$entryPoint);
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).not.toHaveClass('white-space-nowrap');
    });

    it("does not wrap text if column.textWrap is false and table.multilineText is true", function() {
      table.multilineText = true;
      table.columns[0].textWrap = false;
      table.render(session.$entryPoint);
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');
    });

    it("does not wrap text if column.textWrap is true and table.multilineText is false", function() {
      table.multilineText = false;
      table.columns[0].textWrap = true;
      table.render(session.$entryPoint);
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');
    });
  });

  describe("background effect", function() {
    var rgbLevel0 = 'rgb(255, 175, 175)';
    var rgbLevel50 = 'rgb(213, 195, 161)';
    var rgbLevel100 = 'rgb(171, 214, 147)';
    var barChartColor = 'rgb(128, 193, 208)';
    var imageLevel50 = 'linear-gradient(to left, ' + barChartColor + ' 0%, ' + barChartColor + ' 50%, transparent 50%, transparent 100%)';
    var defaultBackgroundColor;

    scout.styles.get = function style(cssClass) {
      if (cssClass === 'column-background-effect-gradient1-start') {
        return {
          backgroundColor: rgbLevel0
        };
      } else if (cssClass === 'column-background-effect-gradient1-end') {
        return {
          backgroundColor: rgbLevel100
        };
      } else if (cssClass === 'column-background-effect-gradient2-start') {
        return {
          backgroundColor: rgbLevel100
        };
      } else if (cssClass === 'column-background-effect-gradient2-end') {
        return {
          backgroundColor: rgbLevel0
        };
      } else if (cssClass === 'column-background-effect-bar-chart') {
        return {
          backgroundColor: barChartColor
        };
      }
    };

    beforeEach(function() {
      var $div = $('<div>').appendTo(session.$entryPoint);
      defaultBackgroundColor = $div.css('background-color');
    });

    describe("colorGradient1", function() {
      it("colors cells from red to green", function() {
        var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        var table = helper.createTable(model);
        var column0 = table.columns[0];
        table.render(session.$entryPoint);

        table.setColumnBackgroundEffect(column0, 'colorGradient1');
        expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
        expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel100);
      });
    });

    it("considers view range -> only colors rendered cells", function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.viewRangeSize = 2;
      table.render(session.$entryPoint);

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
      expect(table.rows[2].$row).toBeFalsy();

      var spy = spyOn(table, '_calculateCurrentViewRange').and.returnValue(new scout.Range(1, 3));
      table._renderViewport();

      expect(table.rows[0].$row).toBeFalsy();
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel100);
    });

    it("updates colors if row gets deleted", function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.render(session.$entryPoint);

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      table._deleteRow(table.rows[2]);
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel100);
    });

    it("updates colors if row gets inserted", function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.render(session.$entryPoint);

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      var row = helper.createModelRowByValues(undefined, 200);
      table._insertRow(row);
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(234, 185, 168)');
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe('rgb(213, 195, 161)');
      expect(table.$cell(column0, table.rows[3].$row).css('background-color')).toBe(rgbLevel100);
    });

    it("updates colors if row gets updated", function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      model.columns[0].backgroundEffect = 'colorGradient1';
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.render(session.$entryPoint);

      // Change row 0 value to 150, row 1 now has the lowest values
      var rows = helper.createModelRows(1, 1);
      rows[0].id = table.rows[0].id;
      rows[0].cells[0].value = 150;
      table._updateRows(rows);

      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel100);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel50);
    });

    it("colors cells if table gets rendered", function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      model.columns[0].backgroundEffect = 'colorGradient1';
      var table = helper.createTable(model);
      var column0 = table.columns[0];

      table.render(session.$entryPoint);
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel100);
    });

    it("restores existing background color if background effect gets removed", function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      model.rows[1].cells[0].backgroundColor = 'ff0000';
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.render(session.$entryPoint);

      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(255, 0, 0)');
      expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe('none');

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);

      table.setColumnBackgroundEffect(column0, null);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(255, 0, 0)');
      expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe('none');
    });

    describe("barChart", function() {
      it("does not overwrite existing background color", function() {
        if (!scout.device.supportsCssGradient()) {
          // PhantomJs does not support gradients
          return;
        }
        var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        model.rows[1].cells[0].backgroundColor = 'ff0000';
        var table = helper.createTable(model);
        var column0 = table.columns[0];
        table.render(session.$entryPoint);

        table.setColumnBackgroundEffect(column0, 'barChart');
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(255, 0, 0)');
        expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe(imageLevel50);
      });
    });

    describe("setBackgroundEffect", function() {
      it("changes the background effect", function() {
        var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        var table = helper.createTable(model);
        var column0 = table.columns[0];
        table.render(session.$entryPoint);

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

        if (scout.device.supportsCssGradient()) {
          table.setColumnBackgroundEffect(column0, 'barChart');
          expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(defaultBackgroundColor);
          expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe(imageLevel50);
        }

        // set to null: no effect
        table.setColumnBackgroundEffect(column0, null);
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(defaultBackgroundColor);
        expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe('none');
      });

      it("sends columnBackgroundEffectChanged event", function() {
        var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        var table = helper.createTable(model);
        var column0 = table.columns[0];
        table.render(session.$entryPoint);

        table.setColumnBackgroundEffect(column0, 'barChart');

        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(1);

        var event = new scout.Event(table.id, 'columnBackgroundEffectChanged', {
          columnId: column0.id,
          backgroundEffect: 'barChart'
        });
        expect(mostRecentJsonRequest()).toContainEvents(event);
      });
    });
  });
});
