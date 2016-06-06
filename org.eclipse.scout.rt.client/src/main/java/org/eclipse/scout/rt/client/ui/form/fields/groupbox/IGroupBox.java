/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.platform.util.TriState;

public interface IGroupBox extends ICompositeField, IContextMenuOwner {

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
   * @deprecated This decoration type is obsolete, do not use it anymore. This constant will be removed in Scout 6.1. To
   *             apply the "section style" to a group box, set its "expandable" property to <code>true</code> and set a
   *             decoration style that supports expandable group boxes (e.g. {@link #BORDER_DECORATION_LINE}). In most
   *             cases, {@link #BORDER_DECORATION_AUTO} is also acceptable. (Thus, the easiest way to migrate is
   *             probably to just remove your <code>getConfiguredBorderDecoration()</code> declaration, thus falling
   *             back to the default BORDER_DECORATION_AUTO).
   */
  @Deprecated
  String BORDER_DECORATION_SECTION = "section";

  /**
   * {@link IContextMenu}
   */
  String PROP_CONTEXT_MENU = "contextMenu";

  /*
   * Runtime
   */

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
   * @return
   */
  IGroupBoxBodyGrid getBodyGrid();

  /**
   * column count for this composite box<br>
   * see {@value #GRID_COLUMN_COUNT_INHERITED} for inherited column count
   */
  int getGridColumnCountHint();

  /**
   * @param c
   *          column count for this composite box<br>
   *          see {@value #GRID_COLUMN_COUNT_INHERITED} for inherited column count
   */
  void setGridColumnCountHint(int c);

  TriState isScrollable();

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

  IGroupBoxUIFacade getUIFacade();

}
