/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Chart, ChartConfig, ChartJsRenderer} from '../../src/index';
import {scout} from '@eclipse-scout/core';
import {ChartArea} from 'chart.js';

describe('ChartJsRendererSpec', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  class SpecChartJsRenderer extends ChartJsRenderer {

    override _adjustGridMaxMin(config: ChartConfig, chartArea: ChartArea) {
      super._adjustGridMaxMin(config, chartArea);
    }

    override _adjustBubbleSizes(config: ChartConfig, chartArea: ChartArea) {
      super._adjustBubbleSizes(config, chartArea);
    }
  }

  describe('_adjustGridMaxMin', () => {
    let renderer: SpecChartJsRenderer,
      chartArea: ChartArea,
      defaultConfig: Partial<ChartConfig>,
      defaultScalesConfig: Partial<ChartConfig>,
      defaultScaleConfig: Partial<ChartConfig>;

    beforeEach(() => {
      renderer = new SpecChartJsRenderer(scout.create(Chart, {parent: session.desktop}));
      chartArea = {
        top: 0,
        bottom: 300,
        left: 0,
        right: 750
      } as ChartArea;
      defaultConfig = {
        data: {
          datasets: [{
            data: [11, 13, 17, 31, 37, 41, 43],
            label: 'Dataset 1'
          }]
        },
        options: {
          adjustGridMaxMin: true
        }
      };
      defaultScalesConfig = $.extend(true, {}, defaultConfig, {
        options: {
          scales: {
            x: {
              minSpaceBetweenTicks: 150
            },
            y: {
              minSpaceBetweenTicks: 35
            }
          }
        }
      });
      defaultScaleConfig = $.extend(true, {}, defaultConfig, {
        options: {
          scales: {
            r: {
              minSpaceBetweenTicks: 35
            }
          }
        }
      });
    });

    it('bar chart, min/max is set on y axis', () => {
      let config = $.extend(true, {}, defaultScalesConfig, {type: Chart.Type.BAR});

      renderer._adjustGridMaxMin(config, chartArea);

      expect(config.options.scales.x).toEqual({
        minSpaceBetweenTicks: 150 // default value, not part of this test
      });
      expect(config.options.scales.y).toEqual({
        minSpaceBetweenTicks: 35, // default value, not part of this test
        suggestedMax: 45,
        suggestedMin: 10,
        ticks: {
          maxTicksLimit: 9,
          stepSize: undefined // default value, not part of this test
        }
      });
    });

    it('horizontal bar chart, min/max is set on x axis', () => {
      let config = $.extend(true, {}, defaultScalesConfig, {
        type: Chart.Type.BAR,
        options: {
          indexAxis: 'y'
        }
      });

      renderer._adjustGridMaxMin(config, chartArea);

      expect(config.options.scales.x).toEqual({
        minSpaceBetweenTicks: 150, // default value, not part of this test
        suggestedMax: 45,
        suggestedMin: 10,
        ticks: {
          maxTicksLimit: 6,
          stepSize: undefined // default value, not part of this test
        }
      });
      expect(config.options.scales.y).toEqual({
        minSpaceBetweenTicks: 35 // default value, not part of this test
      });
    });

    it('polar area chart, min/max is set on scale', () => {
      let config = $.extend(true, {}, defaultScaleConfig, {type: Chart.Type.POLAR_AREA});

      renderer._adjustGridMaxMin(config, chartArea);

      expect(config.options.scales.r).toEqual({
        minSpaceBetweenTicks: 35, // default value, not part of this test
        suggestedMax: 45,
        suggestedMin: 10,
        ticks: {
          maxTicksLimit: 5,
          stepSize: undefined // default value, not part of this test
        }
      });
    });

    it('bubble chart, min/max is set on x and y axis, axis without offset take max(r) into account', () => {
      let config = $.extend(true, {}, defaultScalesConfig, {
        type: Chart.Type.BUBBLE,
        options: {
          scales: {
            x: {
              offset: true
            }
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

      renderer._adjustGridMaxMin(config, chartArea);

      let height = Math.abs(chartArea.top - chartArea.bottom),
        padding = 53, // max(r)
        maxY = 43,
        minY = 11,
        yValuePerPixel = (maxY - minY) / (height - 2 * padding),
        yPaddingValue = yValuePerPixel * padding;

      expect(config.options.scales.x).toEqual({
        minSpaceBetweenTicks: 150, // default value, not part of this test
        offset: true,
        suggestedMax: 46,
        suggestedMin: 11,
        ticks: {
          maxTicksLimit: 6,
          stepSize: undefined // default value, not part of this test
        }
      });
      expect(config.options.scales.y).toEqual({
        minSpaceBetweenTicks: 35, // default value, not part of this test
        suggestedMax: 43 + yPaddingValue,
        suggestedMin: 11 - yPaddingValue,
        ticks: {
          maxTicksLimit: 9,
          stepSize: undefined // default value, not part of this test
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
            scales: {
              x: {
                offset: true
              }
            },
            xLabelMap: labelMap,
            yLabelMap: labelMap
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

      renderer._adjustGridMaxMin(config, chartArea);

      expect(config.options.scales.x).toEqual({
        minSpaceBetweenTicks: 150, // default value, not part of this test
        offset: true,
        suggestedMax: 43,
        suggestedMin: 11,
        ticks: {
          maxTicksLimit: 6,
          stepSize: undefined // default value, not part of this test
        }
      });
      expect(config.options.scales.y).toEqual({
        minSpaceBetweenTicks: 35, // default value, not part of this test
        suggestedMax: 52,
        suggestedMin: 2,
        ticks: {
          maxTicksLimit: 9,
          stepSize: undefined // default value, not part of this test
        }
      });
    });
  });

  describe('_adjustBubbleSizes', () => {
    let renderer: SpecChartJsRenderer,
      chartArea: ChartArea,
      defaultConfig: ChartConfig;

    beforeEach(() => {
      renderer = new SpecChartJsRenderer(scout.create(Chart, {parent: session.desktop}));
      chartArea = {
        top: 0,
        bottom: 300,
        left: 0,
        right: 750
      } as ChartArea;
      defaultConfig = {
        type: Chart.Type.BUBBLE,
        data: {
          datasets: [{
            data: [
              {x: 0, y: 0, z: 100},
              {x: 0, y: 0, z: 81},
              {x: 0, y: 0, z: 49},
              {x: 0, y: 0, z: 25},
              {x: 0, y: 0, z: 16}
            ],
            label: 'Dataset 1'
          }]
        },
        options: {
          bubble: {}
        }
      };
    });

    it('neither sizeOfLargestBubble nor minBubbleSize is set', () => {
      let config = $.extend(true, {}, defaultConfig);

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {x: 0, y: 0, r: 10, z: 100},
        {x: 0, y: 0, r: 9, z: 81},
        {x: 0, y: 0, r: 7, z: 49},
        {x: 0, y: 0, r: 5, z: 25},
        {x: 0, y: 0, r: 4, z: 16}
      ]);
    });

    it('only sizeOfLargestBubble is set', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.options.bubble.sizeOfLargestBubble = 20;

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {x: 0, y: 0, r: 20, z: 100},
        {x: 0, y: 0, r: 18, z: 81},
        {x: 0, y: 0, r: 14, z: 49},
        {x: 0, y: 0, r: 10, z: 25},
        {x: 0, y: 0, r: 8, z: 16}
      ]);
    });

    it('only sizeOfLargestBubble is set, maxR equals 0', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.options.bubble.sizeOfLargestBubble = 20;

      config.data.datasets[0].data = [
        {x: 0, y: 0, z: 0},
        {x: 0, y: 0, z: 0},
        {x: 0, y: 0, z: 0}
      ];

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {x: 0, y: 0, r: 20, z: 0},
        {x: 0, y: 0, r: 20, z: 0},
        {x: 0, y: 0, r: 20, z: 0}
      ]);
    });

    it('only minBubbleSize is set', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.options.bubble.minBubbleSize = 5;

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {x: 0, y: 0, r: 12.5, z: 100},
        {x: 0, y: 0, r: 11.25, z: 81},
        {x: 0, y: 0, r: 8.75, z: 49},
        {x: 0, y: 0, r: 6.25, z: 25},
        {x: 0, y: 0, r: 5, z: 16}
      ]);
    });

    it('only minBubbleSize is set, minR equals 0', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.options.bubble.minBubbleSize = 5;

      config.data.datasets[0].data.push({x: 0, y: 0, z: 0});

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {x: 0, y: 0, r: 15, z: 100},
        {x: 0, y: 0, r: 14, z: 81},
        {x: 0, y: 0, r: 12, z: 49},
        {x: 0, y: 0, r: 10, z: 25},
        {x: 0, y: 0, r: 9, z: 16},
        {x: 0, y: 0, r: 5, z: 0}
      ]);
    });

    it('sizeOfLargestBubble and minBubbleSize are set, scaling is sufficient', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.options.bubble.sizeOfLargestBubble = 20;
      config.options.bubble.minBubbleSize = 5;

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {x: 0, y: 0, r: 20, z: 100},
        {x: 0, y: 0, r: 18, z: 81},
        {x: 0, y: 0, r: 14, z: 49},
        {x: 0, y: 0, r: 10, z: 25},
        {x: 0, y: 0, r: 8, z: 16}
      ]);
    });

    it('sizeOfLargestBubble and minBubbleSize are set, scaling is not sufficient', () => {
      let config = $.extend(true, {}, defaultConfig);
      config.options.bubble.sizeOfLargestBubble = 20;
      config.options.bubble.minBubbleSize = 5;

      config.data.datasets[0].data.push({x: 0, y: 0, z: 1});

      renderer._adjustBubbleSizes(config, chartArea);

      expect(config.data.datasets[0].data).toEqual([
        {x: 0, y: 0, r: 20, z: 100},
        {x: 0, y: 0, r: 9 * (5 / 3) + (10 / 3), z: 81},
        {x: 0, y: 0, r: 15, z: 49},
        {x: 0, y: 0, r: 5 * (5 / 3) + (10 / 3), z: 25},
        {x: 0, y: 0, r: 10, z: 16},
        {x: 0, y: 0, r: 5, z: 1}
      ]);
    });
  });
});
