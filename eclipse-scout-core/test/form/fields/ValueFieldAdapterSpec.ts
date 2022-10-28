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
import {Device, ValueFieldAdapter} from '../../../src/index';
import {FormSpecHelper} from '../../../src/testing/index';

describe('ValueFieldAdapter', () => {
  let session: SandboxSession, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  class SpecValueFieldAdapter extends ValueFieldAdapter {
    override _createPropertySortFunc(order: string[]): (a: string, b: string) => number {
      return super._createPropertySortFunc(order);
    }
  }

  describe('_createPropertySortFunc', () => {

    it('should order properties', () => {
      if (!Device.get().supportsInternationalization()) {
        return;
      }
      let order = ['foo', 'baz', 'bar'];
      let properties = ['x', 'bar', 'foo', 'a', 'y', 'baz'];
      let adapter = new SpecValueFieldAdapter();
      let sortFunc = adapter._createPropertySortFunc(order);
      properties.sort(sortFunc);
      let expected = ['foo', 'baz', 'bar', 'a', 'x', 'y'];
      expect(properties).toEqual(expected);
    });
  });

});
