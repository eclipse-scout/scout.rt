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
scout.PopupBlockerHandler = function(session) {
  this.session = session;
};

scout.PopupBlockerHandler.prototype.openWindow = function(uri, windowName, windowSpecs) {
  var popup;

  windowSpecs = windowSpecs || 'location=no,toolbar=no,menubar=no,resizable=yes,scrollbars=yes';
  windowName = windowName || 'scout_' + new Date().getTime();

  if (scout.device.browser === scout.Device.Browser.INTERNET_EXPLORER) {
    // Workaround for IE: When in "protected mode", window.open() returns null for external URLs, even when
    // the popup was successfully opened! To check if a popup blocker is active, we first open an empty
    // popup with no URL, which will return null when the popup was blocked. If the popup was successful,
    // we change the location to the target URI.
    popup = window.open('', windowName, windowSpecs);
    if (popup) {
      popup.window.location.href = uri;
    }
  } else {
    // Chrome returns undefined, FF null when popup is blocked
    popup = window.open(uri, windowName, windowSpecs);
  }
  return popup;
};

// Shows a notfication when popup blocker has been detected
scout.PopupBlockerHandler.prototype.showNotification = function(vararg) {
  var notification, linkUrl,
    desktop = this.session.desktop;

  if (typeof vararg === 'string') {
    linkUrl = vararg;
  }

  notification = scout.create('DesktopNotification.PopupBlocker', {
    parent: desktop,
    linkUrl: linkUrl
  });

  if (!linkUrl && $.isFunction(vararg)) {
    notification.on('linkClick', vararg);
  }
  notification.show();
};
