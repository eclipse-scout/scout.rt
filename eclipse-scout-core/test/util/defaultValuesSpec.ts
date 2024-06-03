/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {defaultValues} from '../../src/index';

describe('defaultValues', () => {

  afterEach(() => {
    // Reload default values to not influence other tests
    defaultValues.bootstrap();
  });

  describe('init', () => {

    it('can load invalid configurations', () => {
      expect(() => {
        // @ts-expect-error
        defaultValues.init();
      }).toThrow();
      defaultValues.init({});
      defaultValues.init({
        'defaults': {}
      });
      defaultValues.init({
        'objectTypeHierarchy': {}
      });
      expect(() => {
        defaultValues.init({
          'objectTypeHierarchy': {
            'FormField': {
              'TableField': null,
              'ValueField': {
                'TableField': null
              }
            }
          }
        });
      }).toThrow();
    });

  });

  describe('applyTo', () => {

    it('can apply default values to JSON', () => {
      let config = {
        'defaults': {
          'FormField': {
            'width': 10,
            'alignment': -1,
            'keyStrokes': [],
            'gridData': {
              'x': 0,
              'y': 0
            }
          },
          'NumberField': {
            'alignment': 1
          },
          'Chart': {
            'chartData': {},
            '~axisData': {
              '~xAxis': {
                'label': ''
              },
              'yAxis': {
                'label': ''
              }
            }
          }
        },
        'objectTypeHierarchy': {
          'FormField': {
            'ValueField': {
              'NumberField': null,
              'SmartField': null
            }
          }
        }
      };
      defaultValues.init(config);

      let testObjects = [{ // [0]
        'id': '2',
        'objectType': 'SmartField'
      }, { // [1]
        'id': '3',
        'objectType': 'SmartField',
        'width': 20
      }, { // [2]
        'id': '4',
        'objectType': 'NumberField',
        'width': 20
      }, { // [3]
        'id': '100',
        'plainValue': {}
      }, { // [4]
        'id': '103',
        'objectType': 'SmartField',
        'gridData': '77'
      }, { // [5]
        'id': '104',
        'objectType': 'SmartField',
        'gridData': {
          y: 5
        }
      }, { // [6]
        'id': '7',
        'objectType': 'Chart',
        'enabled': false,
        'chartData': {
          'value': 2
        }
      }, { // [7]
        'id': '8',
        'objectType': 'Chart',
        'enabled': true
      }, { // [8]
        'id': '9',
        'objectType': 'Chart',
        'enabled': true,
        'chartData': 'none'
      }, { // [9]
        'id': '10',
        'objectType': 'Chart',
        'axisData': {
          'yAxis': {
            'label': 'non-default'
          }
        }
      }, { // [10]
        'id': '11',
        'objectType': 'Chart',
        'axisData': [{
          'yAxis': {
            'label': 'non-default'
          }
        }, {}, {}]
      }, { // [11]
        'id': '12',
        'objectType': 'Chart',
        'axisData': []
      }];
      defaultValues.applyTo(testObjects);

      expect(testObjects[0].width).toBe(10);
      // @ts-expect-error
      expect(testObjects[0].x).toBe(undefined);
      expect(testObjects[1].width).toBe(20);
      // @ts-expect-error
      expect(testObjects[1].alignment).toBe(-1);
      // @ts-expect-error
      expect(testObjects[2].alignment).toBe(1);
      expect(testObjects[2].width).toBe(20);
      expect(testObjects[3].gridData).toBe(undefined);
      expect(testObjects[4].gridData).toBe('77');
      // @ts-expect-error
      expect(testObjects[5].gridData.x).toBe(0);
      // @ts-expect-error
      expect(testObjects[5].gridData.y).toBe(5);
      // @ts-expect-error
      expect(testObjects[6].chartData.value).toBe(2);
      // @ts-expect-error
      expect(testObjects[7].chartData).toEqual({});
      expect(testObjects[7].axisData).toBe(undefined);
      expect(testObjects[8].chartData).toBe('none');
      expect(testObjects[8].axisData).toBe(undefined);
      // @ts-expect-error
      expect(testObjects[9].axisData.xAxis).toBe(undefined);
      // @ts-expect-error
      expect(testObjects[9].axisData.yAxis.label).toBe('non-default');
      // @ts-expect-error
      expect(testObjects[10].axisData.length).toBe(3);
      expect(testObjects[10].axisData[0].xAxis).toBe(undefined);
      expect(testObjects[10].axisData[0].yAxis.label).toBe('non-default');
      expect(testObjects[10].axisData[1].xAxis).toBe(undefined);
      expect(testObjects[10].axisData[1].yAxis.label).toBe('');
      expect(testObjects[10].axisData[2].xAxis).toBe(undefined);
      expect(testObjects[10].axisData[2].yAxis.label).toBe('');
      // @ts-expect-error
      expect(testObjects[11].axisData.length).toBe(0);
    });

    it('can apply default values to JSON considering the model variant', () => {
      let config = {
        'defaults': {
          'FormField': {
            'enabled': true
          },
          'TableField:Custom': {
            'enabled': false,
            'borderDecoration': 'auto'
          }
        },
        'objectTypeHierarchy': {
          'FormField': {
            'TableField': null
          }
        }
      };
      defaultValues.init(config);

      let testObjects = [{ // [0]
        'id': '1',
        'objectType': 'FormField',
        'visible': true,
        'borderDecoration': 'auto'
      }, { // [1]
        'id': '2',
        'objectType': 'TableField',
        'visible': true,
        'borderDecoration': 'auto'
      }, { // [2]
        'id': '3',
        'objectType': 'FormField:Custom',
        'visible': true,
        'borderDecoration': 'auto'
      }, { // [3]
        'id': '4',
        'objectType': 'TableField:Custom',
        'enabled': true,
        'visible': true
      }];
      defaultValues.applyTo(testObjects);

      expect(testObjects[0].enabled).toBe(true);
      expect(testObjects[0].visible).toBe(true);
      expect(testObjects[0].borderDecoration).toBe('auto');
      expect(testObjects[1].enabled).toBe(true);
      expect(testObjects[1].visible).toBe(true);
      expect(testObjects[1].borderDecoration).toBe('auto');
      expect(testObjects[2].enabled).toBe(true);
      expect(testObjects[2].visible).toBe(true);
      expect(testObjects[2].borderDecoration).toBe('auto');
      expect(testObjects[3].enabled).toBe(true);
      expect(testObjects[3].visible).toBe(true);
      expect(testObjects[3].borderDecoration).toBe('auto');
    });

    it('copies default values \'by value\'', () => {
      let config = {
        'defaults': {
          'Table': {
            rows: []
          }
        }
      };
      defaultValues.init(config);

      let testObjects = [{
        'id': '1',
        'objectType': 'Table'
      }, {
        'id': '2',
        'objectType': 'Table'
      }, {
        'id': '3',
        'objectType': 'Table',
        'rows': ['three']
      }];
      defaultValues.applyTo(testObjects);

      expect(testObjects[0].rows).toEqual([]);
      expect(testObjects[1].rows).toEqual([]);
      expect(testObjects[2].rows).toEqual(['three']);

      let testRows = testObjects[0].rows;
      testRows.push('one');

      expect(testObjects[0].rows).toEqual(['one']);
      expect(testObjects[1].rows).toEqual([]);
      expect(testObjects[2].rows).toEqual(['three']);
    });

  });

});
