/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ConfigProperties, ConfigPropertyCache, DoEntity, scout, systems} from '../index';

let configPropertyCache: ConfigPropertyCache = null;

export const config = {
  /**
   * Load config properties from the given system using the configuration of that {@link System}.
   * @param system The optional system name to which the properties belong. By default, {@link System.MAIN_SYSTEM} is used.
   */
  bootstrapSystem(system?: string): JQuery.Promise<void> {
    const urls = systems.getOrCreate(system).getConfigEndpointUrls();
    return config.bootstrap(urls, system);
  },

  /**
   * Adds the {@link ConfigProperty} instances returned by the urls given to the properties map of the given system.
   * @param urls The urls to fetch the properties from. Typically, 'res/config-properties.json' for the UI backend and/or 'api/config-properties' for the location of a ConfigPropertiesResource on the server backend.
   * @param system The optional system name to which the properties belong. By default, {@link System.MAIN_SYSTEM} is used.
   */
  bootstrap(urls: string | string[], system?: string): JQuery.Promise<void> {
    if (!urls?.length) {
      // no need to create the ConfigProperty cache
      return $.resolvedPromise();
    }
    return config.getConfigPropertyCache().bootstrap(urls);
  },

  /**
   * Gets the value of the property with given key. Optionally from a specific backend system.
   * <p>
   * The method only returns values for properties which have already been loaded from the backend.
   * If the property might not already been loaded, use {@link load} instead.
   * @param key The key of the config property whose value should be returned.
   * @param system An optional system from which the property should be. By default, {@link System.MAIN_SYSTEM} is used.
   */
  get<TKey extends keyof ConfigProperties[TSystem] & string, TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, system?: TSystem): ConfigProperties[TSystem][TKey] {
    return config.getConfigPropertyCache().get(key, system);
  },

  /**
   * Adds a property. If there is already an existing property with the same key, its value is overwritten.
   * @param key The key of the property.
   * @param value The new value of the property.
   * @param system An optional system to which the property belongs. By default, {@link System.MAIN_SYSTEM} is used.
   */
  set<TKey extends keyof ConfigProperties[TSystem] & string, TValue extends ConfigProperties[TSystem][TKey], TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, value: TValue, system?: TSystem): ConfigProperty<TValue> {
    return config.getConfigPropertyCache().set(key, value, system);
  },

  /**
   * Loads the properties from the system given and returns the value of the property with given key.
   * <p>
   * To configure the system URL use {@link systems.getOrCreate} and {@link System.setEndpointUrl} for endpointName 'config-properties' if required.
   * @param key The key of the property.
   * @param system An optional system to which the property belongs. By default, {@link System.MAIN_SYSTEM} is used.
   * @returns a promise that when resolved returns the newly loaded value of the property with the given key.
   */
  load<TKey extends keyof ConfigProperties[TSystem] & string, TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, system?: TSystem): JQuery.Promise<ConfigProperties[TSystem][TKey]> {
    return config.getConfigPropertyCache().load(key, system);
  },

  /**
   * @returns the global {@link ConfigPropertyCache} instance. If required, a new one is created (on first use).
   */
  getConfigPropertyCache(): ConfigPropertyCache {
    if (!configPropertyCache) {
      configPropertyCache = scout.create(ConfigPropertyCache);
    }
    return configPropertyCache;
  }
};

export interface ConfigProperty<TValue> extends DoEntity {
  key: string;
  value: TValue;
}
