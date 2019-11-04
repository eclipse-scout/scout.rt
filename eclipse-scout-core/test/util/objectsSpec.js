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
import {FormField, objects} from '../../src/index';

describe('scout.objects', function() {

  describe('copyProperties', function() {

    it('copies all properties', function() {
      var dest = {},
        source = {
          foo: 6,
          bar: 7
        };
      objects.copyProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
    });

    it('copies the properties from prototype as well', function() {
      var dest = {};
      var TestConstructor = function() {
        this.foo = 6;
      };
      var source = new TestConstructor();
      TestConstructor.prototype.bar = 7;
      source.qux = 8;

      objects.copyProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
      expect(dest.qux).toBe(8);
    });

    it('copies only the properties specified by the filter, if there is one', function() {
      var dest = {};
      var TestConstructor = function() {
        this.foo = 6;
        this.xyz = 9;
      };
      var source = new TestConstructor();
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

  describe('copyOwnProperties', function() {

    it('copies all properties', function() {
      var dest = {},
        source = {
          foo: 6,
          bar: 7
        };
      objects.copyOwnProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
    });

    it('does not copy the properties from prototype', function() {
      var dest = {};
      var TestConstructor = function() {
        this.foo = 6;
      };
      TestConstructor.prototype.bar = 7;
      var source = new TestConstructor();
      source.qux = 8;

      objects.copyOwnProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(undefined);
      expect(dest.qux).toBe(8);
    });

    it('copies only the properties specified by the filter, if there is one', function() {
      var dest = {},
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

  describe('countOwnProperties', function() {

    it('counts all own properties', function() {
      var o = {
        first: 1,
        second: 2
      };
      var F = function() {
        this.foo = 66;
        this.bar = 777;
      };
      F.myProp = 'hello';
      F.prototype.anotherProp = 'goodbye';
      var x = new F();
      var y = {};
      objects.copyProperties(x, y);
      y.qux = 9999;

      expect(objects.countOwnProperties(o)).toBe(2); // first, second
      expect(objects.countOwnProperties(F)).toBe(1); // myProp
      expect(objects.countOwnProperties(x)).toBe(2); // foo, bar (but not myProp or anotherProp)
      expect(objects.countOwnProperties(y)).toBe(4); // foo, bar, anotherProp, qux (because copyProperties also copies properties from prototype)
    });

    it('works for objects created with createMap() function', function() {
      var map = objects.createMap();
      expect(objects.countOwnProperties(map)).toBe(0);
      map = objects.createMap({foo: 1});
      expect(objects.countOwnProperties(map)).toBe(1);
    });

  });

  describe('valueCopy', function() {

    it('copies an object by value', function() {
      var o = {
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
      var o2 = objects.valueCopy(o);
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

    it('works for objects created with createMap() function', function() {
      // Top-level map
      var map = objects.createMap();
      map.name = 'Linda';
      map.book = null;
      map.author = undefined;

      var map2 = objects.valueCopy(map);
      map.name = 'Hans';
      map.total = 444;
      expect(map).not.toBe(map2);
      expect(Object.keys(map).length).toBe(4);
      expect(Object.keys(map2).length).toBe(3);
      expect(map2.name).toBe('Linda');
      expect(map2.book).toBe(null);
      expect(map2.author).toBe(undefined);

      // Nested map
      var o = {
        first: 1,
        second: 2
      };
      o.map = objects.createMap();
      o.map['a-b-c'] = 'ABC';

      var o2 = objects.valueCopy(o);
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

  describe('isNumber', function() {
    it('returns true iff argument is a number', function() {
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

  describe('isArray', function() {
    it('returns true when argument is an array', function() {
      expect(objects.isArray([])).toBe(true);

      expect(objects.isArray(undefined)).toBe(false);
      expect(objects.isArray('foo')).toBe(false);
    });
  });

  describe('isNullOrUndefined', function() {
    it('returns true when argument is null or undefined, but not when 0 or any other value', function() {
      expect(objects.isNullOrUndefined(null)).toBe(true);
      expect(objects.isNullOrUndefined(undefined)).toBe(true);
      expect(objects.isNullOrUndefined(0)).toBe(false);
      expect(objects.isNullOrUndefined('foo')).toBe(false);
    });
  });

  describe('values', function() {
    it('returns object values', function() {
      var Class = function() {
        this.a = 'A';
        this.b = 'B';
      };
      var o1 = {
        a: 'X',
        b: 'Y',
        c: 'Z'
      };
      var o2 = new Class();
      o2.a = 'X';
      o2.c = 'C';

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

    it('can handle maps', function() {
      var map1 = objects.createMap();
      var map2 = objects.createMap();
      map2['x'] = 'y'; // jshint ignore:line
      map2[7] = 7;

      expect(objects.values(map1)).toEqual([]);
      expect(objects.values(map2).length).toBe(2);
      expect(objects.values(map2)).toContain('y');
      expect(objects.values(map2)).toContain(7);
    });

    it('createMap with optional properties', function() {
      var the = objects.createMap({world: 1});
      expect(objects.countOwnProperties(the)).toBe(1);
      expect(the.world).toBe(1);
    });

    it('createMap should not have a prototype', function() {
      var map = objects.createMap();
      expect(Object.getPrototypeOf(map)).toBe(null);
    });
  });

  describe('findChildObjectByKey', function() {

    var obj = {
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

    it('find root object', function() {
      var child = objects.findChildObjectByKey(obj, 'id', 'root');
      expect(child.value).toBe('.root');
    });
    it('find object in tree', function() {
      var child = objects.findChildObjectByKey(obj, 'id', 'subMain');
      expect(child.value).toBe('.root.main.sub');
    });
    it('find object in array', function() {
      var child = objects.findChildObjectByKey(obj, 'id', 'arrayObj3');
      expect(child.value).toBe('.root.array.obj3');
    });
    it('find object in nested array', function() {
      var child = objects.findChildObjectByKey(obj, 'id', 'arrayObj2sub');
      expect(child.value).toBe('.root.array.array.obj2.sub');
    });
    it('find object in array within the tree', function() {
      var child = objects.findChildObjectByKey(obj, 'id', 'arrayObj1');
      expect(child.value).toBe('.root.main.sub.array.obj1');
    });
    it('search for not existing property', function() {
      var child = objects.findChildObjectByKey(obj, 'nope', 'arrayObj1');
      expect(child).toBe(null);
    });
    it('search for not existing id', function() {
      var child = objects.findChildObjectByKey(obj, 'id', 'nope');
      expect(child).toBe(null);
    });
    it('search for not existing property and value', function() {
      var child = objects.findChildObjectByKey(obj, 'nope', 'nope');
      expect(child).toBe(null);
    });
  });

  describe('isPlainObject', function() {

    it('works as expected', function() {
      expect(objects.isPlainObject({})).toBe(true);
      expect(objects.isPlainObject({foo: 'bar'})).toBe(true);
      expect(objects.isPlainObject([])).toBe(false);
      expect(objects.isPlainObject(null)).toBe(false);
      expect(objects.isPlainObject(undefined)).toBe(false);
      expect(objects.isPlainObject(1)).toBe(false);
      expect(objects.isPlainObject('foo')).toBe(false);
    });

  });

  describe('argumentsToArray', function() {

    it('returns an array', function() {
      var result;
      var func = function() {
        result = objects.argumentsToArray(arguments);
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

  describe('equals', function() {

    it('works as expected', function() {
      expect(objects.equals()).toBe(true); // undefined === undefined
      expect(objects.equals(2)).toBe(false);
      expect(objects.equals(2, 2)).toBe(true);
      expect(objects.equals(2, 3)).toBe(false);
      expect(objects.equals(2, '2')).toBe(false);
      expect(objects.equals('2', '2')).toBe(true);
      expect(objects.equals('', false)).toBe(false);
      expect(objects.equals('', '')).toBe(true);
      expect(objects.equals(true, true)).toBe(true);
      expect(objects.equals(null, null)).toBe(true);
      expect(objects.equals([], [])).toBe(false);
      var arr01 = [1, 2, 3];
      expect(objects.equals(arr01, arr01)).toBe(true);
      expect(objects.equals(arr01, [1, 2, 3])).toBe(false);
      var a = {};
      expect(objects.equals(a, a)).toBe(true);
      expect(objects.equals({}, {})).toBe(false);
      expect(objects.equals({
        equals: function() {
          return true;
        }
      }, {
        equals: function() {
          return true;
        }
      })).toBe(true);
    });

  });

  describe('equalsRecursive', function() {

    it('works as expected', function() {
      expect(objects.equalsRecursive()).toBe(true); // undefined === undefined
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
      var arr01 = [1, 2, 3];
      expect(objects.equalsRecursive(arr01, arr01)).toBe(true);
      expect(objects.equalsRecursive(arr01, [1, 2, 3])).toBe(true);
      expect(objects.equalsRecursive(arr01, [3, 2, 1])).toBe(false);
      var a = {};
      expect(objects.equalsRecursive(a, a)).toBe(true);
      expect(objects.equalsRecursive({}, {})).toBe(true);
      expect(objects.equalsRecursive({a: '1', b: '2'}, {b: '2', a: '1'})).toBe(true);
      expect(objects.equalsRecursive({a: [{a: '1', b: '2'}, {a: '3', b: '4'}]}, {a: [{a: '1', b: '2'}, {a: '3', b: '4'}]})).toBe(true);
      expect(objects.equalsRecursive({a: [{a: '3', b: '4'}, {a: '1', b: '2'}]}, {a: [{a: '1', b: '2'}, {a: '3', b: '4'}]})).toBe(false);
      expect(objects.equalsRecursive({
        equals: function() {
          return true;
        }
      }, {
        equals: function() {
          return true;
        }
      })).toBe(true);
    });

  });

  describe('Constant resolving from plain object / JSON model', function() {

    beforeEach(function() {
      window.myConst = 6;
    });

    afterEach(function() {
      delete window.myConst;
    });

    it('resolveConst', function() {
      expect(objects.resolveConst('${const:scout.FormField.LabelPosition.RIGHT}')).toBe(FormField.LabelPosition.RIGHT);
      expect(objects.resolveConst('${const:myConst}')).toBe(6);
      expect(objects.resolveConst(3)).toBe(3); // everything that is not a string, should be returned unchanged
      expect(objects.resolveConst('foo')).toBe('foo'); // a string that is not a constant should be returned unchanged too

      // resolve a constant that does not exist, this will also write a warning in the output
      expect(objects.resolveConst('${const:scout.FormField.LabelPosition.XXX}')).toBe('${const:scout.FormField.LabelPosition.XXX}');
    });

    it('resolveConstProperty', function() {
      // case 1: provide the 'enum' object as constType - resolver takes that object as starting point
      var model = {
        labelPosition: '${const:RIGHT}'
      };
      objects.resolveConstProperty(model, {
        property: 'labelPosition',
        constType: FormField.LabelPosition
      });
      expect(model.labelPosition).toBe(FormField.LabelPosition.RIGHT);

      // case 2: provide the 'Window' object as constType - resolver takes that object as starting point
      model = {
        labelPosition: '${const:scout.FormField.LabelPosition.RIGHT}'
      };
      objects.resolveConstProperty(model, {
        property: 'labelPosition',
        constType: window
      });
      expect(model.labelPosition).toBe(FormField.LabelPosition.RIGHT);
    });

  });

  describe('optProperty', function() {

    it('should return the last property in the object chain', function() {
      var obj = {};
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

});
