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

import {App, Device, ObjectFactory, objects, strings, ValueField, widgets} from './index';
import * as $ from 'jquery';

let $activeElements = null;
let objectFactories = {};

/**
 * Returns the first of the given arguments that is not null or undefined. If no such element
 * is present, the last argument is returned. If no arguments are given, undefined is returned.
 */
export function nvl() {
  var result;
  for (var i = 0; i < arguments.length; i++) {
    result = arguments[i];
    if (result !== undefined && result !== null) {
      break;
    }
  }
  return result;
}

/**
 * Use this method in your functions to assert that a mandatory parameter is passed
 * to the function. Throws an error when value is not set.
 *
 * @param type (optional) if this parameter is set, the given value must be of this type (instanceof check)
 * @return the value (for direct assignment)
 */
export function assertParameter(parameterName, value, type) {
  if (objects.isNullOrUndefined(value)) {
    throw new Error('Missing required parameter \'' + parameterName + '\'');
  }
  if (type && !(value instanceof type)) {
    throw new Error('Parameter \'' + parameterName + '\' has wrong type');
  }
  return value;
}

/**
 * Use this method to assert that a mandatory property is set. Throws an error when value is not set.
 *
 * @param type (optional) if this parameter is set, the value must be of this type (instanceof check)
 * @return the value (for direct assignment)
 */
export function assertProperty(object, propertyName, type) {
  var value = object[propertyName];
  if (objects.isNullOrUndefined(value)) {
    throw new Error('Missing required property \'' + propertyName + '\'');
  }
  if (type && !(value instanceof type)) {
    throw new Error('Property \'' + propertyName + '\' has wrong type');
  }
  return value;
}

/**
 * Checks if one of the arguments from 1-n is equal to the first argument.
 * @param value
 * @param arguments to check against the value, may be an array or a variable argument list.
 */
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
}

/**
 * Creates a new object instance.<p> Delegates the create call to scout.ObjectFactory#create.
 * @returns {object}
 */
export function create(objectType, model, options) {
  return ObjectFactory.get().create(objectType, model, options);
}

/**
 * Prepares the DOM for scout in the given document. This should be called once while initializing scout.
 * If the target document is not specified, the global "document" variable is used instead.
 *
 * This is used by apps (App, LoginApp, LogoutApp)
 *
 * Currently it does the following:
 * - Remove the <noscript> tag (obviously there is no need for it).
 * - Remove <scout-text> tags (they must have been processed before, see texts.readFromDOM())
 * - Remove <scout-version> tag (it must have been processed before, see scout.App._initVersion())
 * - Add a device / browser class to the body tag to allow for device specific CSS rules.
 * - If the browser is Google Chrome, add a special meta header to prevent automatic translation.
 */
export function prepareDOM(targetDocument) {
  targetDocument = targetDocument || document;
  // Cleanup DOM
  $('noscript', targetDocument).remove();
  $('scout-text', targetDocument).remove();
  $('scout-version', targetDocument).remove();
  $('body', targetDocument).addDeviceClass();

  // Prevent "Do you want to translate this page?" in Google Chrome
  if (Device.get().browser === Device.Browser.CHROME) {
    var metaNoTranslate = '<meta name="google" content="notranslate" />';
    var $title = $('head > title', targetDocument);
    if ($title.length === 0) {
      // Add to end of head
      $('head', targetDocument).append(metaNoTranslate);
    } else {
      $title.after(metaNoTranslate);
    }
  }
}

/**
 * Installs a global 'mousedown' interceptor to invoke 'aboutToBlurByMouseDown' on value field before anything else gets executed.
 */
export function installGlobalMouseDownInterceptor(myDocument) {
  myDocument.addEventListener('mousedown', function(event) {
    ValueField.invokeValueFieldAboutToBlurByMouseDown(event.target || event.srcElement);
  }, true); // true=the event handler is executed in the capturing phase
}

/**
 * Because Firefox does not set the active state of a DOM element when the mousedown event
 * for that element is prevented, we set an 'active' CSS class instead. This means in the
 * CSS we must deal with :active and with .active, where we need same behavior for the
 * active state across all browsers.
 * <p>
 * Typically you'd write something like this in your CSS:
 *   button:active, button.active { ... }
 */
export function installSyntheticActiveStateHandler(myDocument) {
  if (Device.get().requiresSyntheticActiveState()) {
    $activeElements = [];
    $(myDocument)
      .on('mousedown', function(event) {
        var $element = $(event.target);
        while ($element.length) {
          $activeElements.push($element.addClass('active'));
          $element = $element.parent();
        }
      })
      .on('mouseup', function() {
        $activeElements.forEach(function($element) {
          $element.removeClass('active');
        });
        $activeElements = [];
      });
  }
}

/**
 * Resolves the widget using the given widget id or HTML element.
 * <p>
 * If the argument is a string or a number, it will search the widget hierarchy for the given id using Widget#widget(id).
 * If the argument is a HTML or jQuery element, it will use widgets.get() to get the widget which belongs to the given element.
 *
 * @param widgetIdOrElement
 *          a widget ID or a HTML or jQuery element
 * @param [partId]
 *          partId of the session the widget belongs to (optional, only relevant if the
 *          argument is a widget ID). If omitted, the first session is used.
 * @returns
 *          the widget for the given element or id
 */
export function widget(widgetIdOrElement, partId) {
  if (objects.isNullOrUndefined(widgetIdOrElement)) {
    return null;
  }
  var $elem = widgetIdOrElement;
  if (typeof widgetIdOrElement === 'string' || typeof widgetIdOrElement === 'number') {
    // Find widget for ID
    var session = scout.getSession(partId);
    if (session) {
      widgetIdOrElement = strings.asString(widgetIdOrElement);
      return session.root.widget(widgetIdOrElement);
    }
  }
  return widgets.get($elem);
}

/**
 * Helper function to get the model adapter for a given adapterId. If there is more than one
 * session, e.g. in case of portlets, the second argument specifies the partId of the session
 * to be queried. If not specified explicitly, the first session is used. If the session or
 * the adapter could not be found, null is returned.
 */
export function adapter(adapterId, partId) {
  if (objects.isNullOrUndefined(adapterId)) {
    return null;
  }
  var session = scout.getSession(partId);
  if (session && session.modelAdapterRegistry) {
    return session.modelAdapterRegistry[adapterId];
  }
  return null;
}

export function getSession(partId) {
  let sessions = App.get().sessions;
  if (!sessions) {
    return null;
  }
  if (objects.isNullOrUndefined(partId)) {
    return sessions[0];
  }
  for (var i = 0; i < sessions.length; i++) {
    var session = sessions[i];
    // noinspection EqualityComparisonWithCoercionJS
    if (session.partId == partId) { // <-- compare with '==' is intentional! (NOSONAR)
      return session;
    }
  }
  return null;
}

/**
 * This method exports the adapter with the given ID as JSON, it returns an plain object containing the
 * configuration of the adapter. You can transform that object into JSON by calling <code>JSON.stringify</code>.
 * This method can only be called through the browser JavaScript console.
 * Here's an example of how to call the method:
 *
 * JSON.stringify(scout.exportAdapter(4))
 *
 * @param adapterId
 */
export function exportAdapter(adapterId, partId) {
  var session = scout.getSession(partId);
  if (session && session.modelAdapterRegistry) {
    var adapter = session.getModelAdapter(adapterId);
    if (!adapter) {
      return null;
    }
    var adapterData = cloneAdapterData(adapterId);
    resolveAdapterReferences(adapter, adapterData);
    adapterData.type = 'model'; // property 'type' is required for models.js
    return adapterData;
  }

  // ----- Helper functions -----

  function cloneAdapterData(adapterId) {
    var adapterData = session.getAdapterData(adapterId);
    adapterData = $.extend(true, {}, adapterData);
    return adapterData;
  }

  function resolveAdapterReferences(adapter, adapterData) {
    var tmpAdapter, tmpAdapterData;
    adapter.widget._widgetProperties.forEach(function(WidgetPropertyName) {
      var WidgetPropertyValue = adapterData[WidgetPropertyName];
      if (!WidgetPropertyValue) {
        return; // nothing to do when property is null
      }
      if (Array.isArray(WidgetPropertyValue)) {
        // value is an array of adapter IDs
        var adapterDataArray = [];
        WidgetPropertyValue.forEach(function(adapterId) {
          tmpAdapter = session.getModelAdapter(adapterId);
          tmpAdapterData = cloneAdapterData(adapterId);
          resolveAdapterReferences(tmpAdapter, tmpAdapterData);
          adapterDataArray.push(tmpAdapterData);
        });
        adapterData[WidgetPropertyName] = adapterDataArray;
      } else {
        // value is an adapter ID
        tmpAdapter = session.getModelAdapter(WidgetPropertyValue);
        tmpAdapterData = cloneAdapterData(WidgetPropertyValue);
        resolveAdapterReferences(tmpAdapter, tmpAdapterData);
        adapterData[WidgetPropertyName] = tmpAdapterData;
      }
    });
    adapterData = adapter.exportAdapterData(adapterData);
  }
}

/**
 * Reloads the entire browser window.
 *
 * Options:
 *   [schedule]
 *     If true, the page reload is not executed in the current thread but scheduled using setTimeout().
 *     This is useful if the caller wants to execute some other code before the reload. The default is false.
 *   [clearBody]
 *     If true, the body is cleared first before the reload is performed. This is useful to prevent
 *     showing "old" content in the browser until the new content arrives. The default is true.
 *   [redirectUrl]
 *      The new URL to load. If not specified, the current location is used (window.location).
 */
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
}

export function addObjectFactories(factories) {
  objectFactories = $.extend(objectFactories, factories);
}

export function cloneShallow(template, properties, createUniqueId) {
  assertParameter('template', template);
  var clone = Object.create(Object.getPrototypeOf(template));
  Object.getOwnPropertyNames(template)
    .forEach((key) => {
      clone[key] = template[key];
    });
  if (properties) {
    for (let key in properties) {
      clone[key] = properties[key];
    }
  }
  if (nvl(createUniqueId, true)) {
    clone.id = ObjectFactory.get().createUniqueId();
  }
  if (clone.cloneOf === undefined) {
    clone.cloneOf = template;
  }
  return clone;
}

export default {
  nvl,
  assertParameter,
  assertProperty,
  isOneOf,
  create,
  prepareDOM,
  installGlobalMouseDownInterceptor,
  installSyntheticActiveStateHandler,
  widget,
  adapter,
  getSession,
  exportAdapter,
  reloadPage,
  addObjectFactories,
  objectFactories,
  cloneShallow
}
