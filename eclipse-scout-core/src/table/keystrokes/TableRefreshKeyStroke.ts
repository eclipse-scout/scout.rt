/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ScoutKeyboardEvent, Table} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class TableRefreshKeyStroke extends KeyStroke {
  declare field: Table;

  constructor(table: Table) {
    super();
    this.field = table;
    this.which = [keys.F5];
    this.renderingHints.offset = 14;
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      return this.field.footer ? this.field.footer._$infoLoad.find('.table-info-button') : null;
    };
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    return accepted && this.field.hasReloadHandler;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.reload();
  }
}
