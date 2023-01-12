/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
