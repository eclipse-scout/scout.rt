/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, DataObjectInventory, dataObjects, dates, ObjectFactory, scout, typeName} from '../../src/index';
import {Fixture01Do, Fixture02Do, Fixture03Do, FixtureDoIfc} from './DataObjectDeserializerSpec';

describe('DataObjectSerializer', () => {

  beforeAll(() => {
    ObjectFactory.get().registerNamespace('scout', {
      Fixture01Do, Fixture02Do, Fixture03Do, Fixture04Do, Fixture05Do, Fixture06Do
    }, {allowedReplacements: ['Fixture01Do', 'Fixture02Do', 'Fixture03Do', 'Fixture04Do', 'Fixture05Do', 'Fixture06Do']});

    // remove Fixture04Do and Fixture05Do
    const doInventory = DataObjectInventory.get();
    doInventory.remove(Fixture04Do);
    doInventory.remove(Fixture05Do);
  });

  afterAll(() => {
    // add Fixture04Do and Fixture05Do again
    const doInventory = DataObjectInventory.get();
    doInventory.add(Fixture04Do);
    doInventory.add(Fixture05Do);
  });

  it('can serialize arrays', () => {
    const serialized = dataObjects.serialize([_createFixture03Do('2024-07-30 07:51:39.708'), _createFixture03Do('2024-07-30 08:51:39.708')]);
    expect(Array.isArray(serialized)).toBeTrue();
    expect(serialized.length).toBe(2);
    expect(serialized[0]).toEqual({
      _type: 'scout.Fixture03',
      nestedNestedDate: '2024-07-30 07:51:39.708'
    });
    expect(serialized[1]).toEqual({
      _type: 'scout.Fixture03',
      nestedNestedDate: '2024-07-30 08:51:39.708'
    });
  });

  it('it excludes undefined properties', () => {
    const fixture = scout.create(Fixture04Do);
    expect(fixture.propObj).toBeUndefined();

    let serialized = dataObjects.serialize(fixture);
    expect(serialized.hasOwnProperty('propObj')).toBeFalse();
  });

  it('fails on cycles', () => {
    const obj1 = {c: null, objectType: 'obj1'};
    const obj2 = {a: [obj1], objectType: 'obj2'};
    const obj3 = {b: new Set([obj2]), objectType: 'obj3'};
    obj1.c = obj3;
    expect(() => dataObjects.serialize(obj3)).toThrowMatching((e: Error) => e.message.includes('[b,a,c]'));

    const s = new Set();
    s.add(s);
    expect(() => dataObjects.serialize({s})).toThrowMatching((e: Error) => e.message.includes('[s]'));

    // does not fail if the same object exists multiple times
    const existsMultipleTimes = {id: 123};
    let data = {a: existsMultipleTimes, b: existsMultipleTimes};
    expect(dataObjects.stringify(data)).toBe('{"a":{"id":123},"b":{"id":123}}');
  });

  it('can serialize based on instance type', () => {
    const fixture01 = _createFixture01Do([
      _createFixture02Do('2024-07-05 13:51:39.708', _createFixture03Do('2024-07-05 12:51:39.708'), _createFixture03Do('2024-07-05 11:51:39.708')),
      _createFixture02Do('2024-07-05 10:51:39.708', _createFixture03Do('2024-07-05 09:51:39.708'), _createFixture03Do('2024-07-05 08:51:39.708'))
    ], _createFixture03Do('2024-07-05 07:51:39.708'));

    const json = dataObjects.stringify(fixture01);
    const expected = JSON.stringify(JSON.parse(`{
        "_type": "scout.Fixture01",
        "propBool": true,
        "propNum": 1234.5678,
        "propStr": "testString",
        "propNull": null,
        "propDate": "2024-07-05 13:51:39.708",
        "propArr": [
          {
            "_type": "scout.Fixture02",
            "nestedDate": "2024-07-05 13:51:39.708",
            "nestedObj": {
              "_type": "scout.Fixture03",
              "nestedNestedDate": "2024-07-05 12:51:39.708"
            },
            "nestedIfc": {
              "_type": "scout.Fixture03",
              "nestedNestedDate": "2024-07-05 11:51:39.708"
            }
          },
          {
            "_type": "scout.Fixture02",
            "nestedDate": "2024-07-05 10:51:39.708",
            "nestedObj": {
              "_type": "scout.Fixture03",
              "nestedNestedDate": "2024-07-05 09:51:39.708"
            },
            "nestedIfc": {
              "_type": "scout.Fixture03",
              "nestedNestedDate": "2024-07-05 08:51:39.708"
            }
          }
        ],
        "propArr2": [[[["2024-07-02 13:51:39.708", "2024-07-02 12:51:39.708"]],[["2024-07-02 11:51:39.708", "2024-07-02 10:51:39.708"]]]],
        "propObj": {
          "_type": "scout.Fixture03",
          "nestedNestedDate": "2024-07-05 07:51:39.708"
        }
      }
    `));
    expect(json).toBe(expected);
  });

  it('keeps existing _type', () => {
    const expected = JSON.stringify(JSON.parse(`{
      "_type":"scout.Fixture01",
      "propObj": {
        "_type":"scout.Fixture03"
      }
    }
    `));

    const json = dataObjects.stringify({
      _type: 'scout.Fixture01',
      propObj: {}
    });
    expect(json).toBe(expected);
  });

  it('can serialize based on existing objectType', () => {
    const expected = JSON.stringify(JSON.parse(`{
      "propObj": {
        "_type":"scout.Fixture03"
      },
      "_type":"scout.Fixture01"
    }
    `));

    const json = dataObjects.stringify({
      objectType: 'scout.Fixture01Do', // with namespace
      propObj: {}
    });
    expect(json).toBe(expected);

    const json2 = dataObjects.stringify({
      objectType: 'Fixture01Do', // without namespace
      propObj: {}
    });
    expect(json2).toBe(expected);
  });

  it('can serialize if parts missing in DataObjectInventory', () => {
    const expected = JSON.stringify(JSON.parse(`{
      "propObj": {
        "propObj2": {
          "propDate": "2024-07-01 11:51:39.708",
          "_type": "scout.Fixture06"
        }
      }
    }
    `));
    const json = dataObjects.stringify({
      objectType: 'Fixture04Do',
      propObj: {
        propObj2: {
          propDate: dates.parseJsonDate('2024-07-01 11:51:39.708')
        }
      }
    });
    expect(json).toBe(expected);
  });

  function _createFixture01Do(arr: Fixture02Do[], obj: Fixture03Do): Fixture01Do {
    const model = {
      propBool: true,
      propNum: 1234.5678,
      propStr: 'testString',
      propDate: dates.parseJsonDate('2024-07-05 13:51:39.708'),
      propNull: null,
      propArr: arr,
      propArr2: [[[[dates.parseJsonDate('2024-07-02 13:51:39.708'), dates.parseJsonDate('2024-07-02 12:51:39.708')]],
        [[dates.parseJsonDate('2024-07-02 11:51:39.708'), dates.parseJsonDate('2024-07-02 10:51:39.708')]]]],
      propObj: obj
    };
    return scout.create(Fixture01Do, model);
  }

  function _createFixture02Do(date: string, obj: Fixture03Do, ifc: FixtureDoIfc): Fixture02Do {
    return scout.create(Fixture02Do, {
      nestedDate: dates.parseJsonDate(date),
      nestedObj: obj,
      nestedIfc: ifc
    });
  }

  function _createFixture03Do(date: string): Fixture03Do {
    return scout.create(Fixture03Do, {nestedNestedDate: dates.parseJsonDate(date)});
  }
});

@typeName('scout.Fixture04')
export class Fixture04Do extends BaseDoEntity {
  propObj: Fixture05Do;
}

@typeName('scout.Fixture05')
export class Fixture05Do extends BaseDoEntity {
  propObj2: Fixture06Do;
}

@typeName('scout.Fixture06')
export class Fixture06Do extends BaseDoEntity {
  propDate: Date;
}
