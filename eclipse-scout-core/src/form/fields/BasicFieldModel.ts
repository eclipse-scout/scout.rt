/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ValueFieldModel} from '../../index';

export interface BasicFieldModel<TValue extends TModelValue, TModelValue = TValue> extends ValueFieldModel<TValue, TModelValue> {
  /**
   * Indicates whether the property {@link FormFieldModel.displayText} should be updated while the user types.
   *
   * - If the property is set to false, the displayText is updated after the user completes the editing (e.g. by pressing TAB).
   * - If the property is set to true, the displayText is updated while the user types after a configurable delay ({@link updateDisplayTextOnModifyDelay}.
   *
   * Default is false.
   */
  updateDisplayTextOnModify?: boolean;
  /**
   * Specifies the delay in milliseconds after the display text will be updated, if {@link updateDisplayTextOnModify} is set to true.
   *
   * Default value is 250ms.
   */
  updateDisplayTextOnModifyDelay?: number;
}
