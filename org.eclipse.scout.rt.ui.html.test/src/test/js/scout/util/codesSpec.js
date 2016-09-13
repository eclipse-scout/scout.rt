/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('scout.codes', function() {

  var CODE_TYPE = 12345;
  var CODE = 12346;

  // Some dummy data used to make the tests below work
  beforeEach(function() {
    scout.codes.init({
      12345: {
        id: 12345,
        codes: [
          {
            id: 12346,
            text: {
              'de': 'Foo-de',
              'default': 'Foo-default'
            }
          }
        ]
      }
    });
  });

  it('finds a code type by ID', function() {
    var codeType = scout.codes.codeType(CODE_TYPE);
    expect(codeType instanceof scout.CodeType).toBe(true);
    expect(codeType.id).toEqual(CODE_TYPE);
  });

  it('finds a code by ID (single and two parameter call)', function() {
    var code = scout.codes.get(CODE_TYPE, CODE);
    expect(code instanceof scout.Code).toBe(true);
    var codeRef = CODE_TYPE + ' ' + CODE;
    code = scout.codes.get(codeRef);
    expect(code instanceof scout.Code).toBe(true);
    expect(code.id).toEqual(CODE);
  });

  it('throws an error when code type is not found', function() {
    var func = scout.codes.codeType.bind(scout.codes, 'DoesNotExist');
    expect(func).toThrowError();
  });

  it('throws an error when code is not found', function() {
    var codeType = scout.codes.codeType(CODE_TYPE);
    var func = codeType.get.bind(codeType, 'DoesNotExist');
    expect(func).toThrowError();
  });

});
