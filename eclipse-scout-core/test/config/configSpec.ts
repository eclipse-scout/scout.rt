/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {config, ConfigProperties, ConfigProperty} from '../../src/index';

describe('scout.config', () => {

  let origConfigMap: Map<string, Map<string, ConfigProperty<any>>>;

  beforeEach(() => {
    origConfigMap = config.configMap;
    config.configMap = new Map<string, Map<string, ConfigProperty<any>>>();
  });
  afterEach(() => {
    config.configMap = origConfigMap;
  });

  describe('init', () => {
    it('adds properties to the correct system', () => {
      config.init([{
        key: 'scout.devMode',
        value: true
      }, {
        key: 'scout.ui.backgroundPollingMaxWaitTime',
        value: 2
      }]);
      config.init([{
        key: 'scout.devMode',
        value: false
      }, {
        key: 'scout.uinotification.waitTimeout',
        value: 4
      }], 'test');

      let testSystem = 'test' as keyof ConfigProperties;
      expect(config.configMap.size).toBe(2);
      expect(config.configMap.get('main').size).toBe(2);
      expect(config.get('scout.devMode')).toBe(true);
      expect(config.get('scout.devMode', testSystem)).toBe(false);
      expect(config.get('scout.ui.backgroundPollingMaxWaitTime')).toBe(2);
      expect(config.get('scout.uinotification.waitTimeout')).toBeUndefined();
      expect(config.get('scout.uinotification.waitTimeout', testSystem)).toBe(4);
    });

    it('skips properties without key', () => {
      // @ts-expect-error
      config.init([{key: null, value: true}, {value: 'test'}, [1], null, {key: 'scout.ui.backgroundPollingMaxWaitTime', value: 11}]);
      expect(config.configMap.size).toBe(1);
      expect(config.configMap.get('main').size).toBe(1);
      expect(config.get('scout.ui.backgroundPollingMaxWaitTime')).toBe(11);
    });

    it('ignores already existing properties', () => {
      config.init([{
        key: 'scout.devMode',
        value: true
      }, {
        key: 'scout.ui.backgroundPollingMaxWaitTime',
        value: 2
      }]);
      config.init([{
        key: 'scout.devMode',
        value: false // is skipped
      }]);
      expect(config.configMap.size).toBe(1);
      expect(config.configMap.get('main').size).toBe(2);
      expect(config.get('scout.devMode')).toBe(true);
    });
  });

  describe('get', () => {
    it('returns the correct values', () => {
      config.init([{
        key: 'scout.devMode',
        value: true
      }, {
        key: 'scout.ui.backgroundPollingMaxWaitTime',
        value: 2
      }]);
      expect(config.configMap.size).toBe(1);
      expect(config.configMap.get('main').size).toBe(2);
      expect(config.get('scout.devMode')).toBe(true);
      expect(config.get('scout.uinotification.waitTimeout')).toBeUndefined();
      expect(config.get(null)).toBeUndefined();
      expect(config.get(undefined)).toBeUndefined();
    });
  });

  describe('set', () => {
    it('writes the correct values', () => {
      config.init([{
        key: 'scout.devMode',
        value: true
      }]);
      let testSystem = 'test' as keyof ConfigProperties;
      config.set('scout.devMode', false); // overwrites
      config.set('scout.devMode', true, testSystem); // creates new property for test system
      config.set('scout.ui.backgroundPollingMaxWaitTime', 44); // creates new property

      expect(config.configMap.size).toBe(2);
      expect(config.configMap.get('main').size).toBe(3);
      expect(config.get('scout.devMode')).toBe(false);
      expect(config.get('scout.devMode', testSystem)).toBe(true);
      expect(config.get('scout.ui.backgroundPollingMaxWaitTime')).toBe(44);
    });
  });
});
