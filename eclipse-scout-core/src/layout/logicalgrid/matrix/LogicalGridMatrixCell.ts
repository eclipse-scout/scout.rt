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
import {LogicalGridWidget} from '../LogicalGridData';
import {GridData} from '../../../index';

export default class LogicalGridMatrixCell {
  widget: LogicalGridWidget;
  data: GridData;

  constructor(widget?: LogicalGridWidget, data?) {
    this.widget = widget;
    this.data = data;
  }

  isEmpty(): boolean {
    return !this.widget;
  }
}
