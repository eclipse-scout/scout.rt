/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, scout, ScoutKeyboardEvent, Table} from '../index';

/**
 * This {@link KeyStroke} executes the {@link Table#defaultRowAction} on ENTER if present and stops propagation of the event.
 */
export class TableDefaultRowActionKeyStroke extends KeyStroke {

  declare field: Table;

  constructor(table: Table) {
    super();

    this.field = scout.assertInstance(table, Table);
    this.which = [keys.ENTER];
    this.renderingHints.render = false;
    this.stopPropagation = true;
    this.stopImmediatePropagation = true;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    return super._accept(event) && !!this.field.defaultRowAction;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.defaultRowAction.doAction();
  }
}
