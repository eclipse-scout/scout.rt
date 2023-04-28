/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CalendarResourceDo, LookupRow, StaticLookupCall} from '../index';

export class CalendarResourceLookupCall extends StaticLookupCall<string> {
  resources: CalendarResourceDo[];

  constructor() {
    super();
    this.resources = [];
  }

  setResources(resources: CalendarResourceDo[]) {
    this.resources = resources;
    this.refreshData();
  }

  protected override _data(): any[] {
    return this.resources.map(resource =>
      [resource.resourceId, resource.name, resource.parentId, resource.cssClass]
    );
  }

  protected override _dataToLookupRow(data: any[], index?: number): LookupRow<string> {
    let lookupRow = super._dataToLookupRow(data, index);
    lookupRow.cssClass = data[3];
    return lookupRow;
  }
}
