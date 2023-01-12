/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ActionKeyStroke, Menu} from '../index';

export class MenuKeyStroke extends ActionKeyStroke {

  declare field: Menu;

  constructor(action: Menu) {
    super(action);
  }

  protected override _isEnabled(): boolean {
    if (this.field.excludedByFilter) {
      return false;
    }
    return super._isEnabled();
  }
}
