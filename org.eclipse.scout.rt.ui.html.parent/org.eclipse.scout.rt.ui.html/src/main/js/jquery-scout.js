/**
 * JQuery plugin with scout extensions
 */

/*global console: false */
(function($) {

  // chris' shortcut
  $.l = console.log.bind(console);

  //== $ extensions
  $.makeDiv = function(id, cssClass, htmlContent) {
    if (id === 0) {
      //Allow 0 as id (!id would result in false)
      id = '0';
    }
    return $('<div' +
      (id ? ' id="' + id + '"' : '') +
      (cssClass ? ' class="' + cssClass + '"' : '') +
      (scout.device.supportsCssUserSelect() ? '' : ' unselectable="on"') + // workaround for IE 9
      '>' +
      (htmlContent || '') +
      '</div>'
    );
  };

  $.makeDIV = function(cssClass, htmlContent) {
    return $.makeDiv(undefined, cssClass, htmlContent);
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
    event.stopPropagation();
  };

  /**
   * Implements the 'debounce' pattern. The given function fx is executed after a certain delay
   * (in milliseconds), but if the same function is called a second time within the waiting time,
   * the timer is reset. The default value for 'delay' is 250 ms.
   */
  $.debounce = function(fx, delay) {
    var delayer = null;
    delay = (typeof delay !== 'undefined') ? delay : 250;
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

  $.nl2br = function(text) {
    if (!text) {
      return text;
    }

    return text.replace(/\n/g,"<br>");
  };

  $.removeAmpersand = function(text) {
    if (!text) {
      return text;
    }

    return text.replace('&', '');
  };

  //== $.prototype extensions

  // prepend - and return new div for chaining
  $.fn.prependDiv = function(id, cssClass, htmlContent) {
    return $.makeDiv(id, cssClass, htmlContent).prependTo(this);
  };

  // append - and return new div for chaining
  $.fn.appendDiv = function(id, cssClass, htmlContent) {
    return $.makeDiv(id, cssClass, htmlContent).appendTo(this);
  };

  // insert after - and return new div for chaining
  $.fn.afterDiv = function(id, cssClass, htmlContent) {
    return $.makeDiv(id, cssClass, htmlContent).insertAfter(this);
  };

  // insert before - and return new div for chaining
  $.fn.beforeDiv = function(id, cssClass, htmlContent) {
    return $.makeDiv(id, cssClass, htmlContent).insertBefore(this);
  };

  // prepend without id - and return new div for chaining
  $.fn.prependDIV = function(cssClass, htmlContent) {
    return $.makeDiv(undefined, cssClass, htmlContent).prependTo(this);
  };

  // append without id - and return new div for chaining
  $.fn.appendDIV = function(cssClass, htmlContent) {
    return $.makeDiv(undefined, cssClass, htmlContent).appendTo(this);
  };

  // insert after without id - and return new div for chaining
  $.fn.afterDIV = function(cssClass, htmlContent) {
    return $.makeDiv(undefined, cssClass, htmlContent).insertAfter(this);
  };

  // insert before without id - and return new div for chaining
  $.fn.beforeDIV = function(cssClass, htmlContent) {
    return $.makeDiv(undefined, cssClass, htmlContent).insertBefore(this);
  };


  // append svg
  $.fn.appendSVG = function(type, id, cssClass, htmlContent) {
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
    return $svgElement.appendTo(this);
  };

  $.pxToNumber = function(pixel) {
    var value = pixel.replace('px', '');
    return parseInt(value, 10);
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

  //todo cgu geht auch mit toggle Class nicht?
  $.fn.updateClass = function(condition, cssClass) {
    if (condition) {
      this.addClass(cssClass);
    } else {
      this.removeClass(cssClass);
    }
    return this;
  };

  $.fn.select = function(selected) {
    return this.updateClass(selected, 'selected');
  };

  $.fn.isSelected = function() {
    return this.hasClass('selected');
  };

  $.fn.setEnabled = function(enabled) {
    if (enabled) {
      this.removeAttr('disabled');
      this.removeClass('disabled');
    } else {
      this.attr('disabled', 'disabled');
      this.addClass('disabled');
    }
    return this;
  };

  $.fn.isEnabled = function() {
    return this.attr('disabled') === undefined;
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
      this.attr('data-icon', iconId);
    } else {
      this.removeAttr('data-icon');
    }
    return this;
  };

  $.fn.placeholder = function(placeholder) {
    if (placeholder) {
      this.attr('placeholder', placeholder);
    } else {
      this.removeAttr('placeholder');
    }
    return this;
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
    duration = duration || 300;

    var oldW = this.outerWidth(),
      newW = this.css('width', 'auto').outerWidth(),
      finalWidth = this.data('finalWidth');

    if (newW !== oldW) {
      this.css('width', oldW);
    }

    if (newW !== finalWidth) {
      this.data('finalWidth', newW);
      this.stop().animateAVCSD('width', newW, null, function() {
        $(this).data('finalWidth', null);
      }, duration);
    }

    return this;
  };

  $.fn.heightToContent = function(duration) {
    duration = duration || 300;

    var oldH = this.outerHeight(),
      newH = this.css('height', 'auto').outerHeight(),
      finalHeight = this.data('finalHeight');

    if (newH !== oldH) {
      this.css('height', oldH);
    }

    if (newH !== finalHeight) {
      this.data('finalHeight', newH);
      this.stop().animateAVCSD('height', newH, null, function() {
        $(this).data('finalHeight', null);
      }, duration);
    }

    return newH;
  };

  $.fn.cssLeft = function(position) {
    return this.css('left', position + 'px');
  };

  $.fn.cssTop = function(position) {
    return this.css('top', position + 'px');
  };

  $.fn.cssRight = function(position) {
    return this.css('right', position + 'px');
  };

  $.fn.cssWidth = function(width) {
    return this.css('width', width + 'px');
  };

  $.fn.cssHeight = function(height) {
    return this.css('height', height + 'px');
  };

  $.fn.cssMarginLeft = function(value) {
    if (value !== undefined) {
      return this.css('margin-left', value + 'px');
    }
    return $.pxToNumber(this.css('margin-left'));
  };

  $.fn.cssMarginBottom = function() {
    return $.pxToNumber(this.css('margin-bottom'));
  };

  $.fn.cssMarginRight = function() {
    return $.pxToNumber(this.css('margin-right'));
  };

  $.fn.cssBorderBottomWidth = function() {
    return $.pxToNumber(this.css('border-bottom-width'));
  };

  $.fn.cssBorderLeftWidth = function() {
    return $.pxToNumber(this.css('border-left-width'));
  };

  $.fn.cssBorderRightWidth = function() {
    return $.pxToNumber(this.css('border-right-width'));
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

}(jQuery));
