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
