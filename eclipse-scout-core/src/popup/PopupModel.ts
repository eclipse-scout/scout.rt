/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FocusRule, PopupAlignment, PopupScrollType, PopupWindowResizeType, Widget, WidgetModel} from '../index';

export interface PopupModel extends WidgetModel {
  /**
   * Default is false.
   */
  animateOpening?: boolean;
  /**
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
   * Default is false.
   */
  withGlassPane?: boolean;
  /**
   * Default is true.
   */
  withFocusContext?: boolean;

  /**
   * A function that returns the {@link HTMLElement} to be focused or a {@link FocusRule}. Default returns {@link FocusRule.AUTO}.
   */
  initialFocus?(): FocusRule | HTMLElement;

  /**
   * Configures whether the container of the Popup should be focusable. Default is false.
   */
  focusableContainer?: boolean;
  /**
   * Default is {@link Popup.Alignment.LEFTEDGE}.
   */
  horizontalAlignment?: PopupAlignment;
  /**
   * Default is {@link Popup.Alignment.BOTTOM}.
   */
  verticalAlignment?: PopupAlignment;
  /**
   * If switch is enabled, the alignment will be changed if the popup overlaps a window border. Default is false.
   * */
  horizontalSwitch?: boolean;
  /**
   * Default is true.
   */
  verticalSwitch?: boolean;
  /**
   * Hints for the layout to control whether the size should be adjusted if the popup does not fit into the window.
   * Before trimming is applied the popup will be switched, if the switch option is enabled.
   * If neither switch nor trim is enabled, the popup will be moved until its right border is visible. Default is false.
   */
  trimWidth?: boolean;
  /**
   * Default is true.
   */
  trimHeight?: boolean;
  /**
   * Defines what should happen when the scroll parent is scrolled. It is also used if the anchor changes its location (needs to support the locationChange event). Default is 'remove'.
   */
  scrollType?: PopupScrollType;
  windowResizeType?: PopupWindowResizeType;
  /**
   * If true, the anchor is considered when computing the position and size of the popup. Default is true.
   */
  boundToAnchor?: boolean;
  /**
   * If true, an arrow is shown pointing to the anchor. If there is no anchor, no arrow will be visible. Default is false.
   * Please note: some alignment combinations are not supported, which are: LEFT or RIGHT + BOTTOM or TOP
   */
  withArrow?: boolean;
  /**
   * If false, the attached mouse down handler will NOT close the popup if the anchor was clicked, the anchor is responsible to close it.
   * This is necessary because the mousedown listener is attached to the capture phase and therefore executed before any other.
   * If anchor was clicked, popup would already be closed and then opened again -> popup could never be closed by clicking the anchor.
   * Default is true.
   */
  closeOnAnchorMouseDown?: boolean;
  /**
   * Defines whether the popup should be closed on a mouse click outside of the popup. Default is true.
   */
  closeOnMouseDownOutside?: boolean;
  /**
   * Defines whether the popup should be closed whenever another popup opens. Default is true.
   */
  closeOnOtherPopupOpen?: boolean;
  /**
   * Defines whether the popup should behave like a modal form. If true, the properties closeOnAnchorMouseDown, closeOnMouseDownOutside
   * and closeOnOtherPopupOpen ore overruled and set to false. The property withGlassPane is overruled too and set to true.
   * Default is false.
   */
  modal?: boolean;
  location?: {
    x?: number;
    y?: number;
  };
  $anchor?: JQuery;
  anchor?: Widget;
}
