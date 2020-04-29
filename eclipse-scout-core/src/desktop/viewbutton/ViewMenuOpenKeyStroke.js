/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke} from '../../index';

/**
 * Keystroke to open the 'ViewMenuPopup' on 'F2'.
 */
export default class ViewMenuOpenKeyStroke extends KeyStroke {

  constructor(viewMenuTab) {
    super();
    this.field = viewMenuTab;

    this.which = [keys.F2];
    this.stopPropagation = true;

    this.renderingHints.$drawingArea = function($drawingArea, event) {
      return this.field.$container;
    }.bind(this);
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    if (this.field.selected && !this.field.inBackground) {
      this.field.togglePopup();
    } else if (this.field.selectedButton) {
      this.field.selectedButton.doAction();
    }
  }

  _postRenderKeyBox($drawingArea) {
    let wKeybox = $drawingArea.find('.key-box').outerWidth(),
      left = this.field.dropdown.$container.outerWidth();

    if (this.field.selected && !this.field.inBackground) {
      left = left / 2;
    } else if (this.field.selectedButton) {
      left = left + this.field.selectedButton.$container.outerWidth() / 2;
    }
    left -= wKeybox / 2;
    $drawingArea.find('.key-box').cssLeft(left);
  }
}
