/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {objects, strings, TypeDescriptor} from '../index';
import $ from 'jquery';
import {ObjectWithType} from '../scout';

/**
 * map of "objectType" -> { defaultValuesObject }
 */
let _defaults = {};

/**
 * map of "objectType" -> [ "objectType", "parentObjectType", ..., "topLevelObjectType" ]
 */
let _objectTypeHierarchyFlat = {};

export interface DefaultValuesBootstrapOptions {
  url?: string;
}

export function bootstrap(options?: DefaultValuesBootstrapOptions): JQuery.Promise<any> {
  options = options || {};
  let defaultOptions = {
    url: 'defaultValues'
  };
  options = $.extend({}, defaultOptions, options);
  // Load default value configuration from server (and cache it)
  return $.ajaxJson(options.url)
    .done(init.bind(this));
}

export function init(data: any) {
  // Store defaults
  _objectTypeHierarchyFlat = {};
  _defaults = data.defaults || {};

  // Generate object type hierarchy
  let objectTypeHierarchy = data.objectTypeHierarchy || {};
  _generateObjectTypeHierarchyRec(objectTypeHierarchy, undefined, _objectTypeHierarchyFlat);

  // For all object types in the defaults that don't have a hierarchy yet, add a dummy hierarchy with one element
  Object.keys(_defaults).forEach(objectType => {
    if (!_objectTypeHierarchyFlat[objectType]) {
      _objectTypeHierarchyFlat[objectType] = [objectType];
    }
  });
}

export function _generateObjectTypeHierarchyRec(json: any, currentParentObjectTypes: any, targetMap: any) {
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
      _generateObjectTypeHierarchyRec(json[objectType], newCurrentParentObjectTypes, targetMap);
    }

    // Store current result
    if (targetMap[objectType]) {
      throw new Error('Object type \'' + objectType + '\' has ambiguous parent object types.');
    }
    targetMap[objectType] = newCurrentParentObjectTypes;
  }, this);
}

/**
 * Applies the defaults for the given object type to the given object. Properties
 * are only set if they don't exist yet. The argument 'objectType' is optional
 * if the object has a property of the same name. If the object is an array,
 * the defaults are applied to each of the elements.
 */
export function applyTo(object: ObjectWithType | ObjectWithType[], objectType?: string) {
  if (Array.isArray(object)) {
    for (let i = 0; i < object.length; i++) {
      applyTo(object[i], objectType);
    }
  } else if (typeof object === 'object') {
    objectType = objectType || object.objectType;
    if (objectType) {
      if (typeof objectType !== 'string') {
        let objectTypeShort = (objectType + '').substring(0, 80);
        throw new Error('objectType has to be a string but is a ' + typeof objectType + ' ObjectType: ' + objectTypeShort);
      }
      _applyToInternal(object, objectType);
    }
  }
}

export function _applyToInternal(object: ObjectWithType, objectType: string) {
  let objectTypeHierarchy = _objectTypeHierarchyFlat[objectType];
  if (!objectTypeHierarchy) {
    // Remove model variant and try again
    let objectInfo = TypeDescriptor.parse(objectType);
    objectType = objectInfo.objectType.toString();
    objectTypeHierarchy = _objectTypeHierarchyFlat[objectType];
  }
  if (!objectTypeHierarchy) {
    // Unknown type, nothing to apply
    return;
  }
  for (let i = 0; i < objectTypeHierarchy.length; i++) {
    let t = objectTypeHierarchy[i];
    let defaults = _defaults[t];
    _extendWithDefaults(object, defaults);
  }
}

export function _extendWithDefaults(object: ObjectWithType, defaults: any) {
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
      _extendWithDefaults(object[realProp], defaults[prop]);
    } else if (Array.isArray(object[realProp]) && objects.isPlainObject(defaults[prop])) {
      // Special case: "array of default objects": If the property value is an array of objects and
      // the default value is an object, extend each object in the array with the default value.
      let objectArray = object[realProp];
      for (let i = 0; i < objectArray.length; i++) {
        if (objects.isPlainObject(objectArray[i])) {
          _extendWithDefaults(objectArray[i], defaults[prop]);
        }
      }
    }
  }, this);
}

export default {
  applyTo,
  bootstrap,
  init
};
