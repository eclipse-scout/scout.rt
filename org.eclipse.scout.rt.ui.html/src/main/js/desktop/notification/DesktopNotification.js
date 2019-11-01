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
import {strings} from '../../index';
import {Notification} from '../../index';
import {Device} from '../../index';
import * as $ from 'jquery';

export default class DesktopNotification extends Notification {

constructor() {
  super();
  this.closable = true;
  this.duration = 5000;
  this.htmlEnabled = false;
  this.removeTimeout;
  this._removing = false;
}


/**
 * When duration is set to INFINITE, the notification is not removed automatically.
 */
static INFINITE = -1;

_init(model) {
  super._init( model);
}

_render() {
  this.$container = this.$parent.prependDiv('desktop-notification');
  this.$content = this.$container.appendDiv('desktop-notification-content');
  this.$messageText = this.$content.appendDiv('desktop-notification-message');
  this.$loader = this.$content.appendDiv('desktop-notification-loader');

  if (Device.get().supportsCssAnimation()) {
    this.$loader.addClass('animated');
  }
}

_remove() {
  super._remove();
  this._removeCloser();
}

_renderProperties() {
  super._renderProperties();
  this._renderClosable();
}

_renderMessage() {
  var message = this.status.message || '';
  if (this.htmlEnabled) {
    this.$messageText.html(message);
    // Add action to app-links
    this.$messageText.find('.app-link')
      .on('click', this._onAppLinkAction.bind(this));
  } else {
    this.$messageText.html(strings.nl2br(message));
  }
}

/**
 * @override
 */
_renderLoading() {
  this.$container.toggleClass('loading', this.loading);
  this.$loader.setVisible(this.loading);
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
  this.$container.oneAnimationEnd(function() {
    this.destroy();
  }.bind(this));
}

/**
 * @override
 */
invalidateLayoutTree() {
  // called by notification.js. Since desktop notification has no htmlComp, no need to invalidate
}

_onAppLinkAction(event) {
  var $target = $(event.delegateTarget);
  var ref = $target.data('ref');
  this.triggerAppLinkAction(ref);
}

triggerAppLinkAction(ref) {
  this.trigger('appLinkAction', {
    ref: ref
  });
}
}
