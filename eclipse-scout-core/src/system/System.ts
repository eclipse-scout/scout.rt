/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, ObjectModel, ObjectWithType, scout} from '../scout';
import {SomeRequired} from '../types';
import {strings} from '../util/strings';

/**
 * Represents a REST backend system.
 */
export class System implements SystemModel, ObjectWithType {
  declare model: SystemModel;
  declare initModel: SomeRequired<this['model'], 'name'>;
  declare self: System;

  id: string;
  objectType: string;
  name: string;
  baseUrl: string;
  hasUiBackend: boolean;

  protected _endpointUrls: Map<string, string>;

  /**
   * The default system name. It is configured to have a UI backend (see {@link setHasUiBackend}).
   */
  static MAIN_SYSTEM = 'main';

  constructor() {
    this.name = null;
    this.baseUrl = 'api';
    this.hasUiBackend = false;
    this._endpointUrls = new Map();
  }

  init(model: InitModelOf<this>) {
    scout.assertParameter('name', model.name);
    this.name = model.name;
    this.hasUiBackend = scout.nvl(model.hasUiBackend, model.name === System.MAIN_SYSTEM);
    this.baseUrl = scout.nvl(model.baseUrl, this.baseUrl);
    if (model.endpointUrls) {
      for (const [key, value] of Object.entries(model.endpointUrls)) {
        if (value) {
          this._endpointUrls.set(key, value);
        }
      }
    }
  }

  /**
   * Sets a new base URL. This is the (typically relative) URL under which all REST endpoints of this system are available.
   * @param baseUrl The new base URL.
   */
  setBaseUrl(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  /**
   * Specifies if this system has a Scout UI backend.
   */
  setHasUiBackend(hasUiBackend: boolean) {
    this.hasUiBackend = hasUiBackend;
  }

  /**
   * Sets the URL relative to the base URL of this system for a named endpoint.
   * @param endpointName The endpoint identifier for which the relative URL should be updated.
   * @param endpointUrl The new endpoint url relative to the {@link baseUrl} of this system.
   */
  setEndpointUrl(endpointName: string, endpointUrl: string) {
    if (!endpointName) {
      return;
    }
    if (endpointUrl) {
      this._endpointUrls.set(endpointName, endpointUrl);
    } else {
      this._endpointUrls.delete(endpointName);
    }
  }

  /**
   * @returns the endpoints to load config properties.
   */
  getConfigEndpointUrls(): string[] {
    let urls = [];
    let defaultBackendUrl = 'config-properties';
    if (this.hasUiBackend) {
      urls.push('res/config-properties.json');
      defaultBackendUrl = null; // by default only fetch from UI server. Only use both if explicitly requested
    }
    let backendConfigResource = this.getEndpointUrl('config-properties', defaultBackendUrl);
    if (backendConfigResource) {
      urls.push(backendConfigResource);
    }
    return urls;
  }

  /**
   * @returns The full URL including the {@link baseUrl} to the endpoint with given name. The result is always without trailing slash.
   */
  getEndpointUrl(endpointName: string, defaultEndpoint?: string): string {
    let customizedEndpointUrl = this._endpointUrls.get(endpointName);
    let relEndpointUrl = customizedEndpointUrl || defaultEndpoint;
    if (!relEndpointUrl) {
      return null;
    }
    return this._concatPath(this.baseUrl, relEndpointUrl);
  }

  protected _concatPath(a: string, b: string): string {
    a = strings.removeSuffix(a, '/');
    b = strings.removeSuffix(strings.removePrefix(b, '/'), '/');
    return strings.join('/', a, b);
  }
}

export interface SystemModel extends ObjectModel<System> {
  /**
   * The name of the system. Default is {@link System.MAIN_SYSTEM}.
   */
  name?: string;
  /**
   * The base URL of the system. This is the (typically relative) URL under which all REST endpoints of this system are available.
   */
  baseUrl?: string;
  /**
   * Specifies if the system has a Scout UI server running. If {@code true} the {@link System.getConfigEndpointUrls} returns the path to the UI instead (config properties are loaded from the UI).
   */
  hasUiBackend?: boolean;
  /**
   * A map with initial endpoint URLs. The key is the endpoint name, the value the system relative URL.
   */
  endpointUrls?: Record<string, string>;
}
