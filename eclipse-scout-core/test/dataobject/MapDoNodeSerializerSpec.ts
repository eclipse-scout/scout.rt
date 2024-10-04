/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, dataObjects, dates, DoRegistry, ObjectFactory, scout, typeName} from '../../src/index';

describe('MapDoNodeSerializer', () => {

  const mapFixture01DoJson = JSON.stringify(JSON.parse(`{
      "_type": "scout.MapFixture01",
      "dateMap": {
        "dateKey1": "2024-10-02 15:00:00.708",
        "dateKey2": "2024-10-02 14:00:00.708",
        "dateKey3": "2024-10-02 13:00:00.708"
      },
      "numMap": {
        "numKey1": 1,
        "numKey2": 2
      },
      "stringMap": {
        "strKey1": "1",
        "strKey2": "2"
      },
      "mapArr": [
        {
          "mapArr1_1": "1",
          "mapArr1_2": "2"
        },
        {
          "mapArr2_1": "1",
          "mapArr2_2": "2"
        }
      ],
      "objArrMap": {
        "objArrMapKey1": [
          {
            "_type": "scout.MapFixture02",
            "nestedString": "11"
          },
          {
            "_type": "scout.MapFixture02",
            "nestedString": "12"
          }
        ],
        "objArrMapKey2": [
          {
            "_type": "scout.MapFixture02",
            "nestedString": "21"
          },
          {
            "_type": "scout.MapFixture02",
            "nestedString": "22"
          }
        ]
      },
      "ifcMap": {
        "ifcMapKey1": {
          "_type": "scout.MapFixture02",
          "nestedString": "11"
        },
        "ifcMapKey2": {
          "_type": "scout.MapFixture02",
          "nestedString": "22"
        }
      },
      "mapComplex": {
        "1": {
          "11": false,
          "12": false
        },
        "2": {
          "21": true,
          "22": true
        }
      },
      "stringRecordMap": {
        "abc": "def",
        "ghi": "jkl"
      },
      "numRecordMap": {
        "1": "11",
        "2": "22"
      }
    }
    `));

  beforeAll(() => {
    ObjectFactory.get().registerNamespace('scout', {MapFixture01Do, MapFixture02Do}, {allowedReplacements: ['scout.MapFixture01Do', 'scout.MapFixture02Do']});
    const doRegistry = DoRegistry.get();
    doRegistry.add(MapFixture01Do);
    doRegistry.add(MapFixture02Do);
  });

  afterAll(() => {
    const doRegistry = DoRegistry.get();
    doRegistry.removeByClass(MapFixture02Do);
    doRegistry.removeByClass(MapFixture01Do);
  });

  it('can serialize maps', () => {
    const fixture = scout.create(MapFixture01Do, {
      dateMap: new Map([['dateKey1', dates.parseJsonDate('2024-10-02 15:00:00.708')],
        ['dateKey2', dates.parseJsonDate('2024-10-02 14:00:00.708')],
        ['dateKey3', dates.parseJsonDate('2024-10-02 13:00:00.708')]]),
      numMap: new Map([['numKey1', 1], ['numKey2', 2]]),
      stringMap: new Map([['strKey1', '1'], ['strKey2', '2']]),
      mapArr: [new Map([['mapArr1_1', '1'], ['mapArr1_2', '2']]), new Map([['mapArr2_1', '1'], ['mapArr2_2', '2']])],
      objArrMap: new Map([['objArrMapKey1', [createMapFixture02Do('11'), createMapFixture02Do('12')]],
        ['objArrMapKey2', [createMapFixture02Do('21'), createMapFixture02Do('22')]]]),
      ifcMap: new Map([['ifcMapKey1', createMapFixture02Do('11')], ['ifcMapKey2', createMapFixture02Do('22')]]),
      mapComplex: new Map([[1, new Map([['11', false], ['12', false]])], [2, new Map([['21', true], ['22', true]])]]),
      stringRecordMap: {abc: 'def', ghi: 'jkl'},
      numRecordMap: {1: '11', 2: '22'}
    });
    const json = dataObjects.stringify(fixture);
    expect(json).toBe(mapFixture01DoJson);
  });

  function createMapFixture02Do(val: string): MapFixture02Do {
    return scout.create(MapFixture02Do, {nestedString: val});
  }

  it('can deserialize maps', () => {
    const fixture = dataObjects.parse(mapFixture01DoJson, MapFixture01Do);
    expect(fixture).toBeInstanceOf(MapFixture01Do);

    expect(fixture.dateMap).toBeInstanceOf(Map);
    expect(fixture.dateMap.get('dateKey1')).toEqual(dates.parseJsonDate('2024-10-02 15:00:00.708'));
    expect(fixture.dateMap.get('dateKey3')).toEqual(dates.parseJsonDate('2024-10-02 13:00:00.708'));

    expect(fixture.numMap).toBeInstanceOf(Map);
    expect(fixture.numMap.get('numKey1')).toBe(1);
    expect(fixture.numMap.get('numKey2')).toBe(2);

    expect(fixture.stringMap).toBeInstanceOf(Map);
    expect(fixture.stringMap.get('strKey1')).toBe('1');
    expect(fixture.stringMap.get('strKey2')).toBe('2');

    expect(fixture.mapArr).toBeInstanceOf(Array);
    expect(fixture.mapArr.length).toBe(2);
    expect(fixture.mapArr[0]).toBeInstanceOf(Map);
    expect(fixture.mapArr[0].get('mapArr1_1')).toBe('1');

    expect(fixture.objArrMap).toBeInstanceOf(Map);
    expect(fixture.objArrMap.size).toBe(2);
    const objArrMapKey2 = fixture.objArrMap.get('objArrMapKey2');
    expect(objArrMapKey2).toBeInstanceOf(Array);
    expect(objArrMapKey2.length).toBe(2);
    expect(objArrMapKey2[1]).toBeInstanceOf(MapFixture02Do);
    expect(objArrMapKey2[1].nestedString).toEqual('22');

    expect(fixture.ifcMap).toBeInstanceOf(Map);
    expect(fixture.ifcMap.size).toBe(2);
    const ifcMapKey2 = fixture.ifcMap.get('ifcMapKey2');
    expect(ifcMapKey2).toBeInstanceOf(MapFixture02Do);
    expect(ifcMapKey2.nestedString).toEqual('22');

    expect(fixture.mapComplex).toBeInstanceOf(Map);
    expect(fixture.mapComplex.size).toBe(2);
    const firstComplexEntry = fixture.mapComplex.get(1);
    expect(firstComplexEntry).toBeInstanceOf(Map);
    expect(firstComplexEntry.get('11')).toBeFalse();
    expect(fixture.mapComplex.get(2).get('22')).toBeTrue();

    expect(fixture.stringRecordMap).toEqual({abc: 'def', ghi: 'jkl'}); // no BaseDoEntity is created as it is explicitly declared as Record
    expect(fixture.numRecordMap).toEqual({1: '11', 2: '22'}); // no BaseDoEntity is created as it is explicitly declared as Record
  });
});

@typeName('scout.MapFixture01')
export class MapFixture01Do extends BaseDoEntity {
  dateMap: Map<string, Date>;
  numMap: Map<string, number>;
  stringMap: Map<string, string>;
  mapArr: Map<string, string>[];
  objArrMap: Map<string, MapFixture02Do[]>;
  ifcMap: Map<string, MapFixtureDoIfc>;
  mapComplex: Map<number, Map<string, boolean>>;
  stringRecordMap: Record<string, string>; // Java Maps can be declared as Map or Record in TS
  numRecordMap: Record<number, string>;
}

@typeName('scout.MapFixture02')
export class MapFixture02Do extends BaseDoEntity implements MapFixtureDoIfc {
  nestedString: string;
}

export interface MapFixtureDoIfc {
  nestedString: string;
}
