/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, ObjectOrChildModel, Point, Rectangle, StatusSeverity, TooltipDirection, TooltipPosition, TooltipScrollType, WidgetModel} from '../index';

export interface TooltipModel extends WidgetModel {
  /**
   * Specifies the text to be displayed inside the tooltip.
   */
  text?: string | (($comp: JQuery) => string);
  /**
   * Specifies the severity of the tooltip which has an impact on the visualization (e.g. the tooltip will be red if severity is error).
   *
   * Default is {@link Status.Severity.INFO}.
   */
  severity?: StatusSeverity;
  /**
   * Specifies the position of the arrow relative to the left edge of the tooltip.
   *
   * Default is 16.
   */
  arrowPosition?: number;
  /**
   * Specifies the unit used to for {@link arrowPosition}.
   *
   * Maybe 'px' or '%'.
   *
   * Default is 'px'.
   */
  arrowPositionUnit?: string;
  /**
   * Specifies whether the origin should be clipped if it does not completely fit into the viewports of the scrollable parents of the {@link $anchor}.
   *
   * The origin is the rectangle at which the tooltip points.
   * It corresponds to the bounds of the {@link $anchor} unless it is provided explicitly through {@link origin} or {@link originProducer}.
   *
   * If the property is true and the origin of a tooltip is inside one or more scrollable containers and the origin is only partly visible,
   * the bounds are clipped and the parts that are not visible are removed.
   * This makes sure the tooltip can still be shown even if the origin is not completely visible.
   * This requires an {@link $anchor} to be set in order to be able to compute the scrollable parents.
   *
   * If the property is set to false and an {@link $anchor} is set, the tooltip will be made invisible
   * if the position to where the tooltip points is not in the viewport of the scrollables.
   *
   * Default is false.
   */
  clipOrigin?: boolean;
  /**
   * Specifies the minimum distance from the tooltip to the window border on the horizontal axis.
   *
   * Default is 10
   */
  windowPaddingX?: number;
  /**
   * Specifies the minimum distance from the tooltip to the window border on the vertical axis.
   *
   * Default is 5
   */
  windowPaddingY?: number;
  /**
   * Specifies the bounds to which the tooltip should be attached.
   *
   * @see $anchor
   */
  origin?: Rectangle;
  /**
   * When the origin point is calculated using $element.offset(),
   * the result is absolute to the window. When positioning the tooltip, the $parent's offset must
   * be subtracted. When the given origin is already relative to the parent, set this option to
   * "true" to disable this additional calculation.
   *
   * Default is false.
   */
  originRelativeToParent?: boolean;
  /**
   * Specifies a producer that creates an origin.
   *
   * The origin is the rectangle at which the tooltip points.
   *
   * The producer is called every time the tooltip is positioned using {@link Tooltip.position}.
   * If no producer is present, the {@link origin} or {@link $anchor} is used for the calculation.
   *
   * @see $anchor
   */
  originProducer?: ($anchor: JQuery) => Rectangle;
  /**
   * Specifies a producer that creates an offset.
   *
   * The offset is the point where the tooltip should point to relative to the origin.
   * E.g. if the offset is `{x: origin.width / 2, y: 0}`, the tooltip will point to the top center of the origin.
   *
   * The producer is called every time the tooltip is positioned using {@link Tooltip.position}.
   * If no producer is present, the top left corner of the {@link origin} is used as offset.
   * If no {@link origin} is specified, the center point on top of the {@link $anchor} is used as offset.
   */
  offsetProducer?: (origin: Rectangle) => Point;
  /**
   * Specifies whether the tooltip should be destroyed if the user click outside the tooltip or interacts with the keyboard.
   *
   * Default is false
   */
  autoRemove?: boolean;
  /**
   * Specifies whether the tooltip should be positioned on `top` or on the `bottom` of the origin.
   *
   * Default is 'top'.
   */
  tooltipPosition?: TooltipPosition;
  /**
   * Specifies the horizontal direction of the tooltip relative to the origin.
   *
   * Default is 'right'.
   */
  tooltipDirection?: TooltipDirection;
  /**
   * Specifies what should happen with the tooltip if the {@link $anchor} scrolls.
   *
   * - position: the tooltip will be positioned again on every scroll event so that it moves along with the {@link $anchor}.
   * - remove: the tooltip will be destroyed as soon as the {@link $anchor} scrolls.
   *
   * Default is 'position'.
   */
  scrollType?: TooltipScrollType;
  /**
   * Specifies whether HTML code in the {@link text} property should be interpreted. If set to false, the HTML will be encoded.
   *
   * Default is false.
   */
  htmlEnabled?: boolean | (($comp: JQuery) => boolean);
  /**
   * Specifies menus that should be available inside the tooltip.
   */
  menus?: ObjectOrChildModel<Menu>[];
  /**
   * Specifies the element to which the tooltip should be attached.
   *
   * If no element is specified, the tooltip will be positioned at the given {@link origin} resp. at the origin computed by {@link originProducer}.
   */
  $anchor?: JQuery;
}
