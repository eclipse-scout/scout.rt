/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {config, ConfigProperties, ConfigProperty, ConfigPropertyCache, ConfigPropertyDo, scout, System} from '../../src/index';

describe('config', () => {

  let origConfigMap: Map<string, Map<string, ConfigProperty<any>>>;

  class TestingConfigPropertyCache extends ConfigPropertyCache {
    override _handleBootstrapResponse(data?: ConfigPropertyDo | ConfigPropertyDo[], system?: string) {
      super._handleBootstrapResponse(data, system);
    }
  }

  beforeEach(() => {
    origConfigMap = config.configMap;
    config.configMap = new Map<string, Map<string, ConfigProperty<any>>>();
  });
  afterEach(() => {
    config.configMap = origConfigMap;
  });

  describe('_handleBootstrapResponse', () => {
    it('adds properties to the correct system', () => {

      let configPropertyCache = config as TestingConfigPropertyCache;
      configPropertyCache._handleBootstrapResponse([
        scout.create(ConfigPropertyDo, {key: 'scout.devMode', value: true}),
        scout.create(ConfigPropertyDo, {key: 'scout.ui.backgroundPollingMaxWaitTime', value: 2})
      ]);
      configPropertyCache._handleBootstrapResponse([
        scout.create(ConfigPropertyDo, {key: 'scout.devMode', value: false}),
        scout.create(ConfigPropertyDo, {key: 'scout.uinotification.waitTimeout', value: 4})
      ], 'test');

      let testSystem = 'test' as keyof ConfigProperties;
      expect(config.configMap.size).toBe(2);
      expect(config.configMap.get('main').size).toBe(2);
      expect(config.get('scout.devMode')?.value).toBeTrue();
      expect(config.get('scout.devMode', testSystem)?.value).toBeFalse();
      expect(config.get('scout.uinotification.waitTimeout')?.value).toBeUndefined();
      expect(config.get('scout.uinotification.waitTimeout', testSystem)?.value).toBe(4);
    });

    it('skips properties without key', () => {
      let configPropertyCache = config as TestingConfigPropertyCache;
      // @ts-expect-error
      configPropertyCache._handleBootstrapResponse([{key: null, value: true}, {value: 'test'}, [1], null, {key: 'scout.ui.backgroundPollingMaxWaitTime', value: 11}]);
      expect(config.configMap.size).toBe(1);
      expect(config.configMap.get('main').size).toBe(1);
      expect(config.get('scout.ui.backgroundPollingMaxWaitTime')?.value).toBe(11);
    });

    it('overwrites already existing properties', () => {
      let configPropertyCache = config as TestingConfigPropertyCache;
      configPropertyCache._handleBootstrapResponse([
        scout.create(ConfigPropertyDo, {key: 'scout.devMode', value: true}),
        scout.create(ConfigPropertyDo, {key: 'scout.ui.backgroundPollingMaxWaitTime', value: 2})
      ]);
      configPropertyCache._handleBootstrapResponse(scout.create(ConfigPropertyDo, {key: 'scout.devMode', value: false /* overwrites */}));
      expect(config.configMap.size).toBe(1);
      expect(config.configMap.get(System.MAIN_SYSTEM).size).toBe(2);
      expect(config.get('scout.devMode')?.value).toBeFalse();
    });
  });

  describe('get', () => {
    it('returns the correct values', () => {
      config.set('scout.devMode', true);
      config.set('scout.ui.backgroundPollingMaxWaitTime', 2);
      expect(config.configMap.size).toBe(1);
      expect(config.configMap.get('main').size).toBe(2);
      expect(config.get('scout.devMode')?.value).toBeTrue();
      expect(config.get('scout.uinotification.waitTimeout')?.value).toBeUndefined();
      expect(config.get(null)).toBeUndefined();
      expect(config.get(undefined)).toBeUndefined();
    });
  });

  describe('set', () => {
    it('writes the correct values', () => {
      config.set('scout.devMode', true);
      let testSystem = 'test' as keyof ConfigProperties;
      config.set('scout.devMode', false); // overwrites
      config.set('scout.devMode', true, testSystem); // creates new property for test system
      config.set('scout.ui.backgroundPollingMaxWaitTime', 44); // creates new property

      expect(config.configMap.size).toBe(2);
      expect(config.configMap.get('main').size).toBe(2);
      expect(config.get('scout.devMode')?.value).toBeFalse();
      expect(config.get('scout.devMode', testSystem)?.value).toBeTrue();
      expect(config.get('scout.ui.backgroundPollingMaxWaitTime')?.value).toBe(44);
    });
  });
});
