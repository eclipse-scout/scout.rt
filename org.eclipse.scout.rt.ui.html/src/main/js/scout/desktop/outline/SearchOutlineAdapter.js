/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.SearchOutlineAdapter = function() {
  scout.SearchOutlineAdapter.parent.call(this);
};
scout.inherits(scout.SearchOutlineAdapter, scout.OutlineAdapter);

scout.SearchOutlineAdapter.prototype._initProperties = function(model) {
  if (model.requestFocusQueryField !== undefined) {
    // ignore pseudo property initially (to prevent the function SearchOutlineAdapter#requestFocusQueryField() to be replaced)
    delete model.requestFocusQueryField;
  }
};

scout.SearchOutlineAdapter.prototype._syncRequestFocusQueryField = function() {
  this.widget.focusQueryField();
};

scout.SearchOutlineAdapter.prototype._onWidgetSearch = function(event) {
  this._send('search', {
    query: event.query
  }, {
    showBusyIndicator: false
  });
};

scout.SearchOutlineAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'search') {
    this._onWidgetSearch(event);
  } else {
    scout.SearchOutlineAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
