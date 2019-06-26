import * as $ from 'jquery';
import * as arrays from './arrays';
import * as scout from '../scout';

export default class JQueryUtils extends $ {

  static bootstrap() {
    let functions = JQueryUtils.getFunctionsOf(this)
      .filter(f => f[0] !== 'getFunctionsOf' && f[0] !== 'bootstrap' && f[0] !== 'linkFunctionsTo');
    this.linkFunctionsTo(functions, $);
    this.linkFunctionsTo(functions, $.fn);
  }

  static linkFunctionsTo(functions, obj) {
    functions
      .filter(f => typeof obj[f[0]] === 'undefined')
      .map(f => obj[f[0]] = f[1]);
  }

  static getFunctionsOf(obj) {
    return Object.getOwnPropertyNames(obj)
      .filter(name => typeof obj[name] === 'function')
      .map(name => [name, obj[name]]);
  }

  //////////////////////////////////////////////////////////////
  ////////////////////// jQuery additions //////////////////////
  //////////////////////////////////////////////////////////////

  static document(domElement) {
    var myDocument = this.length ? (this[0] instanceof Document ? this[0] : this[0].ownerDocument) : null;
    return domElement ? myDocument : $(myDocument);
  };

  static appendDiv(cssClass, text) {
    return this.makeDiv(cssClass, text).appendTo(this);
  };

  static prependDiv(cssClass, text) {
    return this.makeDiv(cssClass, text).prependTo(this);
  };

  static isDisplayNone() {
    return this.css('display') === 'none';
  };

  static body(domElement) {
    var $body = $('body', this.document(true));
    return domElement ? $body[0] : $body;
  };

  static window(domElement) {
    var myDocument = this.document(true),
      myWindow = myDocument ? myDocument.defaultView : null;
    return domElement ? myWindow : $(myWindow);
  };

  static setEnabled(enabled) {
    enabled = !!enabled;
    this.toggleClass('disabled', !enabled);
    // Toggle disabled attribute for elements that support it (see http://www.w3.org/TR/html5/disabled-elements.html)
    if (this.is('button, input, select, textarea, optgroup, option, fieldset')) {
      this.toggleAttr('disabled', !enabled);
    }
    return this;
  };

  static isAttached() {
    return $.contains(this.document(true).documentElement, this[0]);
  };

  static toggleAttr(attr, state, value) {
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

  static makeDiv(cssClass, text) {
    var $div = this.makeElement('<div>', cssClass, text);

    // scout.device may not be initialized yet (e.g. before app is created or if app bootstrap fails)
    /*var unselectable = (scout.device ? scout.device.unselectableAttribute : scout.Device.DEFAULT_UNSELECTABLE_ATTRIBUTE);
    if (unselectable.key) {
        $div.attr(unselectable.key, unselectable.value);
    }*/

    return $div;
  };

  static makeElement(element, cssClass, text) {
    var myDocument = $(document);
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

  static promiseAll(promises, asArray) {
    asArray = scout.nvl(asArray, false);
    promises = arrays.ensure(promises);
    var deferred = $.Deferred();
    $.when.apply($, promises).done(function() {
      if (asArray) {
        deferred.resolve(this.argumentsToArray(arguments));
      } else {
        deferred.resolve.apply(this, arguments);
      }
    }).fail(function() {
      deferred.reject.apply(this, arguments);
    });
    return deferred.promise();
  };

  static resolvedPromise() {
    var deferred = $.Deferred();
    deferred.resolve.apply(deferred, arguments);
    return deferred.promise();
  }

  static setVisible(visible) {
    var isVisible = !this.hasClass('hidden');
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

  static isEveryParentVisible() {
    var everyParentVisible = true;
    this.parents().each(function() {
      if (!$(this).isVisible()) {
        everyParentVisible = false;
        return false;
      }
    });
    return everyParentVisible;
  };

  static rejectedPromise() {
    var deferred = $.Deferred();
    deferred.reject.apply(deferred, arguments);
    return deferred.promise();
  };

  /**
   * Helper function to determine if an object is of type 'jqXHR' (http://api.jquery.com/jQuery.ajax/#jqXHR)
   */
  static isJqXHR(obj) {
    return (typeof obj === 'object' && obj.hasOwnProperty('readyState') && obj.hasOwnProperty('status') && obj.hasOwnProperty('statusText'));
  };

  static attrOrRemove(attributeName, value) {
    if (value) {
      this.attr(attributeName, value);
    } else {
      this.removeAttr(attributeName);
    }
    return this;
  };

  static ajaxJson(url) {
    return $.ajax({
      url: url,
      dataType: 'json',
      contentType: 'application/json; charset=UTF-8'
    }).catch(function() {
      // Reject the promise with usual arguments (jqXHR, textStatus, errorThrown), but add the request
      // options as additional argument (e.g. to make the URL available to the error handler)
      var args = scout.argumentsToArray(arguments);
      args.push(this);
      return this.rejectedPromise.apply($, args);
    });
  }

  static cssPxValue(prop, value) {
    if (value === undefined) {
      return $.pxToNumber(this.css(prop));
    }
    if (value === null) {
      value = ''; // 'null' should also remove the CSS property
    }
    if (typeof value === 'string') {
      return this.css(prop, value);
    }
    return this.css(prop, value + 'px');
  }

  static pxToNumber(pixel) {
    if (!pixel) {
      // parseFloat would return NaN if pixel is '' or undefined
      return 0;
    }
    // parseFloat ignores 'px' and just extracts the number
    return parseFloat(pixel);
  };

  static cssMarginLeft(value) {
    return this.cssPxValue('margin-left', value);
  };

  static cssMarginBottom(value) {
    return this.cssPxValue('margin-bottom', value);
  };

  static cssMarginRight(value) {
    return this.cssPxValue('margin-right', value);
  };

  static cssMarginTop(value) {
    return this.cssPxValue('margin-top', value);
  };

  static cssMarginX(value) {
    if (value === undefined) {
      return this.cssMarginLeft() + this.cssMarginRight();
    }
    this.cssMarginLeft(value);
    this.cssMarginRight(value);
    return this;
  };

  static cssMarginY(value) {
    if (value === undefined) {
      return this.cssMarginTop() + this.cssMarginBottom();
    }
    this.cssMarginTop(value);
    this.cssMarginBottom(value);
    return this;
  };

  static cssPaddingTop(value) {
    return this.cssPxValue('padding-top', value);
  };

  static cssPaddingRight(value) {
    return this.cssPxValue('padding-right', value);
  };

  static cssPaddingBottom(value) {
    return this.cssPxValue('padding-bottom', value);
  };

  static cssPaddingLeft(value) {
    return this.cssPxValue('padding-left', value);
  };

  static cssPaddingX(value) {
    if (value === undefined) {
      return this.cssPaddingLeft() + this.cssPaddingRight();
    }
    this.cssPaddingLeft(value);
    this.cssPaddingRight(value);
    return this;
  };

  static cssPaddingY(value) {
    if (value === undefined) {
      return this.cssPaddingTop() + this.cssPaddingBottom();
    }
    this.cssPaddingTop(value);
    this.cssPaddingBottom(value);
    return this;
  };

  static cssLeft(position) {
    return this.cssPxValue('left', position);
  };

  static cssTop(position) {
    return this.cssPxValue('top', position);
  };

  static cssPosition(point) {
    return this.cssLeft(point.x).cssTop(point.y);
  };

  static cssBottom(position) {
    return this.cssPxValue('bottom', position);
  };

  static cssRight(position) {
    return this.cssPxValue('right', position);
  };

  static cssWidth(width) {
    return this.cssPxValue('width', width);
  };

  static cssHeight(height) {
    return this.cssPxValue('height', height);
  };

  static isVisible() {
    return this.elemVisible(this[0]);
  };

  static elemVisible(elem) {
    // Check if element itself is hidden by its own style attribute
    if (!elem || isHidden(elem.style)) {
      return false;
    }
    // Must use correct window for element / computedStyle
    var myWindow = (elem instanceof Document ? elem : elem.ownerDocument).defaultView;
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

  static cssMinWidth(minWidth) {
    if (minWidth === undefined) {
      var value = this.css('min-width');
      if (value === 'auto') {
        return 0;
      }
      return $.pxToNumber(value);
    }
    return this.cssPxValue('min-width', minWidth);
  };

  static appendSpan(cssClass, text) {
    return this.makeSpan(cssClass, text).appendTo(this);
  };

  static setTabbable(tabbable) {
    return this.attr('tabIndex', tabbable ? 0 : null);
  };

  static isTabbable() {
    return this.attr('tabIndex') >= 0;
  };

  static makeSpan(cssClass, text) {
    return this.makeElement('<span>', cssClass, text);
  };

  static isOrHas(elem) {
    if (elem instanceof $) {
      elem = elem[0];
    }
    return this[0] === elem || this.has(elem).length > 0;
  };

  /**
   * @returns the max-width as number. If max-width is not set (resp. defaults to 'none') Number.MAX_VALUE is returned.
   */
  static cssMaxWidth(maxWidth) {
    if (maxWidth === undefined) {
      var value = this.css('max-width');
      if (value === 'none') {
        return Number.MAX_VALUE;
      }
      return $.pxToNumber(value);
    }
    return this.cssPxValue('max-width', maxWidth);
  };

  static cssMinHeight(minHeight) {
    if (minHeight === undefined) {
      var value = this.css('min-height');
      if (value === 'auto') {
        return 0;
      }
      return $.pxToNumber(value);
    }
    return this.cssPxValue('min-height', minHeight);
  };

  /**
   * @returns the max-height as number. If max-height is not set (resp. defaults to 'none') Number.MAX_VALUE is returned.
   */
  static cssMaxHeight(maxHeight) {
    if (maxHeight === undefined) {
      var value = this.css('max-height');
      if (value === 'none') {
        return Number.MAX_VALUE;
      }
      return $.pxToNumber(value);
    }
    return this.cssPxValue('max-height', maxHeight);
  };

  static unfocusable() {
    return this.addClass('unfocusable');
  };

}
