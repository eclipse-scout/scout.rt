/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Chart} from '../../src/index';

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

      chart.handleValueClick({
        datasetIndex: 0,
        dataIndex: 0
      });

      expect(chart.checkedItems).toEqual([]);

      chart.handleValueClick({
        datasetIndex: 0,
        dataIndex: 3
      });

      expect(chart.checkedItems).toEqual([]);

      chart.handleValueClick({
        datasetIndex: 0,
        dataIndex: 0
      });

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

      chart.handleValueClick({
        datasetIndex: 0,
        dataIndex: 0
      });

      expect(chart.checkedItems).toEqual([{
        datasetIndex: 0,
        dataIndex: 0
      }]);

      chart.handleValueClick({
        datasetIndex: 0,
        dataIndex: 3
      });

      expect(chart.checkedItems).toEqual([{
        datasetIndex: 0,
        dataIndex: 0
      }, {
        datasetIndex: 0,
        dataIndex: 3
      }]);

      chart.handleValueClick({
        datasetIndex: 0,
        dataIndex: 0
      });

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
