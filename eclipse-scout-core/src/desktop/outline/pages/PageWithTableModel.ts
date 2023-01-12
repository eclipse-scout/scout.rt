/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
}
