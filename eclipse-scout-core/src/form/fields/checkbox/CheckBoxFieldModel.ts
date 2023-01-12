/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ValueFieldModel} from '../../../index';

export interface CheckBoxFieldModel extends ValueFieldModel<boolean> {
  /**
   * <ul>
   * <li>true: the check box can have a value of true, false and null. Null is the third state that represents "undefined".
   * <li>false: the check box can have a value of true and false. The value is never null (setting the value to null will automatically convert it to false).
   * </ul>
   * The default is value is false.
   */
  triStateEnabled?: boolean;
  wrapText?: boolean;
  keyStroke?: string;
}
