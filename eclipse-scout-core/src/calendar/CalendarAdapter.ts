/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Calendar, dates, Event, ModelAdapter} from '../index';
import {JsonDateRange} from '../util/dates';
import {CalendarComponentMoveEvent} from './CalendarEventMap';

export default class CalendarAdapter<C extends Calendar = Calendar> extends ModelAdapter<C> {

  constructor() {
    super();
  }

  /**
   * We must send the view-range to the client-model on the server. The view-range is determined by the UI.
   * Thus the calendar cannot be completely initialized without the view-range from the UI.
   */
  protected override _postCreateWidget() {
    this._sendViewRangeChange();
  }

  protected override _onWidgetEvent(event: Event<C>) {
    if (event.type === 'viewRangeChange') {
      this._sendViewRangeChange();
    } else if (event.type === 'modelChange') {
      this._sendModelChange();
    } else if (event.type === 'selectionChange') {
      this._sendSelectionChange();
    } else if (event.type === 'componentMove') {
      this._sendComponentMove(event as CalendarComponentMoveEvent<C>);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _jsonViewRange(): JsonDateRange {
    return dates.toJsonDateRange(this.widget.viewRange);
  }

  protected _jsonSelectedDate(): string {
    return dates.toJsonDate(this.widget.selectedDate);
  }

  protected _sendViewRangeChange() {
    this._send('viewRangeChange', {
      viewRange: this._jsonViewRange()
    });
  }

  protected _sendModelChange() {
    let data = {
      viewRange: this._jsonViewRange(),
      selectedDate: this._jsonSelectedDate(),
      displayMode: this.widget.displayMode
    };
    this._send('modelChange', data);
  }

  protected _sendSelectionChange() {
    let selectedComponentId = this.widget.selectedComponent ? this.widget.selectedComponent.id : null;
    this._send('selectionChange', {
      date: this._jsonSelectedDate(),
      componentId: selectedComponentId
    });
  }

  protected _sendComponentMove(event: CalendarComponentMoveEvent<C>) {
    this._send('componentMove', {
      componentId: event.component.id,
      fromDate: event.component.fromDate,
      toDate: event.component.toDate
    });
  }
}
