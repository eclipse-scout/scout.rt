/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, numbers, scout, strings} from '../index';
import $ from 'jquery';

let styleMap = {};

let element = null;

/**
 * @typedef StyleMap
 * @property {string} [minHeight]
 * @property {string} [minWidth]
 * @property {string} [fill]
 * @property {string} [stroke]
 * @property {string} [fontFamily]
 * @property {string} [backgroundColor]
 * @property {string} [borderColor]
 */

/**
 * Generates an invisible div and appends it to the body, only once. The same div will be reused on subsequent calls.
 * Adds the given css class to that element and returns a style object containing the values for every given property.
 * The style is cached. Subsequent calls with the same css class will return the same style object.
 *
 * @param {object} styleProperties in the form {backgroundColor: 'black'}
 * @return {StyleMap} style
 */
export function get(cssClass, properties, styleProperties) {
  // create invisible div
  let elem = element;
  if (!elem) {
    elem = window.document.createElement('div');
    elem.style.display = 'none';
    window.document.body.appendChild(elem);
    element = elem;
  }

  let displayNoneStyleCssText = elem.style.cssText;
  styleProperties = $.extend(true, {}, styleProperties, {
    display: ''
  });
  Object.keys(styleProperties).sort().forEach(key => {
    elem.style[key] = styleProperties[key];
  });
  // get cssText as additional key component, display is not part of the key component
  let keyCssText = elem.style.cssText;
  // always add display: 'none'
  elem.style.display = 'none';
  let styleCssText = elem.style.cssText;

  // reset style
  elem.style.cssText = displayNoneStyleCssText;

  let cssClassArray = arrays.ensure(cssClass),
    mapKey = keyCssText ? [...cssClassArray, keyCssText] : cssClassArray;

  let style = styleMap[mapKey];
  // ensure array
  properties = arrays.ensure(properties);
  properties = properties.map(prop => {
    return {
      name: prop,
      // replace property names like 'max-width' in 'maxWidth'
      nameCamelCase: prop.replace(/-(.)/g,
        (match, p1) => {
          return p1.toUpperCase();
        })
    };
  });

  // ensure style
  if (!style) {
    style = {};
    put(mapKey, style);
  }

  let notResolvedProperties = properties.filter(prop => {
    return !(prop.nameCamelCase in style);
  });
  if (notResolvedProperties.length === 0) {
    return style;
  }

  // resolve missing properties
  elem.className = cssClassArray[0];
  for (let i = 1; i < cssClassArray.length; i++) {
    let childElem = elem.children[0];
    if (!childElem) {
      childElem = window.document.createElement('div');
      childElem.style.display = 'none';
      elem.appendChild(childElem);
    }
    elem = childElem;
    elem.className = cssClassArray[i];
  }

  // set style properties
  elem.style.cssText = styleCssText;

  let computedStyle = window.getComputedStyle(elem);
  notResolvedProperties.forEach(property => {
    style[property.nameCamelCase] = computedStyle[property.name];
  });

  elem.style.cssText = displayNoneStyleCssText;
  elem = element;

  do {
    elem.className = '';
    elem = elem.children[0];
  }
  while (elem);

  return style;
}

/**
 * Traverses the parents of the given $elem and returns the first opaque background color.
 * @param {jQuery} $elem
 */
export function getFirstOpaqueBackgroundColor($elem) {
  if (!$elem) {
    return;
  }

  let document = $elem.document(true);
  while ($elem && $elem.length && document !== $elem[0]) {
    let rgbString = $elem.css('background-color'),
      rgba = rgb(rgbString);
    if (rgba && rgba.alpha === 1) {
      return rgbString;
    }
    $elem = $elem.parent();
  }
}

export function getSize(cssClass, cssProperty, property, defaultSize) {
  let size = get(cssClass, cssProperty)[property];
  if ('auto' === size) {
    return defaultSize;
  }
  return $.pxToNumber(size);
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
};

const RGB_WHITE = {
  red: 255,
  green: 255,
  blue: 255
};

/**
 * Creates an rgb object based on the given rgb string with the format rgb(0, 0, 0).
 * If the input string cannot be parsed, undefined is returned.
 */
export function rgb(rgbString) {
  if (!rgbString) {
    return undefined;
  }
  let rgbVal = rgbString.replace(/\s/g, '').match(/^rgba?\((\d+),(\d+),(\d+),?(\d+(\.\d+)?)?/i);
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
 * Converts the given hex string to a rgb string.
 */
export function hexToRgb(hexString) {
  if (!hexString) {
    return;
  }

  let r = 0,
    g = 0,
    b = 0,
    a = 255;

  if (hexString.length === 4 || hexString.length === 5) {
    r = '0x' + hexString[1] + hexString[1];
    g = '0x' + hexString[2] + hexString[2];
    b = '0x' + hexString[3] + hexString[3];
    if (hexString.length === 5) {
      a = '0x' + hexString[4] + hexString[4];
    }
  }

  if (hexString.length === 7 || hexString.length === 9) {
    r = '0x' + hexString[1] + hexString[2];
    g = '0x' + hexString[3] + hexString[4];
    b = '0x' + hexString[5] + hexString[6];
    if (hexString.length === 9) {
      a = '0x' + hexString[7] + hexString[8];
    }
  }

  a = +(a / 255).toFixed(3);

  return 'rgba(' + +r + ',' + +g + ',' + +b + ',' + a + ')';
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
  let rgbVal = rgb(color);
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
  let rgbVal = rgb(color);
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
  let fontSpec = {};
  if (strings.hasText(pattern)) {
    let tokens = pattern.split(/[-_,/.;]/);
    for (let i = 0; i < tokens.length; i++) {
      let token = tokens[i].toUpperCase();
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
  let cssColor = '';
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
  let style = '';
  style += legacyForegroundColor(obj, $element, propertyPrefix);
  style += legacyBackgroundColor(obj, $element, propertyPrefix);
  style += legacyFont(obj, $element, propertyPrefix);
  return style;
}

export function legacyForegroundColor(obj, $element, propertyPrefix) {
  propertyPrefix = propertyPrefix || '';

  let cssColor = '';
  if (obj) {
    let foregroundColorProperty = strings.toLowerCaseFirstLetter(propertyPrefix + 'ForegroundColor');
    cssColor = modelToCssColor(obj[foregroundColorProperty]);
  }
  if ($element) {
    $element.css('color', cssColor);
  }
  let style = '';
  if (cssColor) {
    style += 'color: ' + cssColor + '; ';
  }
  return style;
}

export function legacyBackgroundColor(obj, $element, propertyPrefix) {
  propertyPrefix = propertyPrefix || '';

  let cssBackgroundColor = '';
  if (obj) {
    let backgroundColorProperty = strings.toLowerCaseFirstLetter(propertyPrefix + 'BackgroundColor');
    cssBackgroundColor = modelToCssColor(obj[backgroundColorProperty]);
  }
  if ($element) {
    $element.css('background-color', cssBackgroundColor);
  }
  let style = '';
  if (cssBackgroundColor) {
    style += 'background-color: ' + cssBackgroundColor + '; ';
  }
  return style;
}

export function legacyFont(obj, $element, propertyPrefix) {
  propertyPrefix = propertyPrefix || '';

  let cssFontWeight = '';
  let cssFontStyle = '';
  let cssFontSize = '';
  let cssFontFamily = '';
  if (obj) {
    let fontProperty = strings.toLowerCaseFirstLetter(propertyPrefix + 'Font');
    let fontSpec = parseFontSpec(obj[fontProperty]);
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
  let style = '';
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

export function _getElement() {
  return element;
}

export default {
  RGB_BLACK,
  RGB_WHITE,
  clearCache,
  darkerColor,
  get,
  getFirstOpaqueBackgroundColor,
  getSize,
  hexToRgb,
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
  styleMap,
  _getElement
};
