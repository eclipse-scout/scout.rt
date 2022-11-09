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
