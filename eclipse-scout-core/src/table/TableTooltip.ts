/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Tooltip} from '../index';

export default class TableTooltip extends Tooltip {

  constructor() {
    super();
  }

  _init(options) {
    super._init(options);

    this.table = options.table;
  }

  _render() {
    super._render();

    this._rowOrderChangedFunc = function(event) {
      if (event.animating) {
        // row is only set while animating
        if (event.row === this.row) {
          this.position();
        }
      } else {
        this.position();
      }
    }.bind(this);
    this.table.on('rowOrderChanged', this._rowOrderChangedFunc);
  }

  _remove() {
    super._remove();
    this.table.off('rowOrderChanged', this._rowOrderChangedFunc);
  }
}
