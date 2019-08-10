/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.CloneSpecHelper = function(session) {
  this.session = session;
};

scout.CloneSpecHelper.prototype.validateClone = function(original, clone, localProperties) {
  var properties = original._cloneProperties.filter(function(prop) {
      return original._widgetProperties.indexOf(prop) < 0;
    }.bind(this)),
    widgetProperties = original._cloneProperties.filter(function(prop) {
      return original._widgetProperties.indexOf(prop) > -1;
    }.bind(this));

  // simple properties to be cloned
  properties.forEach(function(prop) {
    expect(clone).definedProperty(original, prop);
    expect(original).sameProperty(clone, prop);
  }.bind(this));

  // widget properties to be cloned
  widgetProperties.forEach(function(prop) {
    expect(clone).definedProperty(original, prop);

    expect(original).widgetCloneProperty(clone, prop);
  }.bind(this));

};

scout.CloneSpecHelper.CUSTOM_MATCHER = {
  definedProperty: function(util, customEqualityTesters) {
    return {
      compare: function(original, clone, property) {
        var result = {
          pass: clone[property] !== undefined
        };
        if (!result.pass) {
          result.message = clone.objectType + ' does not have the property ' + property + ' [original:' + original[property] + ', clone:' + clone[property] + ']!';
        }
        return result;

      }
    };
  },
  sameProperty: function(util, customEqualityTesters) {
    return {
      compare: function(original, clone, property) {
        var result = {
          pass: original[property] === clone[property]
        };
        if (!result.pass) {
          result.message = 'property \'' + property + '\' is not the same [original: \'' + original[property] + '\', clone: \'' + clone[property] + '\'].';
        }
        return result;

      }
    };
  },
  widgetCloneProperty: function(util, customEqualityTesters) {
    return {
      compare: function(original, clone, property) {
        var compareWidget = function(originalWidget, clonedWidget, propertyName) {
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

        }.bind(this);
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
          for (var i = 0; i < original[property].length; i++) {
            var result = compareWidget(original[property][i], clone[property][i], property);
            if (!result.pass) {
              return result;
            }
          }
        }
        return {
          pass: true
        };

      }
    };
  }
};
