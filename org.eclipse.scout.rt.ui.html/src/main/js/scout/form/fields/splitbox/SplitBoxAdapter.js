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
scout.SplitBoxAdapter = function() {
  scout.SplitBoxAdapter.parent.call(this);
  this._addAdapterProperties(['firstField', 'secondField', 'collapsibleField']);
  this._addRemoteProperties(['collapsibleField', 'fieldCollapsed']);
};
scout.inherits(scout.SplitBoxAdapter, scout.CompositeFieldAdapter);

scout.SplitBoxAdapter.prototype._onWidgetPositionChange = function(event) {
  this._send('setSplitterPosition', {
    splitterPosition: event.position
  });
};

scout.SplitBoxAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'positionChange') {
    this._onWidgetPositionChange(event);
  } else {
    scout.SplitBoxAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
