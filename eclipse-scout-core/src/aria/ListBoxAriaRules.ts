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

export class ListBoxAriaRules extends GridAriaRules {
  override role: AriaRole = 'listbox';
  override rowRole: AriaRole = 'option';
  override rowGroupRole: AriaRole = 'group';
  override rowCountAttr: string = null;
  override rowIndexAttr: string = null;
  override cellRole: AriaRole = null;
  override childRowIndexAttr = 'aria-posinset';
  override childRowCountAttr = 'aria-setsize';
}
