/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.defaultValues = {

  /**
   * map of "objectType" -> { defaultValuesObject }
   */
  _defaults: {},

  /**
   * map of "objectType" -> [ "objectType", "parentObjectType", ..., "topLevelObjectType" ]
   */
  _objectTypeHierarchyFlat: {},

  bootstrap: function() {
    var that = this;
    // Load default value configuration from server (and cache it)
    return $.mockAjax({
      async: true,
      type: 'GET',
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8',
      cache: true,
      url: 'defaultValues',
      data: ''
    }, {
      done: function(data) {
        that._loadDefaultsConfiguration(data);
      },
      fail: function(jqXHR, textStatus, errorThrown) {
        throw new Error('Error while loading default values: ' + errorThrown);
      }
    });
  },

  _loadDefaultsConfiguration: function(data) {
    // Store defaults
    this._objectTypeHierarchyFlat = {};
    this._defaults = data.defaults || {};

    // Generate object type hierarchy
    var objectTypeHierarchy = data.objectTypeHierarchy || {};
    this._generateObjectTypeHierarchyRec(objectTypeHierarchy, undefined, this._objectTypeHierarchyFlat);

    // For all object types in the defaults that don't have a hierarchy yet, add a dummy hierarchy with one element
    for (var objectType in this._defaults) {
      if (!this._objectTypeHierarchyFlat[objectType]) {
        this._objectTypeHierarchyFlat[objectType] = [objectType];
      }
    }
  },

  _generateObjectTypeHierarchyRec: function(json, currentParentObjectTypes, targetMap) {
    if (!json) {
      return;
    }
    if (!targetMap) {
      throw new Error('Argument \'targetMap\' must not be null');
    }
    for (var objectType in json) {
      var newCurrentParentObjectTypes = [objectType];
      if (currentParentObjectTypes) {
        newCurrentParentObjectTypes = newCurrentParentObjectTypes.concat(currentParentObjectTypes);
      }

      if (typeof json[objectType] === 'object') {
        this._generateObjectTypeHierarchyRec(json[objectType], newCurrentParentObjectTypes, targetMap);
      }

      // Store current result
      if (targetMap[objectType]) {
        throw new Error('Object type \'' + objectType + '\' has ambiguous parent object types.');
      }
      targetMap[objectType] = newCurrentParentObjectTypes;
    }
  },

  /**
   * Applies the defaults for the given object type to the given object. Properties
   * are only set if they don't exist yet. The argument 'objectType' is optional
   * if the object has a property of the same name. If the object is an array,
   * the defaults are applied to each of the elements.
   */
  applyTo: function(object, objectType) {
    if (Array.isArray(object)) {
      for (var i = 0; i < object.length; i++) {
        this.applyTo(object[i], objectType);
      }
    } else if (typeof object === 'object') {
      objectType = objectType || object.objectType;
      if (objectType) {
        this._applyToInternal(object, objectType);
      }
    }
  },

  _applyToInternal: function(object, objectType) {
    var objectTypeHierarchy = this._objectTypeHierarchyFlat[objectType];
    if (!objectTypeHierarchy) {
      // Remove model variant and try again
      objectType = objectType.replace(/\..*/, '');
      objectTypeHierarchy = this._objectTypeHierarchyFlat[objectType];
    }
    if (!objectTypeHierarchy) {
      // Unknown type, nothing to apply
      return;
    }
    for (var i = 0; i < objectTypeHierarchy.length; i++) {
      var t = objectTypeHierarchy[i];
      var defaults = this._defaults[t];
      this._extendWithDefaults(object, defaults);
    }
  },

  _extendWithDefaults: function(object, defaults) {
    if (object === undefined || defaults === undefined) {
      return;
    }
    for (var prop in defaults) {
      // Support for "pseudo" default values: If a property name in the default values definition
      // starts with a "~" character, the defined object will _not_ be applied as a default value
      // for a non-existing property, but inner properties of that object will be applied to an
      // existing object.
      var realProp = prop;
      if (scout.strings.startsWith(prop, '~')) {
        realProp = prop.substring(1);
      }
      // If property does not exist, set the default value and return.
      if (object[realProp] === undefined) {
        object[realProp] = scout.objects.valueCopy(defaults[realProp]);
      }
      // Special case: "default objects". If the property value is an object and default
      // value is also an object, extend the property value instead of replacing it.
      else if (scout.objects.isPlainObject(object[realProp]) && scout.objects.isPlainObject(defaults[prop])) {
        this._extendWithDefaults(object[realProp], defaults[prop]);
      }
      // Special case: "array of default objects": If the property value is an array of objects and
      // the default value is an object, extend each object in the array with the default value.
      else if (Array.isArray(object[realProp]) && scout.objects.isPlainObject(defaults[prop])) {
        var objectArray = object[realProp];
        for (var i = 0; i < objectArray.length; i++) {
          if (scout.objects.isPlainObject(objectArray[i])) {
            this._extendWithDefaults(objectArray[i], defaults[prop]);
          }
        }
      }
    }
  }

};
