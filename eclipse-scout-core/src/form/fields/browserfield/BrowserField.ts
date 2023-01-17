/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BrowserFieldEventMap, BrowserFieldLayout, BrowserFieldModel, EnumObject, FormField, IFrame, InitModelOf, numbers, PopupBlockerHandler, PropertyChangeEvent, Rectangle, scout, strings} from '../../../index';
import $ from 'jquery';
import TriggeredEvent = JQuery.TriggeredEvent;

export type BrowserFieldWindowStates = EnumObject<typeof BrowserField.WindowStates>;

export class BrowserField extends FormField implements BrowserFieldModel {
  declare model: BrowserFieldModel;
  declare eventMap: BrowserFieldEventMap;
  declare self: BrowserField;

  autoCloseExternalWindow: boolean;
  externalWindowButtonText: string;
  externalWindowFieldText: string;
  location: string;
  trackLocation: boolean;
  sandboxEnabled: boolean;
  sandboxPermissions: string;
  trustedMessageOrigins: string[];
  scrollBarEnabled: boolean;
  showInExternalWindow: boolean;
  iframe: IFrame;
  myWindow: Window;
  protected _messageListener: (event: MessageEvent) => any;
  protected _popupWindow: Window;
  protected _externalWindowTextField: JQuery;
  protected _externalWindowButton: JQuery;

  constructor() {
    super();

    this.autoCloseExternalWindow = false;
    this.externalWindowButtonText = null;
    this.externalWindowFieldText = null;
    this.location = null;
    this.trackLocation = false;
    this.sandboxEnabled = true;
    this.sandboxPermissions = null;
    this.trustedMessageOrigins = [];
    this.scrollBarEnabled = true;
    this.showInExternalWindow = false;
    this._messageListener = null;
    this._popupWindow = null;
    this._externalWindowTextField = null;
    this._externalWindowButton = null;
  }

  static WindowStates = {
    WINDOW_OPEN: 'true',
    WINDOW_CLOSED: 'false'
  };

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.iframe = scout.create(IFrame, {
      parent: this,
      location: this.location,
      sandboxEnabled: this.sandboxEnabled,
      sandboxPermissions: this.sandboxPermissions,
      scrollBarEnabled: this.scrollBarEnabled,
      trackLocation: this.trackLocation
    });
    this.iframe.on('propertyChange', this._onIFramePropertyChange.bind(this));
  }

  protected override _render() {
    this.addContainer(this.$parent, 'browser-field', new BrowserFieldLayout(this));
    this.addLabel();
    this.addStatus();

    if (!this.showInExternalWindow) {
      // mode 1: <iframe>
      this.iframe.render();
      this.addFieldContainer(this.iframe.$container);
      this.addField(this.iframe.$iframe);
      this.$field.on('load', this._onLoad.bind(this));
    } else {
      // mode 2: separate window
      this.addField(this.$parent.makeDiv());
      this._externalWindowTextField = this.$field.appendDiv()
        .addClass('alt');
      this._externalWindowButton = this.$field.appendDiv()
        .addClass('button')
        .on('click', event => this._openPopupWindow(true));
    }

    this.myWindow = this.$parent.window(true);

    this._messageListener = this._onMessage.bind(this);
    this.myWindow.addEventListener('message', this._messageListener);

    if (this.enabledComputed) {
      // use setTimeout to call method, because _openPopupWindow must be called after layouting
      setTimeout(this._openPopupWindow.bind(this, true), 20);
    }
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderExternalWindowButtonText();
    this._renderExternalWindowFieldText();
  }

  protected override _remove() {
    super._remove();
    this.myWindow.removeEventListener('message', this._messageListener);
    this._messageListener = null;

    // if content is shown in an external window and auto close is set to true
    if (this.showInExternalWindow && this.autoCloseExternalWindow) {
      // try to close popup window (if it is not already closed)
      if (this._popupWindow && !this._popupWindow.closed) {
        this._popupWindow.close();
      }
    }
  }

  setLocation(location: string) {
    this.setProperty('location', location);
    this.iframe.setLocation(location);
  }

  protected _renderLocation() {
    // Convert empty locations to 'about:blank', because in Firefox (maybe others, too?),
    // empty locations simply remove the src attribute but don't remove the old content.
    let location = this.location || 'about:blank';
    if (this.showInExternalWindow) {
      // fallback: separate window
      if (this._popupWindow && !this._popupWindow.closed) {
        this._popupWindow.location = location;
      }
    }
  }

  setAutoCloseExternalWindow(autoCloseExternalWindow: boolean) {
    this.setProperty('autoCloseExternalWindow', autoCloseExternalWindow);
  }

  setExternalWindowButtonText(externalWindowButtonText: string) {
    this.setProperty('externalWindowButtonText', externalWindowButtonText);
  }

  protected _renderExternalWindowButtonText() {
    if (this.showInExternalWindow) {
      this._externalWindowButton.text(this.externalWindowButtonText || '');
    }
  }

  setExternalWindowFieldText(externalWindowFieldText: string) {
    this.setProperty('externalWindowFieldText', externalWindowFieldText);
  }

  protected _renderExternalWindowFieldText() {
    if (this.showInExternalWindow) {
      this._externalWindowTextField.text(this.externalWindowFieldText || '');
    }
  }

  /**
   * Note: this function is designed to deliver good results to position a popup over a BrowserField in Internet Explorer.
   * Other browsers may not perfectly position the popup, since they return different values for screenX/screenY. Also
   * there's no way to retrieve all required values from the window or screen object, that's why we have to use hard coded
   * values here. In order to make this function more flexible you could implement it as a strategy which has different
   * browser dependent implementations.
   *
   * This implementation does also deal with a multi screen setup (secondary monitor). An earlier implementation used
   * screen.availWidth to make sure the popup is within the visible area of the screen. However, screen.availWidth only
   * returns the size of the primary monitor, so we cannot use it. There's no way to check for a secondary monitor from
   * a HTML document. So we removed the check entirely, which shouldn't be an issue since the browser itself does prevent
   * popups from having an invalid position.
   */
  protected _calcPopupBounds(): Rectangle {
    let myWindow = this.$container.window(true);

    let POPUP_WINDOW_TOP_HEIGHT = 30;
    let POPUP_WINDOW_BOTTOM_HEIGHT = 8;
    let POPUP_WINDOW_CHROME_HEIGHT = POPUP_WINDOW_TOP_HEIGHT + POPUP_WINDOW_BOTTOM_HEIGHT;

    let BROWSER_WINDOW_TOP_HEIGHT = 55;

    // Don't limit screenX/Y in any way. Coordinates can be negative (if we have a secondary monitor on the left side
    // of the primary monitor) or larger then the availSize of the screen (if we have a secondary monitor on the right
    // side of the primary monitor). Note that IE cannot properly place the popup on a monitor on the left. It seems
    // to ignore negative X coordinates somehow (but not entirely).
    let browserBounds = new Rectangle(
      myWindow.screenX,
      myWindow.screenY,
      $(myWindow).width(),
      $(myWindow).height() + BROWSER_WINDOW_TOP_HEIGHT);

    let fieldBounds = new Rectangle(
      this.$field.offset().left,
      this.$field.offset().top,
      this.$field.width(),
      this.$field.height());

    let popupX = browserBounds.x + fieldBounds.x;
    let popupY = browserBounds.y + fieldBounds.y + BROWSER_WINDOW_TOP_HEIGHT;
    let popupWidth = fieldBounds.width;
    let popupHeight = fieldBounds.height + POPUP_WINDOW_CHROME_HEIGHT;

    // ensure that the lower Y of the new popup is not below the lower Y of the browser window
    let popupLowerY = popupY + popupHeight;
    let browserLowerY = browserBounds.y + browserBounds.height;
    if (popupLowerY > browserLowerY) {
      popupHeight -= (popupLowerY - browserLowerY) + POPUP_WINDOW_CHROME_HEIGHT;
    }

    return new Rectangle(
      numbers.round(popupX),
      numbers.round(popupY),
      numbers.round(popupWidth),
      numbers.round(popupHeight)
    );
  }

  protected _openPopupWindow(reopenIfClosed?: boolean) {
    reopenIfClosed = scout.nvl(reopenIfClosed, true);
    if (!this.showInExternalWindow) {
      return;
    }

    if (!this._popupWindow || (reopenIfClosed && this._popupWindow.closed)) {
      let popupBlockerHandler = scout.create(PopupBlockerHandler, {session: this.session});
      let popupBounds = this._calcPopupBounds();
      // (b) window specifications
      let windowSpecs = strings.join(',',
        'directories=no',
        'location=no',
        'menubar=no',
        'resizable=yes',
        'status=no',
        'scrollbars=' + (this.scrollBarEnabled ? 'yes' : 'no'),
        'toolbar=no',
        'dependent=yes',
        'left=' + popupBounds.x,
        'top=' + popupBounds.y,
        'width=' + popupBounds.width,
        'height=' + popupBounds.height
      );
      let location = this.location || 'about:blank';
      popupBlockerHandler.openWindow(location, undefined, windowSpecs, this._popupWindowOpen.bind(this));
    } else if (reopenIfClosed) {
      this._popupWindow.focus();
    }
  }

  protected _popupWindowOpen(popup: Window) {
    this._popupWindow = popup;
    if (this._popupWindow && !this._popupWindow.closed) {
      this.trigger('externalWindowStateChange', {
        windowState: BrowserField.WindowStates.WINDOW_OPEN
      });
      let popupInterval = window.setInterval(() => {
        let popupWindowClosed = false;
        try {
          popupWindowClosed = this._popupWindow === null || this._popupWindow.closed;
        } catch (e) {
          // for some unknown reason, IE sometimes throws a "SCRIPT16386" error while trying to read '._popupWindow.closed'.
          $.log.isInfoEnabled() && $.log.info('Reading the property popupWindow.closed threw an error (Retry in 500ms)');
          return;
        }
        if (popupWindowClosed) {
          window.clearInterval(popupInterval);
          this.trigger('externalWindowStateChange', {
            windowState: BrowserField.WindowStates.WINDOW_CLOSED
          });
        }
      }, 500);
    }
  }

  protected _onMessage(event: MessageEvent) {
    // Only handle event originating form "our" iframe
    if (!this._isValidMessageSource(event.source)) {
      return;
    }
    // Check if the origin is trusted before we do anything else with the data
    if (this.trustedMessageOrigins && this.trustedMessageOrigins.length &&
      !this.trustedMessageOrigins.some(origin => origin === event.origin)) {
      $.log.warn('blocked message from untrusted origin ' + event.origin);
      return;
    }
    $.log.isDebugEnabled() && $.log.debug('received post-message: data=' + event.data + ', origin=' + event.origin);
    this.trigger('message', {
      data: event.data,
      origin: event.origin
    });
  }

  protected _isValidMessageSource(source: MessageEventSource): boolean {
    let iframeWindow = (this.$field[0] as HTMLIFrameElement).contentWindow;
    if (source === iframeWindow) {
      return true; // same source
    }

    // Check parents of window in case event source is an inner iframe
    // parent window of topmost window is itself (https://developer.mozilla.org/en-US/docs/Web/API/Window/parent)
    let win = source as Window;
    while (win && win !== win.parent) {
      win = win.parent;
      if (win === iframeWindow) {
        return true;
      }
    }

    return false; // no valid parent window found
  }

  /**
   * Sends a message to the embedded web page (`iframe`).
   *
   * @param message
   *          The message to send.
   * @param targetOrigin
   *          The expected origin of the receiving `window`. If the origin does not match, the browser will not
   *          dispatch the message for security reasons. See the
   *          <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">documentation</a> for
   *          details.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">window.postMessage (MDN)</a>
   */
  postMessage(message: any, targetOrigin: string) {
    $.log.isDebugEnabled() && $.log.debug('send post-message: message=' + message + ', targetOrigin=' + targetOrigin);
    this.iframe && this.iframe.postMessage(message, targetOrigin);
  }

  /** @see BrowserFieldModel.sandboxPermissions */
  setTrackLocation(trackLocation: boolean) {
    this.setProperty('trackLocation', trackLocation);
    this.iframe.setTrackLocation(trackLocation);
  }

  protected _onIFramePropertyChange(event: PropertyChangeEvent<IFrame>) {
    if (!this.trackLocation) {
      return;
    }
    if (event.propertyName === 'location') {
      this._setProperty('location', event.newValue);
    }
  }

  protected _onLoad(event: TriggeredEvent) {
    if (!this.rendered) { // check needed, because this is an async callback
      return;
    }

    this.invalidateLayoutTree();
  }

  /** @see BrowserFieldModel.sandboxEnabled */
  setSandboxEnabled(sandboxEnabled: boolean) {
    this.setProperty('sandboxEnabled', sandboxEnabled);
    this.iframe.setSandboxEnabled(sandboxEnabled);
  }

  /** @see BrowserFieldModel.sandboxPermissions */
  setSandboxPermissions(sandboxPermissions: string) {
    this.setProperty('sandboxPermissions', sandboxPermissions);
    this.iframe.setSandboxPermissions(sandboxPermissions);
  }

  setScrollBarEnabled(scrollBarEnabled: boolean) {
    this.setProperty('scrollBarEnabled', scrollBarEnabled);
    this.iframe.setScrollBarEnabled(scrollBarEnabled);
  }
}
