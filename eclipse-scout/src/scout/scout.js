import * as $ from 'jquery';
import * as arrays from './util/arrays';
import ObjectFactory from './ObjectFactory';

export let objectFactories = {};

export function create(objectType, model = null, options = null) {
  return ObjectFactory.getInstance().create(objectType, model, options);
}

export function isPlainObject(obj) {
  return typeof obj === 'object' &&
    !isNullOrUndefined(obj) &&
    !Array.isArray(obj);
}

export function isFunction(obj) {
  return typeof obj === 'function';
}

export function isNullOrUndefined(obj) {
  return obj === null || obj === undefined;
}

export function assertParameter(parameterName, value, type) {
  if (isNullOrUndefined(value)) {
    throw new Error('Missing required parameter \'' + parameterName + '\'');
  }
  if (type && !(value instanceof type)) {
    throw new Error('Parameter \'' + parameterName + '\' has wrong type');
  }
  return value;
};

export function nvl() {
  var result;
  for (var i = 0; i < arguments.length; i++) {
    result = arguments[i];
    if (result !== undefined && result !== null) {
      break;
    }
  }
  return result;
};

export function equals(objA, objB) {
  if (objA === objB) {
    return true;
  }
  // both objects have an equals() method
  if ((objA && objB) && (objA.equals && objB.equals)) {
    return objA.equals(objB);
  }
  return false;
}

export function extractProperties(source, dest, properties) {
  properties.forEach(function(propertyName) {
    if (dest[propertyName] === undefined) {
      dest[propertyName] = source[propertyName];
    }
  });
  return dest;
}

export function isActiveElement(element) {
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

export function reloadPage(options) {
  options = options || {};
  if (options.schedule) {
    setTimeout(reloadPageImpl);
  } else {
    reloadPageImpl();
  }

  // ----- Helper functions -----

  function reloadPageImpl() {
    // Hide everything (on entire page, not only $entryPoint)
    if (nvl(options.clearBody, true)) {
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

export function isOneOf(value) {
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

export function someProperties(obj, properties) {
  var propArr = arrays.ensure(properties);
  return propArr.some(function(prop) {
    return prop in obj;
  });
}

export function argumentsToArray(args) {
  return args ? Array.prototype.slice.call(args) : [];
}

export function installGlobalMouseDownInterceptor(myDocument) {
  /*myDocument.addEventListener('mousedown', function(event) {
      scout.ValueField.invokeValueFieldAboutToBlurByMouseDown(event.target || event.srcElement);
  }, true); // true=the event handler is executed in the capturing phase*/
};

export function prepareDOM(targetDocument) {
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

export function widget(widgetIdOrElement, partId) {

};

// FIXME [awe] ES6: review this change to object-factory pattern
export function addObjectFactories(objectFactories0) {
  objectFactories = $.extend(objectFactories, objectFactories0);
};

// FIXME [awe] ES6: this is only required for ES5 clients, what to do with it?
export function inherits(childCtor, parentCtor) {
  childCtor.prototype = Object.create(parentCtor.prototype);
  childCtor.prototype.constructor = childCtor;
  childCtor.parent = parentCtor;
};
