/**
 * Provides information about the device and its supported features.<p>
 * The informations are detected lazily.
 */
scout.Device = function(userAgent) {
  this.userAgent = userAgent;
  this.system;
  this.features = {};
  this.device;
  this.browser = scout.Device.SupportedBrowsers.UNKNWON;
  this.unselectableAttribute = ''; // see initUnselectableAttribute()
  this.parseUserAgent(this.userAgent);
};

// FIXME AWE: user info from server-side BrowserInfo class

scout.Device.vendorPrefixes = ['Webkit', 'Moz', 'O', 'ms', 'Khtml'];

scout.Device.SupportedBrowsers = {
  UNKNOWN: 'Unknown',
  FIREFOX: 'Firefox',
  CHROME: 'Chrome',
  INTERNET_EXPLORER: 'InternetExplorer'
};

scout.Device.prototype.isIos = function() {
  return this.system === scout.Device.SYSTEM_IOS;
};

scout.Device.prototype.supportsTouch = function() {
  // Implement when needed, see https://hacks.mozilla.org/2013/04/detecting-touch-its-the-why-not-the-how/
};

scout.Device.prototype.supportsCssAnimation = function() {
  return this.supportsCssProperty('animation');
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
  return scout.Device.SupportedBrowsers.FIREFOX !== this.browser;
};

scout.Device.prototype.hasPrettyScrollbars = function() {
  return this.supportsFeature('prettyScrollbars', check.bind(this));

  function check(property) {
    // FIXME CGU check for android, osx, or just exclude windows?
    return scout.Device.SYSTEM_IOS === this.system;
  }
};

scout.Device.prototype.supportsCssProperty = function(property) {
  return this.supportsFeature(property, check);

  function check(property) {
    var i;
    if (document.body.style[property] !== undefined) {
      return true;
    }

    property = property.charAt(0).toUpperCase() + property.slice(1);
    for (i = 0; i < scout.Device.vendorPrefixes.length; i++) {
      if (document.body.style[scout.Device.vendorPrefixes[i] + property] !== undefined) {
        return true;
      }
    }

    return false;
  }
};

scout.Device.prototype.supportsFeature = function(property, checkFunc) {
  if (this.features[property] === undefined) {
    this.features[property] = checkFunc(property);
  }
  return this.features[property];
};

scout.Device.prototype.initUnselectableAttribute = function() {
  if (!this.supportsCssUserSelect()) {
    this.unselectableAttribute = ' unselectable="on"'; // workaround for IE 9
  }
};

scout.Device.prototype.parseUserAgent = function(userAgent) {
  if (!userAgent) {
    return;
  }
  // check for IOS devices
  var i, device, iosDevices = ['iPad', 'iPhone'];
  for (i = 0; i < iosDevices.length; i++) {
    device = iosDevices[i];
    if (contains(userAgent, device)) {
      this.device = device;
      this.system = scout.Device.SYSTEM_IOS;
      break;
    }
  }
  // check for browser
  if (contains(userAgent, 'Firefox')) {
    this.browser = scout.Device.SupportedBrowsers.FIREFOX;
  } else if (contains(userAgent, 'MSIE') || contains(userAgent, 'Trident')) {
    this.browser = scout.Device.SupportedBrowsers.INTERNET_EXPLORER;
  } else if (contains(userAgent, 'Chrome')) {
    this.browser = scout.Device.SupportedBrowsers.CHROME;
  }

  // we cannot use scout.strings at the time parseUserAgent is executed
  function contains(haystack, needle) {
    return haystack.indexOf(needle) !== -1;
  }
};

scout.Device.SYSTEM_IOS = 'IOS';

// singleton
scout.device = new scout.Device(navigator.userAgent);
