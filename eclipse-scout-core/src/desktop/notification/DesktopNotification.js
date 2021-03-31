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
import {Device, Notification as ScoutNotification, strings} from '../../index';

export default class DesktopNotification extends ScoutNotification {

  constructor() {
    super();
    this.closable = true;
    this.duration = 5000;
    this.removeTimeout = null;
    this._removing = false;
    this.nativeOnly = false;
    this.nativeNotificationVisibility = DesktopNotification.NativeNotificationVisibility.NONE;
    this.nativeNotification = null;
  }

  static NativeNotificationVisibility = {
    /**
     * No native notification is shown.
     */
    NONE: 'none',
    /**
     * The native notification is only shown if the application is in background.
     */
    BACKGROUND: 'background',
    /**
     * The native notification is always shown.
     */
    ALWAYS: 'always'
  };

  /**
   * When duration is set to INFINITE, the notification is not removed automatically.
   */
  static INFINITE = -1;

  _init(model) {
    super._init(model);
  }

  _render() {
    this._initNativeNotification();
    this.$container = this.$parent.prependDiv('desktop-notification');
    this.$content = this.$container.appendDiv('desktop-notification-content');
    this.$messageText = this.$content.appendDiv('desktop-notification-message');
    this.$loader = this.$content.appendDiv('desktop-notification-loader');

    if (Device.get().supportsCssAnimation()) {
      this.$loader.addClass('animated');
    }
    if (this.nativeOnly) {
      this.setVisible(false);
    }
  }

  /**
   *  @override
   */
  _renderLoading() {
    this.$container.toggleClass('loading', this.loading);
    this.$loader.setVisible(this.loading);
  }

  _isDocumentHidden() {
    return document.hidden;
  }

  _showNativeNotification(permission) {
    if (permission === 'denied' || permission === 'default') {
      return;
    }
    const message = scout.nvl(strings.nl2br(this.status.message), '');
    this.nativeNotification = new Notification(this.session.desktop.title, {
      body: message,
      icon: this.session.desktop.logoUrl
    });

    this.nativeNotification.onclick = event => {
      window.focus();
    };

    this.nativeNotification.onclose = event => {
      if (this.nativeOnly) {
        this.destroy();
      }
    };

    // if nativeOnly and duration = forever, remove notification
    if (this.nativeOnly && this.duration <= 0) {
      this.hide();
      this.destroy();
    }
    if (this.duration > 0) {
      setTimeout(this.nativeNotification.close.bind(this.nativeNotification), this.duration);
    }
  }

  _initNativeNotification() {
    if (this.nativeNotificationVisibility === DesktopNotification.NativeNotificationVisibility.NONE) {
      return;
    }

    if (this.nativeNotificationVisibility === DesktopNotification.NativeNotificationVisibility.BACKGROUND && !this._isDocumentHidden()) {
      return;
    }

    if (window.Notification && Notification.permission !== 'denied') {
      if (this._checkNotificationPromise()) {
        Notification.requestPermission().then(this._showNativeNotification.bind(this));
      } else {
        Notification.requestPermission(this._showNativeNotification.bind(this));
      }
    }
  }

  /**
   * check if browser supports the promise-based version of the method requestPermission. Safari only supports the older callback version.
   */
  _checkNotificationPromise() {
    try {
      Notification.requestPermission().then();
    } catch (e) {
      return false;
    }
    return true;
  }

  _onCloseIconClick() {
    this.hide();
  }

  show() {
    this.session.desktop.addNotification(this);
  }

  hide() {
    if (this._removing) {
      return;
    }
    this.trigger('close');
    this.session.desktop.removeNotification(this);
  }

  fadeIn($parent) {
    this.render($parent);
    if (!Device.get().supportsCssAnimation()) {
      return;
    }
    this.$container.addClassForAnimation('desktop-notification-slide-in');
  }

  fadeOut() {
    if (!Device.get().supportsCssAnimation()) {
      this.destroy();
      return;
    }

    if (!this.rendered) {
      return;
    }
    // prevent fadeOut from running more than once (for instance from the click of a user).
    if (this._removing) {
      return;
    }
    this._removing = true;
    this.$container.addClass('desktop-notification-fade-out');
    this.$container.oneAnimationEnd(() => {
      this.destroy();
    });
  }

  /**
   * @override
   */
  invalidateLayoutTree() {
    // called by notification.js. Since desktop notification has no htmlComp, no need to invalidate
  }
}
