/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, Table} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class TableSelectAllKeyStroke extends KeyStroke {
  declare field: Table;

  constructor(table: Table) {
    super();
    this.field = table;
    this.ctrl = true;
    this.which = [keys.A];
    this.renderingHints.offset = 14;
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      return this.field.footer ? this.field.footer._$infoSelection.find('.table-info-button') : null;
    };
    this.inheritAccessibility = false;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    let table = this.field;
    table.toggleSelection();
    table.selectionHandler.lastActionRow = null;
  }
}
