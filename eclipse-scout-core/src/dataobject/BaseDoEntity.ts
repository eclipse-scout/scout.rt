/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {dataObjects, DeepPartial, DoContributionClassOrType, InitModelOf, objectFactoryHints, objects, ObjectType} from '../index';
import $ from 'jquery';

/**
 * Base class for all data objects.
 */
@objectFactoryHints({ensureObjectType: false})
export class BaseDoEntity {
  declare model: DeepPartial<Omit<this, 'init' | 'getContribution' | 'addContribution' | 'removeContribution' | 'toPojo' | 'clone' | 'equals' | 'model'>>;

  _type?: string;
  _contributions?: BaseDoEntity[];

  init(model: InitModelOf<this>) {
    if (objects.isPojo(model)) {
      const tmpInstance = dataObjects.deserialize(model, this.constructor as ObjectType<this>);
      $.extend(this, tmpInstance);
    }
  }

  /**
   * @see dataObjects.getContribution
   */
  getContribution<TContributionDo extends BaseDoEntity>(contributionClassOrType: DoContributionClassOrType<TContributionDo>): TContributionDo {
    return dataObjects.getContribution(contributionClassOrType, this);
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
   * Note: this extra model is applied to the pojo representation of this data object before a new instance is created. So to modify properties, the structure and types of the properties in their pojo form must be used.
   * @returns A deep clone of this data object instance (compare {@link toPojo}).
   */
  clone(model?: InitModelOf<this> | BaseDoEntity): this {
    const addModel = model instanceof BaseDoEntity ? model.toPojo() : model;
    const thisModel = this.toPojo();
    return dataObjects.deserialize($.extend(true, thisModel, addModel)) as this;
  }

  /**
   * Recursively (deep) compares this data object to the given object.
   * @param obj The object to compare against.
   * @returns true if the two are deeply equal.
   */
  equals(obj: any) {
    return objects.equalsRecursive(this, obj, true /* prevent stackoverflow as this is called in equals */);
  }
}
