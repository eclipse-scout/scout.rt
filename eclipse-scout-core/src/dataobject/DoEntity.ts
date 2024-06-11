/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Constructor, objects, ObjectWithType, scout} from '../index';

/**
 * Base interface for all data object entities.
 *
 * @see "org.eclipse.scout.rt.dataobject.IDoEntity"
 */
export interface DoEntity {
  _type?: string;
  _typeVersion?: string;
}

export class BaseDoEntity implements ObjectWithType, DoEntity {
  declare model: Partial<this>;

  _type?: string;
  objectType: string;

  init(model: any) {
    Object.keys(model).forEach(key => {
      this[key] = this._revive(model[key]);
    });
  }

  protected _revive(value: any): any {
    if (objects.isPlainObject(value) && value.objectType) {
      return scout.create(value);
    }
    if (objects.isArray(value)) {
      return value.map(v => this._revive(v));
    }
    return value;
  }
}

// FIXME mvi [js-bookmark] Do we really want to always add _type to instances? Or only when serializing? Or should DOs only be used to send to the Backend? Then it should be fine?
export function typeName(typeName: string) {
  return <T extends Constructor>(BaseClass: T) => class extends BaseClass {
    constructor(...args: any[]) {
      super(...args);
      // Object.getPrototypeOf(this.constructor)._type = typeName; // static
      Reflect.set(this, '_type', typeName); // instance
    }
  };
}
