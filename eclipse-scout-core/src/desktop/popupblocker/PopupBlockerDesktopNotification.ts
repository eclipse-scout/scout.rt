/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DesktopNotification, InitModelOf, PopupBlockerDesktopNotificationEventMap, PopupBlockerDesktopNotificationModel, scout, Status} from '../../index';

export class PopupBlockerDesktopNotification extends DesktopNotification implements PopupBlockerDesktopNotificationModel {
  declare model: PopupBlockerDesktopNotificationModel;
  declare eventMap: PopupBlockerDesktopNotificationEventMap;
  declare self: PopupBlockerDesktopNotification;

  preserveOpener: boolean;
  linkUrl: string;
  linkText: string;
  $link: JQuery<HTMLAnchorElement>;

  constructor() {
    super();
    this.duration = DesktopNotification.INFINITE;
    this.preserveOpener = false;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.linkText = scout.nvl(this.linkText, this.session.text('ui.OpenManually'));
    this._setStatus({
      message: this.session.text('ui.PopupBlockerDetected'),
      severity: Status.Severity.WARNING
    });
  }

  protected override _render() {
    super._render();

    this.$messageText.addClass('popup-blocked-title');
    this.$link = this.$content.appendElement('<a>', 'popup-blocked-link')
      .text(this.linkText)
      .on('click', this._onLinkClick.bind(this)) as JQuery<HTMLAnchorElement>;
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderLinkUrl();
  }

  protected _renderLinkUrl() {
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

  protected _onLinkClick(event: JQuery.ClickEvent) {
    this.trigger('linkClick');
    this.hide();
  }
}
