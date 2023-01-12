/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, ObjectOrChildModel, Rectangle, StatusSeverity, TooltipDirection, TooltipPosition, TooltipScrollType, WidgetModel} from '../index';

export interface TooltipModel extends WidgetModel {
  /**
   * Default is no text.
   */
  text?: string | (($comp: JQuery) => string);
  /**
   * Default is {@link Status.Severity.INFO}.
   */
  severity?: StatusSeverity;
  /**
   * Default is 16.
   */
  arrowPosition?: number;
  /**
   * Default is 'px'.
   */
  arrowPositionUnit?: string;
  /**
   * Default is 10
   */
  windowPaddingX?: number;
  /**
   * Default is 5
   */
  windowPaddingY?: number;
  origin?: Rectangle;
  /**
   * When the origin point is calculated using $element.offset(),
   * the result is absolute to the window. When positioning the tooltip, the $parent's offset must
   * be subtracted. When the given origin is already relative to the parent, set this option to
   * "true" to disable this additional calculation.
   */
  originRelativeToParent?: boolean;
  /**
   * Default is false
   */
  autoRemove?: boolean;
  /**
   * Default is 'top'.
   */
  tooltipPosition?: TooltipPosition;
  /**
   * Default is 'right'.
   */
  tooltipDirection?: TooltipDirection;
  /**
   * Default is 'position'.
   */
  scrollType?: TooltipScrollType;
  /**
   * Default is false.
   */
  htmlEnabled?: boolean | (($comp: JQuery) => boolean);
  menus?: ObjectOrChildModel<Menu>[];
  $anchor?: JQuery;
}
