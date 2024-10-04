/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, BaseDoEntity, Constructor, ObjectFactory, scout, strings} from '../index';

export class DoRegistry {

  protected static _INSTANCE: DoRegistry = null;
  protected _constructorByJsonType = new Map<string, Constructor<BaseDoEntity>>();
  protected _jsonTypeByDataObject = new Map<string, string>();
  protected _objectTypeByJsonType = new Map<string, string>();

  init() {
    ObjectFactory.get()
      .getClassesInstanceOf(BaseDoEntity)
      .forEach(doClass => this.add(doClass));
  }

  add(DoClass: Constructor<BaseDoEntity>, jsonType?: string) {
    if (!DoClass) {
      return;
    }
    jsonType = jsonType || this._readJsonType(DoClass);
    if (!jsonType) {
      return;
    }

    this._constructorByJsonType.set(jsonType, DoClass);
    const objectType = ObjectFactory.get().getObjectType(DoClass);
    if (objectType) {
      this._jsonTypeByDataObject.set(objectType, jsonType);
      this._objectTypeByJsonType.set(jsonType, objectType);
    }
  }

  removeByClass(DoClass: Constructor<BaseDoEntity>) {
    if (!DoClass) {
      return;
    }
    for (const [key, value] of this._constructorByJsonType) {
      if (value === DoClass) {
        this.removeByJsonType(key);
      }
    }
  }

  removeByJsonType(jsonType: string) {
    if (!jsonType) {
      return;
    }
    this._constructorByJsonType.delete(jsonType);
    const objectType = this._objectTypeByJsonType.get(jsonType);
    if (objectType) {
      this._jsonTypeByDataObject.delete(objectType);
    }
    this._objectTypeByJsonType.delete(jsonType);
  }

  protected _readJsonType(DoClass: Constructor<BaseDoEntity>): string {
    return new DoClass()._type;
  }

  allDataObjects(): IterableIterator<Constructor<BaseDoEntity>> {
    return this._constructorByJsonType.values();
  }

  toConstructor(jsonType: string): Constructor<BaseDoEntity> {
    return this._constructorByJsonType.get(jsonType);
  }

  toJsonType(objectType: string): string {
    objectType = strings.removePrefix(objectType, 'scout.'); // scout elements are in the map without namespace.
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
