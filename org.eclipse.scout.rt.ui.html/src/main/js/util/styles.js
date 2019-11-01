/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays} from '../index';
import {strings} from '../index';
import {numbers} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';



/**
 * @memberOf scout.styles
 */
let styleMap = {};

let element = null;

/**
 * Generates an invisible div and appends it to the body, only once. The same div will be reused on subsequent calls.
 * Adds the given css class to that element and returns a style object containing the values for every given property.
 * The style is cached. Subsequent calls with the same css class will return the same style object.
 */
export function get(cssClass, properties, additionalClass) {
  var style = styleMap[cssClass];
  // ensure array
  properties = arrays.ensure(properties);
  properties = properties.map(function(prop) {
    return {
      name: prop,
      // replace property names like 'max-width' in 'maxWidth'
      nameCamelCase: prop.replace(/-(.)/g,
        function(match, p1) {
          return p1.toUpperCase();
        })
    };
  });

  // ensure style
  if (!style) {
    style = {};
    put(cssClass, style);
  }

  var notResolvedProperties = properties.filter(function(prop) {
    return !(prop.nameCamelCase in style);
  });
  if (notResolvedProperties.length === 0) {
    return style;
  }

  // resolve missing properties
  var elem = element;
  if (!elem) {
    elem = window.document.createElement('div');
    elem.style.display = 'none';
    window.document.body.appendChild(elem);
    element = elem;
  }
  elem.className = cssClass;
  if (additionalClass) {
    elem.className += ' ' + additionalClass;
  }
  var computedStyle = window.getComputedStyle(elem);
  notResolvedProperties.forEach(function(property) {
    style[property.nameCamelCase] = computedStyle[property.name];
  });
  elem.className = '';

  return style;
}

export function getSize(cssClass, cssProperty, property, defaultSize, additionalClass) {
  var size = get(cssClass, cssProperty, additionalClass)[property];
  if ('auto' === size) {
    return defaultSize;
  } else {
    return $.pxToNumber(size);
  }
}

export function put(cssClass, style) {
  styleMap[cssClass] = style;
}

export function clearCache() {
  styleMap = {};
}

const RGB_BLACK = {
  red: 0,
  green: 0,
  blue: 0
}

const RGB_WHITE = {
  red: 255,
  green: 255,
  blue: 255
}

/**
 * Creates an rgb object based on the given rgb string with the format rgb(0, 0, 0).
 * If the input string cannot be parsed, undefined is returned.
 */
export function rgb(rgbString) {
  if (!rgbString) {
    return undefined;
  }
  var rgbVal = rgbString.replace(/\s/g, '').match(/^rgba?\((\d+),(\d+),(\d+),?(\d+(\.\d+)?)?/i);
  if (rgbVal === null) {
    return undefined;
  }
  return {
    red: parseInt(rgbVal[1], 10),
    green: parseInt(rgbVal[2], 10),
    blue: parseInt(rgbVal[3], 10),
    alpha: parseFloat(scout.nvl(rgbVal[4], 1))
  };
}

/**
 * Make a given color darker by mixing it with a certain amount of black.
 * If no color is specified or the color cannot be parsed, undefined is returned.
 *
 * @param color
 *          a CSS color in 'rgb()' or 'rgba()' format.
 * @param ratio
 *          a number between 0 and 1 specifying how much black should be added
 *          to the given color (0.0 = only 'color', 1.0 = only black).
 *          Default is 0.2.
 */
export function darkerColor(color, ratio) {
  var rgbVal = rgb(color);
  if (!rgbVal) {
    return undefined;
  }
  ratio = scout.nvl(ratio, 0.2);
  return mergeRgbColors(RGB_BLACK, ratio, rgbVal, 1 - ratio);
}

/**
 * Make a given color lighter by mixing it with a certain amount of white.
 * If no color is specified or the color cannot be parsed, undefined is returned.
 *
 * @param color
 *          a CSS color in 'rgb()' or 'rgba()' format.
 * @param ratio
 *          a number between 0 and 1 specifying how much white should be added
 *          to the given color (0.0 = only 'color', 1.0 = only white).
 *          Default is 0.2.
 */
export function lighterColor(color, ratio) {
  var rgbVal = rgb(color);
  if (!rgbVal) {
    return undefined;
  }
  ratio = scout.nvl(ratio, 0.2);
  return mergeRgbColors(RGB_WHITE, ratio, rgbVal, 1 - ratio);
}

/**
 * Merges two RGB colors as defined by rgb().
 *
 * The two 'ratio' arguments specify "how much" of the corresponding color is added to the
 * resulting color. Both arguments should (but don't have to) add to 1.0.
 *
 * All arguments are mandatory.
 */
export function mergeRgbColors(color1, ratio1, color2, ratio2) {
  if (typeof color1 === 'string') {
    color1 = rgb(color1);
  }
  if (typeof color2 === 'string') {
    color2 = rgb(color2);
  }
  if (!color1 && !color2) {
    return undefined;
  }
  ratio1 = scout.nvl(ratio1, 0);
  ratio2 = scout.nvl(ratio2, 0);
  if (!color1) {
    color1 = RGB_BLACK;
    ratio1 = 0;
  }
  if (!color2) {
    color2 = RGB_BLACK;
    ratio2 = 0;
  }
  if (ratio1 === 0 && ratio2 === 0) {
    return 'rgb(0,0,0)';
  }
  return 'rgb(' +
    numbers.round((ratio1 * color1.red + ratio2 * color2.red) / (ratio1 + ratio2)) + ',' +
    numbers.round((ratio1 * color1.green + ratio2 * color2.green) / (ratio1 + ratio2)) + ',' +
    numbers.round((ratio1 * color1.blue + ratio2 * color2.blue) / (ratio1 + ratio2)) +
    ')';
}

/**
 * Example: Dialog-PLAIN-12
 */
export function parseFontSpec(pattern) {
  var fontSpec = {};
  if (strings.hasText(pattern)) {
    var tokens = pattern.split(/[-_,\/.;]/);
    for (var i = 0; i < tokens.length; i++) {
      var token = tokens[i].toUpperCase();
      // styles
      if (token === 'NULL' || token === '0') {
        // nop (undefined values)
      } else if (token === 'PLAIN') {
        // nop
      } else if (token === 'BOLD') {
        fontSpec.bold = true;
      } else if (token === 'ITALIC') {
        fontSpec.italic = true;
      } else {
        // size or name
        if (/^\d+$/.test(token)) {
          fontSpec.size = token;
        } else if (token !== 'NULL') {
          fontSpec.name = tokens[i];
        }
      }
    }
  }
  return fontSpec;
}

export function modelToCssColor(color) {
  if (!color) { // prevent conversion from null to 'null' by regex
    return '';
  }
  var cssColor = '';
  if (/^[A-Fa-f0-9]{3}([A-Fa-f0-9]{3})?$/.test(color)) { // hex color
    cssColor = '#' + color;
  } else if (/^[A-Za-z0-9().,%-]+$/.test(color)) { // named colors or color functions
    cssColor = color;
  }
  return cssColor;
}

/**
 * Returns a string with CSS definitions for use in an element's "style" attribute. All CSS relevant
 * properties of the given object are converted to CSS definitions, namely foreground color, background
 * color and font.
 *
 * If an $element is provided, the CSS definitions are directly applied to the element. This can be
 * useful if the "style" attribute is shared and cannot be replaced in it's entirety.
 *
 * If propertyPrefix is provided, the prefix will be applied to the properties, e.g. if the prefix is
 * 'label' the properties labelFont, labelBackgroundColor and labelForegroundColor are used instead of
 * just font, backgroundColor and foregroundColor.
 */
export function legacyStyle(obj, $element, propertyPrefix) {
  var style = '';
  style += legacyForegroundColor(obj, $element, propertyPrefix);
  style += legacyBackgroundColor(obj, $element, propertyPrefix);
  style += legacyFont(obj, $element, propertyPrefix);
  return style;
}

export function legacyForegroundColor(obj, $element, propertyPrefix) {
  propertyPrefix = propertyPrefix || '';

  var cssColor = '';
  if (obj) {
    var foregroundColorProperty = strings.lowercaseFirstLetter(propertyPrefix + 'ForegroundColor');
    cssColor = modelToCssColor(obj[foregroundColorProperty]);
  }
  if ($element) {
    $element.css('color', cssColor);
  }
  var style = '';
  if (cssColor) {
    style += 'color: ' + cssColor + '; ';
  }
  return style;
}

export function legacyBackgroundColor(obj, $element, propertyPrefix) {
  propertyPrefix = propertyPrefix || '';

  var cssBackgroundColor = '';
  if (obj) {
    var backgroundColorProperty = strings.lowercaseFirstLetter(propertyPrefix + 'BackgroundColor');
    cssBackgroundColor = modelToCssColor(obj[backgroundColorProperty]);
  }
  if ($element) {
    $element.css('background-color', cssBackgroundColor);
  }
  var style = '';
  if (cssBackgroundColor) {
    style += 'background-color: ' + cssBackgroundColor + '; ';
  }
  return style;
}

export function legacyFont(obj, $element, propertyPrefix) {
  propertyPrefix = propertyPrefix || '';

  var cssFontWeight = '';
  var cssFontStyle = '';
  var cssFontSize = '';
  var cssFontFamily = '';
  if (obj) {
    var fontProperty = strings.lowercaseFirstLetter(propertyPrefix + 'Font');
    var fontSpec = parseFontSpec(obj[fontProperty]);
    if (fontSpec.bold) {
      cssFontWeight = 'bold';
    }
    if (fontSpec.italic) {
      cssFontStyle = 'italic';
    }
    if (fontSpec.size) {
      cssFontSize = fontSpec.size + 'pt';
    }
    if (fontSpec.name) {
      cssFontFamily = fontSpec.name;
    }
  }
  if ($element) {
    $element
      .css('font-weight', cssFontWeight)
      .css('font-style', cssFontStyle)
      .css('font-size', cssFontSize)
      .css('font-family', cssFontFamily);
  }
  var style = '';
  if (cssFontWeight) {
    style += 'font-weight: ' + cssFontWeight + '; ';
  }
  if (cssFontStyle) {
    style += 'font-style: ' + cssFontStyle + '; ';
  }
  if (cssFontSize) {
    style += 'font-size: ' + cssFontSize + '; ';
  }
  if (cssFontFamily) {
    style += 'font-family: ' + cssFontFamily + '; ';
  }
  return style;
}


export default {
  RGB_BLACK,
  RGB_WHITE,
  clearCache,
  darkerColor,
  get,
  getSize,
  legacyBackgroundColor,
  legacyFont,
  legacyForegroundColor,
  legacyStyle,
  lighterColor,
  mergeRgbColors,
  modelToCssColor,
  parseFontSpec,
  put,
  rgb,
  styleMap
};
