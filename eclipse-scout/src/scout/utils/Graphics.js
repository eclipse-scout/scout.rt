import * as $ from 'jquery';
import Dimension from './Dimension';
import Scout from '../Scout';
import Rectangle from './Rectangle';
import Insets from './Insets';
import Arrays from './Arrays';
import Point from './Point';

export default class Graphics {

  static size($elem, options) {
    if (!$elem[0] || $elem.isDisplayNone()) {
      return new Dimension(0, 0);
    }

    if (typeof options === 'boolean') {
      options = {
        includeMargin: options
      };
    } else {
      options = options || {};
    }

    var bcr = $elem[0].getBoundingClientRect();
    var size = new Dimension(bcr.width, bcr.height);
    var includeMargin = Scout.nvl(options.includeMargin, false);
    if (includeMargin) {
      size.width += $elem.cssMarginX();
      size.height += $elem.cssMarginY();
    }
    // see comments in prefSize()
    var exact = Scout.nvl(options.exact, false);
    if (!exact) {
      size.width = Math.ceil(size.width);
      size.height = Math.ceil(size.height);
    }
    return size;
  }

  static prefSize($elem, options) {
    // Return 0/0 if element is not displayed (display: none).
    // We don't use isVisible by purpose because isVisible returns false for elements with visibility: hidden which is wrong here (we would like to be able to measure hidden elements)
    if (!$elem[0] || $elem.isDisplayNone()) {
      return new Dimension(0, 0);
    }

    if (typeof options === 'boolean') {
      options = {
        includeMargin: options
      };
    } else {
      options = options || {};
    }

    var defaults = {
      includeMargin: false,
      useCssSize: false,
      widthHint: undefined,
      heightHint: undefined,
      restoreScrollPositions: true
    };
    options = $.extend({}, defaults, options);

    if (options.animateClasses && options.animateClasses.length > 0) {
      return this.prefSizeWithoutAnimation($elem, options);
    }

    var oldStyle = $elem.attr('style');
    var oldScrollLeft = $elem.scrollLeft();
    var oldScrollTop = $elem.scrollTop();

    /*if (options.restoreScrollPositions) {
        scout.scrollbars.storeScrollPositions($elem);
    }*/

    // UseCssSize is necessary if the css rules have a fix height or width set.
    // Otherwise setting the width/height to auto could result in a different size
    var newWidth = (options.useCssSize ? '' : Scout.nvl(options.widthHint, 'auto'));
    var newHeight = (options.useCssSize ? '' : Scout.nvl(options.heightHint, 'auto'));

    // modify properties which prevent reading the preferred size
    $elem.css({
      'width': newWidth,
      'height': newHeight
    });

    // measure
    var bcr = $elem[0].getBoundingClientRect();
    var prefSize = new Dimension(bcr.width, bcr.height);
    if (options.includeMargin) {
      prefSize.width += $elem.cssMarginX();
      prefSize.height += $elem.cssMarginY();
    }

    // reset the modified style attribute
    $elem.attrOrRemove('style', oldStyle);
    $elem.scrollLeft(oldScrollLeft);
    $elem.scrollTop(oldScrollTop);

    /*if (options.restoreScrollPositions) {
        scout.scrollbars.restoreScrollPositions($elem);
    }*/

    // Ensure resulting numbers are integers. getBoundingClientRect() might correctly return fractional values
    // (because of the browser's sub-pixel rendering). However, if we use those numbers to set the size
    // of an element using CSS, it gets rounded or cut off. The behavior is not defined amongst different
    // browser engines.
    // Example:
    // - Measured size from this method:      h = 345.239990234375
    // - Set the size to an element:          $elem.css('height', h + 'px')
    // - Results:
    //     IE                   <div id='elem' style='height: 345.23px'>     [Fractional part cut off after two digits]
    //     Firefox & Chrome     <div id='elem' style='height: 345.24px'>     [Fractional part rounded to three digits]
    var exact = Scout.nvl(options.exact, false);
    if (!exact) {
      prefSize.width = Math.ceil(prefSize.width);
      prefSize.height = Math.ceil(prefSize.height);
    }

    return prefSize;
  }

  static prefSizeWithoutAnimation($elem, options) {
    var animateClass = Arrays.find(options.animateClasses, function(cssClass) {
      return $elem.hasClass(cssClass);
    });
    options = $.extend({}, options);
    options.animateClasses = null;

    if (!animateClass) {
      return this.prefSize($elem, options);
    }

    var $clone = $elem
      .clone()
      .removeClass(animateClass)
      .appendTo($elem.parent());
    var prefSize = Graphics.prefSize($clone, options);
    $clone.remove();
    return prefSize;
  }

  static offsetBounds($elem, options) {
    return Graphics._bounds($elem, $elem.offset(), options);
  }

  static _bounds($elem, pos, options) {
    var size = Graphics.size($elem, options);
    return new Rectangle(pos.left, pos.top, size.width, size.height);
  }

  static setLocation($comp, vararg, y) {
    var point = vararg instanceof Point ? vararg : new Point(vararg, y);
    $comp
      .cssLeft(point.x)
      .cssTop(point.y);
  };

  /**
   * Returns a scout.Point consisting of the component's 'cssLeft' and
   * 'cssTop' values (reverse operation to setLocation).
   */
  static location($comp) {
    return new Point($comp.cssLeft(), $comp.cssTop());
  }

  static offset($elem) {
    var pos = $elem.offset();
    return new Point(pos.left, pos.top);
  }

  static position($elem) {
    var pos = $elem.position();
    return new Point(pos.left, pos.top);
  }

  static setSize($comp, vararg, height) {
    var size = vararg instanceof Dimension ? vararg : new Dimension(vararg, height);
    $comp
      .cssWidth(size.width)
      .cssHeight(size.height);
  }

  static insets($comp, options) {
    if (typeof options === 'boolean') {
      options = {
        includeMargin: options
      };
    } else {
      options = options || {};
    }

    var i,
      directions = ['top', 'right', 'bottom', 'left'],
      insets = [0, 0, 0, 0],
      includeMargin = Scout.nvl(options.includeMargin, false),
      includePadding = Scout.nvl(options.includePadding, true),
      includeBorder = Scout.nvl(options.includeBorder, true);

    for (i = 0; i < directions.length; i++) {
      if (includeMargin) {
        insets[i] += $comp.cssPxValue('margin-' + directions[i]);
      }
      if (includePadding) {
        insets[i] += $comp.cssPxValue('padding-' + directions[i]);
      }
      if (includeBorder) {
        insets[i] += $comp.cssPxValue('border-' + directions[i] + '-width');
      }
    }
    return new Insets(insets[0], insets[1], insets[2], insets[3]);
  };

  static margins($comp) {
    return this.insets($comp, {
      includeMargin: true,
      includePadding: false,
      includeBorder: false
    });
  }

  static paddings($comp) {
    return this.insets($comp, {
      includeMargin: false,
      includePadding: true,
      includeBorder: false
    });
  };

  static borders($comp) {
    return this.insets($comp, {
      includeMargin: false,
      includePadding: false,
      includeBorder: true
    });
  }
}
