/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
const jasmineScoutCloneMatchers = {
  definedProperty: (util, customEqualityTesters) => ({
    compare: (original, clone, property) => {
      let result = {
        pass: clone[property] !== undefined
      };
      if (!result.pass) {
        result.message = clone.objectType + ' does not have the property ' + property + ' [original:' + original[property] + ', clone:' + clone[property] + ']!';
      }
      return result;

    }
  }),
  sameProperty: (util, customEqualityTesters) => ({
    compare: (original, clone, property) => {
      let result = {
        pass: original[property] === clone[property]
      };
      if (!result.pass) {
        result.message = 'property \'' + property + '\' is not the same [original: \'' + original[property] + '\', clone: \'' + clone[property] + '\'].';
      }
      return result;

    }
  }),
  widgetCloneProperty: (util, customEqualityTesters) => ({
    compare: (original, clone, property) => {
      let compareWidget = (originalWidget, clonedWidget, propertyName) => {
        if (originalWidget === clonedWidget) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' is same on [original: \'' + original[property] + '\', clone: \'' + clone[property] + '\']. It should be a deep copy.'
          };
        }
        if (originalWidget.objectType !== clonedWidget.objectType) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' has not same object type of clone and orignal. [original.objectType: \'' + originalWidget.objectType + '\', clonedWidget.objectType: \'' + clonedWidget.objectType + '\'].'
          };
        }
        if (clonedWidget.parent !== clone) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' has a wrong parent in clone (widget parent and clone should be same). [clone: \'' + clone + '\', widget.parent: \'' + clonedWidget.parent + '\'].'
          };
        }
        if (originalWidget !== clonedWidget.cloneOf) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' cloneOf of clone is not set correctly. [original: \'' + originalWidget + '\', clone.cloneOf: \'' + clonedWidget.cloneOf + '\'].'
          };
        }
        return {
          pass: true
        };

      };
      if (original[property] === clone[property]) {
        return {
          pass: false,
          message: 'widgetProperty \'' + property + '\' is same on [original: \'' + original[property] + '\', clone: \'' + clone[property] + '\']. It should be a deep copy.'
        };
      }
      if (Array.isArray(original[property])) {
        if (!Array.isArray(clone[property])) {
          return {
            pass: false,
            message: 'widgetProperty \'' + property + '\' is not an array [original: \'' + original[property] + '\', clone: \'' + clone[property] + '\']. It should be a deep copy.'
          };
        }
        for (let i = 0; i < original[property].length; i++) {
          let result = compareWidget(original[property][i], clone[property][i], property);
          if (!result.pass) {
            return result;
          }
        }
      }
      return {
        pass: true
      };

    }
  })
};

beforeEach(() => {
  jasmine.addMatchers(jasmineScoutCloneMatchers);
});
