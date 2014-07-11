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
  this._propertySupportedMap =  {};
};

scout.Device.vendorPrefixes = ['Webkit', 'Moz', 'O', 'ms', 'Khtml'];

scout.Device.prototype.isIos = function() {
  return this.getSystem() === scout.Device.SYSTEM_IOS;
};

scout.Device.prototype.supportsTouch = function() {
  // Implement when needed, see https://hacks.mozilla.org/2013/04/detecting-touch-its-the-why-not-the-how/
};

/**
 * @see https://developer.mozilla.org/en-US/docs/Web/Guide/CSS/Using_CSS_animations/Detecting_CSS_animation_support
 */
scout.Device.prototype.supportsCssAnimation = function() {
  if (this.features.cssAnimation === undefined) {
    this.features.cssAnimation = check();
  }
  return this.features.cssAnimation;

  function check() {
    var i;
    var element = document.createElement('div');
    if (element.style.animation !== undefined) {
      return true;
    }

    for (i = 0; i < scout.Device.vendorPrefixes.length; i++) {
      if (element.style[scout.Device.vendorPrefixes[i] + 'Animation'] !== undefined) {
        return true;
      }
    }
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
    if (userAgent.indexOf(device) != -1) {
      this.device = device;
      this.system = scout.Device.SYSTEM_IOS;
    }
  }

  this._userAgentParsed = true;
};

scout.Device.prototype.supportsCssProperty = function(property) {
  var supported = this._propertySupportedMap[property];
  if (typeof supported !== 'undefined') {
    return supported; // cached
  }
  supported = (property in document.body.style);
  this._propertySupportedMap[property] = supported;
  return supported;
};

scout.Device.SYSTEM_IOS = 'IOS';

//singleton
scout.device = new scout.Device(navigator.userAgent);
