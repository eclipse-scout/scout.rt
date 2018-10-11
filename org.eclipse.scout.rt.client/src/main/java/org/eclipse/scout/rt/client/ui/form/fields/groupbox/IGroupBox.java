/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.groupbox;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.notification.INotification;
import org.eclipse.scout.rt.platform.util.TriState;

public interface IGroupBox extends ICompositeField, IContextMenuOwner {

  /**
   * {@link String}
   */
  String PROP_SUB_LABEL = "subLabel";

  /**
   * {@link Boolean}
   */
  String PROP_BORDER_VISIBLE = "borderVisible";
  /**
   * {@link Boolean}
   */
  String PROP_EXPANDABLE = "expandable";
  /**
   * {@link Boolean}
   */
  String PROP_EXPANDED = "expanded";
  /**
   * {@link String}
   */
  String PROP_BORDER_DECORATION = "borderDecoration";
  /**
   * {@link String}
   */
  String PROP_BACKGROUND_IMAGE_NAME = "backgroundImageName";
  /**
   * {@link Integer}
   */
  String PROP_BACKGROUND_IMAGE_HORIZONTAL_ALIGNMENT = "backgroundImageHorizontalAlignment";
  /**
   * {@link Integer}
   */
  String PROP_BACKGROUND_IMAGE_VERTICAL_ALIGNMENT = "backgroundImageVerticalAlignment";
  /**
   * {@link Integer}
   */
  String PROP_MIN_WIDTH_IN_PIXEL = "minWidthInPixel";
  /**
   * {@link TriState}
   */
  String PROP_SCROLLABLE = "scrollable";

  String BORDER_DECORATION_EMPTY = "empty";
  String BORDER_DECORATION_LINE = "line";
  /**
   * automatic border decoration
   * <ul>
   * <li>MainBox (IGroupBox#isMainBox()) has no border</li>
   * <li>groupbox inside tab box (IGroupBox#isMainBox()) has no border</li>
   * <li>all others use line border</li>
   * </ul>
   */
  String BORDER_DECORATION_AUTO = "auto";

  /**
   * {@link IContextMenu}
   */
  String PROP_CONTEXT_MENU = "contextMenu";

  /**
   * Only implemented for groupboxes inside a tabbox.
   */
  String PROP_SELECTION_KEYSTROKE = "selectionKeystroke";

  /**
   * {@link Integer}
   */
  String PROP_GRID_COLUMN_COUNT = "gridColumnCount";

  String PROP_BODY_LAYOUT_CONFIG = "bodyLayoutConfig";

  /**
   * {@link INotification}
   */
  String PROP_NOTIFICATION = "notification";

  /**
   * {@link String}
   */
  String PROP_MENU_BAR_POSITION = "menuBarPosition";

  /**
   * Default position of the menubar of the {@link IGroupBox}.
   */
  String MENU_BAR_POSITION_AUTO = "auto";

  /**
   * Render an {@link IGroupBox} with the menubar at the top.
   */
  String MENU_BAR_POSITION_TOP = "top";

  /**
   * Render an {@link IGroupBox} with the menuBar at the bottom.
   */
  String MENU_BAR_POSITION_BOTTOM = "bottom";

  /**
   * {@link String}
   */
  String PROP_MENU_BAR_ELLIPSIS_POSITION = "menuBarEllipsisPosition";

  /**
   * Render an {@link IGroupBox} with the ellipsis in the menubar on the left side.
   */
  String MENU_BAR_ELLIPSIS_POSITION_LEFT = "left";

  /**
   * Render an {@link IGroupBox} with the ellipsis in the menubar on the right side.
   */
  String MENU_BAR_ELLIPSIS_POSITION_RIGHT = "right";

  /*
   * Runtime
   */

  String getSubLabel();

  void setSubLabel(String subLabel);

  /**
   * The index of the groupbox in the parent box
   */
  int getGroupBoxIndex(IGroupBox groupBox);

  int getGroupBoxCount();

  int getCustomProcessButtonCount();

  int getSystemProcessButtonCount();

  /**
   * fields excluding process buttons
   */
  List<IFormField> getControlFields();

  List<IGroupBox> getGroupBoxes();

  /**
   * buttons with processButton=true and systemType=none
   */
  List<IButton> getCustomProcessButtons();

  /**
   * buttons with processButton=true and systemType<>none
   */
  List<IButton> getSystemProcessButtons();

  @Override
  IFormFieldContextMenu getContextMenu();

  boolean isMainBox();

  void setMainBox(boolean b);

  boolean isBorderVisible();

  void setBorderVisible(boolean b);

  /**
   * When borderEnabled=true: borderDecoration="line" shows a line decoration around box, borderDecoration="none" or
   * null shows just insets.<br>
   * Other custom borderDecorations are possible and must be handled in the appropriate GUI factory.
   * <p>
   * Note that this is just the style of the border. To define a section group with expand/collapse, use
   * {@link #getConfiguredExpandable()}, {@link #isExpandable()} and {@link #isExpanded()}<br>
   */
  String getBorderDecoration();

  void setBorderDecoration(String s);

  String getBackgroundImageName();

  void setBackgroundImageName(String imageName);

  int getBackgroundImageVerticalAlignment();

  void setBackgroundImageVerticalAlignment(int a);

  int getBackgroundImageHorizontalAlignment();

  void setBackgroundImageHorizontalAlignment(int a);

  /**
   * column count for this composite box<br>
   * see {@value #GRID_COLUMN_COUNT_INHERITED} for inherited column count
   */
  int getGridColumnCount();

  /**
   * @param c
   *          column count for this composite box<br>
   *          see {@value #GRID_COLUMN_COUNT_INHERITED} for inherited column count
   */
  void setGridColumnCount(int c);

  void setBodyLayoutConfig(LogicalGridLayoutConfig config);

  LogicalGridLayoutConfig getBodyLayoutConfig();

  TriState isScrollable();

  /**
   * Setter for {@link AbstractGroupBox#getConfiguredScrollable()}
   * <p>
   * Only changes made before the groupbox is rendered have an effect.
   */
  void setScrollable(TriState scrollable);

  /**
   * Calls {@link #setScrollable(TriState)}
   */
  void setScrollable(boolean scrollable);

  /**
   * @return true if the group <i>can</i> be collapsed see {@link #isExpanded()} to see if border is effectively
   *         expanded or collapsed
   */
  boolean isExpandable();

  void setExpandable(boolean b);

  /**
   * @return true if the group is expanded
   */
  boolean isExpanded();

  void setExpanded(boolean b);

  INotification getNotification();

  /**
   * Adds a {@link INotification} to the group box. To remove the notification set it to {@code null} or use
   * {@link #removeNotification()}
   */
  void setNotification(INotification notification);

  /**
   * Removes the notification from the group box
   */
  void removeNotification();

  String getSelectionKeyStroke();

  void setSelectionKeyStroke(String keystroke);

  /**
   * @see IGroupBox#setMinWidthInPixel(int)
   * @deprecated will be removed with 8.0, use {@link #getBodyLayoutConfig()} instead
   */
  @Deprecated
  int getMinWidthInPixel();

  /**
   * Setter for {@link AbstractGroupBox#getConfiguredMinWidthInPixel()}
   * <p>
   * Only changes made before the groupbox is rendered have an effect.
   *
   * @deprecated will be removed with 8.0, use {@link #setBodyLayoutConfig(LogicalGridLayoutConfig)} instead
   */
  @Deprecated
  void setMinWidthInPixel(int minWidthInPixel);

  IGroupBoxUIFacade getUIFacade();

  /**
   * @return the value to control the menuBar position of this {@link IGroupBox}.
   * @see #MENU_BAR_POSITION_AUTO
   * @see #MENU_BAR_POSITION_TOP
   * @see #MENU_BAR_POSITION_BOTTOM
   */
  String getMenuBarPosition();

  /**
   * Set the given value to control the menuBar position of this {@link IGroupBox}.
   * <ul>
   * <li>{@link #MENU_BAR_POSITION_AUTO}</li>
   * <li>{@link #MENU_BAR_POSITION_TOP}</li>
   * <li>{@link #MENU_BAR_POSITION_BOTTOM}</li>
   * </ul>
   */
  void setMenuBarPosition(String menuBarPosition);

  /**
   * @return the value to control the menuBar ellipsis position of this {@link IGroupBox}.
   * @see #MENU_BAR_ELLIPSIS_POSITION_LEFT
   * @see #MENU_BAR_ELLIPSIS_POSITION_RIGHT
   */
  String getMenuBarEllipsisPosition();

  /**
   * Set the given value to control the menuBar ellipsis position of this {@link IGroupBox}.
   * <ul>
   * <li>{@link #MENU_BAR_ELLIPSIS_POSITION_LEFT}</li>
   * <li>{@link #MENU_BAR_ELLIPSIS_POSITION_RIGHT}</li>
   * </ul>
   */
  void setMenuBarEllipsisPosition(String menuBarEllipsisPosition);

}
