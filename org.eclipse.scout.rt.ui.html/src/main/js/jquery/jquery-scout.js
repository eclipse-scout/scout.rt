/**
 * JQuery plugin with scout extensions
 */

/*global console: false */
(function($) {

  // === $ extensions ===

  // chris' shortcut
  if (typeof console !== 'undefined' && console.log.bind) {
    $.l = console.log.bind(console);
  } else {
    $.l = function() {};
  }

  $.makeDiv = function(cssClass, htmlContent, id) {
    if (id === 0) {
      //Allow 0 as id (!id would result in false)
      id = '0';
    }
    return $('<div' +
      (id ? ' id="' + id + '"' : '') +
      (cssClass ? ' class="' + cssClass + '"' : '') +
      scout.device.unselectableAttribute +
      '>' +
      (htmlContent || '') +
      '</div>'
    );
  };

  $.makeSpan = function(cssClass, text) {
    return $.make('<span>', cssClass, text);
  };

  $.make = function(element, cssClass, text) {
    var $elem = $(element);
    if (cssClass) {
      $elem.addClass(cssClass);
    }
    if (text) {
      $elem.text(text);
    }
    return $elem;
  };

  $.makeSVG = function(type, id, cssClass, htmlContent) {
    var $svgElement = $(document.createElementNS('http://www.w3.org/2000/svg', type));
    if (id) {
      $svgElement.attr('id', id);
    }
    if (cssClass) {
      $svgElement.attr('class', cssClass);
    }
    if (htmlContent) {
      $svgElement.html(htmlContent);
    }
    return $svgElement;
  };

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
   * Implements the 'debounce' pattern. The given function fx is executed after a certain delay
   * (in milliseconds), but if the same function is called a second time within the waiting time,
   * the timer is reset. The default value for 'delay' is 250 ms.
   */
  $.debounce = function(fx, delay) {
    var delayer = null;
    delay = (delay !== undefined) ? delay : 250;
    return function() {
      var that = this,
        args = arguments;
      clearTimeout(delayer);
      delayer = setTimeout(function() {
        fx.apply(that, args);
      }, delay);
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
    return jQuery.ajax(options);
  };

  // === $.prototype extensions ===

  // prepend - and return new div for chaining
  $.fn.prependDiv = function(cssClass, htmlContent, id) {
    return $.makeDiv(cssClass, htmlContent, id).prependTo(this);
  };

  // append - and return new div for chaining
  $.fn.appendDiv = function(cssClass, htmlContent, id) {
    return $.makeDiv(cssClass, htmlContent, id).appendTo(this);
  };

  // insert after - and return new div for chaining
  $.fn.afterDiv = function(cssClass, htmlContent, id) {
    return $.makeDiv(cssClass, htmlContent, id).insertAfter(this);
  };

  // insert before - and return new div for chaining
  $.fn.beforeDiv = function(cssClass, htmlContent, id) {
    return $.makeDiv(cssClass, htmlContent, id).insertBefore(this);
  };

  $.fn.appendSpan = function(cssClass, text) {
    return $.makeSpan(cssClass, text).appendTo(this);
  };

  // append svg
  $.fn.appendSVG = function(type, id, cssClass, htmlContent) {
    return $.makeSVG(type, id, cssClass, htmlContent).appendTo(this);
  };

  $.pxToNumber = function(pixel) {
    if (!pixel) {
      // parseInt would return NaN if pixel is '' or undefined
      return 0;
    }
    // parseInt ignores 'px' and just extracts the number
    return parseInt(pixel, 10);
  };

  // attr and class handling for svg
  $.fn.attrSVG = function(attributeName, value) {
    return this.each(function() {
      this.setAttribute(attributeName, value);
    });
  };

  $.fn.attrXLINK = function(attributeName, value) {
    return this.each(function() {
      this.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:' + attributeName, value);
    });
  };

  $.fn.addClassSVG = function(cssClass) {
    return this.each(function() {
      if (!$(this).hasClassSVG(cssClass)) {
        var old = this.getAttribute('class');
        this.setAttribute('class', old + ' ' + cssClass);
      }
    });
  };

  $.fn.removeClassSVG = function(cssClass) {
    return this.each(function() {
      var old = ' ' + this.getAttribute('class') + ' ';
      this.setAttribute('class', old.replace(' ' + cssClass + ' ', ' '));
    });
  };

  $.fn.hasClassSVG = function(cssClass) {
    var old = ' ' + this.attr('class') + ' ';
    return old.indexOf(' ' + cssClass + ' ') > -1;
  };

  // select one and deselect siblings
  $.fn.selectOne = function() {
    this.siblings().removeClass('selected');
    this.addClass('selected');
    return this;
  };

  $.fn.select = function(selected) {
    return this.toggleClass('selected', !! selected);
  };

  $.fn.isSelected = function() {
    return this.hasClass('selected');
  };

  $.fn.setEnabled = function(enabled) {
    enabled = !! enabled;
    this.toggleClass('disabled', !enabled);
    // Toggle disabled attribute for elements that support it (see http://www.w3.org/TR/html5/disabled-elements.html)
    if (this.is('button, input, select, textarea, optgroup, option, fieldset')) {
      this.toggleAttr('disabled', !enabled);
    }
    return this;
  };

  $.fn.setTabbable = function(tabbable) {
    var tabIndex = 0;
    if (!tabbable) {
      tabIndex = null;
    }
    this.attr('tabIndex', tabIndex);
  };

  $.fn.isEnabled = function() {
    return !this.hasClass('disabled');
  };

  $.fn.setVisible = function(visible) {
    if (this.isVisible() === visible) {
      return this;
    }
    if (visible) {
      this.show();
    } else {
      this.hide();
    }
    return this;
  };

  $.fn.isDisplayNone = function() {
    return this.css('display') === 'none';
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
    return this.toggleAttr('placeholder', !! placeholder, placeholder);
  };

  /**
   * Returns false when the component display is 'none' or visibility is 'hidden', otherwise true.
   * Note: this gives other results than $.is(':visible'), since that method will also return false
   * when a component has absolute positioning and no width and height is defined (well, you cannot
   * see a component with a style like this, but technically it is not set to 'not visible').
   */
  $.fn.isVisible = function() {
    if ('none' === this.css('display')) {
      return false;
    }
    if ('hidden' === this.css('visibility')) {
      return false;
    }
    return true;
  };

  /**
   * @return true if the element is attached (= is in the dom tree), false if not
   */
  $.fn.isAttached = function() {
    return $.contains(document.documentElement, this[0]);
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
  },

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
  },

  // most used animate
  $.fn.animateAVCSD = function(attr, value, complete, step, duration) {
    var properties = {};
    var options = {};

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
  $.fn.animateSVG = function(attr, endValue, duration, complete) {
    return this.each(function() {
      var startValue = parseFloat($(this).attr(attr));

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
      delay: 1000,
      classesToRemove: className
    };
    options = $.extend({}, defaultOptions, options);
    this.addClass(className);
    setTimeout(function() {
      // remove class, otherwise animation will be executed each time the element changes it's visibility (attach/rerender),
      // and even each time when the css classes change
      this.removeClass(options.classesToRemove);
      // delay must be greater than css animation duration
    }.bind(this), options.delay);
  };

  // over engineered animate
  $.fn.widthToContent = function(duration, complete) {
    duration = scout.helpers.nvl(duration, 300);

    var oldW = this.outerWidth(),
      newW = this.css('width', 'auto').outerWidth(),
      finalWidth = this.data('finalWidth');

    if (newW !== oldW) {
      this.css('width', oldW);
    }

    if (newW !== finalWidth) {
      this.data('finalWidth', newW);
      this.stop().animateAVCSD('width', newW, function() {
        $(this).data('finalWidth', null);
        if (complete) {
          complete(); // call-back
        }
      }, null, duration);
    }

    return this;
  };

  $.fn.heightToContent = function(duration) {
    duration = (duration !== undefined) ? duration : 300;

    var oldH = this.outerHeight(),
      newH = this.css('height', 'auto').outerHeight(),
      finalHeight = this.data('finalHeight');
    if (newH !== oldH) {
      this.css('height', oldH);
    }

    if (newH !== finalHeight) {
      this.data('finalHeight', newH);
      this.stop().animateAVCSD('height', newH, function() {
        $(this).data('finalHeight', null);
      }, null, duration);
    }

    return newH;
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
    var newProperties = [];
    var $this = this;
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
    newProperties.forEach(function(prop) {
      $this.css(prop, $origin.css(prop));
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
   */
  $.fn.makeDraggable = function($handle) {
    var $draggable = this;
    $handle = $handle || $draggable;
    return $handle.on('mousedown.draggable', function(event) {
      var orig_offset = $draggable.offset();
      var orig_event = event;
      $handle.parents()
        .on('mousemove.dragging', function(event) {
          var top = orig_offset.top + (event.pageY - orig_event.pageY);
          var left = orig_offset.left + (event.pageX - orig_event.pageX);
          // do not drop outside of viewport (and leave a margin of 100 pixels)
          left = Math.max(100 - $handle.width(), left);
          left = Math.min($('body').width() - 100, left);
          top = Math.max(0, top); // must not be dragged outside of top, otherwise dragging back is impossible
          top = Math.min($(window).height() - 100, top);
          $draggable.offset({
            top: top,
            left: left
          });
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
    duration = (duration !== undefined) ? duration : 150;
    this.stop(true).fadeOut(duration, function() {
      $(this).remove();
      if (callback) {
        callback.call(this);
      }
    });
  };

  var _oldhide = $.fn.hide;
  $.fn.hide = function(speed, callback) {
    this.trigger('hide');
    var returnValue = _oldhide.apply(this, arguments);
    return returnValue;
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

  /**
   * Sets the cursor style of the jQuery object to 'wait' or 'default', depending on the
   * argument 'wait'.
   *
   * This is a workaround needed for Chrome. Usually, it would be sufficient to simply add
   * or remove a CSS class with the desired cursor style. However, due to a bug in Chrome,
   * the cursor change is not triggered until the mouse is moved. Because this behavior
   * could be very confusing, this function implements a workaround. The target cursor
   * style is set within a setTimeout() call, while the cursor is first set to a different
   * value. This ensures that Chrome is notified about the cursor change.
   *
   * Chrome bug: https://code.google.com/p/chromium/issues/detail?id=26723
   */
  $.fn.setMouseCursorWait = function(wait, defaultCursorStyle) {
    defaultCursorStyle = defaultCursorStyle || 'default';
    var cursor1 = (wait ? defaultCursorStyle : 'wait');
    var cursor2 = (wait ? 'wait' : defaultCursorStyle);

    this.css('cursor', cursor1);
    setTimeout(function() {
      this.css('cursor', cursor2);
    }.bind(this));
    return this;
  };

  $.fn.backupSelection = function() {
    var field = this[0];
    if (field && field === document.activeElement) {
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
    if (field && field === document.activeElement && selection) {
      field.setSelectionRange(selection.selectionStart, selection.selectionEnd, selection.selectionDirection);
    }
  };

  // ===== helpers for projects, may not necessarily be used by scout itself =====

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
      .attr('tabindex', "0")
      .attr('data-ref', appLinkBean.ref)
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
      doc = document,
      element = this[0];
    if (doc.body.createTextRange) {
        range = document.body.createTextRange();
        range.moveToElementText(element);
        range.select();
    } else if (window.getSelection) {
        range = document.createRange();
        range.selectNodeContents(element);
        window.getSelection().removeAllRanges();
        window.getSelection().addRange(range);
    }
  };

  /**
   * Check if content is truncated, e.g. detects if an ellipsis is added to text
   * (and is displayed instead of text itself).
   */
  $.fn.isContentTruncated = function() {
    var $clone = this
      .clone()
      .css('display', 'inline')
      .css('width', 'auto')
      .css('visibility', 'hidden')
      .appendTo('body');

    var ret = $clone.width() > this.width();
    $clone.remove();

    return ret;
  };
}(jQuery));
