/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.helpers = {

  prepareDOM: function() {
    // Cleanup DOM
    $('noscript').remove();

    // Prevent "Do you want to translate this page?" in Google Chrome
    if (scout.device.browser === scout.Device.SupportedBrowsers.CHROME) {
      var metaNoTranslate = '<meta name="google" content="notranslate" />';
      var $title = $('head > title');
      if ($title.length === 0) {
        // Add to end of head
        $('head').append(metaNoTranslate);
      } else {
        $title.after(metaNoTranslate);
      }
    }
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
            fontSpec.name = token;
          }
        }
      }
    }
    return fontSpec;
  },

  modelToCssColor: function(color) {
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
    var cssColor = '',
      cssBackgroundColor = '',
      cssFontWeight = '',
      cssFontStyle = '',
      cssFontSize = '',
      cssFontFamily = '';

    propertyPrefix = propertyPrefix || '';

    if (typeof obj === 'object' && obj !== null) {
      cssColor = scout.helpers.modelToCssColor(obj[scout.strings.lowercaseFirstLetter(propertyPrefix + 'ForegroundColor')]);
      cssBackgroundColor = scout.helpers.modelToCssColor(obj[scout.strings.lowercaseFirstLetter(propertyPrefix + 'BackgroundColor')]);

      if (obj.font) {
        var fontSpec = this.parseFontSpec(obj[scout.strings.lowercaseFirstLetter(propertyPrefix + 'Font')]);
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
    }

    // Apply CSS properties
    if ($element) {
      $element
        .css('color', cssColor)
        .css('background-color', cssBackgroundColor)
        .css('font-weight', cssFontWeight)
        .css('font-style', cssFontStyle)
        .css('font-size', cssFontSize)
        .css('font-family', cssFontFamily);
    }

    // Build style string
    var style = '';
    if (cssColor) {
      style += 'color: ' + cssColor + '; ';
    }
    if (cssBackgroundColor) {
      style += 'background-color: ' + cssBackgroundColor + '; ';
    }
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
  },

  /**
   * If 'value' is undefined or null, 'defaultValue' is returned. Otherwise, 'value' is returned.
   */
  nvl: function(value, defaultValue) {
    if (value === undefined || value === null) {
      return defaultValue;
    }
    return value;
  },

  isOneOf: function() {
    if (arguments && arguments.length >= 2) {
      var value = arguments[0];
      var argsToCheck;
      if (arguments.length === 2 && Array.isArray(arguments[1])) {
        argsToCheck = arguments[1];
      } else {
        argsToCheck = Array.prototype.slice.call(arguments, 1);
      }
      return argsToCheck.indexOf(value) !== -1;
    }
    return false;
  }

};
