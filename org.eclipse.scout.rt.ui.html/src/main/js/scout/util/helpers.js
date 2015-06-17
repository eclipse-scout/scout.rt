scout.helpers = {

  /**
   * Opens a popup window or new browser tab for the given URL and returns the
   * window reference.
   * <p>
   * If popupWidth and/or popupHeight is missing, the url is opened in a new
   * browser tab. Otherwise, the new popup window is opened in the desired size
   * and centered above the parent window.
   * <p>
   * The argument 'windowId' is a name for the new window. If a window with this
   * name already exists, the url is (re-)loaded in that window, otherwise a new
   * window is created. If popup sizes are missing (i.e. url should be opened in
   * new tab), the windowId is automatically prefixed with '_' (mostly needed for
   * IE). All characters except [A-Za-z_] are converted to underscores.
   * <p>
   * Please not that you can open new windows only inside a handler of a direct
   * user action. Asynchronous calls to this method are blocked by the browsers.
   *
   * @memberOf scout.helpers
   */
  // FIXME AWE/BSH: unsued after detach-window feature has been removed, check at end of release
  // if it's still unused and delete it in that case-
  openWindow: function(url, windowId, popupWidth, popupHeight) {
    var windowSpec = '';
    if (popupWidth && popupHeight) {
      var parentLeft = (window.screenX !== undefined) ? window.screenX : window.screenLeft;
      var parentTop = (window.screenY !== undefined) ? window.screenY : window.screenTop;
      var parentWidth = (window.outerWidth !== undefined) ? window.outerWidth : screen.width;
      var parentHeight = (window.outerHeight !== undefined) ? window.outerHeight : screen.height;
      var popupLeft = ((parentWidth / 2) - (popupWidth / 2)) + parentLeft;
      var popupTop = ((parentHeight / 2) - (popupHeight / 2)) + parentTop;
      windowSpec = [
        // Should be the default, just to be sure
        'scrollbars=yes',
        // Explicitly enable location bar for IE (Firefox and Chrome always show the location bar)
        'location=yes',
        // Explicitly enable window resizing for IE (Firefox and Chrome always enable resize)
        'resizable=yes',
        // Top left position (is only accurate in Firefox, see below)
        'left=' + popupLeft, 'top=' + popupTop,
        // Target outerWidth/outerHeight is only respected by Firefox
        'outerWidth=' + popupWidth, 'outerHeight=' + popupHeight,
        // Fallback for IE and Chrome (window is not really centered, but it's the best we can do)
        'width=' + popupWidth, 'height=' + popupHeight
      ].join(',');
    } else {
      windowId = '_' + windowId;
    }

    // In case 'window.location' was passed, extract the URL as string
    if (typeof url === 'object' && 'href' in url) {
      url = url.href;
    }
    // In some versions of IE, only [A-Za-z_] are allowed as window IDs
    // (http://stackoverflow.com/a/2189596)
    windowId = (windowId || '').replace(/[^A-Za-z_]/g, '_');

    var w = window.open(url, windowId, windowSpec);
    return w;
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
        if (token === 'PLAIN') {
          // nop
        } else if (token === 'BOLD') {
          fontSpec.bold = true;
        } else if (token === 'ITALIC') {
          fontSpec.italic = true;
        } else {
          // size or name
          if (/^\d+$/.test(token) && token !== '0') {
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
    }
    else if (/^[A-Za-z0-9().,%-]+$/.test(color)) { // named colors or color functions
      cssColor = color;
    }
    return cssColor;
  },

  /**
   * Returns a string with CSS definitions for use in an element's "style" attribute. All CSS relevant
   * properties of the given cell are converted to CSS definitions, namely foreground color, background
   * color and font.
   *
   * If an $element is provided, the CSS definitions are directly applied to the element. This can be
   * useful if the "style" attribute is shared and cannot be replaced in it's entirety.
   */
  legacyCellStyle: function(cell, $element) {
    var cssColor = '',
      cssBackgroundColor = '',
      cssFontWeight = '',
      cssFontStyle = '',
      cssFontSize = '',
      cssFontFamily = '';

    if (typeof cell === 'object' && cell !== null) {
      cssColor = scout.helpers.modelToCssColor(cell.foregroundColor);
      cssBackgroundColor = scout.helpers.modelToCssColor(cell.backgroundColor);

      if (cell.font) {
        var fontSpec = this.parseFontSpec(cell.font);
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
      style += 'font-weight: '+ cssFontWeight +'; ';
    }
    if (cssFontStyle) {
      style += 'font-style: '+ cssFontStyle +'; ';
    }
    if (cssFontSize) {
      style += 'font-size: '+ cssFontSize +'; ';
    }
    if (cssFontFamily) {
      style += 'font-family: '+ cssFontFamily +'; ';
    }

    return style;
  },

  // TODO IMO Is this still needed???
  dynamicResourceUrl: function(adapter, url) {
    return url;
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
      }
      else {
        argsToCheck = Array.prototype.slice.call(arguments, 1);
      }
      for (var i = 0; i < argsToCheck.length; i++) {
        if (value === argsToCheck[i]) {
          return true;
        }
      }
    }
    return false;
  }

};
