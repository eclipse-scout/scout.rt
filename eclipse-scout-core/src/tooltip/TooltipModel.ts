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
import {Menu, Rectangle, WidgetModel} from '../index';
import {StatusSeverity} from '../status/Status';
import {TooltipDirection, TooltipPosition, TooltipScrollType} from './Tooltip';
import {ObjectOrChildModel} from '../scout';

export default interface TooltipModel extends WidgetModel {
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
