/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DoDeserializer, DoEntity, DoSerializer, ObjectType, scout} from '../index';

export const dataObjects = {

  equals(a: DoEntity, b: DoEntity): boolean {
    // FIXME mvi [js-bookmark] implement
    return false;
  },

  stringify(dataobject: any): string {
    if (!dataobject) {
      return null;
    }
    const serializer = scout.create(DoSerializer);
    return JSON.stringify(dataobject, (key, value) => serializer.serialize(key, value));
  },

  serialize(dataobject: any): any {
    if (!dataobject) {
      return null;
    }
    return scout.create(DoSerializer).serialize('', dataobject);
  },

  parse<T extends DoEntity>(json: string, objectType?: ObjectType<T>): T {
    if (!json) {
      return null;
    }
    const value = JSON.parse(json); // don't use reviver here as it works bottom-up. But here top-down is required.
    const deserializer = scout.create(DoDeserializer);
    return deserializer.deserialize(value, objectType);
  },

  deserialize<T extends DoEntity>(obj: any, objectType?: ObjectType<T>): T {
    if (!obj) {
      return null;
    }
    const deserializer = scout.create(DoDeserializer);
    return deserializer.deserialize(obj, objectType);
  },

  /**
   * @returns the DO entity contribution for the given contribution class or type.
   */
  getContribution<TContributionDo extends DoEntity>(contributionClassOrType: DoContributionClassOrType<TContributionDo>, doEntity: DoEntityWithContributions): TContributionDo {
    if (!doEntity?._contributions?.length) {
      return null;
    }
    scout.assertParameter('contributionClassOrType', contributionClassOrType);
    return doEntity._contributions.find(contrib => predicate(contrib, contributionClassOrType)) as TContributionDo;
  },

  /**
   * Adds a new DO entity contribution to the given DO entity.
   * Existing contributions for the same contribution class are replaced. If the contribution is a plain object, existing contributions with the same type are replaced.
   */
  addContribution(contribution: DoEntity, doEntity: DoEntityWithContributions) {
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
   */
  removeContribution<TContributionDo extends DoEntity>(contributionClassOrType: DoContributionClassOrType<TContributionDo>, doEntity: DoEntityWithContributions) {
    if (!doEntity) {
      return;
    }
    scout.assertParameter('contributionClassOrType', contributionClassOrType);
    arrays.removeByPredicate(doEntity._contributions, contrib => predicate(contrib, contributionClassOrType));
    if (doEntity._contributions?.length === 0) {
      delete doEntity._contributions;
    }
  }
};

function predicate(contribution: DoEntity, contributionClassOrType: DoContributionClassOrType<DoEntity>): boolean {
  if (typeof contributionClassOrType === 'string') {
    return contribution._type === contributionClassOrType;
  }
  return contribution.constructor === contributionClassOrType;
}

type DoEntityWithContributions = DoEntity & { _contributions?: DoEntity[] };
type DoContributionClassOrType<TContributionDo extends DoEntity> = string | (new(...args) => TContributionDo);
