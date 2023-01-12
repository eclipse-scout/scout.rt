/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
