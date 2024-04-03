/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, ConfigProperties, DoEntity, objects, System, systems} from '../index';
import $ from 'jquery';

export const config = {
  configMap: new Map<string, Map<string, ConfigProperty<any>>>(),

  /**
   * Adds the {@link ConfigProperty} instances returned by the urls given to the properties map of the given system.
   * @param urls The urls to fetch the properties from. Typically, 'res/config-properties.json' for the UI backend and/or 'api/config-properties' for the location of a ConfigPropertiesResource on the server backend.
   * @param system The optional system name to which the properties belong. By default, {@link System.MAIN_SYSTEM} is used.
   */
  bootstrap(urls: string | string[], system?: string): JQuery.Promise<void> {
    let promises = arrays.ensure(urls)
      .map(url => $.ajaxJson(url)
        .then(properties => config.init(properties, system)));
    return $.promiseAll(promises);
  },

  /**
   * Loads config properties from the given system using the configuration of that {@link System}.
   * @param system The optional system name to which the properties belong. By default, {@link System.MAIN_SYSTEM} is used.
   */
  bootstrapSystem(system?: string): JQuery.Promise<void> {
    const urls = systems.getOrCreate(system).getConfigEndpointUrls();
    return config.bootstrap(urls, system);
  },

  /**
   * Adds the {@link ConfigProperty} instances given to the system given. If a property with the same key already exists, this method does nothing.
   * @param data The properties to add.
   * @param system The optional system name to which the properties belong. By default, {@link System.MAIN_SYSTEM} is used.
   */
  init(data: ConfigProperty<any> | ConfigProperty<any>[], system?: string) {
    arrays.ensure(data).forEach(property => config._init(property, system));
  },

  /**
   * @internal Use {@link #init} instead.
   */
  _init(property: ConfigProperty<any>, system?: string) {
    if (!property?.key) {
      return; // property key is required
    }
    let existingProperty = config._getSystemMap(system)?.get(property.key);
    if (existingProperty) {
      if (!objects.equalsRecursive(existingProperty.value, property.value)) {
        $.log.info(`Already existing config property '${existingProperty.key}' with existing value ${JSON.stringify(existingProperty.value)}.\nIgnoring new value ${JSON.stringify(property.value)}.`);
      }
      return; // keep existing (first) property
    }
    // @ts-expect-error allow properties from the backend that are not declared in TS
    config.set(property.key, property.value, system);
  },

  /**
   * @internal
   */
  _getSystemMap(system?: string): Map<string, ConfigProperty<any>> {
    return config.configMap.get(system || System.MAIN_SYSTEM);
  },

  /**
   * Gets the value of the property with given key. Optionally from a specific backend system.
   *
   * The method only returns values for properties which have already been loaded from the backend.
   * If the property might not already been loaded, use {@link load} instead.
   * @param key The key of the config property whose value should be returned.
   * @param system An optional system from which the property should be. By default, {@link System.MAIN_SYSTEM} is used.
   */
  get<TKey extends keyof ConfigProperties[TSystem] & string, TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, system?: TSystem): ConfigProperties[TSystem][TKey] {
    return config._getSystemMap(system)?.get(key)?.value;
  },

  /**
   * Adds a property. If there is already an existing property with the same key, its value is overwritten.
   * @param key The key of the property.
   * @param value The new value of the property.
   * @param system An optional system to which the property belongs. By default, {@link System.MAIN_SYSTEM} is used.
   */
  set<TKey extends keyof ConfigProperties[TSystem] & string, TValue extends ConfigProperties[TSystem][TKey], TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, value: TValue, system?: TSystem): ConfigProperty<TValue> {
    let systemPropertyMap = config._getSystemMap(system);
    if (!systemPropertyMap) {
      systemPropertyMap = new Map<string, ConfigProperty<any>>();
      config.configMap.set(system || System.MAIN_SYSTEM, systemPropertyMap);
    }
    let property = {key, value};
    systemPropertyMap.set(property.key, property);
    return property;
  },

  /**
   * Loads the properties from the given system and returns the property with the given key.
   *
   * To configure the system URL use {@link systems.getOrCreate} and {@link System.setEndpointUrl} for endpointName 'config-properties' if required.
   * @param key The key of the property.
   * @param system An optional system to which the property belongs. By default, {@link System.MAIN_SYSTEM} is used.
   */
  load<TKey extends keyof ConfigProperties[TSystem] & string, TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, system?: TSystem): JQuery.Promise<ConfigProperties[TSystem][TKey]> {
    return config.bootstrapSystem(system)
      .then(() => config.get(key, system));
  }
};

export interface ConfigProperty<TValue> extends DoEntity {
  key: string;
  value: TValue;
}
