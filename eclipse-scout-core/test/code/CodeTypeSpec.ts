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
    codes.init({
      codeType0: {
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
            objectType: Code
          }]
        }, {
          id: 'code2',
          objectType: Code
        }]
      }
    });
  });

  describe('init', () => {

    it('creates codes and hierarchy', () => {
      let codeType = codes.codeType('codeType0');
      expect(codeType.codes.length).toBe(7);

      let code0 = codeType.get('code0');
      expect(code0.children.length).toBe(2);
      expect(code0.parent).toBe(undefined);

      let code01 = codeType.get('code01');
      expect(code01.children.length).toBe(0);
      expect(code0.children[0]).toBe(code01);
      expect(code01.parent).toBe(code0);

      let code02 = codeType.get('code02');
      expect(code02.children.length).toBe(1);
      expect(code0.children[1]).toBe(code02);
      expect(code02.parent).toBe(code0);

      let code021 = codeType.get('code021');
      expect(code021.children.length).toBe(0);
      expect(code02.children[0]).toBe(code021);
      expect(code021.parent).toBe(code02);

      let code1 = codeType.get('code1');
      expect(code1.parent).toBe(undefined);
      expect(code1.children.length).toBe(1);

      let code11 = codeType.get('code11');
      expect(code11.children.length).toBe(0);
      expect(code1.children[0]).toBe(code11);
      expect(code11.parent).toBe(code1);

      let code2 = codeType.get('code2');
      expect(code2.parent).toBe(undefined);
      expect(code2.children.length).toBe(0);
    });
  });

  describe('get', () => {
    it('returns code with codeId', () => {
      let codeType = codes.codeType('codeType0');
      let code = codeType.get('code11');
      expect(code.id).toBe('code11');
    });

    it('throws error for unknown codeId', () => {
      let codeType = codes.codeType('codeType0');
      expect(() => {
        codeType.get('code6');
      }).toThrow(new Error('No code found for id=code6'));
    });
  });

  describe('getCodes', () => {
    it('returns all codes', () => {
      let codeType = codes.codeType('codeType0');
      let codeArr = codeType.getCodes();
      expect(codeArr.length).toBe(7);
    });

    it('returns root codes', () => {
      let codeType = codes.codeType('codeType0');
      let codeArr = codeType.getCodes(true);
      expect(codeArr[0]).toBe(codeType.get('code0'));
      expect(codeArr[1]).toBe(codeType.get('code1'));
      expect(codeArr[2]).toBe(codeType.get('code2'));
    });
  });

});
