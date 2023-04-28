/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CalendarSidebarSplitter, ResourcePanel, WidgetModel, YearPanel} from '../index';

export interface CalendarSidebarModel extends WidgetModel {
  /**
   * Year panel widget
   */
  yearPanel?: YearPanel;
  /**
   * Splitter widget
   */
  splitter?: CalendarSidebarSplitter;
  /**
   * Resource panel widget
   */
  resoucePanel?: ResourcePanel;
  /**
   * Defines, whether the resource panel is displayable.
   * For example, the resource panel is not visible when the calendar widget has only one resource.
   */
  resourcePanelDisplayable?: boolean;
}
