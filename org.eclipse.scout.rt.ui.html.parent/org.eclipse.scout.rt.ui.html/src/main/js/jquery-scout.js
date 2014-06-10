/**
 * JQuery plugin with scout extensions
 */
/*global console: false */
(function($) {

  //== $ extensions
  $.log = console.log.bind(console);

  // session uses only divs...
  $.makeDiv = function(i, c, h) {
    i = i ? ' id="' + i + '"' : '';
    c = c ? ' class="' + c + '"' : '';
    h = h || '';
    return $('<div ' + i + c + '>' + h + '</div>');
  };

  // used by some animate functions
  $.removeThis = function() {
    $(this).remove();
  };

  $.DOUBLE_CLICK_DELAY_TIME = 250;

  /**
   * This event may be used to listen on click and on double click events on the same element.<p>
   * This special event handling is necessary because javascript fires click events even if a double click happened.
   * Therefore it is not possible to detect whether it is a double click or a click action.
   */
  $.event.special.clicks = {
    delegateType: "click",
    bindType: "click",
    handle: function(event) {
      var handleObj = event.handleObj;
      var targetData = $.data(event.target);
      var ret = null;

      if (!targetData.clicks) {
        targetData.clicks = 0;
      }
      targetData.clicks++;

      if (targetData.clicks == 2) {
        clearTimeout(targetData.clickTimer);
        targetData.clickTimer = null;
        targetData.clicks = null;

        event.type = 'doubleClick';
        ret = handleObj.handler.apply(this, [event]);
        event.type = handleObj.type;
        return ret;
      } else {
        targetData.clickTimer = setTimeout(function() {
          targetData.clickTimer = null;
          targetData.clicks = null;

          event.type = 'singleClick';
          ret = handleObj.handler.apply(this, [event]);
          event.type = handleObj.type;
          return ret;
        }, $.DOUBLE_CLICK_DELAY_TIME);
      }
    }
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
      dataType: "script",
      cache: true,
      url: url
    });

    return jQuery.ajax(options);
  };

  //== $.prototype extensions

  // prepend - and return new div for chaining
  $.fn.prependDiv = function(i, c, h) {
    return $.makeDiv(i, c, h).prependTo(this);
  };

  // append - and return new div for chaining
  $.fn.appendDiv = function(i, c, h) {
    return $.makeDiv(i, c, h).appendTo(this);
  };

  // insert after - and return new div for chaining
  $.fn.afterDiv = function(i, c, h) {
    return $.makeDiv(i, c, h).insertAfter(this);
  };

  // insert before - and return new div for chaining
  $.fn.beforeDiv = function(i, c, h) {
    return $.makeDiv(i, c, h).insertBefore(this);
  };

  // append svg
  $.fn.appendSVG = function(t, i, c, h) {
    var $svgElement = $(document.createElementNS("http://www.w3.org/2000/svg", t));
    if (i) $svgElement.attr('id', i);
    if (c) $svgElement.attr('class', c);
    if (h) $svgElement.html(h);
    return $svgElement.appendTo(this);
  };

  // attr and class handling for svg
  $.fn.attrSVG = function(a, v) {
    return this.each(function() {
      this.setAttribute(a, v);
    });
  };

  $.fn.attrXLINK = function(a, v) {
    return this.each(function() {
      this.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:' + a, v);
    });
  };

  $.fn.addClassSVG = function(c) {
    return this.each(function() {
      if (!$(this).hasClassSVG(c)) {
        var old = this.getAttribute('class');
        this.setAttribute('class', old + ' ' + c);
      }
    });
  };

  $.fn.removeClassSVG = function(c) {
    return this.each(function() {
      var old = ' ' + this.getAttribute('class') + ' ';
      this.setAttribute('class', old.replace(' ' + c + ' ', ' '));
    });
  };

  $.fn.hasClassSVG = function(c) {
    var old = ' ' + this.attr('class') + ' ';
    return old.indexOf(' ' + c + ' ') > -1;
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

  // most used animate
  $.fn.animateAVCSD = function(attr, value, complete, step, duration) {
    var properties = {},
      options = {};

    properties[attr] = value;
    if (complete) options.complete = complete;
    if (step) options.step = step;
    if (duration) options.duration = duration;
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
