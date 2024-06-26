/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DataObjectSerializer, DoEntity, scout} from '../index';

export const dataobjects = {
  equals(a: DoEntity, b: DoEntity): boolean {
    // FIXME mvi [js-bookmark] implement
    return false;
  },

  stringify(dataobject: DoEntity): string {
    if (!dataobject) {
      return null;
    }
    const serializer = scout.create(DataObjectSerializer);
    return JSON.stringify(dataobject, (key, value) => serializer.serialize(key, value));
  },

  parse(string): DoEntity {
    // FIXME mvi [js-bookmark] implement
    return null;
  }
};
