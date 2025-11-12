/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {SmartFieldModel} from '../../../index';

export interface ProposalFieldModel extends SmartFieldModel<string> {
  /**
   * true if leading and trailing whitespace should be stripped from the entered text while validating the value.
   *
   * Default is true.
   */
  trimText?: boolean;
  /**
   * If this flag is set to true the proposal field performs a lookup by text when
   * accept proposal is called. The behavior is similar to what the smart-field does
   * in that case, but without the need to have a valid single match as the result
   * from the lookup.
   *
   * Default is false
   */
  lookupOnAcceptByText?: boolean;
}
