/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.OpenUriHandler = function() {};

scout.OpenUriHandler.prototype.init = function(model) {
  this.session = model.session;
};

scout.OpenUriHandler.prototype.openUri = function(uri, action) {
  $.log.debug('(OpenUriHandler#openUri) uri=' + uri + ' action=' + action);
  if (!uri) {
    return;
  }
  action = scout.nvl(action, scout.Desktop.UriAction.OPEN);

  if (action === scout.Desktop.UriAction.DOWNLOAD) {
    this.handleUriActionDownload(uri);
  } else if (action === scout.Desktop.UriAction.OPEN) {
    this.handleUriActionOpen(uri);
  } else if (action === scout.Desktop.UriAction.NEW_WINDOW) {
    this.handleUriActionNewWindow(uri);
  } else if (action === scout.Desktop.UriAction.SAME_WINDOW) {
    this.handleUriActionSameWindow(uri);
  }
};

scout.OpenUriHandler.prototype.handleUriActionDownload = function(uri) {
  if (scout.device.isIos()) {
    // The iframe trick does not work for ios
    // Since the file cannot be stored on the file system it will be shown in the browser if possible
    // -> create a new window to not replace the existing content.
    // Drawback: Popup-Blocker will show up
    // Opening in new window does not work in standalone mode because the window will be opened in safari which creates a new http session.
    // Because the downloads are linked to the http session they cannot be downloaded using safari
    if (scout.device.isStandalone()) {
      this.openUriInSameWindow(uri);
    } else {
      this.openUriAsNewWindow(uri);
    }
  } else if (scout.device.browser === scout.Device.Browser.CHROME) {
    // "Hidden iframe"-solution is not working in Chromium (https://bugs.chromium.org/p/chromium/issues/detail?id=663325)
    this.openUriInSameWindow(uri);
  } else {
    this.openUriInIFrame(uri);
  }
};

scout.OpenUriHandler.prototype.isUriWithExternallyHandledProtocol = function(uri) {
  return /^(callto|facetime|fax|geo|mailto|maps|notes|sip|skype|tel):/.test(uri);
};

scout.OpenUriHandler.prototype.handleUriActionOpen = function(uri) {
  if (scout.device.isIos()) {
    // Open in same window.
    // Don't call _openUriInIFrame here, if action is set to open, an url is expected to be opened in the same window
    // Additionally, some url types require to be opened in the same window like tel or mailto, at least on mobile devices
    this.openUriInSameWindow(uri);
  } else if (this.isUriWithExternallyHandledProtocol(uri)) {
    if (scout.device.browser === scout.Device.Browser.CHROME) {
      // "Hidden iframe"-solution is not working in Chromium (https://bugs.chromium.org/p/chromium/issues/detail?id=663325)
      this.openUriInSameWindow(uri);
    } else {
      // do not use sameWindow since the poller would be disconnected in firefox
      this.openUriInIFrame(uri);
    }
  } else {
    this.openUriAsNewWindow(uri);
  }
};

scout.OpenUriHandler.prototype.handleUriActionNewWindow = function(uri) {
  this.openUriAsNewWindow(uri);
};

scout.OpenUriHandler.prototype.handleUriActionSameWindow = function(uri) {
  this.openUriInSameWindow(uri);
};

scout.OpenUriHandler.prototype.openUriInSameWindow = function(uri) {
  window.location.assign(uri);
};

scout.OpenUriHandler.prototype.openUriInIFrame = function(uri) {
  // Create a hidden iframe and set the URI as src attribute value
  var $iframe = this.session.$entryPoint.appendElement('<iframe>', 'download-frame')
    .attr('tabindex', -1)
    .attr('src', uri);

  // Remove the iframe again after 10s (should be enough to get the download started)
  setTimeout(function() {
    $iframe.remove();
  }, 10 * 1000);
};

scout.OpenUriHandler.prototype.openUriAsNewWindow = function(uri) {
  var popupBlockerHandler = new scout.PopupBlockerHandler(this.session),
    popup = popupBlockerHandler.openWindow(uri);

  if (!popup) {
    popupBlockerHandler.showNotification(uri);
  }
};
