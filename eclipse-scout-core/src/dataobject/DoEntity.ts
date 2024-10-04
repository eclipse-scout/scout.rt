/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Constructor, objects, ObjectType, ObjectWithType, scout} from '../index';

/**
 * Base interface for all data objects.
 *
 * @see "org.eclipse.scout.rt.dataobject.IDoEntity"
 */
export interface DoEntity {
  _type?: string;
  _typeVersion?: string;
}

export class BaseDoEntity implements ObjectWithType, DoEntity, BaseDoEntityModel {
  declare model: Partial<this> | BaseDoEntityModel;

  _type?: string;
  objectType: string;

  init(model: any) {
    if (objects.isPojo(model)) {
      Object.keys(model).forEach(key => {
        this[key] = this._revive(model[key]);
      });
    }
  }

  protected _revive(value: any): any {
    if (objects.isPojo(value) && value.objectType) {
      return scout.create(value);
    }
    if (objects.isArray(value)) {
      return value.map(v => this._revive(v));
    }
    return value;
  }
}

export interface BaseDoEntityModel {
  objectType?: ObjectType;

  [property: string]: any; // allow custom properties
}

export function typeName(typeName: string) {
  return <T extends Constructor>(BaseClass: T) => class extends BaseClass {
    constructor(...args: any[]) {
      super(...args);
      Reflect.set(this, '_type', typeName); // instance
    }
  };
}
