/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, NullWidget, NumberField, objects, ObjectUuidProvider, scout, Session, Status, StringField, Tooltip, ValueField, Widget} from '../src/index';
import {FormSpecHelper, SpecObjectUuidProvider} from '../src/testing';

describe('main', () => {
  let session: SandboxSession;

  abstract class Animal {
    readonly name: string;

    abstract canFly(): boolean;

    constructor(name) {
      this.name = name;
    }
  }

  class Bird extends Animal {
    constructor(name = 'Bird') {
      super(name);
    }

    override canFly(): boolean {
      return true;
    }
  }

  class Cat extends Animal {
    constructor(name = 'Cat') {
      super(name);
    }

    override canFly(): boolean {
      return false;
    }

    miauw(): string {
      return 'Miauw!';
    }
  }

  class Tiger extends Cat {
    constructor(name = 'Tiger') {
      super(name);
    }

    override miauw(): string {
      return 'Roar!';
    }
  }

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
      expect(() => scout.assertParameter('foo', foo)).toThrowError();
      foo = 'bar';
      expect(() => scout.assertParameter('foo', foo)).not.toThrowError();
      foo = undefined;
      expect(() => scout.assertParameter('foo', foo)).toThrowError();
      foo = false;
      expect(() => scout.assertParameter('foo', foo)).not.toThrowError();
      foo = 0;
      expect(() => scout.assertParameter('foo', foo)).not.toThrowError();

    });

    it('throws Error when value has wrong type', () => {
      let foo = {};
      expect(() => scout.assertParameter('foo', foo, Status)).toThrowError();
      foo = new Status();
      expect(() => scout.assertParameter('foo', foo, Status)).not.toThrowError();

      // Check that this also works for abstract classes
      let bar: any;

      bar = new Bird();
      expect(() => scout.assertParameter('bar', bar, Animal)).not.toThrowError();
      expect(() => scout.assertParameter('bar', bar, Bird)).not.toThrowError();
      expect(() => scout.assertParameter('bar', bar, Cat)).toThrowError();
      expect(() => scout.assertParameter('bar', bar, Tiger)).toThrowError();
      expect(() => scout.assertParameter('bar', bar, Status)).toThrowError();

      bar = new Cat();
      expect(() => scout.assertParameter('bar', bar, Animal)).not.toThrowError();
      expect(() => scout.assertParameter('bar', bar, Bird)).toThrowError();
      expect(() => scout.assertParameter('bar', bar, Cat)).not.toThrowError();
      expect(() => scout.assertParameter('bar', bar, Tiger)).toThrowError();
      expect(() => scout.assertParameter('bar', bar, Status)).toThrowError();

      bar = new Tiger();
      expect(() => scout.assertParameter('bar', bar, Animal)).not.toThrowError();
      expect(() => scout.assertParameter('bar', bar, Bird)).toThrowError();
      expect(() => scout.assertParameter('bar', bar, Cat)).not.toThrowError();
      expect(() => scout.assertParameter('bar', bar, Tiger)).not.toThrowError();
      expect(() => scout.assertParameter('bar', bar, Status)).toThrowError();

      bar = new Status();
      expect(() => scout.assertParameter('bar', bar, Animal)).toThrowError();
      expect(() => scout.assertParameter('bar', bar, Bird)).toThrowError();
      expect(() => scout.assertParameter('bar', bar, Cat)).toThrowError();
      expect(() => scout.assertParameter('bar', bar, Tiger)).toThrowError();
      expect(() => scout.assertParameter('bar', bar, Status)).not.toThrowError();
    });
  });

  describe('assertProperty', () => {

    it('throws Error when value is not set', () => {
      let o = {
        foo: null,
        bar: 'bar',
        notDefined: undefined,
        notTrue: false,
        zero: 0
      };

      expect(() => scout.assertProperty(null, 'foo')).toThrowError();
      expect(() => scout.assertProperty(o, 'foo')).toThrowError();
      expect(() => scout.assertProperty(o, 'bar')).not.toThrowError();
      expect(() => scout.assertProperty(o, 'notDefined')).toThrowError();
      expect(() => scout.assertProperty(o, 'absent')).toThrowError();
      expect(() => scout.assertProperty(o, 'notTrue')).not.toThrowError();
      expect(() => scout.assertProperty(o, 'zero')).not.toThrowError();
    });

    it('throws Error when value has wrong type', () => {
      let o = {
        bar: new Status(),
        bird: new Bird(),
        cat: new Cat(),
        tiger: new Tiger()
      };

      expect(() => scout.assertProperty(o, 'foo', Status)).toThrowError();
      expect(() => scout.assertProperty(o, 'bar', Status)).not.toThrowError();

      // Check that this also works for abstract classes
      expect(() => scout.assertProperty(o, 'bar', Animal)).toThrowError();
      expect(() => scout.assertProperty(o, 'bar', Bird)).toThrowError();
      expect(() => scout.assertProperty(o, 'bar', Cat)).toThrowError();
      expect(() => scout.assertProperty(o, 'bar', Tiger)).toThrowError();

      expect(() => scout.assertProperty(o, 'bird', Animal)).not.toThrowError();
      expect(() => scout.assertProperty(o, 'bird', Bird)).not.toThrowError();
      expect(() => scout.assertProperty(o, 'bird', Cat)).toThrowError();
      expect(() => scout.assertProperty(o, 'bird', Tiger)).toThrowError();

      expect(() => scout.assertProperty(o, 'cat', Animal)).not.toThrowError();
      expect(() => scout.assertProperty(o, 'cat', Bird)).toThrowError();
      expect(() => scout.assertProperty(o, 'cat', Cat)).not.toThrowError();
      expect(() => scout.assertProperty(o, 'cat', Tiger)).toThrowError();

      expect(() => scout.assertProperty(o, 'tiger', Animal)).not.toThrowError();
      expect(() => scout.assertProperty(o, 'tiger', Bird)).toThrowError();
      expect(() => scout.assertProperty(o, 'tiger', Cat)).not.toThrowError();
      expect(() => scout.assertProperty(o, 'tiger', Tiger)).not.toThrowError();
    });
  });

  describe('assertValue', () => {

    it('throws Error when value is not set', () => {
      // @ts-expect-error
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
      // @ts-expect-error
      expect(() => scout.assertInstance()).toThrowError();
      expect(() => scout.assertInstance(null, null)).toThrowError();
      expect(() => scout.assertInstance(undefined, null)).toThrowError();
      expect(() => scout.assertInstance(null, Array)).toThrowError();
      expect(() => scout.assertInstance(undefined, Array)).toThrowError();

      const arr = [];
      const obj = {};
      const re = /a+b/;

      // @ts-expect-error
      expect(() => scout.assertInstance(re)).toThrowError();
      expect(() => scout.assertInstance(re, null)).toThrowError();
      expect(() => scout.assertInstance(re, undefined)).toThrowError();
      expect(() => scout.assertInstance(re, Array)).toThrowError();
      expect(() => scout.assertInstance(re, Array)).toThrowError();

      expect(scout.assertInstance(arr, Array)).toBe(arr);
      expect(scout.assertInstance(obj, Object)).toBe(obj);
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

    it('narrows to the asserted type', () => {
      // This is mostly a compile-time test

      const helper = new FormSpecHelper(session);
      let stringField = helper.createField(StringField);
      expect(stringField.value).toBe(null);

      let widget = scout.assertInstance(stringField, Widget);
      // @ts-expect-error: Class "Widget" does not know about "value"...
      expect(widget.value).toBe(null);

      let valueField = scout.assertInstance(widget, ValueField);
      // ...but "ValueField" does
      expect(valueField.value).toBe(null);

      expect(valueField).toBe(stringField);
      expect(() => scout.assertInstance(widget, NumberField)).toThrow();

      // Check that this also works for abstract types
      let foo: any = new Bird();
      let a1: Animal = scout.assertInstance(foo, Animal);
      let a2: Bird = scout.assertInstance(foo, Bird);
      // @ts-expect-error
      let a3: Cat = scout.assertInstance(foo, Bird);
      expect(() => scout.assertInstance(foo, Cat)).toThrowError();
      expect(() => scout.assertInstance(foo, Tiger)).toThrowError();

      foo = new Tiger();
      let a4: Animal = scout.assertInstance(foo, Animal);
      let a5: Cat = scout.assertInstance(foo, Cat);
      let a6: Tiger = scout.assertInstance(foo, Tiger);
      let a7: Bird = scout.assertInstance(foo, Cat); // works, because bird has the same structure as cat

      // noinspection BadExpressionStatementJS (only used to disable "unused variable" warnings)
      [a1, a2, a3, a4, a5, a6, a7];
    });
  });

  describe('isOneOf', () => {

    it('can check if value is one of multiple values', () => {
      // @ts-expect-error
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
        // @ts-expect-error
        scout.create(1);
      }).toThrow();
      expect(() => {
        // @ts-expect-error
        scout.create();
      }).toThrow();
      expect(() => {
        // @ts-expect-error
        scout.create(true);
      }).toThrow();
      expect(() => {
        // @ts-expect-error
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
        let expectedSeqNo = SpecObjectUuidProvider.getUniqueIdSeqNo() + 1,
          menu = scout.create(Menu, {
            parent: new NullWidget(),
            session: session
          });
        expect(menu.id).toBe(ObjectUuidProvider.UI_ID_PREFIX + expectedSeqNo.toString());
        expect(SpecObjectUuidProvider.getUniqueIdSeqNo()).toBe(expectedSeqNo);
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
