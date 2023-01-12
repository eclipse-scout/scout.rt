/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GridData, LogicalGridWidget} from '../../../index';

export class LogicalGridMatrixCell {
  widget: LogicalGridWidget;
  data: GridData;

  constructor(widget?: LogicalGridWidget, data?: GridData) {
    this.widget = widget;
    this.data = data;
  }

  isEmpty(): boolean {
    return !this.widget;
  }
}
