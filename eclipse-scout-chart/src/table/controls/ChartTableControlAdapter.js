/*
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TableControlAdapter} from '@eclipse-scout/core';

export default class ChartTableControlAdapter extends TableControlAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['chartType', 'chartAggregation', 'chartGroup1', 'chartGroup2']);
  }

}
