/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FocusRule, PopupAlignment, PopupScrollType, PopupWindowResizeType, Rectangle, Widget, WidgetModel} from '../index';

export interface PopupModel extends WidgetModel {
  /**
   * Configures whether the opening of the popup should be animated using CSS.
   *
   * If set to true, the class `animate-open` will be added once the popup opens and automatically removed when the animation finishes.
   *
   * Default is false.
   */
  animateOpening?: boolean;
  /**
   * Configures whether position or size changes of the popup should be animated.
   *
   * Default is false.
   */
  animateResize?: boolean;
  /**
   * Default is 10.
   */
  windowPaddingX?: number;
  /**
   * Default is 5.
   */
  windowPaddingY?: number;
  /**
   * Configures whether a glass pane should be shown on the back of the popup so that elements outside the popup cannot be clicked while it is open.
   *
   * Default is false.
   *
   * @see modal
   */
  withGlassPane?: boolean;
  /**
   * Configures whether a {@link FocusContext} should be installed for a popup.
   *
   * Having a focus context ensures the user cannot tab outside the popup.
   *
   * Default is true.
   */
  withFocusContext?: boolean;

  /**
   * A function that returns the {@link HTMLElement} to be focused or a {@link FocusRule}.
   *
   * Default returns {@link FocusRule.AUTO}.
   */
  initialFocus?(): FocusRule | HTMLElement;

  /**
   * Configures whether the container of the popup should be focusable.
   *
   * Default is false.
   */
  focusableContainer?: boolean;
  /**
   * Specifies how the popup should be horizontally aligned to the {@link anchor}.
   *
   * Default is {@link Popup.Alignment.LEFTEDGE}.
   */
  horizontalAlignment?: PopupAlignment;
  /**
   * Specifies how the popup should be vertically aligned to the {@link anchor}.
   *
   * Default is {@link Popup.Alignment.BOTTOM}.
   */
  verticalAlignment?: PopupAlignment;
  /**
   * Configures whether the horizontal alignment should be switched (e.g. from right to left) if the popup overlaps a window border.
   *
   * Default is false.
   */
  horizontalSwitch?: boolean;
  /**
   * Configures whether the vertical alignment should be switched (e.g. from bottom to top) if the popup overlaps a window border.
   *
   * Default is true.
   */
  verticalSwitch?: boolean;
  /**
   * Configures whether the width should be adjusted if the popup does not fit into the window.
   *
   * Before trimming is applied the popup will be switched, if the switch option is enabled.
   * If neither switch nor trim is enabled, the popup will be moved until its right border is visible.
   *
   * Default is false.
   */
  trimWidth?: boolean;
  /**
   * Configures whether the height should be adjusted if the popup does not fit into the window.
   *
   * Before trimming is applied the popup will be switched, if the switch option is enabled.
   * If neither switch nor trim is enabled, the popup will be moved until its right border is visible.
   *
   * Default is true.
   */
  trimHeight?: boolean;
  /**
   * Defines what should happen when the scroll parent is scrolled.
   *
   * It is also used if the anchor changes its location (needs to support the locationChange event).
   *
   * Default is 'remove'.
   */
  scrollType?: PopupScrollType;
  windowResizeType?: PopupWindowResizeType;
  /**
   * If true, the anchor is considered when computing the position and size of the popup.
   *
   * Default is true.
   */
  boundToAnchor?: boolean;
  /**
   * If true, an arrow is shown pointing to the anchor. If there is no anchor, no arrow will be visible.
   *
   * Please note: some alignment combinations are not supported, which are: LEFT or RIGHT + BOTTOM or TOP
   *
   * Default is false.
   */
  withArrow?: boolean;
  /**
   * If false, the attached mouse down handler will NOT close the popup if the anchor was clicked, the anchor is responsible to close it.
   * This is necessary because the mousedown listener is attached to the capture phase and therefore executed before any other.
   * If anchor was clicked, popup would already be closed and then opened again -> popup could never be closed by clicking the anchor.
   *
   * Default is true.
   *
   * @see modal
   */
  closeOnAnchorMouseDown?: boolean;
  /**
   * Defines whether the popup should be closed on a mouse click outside the popup.
   *
   * Default is true.
   *
   * @see modal
   */
  closeOnMouseDownOutside?: boolean;
  /**
   * Defines whether the popup should be closed whenever another popup opens.
   *
   * Default is true.
   *
   * @see modal
   */
  closeOnOtherPopupOpen?: boolean;
  /**
   * Defines whether the popup should behave like a modal form. If true, the properties {@link closeOnAnchorMouseDown}, {@link closeOnMouseDownOutside}
   * and {@link closeOnOtherPopupOpen} ore overruled and set to false. The property {@link withGlassPane} is overruled too and set to true.
   *
   * Default is false.
   */
  modal?: boolean;
  /**
   * Specifies the location where the popup should be positioned if there is no {@link $anchor}.
   *
   * @see $anchor
   */
  location?: {
    x?: number;
    y?: number;
  };
  /**
   * Specifies the element to which the popup should be attached respecting {@link horizontalAlignment} and {@link verticalAlignment}.
   *
   * If no element is specified, the popup will be positioned at the given {@link anchorBounds} resp. {@link location}.
   * If neither {@link anchorBounds} nor a {@link location} are specified, the popup will be positioned in the optical middle of the window.
   */
  $anchor?: JQuery;
  /**
   * Specifies the widget to which the popup should be attached.
   *
   * It will use {@link Widget.$container} as {@link $anchor}.
   *
   * @see $anchor
   */
  anchor?: Widget;
  /**
   * Specifies the bounds to which the popup should be attached respecting {@link horizontalAlignment} and {@link verticalAlignment}.
   *
   * @see $anchor
   */
  anchorBounds?: Rectangle;
}
