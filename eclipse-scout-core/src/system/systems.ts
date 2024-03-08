/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, System} from '../index';

/**
 * Utility to retrieve and create {@link System} instances.
 * A {@link System} represents a REST backend (typically Scout UI or server backend).
 */
export const systems = {
  _systemsMap: new Map<string, System>(),

  /**
   * Retrieves (and creates if not yet existing) the {@link System} with given name.
   * @param name The optional name of the System. If omitted, {@link System.MAIN_SYSTEM} is used.
   * @param baseUrl The base URL of the REST resources of the system. This is the (typically relative) URL under which all the REST endpoints are available. If omitted, 'api' is used as a default.
   */
  getOrCreate(name?: string, baseUrl?: string): System {
    name = name || System.MAIN_SYSTEM;
    let system = systems._systemsMap.get(name);
    if (system) {
      if (baseUrl) {
        system.setBaseUrl(baseUrl);
      }
      return system;
    }

    system = scout.create(System, {
      name,
      baseUrl
    });
    systems._systemsMap.set(name, system);
    return system;
  },

  /**
   * @param name The optional name of the system. If omitted, {@link System.MAIN_SYSTEM} is used.
   * @returns true if a system with given name is already registered.
   */
  exists(name?: string): boolean {
    return systems._systemsMap.has(name || System.MAIN_SYSTEM);
  }
};
