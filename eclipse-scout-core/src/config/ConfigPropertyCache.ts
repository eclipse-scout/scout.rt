/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, arrays, ConfigProperties, DoEntity, ObjectModel, objects, System, systems} from '../index';
import $ from 'jquery';

/**
 * Cache for {@link ConfigProperty} instances by backend system.
 */
export class ConfigPropertyCache implements ObjectModel<ConfigPropertyCache> {

  id: string;
  configMap: Map<string /* system name */, Map<string /* config property key */, ConfigProperty<any>>>;

  constructor() {
    this.configMap = new Map<string, Map<string, ConfigProperty<any>>>();
  }

  /**
   * Loads config properties from the given system using the configuration of that {@link System}.
   * @param system The optional system name to which the properties belong. By default, {@link System.MAIN_SYSTEM} is used.
   */
  bootstrapSystem(system?: string): JQuery.Promise<void> {
    const urls = systems.getOrCreate(system).getConfigEndpointUrls();
    return this.bootstrap(urls, system);
  }

  /**
   * Adds the {@link ConfigProperty} instances returned by the urls given to the properties map of the given system.
   * @param urls The urls to fetch the properties from. Typically, 'res/config-properties.json' for the UI backend and/or 'api/config-properties' for the server backend.
   * @param system The optional system name to which the properties belong. By default, {@link System.MAIN_SYSTEM} is used.
   */
  bootstrap(urls: string | string[], system?: string): JQuery.Promise<void> {
    let promises = arrays.ensure(urls)
      .map(url => $.ajaxJson(url)
        .then(response => App.handleJsonError(url, response))
        .then(properties => this._handleBootstrapResponse(properties, system)));
    return $.promiseAll(promises);
  }

  /**
   * Add the {@link ConfigProperty} instances given to the system given. If a property with the same key already exists, its value is overwritten.
   * @param data The properties to add.
   * @param system The optional system name to which the properties belong. By default, {@link System.MAIN_SYSTEM} is used.
   */
  protected _handleBootstrapResponse(data?: ConfigProperty<any> | ConfigProperty<any>[], system?: string) {
    arrays.ensure(data).forEach(property => this._handleBootstrapProperty(property, system));
  }

  protected _handleBootstrapProperty(property?: ConfigProperty<any>, system?: string) {
    if (!property?.key) {
      return; // property key is required
    }
    let existingProperty = this._getSystemMap(system)?.get(property.key);
    if (existingProperty && !objects.equalsRecursive(existingProperty.value, property.value)) {
      $.log.info(`Already existing config property '${existingProperty.key}' with existing value ${JSON.stringify(existingProperty.value)} is overwritten with new value ${JSON.stringify(property.value)}.`);
    }
    // @ts-expect-error allow properties from the backend that are not declared in TS
    this.set(property.key, property.value, system);
  }

  protected _getSystemMap(system?: string): Map<string, ConfigProperty<any>> {
    return this.configMap.get(system || System.MAIN_SYSTEM);
  }

  /**
   * Gets the {@link ConfigProperty} with given key. Optionally from a specific backend system.
   *
   * The method only returns properties which have already been loaded from the backend.
   * If the property might not already been loaded, use {@link load} instead.
   * @param key The key of the config property that should be returned.
   * @param system An optional system from which the property should be. By default, {@link System.MAIN_SYSTEM} is used.
   */
  get<TKey extends keyof ConfigProperties[TSystem] & string, TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, system?: TSystem): ConfigProperty<ConfigProperties[TSystem][TKey]> {
    return this._getSystemMap(system)?.get(key);
  }

  /**
   * Adds a property. If there is already an existing property with the same key, its value is overwritten.
   * @param key The key of the property.
   * @param value The new value of the property.
   * @param system An optional system to which the property belongs. By default, {@link System.MAIN_SYSTEM} is used.
   * @returns The created {@link ConfigProperty}.
   */
  set<TKey extends keyof ConfigProperties[TSystem] & string, TValue extends ConfigProperties[TSystem][TKey], TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, value: TValue, system?: TSystem): ConfigProperty<TValue> {
    let systemPropertyMap = this._getSystemMap(system);
    if (!systemPropertyMap) {
      systemPropertyMap = new Map<string, ConfigProperty<any>>();
      this.configMap.set(system || System.MAIN_SYSTEM, systemPropertyMap);
    }
    let property = {key, value};
    systemPropertyMap.set(property.key, property);
    return property;
  }

  /**
   * Loads the properties from the given system and returns the value of the property with the given key.
   *
   * To configure the system URL use {@link systems.getOrCreate} and {@link System.setEndpointUrl} for endpointName 'config-properties' if required.
   * @param key The key of the property that should be returned.
   * @param system An optional system from which the property should be loaded. By default, {@link System.MAIN_SYSTEM} is used.
   * @returns a promise that when resolved returns the newly loaded property with the given key.
   */
  load<TKey extends keyof ConfigProperties[TSystem] & string, TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, system?: TSystem): JQuery.Promise<ConfigProperty<ConfigProperties[TSystem][TKey]>> {
    return this.bootstrapSystem(system) // load/refresh properties from system into this cache
      .then(() => this.get(key, system));
  }
}

export interface ConfigProperty<TValue> extends DoEntity {
  key: string;
  value: TValue;
}

export const config: ConfigPropertyCache = objects.createSingletonProxy(ConfigPropertyCache);
