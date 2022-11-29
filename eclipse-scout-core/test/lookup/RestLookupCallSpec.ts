/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {RestLookupCall, scout} from '../../src/index';

describe('RestLookupCall', () => {

  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  it('applies custom restriction only to the clone', () => {
    const lookupCall = scout.create(RestLookupCall, {
      session: session,
      resourceUrl: 'test-api/dummy',
      maxRowCount: 777
    });
    spyOn(lookupCall, '_call'); // <-- disable the ajax call in this test

    const expectedDefaultRestriction = {
      active: true,
      maxRowCount: lookupCall.maxRowCount
    };

    expect(lookupCall.restriction).toBeNull();
    expect(lookupCall._restriction).toBeNull();

    const cloneAll = lookupCall.cloneForAll();
    expect(cloneAll.restriction).toBeNull();
    expect(cloneAll._restriction).toEqual(expectedDefaultRestriction);
    cloneAll.addRestriction('myProperty', 'someValue');
    cloneAll.execute();
    expect(cloneAll._getRestrictionForAjaxCall()).toEqual({
      ...expectedDefaultRestriction,
      myProperty: 'someValue'
    });

    const cloneText = lookupCall.cloneForText('xyz');
    expect(cloneText.restriction).toBeNull();
    expect(cloneText._restriction).toEqual(expectedDefaultRestriction);
    expect(cloneText.searchText).toBe('xyz'); // not (yet) on the restriction!
    cloneText.addRestriction('myProperty', 'someValue');
    cloneText.execute();
    expect(cloneText._getRestrictionForAjaxCall()).toEqual({
      ...expectedDefaultRestriction,
      text: 'xyz',
      myProperty: 'someValue'
    });

    const cloneRec = lookupCall.cloneForRec(789);
    expect(cloneRec.restriction).toBeNull();
    expect(cloneRec._restriction).toEqual(expectedDefaultRestriction);
    expect(cloneRec.parentKey).toBe(789); // not (yet) on the restriction!
    cloneRec.addRestriction('myProperty', 'someValue');
    // TODO uncomment the lines marked with '<--' when getByRec() is implemented for RestLookupCall.js!
    // cloneRec.execute(); <-- uncomment
    expect(cloneRec._getRestrictionForAjaxCall()).toEqual({
      ...expectedDefaultRestriction,
      // parentKey: 789, <-- uncomment
      myProperty: 'someValue'
    });

    const cloneKey = lookupCall.cloneForKey(123);
    expect(cloneKey.restriction).toBeNull();
    expect(cloneKey._restriction).toBeNull(); // different from all, text and rec
    expect(cloneKey.key).toBe(123); // not (yet) on the restriction!
    cloneKey.addRestriction('myProperty', 'someValue');
    cloneKey.execute();
    expect(cloneKey._getRestrictionForAjaxCall()).toEqual({
      ids: [123],
      myProperty: 'someValue'
    });

    const cloneKeys = lookupCall.cloneForKeys([123, 456]);
    expect(cloneKeys.restriction).toBeNull();
    expect(cloneKeys._restriction).toBeNull(); // different from all, text and rec
    expect(cloneKeys.keys).toEqual([123, 456]); // not (yet) on the restriction!
    cloneKeys.addRestriction('myProperty', 'someValue');
    cloneKeys.execute();
    expect(cloneKeys._getRestrictionForAjaxCall()).toEqual({
      ids: [123, 456],
      myProperty: 'someValue'
    });

    // Check template again (must not have been altered)
    expect(lookupCall.restriction).toBeNull();
    expect(lookupCall._restriction).toBeNull();
  });

  it('applies restrictions in the expected order', () => {
    // This test asserts that the following precedence rules are applies then creating the final restriction object:
    // 1. Restrictions automatically applied to all clones after their creation in the respective cloneFor method.
    // 2. Restrictions predefined in the model property 'restriction', shared by all clones.
    // 3. Restrictions applied to clones programmatically, e.g. during a 'prepareLookupCall' event.
    // 4. Hard-coded properties that are fundamental to the respective queryBy mode (cannot be overridden).

    const lookupCall = scout.create(RestLookupCall, {
      session: session,
      resourceUrl: 'test-api/dummy',
      maxRowCount: 777, // will be overwritten
      active: true, // will be ignored
      myProperty: 'one', // will be ignored
      myIgnoredProperty: 'three', // will be ignored
      restriction: {
        active: false,
        maxRowCount: 888,
        myProperty: 'two',
        myDefaultProperty: 'three'
      }
    });
    spyOn(lookupCall, '_call'); // <-- disable the ajax call in this test

    const expectedModelRestriction = {
      active: false,
      maxRowCount: 888,
      myProperty: 'two',
      myDefaultProperty: 'three'
    };

    expect(lookupCall.restriction).toEqual(expectedModelRestriction);
    expect(lookupCall._restriction).toBeNull();

    const cloneAll = lookupCall.cloneForAll();
    expect(cloneAll.restriction).toEqual(expectedModelRestriction);
    expect(cloneAll._restriction).toBeNull(); // all of the default restriction were already declared in the 'restriction' model object
    cloneAll.addRestriction('myProperty', 'someValue');
    cloneAll.addRestriction('myProperty2', 'someOtherValue');
    cloneAll.execute();
    expect(cloneAll._getRestrictionForAjaxCall()).toEqual({
      ...expectedModelRestriction,
      myProperty: 'someValue',
      myProperty2: 'someOtherValue'
    });

    const cloneText = lookupCall.cloneForText('xyz');
    expect(cloneText.restriction).toEqual(expectedModelRestriction);
    expect(cloneText._restriction).toBeNull(); // all of the default restriction were already declared in the 'restriction' model object
    expect(cloneText.searchText).toBe('xyz'); // not (yet) on the restriction!
    cloneText.addRestriction('myProperty', 'someValue');
    cloneText.addRestriction('myProperty2', 'someOtherValue');
    cloneText.addRestriction('text', 'wrong'); // will be ignored
    cloneText.execute();
    expect(cloneText._getRestrictionForAjaxCall()).toEqual({
      ...expectedModelRestriction,
      text: 'xyz',
      myProperty: 'someValue',
      myProperty2: 'someOtherValue'
    });

    const cloneRec = lookupCall.cloneForRec(789);
    expect(cloneRec.restriction).toEqual(expectedModelRestriction);
    expect(cloneRec._restriction).toBeNull(); // all of the default restriction were already declared in the 'restriction' model object
    expect(cloneRec.parentKey).toBe(789); // not (yet) on the restriction!
    cloneRec.addRestriction('myProperty', 'someValue');
    cloneRec.addRestriction('myProperty2', 'someOtherValue');
    // TODO uncomment the lines marked with '<--' when getByRec() is implemented for RestLookupCall.js!
    // cloneRec.execute(); <-- uncomment
    expect(cloneRec._getRestrictionForAjaxCall()).toEqual({
      ...expectedModelRestriction,
      // parentKey: 789, <-- uncomment
      myProperty: 'someValue',
      myProperty2: 'someOtherValue'
    });

    const cloneKey = lookupCall.cloneForKey(123);
    expect(cloneKey.restriction).toEqual(expectedModelRestriction);
    expect(cloneKey._restriction).toBeNull();
    expect(cloneKey.key).toBe(123); // not (yet) on the restriction!
    cloneKey.addRestriction('myProperty', 'someValue');
    cloneKey.addRestriction('myProperty2', 'someOtherValue');
    cloneKey.addRestriction('ids', [666, 999]); // will be ignored
    cloneKey.execute();
    expect(cloneKey._getRestrictionForAjaxCall()).toEqual({
      ...expectedModelRestriction,
      ids: [123],
      myProperty: 'someValue',
      myProperty2: 'someOtherValue'
    });

    const cloneKeys = lookupCall.cloneForKeys([123, 456]);
    expect(cloneKeys.restriction).toEqual(expectedModelRestriction);
    expect(cloneKeys._restriction).toBeNull();
    expect(cloneKeys.keys).toEqual([123, 456]); // not (yet) on the restriction!
    cloneKeys.addRestriction('myProperty', 'someValue');
    cloneKeys.addRestriction('myProperty2', 'someOtherValue');
    cloneKeys.addRestriction('ids', [666, 999]); // will be ignored
    cloneKeys.execute();
    expect(cloneKeys._getRestrictionForAjaxCall()).toEqual({
      ...expectedModelRestriction,
      ids: [123, 456],
      myProperty: 'someValue',
      myProperty2: 'someOtherValue'
    });

    // Check template again (must not have been altered)
    expect(lookupCall.restriction).toEqual(expectedModelRestriction);
    expect(lookupCall._restriction).toBeNull();
  });

  it('evaluates function restriction values', () => {
    const lookupCall = scout.create(RestLookupCall, {
      session: session,
      resourceUrl: 'test-api/dummy',
      restriction: {
        dyn1: () => 'abc'.toUpperCase()
      }
    });
    spyOn(lookupCall, '_call'); // <-- disable the ajax call in this test

    const expectedDefaultRestriction = {
      active: true,
      maxRowCount: lookupCall.maxRowCount
    };

    const cloneAll = lookupCall.cloneForAll();
    cloneAll.addRestriction('dyn2', () => '123'.split('').reverse().join('-'));
    cloneAll.addRestriction('dyn3', () => null); // will be ignored
    cloneAll.addRestriction('dyn4', () => undefined); // will be ignored
    cloneAll.addRestriction('dyn5', () => 0);
    cloneAll.addRestriction('dyn6', () => false);
    cloneAll.addRestriction('dyn7', () => '');
    cloneAll.execute();
    expect(cloneAll._getRestrictionForAjaxCall()).toEqual({
      ...expectedDefaultRestriction,
      dyn1: 'ABC',
      dyn2: '3-2-1',
      dyn5: 0,
      dyn6: false,
      dyn7: ''
    });
  });

});
