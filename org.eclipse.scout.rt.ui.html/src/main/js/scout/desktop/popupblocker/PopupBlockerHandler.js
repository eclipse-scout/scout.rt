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

/**
 * @param {optional boolean} preserveOpener A boolean indicating if the popup-window should have a back reference to the origin window. By default this parameter is false because of security reasons. Only trusted sites may be allowed to access the opener window and potentially modify the origin web application! See https://mathiasbynens.github.io/rel-noopener/ for more details.
 */
scout.PopupBlockerHandler = function(session, preserveOpener) {
  this.session = session;
  this.preserveOpener = preserveOpener;
};

/**
 * @param {String} uri The URI for the window to open
 * @param {optional String} windowName An optional string name for the new window. The name can be used as the target of links and forms using the target attribute of an 'a' or 'form' element. The name should not contain any blank space. Note that the window name does not specify the title of the new window.
 * @param {optional String} windowSpecs Optional parameter listing the features (size, position, scrollbars, etc.) of the new window. The string must not contain any blank space, each feature name and value must be separated by a comma.
 * @param {optional function} onWindowOpened Optional function to call when the window has been successfully opened. Due to popup-blockers this may not necessarily be directly after the call to this method but may be later when the popup-blocker-notification-link is manually activated by the user.
 *
 * @see https://developer.mozilla.org/en-US/docs/Web/API/Window/open
 */
scout.PopupBlockerHandler.prototype.openWindow = function(uri, windowName, windowSpecs, onWindowOpened) {
  windowSpecs = windowSpecs || 'location=no,toolbar=no,menubar=no,resizable=yes,scrollbars=yes';
  windowName = windowName || 'scout_' + new Date().getTime();

  var popup = window.open('', windowName, windowSpecs);
  if (popup) {
    if (!this.preserveOpener) {
      popup.opener = null;
    }
    popup.window.location.href = uri;
    if (onWindowOpened) {
      onWindowOpened(popup);
    }
  } else {
    $.log.warn('Popup-blocker detected! Show link to open window manually');
    if (onWindowOpened) {
      this.showNotification(function() {
        this.openWindow(uri, windowName, windowSpecs, onWindowOpened);
      }.bind(this));
    } else {
      this.showNotification(uri);
    }
  }
};

// Shows a notification when popup-blocker has been detected
scout.PopupBlockerHandler.prototype.showNotification = function(vararg) {
  var notification, linkUrl,
    desktop = this.session.desktop;

  if (typeof vararg === 'string') {
    linkUrl = vararg;
  }

  notification = scout.create('DesktopNotification:PopupBlocker', {
    parent: desktop,
    linkUrl: linkUrl,
    preserveOpener: this.preserveOpener
  });

  if (!linkUrl && $.isFunction(vararg)) {
    notification.on('linkClick', vararg);
  }
  notification.show();
};
