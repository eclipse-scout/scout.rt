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
import {keys, KeyStroke} from '../../index';

export default class TableSelectAllKeyStroke extends KeyStroke {

  constructor(table) {
    super();
    this.field = table;
    this.ctrl = true;
    this.which = [keys.A];
    this.renderingHints.offset = 14;
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      return this.field.footer ? this.field.footer._$infoSelection.find('.table-info-button') : null;
    };
    this.inheritAccessibility = false;
  }

  handle(event) {
    let table = this.field;
    table.toggleSelection();
    table.selectionHandler.lastActionRow = null;
  }
}
