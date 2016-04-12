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
/**
 * JQuery plugin with scout extensions
 */

// === internal methods ===

/**
 * Returns false when the component display is 'none' or visibility is 'hidden', otherwise true.
 * Note: this gives other results than $.is(':visible'), since that method will also return false
 * when a component has absolute positioning and no width and height is defined (well, you cannot
 * see a component with a style like this, but technically it is not set to 'not visible').
 */
function elemVisible(elem) {
  // Check if element itself is hidden by its own style attribute
  if (isHidden(elem.style)) {
    return false;
  }
  // Must use correct window for element / computedStyle
  var myWindow = elem.ownerDocument.defaultView;
  // Check if element itself is hidden by external style-sheet
  if (isHidden(myWindow.getComputedStyle(elem))) {
    return false;
  }
  // Else visible
  return true;

  // ----- Helper functions -----

  function isHidden(style) {
    return style.display === 'none' || style.visibility === 'hidden';
  }
}

function explodeShorthandProperties(properties) {
  var newProperties = [];
  properties.forEach(function(prop) {
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

// === $ extensions ===

// used by some animate functions
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
$.suppressEvent = function(event) {
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
$.suppressEventIfDisabled = function(event, $target) {
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
 * the timer is reset. The default value for 'delay' is 250 ms.
 */
$.debounce = function(fx, delay) {
  delay = (delay !== undefined) ? delay : 250;
  var timeoutId = null;
  return function() {
    var that = this,
      args = arguments;
    clearTimeout(timeoutId);
    timeoutId = setTimeout(function() {
      fx.apply(that, args);
    }, delay);
  };
};

/**
 * Executes the given function. Further calls to the same function are delayed by the given delay
 * (default 250ms). This is similar to $.debounce() but ensures that function is called at least
 * every 'delay' milliseconds. Can be useful to prevent too many function calls, e.g. from UI events.
 */
$.throttle = function(fx, delay) {
  delay = (delay !== undefined) ? delay : 250;
  var timeoutId = null;
  var lastExecution;
  return function() {
    var that = this,
      args = arguments,
      now = new Date().getTime(),
      callFx = function() {
        lastExecution = now;
        fx.apply(that, args);
      };
    if (lastExecution && lastExecution + delay > now) {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(function() {
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
$.negate = function(fx) {
  return function() {
    return !fx.apply(this, arguments);
  };
};

/**
 * color calculation
 */
$.colorOpacity = function(hex, opacity) {
  // validate hex string
  hex = String(hex).replace(/[^0-9a-f]/gi, '');
  if (hex.length < 6) {
    hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2];
  }
  opacity = opacity || 0;

  // convert to decimal and change luminosity
  var rgb = '#';
  for (var i = 0; i < 3; i++) {
    var c = parseInt(hex.substr(i * 2, 2), 16);
    c = Math.round(Math.min(Math.max(0, 255 - (255 - c) * opacity), 255)).toString(16);
    rgb += ('00' + c).substr(c.length);
  }

  return rgb;
};

/**
 * from http://api.jquery.com/jquery.getscript/
 */
$.getCachedScript = function(url, options) {
  options = $.extend(options || {}, {
    dataType: 'script',
    cache: true,
    url: url
  });
  return $.ajax(options);
};

$.pxToNumber = function(pixel) {
  if (!pixel) {
    // parseFloat would return NaN if pixel is '' or undefined
    return 0;
  }
  // parseFloat ignores 'px' and just extracts the number
  return parseFloat(pixel, 10);
};

// === $.prototype extensions ===

/**
 * @param element string. Example = &lt;input&gt;
 * @param cssClass (optional) class attribute
 * @param text (optional) adds a child text-node with given text (no HTML content)
 */
$.fn.makeElement = function(element, cssClass, text) {
  var myDocument = this.document(true);
  if (myDocument === undefined || element === undefined) {
    return new Error('missing arguments: document, element');
  }
  var $element = $(element, myDocument);
  if (cssClass) {
    $element.addClass(cssClass);
  }
  if (text) {
    $element.text(text);
  }
  return $element;
};

/**
 * Creates a DIV element in the current document. The function adds an unselectable attribute,
 * if this is required by the current device (@see Device.js). When you don't want the (un-)
 * selectable behavior use <code>makeElement('&lt;div&gt;')</code>.
 *
 * @param cssClass (optional) string added to the 'class' attribute
 * @param text (optional) string used as inner text
 */
$.fn.makeDiv = function(cssClass, text) {
  var unselectable = scout.device.unselectableAttribute,
    $div = this.makeElement('<div>', cssClass, text);

  if (unselectable.key) {
    $div.attr(unselectable.key, unselectable.value);
  }
  return $div;
};

$.fn.makeSpan = function(cssClass, text) {
  return this.makeElement('<span>', cssClass, text);
};

/**
 * @return HTML document reference (ownerDocument) of the HTML element.
 * @param domElement (optional) if true this function returns a JQuery object, otherwise only the DOM element is returned
 */
$.fn.document = function(domElement) {
  var myDocument = this.length ? this[0].ownerDocument : null;
  return domElement ? myDocument : $(myDocument);
};

/**
 * @return HTML window reference (defaultView) of the HTML element
 * @param domElement (optional) if true this function returns a JQuery object, otherwise only the DOM element is returned
 */
$.fn.window = function(domElement) {
  var myDocument = this.document(true),
    myWindow = myDocument ? myDocument.defaultView : null;
  return domElement ? myWindow : $(myWindow);
};

/**
 * @return HTML document reference (ownerDocument) of the HTML element.
 * @param domElement (optional) if true this function returns a JQuery object, otherwise only the DOM element is returned
 */
$.fn.activeElement = function(domElement) {
  var myDocument = this.document(true),
    activeElement = myDocument ? myDocument.activeElement : null;
  return domElement ? activeElement : $(activeElement);
};

/**
 * @return the BODY element of the HTML document in which the current HTML element is placed.
 */
$.fn.body = function() {
  return $('body', this.document(true));
};

/**
 * @return the closest DOM element that has the 'scout' class.
 * @param domElement (optional) if true this function returns a JQuery object, otherwise only the DOM element is returned
 */
$.fn.entryPoint = function(domElement) {
  var $element = this.closest('.scout');
  return domElement ? $element[0] : $element;
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

$.fn.makeSVG = function(type, cssClass, text, id) {
  var myDocument = this.document(true);
  if (myDocument === undefined || type === undefined) {
    return new Error('missing arguments: document, type');
  }
  var $svg = $(myDocument.createElementNS('http://www.w3.org/2000/svg', type));
  if (cssClass) {
    $svg.attrSVG('class', cssClass);
  }
    if (text) {
      $svg.text(text);
  }
  if (id !== undefined) {
    $svg.attrSVG('id', id);
  }
  return $svg;
};

// append SVG
$.fn.appendSVG = function(type, cssClass, text, id) {
  return this.makeSVG(type, cssClass, text, id).appendTo(this);
};

// attr and class handling for svg
$.fn.attrSVG = function(attributeName, value) {
  if (this.length === 0) { // shortcut for empty collections
    return this;
  }
  if (this.length === 1) { // shortcut for single element collections
    this[0].setAttribute(attributeName, value);
    return this;
  }
  return this.each(function() {
    this.setAttribute(attributeName, value);
  });
};

$.fn.removeAttrSVG = function(attributeName) {
  if (this.length === 0) { // shortcut for empty collections
    return this;
  }
  if (this.length === 1) { // shortcut for single element collections
    this[0].removeAttribute(attributeName);
    return this;
  }
  return this.each(function() {
    this.removeAttribute(attributeName);
  });
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

$.fn.addClassSVG = function(cssClass) {
  var old;
  if (this.length === 0) { // shortcut for empty collections
    return this;
  }
  if (this.length === 1) { // shortcut for single element collections
    if (!this.hasClassSVG(cssClass)) {
      old = this[0].getAttribute('class');
      this[0].setAttribute('class', scout.strings.join(' ', old, cssClass).trim());
    }
    return this;
  }
  return this.each(function() {
    if (!$(this).hasClassSVG(cssClass)) {
      old = this.getAttribute('class');
      this.setAttribute('class', scout.strings.join(' ', old, cssClass).trim());
    }
  });
};

$.fn.removeClassSVG = function(cssClass) {
  var old;
  if (this.length === 0) { // shortcut for empty collections
    return this;
  }
  if (this.length === 1) { // shortcut for single element collections
    old = ' ' + this[0].getAttribute('class') + ' ';
    this[0].setAttribute('class', old.replace(' ' + cssClass + ' ', ' ').trim());
    return this;
  }
  return this.each(function() {
    var old = ' ' + this.getAttribute('class') + ' ';
    this.setAttribute('class', old.replace(' ' + cssClass + ' ', ' ').trim());
  });
};

$.fn.hasClassSVG = function(cssClass) {
  var old = ' ' + this[0].getAttribute('class') + ' ';
  return old.indexOf(' ' + cssClass + ' ') !== -1;
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
  return this;
};

$.fn.isEnabled = function() {
  return !this.hasClass('disabled');
};

$.fn.setVisible = function(visible) {
  var i, elem;
  for (i = 0; i < this.length; i++) {
    elem = this[i];
    if (elemVisible(elem) != visible) {
      if (visible) {
        this.show();
      } else {
        this.hide();
      }
    }
  }
  return this;
};

$.fn.isDisplayNone = function() {
  return this.css('display') === 'none';
};

$.fn.setTabbable = function(tabbable) {
  return this.attr('tabIndex', tabbable ? 0 : null);
};

$.fn.icon = function(iconId) {
  var icon, $icon = this.data('$icon');
  if (iconId) {
    icon = scout.icons.parseIconId(iconId);
    if (icon.isFontIcon()) {
      getOrCreateIconElement.call(this, $icon, '<span>')
        .addClass(icon.appendCssClass('font-icon'))
        .addClass('icon')
        .text(icon.iconCharacter);
    } else {
      getOrCreateIconElement.call(this, $icon, '<img>')
        .attr('src', icon.iconUrl)
        .addClass('icon');
    }
  } else {
    removeIconElement.call(this, $icon);
  }
  return this;

  // ----- Helper functions -----

  function getOrCreateIconElement($icon, newElement) {
    // If element type does not match existing element, remove the existing element (e.g. when changing from font-icon to picture icon)
    if ($icon && !$icon.is(newElement.replace(/[<>]/g, ''))) {
      removeIconElement.call(this, $icon);
      $icon = null;
    }
    // Create new element if necessary
    if (!$icon) {
      $icon = $(newElement);
      this.data('$icon', $icon);
      this.prepend($icon);
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
  return elemVisible(this[0]);
};

$.fn.isEveryParentVisible = function() {
  var everyParentVisible = true;
  this.parents().each(function() {
    if (!$(this).isVisible()) {
      everyParentVisible = false;
      return false;
    }
  });
  return everyParentVisible;
};

/**
 * @return true if the element is attached (= is in the dom tree), false if not
 */
$.fn.isAttached = function() {
  return $.contains(this.document(true).documentElement, this[0]);
};

/**
 * Returns the first parent which is scrollable
 */
$.fn.scrollParent = function() {
  var $elem = this;
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
  var $scrollParents = $(),
    $elem = this;

  while ($elem.length > 0) {
    if ($elem.data('scrollable')) {
      $scrollParents.push($elem);
    }
    $elem = $elem.parent();
  }
  return $scrollParents;
};

// most used animate
$.fn.animateAVCSD = function(attr, value, complete, step, duration) {
  return this.animateAVSCSD(attr, value, null, complete, step, duration);
};

$.fn.animateAVSCSD = function(attr, value, start,complete, step, duration) {
  var properties = {};
  var options = {};

  properties[attr] = value;
  if (complete) {
    options.complete = complete;
  }
  if (start) {
    options.start = start;
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
    var startValue = parseFloat($(this).attr(attr));
    if (withoutTabIndex) {
      var oldComplete = complete;
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
  var defaultOptions = {
    classesToRemove: className
  };
  options = $.extend({}, defaultOptions, options);
  this.addClass(className);
  this.oneAnimationEnd(function() {
    // remove class, otherwise animation will be executed each time the element changes it's visibility (attach/rerender),
    // and even each time when the css classes change
    this.removeClass(options.classesToRemove);
    // delay must be greater than css animation duration
  }.bind(this));
};

$.fn.oneAnimationEnd = function(selector, data, handler) {
  this.one('animationend webkitAnimationEnd', selector, data, handler);
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
  var oldW = this.outerWidth(),
    newW = this.css('width', 'auto').outerWidth();

  this.cssWidthAnimated(oldW, newW, opts);
  return this;
};

$.fn.cssLeft = function(position) {
  return this.cssPxValue('left', position);
};

$.fn.cssTop = function(position) {
  return this.cssPxValue('top', position);
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

$.fn.cssHeight = function(height) {
  return this.cssPxValue('height', height);
};

$.fn.cssLineHeight = function(height) {
  return this.cssPxValue('line-height', height);
};

$.fn.cssPxValue = function(prop, value) {
  if (value === undefined) {
    return $.pxToNumber(this.css(prop));
  }
  return this.css(prop, value + 'px');
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
};

$.fn.cssMarginY = function(value) {
  if (value === undefined) {
    return this.cssMarginTop() + this.cssMarginBottom();
  }
  this.cssMarginTop(value);
  this.cssMarginBottom(value);
};

$.fn.cssPaddingTop = function(value) {
  return this.cssPxValue('padding-top', value);
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
  this.cssBorderBottomWidth(value);
};

$.fn.cssBorderWidthX = function(value) {
  if (value === undefined) {
    return this.cssBorderLeftWidth() + this.cssBorderRightWidth();
  }
  this.cssBorderLeftWidth(value);
  this.cssBorderRightWidth(value);
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
  var properties = props.split(' ');
  var $this = this;
  properties = explodeShorthandProperties(properties);
  properties.forEach(function(prop) {
    $this.css(prop, $origin.css(prop));
  });
  return $this;
};

$.fn.copyCssIfGreater = function($origin, props) {
  var properties = props.split(' ');
  var $this = this;
  properties = explodeShorthandProperties(properties);
  properties.forEach(function(prop) {
    var originValue = $.pxToNumber($origin.css(prop));
    var thisValue = $.pxToNumber($this.css(prop));
    if (originValue > thisValue) {
      $this.css(prop, originValue + 'px');
    }
  });
  return $this;
};

$.fn.copyCssClasses = function($other, classString) {
  var classes = classString.split(' ');
  var $this = this;
  classes.forEach(function(cssClass) {
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
  return this[0] === elem || this.has(elem).length > 0;
};

/**
 * Makes any element movable with the mouse. If the argument '$handle' is missing, the entire
 * element can be used as a handle.
 *
 * A callback function can be passed as second argument (optional). The function is called for
 * every change of the draggable's position with an object as argument:
 * { top: (top pixels), left: (left pixels) }
 */
$.fn.makeDraggable = function($handle, callback) {
  var $draggable = this;
  $handle = $handle || $draggable;
  return $handle.on('mousedown.draggable', function(event) {
    var orig_offset = $draggable.offset();
    var orig_event = event;
    var handleWidth = $handle.width();
    var windowWidth = $handle.window().width();
    var windowHeight = $handle.window().height();
    $handle.parents()
      .on('mousemove.dragging', function(event) {
        var top = orig_offset.top + (event.pageY - orig_event.pageY);
        var left = orig_offset.left + (event.pageX - orig_event.pageX);
        // do not drop outside of viewport (and leave a margin of 100 pixels)
        left = Math.max(100 - handleWidth, left);
        left = Math.min(windowWidth - 100, left);
        top = Math.max(0, top); // must not be dragged outside of top, otherwise dragging back is impossible
        top = Math.min(windowHeight - 100, top);
        var newOffset = {
          top: top,
          left: left
        };
        $draggable.offset(newOffset);
        callback && callback(newOffset);
      })
      .on('mouseup.dragging', function(e) {
        $handle.parents().off('.dragging');
      });
    event.preventDefault();
  });
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
  this.stop(true).fadeOut(duration, function() {
    $(this).remove();
    if (callback) {
      callback.call(this);
    }
  });
};

var _oldhide = $.fn.hide;
$.fn.hide = function() {
  this.trigger('hide');
  var returnValue = _oldhide.apply(this, arguments);
  return returnValue;
};

/**
 * Compared to the regular hide it does not remember the current display state, it just sets display to none.
 * This is a lot faster (1.8sec faster when applying hide to 500 nodes).
 * It has the drawback that show cannot revert the display style, so you have to know the style.<p>
 * See also showFast.
 */
$.fn.hideFast = function() {
  //TODO [5.2] cgu: change to use css class (this.addClass('hidden');). Replace jquery hide and show -> should be a lot faster because it is not necessary to remember any state.
  this[0].style.display = 'none';
  return this;
};

/**
 * Just sets display to block. See hideFast for details.
 */
$.fn.showFast = function() {
  this[0].style.display = 'block';
  return this;
};

/**
 * Sets the given 'text' as text to the jQuery element, using the text() function (i.e. HTML is encoded automatically).
 * If the text does not contain any non-space characters, the text '&nbsp;' is set instead (using the html() function).
 * If an 'emptyCssClass' is provided, this CSS class is removed in the former and added in the later case.
 */
$.fn.textOrNbsp = function(text, emptyCssClass) {
  if (scout.strings.hasText(text)) {
    this.text(text);
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
 * Same as "textOrNbsp", but with html (caller is responsible for encoding).
 */
$.fn.htmlOrNbsp = function(html, emptyCssClass) {
  if (scout.strings.hasText(html)) {
    this.html(html);
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
    var $element = $(this);
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
  var field = this[0];
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
  var field = this[0];
  if (field && field === this.activeElement(true) && selection) {
    field.setSelectionRange(selection.selectionStart, selection.selectionEnd, selection.selectionDirection);
  }
};

/**
 * If the given value is "truthy", it is set as attribute on the target. Otherwise, the attribute is removed.
 */
$.fn.attrOrRemove = function(attributeName, value) {
  if (value) {
    $(this).attr(attributeName, value);
  } else {
    $(this).removeAttr(attributeName);
  }
};

$.fn.appendAppLink = function(appLinkBean, func) {
  return this.appendSpan().appLink(appLinkBean, func);
};

/**
 * @param func Either a function to be called when the app link has been clicked.
 * Or an object with a method named _onAppLinkAction (e.g. an instance of BeanField).
 */
$.fn.appLink = function(appLinkBean, func) {
  if (typeof func === 'object' && func._onAppLinkAction) {
    func = func._onAppLinkAction.bind(func);
  }
  return this.addClass('app-link')
    .text(appLinkBean.name)
    .attr('tabindex', '0')
    .attr('data-ref', appLinkBean.ref)
    .unfocusable()
    .on('click', func);
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
  var range,
    myDocument = this.document(true),
    myWindow = this.window(true),
    element = this[0];

  if (myDocument.body.createTextRange) {
    range = myDocument.body.createTextRange();
    range.moveToElementText(element);
    range.select();
    return;
  }

  if (myWindow.getSelection) {
    range = myDocument.createRange();
    range.selectNodeContents(element);
    myWindow.getSelection().removeAllRanges();
    myWindow.getSelection().addRange(range);
  }
};

/**
 * Checks if content is truncated.
 */
$.fn.isContentTruncated = function() {
  return this[0].scrollWidth > this[0].clientWidth;
};

// FIXME awe: (graph) consider moving this function to DoubleClickHandler.js
/**
 * This function is used to distinct between single and double clicks.
 * Instead of executing a handler immediately when the first click occurs,
 * we wait for a given timeout (or by default 300 ms) to check if it is followed by a second click.
 * This will delay the execution of a single click a bit, so you should use this function wisely.
 */
$.fn.onSingleOrDoubleClick = function(singleClickFunc, doubleClickFunc, timeout) {
  return this.each(function() {
    var that = this,
      numClicks = 0,
      timeout = timeout || 300;
    $(this).on('click', function(event) {
      numClicks++;
      if (numClicks == 1) {
        setTimeout(function() {
          if (numClicks == 1) {
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
