/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, events, HtmlComponent, IFrameEventMap, IFrameModel, keys, scout, Widget} from '../index';

export class IFrame extends Widget implements IFrameModel {
  declare model: IFrameModel;
  declare eventMap: IFrameEventMap;
  declare self: IFrame;

  location: string;
  sandboxEnabled: boolean;
  sandboxPermissions: string;
  scrollBarEnabled: boolean;
  trackLocation: boolean;

  /**
   * Iframe on iOS is always as big as its content. Workaround it by using a wrapper div with overflow: auto
   * Don't wrap it when running in the chrome emulator (in that case isIosPlatform returns false)
   */
  wrapIframe: boolean;
  $iframe: JQuery<HTMLIFrameElement>;

  constructor() {
    super();

    this.location = null;
    this.sandboxEnabled = true;
    this.sandboxPermissions = null;
    this.scrollBarEnabled = true;
    this.trackLocation = false;
    this.wrapIframe = Device.get().isIosPlatform();
    this.$iframe = null;
  }

  protected override _render() {
    let cssClass = 'iframe ' + Device.get().cssClassForIphone();
    // Inserting an IFrame starts the processing of the micro task queue in Safari.
    // This must not happen during rendering because it could trigger render again for elements being rendered (some layouts render parts of the widget, e.g. widgets with virtual scrolling)
    this.session.layoutValidator.suppressValidate();
    if (this.wrapIframe) {
      this.$container = this.$parent.appendDiv('iframe-wrapper');
      this.$iframe = this.$container.appendElement('<iframe>', cssClass) as JQuery<HTMLIFrameElement>;
    } else {
      this.$iframe = this.$parent.appendElement('<iframe>', cssClass) as JQuery<HTMLIFrameElement>;
      this.$container = this.$iframe;
    }
    this.session.layoutValidator.unsuppressValidate();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this.$iframe.on('load', this._onLoad.bind(this));
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderScrollBarEnabled();
    this._renderSandboxEnabled(); // includes _renderSandboxPermissions()
    this._renderLocation(); // Needs to be after _renderScrollBarEnabled and _renderSandboxEnabled, see comment in _renderScrollBarEnabled
  }

  setLocation(location: string) {
    this.setProperty('location', location);
  }

  protected _renderLocation() {
    // Convert empty locations to 'about:blank', because in Firefox (maybe others, too?),
    // empty locations simply remove the src attribute but don't remove the old content.
    let location = this.location || 'about:blank';
    this.$iframe.attr('src', location);
  }

  setTrackLocation(trackLocation: boolean) {
    this.setProperty('trackLocation', trackLocation);
  }

  protected _contentDocument(): Document {
    if (this.$iframe && this.$iframe[0]) {
      return this.$iframe[0].contentDocument;
    }
    return null;
  }

  protected _onLoad(event: JQuery.TriggeredEvent) {
    if (!this.rendered) { // check needed, because this is an async callback
      return;
    }
    if (this.trackLocation) {
      this._updateLocation();
    }
    this._propagateKeyEvents();
  }

  protected _updateLocation() {
    let doc = this._contentDocument();
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

  /**
   * Make key strokes work even if pressed in the iframe
   */
  protected _propagateKeyEvents() {
    let source = this._contentDocument();
    if (!source) {
      return;
    }
    let target = (this.wrapIframe ? this.$container[0] : this.$parent[0]);
    if (!target) {
      return;
    }
    events.addPropagationListener(source, target, ['keydown', 'keyup', 'keypress'], (event: KeyboardEvent) => {
      // Don't propagate TAB key strokes otherwise it would break tabbing inside the document.
      return event.which !== keys.TAB;
    });
  }

  setScrollBarEnabled(scrollBarEnabled: boolean) {
    this.setProperty('scrollBarEnabled', scrollBarEnabled);
  }

  protected _renderScrollBarEnabled() {
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

  setSandboxEnabled(sandboxEnabled: boolean) {
    this.setProperty('sandboxEnabled', sandboxEnabled);
  }

  protected _renderSandboxEnabled() {
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

  /**
   * @param sandboxPermissions Permission names separated by space.
   * @see IFrame.sandboxPermissions
   */
  setSandboxPermissions(sandboxPermissions: string) {
    this.setProperty('sandboxPermissions', sandboxPermissions);
  }

  protected _renderSandboxPermissions() {
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

  postMessage(message: any, targetOrigin: string, transfer?: Transferable[]) {
    if (!this.rendered) {
      return;
    }
    this.$iframe[0].contentWindow.postMessage(message, targetOrigin, transfer);
  }
}
