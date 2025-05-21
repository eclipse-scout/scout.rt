/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AutoLeafPageWithNodesModel, InitModelOf, PageWithNodes, scout, Table, TableRow} from '../../../index';

export class AutoLeafPageWithNodes extends PageWithNodes implements AutoLeafPageWithNodesModel {
  declare model: AutoLeafPageWithNodesModel;

  constructor() {
    super();
    // hide table and form
    this.detailTableVisible = false;
    this.detailFormVisible = false;
    this.leaf = true;
  }

  protected override _createDetailTable(): Table {
    // do not create a table, AutoLeafPageWithNodes has no own content
    return null;
  }

  protected override _init(model: InitModelOf<this>) {
    scout.assertParameter('row', model.row, TableRow);
    super._init(model);
    this.text = this.computeTextForRow(this.row);
  }
}
