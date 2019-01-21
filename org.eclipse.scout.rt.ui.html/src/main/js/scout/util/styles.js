/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.styles = {

  /**
   * @memberOf scout.styles
   */
  styleMap: {},

  /**
   * Generates an invisible div and appends it to the body, only once. The same div will be reused on subsequent calls.
   * Adds the given css class to that element and returns a style object containing the values for every given property.
   * The style is cached. Subsequent calls with the same css class will return the same style object.
   */
  get: function(cssClass, properties) {
    var style = scout.styles.styleMap[cssClass];
    // ensure array
    properties = scout.arrays.ensure(properties);
    properties = properties.map(function(prop) {
      return {
        name: prop,
        // replace property names like 'max-width' in 'maxWidth'
        nameCamelCase: prop.replace(/\-(.)/g,
          function(match, p1) {
            return p1.toUpperCase();
          })
      };
    });

    // ensure style
    if (!style) {
      style = {};
      scout.styles.put(cssClass, style);
    }

    var notResolvedProperties = properties.filter(function(prop) {
      return !(prop.nameCamelCase in style);
    });
    if (notResolvedProperties.length === 0) {
      return style;
    }

    // resolve missing properties
    var elem = scout.styles.element;
    if (!elem) {
      elem = window.document.createElement('div');
      elem.style.display = 'none';
      window.document.body.appendChild(elem);
      scout.styles.element = elem;
    }
    elem.className = cssClass;
    var computedStyle = window.getComputedStyle(elem);
    notResolvedProperties.forEach(function(property) {
      style[property.nameCamelCase] = computedStyle[property.name];
    });
    elem.className = '';

    return style;
  },

  getSize: function(cssClass, cssProperty, property, defaultSize) {
    var size = scout.styles.get(cssClass, cssProperty)[property];
    if ('auto' === size) {
      return defaultSize;
    } else {
      return $.pxToNumber(size);
    }
  },

  put: function(cssClass, style) {
    this.styleMap[cssClass] = style;
  },

  RGB_BLACK: {
    red: 0,
    green: 0,
    blue: 0
  },

  RGB_WHITE: {
    red: 255,
    green: 255,
    blue: 255
  },

  /**
   * Creates an rgb object based on the given rgb string with the format rgb(0, 0, 0).
   * If the input string cannot be parsed, undefined is returned.
   */
  rgb: function(rgbString) {
    if (!rgbString) {
      return undefined;
    }
    var rgb = rgbString.replace(/\s/g, '').match(/^rgba?\((\d+),(\d+),(\d+),?(\d+(\.\d+)?)?/i);
    if (rgb === null) {
      return undefined;
    }
    return {
      red: parseInt(rgb[1], 10),
      green: parseInt(rgb[2], 10),
      blue: parseInt(rgb[3], 10),
      alpha: parseFloat(scout.nvl(rgb[4], 1))
    };
  },

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
  darkerColor: function(color, ratio) {
    var rgb = this.rgb(color);
    if (!rgb) {
      return undefined;
    }
    ratio = scout.nvl(ratio, 0.2);
    return this.mergeRgbColors(this.RGB_BLACK, ratio, rgb, 1 - ratio);
  },

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
  lighterColor: function(color, ratio) {
    var rgb = this.rgb(color);
    if (!rgb) {
      return undefined;
    }
    ratio = scout.nvl(ratio, 0.2);
    return this.mergeRgbColors(this.RGB_WHITE, ratio, rgb, 1 - ratio);
  },

  /**
   * Merges two RGB colors as defined by rgb().
   *
   * The two 'ratio' arguments specify "how much" of the corresponding color is added to the
   * resulting color. Both arguments should (but don't have to) add to 1.0.
   *
   * All arguments are mandatory.
   */
  mergeRgbColors: function(color1, ratio1, color2, ratio2) {
    if (typeof color1 === 'string') {
      color1 = this.rgb(color1);
    }
    if (typeof color2 === 'string') {
      color2 = this.rgb(color2);
    }
    if (!color1 && !color2) {
      return undefined;
    }
    ratio1 = scout.nvl(ratio1, 0);
    ratio2 = scout.nvl(ratio2, 0);
    if (!color1) {
      color1 = this.RGB_BLACK;
      ratio1 = 0;
    }
    if (!color2) {
      color2 = this.RGB_BLACK;
      ratio2 = 0;
    }
    if (ratio1 === 0 && ratio2 === 0) {
      return 'rgb(0,0,0)';
    }
    return 'rgb(' +
      scout.numbers.round((ratio1 * color1.red + ratio2 * color2.red) / (ratio1 + ratio2)) + ',' +
      scout.numbers.round((ratio1 * color1.green + ratio2 * color2.green) / (ratio1 + ratio2)) + ',' +
      scout.numbers.round((ratio1 * color1.blue + ratio2 * color2.blue) / (ratio1 + ratio2)) +
      ')';
  },

  /**
   * Example: Dialog-PLAIN-12
   */
  parseFontSpec: function(pattern) {
    var fontSpec = {};
    if (scout.strings.hasText(pattern)) {
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
  },

  modelToCssColor: function(color) {
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
  },

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
  legacyStyle: function(obj, $element, propertyPrefix) {
    var style = '';
    style += this.legacyForegroundColor(obj, $element, propertyPrefix);
    style += this.legacyBackgroundColor(obj, $element, propertyPrefix);
    style += this.legacyFont(obj, $element, propertyPrefix);
    return style;
  },


  legacyForegroundColor: function(obj, $element, propertyPrefix) {
    propertyPrefix = propertyPrefix || '';

    var cssColor = '';
    if (obj) {
      var foregroundColorProperty = scout.strings.lowercaseFirstLetter(propertyPrefix + 'ForegroundColor');
      cssColor = this.modelToCssColor(obj[foregroundColorProperty]);
    }
    if ($element) {
      $element.css('color', cssColor);
    }
    var style = '';
    if (cssColor) {
      style += 'color: ' + cssColor + '; ';
    }
    return style;
  },

  legacyBackgroundColor: function(obj, $element, propertyPrefix) {
    propertyPrefix = propertyPrefix || '';

    var cssBackgroundColor = '';
    if (obj) {
      var backgroundColorProperty = scout.strings.lowercaseFirstLetter(propertyPrefix + 'BackgroundColor');
      cssBackgroundColor = this.modelToCssColor(obj[backgroundColorProperty]);
    }
    if ($element) {
      $element.css('background-color', cssBackgroundColor);
    }
    var style = '';
    if (cssBackgroundColor) {
      style += 'background-color: ' + cssBackgroundColor + '; ';
    }
    return style;
  },

  legacyFont: function(obj, $element, propertyPrefix) {
    propertyPrefix = propertyPrefix || '';

    var cssFontWeight = '';
    var cssFontStyle = '';
    var cssFontSize = '';
    var cssFontFamily = '';
    if (obj) {
      var fontProperty = scout.strings.lowercaseFirstLetter(propertyPrefix + 'Font');
      var fontSpec = this.parseFontSpec(obj[fontProperty]);
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

};
