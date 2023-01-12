/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Widget} from '../index';

export interface WidgetSupportOptions {
  /**
   * Widget that created the support
   */
  widget: Widget;

  /**
   * JQuery element that will be used for the visualization.
   * It may be a function to resolve the container later.
   * If this property is not set the $container of the widget is used by default.
   */
  $container?: JQuery | (() => JQuery);
}
