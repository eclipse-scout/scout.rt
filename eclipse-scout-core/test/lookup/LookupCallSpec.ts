/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
