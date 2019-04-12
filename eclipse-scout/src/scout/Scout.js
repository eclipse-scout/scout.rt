import * as $ from 'jquery';
import Arrays from './Utils/Arrays';
import ObjectFactory from './ObjectFactory';
import Strings from './Utils/Strings';

// FIXME [awe] add style folder again

export default class Scout { // FIXME [awe] check if we should use a Singleton? upper/lowercase

  static create(objectType, model = null, options = null) {
    return ObjectFactory.getInstance().create(objectType, model, options);
  }

  static isPlainObject(obj) {
    return typeof obj === 'object' &&
      !Scout.isNullOrUndefined(obj) &&
      !Array.isArray(obj);
  }

  static isFunction(obj) {
    return typeof obj === 'function';
  }

  static isNullOrUndefined(obj) {
    return obj === null || obj === undefined;
  }

  static assertParameter(parameterName, value, type) {
    if (Scout.isNullOrUndefined(value)) {
      throw new Error('Missing required parameter \'' + parameterName + '\'');
    }
    if (type && !(value instanceof type)) {
      throw new Error('Parameter \'' + parameterName + '\' has wrong type');
    }
    return value;
  };

  static nvl() {
    var result;
    for (var i = 0; i < arguments.length; i++) {
      result = arguments[i];
      if (result !== undefined && result !== null) {
        break;
      }
    }
    return result;
  };

  static equals(objA, objB) {
    if (objA === objB) {
      return true;
    }
    // both objects have an equals() method
    if ((objA && objB) && (objA.equals && objB.equals)) {
      return objA.equals(objB);
    }
    return false;
  }

  static extractProperties(source, dest, properties) {
    properties.forEach(function(propertyName) {
      if (dest[propertyName] === undefined) {
        dest[propertyName] = source[propertyName];
      }
    });
    return dest;
  }

  static isActiveElement(element) {
    if (!element) {
      return false;
    }
    var activeElement;
    if (element instanceof $) {
      activeElement = element.activeElement(true);
      element = element[0];
    } else {
      activeElement = (element instanceof Document ? element : element.ownerDocument).activeElement;
    }
    return activeElement === element;
  }

  static reloadPage(options) {
    options = options || {};
    if (options.schedule) {
      setTimeout(reloadPageImpl);
    } else {
      reloadPageImpl();
    }

    // ----- Helper functions -----

    function reloadPageImpl() {
      // Hide everything (on entire page, not only $entryPoint)
      if (Scout.nvl(options.clearBody, true)) {
        $('body').html('');
      }

      // Reload window (using setTimeout, to overcome drawing issues in IE)
      setTimeout(function() {
        if (options.redirectUrl) {
          window.location.href = options.redirectUrl;
        } else {
          window.location.reload();
        }
      });
    }
  };

  static isOneOf(value) {
    if (arguments.length < 2) {
      return false;
    }
    var argsToCheck;
    if (arguments.length === 2 && Array.isArray(arguments[1])) {
      argsToCheck = arguments[1];
    } else {
      argsToCheck = Array.prototype.slice.call(arguments, 1);
    }
    return argsToCheck.indexOf(value) !== -1;
  };

  static someProperties(obj, properties) {
    var propArr = Arrays.ensure(properties);
    return propArr.some(function(prop) {
      return prop in obj;
    });
  }

  static argumentsToArray(args) {
    return args ? Array.prototype.slice.call(args) : [];
  }

  static installGlobalMouseDownInterceptor(myDocument) {
    /*myDocument.addEventListener('mousedown', function(event) {
        scout.ValueField.invokeValueFieldAboutToBlurByMouseDown(event.target || event.srcElement);
    }, true); // true=the event handler is executed in the capturing phase*/
  };

  static prepareDOM(targetDocument) {
    targetDocument = targetDocument || document;
    // Cleanup DOM
    //$('noscript', targetDocument).remove();
    //$('scout-Text', targetDocument).remove();
    $('scout-version', targetDocument).remove();
    //$('body', targetDocument).addDeviceClass();

    // Prevent 'Do you want to translate this page?' in Google Chrome
    //if (scout.device.browser === scout.Device.Browser.CHROME) {
    var metaNoTranslate = '<meta name="google" content="notranslate" />';
    var $title = $('head > title', targetDocument);
    if ($title.length === 0) {
      // Add to end of head
      $('head', targetDocument).append(metaNoTranslate);
    } else {
      $title.after(metaNoTranslate);
    }
    //}
  };
}
