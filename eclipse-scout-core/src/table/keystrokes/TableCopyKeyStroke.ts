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
import {keys, KeyStroke, Table} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class TableCopyKeyStroke extends KeyStroke {
  declare field: Table;

  constructor(table: Table) {
    super();
    this.field = table;
    this.which = [keys.C];
    this.ctrl = true;
    this.renderingHints.render = false;
    this.inheritAccessibility = false;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.exportToClipboard();
  }
}
