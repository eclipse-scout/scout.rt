/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, dataObjects, DoRegistry, ObjectFactory, scout, typeName} from '../../src/index';

describe('SetDoNodeSerializer', () => {

  const setFixture01DoJson = JSON.stringify(JSON.parse(`{
      "_type": "scout.SetFixture01",
      "numSet": [4, 6, 7, 9],
      "objSet": [{"_type": "scout.SetFixture02", "nestedString": "val1"}, {"_type": "scout.SetFixture02", "nestedString": "val2"}],
      "ifcSet": [{"_type": "scout.SetFixture02", "nestedString": "val3"}, {"_type": "scout.SetFixture02", "nestedString": "val4"}],
      "complexSet": [[["a", "b"], ["c"], ["d", "e"]], [["f", "g"]]]
    }
   `));

  beforeAll(() => {
    ObjectFactory.get().registerNamespace('scout', {SetFixture01Do}, {allowedReplacements: ['scout.SetFixture01Do']});
    const doRegistry = DoRegistry.get();
    doRegistry.add(SetFixture01Do);
    doRegistry.add(SetFixture02Do);
  });

  afterAll(() => {
    const doRegistry = DoRegistry.get();
    doRegistry.removeByClass(SetFixture01Do);
    doRegistry.removeByClass(SetFixture02Do);
  });

  it('can serialize Sets', () => {
    const fixture = scout.create(SetFixture01Do, {
      numSet: new Set([4, 6, 7, 9]),
      objSet: new Set([createSetFixture02Do('val1'), createSetFixture02Do('val2')]),
      ifcSet: new Set([createSetFixture02Do('val3'), createSetFixture02Do('val4')]),
      complexSet: new Set([new Set([['a', 'b'], ['c'], ['d', 'e']]), new Set([['f', 'g']])])
    });
    const json = dataObjects.stringify(fixture);
    expect(json).toBe(setFixture01DoJson);
  });

  it('can deserialize Sets', () => {
    const fixture = dataObjects.parse(setFixture01DoJson) as SetFixture01Do;
    expect(fixture).toBeInstanceOf(SetFixture01Do);

    expect(fixture.numSet).toBeInstanceOf(Set);
    expect(Array.from(fixture.numSet)).toEqual([4, 6, 7, 9]);

    expect(fixture.objSet).toBeInstanceOf(Set);
    expect(fixture.objSet.size).toEqual(2);
    const objSet = Array.from(fixture.objSet);
    expect(objSet[0]).toBeInstanceOf(SetFixture02Do);
    expect(objSet[0].nestedString).toEqual('val1');
    expect(objSet[1]).toBeInstanceOf(SetFixture02Do);
    expect(objSet[1].nestedString).toEqual('val2');

    expect(fixture.ifcSet).toBeInstanceOf(Set);
    expect(fixture.ifcSet.size).toEqual(2);
    const ifcSet = Array.from(fixture.ifcSet);
    expect(ifcSet[0]).toBeInstanceOf(SetFixture02Do);
    expect(ifcSet[0].nestedString).toEqual('val3');
    expect(ifcSet[1]).toBeInstanceOf(SetFixture02Do);
    expect(ifcSet[1].nestedString).toEqual('val4');

    expect(fixture.complexSet).toBeInstanceOf(Set);
    expect(fixture.complexSet.size).toEqual(2);
    const complexSet = Array.from(fixture.complexSet);
    const firstInnerSet = Array.from(complexSet[0]);
    expect(firstInnerSet.length).toBe(3);
    expect(firstInnerSet[0]).toEqual(['a', 'b']);
    expect(firstInnerSet[1]).toEqual(['c']);
    expect(firstInnerSet[2]).toEqual(['d', 'e']);
    const secondInnerSet = Array.from(complexSet[1]);
    expect(secondInnerSet.length).toBe(1);
    expect(secondInnerSet[0]).toEqual(['f', 'g']);
  });

  function createSetFixture02Do(val: string): SetFixture02Do {
    return scout.create(SetFixture02Do, {nestedString: val});
  }
});

@typeName('scout.SetFixture01')
export class SetFixture01Do extends BaseDoEntity {
  numSet: Set<number>;
  objSet: Set<SetFixture02Do>;
  ifcSet: Set<SetFixtureDoIfc>;
  complexSet: Set<Set<string[]>>;
}

@typeName('scout.SetFixture02')
export class SetFixture02Do extends BaseDoEntity implements SetFixtureDoIfc {
  nestedString: string;
}

export interface SetFixtureDoIfc {
  nestedString: string;
}

