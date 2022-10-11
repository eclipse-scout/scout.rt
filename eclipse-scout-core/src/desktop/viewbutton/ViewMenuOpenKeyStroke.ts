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
import {keys, KeyStroke, ViewMenuTab} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

/**
 * Keystroke to open the 'ViewMenuPopup' on 'F2'.
 */
export default class ViewMenuOpenKeyStroke extends KeyStroke {
  declare field: ViewMenuTab;

  constructor(viewMenuTab: ViewMenuTab) {
    super();
    this.field = viewMenuTab;

    this.which = [keys.F2];
    this.stopPropagation = true;
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      if (this.field.selected) {
        return this.field.dropdown.$container;
      }
      return this.field.selectedButton.$container;
    };
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    if (this.field.selected) {
      this.field.togglePopup();
    } else if (this.field.selectedButton) {
      this.field.selectedButton.doAction();
    }
  }

  protected override _postRenderKeyBox($drawingArea: JQuery, $keyBox?: JQuery) {
    let width = $drawingArea.outerWidth();
    let keyBoxWidth = $drawingArea.find('.key-box').outerWidth();
    let left = width / 2 - keyBoxWidth / 2;
    $drawingArea.find('.key-box').cssLeft(left);
  }
}
