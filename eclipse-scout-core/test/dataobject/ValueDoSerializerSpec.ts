/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, BinaryResource, dataObjects, dates, Point, scout, typeName, ValueDo} from '../../src';

describe('ValueDoSerializer', () => {

  const valueDoFixtureJson = JSON.stringify(JSON.parse(`{
        "_type": "scout.ValueDoFixture01",
        "locale": {
          "_type": "scout.LocaleValue",
          "value": "fr-CA"
        },
        "long": {
          "_type": "scout.LongValue",
          "value": 100
        },
        "string": {
          "_type": "scout.StringValue",
          "value": "str"
        },
        "boolean": {
          "_type": "scout.BooleanValue",
          "value": true
        },
        "int": {
          "_type": "scout.IntegerValue",
          "value": 200
        },
        "byteArray": {
          "_type": "scout.ByteArrayValue",
          "value": "dGVzdA=="
        },
        "binaryRes": {
          "_type": "scout.BinaryResourceValue",
          "value": {
            "content": "dGVzdA==",
            "contentLength": 4,
            "lastModified": 1793786745101,
            "contentType": "text/plain",
            "filename": "test.txt",
            "charset": "UTF-8",
            "fingerprint": 73204161,
            "cachingAllowed": false,
            "cacheMaxAge": 0
          }
        },
        "obj": {
          "_type": "scout.ObjectValue",
          "value": {
            "x": 1.0,
            "y": 2.0
          }
        },
        "bigDecimal": {
          "value": 1234567
        },
        "list": {
          "_type": "scout.DoEntityListValue",
          "value": [
            {
              "_type": "scout.ValueDoFixture02",
              "nestedNum": 11
            },
            {
              "_type": "scout.ValueDoFixture02",
              "nestedNum": 22
            }
          ]
        },
        "dateTimeWithTimezone": {
          "_type": "scout.DateTimeWithTimeZoneValue",
          "_typeVersion": "6789",
          "value": "1970-01-01 01:00:00.000 +0100"
        },
        "dateTime": {
          "_type": "scout.DateTimeValue",
          "value": "1970-01-01 01:00:00.000"
        },
        "date": {
          "_type": "scout.DateValue",
          "_contributions": [{
              "_type": "scout.ValueDoFixture02",
              "nestedNum": 33
            }],
          "value": "1970-01-01 00:00:00.000"
        },
        "noValueDo": {
          "_type": "scout.NotExistingDateValue",
          "value": "1970-01-01"
        }
      }
    `));

  it('can serialize ValueDo', () => {
    const fixture = scout.create(ValueDoFixture01Do, {
      locale: {
        _type: 'scout.LocaleValue',
        value: 'fr-CA'
      },
      long: {
        _type: 'scout.LongValue',
        value: 100
      },
      string: {
        _type: 'scout.StringValue',
        value: 'str'
      },
      boolean: {
        _type: 'scout.BooleanValue',
        value: true
      },
      int: {
        _type: 'scout.IntegerValue',
        value: 200
      },
      byteArray: {
        _type: 'scout.ByteArrayValue',
        value: 'dGVzdA=='
      },
      binaryRes: {
        _type: 'scout.BinaryResourceValue',
        value: {content: 'dGVzdA==', contentLength: 4, lastModified: 1793786745101, contentType: 'text/plain', filename: 'test.txt', charset: 'UTF-8', fingerprint: 73204161, cachingAllowed: false, cacheMaxAge: 0}
      },
      obj: {
        _type: 'scout.ObjectValue',
        value: {x: 1.0, y: 2.0}
      },
      bigDecimal: {value: 1234567},
      list: {
        _type: 'scout.DoEntityListValue',
        value: [{nestedNum: 11}, {nestedNum: 22}]
      },
      dateTimeWithTimezone: {_type: 'scout.DateTimeWithTimeZoneValue', _typeVersion: '6789', value: '1970-01-01 01:00:00.000 +0100'},
      dateTime: {_type: 'scout.DateTimeValue', value: '1970-01-01 01:00:00.000'},
      date: {
        _type: 'scout.DateValue',
        _contributions: [{_type: 'scout.ValueDoFixture02', nestedNum: 33}],
        value: '1970-01-01 00:00:00.000'
      },
      noValueDo: {
        _type: 'scout.NotExistingDateValue',
        value: '1970-01-01'
      }
    } as any);
    const json = dataObjects.stringify(fixture);
    expect(json).toBe(valueDoFixtureJson);
  });

  it('can deserialize DateValueDo without meta data', () => {
    const fixture1 = dataObjects.parse(`{
      "_type": "scout.DateValue",
      "value": "1970-01-01 00:00:00.000"
    }`) as ValueDo<Date>;
    expect(fixture1).toBeInstanceOf(ValueDo);
    expect(fixture1.value).toBeInstanceOf(Date);

    const fixture2 = dataObjects.parse(`{
      "_type": "scout.LongValue",
      "value": "10000"
    }`) as ValueDo<number>;

    expect(fixture2).toBeInstanceOf(ValueDo);
    expect(fixture2.value).toBeInstanceOf(Number);

    const fixture3 = dataObjects.parse(`{
      "_type": "scout.DoEntityListValue",
      "value": [{"a": 1}, {"b": "b"}]
    }`) as ValueDo<BaseDoEntity[]>;

    expect(fixture3).toBeInstanceOf(ValueDo);
    expect(fixture3.value).toBeInstanceOf(Array);
    expect(fixture3.value.length).toBe(2);
    expect(fixture3.value[0]).toBeInstanceOf(BaseDoEntity);
    expect(fixture3.value[0]['a']).toBe(1);
    expect(fixture3.value[1]).toBeInstanceOf(BaseDoEntity);
    expect(fixture3.value[1]['b']).toBe('b');
  });

  it('can deserialize ValueDo without meta data', () => {
    const json = JSON.stringify(JSON.parse(`{
        "locale": {
          "_type": "scout.LocaleValue",
          "_typeVersion": "6789",
          "value": "fr-CA"
        },
        "long": {
          "_type": "scout.LongValue",
          "value": 100
        },
        "string": {
          "_type": "scout.StringValue",
          "value": "str"
        }
      }
    `));
    const fixture = dataObjects.parse(json /* no type info here */) as ValueDoFixture01Do;
    expect(fixture).toBeInstanceOf(BaseDoEntity);

    expect(fixture.locale).toBeInstanceOf(ValueDo);
    expect(fixture.locale._type).toBe('scout.LocaleValue');
    expect(fixture.locale._typeVersion).toBe('6789');
    expect(fixture.locale.value).toBe('fr-CA');

    expect(fixture.long).toBeInstanceOf(ValueDo);
    expect(fixture.long._type).toBe('scout.LongValue');
    expect(fixture.long.value).toBe(100);

    expect(fixture.string).toBeInstanceOf(ValueDo);
    expect(fixture.string._type).toBe('scout.StringValue');
    expect(fixture.string.value).toBe('str');

    expect(dataObjects.stringify(fixture)).toBe(json);
  });

  it('can deserialize ValueDo', () => {
    const fixture = dataObjects.parse(valueDoFixtureJson) as ValueDoFixture01Do;
    expect(fixture).toBeInstanceOf(ValueDoFixture01Do);

    expect(fixture.locale).toBeInstanceOf(ValueDo);
    expect(fixture.locale._type).toBe('scout.LocaleValue');
    expect(fixture.locale.value).toBe('fr-CA');

    expect(fixture.long).toBeInstanceOf(ValueDo);
    expect(fixture.long._type).toBe('scout.LongValue');
    expect(fixture.long.value).toBe(100);

    expect(fixture.string).toBeInstanceOf(ValueDo);
    expect(fixture.string._type).toBe('scout.StringValue');
    expect(fixture.string.value).toBe('str');

    expect(fixture.boolean).toBeInstanceOf(ValueDo);
    expect(fixture.boolean._type).toBe('scout.BooleanValue');
    expect(fixture.boolean.value).toBe(true);

    expect(fixture.int).toBeInstanceOf(ValueDo);
    expect(fixture.int._type).toBe('scout.IntegerValue');
    expect(fixture.int.value).toBe(200);

    expect(fixture.byteArray).toBeInstanceOf(ValueDo);
    expect(fixture.byteArray._type).toBe('scout.ByteArrayValue');
    expect(fixture.byteArray.value).toBe('dGVzdA==');

    expect(fixture.binaryRes).toBeInstanceOf(ValueDo);
    expect(fixture.binaryRes._type).toBe('scout.BinaryResourceValue');
    expect(fixture.binaryRes.value.content).toBe('dGVzdA==');
    expect(fixture.binaryRes.value.contentType).toBe('text/plain');
    expect(fixture.binaryRes.value.fingerprint).toBe(73204161);
    expect(fixture.binaryRes.value.filename).toBe('test.txt');

    expect(fixture.obj).toBeInstanceOf(ValueDo);
    expect(fixture.obj._type).toBe('scout.ObjectValue');
    expect(fixture.obj.value.x).toBe(1.0);
    expect(fixture.obj.value.y).toBe(2.0);

    expect(fixture.bigDecimal).toBeInstanceOf(ValueDo);
    expect(fixture.bigDecimal._type).toBeUndefined(); // _type missing in json
    expect(fixture.bigDecimal.value).toBe(1234567);

    expect(fixture.list).toBeInstanceOf(ValueDo);
    expect(fixture.list._type).toBe('scout.DoEntityListValue');
    expect(fixture.list.value.length).toBe(2);
    expect(fixture.list.value[0]).toBeInstanceOf(ValueDoFixture02Do);
    expect(fixture.list.value[0]._type).toBe('scout.ValueDoFixture02');
    expect(fixture.list.value[0].nestedNum).toBe(11);
    expect(fixture.list.value[1]).toBeInstanceOf(ValueDoFixture02Do);
    expect(fixture.list.value[1]._type).toBe('scout.ValueDoFixture02');
    expect(fixture.list.value[1].nestedNum).toBe(22);

    expect(fixture.dateTimeWithTimezone).toBeInstanceOf(ValueDo);
    expect(fixture.dateTimeWithTimezone._type).toBe('scout.DateTimeWithTimeZoneValue');
    expect(fixture.dateTimeWithTimezone._typeVersion).toBe('6789');
    expect(fixture.dateTimeWithTimezone.value).toBe('1970-01-01 01:00:00.000 +0100'); // not parsed to Date as not supported!

    expect(fixture.dateTime).toBeInstanceOf(ValueDo);
    expect(fixture.dateTime._type).toBe('scout.DateTimeValue');
    expect(fixture.dateTime.value).toEqual(dates.parseJsonDate('1970-01-01 01:00:00.000'));

    expect(fixture.date).toBeInstanceOf(ValueDo);
    expect(fixture.date._type).toBe('scout.DateValue');
    expect(fixture.date._contributions.length).toBe(1);
    expect(fixture.date._contributions[0]).toBeInstanceOf(ValueDoFixture02Do);
    expect(fixture.date._contributions[0]['nestedNum']).toBe(33);
    expect(fixture.date._contributions[0]._type).toBe('scout.ValueDoFixture02');
    expect(fixture.date.value).toEqual(dates.parseJsonDate('1970-01-01 00:00:00.000'));

    expect(fixture.noValueDo).toBeInstanceOf(BaseDoEntity); // as this is no registered ValueDo _type
    expect(fixture.noValueDo._type).toBe('scout.NotExistingDateValue');
    expect(fixture.noValueDo['value']).toEqual('1970-01-01');
  });
});

@typeName('scout.ValueDoFixture01')
export class ValueDoFixture01Do extends BaseDoEntity {
  locale: ValueDo<string>;
  long: ValueDo<number>;
  string: ValueDo<string>;
  boolean: ValueDo<boolean>;
  int: ValueDo<number>;
  byteArray: ValueDo<string>;
  binaryRes: ValueDo<BinaryResource>;
  obj: ValueDo<Point>;
  bigDecimal: ValueDo<number>;
  list: ValueDo<ValueDoFixture02Do[]>;
  dateTimeWithTimezone: ValueDo<string>; // must be string (and not Date!) as dates.parseJsonDate cannot parse the format used in DateTimeWithTimeZoneValueDo.java!
  dateTime: ValueDo<any>; // no Date generic here
  date: ValueDo<Date>;
  noValueDo: BaseDoEntity;
}

@typeName('scout.ValueDoFixture02')
export class ValueDoFixture02Do extends BaseDoEntity {
  nestedNum: number;
}
