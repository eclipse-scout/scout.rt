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

export class VoidGridAriaRules extends GridAriaRules {
  override role: AriaRole = null;
  override rowRole: AriaRole = null;
  override rowGroupRole: AriaRole = null;
  override cellRole: AriaRole = null;
  override rowIndexAttr = null;
  override rowCountAttr = null;
  override childRowIndexAttr = null;
  override childRowCountAttr = null;
}
