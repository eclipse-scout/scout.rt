/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Id, IdParser, idTypeName, ObjectType, scout} from '../index';

/**
 * Id that is a composition of other Ids.
 */
@idTypeName('scout.CompositeId')
export abstract class CompositeId<TComponentTypes extends Id<any>[], TIdTypeName extends string = 'scout.CompositeId'> extends Id<TComponentTypes, TIdTypeName> {

  protected override _initIdValue(value: (Id<any> | string)[]) {
    const idParser = IdParser.get();
    const componentTypes = this._getComponentTypes();
    if (componentTypes.length !== value.length) {
      throw new Error(`value components size of '${value.length}' does not match the expected number of components ${componentTypes.length}.`);
    }
    this.value = value.map((rawId, index) => this._initIdComponent(rawId, index, componentTypes[index], idParser)) as TComponentTypes;
  }

  protected _initIdComponent(rawId: Id<any> | string, index: number, componentType: CompositeIdComponentType<Id<any>>, idParser: IdParser): Id<any> {
    if (!rawId) {
      return null;
    }
    if (rawId instanceof Id) {
      return rawId;
    }
    scout.assertParameter('idObjectType', componentType?.idObjectType);
    return idParser.fromUnqualified(componentType.idObjectType, rawId, componentType.typeName);
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

  /**
   * Example return value for a composite id with two components (UuId and NumberId):
   * <pre>
   *   return [
   *       {idObjectType: UuId, typeName: 'scout.CompositePart1'},
   *       {idObjectType: NumberId, typeName: 'scout.CompositePart2'}
   *     ];
   * </pre>
   * @retuns the objectType and typeName pairs for each component of this composite id.
   */
  protected abstract _getComponentTypes(): IdArrayToCompositeIdComponentType<TComponentTypes>;
}

export type IdArrayToCompositeIdComponentType<Type extends Id<any>[]> = {
  [IDX in keyof Type]: CompositeIdComponentType<Type[IDX]>;
};

export type CompositeIdComponentType<TId extends Id<any>> = { idObjectType: ObjectType<TId>; typeName?: TId['typeName'] };
