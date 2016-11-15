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
scout.objects = {

  /**
   * Copies all the properties (including the ones from the prototype.) from dest to source
   * @memberOf scout.objects
   */
  copyProperties: function(source, dest) {
    var propertyName;
    for (propertyName in source) {
      dest[propertyName] = source[propertyName];
    }
  },

  /**
   * Copies the own properties (excluding the ones from the prototype) from dest to source
   * @memberOf scout.objects
   */
  copyOwnProperties: function(source, dest) {
    var propertyName;
    for (propertyName in source) {
      if (source.hasOwnProperty(propertyName)) {
        dest[propertyName] = source[propertyName];
      }
    }
  },

  /**
   * Counts and returns the properties of a given object.
   */
  countOwnProperties: function(obj) {
    var count = 0;
    for (var prop in obj) {
      if (obj.hasOwnProperty(prop)) {
        count++;
      }
    }
    return count;
  },

  /**
   * Copies the specified properties (including the ones from the prototype.) from dest to source.
   * Properties that already exist on dest are NOT overwritten.
   */
  extractProperties: function(source, dest, properties) {
    properties.forEach(function(propertyName) {
      if (!dest[propertyName]) {
        dest[propertyName] = source[propertyName];
      }
    });
    return dest;
  },

  /**
   * returns
   *  - true if the obj has at least one of the given properties.
   *  - false if the obj has none of the given properties.
   *
   * @param obj
   * @param properties a single property or an array of properties
   * @returns {Boolean}
   */
  someOwnProperties: function(obj, properties) {
    var propArr = scout.arrays.ensure(properties);
    return propArr.some(function(prop) {
      return obj.hasOwnProperty(prop);
    });
  },

  /**
   * returns
   *  - true if the obj or its prototypes have at least one of the given properties.
   *  - false if the obj or its prototypes have none of the given properties.
   *
   * @param obj
   * @param properties a single property or an array of properties
   * @returns {Boolean}
   */
  someProperties: function(obj, properties) {
    var propArr = scout.arrays.ensure(properties);
    return propArr.some(function(prop) {
      return prop in obj;
    });
  },

  valueCopy: function(obj) {
    // Nothing to be done for immutable things
    if (obj === undefined || obj === null || typeof obj !== 'object') {
      return obj;
    }
    var copy;
    // Arrays
    if (Array.isArray(obj)) {
      copy = [];
      for (var i = 0; i < obj.length; i++) {
        copy[i] = scout.objects.valueCopy(obj[i]);
      }
      return copy;
    }
    // All other objects
    copy = {};
    for (var prop in obj) {
      if (obj.hasOwnProperty(prop)) {
        copy[prop] = scout.objects.valueCopy(obj[prop]);
      }
    }
    return copy;
  },

  /**
   * Returns the first object with the given property and propertyValue or null if there is no such object within parentObj.
   * @param parentObj
   * @param property property to search for
   * @param propertyValue value of the property
   * @returns {Object}
   */
  findChildObjectByKey: function(parentObj, property, propertyValue) {
    if (parentObj === undefined || parentObj === null || typeof parentObj !== 'object') {
      return null;
    }
    if (parentObj[property] === propertyValue) {
      return parentObj;
    }
    var child;
    if (Array.isArray(parentObj)) {
      for (var i = 0; i < parentObj.length; i++) {
        child = scout.objects.findChildObjectByKey(parentObj[i], property, propertyValue);
        if (child) {
          return child;
        }
      }
    }
    for (var prop in parentObj) {
      if (parentObj.hasOwnProperty(prop)) {
        child = scout.objects.findChildObjectByKey(parentObj[prop], property, propertyValue);
        if (child) {
          return child;
        }
      }
    }
    return null;
  },

  /**
   * Returns true if the given object is an object but _not_ an array.
   */
  isPlainObject: function(obj) {
    return (typeof obj === 'object' && !Array.isArray(obj));
  },

  /**
   * Returns the given property if the object is truthy.
   */
  optProperty: function(obj, property) {
    if (obj) {
      return obj[property];
    }
    return null;
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
   *
   * @param obj
   * @returns {Boolean}
   */
  isNumber: function(obj) {
    return obj !== null && !isNaN(obj) && isFinite(obj);
  },

  isString: function(obj) {
    return typeof obj === 'string';
  },

  isNullOrUndefined: function(obj) {
    return obj === null || obj === undefined;
  },

  /**
   * Returns values from the given (map) object. By default only values of 'own' properties are returned.
   *
   * @returns {Array} an Array with values
   * @param {Object} obj
   * @param {boolean} [all] can be set to true to return all properties instead of own properties
   */
  values: function(obj, all) {
    var values = [];
    if (obj) {
      for (var key in obj) {
        if (all || obj.hasOwnProperty(key)) {
          values.push(obj[key]);
        }
      }
    }
    return values;
  },

  /**
   * Java-like equals method. Compares the given objects by checking with ===, if that fails, the function
   * checks if both objects have an equals function and use the equals function to compare the two objects
   * by value.
   * @returns True if both objects are equals by reference or by value
   */
  equals: function(objA, objB) {
    if (objA === objB) {
      return true;
    }
    // both objects have an equals() method
    if ((objA && objB) && (objA.equals && objB.equals)) {
      return objA.equals(objB);
    }
    return false;
  },

  /**
   * Compares a list of properties of two objects by using the equals method for each property.
   */
  propertiesEquals: function(objA, objB, properties) {
    var i, property;
    for (i = 0; i < properties.length; i++) {
      property = properties[i];
      if (!scout.objects.equals(objA[property], objB[property])) {
        return false;
      }
    }
    return true;
  },

  /**
   * @returns the function identified by funcName from the given object. The function will return an error
   *     if that function does not exist. Use this function if you modify an existing framework function
   *     to find problems after refactorings / renamings as soon as possible.
   */
  mandatoryFunction: function(obj, funcName) {
    var func = obj[funcName];
    if (!func || typeof func !== 'function') {
      throw new Error('Function \'' + funcName + '\' does not exist on object. Check if it has been renamed or moved.', obj);
    }
    return func;
  },

  /**
   * Use this method to replace a function on a prototype of an object. It checks if that function exists
   * by calling <code>mandatoryFunction</code>.
   */
  replacePrototypeFunction: function(obj, funcName, func) {
    var proto = obj.prototype;
    this.mandatoryFunction(proto, funcName);
    proto[funcName] = func;
  },

  /**
   * @returns a real Array for the pseudo-array 'arguments'.
   */
  argumentsToArray: function(args) {
    return Array.prototype.slice.call(args);
  },

  /**
   * Used to loop over 'arguments' pseudo-array with forEach.
   */
  forEachArgument: function(args, func) {
    return this.argumentsToArray(args).forEach(func);
  },

  /**
   * TODO [5.2] bsh: Document
   *
   * How to use:
   *   scout.objects.checkFunctionOverrides().join('\n')
   */
  checkFunctionOverrides: function() {
    var whitelist = [
      'ModelAdapter.init',
      'ModelAdapter._init',
      'Calendar.init'
    ];
    var result1 = [
      'Legend:',
      '[!] Function includes super call, and parent function uses arguments',
      ' ~  Function includes super call, but parent function does not use arguments',
      '    Function does not include super call',
      '',
      'Wrong number of arguments:'
    ];
    var result2 = ['Different argument names:'];

    for (var prop in scout) {
      if (!scout.hasOwnProperty(prop)) {
        continue;
      }
      var o = scout[prop];
      // Only check functions that have a "parent"
      if (typeof o === 'function' && o.parent) {
        for (var name in o.prototype) {
          if (!o.prototype.hasOwnProperty(name)) {
            continue;
          }
          var fn = o.prototype[name];
          // Ignore constructor, inherited properties and non-functions
          if (name === 'constructor' || !o.prototype.hasOwnProperty(name) || typeof fn !== 'function') {
            continue;
          }
          var args = getFunctionArguments(fn);
          // Check all parents
          var parent = o.parent;
          while (parent) {
            var parentFn = parent.prototype[name];
            if (parent.prototype.hasOwnProperty(name) && typeof parentFn === 'function') {
              var parentArgs = getFunctionArguments(parentFn);
              // Check arguments (at least all of the parent args must be present)
              var mismatch = false;
              for (var i = 0; i < parentArgs.length; i++) {
                if (args.length < i || args[i] !== parentArgs[i]) {
                  mismatch = true;
                  break;
                }
              }
              var fname = prop + '.' + name;
              if (mismatch && whitelist.indexOf(fname) === -1) { // && args.length !== parentArgs.length) {
                // Collect found mismatch
                var result = fname + '(' + args.join(', ') + ') does not correctly override ' + getPrototypeOwner(parentFn) + '.' + name + '(' + parentArgs.join(', ') + ')';
                var includesSuperCall = (fn.toString().match(new RegExp('scout.' + scout.strings.quote(prop) + '.parent.prototype.' + scout.strings.quote(name) + '.call\\(')) !== null);
                var parentFunctionUsesArguments = false;
                if (includesSuperCall) {
                  for (var j = 0; j < parentArgs.length; j++) {
                    var m = parentFn.toString().match(new RegExp('[^.\\w]' + scout.strings.quote(parentArgs[j]) + '[^\\w]', 'g'));
                    if (m !== null && m.length > 1) {
                      parentFunctionUsesArguments = true;
                      break;
                    }
                  }
                }
                result = (includesSuperCall ? (parentFunctionUsesArguments ? '[!]' : ' ~ ') : '   ') + ' ' + result;
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
      var FN_COMMENTS = /\/\*.*?\*\/|\/\/.*$/mg; // removes comments in function declaration
      var FN_ARGS = /^function[^(]*\((.*?)\)/m; // fetches all arguments in m[1]

      if (typeof fn !== 'function') {
        throw new Error('Argument is not a function: ' + fn);
      }

      var m = fn.toString().replace(FN_COMMENTS, '').match(FN_ARGS);
      var args = [];
      if (m !== null) {
        m[1].split(',').forEach(function(arg, i) {
          arg = arg.trim();
          if (arg.length > 0) {
            args.push(arg);
          }
        });
      }
      return args;
    }

    function getPrototypeOwner(fx) {
      for (var prop in scout) {
        if (!scout.hasOwnProperty(prop)) {
          continue;
        }
        var o = scout[prop];
        if (typeof o === 'function') {
          for (var name in o.prototype) {
            if (!o.prototype.hasOwnProperty(name)) {
              continue;
            }
            var fn = o.prototype[name];
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

};
