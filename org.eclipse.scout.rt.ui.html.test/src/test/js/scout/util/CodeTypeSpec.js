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
describe('CodeType', function() {

  beforeEach(function() {
    scout.codes.init({
      codeType0: {
        id: 'codeType0',
        codes: [{
          id: 'code0',
          children: [{
            id: 'code01'
          }, {
            id: 'code02',
            children: [{
              id: 'code021'
            }]
          }]
        }, {
          id: 'code1',
          children: [{
            id: 'code11'
          }]
        }, {
          id: 'code2'
        }]
      }
    });
  });

  describe('init', function() {

    it('creates codes and hierarchy', function() {
      var codeType = scout.codes.codeType('codeType0');
      expect(codeType.codes.length).toBe(7);

      var code0 = codeType.get('code0');
      expect(code0.childCodes.length).toBe(2);
      expect(code0.parentCode).toBe(undefined);

      var code01 = codeType.get('code01');
      expect(code01.childCodes.length).toBe(0);
      expect(code0.childCodes[0]).toBe(code01);
      expect(code01.parentCode).toBe(code0);

      var code02 = codeType.get('code02');
      expect(code02.childCodes.length).toBe(1);
      expect(code0.childCodes[1]).toBe(code02);
      expect(code02.parentCode).toBe(code0);

      var code021 = codeType.get('code021');
      expect(code021.childCodes.length).toBe(0);
      expect(code02.childCodes[0]).toBe(code021);
      expect(code021.parentCode).toBe(code02);

      var code1 = codeType.get('code1');
      expect(code1.parentCode).toBe(undefined);
      expect(code1.childCodes.length).toBe(1);

      var code11 = codeType.get('code11');
      expect(code11.childCodes.length).toBe(0);
      expect(code1.childCodes[0]).toBe(code11);
      expect(code11.parentCode).toBe(code1);

      var code2 = codeType.get('code2');
      expect(code2.parentCode).toBe(undefined);
      expect(code2.childCodes.length).toBe(0);
    });
  });

  describe('add', function() {
    it('adds new root code to codeType', function() {
      var codeType = scout.codes.codeType('codeType0');
      var code = new scout.Code();
      codeType.add(code);
      expect(code.parentCode).toBe(undefined);
      expect(code.childCodes.length).toBe(0);
    });

    it('adds new child code to codeType', function() {
      var codeType = scout.codes.codeType('codeType0');
      var code2 = codeType.get('code2');
      var childCode = new scout.Code();
      codeType.add(childCode, code2);
      expect(childCode.parentCode).toBe(code2);
      expect(childCode.childCodes.length).toBe(0);
    });
  });

  describe('get', function() {
    it('returns code with codeId', function() {
      var codeType = scout.codes.codeType('codeType0');
      var code = codeType.get('code11');
      expect(code.id).toBe('code11');
    });

    it('throws error for unknown codeId', function() {
      var codeType = scout.codes.codeType('codeType0');
      expect(function(){codeType.get('code6');}).toThrow(new Error('No code found for id=code6'));
    });
  });

  describe('getCodes', function() {
    it('returns all codes', function() {
      var codeType = scout.codes.codeType('codeType0');
      var codes = codeType.getCodes();
      expect(codes.length).toBe(7);
    });

    it('returns root codes', function() {
      var codeType = scout.codes.codeType('codeType0');
      var codes = codeType.getCodes(true);
      expect(codes[0]).toBe(codeType.get('code0'));
      expect(codes[1]).toBe(codeType.get('code1'));
      expect(codes[2]).toBe(codeType.get('code2'));
    });
  });

});
