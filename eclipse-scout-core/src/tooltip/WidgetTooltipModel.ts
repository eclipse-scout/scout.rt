/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ObjectOrChildModel, TooltipModel, Widget} from '../index';

export interface WidgetTooltipModel extends TooltipModel {
  /**
   * Makes the tooltip focusable.
   *
   * Default is true.
   */
  withFocusContext?: boolean;
  /**
   * The {@link Widget} rendered inside the tooltip.
   */
  content?: ObjectOrChildModel<Widget>;
}
