/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractSvgChartRenderer, Chart, SpeedoChartRenderer} from '../../src/index';
import {scout} from '@eclipse-scout/core';
import {LocaleSpecHelper} from '@eclipse-scout/core/testing';

describe('SpeedoChartRenderer', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
  });

  class SpecSpeedoChartRenderer extends SpeedoChartRenderer {

    override _formatValue(value: number): string {
      return super._formatValue(value);
    }
  }

  describe('_formatValue', () => {
    it('should display compact labels for large values', () => {
      let speedo = new SpecSpeedoChartRenderer(scout.create(Chart, {parent: session.desktop}));
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

  describe('click handling', () => {
    it('should handle click', () => {
      let chart = scout.create(Chart, {
        parent: session.desktop,
        data: {
          axes: [],
          chartValueGroups: [{
            colorHexValue: '#ffee00',
            groupName: 'Group0',
            values: [
              1, 5, 10
            ]
          }]
        },
        config: {
          type: Chart.Type.SPEEDO,
          options: {
            autoColor: true,
            clickable: true,
            plugins: {
              tooltip: {
                enabled: true
              },
              legend: {
                display: true,
                position: Chart.Position.RIGHT
              }
            },
            speedo: {
              greenAreaPosition: SpeedoChartRenderer.Position.CENTER
            }
          }
        }
      });
      chart.render();
      chart.revalidateLayout();

      let event = null;
      chart.on('valueClick', event0 => {
        event = event0;
      });
      let $svg = session.desktop.$container.find('svg');
      $svg.click();
      expect(event.data).toEqual({
        xIndex: null,
        dataIndex: null,
        datasetIndex: null
      });
    });
  });

  describe('calculations', () => {
    it('rounded segments are always in the correct part', () => {
      let speedo = new SpeedoChartRenderer(scout.create(Chart, {parent: session.desktop}));

      speedo.parts = SpeedoChartRenderer.NUM_PARTS_GREEN_EDGE;
      speedo.numSegmentsPerPart = 8;
      testSegmentToBeInPart(speedo);

      speedo.parts = SpeedoChartRenderer.NUM_PARTS_GREEN_CENTER;
      speedo.numSegmentsPerPart = 5;
      testSegmentToBeInPart(speedo);
    });

    function testSegmentToBeInPart(speedo) {
      let multiplier = 100,
        modifiers = [0, 1, 5, 10];

      let minValue = 1,
        maxValue = speedo.parts * multiplier;

      for (let i = 0; i < speedo.parts + 1; i++) {
        modifiers.forEach(modifier => {
          if (i > 0 && modifier > 0) {
            let value = i * multiplier - modifier;
            expectSegmentToBeInPart(speedo, value, minValue, maxValue);
          }
          if (i < speedo.parts + 1) {
            let value = i * multiplier + modifier;
            expectSegmentToBeInPart(speedo, value, minValue, maxValue);
          }
        });
      }
    }

    function expectSegmentToBeInPart(speedo, value, minValue, maxValue) {
      let numTotalSegments = speedo.parts * speedo.numSegmentsPerPart,
        valuePercentage = speedo._getValuePercentage(value, minValue, maxValue),
        segmentToPointAt = speedo._getSegmentToPointAt(valuePercentage, numTotalSegments),
        segmentToPointAtPercentage = segmentToPointAt / numTotalSegments,
        partForValue = speedo._getPartForValue(valuePercentage),
        partForSegmentToPointAt = speedo._getPartForValue(segmentToPointAtPercentage);

      expect(partForValue)
        .withContext(`Parts are not equal for value=${value}, minValue=${minValue}, maxValue=${maxValue}. The part for the value is ${partForValue} while the part for the segment is ${partForSegmentToPointAt}.`)
        .toBe(partForSegmentToPointAt);
    }
  });

  describe('aria properties', () => {
    it('has aria description set', () => {
      let chart = scout.create(Chart, {
        parent: session.desktop,
        data: {
          axes: [],
          chartValueGroups: [{
            colorHexValue: '#ffee00',
            groupName: 'Group0',
            values: [
              1, 5, 10
            ]
          }]
        },
        config: {
          type: Chart.Type.SPEEDO,
          options: {
            autoColor: true,
            clickable: true,
            plugins: {
              tooltip: {
                enabled: true
              },
              legend: {
                display: true,
                position: Chart.Position.RIGHT
              }
            },
            speedo: {
              greenAreaPosition: SpeedoChartRenderer.Position.CENTER
            }
          }
        }
      });
      chart.render();
      chart.chartRenderer.refresh();
      expect((chart.chartRenderer as AbstractSvgChartRenderer).$svg.attr('aria-description')).toBeTruthy();
    });
  });
});
