/*
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {Chart, ChartJsRenderer} from '../../src/index';

describe('ChartJsRendererSpec', () => {

  describe('_adjustGrid', () => {
    let renderer = new ChartJsRenderer({}),
      chartArea = {
        top: 0,
        bottom: 300,
        left: 0,
        right: 750
      },
      defaultConfig = {
        data: {
          datasets: [{
            data: [11, 13, 17, 31, 37, 41, 43],
            label: 'Dataset 1'
          }]
        }
      },
      defaultScalesConfig = $.extend(true, {}, defaultConfig, {
        options: {
          scales: {
            xAxes: [{}],
            yAxes: [{}]
          }
        }
      }),
      defaultScaleConfig = $.extend(true, {}, defaultConfig, {
        options: {
          scale: {}
        }
      });

    it('bar chart, min/max is set on y axis', () => {
      let config = $.extend(true, {}, defaultScalesConfig, {type: Chart.Type.BAR});

      renderer._adjustGrid(config, chartArea);

      expect(config.options.scales.xAxes[0]).toEqual({});
      expect(config.options.scales.yAxes[0]).toEqual({
        ticks: {
          maxTicksLimit: 8,
          suggestedMax: 45,
          suggestedMin: 10
        }
      });
    });

    it('horizontal bar chart, min/max is set on x axis', () => {
      let config = $.extend(true, {}, defaultScalesConfig, {type: Chart.Type.BAR_HORIZONTAL});

      renderer._adjustGrid(config, chartArea);

      expect(config.options.scales.xAxes[0]).toEqual({
        ticks: {
          maxTicksLimit: 5,
          suggestedMax: 45,
          suggestedMin: 10
        }
      });
      expect(config.options.scales.yAxes[0]).toEqual({});
    });

    it('polar area chart, min/max is set on scale', () => {
      let config = $.extend(true, {}, defaultScaleConfig, {type: Chart.Type.POLAR_AREA});

      renderer._adjustGrid(config, chartArea);

      expect(config.options.scale).toEqual({
        ticks: {
          maxTicksLimit: 4,
          suggestedMax: 45,
          suggestedMin: 10
        }
      });
    });

    it('bubble chart, min/max is set on x and y axis, axis without offset take max(r) into account', () => {
      let config = $.extend(true, {}, defaultScalesConfig, {
        type: Chart.Type.BUBBLE,
        options: {
          bubble: {
            sizeOfLargestBubble: 50
          },
          scales: {
            xAxes: [
              {
                offset: true
              }
            ]
          }
        }
      });

      config.data.datasets[0].data = [
        {x: 11, y: 37, r: 47},
        {x: 13, y: 17, r: 29},
        {x: 17, y: 41, r: 19},
        {x: 31, y: 11, r: 53},
        {x: 37, y: 31, r: 13},
        {x: 41, y: 43, r: 11},
        {x: 43, y: 13, r: 37}
      ];

      renderer._adjustGrid(config, chartArea);

      let height = Math.abs(chartArea.top - chartArea.bottom),
        padding = 53, // max(r)
        maxY = 43,
        minY = 11,
        yValuePerPixel = (maxY - minY) / (height - 2 * padding),
        yPaddingValue = yValuePerPixel * padding;

      expect(config.options.scales.xAxes[0]).toEqual({
        offset: true,
        ticks: {
          maxTicksLimit: 5,
          suggestedMax: 46,
          suggestedMin: 11
        }
      });
      expect(config.options.scales.yAxes[0]).toEqual({
        ticks: {
          maxTicksLimit: 8,
          suggestedMax: 43 + yPaddingValue,
          suggestedMin: 11 - yPaddingValue
        }
      });
    });

    it('bubble chart, min/max is set on x and y axis, axis without offset take max(r) into account, axis with labelMap calculate exact min/max', () => {

      let labelMap = {
          11: 'Label 11',
          13: 'Label 13',
          17: 'Label 17',
          31: 'Label 31',
          37: 'Label 37',
          41: 'Label 41',
          43: 'Label 43'
        },
        config = $.extend(true, {}, defaultScalesConfig, {
          type: Chart.Type.BUBBLE,
          options: {
            bubble: {
              sizeOfLargestBubble: 50
            },
            scales: {
              xLabelMap: labelMap,
              yLabelMap: labelMap,
              xAxes: [
                {
                  offset: true
                }
              ]
            }
          }
        });

      config.data.datasets[0].data = [
        {x: 11, y: 37, r: 47},
        {x: 13, y: 17, r: 29},
        {x: 17, y: 41, r: 19},
        {x: 31, y: 11, r: 53},
        {x: 37, y: 31, r: 13},
        {x: 41, y: 43, r: 11},
        {x: 43, y: 13, r: 37}
      ];

      renderer._adjustGrid(config, chartArea);

      expect(config.options.scales.xAxes[0]).toEqual({
        offset: true,
        ticks: {
          maxTicksLimit: 5,
          suggestedMax: 43,
          suggestedMin: 11
        }
      });
      expect(config.options.scales.yAxes[0]).toEqual({
        ticks: {
          maxTicksLimit: 8,
          suggestedMax: 52,
          suggestedMin: 2
        }
      });
    });
  });
});
