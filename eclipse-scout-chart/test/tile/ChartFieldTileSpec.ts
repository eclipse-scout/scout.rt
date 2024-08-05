/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {ColorScheme, colorSchemes, scout} from '@eclipse-scout/core';
import {Chart, ChartField, ChartFieldModel, ChartFieldTile, ChartFieldTileModel} from '../../src/index';
import $ from 'jquery';

describe('ChartFieldTile', () => {
  let session: SandboxSession;
  let tile: ChartFieldTile;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    tile = createChartFieldTile();
  });

  function createChartFieldTile(model?: ChartFieldTileModel): ChartFieldTile {
    return scout.create(ChartFieldTile, $.extend({}, {
      parent: session.desktop,
      tileWidget: {
        objectType: ChartField,
        chart: {
          objectType: Chart
        }
      }
    }, model));
  }

  function createChartField(model?: ChartFieldModel): ChartField {
    return scout.create(ChartField, $.extend({}, {
      parent: session.desktop,
      chart: {
        objectType: Chart
      }
    }, model));
  }

  function getChartColorScheme(): ColorScheme {
    return tile.tileWidget.chart.config.options.colorScheme as ColorScheme;
  }

  describe('colorScheme', () => {

    it('is updated on the chart if changed on the tile', () => {
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.DEFAULT, true));

      tile.setColorScheme(colorSchemes.ColorSchemeId.RAINBOW);
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.RAINBOW, true));

      tile.setColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted');
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted', true));
    });

    it('is prevented from being updated on the chart', () => {
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.DEFAULT, true));

      const chart = tile.tileWidget.chart;
      const setChartColorScheme = (colorScheme: string) => {
        chart.setConfig($.extend(true, {}, chart.config, {
          options: {colorScheme}
        }));
      };

      setChartColorScheme(colorSchemes.ColorSchemeId.RAINBOW);
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.DEFAULT, true));

      setChartColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted');
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.DEFAULT, true));

      tile.setColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted');
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted', true));

      setChartColorScheme(colorSchemes.ColorSchemeId.RAINBOW);
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted', true));

      setChartColorScheme(colorSchemes.ColorSchemeId.DEFAULT);
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted', true));
    });

    it('is set on the chart if the tile widget changes', () => {
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.DEFAULT, true));

      const createChartFieldWithColorScheme = (colorScheme: string) => createChartField({
        chart: {
          objectType: Chart,
          config: {
            type: Chart.Type.PIE,
            options: {colorScheme}
          }
        }
      });

      tile.setTileWidget(createChartFieldWithColorScheme(colorSchemes.ColorSchemeId.RAINBOW));
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.DEFAULT, true));

      tile.setTileWidget(createChartFieldWithColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted'));
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.DEFAULT, true));

      tile.setColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted');
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted', true));

      tile.setTileWidget(createChartFieldWithColorScheme(colorSchemes.ColorSchemeId.RAINBOW));
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted', true));

      tile.setTileWidget(createChartFieldWithColorScheme(colorSchemes.ColorSchemeId.DEFAULT));
      expect(getChartColorScheme()).toEqual(colorSchemes.ensureColorScheme(colorSchemes.ColorSchemeId.ALTERNATIVE + '-inverted', true));
    });
  });
});
