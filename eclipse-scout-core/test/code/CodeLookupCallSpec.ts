/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Code, CodeLookupCall, codes, CodeType, scout} from '../../src/index';

describe('CodeLookupCall', () => {
  let session: SandboxSession, codeType123: CodeType, origCodeTypeCache;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    origCodeTypeCache = codes.registry;
    codes.registry = new Map();
    codeType123 = scout.create(CodeType, {
      id: 'codeType.123',
      codes: [{
        id: 'code.1',
        objectType: Code,
        text: 'code 1'
      }, {
        id: 'code.2',
        objectType: Code,
        text: 'code 2',
        children: [{
          id: 'childcode.2a',
          objectType: Code,
          text: 'child code 2a'
        }, {
          id: 'childcode.2b',
          objectType: Code,
          text: 'child code 2b'
        }]
      }]
    });
  });

  afterEach(() => {
    // cleanup
    codes.registry = origCodeTypeCache;
  });

  function createLookupCall<T>(codeType: CodeType<T>): CodeLookupCall<T> {
    return scout.create(CodeLookupCall, {
      session: session,
      codeType: codeType.id
    }) as CodeLookupCall<T>;
  }

  describe('getByKey', () => {
    beforeEach(() => {
      codes.add(codeType123);
    });

    it('returns a promise which will resolve with a lookup row for the code by key', done => {
      createLookupCall(codeType123).getByKey('code.1')
        .then(result => {
          expect(result.lookupRows[0].key).toBe('code.1');
          expect(result.lookupRows[0].text).toBe('code 1');
        })
        .catch(fail)
        .always(done);
    });

    it('returns a promise which will be rejected if key doesn\' exist', done => {
      createLookupCall(codeType123).getByKey('asdf')
        .then(result => {
          fail('Promise should be rejected but was resolved.');
        })
        .catch(() => {
          expect(true).toBe(true);
          done();
        });
    });
  });

  describe('getByText', () => {
    beforeEach(() => {
      codes.add(codeType123);
    });

    it('returns the lookupRows which match the given text', done => {
      let promise1 = createLookupCall(codeType123).getByText('code')
        .then(result => {
          expect(result.lookupRows.length).toBe(2);
          expect(result.lookupRows[0].key).toBe('code.1');
          expect(result.lookupRows[0].text).toBe('code 1');
          expect(result.lookupRows[1].key).toBe('code.2');
          expect(result.lookupRows[1].text).toBe('code 2');
        })
        .catch(fail);

      let promise2 = createLookupCall(codeType123).getByText('code 2')
        .then(result => {
          expect(result.lookupRows.length).toBe(1);
          expect(result.lookupRows[0].key).toBe('code.2');
          expect(result.lookupRows[0].text).toBe('code 2');
        })
        .catch(fail);

      $.promiseAll([promise1, promise2]).then(done);
    });

    it('returns no lookupRows if no codes match the given text', done => {
      createLookupCall(codeType123).getByText('asdf')
        .then(result => {
          expect(result.lookupRows.length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('getByRec', () => {
    beforeEach(() => {
      codes.add(codeType123);
    });

    it('returns the lookupRows of the children of the given parent key', done => {
      createLookupCall(codeType123).getByRec('code.2')
        .then(result => {
          expect(result.lookupRows.length).toBe(2);
          expect(result.lookupRows[0].key).toBe('childcode.2a');
          expect(result.lookupRows[0].text).toBe('child code 2a');
          expect(result.lookupRows[1].key).toBe('childcode.2b');
          expect(result.lookupRows[1].text).toBe('child code 2b');
        })
        .catch(fail)
        .always(done);
    });

    it('returns no lookupRows if the parent code doesn\'t have children', done => {
      createLookupCall(codeType123).getByRec('code.1')
        .then(result => {
          expect(result.lookupRows.length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });

    it('returns no lookupRows if no codes match the given text', done => {
      createLookupCall(codeType123).getByRec('asdf')
        .then(result => {
          expect(result.lookupRows.length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('getByAll', () => {
    beforeEach(() => {
      codes.add(codeType123);
    });

    it('returns lookupRows for every code', done => {
      createLookupCall(codeType123).getAll()
        .then(result => {
          expect(result.lookupRows.length).toBe(4);
          expect(result.lookupRows[0].key).toBe('code.1');
          expect(result.lookupRows[0].text).toBe('code 1');
          expect(result.lookupRows[0].parentKey).toBe(null);
          expect(result.lookupRows[1].key).toBe('code.2');
          expect(result.lookupRows[1].text).toBe('code 2');
          expect(result.lookupRows[1].parentKey).toBe(null);
          expect(result.lookupRows[2].key).toBe('childcode.2a');
          expect(result.lookupRows[2].text).toBe('child code 2a');
          expect(result.lookupRows[2].parentKey).toBe('code.2');
          expect(result.lookupRows[3].key).toBe('childcode.2b');
          expect(result.lookupRows[3].text).toBe('child code 2b');
          expect(result.lookupRows[3].parentKey).toBe('code.2');
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('codes.remove', () => {

    it('makes, that existing lookup calls don\'t return a result anymore', done => {
      codes.add(codeType123);
      let lookupCall = createLookupCall(codeType123);

      lookupCall.cloneForKey('code.1').execute()
        .then(result => {
          expect(result.lookupRows[0].key).toBe('code.1');
          expect(result.lookupRows[0].text).toBe('code 1');
        })
        .catch(fail)
        .then(() => {
          codes.remove(codeType123);
          return lookupCall.cloneForKey('code.1').execute();
        })
        .then(result => {
          fail('Promise should be rejected but was resolved.');
        })
        .catch(() => {
          done();
        });
    });
  });

  describe('codes.add', () => {

    it('makes, that existing lookups consider the new code type', done => {
      codes.add(codeType123);
      let lookupCall = createLookupCall(codeType123);

      lookupCall.cloneForKey('code.1').execute()
        .then(result => {
          expect(result.lookupRows[0].key).toBe('code.1');
          expect(result.lookupRows[0].text).toBe('code 1');
        })
        .then(() => {
          codes.remove(codeType123);
          codes.add({
            id: 'codeType.123',
            objectType: CodeType,
            codes: [{
              id: 'newcode.1',
              objectType: Code,
              text: 'new code 1'
            }]
          });
          return lookupCall.cloneForKey('newcode.1').execute();
        })
        .then(result => {
          expect(result.lookupRows[0].key).toBe('newcode.1');
          expect(result.lookupRows[0].text).toBe('new code 1');
        })
        .catch(fail)
        .then(() => {
          return lookupCall.cloneForKey('code.1').execute();
        })
        .then(result => {
          // Code.1 does not exist anymore -> has to fail
          fail('Promise should be rejected but was resolved.');
        })
        .catch(() => {
          done();
        });
    });
  });
});
