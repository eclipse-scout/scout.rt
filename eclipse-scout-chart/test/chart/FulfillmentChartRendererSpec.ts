/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Chart, FulfillmentChartRenderer} from '../../src/index';
import {scout} from '@eclipse-scout/core';

describe('FulfillmentChartRendererSpec', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
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
        options: {
          fulfillment: {
            startValue: 2
          }
        }
      };
      let chart = {
        data: data,
        config: config
      };

      let fulfillment = new FulfillmentChartRenderer(chart);
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(false);

      config.options.fulfillment.startValue = 0;
      fulfillment = new FulfillmentChartRenderer(chart);
      expect(fulfillment.shouldAnimateRemoveOnUpdate(opts)).toBe(false);
    });
  });

  describe('aria properties', () => {
    it('has aria description set', () => {
      let chart = scout.create(Chart, {
        parent: session.desktop,
        data: {
          axes: [],
          chartValueGroups: [{
            values: [6]
          }, {
            values: [10]
          }]
        },
        config: {
          type: Chart.Type.FULFILLMENT,
          options: {
            fulfillment: {
              startValue: 2
            }
          }
        }
      });
      chart.render();
      chart.chartRenderer.refresh();
      expect(chart.chartRenderer.$svg.attr('aria-description')).toBeTruthy();
    });
  });
});
