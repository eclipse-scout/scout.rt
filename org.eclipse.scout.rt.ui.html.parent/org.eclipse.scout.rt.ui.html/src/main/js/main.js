/**
 * See javadoc for Session.js
 */
scout.init = function(initOptions) {
  var tabId = '' + new Date().getTime();
  window.scout.sessions = []; // FIXME BSH Needed for detaching windows, but can we do this better???
  $('.scout').each(function() {
    var portletPartId = $(this).data('partid') || '0';
    var jsonSessionId = [portletPartId, tabId].join(':');
    var session = new scout.Session($(this), jsonSessionId, initOptions);
    session.init();
    window.scout.sessions.push(session);
  });
};

/**
 * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Details_of_the_Object_Model
 */
scout.inherits = function(childCtor, parentCtor) {
  childCtor.prototype = Object.create(parentCtor.prototype);
  childCtor.prototype.constructor = childCtor;
  childCtor.parent = parentCtor;
};

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
 */
scout.openWindow = function(url, windowId, popupWidth, popupHeight) {
  var windowSpec = '';
  if (popupWidth && popupHeight) {
    var parentLeft = (typeof window.screenX !== 'undefined') ? window.screenX : window.screenLeft;
    var parentTop = (typeof window.screenY !== 'undefined') ? window.screenY : window.screenTop;
    var parentWidth = (typeof window.outerWidth !== 'undefined') ? window.outerWidth : screen.width;
    var parentHeight = (typeof window.outerHeight !== 'undefined') ? window.outerHeight : screen.height;
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
};

/**
 * Returns a random sequence of characters out of the set [a-zA-Z0-9] with the
 * given length. The default length is 8.
 */
scout.getRandomId = function(length) {
  if (typeof length === 'undefined') {
    length = 8;
  }
  var charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  var s = '';
  for (var i = 0; i < length; i++) {
    s += charset[Math.floor(Math.random() * charset.length)];
  }
  return s;
};

/**
 * Converts the given decimal number to base-62 (i.e. the same value, but
 * represented by [a-zA-Z0-9] instead of only [0-9].
 */
scout.numberToBase62 = function(number) {
  if (typeof number === 'undefined') {
    return undefined;
  }
  var symbols = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'.split('');
  var base = 62;
  var s = '';
  var n;
  while (number >= 1) {
    n = Math.floor(number / base);
    s = symbols[(number - (base * n))] + s;
    number = n;
  }
  return s;
};

/**
 * Returns the current time (with milliseconds) as a string in the format
 * [year#4][month#2][day#2][hour#2][minute#2][second#2][#millisecond#3].
 */
scout.getTimestamp = function() {
  var padZeroLeft = function(s, padding) {
    if ((s + '').length >= padding) {
      return s;
    }
    var z = new Array(padding + 1).join('0') + s;
    return z.slice(-padding);
  };
  var date = new Date();
  return date.getFullYear() + padZeroLeft(date.getMonth() + 1, 2) + padZeroLeft(date.getDate(), 2) + padZeroLeft(date.getHours(), 2) + padZeroLeft(date.getMinutes(), 2) + padZeroLeft(date.getSeconds(), 2) + padZeroLeft(date.getMilliseconds(), 3);
};

/**
 * Counts and returns the properties of a given object.
 */
scout.countProperties = function(obj) {
  var count = 0;
  for (var prop in obj) {
    if (obj.hasOwnProperty(prop)) {
      count++;
    }
  }
  return count;
};
