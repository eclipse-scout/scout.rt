/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AbstractConstructor, BaseDoEntity, Constructor} from '../index';

/**
 * Base interface for data objects. Consider using classes extending {@link BaseDoEntity} instead.
 */
export interface DoEntity {
  _type?: string;
  _typeVersion?: string;
}

/**
 * Base interface for data objects which accept all properties. Consider using classes extending {@link BaseDoEntity} instead.
 */
export interface AnyDoEntity extends DoEntity {
  [property: string]: any; // allow custom properties
}

/**
 * Class decorator function for data objects. It writes the given typeName value to the _type attribute of the data object instance. Is executed before the class constructor.
 * @param typeName The typeName (_type) of the data object.
 */
export function typeName(typeName?: string) {
  return <T extends Constructor | AbstractConstructor>(BaseClass: T) => class extends BaseClass {
    constructor(...args: any[]) {
      super(...args);
      Reflect.set(this, '_type', typeName);
    }
  };
}
