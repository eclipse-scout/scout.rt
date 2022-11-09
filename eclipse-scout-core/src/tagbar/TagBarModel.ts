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
import {WidgetModel} from '../index';

export interface TagBarModel extends WidgetModel {
  /**
   * Default is true
   */
  overflowEnabled?: boolean;
  /**
   * Default is false
   */
  overflowVisible?: boolean;
  tags?: string[];
  /**
   * Whether the tag elements are clickable (even when TagBar is disabled).
   * When the tag elements are clickable a click handler is registered and
   * a pointer cursor appears when hovering over the element. Default is false.
   */
  clickable?: boolean;
}
