/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, codes, CodeType} from '../../src/index';

describe('CodeType', () => {

  beforeEach(() => {
    codes.init([{
      objectType: CodeType,
      id: 'codeType0',
      codes: [{
        id: 'code0',
        objectType: Code,
        children: [{
          id: 'code01',
          objectType: Code
        }, {
          id: 'code02',
          objectType: Code,
          children: [{
            id: 'code021',
            objectType: Code
          }]
        }]
      }, {
        id: 'code1',
        objectType: Code,
        children: [{
          id: 'code11',
          objectType: Code,
          fieldName: 'id' // is ignored as the CodeType already has such a property
        }]
      }, {
        id: 'code2',
        objectType: Code,
        active: false,
        enabled: false,
        partitionId: 4,
        sortCode: 3,
        fieldName: 'iconId'
      }]
    }]);
  });

  describe('init', () => {

    it('creates codes and hierarchy', () => {
      let codeType = codes.get('codeType0');
      expect(codeType.codes().length).toBe(7);
      expect(codeType.id).toBe('codeType0'); // fieldName of code1 must not override an already existing CodeType property value
      expect(codeType.maxLevel).toBe(2147483647); // default from Scout Classic
      expect(codeType.hierarchical).toBeFalse();

      let code0 = codeType.get('code0');
      expect(code0.children.length).toBe(2);
      expect(code0.parent).toBeUndefined();
      expect(code0.codeType).toBe(codeType);
      expect(code0.active).toBeTrue();
      expect(code0.enabled).toBeTrue();
      expect(code0.partitionId).toBe(0);
      expect(code0.sortCode).toBe(-1);

      let code01 = codeType.get('code01');
      expect(code01.children.length).toBe(0);
      expect(code0.children[0]).toBe(code01);
      expect(code01.parent).toBe(code0);
      expect(code01.codeType).toBe(codeType);

      let code02 = codeType.get('code02');
      expect(code02.children.length).toBe(1);
      expect(code0.children[1]).toBe(code02);
      expect(code02.parent).toBe(code0);
      expect(code02.codeType).toBe(codeType);

      let code021 = codeType.get('code021');
      expect(code021.children.length).toBe(0);
      expect(code02.children[0]).toBe(code021);
      expect(code021.parent).toBe(code02);
      expect(code021.codeType).toBe(codeType);

      let code1 = codeType.get('code1');
      expect(code1.parent).toBe(undefined);
      expect(code1.children.length).toBe(1);
      expect(code1.codeType).toBe(codeType);

      let code11 = codeType.get('code11');
      expect(code11.children.length).toBe(0);
      expect(code1.children[0]).toBe(code11);
      expect(code11.parent).toBe(code1);
      expect(code11.codeType).toBe(codeType);

      let code2 = codeType.get('code2');
      expect(code2.parent).toBe(undefined);
      expect(code2.children.length).toBe(0);
      expect(code2.codeType).toBe(codeType);
      expect(code2.active).toBeFalse();
      expect(code2.enabled).toBeFalse();
      expect(code2.partitionId).toBe(4);
      expect(code2.sortCode).toBe(3);
      expect(codeType.iconId as any).toBe(code2); // fieldName is used and written, as this field exists and is falsy.
    });
  });

  describe('get', () => {
    it('returns code with codeId', () => {
      let codeType = codes.get('codeType0');
      let code = codeType.get('code11');
      expect(code.id).toBe('code11');
    });

    it('returns undefined for unknown codeId', () => {
      expect(codes.get('codeType0').get('code6')).toBeUndefined();
    });
  });

  describe('codes', () => {
    it('returns all codes', () => {
      let codeType = codes.get('codeType0');
      let codeArr = codeType.codes();
      expect(codeArr.length).toBe(7);
    });

    it('returns root codes', () => {
      let codeType = codes.get('codeType0');
      let codeArr = codeType.codes(true);
      expect(codeArr.length).toBe(3);
      expect(codeArr[0]).toBe(codeType.get('code0'));
      expect(codeArr[1]).toBe(codeType.get('code1'));
      expect(codeArr[2]).toBe(codeType.get('code2'));
    });
  });

});
