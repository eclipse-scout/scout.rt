/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, BaseDoEntity, Constructor, DataObjectDeserializer, dataObjects, dates, DefaultDoTypeResolver, DoEntity, DoValueMetaData, ObjectFactory, objects, scout, typeName} from '../../src/index';

describe('DataObjectDeserializer', () => {

  beforeAll(() => {
    ObjectFactory.get().registerNamespace('scout', {
      Fixture01Do, Fixture02Do, Fixture03Do, Fixture03SubDo
    }, {allowedReplacements: ['scout.Fixture01Do', 'scout.Fixture02Do', 'scout.Fixture03Do', 'scout.Fixture03SubDo']});
  });

  it('can deserialize based on _type', () => {
    const json = `{
      "_type": "scout.Fixture01",
      "propBool": true,
      "propNum": 1234.5678,
      "propStr": "testString",
      "propDate": "2024-07-15 13:51:39.708Z",
      "propNull": null,
      "propArr": [
        {
          "nestedDate": "2024-07-14 13:51:39.708Z",
          "nestedObj": {
            "nestedNestedDate": "2024-07-13 13:51:39.708Z"
          },
          "nestedIfc": {
            "_type": "scout.Fixture03",
            "nestedNestedDate": "2024-07-12 13:51:39.708Z"
          }
        },
        {
          "nestedDate": "2024-07-11 13:51:39.708Z",
          "nestedObj": {
            "nestedNestedDate": "2024-07-10 13:51:39.708Z"
          },
          "nestedIfc": {
            "_type": "not.existing.but.should.survive",
            "nestedNestedDate": "2024-07-09 13:51:39.708Z"
          }
        }
      ],
      "propArr2": [[[["2024-07-02 13:51:39.708Z", "2024-07-02 12:51:39.708Z"]], [["2024-07-02 11:51:39.708Z", "2024-07-02 10:51:39.708Z"]]]],
      "propObj": {
        "nestedNestedDate": "2024-07-08 13:51:39.708Z"
      }
    }
    `;
    const dataobject = dataObjects.parse(json) as Fixture01Do; // do not pass type so that the detection is tested as well
    expect(dataobject).toBeInstanceOf(Fixture01Do);
    expect(dataobject.propBool).toBeTrue();
    expect(dataobject.propNum).toBe(1234.5678);
    expect(dataobject.propStr).toBe('testString');
    expect(dataobject.propDate).toBeInstanceOf(Date);
    expect(dataobject.propDate).toEqual(dates.parseJsonDate('2024-07-15 13:51:39.708Z'));
    expect(Array.isArray(dataobject.propArr)).toBeTrue();
    expect(dataobject.propArr.length).toBe(2);
    expect(dataobject._type).toBe('scout.Fixture01'); // does not come from deserialize but from the instance creation
    expect(dataobject.propNull).toBeNull();

    const propArr0 = dataobject.propArr[0];
    expect(propArr0).toBeInstanceOf(Fixture02Do);
    expect(propArr0.nestedDate).toEqual(dates.parseJsonDate('2024-07-14 13:51:39.708Z'));
    expect(propArr0.nestedObj).toBeInstanceOf(Fixture03Do);
    expect(propArr0.nestedObj.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-13 13:51:39.708Z'));
    expect(propArr0.nestedIfc).toBeInstanceOf(Fixture03Do);
    expect(propArr0.nestedIfc.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-12 13:51:39.708Z'));

    const propArr1 = dataobject.propArr[1];
    expect(propArr1).toBeInstanceOf(Fixture02Do);
    expect(propArr1.nestedDate).toEqual(dates.parseJsonDate('2024-07-11 13:51:39.708Z'));
    expect(propArr1.nestedObj).toBeInstanceOf(Fixture03Do);
    expect(propArr1.nestedObj.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-10 13:51:39.708Z'));
    expect(propArr1.nestedIfc).toBeInstanceOf(BaseDoEntity); // because _type is missing and the interface is not helpful
    const nested = propArr1.nestedIfc as any;
    expect(nested.nestedNestedDate).toBe('2024-07-09 13:51:39.708Z');
    expect(nested._type).toBe('not.existing.but.should.survive');

    const expectedDateArray = [[[[dates.parseJsonDate('2024-07-02 13:51:39.708Z'), dates.parseJsonDate('2024-07-02 12:51:39.708Z')]],
      [[dates.parseJsonDate('2024-07-02 11:51:39.708Z'), dates.parseJsonDate('2024-07-02 10:51:39.708Z')]]]];
    expect(dataobject.propArr2).toEqual(expectedDateArray); // is detected as array of Date with dimension 4
    expect(dataobject.propObj).toBeInstanceOf(Fixture03Do);
    expect(dataobject.propObj.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-08 13:51:39.708Z'));
  });

  it('can deserialize objects without prototype', () => {
    let o1 = Object.create(null); // e.g. used by objects.createMap
    let deserializer = new DataObjectDeserializer({
      createPojoIfDoIsUnknown: true
    });
    expect(deserializer.deserialize(o1)).toEqual({});
  });

  it('can detect type if an unknown dataobject is in between', () => {
    const fixture = `{
      "_type": "scout.Fixture03",
      "sub": {
        "nested": {
          "_type": "scout.Fixture03Sub"
        }
      }
    }
    `;
    const dataobject = dataObjects.parse(fixture) as any;
    expect(dataobject).toBeInstanceOf(Fixture03Do);
    expect(dataobject.sub).toBeInstanceOf(BaseDoEntity);
    expect(dataobject.sub.nested).toBeInstanceOf(Fixture03SubDo);
  });

  it('can deserialize based on objectType', () => {
    const withStringObjectType = `{
      "objectType": "scout.Fixture03Do",
      "nestedNestedDate": "2024-07-25 09:41:10.708Z"
    }
    `;
    const resultFromStringObjectType = dataObjects.parse(withStringObjectType) as Fixture03Do;
    expect(resultFromStringObjectType).toBeInstanceOf(Fixture03Do);
    expect(resultFromStringObjectType.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-25 09:41:10.708Z'));

    const withConstructorObjectType = {
      objectType: Fixture03Do,
      nestedNestedDate: '2024-07-25 08:41:10.708Z'
    };
    const resultFromConstructorObjectType = new DataObjectDeserializer().deserialize(withConstructorObjectType) as Fixture03Do;
    expect(resultFromConstructorObjectType).toBeInstanceOf(Fixture03Do);
    expect(resultFromConstructorObjectType.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-25 08:41:10.708Z'));
  });

  it('uses BaseDoEntity if no type information is available', () => {
    const result = dataObjects.parse('{"num":1234}') as any;
    expect(result).toBeInstanceOf(BaseDoEntity);
    expect(result.num).toBe(1234);
  });

  it('ignores _typeVersion depending on retainTypeVersion-option', () => {
    const json = '{' +
      '  "_type": "whatever",' +
      '  "_typeVersion": "1.2.3",' +
      '  "value": 1234,' +
      '  "nestedObj": {' +
      '    "_type": "scout.Fixture01",' +
      '    "_typeVersion": "3.2.1"' +
      '  }' +
      '}';

    const baseDoEntity = dataObjects.parse(json) as any;
    expect(baseDoEntity).toBeInstanceOf(BaseDoEntity); // as _type cannot be found.
    expect(baseDoEntity._type).toBe('whatever'); // is kept
    // retainTypeVersion is false if object is instance of BaseDoEntity -> _typeVersion is skipped when deserializing
    expect(baseDoEntity._typeVersion).toBeUndefined();
    expect(baseDoEntity.value).toBe(1234);
    expect(baseDoEntity.nestedObj).toBeInstanceOf(Fixture01Do);
    // retainTypeVersion is false if object is instance of BaseDoEntity -> _typeVersion is skipped when deserializing
    expect(baseDoEntity.nestedObj._typeVersion).toBeUndefined();

    const baseDoEntityWithTypeVersion = dataObjects.parse(json, null, {retainTypeVersion: true}) as any;
    expect(baseDoEntityWithTypeVersion).toBeInstanceOf(BaseDoEntity); // as _type cannot be found.
    expect(baseDoEntityWithTypeVersion._type).toBe('whatever'); // is kept
    // _typeVersion is NOT skipped when deserializing
    expect(baseDoEntityWithTypeVersion._typeVersion).toBe('1.2.3');
    expect(baseDoEntityWithTypeVersion.value).toBe(1234);
    expect(baseDoEntityWithTypeVersion.nestedObj).toBeInstanceOf(Fixture01Do);
    // _typeVersion is NOT skipped when deserializing
    expect(baseDoEntityWithTypeVersion.nestedObj._typeVersion).toBe('3.2.1');

    const baseDoEntityWithoutTypeVersion = dataObjects.parse(json, null, {retainTypeVersion: false}) as any;
    expect(baseDoEntityWithoutTypeVersion).toBeInstanceOf(BaseDoEntity); // as _type cannot be found.
    expect(baseDoEntityWithoutTypeVersion._type).toBe('whatever'); // is kept
    // _typeVersion is skipped when deserializing
    expect(baseDoEntityWithoutTypeVersion._typeVersion).toBeUndefined();
    expect(baseDoEntityWithoutTypeVersion.value).toBe(1234);
    expect(baseDoEntityWithoutTypeVersion.nestedObj).toBeInstanceOf(Fixture01Do);
    // _typeVersion is skipped when deserializing
    expect(baseDoEntityWithoutTypeVersion.nestedObj._typeVersion).toBeUndefined();

    const baseDoEntityWithTypeVersionPredicate = dataObjects.parse(json, null, {retainTypeVersion: (obj: DoEntity) => obj._type !== 'whatever'}) as any;
    expect(baseDoEntityWithTypeVersionPredicate).toBeInstanceOf(BaseDoEntity); // as _type cannot be found.
    expect(baseDoEntityWithTypeVersionPredicate._type).toBe('whatever'); // is kept
    // _typeVersion is skipped when deserializing
    expect(baseDoEntityWithTypeVersionPredicate._typeVersion).toBeUndefined();
    expect(baseDoEntityWithTypeVersionPredicate.value).toBe(1234);
    expect(baseDoEntityWithTypeVersionPredicate.nestedObj).toBeInstanceOf(Fixture01Do);
    // _typeVersion is NOT skipped when deserializing
    expect(baseDoEntityWithTypeVersionPredicate.nestedObj._typeVersion).toBe('3.2.1');

    const pojo = dataObjects.parse(json, null, {createPojoIfDoIsUnknown: true}) as any;
    expect(pojo).not.toBeInstanceOf(BaseDoEntity); // as _type cannot be found.
    expect(pojo._type).toBe('whatever'); // is kept
    // retainTypeVersion is true if object is not instance of BaseDoEntity -> _typeVersion is NOT skipped when deserializing
    expect(pojo._typeVersion).toBe('1.2.3');
    expect(pojo.value).toBe(1234);
    expect(pojo.nestedObj).toBeInstanceOf(Fixture01Do);
    // retainTypeVersion is false if object is instance of BaseDoEntity -> _typeVersion is skipped when deserializing
    expect(pojo.nestedObj._typeVersion).toBeUndefined();

    const pojoWithTypeVersion = dataObjects.parse(json, null, {createPojoIfDoIsUnknown: true, retainTypeVersion: true}) as any;
    expect(pojoWithTypeVersion).not.toBeInstanceOf(BaseDoEntity); // as _type cannot be found.
    expect(pojoWithTypeVersion._type).toBe('whatever'); // is kept
    // _typeVersion is NOT skipped when deserializing
    expect(pojoWithTypeVersion._typeVersion).toBe('1.2.3');
    expect(pojoWithTypeVersion.value).toBe(1234);
    expect(pojoWithTypeVersion.nestedObj).toBeInstanceOf(Fixture01Do);
    // _typeVersion is NOT skipped when deserializing
    expect(pojoWithTypeVersion.nestedObj._typeVersion).toBe('3.2.1');

    const pojoWithoutTypeVersion = dataObjects.parse(json, null, {createPojoIfDoIsUnknown: true, retainTypeVersion: false}) as any;
    expect(pojoWithoutTypeVersion).not.toBeInstanceOf(BaseDoEntity); // as _type cannot be found.
    expect(pojoWithoutTypeVersion._type).toBe('whatever'); // is kept
    // _typeVersion is skipped when deserializing
    expect(pojoWithoutTypeVersion._typeVersion).toBeUndefined();
    expect(pojoWithoutTypeVersion.value).toBe(1234);
    expect(pojoWithoutTypeVersion.nestedObj).toBeInstanceOf(Fixture01Do);
    // _typeVersion is skipped when deserializing
    expect(pojoWithoutTypeVersion.nestedObj._typeVersion).toBeUndefined();

    const pojoWithTypeVersionPredicate = dataObjects.parse(json, null, {createPojoIfDoIsUnknown: true, retainTypeVersion: (obj: DoEntity) => obj._type !== 'whatever'}) as any;
    expect(pojoWithTypeVersionPredicate).not.toBeInstanceOf(BaseDoEntity); // as _type cannot be found.
    expect(pojoWithTypeVersionPredicate._type).toBe('whatever'); // is kept
    // _typeVersion is skipped when deserializing
    expect(pojoWithTypeVersionPredicate._typeVersion).toBeUndefined();
    expect(pojoWithTypeVersionPredicate.value).toBe(1234);
    expect(pojoWithTypeVersionPredicate.nestedObj).toBeInstanceOf(Fixture01Do);
    // _typeVersion is NOT skipped when deserializing
    expect(pojoWithTypeVersionPredicate.nestedObj._typeVersion).toBe('3.2.1');
  });

  it('throws if expected and given type differ', () => {
    const json = `{
      "_type": "scout.Fixture01",
      "nestedNestedDate": "2024-07-25 07:41:10.708Z"
    }
    `;
    expect(() => dataObjects.parse(json, Fixture03Do)).toThrow();
    expect(() => dataObjects.parse(json, 'scout.Fixture03Do')).toThrow();
  });

  it('can deserialize arrays', () => {
    const json = `[{
      "_type": "scout.Fixture03",
      "nestedNestedDate": "2024-07-31 07:52:39.708Z"
    }, {
      "_type": "scout.Fixture03",
      "nestedNestedDate": "2024-07-31 07:54:39.708Z"
    }]
    `;
    const arr = dataObjects.parse(json) as unknown as Fixture03Do[];
    expect(Array.isArray(arr)).toBeTrue();
    expect(arr.length).toBe(2);

    const first = arr[0];
    expect(first).toBeInstanceOf(Fixture03Do);
    expect(first._type).toBe('scout.Fixture03');
    expect(first.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-31 07:52:39.708Z'));

    const second = arr[1];
    expect(second).toBeInstanceOf(Fixture03Do);
    expect(second._type).toBe('scout.Fixture03');
    expect(second.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-31 07:54:39.708Z'));
  });

  it('can deserialize if _type is a subtype of declared type', () => {
    const json = `{
      "_type": "scout.Fixture02",
      "nestedDate": "2024-12-12 07:52:39.708Z",
      "objectType": "scout.TestObjectType",
      "nestedObj": {
        "_type": "scout.Fixture03Sub",
        "nestedNestedDateSub": "2024-12-12 08:03:39.708Z"
      }
    }
    `;
    const fixture02 = dataObjects.parse(json) as Fixture02Do;
    expect(fixture02).toBeInstanceOf(Fixture02Do);
    expect(fixture02.nestedDate).toEqual(dates.parseJsonDate('2024-12-12 07:52:39.708Z'));
    expect(fixture02['objectType']).toBe('scout.TestObjectType'); // objectType is preserved. Required e.g. for CodeTypes which may have objectType property coming from the backend
    const fixture03Sub = fixture02.nestedObj as Fixture03SubDo;
    expect(fixture03Sub).toBeInstanceOf(Fixture03SubDo); // subtype of type declared in Fixture02Do is used
    expect(fixture03Sub.nestedNestedDateSub).toEqual(dates.parseJsonDate('2024-12-12 08:03:39.708Z'));
  });

  it('uses pojo for unknown DOs if requested', () => {
    const json = `{
      "_type": "scout.Fixture02",
      "nestedObjUnknown": {
        "propertyOfPojo": true
      }
    }
    `;
    const fixture02 = dataObjects.parse(json, Fixture02Do, {createPojoIfDoIsUnknown: true}) as any;
    expect(fixture02).toBeInstanceOf(Fixture02Do);
    expect(fixture02._type).toBe('scout.Fixture02');
    expect(fixture02.nestedDate).toBeUndefined();
    expect(fixture02.nestedObj).toBeUndefined();
    expect(fixture02.nestedIfc).toBeUndefined();
    expect(fixture02._contributions).toBeUndefined();
    expect(objects.isPojo(fixture02.nestedObjUnknown)).toBeTrue();
    expect(fixture02.nestedObjUnknown.propertyOfPojo).toBeTrue();

    const topLevelPojo = dataObjects.parse('{"_type": "not_known"}', null, {createPojoIfDoIsUnknown: true}) as any;
    expect(objects.isPojo(topLevelPojo)).toBeTrue();
    expect(topLevelPojo._type).toBe('not_known');
  });

  it('considers custom type resolvers when resolving the data object type', () => {
    class DoTypeResolver implements DefaultDoTypeResolver {
      resolve(rawObj: Record<string, any>, metaData: DoValueMetaData): Constructor {
        if (rawObj?._type === 'SpecialOne') {
          return Fixture01Do;
        }
        return null;
      }
    }

    let dataObject = dataObjects.parse('{"_type": "SpecialOne"}');
    expect(dataObject).toBeInstanceOf(BaseDoEntity);

    // Custom resolver always resolves to Fixture01Do
    const resolver = new DoTypeResolver();
    dataObjects.doTypeResolvers.push(resolver);
    dataObject = dataObjects.parse('{"_type": "SpecialOne"}');
    expect(dataObject).toBeInstanceOf(Fixture01Do);

    // Default behavior applies if resolver is removed again
    arrays.remove(dataObjects.doTypeResolvers, resolver);
    dataObject = dataObjects.parse('{"_type": "SpecialOne"}');
    expect(dataObject).toBeInstanceOf(BaseDoEntity);
  });

  it('can deserialize properties from abstract DOs', () => {
    const json = `{
      "_type": "scout.Fixture01",
      "abstractPropDate": "2013-05-25 22:30:00.000Z",
      "propDate": "2013-05-25 22:30:00.000Z"
    }
    `;
    const foo = dataObjects.parse(json) as Fixture01Do;
    expect(foo).toBeInstanceOf(Fixture01Do);
    expect(foo.abstractPropDate).toEqual(dates.parseJsonDate('2013-05-25 22:30:00.000Z'));
    expect(foo.propDate).toEqual(dates.parseJsonDate('2013-05-25 22:30:00.000Z'));
  });
});

@typeName()
export abstract class AbstractFixture01Do extends BaseDoEntity {
  abstractPropDate: Date;
}

@typeName('scout.Fixture01')
export class Fixture01Do extends AbstractFixture01Do {
  propBool: boolean;
  propNum: number;
  propStr: string;
  propNull: string;
  propDate: Date;
  propArr: Fixture02Do[];
  propArr2: Array<Array<Date[]>[]>;
  propObj: Fixture03Do;
}

@typeName('scout.Fixture02')
export class Fixture02Do extends BaseDoEntity {
  nestedDate: Date;
  nestedObj: Fixture03Do;
  nestedIfc: FixtureDoIfc;
}

export interface FixtureDoIfc {
  nestedNestedDate: Date;
}

@typeName('scout.Fixture03')
export class Fixture03Do extends BaseDoEntity implements FixtureDoIfc {
  nestedNestedDate: Date;
}

@typeName('scout.Fixture03Sub')
export class Fixture03SubDo extends Fixture03Do {
  nestedNestedDateSub: Date;
}
