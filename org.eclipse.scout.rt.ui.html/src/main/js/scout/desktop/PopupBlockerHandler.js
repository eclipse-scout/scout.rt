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

scout.PopupBlockerHandler.prototype.openWindow = function(uri, target, windowSpecs) {
  var popup;
  if (scout.device.browser === scout.Device.Browser.INTERNET_EXPLORER) {
    // Workaround for IE: When in "protected mode", window.open() returns null for external URLs, even when
    // the popup was successfully opened! To check if a popup blocker is active, we first open an empty
    // popup with no URL, which will return null when the popup was blocked. If the popup was successful,
    // we change the location to the target URI.
    popup = window.open('', target, windowSpecs);
    if (popup) {
      popup.window.location.href = uri;
    }
  } else {
    // Chrome returns undefined, FF null when popup is blocked
    popup = window.open(uri, target, windowSpecs);
  }
  return popup;
};

// Shows a notfication when popup blocker has been detected
scout.PopupBlockerHandler.prototype.showNotification = function(vararg) {
  var desktop = this.session.desktop,
    $notification = desktop.$container.makeDiv('notification'),
    $notificationContent = $notification.appendDiv('notification-content notification-closable');

  $notificationContent
    .appendDiv('close')
    .on('click', desktop.removeNotification.bind(desktop, $notification));
  $notificationContent
    .appendDiv('popup-blocked-title')
    .text(this.session.text('ui.PopupBlockerDetected'));

  var $a = $notificationContent
    .appendElement('<a>', 'popup-blocked-link')
    .text(this.session.text('ui.OpenManually'));

  if (typeof vararg === 'string') {
    // vararg = URL
    $a
      .attr('href', scout.strings.encode(vararg))
      .attr('target', '_blank')
      .on('click', desktop.removeNotification.bind(desktop, $notification));
  } else if ($.isFunction(vararg)) {
    // vararg = click-handler
    $a.on('click', function() {
      vararg();
      desktop.removeNotification($notification);
    });
  }

  desktop.addNotification($notification);
};
