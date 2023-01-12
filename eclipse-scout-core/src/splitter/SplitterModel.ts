/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LayoutData, WidgetModel} from '../index';

export interface SplitterModel extends WidgetModel {
  /**
   * Default is true
   */
  splitHorizontal?: boolean;
  /**
   * current splitter position in pixels, updated by updatePosition()
   */
  position?: number;
  /**
   * Direction set to position the splitter inside the root element. Default is `top`.
   */
  orientation?: 'top' | 'right' | 'bottom' | 'left';
  layoutData?: LayoutData;
  $anchor?: JQuery;
  /**
   * Optional. Fallback is {@link this.$parent}.
   */
  $root?: JQuery;
}
