/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AbstractTableNavigationKeyStroke, keys, ScoutKeyboardEvent, Table} from '../..';

export class TableSelectKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.SPACE, keys.ENTER];
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent) => {
      return this.field.focusedRow?.$row;
    };
    this.stopPropagation = true;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    return this.field.focusedRow && !this.field.isRowSelected(this.field.focusedRow);
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.selectRows(this.field.focusedRow);
    if (event.which !== keys.SPACE) {
      // immediatePropagation is allowed for space to propagate it to TableToggleRowKeyStroke so pressing space selects and checks at once
      event.stopImmediatePropagation();
    }
  }
}
