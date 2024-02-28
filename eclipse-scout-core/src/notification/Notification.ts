/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlComponent, Icon, InitModelOf, NotificationEventMap, NotificationModel, scout, Status, StatusOrModel, strings, texts, Widget} from '../index';
import $ from 'jquery';

export class Notification extends Widget implements NotificationModel {
  declare model: NotificationModel;
  declare eventMap: NotificationEventMap;
  declare self: Notification;

  status: Status;
  closable: boolean;
  htmlEnabled: boolean;
  $content: JQuery;
  $messageText: JQuery;
  $closer: JQuery;

  protected _icon: Icon;

  constructor() {
    super();
    this.status = Status.info();
    this.closable = false;
    this.htmlEnabled = false;
    this._icon = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    // this allows to set the properties severity and message directly on the model object
    // without having a status object. because it's more convenient when you must create
    // a notification programmatically.
    if (model.severity || model.message || model.iconId) {
      this.status = new Status({
        severity: scout.nvl(model.severity, this.status.severity),
        message: scout.nvl(model.message, this.status.message),
        iconId: scout.nvl(model.iconId, this.status.iconId)
      });
    }
    texts.resolveTextProperty(this.status, 'message', this.session);
    this._setStatus(this.status);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('notification alternative'); // Alternative is the new default.
    this.$content = this.$container.appendDiv('notification-content');
    this.$messageText = this.$content.appendDiv('notification-message');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.pixelBasedSizing = false;
  }

  protected override _remove() {
    super._remove();
    this._removeCloser();
    this._removeIcon();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderStatus();
    this._renderClosable();
  }

  setStatus(status: StatusOrModel) {
    this.setProperty('status', status);
  }

  protected _setStatus(status: StatusOrModel) {
    if (this.rendered) {
      this._removeStatus();
    }
    status = Status.ensure(status) || Status.info();
    this._setProperty('status', status);
  }

  protected _removeStatus() {
    if (this.status) {
      this.$container.removeClass(this.status.cssClass());
    }
  }

  protected _renderStatus() {
    if (this.status) {
      this.$container.addClass(this.status.cssClass());
      this._renderIconId();
    }
    this._renderMessage();
  }

  protected _renderMessage() {
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

  setIconId(iconId: string) {
    if (!this.status) {
      this.status = Status.info();
    }
    this.status.iconId = iconId;
    if (this.rendered) {
      this._renderStatus();
    }
  }

  protected _renderIconId() {
    let hasIcon = this.status && !!this.status.iconId;
    this.$container.toggleClass('has-icon', hasIcon);
    this.$container.toggleClass('no-icon', !hasIcon);
    if (hasIcon) {
      this._renderIcon();
    } else {
      this._removeIcon();
    }
  }

  protected _renderIcon() {
    if (this._icon) {
      this._icon.setIconDesc(this.status.iconId);
      return;
    }

    this._icon = scout.create(Icon, {
      parent: this,
      iconDesc: this.status.iconId,
      prepend: true
    });
    this._icon.one('destroy', () => {
      this._icon = null;
    });
    this._icon.render();
    this._icon.$container.addClass('notification-icon');
  }

  protected _removeIcon() {
    if (this._icon) {
      this._icon.destroy();
    }
  }

  setClosable(closable: boolean) {
    this.setProperty('closable', closable);
  }

  protected _renderClosable() {
    this.$container.toggleClass('closable', this.closable);
    this.$content.toggleClass('closable', this.closable);
    if (!this.closable) {
      this._removeCloser();
    } else {
      this._renderCloser();
    }
  }

  protected _removeCloser() {
    if (!this.$closer) {
      return;
    }
    this.$closer.remove();
    this.$closer = null;
  }

  protected _renderCloser() {
    if (this.$closer) {
      return;
    }
    this.$closer = this.$content
      .appendDiv('closer')
      .on('click', this._onCloseIconClick.bind(this));
  }

  protected _onCloseIconClick(event: JQuery.ClickEvent) {
    if (this.removing) {
      return;
    }
    this.trigger('close');
  }

  protected _onAppLinkAction(event: JQuery.ClickEvent) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref') as string;
    this.triggerAppLinkAction(ref);
  }

  triggerAppLinkAction(ref: string) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }
}
