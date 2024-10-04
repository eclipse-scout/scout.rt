/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, CompositeId, CompositeIdComponentType, dataObjects, DoRegistry, Id, IdParser, idTypeName, NumberId, numbers, ObjectFactory, scout, StringId, typeName, UuId} from '../../src/index';

describe('IdDoNodeSerializer', () => {

  const idFixture01DoJson = JSON.stringify(JSON.parse(`{
      "_type": "scout.IdFixture01",
      "concreteUuid": "07e51622-5a1d-4b52-894c-f987a7a16603",
      "unConcreteUuid": "scout.SampleUuid2:d4e2a738-c5e1-415a-8ebb-c3f01446a7df",
      "unConcreteUuidAny": "scout.SampleUuid3:e22ba8b4-005e-45c1-9474-bacd54937371",
      "unConcreteUuidNoGeneric": "scout.SampleUuid4:350d7435-a885-49e4-9235-f62e09ec7250",
      "concreteStringId": "sampleStringId1",
      "unConcreteStringId": "scout.SampleStringId2:sampleStringId2",
      "concreteLongId": "1234###signature",
      "unConcreteLongId": "scout.SampleLongId2:5678###signature",
      "customIdClass": "testIdValue;true;456",
      "abstractId": "scout.AbstractNumberId:444###sig",
      "compositeId": "7690f488-a29a-4e13-a977-34814e32c673;1234;###sig"
    }
   `));

  beforeAll(() => {
    ObjectFactory.get().registerNamespace('scout', {IdFixture01Do, TestId, TestCompositeId}, {allowedReplacements: ['scout.IdFixture01Do', 'scout.TestId', 'scout.TestCompositeId']});
    const doRegistry = DoRegistry.get();
    doRegistry.add(IdFixture01Do);
    IdParser.register('scout.AbstractNumberId', NumberId);
    IdParser.register('TestIdTypeName', TestId);
  });

  afterAll(() => {
    const doRegistry = DoRegistry.get();
    doRegistry.removeByClass(IdFixture01Do);
  });

  it('can serialize Ids', () => {
    const fixture = scout.create(IdFixture01Do, {
      concreteUuid: UuId.of('07e51622-5a1d-4b52-894c-f987a7a16603', 'scout.SampleUuid'),
      unConcreteUuid: UuId.of('d4e2a738-c5e1-415a-8ebb-c3f01446a7df', 'scout.SampleUuid2'),
      unConcreteUuidAny: UuId.of('e22ba8b4-005e-45c1-9474-bacd54937371', 'scout.SampleUuid3'),
      unConcreteUuidNoGeneric: UuId.of('350d7435-a885-49e4-9235-f62e09ec7250', 'scout.SampleUuid4'),
      concreteStringId: StringId.of('sampleStringId1', 'scout.SampleStringId'),
      unConcreteStringId: StringId.of('sampleStringId2', 'scout.SampleStringId2'),
      concreteLongId: NumberId.of(1234, 'scout.SampleLongId', 'signature'),
      unConcreteLongId: NumberId.of(5678, 'scout.SampleLongId2', 'signature'),
      customIdClass: TestId.of(['testIdValue', 'true', '456']),
      abstractId: NumberId.of(444, 'scout.AbstractNumberId', 'sig'),
      compositeId: TestCompositeId.of('7690f488-a29a-4e13-a977-34814e32c673', '1234', '')
    });
    const json = dataObjects.stringify(fixture);
    expect(json).toBe(idFixture01DoJson);
  });

  it('can deserialize Ids', () => {
    const fixture = dataObjects.parse(idFixture01DoJson, IdFixture01Do);
    expect(fixture).toBeInstanceOf(IdFixture01Do);

    expect(fixture.concreteUuid).toBeInstanceOf(UuId);
    expect(fixture.concreteUuid.value).toEqual('07e51622-5a1d-4b52-894c-f987a7a16603');
    expect(fixture.concreteUuid.typeName).toEqual('scout.SampleUuid');

    expect(fixture.unConcreteUuid).toBeInstanceOf(UuId);
    expect(fixture.unConcreteUuid.value).toEqual('d4e2a738-c5e1-415a-8ebb-c3f01446a7df');
    expect(fixture.unConcreteUuid.typeName).toEqual('scout.SampleUuid2');

    expect(fixture.unConcreteUuidAny).toBeInstanceOf(UuId);
    expect(fixture.unConcreteUuidAny.value).toEqual('e22ba8b4-005e-45c1-9474-bacd54937371');
    expect(fixture.unConcreteUuidAny.typeName).toEqual('scout.SampleUuid3');

    expect(fixture.unConcreteUuidNoGeneric).toBeInstanceOf(UuId);
    expect(fixture.unConcreteUuidNoGeneric.value).toEqual('350d7435-a885-49e4-9235-f62e09ec7250');
    expect(fixture.unConcreteUuidNoGeneric.typeName).toEqual('scout.SampleUuid4');

    expect(fixture.concreteStringId).toBeInstanceOf(StringId);
    expect(fixture.concreteStringId.value).toEqual('sampleStringId1');
    expect(fixture.concreteStringId.typeName).toEqual('scout.SampleStringId');

    expect(fixture.unConcreteStringId).toBeInstanceOf(StringId);
    expect(fixture.unConcreteStringId.value).toEqual('sampleStringId2');
    expect(fixture.unConcreteStringId.typeName).toEqual('scout.SampleStringId2');

    expect(fixture.concreteLongId).toBeInstanceOf(NumberId);
    expect(fixture.concreteLongId.value).toEqual(1234);
    expect(fixture.concreteLongId.typeName).toEqual('scout.SampleLongId');
    expect(fixture.concreteLongId.signature).toEqual('signature');

    expect(fixture.unConcreteLongId).toBeInstanceOf(NumberId);
    expect(fixture.unConcreteLongId.value).toEqual(5678);
    expect(fixture.unConcreteLongId.typeName).toEqual('scout.SampleLongId2');
    expect(fixture.unConcreteLongId.signature).toEqual('signature');

    expect(fixture.customIdClass).toBeInstanceOf(TestId);
    expect(fixture.customIdClass.value).toEqual('testIdValue');
    expect(fixture.customIdClass.typeName).toEqual('scout.TestIdTypeName');
    expect(fixture.customIdClass.customAttr).toBeTrue();
    expect(fixture.customIdClass.secondAttr).toEqual(456);

    expect(fixture.abstractId).toBeInstanceOf(NumberId);
    expect(fixture.abstractId.value).toEqual(444);
    expect(fixture.abstractId.typeName).toEqual('scout.AbstractNumberId');
    expect(fixture.abstractId.signature).toEqual('sig');

    expect(fixture.compositeId).toBeInstanceOf(TestCompositeId);
    expect(fixture.compositeId.typeName).toEqual('scout.CompositeId');
    expect(fixture.compositeId.signature).toEqual('sig');
    expect(fixture.compositeId.value.length).toEqual(3);
    expect(fixture.compositeId.part1).toBeInstanceOf(UuId);
    expect(fixture.compositeId.part1.value).toEqual('7690f488-a29a-4e13-a977-34814e32c673');
    expect(fixture.compositeId.part1.typeName).toEqual('scout.CompositePart1');
    expect(fixture.compositeId.part1.signature).toBeNull();
    expect(fixture.compositeId.part2).toBeInstanceOf(NumberId);
    expect(fixture.compositeId.part2.value).toEqual(1234);
    expect(fixture.compositeId.part2.typeName).toEqual('scout.CompositePart2');
    expect(fixture.compositeId.part1.signature).toBeNull();
    expect(fixture.compositeId.part3).toBeNull();
  });
});

@typeName('scout.IdFixture01')
export class IdFixture01Do extends BaseDoEntity {
  concreteUuid: UuId<'scout.SampleUuid'>;
  unConcreteUuid: UuId<string>;
  unConcreteUuidAny: UuId<any>;
  // @ts-expect-error
  unConcreteUuidNoGeneric: UuId;
  concreteStringId: StringId<'scout.SampleStringId'>;
  unConcreteStringId: StringId<string>;
  concreteLongId: NumberId<'scout.SampleLongId'>;
  unConcreteLongId: NumberId<string>;
  customIdClass: TestId;
  abstractId: Id<number>;
  compositeId: TestCompositeId;
}

@idTypeName('scout.TestIdTypeName')
export class TestId<TTypeName extends string = 'TestIdTypeName'> extends StringId<TTypeName> {
  customAttr: boolean;
  secondAttr: number;

  protected override _initIdValue(value: string | string[]) {
    if (typeof value === 'string') {
      this.value = value;
    } else {
      this.value = value[0];
      this.customAttr = value[1] === 'true';
      this.secondAttr = numbers.ensure(value[2]);
    }
  }

  protected override _toString(): string {
    return this.value + IdParser.COMPONENT_DELIMITER + this.customAttr + IdParser.COMPONENT_DELIMITER + this.secondAttr;
  }

  static override of<TTypeName extends string = 'TestIdTypeName'>(value: string | string[]): TestId<TTypeName> {
    return scout.create(TestId<TTypeName>, {value}, {ensureUniqueId: false});
  }
}

export class TestCompositeId extends CompositeId {

  get part1(): UuId<'scout.CompositePart1'> {
    return this.value[0] as UuId<'scout.CompositePart1'>;
  }

  get part2(): NumberId<'scout.CompositePart2'> {
    return this.value[1] as NumberId<'scout.CompositePart2'>;
  }

  get part3(): StringId<'scout.CompositePart3'> {
    return this.value[2] as StringId<'scout.CompositePart3'>;
  }

  protected _getComponentTypes(): CompositeIdComponentType[] {
    return [
      {idObjectType: UuId, typeName: 'scout.CompositePart1'},
      {idObjectType: NumberId, typeName: 'scout.CompositePart2'},
      {idObjectType: StringId, typeName: 'scout.CompositePart3'}
    ];
  }

  static of(...value: string[]): TestCompositeId {
    return scout.create(TestCompositeId, {value, signature: 'sig'}, {ensureUniqueId: false});
  }
}

