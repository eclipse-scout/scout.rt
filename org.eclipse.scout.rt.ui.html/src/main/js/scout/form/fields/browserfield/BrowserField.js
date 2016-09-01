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

  this._postMessageListener;
  this._popupWindow;
  this._externalWindowTextField;
  this._externalWindowButton;
  this.loadingSupport = new scout.LoadingSupport({widget: this});
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
    this.addField($parent.makeElement('<iframe>'));
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

  this._postMessageListener = this._onPostMessage.bind(this);
  this.myWindow.addEventListener('message', this._postMessageListener);

  if (this.enabled) {
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
  if (!this.showInExternalWindow) {
    // <iframe>
    this.$field.attr('src', this.location);
  } else {
    // fallback: separate window
    if (this._popupWindow && !this._popupWindow.closed) {
      this._popupWindow.location = this.location;
    }
  }
};

scout.BrowserField.prototype._renderScrollBarEnabled = function() {
  if (!this.showInExternalWindow) {
    this.$field.toggleClass('no-scrolling', !this.scrollBarEnabled);
    // According to http://stackoverflow.com/a/18470016, setting 'overflow: hidden' via
    // CSS should be enough. However, if the inner page sets 'overflow' to another value,
    // scroll bars are shown again. Therefore, we add the legacy 'scrolling=no' attribute,
    // which is deprecated in HTML5, but seems to do the trick.
    if (this.scrollBarEnabled) {
      this.$field.removeAttr('scrolling');
    } else {
      this.$field.attr('scrolling', 'no');
    }
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
  }
};

scout.BrowserField.prototype._renderSandboxPermissions = function() {
  if (!this.showInExternalWindow && this.sandboxEnabled) {
    this.$field.attr('sandbox', scout.nvl(this.sandboxPermissions, ''));
    if (scout.device.requiresIframeSecurityAttribute()) {
      this.$field.attr('security', 'restricted');
    }
  }
};

scout.BrowserField.prototype._renderExternalWindowButtonText = function() {
  if (this.showInExternalWindow) {
    this._externalWindowButton.text(this.externalWindowButtonText);
  }
};

scout.BrowserField.prototype._renderExternalWindowFieldText = function() {
  if (this.showInExternalWindow) {
    this._externalWindowTextField .text(this.externalWindowFieldText);
  }
};

scout.BrowserField.prototype._openPopupWindow = function(reopenIfClosed) {
  reopenIfClosed = scout.nvl(reopenIfClosed, true);
  if (!this.showInExternalWindow) {
    return;
  }

  if (!this._popupWindow || (reopenIfClosed && this._popupWindow.closed)) {
    var popupBlockerHandler = new scout.PopupBlockerHandler(this.session);
    // (a) positioning and sizing
    // screenLeft, screenTop might reveal the actual document screen position; screenX, screenY is just the browser window screen position
    // window should not be positioned outside of the available screen (probably not even possible), subtract 400 pixel of lower right corner of screen
    var windowLeft = Math.min(scout.nvl(window.screenLeft, window.screenX) + this.$field.offset().left, window.screen.availWidth - 400);
    // add 50 px (guessing there is a toolbar of 50 px)
    var windowTop = Math.min(scout.nvl(window.screenTop, window.screenY + 50) + this.$field.offset().top, window.screen.availHeight - 400);
    // do not taskbar hide window, leave a safety margin of 40 pixel to lower screen bound (suppose a taskbar is shown there)
    var windowWidth = ((this.$field.width() + windowLeft) > window.screen.availWidth) ? (window.screen.availWidth - windowLeft) : this.$field.width();
    var windowHeight = ((this.$field.height() + windowTop) > window.screen.availHeight) ? (window.screen.availHeight - windowTop) : this.$field.height();
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
        'left=' + windowLeft,
        'top=' + windowTop,
        'width=' + windowWidth,
        'height=' + windowHeight
        );
    this._popupWindow = popupBlockerHandler.openWindow(this.location,
        undefined,
        windowSpecs);
    if (this._popupWindow) {
      this._popupWindowOpen();
    } else {
      $.log.warn('Popup-blocker detected! Show link to open window manually');
      popupBlockerHandler.showNotification(function() {
        this._popupWindow = window.open(this.location,
            undefined,
            windowSpecs);
        this._popupWindowOpen();
      }.bind(this));
    }
  }
  else if (reopenIfClosed) {
    this._popupWindow.focus();
  }
};

scout.BrowserField.prototype._popupWindowOpen = function() {
  if (this._popupWindow && !this._popupWindow.closed) {
    this._send('externalWindowState', { 'windowState': scout.BrowserField.windowStates.WINDOW_OPEN });
    var popupInterval = window.setInterval(function() {
      if (this._popupWindow === null || this._popupWindow.closed) {
        window.clearInterval(popupInterval);
        this._send('externalWindowState', { 'windowState': scout.BrowserField.windowStates.WINDOW_CLOSED });
      }
    }.bind(this), 500);
  }
};

scout.BrowserField.prototype._onPostMessage = function(event) {
  $.log.debug('received post-message data=' + event.data + ' origin=' + event.origin);
  this._send('postMessage', {
    data: event.data,
    origin: event.origin
  });
};

/**
 * @override FormField.js
 */
scout.BrowserField.prototype._remove = function() {
  scout.BrowserField.parent.prototype._remove.call(this);
  this.myWindow.removeEventListener('message', this._postMessageListener);
  this._postMessageListener = null;
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

