/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, codes, CodeType, CodeTypeModel} from '../../src/index';

describe('codes', () => {

  let CODE_TYPE = '12345';
  let CODE = 12346;
  let origCodeTypeCache;

  // Some dummy data used to make the tests below work
  beforeEach(() => {
    origCodeTypeCache = codes.registry;
    codes.registry = new Map();
    codes.add([{
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
    ]);
  });

  afterEach(() => {
    // cleanup
    codes.registry = origCodeTypeCache;
  });

  it('finds a code type by ID', () => {
    let codeType = codes.get(CODE_TYPE);
    expect(codeType instanceof CodeType).toBe(true);
    expect(codeType.id).toEqual(CODE_TYPE);
  });

  it('finds a code by ID', () => {
    let code = codes.get(CODE_TYPE).get(CODE);
    expect(code instanceof Code).toBe(true);
    expect(code.id).toEqual(CODE);
  });

  it('returns undefined when code type is not found', () => {
    expect(codes.get('DoesNotExist')).toBeUndefined();
  });

  it('returns undefined when code is not found', () => {
    expect(codes.get(CODE_TYPE).get('DoesNotExist')).toBeUndefined();
  });

  describe('add', () => {
    it('adds a code type or an array of code types and removes it afterwards', () => {
      let origSize = codes.registry.size;
      let codeType: CodeTypeModel = {
        id: 'codeType.123',
        codes: [{
          id: 'code.123',
          text: 'a text'
        }]
      };
      expect(codes.get('codeType.123')).toBeUndefined();

      codes.add(codeType);
      expect(codes.get('codeType.123').id).toBe('codeType.123');
      expect(codes.get('codeType.123').get('code.123').id).toBe('code.123');
      expect(codes.registry.size).toBe(origSize + 1);

      // cleanup
      codes.remove('codeType.123');
      expect(codes.registry.size).toBe(origSize);
    });
  });
});
