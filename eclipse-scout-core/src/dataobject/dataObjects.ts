/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AnyDoEntity, ArrayDoNodeSerializer, arrays, BaseDoEntity, Constructor, DataObjectDeserializer, DataObjectDeserializerModel, DataObjectSerializer, DateDoNodeSerializer, DoEntity, DoNodeSerializer, doValueMetaData, MapDoNodeSerializer,
  NumberDoNodeSerializer, objects, ObjectType, scout, SetDoNodeSerializer
} from '../index';

/**
 * Helper functions to deal with data objects.
 */
export const dataObjects = {

  /**
   * Editable array of {@link DoNodeSerializer} instances.
   * May be modified to add custom (de)serialization logic for custom types.
   */
  serializers: [
    new DateDoNodeSerializer(),
    new MapDoNodeSerializer(),
    new SetDoNodeSerializer(),
    new NumberDoNodeSerializer(),
    new ArrayDoNodeSerializer() // must be after Set
  ] as DoNodeSerializer<any>[],

  /**
   * Serializes the given value and converts it to a JSON string using {@link JSON.stringify}.
   *
   * See {@link serialize} for details.
   *
   * @param dataObject The value to serialize. Can be primitives, arrays or objects/classes. Typically, a pojo or data object class extending {@link BaseDoEntity}.
   * @returns the JSON string.
   */
  stringify(dataObject: any): string {
    return JSON.stringify(dataObjects.serialize(dataObject));
  },

  /**
   * Serializes the given value.
   *
   * Note:
   * * {@link Map} is converted to a pojo. This means the Map key must be able to be serialized to an object key (string, number or symbol) and must be unique.
   * * {@link Set} is converted to array.
   * * {@link Date} instances are converted to a string according to {@link dates.toJsonDate}.
   * * Properties with value 'undefined' are not part of the result (skipped).
   *
   * @param dataObject The value to serialize. Can be primitives, arrays or objects/classes. Typically, a pojo or data object class extending {@link BaseDoEntity}.
   * @returns the input serialized. Typically, a pojo. Returns the unaltered input if the input is falsy.
   */
  serialize(dataObject: any): any {
    if (!dataObject) {
      return dataObject;
    }
    return scout.create(DataObjectSerializer).serialize(dataObject);
  },

  /**
   * Parses the given JSON string into a pojo and deserializes it to the data object classes if possible.
   *
   * See {@link deserialize} for details.
   *
   * @param json The JSON string to parse.
   * @param objectType The expected resulting data object.
   * @param deserializerModel Optional configuration object for the DataObjectDeserializer.
   * @returns The deserialized data object instance, if json is defined. Returns undefined if json is undefined. Returns null if json is null or the empty string.
   */
  parse: ((json: string, objectType?: ObjectType<BaseDoEntity | BaseDoEntity[]>, deserializerModel?: DataObjectDeserializerModel) => {
    if (objects.isNullOrUndefined(json)) {
      return json;
    }
    // check empty string
    if (!json) {
      return null;
    }
    const value = JSON.parse(json);
    return dataObjects.deserialize(value, objectType, deserializerModel);
  }) as DoDeserializeFunction<string>,

  /**
   * Deserializes the given object to data object classes if possible.
   *
   * If the data object class cannot be computed for an object (e.g. because the _type value is unknown), a generic {@link BaseDoEntity} instance is created holding all the attributes.
   * If a pojo should be created instead, use {@link DataObjectDeserializerModel.createPojoIfDoIsUnknown}
   *
   * @param obj The pojo to deserialize.
   * @param objectType The expected resulting data object.
   * @param deserializerModel Optional configuration object for the DataObjectDeserializer.
   * @returns The deserialized data object instance. Returns undefined if obj is undefined. Returns null if obj is null or falsy.
   */
  deserialize: ((obj: any, objectType?: ObjectType<BaseDoEntity | BaseDoEntity[]>, deserializerModel?: DataObjectDeserializerModel) => {
    if (objects.isNullOrUndefined(obj)) {
      return obj;
    }
    // falsy values can't be deserialized
    if (!obj) {
      return null;
    }
    // convert string to constructor if possible as the datatype metadata would be on the constructor
    const metaData = doValueMetaData.resolveFieldMetaData(objectType);

    const deserializer = scout.create(DataObjectDeserializer, deserializerModel);
    return deserializer.deserialize(obj, metaData);
  }) as DoDeserializeFunction<any>,

  /**
   * @returns the DO entity contribution for the given contribution class or type.
   */
  getContribution<TContributionDo extends DoEntity>(contributionClassOrType: DoContributionClassOrType<TContributionDo>, doEntity: DoEntityWithContributions): TContributionDo {
    if (!doEntity?._contributions?.length) {
      return null;
    }
    scout.assertParameter('contributionClassOrType', contributionClassOrType);
    return doEntity._contributions.find(getContribPredicate(contributionClassOrType)) as TContributionDo;
  },

  /**
   * Adds a new DO entity contribution to the given DO entity.
   * Existing contributions for the same contribution class are replaced. If the contribution is a plain object, existing contributions with the same _type are replaced.
   */
  addContribution(contribution: AnyDoEntity, doEntity: DoEntityWithContributions) {
    if (!doEntity) {
      return;
    }
    scout.assertParameter('contribution', contribution);
    if (objects.isPojo(contribution)) {
      scout.assertProperty(contribution, '_type');
      dataObjects.removeContribution(contribution._type, doEntity);
    } else {
      // @ts-expect-error
      dataObjects.removeContribution(contribution.constructor, doEntity);
    }
    doEntity._contributions = arrays.ensure(doEntity._contributions);
    doEntity._contributions.push(contribution);
  },

  /**
   * Removes the DO entity contributions whose class or type matches the given contribution class.
   * @returns true if a contribution was removed.
   */
  removeContribution<TContributionDo extends DoEntity>(contributionClassOrType: DoContributionClassOrType<TContributionDo>, doEntity: DoEntityWithContributions): boolean {
    if (!doEntity) {
      return;
    }
    scout.assertParameter('contributionClassOrType', contributionClassOrType);
    const removed = arrays.removeByPredicate(doEntity._contributions, getContribPredicate(contributionClassOrType));
    if (doEntity._contributions?.length === 0) {
      if (doEntity instanceof BaseDoEntity) {
        // Instances of BaseDoEntity have the _contributions attribute set to undefined by default: reset it to the default when no longer used
        (doEntity as any)._contributions = undefined;
      } else {
        // Pojo doesn't have the _contributions attribute by default -> remove it when no longer used
        delete doEntity._contributions;
      }
    }
    return removed;
  }
};

function getContribPredicate(contributionClassOrType: DoContributionClassOrType<DoEntity>): (c: DoEntity) => boolean {
  if (typeof contributionClassOrType === 'string') {
    return contribution => contribution._type === contributionClassOrType;
  }
  return contribution => contribution.constructor === contributionClassOrType;
}

export type DoEntityWithContributions = DoEntity & { _contributions?: DoEntity[] };
export type DoContributionClassOrType<TContributionDo extends DoEntity> = string | Constructor<TContributionDo>;

export interface DoDeserializeFunction<TInput> {
  (obj: TInput): BaseDoEntity;

  <TResultDo extends BaseDoEntity | BaseDoEntity[] = BaseDoEntity>(obj: TInput, objectType: ObjectType<TResultDo>): TResultDo;

  <TConfig extends DataObjectDeserializerModel = DataObjectDeserializerModel>(obj: TInput, objectType: null | undefined, deserializerModel: TConfig): TConfig['createPojoIfDoIsUnknown'] extends true ? any : BaseDoEntity;

  <TResultDo extends BaseDoEntity | BaseDoEntity[] = BaseDoEntity, TConfig extends DataObjectDeserializerModel = DataObjectDeserializerModel>(obj: TInput, objectType: ObjectType<TResultDo>, deserializerModel: TConfig): TResultDo;
}
