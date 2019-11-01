import {FulfillmentChartRenderer} from '../../../main/js/index';

/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe('FulfillmentChartRendererSpec', function() {

  beforeEach(function() {
    setFixtures(sandbox());
  });

  describe('should animate remove on update', function() {

    var opts = {
      requestAnimation: true
    };

    it('without start value property', function() {
      var fulfillment = new FulfillmentChartRenderer({});
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(true);

      fulfillment = new FulfillmentChartRenderer({
        chartData: 'notEmpty'
      });
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(true);

      fulfillment = new FulfillmentChartRenderer({
        chartData: {
          customProperties: 'notEmpty'
        }
      });
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(true);

      opts.requestAnimation = false;
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(false);
    });

    it('with start value property', function() {
      opts.requestAnimation = true;
      var actualChartValue = {
        values: [6]
      };
      var totalChartValue = {
        values: [10]
      };
      var chartData = {
        axes: [],
        chartValueGroups: [actualChartValue, totalChartValue],
        customProperties: {
          startValue: 2
        }
      };
      var chart = {
        chartData: chartData
      };

      var fulfillment = new FulfillmentChartRenderer(chart);
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(false);

      chartData.customProperties.startValue = 0;
      fulfillment = new FulfillmentChartRenderer(chart);
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(false);
    });
  });
});
