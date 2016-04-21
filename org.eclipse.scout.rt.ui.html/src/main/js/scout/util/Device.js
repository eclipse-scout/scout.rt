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

/* global FastClick */

/**
 * Provides information about the device and its supported features.<p>
 * The informations are detected lazily.
 *
 * @singleton
 */
scout.Device = function(userAgent) {
  this.userAgent = userAgent;
  this.features = {};
  this.system = scout.Device.System.UNKNOWN;
  this.type = scout.Device.Type.DESKTOP;
  this.browser = scout.Device.Browser.UNKNOWN;
  this.browserVersion = 0;
  this.scrollbarWidth;

  // --- device specific configuration
  // initialize with empty string so that it can be used without calling initUnselectableAttribute()
  // this property is used with regular JQuery attr(key, value) Syntax and in cases where we create
  // DOM elements by creating a string.
  this.unselectableAttribute = scout.Device.DEFAULT_UNSELECTABLE_ATTRIBUTE;
  this.tableAdditionalDivRequired = false;
  this.focusManagerActive = true;

  if (userAgent) {
    this._parseSystem(userAgent);
    this._parseBrowser(userAgent);
    this._parseBrowserVersion(userAgent);
  }
};

scout.Device.DEFAULT_UNSELECTABLE_ATTRIBUTE = {
  key: null,
  value: null,
  string: ''
};

scout.Device.vendorPrefixes = ['Webkit', 'Moz', 'O', 'ms', 'Khtml'];

scout.Device.Browser = {
  UNKNOWN: 'Unknown',
  FIREFOX: 'Firefox',
  CHROME: 'Chrome',
  INTERNET_EXPLORER: 'InternetExplorer',
  EDGE: 'Edge',
  SAFARI: 'Safari'
};

scout.Device.System = {
  UNKNOWN: 'Unknown',
  IOS: 'IOS',
  ANDROID: 'ANDROID'
};

scout.Device.Type = {
  DESKTOP: 'DESKTOP',
  TABLET: 'TABLET',
  MOBILE: 'MOBILE'
};

/**
 * Called during bootstrap by index.html before the session startup.<p>
 * Precalculates the value of some attributes to store them
 * in a static way (and prevent many repeating function calls within loops).<p>
 * Also loads device specific scripts (fast click for ios devices)
 */
scout.Device.prototype.bootstrap = function() {
  var deferreds = [];

  // Precalculate value and store in a simple property, to prevent many function calls inside loops (e.g. when generating table rows)
  this.unselectableAttribute = this.getUnselectableAttribute();
  this.tableAdditionalDivRequired = this.isTableAdditionalDivRequired();
  this.scrollbarWidth = this._detectScrollbarWidth();
  this.type = this._detectType(this.userAgent);

  if (this.isIos()) {
    // We use Fastclick to prevent the 300ms delay when touching an element.
    // With Chrome 32 the issue is solved, so no need to load the script for other devices than iOS
    deferreds.push(this._loadFastClickDeferred());
  }

  if (this.hasOnScreenKeyboard()) {
    // Auto focusing of elements is bad with on screen keyboards -> deactivate to prevent unwanted popping up of the keyboard
    this.focusManagerActive = false;
    deferreds.push(this._loadJQueryMobileDeferred());
  }
  return deferreds;
};

scout.Device.prototype._loadFastClickDeferred = function() {
  return this._loadScriptDeferred('res/fastclick-1.0.6.min.js', function() {
    FastClick.attach(document.body);
    $.log.info('FastClick script loaded and attached');
  });
};

scout.Device.prototype._loadJQueryMobileDeferred = function() {
  return this._loadScriptDeferred('res/jquery.mobile.custom-1.4.5.min.js', function() {
    $.log.info('JQuery Mobile script loaded');
  });
};

scout.Device.prototype._loadScriptDeferred = function(scriptUrl, doneFunc) {
  return $
    .getCachedScript(scriptUrl)
    .done(doneFunc);
};

scout.Device.prototype.hasOnScreenKeyboard = function() {
  return this.supportsFeature('_onScreenKeyboard', function() {
    return this.isIos() || this.isAndroid() || this.isWindowsTablet();
  }.bind(this));
};

/**
 * Safari shows a tooltip if ellipsis are displayed due to text truncation. This is fine but, unfortunately, it cannot be prevented.
 * Because showing two tooltips at the same time (native and custom) is bad, the custom tooltip cannot be displayed.
 * @returns {Boolean}
 */
scout.Device.prototype.isCustomEllipsisTooltipPossible = function() {
  return this.browser !== scout.Device.Browser.SAFARI;
};

scout.Device.prototype.isIos = function() {
  return scout.Device.System.IOS === this.system;
};

scout.Device.prototype.isAndroid = function() {
  return scout.Device.System.ANDROID === this.system;
};

/**
 * The best way we have to detect a Microsoft Surface Tablet in table mode is to check if
 * the scrollbar width is 0 pixel. In desktop mode the scrollbar width is > 0 pixel.
 */
scout.Device.prototype.isWindowsTablet = function() {
  return scout.Device.Browser.EDGE === this.browser && this.scrollbarWidth === 0;
};

/**
 * This method returns false for very old browsers. Basically we check for the first version
 * that supports ECMAScript 5. This methods excludes all browsers that are known to be
 * unsupported, all others (e.g. unknown engines) are allowed by default.
 */
scout.Device.prototype.isSupportedBrowser = function(browser, version) {
  browser = scout.nvl(browser, this.browser);
  version = scout.nvl(version, this.browserVersion);
  var browsers = scout.Device.Browser;
  if ((browser === browsers.INTERNET_EXPLORER && version < 9) ||
    (browser === browsers.CHROME && version < 23) ||
    (browser === browsers.FIREFOX && version < 21) ||
    (browser === browsers.SAFARI && version < 7)) {
    return false;
  }
  return true;
};

scout.Device.prototype._parseSystem = function(userAgent) {
  if (userAgent.indexOf('iPhone') > -1 || userAgent.indexOf('iPad') > -1) {
    this.system = scout.Device.System.IOS;
  } else if (userAgent.indexOf('Android') > -1) {
    this.system = scout.Device.System.ANDROID;
  }
};

/**
 * Can not detect type until DOM is ready because we must create a DIV to measure the scrollbars.
 */
scout.Device.prototype._detectType = function(userAgent) {
  if (scout.Device.System.ANDROID === this.system) {
    if (userAgent.indexOf('Mobile') > -1) {
      return scout.Device.Type.MOBILE;
    } else {
      return scout.Device.Type.TABLET;
    }
  } else if (scout.Device.System.IOS === this.system) {
    if (userAgent.indexOf('iPad') > -1) {
      return scout.Device.Type.TABLET;
    } else {
      return scout.Device.Type.MOBILE;
    }
  } else if (this.isWindowsTablet()) {
    return scout.Device.Type.TABLET;
  }
  return scout.Device.Type.DESKTOP;
};

scout.Device.prototype._parseBrowser = function(userAgent) {
  if (userAgent.indexOf('Firefox') > -1) {
    this.browser = scout.Device.Browser.FIREFOX;
  } else if (userAgent.indexOf('MSIE') > -1 || userAgent.indexOf('Trident') > -1) {
    this.browser = scout.Device.Browser.INTERNET_EXPLORER;
  } else if (userAgent.indexOf('Edge') > -1) {
    // must check for Edge before we do other checks, because the Edge user-agent string
    // also contains matches for Chrome and Webkit.
    this.browser = scout.Device.Browser.EDGE;
  } else if (userAgent.indexOf('Chrome') > -1) {
    this.browser = scout.Device.Browser.CHROME;
  } else if (userAgent.indexOf('Safari') > -1) {
    this.browser = scout.Device.Browser.SAFARI;
  }
};

scout.Device.prototype.supportsFeature = function(property, checkFunc) {
  if (this.features[property] === undefined) {
    this.features[property] = checkFunc(property);
  }
  return this.features[property];
};

/**
 * Currently this method should be used when you want to check if the device is "touch only" -
 * which means the user has no keyboard or mouse. Some hybrids like Surface tablets in desktop mode are
 * still touch devices, but support keyboard and mouse at the same time. In such cases this method will
 * return false, since the device is not touch only.
 *
 * Currently this method returns the same as hasOnScreenKeyboard(). Maybe the implementation here will be
 * different in the future.
 */
scout.Device.prototype.supportsTouch = function() {
  return this.supportsFeature('_touch', this.hasOnScreenKeyboard.bind(this));
};

scout.Device.prototype.supportsFile = function() {
  return (window.File ? true : false);
};

scout.Device.prototype.supportsCssAnimation = function() {
  return this.supportsCssProperty('animation');
};

scout.Device.prototype.supportsCssGradient = function() {
  var testValue = 'linear-gradient(to left, #000 0%, #000 50%, transparent 50%, transparent 100% )';
  return this.supportsFeature('gradient', this.checkCssValue.bind(this, 'backgroundImage', testValue, function(actualValue) {
    return (actualValue + '').indexOf('gradient') > 0;
  }));
};

scout.Device.prototype.supportsCssUserSelect = function() {
  return this.supportsCssProperty('userSelect');
};

scout.Device.prototype.supportsInternationalization = function() {
  return window.Intl && typeof window.Intl === 'object';
};

/**
 * Returns true if the device supports the download of resources in the same window as the single page app is running.
 * With "download" we mean: change <code>window.location.href</code> to the URL of the resource to download. Some browsers don't
 * support this behavior and require the resource to be opened in a new window with <code>window.open</code>.
 */
scout.Device.prototype.supportsDownloadInSameWindow = function() {
  return scout.Device.Browser.FIREFOX !== this.browser;
};

scout.Device.prototype.hasPrettyScrollbars = function() {
  return this.supportsFeature('_prettyScrollbars', function check(property) {
    return this.scrollbarWidth === 0;
  }.bind(this));
};

scout.Device.prototype.supportsCopyFromDisabledInputFields = function() {
  return scout.Device.Browser.FIREFOX !== this.browser;
};

scout.Device.prototype.supportsFocusEmptyBeforeDiv = function() {
  return scout.Device.Browser.FIREFOX !== this.browser;
};

scout.Device.prototype.supportsCssProperty = function(property) {
  return this.supportsFeature(property, function check(property) {
    if (document.body.style[property] !== undefined) {
      return true;
    }

    property = property.charAt(0).toUpperCase() + property.slice(1);
    for (var i = 0; i < scout.Device.vendorPrefixes.length; i++) {
      if (document.body.style[scout.Device.vendorPrefixes[i] + property] !== undefined) {
        return true;
      }
    }
    return false;
  });
};

scout.Device.prototype.checkCssValue = function(property, value, checkFunc) {
  // Check if property is supported at all, otherwise div.style[property] would just add it and checkFunc would always return true
  if (document.body.style[property] === undefined) {
    return false;
  }
  var div = document.createElement('div');
  div.style[property] = value;
  if (checkFunc(div.style[property])) {
    return true;
  }

  property = property.charAt(0).toUpperCase() + property.slice(1);
  for (var i = 0; i < scout.Device.vendorPrefixes.length; i++) {
    var vendorProperty = scout.Device.vendorPrefixes[i] + property;
    if (document.body.style[vendorProperty] !== undefined) {
      div.style[vendorProperty] = value;
      if (checkFunc(div.style[vendorProperty])) {
        return true;
      }
    }
  }
  return false;
};

/**
 * Returns '' for modern browsers, that support the 'user-select' CSS property.
 * Returns ' unselectable="on"' for IE9.
 * This string can be used to add to any HTML element as attribute.
 */
scout.Device.prototype.getUnselectableAttribute = function() {
  return this.supportsFeature('_unselectableAttribute', function(property) {
    if (this.supportsCssUserSelect()) {
      return scout.Device.DEFAULT_UNSELECTABLE_ATTRIBUTE;
    }
    // required for IE 9
    return {
      key: 'unselectable',
      value: 'on',
      string: ' unselectable="on"'
    };
  }.bind(this));
};

/**
 * Returns false for modern browsers, that support CSS table-cell properties restricted
 * with a max-width and hidden overflow. Returns true if an additional div level is required.
 */
scout.Device.prototype.isTableAdditionalDivRequired = function() {
  return this.supportsFeature('_tableAdditionalDivRequired', function(property) {
    var $test = $('body')
      .appendDiv()
      .text('Scout')
      .css('visibility', 'hidden')
      .css('display', 'table-cell')
      .css('max-width', '1px')
      .css('overflow', 'hidden');
    var result = $test.width() > 1;
    $test.remove();
    return result;
  }.bind(this));
};

scout.Device.prototype.requiresIframeSecurityAttribute = function() {
  return this.supportsFeature('_requiresIframeSecurityAttribute', function(property) {
    var test = document.createElement('iframe');
    var supportsSandbox = ('sandbox' in test);

    if (supportsSandbox) {
      return false;
    } else {
      return ('security' in test);
    }
  }.bind(this));
};

/**
 * Currently the browserVersion is only set for IE. Because the only version-check we do,
 * is whether or not we use an old IE version. Version regex only matches the first number pair
 * but not the revision-version. Example:
 * - 21     match: 21
 * - 21.1   match: 21.1
 * - 21.1.3 match: 21.1
 */
scout.Device.prototype._parseBrowserVersion = function(userAgent) {
  var versionRegex, browsers = scout.Device.Browser;
  if (this.browser === browsers.INTERNET_EXPLORER) {
    // with internet explorer 11 user agent string does not contain the 'MSIE' string anymore
    // additionally in new version the version-number after Trident/ is not the browser-version
    // but the engine-version.
    if (userAgent.indexOf('MSIE') > -1) {
      versionRegex = /MSIE ([0-9]+\.?[0-9]*)/;
    } else {
      versionRegex = /rv:([0-9]+\.?[0-9]*)/;
    }
  } else if (this.browser === browsers.EDGE) {
    versionRegex = /Edge\/([0-9]+\.?[0-9]*)/;
  } else if (this.browser === browsers.SAFARI) {
    versionRegex = /Version\/([0-9]+\.?[0-9]*)/;
  } else if (this.browser === browsers.FIREFOX) {
    versionRegex = /Firefox\/([0-9]+\.?[0-9]*)/;
  } else if (this.browser === browsers.CHROME) {
    versionRegex = /Chrome\/([0-9]+\.?[0-9]*)/;
  }
  if (versionRegex) {
    var matches = versionRegex.exec(userAgent);
    if (Array.isArray(matches) && matches.length === 2) {
      this.browserVersion = parseFloat(matches[1]);
    }
  }
};

scout.Device.prototype._detectScrollbarWidth = function(userAgent) {
  var $measure = $('body')
    .appendDiv()
    .attr('id', 'MeasureScrollbar')
    .css('width', 50)
    .css('height', 50)
    .css('overflow-y', 'scroll'),
    measureElement = $measure[0];
  var scrollbarWidth = measureElement.offsetWidth - measureElement.clientWidth;
  $measure.remove();
  return scrollbarWidth;
};

scout.Device.prototype.toString = function() {
  return 'scout.Device[' +
    'system=' + this.system +
    ' browser=' + this.browser +
    ' browserVersion=' + this.browserVersion +
    ' type=' + this.type +
    ' scrollbarWidth=' + this.scrollbarWidth +
    ' features=' + JSON.stringify(this.features) + ']';
};

scout.device = new scout.Device(navigator.userAgent);
