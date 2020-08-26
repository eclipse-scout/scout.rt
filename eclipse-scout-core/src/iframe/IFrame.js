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
import {Device, HtmlComponent, scout, Widget} from '../index';

export default class IFrame extends Widget {

  constructor() {
    super();

    this.location = null;
    this.sandboxEnabled = true;
    this.sandboxPermissions = null;
    this.scrollBarEnabled = true;
    this.trackLocation = false;
    // Iframe on iOS is always as big as its content. Workaround it by using a wrapper div with overflow: auto
    // Don't wrap it when running in the chrome emulator (in that case isIosPlatform returns false)
    this.wrapIframe = Device.get().isIosPlatform();
    this.$iframe = null;
    this._loadHandler = this._onLoad.bind(this);
  }

  _render() {
    var cssClass = 'iframe ' + Device.get().cssClassForIphone();
    if (this.wrapIframe) {
      this.$container = this.$parent.appendDiv('iframe-wrapper');
      this.$iframe = this.$container.appendElement('<iframe>', cssClass);
    } else {
      this.$iframe = this.$parent.appendElement('<iframe>', cssClass);
      this.$container = this.$iframe;
    }
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  /**
   * @override ValueField.js
   */
  _renderProperties() {
    super._renderProperties();
    this._renderScrollBarEnabled();
    this._renderSandboxEnabled(); // includes _renderSandboxPermissions()
    this._renderLocation(); // Needs to be after _renderScrollBarEnabled and _renderSandboxEnabled, see comment in _renderScrollBarEnabled
    this._renderTrackLocation();
  }

  setLocation(location) {
    this.setProperty('location', location);
  }

  _renderLocation() {
    // Convert empty locations to 'about:blank', because in Firefox (maybe others, too?),
    // empty locations simply remove the src attribute but don't remove the old content.
    let location = this.location || 'about:blank';
    this.$iframe.attr('src', location);
  }

  setTrackLocation(trackLocation) {
    this.setProperty('trackLocation', trackLocation);
  }

  _renderTrackLocation(trackLocation) {
    if (this.trackLocation) {
      this.$iframe.on('load', this._loadHandler);
    } else {
      this.$iframe.off('load', this._loadHandler);
    }
  }

  _onLoad(event) {
    if (!this.rendered) { // check needed, because this is an async callback
      return;
    }

    if (this.trackLocation) {
      let doc = this.$iframe[0].contentDocument;
      if (!doc) {
        // Doc can be null if website cannot be loaded or if website is not from same origin
        return;
      }
      let location = doc.location.href;
      if (location === 'about:blank') {
        location = null;
      }
      this._setProperty('location', location);
    }
  }

  setScrollBarEnabled(scrollBarEnabled) {
    this.setProperty('scrollBarEnabled', scrollBarEnabled);
  }

  _renderScrollBarEnabled() {
    this.$container.toggleClass('no-scrolling', !this.scrollBarEnabled);
    // According to http://stackoverflow.com/a/18470016, setting 'overflow: hidden' via
    // CSS should be enough. However, if the inner page sets 'overflow' to another value,
    // scroll bars are shown again. Therefore, we add the legacy 'scrolling' attribute,
    // which is deprecated in HTML5, but seems to do the trick.
    this.$iframe.attr('scrolling', (this.scrollBarEnabled ? 'yes' : 'no'));

    // re-render location otherwise the attribute change would have no effect, see
    // https://html.spec.whatwg.org/multipage/embedded-content.html#attr-iframe-sandbox
    if (this.rendered) {
      this._renderLocation();
    }
  }

  setSandboxEnabled(sandboxEnabled) {
    this.setProperty('sandboxEnabled', sandboxEnabled);
  }

  _renderSandboxEnabled() {
    if (this.sandboxEnabled) {
      this._renderSandboxPermissions();
    } else {
      this.$iframe.removeAttr('sandbox');
      this.$iframe.removeAttr('security');
    }
    // re-render location otherwise the attribute change would have no effect, see
    // https://html.spec.whatwg.org/multipage/embedded-content.html#attr-iframe-sandbox
    if (this.rendered) {
      this._renderLocation();
    }
  }

  setSandboxPermissions(sandboxPermissions) {
    this.setProperty('sandboxPermissions', sandboxPermissions);
  }

  _renderSandboxPermissions() {
    if (!this.sandboxEnabled) {
      return;
    }
    this.$iframe.attr('sandbox', scout.nvl(this.sandboxPermissions, ''));
    // re-render location otherwise the attribute change would have no effect, see
    // https://html.spec.whatwg.org/multipage/embedded-content.html#attr-iframe-sandbox
    if (this.rendered) {
      this._renderLocation();
    }
  }
}
