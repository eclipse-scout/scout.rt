/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke} from '../../index';
import {FieldStatus} from './FieldStatus';

export class FieldStatusExecKeyStroke extends KeyStroke {
  declare field: FieldStatus;

  constructor(fieldStatus: FieldStatus) {
    super();
    this.field = fieldStatus;
    this.which = [keys.SPACE, keys.ENTER];
    this.stopPropagation = true;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.doAction();
  }
}
