/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/* global LocaleSpecHelper */
/* global linkWidgetAndAdapter */
describe('NumberColumn', function() {
  var session;
  var helper;
  var locale;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TableSpecHelper(session);
    locale = new scout.LocaleSpecHelper().createLocale(scout.LocaleSpecHelper.DEFAULT_LOCALE);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('background effect', function() {
    var rgbLevel0 = 'rgb(255, 175, 175)';
    var rgbLevel50 = 'rgb(213, 195, 161)';
    var rgbLevel100 = 'rgb(171, 214, 147)';
    var barChartColor = 'rgb(128, 193, 208)';
    var imageLevel50 = 'linear-gradient(to left, ' + barChartColor + ' 0%, ' + barChartColor + ' 50%, transparent 50%, transparent 100%)';
    var defaultBackgroundColor;

    scout.styles.put('column-background-effect-gradient1-start', {
      backgroundColor: rgbLevel0
    });
    scout.styles.put('column-background-effect-gradient1-end', {
      backgroundColor: rgbLevel100
    });
    scout.styles.put('column-background-effect-gradient2-start', {
      backgroundColor: rgbLevel100
    });
    scout.styles.put('column-background-effect-gradient2-end', {
      backgroundColor: rgbLevel0
    });
    scout.styles.put('column-background-effect-bar-chart', {
      backgroundColor: barChartColor
    });

    beforeEach(function() {
      var $div = $('<div>').appendTo(session.$entryPoint);
      defaultBackgroundColor = $div.css('background-color');
    });

    describe('colorGradient1', function() {
      it('colors cells from red to green', function() {
        var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        var table = helper.createTable(model);
        var column0 = table.columns[0];
        table.render();

        table.setColumnBackgroundEffect(column0, 'colorGradient1');
        expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
        expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel100);
      });

      it('colors cells according to rounded values', function() {
        var model = helper.createModelSingleColumnByValues([0, 0.005, 0.006, 0.02], 'NumberColumn');
        var table = helper.createTable(model);
        var column0 = table.columns[0];
        table.render();

        column0.decimalFormat = new scout.DecimalFormat(locale, {
          pattern: '#.00'
        });
        table.setColumnBackgroundEffect(column0, 'colorGradient1');
        expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
        expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel50);
        expect(table.$cell(column0, table.rows[3].$row).css('background-color')).toBe(rgbLevel100);
      });
    });

    it('considers view range -> only colors rendered cells', function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.viewRangeSize = 2;
      table.render();

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

    it('updates colors if row gets deleted', function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.render();

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      table.deleteRow(table.rows[2]);
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel100);
    });

    it('updates colors if row gets inserted', function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.render();

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      var row = helper.createModelRowByValues(undefined, 200);
      table.insertRow(row);
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(234, 185, 168)');
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe('rgb(213, 195, 161)');
      expect(table.$cell(column0, table.rows[3].$row).css('background-color')).toBe(rgbLevel100);
    });

    it('updates colors if row gets updated', function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      model.columns[0].backgroundEffect = 'colorGradient1';
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.render();

      // Change row 0 value to 150, row 1 now has the lowest values
      var rows = helper.createModelRows(1, 1);
      rows[0].id = table.rows[0].id;
      rows[0].cells[0].value = 150;
      table.updateRows(rows);

      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel100);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel50);
    });

    it('colors cells if table gets rendered', function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      model.columns[0].backgroundEffect = 'colorGradient1';
      var table = helper.createTable(model);
      var column0 = table.columns[0];

      table.render();
      expect(table.$cell(column0, table.rows[0].$row).css('background-color')).toBe(rgbLevel0);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);
      expect(table.$cell(column0, table.rows[2].$row).css('background-color')).toBe(rgbLevel100);
    });

    it('restores existing background color if background effect gets removed', function() {
      var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
      model.rows[1].cells[0].backgroundColor = 'ff0000';
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.render();

      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(255, 0, 0)');
      expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe('none');

      table.setColumnBackgroundEffect(column0, 'colorGradient1');
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe(rgbLevel50);

      table.setColumnBackgroundEffect(column0, null);
      expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(255, 0, 0)');
      expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe('none');
    });

    describe('barChart', function() {
      it('does not overwrite existing background color', function() {
        if (!scout.device.supportsCssGradient()) {
          // PhantomJs does not support gradients
          return;
        }
        var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        model.rows[1].cells[0].backgroundColor = 'ff0000';
        var table = helper.createTable(model);
        var column0 = table.columns[0];
        table.render();

        table.setColumnBackgroundEffect(column0, 'barChart');
        expect(table.$cell(column0, table.rows[1].$row).css('background-color')).toBe('rgb(255, 0, 0)');
        expect(table.$cell(column0, table.rows[1].$row).css('background-image')).toBe(imageLevel50);
      });
    });

    describe('setBackgroundEffect', function() {
      it('changes the background effect', function() {
        var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        var table = helper.createTable(model);
        var column0 = table.columns[0];
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

      it('sends columnBackgroundEffectChanged event', function() {
        var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        var table = helper.createTable(model);
        var column0 = table.columns[0];
        table.render();

        linkWidgetAndAdapter(table, 'TableAdapter');
        table.setColumnBackgroundEffect(column0, 'barChart');

        sendQueuedAjaxCalls();
        expect(jasmine.Ajax.requests.count()).toBe(1);

        var event = new scout.RemoteEvent(table.id, 'columnBackgroundEffectChanged', {
          columnId: column0.id,
          backgroundEffect: 'barChart'
        });
        expect(mostRecentJsonRequest()).toContainEvents(event);
      });

      it('does not send columnBackgroundEffectChanged if server triggered it', function() {
        var model = helper.createModelSingleColumnByValues([0, 50, 100], 'NumberColumn');
        var table = helper.createTable(model);
        var column0 = table.columns[0];
        table.render();

        linkWidgetAndAdapter(table, 'TableAdapter');
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

  describe('calculateMinMaxValues', function() {
    it('calculates the min/max values based on rounded values', function() {
      var model = helper.createModelSingleColumnByValues([0.005, 0.006], 'NumberColumn');
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      table.render();

      column0.decimalFormat = new scout.DecimalFormat(locale, {
        pattern: '#.00'
      });

      column0.calculateMinMaxValues();
      expect(column0.minValue).toBe(0.01);
      expect(column0.maxValue).toBe(0.01);
    });
  });
});
