/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Constructor, DataObjectDeserializer, dataObjects, DataObjectVisitor, dataObjectVisitors, DeepPartial, DoContributionClassOrType, DoEntity, InitModelOf, objectFactoryHints, objects, ObjectType} from '../index';
import $ from 'jquery';

/**
 * Base class for all data objects.
 */
@objectFactoryHints({ensureObjectType: false})
export class BaseDoEntity implements DoEntity {
  declare model: DeepPartial<Omit<this, 'model' | 'init' | 'getContribution' | 'getContributions' | 'addContribution' | 'removeContribution' | 'contribution' | 'toPojo' | 'clone' | 'equals' | 'visit'>>;

  _type?: string;
  _typeVersion?: string;
  _contributions?: BaseDoEntity[];

  init(model: InitModelOf<this>) {
    if (objects.isPojo(model)) {
      const tmpInstance = dataObjects.deserialize(model, this.constructor as ObjectType<this>);
      $.extend(this, tmpInstance);
    } else if (model instanceof BaseDoEntity) {
      $.extend(this, model);
    }
  }

  /**
   * @see dataObjects.getContribution
   */
  getContribution<TContributionDo extends BaseDoEntity>(contributionClassOrType: DoContributionClassOrType<TContributionDo>): TContributionDo {
    return dataObjects.getContribution(contributionClassOrType, this);
  }

  /**
   * @see dataObjects.getContributions
   */
  getContributions<TContributionDo extends BaseDoEntity>(): TContributionDo[] {
    return dataObjects.getContributions(this);
  }

  /**
   * @see dataObjects.addContribution
   */
  addContribution(contribution: BaseDoEntity) {
    dataObjects.addContribution(contribution, this);
  }

  /**
   * @see dataObjects.removeContribution
   */
  removeContribution<TContributionDo extends BaseDoEntity>(contributionClassOrType: DoContributionClassOrType<TContributionDo>): boolean {
    return dataObjects.removeContribution(contributionClassOrType, this);
  }

  /**
   * @see dataObjects.contribution
   */
  contribution<TContributionDo extends BaseDoEntity>(contributionClass: Constructor<TContributionDo>, model?: InitModelOf<TContributionDo>): TContributionDo {
    return dataObjects.contribution(contributionClass, this, model);
  }

  /**
   * Converts this data object instance to a pojo.
   *
   * See {@link dataObjects.serialize} for details.
   *
   * @returns this instance converted to a pojo.
   */
  toPojo(): object {
    return dataObjects.serialize(this);
  }

  /**
   * Creates a deep clone of this data object by serializing and deserializing this instance.
   *
   * In most cases the result will be equal to this instance. However, there are some exceptions:
   * * If you pass an extra model to apply to this function.
   * * If this data object contains Maps having keys that are unique for the Map implementation, but are no longer unique after conversion.
   * One example are {@link Date} instances: While two different instances with the same date value may be added to the Map,
   * it cannot be retained during serialization as it is converted to an object property key (string) which must be unique.
   * * If this data object contains pojo objects (e.g. Record) with properties having the value 'undefined'. These properties will not be part of the result.
   *
   * See {@link dataObjects.serialize} for details.
   *
   * @param model An optional model to apply to the clone.
   * * The values of the model are copied by reference and nested objects are _not_ merged (shallow-copy).
   * * The model is applied to the pojo representation of this data object before a new instance is created. So to modify properties, the structure and types of the properties in their pojo form must be used.
   * @returns A deep clone of this data object instance (compare {@link toPojo}).
   */
  clone(model?: InitModelOf<this> | BaseDoEntity): this {
    const addModel = model instanceof BaseDoEntity ? model.toPojo() : model;
    const thisModel = this.toPojo();
    return dataObjects.deserialize($.extend(thisModel, addModel)) as this;
  }

  /**
   * Recursively (deep) compares this data object to the given object.
   * @param obj The object to compare against.
   * @returns true if the two are deeply equal.
   */
  equals(obj: any) {
    if (obj === this) {
      return true;
    }
    if (!objects.isObject(obj)) {
      return false;
    }
    if (Object.getPrototypeOf(obj) !== Object.getPrototypeOf(this)) {
      return false;
    }

    const propertiesToCompare = new Set(Object.keys(this).concat(Object.keys(obj)));

    // special handling for _typeVersion
    const hasTypeVersion = propertiesToCompare.delete(DataObjectDeserializer._TYPE_VERSION_ATTRIBUTE_NAME);
    if (hasTypeVersion) {
      const otherTypeVersion = obj[DataObjectDeserializer._TYPE_VERSION_ATTRIBUTE_NAME];
      if (otherTypeVersion && this._typeVersion && otherTypeVersion !== this._typeVersion) {
        // must only match if both have a version, otherwise it should be ignored
        return false;
      }
    }

    for (const key of propertiesToCompare) {
      if (!objects.equalsRecursive(obj[key], this[key])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Visits this {@link BaseDoEntity} using {@link dataObjectVisitors.forEachRecWhile}.
   */
  visit<T>(visitor: DataObjectVisitor<T>, type?: Constructor<T>) {
    dataObjectVisitors.forEachRecWhile(this, type, visitor);
  }
}
