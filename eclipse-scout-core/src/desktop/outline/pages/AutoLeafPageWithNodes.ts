/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AutoLeafPageWithNodesModel, InitModelOf, Page, scout, SomeRequired, TableRow} from '../../../index';

export class AutoLeafPageWithNodes extends Page implements AutoLeafPageWithNodesModel {
  declare model: AutoLeafPageWithNodesModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'row'>;

  constructor() {
    super();
    this.leaf = true;
  }

  protected override _init(model: InitModelOf<this>) {
    scout.assertParameter('row', model.row, TableRow);
    super._init(model);
    this.text = this.row.cells[0].text;
  }
}
