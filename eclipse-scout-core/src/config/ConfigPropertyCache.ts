/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, config, ConfigProperties, ConfigProperty, ObjectModel, objects, System} from '../index';
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
   * @see config.bootstrap
   */
  bootstrap(urls: string | string[], system?: string): JQuery.Promise<void> {
    let promises = arrays.ensure(urls)
      .map(url => $.ajaxJson(url)
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
      $.log.warn(`Already existing config property '${existingProperty.key}' with existing value ${JSON.stringify(existingProperty.value)} is overwritten with new value ${JSON.stringify(property.value)}.`);
    }
    // @ts-expect-error allow properties from the backend that are not declared in TS
    this.set(property.key, property.value, system);
  }

  protected _getSystemMap(system?: string): Map<string, ConfigProperty<any>> {
    return this.configMap.get(system || System.MAIN_SYSTEM);
  }

  /**
   * @see config.get
   */
  get<TKey extends keyof ConfigProperties[TSystem] & string, TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, system?: TSystem): ConfigProperties[TSystem][TKey] {
    return this._getSystemMap(system)?.get(key)?.value;
  }

  /**
   * @see config.set
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
   * @see config.load
   */
  load<TKey extends keyof ConfigProperties[TSystem] & string, TSystem extends keyof ConfigProperties & string = 'main'>(key: TKey, system?: TSystem): JQuery.Promise<ConfigProperties[TSystem][TKey]> {
    return config.bootstrapSystem(system) // load/refresh properties from system into this cache
      .then(() => this.get(key, system));
  }
}
