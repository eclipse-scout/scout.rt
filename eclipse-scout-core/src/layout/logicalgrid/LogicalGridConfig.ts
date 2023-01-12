/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LogicalGridWidget, Widget} from '../../index';

export interface LogicalGridContainer extends Widget {
  gridColumnCount?: number;
  widgets?: LogicalGridWidget[];
}

export class LogicalGridConfig {
  widget: LogicalGridContainer;

  constructor() {
    this.widget = null;
  }

  setWidget(widget: LogicalGridContainer) {
    this.widget = widget;
  }

  getGridColumnCount(): number {
    return this.widget.gridColumnCount;
  }

  getGridWidgets(): LogicalGridWidget[] {
    return this.widget.widgets;
  }
}
