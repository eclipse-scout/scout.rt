/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  ArrayDoNodeSerializer, arrays, BaseDoEntity, Constructor, DataObjectDeserializer, DataObjectDeserializerModel, DataObjectSerializer, DateDoNodeSerializer, DefaultDoTypeResolver, DoEntity, DoNodeSerializer, DoTypeResolver, doValueMetaData,
  InitModelOf, MapDoNodeSerializer, NumberDoNodeSerializer, objects, ObjectType, scout, SetDoNodeSerializer
} from '../index';

/**
 * Helper functions to deal with data objects.
 */
export const dataObjects = {

  _TYPE_VERSION_ATTRIBUTE_NAME: '_typeVersion',
  _TYPE_ATTRIBUTE_NAME: '_type',
  _CONTRIBUTIONS_ATTRIBUTE_NAME: '_contributions',

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
   * Editable array of {@link DoTypeResolver} instances.
   *
   * The resolvers are called when an object is being deserialized by {@link DataObjectDeserializer} according to the order of this list.
   * If a resolver returns a value, the next resolvers won't be called.
   *
   * By default, the list only contains an instance of {@link DefaultDoTypeResolver}.
   *
   * The list may be modified to add a custom resolver. This may be useful for example in the following scenario:
   * Normally, a {@link BaseDoEntity} is created if the data object class cannot be resolved, unless {@link DataObjectDeserializerModel.createPojoIfDoIsUnknown} is set to true.
   * To use a specific {@link BaseDoEntity} class for unknown {@link BaseDoEntity._type} values, a custom resolver can be added.
   */
  doTypeResolvers: [new DefaultDoTypeResolver()] as DoTypeResolver[],

  /**
   * Serializes the given value and converts it to a JSON string using {@link JSON.stringify}.
   *
   * See {@link serialize} for details.
   *
   * Note: Like {@link JSON.stringify}, this method will convert `null` to the string `'null'`. If this is not desired, check the input before calling this method.
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
   * @returns all DO entity contributions as array.
   */
  getContributions<TContributionDo extends DoEntity>(doEntity: DoEntityWithContributions): TContributionDo[] {
    if (!doEntity?._contributions?.length) {
      return [];
    }
    return [...doEntity._contributions] as TContributionDo[];
  },

  /**
   * Adds a new DO entity contribution to the given DO entity.
   * Existing contributions for the same contribution class are replaced. If the contribution is a plain object, existing contributions with the same _type are replaced.
   */
  addContribution(contribution: DoEntity, doEntity: DoEntityWithContributions) {
    if (!doEntity) {
      return;
    }
    scout.assertParameter('contribution', contribution);
    if (objects.isPojo(contribution)) {
      scout.assertProperty(contribution, dataObjects._TYPE_ATTRIBUTE_NAME);
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
  },

  /**
   * Returns the existing DO entity contribution for the given contribution class if available.
   * Otherwise, creates a new DO entity contribution instance, adds it to the contributions and returns it.
   *
   * @param model will be used as initial model if a new instance of the given `contributionClass` needs to be created.
   */
  contribution<TContributionDo extends BaseDoEntity>(contributionClass: Constructor<TContributionDo>, doEntity: DoEntityWithContributions, model?: InitModelOf<TContributionDo>): TContributionDo {
    let contribution = dataObjects.getContribution(contributionClass, doEntity);
    if (!contribution) {
      contribution = scout.create(contributionClass, model);
      dataObjects.addContribution(contribution, doEntity);
    }
    return contribution;
  },

  /**
   * Checks if the given DataObject is empty.
   * Means: all properties have the value `undefined` (`_type`, `_typeVersion` and `_contributions` are always ignored and the DataObject is considered empty even if these internal attributes are set).
   * @returns `true` if all attributes (except `_type`, `_typeVersion` and `_contributions`) are `undefined` or the DataObject is `falsy`.
   */
  isEmpty(dataObject: DoEntity): boolean {
    if (!dataObject) {
      return true; // same as objects.isEmpty does
    }
    return dataObjects.values(dataObject).every(value => value === undefined);
  },

  /**
   * @returns the values (attribute values) of the given DataObject. The values of internal keys (`_type`, `_typeVersion` and `_contributions`) are *NOT* returned. A `falsy` input returns an empty array.
   */
  values(dataObject: DoEntity): any[] {
    if (!dataObject) {
      return [];
    }
    return dataObjects.entries(dataObject).map(([key, value]) => value);
  },

  /**
   * @returns the keys (attribute names) of the given DataObject. Internal keys (`_type`, `_typeVersion` and `_contributions`) are *NOT* returned. A `falsy` input returns an empty array.
   */
  keys(dataObject: DoEntity): string[] {
    if (!dataObject) {
      return [];
    }
    return Object.keys(dataObject).filter(key => key !== dataObjects._TYPE_ATTRIBUTE_NAME && key !== dataObjects._TYPE_VERSION_ATTRIBUTE_NAME && key !== dataObjects._CONTRIBUTIONS_ATTRIBUTE_NAME);
  },

  /**
   * @returns The key-value-pairs of the given DataObject. Internal keys (`_type`, `_typeVersion` and `_contributions`) are *NOT* returned. A `falsy` input returns an empty array.
   */
  entries(dataObject: DoEntity): [string, any][] {
    if (!dataObject) {
      return [];
    }
    return Object.entries(dataObject)
      .filter(([k, v]) => k !== dataObjects._TYPE_ATTRIBUTE_NAME && k !== dataObjects._TYPE_VERSION_ATTRIBUTE_NAME && k !== dataObjects._CONTRIBUTIONS_ATTRIBUTE_NAME);
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
