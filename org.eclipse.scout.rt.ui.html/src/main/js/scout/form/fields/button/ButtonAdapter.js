/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ButtonAdapter = function() {
  scout.ButtonAdapter.parent.call(this);
  this._addRemoteProperties(['selected']);
};
scout.inherits(scout.ButtonAdapter, scout.FormFieldAdapter);

scout.ButtonAdapter.prototype._onWidgetClick = function(event) {
  this._send('click');
};

scout.ButtonAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'click') {
    this._onWidgetClick(event);
  } else {
    scout.ButtonAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
