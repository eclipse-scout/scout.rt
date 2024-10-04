/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Id, IdParser, ObjectType, scout} from '../index';

export abstract class CompositeId extends Id<Id<any>[], 'scout.CompositeId'> {

  constructor() {
    super();
    this.typeName = 'scout.CompositeId';
  }

  protected override _initIdValue(value: Id<any>[] | string[]) {
    const idParser = scout.create(IdParser);
    const componentTypes = this._getComponentTypes();
    if (componentTypes.length !== value.length) {
      throw new Error(`value components size of '${value.length}' does not match the expected number of components ${componentTypes.length}.`);
    }
    this.value = value.map((rawId, index) => this._initIdComponent(rawId, index, componentTypes[index], idParser));
  }

  protected _initIdComponent(rawId: Id<any> | string, index: number, componentType: CompositeIdComponentType, idParser: IdParser): Id<any> {
    if (!rawId) {
      return null;
    }
    if (rawId instanceof Id) {
      return rawId;
    }
    scout.assertParameter('idObjectType', componentType?.idObjectType);
    return idParser.fromUnQualified(componentType.idObjectType, rawId, componentType.typeName);
  }

  protected override _toString(): string {
    if (!this.value?.length) {
      return '';
    }
    // cannot use strings.join here as null/undefined segments must be added as well
    const idToString = id => id ? id._toString() : '';
    let s = idToString(this.value[0]);
    for (let i = 1; i < this.value.length; i++) {
      s += IdParser.COMPONENT_DELIMITER;
      s += idToString(this.value[i]);
    }
    return s;
  }

  protected abstract _getComponentTypes(): CompositeIdComponentType[];
}

export type CompositeIdComponentType = { idObjectType: ObjectType<Id<any>>; typeName?: string };
