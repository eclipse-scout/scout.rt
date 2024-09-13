/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, DesktopUriAction, Device, InitModelOf, ObjectWithType, OpenUriHandlerModel, PopupBlockerHandler, scout, Session, SomeRequired} from '../index';
import $ from 'jquery';

export class OpenUriHandler implements OpenUriHandlerModel, ObjectWithType {
  declare model: OpenUriHandlerModel;
  declare initModel: SomeRequired<this['model'], 'session'>;

  objectType: string;
  session: Session;

  init(model: InitModelOf<this>) {
    this.session = model.session;
  }

  openUri(uri: string, action?: DesktopUriAction) {
    $.log.isDebugEnabled() && $.log.debug('(OpenUriHandler#openUri) uri=' + uri + ' action=' + action);
    if (!uri) {
      return;
    }
    action = scout.nvl(action, Desktop.UriAction.OPEN);

    if (action === Desktop.UriAction.DOWNLOAD) {
      this.handleUriActionDownload(uri);
    } else if (action === Desktop.UriAction.OPEN) {
      this.handleUriActionOpen(uri);
    } else if (action === Desktop.UriAction.NEW_WINDOW) {
      this.handleUriActionNewWindow(uri);
    } else if (action === Desktop.UriAction.POPUP_WINDOW) {
      this.handleUriActionPopupWindow(uri);
    } else if (action === Desktop.UriAction.SAME_WINDOW) {
      this.handleUriActionSameWindow(uri);
    }
  }

  handleUriActionDownload(uri: string) {
    if (Device.get().isIos()) {
      // The iframe trick does not work for ios
      // Since the file cannot be stored on the file system it will be shown in the browser if possible
      // -> create a new window to not replace the existing content.
      // Drawback: Popup-Blocker will show up
      // Opening in new window does not work in standalone mode because the window will be opened in safari which creates a new http session.
      // Because the downloads are linked to the http session they cannot be downloaded using safari
      if (Device.get().isStandalone()) {
        this.openUriInSameWindow(uri);
      } else {
        this.openUriAsNewWindow(uri);
      }
    } else if (Device.get().browser === Device.Browser.CHROME && this.isUriWithExternallyHandledProtocol(uri)) {
      // "Hidden iframe"-solution is not working in Chromium (https://bugs.chromium.org/p/chromium/issues/detail?id=663325)
      this.openUriInSameWindow(uri);
    } else {
      this.openUriInIFrame(uri);
    }
  }

  isUriWithExternallyHandledProtocol(uri: string): boolean {
    return /^(callto|facetime|fax|geo|mailto|maps|notes|sip|skype|tel|google.navigation|sms|msteams):/.test(uri);
  }

  handleUriActionOpen(uri: string) {
    if (Device.get().isIos()) {
      // Open in same window.
      // Don't call _openUriInIFrame here, if action is set to open, an url is expected to be opened in the same window
      // Additionally, some url types require to be opened in the same window like tel or mailto, at least on mobile devices
      this.openUriInSameWindow(uri);
    } else if (this.isUriWithExternallyHandledProtocol(uri)) {
      if (Device.get().browser === Device.Browser.CHROME || Device.get().isAndroid()) {
        // "Hidden iframe"-solution is not working in Chromium (https://bugs.chromium.org/p/chromium/issues/detail?id=663325)
        this.openUriInSameWindow(uri);
      } else {
        // do not use sameWindow since the poller would be disconnected in firefox
        this.openUriInIFrame(uri);
      }
    } else {
      this.openUriAsNewWindow(uri);
    }
  }

  handleUriActionNewWindow(uri: string) {
    this.openUriAsNewWindow(uri);
  }

  handleUriActionPopupWindow(uri: string) {
    this.openUriAsPopupWindow(uri);
  }

  handleUriActionSameWindow(uri: string) {
    this.openUriInSameWindow(uri);
  }

  openUriInSameWindow(uri: string) {
    window.location.assign(uri);
  }

  openUriInIFrame(uri: string) {
    // Create a hidden iframe and set the URI as src attribute value
    let $iframe = this.session.$entryPoint.appendElement('<iframe>', 'download-frame')
      .attr('tabindex', -2)
      .attr('src', uri);

    // Remove the iframe again after 10s (should be enough to get the download started)
    setTimeout(() => {
      $iframe.remove();
    }, 10 * 1000);
  }

  openUriAsNewWindow(uri: string) {
    let popupBlockerHandler = scout.create(PopupBlockerHandler, {session: this.session});
    popupBlockerHandler.openWindow(uri);
  }

  openUriAsPopupWindow(uri: string) {
    let popupBlockerHandler = scout.create(PopupBlockerHandler, {session: this.session});
    popupBlockerHandler.openWindow(uri, null, 'location=no,toolbar=no,menubar=no,resizable=yes,scrollbars=yes');
  }
}
