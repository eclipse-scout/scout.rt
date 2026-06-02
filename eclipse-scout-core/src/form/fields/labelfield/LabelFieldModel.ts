/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ValueFieldModel} from '../../../index';

export interface LabelFieldModel extends ValueFieldModel<string> {
  /**
   * Configures whether HTML tags in the {@link value} should be interpreted or encoded.
   *
   * - If set to false, the HTML tags in the `value` will be encoded, so the tags won't have any effect and only plain text will be displayed.
   * - If set to true, the HTML tags in the `value` will be interpreted.
   *   In that case, you have to make sure that user input is encoded by yourself.
   *   E.g. if the `value` should display text from an input field, use {@link strings.encode} to prevent HTML injection.
   *
   * Default is false.
   */
  htmlEnabled?: boolean;
  /**
   * Configures whether the text should be selectable.
   *
   * Default is true.
   */
  selectable?: boolean;
  /**
   * Configures whether the value should be wrapped if there is not enough space to display it on one line.
   *
   * Default is false.
   */
  wrapText?: boolean;
}
