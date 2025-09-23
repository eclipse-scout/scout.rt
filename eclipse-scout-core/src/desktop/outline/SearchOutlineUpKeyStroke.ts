/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {keys, KeyStroke, SearchOutline} from '../../index';

/**
 * Prevents up keystroke from outline.
 *
 * Pressing up would focus the node above the focused node.
 * This is confusing when the search field is focused because the focused node is not visible.
 */
export class SearchOutlineUpKeyStroke extends KeyStroke {
  declare field: SearchOutline;

  constructor(outline: SearchOutline) {
    super();
    this.field = outline;
    this.which = [keys.UP];
    this.stopPropagation = true;
    this.renderingHints.render = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    // NOP
  }
}
