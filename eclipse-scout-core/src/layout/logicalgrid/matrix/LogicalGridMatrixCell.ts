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
