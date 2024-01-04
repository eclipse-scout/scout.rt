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

describe('codes', () => {

  let CODE_TYPE = '12345';
  let CODE = 12346;

  // Some dummy data used to make the tests below work
  beforeEach(() => {
    codes.init({
      12345: {
        id: '12345',
        objectType: CodeType,
        codes: [
          {
            id: 12346,
            objectType: Code,
            texts: {
              'de': 'Foo-de',
              'en': 'Foo-en'
            }
          }
        ]
      }
    });
  });

  it('can init without data', () => {
    let emptyRegistry = {};
    codes.registry = emptyRegistry;
    codes.init();
    expect(codes.registry).toBe(emptyRegistry);
  });

  it('finds a code type by ID', () => {
    let codeType = codes.codeType(CODE_TYPE);
    expect(codeType instanceof CodeType).toBe(true);
    expect(codeType.id).toEqual(CODE_TYPE);
  });

  it('finds a code by ID', () => {
    let code = codes.get(CODE_TYPE, CODE);
    expect(code instanceof Code).toBe(true);
    expect(code.id).toEqual(CODE);
  });

  it('throws an error when code type is not found', () => {
    let func = codes.codeType.bind(codes, 'DoesNotExist');
    expect(func).toThrowError();
  });

  it('throws an error when code is not found', () => {
    let codeType = codes.codeType(CODE_TYPE);
    let func = codeType.get.bind(codeType, 'DoesNotExist');
    expect(func).toThrowError();
  });

  describe('optGet', () => {

    it('should work as get if code exists', () => {
      let code = codes.optGet(CODE_TYPE, CODE);
      expect(code instanceof Code).toBe(true);
    });

    it('should return null if code does not exist', () => {
      let code = codes.optGet(CODE_TYPE, 'DoesNotExist');
      expect(code).toBe(undefined);
    });

  });

  describe('add', () => {
    it('adds a code type or an array of code types', () => {
      let codeType = {
        id: 'codeType.123',
        objectType: CodeType,
        codes: [{
          id: 'code.123',
          objectType: Code,
          text: 'a text'
        }]
      };
      expect(codes.registry['codeType.123']).toBeUndefined();

      codes.add(codeType);
      expect(codes.codeType('codeType.123').id).toBe('codeType.123');
      expect(codes.codeType('codeType.123').get('code.123').id).toBe('code.123');

      // cleanup
      delete codes.registry['codeType.123'];
    });
  });
});
