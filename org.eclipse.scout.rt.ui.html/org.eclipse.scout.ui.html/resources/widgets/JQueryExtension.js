// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

// my log function!
var log = console.log.bind(console);

//
// extend jQuery, with all helpers
//

(function ($) {
  // scout uses only divs...
    $.makeDiv = function (i, c, h) {
    i = i ? ' id="' + i + '"' : '';
    c = c ? ' class="' + c + '"' : '';
    h = h || '';
    return $('<div ' + i + c + '>' + h + '</div>');
    };

  // prepend - and return new div for chaining
    $.fn.prependDiv = function (i, c, h) {
    return $.makeDiv(i, c, h).prependTo(this);
    };

  // append - and return new div for chaining
    $.fn.appendDiv = function (i, c, h) {
    return $.makeDiv(i, c, h).appendTo(this);
    };

  // insert after - and return new div for chaining
    $.fn.afterDiv = function (i, c, h) {
    return $.makeDiv(i, c, h).insertAfter(this);
    };

  // insert before - and return new div for chaining
    $.fn.beforeDiv = function (i, c, h) {
    return $.makeDiv(i, c, h).insertBefore(this);
    };

  // select one and deselect siblings
  $.fn.selectOne = function () {
    this.siblings().removeClass('selected');
    this.addClass('selected');
    return this;
  };

  $.fn.select = function (selected) {
    if(selected) {
      this.addClass('selected');
    }
    else {
      this.removeClass('selected');
    }

    return this;
  };

  // most used animate
  $.fn.animateAVCSD = function (attr, value, complete, step, duration) {
    var properties = {},
      options = {};

    properties[attr] = value;
    if (complete) options.complete = complete;
    if (step) options.step = step;
    if (duration) options.duration = duration;

    this.animate(properties, options);
    return this;
  };

  // used by some animate functions
  $.removeThis = function () { $(this).remove(); };

}(jQuery));
