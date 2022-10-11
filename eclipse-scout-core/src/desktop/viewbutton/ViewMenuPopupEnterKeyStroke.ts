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

import {keys, KeyStroke, ScoutKeyboardEvent, ViewMenuPopup} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class ViewMenuPopupEnterKeyStroke extends KeyStroke {
  declare field: ViewMenuPopup;

  constructor(popup: ViewMenuPopup) {
    super();
    this.field = popup;
    this.which = [keys.ENTER, keys.SPACE];
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let tile = this.field.content.selectedTiles[0];
      if (tile && tile.rendered) {
        return tile.$container;
      }
    };
    this.inheritAccessibility = false;
  }

  override accept(event: ScoutKeyboardEvent): boolean {
    return super.accept(event) && this.field.content.selectedTiles.length === 1 && this.field.content.selectedTiles[0].enabledComputed;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.activateTile(this.field.content.selectedTiles[0]);
  }
}
