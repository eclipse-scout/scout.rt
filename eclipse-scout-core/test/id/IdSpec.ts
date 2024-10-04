/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TestCompositeId} from '../dataobject/IdDoNodeSerializerSpec';
import {dates, Id, IdParser, scout} from '../../src';

describe('Id', () => {
  it('equals', () => {
    const compositeId1 = TestCompositeId.of('7690f488-a29a-4e13-a977-34814e32c673', '1234', 'test');
    const compositeId2 = TestCompositeId.of('7690f488-a29a-4e13-a977-34814e32c673', '1234', 'test');
    const compositeId3 = TestCompositeId.of('7690f488-a29a-4e13-a977-34814e32c673', '12345', 'test');
    const dateId1 = scout.create(TestDateId, {value: ['2024-10-09 15:31:39.708Z']}, {ensureUniqueId: false});
    const dateId2 = scout.create(TestDateId, {value: ['2024-10-09 15:31:39.708Z']}, {ensureUniqueId: false});
    const dateId3 = scout.create(TestDateId, {value: ['2024-10-09 16:31:39.708Z']}, {ensureUniqueId: false});
    const dateId3_2 = scout.create(TestDateId2, {value: ['2024-10-09 16:31:39.708Z']}, {ensureUniqueId: false});

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
