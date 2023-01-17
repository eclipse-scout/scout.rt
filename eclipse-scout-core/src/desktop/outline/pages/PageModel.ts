/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
