/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupCall, StaticLookupCall} from '../../src';

describe('LookupCall', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('ensure', () => {
    it('does nothing if empty', () => {
      let lookupCall = LookupCall.ensure('', session);
      expect(lookupCall).toBeFalsy();
    });

    it('accepts string', () => {
      let lookupCall = LookupCall.ensure('StaticLookupCall', session);
      expect(lookupCall instanceof StaticLookupCall).toBe(true);
    });

    it('accepts class reference', () => {
      let lookupCall = LookupCall.ensure(StaticLookupCall, session);
      expect(lookupCall instanceof StaticLookupCall).toBe(true);

      let lookupCallGeneric = LookupCall.ensure((StaticLookupCall<number>), session);
      lookupCallGeneric.getByKey(3);
      expect(lookupCallGeneric instanceof StaticLookupCall).toBe(true);
    });

    it('accepts object reference', () => {
      let lookupCall = LookupCall.ensure({
        objectType: StaticLookupCall,
        delay: 5000
      }, session);
      expect(lookupCall instanceof StaticLookupCall).toBe(true);
      expect(lookupCall.delay).toBe(5000);
    });
  });
});
