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
import {Form, ObjectOrChildModel, ObjectOrModel, Outline, Page, Table, TreeNodeModel} from '../../../index';

export interface PageModel extends TreeNodeModel {
  parent?: Outline;
  childNodes?: ObjectOrModel<Page>[];
  compactRoot?: boolean;
  detailTable?: ObjectOrChildModel<Table>;
  detailTableVisible?: boolean;
  detailForm?: ObjectOrChildModel<Form>;
  detailFormVisible?: boolean;
  navigateButtonsVisible?: boolean;
  tableStatusVisible?: boolean;
  /**
   * True to select the page linked with the selected row when the row was selected. May be useful on touch devices.
   */
  drillDownOnRowClick?: boolean;
  /**
   * The icon id which is used for icons in the tile outline overview.
   */
  overviewIconId?: string;
  showTileOverview?: boolean;
}
