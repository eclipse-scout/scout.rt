/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AriaRole} from '../aria';

export class AbstractTableAccessibilityRenderer {

  rowRole: AriaRole;
  cellRole: AriaRole;

  renderTable: ($elem: JQuery<Element>) => void;
  renderRow: ($elem: JQuery<Element>) => void;
  renderRowGroup: ($elem: JQuery<Element>) => void;
  renderCell: ($elem: JQuery<Element>) => void;

  constructor() {
    this.renderTable = null;
    this.renderRow = null;
    this.renderRowGroup = null;
    this.renderCell = null;
    this.rowRole = null;
    this.cellRole = null;
  }
}
