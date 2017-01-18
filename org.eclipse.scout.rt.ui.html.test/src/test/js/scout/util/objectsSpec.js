/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("scout.objects", function() {

  describe("copyProperties", function() {

    it("copies all properties", function() {
      var dest = {}, source = {
          foo: 6,
          bar: 7
        };
      scout.objects.copyProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
    });

    it("copies the properties from prototype as well", function() {
      var dest = {};
      var TestConstructor = function() {
        this.foo = 6;
        this.bar = 7;
      };
      var source = new TestConstructor();
      source.qux = 8;

      scout.objects.copyProperties(source, dest);
      expect(dest.foo).toBe(6);
      expect(dest.bar).toBe(7);
      expect(dest.qux).toBe(8);
    });

  });

  describe("countOwnProperties", function() {

    it("counts all own properties", function() {
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
      scout.objects.copyProperties(x, y);
      y.qux = 9999;

      expect(scout.objects.countOwnProperties(o)).toBe(2); // first, second
      expect(scout.objects.countOwnProperties(F)).toBe(1); // myProp
      expect(scout.objects.countOwnProperties(x)).toBe(2); // foo, bar (but not myProp or anotherProp)
      expect(scout.objects.countOwnProperties(y)).toBe(4); // foo, bar, anotherProp, qux (because copyProperties also copies properties from prototype)
    });

  });

  describe("valueCopy", function() {

    it("copies an object by value", function() {
      var o = {
          first: 1,
          second: 2,
          arr: [],
          arr2: [ {name: 'Hans'}, {name: 'Linda'} ],
          hamlet: {
            type: 'Book',
            title: { shortTitle: 'Hamlet', longTitle: 'The Tragicall Historie of Hamlet, Prince of Denmarke' },
            author: 'Shakespeare',
            refs: [ { type: 'Book', author: 'Dickens', title: '???' }, { type: 'Audio', author: 'Shakespeare', title: 'Hamlet on CD' } ]
          }
      };
      var o2 = scout.objects.valueCopy(o);
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

  });

  describe('isNumber', function() {
    it('returns true when argument is a number', function() {
      expect(scout.objects.isNumber(0)).toBe(true);
      expect(scout.objects.isNumber(1)).toBe(true);
      expect(scout.objects.isNumber(1.0)).toBe(true);
      expect(scout.objects.isNumber(-1)).toBe(true);
      expect(scout.objects.isNumber('0x0a')).toBe(true); // valid hex-value
      expect(scout.objects.isNumber(null)).toBe(false); // a number reference could be null

      expect(scout.objects.isNumber(undefined)).toBe(false);
      expect(scout.objects.isNumber('foo')).toBe(false);
    });
  });

  describe('isArray', function() {
    it('returns true when argument is an array', function() {
      expect(scout.objects.isArray([])).toBe(true);

      expect(scout.objects.isArray(undefined)).toBe(false);
      expect(scout.objects.isArray('foo')).toBe(false);
    });
  });

  describe('isNullOrUndefined', function() {
    it('returns true when argument is null or undefined, but not when 0 or any other value', function() {
      expect(scout.objects.isNullOrUndefined(null)).toBe(true);
      expect(scout.objects.isNullOrUndefined(undefined)).toBe(true);
      expect(scout.objects.isNullOrUndefined(0)).toBe(false);
      expect(scout.objects.isNullOrUndefined('foo')).toBe(false);
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

      expect(scout.objects.values()).toEqual([]);
      expect(scout.objects.values(null)).toEqual([]);
      expect(scout.objects.values(undefined)).toEqual([]);
      expect(scout.objects.values({})).toEqual([]);
      expect(scout.objects.values(o1).length).toBe(3);
      expect(scout.objects.values(o2).length).toBe(3);
      expect(scout.objects.values(o1)).toContain('X');
      expect(scout.objects.values(o1)).toContain('Y');
      expect(scout.objects.values(o1)).toContain('Z');
      expect(scout.objects.values(o2)).toContain('X'); // not A
      expect(scout.objects.values(o2)).toContain('B');
      expect(scout.objects.values(o2)).toContain('C');
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
      var child = scout.objects.findChildObjectByKey(obj, 'id', 'root');
      expect(child.value).toBe('.root');
    });
    it('find object in tree', function() {
      var child = scout.objects.findChildObjectByKey(obj, 'id', 'subMain');
      expect(child.value).toBe('.root.main.sub');
    });
    it('find object in array', function() {
      var child = scout.objects.findChildObjectByKey(obj, 'id', 'arrayObj3');
      expect(child.value).toBe('.root.array.obj3');
    });
    it('find object in nested array', function() {
      var child = scout.objects.findChildObjectByKey(obj, 'id', 'arrayObj2sub');
      expect(child.value).toBe('.root.array.array.obj2.sub');
    });
    it('find object in array within the tree', function() {
      var child = scout.objects.findChildObjectByKey(obj, 'id', 'arrayObj1');
      expect(child.value).toBe('.root.main.sub.array.obj1');
    });
    it('search for not existing property', function() {
      var child = scout.objects.findChildObjectByKey(obj, 'nope', 'arrayObj1');
      expect(child).toBe(null);
    });
    it('search for not existing id', function() {
      var child = scout.objects.findChildObjectByKey(obj, 'id', 'nope');
      expect(child).toBe(null);
    });
    it('search for not existing property and value', function() {
      var child = scout.objects.findChildObjectByKey(obj, 'nope', 'nope');
      expect(child).toBe(null);
    });
  });

});
