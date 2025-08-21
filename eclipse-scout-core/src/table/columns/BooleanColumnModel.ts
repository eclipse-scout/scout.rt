/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColumnModel} from '../../index';

export interface BooleanColumnModel extends ColumnModel<boolean> {
  /**
   * Specifies whether the cell values can represent three states.
   *
   * - true: the value can be true, false or null. Null is the third state that represents "undefined".
   * - false: the value can be true or false. The value is never null (setting the value to null will automatically convert it to false).
   *
   * Default is false.
   */
  triStateEnabled?: boolean;
}
