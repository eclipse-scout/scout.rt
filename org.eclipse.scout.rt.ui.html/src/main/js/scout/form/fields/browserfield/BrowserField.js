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
scout.BrowserField = function() {
  scout.BrowserField.parent.call(this);

  this.autoCloseExternalWindow = false;
  this.externalWindowButtonText;
  this.externalWindowFieldText;
  this.location;
  this.sandboxEnabled = true;
  this.scrollBarEnabled = false;
  this.showInExternalWindow = false;
  this._messageListener;
  this._popupWindow;
  this._externalWindowTextField;
  this._externalWindowButton;
  // Iframe on iOS is always as big as its content. Workaround it by using a wrapper div with overflow: auto
  this.wrapIframe = scout.device.isIos();
};
scout.inherits(scout.BrowserField, scout.ValueField);

scout.BrowserField.windowStates = {
  WINDOW_OPEN: "true",
  WINDOW_CLOSED: "false"
};

scout.BrowserField.prototype._render = function($parent) {
  this.addContainer($parent, 'browser-field');
  this.addLabel();
  this.addStatus();

  if (!this.showInExternalWindow) {
    // mode 1: <iframe>
    var $iframe;
    if (this.wrapIframe) {
      this.addFieldContainer($parent.makeDiv('iframe-wrapper'));
      $iframe = this.$fieldContainer.appendElement('<iframe>');
    } else {
      $iframe = $parent.makeElement('<iframe>');
    }
    this.addField($iframe);
  } else {
    // mode 2: separate window
    this.addField($parent.makeDiv());
    this._externalWindowTextField = this.$field.appendDiv()
      .addClass('alt');
    this._externalWindowButton = this.$field.appendDiv()
      .addClass('button')
      .on('click', this._openPopupWindow.bind(this));
  }

  this.myWindow = $parent.window(true);

  this._messageListener = this._onMessage.bind(this);
  this.myWindow.addEventListener('message', this._messageListener);

  if (this.enabledComputed) {
    // use setTimeout to call method, because _openPopupWindow must be called after layouting
    setTimeout(this._openPopupWindow.bind(this, true), 20);
  }
};

/**
 * @override ValueField.js
 */
scout.BrowserField.prototype._renderProperties = function() {
  scout.BrowserField.parent.prototype._renderProperties.call(this);
  this._renderIframeProperties();
  // external window properties
  this._renderExternalWindowButtonText();
  this._renderExternalWindowFieldText();
};

scout.BrowserField.prototype._renderIframeProperties = function() {
  this._renderLocation();
  this._renderScrollBarEnabled();
  this._renderSandboxEnabled(); // includes _renderSandboxPermissions()
};

scout.BrowserField.prototype._renderLocation = function() {
  // Convert empty locations to 'about:blank', because in Firefox (maybe others, too?),
  // empty locations simply remove the src attribute but don't remove the old content.
  var location = this.location || 'about:blank';
  if (!this.showInExternalWindow) {
    // <iframe>
    this.$field.attr('src', location);
  } else {
    // fallback: separate window
    if (this._popupWindow && !this._popupWindow.closed) {
      this._popupWindow.location = location;
    }
  }
};

scout.BrowserField.prototype._renderScrollBarEnabled = function() {
  if (!this.showInExternalWindow) {
    this.$fieldContainer.toggleClass('no-scrolling', !this.scrollBarEnabled);
    // According to http://stackoverflow.com/a/18470016, setting 'overflow: hidden' via
    // CSS should be enough. However, if the inner page sets 'overflow' to another value,
    // scroll bars are shown again. Therefore, we add the legacy 'scrolling' attribute,
    // which is deprecated in HTML5, but seems to do the trick.
    this.$field.attr('scrolling', (this.scrollBarEnabled ? 'yes' : 'no'));

    // re-render location otherwise the attribute change would have no effect, see
    // https://html.spec.whatwg.org/multipage/embedded-content.html#attr-iframe-sandbox
    this._renderLocation();
  }
};

scout.BrowserField.prototype._renderSandboxEnabled = function() {
  if (!this.showInExternalWindow) {
    if (this.sandboxEnabled) {
      this._renderSandboxPermissions();
    } else {
      this.$field.removeAttr('sandbox');
      this.$field.removeAttr('security');
    }
    // re-render location otherwise the attribute change would have no effect, see
    // https://html.spec.whatwg.org/multipage/embedded-content.html#attr-iframe-sandbox
    this._renderLocation();
  }
};

scout.BrowserField.prototype._renderSandboxPermissions = function() {
  if (!this.showInExternalWindow && this.sandboxEnabled) {
    this.$field.attr('sandbox', scout.nvl(this.sandboxPermissions, ''));
    if (scout.device.requiresIframeSecurityAttribute()) {
      this.$field.attr('security', 'restricted');
    }
    // re-render location otherwise the attribute change would have no effect, see
    // https://html.spec.whatwg.org/multipage/embedded-content.html#attr-iframe-sandbox
    this._renderLocation();
  }
};

scout.BrowserField.prototype._renderExternalWindowButtonText = function() {
  if (this.showInExternalWindow) {
    this._externalWindowButton.text(this.externalWindowButtonText);
  }
};

scout.BrowserField.prototype._renderExternalWindowFieldText = function() {
  if (this.showInExternalWindow) {
    this._externalWindowTextField.text(this.externalWindowFieldText);
  }
};

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
scout.BrowserField.prototype._calcPopupBounds = function() {
  var myWindow = this.$container.window(true);

  var POPUP_WINDOW_TOP_HEIGHT = 30;
  var POPUP_WINDOW_BOTTOM_HEIGHT = 8;
  var POPUP_WINDOW_CHROME_HEIGHT = POPUP_WINDOW_TOP_HEIGHT + POPUP_WINDOW_BOTTOM_HEIGHT;

  var BROWSER_WINDOW_TOP_HEIGHT = 55;
  var browserX = Math.max(0, myWindow.screenX);
  var browserY = Math.max(0, myWindow.screenY);

  var browserBounds = new scout.Rectangle(
    browserX,
    browserY,
    $(myWindow).width(),
    $(myWindow).height() + BROWSER_WINDOW_TOP_HEIGHT);

  var fieldBounds = new scout.Rectangle(
    this.$field.offset().left,
    this.$field.offset().top,
    this.$field.width(),
    this.$field.height());

  var popupX = browserBounds.x + fieldBounds.x;
  var popupY = browserBounds.y + fieldBounds.y + BROWSER_WINDOW_TOP_HEIGHT;
  var popupWidth = fieldBounds.width;
  var popupHeight = fieldBounds.height + POPUP_WINDOW_CHROME_HEIGHT;

  // ensure that the lower Y of the new popup is not below the lower Y of the browser window
  var popupLowerY = popupY + popupHeight;
  var browserLowerY = browserBounds.y + browserBounds.height;
  if (popupLowerY > browserLowerY) {
    popupHeight -= (popupLowerY - browserLowerY) + POPUP_WINDOW_CHROME_HEIGHT;
  }

  return new scout.Rectangle(
    scout.numbers.round(popupX),
    scout.numbers.round(popupY),
    scout.numbers.round(popupWidth),
    scout.numbers.round(popupHeight)
  );
};

scout.BrowserField.prototype._openPopupWindow = function(reopenIfClosed) {
  reopenIfClosed = scout.nvl(reopenIfClosed, true);
  if (!this.showInExternalWindow) {
    return;
  }

  if (!this._popupWindow || (reopenIfClosed && this._popupWindow.closed)) {
    var popupBlockerHandler = new scout.PopupBlockerHandler(this.session);
    var popupBounds = this._calcPopupBounds();
    // (b) window specifications
    var windowSpecs = scout.strings.join(',',
      'directories=no',
      'location=no',
      'menubar=no',
      'resizable=yes,',
      'status=no',
      'scrollbars=' + (this.scrollBarEnabled ? 'yes' : 'no'),
      'toolbar=no',
      'dependent=yes',
      'left=' + popupBounds.x,
      'top=' + popupBounds.y,
      'width=' + popupBounds.width,
      'height=' + popupBounds.height
    );
    var location = this.location || 'about:blank';
    this._popupWindow = popupBlockerHandler.openWindow(location,
      undefined,
      windowSpecs);
    if (this._popupWindow) {
      this._popupWindowOpen();
    } else {
      $.log.warn('Popup-blocker detected! Show link to open window manually');
      popupBlockerHandler.showNotification(function() {
        this._popupWindow = window.open(location,
          undefined,
          windowSpecs);
        this._popupWindowOpen();
      }.bind(this));
    }
  } else if (reopenIfClosed) {
    this._popupWindow.focus();
  }
};

scout.BrowserField.prototype._popupWindowOpen = function() {
  if (this._popupWindow && !this._popupWindow.closed) {
    this.trigger('externalWindowStateChange', {
      windowState: scout.BrowserField.windowStates.WINDOW_OPEN
    });
    var popupInterval = window.setInterval(function() {
      var popupWindowClosed = false;
      try {
        popupWindowClosed = this._popupWindow === null || this._popupWindow.closed;
      } catch (e) {
        // for some unknown reason, IE sometimes throws a "SCRIPT16386" error while trying to read '._popupWindow.closed'.
        $.log.info('Reading the property popupWindow.closed threw an error (Retry in 500ms)');
        return;
      }
      if (popupWindowClosed) {
        window.clearInterval(popupInterval);
        this.trigger('externalWindowStateChange', {
          windowState: scout.BrowserField.windowStates.WINDOW_CLOSED
        });
      }
    }.bind(this), 500);
  }
};

scout.BrowserField.prototype._onMessage = function(event) {
  $.log.debug('received post-message data=' + event.data + ' origin=' + event.origin);
  this.trigger('message', {
    data: event.data,
    origin: event.origin
  });
};

/**
 * @override FormField.js
 */
scout.BrowserField.prototype._remove = function() {
  scout.BrowserField.parent.prototype._remove.call(this);
  this.myWindow.removeEventListener('message', this._messageListener);
  this._messageListener = null;

  // if content is shown in an external window and auto close is set to true
  if (this.showInExternalWindow && this.autoCloseExternalWindow) {
    // try to close popup window (if it is not already closed)
    if (this._popupWindow && !this._popupWindow.closed) {
      this._popupWindow.close();
    }
  }
};

/**
 * @override Widget.js
 */
scout.BrowserField.prototype._afterAttach = function(parent) {
  // the security=restricted attribute prevents browsers (IE 9 and below) from
  // sending any cookies a second time
  // as a workaround for IFRAMEs to work, we have to recreate the whole field in that case
  if (!this.showInExternalWindow && scout.device.requiresIframeSecurityAttribute()) {
    this.$field.remove();
    this._removeField();
    this.addField(parent.$container.makeElement('<iframe>'));
    this._renderIframeProperties();
    this.htmlComp.revalidateLayout();
  }
};
