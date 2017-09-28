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
scout.IFrame = function() {
  scout.IFrame.parent.call(this);

  this.location = null;
  this.sandboxEnabled = true;
  this.sandboxPermissions = null;
  this.scrollBarEnabled = true;
  // Iframe on iOS is always as big as its content. Workaround it by using a wrapper div with overflow: auto
  // Don't wrap it when running in the chrome emulator (in that case isIosPlatform returns false)
  this.wrapIframe = scout.device.isIosPlatform();
  this.$iframe;
};
scout.inherits(scout.IFrame, scout.Widget);

scout.IFrame.prototype._render = function() {
  if (this.wrapIframe) {
    this.$container = this.$parent.appendDiv('iframe-wrapper');
    this.$iframe = this.$container.appendElement('<iframe>', 'iframe');
  } else {
    this.$iframe = this.$parent.appendElement('<iframe>', 'iframe');
    this.$container = this.$iframe;
  }
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

/**
 * @override ValueField.js
 */
scout.IFrame.prototype._renderProperties = function() {
  scout.IFrame.parent.prototype._renderProperties.call(this);
  this._renderLocation();
  this._renderScrollBarEnabled();
  this._renderSandboxEnabled(); // includes _renderSandboxPermissions()
};

scout.IFrame.prototype.setLocation = function(location) {
  this.setProperty('location', location);
};

scout.IFrame.prototype._renderLocation = function() {
  // Convert empty locations to 'about:blank', because in Firefox (maybe others, too?),
  // empty locations simply remove the src attribute but don't remove the old content.
  var location = this.location || 'about:blank';
  this.$iframe.attr('src', location);
};

scout.IFrame.prototype.setScrollBarEnabled = function(scrollBarEnabled) {
  this.setProperty('scrollBarEnabled', scrollBarEnabled);
};

scout.IFrame.prototype._renderScrollBarEnabled = function() {
  this.$container.toggleClass('no-scrolling', !this.scrollBarEnabled);
  // According to http://stackoverflow.com/a/18470016, setting 'overflow: hidden' via
  // CSS should be enough. However, if the inner page sets 'overflow' to another value,
  // scroll bars are shown again. Therefore, we add the legacy 'scrolling' attribute,
  // which is deprecated in HTML5, but seems to do the trick.
  this.$iframe.attr('scrolling', (this.scrollBarEnabled ? 'yes' : 'no'));

  // re-render location otherwise the attribute change would have no effect, see
  // https://html.spec.whatwg.org/multipage/embedded-content.html#attr-iframe-sandbox
  this._renderLocation();
};

scout.IFrame.prototype.setSandboxEnabled = function(sandboxEnabled) {
  this.setProperty('sandboxEnabled', sandboxEnabled);
};

scout.IFrame.prototype._renderSandboxEnabled = function() {
  if (this.sandboxEnabled) {
    this._renderSandboxPermissions();
  } else {
    this.$iframe.removeAttr('sandbox');
    this.$iframe.removeAttr('security');
  }
  // re-render location otherwise the attribute change would have no effect, see
  // https://html.spec.whatwg.org/multipage/embedded-content.html#attr-iframe-sandbox
  this._renderLocation();
};

scout.IFrame.prototype.setSandboxPermissions = function(sandboxPermissions) {
  this.setProperty('sandboxPermissions', sandboxPermissions);
};

scout.IFrame.prototype._renderSandboxPermissions = function() {
  if (!this.sandboxEnabled) {
    return;
  }
  this.$iframe.attr('sandbox', scout.nvl(this.sandboxPermissions, ''));
  if (scout.device.requiresIframeSecurityAttribute()) {
    this.$iframe.attr('security', 'restricted');
  }
  // re-render location otherwise the attribute change would have no effect, see
  // https://html.spec.whatwg.org/multipage/embedded-content.html#attr-iframe-sandbox
  this._renderLocation();
};
