/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AriaOrientation, AriaRole} from '../index';

export class GridAriaRules {
  role: AriaRole = 'grid';
  rowRole: AriaRole = 'row';
  rowGroupRole: AriaRole = 'rowgroup';
  cellRole: AriaRole = 'gridcell';
  /**
   * The name of the DOM attribute that holds the row index.
   * It will be put on each row if the grid has virtual scrolling enabled.
   */
  rowIndexAttr = 'aria-rowindex';
  /**
   * The name of the DOM attribute that holds the number of rows.
   * It will be put on the focusable container if the grid has virtual scrolling enabled.
   */
  rowCountAttr = 'aria-rowcount';
  /**
   * The name of the DOM attribute that holds the child row index.
   * It will be put on each row, also on the top level rows if the grid has virtual scrolling enabled.
   */
  childRowIndexAttr: string = null;
  /**
   * The name of the DOM attribute that holds the number of child rows.
   * It will be put on each row, also on the top level rows if the grid has virtual scrolling enabled.
   */
  childRowCountAttr: string = null;
  levelAttr: string = null;
  orientationAttr: AriaOrientation = null;
}
