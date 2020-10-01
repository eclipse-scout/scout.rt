/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, scout, Status, strings, texts, Widget} from '../index';
import $ from 'jquery';

export default class Notification extends Widget {

  constructor() {
    super();
    this.status = Status.info();
    this.closable = false;
    this.htmlEnabled = false;
  }

  _init(model) {
    super._init(model);

    // this allows to set the properties severity and message directly on the model object
    // without having a status object. because it's more convenient when you must create
    // a notification programmatically.
    if (model.severity || model.message) {
      this.status = new Status({
        severity: scout.nvl(model.severity, this.status.severity),
        message: scout.nvl(model.message, this.status.message)
      });
    }
    texts.resolveTextProperty(this.status, 'message', this.session);
    this._setStatus(this.status);
  }

  _render() {
    this.$container = this.$parent.appendDiv('notification');
    this.$content = this.$container.appendDiv('notification-content');
    this.$messageText = this.$content.appendDiv('notification-message');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  _remove() {
    super._remove();
    this._removeCloser();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderStatus();
    this._renderClosable();
  }

  setStatus(status) {
    this.setProperty('status', status);
  }

  _setStatus(status) {
    if (this.rendered) {
      this._removeStatus();
    }
    status = Status.ensure(status);
    this._setProperty('status', status);
  }

  _removeStatus() {
    if (this.status) {
      this.$container.removeClass(this.status.cssClass());
    }
  }

  _renderStatus() {
    if (this.status) {
      this.$container.addClass(this.status.cssClass());
    }
    this._renderMessage();
  }

  _renderMessage() {
    let message = this.status.message || '';
    if (this.htmlEnabled) {
      this.$messageText.html(message);
      // Add action to app-links
      this.$messageText.find('.app-link')
        .on('click', this._onAppLinkAction.bind(this));
    } else {
      this.$messageText.html(strings.nl2br(message));
    }
    this.invalidateLayoutTree();
  }

  setClosable(closable) {
    this.setProperty('closable', closable);
  }

  _renderClosable() {
    this.$content.toggleClass('closable', this.closable);
    if (!this.closable) {
      this._removeCloser();
    } else {
      this._renderCloser();
    }
  }

  _removeCloser() {
    if (!this.$closer) {
      return;
    }
    this.$closer.remove();
    this.$closer = null;
  }

  _renderCloser() {
    if (this.$closer) {
      return;
    }
    this.$closer = this.$content
      .appendDiv('closer')
      .on('click', this._onCloseIconClick.bind(this));
  }

  _onCloseIconClick() {
    if (this._removing) {
      return;
    }
    this.trigger('close');
  }

  _onAppLinkAction(event) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref');
    this.triggerAppLinkAction(ref);
  }

  triggerAppLinkAction(ref) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }
}
