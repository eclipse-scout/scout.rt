/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {MessageBoxes, scout, Status} from '../../index';
import $ from 'jquery';

export default class PopupBlockerHandler {
  /**
   * @param {boolean} [preserveOpener] A boolean indicating if the popup-window should have a back reference to the origin window. By default this parameter is false because of security reasons. Only trusted sites may be allowed to access the opener window and potentially modify the origin web application! See https://mathiasbynens.github.io/rel-noopener/ for more details.
   * @deprecated use scout.create to create an instance of this class
   */
  constructor(session, preserveOpener) {
    this.session = session;
    this.preserveOpener = preserveOpener;
  }

  init(options) {
    $.extend(this, options);
  }

  /**
   * @param {String} uri The URI for the window to open
   * @param {String} [windowName] An optional string name for the new window. The name can be used as the target of links and forms using the target attribute of an 'a' or 'form' element. The name should not contain any blank space.
   *         Note that the window name does not specify the title of the new window.
   * @param {String} [windowSpecs] Optional parameter listing the features (size, position, scrollbars, etc.) of the new window.
   *         The string must not contain any blank space, each feature name and value must be separated by a comma.
   * @param {function} [onWindowOpened] Optional function to call when the window has been successfully opened.
   *         Due to popup-blockers this may not necessarily be directly after the call to this method but may be later when the popup-blocker-notification-link is manually activated by the user.
   *
   * @see https://developer.mozilla.org/en-US/docs/Web/API/Window/open
   */
  openWindow(uri, windowName, windowSpecs, onWindowOpened) {
    windowName = windowName || 'scout_' + new Date().getTime();

    let popup = window.open('', windowName, windowSpecs);
    if (popup) {
      if (!this.preserveOpener) {
        popup.opener = null;
      }
      try {
        popup.window.location.href = uri;
      } catch (err) {
        this._handleInvalidUri(uri, popup, err);
        return;
      }
      if (onWindowOpened) {
        onWindowOpened(popup);
      }
    } else {
      $.log.warn('Popup-blocker detected! Show link to open window manually');
      this.showNotification(() => {
        this.openWindow(uri, windowName, windowSpecs, onWindowOpened);
      });
    }
  }

  // Shows a notification when popup-blocker has been detected
  showNotification(vararg) {
    let notification, linkUrl,
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
  }

  _handleInvalidUri(uri, popup, err) {
    // Log
    scout.create('ErrorHandler', {
      logError: true,
      displayError: false,
      sendError: false
    }).handle(err);

    // Close popup
    popup.close();

    // Show message
    MessageBoxes.createOk(this.session.desktop)
      .withHeader(this.session.text('ui.UnexpectedProblem'))
      .withBody(this.session.text('ui.InvalidUriMsg'))
      .withSeverity(Status.Severity.ERROR)
      .buildAndOpen();
  }
}
