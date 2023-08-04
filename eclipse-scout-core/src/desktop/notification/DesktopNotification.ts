/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, DesktopNotificationEventMap, DesktopNotificationModel, Device, EnumObject, InitModelOf, Notification as ScoutNotification, scout, Status, StatusOrModel, strings} from '../../index';

export class DesktopNotification extends ScoutNotification implements DesktopNotificationModel {
  declare model: DesktopNotificationModel;
  declare eventMap: DesktopNotificationEventMap;
  declare self: DesktopNotification;

  duration: number;
  removeTimeout: number;
  nativeOnly: boolean;
  nativeNotificationTitle: string;
  nativeNotificationStatus: Status;
  nativeNotificationVisibility: NativeNotificationVisibility;
  nativeNotification: Notification;
  nativeNotificationShown: boolean;
  $loader: JQuery;
  protected _removing: boolean;

  constructor() {
    super();
    this.closable = true;
    this.duration = 5000;
    this.removeTimeout = null;
    this._removing = false;
    this.nativeOnly = false;
    this.nativeNotificationTitle = null;
    this.nativeNotificationStatus = null;
    this.nativeNotificationVisibility = DesktopNotification.NativeNotificationVisibility.NONE;
    this.nativeNotification = null;
    this.nativeNotificationShown = false;
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
  } as const;

  /**
   * When duration is set to INFINITE, the notification is not removed automatically.
   */
  static INFINITE = -1;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    let defaults = this.session.desktop.nativeNotificationDefaults;
    if (defaults) {
      this.nativeNotificationTitle = model.nativeNotificationTitle !== undefined ? model.nativeNotificationTitle : defaults.title;
      if (this.nativeNotificationStatus) {
        this.nativeNotificationStatus.iconId = this.nativeNotificationStatus.iconId !== undefined ? this.nativeNotificationStatus.iconId : defaults.iconId;
      } else {
        this.nativeNotificationStatus = new Status({
          iconId: defaults.iconId
        });
      }
      this.nativeNotificationVisibility = scout.nvl(model.nativeNotificationVisibility !== undefined ? model.nativeNotificationVisibility : defaults.visibility, DesktopNotification.NativeNotificationVisibility.NONE);
    }
    this.resolveTextKeys(['nativeNotificationTitle']);
  }

  protected override _render() {
    this._initNativeNotification();
    this.$container = this.$parent.prependDiv('desktop-notification');
    this.$content = this.$container.appendDiv('desktop-notification-content');
    this.$messageText = this.$content.appendDiv('desktop-notification-message');
    this.$loader = this.$container.appendDiv('desktop-notification-loader');

    aria.role(this.$container, 'alert'); // Read the notification to screen reader users

    if (Device.get().supportsCssAnimation()) {
      this.$loader.addClass('animated');
    }
    if (this.nativeOnly) {
      this.setVisible(false);
    }
  }

  protected override _renderLoading() {
    this.$container.toggleClass('loading', this.loading);
    this.$loader.setVisible(this.loading);
  }

  protected override _destroy() {
    if (this.nativeNotification) {
      // No need to keep the native notification open if the regular one is closed (relevant if the user actively closes it)
      this.nativeNotification.close();
    }
    super._destroy();
  }

  /** @internal */
  _isDocumentHidden(): boolean {
    return document.hidden;
  }

  protected _showNativeNotification(permission: NotificationPermission) {
    if (permission === 'denied' || permission === 'default') {
      if (this.nativeOnly) {
        // See comment in _initNativeNotification
        this.hide();
      }
      return;
    }
    let title = scout.nvl(this.nativeNotificationTitle, '');
    let body = (this.nativeNotificationStatus || {}).message;
    if (strings.empty(body)) {
      body = (this.status || {}).message;
    }
    if (!body) {
      body = '';
    }
    if (this.htmlEnabled) {
      body = strings.plainText(body, {removeFontIcons: true});
    }
    let iconId = (this.nativeNotificationStatus || {}).iconId;
    if (strings.empty(iconId)) {
      // icon must not be null or empty. If no icon it must be undefined
      iconId = undefined;
    }
    this.nativeNotification = new Notification(title, {
      body: body,
      icon: iconId
    });

    this.nativeNotification.addEventListener('show', event => {
      this._setNativeNotificationShown(true);
    });

    this.nativeNotification.addEventListener('click', event => {
      window.focus();
    });

    // Native notifications are closed when the regular notification is closed (either by the user, the timeout or programmatically)
    this.nativeNotification.addEventListener('close', event => {
      if (this.nativeOnly) {
        // Only close it if nativeOnly is true.
        // If nativeOnly is false, clicking the notification should reveal the app incl. the original notification which could contain more information (e.g. a link).
        this.hide();
      }
      this.nativeNotification = null;
      this._setNativeNotificationShown(false);
    });
  }

  protected _initNativeNotification() {
    if (this.nativeNotificationShown) {
      // Don't show the same notification twice (could happen if the user reloads the page and the notification is still open. Especially important for nativeOnly with infinite duration).
      return;
    }
    if (this.nativeNotificationVisibility === DesktopNotification.NativeNotificationVisibility.NONE) {
      this._hideLaterIfNativeOnly();
      return;
    }

    if (this.nativeNotificationVisibility === DesktopNotification.NativeNotificationVisibility.BACKGROUND && !this._isDocumentHidden()) {
      this._hideLaterIfNativeOnly();
      return;
    }

    if (!window.Notification || Notification.permission === 'denied') {
      this._hideLaterIfNativeOnly();
      return;
    }
    if (this._checkNotificationPromise()) {
      Notification.requestPermission().then(this._showNativeNotification.bind(this));
    } else {
      // noinspection JSIgnoredPromiseFromCall
      Notification.requestPermission(this._showNativeNotification.bind(this));
    }
  }

  protected _hideLaterIfNativeOnly() {
    if (!this.nativeOnly) {
      return;
    }
    // If native notifications are not shown, there is no need to keep the (invisible) desktop notification open (prevent dom-leak)
    setTimeout(() => this.hide()); // async because this method is called in render and removing the notification within render throws exception
  }

  /**
   * Checks if browser supports the promise-based version of the method requestPermission. Safari only supports the older callback version.
   */
  protected _checkNotificationPromise(): boolean {
    try {
      Notification.requestPermission().then();
    } catch (e) {
      return false;
    }
    return true;
  }

  protected override _onCloseIconClick() {
    this.hide();
  }

  /**
   * Displays the notification by adding it to the desktop and rendering it.
   */
  show() {
    this.session.desktop.addNotification(this);
  }

  /**
   * Closes the notification by removing it from the desktop and destroying it. Also triggers a close event.
   */
  hide() {
    if (this._removing) {
      return;
    }
    this.trigger('close');
    this.session.desktop.removeNotification(this);
  }

  fadeIn($parent: JQuery) {
    this.render($parent);
    if (!Device.get().supportsCssAnimation()) {
      return;
    }
    this.$container.addClassForAnimation('desktop-notification-slide-in');
  }

  fadeOut() {
    // prevent fadeOut from running more than once (for instance from the click of a user).
    if (this._removing) {
      return;
    }
    this._removing = true;
    if (!Device.get().supportsCssAnimation() || !this.rendered) {
      this.destroy();
      return;
    }
    if (!this.$container.isVisible()) {
      // Destroy immediately if it is invisible because the animationend event would not be triggered (is the case if nativeOnly is true)
      this.destroy();
      return;
    }
    this.$container.addClass('desktop-notification-fade-out');
    this.$container.oneAnimationEnd(() => {
      this.destroy();
    });
  }

  override invalidateLayoutTree() {
    // called by notification.js. Since desktop notification has no htmlComp, no need to invalidate
  }

  protected _setNativeNotificationShown(shown: boolean) {
    this._setProperty('nativeNotificationShown', shown);
  }

  setNativeNotificationTitle(title: string) {
    this.setProperty('nativeNotificationTitle', title);
  }

  setNativeNotificationStatus(status: StatusOrModel) {
    this.setProperty('nativeNotificationStatus', status);
  }

  protected _setNativeNotificationStatus(status: StatusOrModel) {
    status = Status.ensure(status);
    this._setProperty('nativeNotificationStatus', status);
  }

  setNativeNotificationVisibility(visibility: NativeNotificationVisibility) {
    this.setProperty('nativeNotificationVisibility', visibility);
  }
}

export type NativeNotificationVisibility = EnumObject<typeof DesktopNotification.NativeNotificationVisibility>;
