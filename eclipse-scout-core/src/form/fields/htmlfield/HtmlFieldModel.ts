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

export interface HtmlFieldModel extends ValueFieldModel<string> {
  /**
   * Configures whether scrollbars should be shown if the content is bigger than the available size.
   *
   * Default is false.
   */
  scrollBarEnabled?: boolean;
  /**
   * Configures whether the text should be selectable.
   *
   * Default is true.
   */
  selectable?: boolean;
  scrollToAnchor?: string;
}
