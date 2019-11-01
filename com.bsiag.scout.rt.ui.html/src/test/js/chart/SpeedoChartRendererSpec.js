/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/* global sandboxSession, createSimpleModel*/

import {SpeedoChartRenderer, Chart} from '../../../main/js/index';
import {scout} from '@eclipse-scout/core';
import {LocaleSpecHelper} from '@eclipse-scout/testing';

describe('SpeedoChartRenderer', function() {
  var locale, helper, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new LocaleSpecHelper();
    locale = helper.createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
  });

  describe('_formatValue', function() {
    it('should display compact labels for large values', function() {
      var speedo = new SpeedoChartRenderer({});
      speedo.session = {
        locale: locale
      };
      expect(speedo._formatValue(1)).toBe('1');
      expect(speedo._formatValue(999)).toBe('999');
      expect(speedo._formatValue(1000)).toBe('1\'000');
      expect(speedo._formatValue(9999)).toBe('9\'999');
      expect(speedo._formatValue(10000)).toBe('10k');
      expect(speedo._formatValue(999000)).toBe('999k');
      expect(speedo._formatValue(1000000)).toBe('1M');
      expect(speedo._formatValue(1200000)).toBe('1.2M');
      expect(speedo._formatValue(1230000)).toBe('1.23M');
    });
  });

  describe('click handling', function() {
    it('should handle click', function() {
      var chart = scout.create('Chart', {
        parent: session.desktop,
        clickable: true,
        chartType: Chart.SPEEDO,
        chartData: {
          axes: [],
          chartValueGroups: [{
            clickable: true,
            colorHexValue: '#ffee00',
            groupName: 'Group0',
            values: [
              1, 5, 10
            ]
          }],
          customProperties: {
            greenAreaPosition: SpeedoChartRenderer.GREEN_AREA_POSITION_CENTER
          }
        }
      });
      chart.render();
      chart.revalidateLayout();

      var event = null;
      chart.on('valueClick', function(event0) {
        event = event0;
      });
      var $svg = session.desktop.$container.find('svg');
      $svg.click();
      expect(event.data).toEqual({
        axisIndex: -1,
        groupIndex: -1,
        valueIndex: -1
      });
    });
  });
});
