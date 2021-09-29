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
import {dates, ModelAdapter} from '../index';

export default class CalendarAdapter extends ModelAdapter {

  constructor() {
    super();
  }

  /**
   * We must send the view-range to the client-model on the server. The view-range is determined by the UI.
   * Thus the calendar cannot be completely initialized without the view-range from the UI.
   * @override ModelAdapter.js
   */
  _postCreateWidget() {
    this._sendViewRangeChange();
  }

  /**
   * @override ModelAdapter.js
   */
  _onWidgetEvent(event) {
    if (event.type === 'viewRangeChange') {
      this._sendViewRangeChange();
    } else if (event.type === 'modelChange') {
      this._sendModelChange();
    } else if (event.type === 'selectionChange') {
      this._sendSelectionChange();
    } else if (event.type === 'componentMove') {
      this._sendComponentMove(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  _jsonViewRange() {
    return dates.toJsonDateRange(this.widget.viewRange);
  }

  _jsonSelectedDate() {
    return dates.toJsonDate(this.widget.selectedDate);
  }

  _sendViewRangeChange() {
    this._send('viewRangeChange', {
      viewRange: this._jsonViewRange()
    });
  }

  _sendModelChange() {
    let data = {
      viewRange: this._jsonViewRange(),
      selectedDate: this._jsonSelectedDate(),
      displayMode: this.widget.displayMode
    };
    this._send('modelChange', data);
  }

  _sendSelectionChange() {
    let selectedComponentId = this.widget.selectedComponent ? this.widget.selectedComponent.id : null;
    this._send('selectionChange', {
      date: this._jsonSelectedDate(),
      componentId: selectedComponentId
    });
  }

  _sendComponentMove(event) {
    this._send('componentMove', {
      componentId: event.component.id,
      fromDate: event.component.fromDate,
      toDate: event.component.toDate
    });
  }
}
