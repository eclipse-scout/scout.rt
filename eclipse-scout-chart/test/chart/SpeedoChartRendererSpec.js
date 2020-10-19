/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/* global sandboxSession, createSimpleModel*/

import {Chart, SpeedoChartRenderer} from '../../src/index';
import {scout} from '@eclipse-scout/core';
import {LocaleSpecHelper} from '@eclipse-scout/core/src/testing/index';

describe('SpeedoChartRenderer', () => {
  let locale, helper, session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new LocaleSpecHelper();
    locale = helper.createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
  });

  describe('_formatValue', () => {
    it('should display compact labels for large values', () => {
      let speedo = new SpeedoChartRenderer({});
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

  describe('click handling', () => {
    it('should handle click', () => {
      let chart = scout.create('Chart', {
        parent: session.desktop,
        data: {
          axes: [],
          chartValueGroups: [{
            clickable: true,
            colorHexValue: '#ffee00',
            groupName: 'Group0',
            values: [
              1, 5, 10
            ]
          }]
        },
        config: {
          type: Chart.Type.SPEEDO,
          clickable: true,
          options: {
            autoColor: true,
            clickable: true,
            tooltips: {
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
        datasetIndex: null
      });
    });
  });
});
