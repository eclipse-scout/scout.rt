/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ErrorHandler, Event, EventHandler, InitModelOf, MessageBoxes, ObjectWithType, PopupBlockerDesktopNotification, PopupBlockerHandlerModel, scout, Session, SomeRequired, Status} from '../../index';
import $ from 'jquery';

export class PopupBlockerHandler implements PopupBlockerHandlerModel, ObjectWithType {
  declare model: PopupBlockerHandlerModel;
  declare initModel: SomeRequired<this['model'], 'session'>;

  objectType: string;
  session: Session;
  preserveOpener: boolean;

  constructor() {
    this.session = null;
    this.preserveOpener = false;
  }

  init(options: InitModelOf<this>) {
    $.extend(this, options);
  }

  /**
   * @param uri The URI for the window to open
   * @param windowName An optional string name for the new window. The name can be used as the target of links and forms using the target attribute of an 'a' or 'form' element. The name should not contain any blank space.
   *         Note that the window name does not specify the title of the new window.
   * @param windowSpecs Optional parameter listing the features (size, position, scrollbars, etc.) of the new window.
   *         The string must not contain any blank space, each feature name and value must be separated by a comma.
   * @param onWindowOpened Optional function to call when the window has been successfully opened.
   *         Due to popup-blockers this may not necessarily be directly after the call to this method but may be later when the popup-blocker-notification-link is manually activated by the user.
   *
   * @see https://developer.mozilla.org/en-US/docs/Web/API/Window/open
   */
  openWindow(uri: string, windowName?: string, windowSpecs?: string, onWindowOpened?: (popup: Window) => void) {
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
  showNotification(vararg: string | EventHandler<Event<PopupBlockerDesktopNotification>>) {
    let linkUrl: string,
      desktop = this.session.desktop;

    if (typeof vararg === 'string') {
      linkUrl = vararg;
    }

    let notification = scout.create(PopupBlockerDesktopNotification, {
      parent: desktop,
      linkUrl: linkUrl,
      preserveOpener: this.preserveOpener
    });

    if (!linkUrl && $.isFunction(vararg)) {
      notification.on('linkClick', vararg);
    }
    notification.show();
  }

  protected _handleInvalidUri(uri: string, popup: Window, err: any) {
    // Log
    scout.create(ErrorHandler, {
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
