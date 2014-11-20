/**
 * Provides information about the device and its supported features.<p>
 * The informations are detected lazily.
 */
scout.Device = function(userAgent) {
  this.userAgent = userAgent;
  this.system;
  this.features = {};
  this.device;
  this._userAgentParsed = false;
};

scout.Device.vendorPrefixes = ['Webkit', 'Moz', 'O', 'ms', 'Khtml'];

scout.Device.prototype.isIos = function() {
  return this.getSystem() === scout.Device.SYSTEM_IOS;
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

scout.Device.prototype.supportsCssProperty = function(property) {
  if (this.features[property] === undefined) {
    this.features[property] = check(property);
  }
  return this.features[property];

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

scout.Device.prototype.getSystem = function() {
  if (this.userAgent && !this._userAgentParsed) {
    this.parseUserAgent(this.userAgent);
  }
  return this.system;
};

scout.Device.prototype.parseUserAgent = function(userAgent) {
  if (!userAgent) {
    return;
  }
  var iosDevices = ['iPad', 'iPhone'];
  for (var i = 0; i < iosDevices.length; i++) {
    var device = iosDevices[i];
    if (userAgent.indexOf(device) !== -1) {
      this.device = device;
      this.system = scout.Device.SYSTEM_IOS;
    }
  }

  this._userAgentParsed = true;
};

scout.Device.SYSTEM_IOS = 'IOS';

//singleton
scout.device = new scout.Device(navigator.userAgent);
