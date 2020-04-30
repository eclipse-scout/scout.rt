import {FulfillmentChartRenderer} from '../../../main/js/index';

/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe('FulfillmentChartRendererSpec', () => {

  beforeEach(() => {
    setFixtures(sandbox());
  });

  describe('should animate remove on update', () => {

    let opts = {
      requestAnimation: true
    };

    it('without start value property', () => {
      let fulfillment = new FulfillmentChartRenderer({});
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(true);

      fulfillment = new FulfillmentChartRenderer({
        config: 'notEmpty'
      });
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(true);

      fulfillment = new FulfillmentChartRenderer({
        config: {
          fulfillment: 'notEmpty'
        }
      });
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(true);

      opts.requestAnimation = false;
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(false);
    });

    it('with start value property', () => {
      opts.requestAnimation = true;
      let actualChartValue = {
        values: [6]
      };
      let totalChartValue = {
        values: [10]
      };
      let data = {
        axes: [],
        chartValueGroups: [actualChartValue, totalChartValue]
      };
      let config = {
        fulfillment: {
          startValue: 2
        }
      };
      let chart = {
        data: data,
        config: config
      };

      let fulfillment = new FulfillmentChartRenderer(chart);
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(false);

      config.fulfillment.startValue = 0;
      fulfillment = new FulfillmentChartRenderer(chart);
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(false);
    });
  });
});
