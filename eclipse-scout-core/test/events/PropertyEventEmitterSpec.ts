/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PropertyEventEmitter, scout} from '../../src';

describe('PropertyEventEmitter', () => {

  class ComputedPropertyEventEmitter extends PropertyEventEmitter {
    _computed: boolean;

    constructor() {
      super();
      this._computed = true;
      this._addComputedProperties(['computed']);
    }

    get computed() {
      return this._computed;
    }
  }

  class DimensionalPropertyEventEmitter extends PropertyEventEmitter {
    constructor() {
      super();
      this._addMultiDimensionalProperty('multiProp', true);
      this._addMultiDimensionalProperty('multiProp2', false);
    }
  }

  class SubDimensionalPropertyEventEmitter extends DimensionalPropertyEventEmitter {
    multiProp: boolean;

    constructor() {
      super();
      this.multiProp = false;
    }
  }

  class DimensionAliasPropertyEventEmitter extends PropertyEventEmitter {

    constructor() {
      super();
      this._addMultiDimensionalProperty('multiProp', true);
      this._addPropertyDimensionAlias('multiProp', 'alias');
      this._addPropertyDimensionAlias('multiProp', 'invertedAlias', {inverted: true});
    }

    setAlias(alias: boolean) {
      this.setProperty('alias', alias);
    }

    get alias() {
      return this.getPropertyDimension('multiProp', 'alias');
    }
  }

  class DimensionMultiPropertyAliasEventEmitter extends PropertyEventEmitter {

    foo: boolean;
    bar: boolean;

    constructor() {
      super();
      this.foo = true;
      this.bar = true;
      this._addMultiDimensionalProperty('foo', true);
      this._addMultiDimensionalProperty('bar', true);
      this._addPropertyDimensionAlias('foo', 'fooDim', {dimension: 'dim'});
      this._addPropertyDimensionAlias('bar', 'barDim', {dimension: 'dim'});
    }

    setFoo(foo: boolean) {
      this.setProperty('foo', foo);
    }

    setFooDim(fooDim: boolean) {
      this.setProperty('fooDim', fooDim);
    }

    get fooDim(): boolean {
      return this.getProperty('fooDim');
    }

    setBar(bar: boolean) {
      this.setProperty('bar', bar);
    }

    setBarDim(barDim: boolean) {
      this.setProperty('barDim', barDim);
    }

    get barDim(): boolean {
      return this.getProperty('barDim');
    }
  }

  describe('init', () => {
    describe('multidimensional property', () => {
      it('default dimension can be passed as boolean or object', () => {
        let emitter = scout.create(DimensionalPropertyEventEmitter);
        expect(emitter.getProperty('multiProp')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(emitter['multiProp-default']).toBe(undefined); // Won't be written onto the emitter directly

        emitter = scout.create(DimensionalPropertyEventEmitter, {
          multiProp: false
        });
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(emitter['multiProp-default']).toBe(undefined);

        // Object contains the dimensions
        emitter = scout.create(DimensionalPropertyEventEmitter, {
          multiProp: {
            default: false
          }
        });
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(emitter['multiProp-default']).toBe(undefined);

        // Also supported but not the preferred way
        emitter = scout.create(DimensionalPropertyEventEmitter, {
          'multiProp-default': false
        });
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(emitter['multiProp-default']).toBe(undefined);
      });

      it('non default dimensions can be passed as object', () => {
        let emitter = scout.create(DimensionalPropertyEventEmitter);
        expect(emitter.getProperty('multiProp')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);

        emitter = scout.create(DimensionalPropertyEventEmitter, {
          multiProp: {
            xyz: false,
            zzz: true
          }
        });
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'xyz')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'zzz')).toBe(true);

        // Also supported but not preferred
        emitter = scout.create(DimensionalPropertyEventEmitter, {
          'multiProp-xyz': false,
          'multiProp-zzz': true
        });
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'xyz')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'zzz')).toBe(true);
      });

      it('maps aliases to dimensions and does not write them onto the emitter', () => {
        let emitter = scout.create(DimensionAliasPropertyEventEmitter, {
          alias: false
        });
        expect(emitter.isPropertyDimensionAlias('alias')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getProperty('multiProp-alias')).toBe(false);
        expect(emitter.alias).toBe(false);

        emitter.setAlias(true);
        expect(emitter.getProperty('multiProp')).toBe(true);
        expect(emitter.getProperty('multiProp-alias')).toBe(true);
        expect(emitter.alias).toBe(true);

        emitter.setProperty('alias', false);
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getProperty('alias')).toBe(false);
      });

      it('updates the correct dimension if multiple properties use the same alias', () => {
        let emitter = scout.create(DimensionMultiPropertyAliasEventEmitter);
        expect(emitter.isMultiDimensionalProperty('foo')).toBe(true);
        expect(emitter.isMultiDimensionalProperty('bar')).toBe(true);
        expect(emitter.isPropertyDimensionAlias('fooDim')).toBe(true);
        expect(emitter.isPropertyDimensionAlias('barDim')).toBe(true);

        expect(emitter.getProperty('foo')).toBe(true);
        expect(emitter.getProperty('fooDim')).toBe(true);
        expect(emitter.getProperty('foo-dim')).toBe(true);
        expect(emitter.foo).toBe(true);
        expect(emitter.fooDim).toBe(true);
        expect(emitter.getProperty('bar')).toBe(true);
        expect(emitter.getProperty('barDim')).toBe(true);
        expect(emitter.getProperty('bar-dim')).toBe(true);
        expect(emitter.bar).toBe(true);
        expect(emitter.barDim).toBe(true);

        emitter.setBarDim(false);
        expect(emitter.getProperty('foo')).toBe(true);
        expect(emitter.getProperty('fooDim')).toBe(true);
        expect(emitter.getProperty('foo-dim')).toBe(true);
        expect(emitter.foo).toBe(true);
        expect(emitter.fooDim).toBe(true);
        expect(emitter.getProperty('bar')).toBe(false);
        expect(emitter.getProperty('barDim')).toBe(false);
        expect(emitter.getProperty('bar-dim')).toBe(false);
        expect(emitter.bar).toBe(false);
        expect(emitter.barDim).toBe(false);

        emitter.setFoo(false);
        expect(emitter.getProperty('foo')).toBe(false);
        expect(emitter.getProperty('fooDim')).toBe(true);
        expect(emitter.getProperty('foo-dim')).toBe(true);
        expect(emitter.foo).toBe(false);
        expect(emitter.fooDim).toBe(true);
        expect(emitter.getProperty('bar')).toBe(false);
        expect(emitter.getProperty('barDim')).toBe(false);
        expect(emitter.getProperty('bar-dim')).toBe(false);
        expect(emitter.bar).toBe(false);
        expect(emitter.barDim).toBe(false);
      });

      it('supports inverted aliases', () => {
        let emitter = scout.create(DimensionAliasPropertyEventEmitter, {
          invertedAlias: true
        });
        expect(emitter.isPropertyDimensionAlias('invertedAlias')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getProperty('multiProp-invertedAlias')).toBe(true);
        expect(emitter.getProperty('invertedAlias')).toBe(true);

        emitter.setProperty('invertedAlias', false);
        expect(emitter.getProperty('multiProp')).toBe(true);
        expect(emitter.getProperty('multiProp-invertedAlias')).toBe(false);
        expect(emitter.getProperty('invertedAlias')).toBe(false);

        emitter.setPropertyDimension('multiProp', 'invertedAlias', true);
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getProperty('multiProp-invertedAlias')).toBe(true);
        expect(emitter.getProperty('invertedAlias')).toBe(true);
      });

      it('supports initializing default value in constructor', () => {
        let emitter = scout.create(SubDimensionalPropertyEventEmitter);
        expect(emitter.multiProp).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);

        // Passing the dimension in the model always wins
        emitter = scout.create(SubDimensionalPropertyEventEmitter, {
          multiProp: true,
          'multiProp-xyz': false
        });
        expect(emitter.multiProp).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'xyz')).toBe(false);

        emitter = scout.create(SubDimensionalPropertyEventEmitter, {
          'multiProp-default': true
        });
        expect(emitter.multiProp).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);

        emitter = scout.create(SubDimensionalPropertyEventEmitter, {
          multiProp: {
            default: true
          }
        });
        expect(emitter.multiProp).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
      });
    });
  });

  describe('multidimensional property', () => {
    it('is computed based on its dimensions', () => {
      let emitter = scout.create(DimensionalPropertyEventEmitter);
      emitter.setPropertyDimension('multiProp', 'aaa', false);
      emitter.setPropertyDimension('multiProp', 'ccc', true);
      expect(emitter.getProperty('multiProp')).toBe(false);

      // The computed property is true if every property is true
      emitter.setPropertyDimension('multiProp', 'aaa', true);
      expect(emitter.getProperty('multiProp')).toBe(true);

      // If default value is false (which is the case for multiProp2), the computation is reversed -> the computed property is only false if every property is false
      emitter.setPropertyDimension('multiProp2', 'aaa', false);
      emitter.setPropertyDimension('multiProp2', 'ccc', true);
      expect(emitter.getProperty('multiProp2')).toBe(true);

      emitter.setPropertyDimension('multiProp2', 'ccc', false);
      expect(emitter.getProperty('multiProp2')).toBe(false);
    });

    it('if no dimensions are present, the default value is used', () => {
      let emitter = scout.create(DimensionalPropertyEventEmitter);
      expect(emitter.getProperty('multiProp')).toBe(true);
      expect(emitter.getProperty('multiProp2')).toBe(false);

      emitter.setProperty('multiProp', {});
      expect(emitter.getProperty('multiProp')).toBe(true);

      emitter.setProperty('multiProp2', {});
      expect(emitter.getProperty('multiProp2')).toBe(false);
    });
  });

  describe('setProperty', () => {

    it('triggers a property change event if the value changes', () => {
      let propertyChangeEvent;
      let emitter = scout.create(PropertyEventEmitter);
      emitter.on('propertyChange', event => {
        propertyChangeEvent = event;
      });
      emitter.setProperty('selected', true);
      expect(propertyChangeEvent.type).toBe('propertyChange');
      expect(propertyChangeEvent.propertyName).toBe('selected');
      expect(propertyChangeEvent.oldValue).toBe(undefined);
      expect(propertyChangeEvent.newValue).toBe(true);

      emitter.setProperty('selected', false);
      expect(propertyChangeEvent.type).toBe('propertyChange');
      expect(propertyChangeEvent.propertyName).toBe('selected');
      expect(propertyChangeEvent.oldValue).toBe(true);
      expect(propertyChangeEvent.newValue).toBe(false);
    });

    it('does not trigger a property change event if the value does not change', () => {
      let propertyChangeEvent;
      let emitter = scout.create(PropertyEventEmitter);
      emitter.setProperty('selected', true);
      emitter.on('propertyChange', event => {
        propertyChangeEvent = event;
      });
      emitter.setProperty('selected', true);
      expect(propertyChangeEvent).toBe(undefined);
    });

    describe('with computed property', () => {
      it('triggers event as for regular properties', () => {
        let computedEmitter = scout.create(ComputedPropertyEventEmitter);
        expect(computedEmitter.getProperty('computed')).toBe(true);
        expect(computedEmitter.computed).toBe(true);

        let propertyChangeEvent;
        computedEmitter.on('propertyChange:computed', event => {
          propertyChangeEvent = event;
        });
        computedEmitter.setProperty('computed', false);
        expect(propertyChangeEvent.type).toBe('propertyChange');
        expect(propertyChangeEvent.propertyName).toBe('computed');
        expect(propertyChangeEvent.oldValue).toBe(true);
        expect(propertyChangeEvent.newValue).toBe(false);
        expect(computedEmitter.getProperty('computed')).toBe(false);
        expect(computedEmitter.computed).toBe(false);
      });
    });

    describe('with multi dimensional property', () => {
      it('sets the dimensions if an object is passed', () => {
        let emitter = scout.create(DimensionalPropertyEventEmitter);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        emitter.setProperty('multiProp', {
          default: false,
          dim1: false
        });
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(false);
      });

      it('only accepts objects with the correct structure for legacy reasons', () => {
        // Prior to the multidimensional properties, not only real booleans were accepted
        // but also falsy (null, undefined, empty string, 0) and truthy values (strings, objects, numbers etc.)
        // Even though it is bad practise, people used it and without using TypeScript, such errors are hard to find
        // -> falsy and truthy should still be supported as far as possible
        let emitter = scout.create(DimensionalPropertyEventEmitter);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(true);

        emitter.setProperty('multiProp', null);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(false);

        emitter.setProperty('multiProp', {});
        expect(emitter.getProperty('multiProp')).toBe(true);

        emitter.setProperty('multiProp', undefined);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(false);

        emitter.setProperty('multiProp', {});
        expect(emitter.getProperty('multiProp')).toBe(true);

        emitter.setProperty('multiProp', 0);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(false);

        emitter.setProperty('multiProp', {});
        expect(emitter.getProperty('multiProp')).toBe(true);

        emitter.setProperty('multiProp', '');
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(false);

        emitter.setProperty('multiProp', {});
        expect(emitter.getProperty('multiProp')).toBe(true);

        emitter.setProperty('multiProp', NaN);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(false);

        emitter.setProperty('multiProp', {});
        expect(emitter.getProperty('multiProp')).toBe(true);

        // Multidimensional properties only work with booleans
        // If not all values in the given object are booleans, the object will be used as value for the default dimension
        emitter.setProperty('multiProp', {dim1: 'asdf', default: false});
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(true);

        emitter.setProperty('multiProp', {dim1: false, default: true});
        expect(emitter.getProperty('multiProp')).toBe(false);

        // Assert that only default dimension is changed and dim1 stays false
        emitter.setProperty('multiProp', {dim1: 'asdf', default: false});
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(false);
        expect(emitter.getProperty('multiProp')).toBe(false);
      });

      it('clears the dimension object if dimensions are default', () => {
        let emitter = scout.create(DimensionalPropertyEventEmitter);
        expect(Object.keys(emitter.getPropertyDimensions('multiProp')).length).toBe(0);

        emitter.setProperty('multiProp', {
          default: false
        });
        expect(emitter.getPropertyDimensions('multiProp')['default']).toBe(false);

        emitter.setProperty('multiProp', {
          default: true
        });
        expect(Object.keys(emitter.getPropertyDimensions('multiProp')).length).toBe(0);

        emitter.setProperty('multiProp', {
          default: false
        });
        expect(emitter.getPropertyDimensions('multiProp')['default']).toBe(false);

        // Check if it works that an empty object is passed (=use default values)
        emitter.setProperty('multiProp', {});
        expect(Object.keys(emitter.getPropertyDimensions('multiProp')).length).toBe(0);
      });

      it('sets an individual dimension if the property contains -', () => {
        let emitter = scout.create(DimensionalPropertyEventEmitter);
        expect(emitter.getProperty('multiProp-default')).toBe(true);
        expect(emitter.getProperty('multiProp-dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(true);

        emitter.setProperty('multiProp-dim1', false);
        expect(emitter.getProperty('multiProp-default')).toBe(true);
        expect(emitter.getProperty('multiProp-dim1')).toBe(false);
        expect(emitter.getProperty('multiProp')).toBe(false);

        emitter.setProperty('multiProp-dim1', true);
        expect(emitter.getProperty('multiProp-default')).toBe(true);
        expect(emitter.getProperty('multiProp-dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(true);

        // No separator delegates to default dimension
        emitter.setProperty('multiProp', false);
        expect(emitter.getProperty('multiProp-default')).toBe(false);
        expect(emitter.getProperty('multiProp-dim1')).toBe(true);
        expect(emitter.getProperty('multiProp')).toBe(false);
      });

      it('triggers a property change for every dimension change', () => {
        let emitter = scout.create(DimensionalPropertyEventEmitter);
        expect(emitter.getProperty('multiProp')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);

        let multiPropEvent;
        emitter.on('propertyChange:multiProp', event => {
          multiPropEvent = event;
        });
        let multiPropDefaultEvent;
        emitter.on('propertyChange:multiProp-default', event => {
          multiPropDefaultEvent = event;
        });
        let multiPropDim1Event;
        emitter.on('propertyChange:multiProp-dim1', event => {
          multiPropDim1Event = event;
        });
        emitter.setPropertyDimension('multiProp', 'dim1', false);
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(multiPropEvent.newValue).toBe(false);
        expect(multiPropDim1Event.newValue).toBe(false);
        expect(multiPropDefaultEvent).toBe(undefined);

        multiPropEvent = null;
        multiPropDim1Event = null;
        emitter.setProperty('multiProp', false);
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(multiPropEvent).toBe(null);
        expect(multiPropDim1Event).toBe(null);
        expect(multiPropDefaultEvent.newValue).toBe(false);

        multiPropDefaultEvent = null;
        multiPropEvent = null;
        multiPropDim1Event = null;
        emitter.setPropertyDimension('multiProp', 'dim1', true);
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(false);
        expect(multiPropEvent).toBe(null);
        expect(multiPropDim1Event.newValue).toBe(true);
        expect(multiPropDefaultEvent).toBe(null);

        multiPropDefaultEvent = null;
        multiPropEvent = null;
        multiPropDim1Event = null;
        emitter.setProperty('multiProp', true);
        expect(emitter.getProperty('multiProp')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'dim1')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'default')).toBe(true);
        expect(multiPropEvent.newValue).toBe(true);
        expect(multiPropDim1Event).toBe(null);
        expect(multiPropDefaultEvent.newValue).toBe(true);
      });

      it('does not trigger property change if value does not change', () => {
        let emitter = scout.create(DimensionalPropertyEventEmitter);
        let multiPropEvent;
        emitter.on('propertyChange:multiProp', event => {
          multiPropEvent = event;
        });
        let multiPropDefaultEvent;
        emitter.on('propertyChange:multiProp-default', event => {
          multiPropDefaultEvent = event;
        });
        let multiPropDim1Event;
        emitter.on('propertyChange:multiProp-dim1', event => {
          multiPropDim1Event = event;
        });
        let _setPropertySpy = spyOn(emitter, '_setProperty').and.callThrough();
        let changed = emitter.setProperty('multiProp', true);
        expect(changed).toBe(false);
        expect(multiPropEvent).toBe(undefined);
        expect(multiPropDim1Event).toBe(undefined);
        expect(multiPropDefaultEvent).toBe(undefined);
        expect(_setPropertySpy.calls.count()).toBe(0);

        changed = emitter.setProperty('multiProp', {
          dim1: true
        });
        expect(changed).toBe(false);
        expect(multiPropEvent).toBe(undefined);
        expect(multiPropDim1Event).toBe(undefined);
        expect(multiPropDefaultEvent).toBe(undefined);
        expect(_setPropertySpy.calls.count()).toBe(0);

        changed = emitter.setProperty('multiProp', {
          dim1: true,
          default: true
        });
        expect(changed).toBe(false);
        expect(multiPropEvent).toBe(undefined);
        expect(multiPropDim1Event).toBe(undefined);
        expect(multiPropDefaultEvent).toBe(undefined);
        expect(_setPropertySpy.calls.count()).toBe(0);
      });

      it('triggers an alias property change for a dimension with an alias', () => {
        let emitter = scout.create(DimensionAliasPropertyEventEmitter);
        expect(emitter.getProperty('multiProp')).toBe(true);
        expect(emitter.getProperty('alias')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'alias')).toBe(true);

        let multiPropAliasEvent;
        emitter.on('propertyChange:alias', event => {
          multiPropAliasEvent = event;
        });
        emitter.setProperty('alias', false);
        expect(emitter.getProperty('multiProp')).toBe(false);
        expect(emitter.getProperty('alias')).toBe(false);
        expect(emitter.getPropertyDimension('multiProp', 'alias')).toBe(false);
        expect(multiPropAliasEvent.newValue).toBe(false);

        // The same happens if setPropertyDimension is called
        multiPropAliasEvent = null;
        emitter.setPropertyDimension('multiProp', 'alias', true);
        expect(emitter.getProperty('multiProp')).toBe(true);
        expect(emitter.getProperty('alias')).toBe(true);
        expect(emitter.getPropertyDimension('multiProp', 'alias')).toBe(true);
        expect(multiPropAliasEvent.newValue).toBe(true);
      });
    });
  });
});
