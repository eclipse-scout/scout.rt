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
import {Page, scout, TableRow} from '../../../index';

/**
 * @class
 * @augments {Page}
 */
export default class AutoLeafPageWithNodes extends Page {

  constructor() {
    super();

    this.leaf = true;
  }

  /**
   * @override Page.js
   */
  _init(model) {
    scout.assertParameter('row', model.row, TableRow);
    super._init(model);
    this.text = this.row.cells[0].text;
  }
}
