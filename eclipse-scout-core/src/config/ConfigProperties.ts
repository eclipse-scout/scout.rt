/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * Map of config property source system to the config properties from that system.
 * More systems can be added using TypeScript declaration merging.
 */
export interface ConfigProperties {
  main: MainConfigProperties;
}

/**
 * Map for the main config properties: key to value data type.
 * More properties can be added using TypeScript declaration merging.
 */
export interface MainConfigProperties {
  'scout.devMode': boolean;
  'scout.ui.backgroundPollingMaxWaitTime': number;
  'scout.uinotification.waitTimeout': number;
}
