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
scout.CalendarAdapter = function() {
  scout.CalendarAdapter.parent.call(this);
};
scout.inherits(scout.CalendarAdapter, scout.ModelAdapter);

/**
 * We must send the view-range to the client-model on the server. The view-range is determined by the UI.
 * Thus the calendar cannot be completely initialized without the view-range from the UI.
 * @override ModelAdapter.js
 */
scout.CalendarAdapter.prototype._postCreateWidget = function() {
  this._sendViewRangeChange();
};

/**
 * @override ModelAdapter.js
 */
scout.CalendarAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'viewRangeChange') {
    this._sendViewRangeChange();
  } else if (event.type === 'modelChange') {
    this._sendModelChange();
  } else if (event.type === 'selectionChange') {
    this._sendSelectionChange();
  } else {
    scout.CalendarAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.CalendarAdapter.prototype._jsonViewRange = function() {
  return scout.dates.toJsonDateRange(this.widget.viewRange);
};

scout.CalendarAdapter.prototype._jsonSelectedDate = function() {
  return scout.dates.toJsonDate(this.widget.selectedDate);
};

scout.CalendarAdapter.prototype._sendViewRangeChange = function() {
  this._send('viewRangeChange', {
    viewRange: this._jsonViewRange()
  });
};

scout.CalendarAdapter.prototype._sendModelChange = function() {
  var data = {
    viewRange: this._jsonViewRange(),
    selectedDate: this._jsonSelectedDate(),
    displayMode: this.widget.displayMode
  };
  this._send('modelChange', data);
};

scout.CalendarAdapter.prototype._sendSelectionChange = function() {
  var selectedComponentId = this.widget.selectedComponent ? this.widget.selectedComponent.id : null;
  this._send('selectionChange', {
    date: this._jsonSelectedDate(),
    componentId: selectedComponentId
  });
};
