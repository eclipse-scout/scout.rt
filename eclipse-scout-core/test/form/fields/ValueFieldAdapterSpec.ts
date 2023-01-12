/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
