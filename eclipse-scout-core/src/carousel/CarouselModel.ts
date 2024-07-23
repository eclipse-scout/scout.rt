/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GridData, ObjectOrChildModel, Widget, WidgetModel} from '../index';

export interface CarouselModel extends WidgetModel {
  /**
   * Default is true
   */
  statusEnabled?: boolean;
  /**
   * Default is 0.25
   */
  moveThreshold?: number;
  widgets?: ObjectOrChildModel<Widget> | ObjectOrChildModel<Widget>[];
  currentItem?: number;
  gridData?: GridData;
}
