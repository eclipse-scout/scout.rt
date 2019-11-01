/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Widget} from '../index';
import {arrays} from '../index';

export default class PopupManager extends Widget {

constructor() {
  super();
  this.popups = [];
  this._addWidgetProperties(['popups']);
  this._addPreserveOnPropertyChangeProperties(['popups']);
}


_init(model) {
  super._init( model);
  this.session.layoutValidator.schedulePostValidateFunction(function() {
    this._openPopups(this.popups);
  }.bind(this));
}

setPopups(popups) {
  this.setProperty('popups', popups);
}

_setPopups(popups) {
  this._openPopups(arrays.diff(popups, this.popups));
  this._destroyPopups(arrays.diff(this.popups, popups));
  this._setProperty('popups', popups);
}

_openPopups(popups) {
  popups.forEach(function(popup) {
    popup.open(popup.session.$entryPoint);
  });
}

_destroyPopups(popups) {
  popups.forEach(function(popup) {
    popup.destroy();
  });
}
}
