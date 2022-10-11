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
import {AutoLeafPageWithNodesModel, Page, scout, TableRow} from '../../../index';

export default class AutoLeafPageWithNodes extends Page implements AutoLeafPageWithNodesModel {
  declare model: AutoLeafPageWithNodesModel;

  constructor() {
    super();
    this.leaf = true;
  }

  protected override _init(model: AutoLeafPageWithNodesModel) {
    scout.assertParameter('row', model.row, TableRow);
    super._init(model);
    this.text = this.row.cells[0].text;
  }
}
