/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Group, keys, KeyStroke} from '../../index';

export class GroupToggleCollapseKeyStroke extends KeyStroke {
  declare field: Group;

  constructor(group: Group) {
    super();
    this.field = group;
    this.which = [keys.SPACE];
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.toggleCollapse();
  }
}
