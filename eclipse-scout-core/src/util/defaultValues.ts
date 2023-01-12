/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {objects, strings, TypeDescriptor} from '../index';
import $ from 'jquery';

export interface DefaultValuesBootstrapOptions {
  url?: string;
}

export const defaultValues = {
  /**
   * map of "objectType" -> { defaultValuesObject }
   * @internal
   */
  _defaults: {},

  /**
   * map of "objectType" -> [ "objectType", "parentObjectType", ..., "topLevelObjectType" ]
   * @internal
   */
  _objectTypeHierarchyFlat: {},

  bootstrap(options?: DefaultValuesBootstrapOptions): JQuery.Promise<any> {
    options = options || {};
    let defaultOptions = {
      url: 'defaultValues'
    };
    options = $.extend({}, defaultOptions, options);
    // Load default value configuration from server (and cache it)
    return $.ajaxJson(options.url)
      .done(defaultValues.init.bind(this));
  },

  init(data: any) {
    // Store defaults
    defaultValues._objectTypeHierarchyFlat = {};
    defaultValues._defaults = data.defaults || {};

    // Generate object type hierarchy
    let objectTypeHierarchy = data.objectTypeHierarchy || {};
    defaultValues._generateObjectTypeHierarchyRec(objectTypeHierarchy, undefined, defaultValues._objectTypeHierarchyFlat);

    // For all object types in the defaults that don't have a hierarchy yet, add a dummy hierarchy with one element
    Object.keys(defaultValues._defaults).forEach(objectType => {
      if (!defaultValues._objectTypeHierarchyFlat[objectType]) {
        defaultValues._objectTypeHierarchyFlat[objectType] = [objectType];
      }
    });
  },

  /** @internal */
  _generateObjectTypeHierarchyRec(json: any, currentParentObjectTypes: any, targetMap: any) {
    if (!json) {
      return;
    }
    if (!targetMap) {
      throw new Error('Argument \'targetMap\' must not be null');
    }
    Object.keys(json).forEach(objectType => {
      let newCurrentParentObjectTypes = [objectType];
      if (currentParentObjectTypes) {
        newCurrentParentObjectTypes = newCurrentParentObjectTypes.concat(currentParentObjectTypes);
      }

      if (typeof json[objectType] === 'object') {
        defaultValues._generateObjectTypeHierarchyRec(json[objectType], newCurrentParentObjectTypes, targetMap);
      }

      // Store current result
      if (targetMap[objectType]) {
        throw new Error('Object type \'' + objectType + '\' has ambiguous parent object types.');
      }
      targetMap[objectType] = newCurrentParentObjectTypes;
    }, this);
  },

  /**
   * Applies the defaults for the given object type to the given object. Properties
   * are only set if they don't exist yet. The argument 'objectType' is optional
   * if the object has a property of the same name. If the object is an array,
   * the defaults are applied to each of the elements.
   */
  applyTo(object: Record<string, any> | Record<string, any>[], objectType?: string) {
    if (Array.isArray(object)) {
      for (let i = 0; i < object.length; i++) {
        defaultValues.applyTo(object[i], objectType);
      }
    } else if (typeof object === 'object') {
      objectType = objectType || object.objectType;
      if (objectType) {
        if (typeof objectType !== 'string') {
          let objectTypeShort = (objectType + '').substring(0, 80);
          throw new Error('objectType has to be a string but is a ' + typeof objectType + ' ObjectType: ' + objectTypeShort);
        }
        defaultValues._applyToInternal(object, objectType);
      }
    }
  },

  /** @internal */
  _applyToInternal(object: Record<string, any>, objectType: string) {
    let objectTypeHierarchy = defaultValues._objectTypeHierarchyFlat[objectType];
    if (!objectTypeHierarchy) {
      // Remove model variant and try again
      let objectInfo = TypeDescriptor.parse(objectType);
      objectType = objectInfo.objectType.toString();
      objectTypeHierarchy = defaultValues._objectTypeHierarchyFlat[objectType];
    }
    if (!objectTypeHierarchy) {
      // Unknown type, nothing to apply
      return;
    }
    for (let i = 0; i < objectTypeHierarchy.length; i++) {
      let t = objectTypeHierarchy[i];
      let defaults = defaultValues._defaults[t];
      defaultValues._extendWithDefaults(object, defaults);
    }
  },

  /** @internal */
  _extendWithDefaults(object: Record<string, any>, defaults: Record<string, any>) {
    if (object === undefined || defaults === undefined) {
      return;
    }
    Object.keys(defaults).forEach(prop => {
      // Support for "pseudo" default values: If a property name in the default values definition
      // starts with a "~" character, the defined object will _not_ be applied as a default value
      // for a non-existing property, but inner properties of that object will be applied to an
      // existing object.
      let realProp = prop;
      if (strings.startsWith(prop, '~')) {
        realProp = prop.substring(1);
      }
      // If property does not exist, set the default value and return.
      if (object[realProp] === undefined) {
        object[realProp] = objects.valueCopy(defaults[realProp]);
      } else if (objects.isPlainObject(object[realProp]) && objects.isPlainObject(defaults[prop])) {
        // Special case: "default objects". If the property value is an object and default
        // value is also an object, extend the property value instead of replacing it.
        defaultValues._extendWithDefaults(object[realProp], defaults[prop]);
      } else if (Array.isArray(object[realProp]) && objects.isPlainObject(defaults[prop])) {
        // Special case: "array of default objects": If the property value is an array of objects and
        // the default value is an object, extend each object in the array with the default value.
        let objectArray = object[realProp];
        for (let i = 0; i < objectArray.length; i++) {
          if (objects.isPlainObject(objectArray[i])) {
            defaultValues._extendWithDefaults(objectArray[i], defaults[prop]);
          }
        }
      }
    });
  }
};
