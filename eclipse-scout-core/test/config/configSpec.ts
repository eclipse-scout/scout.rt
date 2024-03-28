/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {config, ConfigProperties, ConfigProperty, ConfigPropertyCache, System} from '../../src/index';

describe('scout.config', () => {

  let origConfigMap: Map<string, Map<string, ConfigProperty<any>>>;

  class TestingConfigPropertyCache extends ConfigPropertyCache {
    override _handleBootstrapResponse(data?: ConfigProperty<any> | ConfigProperty<any>[], system?: string) {
      super._handleBootstrapResponse(data, system);
    }
  }

  beforeEach(() => {
    origConfigMap = config.getConfigPropertyCache().configMap;
    config.getConfigPropertyCache().configMap = new Map<string, Map<string, ConfigProperty<any>>>();
  });
  afterEach(() => {
    config.getConfigPropertyCache().configMap = origConfigMap;
  });

  describe('_handleBootstrapResponse', () => {
    it('adds properties to the correct system', () => {
      let configPropertyCache = config.getConfigPropertyCache() as TestingConfigPropertyCache;
      configPropertyCache._handleBootstrapResponse([{
        key: 'scout.devMode',
        value: true
      }, {
        key: 'scout.application.version',
        value: '2'
      }]);
      configPropertyCache._handleBootstrapResponse([{
        key: 'scout.devMode',
        value: false
      }, {
        key: 'scout.uinotification.waitTimeout',
        value: 4
      }], 'test');

      let testSystem = 'test' as keyof ConfigProperties;
      expect(config.getConfigPropertyCache().configMap.size).toBe(2);
      expect(config.getConfigPropertyCache().configMap.get('main').size).toBe(2);
      expect(config.get('scout.devMode')).toBeTrue();
      expect(config.get('scout.devMode', testSystem)).toBeFalse();
      expect(config.get('scout.application.version')).toBe('2');
      expect(config.get('scout.uinotification.waitTimeout')).toBeUndefined();
      expect(config.get('scout.uinotification.waitTimeout', testSystem)).toBe(4);
    });

    it('skips properties without key', () => {
      let configPropertyCache = config.getConfigPropertyCache() as TestingConfigPropertyCache;
      // @ts-expect-error
      configPropertyCache._handleBootstrapResponse([{key: null, value: true}, {value: 'test'}, [1], null, {key: 'scout.application.version', value: '11'}]);
      expect(config.getConfigPropertyCache().configMap.size).toBe(1);
      expect(config.getConfigPropertyCache().configMap.get('main').size).toBe(1);
      expect(config.get('scout.application.version')).toBe('11');
    });

    it('overwrites already existing properties', () => {
      let configPropertyCache = config.getConfigPropertyCache() as TestingConfigPropertyCache;
      configPropertyCache._handleBootstrapResponse([{
        key: 'scout.devMode',
        value: true
      }, {
        key: 'scout.application.version',
        value: '2'
      }]);
      configPropertyCache._handleBootstrapResponse([{
        key: 'scout.devMode',
        value: false // overwrites
      }]);
      expect(config.getConfigPropertyCache().configMap.size).toBe(1);
      expect(config.getConfigPropertyCache().configMap.get(System.MAIN_SYSTEM).size).toBe(2);
      expect(config.get('scout.devMode')).toBeFalse();
    });
  });

  describe('get', () => {
    it('returns the correct values', () => {
      config.set('scout.devMode', true);
      config.set('scout.application.version', '2');
      expect(config.getConfigPropertyCache().configMap.size).toBe(1);
      expect(config.getConfigPropertyCache().configMap.get('main').size).toBe(2);
      expect(config.get('scout.devMode')).toBeTrue();
      expect(config.get('scout.uinotification.waitTimeout')).toBeUndefined();
      expect(config.get(null)).toBeUndefined();
      expect(config.get(undefined)).toBeUndefined();
    });
  });

  describe('set', () => {
    it('writes the correct values', () => {
      config.set('scout.devMode', true);
      config.set('scout.application.version', '2');
      let testSystem = 'test' as keyof ConfigProperties;
      config.set('scout.devMode', false); // overwrites
      config.set('scout.devMode', true, testSystem); // creates new property for test system
      config.set('scout.ui.backgroundPollingMaxWaitTime', 44); // creates new property

      expect(config.getConfigPropertyCache().configMap.size).toBe(2);
      expect(config.getConfigPropertyCache().configMap.get('main').size).toBe(3);
      expect(config.get('scout.devMode')).toBeFalse();
      expect(config.get('scout.devMode', testSystem)).toBeTrue();
      expect(config.get('scout.application.version')).toBe('2');
      expect(config.get('scout.ui.backgroundPollingMaxWaitTime')).toBe(44);
    });
  });
});
