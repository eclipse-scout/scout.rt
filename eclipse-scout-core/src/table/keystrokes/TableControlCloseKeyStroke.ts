/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, TableControl} from '../../index';

export class TableControlCloseKeyStroke extends KeyStroke {
  declare field: TableControl;

  constructor(tableControl: TableControl) {
    super();
    this.field = tableControl;
    this.which = [keys.ESC];
    this.stopPropagation = true;
    this.renderingHints.render = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.toggle();
  }
}
