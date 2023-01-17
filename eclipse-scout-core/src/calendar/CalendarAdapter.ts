/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Calendar, CalendarComponentMoveEvent, dates, Event, JsonDateRange, ModelAdapter} from '../index';

export class CalendarAdapter extends ModelAdapter {
  declare widget: Calendar;

  /**
   * We must send the view-range to the client-model on the server. The view-range is determined by the UI.
   * Thus, the calendar cannot be completely initialized without the view-range from the UI.
   * @internal
   */
  override _postCreateWidget() {
    this._sendViewRangeChange();
  }

  protected override _onWidgetEvent(event: Event<Calendar>) {
    if (event.type === 'viewRangeChange') {
      this._sendViewRangeChange();
    } else if (event.type === 'modelChange') {
      this._sendModelChange();
    } else if (event.type === 'selectionChange') {
      this._sendSelectionChange();
    } else if (event.type === 'componentMove') {
      this._sendComponentMove(event as CalendarComponentMoveEvent);
    } else if (event.type === 'selectedRangeChange') {
      this._sendSelectedRangeChange();
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _jsonViewRange(): JsonDateRange {
    return dates.toJsonDateRange(this.widget.viewRange);
  }

  protected _jsonSelectedRange(): JsonDateRange {
    if (!this.widget.selectedRange) {
      return null;
    }
    return dates.toJsonDateRange(this.widget.selectedRange);
  }

  protected _jsonSelectedDate(): string {
    return dates.toJsonDate(this.widget.selectedDate);
  }

  protected _sendViewRangeChange() {
    this._send('viewRangeChange', {
      viewRange: this._jsonViewRange()
    });
  }

  _sendSelectedRangeChange() {
    this._send('selectedRangeChange', {
      selectedRange: this._jsonSelectedRange()
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

  protected _sendComponentMove(event: CalendarComponentMoveEvent) {
    this._send('componentMove', {
      componentId: event.component.id,
      fromDate: event.component.fromDate,
      toDate: event.component.toDate
    });
  }
}
