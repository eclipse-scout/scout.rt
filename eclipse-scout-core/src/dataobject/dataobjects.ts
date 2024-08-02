/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DoDateSerializer, DoDeserializer, DoEntity, DoSerializer, JsonDeSerializer, ObjectType, scout} from '../index';

export const dataobjects = {

  jsonDeSerializers: [new DoDateSerializer()] as JsonDeSerializer<any>[],

  equals(a: DoEntity, b: DoEntity): boolean {
    // FIXME mvi [js-bookmark] implement
    return false;
  },

  stringify(dataobject: any): string {
    const serialized = dataobjects.serialize(dataobject);
    if (!serialized) {
      return null;
    }
    return JSON.stringify(serialized);
  },

  serialize(dataobject: any): any {
    if (!dataobject) {
      return null;
    }
    return scout.create(DoSerializer).serialize(dataobject);
  },

  parse<T extends DoEntity>(json: string, objectType?: ObjectType<T>): T {
    if (!json) {
      return null;
    }
    const value = JSON.parse(json);
    return dataobjects.deserialize(value, objectType);
  },

  deserialize<T extends DoEntity>(obj: any, objectType?: ObjectType<T>): T {
    if (!obj) {
      return null;
    }
    const deserializer = scout.create(DoDeserializer);
    return deserializer.deserialize(obj, objectType);
  }
};

