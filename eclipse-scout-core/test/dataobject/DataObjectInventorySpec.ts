/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, DataObjectInventory, ObjectFactory, typeName} from '../../src/index';

describe('DataObjectInventory', () => {

  let inventory: SpecDataObjectInventory;

  class SpecDataObjectInventory extends DataObjectInventory {
    declare _constructorByTypeName: Map<string, Constructor<BaseDoEntity>>;
    declare _typeNameByObjectType: Map<string, string>;
    declare _objectTypeByTypeName: Map<string, string>;

    constructor() {
      super();
    }
  }

  @typeName('scout.Spec')
  class SpecDo extends BaseDoEntity {
  }

  class Spec2Do extends BaseDoEntity {
    constructor() {
      super();
      this._type = 'scout.Spec2';
    }
  }

  // no decorator here
  class Spec3Do extends BaseDoEntity {
  }

  beforeEach(() => {
    inventory = new SpecDataObjectInventory();
  });

  function expectInventorySize(expected: number) {
    expect(inventory._constructorByTypeName.size).toBe(expected);
    expect(inventory._typeNameByObjectType.size).toBe(expected);
    expect(inventory._objectTypeByTypeName.size).toBe(expected);
  }

  function expectSpecDoInRegistry() {
    expect(inventory.toConstructor('scout.Spec')).toBe(SpecDo);
    expect(inventory.toConstructor('scout.SpecDo')).toBe(SpecDo);
    expect(inventory.toConstructor('SpecDo')).toBe(SpecDo);
    expect(inventory.toTypeName('scout.SpecDo')).toBe('scout.Spec');
    expect(inventory.toTypeName('SpecDo')).toBe('scout.Spec');
    expect(inventory.toObjectType('scout.Spec')).toBe('SpecDo');
  }

  it('register with all arguments works', () => {
    expect(inventory.add(SpecDo, 'scout.Spec', 'scout.SpecDo')).toBeTrue();
    expectInventorySize(1);
    expectSpecDoInRegistry();

    inventory.remove(SpecDo); // remove by class
    expectInventorySize(0);
  });

  it('register with objectType auto detection works', () => {
    ObjectFactory.get().registerNamespace('scout', {SpecDo}, {allowedReplacements: ['scout.SpecDo']});
    expect(inventory.add(SpecDo, 'scout.Spec')).toBeTrue(); // objectType is detected from ObjectFactory
    expectInventorySize(1);
    expectSpecDoInRegistry();
    inventory.remove('scout.Spec'); // remove by typeName
    expectInventorySize(0);
  });

  it('register constructor only works', () => {
    ObjectFactory.get().registerNamespace('scout', {SpecDo}, {allowedReplacements: ['scout.SpecDo']});
    expect(inventory.add(SpecDo)).toBeTrue(); // objectType is detected from ObjectFactory, typeName from decorator
    expectInventorySize(1);
    expectSpecDoInRegistry();
  });

  it('register constructor only without decorator works', () => {
    ObjectFactory.get().registerNamespace('scout', {Spec2Do}, {allowedReplacements: ['scout.Spec2Do']});
    expect(inventory.add(Spec2Do)).toBeTrue(); // objectType is detected from ObjectFactory, typeName from constructor
    expectInventorySize(1);
    expect(inventory.toConstructor('scout.Spec2')).toBe(Spec2Do);
    expect(inventory.toConstructor('scout.Spec2Do')).toBe(Spec2Do);
    expect(inventory.toConstructor('Spec2Do')).toBe(Spec2Do);
    expect(inventory.toTypeName('scout.Spec2Do')).toBe('scout.Spec2');
    expect(inventory.toTypeName('Spec2Do')).toBe('scout.Spec2');
    expect(inventory.toObjectType('scout.Spec2')).toBe('Spec2Do');
  });

  it('register fails if typeName or objectType is missing', () => {
    expect(inventory.add(undefined)).toBeFalse(); // nothing
    expect(inventory.add(Spec3Do)).toBeFalse(); // only constructor
    expectInventorySize(0);
    expect(inventory.add(Spec3Do, 'scout.Spec3')).toBeFalse(); // only constructor and typeName (objectType missing)
    expect(inventory._constructorByTypeName.size).toBe(1);
    expect(inventory._typeNameByObjectType.size).toBe(0);
    expect(inventory._objectTypeByTypeName.size).toBe(0);
  });

  it('register fails if type name is already present', () => {
    expectInventorySize(0);
    expect(inventory.add(SpecDo)).toBeTrue();
    expectInventorySize(1);
    expectSpecDoInRegistry();

    expect(() => inventory.add(SpecDo)).toThrowError('There is already a constructor registered for type name \'scout.Spec\'.');
  });
});

