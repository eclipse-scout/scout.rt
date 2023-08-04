/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AbstractTableAccessibilityRenderer, aria} from '../index';

export class ListBoxTableAccessibilityRenderer extends AbstractTableAccessibilityRenderer {

  constructor() {
    super();
    this.renderTable = ($elem: JQuery<Element>) => aria.role($elem, 'listbox');
    this.renderRow = ($elem: JQuery<Element>) => aria.role($elem, 'option');
    this.renderRowGroup = ($elem: JQuery<Element>) => aria.role($elem, 'group');
    this.renderCell = ($elem: JQuery<Element>) => aria.role($elem, null);
    this.rowRole = 'option';
  }
}
