/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, DataObjectInventory, dates, ObjectFactory, scout, typeName} from '../../src/index';

describe('BaseDoEntity', () => {
  beforeAll(() => {
    ObjectFactory.get().registerNamespace('scout', {
      BaseDoEntityFixture01Do, BaseDoEntityFixture02Do
    }, {allowedReplacements: ['scout.BaseDoEntityFixture01Do', 'scout.BaseDoEntityFixture02Do']});

    const doInventory = DataObjectInventory.get();
    doInventory.add(BaseDoEntityFixture01Do);
    doInventory.add(BaseDoEntityFixture02Do);
  });

  afterAll(() => {
    const doInventory = DataObjectInventory.get();
    doInventory.remove(BaseDoEntityFixture01Do);
    doInventory.remove(BaseDoEntityFixture02Do);
  });

  describe('clone', () => {
    it('is deep', () => {
      const fixture = scout.create(BaseDoEntityFixture01Do, {
        propObj: {
          dateProp: dates.parseJsonDate('2025-01-06 11:04:40.708Z')
        }
      });
      const clone = fixture.clone();

      expect(fixture.equals(clone)).toBeTrue();
      expect(clone).not.toBe(fixture);
      expect(clone).toBeInstanceOf(BaseDoEntityFixture01Do);
      expect(clone.propObj).toBeInstanceOf(BaseDoEntityFixture02Do);
      expect(clone.propObj).not.toBe(fixture.propObj);
      expect(clone.propObj.dateProp).toBeInstanceOf(Date);
      expect(clone.propObj.dateProp).toEqual(dates.parseJsonDate('2025-01-06 11:04:40.708Z'));
      // modify clone, expect fixture to be untouched

      clone.propObj.dateProp = dates.parseJsonDate('2025-01-06 14:04:40.708Z');
      expect(fixture.propObj.dateProp).toEqual(dates.parseJsonDate('2025-01-06 11:04:40.708Z'));
      expect(fixture.equals(clone)).toBeFalse();
    });

    it('accepts extra model', () => {
      const fixture = scout.create(BaseDoEntityFixture01Do, {
        propObj: {
          dateProp: dates.parseJsonDate('2025-01-14 11:04:40.708Z')
        }
      });
      const extra = {
        dateProp: dates.parseJsonDate('2025-01-14 12:04:40.708Z'),
        propObj: {
          shouldBeMerged: true
        }
      } as any;
      const copy = fixture.clone(extra) as any;
      fixture.propObj.dateProp.setFullYear(2020);
      extra.dateProp.setFullYear(2021);
      expect(copy.propObj.dateProp.getFullYear()).toBe(2025); // change in fixture has no effect to copy
      expect(copy.propObj.shouldBeMerged).toBeTrue();
      expect(copy.dateProp.getFullYear()).toBe(2021); // change in extra has effect to copy as Date is copied by reference
    });

    it('accepts extra data object', () => {
      const fixture = scout.create(BaseDoEntityFixture01Do, {
        propObj: {
          dateProp: dates.parseJsonDate('2025-01-14 11:04:40.708Z')
        }
      });
      const extra = scout.create(BaseDoEntityFixture02Do, {
        dateProp: dates.parseJsonDate('2025-01-14 12:04:40.708Z')
      });
      const copy = fixture.clone(extra) as any;
      fixture.propObj.dateProp.setFullYear(2020);
      extra.dateProp.setFullYear(2021);
      expect(copy.propObj.dateProp.getFullYear()).toBe(2025); // change in fixture has no effect to copy
      expect(copy.dateProp.getFullYear()).toBe(2025); // change in extra has no effect to copy
    });
  });
});

@typeName('scout.BaseDoEntityFixture01')
export class BaseDoEntityFixture01Do extends BaseDoEntity {
  propObj: BaseDoEntityFixture02Do;
}

@typeName('scout.BaseDoEntityFixture02')
export class BaseDoEntityFixture02Do extends BaseDoEntity {
  dateProp: Date;
}
