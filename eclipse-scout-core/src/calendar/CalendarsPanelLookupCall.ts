/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CalendarDescriptor, LookupRow, StaticLookupCall} from '../index';

export class CalendarsPanelLookupCall extends StaticLookupCall<string> {
  calendars: CalendarDescriptor[];

  constructor() {
    super();
    this.calendars = [];
  }

  setCalendars(calendars: CalendarDescriptor[]) {
    this.calendars = calendars;
    this.refreshData();
  }

  protected override _data(): any[] {
    return this.calendars.map(calendar =>
      [calendar.calendarId, calendar.name, calendar.parentId, calendar.cssClass]
    );
  }

  protected override _dataToLookupRow(data: any[], index?: number): LookupRow<string> {
    let lookupRow = super._dataToLookupRow(data, index);
    lookupRow.cssClass = data[3];
    return lookupRow;
  }
}
