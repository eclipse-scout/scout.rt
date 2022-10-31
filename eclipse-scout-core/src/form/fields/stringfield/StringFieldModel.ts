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
import {BasicFieldModel, MaxLengthHandler} from '../../../index';
import {StringFieldFormat} from './StringField';

export default interface StringFieldModel extends BasicFieldModel<string> {
  format?: StringFieldFormat;
  /**
   * Default is false.
   */
  hasAction?: boolean;
  /**
   * true if all characters should be masked (e.g. for a password field).
   *
   * Default is false.
   */
  inputMasked?: boolean;
  /**
   * Default is false.
   */
  inputObfuscated?: boolean;
  /**
   * Default is 4000.
   */
  maxLength?: number;
  maxLengthHandler?: MaxLengthHandler;
  /**
   * Default is false.
   */
  multilineText?: boolean;
  /**
   * Default is 0.
   */
  selectionStart?: number;
  /**
   * Default is 0.
   */
  selectionEnd?: number;
  /**
   * Define whether selection tracking should be enabled.
   * If false, selectionStart and selectionEnd might not reflect the actual selection.
   *
   * Default is false.
   */
  selectionTrackingEnabled?: boolean;
  /**
   * Default is false.
   */
  spellCheckEnabled?: boolean;
  /**
   * true if leading and trailing whitespace should be stripped from the entered text while validating the value.
   *
   * Default is true.
   */
  trimText?: boolean;
  /**
   * Default is false.
   */
  wrapText?: boolean;
}
