/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.MessageBoxAdapter = function() {
  scout.MessageBoxAdapter.parent.call(this);
};
scout.inherits(scout.MessageBoxAdapter, scout.ModelAdapter);

scout.MessageBoxAdapter.prototype._onWidgetAction = function(event) {
  this._send('action', {
    option: event.option
  });
};

scout.MessageBoxAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'action') {
    this._onWidgetAction(event);
  } else {
    scout.MessageBoxAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
