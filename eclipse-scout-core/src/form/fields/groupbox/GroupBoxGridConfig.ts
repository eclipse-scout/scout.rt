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
import {GroupBox, LogicalGridConfig, LogicalGridWidget, scout} from '../../../index';

export class GroupBoxGridConfig extends LogicalGridConfig {
  declare widget: GroupBox;

  override getGridWidgets(): LogicalGridWidget[] {
    return this.widget.controls;
  }

  override getGridColumnCount(): number {
    let gridColumns = -1,
      widget = this.widget;

    do {
      gridColumns = scout.nvl(widget.gridColumnCount, gridColumns);
    } while (gridColumns < 0 && widget.getParentGroupBox && (widget = widget.getParentGroupBox()));

    return gridColumns < 0 ? 2 : gridColumns;
  }
}
