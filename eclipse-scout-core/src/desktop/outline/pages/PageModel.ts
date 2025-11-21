/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, ObjectModelWithUuid, ObjectOrChildModel, ObjectOrModel, ObjectOrType, Outline, Page, PageDetailMenuContributor, PageParamDo, Table, TreeNode, TreeNodeModel} from '../../../index';

export interface PageModel extends TreeNodeModel, ObjectModelWithUuid<TreeNode> {
  /**
   * The {@link PageParamDo} containing all parameters that are required to load the page.
   */
  pageParam?: PageParamDo;
  parent?: Outline;
  childNodes?: ObjectOrModel<Page>[];
  compactRoot?: boolean;
  detailTable?: ObjectOrChildModel<Table>;
  detailTableVisible?: boolean;
  detailForm?: ObjectOrChildModel<Form>;
  detailFormVisible?: boolean;
  /**
   * Additional detail menu contributors that will be added to the default contributors.
   */
  detailMenuContributors?: ObjectOrType<PageDetailMenuContributor>[];
  navigateButtonsVisible?: boolean;
  tableStatusVisible?: boolean;
  /**
   * True to select the page linked with the selected row when the row was selected. May be useful on touch devices.
   */
  drillDownOnRowClick?: boolean;
  /**
   * Configures the text which is used in the tile outline overview.
   *
   * If the value is `undefined`, the {@link PageTileButton} uses the {@link PageModel.text} instead.
   * If no text should be displayed, set the value to `null`.
   */
  overviewText?: string;
  /**
   * Configures the icon which is used in the tile outline overview.
   *
   * If the value is `undefined`, the {@link PageTileButton} uses the {@link PageModel.iconId} instead.
   * If no icon should be displayed, set the value to `null`.
   */
  overviewIconId?: string;
  /**
   * Configures whether HTML code in the {@link PageModel.overviewText} property should be interpreted. If set to false, the HTML will be encoded.
   *
   * If the value is `undefined`, the {@link PageTileButton} uses {@link PageModel.htmlEnabled} instead.
   *
   * Default is false.
   */
  overviewHtmlEnabled?: boolean;
  showTileOverview?: boolean;
  /**
   * True to inherit all menus (single selection) from the parent table page, false to inherit none.
   *
   * Default is true
   */
  inheritMenusFromParentTablePage?: boolean;
  /**
   * Additional qualifier that is added to the key for {@link UiPreferences} for the detail table of this page.
   * Useful when the same class is used in multiple locations but preferences should be stored separately.
   */
  userPreferenceContext?: string;
}
