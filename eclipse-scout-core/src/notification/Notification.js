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
import {texts} from '../index';
import {HtmlComponent} from '../index';
import {Status} from '../index';
import {strings} from '../index';
import {Widget} from '../index';
import {scout} from '../index';

export default class Notification extends Widget {

constructor() {
  super();
  this.status = Status.info();
}


_init(model) {
  super._init( model);

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
  this.htmlComp = HtmlComponent.install(this.$container, this.session);
}

_renderProperties() {
  super._renderProperties();

  this._renderStatus();
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
  var message = scout.nvl(strings.nl2br(this.status.message), '');
  this.$container.html(message);
  this.invalidateLayoutTree();
}
}
