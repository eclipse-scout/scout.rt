scout.init = function(userAgent, objectFactories) {
  var tabId = '' + new Date().getTime();
  $('.scout').each(function() {
    var portletPartId = $(this).data('partid') || '0';
    var jsonSessionId = [portletPartId, tabId].join(':');
    var session = new scout.Session($(this), jsonSessionId, userAgent);
    session.init();
    if (!objectFactories) {
      objectFactories = scout.defaultObjectFactories;
    }
    session.objectFactory.register(objectFactories);
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
 * Implements the 'debounce' pattern. The given function fx is executed after a certain delay
 * (in milliseconds), but if the same function is called a second time within the waiting time,
 * the timer is reset. The default value for 'delay' is 250 ms.
 */
scout.debounce = function(fx, delay) {
  var delayer = null;
  delay = (typeof delay !== 'undefined') ? delay : 250; // default
  return function() {
    var that = this;
    var args = arguments;
    // Cancel a previously scheduled delayer function
    clearTimeout(delayer);
    // Schedule a new delayer function
    delayer = setTimeout(function() {
      fx.apply(that, args);
    }, delay);
  };
};

/**
 * Opens a popup window or new browser tab for the given URL and returns the window reference.
 * <p>
 * If popupWidth and/or popupHeight is missing, the url is opened in a new browser tab.
 * Otherwise, the new popup window is opened in the desired size and centered above the
 * parent window.
 * <p>
 * The argument 'windowId' is a name for the new window. If a window with this name already
 * exists, the url is (re-)loaded in that window, otherwise a new window is created. If
 * popup sizes are missing (i.e. url should be opened in new tab), the windowId is automatically
 * prefixed with '_' (mostly needed for IE). All characters except [A-Za-z_] are converted
 * to underscores.
 * <p>
 * Please not that you can open new windows only inside a handler of a direct user action.
 * Asynchronous calls to this method are blocked by the browsers.
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
      'left=' + popupLeft,
      'top=' + popupTop,
      // Target outerWidth/outerHeight is only respected by Firefox
      'outerWidth=' + popupWidth,
      'outerHeight=' + popupHeight,
      // Fallback for IE and Chrome (window is not really centered, but it's the best we can do)
      'width=' + popupWidth,
      'height=' + popupHeight,
    ].join(',');
  } else {
    windowId = '_' + windowId;
  }

  // In case 'window.location' was passed, extract the URL as string
  if (typeof url === 'object' && 'href' in url) {
    url = url.href;
  }
  // In some versions of IE, only [A-Za-z_] are allowed as window IDs (http://stackoverflow.com/a/2189596)
  windowId = (windowId || '').replace(/[^A-Za-z_]/g, '_');

  var w = window.open(url, windowId, windowSpec);
  return w;
};
