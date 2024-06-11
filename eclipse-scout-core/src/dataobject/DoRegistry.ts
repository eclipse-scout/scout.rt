/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, BaseDoEntity, Constructor, ObjectFactory, scout} from '../index';

export class DoRegistry {

  protected static _INSTANCE: DoRegistry = null;
  protected _constructorByJsonType = new Map<string, Constructor<BaseDoEntity>>();
  protected _jsonTypeByDataObject = new Map<string, string>();
  protected _objectTypeByJsonType = new Map<string, string>();

  init() {
    ObjectFactory.get().getClassesInstanceOf(BaseDoEntity).forEach(DoConstructor => {
      const instance = new DoConstructor();
      const _type = instance._type;
      if (_type) {
        this._constructorByJsonType.set(_type, DoConstructor);
        const objectType = ObjectFactory.get().getObjectType(DoConstructor);
        if (objectType) {
          this._jsonTypeByDataObject.set(objectType, _type);
          this._objectTypeByJsonType.set(_type, objectType);
        }
      }
    });
  }

  allDataObjects(): IterableIterator<Constructor<BaseDoEntity>> {
    return this._constructorByJsonType.values();
  }

  toConstructor(jsonType: string): Constructor<BaseDoEntity> {
    return this._constructorByJsonType.get(jsonType);
  }

  toJsonType(objectType: string): string {
    return this._jsonTypeByDataObject.get(objectType);
  }

  toObjectType(jsonType: string): string {
    return this._objectTypeByJsonType.get(jsonType);
  }

  static get(): DoRegistry {
    if (!DoRegistry._INSTANCE) {
      DoRegistry._INSTANCE = scout.create(DoRegistry);
    }
    return DoRegistry._INSTANCE;
  }
}

App.addListener('bootstrap', () => DoRegistry.get().init());
