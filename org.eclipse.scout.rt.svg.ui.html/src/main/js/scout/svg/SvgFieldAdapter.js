/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.SvgFieldAdapter = function() {
  scout.SvgFieldAdapter.parent.call(this);
};
scout.inherits(scout.SvgFieldAdapter, scout.ValueFieldAdapter);

scout.SvgFieldAdapter.prototype._onWidgetAppLinkAction = function(event) {
  this._send('appLinkAction', {
    ref: event.ref
  });
};

scout.SvgFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'appLinkAction') {
    this._onWidgetAppLinkAction(event);
  } else {
    scout.SvgFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
