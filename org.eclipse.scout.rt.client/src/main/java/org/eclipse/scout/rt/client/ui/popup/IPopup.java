/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.popup;

import org.eclipse.scout.rt.client.ui.IWidget;

/**
 * Root interface for a popup.
 *
 * @since 9.0
 */
public interface IPopup extends IWidget {

  String PROP_ANCHOR = "anchor";
  String PROP_ANIMATE_OPENING = "animateOpening";
  String PROP_ANIMATE_RESIZE = "animateResize";
  String PROP_WITH_GLASS_PANE = "withGlassPane";
  String PROP_SCROLL_TYPE = "scrollType";
  String PROP_HORIZONTAL_ALIGNMENT = "horizontalAlignment";
  String PROP_VERTICAL_ALIGNMENT = "verticalAlignment";
  String PROP_WITH_ARROW = "withArrow";

  String PROP_TRIM_WIDTH = "trimWidth";
  String PROP_TRIM_HEIGHT = "trimHeight";

  String PROP_HORIZONTAL_SWITCH = "horizontalSwitch";
  String PROP_VERTICAL_SWITCH = "verticalSwitch";

  String PROP_CLOSE_ON_ANCHOR_MOUSE_DOWN = "closeOnAnchorMouseDown";
  String PROP_CLOSE_ON_MOUSE_DOWN_OUTSIDE = "closeOnMouseDownOutside";
  String PROP_CLOSE_ON_OTHER_POPUP_OPEN = "closeOnOtherPopupOpen";

  String SCROLL_TYPE_POSITION = "position";
  String SCROLL_TYPE_LAYOUT_AND_POSITION = "layoutAndPosition";
  String SCROLL_TYPE_REMOVE = "remove";

  /**
   * The entire popup is positioned horizontally left of the anchor.
   */
  String POPUP_ALIGNMENT_LEFT = "left";
  /**
   * With arrow: The arrow at the left edge of the popup is aligned horizontally with the center of the anchor.
   * <p>
   * Without arrow: The left edges of both the popup and the anchor are aligned horizontally.
   */
  String POPUP_ALIGNMENT_LEFTEDGE = "leftedge";
  /**
   * The entire popup is positioned vertically above the anchor.
   */
  String POPUP_ALIGNMENT_TOP = "top";
  /**
   * With arrow: The arrow at the top edge of the popup is aligned vertically with the center of the anchor.
   * <p>
   * Without arrow: The top edges of both the popup and the anchor are aligned vertically.
   */
  String POPUP_ALIGNMENT_TOPEDGE = "topedge";
  /**
   * The centers of both the popup and the anchor are aligned in the respective dimension.
   */
  String POPUP_ALIGNMENT_CENTER = "center";
  /**
   * The entire popup is positioned horizontally to the right of the anchor.
   */
  String POPUP_ALIGNMENT_RIGHT = "right";
  /**
   * With arrow: The arrow at the right edge of the popup is aligned horizontally with the center of the anchor.
   * <p>
   * Without arrow: The right edges of both the popup and the anchor are aligned horizontally.
   */
  String POPUP_ALIGNMENT_RIGHTEDGE = "rightedge";
  /**
   * The entire popup is positioned vertically below the anchor.
   */
  String POPUP_ALIGNMENT_BOTTOM = "bottom";
  /**
   * With arrow: The arrow at the bottom edge of the popup is aligned vertically with the center of the anchor.
   * <p>
   * Without arrow: The bottom edges of both the popup and the anchor are aligned vertically.
   */
  String POPUP_ALIGNMENT_BOTTOMEDGE = "bottomedge";

  void setAnchor(IWidget anchor);

  IWidget getAnchor();

  void setAnimateOpening(boolean animateOpening);

  boolean isAnimateOpening();

  void setAnimateResize(boolean nnimateResize);

  boolean isAnimateResize();

  /**
   * Note: setting this property while the popup is open has no effect.
   */
  void setWithGlassPane(boolean withGlassPane);

  boolean isWithGlassPane();

  /**
   * Note: setting this property while the popup is open has no effect.
   */
  void setScrollType(String scrollType);

  String getScrollType();

  void setTrimWidth(boolean trimWidth);

  boolean isTrimWidth();

  void setTrimHeight(boolean trimHeight);

  boolean isTrimHeight();

  void setHorizontalAlignment(String horizontalAlignment);

  String getHorizontalAlignment();

  void setVerticalAlignment(String verticalAlignment);

  String getVerticalAlignment();

  void setWithArrow(boolean withArrow);

  boolean isWithArrow();

  void setHorizontalSwitch(boolean horizontalSwitch);

  boolean isHorizontalSwitch();

  void setVerticalSwitch(boolean verticalSwitch);

  boolean isVerticalSwitch();

  /**
   * Note: setting this property while the popup is open has no effect.
   */
  void setCloseOnAnchorMouseDown(boolean closeOnAnchorMouseDown);

  boolean isCloseOnAnchorMouseDown();

  /**
   * Note: setting this property while the popup is open has no effect.
   */
  void setCloseOnMouseDownOutside(boolean closeOnMouseDownOutside);

  boolean isCloseOnMouseDownOutside();

  /**
   * Note: setting this property while the popup is open has no effect.
   */
  void setCloseOnOtherPopupOpen(boolean closeOnOtherPopupOpen);

  boolean isCloseOnOtherPopupOpen();

  void open();

  void close();

  IPopupUIFacade getUIFacade();
}
