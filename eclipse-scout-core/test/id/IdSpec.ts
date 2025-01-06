/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TestCompositeId} from '../dataobject/IdDoNodeSerializerSpec';
import {dates, Id, IdParser, scout, StringId} from '../../src';

describe('Id', () => {
  it('equals', () => {
    const compositeId1 = TestCompositeId.of('7690f488-a29a-4e13-a977-34814e32c673', '1234', 'test');
    const compositeId2 = TestCompositeId.of('7690f488-a29a-4e13-a977-34814e32c673', '1234', 'test');
    const compositeId3 = TestCompositeId.of('7690f488-a29a-4e13-a977-34814e32c673', '12345', 'test');
    const dateId1 = scout.create(TestDateId, {value: ['2024-10-09 15:31:39.708Z']});
    const dateId2 = scout.create(TestDateId, {value: ['2024-10-09 15:31:39.708Z']});
    const dateId3 = scout.create(TestDateId, {value: ['2024-10-09 16:31:39.708Z']});
    const dateId3_2 = scout.create(TestDateId2, {value: ['2024-10-09 16:31:39.708Z']});
    const stringId1 = StringId.of('1', 'A');
    const stringId2 = StringId.of('2', 'A');
    const stringId3 = StringId.of('1', 'B');
    const stringId4 = StringId.of('2', 'B');
    const stringId5 = StringId.of('1', 'A');

    expect(compositeId1.equals(compositeId2)).toBeTrue();
    expect(compositeId2.equals(compositeId1)).toBeTrue();
    expect(compositeId1.equals(compositeId1)).toBeTrue();
    expect(compositeId1.equals(compositeId3)).toBeFalse();
    expect(compositeId1.equals('')).toBeFalse();
    expect(compositeId1.equals(null)).toBeFalse();
    expect(compositeId1.equals(undefined)).toBeFalse();
    expect(compositeId1.equals(new IdParser())).toBeFalse();

    expect(dateId1.equals(dateId2)).toBeTrue();
    expect(dateId2.equals(dateId1)).toBeTrue();
    expect(dateId2.equals(dateId3)).toBeFalse();

    expect(dateId2.equals(compositeId1)).toBeFalse();
    expect(dateId3.equals(dateId3_2)).toBeFalse(); // same in value but different in class

    expect(stringId1.equals(stringId2)).toBeFalse(); // differs in value
    expect(stringId1.equals(stringId3)).toBeFalse(); // differs in typeName
    expect(stringId1.equals(stringId4)).toBeFalse(); // differs in both
    expect(stringId1.equals(stringId5)).toBeTrue();
  });
});

export class TestDateId extends Id<Date, 'Whatever'> {
  protected override _initIdValue(value: Date | string[]) {
    if (value instanceof Date) {
      this.value = value;
    } else {
      this.value = dates.parseJsonDate(value[0]);
    }
  }

  protected override _toString(): string {
    return dates.toJsonDate(this.value);
  }
}

export class TestDateId2 extends Id<Date, 'Whatever'> {
  protected override _initIdValue(value: Date | string[]) {
    if (value instanceof Date) {
      this.value = value;
    } else {
      this.value = dates.parseJsonDate(value[0]);
    }
  }

  protected override _toString(): string {
    return dates.toJsonDate(this.value);
  }
}
