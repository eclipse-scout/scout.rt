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

import {Chart} from '../../src/index';
import {Event} from '@eclipse-scout/core';

describe('ChartSpec', () => {

  describe('click and check', () => {
    let defaultConfig = {
      type: Chart.Type.BAR,
      data: {
        datasets: [{
          data: [11, 13, 17, 31, 37, 41, 43],
          label: 'Dataset 1'
        }]
      },
      options: {
        clickable: true
      }
    };

    it('click a non-checkable chart', () => {
      let chart = new Chart();
      chart.setConfig(defaultConfig);

      expect(chart.checkedItems).toEqual([]);

      let click0 = new Event();
      click0.data = {
        datasetIndex: 0,
        dataIndex: 0
      };
      chart._onValueClick(click0);

      expect(chart.checkedItems).toEqual([]);

      let click3 = new Event();
      click3.data = {
        datasetIndex: 0,
        dataIndex: 3
      };
      chart._onValueClick(click3);

      expect(chart.checkedItems).toEqual([]);

      let click0Again = new Event();
      click0Again.data = {
        datasetIndex: 0,
        dataIndex: 0
      };
      chart._onValueClick(click0Again);

      expect(chart.checkedItems).toEqual([]);
    });

    it('click a checkable chart', () => {
      let chart = new Chart(),
        config = $.extend(true, {}, defaultConfig, {
          options: {
            checkable: true
          }
        });
      chart.setConfig(config);

      expect(chart.checkedItems).toEqual([]);

      let click0 = new Event();
      click0.data = {
        datasetIndex: 0,
        dataIndex: 0
      };
      chart._onValueClick(click0);

      expect(chart.checkedItems).toEqual([{
        datasetIndex: 0,
        dataIndex: 0
      }]);

      let click3 = new Event();
      click3.data = {
        datasetIndex: 0,
        dataIndex: 3
      };
      chart._onValueClick(click3);

      expect(chart.checkedItems).toEqual([{
        datasetIndex: 0,
        dataIndex: 0
      }, {
        datasetIndex: 0,
        dataIndex: 3
      }]);

      let click0Again = new Event();
      click0Again.data = {
        datasetIndex: 0,
        dataIndex: 0
      };
      chart._onValueClick(click0Again);

      expect(chart.checkedItems).toEqual([{
        datasetIndex: 0,
        dataIndex: 3
      }]);
    });

    it('setCheckedItems', () => {
      let chart = new Chart(),
        config = $.extend(true, {}, defaultConfig, {
          options: {
            checkable: true
          }
        });
      chart.setConfig(config);

      expect(chart.checkedItems).toEqual([]);

      chart.setCheckedItems([{
        datasetIndex: 0,
        dataIndex: 0
      }]);

      expect(chart.checkedItems).toEqual([{
        datasetIndex: 0,
        dataIndex: 0
      }]);

      chart.setCheckedItems([{
        datasetIndex: 0,
        dataIndex: 3
      }]);

      expect(chart.checkedItems).toEqual([{
        datasetIndex: 0,
        dataIndex: 3
      }]);

      chart.setCheckedItems([{
        datasetIndex: 0,
        dataIndex: 0
      }, {
        datasetIndex: 0,
        dataIndex: 3
      }]);

      expect(chart.checkedItems).toEqual([{
        datasetIndex: 0,
        dataIndex: 0
      }, {
        datasetIndex: 0,
        dataIndex: 3
      }]);

      chart.setCheckedItems([]);

      expect(chart.checkedItems).toEqual([]);

      // dataIndex 42 is too large and is therefore filtered out
      chart.setCheckedItems([{
        datasetIndex: 0,
        dataIndex: 0
      }, {
        datasetIndex: 0,
        dataIndex: 3
      }, {
        datasetIndex: 0,
        dataIndex: 42
      }]);

      expect(chart.checkedItems).toEqual([{
        datasetIndex: 0,
        dataIndex: 0
      }, {
        datasetIndex: 0,
        dataIndex: 3
      }]);
    });
  });
});
