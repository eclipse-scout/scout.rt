/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {dataObjects, DoEntity} from '../../src';

describe('dataObjects', () => {

  class DoContrib implements DoEntity {
    _type = 'DoContrib';
    prop: string;
  }

  class AnotherDoContrib implements DoEntity {
    _type = 'AnotherDoContrib';
    prop: string;
  }

  describe('addContribution', () => {
    it('adds a contribution to the DO', () => {
      let doEntity: DoEntityWithContributions = {};
      expect(doEntity._contributions).toBeUndefined();

      let contrib = new DoContrib();
      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      let contrib2 = new AnotherDoContrib();
      dataObjects.addContribution(contrib2, doEntity);
      expect(doEntity._contributions.length).toBe(2);
      expect(doEntity._contributions[0]).toBe(contrib);
      expect(doEntity._contributions[1]).toBe(contrib2);

      dataObjects.addContribution(contrib2, null);
      expect(doEntity._contributions.length).toBe(2);
      expect(() => dataObjects.addContribution(null, doEntity)).toThrow();
    });

    it('replaces an existing contribution with the same class', () => {
      let doEntity: DoEntityWithContributions = {};
      let contrib = new DoContrib();
      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      let contrib2 = new DoContrib();
      dataObjects.addContribution(contrib2, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib2);
    });

    it('uses _type if contribution is a pojo', () => {
      let doEntity: DoEntityWithContributions = {};
      let contrib: DoEntity = {
        _type: 'PojoContrib'
      };
      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      dataObjects.addContribution(contrib, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib);

      // Replaces first
      let contrib2: DoEntity = {
        _type: 'PojoContrib'
      };
      dataObjects.addContribution(contrib2, doEntity);
      expect(doEntity._contributions.length).toBe(1);
      expect(doEntity._contributions[0]).toBe(contrib2);

      let contrib3: DoEntity = {
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
      let doEntity: DoEntityWithContributions = {};
      let contrib = new DoContrib();
      dataObjects.addContribution(contrib, doEntity);

      expect(dataObjects.getContribution(DoContrib, doEntity)).toBe(contrib);
      expect(dataObjects.getContribution(AnotherDoContrib, doEntity)).toBeUndefined();
      expect(dataObjects.getContribution(AnotherDoContrib, null)).toBe(null);
      expect(() => dataObjects.getContribution(null, doEntity)).toThrow();
    });

    it('returns the contribution for the given type', () => {
      let doEntity: DoEntityWithContributions = {};
      let contrib = new DoContrib();
      dataObjects.addContribution(contrib, doEntity);

      expect(dataObjects.getContribution(contrib._type, doEntity)).toBe(contrib);
      expect(dataObjects.getContribution('AnotherDoContrib', doEntity)).toBeUndefined();

      let contrib2: DoEntity = {
        _type: 'PojoContrib2'
      };
      dataObjects.addContribution(contrib2, doEntity);
      expect(dataObjects.getContribution(contrib2._type, doEntity)).toBe(contrib2);
    });
  });

  describe('removeContribution', () => {
    it('removes the contribution for the given class', () => {
      let doEntity: DoEntityWithContributions = {};
      let contrib = new DoContrib();
      let contrib2 = new AnotherDoContrib();
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
      let doEntity: DoEntityWithContributions = {};
      let contrib: DoEntity = {
        _type: 'PojoContrib'
      };
      let contrib2: DoEntity = {
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
});

type DoEntityWithContributions = DoEntity & { _contributions?: DoEntity[] };
