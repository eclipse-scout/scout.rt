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
  this.loadingSupport = new scout.DefaultFieldLoadingSupport(this);
};
scout.inherits(scout.BrowserField, scout.ValueField);

scout.BrowserField.prototype._render = function($parent) {
  this.addContainer($parent, 'browser-field');
  this.addLabel();
  this.addField($parent.makeElement('<iframe>'));
  this.addStatus();
  this.myWindow = $parent.window(true);

  this._postMessageListener = this._onPostMessage.bind(this);
  this.myWindow.addEventListener('message', this._postMessageListener);
};

/**
 * @override ValueField.js
 */
scout.BrowserField.prototype._renderProperties = function() {
  scout.BrowserField.parent.prototype._renderProperties.call(this);
  this._renderIframeProperties();
};

scout.BrowserField.prototype._renderIframeProperties = function() {
  this._renderLocation();
  this._renderScrollBarsEnabled();
  this._renderSandboxEnabled(); // includes _renderSandboxPermissions()
};


scout.BrowserField.prototype._renderLocation = function() {
  this.$field.attr('src', this.location);
};

scout.BrowserField.prototype._renderScrollBarsEnabled = function() {
  this.$field.toggleClass('no-scrolling', !this.scrollBarsEnabled);
  // According to http://stackoverflow.com/a/18470016, setting 'overflow: hidden' via
  // CSS should be enough. However, if the inner page sets 'overflow' to another value,
  // scroll bars are shown again. Therefore, we add the legacy 'scrolling=no' attribute,
  // which is deprecated in HTML5, but seems to do the trick.
  if (this.scrollBarsEnabled) {
    this.$field.removeAttr('scrolling');
  } else {
    this.$field.attr('scrolling', 'no');
  }
};

scout.BrowserField.prototype._renderSandboxEnabled = function() {
  if (this.sandboxEnabled) {
    this._renderSandboxPermissions();
  } else {
    this.$field.removeAttr('sandbox');
    this.$field.removeAttr('security');
  }
};

scout.BrowserField.prototype._renderSandboxPermissions = function() {
  if (this.sandboxEnabled) {
    this.$field.attr('sandbox', scout.nvl(this.sandboxPermissions, ''));
    if (scout.device.requiresIframeSecurityAttribute()) {
      this.$field.attr('security', 'restricted');
    }
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
  if (scout.device.requiresIframeSecurityAttribute()) {
    this.$field.remove();
    this._removeField();
    this.addField(parent.$container.makeElement('<iframe>'));
    this._renderIframeProperties();
    this.htmlComp.revalidateLayout();
  }
};

