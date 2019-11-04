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
import {DesktopNotification, scout, Status} from '../../index';

export default class PopupBlockerDesktopNotification extends DesktopNotification {

  constructor() {
    super();
    this.duration = DesktopNotification.INFINITE;
    this.linkUrl;
    this.preserveOpener = false;
  }

  _init(model) {
    super._init(model);
    this.linkText = scout.nvl(this.linkText, this.session.text('ui.OpenManually'));
    this._setStatus({
      message: this.session.text('ui.PopupBlockerDetected'),
      severity: Status.Severity.WARNING
    });
  }

  _render() {
    super._render();

    this.$messageText.addClass('popup-blocked-title');
    this.$link = this.$content.appendElement('<a>', 'popup-blocked-link')
      .text(this.linkText)
      .on('click', this._onLinkClick.bind(this));
  }

  _renderProperties() {
    super._renderProperties();
    this._renderLinkUrl();
  }

  _renderLinkUrl() {
    if (this.linkUrl) {
      this.$link
        .attr('href', this.linkUrl)
        .attr('target', '_blank');
      if (!this.preserveOpener) {
        this.$link.attr('rel', 'noreferrer noopener');
      }
    } else {
      this.$link.removeAttr('href')
        .removeAttr('target');
    }
  }

  _onLinkClick() {
    this.trigger('linkClick');
    this.hide();
  }
}
