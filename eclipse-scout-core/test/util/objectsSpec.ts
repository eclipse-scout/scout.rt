/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, arrays, FormField, Menu, objects, scout, Widget} from '../../src/index';

describe('objects', () => {

  describe('copyProperties', () => {

    it('copies all properties', () => {
      let dest: any = {},
        source = {
          foo: 6,
          bar: 7
        };
      objects.copyProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
    });

    it('copies the properties from prototype as well', () => {
      let dest: any = {};
      let TestConstructor = function() {
        this.foo = 6;
      };
      let source = new TestConstructor();
      TestConstructor.prototype.bar = 7;
      source.qux = 8;

      objects.copyProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
      expect(dest.qux).toBe(8);
    });

    it('copies only the properties specified by the filter, if there is one', () => {
      let dest: any = {};
      let TestConstructor = function() {
        this.foo = 6;
        this.xyz = 9;
      };
      let source = new TestConstructor();
      TestConstructor.prototype.bar = 7;
      TestConstructor.prototype.abc = 2;
      source.qux = 8;
      source.baz = 3;

      objects.copyProperties(source, dest, ['foo', 'bar', 'qux']);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
      expect(dest.qux).toBe(8);
      expect(dest.xyz).toBe(undefined);
      expect(dest.abc).toBe(undefined);
      expect(dest.baz).toBe(undefined);
    });

  });

  describe('copyOwnProperties', () => {

    it('copies all properties', () => {
      let dest: any = {},
        source = {
          foo: 6,
          bar: 7
        };
      objects.copyOwnProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
    });

    it('does not copy the properties from prototype', () => {
      let dest: any = {};
      let TestConstructor = function() {
        this.foo = 6;
      };
      TestConstructor.prototype.bar = 7;
      let source = new TestConstructor();
      source.qux = 8;

      objects.copyOwnProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(undefined);
      expect(dest.qux).toBe(8);
    });

    it('copies only the properties specified by the filter, if there is one', () => {
      let dest: any = {},
        source = {
          foo: 6,
          bar: 7,
          another: 8
        };

      objects.copyOwnProperties(source, dest, ['bar', 'another']);
      expect(dest.foo).toBe(undefined);
      expect(dest.bar).toBe(7);
      expect(dest.another).toBe(8);
    });

  });

  describe('countOwnProperties', () => {

    it('counts all own properties', () => {
      let o = {
        first: 1,
        second: 2
      };
      let F = function() {
        this.foo = 66;
        this.bar = 777;
      };
      F['myProp'] = 'hello';
      F.prototype.anotherProp = 'goodbye';
      let x = new F();
      let y = {};
      objects.copyProperties(x, y);
      y['qux'] = 9999;

      expect(objects.countOwnProperties(o)).toBe(2); // first, second
      expect(objects.countOwnProperties(F)).toBe(1); // myProp
      expect(objects.countOwnProperties(x)).toBe(2); // foo, bar (but not myProp or anotherProp)
      expect(objects.countOwnProperties(y)).toBe(4); // foo, bar, anotherProp, qux (because copyProperties also copies properties from prototype)
    });

    it('works for objects created with createMap() function', () => {
      let map = objects.createMap();
      expect(objects.countOwnProperties(map)).toBe(0);
      map = objects.createMap({foo: 1});
      expect(objects.countOwnProperties(map)).toBe(1);
    });

  });

  describe('valueCopy', () => {

    it('copies an object by value', () => {
      let o: any = {
        first: 1,
        second: 2,
        arr: [],
        arr2: [{
          name: 'Hans'
        }, {
          name: 'Linda'
        }],
        hamlet: {
          type: 'Book',
          title: {
            shortTitle: 'Hamlet',
            longTitle: 'The Tragicall Historie of Hamlet, Prince of Denmarke'
          },
          author: 'Shakespeare',
          refs: [{
            type: 'Book',
            author: 'Dickens',
            title: '???'
          }, {
            type: 'Audio',
            author: 'Shakespeare',
            title: 'Hamlet on CD'
          }]
        }
      };
      let o2 = objects.valueCopy(o);
      o.first = 'one';
      o.second = 'two';
      o.arr.push('test');
      o.arr2[0].name = 'Dagobert';
      o.hamlet.author = 'Unknown';
      o.hamlet.title.longTitle = 'NO LONG TITLE';
      o.hamlet.refs.push({});

      expect(o2).not.toBe(o);
      expect(o2.first).toBe(1);
      expect(o2.second).toBe(2);
      expect(o2.arr).toEqual([]);
      expect(o2.arr2[0].name).toBe('Hans');
      expect(o2.hamlet.author).toBe('Shakespeare');
      expect(o2.hamlet.title.longTitle).toBe('The Tragicall Historie of Hamlet, Prince of Denmarke');
      expect(o2.hamlet.refs.length).toBe(2);
    });

    it('works for objects created with createMap() function', () => {
      // Top-level map
      let map = objects.createMap();
      map.name = 'Linda';
      map.book = null;
      map.author = undefined;

      let map2 = objects.valueCopy(map);
      map.name = 'Hans';
      map.total = 444;
      expect(map).not.toBe(map2);
      expect(Object.keys(map).length).toBe(4);
      expect(Object.keys(map2).length).toBe(3);
      expect(map2.name).toBe('Linda');
      expect(map2.book).toBe(null);
      expect(map2.author).toBe(undefined);

      // Nested map
      let o: any = {
        first: 1,
        second: 2
      };
      o.map = objects.createMap();
      o.map['a-b-c'] = 'ABC';

      let o2 = objects.valueCopy(o);
      o.first = 'one';
      o.second = 'two';
      o.map['a-b-c'] = 'GREEN';
      o.map['d-e-f'] = 'BLUE';

      expect(o2).not.toBe(o);
      expect(o2.first).toBe(1);
      expect(o2.second).toBe(2);
      expect(Object.keys(o.map).length).toEqual(2);
      expect(Object.keys(o2.map).length).toEqual(1);
      expect(o2.map['a-b-c']).toEqual('ABC');
    });

  });

  describe('isNumber', () => {
    it('returns true iff argument is a number', () => {
      expect(objects.isNumber(0)).toBe(true);
      expect(objects.isNumber(1)).toBe(true);
      expect(objects.isNumber(1.0)).toBe(true);
      expect(objects.isNumber(-1)).toBe(true);
      expect(objects.isNumber('0x0a')).toBe(true); // valid hex-value
      expect(objects.isNumber(null)).toBe(false); // a number reference could be null

      expect(objects.isNumber(undefined)).toBe(false);
      expect(objects.isNumber('foo')).toBe(false);
      expect(objects.isNumber(false)).toBe(false);
      expect(objects.isNumber('5.3')).toBe(true);
    });
  });

  describe('isString', () => {
    it('returns true iff argument is a string', () => {
      // @ts-expect-error
      expect(objects.isString()).toBe(false);
      expect(objects.isString(null)).toBe(false);
      expect(objects.isString(123)).toBe(false);
      expect(objects.isString(['foo'])).toBe(false);
      expect(objects.isString('foo')).toBe(true);
      expect(objects.isString(String('foo'))).toBe(true);
      expect(objects.isString(String(null))).toBe(true);
      expect(objects.isString(JSON.stringify({a: 1}))).toBe(true);

      /* eslint-disable no-new-wrappers */
      // noinspection JSPrimitiveTypeWrapperUsage
      expect(objects.isString(new String('foo'))).toBe(true); // typeof 'object'
    });
  });

  describe('isArray', () => {
    it('returns true when argument is an array', () => {
      expect(objects.isArray([])).toBe(true);

      expect(objects.isArray(undefined)).toBe(false);
      expect(objects.isArray('foo')).toBe(false);
    });
  });

  describe('isNullOrUndefined', () => {
    it('returns true when argument is null or undefined, but not when 0 or any other value', () => {
      expect(objects.isNullOrUndefined(null)).toBe(true);
      expect(objects.isNullOrUndefined(undefined)).toBe(true);
      expect(objects.isNullOrUndefined(0)).toBe(false);
      expect(objects.isNullOrUndefined('foo')).toBe(false);
    });
  });

  describe('isPromise', () => {
    it('returns false for "not a promise" inputs', () => {
      // @ts-expect-error
      expect(objects.isPromise()).toBeFalse();
      expect(objects.isPromise(null)).toBeFalse();
      expect(objects.isPromise('foo')).toBeFalse();
      expect(objects.isPromise(5)).toBeFalse();
      expect(objects.isPromise(['foo'])).toBeFalse();
      expect(objects.isPromise({foo: 'bar'})).toBeFalse();
    });

    it('returns true for thenables', () => {
      expect(objects.isPromise($.resolvedPromise())).toBeTrue();
      expect(objects.isPromise($.Deferred().promise())).toBeTrue();
      expect(objects.isPromise({
        then() {
          // nop
        }
      })).toBeTrue();
    });
  });

  describe('values', () => {
    it('returns object values', () => {
      let Class = function() {
        this.a = 'A';
        this.b = 'B';
      };
      let o1 = {
        a: 'X',
        b: 'Y',
        c: 'Z'
      };
      let o2 = new Class();
      o2.a = 'X';
      o2.c = 'C';

      // @ts-expect-error
      expect(objects.values()).toEqual([]);
      expect(objects.values(null)).toEqual([]);
      expect(objects.values(undefined)).toEqual([]);
      expect(objects.values({})).toEqual([]);
      expect(objects.values(o1).length).toBe(3);
      expect(objects.values(o2).length).toBe(3);
      expect(objects.values(o1)).toContain('X');
      expect(objects.values(o1)).toContain('Y');
      expect(objects.values(o1)).toContain('Z');
      expect(objects.values(o2)).toContain('X'); // not A
      expect(objects.values(o2)).toContain('B');
      expect(objects.values(o2)).toContain('C');
    });

    it('can handle maps', () => {
      let map1 = objects.createMap();
      let map2 = objects.createMap();
      map2['x'] = 'y'; // jshint ignore:line
      map2[7] = 7;

      expect(objects.values(map1)).toEqual([]);
      expect(objects.values(map2).length).toBe(2);
      expect(objects.values(map2)).toContain('y');
      expect(objects.values(map2)).toContain(7);
    });

    it('createMap with optional properties', () => {
      let the = objects.createMap({world: 1});
      expect(objects.countOwnProperties(the)).toBe(1);
      expect(the.world).toBe(1);
    });

    it('createMap should not have a prototype', () => {
      let map = objects.createMap();
      expect(Object.getPrototypeOf(map)).toBe(null);
    });
  });

  describe('findChildObjectByKey', () => {

    let obj = {
      id: 'root',
      value: '.root',
      main: {
        id: 'main',
        value: '.root.main',
        sub: {
          id: 'subMain',
          value: '.root.main.sub',
          array: [{
            id: 'arrayObj1',
            value: '.root.main.sub.array.obj1'
          }]
        }
      },
      second: {
        id: 'second',
        value: '.root.second'
      },
      array: [{
        array: [{
          id: 'arrayObj2',
          value: '.root.array.array.obj2',
          sub: {
            id: 'arrayObj2sub',
            value: '.root.array.array.obj2.sub'
          }
        }]
      }, {
        id: 'arrayObj3',
        value: '.root.array.obj3'
      }]
    };

    it('find root object', () => {
      let child = objects.findChildObjectByKey(obj, 'id', 'root');
      expect(child.value).toBe('.root');
    });
    it('find object in tree', () => {
      let child = objects.findChildObjectByKey(obj, 'id', 'subMain');
      expect(child.value).toBe('.root.main.sub');
    });
    it('find object in array', () => {
      let child = objects.findChildObjectByKey(obj, 'id', 'arrayObj3');
      expect(child.value).toBe('.root.array.obj3');
    });
    it('find object in nested array', () => {
      let child = objects.findChildObjectByKey(obj, 'id', 'arrayObj2sub');
      expect(child.value).toBe('.root.array.array.obj2.sub');
    });
    it('find object in array within the tree', () => {
      let child = objects.findChildObjectByKey(obj, 'id', 'arrayObj1');
      expect(child.value).toBe('.root.main.sub.array.obj1');
    });
    it('search for not existing property', () => {
      let child = objects.findChildObjectByKey(obj, 'nope', 'arrayObj1');
      expect(child).toBe(null);
    });
    it('search for not existing id', () => {
      let child = objects.findChildObjectByKey(obj, 'id', 'nope');
      expect(child).toBe(null);
    });
    it('search for not existing property and value', () => {
      let child = objects.findChildObjectByKey(obj, 'nope', 'nope');
      expect(child).toBe(null);
    });
  });

  describe('getByPath', () => {
    it('throws on invalid arguments', () => {
      // @ts-expect-error
      expect(() => objects.getByPath()).toThrowError('Missing required parameter \'object\'');
      // @ts-expect-error
      expect(() => objects.getByPath('')).toThrowError('Parameter \'object\' has wrong type');
      // @ts-expect-error
      expect(() => objects.getByPath({})).toThrowError('Missing required parameter \'path\'');
    });

    it('rejects malformed path expressions', () => {
      expect(() => objects.getByPath({}, 'foo.')).toThrowError('Malformed path expression "foo."');
      expect(() => objects.getByPath({}, 'foo bar')).toThrowError('Malformed path expression "foo bar"');
      expect(() => objects.getByPath({}, 'foo..bar')).toThrowError('Malformed path expression "foo..bar"');
      expect(() => objects.getByPath({}, 'foo[bar')).toThrowError('Malformed path expression "foo[bar"');
      expect(() => objects.getByPath({}, 'foobar]')).toThrowError('Malformed path expression "foobar]"');
      expect(() => objects.getByPath({}, 'foo[bar].')).toThrowError('Malformed path expression "foo[bar]."');
      expect(() => objects.getByPath({}, 'foo[bar]..baz')).toThrowError('Malformed path expression "foo[bar]..baz"');
    });

    let testObject = {
      foo: {
        value: 'val3',
        isNull: null,
        isUndefined: undefined
      },
      bar: [
        {
          id: 'bar1',
          bazbar: {
            value: 'val4'
          },
          baz: [
            {
              id: 'baz1',
              value: 'val1'
            },
            {
              id: 'baz99'
            },
            {
              id: 'baz99'
            }
          ]
        },
        {
          id: 'test.bar'
        },
        {
          id: 'test-bar'
        },
        {
          id: 'bar99'
        },
        {
          id: 'bar99'
        }
      ],
      foobar: {
        barbaz: {
          value: 'val2'
        }
      }
    };

    it('throws when pathElement contains array filter but property is not an array', () => {
      expect(() => objects.getByPath(testObject, 'foo[bar]')).toThrowError('Path element "foo[bar]" contains array filter but property "foo" does not contain an array at the root level of the provided object.');
      expect(() => objects.getByPath(testObject, 'foobar.barbaz[bar]')).toThrowError('Path element "barbaz[bar]" contains array filter but property "barbaz" does not contain an array at the matched path "foobar".');
    });

    it('throws when property denoted by path could not be found', () => {
      expect(() => objects.getByPath(testObject, 'doesNotExist')).toThrowError('Property "doesNotExist" does not exist at the root level of the provided object.');
      expect(() => objects.getByPath(testObject, 'foo.childDoesNotExist')).toThrowError('Property "childDoesNotExist" does not exist at the matched path "foo".');
      expect(() => objects.getByPath(testObject, 'bar[bar1].childDoesNotExist')).toThrowError('Property "childDoesNotExist" does not exist at the matched path "bar[bar1]".');
      expect(() => objects.getByPath(testObject, 'bar[bar1].baz[baz1].childDoesNotExist')).toThrowError('Property "childDoesNotExist" does not exist at the matched path "bar[bar1].baz[baz1]".');
      expect(() => objects.getByPath(testObject, 'bar[bar1].baz[baz1].childDoesNotExist[withFilter]')).toThrowError('Property "childDoesNotExist" does not exist at the matched path "bar[bar1].baz[baz1]".');
    });

    it('throws when array property does not contain or contains more than one object with specified filter', () => {
      expect(() => objects.getByPath(testObject, 'bar[bar0]')).toThrowError('No object found with id property "bar0" in array property "bar" at the root level of the provided object.');
      expect(() => objects.getByPath(testObject, 'bar[bar1].baz[baz0]')).toThrowError('No object found with id property "baz0" in array property "baz" at the matched path "bar[bar1]".');
      expect(() => objects.getByPath(testObject, 'bar[bar99]')).toThrowError('More than one object found with id property "bar99" in array property "bar" at the root level of the provided object.');
      expect(() => objects.getByPath(testObject, 'bar[bar1].baz[baz99]')).toThrowError('More than one object found with id property "baz99" in array property "baz" at the matched path "bar[bar1]".');
    });

    it('throws when property alongside path (all except the last one) is null or undefined', () => {
      expect(() => objects.getByPath(testObject, 'foo.isNull.someProp')).toThrowError('Value selected by matched path "foo.isNull" is null or undefined. Further traversal not possible.');
      expect(() => objects.getByPath(testObject, 'foo.isUndefined.someProp')).toThrowError('Value selected by matched path "foo.isUndefined" is null or undefined. Further traversal not possible.');
    });

    it('selects the correct properties', () => {
      expect(objects.getByPath(testObject, 'foo')).toBe(testObject.foo);
      expect(objects.getByPath(testObject, 'foo.isNull')).toBe(testObject.foo.isNull);
      expect(objects.getByPath(testObject, 'foo.isUndefined')).toBe(testObject.foo.isUndefined);
      expect(objects.getByPath(testObject, 'bar[bar1]')).toBe(testObject.bar[0]);
      expect(objects.getByPath(testObject, 'bar[test.bar]')).toBe(testObject.bar[1]);
      expect(objects.getByPath(testObject, 'bar[test-bar]')).toBe(testObject.bar[2]);
      expect(objects.getByPath(testObject, 'bar[bar1].bazbar')).toBe(testObject.bar[0].bazbar);
      expect(objects.getByPath(testObject, 'bar[bar1].baz[baz1]')).toBe(testObject.bar[0].baz[0]);
    });
  });

  describe('isPlainObject', () => {

    it('works as expected', () => {
      expect(objects.isPlainObject({})).toBe(true);
      expect(objects.isPlainObject({foo: 'bar'})).toBe(true);
      expect(objects.isPlainObject([])).toBe(false);
      expect(objects.isPlainObject(null)).toBe(false);
      expect(objects.isPlainObject(undefined)).toBe(false);
      expect(objects.isPlainObject(1)).toBe(false);
      expect(objects.isPlainObject('foo')).toBe(false);
    });

  });

  describe('argumentsToArray', () => {

    it('returns an array', () => {
      let result;
      let func = (...args) => {
        result = args;
      };

      func();
      expect(result).toEqual([]);

      func(1);
      expect(result).toEqual([1]);

      func(undefined, 'a', 'b', null, undefined);
      expect(result).toEqual([undefined, 'a', 'b', null, undefined]);
      expect(objects.isArray(result)).toBe(true);
    });

  });

  describe('equals', () => {

    it('works as expected', () => {
      // @ts-expect-error
      expect(objects.equals()).toBe(true); // undefined === undefined
      // @ts-expect-error
      expect(objects.equals(2)).toBe(false);
      expect(objects.equals(2, 2)).toBe(true);
      expect(objects.equals(2, 3)).toBe(false);
      expect(objects.equals(2, '2')).toBe(false);
      expect(objects.equals('2', '2')).toBe(true);
      expect(objects.equals('', false)).toBe(false);
      expect(objects.equals('', '')).toBe(true);
      expect(objects.equals(true, true)).toBe(true);
      expect(objects.equals(null, null)).toBe(true);
      expect(objects.equals([], [])).toBe(true);
      expect(objects.equals([42], [42])).toBe(false);
      let arr01: number[] & {equals?: (o: any) => boolean} = [1, 2, 3];
      expect(objects.equals(arr01, arr01)).toBe(true);
      expect(objects.equals(arr01, [1, 2, 3])).toBe(false);
      arr01.equals = a => objects.isArray(a) && arrays.equalsIgnoreOrder(arr01, a);
      const arr02: number[] & {equals?: (o: any) => boolean} = [2, 1, 3];
      arr02.equals = a => objects.isArray(a) && arrays.equalsIgnoreOrder(arr02, a);
      expect(objects.equals(arr01, arr02)).toBe(true);
      let a = {};
      expect(objects.equals(a, a)).toBe(true);
      expect(objects.equals({}, {})).toBe(false);
      expect(objects.equals({
        equals: () => true
      }, {
        equals: () => true
      })).toBe(true);
    });

  });

  describe('equalsRecursive', () => {

    it('works as expected', () => {
      // @ts-expect-error
      expect(objects.equalsRecursive()).toBe(true); // undefined === undefined
      // @ts-expect-error
      expect(objects.equalsRecursive(2)).toBe(false);
      expect(objects.equalsRecursive(2, 2)).toBe(true);
      expect(objects.equalsRecursive(2, 3)).toBe(false);
      expect(objects.equalsRecursive(2, '2')).toBe(false);
      expect(objects.equalsRecursive('2', '2')).toBe(true);
      expect(objects.equalsRecursive('', false)).toBe(false);
      expect(objects.equalsRecursive('', '')).toBe(true);
      expect(objects.equalsRecursive(true, true)).toBe(true);
      expect(objects.equalsRecursive(null, null)).toBe(true);
      expect(objects.equalsRecursive([], [])).toBe(true);
      let arr01 = [1, 2, 3];
      expect(objects.equalsRecursive(arr01, arr01)).toBe(true);
      expect(objects.equalsRecursive(arr01, [1, 2, 3])).toBe(true);
      expect(objects.equalsRecursive(arr01, [3, 2, 1])).toBe(false);
      let a = {};
      expect(objects.equalsRecursive(a, a)).toBe(true);
      expect(objects.equalsRecursive({}, {})).toBe(true);
      expect(objects.equalsRecursive({a: '1', b: '2'}, {a: '1', b: '2'})).toBe(true);
      expect(objects.equalsRecursive({a: [{a: '1', b: '2'}, {a: '3', b: '4'}]}, {a: [{a: '1', b: '2'}, {a: '3', b: '4'}]})).toBe(true);
      expect(objects.equalsRecursive({a: [{a: '3', b: '4'}, {a: '1', b: '2'}]}, {a: [{a: '1', b: '2'}, {a: '3', b: '4'}]})).toBe(false);
      expect(objects.equalsRecursive({
        equals: () => true
      }, {
        equals: () => true
      })).toBe(true);
    });

    it('ignores key order', () => {
      expect(objects.equalsRecursive({a: '1', b: '2'}, {b: '2', a: '1'})).toBe(true);
      expect(objects.equalsRecursive({a: {b: '1', c: '2'}}, {a: {c: '2', b: '1'}})).toBe(true);

      // Object.keys() returns key based on insertion order -> assert that this won't matter
      let a = {};
      let b = {};
      a['c'] = '1';
      a['d'] = '2';
      b['d'] = '2';
      b['c'] = '1';
      expect(objects.equalsRecursive(a, b)).toBe(true);
    })
  });

  describe('Constant resolving from plain object / JSON model', () => {

    beforeEach(() => {
      window['myConst'] = 6;
    });

    afterEach(() => {
      delete window['myConst'];
    });

    it('resolveConst', () => {
      expect(objects.resolveConst('${const:scout.FormField.LabelPosition.RIGHT}')).toBe(FormField.LabelPosition.RIGHT);
      expect(objects.resolveConst('${const:myConst}')).toBe(6);
      // @ts-expect-error
      expect(objects.resolveConst(3)).toBe(3); // everything that is not a string, should be returned unchanged
      expect(objects.resolveConst('foo')).toBe('foo'); // a string that is not a constant should be returned unchanged too

      // resolve a constant that does not exist, this will also write a warning in the output
      expect(objects.resolveConst('${const:scout.FormField.LabelPosition.XXX}')).toBe('${const:scout.FormField.LabelPosition.XXX}');
    });

    it('resolveConstProperty', () => {
      // case 1: provide the 'enum' object as constType - resolver takes that object as starting point
      let model = {
        labelPosition: '${const:RIGHT}'
      };
      objects.resolveConstProperty(model, {
        property: 'labelPosition',
        constType: FormField.LabelPosition
      });
      // @ts-expect-error
      expect(model.labelPosition).toBe(FormField.LabelPosition.RIGHT);

      // case 2: provide the 'Window' object as constType - resolver takes that object as starting point
      model = {
        labelPosition: '${const:scout.FormField.LabelPosition.RIGHT}'
      };
      objects.resolveConstProperty(model, {
        property: 'labelPosition',
        constType: window
      });
      // @ts-expect-error
      expect(model.labelPosition).toBe(FormField.LabelPosition.RIGHT);
    });

  });

  describe('optProperty', () => {

    it('should return the last property in the object chain', () => {
      let obj = {};
      expect(objects.optProperty(obj)).toBe(obj);
      expect(objects.optProperty(null)).toBe(null);
      expect(objects.optProperty(obj, 'foo')).toBe(undefined);
      expect(objects.optProperty(null, 'foo')).toBe(null);

      obj = {
        foo: 1
      };
      expect(objects.optProperty(obj, 'foo')).toBe(1);

      obj = {
        foo: {
          bar: 1
        }
      };
      expect(objects.optProperty(obj, 'foo', 'bar')).toBe(1);

      obj = {
        foo: {
          bar: {
            baz: 1
          }
        }
      };
      expect(objects.optProperty(obj, 'foo', 'bar', 'baz')).toBe(1);
    });

  });

  describe('isEmpty', () => {
    it('returns true if argument is an empty object, false if it is a non-empty object, undefined if argument is not an object', () => {
      expect(objects.isEmpty({})).toBe(true);
      expect(objects.isEmpty(undefined)).toBe(true);
      expect(objects.isEmpty(null)).toBe(true);

      expect(objects.isEmpty({test: 'test'})).toBe(false);
      expect(objects.isEmpty({test: 42})).toBe(false);

      expect(objects.isEmpty('test')).toBe(undefined);
      expect(objects.isEmpty(42)).toBe(undefined);
      expect(objects.isEmpty(['test'])).toBe(undefined);
      expect(objects.isEmpty([42])).toBe(undefined);
    });
  });

  describe('ensureValidKey', () => {
    it('always returns a string', () => {
      // @ts-expect-error
      expect(objects.ensureValidKey()).toBe('undefined');
      expect(objects.ensureValidKey(null)).toBe('null');
      expect(objects.ensureValidKey(true)).toBe('true');
      expect(objects.ensureValidKey([])).toBe('[]');
      expect(objects.ensureValidKey(1.5)).toBe('1.5');
      expect(objects.ensureValidKey(.1)).toBe('0.1');
      expect(objects.ensureValidKey(2.0)).toBe('2');
      expect(objects.ensureValidKey(' abc ')).toBe(' abc ');
      expect(objects.ensureValidKey('')).toBe('');
      expect(objects.ensureValidKey('.')).toBe('.');
      expect(objects.ensureValidKey('\u2013')).toBe('\u2013');
      expect(objects.ensureValidKey([42, 100, 999])).toBe('[42,100,999]');

      let o = {
        b: 17,
        a: [-6.0, 23, {valid: false}],
        z: {':{! ': '{type: \'none\'}'}
      };
      expect(objects.ensureValidKey(o)).toBe('{"b":17,"a":[-6,23,{"valid":false}],"z":{":{! ":"{type: \'none\'}"}}');
    });
  });

  describe('isNullOrUndefinedOrEmpty', () => {
    it('detects null or undefined or empty values', () => {
      expect(objects.isNullOrUndefinedOrEmpty(undefined)).toBe(true);
      expect(objects.isNullOrUndefinedOrEmpty(null)).toBe(true);
      expect(objects.isNullOrUndefinedOrEmpty({})).toBe(true);
      expect(objects.isNullOrUndefinedOrEmpty([])).toBe(true);

      expect(objects.isNullOrUndefinedOrEmpty('')).toBe(undefined); // not an object
      expect(objects.isNullOrUndefinedOrEmpty(' ')).toBe(undefined); // not an object
      expect(objects.isNullOrUndefinedOrEmpty(0)).toBe(undefined); // not an object
      expect(objects.isNullOrUndefinedOrEmpty(1)).toBe(undefined); // not an object
      expect(objects.isNullOrUndefinedOrEmpty([0])).toBe(false);
      expect(objects.isNullOrUndefinedOrEmpty([''])).toBe(false);
      expect(objects.isNullOrUndefinedOrEmpty({a: 0})).toBe(false);
    });
  });

  describe('removeEmptyProperties', () => {
    it('removes empty properties', () => {
      expect(objects.removeEmptyProperties(undefined)).toEqual(undefined);
      expect(objects.removeEmptyProperties(null)).toEqual(null);
      expect(objects.removeEmptyProperties({})).toEqual({});

      expect(objects.removeEmptyProperties({a: null})).toEqual({});
      expect(objects.removeEmptyProperties({a: undefined})).toEqual({});
      expect(objects.removeEmptyProperties({a: []})).toEqual({});
      expect(objects.removeEmptyProperties({a: {}})).toEqual({});

      expect(objects.removeEmptyProperties({a: ''})).toEqual({a: ''});
      expect(objects.removeEmptyProperties({a: ' '})).toEqual({a: ' '});
      expect(objects.removeEmptyProperties({a: 0})).toEqual({a: 0});
      expect(objects.removeEmptyProperties({a: {b: {}}})).toEqual({a: {b: {}}}); // no recursive clean
    });
  });

  describe('isSameOrExtendsClass', () => {
    it('returns true if first class is the same as the second one', () => {
      expect(objects.isSameOrExtendsClass(Widget, Widget)).toBe(true);
      expect(objects.isSameOrExtendsClass(Date, Date)).toBe(true);
      expect(objects.isSameOrExtendsClass(Date, Widget)).toBe(false);
      expect(objects.isSameOrExtendsClass(Date, null)).toBe(false);
      expect(objects.isSameOrExtendsClass(null, Date)).toBe(false);
      expect(objects.isSameOrExtendsClass(null, null)).toBe(false);
      // @ts-expect-error
      expect(objects.isSameOrExtendsClass(3, 3)).toBe(false);
    });

    it('returns true if first class extends form the second one', () => {
      expect(objects.isSameOrExtendsClass(Menu, Widget)).toBe(true);
      expect(objects.isSameOrExtendsClass(Widget, Menu)).toBe(false);
    });

    it('uses a type predicate to narrow the type', () => {
      // compile time test, ifs are always false

      let menuClass: new() => Menu;
      if (objects.isSameOrExtendsClass(menuClass, Action)) {
        // menu is a Menu
        let menu = scout.create(menuClass, {parent: null});
        expect(menu.shrinkable).toBe(false);
      }

      let menuClass2;
      if (objects.isSameOrExtendsClass(menuClass2, Action)) {
        // menu is an Action
        let menu = scout.create(menuClass2, {parent: null});
        expect(menu.text).toBe(null);
        // @ts-expect-error shrinkable does not exist on Action
        expect(menu.shrinkable).toBe(false);
      }
    });
  });
});
