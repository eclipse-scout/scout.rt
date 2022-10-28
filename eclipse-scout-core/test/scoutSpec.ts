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
import {Menu, NullWidget, ObjectFactory, objects, scout, Session, Status, Tooltip} from '../src/index';

describe('main', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('nvl', () => {

    it('can return alternative value', () => {
      expect(scout.nvl()).toBe(undefined);
      expect(scout.nvl('X')).toBe('X');
      expect(scout.nvl('X', 'Y')).toBe('X');
      expect(scout.nvl(undefined)).toBe(undefined);
      expect(scout.nvl(undefined, undefined)).toBe(undefined);
      expect(scout.nvl(undefined, null)).toBe(null);
      expect(scout.nvl(undefined, '')).toBe('');
      expect(scout.nvl(undefined, 'X')).toBe('X');
      expect(scout.nvl(null, 'X')).toBe('X');
      expect(scout.nvl(null, '')).toBe('');
      expect(scout.nvl(null, undefined)).toBe(undefined);
      expect(scout.nvl(null, null)).toBe(null);
      expect(scout.nvl(null)).toBe(null);
      expect(scout.nvl(0, '123')).toBe(0);
      expect(scout.nvl(1, '123')).toBe(1);
      expect(scout.nvl(undefined, '123')).toBe('123');
      expect(scout.nvl(undefined, 123)).toBe(123);
      expect(scout.nvl(0.000000000000000000000001, -1)).toBe(0.000000000000000000000001);
      expect(scout.nvl({}, {x: 2})).toEqual({});
      expect(scout.nvl({y: undefined}, {x: 2})).toEqual({y: undefined});
      expect(scout.nvl(null, {x: 2})).toEqual({x: 2});
      expect(scout.nvl(null, undefined, '3', '4', null)).toBe('3');
    });

  });

  describe('assertParameter', () => {

    it('throws Error when value is not set', () => {
      let foo = null;
      let func = scout.assertParameter.bind(scout, 'foo', foo);
      expect(func).toThrowError();
      foo = 'bar';
      func = scout.assertParameter.bind(scout, 'foo', foo);
      expect(func).not.toThrowError();
      foo = undefined;
      func = scout.assertParameter.bind(scout, 'foo', foo);
      expect(func).toThrowError();
      foo = false;
      func = scout.assertParameter.bind(scout, 'foo', foo);
      expect(func).not.toThrowError();
      foo = 0;
      func = scout.assertParameter.bind(scout, 'foo', foo);
      expect(func).not.toThrowError();
    });

    it('throws Error when value has wrong type', () => {
      let foo = {};
      let func = scout.assertParameter.bind(scout, 'foo', foo, Status);
      expect(func).toThrowError();
      foo = new Status();
      func = scout.assertParameter.bind(scout, 'foo', foo, Status);
      expect(func).not.toThrowError();
    });

  });

  describe('assertValue', () => {

    it('throws Error when value is not set', () => {
      // @ts-ignore
      expect(() => scout.assertValue()).toThrowError();
      expect(() => scout.assertValue(null)).toThrowError();
      expect(() => scout.assertValue(undefined)).toThrowError();

      const arr = [];
      const obj = {};
      expect(scout.assertValue('null')).toBe('null');
      expect(scout.assertValue(0)).toBe(0);
      expect(scout.assertValue(false)).toBe(false);
      expect(scout.assertValue(arr)).toBe(arr);
      expect(scout.assertValue(obj)).toBe(obj);

      // Check that the given message is thrown
      expect(() => scout.assertValue(null, 'not-good')).toThrowError('not-good');
    });

  });

  describe('assertInstance', () => {

    it('throws Error when value has wrong type', () => {
      // @ts-ignore
      expect(() => scout.assertInstance()).toThrowError();
      expect(() => scout.assertInstance(null, null)).toThrowError();
      expect(() => scout.assertInstance(undefined, null)).toThrowError();
      expect(() => scout.assertInstance(null, Array)).toThrowError();
      expect(() => scout.assertInstance(undefined, Array)).toThrowError();

      const arr = [];
      const obj = {};
      const re = /a+b/;

      // @ts-ignore
      expect(() => scout.assertInstance(re)).toThrowError();
      expect(() => scout.assertInstance(re, null)).toThrowError();
      expect(() => scout.assertInstance(re, undefined)).toThrowError();
      expect(() => scout.assertInstance(re, Array)).toThrowError();
      expect(() => scout.assertInstance(re, Array)).toThrowError();

      expect(scout.assertInstance(arr, Array)).toBe(arr);
      expect(scout.assertInstance(obj, Object)).toBe(obj);
      // @ts-ignore
      expect(scout.assertInstance(re, RegExp)).toBe(re);
      expect(scout.assertInstance(session, Session)).toBe(session);

      // Special behavior for primitive types
      expect(() => scout.assertInstance('123', String)).toThrowError();
      // noinspection JSPrimitiveTypeWrapperUsage
      const str = new String('123'); // eslint-disable-line
      expect(scout.assertInstance(str, String)).toEqual('123');

      // Check that the given message is thrown
      expect(() => scout.assertInstance(obj, Session, 'not-good')).toThrowError('not-good');
    });

  });

  describe('isOneOf', () => {

    it('can check if value is one of multiple values', () => {
      // @ts-ignore
      expect(scout.isOneOf()).toBe(false);
      expect(scout.isOneOf('test')).toBe(false);
      expect(scout.isOneOf('test', 'bla')).toBe(false);
      expect(scout.isOneOf('test', {test: 'test'})).toBe(false);
      expect(scout.isOneOf('test', 'bla', 123, {test: 'test'})).toBe(false);
      expect(scout.isOneOf('test', 'bla', 123, {test: 'test'}, 'test', true)).toBe(true);
      expect(scout.isOneOf('test', 'bla', 123, {test: 'test'}, ['test'], true)).toBe(false);
      expect(scout.isOneOf('test', 'bla', 123, {test: 'test'}, 'Test', true)).toBe(false);
      expect(scout.isOneOf('test', ['bla', 123, {test: 'test'}, 'test', true])).toBe(true);
      expect(scout.isOneOf(123, '123', 123.00000000000001, -123)).toBe(false);
      expect(scout.isOneOf(-123, '123', 123.00000000000001, -123)).toBe(true);
    });

  });

  describe('create', () => {

    it('accepts string as first argument', () => {
      let menu = scout.create(Menu, {
        parent: new NullWidget(),
        session: session
      });
      expect(menu instanceof Menu).toBe(true);
    });

    it('accepts class reference as first argument', () => {
      let menu = scout.create(Menu, {
        parent: new NullWidget(),
        session: session
      });
      expect(menu instanceof Menu).toBe(true);
    });

    it('accepts object with objectType as first argument', () => {
      let menu = scout.create({
        parent: new NullWidget(),
        session: session,
        objectType: Menu
      });
      expect(menu instanceof Menu).toBe(true);
    });

    it('accepts object with objectType as class reference as first argument', () => {
      let menu = scout.create({
        parent: new NullWidget(),
        session: session,
        objectType: Menu
      });
      expect(menu instanceof Menu).toBe(true);
    });

    it('throws when first argument is invalid', () => {
      // must fail
      expect(() => {
        // @ts-ignore
        scout.create(1);
      }).toThrow();
      expect(() => {
        // @ts-ignore
        scout.create();
      }).toThrow();
      expect(() => {
        // @ts-ignore
        scout.create(true);
      }).toThrow();
      expect(() => {
        // @ts-ignore
        scout.create(() => {
          // nop
        });
      }).toThrow();
    });

    it('creates a new initialized widget with parent and session set', () => {
      let parent = new NullWidget();
      let widget = scout.create(Tooltip, {
        parent: parent,
        session: session
      });
      expect(widget).toBeTruthy();
      expect(widget instanceof Tooltip).toBe(true);
      expect(widget.parent).toBe(parent);
      expect(widget.session).toBe(session);
    });

    describe('local object', () => {

      it('sets property \'id\' correctly when no ID is provided', () => {
        let expectedSeqNo = ObjectFactory.get().uniqueIdSeqNo + 1,
          menu = scout.create(Menu, {
            parent: new NullWidget(),
            session: session
          });
        expect(menu.id).toBe('ui' + expectedSeqNo.toString());
        expect(ObjectFactory.get().uniqueIdSeqNo).toBe(expectedSeqNo);
      });

      it('session must be set, but adapter should not be registered', () => {
        let oldNumProperties = objects.countOwnProperties(session.modelAdapterRegistry),
          menu = scout.create(Menu, {
            parent: new NullWidget(),
            session: session
          });
        expect(menu.session === session).toBe(true);
        expect(objects.countOwnProperties(session.modelAdapterRegistry)).toBe(oldNumProperties);
      });
    });
  });
});
