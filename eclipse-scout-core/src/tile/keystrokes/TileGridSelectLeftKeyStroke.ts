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
import {keys, TileGridSelectKeyStroke} from '../../index';

export default class TileGridSelectLeftKeyStroke extends TileGridSelectKeyStroke {

  constructor(tileGrid) {
    super(tileGrid);
    this.stopPropagation = true;
    this.repeatable = true;
    this.which = [keys.LEFT];
    this.renderingHints.text = '←';
  }

  _accept(event) {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    if (!(this.getSelectionHandler().isHorizontalGridActive())) {
      return false;
    }
    return true;
  }

  _computeNewSelection(extend) {
    return this.getSelectionHandler().computeSelectionX(-1, extend);
  }
}
