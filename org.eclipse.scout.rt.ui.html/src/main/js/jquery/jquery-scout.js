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
   * Convenience function that can be used as an jQuery event handler, when
   * this event should be "swallowed". Technically, this function just calls
   * 'stopPropagation()' on the event.
   */
  $.suppressEvent = function(event) {
    if (event) {
      event.stopPropagation();
      event.preventDefault();
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
    return this.toggleClass('selected', selected);
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
    if (visible) {
      this.show();
    } else {
      this.hide();
    }
    return this;
  };

  $.fn.icon = function(iconId) {
    if (iconId) {
      var icon = scout.icons.parseIconId(iconId);
      if (icon.isFontIcon()) {
        this.attr('data-icon', icon.iconCharacter);
        this.attr('data-iconFont', icon.font);
      } else {
        // FIXME BSH Handle URL icons. Check also: Button.js
      }
    } else {
      this.removeAttr('data-icon');
    }
    return this;
  };

  $.fn.placeholder = function(placeholder) {
    return this.toggleAttr('placeholder', !!placeholder, placeholder);
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

  // over engineered animate
  $.fn.widthToContent = function(duration) {
    duration = (duration !== undefined) ? duration : 300;

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

  $.fn.disableSpellcheck = function() {
    return this.attr('spellcheck', false);
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
          $draggable.offset({
            top: orig_offset.top + (event.pageY - orig_event.pageY),
            left: orig_offset.left + (event.pageX - orig_event.pageX)
          });
        })
        .on('mouseup.dragging', function(e) {
          $handle.parents().off('.dragging');
        });
      event.preventDefault();
    });
  };

  /**
   * Installs listeners to the given element to ensure that the focus cannot leave it with the TAB key. Instead,
   * when the last focusable child element is reached, the focus is set the first focusable element automatically.
   *
   * @param $firstFocusElement If the argument is a jQuery object, the current focus is set to this element. If the
   *   argument is the string 'auto', the current focus is set to the first focusable element inside the parent
   *   element. All other values don't change the current focus.
   */
  $.fn.installFocusContext = function($firstFocusElement, uiSessionId) {
    scout.focusManager.installFocusContext(this, uiSessionId, $firstFocusElement);
  };

  $.fn.uninstallFocusContext = function(uiSessionId) {
    scout.focusManager.uninstallFocusContextForContainer(this, uiSessionId);
  };

  /**
   * Calls jQuery.fadeOut() and then removes the element from the DOM.
   * Default fade-out duration is 150 ms.
   */
  $.fn.fadeOutAndRemove = function(duration) {
    duration = (duration !== undefined) ? duration : 150;
    this.stop(true).fadeOut(duration, $.removeThis);
  };

  var _oldhide = $.fn.hide;
  $.fn.hide = function(speed, callback) {
    this.trigger('hide');
    return _oldhide.apply(this, arguments);
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
    }
    else {
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
      }
      else {
        // remove attr
        $element.removeAttr(attr);
      }
    });
};

  // === helpers for projects, may not necesserily be used by scout itself ===
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

}(jQuery));
