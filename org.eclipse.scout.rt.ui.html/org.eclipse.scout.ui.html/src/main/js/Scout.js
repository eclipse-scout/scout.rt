// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

/*global console: false */
/*exported log*/

// scout namespace
Scout = {};

//local log function
var log = console.log.bind(console);

//extend jQuery, with all helpers
(function($) {
  // scout uses only divs...
  $.makeDiv = function(i, c, h) {
    i = i ? ' id="' + i + '"' : '';
    c = c ? ' class="' + c + '"' : '';
    h = h || '';
    return $('<div ' + i + c + '>' + h + '</div>');
  };

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
  $.fn.widthToContent = function() {
    var oldW = this.outerWidth(),
      newW = this.css('width', 'auto').outerWidth(),
      finalWidth = this.data('finalWidth');

    if (newW != oldW) {
      this.css('width', oldW);
    }

    if (newW != finalWidth) {
      this.stop().animateAVCSD('width', newW, null, function() {
        $(this).data('finalWidth', null);
      });
      this.data('finalWidth', newW);
    }

    return this;
  };

  // used by some animate functions
  $.removeThis = function() {
    $(this).remove();
  };

  // converter functions constants
  // todo: holen aus kleinem Array
  // todo: verschiebene aller locale dinge nach Scout.Locale

  $.DEC = '.';
  $.GROUP = "'";
  $.DATE = ['dd', 'mm', 'yyyy'];
  $.DATE_SEP = '.';
  $.WEEKDAY = ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'];
  $.WEEKDAY_LONG = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag', 'Sonntag'];
  $.MONTH = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'];
  $.MONTH_LONG = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'];

  // converter functions
  $.numberToString = function(number, round) {
    var string = String(number.toFixed(round));

    // find start and end position of main number
    var start = string.indexOf($.DEC);
    var end = string.indexOf('-');

    start = (start == -1 ? string.length : start);
    end = (end == -1 ? 0 : 1);

    // group digits
    for (var i = start - 3; i > end; i -= 3) {
      string = string.substr(0, i) + $.GROUP + string.substr(i);
    }

    return string;
  };

  $.stringToDate = function(string) {
    var splitter = string.split($.DATE_SEP);

    var d = parseInt(splitter[$.DATE.indexOf('dd')], 10);
    var m = parseInt(splitter[$.DATE.indexOf('mm')], 10);
    var y = parseInt(splitter[$.DATE.indexOf('yyyy')], 10);

    return new Date((y < 100 ? y + 2000 : y), m - 1, d);
  };

  $.dateToString = function(date) {
    var d = date.getDate(),
      m = date.getMonth() + 1,
      y = date.getFullYear();

    var string = $.DATE.join($.DATE_SEP);

    return string.replace('dd', (d <= 9 ? '0' + d : d)).replace('mm', (m <= 9 ? '0' + m : m)).replace('yyyy', y);
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
}(jQuery));
