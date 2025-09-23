/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {HAlign, keys, KeyStroke, ScoutKeyboardEvent, SearchOutline} from '../../index';

/**
 * Focuses the first node when pressing down.
 */
export class SearchOutlineDownKeyStroke extends KeyStroke {
  declare field: SearchOutline;

  constructor(outline: SearchOutline) {
    super();
    this.field = outline;
    this.which = [keys.DOWN];
    this.stopPropagation = true;
    this.renderingHints.hAlign = HAlign.RIGHT;
    this.renderingHints.text = '↓';
    this.renderingHints.$drawingArea = ($drawingArea, event) => this.field.$searchPanel;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    if (!super._accept(event)) {
      return false;
    }
    return this.field.visibleNodesFlat.length > 0;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.setFocusedNode(this.field.visibleNodesFlat[0]);
    this.field.focus();
  }
}
