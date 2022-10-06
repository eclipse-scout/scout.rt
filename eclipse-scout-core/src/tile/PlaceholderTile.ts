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
import {Tile} from '../index';

export default class PlaceholderTile extends Tile {

  constructor() {
    super();
    this.cssClass = 'placeholder-tile';
    this.displayStyle = Tile.DisplayStyle.PLAIN;
  }

  _setSelectable(selectable) {
    // Placeholder tiles should not be selectable
    super._setSelectable(false);
  }
}
