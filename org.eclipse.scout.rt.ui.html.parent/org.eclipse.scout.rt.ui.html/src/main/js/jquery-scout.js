/**
 * JQuery plugin with scout extensions
 */
/*global console: false */
(function($) {

  //== $ extensions
  $.log = console.log.bind(console);

  $.makeDiv = function(id, cssClass, htmlContent) {
    return $('<div' +
      (id ? ' id="' + id + '"' : '') +
      (cssClass ? ' class="' + cssClass + '"' : '') +
      (scout.device.supportsCssProperty('user-select') ? '' : ' unselectable="on"') + // workaround for IE 9
      '>' +
      (htmlContent || '') +
      '</div>'
    );
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
    if (selected) {
      this.addClass('selected');
    } else {
      this.removeClass('selected');
    }
    return this;
  };

  $.fn.isSelected = function() {
    return this.hasClass('selected');
  };

  $.fn.setEnabled = function(enabled) {
    if (enabled) {
      this.removeAttr('disabled');
    } else {
      this.attr('disabled', 'disabled');
    }
    return this;
  };

  $.fn.isEnabled = function() {
    return this.attr('disabled');
  };

  $.fn.setVisible = function(visible) {
    if (visible) {
      this.show();
    } else {
      this.hide();
    }
    return this;
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

    if (newW != oldW) {
      this.css('width', oldW);
    }

    if (newW != finalWidth) {
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

    if (newH != oldH) {
      this.css('height', oldH);
    }

    if (newH != finalHeight) {
      this.data('finalHeight', newH);
      this.stop().animateAVCSD('height', newH, null, function() {
        $(this).data('finalHeight', null);
      }, duration);
    }

    return newH;
  };

}(jQuery));
