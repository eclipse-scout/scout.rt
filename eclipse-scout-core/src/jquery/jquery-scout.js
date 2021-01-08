/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/**
 * jQuery plugin with scout extensions
 */
import $ from 'jquery';
import {arrays, Device, Dimension, events, IconDesc, icons, objects, scout, strings} from '../index';

/**
 * By using $ in jsdoc all the functions is this file are recognized as part of $.
 * By additionally defining $ as jQuery all default jQuery functions are recognized as well.
 * "JQuery" (with a capital J) refers to the the type definitions "@types/jquery".
 * @typedef {JQuery|jQuery} $
 */

// === internal methods ===

/**
 * Returns false when the component display is 'none', otherwise true.
 *
 * Note: this gives other results than $.is(':visible'), since that method will also return false
 * when a component has absolute positioning and no width and height is defined (well, you cannot
 * see a component with a style like this, but technically it is not set to 'not visible').
 *
 * Also note that this function _only_ checks the 'display' property! Other methods to make an element
 * invisible to the user ('visibility: hidden', 'opacity: 0', off-screen position etc.) are _not_
 * considered.
 */
function elemVisible(elem) {
  // Check if element itself is hidden by its own style attribute
  if (!elem || isHidden(elem.style)) {
    return false;
  }
  // Must use correct window for element / computedStyle
  let myWindow = (elem instanceof Document ? elem : elem.ownerDocument).defaultView;
  // In some cases with popup windows the window object may be already set to null
  // but we still have a valid reference to a DOM element. In that case we assume
  // the element is not visible anymore.
  if (!myWindow) {
    return false;
  }
  // Check if element itself is hidden by external style-sheet
  if (isHidden(myWindow.getComputedStyle(elem))) {
    return false;
  }
  // Else visible
  return true;

  // ----- Helper functions -----

  function isHidden(style) {
    return style.display === 'none';
  }
}

function explodeShorthandProperties(properties) {
  let newProperties = [];
  properties.forEach(prop => {
    // shorthand css properties may not be copied directly (at least not in firefox) -> copy the actual properties
    if (prop === 'margin' || prop === 'padding') {
      newProperties.push(prop + '-top');
      newProperties.push(prop + '-right');
      newProperties.push(prop + '-bottom');
      newProperties.push(prop + '-left');
    } else if (prop === 'border') {
      newProperties.push('border-top-style');
      newProperties.push('border-right-style');
      newProperties.push('border-bottom-style');
      newProperties.push('border-left-style');

      newProperties.push('border-top-color');
      newProperties.push('border-right-color');
      newProperties.push('border-bottom-color');
      newProperties.push('border-left-color');

      newProperties.push('border-top-width');
      newProperties.push('border-right-width');
      newProperties.push('border-bottom-width');
      newProperties.push('border-left-width');
    } else {
      newProperties.push(prop);
    }
  });
  return newProperties;
}

/**
 * This function is required because most jQuery functions can be used with or without arguments
 * and do return the jQuery instance when used as a setter (with arguments), ceiling should only
 * be done, when used as getter (without arguments).
 */
function _ceilNumber(val) {
  return objects.isNumber(val) ? Math.ceil(val) : val;
}

// === $ extensions ===

/* !
 * jQuery UI Widget 1.11.2
 * http://jqueryui.com
 *
 * Copyright 2014 jQuery Foundation and other contributors
 * Released under the MIT license.
 * http://jquery.org/license
 *
 * http://api.jqueryui.com/jQuery.widget/
 */
const __origCleanData = $.cleanData;
/**
 * This function is copied from jQuery UI. It is used to fire a 'remove' event
 * when we call the .remove() function on a jQuery object.
 */
$.cleanData = elems => {
  let events, elem, i;
  for (i = 0; (elem = elems[i]); i++) { // NOSONAR
    try {
      // Only trigger remove when necessary to save time
      events = $._data(elem, 'events');
      if (events && events.remove) {
        $(elem).triggerHandler('remove');
      }
      // http://bugs.jquery.com/ticket/8235
    } catch (e) {
      // NOP
    }
  }
  __origCleanData(elems);
};

/**
 * Used by some animate functions.
 */
$.removeThis = function() {
  $(this).remove();
};

/**
 * Convenience function that can be used as an jQuery event handler, when this
 * event should be "swallowed". Technically, this function calls preventDefault(),
 * stopPropagation() and stopImmediatePropagation() on the event.
 *
 * Note: "return false" is equal to preventDefault() and stopPropagation(), but
 * not stopImmediatePropagation().
 */
$.suppressEvent = event => {
  if (event) {
    event.preventDefault();
    event.stopPropagation();
    event.stopImmediatePropagation();
  }
};

/**
 * If the event target is disabled (according to $.fn.isEnabled()), the event is suppressed
 * and the method returns true. Otherwise, false is returned.
 */
$.suppressEventIfDisabled = (event, $target) => {
  $target = $target || $(event.target);
  if (!$target.isEnabled()) {
    $.suppressEvent(event);
    return true;
  }
  return false;
};

/**
 * Implements the 'debounce' pattern. The given function fx is executed after a certain delay
 * (in milliseconds), but if the same function is called a second time within the waiting time,
 * depending on the 'reschedule' option, the timer is reset or the second call is ignored.
 * The default value for the 'delay' option is 250 ms.
 *
 * The resulting function has a function member 'cancel' that can be used to clear any scheduled
 * calls to the original function. If no such call was scheduled, cancel() returns false,
 * otherwise true.
 *
 * OPTION         DEFAULT VALUE   DESCRIPTION
 * ------------------------------------------------------------------------------------
 * delay          250             Waiting time in milliseconds before the function is executed.
 *
 * reschedule     true            Defines whether subsequent call to the debounced function
 *                                within the waiting time cause the timer to be reset or not.
 *                                If the reschedule option is 'false', subsequent calls within
 *                                the waiting time will just be ignored.
 *
 * @param fx
 *          the function to wrap
 * @param [options]
 *          an optional options object (see table above). Short-hand version: If a number is passed instead
 *          of an object, the value is automatically converted to the option 'delay'.
 */
$.debounce = (fx, options) => {
  if (typeof options === 'number') {
    options = {
      delay: options
    };
  }
  options = $.extend({
    delay: 250,
    reschedule: true
  }, options);

  let timeoutId = null;
  let fn = function(...args) {
    let that = this;

    if (timeoutId && !options.reschedule) {
      // Function is already schedule but 'reschedule' option is set to false --> discard this request
      return;
    }
    if (timeoutId) {
      // Function is already scheduled --> cancel current scheduled call and re-schedule the call
      clearTimeout(timeoutId);
    }
    timeoutId = setTimeout(() => {
      timeoutId = null;
      fx.apply(that, args);
    }, options.delay);
  };
  fn.cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
      timeoutId = null;
      return true;
    }
    return false;
  };
  return fn;
};

/**
 * Executes the given function. Further calls to the same function are delayed by the given delay
 * (default 250ms). This is similar to $.debounce() but ensures that function is called at least
 * every 'delay' milliseconds. Can be useful to prevent too many function calls, e.g. from UI events.
 */
$.throttle = (fx, delay) => {
  delay = (delay !== undefined) ? delay : 250;
  let timeoutId = null;
  let lastExecution;
  return function(...args) {
    let that = this;
    let now = new Date().getTime();
    let callFx = () => {
      lastExecution = now;
      fx.apply(that, args);
    };
    if (lastExecution && lastExecution + delay > now) {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        callFx();
      }, delay);
    } else {
      callFx();
    }
  };
};

/**
 * Returns a function which negates the return value of the given function when called.
 */
$.negate = fx => function(...args) {
  return !fx.apply(this, args);
};

/**
 * color calculation
 */
$.colorOpacity = (hex, opacity) => {
  // validate hex string
  hex = String(hex).replace(/[^0-9a-f]/gi, '');
  if (hex.length < 6) {
    hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2];
  }
  opacity = opacity || 0;

  // convert to decimal and change luminosity
  let rgb = '#';
  for (let i = 0; i < 3; i++) {
    let c = parseInt(hex.substr(i * 2, 2), 16);
    c = Math.round(Math.min(Math.max(0, 255 - (255 - c) * opacity), 255)).toString(16);
    rgb += ('00' + c).substr(c.length);
  }

  return rgb;
};

/**
 * CSP-safe method to dynamically load and execute a script from server.
 *
 * A new <script> tag is added to the document's head element. The methods returns
 * a promise which can be used to execute code after the loading has been completed.
 * A jQuery object referring to the new script tag is passed to the promise's
 * callback functions.
 *
 *   $.injectScript('http://server/path/script.js')
 *     .done(function($scriptTag) { ... });
 *
 * Options (optional):
 *
 * NAME              DEFAULT             DESCRIPTION
 * --------------------------------------------------------------------------------------------
 * document          window.document     Which document to inject the script to.
 *
 * removeTag         false               Whether to remove the script tag again from the DOM
 *                                       after the script has been loaded.
 */
$.injectScript = (url, options) => {
  options = options || {};
  let deferred = $.Deferred();

  let myDocument = options.document || window.document;
  let linkTag = myDocument.createElement('script');
  $(linkTag)
    .attr('src', url)
    .attr('async', true)
    .on('load error', event => {
      if (options.removeTag) {
        myDocument.head.removeChild(linkTag);
      }
      if (event.type === 'error') {
        deferred.reject($(linkTag));
      } else {
        deferred.resolve($(linkTag));
      }
    });
  // Use raw JS function to append the <script> tag, because jQuery handles
  // script tags specially (see "domManip" function) and uses eval() which
  // is not CSP-safe.
  myDocument.head.appendChild(linkTag);

  return deferred.promise();
};

/**
 * CSP-safe method to dynamically load a style sheet from server.
 *
 * A new <link> tag is added to the document's head element. The methods returns
 * a promise which can be used to execute code after the loading has been completed.
 * A jQuery object referring to the new link tag is passed to the promise's
 * callback functions.
 *
 *   $.injectStyleSheet('http://server/path/style.css')
 *     .done(function($linkTag) { ... });
 *
 * Options (optional):
 *
 * NAME              DEFAULT             DESCRIPTION
 * --------------------------------------------------------------------------------------------
 * document          window.document     Which document to inject the style sheet to.
 */
$.injectStyleSheet = (url, options) => {
  options = options || {};
  let deferred = $.Deferred();

  let myDocument = options.document || window.document;
  let linkTag = myDocument.createElement('link');
  $(linkTag)
    .attr('rel', 'stylesheet')
    .attr('type', 'text/css')
    .attr('href', url)
    .on('load error', event => {
      if (event.type === 'error') {
        deferred.reject($(linkTag));
      } else {
        deferred.resolve($(linkTag));
      }
    });
  // Use raw JS function to append the <script> tag, because jQuery handles
  // script tags specially (see "domManip" function) and uses eval() which
  // is not CSP-safe.
  myDocument.head.appendChild(linkTag);

  return deferred.promise();
};

/**
 * Dynamically adds styles to the document.
 *
 * A new <style> tag is added to the document's head element. The methods returns
 * a jQuery object referring to the new style tag.
 *
 *   $styleTag = $.injectStyle('p { text-color: orange; }');
 *
 * Options (optional):
 *
 * NAME              DEFAULT             DESCRIPTION
 * --------------------------------------------------------------------------------------------
 * document          window.document     Which document to inject the style to.
 */
$.injectStyle = (data, options) => {
  options = options || {};

  let myDocument = options.document || window.document;
  let styleTag = myDocument.createElement('style');
  let $styleTag = $(styleTag);
  $styleTag
    .attr('type', 'text/css')
    .html(data);
  myDocument.head.appendChild(styleTag);

  return $styleTag;
};

$.pxToNumber = pixel => {
  if (!pixel) {
    // parseFloat would return NaN if pixel is '' or undefined
    return 0;
  }
  // parseFloat ignores 'px' and just extracts the number
  return parseFloat(pixel);
};

/**
 * Use this function as shorthand of this:
 * <code>$.Deferred().resolve([arguments]);</code>
 *
 * @param {object[]} [args] of this function are passed to the resolve function of the deferred
 * @returns {$.Deferred} a deferred for an already resolved jQuery.Deferred object.
 */
$.resolvedDeferred = (...args) => {
  let deferred = $.Deferred();
  deferred.resolve(...args);
  return deferred;
};

/**
 * Use this function as shorthand of this:
 * <code>$.Deferred().resolve([arguments]).promise();</code>
 *
 * @param {...*} [args] of this function are passed to the resolve function of the deferred
 * @returns {Promise} a promise for an already resolved jQuery.Deferred object.
 */
$.resolvedPromise = (...args) => {
  let deferred = $.Deferred();
  deferred.resolve(...args);
  return deferred.promise();
};

/**
 * Use this function as shorthand of this:
 * <code>$.Deferred().reject([arguments]).promise();</code>
 *
 * @param {object[]} [arguments] of this function are passed to the reject function of the deferred
 * @returns {Promise} a promise for an already rejected jQuery.Deferred object.
 */
$.rejectedPromise = (...args) => {
  let deferred = $.Deferred();
  deferred.reject(...args);
  return deferred.promise();
};

/**
 * Creates a new promise which resolves when all promises resolve and fails when the first promise fails.
 *
 * @param {boolean} [asArray] (optional) when set to true, the resolve function will transform the
 *    flat arguments list containing the results into an array. The arguments of the reject function won't be touched. Default is false.
 */
$.promiseAll = (promises, asArray) => {
  asArray = scout.nvl(asArray, false);
  promises = arrays.ensure(promises);
  let deferred = $.Deferred();
  $.when(...promises).done((...args) => {
    if (asArray) {
      deferred.resolve(args);
    } else {
      deferred.resolve(...args);
    }
  }).fail((...args) => {
    deferred.reject(...args);
  });
  return deferred.promise();
};

/**
 * Shorthand for an AJAX request for a JSON file with UTF8 encoding.
 * Errors are caught and converted to a rejected promise with the following
 * arguments: jqXHR, textStatus, errorThrown, requestOptions.
 *
 * @returns a promise from JQuery function $.ajax
 */
$.ajaxJson = url => $.ajax({
  url: url,
  dataType: 'json',
  contentType: 'application/json; charset=UTF-8'
}).catch(function(...args) {
  // Reject the promise with usual arguments (jqXHR, textStatus, errorThrown), but add the request
  // options as additional argument (e.g. to make the URL available to the error handler)
  args.push(this);
  return $.rejectedPromise(...args);
});

/**
 * Ensures the given parameter is a jQuery object.<p>
 * If it is a jQuery object, it will be returned as it is.
 * If it isn't, it will be passed to $() in order to create one.
 * <p>
 * Just using $() on an existing jQuery object would clone it which would work but is unnecessary.
 * @returns $
 */
$.ensure = $elem => {
  if ($elem instanceof $) {
    return $elem;
  }
  return $($elem);
};

/**
 * Helper function to determine if an object is of type "jqXHR" (http://api.jquery.com/jQuery.ajax/#jqXHR)
 */
$.isJqXHR = obj => (typeof obj === 'object' && obj.hasOwnProperty('readyState') && obj.hasOwnProperty('status') && obj.hasOwnProperty('statusText'));

// === $.prototype extensions ===

$.fn.nvl = function($element) {
  if (this.length || !($element instanceof $)) {
    return this;
  }
  return $element;
};

/**
 * @param element string. Example = &lt;input&gt;
 * @param cssClass (optional) class attribute
 * @param text (optional) adds a child text-node with given text (no HTML content)
 */
$.fn.makeElement = function(element, cssClass, text) {
  let myDocument = this.document(true);
  if (myDocument === undefined || element === undefined) {
    return new Error('missing arguments: document, element');
  }
  let $element = $(element, myDocument);
  if (cssClass) {
    $element.addClass(cssClass);
  }
  if (text) {
    $element.text(text);
  }
  return $element;
};

/**
 * Creates a DIV element in the current document.
 *
 * @param cssClass (optional) string added to the 'class' attribute
 * @param text (optional) string used as inner text
 */
$.fn.makeDiv = function(cssClass, text) {
  return this.makeElement('<div>', cssClass, text);
};

$.fn.makeSpan = function(cssClass, text) {
  return this.makeElement('<span>', cssClass, text);
};

/**
 * @param {boolean} [domElement] (optional) if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
 * @returns {Document} document reference (ownerDocument) of the HTML element.
 */
$.fn.document = function(domElement) {
  let myDocument = this.length ? (this[0] instanceof Document ? this[0] : this[0].ownerDocument) : null;
  return domElement ? myDocument : $(myDocument);
};

/**
 * @param {boolean} [domElement] (optional) if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
 * @returns {Window|$} window reference (defaultView) of the HTML element
 */
$.fn.window = function(domElement) {
  let myDocument = this.document(true),
    myWindow = myDocument ? myDocument.defaultView : null;
  return domElement ? myWindow : $(myWindow);
};

/**
 * @param {boolean} [domElement] (optional) if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
 * @returns {$|HTMLElement} the active element of the current document
 */
$.fn.activeElement = function(domElement) {
  let myDocument = this.document(true),
    activeElement = myDocument ? myDocument.activeElement : null;
  return domElement ? activeElement : $(activeElement);
};

/**
 * @param {boolean} [domElement] (optional) if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
 * @returns {$|HTMLElement} the BODY element of the HTML document in which the current HTML element is placed.
 */
$.fn.body = function(domElement) {
  let $body = $('body', this.document(true));
  return domElement ? $body[0] : $body;
};

/**
 * @param {boolean} [domElement] (optional) if true the result is returned as DOM element, otherwise it is returned as jQuery object. The default is false.
 * @returns {$|HTMLElement} the closest DOM element that has the 'scout' class.
 */
$.fn.entryPoint = function(domElement) {
  let $element = this.closest('.scout');
  return domElement ? $element[0] : $element;
};

/**
 * @returns {Dimension} size of the window (width and height)
 */
$.fn.windowSize = function() {
  let $window = this.window();
  return new Dimension($window.width(), $window.height());
};

/**
 * Returns the element at the given point considering only child elements and elements matching the selector, if specified.
 */
$.fn.elementFromPoint = function(x, y, selector) {
  let $container = $(this),
    doc = $container.document(true),
    elements = [],
    i = 0,
    $element;

  if (!doc) {
    // If doc is null the $container itself is the document
    doc = $container[0];
  }
  if (!doc) {
    // If doc is still null (e.g. because the current jQuery collection does not contain any elements) return an empty collection
    return $();
  }

  // eslint-disable-next-line no-constant-condition
  while (true) {
    $element = $(doc.elementFromPoint(x, y));
    if ($element.length === 0 || $element[0] === doc.documentElement) {
      break;
    }
    if ($container.isOrHas($element) && (!selector || $element.is(selector))) {
      break;
    }
    elements.push($element);
    // make the element invisible to get the underlying element (uses visibility: hidden to make sure element size and position won't be changed)
    $element.addClass('invisible');
    i++;
    if (i > 1000) {
      $.log.warn('Infinite loop aborted', $element);
      $element = $();
      break;
    }
  }

  if ($element[0] === doc.documentElement && $container[0] !== doc) {
    // return an empty element if the only element found is the document element and the document element is not the container
    $element = $();
  }

  elements.forEach($element => {
    // show element again
    $element.removeClass('invisible');
  });
  return $element;
};

// prepend - and return new div for chaining
$.fn.prependDiv = function(cssClass, text) {
  return this.makeDiv(cssClass, text).prependTo(this);
};

// append - and return new div for chaining
$.fn.appendDiv = function(cssClass, text) {
  return this.makeDiv(cssClass, text).appendTo(this);
};

$.fn.prependElement = function(element, cssClass, text) {
  return this.makeElement(element, cssClass, text).prependTo(this);
};

$.fn.appendElement = function(element, cssClass, text) {
  return this.makeElement(element, cssClass, text).appendTo(this);
};

// insert after - and return new div for chaining
$.fn.afterDiv = function(cssClass, text) {
  return this.makeDiv(cssClass, text).insertAfter(this);
};

// insert before - and return new div for chaining
$.fn.beforeDiv = function(cssClass, text) {
  return this.makeDiv(cssClass, text).insertBefore(this);
};

$.fn.appendSpan = function(cssClass, text) {
  return this.makeSpan(cssClass, text).appendTo(this);
};

$.fn.appendBr = function(cssClass) {
  return this.makeElement('<br>', cssClass).appendTo(this);
};

$.fn.appendTextNode = function(text) {
  return $(this.document(true).createTextNode(text)).appendTo(this);
};

/**
 * @param {IconDesc|string} iconId
 */
$.fn.appendIcon = function(iconId, cssClass) {
  if (!iconId) {
    return this.appendSpan(cssClass)
      .addClass('icon');
  }
  let icon;
  if (iconId instanceof IconDesc) {
    icon = iconId;
  } else {
    icon = icons.parseIconId(iconId);
  }
  if (icon.isFontIcon()) {
    return this.makeSpan(cssClass, icon.iconCharacter)
      .addClass('icon')
      .addClass(icon.appendCssClass('font-icon'))
      .appendTo(this);
  }
  return this.appendImg(icon.iconUrl, cssClass)
    .addClass('icon image-icon');
};

$.fn.appendImg = function(imageSrc, cssClass) {
  let $icon = this.appendElement('<img>', cssClass);
  if (imageSrc) {
    $icon.attr('src', imageSrc);
  }
  return $icon;
};

$.fn.makeSVG = function(type, cssClass, text, id) {
  let myDocument = this.document(true);
  if (myDocument === undefined || type === undefined) {
    return new Error('missing arguments: document, type');
  }
  let $svg = $(myDocument.createElementNS('http://www.w3.org/2000/svg', type));
  if (cssClass) {
    $svg.attr('class', cssClass);
  }
  if (text) {
    $svg.text(text);
  }
  if (id !== undefined) {
    $svg.attr('id', id);
  }
  return $svg;
};

// append SVG
$.fn.appendSVG = function(type, cssClass, text, id) {
  return this.makeSVG(type, cssClass, text, id).appendTo(this);
};

$.fn.attrXLINK = function(attributeName, value) {
  if (this.length === 0) { // shortcut for empty collections
    return this;
  }
  if (this.length === 1) { // shortcut for single element collections
    this[0].setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:' + attributeName, value);
    return this;
  }
  return this.each(function() {
    this.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:' + attributeName, value);
  });
};

/**
 * This function adds a device specific CSS class to the current element.
 * The current implementation adds a class 'ios' if it is an ios device.
 */
$.fn.addDeviceClass = function() {
  let device = Device.get();
  if (device.isIos()) {
    this.addClass('ios');
  }
  return this;
};

// select one and deselect siblings
$.fn.selectOne = function() {
  this.siblings().removeClass('selected');
  this.addClass('selected');
  return this;
};

$.fn.select = function(selected) {
  return this.toggleClass('selected', !!selected);
};

$.fn.isSelected = function() {
  return this.hasClass('selected');
};

$.fn.setEnabled = function(enabled) {
  enabled = !!enabled;
  this.toggleClass('disabled', !enabled);
  // Toggle disabled attribute for elements that support it (see http://www.w3.org/TR/html5/disabled-elements.html)
  if (this.is('button, input, select, textarea, optgroup, option, fieldset')) {
    this.toggleAttr('disabled', !enabled);
  }
  this.trigger(enabled ? 'enable' : 'disable');
  return this;
};

$.fn.isEnabled = function() {
  return !this.hasClass('disabled');
};

$.fn.setVisible = function(visible) {
  let isVisible = !this.hasClass('hidden');
  if (isVisible === visible) {
    return this;
  }
  if (!visible) {
    this.addClass('hidden');
    this.trigger('hide');
  } else {
    this.removeClass('hidden');
    this.trigger('show');
  }
  return this;
};

$.fn.isDisplayNone = function() {
  return this.css('display') === 'none';
};

/**
 * @param {boolean} tabbable true, to make the component tabbable. False, to make it neither tabbable nor focusable.
 * @returns {$}
 */
$.fn.setTabbable = function(tabbable) {
  return this.attr('tabIndex', tabbable ? 0 : null);
};

/**
 * @param {boolean} tabbable true, to make the component tabbable. False, to make it not tabbable but focusable, so the user can focus it with the mouse but not with the keyboard.
 * @returns {$}
 */
$.fn.setTabbableOrFocusable = function(tabbable) {
  return this.attr('tabIndex', tabbable ? 0 : -1);
};

$.fn.isTabbable = function() {
  return this.attr('tabIndex') >= 0;
};

/**
 * @param {string} iconId
 * @param {function} [addToDomFunc] optional function which is used to add the new icon element to the DOM
 *     When not set, this.prepend($icon) is called.
 * @returns {$}
 * @see Icon as an alternative
 */
$.fn.icon = function(iconId, addToDomFunc) {
  let icon, $icon = this.data('$icon');
  if (iconId) {
    icon = icons.parseIconId(iconId);
    if (icon.isFontIcon()) {
      getOrCreateIconElement.call(this, $icon, '<span>', addToDomFunc)
        .addClass('icon')
        .addClass(icon.appendCssClass('font-icon'))
        .text(icon.iconCharacter);
    } else {
      getOrCreateIconElement.call(this, $icon, '<img>', addToDomFunc)
        .attr('src', icon.iconUrl)
        .addClass('icon image-icon');
    }
  } else {
    removeIconElement.call(this, $icon);
  }
  return this;

  // ----- Helper functions -----

  function getOrCreateIconElement($icon, newElement, addToDomFunc) {
    // If element type does not match existing element, remove the existing element (e.g. when changing from font-icon to picture icon)
    if ($icon && !$icon.is(newElement.replace(/[<>]/g, ''))) {
      removeIconElement.call(this, $icon);
      $icon = null;
    }
    // Create new element if necessary
    if (!$icon) {
      $icon = $(newElement);
      this.data('$icon', $icon);
      if (!addToDomFunc) {
        this.prepend($icon);
      } else {
        addToDomFunc.call(this, $icon);
      }
    }
    return $icon;
  }

  function removeIconElement($icon) {
    if ($icon) {
      $icon.remove();
    }
    this.removeData('$icon');
  }
};

$.fn.placeholder = function(placeholder) {
  return this.toggleAttr('placeholder', !!placeholder, placeholder);
};

$.fn.isVisible = function() {
  if (this.hasClass('hidden')) {
    return false;
  }
  return elemVisible(this[0]);
};

$.fn.isEveryParentVisible = function() {
  let everyParentVisible = true;
  this.parents().each(function() {
    if (!$(this).isVisible()) {
      everyParentVisible = false;
      return false;
    }
  });
  return everyParentVisible;
};

/**
 * @return {boolean} true if the element is attached (= is in the dom tree), false if not
 */
$.fn.isAttached = function() {
  return $.contains(this.document(true).documentElement, this[0]);
};

/**
 * @returns {$} the current element if it is scrollable, otherwise the first parent which is scrollable
 */
$.fn.scrollParent = function() {
  let $elem = this;
  while ($elem.length > 0) {
    if ($elem.data('scrollable')) {
      return $elem;
    }
    $elem = $elem.parent();
  }
  return $();
};

/**
 * Returns every parent which is scrollable
 */
$.fn.scrollParents = function() {
  let $scrollParents = $(),
    $elem = this;

  while ($elem.length > 0) {
    if ($elem.data('scrollable')) {
      $scrollParents.push($elem);
    }
    $elem = $elem.parent();
  }
  return $scrollParents;
};

/**
 * Similar to closest() but with a predicate function and the ability to stop at a given element.
 * @param {function} predicate the search predicate
 * @param {$} [$stop] If provided, the search is done until this element is reached.
 * @returns {$}
 */
$.fn.findUp = function(predicate, $stop) {
  let $elem = $(this);
  while ($elem.length > 0) {
    if (predicate($elem)) {
      return $elem;
    }
    if ($stop && $elem[0] === $stop[0]) {
      return $();
    }
    $elem = $elem.parent();
  }
  return $();
};

// most used animate
$.fn.animateAVCSD = function(attr, value, complete, step, duration) {
  let properties = {};
  let options = {};

  properties[attr] = value;
  if (complete) {
    options.complete = complete;
  }
  if (step) {
    options.step = step;
  }
  if (duration) {
    options.duration = duration;
  }
  options.queue = false;

  this.animate(properties, options);
  return this;
};

// SVG animate, array contains attr, endValue + startValue
$.fn.animateSVG = function(attr, endValue, duration, complete, withoutTabIndex) {
  return this.each(function() {
    let startValue = parseFloat($(this).attr(attr));
    if (withoutTabIndex) {
      let oldComplete = complete;
      complete = function() {
        if (oldComplete) {
          oldComplete.call(this);
        }
        $(this).removeAttr('tabindex');
      };
    }
    $(this).animate({
      tabIndex: 0
    }, {
      step: function(now, fx) {
        this.setAttribute(attr, startValue + (endValue - startValue) * fx.pos);
      },
      duration: duration,
      complete: complete,
      queue: false
    });
  });
};

$.fn.addClassForAnimation = function(className, options) {
  let defaultOptions = {
    classesToRemove: className
  };
  options = $.extend({}, defaultOptions, options);
  this.addClass(className);
  this.oneAnimationEnd(event => {
    // remove class, otherwise animation will be executed each time the element changes it's visibility (attach/rerender),
    // and even each time when the css classes change
    this.removeClass(options.classesToRemove);
  });
  return this;
};

/**
 * Adds a handler that is executed when a CSS animation ends on the current element. It will be executed
 * only once when the 'animationend' event is triggered on the current element. Bubbling events from child
 * elements are ignored.
 *
 * @param {function} handler - A function to execute when the 'animationend' event is triggered
 * @return {$}
 */
$.fn.oneAnimationEnd = function(handler) {
  if (!handler) {
    return this;
  }
  let oneHandler = event => {
    if (event.target !== this[0]) {
      // Ignore events that bubble up from child elements
      return;
    }
    // Unregister listener to implement "one" semantics
    this.off('animationend webkitAnimationEnd', oneHandler);
    // Notify actual event handler
    handler(event);
  };
  return this.on('animationend webkitAnimationEnd', oneHandler);
};

$.fn.hasAnimationClass = function() {
  // matches any CSS class that starts with 'animate-'
  return /(^|\s)animate-/.test(this.attr('class'));
};

/**
 * Animates from old to new width
 */
$.fn.cssWidthAnimated = function(oldWidth, newWidth, opts) {
  opts = opts || {};
  opts.duration = scout.nvl(opts.duration, 300);

  // Reset to old width first
  this.cssWidth(oldWidth);

  // Then animate to new width
  this.animate({
    width: newWidth
  }, opts);

  return this;
};

$.fn.cssHeightAnimated = function(oldHeight, newHeight, opts) {
  opts = opts || {};
  opts.duration = scout.nvl(opts.duration, 300);

  // Reset to old height first
  this.cssHeight(oldHeight);

  // Then animate to new height
  this.animate({
    height: newHeight
  }, opts);

  return this;
};

$.fn.cssLeftAnimated = function(from, to, opts) {
  opts = opts || {};
  opts.duration = scout.nvl(opts.duration, 300);

  // Reset to from first
  this.cssLeft(from);

  // Then animate to new width
  this.animate({
    left: to
  }, opts);

  return this;
};

$.fn.cssTopAnimated = function(from, to, opts) {
  opts = opts || {};
  opts.duration = scout.nvl(opts.duration, 300);

  // Reset to from first
  this.cssTop(from);

  // Then animate to new pos
  this.animate({
    top: to
  }, opts);

  return this;
};

$.fn.cssAnimated = function(fromVals, toVals, opts) {
  opts = opts || {};
  opts.duration = scout.nvl(opts.duration, 300);

  // Reset to from first
  this.css(fromVals);

  // Then animate to new pos
  this.animate(toVals, opts);
  return this;
};

// over engineered animate
$.fn.widthToContent = function(opts) {
  let oldW = this.outerWidth(),
    newW = this.css('width', 'auto').outerWidth();

  this.cssWidthAnimated(oldW, newW, opts);
  return this;
};

/**
 * Offset to a specific ancestor and not to the document as offset() would do.
 * Not the same as position() which returns the position relative to the offset parent.
 */
$.fn.offsetTo = function($to) {
  let toOffset = $to.offset(),
    offset = this.offset();

  return {
    top: offset.top - toOffset.top,
    left: offset.left - toOffset.left
  };
};

$.fn.cssPxValue = function(prop, value) {
  if (value === undefined) {
    return $.pxToNumber(this.css(prop));
  }
  if (value === null) {
    value = ''; // "null" should also remove the CSS property
  }
  if (typeof value === 'string') {
    return this.css(prop, value);
  }
  return this.css(prop, value + 'px');
};

$.fn.cssLeft = function(position) {
  return this.cssPxValue('left', position);
};

$.fn.cssTop = function(position) {
  return this.cssPxValue('top', position);
};

/**
 * Sets the CSS properties 'left' and 'top' based on the x and y properties of the given point instance.
 *
 * @param {Point} point
 */
$.fn.cssPosition = function(point) {
  return this.cssLeft(point.x).cssTop(point.y);
};

$.fn.cssBottom = function(position) {
  return this.cssPxValue('bottom', position);
};

$.fn.cssRight = function(position) {
  return this.cssPxValue('right', position);
};

$.fn.cssWidth = function(width) {
  return this.cssPxValue('width', width);
};

$.fn.cssMinWidth = function(minWidth) {
  if (minWidth === undefined) {
    let value = this.css('min-width');
    if (value === 'auto' || value.indexOf('%') !== -1) {
      return 0;
    }
    return $.pxToNumber(value);
  }
  return this.cssPxValue('min-width', minWidth);
};

/**
 * @returns {number} the max-width as number. If max-width is not set (resp. defaults to 'none') Number.MAX_VALUE is returned.
 */
$.fn.cssMaxWidth = function(maxWidth) {
  if (maxWidth === undefined) {
    let value = this.css('max-width');
    if (value === 'none' || value.indexOf('%') !== -1) {
      return Number.MAX_VALUE;
    }
    return $.pxToNumber(value);
  }
  return this.cssPxValue('max-width', maxWidth);
};

$.fn.cssHeight = function(height) {
  return this.cssPxValue('height', height);
};

$.fn.cssMinHeight = function(minHeight) {
  if (minHeight === undefined) {
    let value = this.css('min-height');
    if (value === 'auto' || value.indexOf('%') !== -1) {
      return 0;
    }
    return $.pxToNumber(value);
  }
  return this.cssPxValue('min-height', minHeight);
};

/**
 * @returns {number} the max-height as number. If max-height is not set (resp. defaults to 'none') Number.MAX_VALUE is returned.
 */
$.fn.cssMaxHeight = function(maxHeight) {
  if (maxHeight === undefined) {
    let value = this.css('max-height');
    if (value === 'none' || value.indexOf('%') !== -1) {
      return Number.MAX_VALUE;
    }
    return $.pxToNumber(value);
  }
  return this.cssPxValue('max-height', maxHeight);
};

$.fn.cssLineHeight = function(height) {
  return this.cssPxValue('line-height', height);
};

$.fn.cssMarginLeft = function(value) {
  return this.cssPxValue('margin-left', value);
};

$.fn.cssMarginBottom = function(value) {
  return this.cssPxValue('margin-bottom', value);
};

$.fn.cssMarginRight = function(value) {
  return this.cssPxValue('margin-right', value);
};

$.fn.cssMarginTop = function(value) {
  return this.cssPxValue('margin-top', value);
};

$.fn.cssMarginX = function(value) {
  if (value === undefined) {
    return this.cssMarginLeft() + this.cssMarginRight();
  }
  this.cssMarginLeft(value);
  this.cssMarginRight(value);
  return this;
};

$.fn.cssMarginY = function(value) {
  if (value === undefined) {
    return this.cssMarginTop() + this.cssMarginBottom();
  }
  this.cssMarginTop(value);
  this.cssMarginBottom(value);
  return this;
};

$.fn.cssPaddingTop = function(value) {
  return this.cssPxValue('padding-top', value);
};

$.fn.cssPaddingRight = function(value) {
  return this.cssPxValue('padding-right', value);
};

$.fn.cssPaddingBottom = function(value) {
  return this.cssPxValue('padding-bottom', value);
};

$.fn.cssPaddingLeft = function(value) {
  return this.cssPxValue('padding-left', value);
};

$.fn.cssPaddingX = function(value) {
  if (value === undefined) {
    return this.cssPaddingLeft() + this.cssPaddingRight();
  }
  this.cssPaddingLeft(value);
  this.cssPaddingRight(value);
  return this;
};

$.fn.cssPaddingY = function(value) {
  if (value === undefined) {
    return this.cssPaddingTop() + this.cssPaddingBottom();
  }
  this.cssPaddingTop(value);
  this.cssPaddingBottom(value);
  return this;
};

$.fn.cssBorderBottomWidth = function(value) {
  return this.cssPxValue('border-bottom-width', value);
};

$.fn.cssBorderLeftWidth = function(value) {
  return this.cssPxValue('border-left-width', value);
};

$.fn.cssBorderRightWidth = function(value) {
  return this.cssPxValue('border-right-width', value);
};

$.fn.cssBorderTopWidth = function(value) {
  return this.cssPxValue('border-top-width', value);
};

$.fn.cssBorderWidthY = function(value) {
  if (value === undefined) {
    return this.cssBorderTopWidth() + this.cssBorderBottomWidth();
  }
  this.cssBorderTopWidth(value);
  return this.cssBorderBottomWidth(value);
};

$.fn.cssBorderWidthX = function(value) {
  if (value === undefined) {
    return this.cssBorderLeftWidth() + this.cssBorderRightWidth();
  }
  this.cssBorderLeftWidth(value);
  return this.cssBorderRightWidth(value);
};

/**
 * Bottom of a html element without margin and border relative to offset parent. Expects border-box model.
 */
$.fn.innerBottom = function() {
  return this.position().top + this.outerHeight(true) - this.cssMarginBottom() - this.cssBorderBottomWidth();
};

/**
 * Right of a html element without margin and border relative to offset parent. Expects border-box model.
 */
$.fn.innerRight = function() {
  return this.position().left + this.outerWidth(true) - this.cssMarginRight() - this.cssBorderRightWidth();
};

$.fn.copyCss = function($origin, props) {
  let properties = props.split(' ');
  let $this = this;
  properties = explodeShorthandProperties(properties);
  properties.forEach(prop => {
    $this.css(prop, $origin.css(prop));
  });
  return $this;
};

$.fn.copyCssIfGreater = function($origin, props) {
  let properties = props.split(' ');
  let $this = this;
  properties = explodeShorthandProperties(properties);
  properties.forEach(prop => {
    let originValue = $.pxToNumber($origin.css(prop));
    let thisValue = $.pxToNumber($this.css(prop));
    if (originValue > thisValue) {
      $this.css(prop, originValue + 'px');
    }
  });
  return $this;
};

$.fn.copyCssClasses = function($other, classString) {
  let classes = classString.split(' ');
  let $this = this;
  classes.forEach(cssClass => {
    if ($other.hasClass(cssClass)) {
      $this.addClass(cssClass);
    }
  });
  return $this;
};

$.fn.disableSpellcheck = function() {
  return this.attr('spellcheck', false);
};

/**
 * Returns whether the current element is the given element or has a child which is the given element.
 */
$.fn.isOrHas = function(elem) {
  if (elem instanceof $) {
    elem = elem[0];
  }
  return this[0] === elem || this.has(elem).length > 0;
};

/**
 * Makes the current element resizable, which means DIVs for resize-handling are added to the DOM
 * in the E, SE and S of the element. This is primarily useful for (modal) dialogs.
 */
$.fn.resizable = function(model) {
  let $this = $(this);
  let resizable = $this.data('resizable');
  if (resizable) {
    // Already resizable
    return this;
  }
  resizable = scout.create('Resizable', $.extend(model, {$container: $this}));
  $this.data('resizable', resizable);
  return this;
};

/**
 * Removes the resize handles and event handlers in order to make the element un resizable again.
 */
$.fn.unresizable = function() {
  let $this = $(this);
  let resizable = $this.data('resizable');
  if (resizable) {
    resizable.destroy();
    $this.removeData('resizable');
  }
  return this;
};

/**
 * Makes any element movable with the mouse. If the argument '$handle' is missing, the entire
 * element can be used as a handle.
 *
 * A callback function can be passed as second argument (optional). The function is called for
 * every change of the draggable's position with an object as argument:
 * { top: (top pixels), left: (left pixels) }
 */
$.fn.draggable = function($handle, callback) {
  let $draggable = this;
  $handle = $handle || $draggable;
  return $handle.on('mousedown.draggable', event => {
    $('iframe').addClass('dragging-in-progress');
    let orig_offset = $draggable.offset();
    let orig_event = event;
    let handleWidth = $handle.width();
    let windowWidth = $handle.window().width();
    let windowHeight = $handle.window().height();
    $handle.parents()
      .on('mousemove.dragging', event => {
        let top = orig_offset.top + (event.pageY - orig_event.pageY);
        let left = orig_offset.left + (event.pageX - orig_event.pageX);
        // do not drop outside of viewport (and leave a margin of 100 pixels)
        left = Math.max(100 - handleWidth, left);
        left = Math.min(windowWidth - 100, left);
        top = Math.max(0, top); // must not be dragged outside of top, otherwise dragging back is impossible
        top = Math.min(windowHeight - 100, top);
        let newOffset = {
          top: top,
          left: left
        };
        $draggable.offset(newOffset);
        callback && callback(newOffset);
      })
      .on('mouseup.dragging', e => {
        $handle.parents().off('.dragging');
        $('iframe').removeClass('dragging-in-progress');
      });
    event.preventDefault();
  });
};

/**
 *
 * Removes the mouse down handler which was added by draggable() in order to make it un draggable again.
 */
$.fn.undraggable = function($handle) {
  let $draggable = this;
  $handle = $handle || $draggable;
  return $handle.off('mousedown.draggable');
};

/**
 * Calls jQuery.fadeOut() and then removes the element from the DOM.
 * Default fade-out duration is 150 ms.
 */
$.fn.fadeOutAndRemove = function(duration, callback) {
  if (callback === undefined && typeof duration === 'function') {
    callback = duration;
    duration = undefined;
  }
  duration = scout.nvl(duration, 150);
  return this.stop(true).fadeOut(duration, function() {
    $(this).remove();
    if (callback) {
      callback.call(this);
    }
  });
};

$.fn.removeAnimated = function(cssClass, callback) {
  if (callback === undefined && typeof cssClass === 'function') {
    callback = cssClass;
    cssClass = undefined;
  }
  if (this.isDisplayNone()) {
    // Remove without animation
    this.remove();
    callback && callback.call(this);
  } else if (!Device.get().supportsCssAnimation()) {
    // Cannot remove animated, remove with jQuery.fadeOut()
    this.fadeOutAndRemove(callback);
  } else {
    // Add CSS class and wait for 'animationend' event
    this.addClass(cssClass || 'animate-remove');
    this.oneAnimationEnd(function() {
      $(this).remove();
      callback && callback.call(this);
    });
  }
};

const __origHide = $.fn.hide;
$.fn.hide = function(...args) {
  this.trigger('hide');
  return __origHide.apply(this, args);
};

const __origWidth = $.fn.width;
// noinspection JSValidateTypes
$.fn.width = function(...args) {
  return _ceilNumber(__origWidth.apply(this, args));
};

const __origOuterWidth = $.fn.outerWidth;
// noinspection JSValidateTypes
$.fn.outerWidth = function(...args) {
  return _ceilNumber(__origOuterWidth.apply(this, args));
};

const __origHeight = $.fn.height;
// noinspection JSValidateTypes
$.fn.height = function(...args) {
  return _ceilNumber(__origHeight.apply(this, args));
};

let __origOuterHeight = $.fn.outerHeight;
// noinspection JSValidateTypes
$.fn.outerHeight = function(...args) {
  return _ceilNumber(__origOuterHeight.apply(this, args));
};

/**
 * Sets the given 'text' as text to the jQuery element, using the text() function (i.e. HTML is encoded automatically).
 * If the text does not contain any non-space characters, the text '&nbsp;' is set instead (using the html() function).
 * If an 'emptyCssClass' is provided, this CSS class is removed in the former and added in the later case.
 */
$.fn.textOrNbsp = function(text, emptyCssClass) {
  return this.contentOrNbsp(false, text, emptyCssClass);
};

/**
 * Same as "textOrNbsp", but with html (caller is responsible for encoding).
 */
$.fn.htmlOrNbsp = function(html, emptyCssClass) {
  return this.contentOrNbsp(true, html, emptyCssClass);
};

/**
 * Renders the given content as plain-text or HTML depending on the given htmlEnabled flag.
 *
 * @param {boolean} htmlEnabled
 * @param {string} content
 * @param {string} emptyCssClass
 * @returns {$}
 */
$.fn.contentOrNbsp = function(htmlEnabled, content, emptyCssClass) {
  let func = htmlEnabled ? this.html : this.text;
  if (strings.hasText(content)) {
    func.call(this, content);
    if (emptyCssClass) {
      this.removeClass(emptyCssClass);
    }
  } else {
    this.html('&nbsp;');
    if (emptyCssClass) {
      this.addClass(emptyCssClass);
    }
  }
  return this;
};

/**
 * Like toggleClass(), this toggles a HTML attribute on a set of jquery elements.
 *
 * @param attr
 *          Name of the attribute to toggle.
 * @param state
 *          Specifies if the attribute should be added or removed (based on whether the argument is truthy or falsy).
 *          If this argument is not defined, the attribute is added when it exists, and vice-versa. If this behavior
 *          is not desired, explicitly cast the argument to a boolean using "!!".
 * @param value
 *          Value to use when adding the attribute.
 *          If this argument is not specified, 'attr' is used as value.
 */
$.fn.toggleAttr = function(attr, state, value) {
  if (!attr) {
    return this;
  }
  if (value === undefined) {
    value = attr;
  }
  return this.each(function() {
    let $element = $(this);
    if (state === undefined) {
      // set state according to the current value
      state = ($element.attr(attr) === undefined);
    }
    if (state) {
      // set attr
      $element.attr(attr, value);
    } else {
      // remove attr
      $element.removeAttr(attr);
    }
  });
};

$.fn.backupSelection = function() {
  let field = this[0];
  if (field && field === this.activeElement(true)) {
    return {
      selectionStart: field.selectionStart,
      selectionEnd: field.selectionEnd,
      selectionDirection: field.selectionDirection
    };
  }
  return null;
};

$.fn.restoreSelection = function(selection) {
  let field = this[0];
  if (field && field === this.activeElement(true) && selection) {
    field.setSelectionRange(selection.selectionStart, selection.selectionEnd, selection.selectionDirection);
  }
  return this;
};

/**
 * If the given value is "truthy", it is set as attribute on the target. Otherwise, the attribute is removed.
 */
$.fn.attrOrRemove = function(attributeName, value) {
  if (value) {
    this.attr(attributeName, value);
  } else {
    this.removeAttr(attributeName);
  }
  return this;
};

$.fn.appendAppLink = function(appLinkBean, func) {
  return this.appendSpan().appLink(appLinkBean, func);
};

/**
 * @param appLinkBean
 *          Either
 *           - an AppLinkBean with both (1) a ref attribute which will be mapped to the
 *             data-ref attribute of the element and (2) a text attribute which will be
 *             set as the text of the element.
 *           - or just a ref, which will be mapped to the data-ref attribute of the
 *             element.
 * @param func
 *          Optional. Either
 *           - a function to be called when the app link has been clicked
 *           - or an object with a method named _onAppLinkAction (e.g. an instance of
 *             BeanField)
 *          If func is not set, the _onAppLinkAction of the inner most widget relative to
 *          this element (if any) will be called when the app link has been clicked.
 */
$.fn.appLink = function(appLinkBean, func) {
  if (!func) {
    func = function(event) {
      let widget = scout.widget(this);
      if (widget && widget._onAppLinkAction) {
        widget._onAppLinkAction(event);
      }
    }.bind(this);
  } else if (typeof func === 'object' && func._onAppLinkAction) {
    func = func._onAppLinkAction.bind(func);
  }

  this.addClass('app-link')
    .attr('tabindex', '0')
    .unfocusable()
    .on('click', func);

  if (typeof appLinkBean === 'string') {
    this.attr('data-ref', appLinkBean);
  } else {
    this
      .text(appLinkBean.name)
      .attr('data-ref', appLinkBean.ref);
  }
  return this;
};

/**
 * Adds the class 'unfocusable' to current result set. The class is not used for styling purposes
 * but has a meaning to the FocusManager.
 */
$.fn.unfocusable = function() {
  return this.addClass('unfocusable');
};

/**
 * Select all text within an element, e.g. within a content editable div element.
 */
$.fn.selectAllText = function() {
  let range,
    myDocument = this.document(true),
    myWindow = this.window(true),
    element = this[0];

  if (!myDocument || !myDocument.body || !myWindow || !element) {
    return this;
  }

  if (myWindow.getSelection) {
    range = myDocument.createRange();
    range.selectNodeContents(element);
    myWindow.getSelection().removeAllRanges();
    myWindow.getSelection().addRange(range);
  }

  return this;
};

$.fn._getClientAndScrollWidthRounded = function() {
  let element = this[0];
  if (Device.get().isEdge()) {
    // Edge seem to round up the scrollWidth. Therefore the clientWidth must be rounded up as well to have a valid comparison.
    return {
      clientWidth: Math.ceil(element.getBoundingClientRect().width) - this.cssBorderWidthX(), // getBoundingClientRect includes the border -> remove it again to have the clientWidth
      scrollWidth: element.scrollWidth
    };
  }

  return {
    clientWidth: element.clientWidth,
    scrollWidth: element.scrollWidth
  };
};

$.fn._getClientAndScrollWidthReliable = function() {
  let widths = this._getClientAndScrollWidthRounded();
  if (!Device.get().isScrollWidthIncludingPadding()) {
    // browser supports accurate client- and scroll widths.
    return widths;
  }
  if (widths.scrollWidth > widths.clientWidth) {
    // content is large enough so that the scroll-width is already larger than the client-width. Values are correct.
    return widths;
  }

  let paddingRight = this.cssPaddingRight(),
    oldStyle = this.attr('style');
  if (paddingRight > 0) {
    // Some browsers render text within the right-padding (even with overflow=hidden). This has an effect on the value of scrollWidth which may be wrong in these cases (scrollWidth == clientWidth but ellipsis is shown).
    // Solution: temporary remove the padding and reduce the width by the padding-size to have the same space for the text but without padding.
    this.css({
      width: widths.clientWidth - paddingRight,
      paddingRight: '0px'
    });
    widths = this._getClientAndScrollWidthRounded(); // read value again.
    this.attrOrRemove('style', oldStyle);
    if (widths.scrollWidth > widths.clientWidth) {
      return widths;
    }
  }

  // In some cases the browser returns the same values for clientWidth and scrollWidth,
  // but will cut off the text nevertheless. At least in FF this seems to be a bug related
  // to sub-pixel rendering. The text is "slightly" (0.2 pixels) larger than the clientWidth,
  // but scrollWidth returns the same value.
  // As a workaround, we do a second measurement of the uncut width before returning false.
  let clientWidth = this[0].getBoundingClientRect().width;
  this.css('width', 'auto');
  this.css('max-width', 'none');
  let scrollWidth = this[0].getBoundingClientRect().width;
  this.attrOrRemove('style', oldStyle);
  return {
    clientWidth: clientWidth,
    scrollWidth: scrollWidth
  };
};

/**
 * Checks if content is truncated.
 *
 * @return {boolean}
 */
$.fn.isContentTruncated = function() {
  let widths = this._getClientAndScrollWidthReliable();
  if (widths.scrollWidth > widths.clientWidth) {
    return true;
  }
  return false;
};

/**
 * This function is used to distinct between single and double clicks.
 * Instead of executing a handler immediately when the first click occurs,
 * we wait for a given timeout (or by default 300 ms) to check if it is followed by a second click.
 * This will delay the execution of a single click a bit, so you should use this function wisely.
 */
$.fn.onSingleOrDoubleClick = function(singleClickFunc, doubleClickFunc, timeout) {
  return this.each(function() {
    let that = this;
    let numClicks = 0;
    let timeout = scout.nvl(timeout, 300);
    $(this).on('click', event => {
      numClicks++;
      if (numClicks === 1) {
        setTimeout(() => {
          if (numClicks === 1) {
            singleClickFunc.call(that, event);
          } else {
            doubleClickFunc.call(that, event);
          }
          numClicks = 0;
        }, timeout);
      }
    });
  });
};

$.fn.onPassive = function(eventType, handler) {
  let options = events.passiveOptions();
  this[0].addEventListener(eventType, handler, options);
  return this;
};

$.fn.offPassive = function(eventType, handler) {
  let options = events.passiveOptions();
  this[0].removeEventListener(eventType, handler, options);
  return this;
};

// ------------------------------------------------------------------

$.fn.appendTable = function(cssClass) {
  return this.appendElement('<table>', cssClass);
};

$.fn.appendColgroup = function(cssClass) {
  return this.appendElement('<colgroup>', cssClass);
};

$.fn.appendCol = function(cssClass) {
  return this.appendElement('<col>', cssClass);
};

$.fn.appendTr = function(cssClass) {
  return this.appendElement('<tr>', cssClass);
};

$.fn.appendTd = function(cssClass, text) {
  return this.appendElement('<td>', cssClass, text);
};

$.fn.appendTh = function(cssClass, text) {
  return this.appendElement('<th>', cssClass, text);
};

$.fn.appendUl = function(cssClass) {
  return this.appendElement('<ul>', cssClass);
};

$.fn.appendLi = function(cssClass, text) {
  return this.appendElement('<li>', cssClass, text);
};

// === $.easing extensions ===

$.extend($.easing, {
  easeOutQuart: x => 1 - Math.pow(1 - x, 4)
});

/**
 * jquery.binarytransport.js
 *
 * @description. jQuery ajax transport for making binary data type requests.
 * @version 1.0
 * @author Henry Algus <henryalgus@gmail.com>
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Henry Algus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

// use this transport for "binary" data type
$.ajaxTransport('+binary', (options, originalOptions, jqXHR) => {
  // check for conditions and support for blob / arraybuffer response type
  if (window.FormData && ((options.dataType && (options.dataType === 'binary')) ||
    (options.data && ((window.ArrayBuffer && options.data instanceof ArrayBuffer) ||
      (window.Blob && options.data instanceof Blob))))) {
    return {
      // create new XMLHttpRequest
      send: (headers, callback) => {
        // setup all variables
        let xhr = new XMLHttpRequest(),
          url = options.url,
          type = options.type,
          async = options.async || true,
          // blob or arraybuffer. Default is blob
          dataType = options.responseType || 'blob',
          data = options.data || null,
          username = options.username || null,
          password = options.password || null;

        xhr.addEventListener('load', () => {
          let data = {};
          data[options.dataType] = xhr.response;
          // make callback and send data
          callback(xhr.status, xhr.statusText, data, xhr.getAllResponseHeaders());
        });

        xhr.open(type, url, async, username, password);

        // setup custom headers
        for (let i in headers) { // NOSONAR
          xhr.setRequestHeader(i, headers[i]);
        }

        // apply custom fields (if provided)
        // noinspection JSUnresolvedVariable
        if (options.xhrFields) {
          for (let j in options.xhrFields) {
            xhr[j] = options.xhrFields[j];
          }
        }

        xhr.responseType = dataType;
        xhr.send(data);
      },
      abort: () => {
      }
    };
  }
});

// jquery's noGlobal is true with webpack so jquery does not expose $ by itself
if (window.$ !== $) {
  window.$ = $;
}
