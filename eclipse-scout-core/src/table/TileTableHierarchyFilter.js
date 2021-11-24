/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TableFilter} from '../index';
import $ from 'jquery';

export default class TileTableHierarchyFilter extends TableFilter {

  constructor(model) {
    super();
    $.extend(this, model);
  }

  accept(row) {
    return !row.parentRow;
  }

  createLabel() {
    return this.table.session.text('ui.TileView');
  }
}
