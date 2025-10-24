/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AriaRole, GridAriaRules} from '../index';

export class TreeGridAriaRules extends GridAriaRules {
  override role: AriaRole = 'treegrid';
  override childRowIndexAttr = 'aria-posinset';
  override childRowCountAttr = 'aria-setsize';
  override rowCountAttr: string = null; // Not needed for tree grid
  override rowIndexAttr: string = null; // Not needed for tree grid
  override levelAttr = 'aria-level';
}
