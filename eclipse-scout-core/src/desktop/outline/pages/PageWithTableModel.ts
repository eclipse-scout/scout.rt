/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PageModel} from '../../../index';

export interface PageWithTableModel extends PageModel {
  /**
   * Configures whether a default child page should be created for each table row if no page is created. Default is false.
   */
  alwaysCreateChildPage?: boolean;
  /**
   * Configures whether the table data is loaded automatically with default search constraint when this page
   * is activated initially, or whether loading the table data must be triggered explicitly by the user.
   * Default is false. Set this to true if an unconstrained search would result in a large amount of data.
   */
  searchRequired?: boolean;
}
