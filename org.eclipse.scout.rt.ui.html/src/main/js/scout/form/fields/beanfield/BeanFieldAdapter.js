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
scout.BeanFieldAdapter = function() {
  scout.BeanFieldAdapter.parent.call(this);
};
scout.inherits(scout.BeanFieldAdapter, scout.ValueFieldAdapter);

scout.BeanFieldAdapter.prototype._onWidgetAppLinkAction = function(event) {
  this._send('appLinkAction', {
    ref: event.ref
  });
};

scout.BeanFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'appLinkAction') {
    this._onWidgetAppLinkAction(event);
  } else {
    scout.BeanFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
