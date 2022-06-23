/*
 * Copyright (c) 2014-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, scout, strings} from '../index';
import $ from 'jquery';

const CONST_REGEX = /\${const:([^}]*)}/;

/**
 * Uses Object.create(null) to create an object without a prototype. This is different to use the literal {} which links the object to Object.prototype.
 * <p>
 * Not using the literal has the advantage that the object does not contain any inherited properties like `toString` so it is not necessary to use `o.hasOwnProperty(p)`
 * instead of `p in o` to check for the existence.
 *
 * @param [object] properties optional initial properties to be set on the new created object
 */
export function createMap(properties) {
  let map = Object.create(null);
  if (properties) {
    $.extend(map, properties);
  }
  return map;
}

/**
 * Copies all the properties (including the ones from the prototype.) from dest to source
 * @param {[]} [filter] an array of property names.
 * @returns {object} the destination object (the destination parameter will be modified as well)
 */
export function copyProperties(source, dest, filter) {
  let propertyName;
  filter = arrays.ensure(filter);
  for (propertyName in source) {
    if (filter.length === 0 || filter.indexOf(propertyName) !== -1) {
      dest[propertyName] = source[propertyName];
    }
  }
  return dest;
}

/**
 * Copies the own properties (excluding the ones from the prototype) from dest to source.
 * If a filter is specified, only the properties matching the ones in the filter are copied.
 * @param {[]} [filter] an array of property names.
 * @returns {object} the destination object (the destination parameter will be modified as well)
 */
export function copyOwnProperties(source, dest, filter) {
  let propertyName;
  filter = arrays.ensure(filter);
  for (propertyName in source) {
    if (Object.prototype.hasOwnProperty.call(source, propertyName) && (filter.length === 0 || filter.indexOf(propertyName) !== -1)) {
      dest[propertyName] = source[propertyName];
    }
  }
  return dest;
}

/**
 * Counts and returns the properties of a given object or map (see #createMap).
 */
export function countOwnProperties(obj) {
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
}

/**
 * Copies the specified properties (including the ones from the prototype) from source to dest.
 * Properties that already exist on dest are NOT overwritten.
 */
export function extractProperties(source, dest, properties) {
  properties.forEach(propertyName => {
    if (dest[propertyName] === undefined) {
      dest[propertyName] = source[propertyName];
    }
  });
  return dest;
}

/**
 * returns
 *  - true if the obj has at least one of the given properties.
 *  - false if the obj has none of the given properties.
 *
 * @param obj
 * @param properties a single property or an array of properties
 * @returns {Boolean}
 */
export function someOwnProperties(obj, properties) {
  let propArr = arrays.ensure(properties);
  return propArr.some(prop => {
    return Object.prototype.hasOwnProperty.call(obj, prop);
  });
}

/**
 * returns
 *  - true if the obj or its prototypes have at least one of the given properties.
 *  - false if the obj or its prototypes have none of the given properties.
 *
 * @param obj
 * @param properties a single property or an array of properties
 * @returns {Boolean}
 */
export function someProperties(obj, properties) {
  let propArr = arrays.ensure(properties);
  return propArr.some(prop => {
    return prop in obj;
  });
}

/**
 * @return {*}
 */
export function valueCopy(obj) {
  // Nothing to be done for immutable things
  if (obj === undefined || obj === null || typeof obj !== 'object') {
    return obj;
  }
  let copy;
  // Arrays
  if (Array.isArray(obj)) {
    copy = [];
    for (let i = 0; i < obj.length; i++) {
      copy[i] = valueCopy(obj[i]);
    }
    return copy;
  }
  // All other objects
  copy = {};
  for (let prop in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, prop)) {
      copy[prop] = valueCopy(obj[prop]);
    }
  }
  return copy;
}

/**
 * Returns the first object with the given property and propertyValue or null if there is no such object within parentObj.
 * @param parentObj
 * @param property property to search for
 * @param propertyValue value of the property
 * @returns {Object}
 */
export function findChildObjectByKey(parentObj, property, propertyValue) {
  if (parentObj === undefined || parentObj === null || typeof parentObj !== 'object') {
    return null;
  }
  if (parentObj[property] === propertyValue) {
    return parentObj;
  }
  let child;
  if (Array.isArray(parentObj)) {
    for (let i = 0; i < parentObj.length; i++) {
      child = findChildObjectByKey(parentObj[i], property, propertyValue);
      if (child) {
        return child;
      }
    }
  }
  for (let prop in parentObj) {
    if (Object.prototype.hasOwnProperty.call(parentObj, prop)) {
      child = findChildObjectByKey(parentObj[prop], property, propertyValue);
      if (child) {
        return child;
      }
    }
  }
  return null;
}

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
 * @return Object Returns the selected object.
 * @throws Throws an error, if the provided parameters are malformed, or a property could not be found/a id property filter does not find any elements.
 */
export function getByPath(object, path) {
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
    if (isNullOrUndefined(currentContext)) {
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
}

/**
 * Returns true if the given object is an object, _not_ an array and not null or undefined.
 */
export function isPlainObject(obj) {
  return typeof obj === 'object' &&
    !isNullOrUndefined(obj) &&
    !Array.isArray(obj);
}

/**
 * Null-safe access the property of an objects. Examples:
 * <ul>
 * <li><code>optProperty(obj, 'value');</code> try to access and return obj.value</li>
 * <li><code>optProperty(obj, 'foo', 'bar');</code> try to access and return obj.foo.bar</li>
 * </ul>
 *
 * @returns {*} the value of the requested property or undefined if the property does not exist on the object
 */
export function optProperty(obj, ...properties) {
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
}

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
 *
 * @param obj
 * @returns {Boolean}
 */
export function isNumber(obj) {
  return obj !== null && !isNaN(obj) && isFinite(obj) && !isNaN(parseFloat(obj));
}

export function isString(obj) {
  return typeof obj === 'string' || obj instanceof String;
}

export function isNullOrUndefined(obj) {
  return obj === null || obj === undefined;
}

export function isFunction(obj) {
  return $.isFunction(obj);
}

export function isArray(obj) {
  return Array.isArray(obj);
}

/**
 * Checks whether the provided value is a promise or not.
 * @param {any} value The value to check.
 * @return {boolean} Returns true, in case the provided value is a thenable, false otherwise.
 *
 * Note: This method checks whether the provided value is a "thenable" (see https://promisesaplus.com/#terminology).
 *       Checking for promise would require to check the behavior which is not possible. So you could provide an object
 *       with a "then" function that does not conform to the Promises/A+ spec but this method would still return true.
 */
export function isPromise(value) {
  return !!value && typeof value === 'object' && typeof value.then === 'function';
}

/**
 * Returns values from the given (map) object. By default only values of 'own' properties are returned.
 *
 * @returns {Array} an Array with values
 * @param {Object} obj
 * @param {boolean} [all] can be set to true to return all properties instead of own properties
 */
export function values(obj, all) {
  let values = [];
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
}

/**
 * @returns {string} the key / name of a property
 */
export function keyByValue(obj, value) {
  return Object.keys(obj)[values(obj).indexOf(value)];
}

/**
 * Java-like equals method. Compares the given objects by checking with ===, if that fails, the function
 * checks if both objects have an equals function and use the equals function to compare the two objects
 * by value.
 * @returns {boolean} true if both objects are equals by reference or by value
 */
export function equals(objA, objB) {
  if (objA === objB) {
    return true;
  }
  // both objects have an equals() method
  if (objA && objB && (objA.equals && objB.equals)) {
    return objA.equals(objB);
  }
  return false;
}

/**
 * Compare two objects and all its child elements recursively.
 * @returns {boolean} true if both objects and all child elements are equals by value or implemented equals method
 */
export function equalsRecursive(objA, objB) {
  let i;
  if (isPlainObject(objA) && isPlainObject(objB)) {
    if (isFunction(objA.equals) && isFunction(objB.equals)) {
      return objA.equals(objB);
    }
    let keysA = Object.keys(objA);
    let keysB = Object.keys(objB);
    if (!arrays.equalsIgnoreOrder(keysA, keysB)) {
      return false;
    }
    for (i = 0; i < keysA.length; i++) {
      if (!equalsRecursive(objA[keysA[i]], objB[keysA[i]])) {
        return false;
      }
    }
    return true;
  } else if (isArray(objA) && isArray(objB)) {
    if (objA.length !== objB.length) {
      return false;
    }
    for (i = 0; i < objA.length; i++) {
      if (!equalsRecursive(objA[i], objB[i])) {
        return false;
      }
    }

    return true;
  }
  return objA === objB;
}

/**
 * Compares a list of properties of two objects by using the equals method for each property.
 */
export function propertiesEquals(objA, objB, properties) {
  let i, property;
  for (i = 0; i < properties.length; i++) {
    property = properties[i];
    if (!equals(objA[property], objB[property])) {
      return false;
    }
  }
  return true;
}

/**
 * @returns {function} the function identified by funcName from the given object. The function will return an error
 *     if that function does not exist. Use this function if you modify an existing framework function
 *     to find problems after refactorings / renamings as soon as possible.
 */
export function mandatoryFunction(obj, funcName) {
  let func = obj[funcName];
  if (!func || typeof func !== 'function') {
    throw new Error('Function \'' + funcName + '\' does not exist on object. Check if it has been renamed or moved. Object: ' + obj);
  }
  return func;
}

/**
 * Use this method to replace a function on a prototype of an object. It checks if that function exists
 * by calling <code>mandatoryFunction</code>.
 */
export function replacePrototypeFunction(obj, funcName, func, rememberOrig) {
  let proto = obj.prototype;
  mandatoryFunction(proto, funcName);
  if (rememberOrig) {
    proto[funcName + 'Orig'] = proto[funcName];
  }
  proto[funcName] = func;
}

/**
 * @returns a real Array for the pseudo-array 'arguments'.
 */
export function argumentsToArray(args) {
  return args ? Array.prototype.slice.call(args) : [];
}

/**
 * Used to loop over 'arguments' pseudo-array with forEach.
 */
export function forEachArgument(args, func) {
  return argumentsToArray(args).forEach(func);
}

/**
 * Development utility to check if overrides in JavaScript "classes" are correct.
 *
 * How to use:
 *   1. Start application in development mode (non-minimized).
 *   2. Open browser's development console
 *   3. Type: checkFunctionOverrides().join('\n')
 */
export function checkFunctionOverrides() {
  let whitelist = [
    'ModelAdapter.init',
    'ModelAdapter._init',
    'Calendar.init'
  ];
  let result1 = [
    'Legend:',
    '[!] Function includes super call, and parent function uses arguments',
    ' ~  Function includes super call, but parent function does not use arguments',
    '    Function does not include super call',
    '',
    'Wrong number of arguments:'
  ];
  let result2 = ['Different argument names:'];

  for (let prop in scout) {
    if (!scout.hasOwnProperty(prop)) {
      continue;
    }
    let o = scout[prop];
    // Only check functions that have a "parent"
    if (typeof o === 'function' && o.parent) {
      for (let name in o.prototype) {
        if (!o.prototype.hasOwnProperty(name)) {
          continue;
        }
        let fn = o.prototype[name];
        // Ignore constructor, inherited properties and non-functions
        if (name === 'constructor' || !o.prototype.hasOwnProperty(name) || typeof fn !== 'function') {
          continue;
        }
        let args = getFunctionArguments(fn);
        // Check all parents
        let parent = o.parent;
        while (parent) {
          let parentFn = parent.prototype[name];
          if (parent.prototype.hasOwnProperty(name) && typeof parentFn === 'function') {
            let parentArgs = getFunctionArguments(parentFn);
            // Check arguments (at least all of the parent args must be present)
            let mismatch = false;
            for (let i = 0; i < parentArgs.length; i++) {
              if (args.length < i || args[i] !== parentArgs[i]) {
                mismatch = true;
                break;
              }
            }
            let fname = prop + '.' + name;
            if (mismatch && whitelist.indexOf(fname) === -1) { // && args.length !== parentArgs.length) {
              // Collect found mismatch
              let result = fname + '(' + args.join(', ') + ') does not correctly override ' + getPrototypeOwner(parentFn) + '.' + name + '(' + parentArgs.join(', ') + ')';
              let includesSuperCall = fn.toString().match(new RegExp('scout.' + strings.quote(prop) + '.parent.prototype.' + strings.quote(name) + '.call\\(')) !== null;
              let parentFunctionUsesArguments = false;
              if (includesSuperCall) {
                for (let j = 0; j < parentArgs.length; j++) {
                  let m = parentFn.toString().match(new RegExp('[^.\\w]' + strings.quote(parentArgs[j]) + '[^\\w]', 'g'));
                  if (m !== null && m.length > 1) {
                    parentFunctionUsesArguments = true;
                    break;
                  }
                }
              }
              result = (includesSuperCall ? parentFunctionUsesArguments ? '[!]' : ' ~ ' : '   ') + ' ' + result;
              if (args.length !== parentArgs.length) {
                result1.push(result);
              } else {
                result2.push(result);
              }
            }
          }
          parent = parent.parent;
        }
      }
    }
  }

  result1.push('');
  return result1.concat(result2);

  // ----- Helper functions -----

  function getFunctionArguments(fn) {
    let FN_COMMENTS = /\/\*.*?\*\/|\/\/.*$/mg; // removes comments in function declaration
    let FN_ARGS = /^function[^(]*\((.*?)\)/m; // fetches all arguments in m[1]

    if (typeof fn !== 'function') {
      throw new Error('Argument is not a function: ' + fn);
    }

    let m = fn.toString().replace(FN_COMMENTS, '')
      .match(FN_ARGS);
    let args = [];
    if (m !== null) {
      m[1].split(',').forEach((arg, i) => {
        arg = arg.trim();
        if (arg.length > 0) {
          args.push(arg);
        }
      });
    }
    return args;
  }

  function getPrototypeOwner(fx) {
    for (let prop in scout) {
      if (!scout.hasOwnProperty(prop)) {
        continue;
      }
      let o = scout[prop];
      if (typeof o === 'function') {
        for (let name in o.prototype) {
          if (!o.prototype.hasOwnProperty(name)) {
            continue;
          }
          let fn = o.prototype[name];
          // Ignore constructor, inherited properties and non-functions
          if (name === 'constructor' || !o.prototype.hasOwnProperty(name) || typeof fn !== 'function') {
            continue;
          }
          if (fn === fx) {
            return prop;
          }
        }
      }
    }
    return '';
  }
}

/**
 * @param {string} value text which contains a constant reference like '${const:FormField.LabelPosition.RIGHT}'.
 * @return {any} the resolved constant value or the unchanged input value if the constant could not be resolved.
 */
export function resolveConst(value, constType) {
  if (!isString(value)) {
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
}

/**
 * @param object config An object with 2 properties: property, constType
 */
export function resolveConstProperty(object, config) {
  scout.assertProperty(config, 'property');
  scout.assertProperty(config, 'constType');
  let value = object[config.property];
  let resolvedValue = resolveConst(value, config.constType);
  if (value !== resolvedValue) {
    object[config.property] = resolvedValue;
  }
}

/**
 * @param {object} obj
 * @returns {Boolean|undefined}
 *  - true if the obj is empty, null or undefined
 *  - false if the obj is not empty
 *  - nothing if the obj is not an object
 */
export function isEmpty(obj) {
  if (isNullOrUndefined(obj)) {
    return true;
  }
  if (!isPlainObject(obj)) {
    return;
  }
  return Object.keys(obj).length === 0;
}

/**
 * Converts any non-string argument to a string that can be used as an object property name.
 * Complex objects are converted to their JSON representation (instead of returning something
 * non-descriptive such as '[Object object]').
 */
export function ensureValidKey(key) {
  if (key === undefined) {
    return 'undefined';
  }
  if (isString(key)) {
    return key;
  }
  return JSON.stringify(key);
}

export default {
  CONST_REGEX,
  argumentsToArray,
  checkFunctionOverrides,
  copyOwnProperties,
  copyProperties,
  countOwnProperties,
  createMap,
  ensureValidKey,
  equals,
  equalsRecursive,
  extractProperties,
  findChildObjectByKey,
  getByPath,
  forEachArgument,
  isArray,
  isEmpty,
  isFunction,
  isNullOrUndefined,
  isNumber,
  isPlainObject,
  isString,
  isPromise,
  keyByValue,
  mandatoryFunction,
  optProperty,
  propertiesEquals,
  replacePrototypeFunction,
  resolveConst,
  resolveConstProperty,
  someOwnProperties,
  someProperties,
  valueCopy,
  values
};
