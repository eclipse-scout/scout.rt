/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, BaseDoEntity, dates, ObjectFactory, Primitive, scout, Widget} from '../index';
import $ from 'jquery';

const CONST_REGEX = /\${const:([^}]*)}/;

export const objects = {

  /**
   * Uses Object.create(null) to create an object without a prototype. This is different to use the literal {} which links the object to Object.prototype.
   * <p>
   * Not using the literal has the advantage that the object does not contain any inherited properties like `toString` so it is not necessary to use `o.hasOwnProperty(p)`
   * instead of `p in o` to check for the existence.
   *
   * @param [properties] optional initial properties to be set on the new created object
   */
  createMap(properties?: object): any {
    let map = Object.create(null);
    if (properties) {
      $.extend(map, properties);
    }
    return map;
  },

  /**
   * Copies all the properties (including the ones from the prototype.) from dest to source
   * @param [filter] an array of property names.
   * @returns the destination object (the destination parameter will be modified as well)
   */
  copyProperties<D>(source: object, dest: D, filter?: string[] | string): D {
    let propertyName;
    filter = arrays.ensure(filter);
    for (propertyName in source) {
      if (filter.length === 0 || filter.indexOf(propertyName) !== -1) {
        dest[propertyName] = source[propertyName];
      }
    }
    return dest;
  },

  /**
   * Creates a dynamic proxy which can be used e.g. to initialize a constant.
   * The proxy wraps an instance created on first use using the given constructor function.
   * All calls to the proxy are forwarded to this lazy instance.
   * The instance can only be created after the {@link ObjectFactory} has been initialized.
   * @param constr The constructor to lazily create the instance on first use.
   * @returns A proxy that delegates calls to the lazy instance.
   */
  createSingletonProxy<T extends object>(constr: new() => T): T {
    return new Proxy({/* target obj for the lazy instance */}, {
      get(target: { instance?: T }, prop: string | symbol, proxy) {
        if (!target.instance) {
          if (prop === 'prototype') {
            // variable has no prototype: directly return undefined so that no instance is created yet.
            return undefined;
          }
          if (!ObjectFactory.get()?.initialized) {
            // only allow singleton creation after ObjectFactory has been set up. Otherwise, the class cannot be customized/replaced.
            throw Error('Singleton cannot be created yet as the ObjectFactory is not initialized.');
          }
          target.instance = scout.create(constr);
        }
        let requestedProperty = target.instance[prop];
        if (typeof requestedProperty === 'function') {
          return requestedProperty.bind(target.instance);
        }
        return Reflect.get(target.instance, prop, target.instance);
      },

      set(target: { instance?: T }, prop: string | symbol, value: any) {
        return Reflect.set(target.instance, prop, value, target.instance);
      }
    }) as T;
  },

  /**
   * Copies the own properties (excluding the ones from the prototype) from source to dest.
   * If a filter is specified, only the properties matching the ones in the filter are copied.
   * @param [filter] an array of property names.
   * @returns the destination object (the destination parameter will be modified as well)
   */
  copyOwnProperties<D>(source: object, dest: D, filter?: string[] | string): D {
    let propertyName;
    filter = arrays.ensure(filter);
    for (propertyName in source) {
      if (Object.prototype.hasOwnProperty.call(source, propertyName) && (filter.length === 0 || filter.indexOf(propertyName) !== -1)) {
        dest[propertyName] = source[propertyName];
      }
    }
    return dest;
  },

  /**
   * Counts and returns the properties of a given object or map (see #createMap).
   */
  countOwnProperties(obj: object): number {
    // map objects don't have a prototype
    if (!Object.getPrototypeOf(obj)) {
      return Object.keys(obj).length;
    }

    // regular objects may inherit a property through their prototype
    // we're only interested in own properties
    let count = 0;
    for (let prop in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, prop)) {
        count++;
      }
    }
    return count;
  },

  /**
   * Copies the specified properties (including the ones from the prototype) from source to dest.
   * Properties that already exist on dest are NOT overwritten.
   */
  extractProperties<D>(source: object, dest: D, properties: string[]): D {
    properties.forEach(propertyName => {
      if (dest[propertyName] === undefined) {
        dest[propertyName] = source[propertyName];
      }
    });
    return dest;
  },

  /**
   * returns
   *  - true if the obj has at least one of the given properties.
   *  - false if the obj has none of the given properties.
   */
  someOwnProperties(obj: object, properties: string[] | string): boolean {
    let propArr = arrays.ensure(properties);
    return propArr.some(prop => {
      return Object.prototype.hasOwnProperty.call(obj, prop);
    });
  },

  /**
   * returns
   *  - true if the obj or its prototypes have at least one of the given properties.
   *  - false if the obj or its prototypes have none of the given properties.
   */
  someProperties(obj: object, properties: string[] | string): boolean {
    let propArr = arrays.ensure(properties);
    return propArr.some(prop => {
      return prop in obj;
    });
  },

  /**
   * Creates a copy of the given object with the properties in alphabetic order. The characters in the property names are compared
   * individually (no alphanumeric sorting). The names are first sorted by ascending length and then by character class: special
   * characters (punctuation etc.), then numbers (0-9), then lowercase letters (a-z), then uppercase letters (A-Z).
   *
   * The order of elements in an array is preserved. Values that are not plain objects are left as is. This method detects cyclic
   * references and does not throw an error. Instead, the cyclic reference is reassigned to the corresponding copy.
   *
   * @param recursive whether to recursively sort property names in nested objects. The default value is `true`.
   */
  sortProperties<T>(obj: T, recursive = true): T {
    return sortPropertiesImpl(obj, recursive, new Map());

    function sortPropertiesImpl(obj: any, recursive: boolean, seen: Map<any, any>): any {
      // Check for cyclic references
      if (seen.has(obj)) {
        return seen.get(obj);
      }

      if (objects.isArray(obj)) {
        let copy = [];
        seen.set(obj, copy);
        return obj.reduce((acc, elem) => {
          if (recursive) {
            acc.push(sortPropertiesImpl(elem, recursive, seen));
          } else {
            acc.push(elem);
          }
          return acc;
        }, copy);
      }

      if (objects.isObject(obj)) {
        let copy = {};
        seen.set(obj, copy);
        return Object.keys(obj)
          .sort((k1, k2) => k1.localeCompare(k2))
          .reduce((acc, key) => {
            if (recursive) {
              acc[key] = sortPropertiesImpl(obj[key], recursive, seen);
            } else {
              acc[key] = obj[key];
            }
            return acc;
          }, copy);
      }

      return obj;
    }
  },

  /**
   * Creates deep clones of given value.
   * The following types are supported:
   * * Pojo
   * * `Array`
   * * `Date`
   * * `Map`
   * * `Set`
   * * All objects (except Widget) having a `clone` function (expected to create a deep clone without taking any arguments). These are classes like `BaseDoEntity`, `Dimension`, `GridData`, `Insets`, `Point`, `Status`, `URL`, ...
   * @param val The value to deep clone.
   * @returns The deep clone if supported, the input value otherwise.
   */
  valueCopy<T>(val: T): T {
    if (objects.isPojo(val)) {
      return objects.copyPropertiesRecursive(val, {}) as T;
    }
    if (objects.isArray(val)) {
      return val.map(e => objects.valueCopy(e)) as T;
    }
    return deepCloneClass(val); // handles all class types
  },

  /**
   * Recursively copies all the properties from source to destination (deep clone).
   *
   * All properties are recursively cloned using {@link objects#valueCopy}. Therefore, only properties of types supported by {@link objects#valueCopy} must be present.
   * @param source Where to get the properties to copy.
   * @param destination Where to store the cloned properties.
   */
  copyPropertiesRecursive<T>(source: T, destination: T): T {
    for (const [k, v] of Object.entries(source)) {
      destination[k] = objects.valueCopy(v);
    }
    return destination;
  },

  /**
   * Returns the first object with the given property and propertyValue or null if there is no such object within parentObj.
   * @param property property to search for
   * @param propertyValue value of the property
   */
  findChildObjectByKey(parentObj: any, property: string, propertyValue: any): any {
    if (parentObj === undefined || parentObj === null || typeof parentObj !== 'object') {
      return null;
    }
    if (parentObj[property] === propertyValue) {
      return parentObj;
    }
    let child;
    if (Array.isArray(parentObj)) {
      for (let i = 0; i < parentObj.length; i++) {
        child = objects.findChildObjectByKey(parentObj[i], property, propertyValue);
        if (child) {
          return child;
        }
      }
    }
    for (let prop in parentObj) {
      if (Object.prototype.hasOwnProperty.call(parentObj, prop)) {
        child = objects.findChildObjectByKey(parentObj[prop], property, propertyValue);
        if (child) {
          return child;
        }
      }
    }
    return null;
  },

  /**
   * This function returns the value of a property from the provided object specified by the second path parameter.
   * The path consists of a dot separated series of property names (e.g. foo, foo.bar, foo.bar.baz).
   * In addition, traversing into array properties is possible by specifying a suitable filter for the element's id property in square brackets (e.g. foo[bar], foo.bar[baz]).
   *
   * Example:
   *
   * let obj = {
   *   foo: {
   *     bar: {
   *       foobar: 'val1'
   *     }
   *   },
   *   baz: [
   *     {
   *       id: 'baz1',
   *       value: 'val2'
   *     },
   *     {
   *       id: 'baz2',
   *       value: 'val3'
   *     }
   *   ]
   * }
   *
   * objects.getByPath(obj, 'foo') === obj.foo;
   * objects.getByPath(obj, 'foo.bar') === obj.foo.bar;
   * objects.getByPath(obj, 'baz[baz1]') → { id: 'baz1', value: 'val2' }
   * objects.getByPath(obj, 'baz[baz2].value') → 'val3'
   *
   * @param object The object to select a property from.
   * @param path The path for the selection.
   * @returns Object Returns the selected object.
   * @throws Throws an error, if the provided parameters are malformed, or a property could not be found/a id property filter does not find any elements.
   */
  getByPath(object: object, path: string): any {
    scout.assertParameter('object', object, Object);
    scout.assertParameter('path', path);

    const pathElementRegexString = '(\\w+)(?:\\[((?:\\w|\\.|-)+)\\])?';
    const pathValidationRegex = new RegExp('^' + pathElementRegexString + '(?:\\.' + pathElementRegexString + ')*$');

    if (!pathValidationRegex.test(path)) {
      throw new Error('Malformed path expression "' + path + '"');
    }

    const pathElementRegex = new RegExp(pathElementRegexString);
    let pathMatchedSoFar = '';
    let currentContext = object;

    // Split by dot, but only if the dot is not followed by a string containing a ] that is not preceded by a [.
    // That excludes dots, that are part of an array filter (e.g. foo[foo.bar]).
    // Explanation: The regular expression matches dots literally, (\.), that are not followed (negative lookahead: (?!...)
    // by any mount of "not opening square brackets" ([^[]*) followed by a closing square bracket (last closing square bracket: ])
    path.split(/\.(?![^[]*])/).forEach(pathElement => {
      // After the first iteration, the current context may be null or undefined. In this case, further traversal is not possible.
      if (objects.isNullOrUndefined(currentContext)) {
        throw new Error('Value selected by matched path "' + pathMatchedSoFar + '" is null or undefined. Further traversal not possible.');
      }

      // match path element to retrieve property name and optional array property index
      let pathElementMatch = pathElementRegex.exec(pathElement);
      let propertyName = pathElementMatch[1];
      let arrayPropertyFilter = pathElementMatch[2];

      let pathMatchedErrorContext = pathMatchedSoFar.length === 0 ? 'root level of the provided object.' : 'matched path "' + pathMatchedSoFar + '".';

      // check if property 'propertyName' exists
      if (!currentContext.hasOwnProperty(propertyName)) {
        throw new Error('Property "' + propertyName + '" does not exist at the ' + pathMatchedErrorContext);
      }

      let property = currentContext[propertyName];

      // check if we are trying to match an array property or not
      if (arrayPropertyFilter) {
        // check for correct type of property
        if (!Array.isArray(property)) {
          throw new Error('Path element "' + pathElement + '" contains array filter but property "' + propertyName + '" does not contain an array at the ' + pathMatchedErrorContext);
        }
        // find elements matching criteria and make sure that exactly one object was found
        let matchedElements = property.filter(element => {
          return element['id'] === arrayPropertyFilter;
        });
        if (matchedElements.length === 0) {
          throw new Error('No object found with id property "' + arrayPropertyFilter + '" in array property "' + propertyName + '" at the ' + pathMatchedErrorContext);
        } else if (matchedElements.length > 1) {
          throw new Error('More than one object found with id property "' + arrayPropertyFilter + '" in array property "' + propertyName + '" at the ' + pathMatchedErrorContext);
        }
        // reassign current context to found element
        currentContext = matchedElements[0];
      } else {
        // reassign current context to found property
        currentContext = property;
      }

      if (pathMatchedSoFar) {
        pathMatchedSoFar += '.';
      }
      pathMatchedSoFar += pathElement;
    });

    return currentContext;
  },

  /**
   * @deprecated The method was renamed to {@link isObject}. Use the new name or consider using {@link isPojo} instead.
   */
  isPlainObject<T>(obj: T): obj is Exclude<typeof obj, Primitive | undefined | null | T[]> {
    return objects.isObject(obj);
  },

  /**
   * @returns true if the given object is an object: no primitive type (number, string, boolean, bigint, symbol), no array, not null and not undefined.
   */
  isObject<T>(obj: T): obj is Exclude<typeof obj, Primitive | undefined | null | T[]> {
    return typeof obj === 'object' &&
      !objects.isNullOrUndefined(obj) &&
      !Array.isArray(obj);
  },

  /**
   * Checks if the given object is a plain old JavaScript object, which is an object created by the object literal notation (`{}`), `Object.create` or `new Object()`.
   *
   * Note: objects without prototype (e.g. created using `Object.create(null)` or {@link objects.createMap}) return true here.
   * So methods typically <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object#instance_methods">inherited from Object</a> may not be available for a pojo!
   * @returns true if it is a pojo, false otherwise.
   */
  isPojo<T>(obj: T): obj is Exclude<typeof obj, Primitive | undefined | null | T[]> {
    if (!objects.isObject(obj)) {
      return false;
    }
    let prototype = Object.getPrototypeOf(obj);
    return prototype === Object.prototype || prototype === null;
  },

  /**
   * Null-safe access the property of objects. Instead of using this function consider using conditional chaining with the elvis operator: obj?.foo?.bar.
   * Examples:
   * <ul>
   * <li><code>optProperty(obj, 'value');</code> try to access and return obj.value</li>
   * <li><code>optProperty(obj, 'foo', 'bar');</code> try to access and return obj.foo.bar</li>
   * </ul>
   *
   * @returns the value of the requested property or undefined if the property does not exist on the object
   */
  optProperty(obj: object, ...properties: string[]): any {
    if (!obj) {
      return null;
    }

    let numArgs = properties.length;
    if (numArgs === 0) {
      return obj;
    }
    if (numArgs === 1) {
      return obj[properties[0]];
    }

    for (let i = 0; i < numArgs - 1; i++) {
      obj = obj[properties[i]];
      if (!obj) {
        return null;
      }
    }
    return obj[properties[numArgs - 1]];
  },

  /**
   * Returns true if:
   * - obj is not undefined or null
   * - obj not isNaN
   * - obj isFinite
   *
   * This method is handy in cases where you want to check if a number is set. Since you cannot write:
   *   if (myNumber) { ...
   *
   * Because when myNumber === 0 would also resolve to false. In that case use instead:
   *   if (isNumber(myNumber)) { ...
   */
  isNumber(obj: any): obj is number {
    return obj !== null && !isNaN(obj) && isFinite(obj) && !isNaN(parseFloat(obj));
  },

  isString(obj: any): obj is string {
    return typeof obj === 'string' || obj instanceof String;
  },

  isNullOrUndefined(obj: any): obj is null | undefined {
    return obj === null || obj === undefined;
  },

  // eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
  isFunction(obj: any): obj is Function {
    return $.isFunction(obj);
  },

  /**
   * Returns true if the given object is {@link isNullOrUndefined null or undefined} or {@link isEmpty empty}.
   */
  isNullOrUndefinedOrEmpty(obj: any): boolean {
    if (objects.isNullOrUndefined(obj)) {
      return true;
    }
    return objects.isEmpty(obj);
  },

  isArray(obj: any): obj is Array<any> {
    return Array.isArray(obj);
  },

  /**
   * Checks whether the provided value is a promise or not.
   * @param value The value to check.
   * @returns true, in case the provided value is a thenable, false otherwise.
   *
   * Note: This method checks whether the provided value is a "thenable" (see https://promisesaplus.com/#terminology).
   *       Checking for promise would require to check the behavior which is not possible. So you could provide an object
   *       with a "then" function that does not conform to the Promises/A+ spec but this method would still return true.
   */
  isPromise(value: any): value is PromiseLike<any> {
    return !!value && typeof value === 'object' && typeof value.then === 'function';
  },

  /**
   * Returns values from the given (map) object. By default, only values of 'own' properties are returned.
   *
   * @param obj
   * @param all can be set to true to return all properties instead of own properties
   * @returns an Array with values
   */
  values<K extends PropertyKey, V>(obj: Record<K, V>, all?: boolean): V[] {
    let values: V[] = [];
    if (obj) {
      if (typeof obj.hasOwnProperty !== 'function') {
        all = true;
      }
      for (let key in obj) {
        if (all || obj.hasOwnProperty(key)) {
          values.push(obj[key]);
        }
      }
    }
    return values;
  },

  /**
   * @returns the key (name) of a property with given value
   */
  keyByValue<V>(obj: Record<string, V>, value: V): string {
    return Object.keys(obj)[objects.values(obj).indexOf(value)];
  },

  /**
   * Java-like equals method.
   *
   * The two values are considered equal if one of the following rules applies:
   *
   * * They are the same objects (===).
   * * They are Dates having the same value.
   * * They are both zero-length collections (Array, Map or Set).
   * * They have both an `equals` method, are of the same Class type and this `equals` method returns `true`.
   *
   * @returns true if both values are equal.
   */
  equals(objA: any, objB: any): boolean {
    return !!equalsImpl(objA, objB); // equalsImpl might return null which means false.
  },

  /**
   * Compares two objects and all its child elements recursively using value equality as defined by {@link #equals}. Order of the property keys is ignored.
   *
   * @param objA The first value to compare.
   * @param objB The second value to compare.
   * @param skipRootEquals An optional boolean indicating if the equals method should be ignored for the given two objects. Default is false.
   * It might be handy to set this to true if it is called from within an equals method to prevent stack overflows.
   * @returns true if both objects and all child elements are equals by value or implemented equals method.
   * @see objects.equals
   */
  equalsRecursive(objA: any, objB: any, skipRootEquals = false): boolean {
    const equalsResult = equalsImpl(objA, objB, !skipRootEquals);
    if (equalsResult !== null) {
      return equalsResult;
    }

    // Map
    if (objA instanceof Map && objB instanceof Map) {
      return objects.equalsMap(objA, objB);
    }

    // Set
    if (objA instanceof Set && objB instanceof Set) {
      return objects.equalsSet(objA, objB, true);
    }

    // Objects
    if (objects.isObject(objA) && objects.isObject(objB)) {
      const keysA = Object.keys(objA);
      const keysB = Object.keys(objB);
      if (!arrays.equalsIgnoreOrder(keysA, keysB)) {
        return false;
      }
      for (const key of keysA) {
        if (!objects.equalsRecursive(objA[key], objB[key])) {
          return false;
        }
      }
      return true;
    }

    // Arrays
    if (objects.isArray(objA) && objects.isArray(objB)) {
      if (objA.length !== objB.length) {
        return false;
      }
      for (let i = 0; i < objA.length; i++) {
        if (!objects.equalsRecursive(objA[i], objB[i])) {
          return false;
        }
      }
      return true;
    }

    return false;
  },

  /**
   * Compares a list of properties of two objects by using the equals method for each property.
   */
  propertiesEquals(objA: object, objB: object, properties: string[]): boolean {
    let i, property;
    for (i = 0; i < properties.length; i++) {
      property = properties[i];
      if (!objects.equals(objA[property], objB[property])) {
        return false;
      }
    }
    return true;
  },

  /**
   * @returns the function identified by funcName from the given object. The function will return an error
   *     if that function does not exist. Use this function if you modify an existing framework function
   *     to find problems after refactoring / renaming as soon as possible.
   */
  // eslint-disable-next-line
  mandatoryFunction(obj: object, funcName: string): Function {
    let func = obj[funcName];
    if (!func || typeof func !== 'function') {
      throw new Error('Function \'' + funcName + '\' does not exist on object. Check if it has been renamed or moved. Object: ' + obj);
    }
    return func;
  },

  /**
   * Use this method to replace a function on a prototype of an object. It checks if that function exists
   * by calling <code>mandatoryFunction</code>.
   */
  // eslint-disable-next-line
  replacePrototypeFunction(obj: any, funcOrName: string | ((...args) => any), func: Function, rememberOrig: boolean) {
    let proto = obj.prototype;
    let funcName;
    if (typeof funcOrName === 'string') {
      funcName = funcOrName;
    } else {
      funcName = funcOrName.name;
    }
    objects.mandatoryFunction(proto, funcName);
    if (rememberOrig) {
      proto[funcName + 'Orig'] = proto[funcName];
    }
    proto[funcName] = func;
  },

  /**
   * @returns a real Array for the pseudo-array 'arguments'.
   */
  argumentsToArray(args: IArguments): any[] {
    return args ? Array.prototype.slice.call(args) : [];
  },

  /**
   * Used to loop over 'arguments' pseudo-array with forEach.
   */
  forEachArgument(args: IArguments, func: (value: any, index: number, args: any[]) => void) {
    return objects.argumentsToArray(args).forEach(func);
  },

  /**
   * @param value text which contains a constant reference like '${const:FormField.LabelPosition.RIGHT}'.
   * @returns the resolved constant value or the unchanged input value if the constant could not be resolved.
   */
  resolveConst(value: string, constType?: any): any {
    if (!objects.isString(value)) {
      return value;
    }

    let result = CONST_REGEX.exec(value);
    if (result && result.length === 2) {
      // go down the object hierarchy starting on the given constType-object or on 'window'
      let objectHierarchy = result[1].split('.');
      let obj = constType || window;
      for (let i = 0; i < objectHierarchy.length; i++) {
        obj = obj[objectHierarchy[i]];
        if (obj === undefined) {
          window.console.log('Failed to resolve constant \'' + result[1] + '\', object is undefined');
          return value;
        }
      }
      return obj;
    }
    return value;
  },

  resolveConstProperty(object: object, config: { property: string; constType: any }) {
    scout.assertProperty(config, 'property');
    scout.assertProperty(config, 'constType');
    let value = object[config.property];
    let resolvedValue = objects.resolveConst(value, config.constType);
    if (value !== resolvedValue) {
      object[config.property] = resolvedValue;
    }
  },

  resolveConstProperties(object: object, configs: { property: string; constType: any }[]) {
    arrays.ensure(configs).forEach(config => {
      objects.resolveConstProperty(object, config);
    });
  },

  /**
   * Cleans the given object, i.e. removes all top-level properties with values that are null, undefined or
   * consist of an empty array or an empty object. This is useful to have a minimal data object.
   *
   * This method is *not* recursive.
   *
   * The object is modified *in-place* and is also returned.
   *
   * If the given object is set but not a {@link isObject plain object}, an error is thrown.
   *
   * @see isNullOrUndefinedOrEmpty
   */
  removeEmptyProperties(object: any): any {
    if (objects.isNullOrUndefined(object)) {
      return object;
    }
    if (!objects.isObject(object)) {
      throw new Error('Not an object: ' + object);
    }

    // Attributes in DOs should not be removed but set to undefined so that they look the same as a new instance. Important when comparing DOs.
    const removeAttribute = object instanceof BaseDoEntity ? (key: string) => {
      object[key] = undefined;
    } : (key: string) => delete object[key];

    Object.keys(object).forEach(key => {
      if (objects.isNullOrUndefinedOrEmpty(object[key])) {
        removeAttribute(key);
      }
    });
    return object;
  },

  /**
   * Empty if the argument is:
   * - `null`
   * - `undefined`
   * - an empty {@link Array}
   * - an empty {@link Map}
   * - an empty {@link Set}
   * - or an object without keys (except {@link Date} which is never empty).
   *
   * @returns `true` if *obj* is empty, `false` if *obj* is not empty, `undefined` if *obj* is no object (e.g. a primitive).
   */
  isEmpty(obj: any): boolean | undefined {
    if (objects.isNullOrUndefined(obj)) {
      return true;
    }
    if (objects.isArray(obj)) {
      return arrays.empty(obj);
    }
    if (!objects.isObject(obj)) {
      return undefined;
    }
    if (obj instanceof Date) {
      return false;
    }
    if (obj instanceof Map) {
      return obj.size === 0;
    }
    if (obj instanceof Set) {
      return obj.size === 0;
    }
    return Object.keys(obj).length === 0;
  },

  /**
   * @returns true if the first parameter is the same or a subclass of the second parameter.
   */
  isSameOrExtendsClass<TClass2>(class1: any, class2: abstract new() => TClass2): class1 is new() => TClass2 {
    if (typeof class1 !== 'function' || typeof class2 !== 'function') {
      return false;
    }
    return class1 === class2 || class2.isPrototypeOf(class1);
  },

  /**
   * Converts any non-string argument to a string that can be used as an object property name.
   * Complex objects are converted to their JSON representation (instead of returning something
   * non-descriptive such as '[Object object]').
   */
  ensureValidKey(key: any): string {
    if (key === undefined) {
      return 'undefined';
    }
    if (objects.isString(key)) {
      return key;
    }
    return JSON.stringify(key);
  },

  /**
   * Receives the value for the given key from the map, if the value is not null or undefined.
   * If the key has no value associated, the value will be computed with the given function and added to the map, unless it is null or undefined.
   *
   * @returns the value associated with the given key or the computed value returned by the given mapping function.
   */
  getOrSetIfAbsent<TKey, TValue>(map: Map<TKey, TValue>, key: TKey, computeValue: (key: TKey) => TValue): TValue {
    let value = map.get(key);
    if (!objects.isNullOrUndefined(value)) {
      return value;
    }
    value = computeValue(key);
    if (!objects.isNullOrUndefined(value)) {
      map.set(key, value);
    }
    return value;
  },

  /**
   * Compares two Sets for equality using {@link equals}.
   *
   * @param setA First Set.
   * @param setB Second Set.
   * @param deep Specifies if a deep comparison should be performed (recursively). If the Set value is a non-primitive type the equals steps into these structures (Objects, Arrays, Maps, Sets). Default is false.
   */
  equalsSet(setA: Set<any>, setB: Set<any>, deep = false) {
    if (setA === setB) {
      return true;
    }
    if (!setA || !setB) {
      return false;
    }
    if (setA.size !== setB.size) {
      return false;
    }
    return equalsIterable(setA, setB, deep ? objects.equalsRecursive : objects.equals);
  },

  /**
   * Compares two Maps for equality using {@link equals} on keys and values.
   *
   * @param setA First Map.
   * @param setB Second Map.
   * @param deep Specifies if a deep comparison should be performed (recursively). If the Map key or value is a non-primitive type the equals steps into these structures (Objects, Arrays, Maps, Sets). Default is false.
   */
  equalsMap(mapA: Map<any, any>, mapB: Map<any, any>, deep = false): boolean {
    if (mapA === mapB) {
      return true;
    }
    if (!mapA || !mapB) {
      return false;
    }
    if (mapA.size !== mapB.size) {
      return false;
    }
    const equalsEntriesFunc = deep ? equalsMapEntriesRecursive : equalsMapEntries;
    return equalsIterable(mapA.entries(), mapB.entries(), equalsEntriesFunc);
  }
};

/**
 * Deep clone implementation for special classes. The following classes are supported:
 * * `Array`
 * * `Date`
 * * `Map`
 * * `Set`
 * * All objects having a `clone` function (expected to create a deep clone without taking any arguments). Widgets are excluded.
 * These are classes like `BaseDoEntity`, `Dimension`, `GridData`, `Insets`, `Point`, `Status`, `URL`, ...
 */
function deepCloneClass(val: any): any {
  if (!val) {
    return val;
  }

  // Date
  if (val instanceof Date) {
    return new Date(val.getTime());
  }

  // Map
  if (val instanceof Map) {
    const mapCopy = new Map();
    for (const [key, value] of val) {
      mapCopy.set(objects.valueCopy(key), objects.valueCopy(value));
    }
    return mapCopy;
  }

  // Set
  if (val instanceof Set) {
    const setCopy = new Set();
    for (const item of val) {
      setCopy.add(objects.valueCopy(item));
    }
    return setCopy;
  }

  if (!(val instanceof Widget)) { // clone for widgets makes no sense here so their clone() function is ignored
    // with clone() function. E.g. for BaseDoEntity, Dimension, GridData, Insets, Point, Status, URL, etc.
    const cloneFunction = val['clone'];
    if (objects.isFunction(cloneFunction)) {
      return cloneFunction.call(val); // expected to work without arguments and to create a deep clone.
    }
  }

  return val;
}

function equalsImpl(objA: any, objB: any, useEqualsFunc = true): boolean | null {
  if (objA === objB) {
    return true;
  }

  // both values are of the same type (which may be null)
  if (protoTypeOf(objA) !== protoTypeOf(objB)) {
    return false; // cannot be equal if different type
  }

  // dates
  if (objA instanceof Date) {
    return dates.equals(objA, objB);
  }

  // two empty arrays are equal
  if (objects.isArray(objA) && !objA.length && !objB.length) {
    return true;
  }

  // two empty maps/sets are equal
  if ((objA instanceof Map || objA instanceof Set) && !objA.size && !objB.size) {
    return true;
  }

  // both objects have an equals() function
  if (useEqualsFunc && objects.isFunction(objA?.equals) && objects.isFunction(objB?.equals)) {
    return objA.equals(objB);
  }

  return null; // = false
}

function equalsMapEntries(entryA: [any, any], entryB: [any, any]): boolean {
  const keyA = entryA[0];
  const valueA = entryA[1];
  const keyB = entryB[0];
  const valueB = entryB[1];
  return objects.equals(keyA, keyB) && objects.equals(valueA, valueB);
}

function equalsMapEntriesRecursive(entryA: [any, any], entryB: [any, any]): boolean {
  const keyA = entryA[0];
  const valueA = entryA[1];
  const keyB = entryB[0];
  const valueB = entryB[1];
  return objects.equalsRecursive(keyA, keyB) && objects.equalsRecursive(valueA, valueB);
}

function protoTypeOf(obj: any): any {
  return objects.isNullOrUndefined(obj) ? null : Object.getPrototypeOf(obj);
}

function equalsIterable<T>(setA: Iterable<T>, setB: Iterable<T>, equalsFunction: (a: T, b: T) => boolean): boolean {
  const copyB = Array.from(setB);
  for (const entry of setA) {
    const foundAt = arrays.findIndex(copyB, e => equalsFunction(entry, e));
    if (foundAt < 0) {
      return false;
    }
    copyB.splice(foundAt, 1); // remove item found
  }
  return true;
}
