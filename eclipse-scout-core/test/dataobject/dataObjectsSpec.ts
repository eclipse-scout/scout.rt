/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, dataObjects, DoEntity, DoEntityWithContributions, scout, typeName} from '../../src/index';

describe('dataObjects', () => {

  class DoContrib implements DoEntity {
    _type = 'DoContrib';
    prop: string;
  }

  class AnotherDoContrib implements DoEntity {
    _type = 'AnotherDoContrib';
    prop: string;
  }

  @typeName('scout.BaseDoEntityFixture')
  class BaseDoEntityFixtureDo extends BaseDoEntity {
    prop: string;
  }

  @typeName('scout.BaseDoEntityContrib')
  class BaseDoEntityContribDo extends BaseDoEntity {
    contribProp: string;
  }

  @typeName('scout.AnotherBaseDoEntityContrib')
  class AnotherBaseDoEntityContribDo extends BaseDoEntity {
    contribProp2: string;
  }

  describe('addContribution', () => {
    it('adds a contribution to the DO', () => {
      const doEntity: DoEntityWithContributions = {};
      expect(doEntity._contributions).toBeUndefined();

      const contrib = new DoContrib();
      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      const contrib2 = new AnotherDoContrib();
      dataObjects.addContribution(contrib2, doEntity);
      expect(doEntity._contributions.length).toBe(2);
      expect(doEntity._contributions[0]).toBe(contrib);
      expect(doEntity._contributions[1]).toBe(contrib2);

      dataObjects.addContribution(contrib2, null);
      expect(doEntity._contributions.length).toBe(2);
      expect(() => dataObjects.addContribution(null, doEntity)).toThrow();
    });

    it('replaces an existing contribution with the same class', () => {
      const doEntity: DoEntityWithContributions = {};
      const contrib = new DoContrib();
      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      const contrib2 = new DoContrib();
      dataObjects.addContribution(contrib2, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib2);
    });

    it('uses _type if contribution is a pojo', () => {
      const doEntity: DoEntityWithContributions = {};
      const contrib: DoEntity = {
        _type: 'PojoContrib'
      };
      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      // Replaces first
      const contrib2: DoEntity = {
        _type: 'PojoContrib'
      };
      dataObjects.addContribution(contrib2, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib2);

      const contrib3: DoEntity = {
        _type: 'PojoContrib2'
      };
      dataObjects.addContribution(contrib3, doEntity);
      expect(doEntity._contributions.length).toBe(2);
      expect(doEntity._contributions[0]).toBe(contrib2);
      expect(doEntity._contributions[1]).toBe(contrib3);
    });
  });

  describe('getContribution', () => {
    it('returns the contribution for the given class', () => {
      const doEntity: DoEntityWithContributions = {};
      const contrib = new DoContrib();
      dataObjects.addContribution(contrib, doEntity);

      expect(dataObjects.getContribution(DoContrib, doEntity)).toBe(contrib);
      expect(dataObjects.getContribution(AnotherDoContrib, doEntity)).toBeUndefined();
      expect(dataObjects.getContribution(AnotherDoContrib, null)).toBe(null);
      expect(() => dataObjects.getContribution(null, doEntity)).toThrow();
    });

    it('returns the contribution for the given type', () => {
      const doEntity: DoEntityWithContributions = {};
      const contrib = new DoContrib();
      dataObjects.addContribution(contrib, doEntity);

      expect(dataObjects.getContribution(contrib._type, doEntity)).toBe(contrib);
      expect(dataObjects.getContribution('AnotherDoContrib', doEntity)).toBeUndefined();

      const contrib2: DoEntity = {
        _type: 'PojoContrib2'
      };
      dataObjects.addContribution(contrib2, doEntity);
      expect(dataObjects.getContribution(contrib2._type, doEntity)).toBe(contrib2);
    });
  });

  describe('getContributions', () => {
    it('returns all contributions', () => {
      let doEntity: DoEntityWithContributions = {};

      expect(dataObjects.getContributions(doEntity)).toEqual([]);

      let contrib = new DoContrib();
      let contrib2: DoEntity = {
        _type: 'PojoContrib2'
      };
      dataObjects.addContribution(contrib, doEntity);
      dataObjects.addContribution(contrib2, doEntity);

      expect(dataObjects.getContributions(doEntity)).toEqual([contrib, contrib2]);
      expect(dataObjects.getContributions(null)).toEqual([]);
    });
  });

  describe('removeContribution', () => {
    it('removes the contribution for the given class', () => {
      const doEntity: DoEntityWithContributions = {};
      const contrib = new DoContrib();
      const contrib2 = new AnotherDoContrib();
      dataObjects.addContribution(contrib, doEntity);
      dataObjects.addContribution(contrib2, doEntity);
      expect(doEntity._contributions.length).toBe(2);
      expect(doEntity._contributions[0]).toBe(contrib);
      expect(doEntity._contributions[1]).toBe(contrib2);

      dataObjects.removeContribution(DoContrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib2);

      dataObjects.removeContribution(AnotherDoContrib, doEntity);
      expect(doEntity._contributions).toBeUndefined();

      dataObjects.removeContribution(AnotherDoContrib, null);
      expect(doEntity._contributions).toBeUndefined();
      expect(() => dataObjects.removeContribution(null, doEntity)).toThrow();
    });

    it('removes the contribution for the given type', () => {
      const doEntity: DoEntityWithContributions = {};
      const contrib: DoEntity = {
        _type: 'PojoContrib'
      };
      const contrib2: DoEntity = {
        _type: 'PojoContrib2'
      };
      dataObjects.addContribution(contrib, doEntity);
      dataObjects.addContribution(contrib2, doEntity);
      expect(doEntity._contributions.length).toBe(2);
      expect(doEntity._contributions[0]).toBe(contrib);
      expect(doEntity._contributions[1]).toBe(contrib2);

      dataObjects.removeContribution(contrib._type, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib2);

      dataObjects.removeContribution(contrib2._type, doEntity);
      expect(doEntity._contributions).toBeUndefined();
    });
  });

  describe('contribution', () => {
    it('adds a new one or returns the existing contribution', () => {
      const doEntity = scout.create(BaseDoEntityFixtureDo);
      expect(doEntity.getContributions().length).toBe(0);

      const contrib = dataObjects.contribution(BaseDoEntityContribDo, doEntity);
      expect(contrib).toBeInstanceOf(BaseDoEntityContribDo);
      expect(doEntity.getContributions()).toEqual([contrib]);

      const contrib2 = dataObjects.contribution(BaseDoEntityContribDo, doEntity);
      expect(contrib).toBe(contrib2);
      expect(doEntity.getContributions()).toEqual([contrib]);

      const anotherContrib = dataObjects.contribution(AnotherBaseDoEntityContribDo, doEntity, {contribProp2: 'hi'});
      expect(anotherContrib).toBeInstanceOf(AnotherBaseDoEntityContribDo);
      expect(anotherContrib.contribProp2).toBe('hi');
      expect(doEntity.getContributions()).toEqual([contrib, anotherContrib]);

      const anotherContrib2 = dataObjects.contribution(AnotherBaseDoEntityContribDo, doEntity, {contribProp2: 'hello'});
      expect(anotherContrib).toBe(anotherContrib2);
      expect(anotherContrib.contribProp2).toBe('hi');
      expect(doEntity.getContributions()).toEqual([contrib, anotherContrib]);
    });
  });

  describe('stringify', () => {
    it('booleans', () => {
      expect(dataObjects.stringify(true)).toBe('true');
      expect(dataObjects.stringify(false)).toBe('false');
    });
    it('numbers', () => {
      expect(dataObjects.stringify(-1)).toBe('-1');
      expect(dataObjects.stringify(0)).toBe('0');
      expect(dataObjects.stringify(NaN)).toBe('null');
      expect(dataObjects.stringify(Infinity)).toBe('null');
    });
    it('strings', () => {
      expect(dataObjects.stringify('truthy')).toBe('"truthy"');
      expect(dataObjects.stringify('')).toBe('""');
    });
    it('null and undefined', () => {
      expect(dataObjects.stringify(null)).toBe('null');
      expect(dataObjects.stringify(undefined)).toBe(undefined);
    });
  });

  describe('serialize', () => {
    it('booleans', () => {
      expect(dataObjects.serialize(true)).toBe(true);
      expect(dataObjects.serialize(false)).toBe(false);
    });
    it('numbers', () => {
      expect(dataObjects.serialize(-1)).toBe(-1);
      expect(dataObjects.serialize(0)).toBe(0);
      let serializedNan = dataObjects.serialize(NaN);
      // self-comparison to assert NaN as NaN === NaN is always false
      // eslint-disable-next-line no-self-compare
      expect(serializedNan !== undefined && serializedNan !== serializedNan && isNaN(serializedNan)).toBeTrue();
      expect(dataObjects.serialize(Infinity)).toBe(Infinity);
    });
    it('strings', () => {
      expect(dataObjects.serialize('truthy')).toBe('truthy');
      expect(dataObjects.serialize('')).toBe('');
    });
    it('null and undefined', () => {
      expect(dataObjects.serialize(null)).toBe(null);
      expect(dataObjects.serialize(undefined)).toBe(undefined);
    });
  });

  describe('parse', () => {
    it('strings', () => {
      const parsedObject = dataObjects.parse('{' +
        '  "value": 13' +
        '}') as any;
      expect(parsedObject).toBeInstanceOf(BaseDoEntity);
      expect(parsedObject.value).toBe(13);
      expect(dataObjects.parse('')).toBe(null);
    });
    it('null and undefined', () => {
      expect(dataObjects.parse(null)).toBe(null);
      expect(dataObjects.parse(undefined)).toBe(undefined);
    });
  });

  describe('deserialize', () => {
    it('strings', () => {
      let deserializedObject = dataObjects.deserialize({
        value: 13
      }) as any;
      expect(deserializedObject).toBeInstanceOf(BaseDoEntity);
      expect(deserializedObject.value).toBe(13);
      expect(dataObjects.deserialize('')).toEqual(null);
    });
    it('null and undefined', () => {
      expect(dataObjects.deserialize(null)).toBe(null);
      expect(dataObjects.deserialize(undefined)).toBe(undefined);
    });
  });
});
