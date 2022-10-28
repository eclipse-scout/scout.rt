/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ValueFieldModel} from '../../index';

export default interface BasicFieldModel<TValue extends TModelValue, TModelValue = TValue> extends ValueFieldModel<TValue, TModelValue> {
  /**
   * Indicates whether the property {@link FormFieldModel.displayText} should be updated while the user types.
   *
   * If the property is set to false, the displayText is updated after the user completes the editing (e.g. by pressing TAB).
   * If the property is set to true, the displayText is updated while the user types after a configurable delay ({@link updateDisplayTextOnModifyDelay}.
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
